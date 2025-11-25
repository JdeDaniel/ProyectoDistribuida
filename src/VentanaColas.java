import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
 

public class VentanaColas extends JFrame {
    private static final int CELL_SIZE = 40; // ancho y alto fijos por celda
    private static final int GAP_FILAS = 8;
    private static final int MARGEN_IZQ = 80;
    private static final int MARGEN_SUP = 40;
    private static final int WINDOW_CELLS = 12; // número de celdas que se muestran en la cuadrícula

    private final ManejoDeProcesos scheduler;
    private final JLabel tickLabel = new JLabel("Tick: 0");
    private final PanelColas panelColas;

    public VentanaColas(ManejoDeProcesos scheduler) {
        super("Colas en tiempo real");
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
        // cache de colores por proceso, estable y determinista
        private final Map<String,Color> colorCache = new HashMap<>();

        private Color getColorForProcess(String name) {
            return colorCache.computeIfAbsent(name, k -> {
                int h = Math.abs(k.hashCode());
                float hue = (h % 360) / 360f;
                float sat = 0.68f;
                float bri = 0.86f;
                return Color.getHSBColor(hue, sat, bri);
            });
        }

        private Color pickTextColor(Color bg) {
            double r = bg.getRed();
            double g = bg.getGreen();
            double b = bg.getBlue();
            double lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            return lum > 0.6 ? Color.BLACK : Color.WHITE;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int tick = scheduler.getTick();

            // Título
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.BLACK);
            //g2.drawString("Monitor de colas (READY / RECHAZADOS)", MARGEN_IZQ, 20);

            int x0 = MARGEN_IZQ;
            // Dibujar la cuadrícula de tiempo (etiquetas y celdas) fija
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            for (int c = 0; c < WINDOW_CELLS; c++) {
                int x = x0 + c * CELL_SIZE;
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(String.valueOf(tick + c), x + CELL_SIZE/2 - 6, MARGEN_SUP - 12);
            }

            // Fijar layout: dos áreas (READY arriba, RECHAZADOS abajo) con separación constante
            final int MAX_ROWS_READY = 1;
            final int MAX_ROWS_REJECT = 1;
            final int GAP_SECTION = 24;
            final int VERTICAL_SHIFT = 28; // bajar ambas secciones un poco
            int baseYReady = MARGEN_SUP + VERTICAL_SHIFT;
            int heightReady = MAX_ROWS_READY * (CELL_SIZE + GAP_FILAS);
            int baseYReject = baseYReady + heightReady + GAP_SECTION;
            int heightReject = MAX_ROWS_REJECT * (CELL_SIZE + GAP_FILAS);

            // Dibujar líneas verticales de la cuadrícula para ambas secciones (mismas columnas)
            g2.setColor(new Color(200,200,200));
            for (int c = 0; c < WINDOW_CELLS; c++) {
                int x = x0 + c * CELL_SIZE;
                g2.drawLine(x, baseYReady, x, baseYReady + heightReady - GAP_FILAS);
                g2.drawLine(x, baseYReject, x, baseYReject + heightReject - GAP_FILAS);
            }

            // Obtener snapshots
            List<Proceso> enEspera = new ArrayList<>(scheduler.getEnEsperaSnapshot());
            List<Proceso> rechazados = new ArrayList<>(scheduler.getRechazadosSnapshot());
            Proceso running = scheduler.getRunning();

            // Mantener orden FIFO de la cola enEspera (no reordenar)
            // Para rechazados, mantener también orden de la cola

            // Título sección READY
            g2.setColor(Color.BLACK);
            g2.drawString("READY (en espera)", 10, baseYReady - 12);

            // Dibujar cuadrícula de celdas para READY usando UNA sola fila
            int ROWS_READY = 1; // mostrar exactamente 1 fila para READY
            for (int r = 0; r < ROWS_READY; r++) {
                for (int c = 0; c < WINDOW_CELLS; c++) {
                    int gx = x0 + c * CELL_SIZE;
                    int gy = baseYReady + r * (CELL_SIZE + GAP_FILAS);
                    g2.setColor(Color.WHITE);
                    g2.fillRect(gx, gy, CELL_SIZE, CELL_SIZE);
                    g2.setColor(new Color(200,200,200));
                    g2.drawRect(gx, gy, CELL_SIZE, CELL_SIZE);
                }
            }

            // Pintar procesos en READY sobre la cuadrícula (UNA fila, contiguos en orden FIFO)
            int gy = baseYReady; // una única fila
            int cursorCell = 0; // posición relativa en celdas donde colocar siguiente proceso
            for (Proceso p : enEspera) {
                int dur = p.getDuracion();
                int ejecutado = 0;
                if (running != null && running == p && p.getInicio() != null) {
                    ejecutado = Math.max(0, tick - p.getInicio());
                }
                // remanente (lo que queda por ejecutar) — esto hace que la barra "se mueva" hacia la izquierda
                int rem = Math.max(0, dur - ejecutado);
                // colocamos el proceso (las celdas restantes) de forma contigua en la siguiente celda disponible
                Color baseColor = getColorForProcess(p.getNombre());
                for (int i = 0; i < rem; i++) {
                    int cellIndex = cursorCell + i;
                    if (cellIndex >= 0 && cellIndex < WINDOW_CELLS) {
                        int x = x0 + cellIndex * CELL_SIZE;
                        g2.setColor(baseColor);
                        g2.fillRect(x+1, gy+1, CELL_SIZE-1, CELL_SIZE-1);
                        g2.setColor(Color.BLACK);
                        g2.drawRect(x, gy, CELL_SIZE, CELL_SIZE);
                        g2.setColor(pickTextColor(baseColor));
                        FontMetrics fm = g2.getFontMetrics();
                        String label = p.getNombre();
                        int tx = x + (CELL_SIZE - fm.stringWidth(label)) / 2;
                        int ty = gy + (CELL_SIZE + fm.getAscent()) / 2 - 4;
                        g2.drawString(label, Math.max(tx, x+2), ty);
                    }
                }
                cursorCell += rem; // próximo proceso se pega tras las celdas restantes
                if (cursorCell >= WINDOW_CELLS) break; // no hay más espacio visible
            }

            // Separador fijo: dibujar línea entre secciones en posición determinada
            int sepY = baseYReject - (GAP_SECTION / 2);
            g2.setColor(Color.GRAY);
            g2.drawLine(10, sepY, getWidth() - 10, sepY);

            // Título sección RECHAZADOS
            g2.setColor(Color.BLACK);
            g2.drawString("RECHAZADOS", 10, baseYReject - 12);
            // Mostrar RECHAZADOS: dibujar la misma cuadrícula fija que READY, una fila
            for (int c = 0; c < WINDOW_CELLS; c++) {
                int gx = x0 + c * CELL_SIZE;
                int gyRejectGrid = baseYReject;
                g2.setColor(Color.WHITE);
                g2.fillRect(gx, gyRejectGrid, CELL_SIZE, CELL_SIZE);
                g2.setColor(new Color(200,200,200));
                g2.drawRect(gx, gyRejectGrid, CELL_SIZE, CELL_SIZE);
            }

            // Mostrar RECHAZADOS en UNA fila, un recuadro por proceso, en orden FIFO y contiguos
            int cursorR = 0;
            int gyR = baseYReject; // una sola fila para rechazados
            for (Proceso p : rechazados) {
                if (cursorR >= WINDOW_CELLS) break; // pantalla llena
                int x = x0 + cursorR * CELL_SIZE;
                Color cReject = getColorForProcess(p.getNombre());
                g2.setColor(cReject);
                g2.fillRect(x+1, gyR+1, CELL_SIZE-1, CELL_SIZE-1);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, gyR, CELL_SIZE, CELL_SIZE);
                g2.setColor(pickTextColor(cReject));
                FontMetrics fm = g2.getFontMetrics();
                String label = p.getNombre();
                int tx = x + (CELL_SIZE - fm.stringWidth(label)) / 2;
                int ty = gyR + (CELL_SIZE + fm.getAscent()) / 2 - 4;
                g2.drawString(label, Math.max(tx, x+2), ty);
                cursorR++;
            }
        }

        // (no longer needed: getColorForProcess provides deterministic vivid colours)
    }

    public static void mostrar(ManejoDeProcesos scheduler) {
        SwingUtilities.invokeLater(() -> {
            VentanaColas v = new VentanaColas(scheduler);
            v.setVisible(true);
        });
    }
}

