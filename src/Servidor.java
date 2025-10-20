import java.util.HashSet;
import java.util.Set;

import org.apache.xmlrpc.server.PropertyHandlerMapping; // Registra mapeos entre nombres de servicios (strings) y clases Java
import org.apache.xmlrpc.server.XmlRpcServer; // Clase principal que maneja la lógica del servidor RPC basado en XML
import org.apache.xmlrpc.webserver.WebServer; // Servidor web embebido simple que puede escuchar peticiones HTTP en un puerto específico
import java.io.File;

public class Servidor extends XmlRpcServer{
    private static final int PORT = 8080; // Puerto en el que el servidor escuchará las conexiones
    private Set<String> clientesRegistrados = new HashSet<>();

    public synchronized void registrarCliente(String nombreCliente) {
        if(clientesRegistrados.contains(nombreCliente)) {
            return; // El cliente ya está registrado
        }
        clientesRegistrados.add(nombreCliente);
        String ruta = nombreCliente + "_procesos.txt"; // Ruta del archivo para el cliente
        File archivo = new File(ruta);
        if (!archivo.exists()) { // Si el archivo no existe, lo crea, aqui guardara los procesos del cliente
            try {
                archivo.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Metodo que se llamara cuando un el servidor se cierre
    private void Cierre() {
        for (String cliente : clientesRegistrados) {
            String ruta = cliente + "_procesos.txt";
            File fichero = new File(ruta);

            if (fichero.delete())
                System.out.println("El fichero " + ruta + " ha sido borrado satisfactoriamente");
            else
                System.out.println("El fichero " + ruta + " no pudó ser borrado");
        }
    }

    public static void main(String[] args) throws Exception {

        

        System.out.println("Iniciando servidor XML-RPC en el puerto " + PORT + "...");

        // Crea un nuevo servidor web que escuchará en el puerto especificado
        WebServer webServer = new WebServer(PORT);
        // Obtiene la instancia del servidor XML-RPC asociado con el servidor web
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
        // Crea un mapeo de manejadores para registrar las clases que contendrán los métodos RPC
        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        phm.setVoidMethodEnabled(true); // Permite métodos que no retornan valor

        // Registra la clase 'Servidor' bajo el nombre 'MiServidor'.
        // Esto significa que los métodos públicos de la clase 'Servidor' podrán ser invocados remotamente
        // prefijando el nombre del método con "MiServidor.". Por ejemplo, "MiServidor.sumar".
        phm.addHandler("Servidor", Servidor.class);

        phm.addHandler("Manejador", ManejoDeProcesos.class);
        //phm.addHandler("Proceso", Proceso.class);
        // Establece el mapeo de manejadores en el servidor XML-RPC
        xmlRpcServer.setHandlerMapping(phm);

        // Inicia el servidor web, haciéndolo disponible para recibir peticiones
        webServer.start();

        System.out.println("Servidor iniciado exitosamente. Esperando peticiones...");
            
        /*
        Opcion: Mostrar graficas de los procesos
        String ruta = "Todos_procesos.txt";
        mostrar(ruta); 
        */

    }

    
    
}