import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;



public class ManejoDeProcesos extends Thread {
    
    // Reemplaza campos estáticos por de instancia (mejor) o usa todos ConcurrentLinkedQueue
    private final Queue<Proceso> enEspera = new ConcurrentLinkedQueue<>();
    private final Queue<Proceso> rechazados = new ConcurrentLinkedQueue<>();
    private final Queue<Proceso> procesos = new ConcurrentLinkedQueue<>();
    

    // Si mantienes el “buffer de admisión”, documenta:
    private int bufferCap = 10;   // capacidad total de t admitidos
    private int bufferUsed = 0;   // t admitido acumulado
    
   
    private int tick = 0;
    private volatile boolean ejecutando = true;

    // --- admisión FIFO en lote---
    private void admitirPendientes() {
        // Solo intentar admitir procesos cuyo "inicioDeseado" sea <= tick (o nulo)
        for (Proceso p : procesos) {
            Integer inicioDeseado = p.getInicioDeseado();
            if (inicioDeseado != null && tick < inicioDeseado) {
                // Aún no es su tick; saltar
                continue;
            }
            // Intentar admitir este proceso
            if (bufferCap - bufferUsed >= p.getDuracion()) {
                // remover y admitir
                if (procesos.remove(p)) {
                    if (p.getCreacion() == null) p.setCreacion(tick);
                    enEspera.offer(p);
                    bufferUsed += p.getDuracion();
                    System.out.println("Admitido " + p.getNombre() + " en C=" + p.getCreacion());
                }
            } else {
                // remover de cola de admisión y pasar a rechazados
                if (procesos.remove(p)) {
                    rechazados.offer(p);
                    System.out.println("Rechazado " + p.getNombre() + " en tick " + tick);
                }
            }
        }
    }


    //Rechazos por lote
    private int contadorRechazo = 0;
    private void reintentarRechazados() {
        if (++contadorRechazo < 3) return;
        contadorRechazo = 0;
        while (!rechazados.isEmpty()) {
            Proceso p = rechazados.peek();
            if (bufferCap - bufferUsed >= p.getDuracion()) {
                rechazados.poll();
                p.setCreacion(tick);
                enEspera.offer(p);
                bufferUsed += p.getDuracion();
                System.out.println("Reingresado " + p.getNombre() + " en C=" + p.getCreacion());
            } else {
                if (p.getIntentos() >= 3) {
                    rechazados.poll();
                    System.out.println("Descartado definitivo " + p.getNombre());
                } else {
                    p.subirIntentos();
                }
                break; // no hay más cupo, sal
            }
        }
    }

    
    // --- ejecución no expropiativa: uno a la vez ---
    private Proceso running = null;

    private void ejecutarUnTick() {
        if (running == null) {
            running = enEspera.peek();
            if (running == null) return; // CPU ociosa
            if (running.getInicio() == null) {
                running.setInicio(tick);
                running.setEnEspera(tick - running.getCreacion()); // E = S - C
                System.out.println("Inicio " + running.getNombre() + " S=" + running.getInicio());
            }
        }
        int ejecutado = tick - running.getInicio() + 1;   // consumimos esta unidad
        int restante  = running.getDuracion() - ejecutado;
        System.out.println("Tick " + tick + " ejecutando " + running.getNombre() + " restante=" + Math.max(restante,0));
        if (restante <= 0) {
            Terminado(running);     // F = t + E, P = F/t; libera bufferUsed
            enEspera.poll();
            running = null;
        }
    }


    // --- fin de proceso: calcula F, P coherentes y escribe archivos ---
    public void Terminado(Proceso p) {
        // E ya está; F = t + E; P = F / t
        int E = p.getEnEspera();
        int F = p.getDuracion() + E;
        p.setFinalizacion(F);
        p.setPenalizacion((double) F / (double) p.getDuracion());

        // liberar del buffer de admisión (si lo usas)
        bufferUsed -= p.getDuracion();
        if (bufferUsed < 0) bufferUsed = 0;

        String linea = String.format(
            "Proceso: %s, Duracion: %d, Creacion: %d, En Espera: %d, Inicio: %d, Finalizacion: %d, Penalizacion: %.2f, Cliente: %s%n",
            p.getNombre(), p.getDuracion(), p.getCreacion(), p.getEnEspera(),
            p.getInicio(), p.getFinalizacion(), p.getPenalizacion(), p.getCliente()
        );

        escribirLinea(p.getCliente() + "_procesos.txt", linea);
        escribirLinea("Todos_procesos.txt", linea);
        System.out.println("Terminado " + p.getNombre() + " F=" + F + " P=" + String.format("%.2f", p.getPenalizacion()));
    }

    private void escribirLinea(String ruta, String linea){
        try (BufferedWriter w = new BufferedWriter(new FileWriter(ruta, true))) {
            w.write(linea);
            w.flush();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    

    
    // --- bucle principal ---
    @Override
    public void run() {
        ejecutando = true;
        while (ejecutando) {
            try {
                Thread.sleep(300);      // 1 tick
                admitirPendientes();        // 1) llenar READY hasta bufferCap
                reintentarRechazados();     // 2) reintentos cuando toque
                ejecutarUnTick();           // 3) consumir 1 unidad de CPU
                tick++;

                if (procesos.isEmpty() && enEspera.isEmpty() && rechazados.isEmpty() && running == null) {
                    ejecutando = false;
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    

    // API de entrada
    public void actualizarProcesos(Queue<Proceso> nuevos){
        procesos.clear();
        procesos.addAll(nuevos);
    }
    
    public void submit(Proceso p) {
        // solo encola para admisión; C se fija al admitir
        // usa la cola 'procesos' de instancia (no estática)
        procesos.offer(p);
    }
    
    // --- getters para UI / monitorización ---
    public List<Proceso> getEnEsperaSnapshot() {
        return new ArrayList<>(enEspera);
    }

    public List<Proceso> getRechazadosSnapshot() {
        return new ArrayList<>(rechazados);
    }

    public int getTick() { return tick; }
    
    // Devuelve el proceso en ejecución (si hay)
    public Proceso getRunning() { return running; }
    
}
