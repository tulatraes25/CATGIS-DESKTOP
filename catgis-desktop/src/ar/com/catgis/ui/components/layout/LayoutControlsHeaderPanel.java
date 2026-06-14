package ar.com.catgis.ui.components.layout;

import ar.com.catgis.AppIcons;
import ar.com.catgis.layout.ScaleRule;
import ar.com.catgis.layout.ScaleStyle;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Fixed controls header panel with title, page/scale settings, and scale editor.
 * Extracted from {@code buildFixedControlsHeaderPanel()}.
 */
public class LayoutControlsHeaderPanel extends JPanel {

    private static void addSection(JPanel panel, GridBagConstraints gc, String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setForeground(new Color(27, 38, 56));
        panel.add(label, gc);
    }

    private static void addField(JPanel panel, GridBagConstraints gc, String labelText, java.awt.Component field) {
        panel.add(new JLabel(labelText + ":"), gc);
        gc.gridy++;
        panel.add(field, gc);
    }

    public LayoutControlsHeaderPanel(
            JComboBox<?> templateCombo,
            JComboBox<?> pageSizeCombo,
            JComboBox<?> orientationCombo,
            JComboBox<?> dpiCombo,
            JComboBox<ScaleStyle> scaleStyleCombo,
            JComboBox<ScaleRule> scaleRuleCombo,
            JCheckBox scaleCheck,
            JLabel scaleInfoLabel,
            LayoutScaleEditorPanel scaleEditor) {
        super(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel header = new JLabel("CATMAP");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        header.setForeground(new Color(27, 38, 56));
        add(header, gc);

        gc.gridy++;
        JLabel info = new JLabel("<html>Modulo cartografico de CATGIS para maquetar, componer y exportar mapas con control visual real.</html>");
        info.setFont(info.getFont().deriveFont(Font.PLAIN, 11.5f));
        info.setForeground(new Color(88, 98, 112));
        add(info, gc);

        gc.gridy++;
        addSection(this, gc, "Pagina y escala");
        gc.gridy++;
        addField(this, gc, "Plantilla de layout", templateCombo);
        gc.gridy += 2;
        addField(this, gc, "Tamano de pagina", pageSizeCombo);
        gc.gridy += 2;
        addField(this, gc, "Orientacion", orientationCombo);
        gc.gridy += 2;
        addField(this, gc, "Resolucion de salida", dpiCombo);
        gc.gridy += 2;
        addField(this, gc, "Tipo de escala", scaleStyleCombo);
        gc.gridy += 2;
        addField(this, gc, "Regla de escala", scaleRuleCombo);
        gc.gridy += 2;
        addField(this, gc, "Escala objetivo (1:n)", scaleEditor);
        gc.gridy += 2;
        scaleInfoLabel.setForeground(new Color(63, 74, 88));
        scaleInfoLabel.setFont(scaleInfoLabel.getFont().deriveFont(Font.PLAIN, 11.5f));
        add(scaleInfoLabel, gc);
        gc.gridy++;
        add(scaleCheck, gc);
        gc.gridy++;
        add(new JLabel(""), gc);
    }
}
