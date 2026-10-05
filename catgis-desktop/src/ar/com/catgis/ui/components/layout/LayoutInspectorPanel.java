package ar.com.catgis.ui.components.layout;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * CATMAP element inspector panel with fields for position, size, text, font, etc.
 * Extracted from {@code buildCatmapInspectorPanel()} and {@code labeledMiniField()}.
 */
public class LayoutInspectorPanel extends JPanel {

    private static JPanel labeledMiniField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setOpaque(false);
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static void addField(JPanel panel, GridBagConstraints gc, String labelText, java.awt.Component field) {
        panel.add(new JLabel(labelText + ":"), gc);
        gc.gridy++;
        panel.add(field, gc);
    }

    public LayoutInspectorPanel(
            JLabel typeValueLabel,
            JTextField labelField,
            JTextField xField,
            JTextField yField,
            JTextField widthField,
            JTextField heightField,
            JTextArea textArea,
            JTextField imagePathField,
            JSpinner fontSizeSpinner,
            JSpinner lineWidthSpinner,
            JComboBox<?> alignCombo,
            JCheckBox boldCheck,
            JCheckBox italicCheck,
            JCheckBox visibleCheck,
            JCheckBox lockedCheck,
            Runnable onApply,
            Runnable onEditFull,
            Runnable onToggleVisibility,
            Runnable onToggleLock) {
        super(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Inspector CATMAP"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        addField(this, gc, "Tipo", typeValueLabel);
        gc.gridy += 2;
        addField(this, gc, "Etiqueta", labelField);
        gc.gridy += 2;

        JPanel positionRow = new JPanel(new GridBagLayout());
        positionRow.setOpaque(false);
        GridBagConstraints pc = new GridBagConstraints();
        pc.insets = new Insets(0, 0, 0, 6);
        pc.gridx = 0;
        pc.gridy = 0;
        pc.fill = GridBagConstraints.HORIZONTAL;
        pc.weightx = 1;
        positionRow.add(labeledMiniField("X", xField), pc);
        pc.gridx++;
        positionRow.add(labeledMiniField("Y", yField), pc);
        pc.gridx++;
        positionRow.add(labeledMiniField("W", widthField), pc);
        pc.gridx++;
        pc.insets = new Insets(0, 0, 0, 0);
        positionRow.add(labeledMiniField("H", heightField), pc);
        addField(this, gc, "Pos / tam", positionRow);
        gc.gridy += 2;

        addField(this, gc, "Texto", new JScrollPane(textArea));
        gc.gridy += 2;
        addField(this, gc, "Imagen", imagePathField);
        gc.gridy += 2;
        addField(this, gc, "Tam. fuente", fontSizeSpinner);
        gc.gridy += 2;
        addField(this, gc, "Grosor", lineWidthSpinner);
        gc.gridy += 2;
        addField(this, gc, "Alineacion", alignCombo);
        gc.gridy += 2;

        JPanel styleChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        styleChecks.setOpaque(false);
        styleChecks.add(boldCheck);
        styleChecks.add(italicCheck);
        add(styleChecks, gc);
        gc.gridy++;

        JPanel stateChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stateChecks.setOpaque(false);
        stateChecks.add(visibleCheck);
        stateChecks.add(lockedCheck);
        add(stateChecks, gc);
        gc.gridy++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        JButton applyButton = new JButton("Aplicar inspector");
        applyButton.addActionListener(e -> onApply.run());
        JButton advancedButton = new JButton("Editor completo...");
        advancedButton.addActionListener(e -> onEditFull.run());
        JButton toggleVisibilityButton = new JButton("Mostrar/ocultar");
        toggleVisibilityButton.addActionListener(e -> onToggleVisibility.run());
        JButton toggleLockButton = new JButton("Bloquear/liberar");
        toggleLockButton.addActionListener(e -> onToggleLock.run());
        actions.add(applyButton);
        actions.add(advancedButton);
        actions.add(toggleVisibilityButton);
        actions.add(toggleLockButton);
        add(actions, gc);
    }
}
