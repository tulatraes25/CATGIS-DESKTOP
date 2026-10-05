package ar.com.catgis.layout;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for inspecting and editing properties of a selected LayoutElement.
 * Provides fields for position, size, alignment, colors, font, etc.
 */
public class ElementInspectorPanel extends JPanel {

    private final LayoutModel model;
    private final CanvasRenderer renderer;
    private JTextField xField, yField, wField, hField;
    private JTextField nameField;
    private JCheckBox visibleCheck, lockedCheck;
    private JButton colorBtn, borderColorBtn, bgColorBtn;

    public ElementInspectorPanel(LayoutModel model, CanvasRenderer renderer) {
        this.model = model;
        this.renderer = renderer;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Propiedades"));
    }

    /**
     * Load properties from the currently selected element.
     */
    public void refresh() {
        LayoutElement sel = model.getSelected();
        if (sel == null) {
            setVisible(false);
            return;
        }
        setVisible(true);
        if (nameField != null) nameField.setText(sel.getName());
        if (xField != null) xField.setText(formatMm(sel.getBoundsMm().x));
        if (yField != null) yField.setText(formatMm(sel.getBoundsMm().y));
        if (wField != null) wField.setText(formatMm(sel.getBoundsMm().width));
        if (hField != null) hField.setText(formatMm(sel.getBoundsMm().height));
        if (visibleCheck != null) visibleCheck.setSelected(sel.isVisible());
        if (lockedCheck != null) lockedCheck.setSelected(sel.isLocked());
    }

    /**
     * Apply edited values back to the selected element.
     */
    public void apply() {
        LayoutElement sel = model.getSelected();
        if (sel == null) return;
        try {
            if (nameField != null) sel.setName(nameField.getText().trim());
            double x = xField != null ? parseMm(xField.getText()) : sel.getBoundsMm().x;
            double y = yField != null ? parseMm(yField.getText()) : sel.getBoundsMm().y;
            double w = wField != null ? parseMm(wField.getText()) : sel.getBoundsMm().width;
            double h = hField != null ? parseMm(hField.getText()) : sel.getBoundsMm().height;
            sel.setBoundsMm(x, y, Math.max(5, w), Math.max(5, h));
            if (visibleCheck != null) sel.setVisible(visibleCheck.isSelected());
            if (lockedCheck != null) sel.setLocked(lockedCheck.isSelected());
            model.saveSnapshot();
        } catch (NumberFormatException ignored) {}
    }

    private static String formatMm(double v) { return String.format("%.1f", v); }
    private static double parseMm(String s) { return Double.parseDouble(s.trim()); }
}
