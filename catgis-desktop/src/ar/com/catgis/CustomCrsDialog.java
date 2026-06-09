package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for defining custom Coordinate Reference Systems.
 * Supports WKT, PROJ4, and EPSG codes.
 */
public class CustomCrsDialog extends JDialog {

    private final JTextField crsField;
    private final JTextArea previewArea;
    private final JLabel statusLabel;

    public CustomCrsDialog() {
        super((Frame) null, "Definir CRS Personalizado", false);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("Definir Sistema de Coordenadas de Referencia");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Ingresa un codigo EPSG, WKT, o PROJ4");
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new BorderLayout(4, 4));
        form.setBorder(new EmptyBorder(4, 8, 4, 8));

        crsField = new JTextField();
        crsField.setToolTipText("EPSG:4326, PROJ4:+proj=longlat +datum=WGS84, o WKT completo");
        form.add(crsField, BorderLayout.NORTH);

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        previewArea.setBorder(BorderFactory.createTitledBorder("Vista previa del CRS"));
        form.add(previewArea, BorderLayout.CENTER);

        add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton validateButton = new JButton("Validar");
        validateButton.addActionListener(e -> validateCrs());
        JButton okButton = new JButton("Aplicar");
        okButton.addActionListener(e -> applyCrs());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(validateButton);
        footer.add(okButton);
        footer.add(closeButton);
        statusLabel = new JLabel(" ");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);

        // Examples
        JTextArea examples = new JTextArea();
        examples.setEditable(false);
        examples.setRows(4);
        examples.setText("Ejemplos:\n"
                + "  EPSG:4326 — WGS 84\n"
                + "  +proj=longlat +datum=WGS84 +no_defs — PROJ4\n"
                + "  GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",...]] — WKT");
        examples.setBorder(BorderFactory.createTitledBorder("Ejemplos"));
        add(examples, BorderLayout.SOUTH);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new CustomCrsDialog().setVisible(true));
    }

    private void validateCrs() {
        String input = crsField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un codigo CRS.");
            return;
        }

        try {
            String normalized = CRSDefinitions.normalizeCode(input);
            previewArea.setText("CRS normalizado: " + normalized + "\n\n");
            previewArea.append("Tipo: " + (CRSDefinitions.isManualDefinition(input) ? "Manual (WKT/PROJ4)" : "EPSG") + "\n");
            previewArea.append("Etiqueta: " + CRSDefinitions.getLabelForCode(normalized) + "\n");
            statusLabel.setText("CRS valido");
            statusLabel.setForeground(new Color(0, 128, 0));
        } catch (Exception e) {
            previewArea.setText("Error al validar:\n" + e.getMessage());
            statusLabel.setText("CRS invalido");
            statusLabel.setForeground(Color.RED);
        }
    }

    private void applyCrs() {
        String input = crsField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un codigo CRS.");
            return;
        }

        try {
            String normalized = CRSDefinitions.normalizeCode(input);
            if (CatgisDesktopApp.currentProject != null) {
                CatgisDesktopApp.currentProject.setProjectCRS(normalized);
            }
            statusLabel.setText("CRS aplicado: " + normalized);
            statusLabel.setForeground(new Color(0, 128, 0));
            JOptionPane.showMessageDialog(this,
                    "CRS aplicado al proyecto:\n" + normalized + "\n\n"
                    + "Etiqueta: " + CRSDefinitions.getLabelForCode(normalized),
                    "CRS Aplicado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al aplicar CRS:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
