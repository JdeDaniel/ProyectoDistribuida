import java.util.HashSet;
import java.util.Set;

import org.apache.xmlrpc.server.PropertyHandlerMapping; // Registra mapeos entre nombres de servicios (strings) y clases Java
import org.apache.xmlrpc.server.XmlRpcServer; // Clase principal que maneja la lógica del servidor RPC basado en XML
import org.apache.xmlrpc.webserver.WebServer; // Servidor web embebido simple que puede escuchar peticiones HTTP en un puerto específico
import java.io.File;
import org.apache.xmlrpc.XmlRpcConfigImpl;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;

public class Servidor{
    //private static final int PORT = 8080; // Puerto en el que el servidor escuchará las conexiones
    private static Set<String> clientesRegistrados = new HashSet<>();

    public Servidor() {
    }

    public void registrarCliente(String nombreCliente) {
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
    private static void Cierre() {
        for (String cliente : clientesRegistrados) {
            String ruta = cliente + "_procesos.txt";
            File fichero = new File(ruta);

            if (!fichero.delete())
                System.out.println("El fichero " + ruta + " no pudó ser borrado");
        }
        File fichero = new File("Todos_procesos.txt");
        if (!fichero.delete())
            System.out.println("El fichero Todos_procesos.txt no pudó ser borrado");
    }

    public static void main(String[] args) throws Exception {

        try{
            int PORT = 8080; // Puerto en el que el servidor escuchará las conexiones
            Graficas grafica;
            ManejoDeProcesos manejo = new ManejoDeProcesos();

            System.out.println("Iniciando servidor XML-RPC en el puerto " + PORT + "...");

            // Crea un nuevo servidor web que escuchará en el puerto especificado
            WebServer webServer = new WebServer(PORT);
            // Obtiene la instancia del servidor XML-RPC asociado con el servidor web
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
            XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
            serverConfig.setEnabledForExtensions(true); //Permite valores null
            serverConfig.setContentLengthOptional(false);

            // Crea un mapeo de manejadores para registrar las clases que contendrán los métodos RPC
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.setVoidMethodEnabled(true); // Permite métodos que no retornan valor

            // Registra la clase 'Servidor' bajo el nombre 'MiServidor'.
            // Esto significa que los métodos públicos de la clase 'Servidor' podrán ser invocados remotamente
            // prefijando el nombre del método con "MiServidor.". Por ejemplo, "MiServidor.sumar".
            phm.addHandler("Servidor", Servidor.class);
            phm.addHandler("Manejador", RegistroProceso.class);
            //phm.addHandler("Proceso", Proceso.class);
            // Establece el mapeo de manejadores en el servidor XML-RPC
            xmlRpcServer.setHandlerMapping(phm);

            // Inicia el servidor web, haciéndolo disponible para recibir peticiones
            webServer.start();

            System.out.println("Servidor iniciado exitosamente. Esperando peticiones...");

            while (true) {
                System.out.println("Menu del servidor:");
                System.out.println("1. Imprimir Procesos de clientes");
                System.out.println("2. Comenzar Manejo de Procesos");
                System.out.println("3. Cerrar servidor");
                System.out.print("Seleccione una opción: ");
                int opcion = new java.util.Scanner(System.in).nextInt();
                switch (opcion) {
                    case 1:
                        //Opcion: Mostrar graficas de los procesos
                        String ruta = "Todos_procesos.txt";
                        grafica = new Graficas(ruta);
                        grafica.mostrar(ruta);
                        break;
                    case 2:
                        System.out.println("Comenzando manejo de procesos...");
                        manejo.run();
                        break;
                    case 3:
                        System.out.println("Cerrando servidor...");
                        Cierre();
                        webServer.shutdown();
                        System.out.println("Servidor cerrado.");
                        System.exit(0);
                        break; 
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
        
            
        /*
        Opcion: Mostrar graficas de los procesos
        String ruta = "Todos_procesos.txt";
        mostrar(ruta); 
        */

    }

    
    
}