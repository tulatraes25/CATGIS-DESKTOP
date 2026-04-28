package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopologyValidationDialog extends JDialog {

    private final JComboBox<Layer> layerCombo;
    private final JTextField txtFilter;
    private final JComboBox<String> comboSeverity;
    private final JComboBox<String> comboType;
    private final JTextField txtTolerance;
    private final JTextField txtSliverArea;
    private final JCheckBox chkInvalid;
    private final JCheckBox chkSelfIntersections;
    private final JCheckBox chkDangling;
    private final JCheckBox chkNearMiss;
    private final JCheckBox chkDuplicates;
    private final JCheckBox chkOverlaps;
    private final JCheckBox chkHoles;
    private final JCheckBox chkSlivers;
    private final JCheckBox chkMultiparts;
    private final JCheckBox chkAutoZoom;
    private final JLabel lblSummary;
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    private List<TopologyCheckResult> currentResults = new ArrayList<>();

    public TopologyValidationDialog() {
        setTitle("Comprobaciones de topologia");
        setModal(false);
        setSize(1080, 680);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(8, 8));

        layerCombo = new JComboBox<>(VectorLayerUtils.getVectorLayers().toArray(new Layer[0]));
        txtFilter = new JTextField(18);
        comboSeverity = new JComboBox<>(new String[]{"Todas", "Error", "Advertencia"});
        comboType = new JComboBox<>(new String[]{"Todos"});
        txtTolerance = new JTextField("1.0", 6);
        txtSliverArea = new JTextField("1.0", 6);
        chkInvalid = new JCheckBox("Geometrias invalidas", true);
        chkSelfIntersections = new JCheckBox("Auto-intersecciones", true);
        chkDangling = new JCheckBox("Extremos colgantes", true);
        chkNearMiss = new JCheckBox("Overshoots / undershoots", true);
        chkDuplicates = new JCheckBox("Duplicados", true);
        chkOverlaps = new JCheckBox("Superposiciones", true);
        chkHoles = new JCheckBox("Huecos", true);
        chkSlivers = new JCheckBox("Slivers", true);
        chkMultiparts = new JCheckBox("Multipartes problematicas", true);
        chkAutoZoom = new JCheckBox("Auto zoom al resultado", true);
        lblSummary = new JLabel("Sin validacion ejecutada.");

        model = new DefaultTableModel(new Object[]{"Severidad", "Tipo", "Detalle", "Entidades"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(false);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topRow.add(new JLabel("Capa:"));
        topRow.add(layerCombo);
        JButton runButton = new JButton("Validar");
        runButton.addActionListener(e -> runValidation());
        topRow.add(runButton);
        topRow.add(new JLabel("Filtro:"));
        topRow.add(txtFilter);
        topRow.add(new JLabel("Severidad:"));
        topRow.add(comboSeverity);
        topRow.add(new JLabel("Tipo:"));
        topRow.add(comboType);
        topRow.add(chkAutoZoom);

        JPanel optionsPanel = new JPanel(new GridLayout(3, 4, 6, 4));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Reglas"));
        optionsPanel.add(chkInvalid);
        optionsPanel.add(chkSelfIntersections);
        optionsPanel.add(chkDangling);
        optionsPanel.add(chkNearMiss);
        optionsPanel.add(chkDuplicates);
        optionsPanel.add(chkOverlaps);
        optionsPanel.add(chkHoles);
        optionsPanel.add(chkSlivers);
        optionsPanel.add(chkMultiparts);
        optionsPanel.add(new JLabel("Tol. conexion:"));
        optionsPanel.add(txtTolerance);
        optionsPanel.add(new JLabel("Area sliver:"));
        optionsPanel.add(txtSliverArea);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        summaryPanel.add(lblSummary);

        header.add(topRow, BorderLayout.NORTH);
        header.add(optionsPanel, BorderLayout.CENTER);
        header.add(summaryPanel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton prevButton = new JButton("Anterior");
        prevButton.addActionListener(e -> moveSelection(-1));
        JButton nextButton = new JButton("Siguiente");
        nextButton.addActionListener(e -> moveSelection(1));
        JButton selectButton = new JButton("Seleccionar");
        selectButton.addActionListener(e -> selectSelectedResult());
        JButton zoomButton = new JButton("Zoom");
        zoomButton.addActionListener(e -> zoomToSelectedResult());
        JButton highlightButton = new JButton("Resaltar");
        highlightButton.addActionListener(e -> highlightSelectedResult());
        JButton exportButton = new JButton("Exportar reporte");
        exportButton.addActionListener(e -> exportReport());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(prevButton);
        footer.add(nextButton);
        footer.add(selectButton);
        footer.add(zoomButton);
        footer.add(highlightButton);
        footer.add(exportButton);
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        txtFilter.getDocument().addDocumentListener(new SimpleDocumentListener(this::applyFilters));
        comboSeverity.addActionListener(e -> applyFilters());
        comboType.addActionListener(e -> applyFilters());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && chkAutoZoom.isSelected()) {
                highlightSelectedResult();
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    zoomToSelectedResult();
                }
            }
        });
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new TopologyValidationDialog().setVisible(true));
    }

    private void runValidation() {
        Layer layer = layerCombo.getItemCount() > 0 ? layerCombo.getItemAt(layerCombo.getSelectedIndex()) : null;
        if (layer == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una capa vectorial.");
            return;
        }

        ShapefileData data = VectorLayerUtils.ensureVectorData(layer);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "La capa seleccionada no tiene datos vectoriales disponibles.");
            return;
        }

        try {
            TopologyValidationOptions options = buildOptions();
            currentResults = TopologyValidationService.validateLayer(layer, data, options);
            reloadTable();
            updateTypeFilter();
            applyFilters();
            lblSummary.setText("Observaciones detectadas: " + currentResults.size());

            if (currentResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se detectaron problemas con las reglas actuales.");
            } else {
                table.setRowSelectionInterval(0, 0);
                if (chkAutoZoom.isSelected()) {
                    highlightSelectedResult();
                }
            }
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo ejecutar la validacion topologica", ex);
            AppErrorSupport.showErrorDialog(this, "Topologia", "No se pudo ejecutar la validacion.", ex);
        }
    }

    private TopologyValidationOptions buildOptions() {
        TopologyValidationOptions options = new TopologyValidationOptions();
        options.setInvalidGeometries(chkInvalid.isSelected());
        options.setSelfIntersections(chkSelfIntersections.isSelected());
        options.setDanglingEndpoints(chkDangling.isSelected());
        options.setNearMissEndpoints(chkNearMiss.isSelected());
        options.setDuplicates(chkDuplicates.isSelected());
        options.setOverlaps(chkOverlaps.isSelected());
        options.setHoles(chkHoles.isSelected());
        options.setSlivers(chkSlivers.isSelected());
        options.setProblematicMultiparts(chkMultiparts.isSelected());
        options.setConnectionTolerance(parseDoubleField(txtTolerance, 1.0));
        options.setSliverAreaThreshold(parseDoubleField(txtSliverArea, 1.0));
        return options;
    }

    private double parseDoubleField(JTextField field, double fallback) {
        try {
            String text = field.getText() != null ? field.getText().trim().replace(',', '.') : "";
            return text.isBlank() ? fallback : Math.max(0d, Double.parseDouble(text));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void reloadTable() {
        model.setRowCount(0);
        for (TopologyCheckResult result : currentResults) {
            model.addRow(new Object[]{
                    result.getSeverity(),
                    result.getCheckType(),
                    result.getDetail(),
                    result.getFeatureIds().isEmpty() ? "-" : String.join(", ", result.getFeatureIds())
            });
        }
    }

    private void updateTypeFilter() {
        Object previous = comboType.getSelectedItem();
        comboType.removeAllItems();
        comboType.addItem("Todos");
        for (String type : currentResults.stream().map(TopologyCheckResult::getCheckType).distinct().sorted().toList()) {
            comboType.addItem(type);
        }
        if (previous != null) {
            comboType.setSelectedItem(previous);
            if (comboType.getSelectedItem() == null) {
                comboType.setSelectedIndex(0);
            }
        } else {
            comboType.setSelectedIndex(0);
        }
    }

    private void applyFilters() {
        String text = txtFilter.getText() != null ? txtFilter.getText().trim().toLowerCase(Locale.ROOT) : "";
        String severity = comboSeverity.getSelectedItem() != null ? comboSeverity.getSelectedItem().toString() : "Todas";
        String type = comboType.getSelectedItem() != null ? comboType.getSelectedItem().toString() : "Todos";

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!text.isBlank()) {
            filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2, 3));
        }
        if (!"Todas".equalsIgnoreCase(severity)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(severity) + "$", 0));
        }
        if (!"Todos".equalsIgnoreCase(type)) {
            filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(type) + "$", 1));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblSummary.setText("Observaciones visibles: " + table.getRowCount() + " / " + currentResults.size());
    }

    private void moveSelection(int delta) {
        int rowCount = table.getRowCount();
        if (rowCount <= 0) {
            return;
        }
        int selected = table.getSelectedRow();
        int next = selected < 0 ? 0 : Math.max(0, Math.min(rowCount - 1, selected + delta));
        table.setRowSelectionInterval(next, next);
        table.scrollRectToVisible(table.getCellRect(next, 0, true));
    }

    private void selectSelectedResult() {
        Layer layer = layerCombo.getItemCount() > 0 ? layerCombo.getItemAt(layerCombo.getSelectedIndex()) : null;
        TopologyCheckResult result = getSelectedResult();
        if (layer == null || result == null || CatgisDesktopApp.mapPanel == null) {
            return;
        }
        if (!result.getFeatureIds().isEmpty()) {
            CatgisDesktopApp.mapPanel.syncSelectionFromAttributeTable(layer, result.getFeatureIds());
        } else if (result.hasFocusGeometry()) {
            CatgisDesktopApp.mapPanel.flashGeometry(result.getFocusGeometry(), result.getSourceCrs());
        }
    }

    private void highlightSelectedResult() {
        TopologyCheckResult result = getSelectedResult();
        if (result == null || CatgisDesktopApp.mapPanel == null) {
            return;
        }
        if (result.hasFocusGeometry()) {
            CatgisDesktopApp.mapPanel.flashGeometry(result.getFocusGeometry(), result.getSourceCrs());
        } else {
            selectSelectedResult();
        }
    }

    private void zoomToSelectedResult() {
        Layer layer = layerCombo.getItemCount() > 0 ? layerCombo.getItemAt(layerCombo.getSelectedIndex()) : null;
        TopologyCheckResult result = getSelectedResult();
        if (result == null || CatgisDesktopApp.mapPanel == null) {
            return;
        }
        if (!result.getFeatureIds().isEmpty() && layer != null) {
            CatgisDesktopApp.mapPanel.syncSelectionFromAttributeTable(layer, result.getFeatureIds());
            CatgisDesktopApp.mapPanel.zoomToFeatureSelection(layer, result.getFeatureIds());
            highlightSelectedResult();
            return;
        }
        if (result.hasFocusGeometry()) {
            CatgisDesktopApp.mapPanel.zoomToGeometry(result.getFocusGeometry(), result.getSourceCrs());
        }
    }

    private void exportReport() {
        if (currentResults == null || currentResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero ejecute una validacion.");
            return;
        }

        JFileChooser chooser = FileChooserSupport.createChooser("topology-report", "Exportar comprobaciones de topologia");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));
        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile("topology-report", new File("topologia_report.csv")));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }
        FileChooserSupport.rememberFile("topology-report", file);

        try {
            TopologyValidationService.exportReport(currentResults, file);
            JOptionPane.showMessageDialog(this, "Reporte exportado:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            AppErrorSupport.logFailure("No se pudo exportar el reporte de topologia a " + file.getAbsolutePath(), ex);
            AppErrorSupport.showErrorDialog(this, "Topologia", "No se pudo exportar el reporte.", ex);
        }
    }

    private TopologyCheckResult getSelectedResult() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentResults.size()) {
            return null;
        }
        return currentResults.get(modelRow);
    }

    private static final class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        private SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }
}
