import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Graficas extends JPanel {
    private static final int ALTURA_BARRA = 30;
    private static final int GAP_FILAS = 12;
    private static final int MARGEN_IZQ = 100;
    private static final int MARGEN_SUP = 50;
    private static final int ESCALA_TIEMPO = 30; // px por unidad
    private final List<Proceso> procesos = new ArrayList<>();
    private int makespan = 0;

    public Graficas(String rutaArchivo) {
        cargarProcesos(rutaArchivo);
        int alto = MARGEN_SUP + procesos.size() * (ALTURA_BARRA + GAP_FILAS) + 80;
        int ancho = MARGEN_IZQ + (makespan + 2) * ESCALA_TIEMPO;
        setPreferredSize(new Dimension(ancho, alto));
        setBackground(Color.WHITE);
    }

    private void cargarProcesos(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // Formato esperado:
                // Proceso: A, Duracion: 3, Creacion: 0, En Espera: 0, Finalizacion: 3, Penalizacion: 1.0 [, Cliente: W1]
                String[] partes = linea.split(",");
                Map<String,String> kv = new LinkedHashMap<>();
                for (String p : partes) {
                    String[] kvp = p.split(":", 2);
                    if (kvp.length < 2) continue;
                    kv.put(kvp[0].trim().toLowerCase(), kvp[1].trim());
                }
                String nombre = req(kv, "proceso");
                int t  = Integer.parseInt(req(kv, "duracion"));
                int C  = Integer.parseInt(req(kv, "creacion"));
                int E  = Integer.parseInt(req(kv, "en espera"));
                // calcular tiempos coherentes
                int inicio = C + E;
                int F = inicio + t; // si el archivo trae "finalizacion", se ignora si no coincide
                String cliente = kv.getOrDefault("cliente", "W?");
                Proceso p = new Proceso(t, nombre, cliente);
                p.setCreacion(C);
                p.setInicio(inicio);
                p.setFinalizacion(F);
                procesos.add(p);
                makespan = Math.max(makespan, F);
            }
            // ordenar por inicio para dibujar
            procesos.sort(Comparator.comparingInt(Proceso::getInicio).thenComparing(Proceso::getNombre));
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage());
        }
    }

    private static String req(Map<String,String> m, String key){
        String v = m.get(key);
        if (v == null) throw new IllegalArgumentException("Falta campo: " + key);
        return v;
    }

    private static Color colorDeterminista(String s){
        int h = s.hashCode();
        int r = 50 + Math.floorMod(h, 206);
        int g = 50 + Math.floorMod(h>>8, 206);
        int b = 50 + Math.floorMod(h>>16, 206);
        return new Color(r,g,b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Título
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.setColor(Color.BLACK);
        g2.drawString("Diagrama FIFO", MARGEN_IZQ, 25);

        // Eje de tiempo
        g2.setColor(new Color(210,210,210));
        for (int t = 0; t <= makespan; t++) {
            int x = MARGEN_IZQ + t * ESCALA_TIEMPO;
            g2.drawLine(x, MARGEN_SUP - 10, x, getHeight() - 40);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.valueOf(t), x - 3, MARGEN_SUP - 15);
            g2.setColor(new Color(210,210,210));
        }

        // Barras
        int y = MARGEN_SUP;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (Proceso p : procesos) {
            int x = MARGEN_IZQ + p.getInicio() * ESCALA_TIEMPO;
            int width = p.getDuracion() * ESCALA_TIEMPO;
            Color c = colorDeterminista(p.getNombre()); // estable
            g2.setColor(c);
            g2.fillRoundRect(x, y, width, ALTURA_BARRA, 8, 8);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(x, y, width, ALTURA_BARRA, 8, 8);
            // etiqueta izquierda: nombre y cliente
            g2.drawString(p.getNombre() + " (" + p.getCliente() + ")", 10, y + ALTURA_BARRA - 8);
            // etiqueta centrada en barra
            g2.drawString(p.getNombre(), x + width/2 - 6, y + ALTURA_BARRA/2 + 5);
            y += ALTURA_BARRA + GAP_FILAS;
        }
    }

    public static void mostrar(String rutaArchivo) {
        JFrame frame = new JFrame("FIFO");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Graficas panel = new Graficas(rutaArchivo);
        frame.setContentPane(new JScrollPane(panel));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
