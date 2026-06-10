package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.locationtech.jts.geom.Coordinate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Real dialog for Network Analysis operations.
 * Replaces the JOptionPane stub with actual functionality.
 */
public class NetworkAnalysisDialog extends JDialog {

    private final JComboBox<String> operationCombo;
    private final JComboBox<String> layerCombo;
    private final JTextField startXField, startYField;
    private final JTextField endXField, endYField;
    private final JTextField distanceField;
    private final JTextArea resultArea;
    private final JLabel statusLabel;

    public NetworkAnalysisDialog() {
        super((Frame) null, "Analisis de Red", false);
        setSize(650, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("Analisis de Red");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(4, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Operation
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Operacion:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        operationCombo = new JComboBox<>(new String[]{
                "Ruta mas corta",
                "Matriz de costos",
                "Area de servicio",
                "Estadisticas de red"
        });
        operationCombo.addActionListener(e -> refreshUi());
        form.add(operationCombo, gbc);

        // Layer
        gbc.gridy = 1; gbc.gridx = 0;
        form.add(new JLabel("Capa de red:"), gbc);
        gbc.gridx = 1;
        layerCombo = new JComboBox<>();
        refreshLayers();
        form.add(layerCombo, gbc);

        // Start point
        gbc.gridy = 2; gbc.gridx = 0;
        form.add(new JLabel("Inicio X/Y:"), gbc);
        gbc.gridx = 1;
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        startXField = new JTextField("0", 8);
        startYField = new JTextField("0", 8);
        startPanel.add(startXField);
        startPanel.add(new JLabel("/"));
        startPanel.add(startYField);
        form.add(startPanel, gbc);

        // End point
        gbc.gridy = 3; gbc.gridx = 0;
        form.add(new JLabel("Fin X/Y:"), gbc);
        gbc.gridx = 1;
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        endXField = new JTextField("100", 8);
        endYField = new JTextField("0", 8);
        endPanel.add(endXField);
        endPanel.add(new JLabel("/"));
        endPanel.add(endYField);
        form.add(endPanel, gbc);

        // Distance (for service area)
        gbc.gridy = 4; gbc.gridx = 0;
        form.add(new JLabel("Distancia max:"), gbc);
        gbc.gridx = 1;
        distanceField = new JTextField("500", 8);
        form.add(distanceField, gbc);

        add(form, BorderLayout.NORTH);

        // Results
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Resultados"));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runButton = new JButton("Ejecutar");
        runButton.addActionListener(e -> execute());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(runButton);
        footer.add(closeButton);
        statusLabel = new JLabel("Selecciona una operacion y capa de red.");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new NetworkAnalysisDialog().setVisible(true));
    }

    private void refreshLayers() {
        layerCombo.removeAllItems();
        if (CatgisDesktopApp.mapPanel == null) return;
        for (Layer layer : CatgisDesktopApp.mapPanel.getRenderOrderLayers()) {
            ShapefileData data = ar.com.catgis.data.vector.VectorLayerUtils.ensureVectorData(layer);
            if (data != null && data.getSchema() != null) {
                String geomType = data.getSchema().getGeometryDescriptor() != null
                        ? data.getSchema().getGeometryDescriptor().getType().getBinding().getSimpleName() : "";
                if (geomType.contains("Line")) {
                    layerCombo.addItem(layer.getName());
                }
            }
        }
    }

    private void refreshUi() {
        String op = (String) operationCombo.getSelectedItem();
        boolean needsEndpoint = "Ruta mas corta".equals(op);
        boolean needsDistance = "Area de servicio".equals(op);
        endXField.setEnabled(needsEndpoint);
        endYField.setEnabled(needsEndpoint);
        distanceField.setEnabled(needsDistance);
    }

    private void execute() {
        String op = (String) operationCombo.getSelectedItem();
        String layerName = (String) layerCombo.getSelectedItem();
        if (layerName == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una capa de red.");
            return;
        }

        // Find the layer
        Layer targetLayer = null;
        for (Layer layer : CatgisDesktopApp.mapPanel.getRenderOrderLayers()) {
            if (layer.getName().equals(layerName)) {
                targetLayer = layer;
                break;
            }
        }
        if (targetLayer == null) return;

        ShapefileData data = ar.com.catgis.data.vector.VectorLayerUtils.ensureVectorData(targetLayer);
        if (data == null || data.getFeatures() == null) return;

        List<org.geotools.api.feature.simple.SimpleFeature> features = new ArrayList<>(data.getFeatures());

        double startX = parseDouble(startXField.getText());
        double startY = parseDouble(startYField.getText());
        double endX = parseDouble(endXField.getText());
        double endY = parseDouble(endYField.getText());

        statusLabel.setText("Calculando...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return switch (op) {
                    case "Ruta mas corta" -> computeShortestPath(features, startX, startY, endX, endY);
                    case "Matriz de costos" -> computeCostMatrix(features, startX, startY);
                    case "Area de servicio" -> computeServiceArea(features, startX, startY);
                    case "Estadisticas de red" -> computeStats(features);
                    default -> "Operacion no soportada.";
                };
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    resultArea.setText(result);
                    statusLabel.setText("Completado.");
                    statusLabel.setForeground(new Color(0, 128, 0));
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private String computeShortestPath(List<org.geotools.api.feature.simple.SimpleFeature> features,
                                        double sx, double sy, double ex, double ey) {
        var path = NetworkAnalysisEngine.shortestPath(features, new Coordinate(sx, sy), new Coordinate(ex, ey), 10);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Ruta Mas Corta ===\n\n");
        sb.append("Inicio: (").append(sx).append(", ").append(sy).append(")\n");
        sb.append("Fin: (").append(ex).append(", ").append(ey).append(")\n");
        sb.append("Distancia total: ").append(String.format("%.2f", path.totalDistance())).append(" unidades\n");
        sb.append("Vertices en ruta: ").append(path.route().size()).append("\n");
        if (!path.warnings().isEmpty()) {
            sb.append("\nAdvertencias: ").append(path.warnings()).append("\n");
        }
        if (!path.route().isEmpty()) {
            sb.append("\nCoordenadas de la ruta:\n");
            for (int i = 0; i < path.route().size(); i++) {
                var c = path.route().get(i);
                sb.append("  ").append(i + 1).append(": (").append(String.format("%.2f", c.x)).append(", ").append(String.format("%.2f", c.y)).append(")\n");
            }
        }
        return sb.toString();
    }

    private String computeCostMatrix(List<org.geotools.api.feature.simple.SimpleFeature> features,
                                      double sx, double sy) {
        List<org.locationtech.jts.geom.Coordinate> points = new ArrayList<>();
        points.add(new org.locationtech.jts.geom.Coordinate(sx, sy));
        points.add(new org.locationtech.jts.geom.Coordinate(sx + 100, sy));
        points.add(new org.locationtech.jts.geom.Coordinate(sx, sy + 100));

        double[][] matrix = NetworkAnalysisEngine.computeCostMatrix(features, points, 10);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Matriz de Costos (3 puntos) ===\n\n");
        sb.append(String.format("%-12s %-12s %-12s %-12s\n", "", "Punto 1", "Punto 2", "Punto 3"));
        for (int i = 0; i < matrix.length; i++) {
            sb.append(String.format("Punto %-5d", i + 1));
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(String.format("%-12s", String.format("%.2f", matrix[i][j])));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String computeServiceArea(List<org.geotools.api.feature.simple.SimpleFeature> features,
                                       double sx, double sy) {
        double maxDist = parseDouble(distanceField.getText());
        var area = NetworkAnalysisEngine.serviceArea(features, new org.locationtech.jts.geom.Coordinate(sx, sy), maxDist, 10);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Area de Servicio ===\n\n");
        sb.append("Centro: (").append(sx).append(", ").append(sy).append(")\n");
        sb.append("Distancia maxima: ").append(maxDist).append(" unidades\n");
        sb.append("Nodos alcanzables: ").append(area.size()).append("\n");
        if (!area.isEmpty()) {
            sb.append("\nCoordenadas alcanzables:\n");
            for (int i = 0; i < Math.min(area.size(), 20); i++) {
                var c = area.get(i);
                sb.append("  ").append(i + 1).append(": (").append(String.format("%.2f", c.x)).append(", ").append(String.format("%.2f", c.y)).append(")\n");
            }
            if (area.size() > 20) sb.append("  ... y ").append(area.size() - 20).append(" mas\n");
        }
        return sb.toString();
    }

    private String computeStats(List<org.geotools.api.feature.simple.SimpleFeature> features) {
        var stats = NetworkAnalysisEngine.computeStats(features, 10);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estadisticas de Red ===\n\n");
        sb.append("Nodos: ").append(stats.nodeCount()).append("\n");
        sb.append("Aristas: ").append(stats.edgeCount()).append("\n");
        sb.append("Longitud total: ").append(String.format("%.2f", stats.totalLength())).append(" unidades\n");
        sb.append("Grado promedio: ").append(String.format("%.2f", stats.avgDegree())).append("\n");
        sb.append("Densidad: ").append(String.format("%.4f", stats.density())).append("\n");
        return sb.toString();
    }

    private double parseDouble(String text) {
        try { return Double.parseDouble(text.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
