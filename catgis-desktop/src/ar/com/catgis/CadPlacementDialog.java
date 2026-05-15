package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class CadPlacementDialog extends JDialog {

    private final Layer layer;
    private final JSpinner offsetXSpinner;
    private final JSpinner offsetYSpinner;
    private final JSpinner scaleSpinner;
    private final JSpinner rotationSpinner;
    private CadPlacementSupport.Result result = new CadPlacementSupport.Result(false, 0, 0, 1, 0);

    public CadPlacementDialog(Frame owner, Layer layer) {
        super(owner, "Ajuste geografico CAD", true);
        this.layer = layer;
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        header.setBackground(new Color(247, 250, 252));
        JLabel title = new JLabel("Ajuste fino de referencia CAD");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitle = new JLabel("<html>Este ajuste se aplica sobre toda la referencia CAD despues del CRS. "
                + "Sirve para mover, escalar o rotar el dibujo sin deformarlo por partes.</html>");
        subtitle.setForeground(new Color(75, 85, 99));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        offsetXSpinner = new JSpinner(new SpinnerNumberModel(layer != null ? layer.getCadOffsetX() : 0d, -1_000_000_000d, 1_000_000_000d, 1d));
        offsetYSpinner = new JSpinner(new SpinnerNumberModel(layer != null ? layer.getCadOffsetY() : 0d, -1_000_000_000d, 1_000_000_000d, 1d));
        scaleSpinner = new JSpinner(new SpinnerNumberModel(layer != null ? layer.getCadScale() : 1d, 0.000001d, 1_000_000d, 0.001d));
        rotationSpinner = new JSpinner(new SpinnerNumberModel(layer != null ? layer.getCadRotationDegrees() : 0d, -360d, 360d, 0.1d));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 0, 4, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(new JLabel("Desplazamiento X"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(offsetXSpinner, gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0;
        form.add(new JLabel("Desplazamiento Y"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(offsetYSpinner, gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0;
        form.add(new JLabel("Escala global"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(scaleSpinner, gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0;
        form.add(new JLabel("Rotacion (grados)"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(rotationSpinner, gc);

        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 2;
        gc.weighty = 1;
        JLabel note = new JLabel("<html><b>Tip:</b> si el CAD ya cae cerca pero no exacto, primero defini el CRS correcto y despues ajusta aqui.</html>");
        note.setForeground(new Color(75, 85, 99));
        form.add(note, gc);

        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton reset = new JButton("Resetear");
        reset.addActionListener(e -> {
            offsetXSpinner.setValue(0d);
            offsetYSpinner.setValue(0d);
            scaleSpinner.setValue(1d);
            rotationSpinner.setValue(0d);
        });
        JButton dragOnMap = new JButton("Arrastrar en mapa");
        dragOnMap.addActionListener(e -> {
            dispose();
            CadWorkflowSupport.openCadDragPlacementWorkflow(owner, this.layer);
        });
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> applyAndClose());
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> dispose());
        buttons.add(reset);
        buttons.add(dragOnMap);
        buttons.add(cancel);
        buttons.add(apply);
        add(buttons, BorderLayout.SOUTH);

        setMinimumSize(new java.awt.Dimension(620, 340));
        pack();
        setLocationRelativeTo(owner);
    }

    public static CadPlacementSupport.Result open(Frame owner, Layer layer) {
        CadPlacementDialog dialog = new CadPlacementDialog(owner, layer);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void applyAndClose() {
        double scale = ((Number) scaleSpinner.getValue()).doubleValue();
        if (scale <= 0d) {
            JOptionPane.showMessageDialog(this, "La escala debe ser mayor que cero.", "Ajuste CAD", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result = new CadPlacementSupport.Result(
                true,
                ((Number) offsetXSpinner.getValue()).doubleValue(),
                ((Number) offsetYSpinner.getValue()).doubleValue(),
                scale,
                ((Number) rotationSpinner.getValue()).doubleValue()
        );
        dispose();
    }
}
