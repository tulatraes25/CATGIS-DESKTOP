package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CsvDataSourceDialog extends JDialog {

    private final File file;
    private final TablePointData tableData;
    private final JCheckBox spatialCheck;
    private final JComboBox<String> cmbX;
    private final JComboBox<String> cmbY;
    private final JComboBox<String> cmbLabel;
    private final JButton btnCrs;
    private final JLabel lblModeHint;
    private String selectedCrs = "EPSG:4326";

    public CsvDataSourceDialog(File file, TablePointData tableData) {
        this.file = file;
        this.tableData = tableData;

        setTitle("Origen de datos CSV");
        setModal(true);
        setSize(820, 520);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(8, 8));

        List<String> columns = tableData != null ? tableData.getColumns() : List.of();
        cmbX = new JComboBox<>(columns.toArray(new String[0]));
        cmbY = new JComboBox<>(columns.toArray(new String[0]));
        cmbLabel = new JComboBox<>(columns.toArray(new String[0]));
        spatialCheck = new JCheckBox("Interpretar como capa espacial de puntos (X/Y)");
        spatialCheck.setSelected(hasLikelySpatialColumns(columns));
        spatialCheck.addActionListener(e -> refreshSpatialState());

        autoSelectFields(columns);
        selectedCrs = suggestCrs();

        JPanel form = new JPanel(new GridLayout(6, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        form.add(new JLabel("Archivo CSV:"));
        form.add(new JLabel(file != null ? file.getAbsolutePath() : "-"));
        form.add(new JLabel("Modo de carga:"));
        form.add(spatialCheck);
        form.add(new JLabel("Columna X:"));
        form.add(cmbX);
        form.add(new JLabel("Columna Y:"));
        form.add(cmbY);
        form.add(new JLabel("Columna etiqueta:"));
        form.add(cmbLabel);
        form.add(new JLabel("CRS de origen:"));
        btnCrs = new JButton(CRSDefinitions.getLabelForCode(selectedCrs));
        btnCrs.addActionListener(e -> chooseCrs());
        form.add(btnCrs);
        add(form, BorderLayout.NORTH);

        add(buildPreviewPanel(), BorderLayout.CENTER);

        lblModeHint = new JLabel();
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        footer.add(lblModeHint, BorderLayout.WEST);

        JPanel buttons = new JPanel();
        JButton previewButton = new JButton("Ver tabla");
        previewButton.addActionListener(e -> CsvPreviewDialog.open(file, tableData));
        JButton importButton = new JButton("Cargar");
        importButton.addActionListener(e -> loadData());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttons.add(previewButton);
        buttons.add(importButton);
        buttons.add(cancelButton);
        footer.add(buttons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        refreshSpatialState();
    }

    public static void open(File file, TablePointData data) {
        SwingUtilities.invokeLater(() -> new CsvDataSourceDialog(file, data).setVisible(true));
    }

    private JScrollPane buildPreviewPanel() {
        DefaultTableModel model = new DefaultTableModel();
        if (tableData != null) {
            for (String column : tableData.getColumns()) {
                model.addColumn(column);
            }
            int maxRows = Math.min(tableData.getRows().size(), 40);
            for (int i = 0; i < maxRows; i++) {
                Map<String, String> row = tableData.getRows().get(i);
                Object[] values = new Object[tableData.getColumns().size()];
                for (int c = 0; c < tableData.getColumns().size(); c++) {
                    values[c] = row != null ? row.getOrDefault(tableData.getColumns().get(c), "") : "";
                }
                model.addRow(values);
            }
        }

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(22);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(780, 320));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 10, 4, 10),
                scrollPane.getBorder()
        ));
        return scrollPane;
    }

    private void chooseCrs() {
        CRSSelectorDialog.open("CRS del CSV", selectedCrs, code -> {
            if (code != null && !code.isBlank()) {
                selectedCrs = code;
                btnCrs.setText(CRSDefinitions.getLabelForCode(code));
            }
        });
    }

    private void refreshSpatialState() {
        boolean spatial = spatialCheck.isSelected();
        cmbX.setEnabled(spatial);
        cmbY.setEnabled(spatial);
        cmbLabel.setEnabled(spatial);
        btnCrs.setEnabled(spatial);
        lblModeHint.setText(spatial
                ? "Se cargara como puntos reutilizando el flujo espacial del proyecto."
                : "Se abrira como tabla CSV dentro de CATGIS, sin geometria.");
    }

    private void loadData() {
        try {
            if (!spatialCheck.isSelected()) {
                CsvPreviewDialog.open(file, tableData);
                dispose();
                return;
            }

            String xField = String.valueOf(cmbX.getSelectedItem());
            String yField = String.valueOf(cmbY.getSelectedItem());
            String labelField = String.valueOf(cmbLabel.getSelectedItem());
            if (xField == null || yField == null || xField.isBlank() || yField.isBlank()) {
                JOptionPane.showMessageDialog(this, "Debe indicar columnas X e Y para la capa espacial.");
                return;
            }

            ShapefileData data = TablePointLayerBuilder.build(tableData, xField, yField, selectedCrs, labelField);
            String layerName = file != null ? file.getName() : "tabla_csv";
            Layer layer = VectorLayerUtils.addResultLayer(layerName, data, null, selectedCrs, file != null ? file.getAbsolutePath() : "");
            if (layer != null) {
                layer.setLabelsVisible(true);
                layer.setLabelField(labelField);
            }
            JOptionPane.showMessageDialog(this, "CSV cargado correctamente como capa de puntos.");
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar CSV: " + ex.getMessage(), "Origen de datos CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasLikelySpatialColumns(List<String> columns) {
        return findBestMatch(columns, new String[]{"x", "coord_x", "utm_x", "este", "lon", "longitud"}) != null
                && findBestMatch(columns, new String[]{"y", "coord_y", "utm_y", "norte", "lat", "latitud"}) != null;
    }

    private void autoSelectFields(List<String> columns) {
        String x = findBestMatch(columns, new String[]{"x", "coord_x", "coordenada_x", "utm_x", "este", "easting", "lon", "longitude", "longitud"});
        String y = findBestMatch(columns, new String[]{"y", "coord_y", "coordenada_y", "utm_y", "norte", "northing", "lat", "latitude", "latitud"});
        String label = findBestMatch(columns, new String[]{"nombre", "name", "etiqueta", "label", "descripcion", "description", "id"});
        if (x != null) {
            cmbX.setSelectedItem(x);
        }
        if (y != null) {
            cmbY.setSelectedItem(y);
        }
        if (label != null) {
            cmbLabel.setSelectedItem(label);
        }
    }

    private String suggestCrs() {
        String xField = String.valueOf(cmbX.getSelectedItem());
        String yField = String.valueOf(cmbY.getSelectedItem());
        if (xField == null || yField == null || tableData == null) {
            return "EPSG:4326";
        }

        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();
        for (Map<String, String> row : tableData.getRows()) {
            try {
                xs.add(Double.parseDouble(row.getOrDefault(xField, "").replace(',', '.')));
                ys.add(Double.parseDouble(row.getOrDefault(yField, "").replace(',', '.')));
            } catch (Exception ignored) {
            }
            if (xs.size() >= 20) {
                break;
            }
        }

        if (xs.isEmpty() || ys.isEmpty()) {
            return "EPSG:4326";
        }

        double minX = xs.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxX = xs.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double minY = ys.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxY = ys.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        if (minX >= -180 && maxX <= 180 && minY >= -90 && maxY <= 90) {
            return "EPSG:4326";
        }
        if (minX > 1000000 && maxX < 10000000 && minY > 1000000 && maxY < 10000000) {
            return CatgisDesktopApp.currentProject != null && CatgisDesktopApp.currentProject.getProjectCRS() != null
                    ? CatgisDesktopApp.currentProject.getProjectCRS()
                    : "EPSG:22182";
        }
        return "EPSG:4326";
    }

    private String findBestMatch(List<String> columns, String[] priorities) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        for (String priority : priorities) {
            for (String column : columns) {
                String normalized = normalize(column);
                if (normalized.equals(priority) || normalized.contains(priority)) {
                    return column;
                }
            }
        }
        return null;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9]+", "_");
    }
}
