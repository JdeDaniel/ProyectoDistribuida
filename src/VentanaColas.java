import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class VentanaColas extends JFrame {
    private static final int ALTURA_BARRA = 24;
    private static final int GAP_FILAS = 8;
    private static final int MARGEN_IZQ = 80;
    private static final int MARGEN_SUP = 40;
    private static final int ESCALA_TIEMPO = 20; // px por unidad

    private final ManejoDeProcesos scheduler;
    private final JLabel tickLabel = new JLabel("Tick: 0");
    private final PanelColas panelColas;

    public VentanaColas(ManejoDeProcesos scheduler) {
        super("Colas - Monitor en tiempo real");
        this.scheduler = scheduler;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(6,6));
        JPanel top = new JPanel(new BorderLayout());
        top.add(tickLabel, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        panelColas = new PanelColas();
        add(new JScrollPane(panelColas), BorderLayout.CENTER);

        setSize(900, 600);
        setLocationRelativeTo(null);

        // repaint timer
        Timer t = new Timer(200, e -> {
            tickLabel.setText("Tick: " + scheduler.getTick());
            panelColas.repaint();
        });
        t.start();
    }

    private class PanelColas extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int tick = scheduler.getTick();

            // Título
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.BLACK);
            g2.drawString("Monitor de colas (READY / RECHAZADOS)", MARGEN_IZQ, 20);

            // Eje de tiempo relativo al tick actual: dibujar desde tick..tick+N
            int window = 40; // unidades de tiempo a mostrar
            int x0 = MARGEN_IZQ;
            for (int t = 0; t <= window; t++) {
                int x = x0 + t * ESCALA_TIEMPO;
                g2.setColor(new Color(220,220,220));
                g2.drawLine(x, MARGEN_SUP - 10, x, getHeight() - 40);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(String.valueOf(tick + t), x - 6, MARGEN_SUP - 15);
            }

            // Obtener snapshots
            List<Proceso> enEspera = new ArrayList<>(scheduler.getEnEsperaSnapshot());
            List<Proceso> rechazados = new ArrayList<>(scheduler.getRechazadosSnapshot());
            Proceso running = scheduler.getRunning();

            // Preparar orden: por inicio esperado (creacion+enEspera o inicioDeseado)
            Comparator<Proceso> cmp = Comparator.comparingInt(p -> {
                Integer s = p.getInicio();
                if (s != null) return s;
                Integer C = p.getCreacion();
                if (C != null) return C + (p.getEnEspera() != null ? p.getEnEspera() : 0);
                Integer d = p.getInicioDeseado();
                return d != null ? d : tick;
            });

            enEspera.sort(cmp);
            rechazados.sort(cmp);

            // Dibujar EN ESPERA (arriba)
            int y = MARGEN_SUP;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(Color.BLACK);
            g2.drawString("READY (en espera)", 10, y + 6);
            y += 10;
            for (Proceso p : enEspera) {
                int inicioEstimado = (p.getInicio() != null) ? p.getInicio() :
                        (p.getCreacion() != null ? p.getCreacion() + (p.getEnEspera() != null ? p.getEnEspera() : 0) : (p.getInicioDeseado() != null ? p.getInicioDeseado() : tick));
                int dur = p.getDuracion();
                // Si es el running, ajustar restante
                if (running != null && running == p && p.getInicio() != null) {
                    int ejecutado = Math.max(0, tick - p.getInicio());
                    dur = Math.max(0, p.getDuracion() - ejecutado);
                }
                int x = x0 + (inicioEstimado - tick) * ESCALA_TIEMPO;
                int width = Math.max(4, dur * ESCALA_TIEMPO);
                // recortar a ventana
                if (x + width < x0) continue;
                g2.setColor(colorDeterminista(p.getNombre()));
                g2.fillRoundRect(x, y, width, ALTURA_BARRA, 6, 6);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(x, y, width, ALTURA_BARRA, 6, 6);
                g2.drawString(p.getNombre() + " (" + p.getCliente() + ")", x + 4, y + ALTURA_BARRA - 6);
                y += ALTURA_BARRA + GAP_FILAS;
            }

            // Separador
            y += 10;
            g2.drawLine(10, y, getWidth() - 10, y);
            y += 20;

            // Dibujar RECHAZADOS (abajo)
            g2.drawString("RECHAZADOS", 10, y - 8);
            for (Proceso p : rechazados) {
                int inicioEstimado = (p.getInicioDeseado() != null) ? p.getInicioDeseado() : (p.getCreacion() != null ? p.getCreacion() : tick);
                int dur = p.getDuracion();
                int x = x0 + (inicioEstimado - tick) * ESCALA_TIEMPO;
                int width = Math.max(4, dur * ESCALA_TIEMPO);
                g2.setColor(new Color(200, 80, 80));
                g2.fillRoundRect(x, y, width, ALTURA_BARRA, 6, 6);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(x, y, width, ALTURA_BARRA, 6, 6);
                g2.drawString(p.getNombre() + " (" + p.getCliente() + ")", x + 4, y + ALTURA_BARRA - 6);
                y += ALTURA_BARRA + GAP_FILAS;
            }
        }

        private Color colorDeterminista(String s){
            int h = s.hashCode();
            int r = 50 + Math.floorMod(h, 156);
            int g = 50 + Math.floorMod(h>>8, 156);
            int b = 50 + Math.floorMod(h>>16, 156);
            return new Color(r,g,b);
        }
    }

    public static void mostrar(ManejoDeProcesos scheduler) {
        SwingUtilities.invokeLater(() -> {
            VentanaColas v = new VentanaColas(scheduler);
            v.setVisible(true);
        });
    }
}

