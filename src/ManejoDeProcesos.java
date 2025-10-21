import java.util.LinkedList;
import java.util.Queue;
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

    // --- admisión FIFO ---
    public void Espera() {
        Proceso p = procesos.poll();
        if (p == null) return;

        // Si ya traía C, respétalo; si no, asígnalo al llegar
        if (p.getCreacion() == null) p.setCreacion(tick);

        // Si usas buffer de admisión
        if (bufferCap - bufferUsed >= p.getDuracion()) {
            enEspera.offer(p);
            bufferUsed += p.getDuracion();
            System.out.println("Admitido " + p.getNombre() + " en C=" + p.getCreacion());
        } else {
            rechazados.offer(p);
            System.out.println("Rechazado " + p.getNombre() + " en tick " + tick);
        }
    }


    // --- reintento de rechazados cada 3 ticks ---
    private int contadorRechazo = 0;
    public void Rechazo() {
        if (rechazados.isEmpty()) return;
        if (++contadorRechazo < 3) return;
        contadorRechazo = 0;

        Proceso p = rechazados.peek();
        if (p == null) return;

        if (bufferCap - bufferUsed >= p.getDuracion()) {
            rechazados.poll();
            // reingreso: C debe ser el instante de admisión real
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
        }
    }
    
    // --- ejecución no expropiativa: uno a la vez ---
    private Proceso running = null;

    public void Ejecucion() {
        if (running == null) {
            running = enEspera.peek();
            if (running == null) return;

            // Primera vez que corre
            if (running.getInicio() == null) {
                running.setInicio(tick);
                running.setEnEspera(tick - running.getCreacion()); // E = S - C
                System.out.println("Inicio " + running.getNombre() + " S=" + running.getInicio() + " E=" + running.getEnEspera());
            }
        }

        // Progreso: ejecutamos 1 unidad por tick
        int ejecutado = tick - running.getInicio(); // tiempo ejecutado desde S
        int restante = running.getDuracion() - ejecutado;
        System.out.println("Tick " + tick + " ejecutando " + running.getNombre() + " restante=" + Math.max(restante,0));

        if (restante <= 0) {
            Terminado(running);
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
                Espera();
                Rechazo();
                Ejecucion();
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
    
}
