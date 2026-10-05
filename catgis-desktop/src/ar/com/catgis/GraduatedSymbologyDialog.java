package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for configuring graduated (classified) symbology.
 * Lets the user pick a numeric field, classification method, number of classes,
 * color ramp, and preview the resulting ranges.
 */
public class GraduatedSymbologyDialog extends JDialog {

    private final Layer layer;
    private final ShapefileData data;
    private final boolean pointMode;
    private final boolean polygonMode;
    private final GraduatedSymbology workingCopy;

    private final JComboBox<String> fieldCombo;
    private final JSpinner classSpinner;
    private final JComboBox<String> methodCombo;
    private final JButton startColorBtn;
    private final JButton endColorBtn;
    private final JPanel previewPanel;
    private final JLabel statusLabel;
    private boolean accepted = false;

    private static final Color[] PRESET_RAMPS = {
        new Color(239, 243, 255), new Color(30, 64, 175),  // Blues
        new Color(255, 247, 236), new Color(180, 60, 10),  // Oranges
        new Color(247, 252, 245), new Color(0, 100, 0),    // Greens
        new Color(252, 251, 253), new Color(100, 10, 100), // Purples
        new Color(255, 245, 240), new Color(120, 10, 10),  // Reds
        new Color(240, 240, 240), new Color(30, 30, 30),   // Grays
    };

    public GraduatedSymbologyDialog(Frame owner, Layer layer, ShapefileData data) {
        super(owner, "Simbologia graduada", true);
        this.layer = layer;
        this.data = data;
        String geomFamily = resolveGeometryFamily();
        this.pointMode = "POINT".equalsIgnoreCase(geomFamily);
        this.polygonMode = "POLYGON".equalsIgnoreCase(geomFamily);
        this.workingCopy = copyOf(resolveTargetSymbology());

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(new Color(246, 248, 251));

        // --- Top: field + method config ---
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        configPanel.add(new JLabel("Campo numerico:"), g);

        fieldCombo = new JComboBox<>();
        if (data != null) {
            for (String name : data.getAttributeNames()) {
                fieldCombo.addItem(name);
            }
        }
        if (workingCopy.getFieldName() != null && !workingCopy.getFieldName().isBlank()) {
            fieldCombo.setSelectedItem(workingCopy.getFieldName());
        }
        g.gridx = 1;
        configPanel.add(fieldCombo, g);

        g.gridx = 0; g.gridy = 1;
        configPanel.add(new JLabel("Clases:"), g);
        classSpinner = new JSpinner(new SpinnerNumberModel(workingCopy.getNumClasses(), 2, 20, 1));
        g.gridx = 1;
        configPanel.add(classSpinner, g);

        g.gridx = 0; g.gridy = 2;
        configPanel.add(new JLabel("Metodo:"), g);
        methodCombo = new JComboBox<>(new String[]{
            GraduatedSymbology.ClassificationMethod.EQUAL_INTERVAL.getDisplayName(),
            GraduatedSymbology.ClassificationMethod.QUANTILE.getDisplayName(),
            GraduatedSymbology.ClassificationMethod.NATURAL_BREAKS.getDisplayName()
        });
        methodCombo.setSelectedIndex(workingCopy.getMethod().ordinal());
        g.gridx = 1;
        configPanel.add(methodCombo, g);

        g.gridx = 0; g.gridy = 3;
        configPanel.add(new JLabel("Rampa inicio:"), g);
        startColorBtn = new JButton("     ");
        startColorBtn.setBackground(workingCopy.getRampStartColor());
        startColorBtn.setOpaque(true);
        startColorBtn.setBorderPainted(false);
        startColorBtn.setPreferredSize(new Dimension(50, 22));
        startColorBtn.addActionListener(e -> pickColor(startColorBtn));
        g.gridx = 1;
        configPanel.add(startColorBtn, g);

        g.gridx = 0; g.gridy = 4;
        configPanel.add(new JLabel("Rampa fin:"), g);
        endColorBtn = new JButton("     ");
        endColorBtn.setBackground(workingCopy.getRampEndColor());
        endColorBtn.setOpaque(true);
        endColorBtn.setBorderPainted(false);
        endColorBtn.setPreferredSize(new Dimension(50, 22));
        endColorBtn.addActionListener(e -> pickColor(endColorBtn));
        g.gridx = 1;
        configPanel.add(endColorBtn, g);

        // Ramp presets
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        presetsPanel.setOpaque(false);
        presetsPanel.add(new JLabel("Rapidos:"));
        for (int i = 0; i < PRESET_RAMPS.length; i += 2) {
            Color start = PRESET_RAMPS[i];
            Color end = PRESET_RAMPS[i + 1];
            JButton presetBtn = new JButton();
            presetBtn.setPreferredSize(new Dimension(36, 18));
            // Draw mini gradient
            presetBtn.setIcon(new Icon() {
                @Override public void paintIcon(Component c, Graphics g2, int x, int y) {
                    Graphics2D g = (Graphics2D) g2.create();
                    int w = presetBtn.getWidth();
                    int h = presetBtn.getHeight();
                    for (int px = 0; px < w; px++) {
                        float ratio = (float) px / w;
                        Color col = interpolateColor(start, end, ratio);
                        g.setColor(col);
                        g.drawLine(x + px, y, x + px, y + h);
                    }
                    g.dispose();
                }
                @Override public int getIconWidth() { return 36; }
                @Override public int getIconHeight() { return 18; }
            });
            int rampIdx = i / 2;
            presetBtn.addActionListener(e -> {
                startColorBtn.setBackground(PRESET_RAMPS[rampIdx * 2]);
                endColorBtn.setBackground(PRESET_RAMPS[rampIdx * 2 + 1]);
                classifyAndPreview();
            });
            presetsPanel.add(presetBtn);
        }
        configPanel.add(presetsPanel, g);

        // Classify button
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2;
        JButton classifyBtn = new JButton("Clasificar");
        classifyBtn.setFont(classifyBtn.getFont().deriveFont(Font.BOLD));
        classifyBtn.addActionListener(e -> classifyAndPreview());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);
        btnPanel.add(classifyBtn);
        configPanel.add(btnPanel, g);

        // --- Center: preview of ranges ---
        previewPanel = new JPanel(new GridBagLayout());
        previewPanel.setOpaque(false);
        JScrollPane scrollPreview = new JScrollPane(previewPanel);
        scrollPreview.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Vista previa", TitledBorder.LEADING, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 11)));
        scrollPreview.setPreferredSize(new Dimension(400, 200));
        scrollPreview.setOpaque(false);

        // --- Bottom: OK/Cancel ---
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(71, 85, 105));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnRow.setOpaque(false);
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());
        JButton okBtn = new JButton("Aceptar");
        okBtn.setFont(okBtn.getFont().deriveFont(Font.BOLD));
        okBtn.addActionListener(e -> accept());
        btnRow.add(cancelBtn);
        btnRow.add(okBtn);
        bottomPanel.add(btnRow, BorderLayout.EAST);

        // Assemble
        add(configPanel, BorderLayout.NORTH);
        add(scrollPreview, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(520, 500);
        setLocationRelativeTo(owner);

        // Auto-classify on open if already configured
        if (workingCopy.isConfigured() && workingCopy.getNumClasses() > 0) {
            restorePreviewFromWorkingCopy();
        }
    }

    // ========== Actions ==========

    private void pickColor(JButton btn) {
        Color c = JColorChooser.showDialog(this, "Seleccionar color", btn.getBackground());
        if (c != null) {
            btn.setBackground(c);
        }
    }

    private void classifyAndPreview() {
        if (data == null) return;

        String fieldName = (String) fieldCombo.getSelectedItem();
        if (fieldName == null || fieldName.isBlank()) {
            statusLabel.setText("Selecciona un campo numerico.");
            return;
        }

        // Collect numeric values
        List<Double> values = new ArrayList<>();
        try (org.geotools.feature.FeatureIterator<org.geotools.api.feature.simple.SimpleFeature> it =
                data.getFeatureCollection().features()) {
            while (it.hasNext()) {
                org.geotools.api.feature.simple.SimpleFeature f = it.next();
                Object v = f.getAttribute(fieldName);
                if (v != null) {
                    try {
                        values.add(Double.parseDouble(String.valueOf(v)));
                    } catch (Exception ignored) { CatgisLogger.warn("GraduatedSymbologyDialog: operation failed", ignored); }
                }
            }
        } catch (Exception ex) {
            statusLabel.setText("Error al leer datos: " + ex.getMessage());
            return;
        }

        if (values.isEmpty()) {
            statusLabel.setText("No se encontraron valores numericos en el campo seleccionado.");
            return;
        }

        workingCopy.setFieldName(fieldName);
        workingCopy.setNumClasses((Integer) classSpinner.getValue());
        workingCopy.setMethod(GraduatedSymbology.ClassificationMethod.values()[methodCombo.getSelectedIndex()]);
        workingCopy.setRampStartColor(startColorBtn.getBackground());
        workingCopy.setRampEndColor(endColorBtn.getBackground());

        workingCopy.classify(values);
        refreshPreview(workingCopy.getRules());
        statusLabel.setText(workingCopy.getRules().size() + " clases generadas con " + values.size() + " valores.");
    }

    private void restorePreviewFromWorkingCopy() {
        fieldCombo.setSelectedItem(workingCopy.getFieldName());
        classSpinner.setValue(workingCopy.getNumClasses());
        methodCombo.setSelectedIndex(workingCopy.getMethod().ordinal());
        startColorBtn.setBackground(workingCopy.getRampStartColor());
        endColorBtn.setBackground(workingCopy.getRampEndColor());
        refreshPreview(workingCopy.getRules());
        statusLabel.setText(workingCopy.getRules().size() + " clases configuradas.");
    }

    private void refreshPreview(java.util.List<GraduatedRangeRule> rules) {
        previewPanel.removeAll();
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 8, 2, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // Header
        g.gridx = 0; g.gridy = 0;
        JLabel rangeHdr = new JLabel("Rango");
        rangeHdr.setFont(rangeHdr.getFont().deriveFont(Font.BOLD, 11f));
        previewPanel.add(rangeHdr, g);
        g.gridx = 1;
        JLabel colorHdr = new JLabel("Color");
        colorHdr.setFont(colorHdr.getFont().deriveFont(Font.BOLD, 11f));
        previewPanel.add(colorHdr, g);

        for (int i = 0; i < rules.size(); i++) {
            GraduatedRangeRule rule = rules.get(i);
            g.gridy = i + 1;
            g.gridx = 0;

            JLabel rangeLabel = new JLabel(rule.getLabel());
            rangeLabel.setFont(rangeLabel.getFont().deriveFont(11f));
            previewPanel.add(rangeLabel, g);

            g.gridx = 1;
            JPanel colorSwatch = new JPanel();
            colorSwatch.setBackground(rule.getPrimaryColor());
            colorSwatch.setOpaque(true);
            colorSwatch.setPreferredSize(new Dimension(28, 16));
            colorSwatch.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            previewPanel.add(colorSwatch, g);
        }

        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void accept() {
        // Run classification before accepting
        if (!workingCopy.isConfigured() || workingCopy.getRules().isEmpty()) {
            classifyAndPreview();
            if (workingCopy.getRules().isEmpty()) {
                boolean ok = NotificationManager.confirm(this,
                    "Simbologia graduada",
                    "No hay clases generadas. ¿Guardar configuracion vacia?");
                if (!ok) return;
            }
        }
        saveToLayer();
        accepted = true;
        dispose();
    }

    private void saveToLayer() {
        GraduatedSymbology target = resolveTargetSymbology();
        target.setFieldName(workingCopy.getFieldName());
        target.setNumClasses(workingCopy.getNumClasses());
        target.setMethod(workingCopy.getMethod());
        target.setRampStartColor(workingCopy.getRampStartColor());
        target.setRampEndColor(workingCopy.getRampEndColor());
        target.clearRules();
        for (GraduatedRangeRule rule : workingCopy.getRules()) {
            target.getRules().add(rule);
        }
    }

    public boolean isAccepted() { return accepted; }

    // ========== Helpers ==========

    private String resolveGeometryFamily() {
        if (data == null || data.getFeatureCollection() == null) return "POINT";
        String geomType = data.getFeatureCollection().getSchema()
            .getGeometryDescriptor().getType().getBinding().getSimpleName();
        if (geomType == null) return "POINT";
        String upper = geomType.toUpperCase();
        if (upper.contains("POLYGON") || upper.contains("MULTIPOLYGON")) return "POLYGON";
        if (upper.contains("LINE") || upper.contains("MULTILINESTRING")) return "LINE";
        return "POINT";
    }

    private GraduatedSymbology resolveTargetSymbology() {
        if (pointMode) return layer.getPointGraduatedSymbology();
        if (polygonMode) return layer.getPolygonGraduatedSymbology();
        return layer.getLineGraduatedSymbology();
    }

    private static GraduatedSymbology copyOf(GraduatedSymbology original) {
        GraduatedSymbology copy = new GraduatedSymbology();
        copy.setFieldName(original.getFieldName());
        copy.setNumClasses(original.getNumClasses());
        copy.setMethod(original.getMethod());
        copy.setRampStartColor(original.getRampStartColor());
        copy.setRampEndColor(original.getRampEndColor());
        for (GraduatedRangeRule rule : original.getRules()) {
            copy.getRules().add(rule);
        }
        return copy;
    }

    private static Color interpolateColor(Color a, Color b, float ratio) {
        float r = a.getRed() / 255f + (b.getRed() / 255f - a.getRed() / 255f) * ratio;
        float g = a.getGreen() / 255f + (b.getGreen() / 255f - a.getGreen() / 255f) * ratio;
        float bl = a.getBlue() / 255f + (b.getBlue() / 255f - a.getBlue() / 255f) * ratio;
        return new Color(Math.round(r * 255), Math.round(g * 255), Math.round(bl * 255));
    }

    /** Convenience: open dialog and return whether user accepted. */
    public static boolean open(Component owner, Layer layer) {
        ShapefileData data = AppContext.mapPanel() != null
            ? AppContext.mapPanel().getShapefileData(layer) : null;
        if (data == null) {
            data = VectorLayerUtils.ensureVectorData(layer);
        }
        if (data == null) {
            NotificationManager.warn(owner,
                "Simbologia graduada",
                "No se pudieron cargar los datos de la capa.");
            return false;
        }
        Frame frame = owner instanceof Frame ? (Frame) owner :
            (Frame) SwingUtilities.getWindowAncestor(owner);
        GraduatedSymbologyDialog dlg = new GraduatedSymbologyDialog(frame, layer, data);
        dlg.setVisible(true);
        return dlg.isAccepted();
    }
}
