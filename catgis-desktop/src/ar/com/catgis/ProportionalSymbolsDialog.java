package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import javax.swing.*;
import java.awt.*;

/**
 * Simple dialog for configuring proportional symbols.
 */
public class ProportionalSymbolsDialog extends JDialog {

    private final Layer layer;
    private final ShapefileData data;
    private final ProportionalSymbols workingCopy;
    private boolean accepted = false;

    private final JComboBox<String> fieldCombo;
    private final JSpinner minSizeSpinner;
    private final JSpinner maxSizeSpinner;
    private final JCheckBox scaleByAreaCheck;

    public ProportionalSymbolsDialog(Frame owner, Layer layer, ShapefileData data) {
        super(owner, "Simbolos proporcionales", true);
        this.layer = layer;
        this.data = data;
        this.workingCopy = copyOf(layer.getProportionalSymbols());

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(new Color(246, 248, 251));

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        form.add(new JLabel("Campo numerico:"), g);
        fieldCombo = new JComboBox<>();
        if (data != null) {
            for (String name : data.getAttributeNames()) {
                fieldCombo.addItem(name);
            }
        }
        if (workingCopy.getFieldName() != null) fieldCombo.setSelectedItem(workingCopy.getFieldName());
        g.gridx = 1;
        form.add(fieldCombo, g);

        g.gridx = 0; g.gridy = 1;
        form.add(new JLabel("Tamano minimo:"), g);
        minSizeSpinner = new JSpinner(new SpinnerNumberModel(workingCopy.getMinSize(), 2, 100, 1));
        g.gridx = 1;
        form.add(minSizeSpinner, g);

        g.gridx = 0; g.gridy = 2;
        form.add(new JLabel("Tamano maximo:"), g);
        maxSizeSpinner = new JSpinner(new SpinnerNumberModel(workingCopy.getMaxSize(), 4, 200, 1));
        g.gridx = 1;
        form.add(maxSizeSpinner, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        scaleByAreaCheck = new JCheckBox("Escalar por area (radio no lineal)", workingCopy.isScaleByArea());
        form.add(scaleByAreaCheck, g);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        JLabel hint = new JLabel("<html><i>El tamano del simbolo se interpola entre minimo y maximo<br>segun el valor del campo seleccionado.</i></html>");
        hint.setForeground(new Color(100, 110, 120));
        form.add(hint, g);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bottom.setOpaque(false);
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());
        JButton okBtn = new JButton("Aceptar");
        okBtn.setFont(okBtn.getFont().deriveFont(Font.BOLD));
        okBtn.addActionListener(e -> accept());
        bottom.add(cancelBtn);
        bottom.add(okBtn);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setSize(400, 280);
        setLocationRelativeTo(owner);
    }

    private void accept() {
        String field = (String) fieldCombo.getSelectedItem();
        if (field == null || field.isBlank()) {
            workingCopy.setEnabled(false);
            layer.getProportionalSymbols().setEnabled(false);
        } else {
            workingCopy.setFieldName(field);
            workingCopy.setMinSize((Integer) minSizeSpinner.getValue());
            workingCopy.setMaxSize((Integer) maxSizeSpinner.getValue());
            workingCopy.setScaleByArea(scaleByAreaCheck.isSelected());
            workingCopy.setEnabled(true);
            // Auto-calculate min/max values from field data
            autoCalculateRange(field);
            saveToLayer();
        }
        accepted = true;
        dispose();
    }

    private void saveToLayer() {
        ProportionalSymbols target = layer.getProportionalSymbols();
        target.setFieldName(workingCopy.getFieldName());
        target.setMinValue(workingCopy.getMinValue());
        target.setMaxValue(workingCopy.getMaxValue());
        target.setMinSize(workingCopy.getMinSize());
        target.setMaxSize(workingCopy.getMaxSize());
        target.setScaleByArea(workingCopy.isScaleByArea());
        target.setEnabled(workingCopy.isEnabled());
    }

    private void autoCalculateRange(String fieldName) {
        if (data == null) return;
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        try (org.geotools.feature.FeatureIterator<
                org.geotools.api.feature.simple.SimpleFeature> it =
                data.getFeatureCollection().features()) {
            while (it.hasNext()) {
                org.geotools.api.feature.simple.SimpleFeature f = it.next();
                Object v = f.getAttribute(fieldName);
                if (v == null) continue;
                try {
                    double val = Double.parseDouble(String.valueOf(v));
                    min = Math.min(min, val);
                    max = Math.max(max, val);
                } catch (Exception ignored) { CatgisLogger.warn("ProportionalSymbolsDialog: operation failed", ignored); }
            }
        } catch (Exception ignored) { CatgisLogger.warn("ProportionalSymbolsDialog: operation failed", ignored); }
        if (min < Double.MAX_VALUE && max > -Double.MAX_VALUE) {
            workingCopy.setMinValue(min);
            workingCopy.setMaxValue(max);
        }
    }

    public boolean isAccepted() { return accepted; }

    private static ProportionalSymbols copyOf(ProportionalSymbols original) {
        ProportionalSymbols c = new ProportionalSymbols();
        c.setFieldName(original.getFieldName());
        c.setMinValue(original.getMinValue());
        c.setMaxValue(original.getMaxValue());
        c.setMinSize(original.getMinSize());
        c.setMaxSize(original.getMaxSize());
        c.setScaleByArea(original.isScaleByArea());
        c.setEnabled(original.isEnabled());
        return c;
    }

    public static boolean open(Component parent, Layer layer) {
        ShapefileData data = AppContext.mapPanel() != null
            ? AppContext.mapPanel().getShapefileData(layer) : null;
        if (data == null) data = VectorLayerUtils.ensureVectorData(layer);
        if (data == null) {
            NotificationManager.warn(parent,
                "Simbolos proporcionales",
                "No se pudieron cargar los datos de la capa.");
            return false;
        }
        Frame frame = parent instanceof Frame ? (Frame) parent
            : (Frame) SwingUtilities.getWindowAncestor(parent);
        ProportionalSymbolsDialog dlg = new ProportionalSymbolsDialog(frame, layer, data);
        dlg.setVisible(true);
        return dlg.isAccepted();
    }
}
