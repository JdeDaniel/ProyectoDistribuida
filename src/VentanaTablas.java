import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class VentanaTablas extends JFrame {

    private final List<Map<String, String>> datos = new ArrayList<>();

    public VentanaTablas(String rutaArchivo) {
        setTitle("Tablas de Resultados");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(2, 1, 10, 10));

        // Cargar datos desde el mismo archivo
        cargarDatos(rutaArchivo);

        // ===== TABLA DE PROCESOS =====
        JTable tablaProcesos = crearTablaProcesos();
        JPanel panelProcesos = new JPanel(new BorderLayout());
        panelProcesos.setBorder(BorderFactory.createTitledBorder("Datos de Procesos"));
        panelProcesos.add(new JScrollPane(tablaProcesos), BorderLayout.CENTER);

        // ===== TABLAS DE RESULTADOS =====
        JPanel panelResultados = new JPanel(new GridLayout(1, 3, 10, 10));
        panelResultados.add(crearTablaGenerica("Tiempo de Espera", "en espera"));
        panelResultados.add(crearTablaGenerica("Tiempo de Finalización", "finalizacion"));
        panelResultados.add(crearTablaGenerica("Penalización", "penalizacion"));

        add(panelProcesos);
        add(panelResultados);
    }

    private void cargarDatos(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                // ejemplo de línea:
                // Proceso: A, Duracion: 3, Creacion: 0, En Espera: 0, Finalizacion: 3, Penalizacion: 1.0
                String[] partes = linea.split(",");
                Map<String, String> kv = new LinkedHashMap<>();
                for (String p : partes) {
                    String[] kvp = p.split(":", 2);
                    if (kvp.length == 2) kv.put(kvp[0].trim().toLowerCase(), kvp[1].trim());
                }
                datos.add(kv);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error leyendo archivo: " + e.getMessage());
        }
    }

    private JTable crearTablaProcesos() {
        String[] columnas = {"Proceso", "Creación (C)", "Duración (t)"};
        Object[][] filas = new Object[datos.size()][3];
        int i = 0;
        for (Map<String, String> kv : datos) {
            filas[i][0] = kv.getOrDefault("proceso", "-");
            filas[i][1] = kv.getOrDefault("creacion", "-");
            filas[i][2] = kv.getOrDefault("duracion", "-");
            i++;
        }
        return new JTable(new DefaultTableModel(filas, columnas));
    }

    private JPanel crearTablaGenerica(String titulo, String campo) {
        String[] columnas = {"Proceso", titulo};
        Object[][] filas = new Object[datos.size() + 1][2]; // +1 para la media

        double suma = 0.0;
        int i = 0;
        for (Map<String, String> kv : datos) {
            filas[i][0] = kv.getOrDefault("proceso", "-");
            String val = kv.getOrDefault(campo, "0");
            filas[i][1] = val;
            try { suma += Double.parseDouble(val); } catch (NumberFormatException ignored) {}
            i++;
        }

        // Calcular y mostrar media
        double media = (datos.isEmpty()) ? 0 : suma / datos.size();
        filas[i][0] = "Media";
        filas[i][1] = String.format("%.2f", media);

        JTable tabla = new JTable(new DefaultTableModel(filas, columnas));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    public static void mostrar(String rutaArchivo) {
        SwingUtilities.invokeLater(() -> new VentanaTablas(rutaArchivo).setVisible(true));
    }
}
