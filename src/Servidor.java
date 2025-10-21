import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;
import javax.swing.SwingUtilities;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.File;

public class Servidor {
    private static final Set<String> clientesRegistrados = new HashSet<>();
    private static ManejoDeProcesos scheduler = new ManejoDeProcesos();

    public static ManejoDeProcesos getScheduler() { return scheduler; }
    public Servidor() {}

    public void registrarCliente(String nombreCliente) {
        if (!clientesRegistrados.add(nombreCliente)) return;
        File f = new File(nombreCliente + "_procesos.txt");
        try { if (!f.exists()) f.createNewFile(); } catch (Exception e) { e.printStackTrace(); }
    }

    private static void cierreLimpio(WebServer webServer, Scanner sc) {
        try { if (sc != null) sc.close(); } catch (Exception ignore) {}
        for (String cliente : clientesRegistrados) {
            File f = new File(cliente + "_procesos.txt");
            if (!f.delete()) System.out.println("No se pudo borrar " + f.getName());
        }
        File g = new File("Todos_procesos.txt");
        if (!g.delete()) System.out.println("No se pudo borrar " + g.getName());
        try { if (webServer != null) webServer.shutdown(); } catch (Exception ignore) {}
    }

    public static void main(String[] args) {
        final int PORT = 8080;
        WebServer webServer = null;
        Scanner sc = new Scanner(System.in);

        try {
            System.out.println("Iniciando servidor XML-RPC en el puerto " + PORT + "…");
            webServer = new WebServer(PORT);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
            XmlRpcServerConfigImpl cfg = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
            cfg.setEnabledForExtensions(true);
            cfg.setContentLengthOptional(false);

            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.setVoidMethodEnabled(true);
            phm.addHandler("Servidor", Servidor.class);
            phm.addHandler("Manejador", RegistroProceso.class);
            phm.addHandler("Registro", RegistroCliente.class);
            phm.addHandler("Proceso",  Proceso.class);
            xmlRpcServer.setHandlerMapping(phm);

            // Opcional: deja SOLO uno, hook o finally. Mantengo finally y comento hook.
            WebServer finalWeb = webServer;
            // Runtime.getRuntime().addShutdownHook(new Thread(() -> cierreLimpio(finalWeb, null)));
            
            limpiarArchivos();
            
            webServer.start();
            System.out.println("Servidor iniciado. Esperando peticiones…");
            
            
            
            
            boolean loop = true;
            while (loop) {
                try {
                    mostrarMenu();
                    int opcion = Integer.parseInt(sc.nextLine().trim());
                    switch (opcion) {
                        case 1 -> {
                            String ruta = "Todos_procesos.txt";
                            
                            
                            SwingUtilities.invokeLater(() -> Graficas.mostrar(ruta));
                            esperarEnter(sc, "Pulsa ENTER para volver al menú...");
                        }
                        case 2 -> {
                            if (!scheduler.isAlive()) {
                                System.out.println("============ Iniciando planificador FIFO ============");
                                scheduler.start();
                            } else {
                                System.out.println("Planificador ya está corriendo.");
                            }
                            esperarEnter(sc, "Pulsa ENTER para volver al menú...");
                        }
                        case 3 -> {
                            System.out.println("Cerrando servidor…");
                            loop = false;
                        }
                        default -> System.out.println("Opción inválida.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada no válida.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        } finally {
            cierreLimpio(webServer, sc);
            System.out.println("Servidor cerrado.");
        }
    }

    private static void mostrarMenu() {
        System.out.println("\nMenú del servidor:");
        System.out.println("1. Mostrar gráficas");
        System.out.println("2. Comenzar manejo de procesos");
        System.out.println("3. Cerrar servidor");
        System.out.print("Opción: ");
    }

    private static void esperarEnter(Scanner sc, String msg) {
        System.out.print(msg);
        sc.nextLine();
    }
    
    private static void limpiarArchivos() {
    new File("Todos_procesos.txt").delete();
    for (String c : clientesRegistrados) new File(c + "_procesos.txt").delete();
}
}
