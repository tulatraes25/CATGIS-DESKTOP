package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog for computing spectral indices from raster layers.
 */
public class SpectralIndexDialog extends JDialog {

    private final JComboBox<String> indexCombo;
    private final JTextArea descriptionArea;
    private final JLabel statusLabel;
    private final List<SpectralIndexEngine.SpectralIndex> indices;

    public SpectralIndexDialog() {
        super((Frame) null, "Indices Espectrales", false);
        setSize(550, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        indices = SpectralIndexEngine.getIndices();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(8, 8, 4, 8));
        JLabel title = new JLabel("Calculo de Indices Espectrales");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(8, 8, 4, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Index selector
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Indice:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        String[] indexNames = indices.stream().map(SpectralIndexEngine.SpectralIndex::name).toArray(String[]::new);
        indexCombo = new JComboBox<>(indexNames);
        indexCombo.addActionListener(e -> onIndexSelected());
        form.add(indexCombo, gbc);

        add(form, BorderLayout.CENTER);

        // Description
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Descripcion y Formula"));
        add(descriptionArea, BorderLayout.SOUTH);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton infoButton = new JButton("Info");
        infoButton.addActionListener(e -> showInfo());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(infoButton);
        footer.add(closeButton);
        statusLabel = new JLabel("Selecciona un indice para ver detalles.");
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);

        if (!indices.isEmpty()) {
            indexCombo.setSelectedIndex(0);
        }
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new SpectralIndexDialog().setVisible(true));
    }

    private void onIndexSelected() {
        int idx = indexCombo.getSelectedIndex();
        if (idx < 0 || idx >= indices.size()) return;
        SpectralIndexEngine.SpectralIndex si = indices.get(idx);
        descriptionArea.setText(si.description()
                + "\n\nFormula: " + si.formula()
                + "\n\nBanda A: " + si.bandA()
                + "\nBanda B: " + si.bandB()
                + "\n\nRango de valores: " + java.util.Arrays.toString(SpectralIndexEngine.getIndexRange(si.id()))
                + "\nPaleta de colores: " + SpectralIndexEngine.getColorRampName(si.id()));
    }

    private void showInfo() {
        JOptionPane.showMessageDialog(this,
                "Los indices espectrales se calculan combinando bandas de imagenes satelitales.\n\n"
                + "Para usar esta herramienta:\n"
                + "1. Selecciona un indice de la lista\n"
                + "2. Indica los numeros de banda (ej: NIR=4, Red=3 para Landsat 8)\n"
                + "3. El indice se calcula pixel por pixel\n"
                + "4. El resultado es un raster de 1 banda con valores normalizados\n\n"
                + "Landsat 8/9 bandas tipicas:\n"
                + "  Banda 2: Blue (490nm)\n"
                + "  Banda 3: Red (665nm)\n"
                + "  Banda 4: NIR (865nm)\n"
                + "  Banda 5: Red Edge (705nm)\n"
                + "  Banda 6: SWIR1 (1610nm)\n"
                + "  Banda 7: SWIR2 (2190nm)\n\n"
                + "Sentinel-2 bandas tipicas:\n"
                + "  Banda 3: Green (560nm)\n"
                + "  Banda 4: Red (665nm)\n"
                + "  Banda 5: Red Edge (705nm)\n"
                + "  Banda 8: NIR (842nm)\n"
                + "  Banda 11: SWIR1 (1610nm)",
                "Guias de uso", JOptionPane.INFORMATION_MESSAGE);
    }
}
