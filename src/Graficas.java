import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Graficas extends JPanel{
    private List<Proceso> procesos = new ArrayList<>();
    private static final int ALTURA_BARRA = 40;
    private static final int MARGEN_IZQ = 100;
    private static final int MARGEN_SUP = 50;
    private static final int ESCALA_TIEMPO = 30; // píxeles por unidad de tiempo

    public Graficas (String rutaArchivo) {
        cargarProcesos(rutaArchivo);
        setPreferredSize(new Dimension(ESCALA_TIEMPO * 40, 300));
    }

    private void cargarProcesos(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Formato esperado: Proceso: A, Duracion: 3, Creacion: 0, En Espera: 0, Finalizacion: 3, Penalizacion: 1.0
                if (!linea.trim().isEmpty()) {
                    String[] partes = linea.split(",");
                    String nombre = partes[0].split(":")[1].trim();
                    int duracion = Integer.parseInt(partes[1].split(":")[1].trim());
                    int creacion = Integer.parseInt(partes[2].split(":")[1].trim());
                    int finalizacion = Integer.parseInt(partes[5].split(":")[1].trim());
                    int inicio = Integer.parseInt(partes[4].split(":")[1].trim());
                    Proceso nuevo = new Proceso(duracion, nombre, "\0");
                    nuevo.setFinalizacion(finalizacion);
                    nuevo.setCreacion(creacion);
                    nuevo.setInicio(inicio);
                    procesos.add(nuevo);

                    //procesos.add(new Proceso(nombre, inicio, duracion, finalizacion));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Dibujar ejes de tiempo
        g2.setColor(Color.GRAY);
        for (int t = 0; t <= 45; t++) {
            int x = MARGEN_IZQ + t * ESCALA_TIEMPO;
            g2.drawLine(x, MARGEN_SUP - 10, x, (getHeight()/2));
            g2.drawString(String.valueOf(t), x - 5, MARGEN_SUP - 15);
        }

        // Dibujar barras de procesos
        int y = MARGEN_SUP;
        for (Proceso p : procesos) {
            Color color = new Color((int)(Math.random() * 0xFFFFFF));
            g2.setColor(color);
            int x = MARGEN_IZQ + p.getInicio() * ESCALA_TIEMPO;
            int width = p.getDuracion() * ESCALA_TIEMPO;

            g2.fillRect(x, y, width, ALTURA_BARRA);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, width, ALTURA_BARRA);
            g2.drawString(p.getNombre(), x + width / 2 - 5, y + ALTURA_BARRA / 2 + 5);
            y += ALTURA_BARRA + 15;
        }

        // Título
        g2.setFont(new Font("Procesos", Font.BOLD, 18));
        g2.drawString("Diagrama FIFO", 150, 25);
    }

    public void mostrar(String rutaArchivo) {
        JFrame frame = new JFrame("FIFO");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(new Graficas(rutaArchivo)));
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}
