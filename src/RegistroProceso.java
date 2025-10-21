import java.util.LinkedList;
import java.util.Queue;

public class RegistroProceso {
    public RegistroProceso(){}

    // RPC: Manejador.IngresarProceso(int Duracion, String Nombre, String Cliente)
    public synchronized void IngresarProceso(int Duracion, String Nombre, String Cliente){
        Proceso p = new Proceso(Duracion, Nombre, Cliente);
        // No setees C aquí; se asigna al admitir (tick actual) en ManejoDeProcesos
        Servidor.getScheduler().submit(p);
    }
}
