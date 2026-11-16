package ar.com.catgis.catmap.ui;

import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

/**
 * CATMAP "Configure page" dialog: A3/A4/A5/Letter/Legal/Tabloid or custom
 * millimetres, orientation and print margins.
 *
 * <p>The pure {@link #buildSpec} method is unit-tested headlessly; the Swing
 * shell only collects the values.</p>
 */
public final class PageSetupDialog {

    static final String CUSTOM_LABEL = "Personalizado";

    private PageSetupDialog() {}

    /**
     * Validate dialog inputs into a document page spec.
     *
     * @param preset      selected preset, or {@code null} for custom millimetres
     * @param orientation landscape or portrait
     * @param customWidthMm  portrait base width for the custom sheet
     * @param customHeightMm portrait base height for the custom sheet
     */
    public static LayoutPageSpec buildSpec(PageSizePreset preset,
                                           PageOrientation orientation,
                                           double customWidthMm,
                                           double customHeightMm,
                                           double marginTopMm,
                                           double marginRightMm,
                                           double marginBottomMm,
                                           double marginLeftMm) {
        if (orientation == null) {
            throw new IllegalArgumentException("CATMAP requiere una orientacion de pagina.");
        }
        LayoutPageSpec spec;
        if (preset != null) {
            spec = LayoutPageSpec.preset(preset, orientation);
        } else {
            spec = LayoutPageSpec.custom(customWidthMm, customHeightMm, orientation);
        }
        if (spec.hasMargins() || marginTopMm > 0 || marginRightMm > 0
                || marginBottomMm > 0 || marginLeftMm > 0) {
            spec = spec.withMargins(marginTopMm, marginRightMm, marginBottomMm, marginLeftMm);
        }
        return spec;
    }

    /**
     * Show the dialog; returns the accepted spec or {@code null} when cancelled.
     * The caller applies it to the layout model and refreshes the preview.
     */
    public static LayoutPageSpec showDialog(Component parent, LayoutPageSpec current) {
        LayoutPageSpec initial = current != null ? current : LayoutPageSpec.A4_LANDSCAPE;
        JDialog dialog = new JDialog(
                parent instanceof java.awt.Frame frame ? frame : null,
                "Configurar pagina", true);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] presetLabels = new String[PageSizePreset.values().length + 1];
        for (int i = 0; i < PageSizePreset.values().length; i++) {
            presetLabels[i] = PageSizePreset.values()[i].toString();
        }
        presetLabels[presetLabels.length - 1] = CUSTOM_LABEL;

        JComboBox<String> presetCombo = new JComboBox<>(presetLabels);
        JComboBox<String> orientationCombo = new JComboBox<>(new String[]{"Horizontal", "Vertical"});
        JTextField widthField = new JTextField(8);
        JTextField heightField = new JTextField(8);
        JTextField topField = new JTextField(6);
        JTextField rightField = new JTextField(6);
        JTextField bottomField = new JTextField(6);
        JTextField leftField = new JTextField(6);
        JLabel error = new JLabel(" ");

        int presetIndex = currentIndex(initial);
        presetCombo.setSelectedIndex(presetIndex);
        orientationCombo.setSelectedIndex(
                initial.orientation() == PageOrientation.PORTRAIT ? 1 : 0);
        widthField.setText(formatMm(initial.portraitWidthMm()));
        heightField.setText(formatMm(initial.portraitHeightMm()));
        topField.setText(formatMm(initial.marginTopMm()));
        rightField.setText(formatMm(initial.marginRightMm()));
        bottomField.setText(formatMm(initial.marginBottomMm()));
        leftField.setText(formatMm(initial.marginLeftMm()));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.anchor = GridBagConstraints.WEST;
        int row = 0;
        addRow(form, c, row++, "Tamano", presetCombo);
        addRow(form, c, row++, "Orientacion", orientationCombo);
        addRow(form, c, row++, "Ancho (mm)", widthField);
        addRow(form, c, row++, "Alto (mm)", heightField);
        addRow(form, c, row++, "Margen superior (mm)", topField);
        addRow(form, c, row++, "Margen derecho (mm)", rightField);
        addRow(form, c, row++, "Margen inferior (mm)", bottomField);
        addRow(form, c, row++, "Margen izquierdo (mm)", leftField);
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        form.add(error, c);

        Runnable refreshCustomFields = () -> {
            PageSizePreset preset = selectedPreset(presetCombo);
            boolean custom = preset == null;
            widthField.setEnabled(custom);
            heightField.setEnabled(custom);
            if (preset != null) {
                widthField.setText(formatMm(preset.widthMm));
                heightField.setText(formatMm(preset.heightMm));
            }
        };
        presetCombo.addActionListener(event -> refreshCustomFields.run());
        refreshCustomFields.run();

        LayoutPageSpec[] result = new LayoutPageSpec[1];
        JButton accept = new JButton("Aceptar");
        accept.addActionListener(event -> {
            try {
                LayoutPageSpec spec = buildSpec(
                        selectedPreset(presetCombo),
                        orientationCombo.getSelectedIndex() == 1
                                ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE,
                        parseMm(widthField, "ancho"),
                        parseMm(heightField, "alto"),
                        parseMm(topField, "margen superior"),
                        parseMm(rightField, "margen derecho"),
                        parseMm(bottomField, "margen inferior"),
                        parseMm(leftField, "margen izquierdo"));
                result[0] = spec;
                dialog.dispose();
            } catch (IllegalArgumentException invalid) {
                String message = invalid.getMessage() != null
                        ? invalid.getMessage() : "Configuracion de pagina invalida.";
                error.setText(message);
            }
        });
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(event -> dialog.dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(accept);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static int currentIndex(LayoutPageSpec spec) {
        if (spec.isCustom()) {
            return PageSizePreset.values().length;
        }
        try {
            return PageSizePreset.valueOf(spec.presetName()).ordinal();
        } catch (IllegalArgumentException unknown) {
            return PageSizePreset.values().length;
        }
    }

    private static PageSizePreset selectedPreset(JComboBox<String> combo) {
        int index = combo.getSelectedIndex();
        PageSizePreset[] values = PageSizePreset.values();
        return index >= 0 && index < values.length ? values[index] : null;
    }

    private static double parseMm(JTextField field, String label) {
        String text = field.getText() == null ? "" : field.getText().trim().replace(',', '.');
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("CATMAP: " + label + " no es un numero valido.");
        }
    }

    private static String formatMm(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static void addRow(JPanel form, GridBagConstraints c, int row,
                               String label, Component field) {
        c.gridy = row;
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(field, c);
    }

    /** Exposed for tests and for a future "apply" preview before accepting. */
    public static void showValidationMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Configurar pagina",
                JOptionPane.WARNING_MESSAGE);
    }
}
