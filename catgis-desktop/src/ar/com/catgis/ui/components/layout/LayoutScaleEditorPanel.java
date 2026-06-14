package ar.com.catgis.ui.components.layout;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Scale editor with a text field and "Aplicar" button.
 * Extracted from {@code buildScaleEditorPanel()}.
 */
public class LayoutScaleEditorPanel extends JPanel {

    public LayoutScaleEditorPanel(JTextField mapScaleField, Runnable onApplyScale) {
        super(new BorderLayout(6, 0));
        setOpaque(false);

        mapScaleField.setToolTipText("Introduce la escala objetivo del frame de mapa. Ejemplo: 1:5000");
        mapScaleField.addActionListener(e -> onApplyScale.run());
        JButton applyButton = new JButton("Aplicar");
        applyButton.setToolTipText("Ajusta el mapa dentro de CATMAP a la escala indicada.");
        applyButton.addActionListener(e -> onApplyScale.run());

        add(mapScaleField, BorderLayout.CENTER);
        add(applyButton, BorderLayout.EAST);
    }
}
