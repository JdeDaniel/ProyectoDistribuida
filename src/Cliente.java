import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URI;
import java.util.Scanner;
import java.nio.file.*;
import java.io.*;
import java.util.Properties;

public class Cliente {
    public static int ProcesoID = 0;
    private static final Path CONF = Paths.get(System.getProperty("user.home"), ".ProyectoDistribuida.properties");

    public static void main(String[] args) {
        try {
            String url = (args.length > 0) ? args[0] : "http://127.0.0.1:8080/";

            // Config XML-RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URI(url).toURL());
            config.setEnabledForExtensions(true);
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            // Cargar estado local
            Properties props = loadProps();
            String clientId = props.getProperty("clientId");
            String nombreCliente = props.getProperty("name");

            // Registrar si no existe
            if (clientId == null || clientId.isEmpty()) {
                nombreCliente = pedirNombreInteractivo();
                clientId = (String) client.execute("Registro.registrar", new Object[]{ nombreCliente });

                props.setProperty("clientId", clientId);
                props.setProperty("name", nombreCliente);
                saveProps(props);

                System.out.println("Registrado con id: " + clientId + " nombre: " + nombreCliente);
            } else {
                System.out.println("Sesión previa: id=" + clientId + " nombre=" + nombreCliente);
            }

            // Mostrar conectados (opcional)
            Object[] lista = (Object[]) client.execute("Registro.listar", new Object[]{});
            System.out.println("Conectados:");
            for (Object s : lista) System.out.println("  " + s);
            
            
            // Loop de envío de procesos
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter = enviar proceso, 'q' = salir");
            while (true) {
                String entrada = sc.nextLine().trim();
                if (entrada.equalsIgnoreCase("q")) break;

                ProcesoID++;
                String nombreProceso = "Proceso " + ProcesoID;
                int duracion = 1 + (int)(Math.random() * 10); // 1..10

                // Pedir tick de inicio deseado al usuario
                Integer inicioDeseado = null;
                while (inicioDeseado == null) {
                    System.out.print("Tick inicio deseado (entero >=0, ENTER para 0): ");
                    String s = sc.nextLine().trim();
                    if (s.isEmpty()) { inicioDeseado = 0; break; }
                    try {
                        int v = Integer.parseInt(s);
                        if (v < 0) { System.out.println("Debe ser >= 0."); continue; }
                        inicioDeseado = v;
                    } catch (NumberFormatException e) { System.out.println("Número inválido."); }
                }

                System.out.println("Enviando " + nombreProceso + " t=" + duracion + " startTick=" + inicioDeseado);
                Object[] params = new Object[]{ duracion, nombreProceso, nombreCliente, inicioDeseado };
                client.execute("Manejador.IngresarProceso", params);
            }
            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pide y valida nombre (1–20 chars)
    private static String pedirNombreInteractivo() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("Elige tu nombre (1–20 caracteres): ");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) { System.out.println("No puede estar vacío."); continue; }
            if (s.length() > 20) { System.out.println("Máximo 20 caracteres."); continue; }
            return s;
        }
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        if (Files.exists(CONF)) {
            try (InputStream in = Files.newInputStream(CONF)) { p.load(in); }
            catch (IOException ignored) {}
        }
        return p;
    }

    private static void saveProps(Properties p) {
        try (OutputStream out = Files.newOutputStream(CONF)) {
            p.store(out, "ProyectoDistribuida cliente");
        } catch (IOException e) {
            System.err.println("No se pudo guardar configuración: " + e.getMessage());
        }
    }
}
