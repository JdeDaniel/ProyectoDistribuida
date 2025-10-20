import java.util.LinkedList;
import java.util.Queue;


public class RegistroProceso {
    
    private static Queue <Proceso> Procesos = new LinkedList<>();
    private ManejoDeProcesos manejo = new ManejoDeProcesos();

    public RegistroProceso(){

    }

    public synchronized void IngresarProceso(int Duracion, String Nombre, String Cliente){
        Proceso nuevoProceso = new Proceso(Duracion, Nombre, Cliente);
        Procesos.offer(nuevoProceso);
        manejo.actualizarProcesos(Procesos);
    }

}
