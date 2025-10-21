import java.util.LinkedList;
import java.util.Queue;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;



public class ManejoDeProcesos extends Thread {
    
    //private static Queue<Proceso> EnEspera = new LinkedList<>();
    private static Queue<Proceso> EnEspera = new ConcurrentLinkedQueue<>();
    private static Queue<Proceso> Rechazados = new LinkedList<>();
    private static Queue <Proceso> Procesos = new LinkedList<>();
    //private Queue <Proceso> Ejecucion = new LinkedList<>();
    private static int QuantusPosibles = 10;
    private static int QuantusUsados = 0;
    private static int QuantuActual = 0;
    private static int IntentoRechazo = 0;

    public ManejoDeProcesos(){

    }

    /*
    public synchronized void IngresarProceso(int Duracion, String Nombre, String Cliente){
        Proceso nuevoProceso = new Proceso(Duracion, Nombre, Cliente);
        Procesos.offer(nuevoProceso);
    }
    */

    public void actualizarProcesos(Queue<Proceso> nuevosProcesos){
        Procesos = nuevosProcesos;
    }

    //Metodo que se llamara cada quantum para actualizar los procesos en espera
    public void Espera(){
        if(Procesos.isEmpty()){ //No hay procesos nuevos
            return;
        }
        Proceso procesoActual = (Proceso) Procesos.poll(); //Obtenemos el proceso al frente de la cola
        if(QuantusPosibles - QuantusUsados >= procesoActual.getDuracion()){ //Si hay espacio para el proceso entra a espera
            System.out.println("Proceso " + procesoActual.getNombre() + " ha entrado en espera en el quantum " + QuantuActual);
            procesoActual.setCreacion(QuantuActual);
            EnEspera.offer(procesoActual);
            QuantusUsados += procesoActual.getDuracion();
        } else { //Si no hay espacio se rechaza el 
            System.out.println("Proceso " + procesoActual.getNombre() + " ha sido rechazado en el quantum " + QuantuActual);
            Rechazados.offer(procesoActual);
        }
    }

    //Metodo que se llamara cada quantum para revisar los procesos rechazados
    public void Rechazo(){
        if(!Rechazados.isEmpty()){ //Si hay procesos rechazados
            IntentoRechazo++; //Aumentamos el contador de tiempo para reintento
            if(IntentoRechazo >= 3){ 
                IntentoRechazo = 0; //Reiniciamos el contador

                Proceso procesoRechazado = (Proceso) Rechazados.peek(); //Obtenemos el proceso rechazado al frente de la cola
                if (QuantusPosibles - QuantusUsados > procesoRechazado.getDuracion()){ //Si hay espacio para el proceso entra a espera 
                    System.out.println("Proceso " + procesoRechazado.getNombre() + " ha entrado en espera en el quantum " + QuantuActual);     
                    procesoRechazado.setCreacion(QuantuActual);
                    EnEspera.offer(procesoRechazado);
                    QuantusUsados += procesoRechazado.getDuracion();
                    Rechazados.poll(); //Removemos el proceso de la cola de rechazados
                }else{
                    if(procesoRechazado.getIntentos() >= 3){ //Si ya se ha intentado 3 veces se elimina el proceso
                        System.out.println("Proceso " + procesoRechazado.getNombre() + " ha sido rechazado en el quantum " + QuantuActual);
                        Rechazados.poll();
                    } else {
                        procesoRechazado.subirIntentos(); //Aumentamos el contador de intentos del proceso
                    }
                }
            }
        }
    }
    
    //Metodo que se llamara cada quantum para actualizar el avance de los procesos en Ejecucion
    public void Ejecucion(){
        if(QuantusUsados > 0){
            System.out.println("Quantum " + QuantuActual + " en ejecucion. Quantus usados: " + QuantusUsados);
            QuantusUsados -= 1; //Reducimos el uso de quantus por el proceso en ejecucion
        }

        if(EnEspera.isEmpty()){ //No hay procesos en espera
            return;
        }

        Proceso procesoEjecucion = (Proceso) EnEspera.peek(); //Obtenemos el proceso al frente de la cola
        if(procesoEjecucion.getEnEspera() != null){
            System.out.println("Ejecutando proceso: " + procesoEjecucion.getNombre() + ", Duracion restante: " + (procesoEjecucion.getDuracion() - (QuantuActual - procesoEjecucion.getEnEspera() + procesoEjecucion.getCreacion())) );
        }
        
        if (procesoEjecucion.getEnEspera() == null){  //Si es la primera vez que se ejecuta el proceso 
            System.out.println("Proceso " + procesoEjecucion.getNombre() + " ha comenzado su ejecucion en el quantum " + QuantuActual);
            procesoEjecucion.setInicio(QuantuActual);
            procesoEjecucion.setEnEspera(QuantuActual - procesoEjecucion.getCreacion()); //Calculamos el tiempo en espera

        }
        //System.out.println("Tiempo de ejecucion del proceso " + procesoEjecucion.getNombre() + ": " + (QuantuActual - (procesoEjecucion.getEnEspera() + procesoEjecucion.getCreacion())));
        if(procesoEjecucion.getEnEspera() != null && (procesoEjecucion.getDuracion() == (QuantuActual - (procesoEjecucion.getEnEspera() + procesoEjecucion.getCreacion())))){ //Si el proceso ha terminado su duracion
            System.out.println("Proceso " + procesoEjecucion.getNombre() + " ha terminado su ejecucion en el quantum " + QuantuActual);
            Terminado(procesoEjecucion);
        }

    }

    //Metodo que se llamara cuando un proceso haya terminado su ejecucion
    public void Terminado(Proceso procesoFinalizado){
        procesoFinalizado.setFinalizacion(QuantuActual); //Calculamos el tiempo de finalizacion
        procesoFinalizado.setPenalizacion(procesoFinalizado.getFinalizacion()  / procesoFinalizado.getDuracion()); //Calculamos la penalizacion
        System.out.println("Proceso " + procesoFinalizado.getNombre() + " ha terminado en el quantum " + QuantuActual);
        String ruta = procesoFinalizado.getCliente() + "_procesos.txt"; // Ruta del archivo para el cliente
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) { // Escribimos el proceso en el archivo
            writer.write("Proceso: " + procesoFinalizado.getNombre() + ", Duracion: " + procesoFinalizado.getDuracion() + ", Creacion: " + procesoFinalizado.getCreacion() + ", En Espera: " + procesoFinalizado.getEnEspera()+ ", Inicio: " + procesoFinalizado.getInicio() + ", Finalizacion: " + procesoFinalizado.getFinalizacion() + ", Penalizacion: " + procesoFinalizado.getPenalizacion() + "\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ruta = "Todos_procesos.txt"; // Ruta del archivo general

        File archivo = new File(ruta);
        if (!archivo.exists()) { // Si el archivo no existe, lo crea, aqui guardara todos los procesos
            try {
                archivo.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) { // Escribimos el proceso en el archivo general
            writer.write("Proceso: " + procesoFinalizado.getCliente() + "_" + procesoFinalizado.getNombre() + ", Duracion: " + procesoFinalizado.getDuracion() + ", Creacion: " + procesoFinalizado.getCreacion() + ", En Espera: " + procesoFinalizado.getEnEspera() + ", Inicio: " + procesoFinalizado.getInicio() + ", Finalizacion: " + procesoFinalizado.getFinalizacion() + ", Penalizacion: " + procesoFinalizado.getPenalizacion() + "\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        EnEspera.poll(); //Removemos el proceso de la cola de espera
    }

    @Override
    public void run(){
        while(true){
            try {
                Thread.sleep(2000); //Simulamos un quantum de 1 segundo
                Espera(); //Llamamos al metodo de espera
                Thread.sleep(500); //Pequeña pausa para simular el tiempo entre metodos
                Rechazo(); //Llamamos al metodo de rechazo
                Thread.sleep(500); //Pequeña pausa para simular el tiempo entre metodos
                Ejecucion(); //Llamamos al metodo de ejecucion
                Thread.sleep(500); //Pequeña pausa para simular el tiempo entre metodos
                QuantuActual += 1; //Aumentamos el contador de quantums
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    

}
