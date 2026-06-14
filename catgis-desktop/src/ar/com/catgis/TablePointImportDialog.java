package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;

import ar.com.catgis.core.model.Project;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ar.com.catgis.core.model.Layer;

public class TablePointImportDialog extends JDialog {

    private final File file;
    private final TablePointData tableData;

    private final JComboBox<String> cmbX;
    private final JComboBox<String> cmbY;
    private final JComboBox<String> cmbLabel;
    private final JButton btnCRS;
    private final JLabel lblSuggestion;

    private String selectedCRS = "EPSG:4326";

    public TablePointImportDialog(File file, TablePointData tableData) {
        this.file = file;
        this.tableData = tableData;

        setTitle("Importar tabla como puntos");
        setModal(true);
        setSize(620, 310);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        List<String> cols = tableData.getColumns();

        cmbX = new JComboBox<>(cols.toArray(new String[0]));
        cmbY = new JComboBox<>(cols.toArray(new String[0]));
        cmbLabel = new JComboBox<>(cols.toArray(new String[0]));

        autoSelectFields(cols);

        String suggestedCRS = suggestCRS();
        if (suggestedCRS != null && !suggestedCRS.isBlank()) {
            selectedCRS = suggestedCRS;
        }

        JPanel form = new JPanel(new GridLayout(6, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        form.add(new JLabel("Campo X:"));
        form.add(cmbX);

        form.add(new JLabel("Campo Y:"));
        form.add(cmbY);

        form.add(new JLabel("Campo etiqueta:"));
        form.add(cmbLabel);

        form.add(new JLabel("CRS de la tabla:"));
        btnCRS = new JButton(CRSDefinitions.getLabelForCode(selectedCRS));
        btnCRS.addActionListener(e -> chooseCRS());
        form.add(btnCRS);

        form.add(new JLabel("Sugerencia automática:"));
        lblSuggestion = new JLabel(buildSuggestionText(selectedCRS));
        form.add(lblSuggestion);

        form.add(new JLabel("Archivo:"));
        form.add(new JLabel(file.getName()));

        JPanel buttons = new JPanel();

        JButton btnImport = new JButton("Importar al mapa");
        btnImport.addActionListener(e -> importLayer());

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());

        buttons.add(btnImport);
        buttons.add(btnCancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public static void open(File file, TablePointData data) {
        SwingUtilities.invokeLater(() -> new TablePointImportDialog(file, data).setVisible(true));
    }

    private void chooseCRS() {
        CRSSelectorDialog.open("CRS de la tabla", selectedCRS, code -> {
            selectedCRS = code;
            btnCRS.setText(CRSDefinitions.getLabelForCode(code));
            lblSuggestion.setText(buildSuggestionText(code));
        });
    }

    private void importLayer() {
        try {
            String xField = String.valueOf(cmbX.getSelectedItem());
            String yField = String.valueOf(cmbY.getSelectedItem());
            String labelField = String.valueOf(cmbLabel.getSelectedItem());

            ShapefileData data = TablePointLayerBuilder.build(
                    tableData,
                    xField,
                    yField,
                    selectedCRS,
                    labelField
            );

            Layer layer = new Layer(file.getName(), file.getAbsolutePath(), "VECTOR");
            layer.setVisible(true);
            layer.setSourceName(data.getSourceName());
            layer.setFeatureCount(data.getFeatureCount());
            layer.setSourceCRS(selectedCRS);
            layer.setLabelsVisible(true);
            layer.setLabelField(labelField);

            if (AppContext.project() == null) {
                AppContext.setCurrentProject(new Project("Proyecto actual"));
            }

            AppContext.project().addLayer(layer);
            CatgisDesktopApp.markProjectDirty();
            AppContext.addLayer(layer);
            AppContext.mapPanel().addOrUpdateShapefileLayer(layer, data);
            AppContext.mapPanel().showOpenedFile(layer.getName());
            AppContext.mapPanel().resetView();
            AppContext.mapPanel().repaint();

            NotificationManager.info(this, null, "Tabla importada correctamente como capa de puntos.");
            dispose();

        } catch (Exception ex) {
            AppErrorSupport.logFailure("Error al importar tabla como puntos desde " + file.getAbsolutePath(), ex);
            AppErrorSupport.showErrorDialog(this, "Importar tabla", "Error al importar tabla como puntos.", ex);
        }
    }

    private void autoSelectFields(List<String> cols) {
        selectBestX(cols);
        selectBestY(cols);
        selectBestLabel(cols);
    }

    private void selectBestX(List<String> cols) {
        String[] priority = {
                "x",
                "coord_x",
                "coordenada_x",
                "este",
                "easting",
                "utm_x",
                "x_utm",
                "longitud",
                "longitude",
                "lon",
                "long",
                "lng"
        };

        String match = findBestMatch(cols, priority);
        if (match != null) {
            cmbX.setSelectedItem(match);
        }
    }

    private void selectBestY(List<String> cols) {
        String[] priority = {
                "y",
                "coord_y",
                "coordenada_y",
                "norte",
                "northing",
                "utm_y",
                "y_utm",
                "latitud",
                "latitude",
                "lat"
        };

        String match = findBestMatch(cols, priority);
        if (match != null) {
            cmbY.setSelectedItem(match);
        }
    }

    private void selectBestLabel(List<String> cols) {
        String[] priority = {
                "nombre",
                "name",
                "descripcion",
                "description",
                "etiqueta",
                "label",
                "id",
                "codigo",
                "code"
        };

        String match = findBestMatch(cols, priority);
        if (match != null) {
            cmbLabel.setSelectedItem(match);
        }
    }

    private String findBestMatch(List<String> cols, String[] priority) {
        for (String p : priority) {
            for (String c : cols) {
                if (normalize(c).equals(p)) {
                    return c;
                }
            }
        }

        for (String p : priority) {
            for (String c : cols) {
                if (normalize(c).contains(p)) {
                    return c;
                }
            }
        }

        return cols.isEmpty() ? null : cols.get(0);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        String value = text.toLowerCase().trim();
        value = value.replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");
        value = value.replaceAll("[^a-z0-9]+", "_");
        return value;
    }

    private String suggestCRS() {
        String xField = String.valueOf(cmbX.getSelectedItem());
        String yField = String.valueOf(cmbY.getSelectedItem());

        if (xField == null || yField == null || xField.isBlank() || yField.isBlank()) {
            return "EPSG:4326";
        }

        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        for (Map<String, String> row : tableData.getRows()) {
            String xText = row.getOrDefault(xField, "").trim();
            String yText = row.getOrDefault(yField, "").trim();

            if (xText.isBlank() || yText.isBlank()) {
                continue;
            }

            try {
                double x = parseCoordinate(xText);
                double y = parseCoordinate(yText);
                xs.add(x);
                ys.add(y);
            } catch (Exception ignored) { CatgisLogger.warn("TablePointImportDialog: operation failed", ignored); }

            if (xs.size() >= 50) {
                break;
            }
        }

        if (xs.isEmpty() || ys.isEmpty()) {
            return "EPSG:4326";
        }

        double minX = min(xs);
        double maxX = max(xs);
        double minY = min(ys);
        double maxY = max(ys);

        if (looksGeographic(minX, maxX, minY, maxY)) {
            return "EPSG:4326";
        }

        if (looksUtmSouth(minX, maxX, minY, maxY)) {
            return guessBestUtmSouth(minX, maxX, minY, maxY);
        }

        if (looksArgentinaFaja(minX, maxX, minY, maxY)) {
            return guessBestArgentinaFaja(minX, maxX);
        }

        return "EPSG:4326";
    }

    private boolean looksGeographic(double minX, double maxX, double minY, double maxY) {
        return minX >= -180 && maxX <= 180 && minY >= -90 && maxY <= 90;
    }

    private boolean looksUtmSouth(double minX, double maxX, double minY, double maxY) {
        return minX >= 100000 && maxX <= 900000 && minY >= 1000000 && maxY <= 10000000;
    }

    private boolean looksArgentinaFaja(double minX, double maxX, double minY, double maxY) {
        return minX >= 1000000 && maxX <= 8000000 && minY >= 1000000 && maxY <= 10000000;
    }

    private String guessBestUtmSouth(double minX, double maxX, double minY, double maxY) {
        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;

        if (centerX >= 200000 && centerX <= 400000) {
            return "EPSG:32719";
        }
        if (centerX > 400000 && centerX <= 600000) {
            return "EPSG:32720";
        }
        if (centerX > 600000 && centerX <= 800000) {
            return "EPSG:32721";
        }

        if (centerY < 0) {
            return "EPSG:32720";
        }

        return "EPSG:32720";
    }

    private String guessBestArgentinaFaja(double minX, double maxX) {
        double centerX = (minX + maxX) / 2.0;

        if (centerX >= 1500000 && centerX < 2500000) return "EPSG:22181";
        if (centerX >= 2500000 && centerX < 3500000) return "EPSG:22182";
        if (centerX >= 3500000 && centerX < 4500000) return "EPSG:22183";
        if (centerX >= 4500000 && centerX < 5500000) return "EPSG:22184";
        if (centerX >= 5500000 && centerX < 6500000) return "EPSG:22185";
        if (centerX >= 6500000 && centerX < 7500000) return "EPSG:22186";

        return "EPSG:22187";
    }

    private String buildSuggestionText(String code) {
        if (code == null || code.isBlank()) {
            return "Sin sugerencia";
        }
        return CRSDefinitions.getLabelForCode(code);
    }

    private double parseCoordinate(String text) {
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    private double min(List<Double> values) {
        double v = Double.POSITIVE_INFINITY;
        for (Double d : values) {
            if (d != null && d < v) {
                v = d;
            }
        }
        return v;
    }

    private double max(List<Double> values) {
        double v = Double.NEGATIVE_INFINITY;
        for (Double d : values) {
            if (d != null && d > v) {
                v = d;
            }
        }
        return v;
    }
}