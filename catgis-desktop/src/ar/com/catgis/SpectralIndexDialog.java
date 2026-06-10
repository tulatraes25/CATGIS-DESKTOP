package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.LocalRasterData;
import org.locationtech.jts.geom.Envelope;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for computing spectral indices from real raster layer bands.
 * Integrates with MapPanel's raster display system.
 */
public class SpectralIndexDialog extends JDialog {

    private final JComboBox<String> indexCombo;
    private final JComboBox<String> rasterLayerCombo;
    private final JSpinner bandASpinner;
    private final JSpinner bandBSpinner;
    private final JTextArea descriptionArea;
    private final JLabel statusLabel;
    private final List<SpectralIndexEngine.SpectralIndex> indices;

    public SpectralIndexDialog() {
        super((Frame) null, "Indices Espectrales", false);
        setSize(600, 500);
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
        form.setBorder(new EmptyBorder(4, 8, 4, 8));
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

        // Raster layer selector
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Capa raster:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        rasterLayerCombo = new JComboBox<>();
        refreshRasterLayers();
        form.add(rasterLayerCombo, gbc);

        // Band A
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Banda A:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        bandASpinner = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
        form.add(bandASpinner, gbc);

        // Band B
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Banda B:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        bandBSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        form.add(bandBSpinner, gbc);

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
        JButton computeButton = new JButton("Calcular indice");
        computeButton.addActionListener(e -> computeIndex());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(computeButton);
        footer.add(closeButton);
        statusLabel = new JLabel("Selecciona una capa y bandas.");
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
                + "\nPaleta: " + SpectralIndexEngine.getColorRampName(si.id()));
    }

    private void refreshRasterLayers() {
        rasterLayerCombo.removeAllItems();
        if (CatgisDesktopApp.mapPanel == null) return;
        for (Layer layer : CatgisDesktopApp.mapPanel.getRenderOrderLayers()) {
            if (layer instanceof RasterLayer) {
                rasterLayerCombo.addItem(layer.getName());
            }
        }
    }

    private void computeIndex() {
        int idx = indexCombo.getSelectedIndex();
        if (idx < 0 || idx >= indices.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona un indice.");
            return;
        }

        String layerName = (String) rasterLayerCombo.getSelectedItem();
        if (layerName == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una capa raster.");
            return;
        }

        int bandA = (int) bandASpinner.getValue();
        int bandB = (int) bandBSpinner.getValue();

        // Find the raster layer
        Layer targetLayer = null;
        for (Layer layer : CatgisDesktopApp.mapPanel.getRenderOrderLayers()) {
            if (layer.getName().equals(layerName) && layer instanceof RasterLayer) {
                targetLayer = layer;
                break;
            }
        }
        if (targetLayer == null) return;

        // Get raster data
        LocalRasterData data = CatgisDesktopApp.mapPanel.getRasterData(targetLayer);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "No hay datos raster para esta capa.");
            return;
        }

        int bandCount = data.getBandCount();
        if (bandCount < 2) {
            JOptionPane.showMessageDialog(this,
                    "La capa solo tiene " + bandCount + " banda(s). Se necesitan al menos 2 bandas para calcular indices.",
                    "Bandas insuficientes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BufferedImage image = data.getImage();
        if (image == null) {
            JOptionPane.showMessageDialog(this, "No se pudo leer la imagen raster.");
            return;
        }

        int actualBands = image.getRaster().getNumBands();
        if (bandA > actualBands || bandB > actualBands) {
            JOptionPane.showMessageDialog(this,
                    "La imagen tiene " + actualBands + " bandas. Selecciona bandas entre 1 y " + actualBands + ".",
                    "Bandas invalidas", JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("Calculando " + indices.get(idx).id() + "...");
        statusLabel.setForeground(Color.BLUE);

        // Extract bands (convert 1-based to 0-based)
        BufferedImage bandAImage = extractBand(image, bandA - 1);
        BufferedImage bandBImage = extractBand(image, bandB - 1);

        // Compute index
        SpectralIndexEngine.SpectralIndex si = indices.get(idx);
        BufferedImage result = SpectralIndexEngine.computeIndex(bandAImage, bandBImage, si.id());

        if (result == null) {
            JOptionPane.showMessageDialog(this, "Error al calcular el indice.");
            return;
        }

        // Add result as new raster layer
        String resultName = si.id() + "_" + layerName;
        Envelope envelope = data.getEnvelope();
        LocalRasterData resultData = new LocalRasterData(result, envelope, 1, true, data.getSourceCRS());
        Layer resultLayer = new RasterLayer(resultName, "");
        CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(resultLayer, resultData);

        statusLabel.setText("Indice " + si.id() + " calculado y agregado como capa.");
        statusLabel.setForeground(new Color(0, 128, 0));

        JOptionPane.showMessageDialog(this,
                "Indice " + si.name() + " calculado exitosamente.\n\n"
                        + "Formula: " + si.formula() + "\n"
                        + "Capa: " + layerName + "\n"
                        + "Banda A: " + bandA + ", Banda B: " + bandB + "\n"
                        + "Dimensiones: " + result.getWidth() + " x " + result.getHeight() + "\n\n"
                        + "Se agrego como nueva capa: " + resultName,
                "Exito", JOptionPane.INFORMATION_MESSAGE);
    }

    private BufferedImage extractBand(BufferedImage source, int bandIndex) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage band = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Raster src = source.getRaster();
        WritableRaster dst = band.getRaster();
        double[] pixel = new double[src.getNumBands()];
        double[] out = new double[1];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                src.getPixel(x, y, pixel);
                int idx = Math.min(bandIndex, pixel.length - 1);
                out[0] = pixel[idx];
                dst.setPixel(x, y, out);
            }
        }
        return band;
    }
}
