import java.util.LinkedList;
import java.util.Queue;

public class RegistroProceso {
    public RegistroProceso(){}

    // RPC: Manejador.IngresarProceso(int Duracion, String Nombre, String Cliente, Integer InicioDeseado)
    public synchronized void IngresarProceso(int Duracion, String Nombre, String Cliente, Integer InicioDeseado){
        Proceso p = new Proceso(Duracion, Nombre, Cliente, InicioDeseado);
        // No setees C aquí; se asigna al admitir (tick actual) en ManejoDeProcesos
        Servidor.getScheduler().submit(p);
    }
}
