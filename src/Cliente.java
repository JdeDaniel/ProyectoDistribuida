import org.apache.xmlrpc.client.XmlRpcClient;   // Importa la clase que permite crear un cliente XML-RPC para enviar peticiones al servidor
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl; // Importa la clase para configurar las opciones del cliente XML-RPC, como la URL del servidor

import java.net.URL;   // Importa la clase URL que representa la dirección a la cual el cliente se conectará
import java.util.Arrays;    // Importa la clase Arrays para trabajar con arreglos, útil para pasar parámetros o manipular datos

public class Cliente {
    public static int ProcesoID = 0;

    public static void main(String[] args) {
        try {
            // Configuración del cliente XML-RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8080/")); // URL del servidor XML-RPC

            String NombreCliente = "ClienteA";

            config.setBasicUserName(NombreCliente); // Nombre de usuario para autenticación básica");


            // Creación del cliente XML-RPC
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            // Ejemplo de llamada remota al método 'IngresarProceso' del servidor
            Object[] params = new Object[]{5, "Proceso1", NombreCliente}; // Parámetros para el método remoto
            
            client.execute("Servidor.registrarCliente", Arrays.asList(NombreCliente)); // Llamada para registrar el cliente
            client.execute("Manejador.IngresarProceso", Arrays.asList(params)); // Llamada al método remoto

        } catch (Exception e) {
            e.printStackTrace(); // Mane
        }
    }
    
}
