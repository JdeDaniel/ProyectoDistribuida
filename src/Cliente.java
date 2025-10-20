import org.apache.xmlrpc.client.XmlRpcClient;   // Importa la clase que permite crear un cliente XML-RPC para enviar peticiones al servidor
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl; // Importa la clase para configurar las opciones del cliente XML-RPC, como la URL del servidor

import java.net.URI;
//import java.net.URL;   // Importa la clase URL que representa la dirección a la cual el cliente se conectará
import java.util.Arrays;    // Importa la clase Arrays para trabajar con arreglos, útil para pasar parámetros o manipular datos

public class Cliente {
    public static int ProcesoID = 0;
    
    public static void main(String[] args) {
        try {
            String NombreCliente = "ClienteA";

            // Configuración del cliente XML-RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            URI uri = new URI("http://192.168.1.68:8080/");  // URL del servidor XML-RPC
            config.setServerURL(uri.toURL()); 
            config.setEnabledForExtensions(true); // Permite valores null

            

            //config.setBasicUserName(NombreCliente); // Nombre de usuario para autenticación básica");


            // Creación del cliente XML-RPC
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            // Registro del cliente en el servidor
            Object[] param = new Object[]{NombreCliente}; // Parámetros para el método remoto
            client.execute("Servidor.registrarCliente", param); // Llamada para registrar el cliente

            while (true) {
                System.out.println("Presione Enter para enviar un nuevo proceso al servidor o 'q' para salir...");
                String entrada = System.console().readLine();
                if (entrada.equalsIgnoreCase("q")) {
                    System.out.println("Saliendo del cliente.");
                    break;
                }
                ProcesoID++;
                String NombreProceso = "Proceso" + ProcesoID;
                int Duracion = (int) (Math.random() * 10) + 1; // Duración aleatoria entre 1 y 10
                System.out.println("Enviando " + NombreProceso + " con duración " + Duracion + " al servidor...");
                Object[] params = new Object[]{Duracion, NombreProceso, NombreCliente}; // Parámetros para el método remoto
                client.execute("Manejador.IngresarProceso", Arrays.asList(params)); // Llamada al método remoto
                
            }

        } catch (Exception e) {
            e.printStackTrace(); // Mane
        }
    }
    
}
