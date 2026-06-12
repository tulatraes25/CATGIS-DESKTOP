package ar.com.catgis;

import ar.com.catgis.analysis.HillshadeGenerator;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.LocalRasterData;
import org.locationtech.jts.geom.Envelope;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Dialog to generate hillshade from a loaded DEM raster.
 */
public class HillshadeDialog extends JDialog {

    private final JComboBox<String> layerCombo;
    private final JTextField azimuthField, altitudeField, zFactorField;
    private final JLabel statusLabel;

    public HillshadeDialog(Frame owner) {
        super(owner, "Hillshade (Sombreado)", true);
        setSize(350, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0; g.gridx = 0;
        form.add(new JLabel("Capa DEM:"), g);
        g.gridx = 1; g.weightx = 1;
        layerCombo = new JComboBox<>();
        populateRasterLayers();
        form.add(layerCombo, g);

        g.gridy = 1; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Azimuth (0-360):"), g);
        g.gridx = 1; g.weightx = 1;
        azimuthField = new JTextField("315", 6);
        form.add(azimuthField, g);

        g.gridy = 2; g.gridx = 0;
        form.add(new JLabel("Altura sol (0-90):"), g);
        g.gridx = 1;
        altitudeField = new JTextField("45", 6);
        form.add(altitudeField, g);

        g.gridy = 3; g.gridx = 0;
        form.add(new JLabel("Exageracion Z:"), g);
        g.gridx = 1;
        zFactorField = new JTextField("2.0", 6);
        form.add(zFactorField, g);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusLabel = new JLabel("");
        JButton genBtn = new JButton("Generar");
        genBtn.addActionListener(e -> generate());
        bottom.add(statusLabel);
        bottom.add(genBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void populateRasterLayers() {
        layerCombo.removeAllItems();
        if (CatgisDesktopApp.mapPanel == null) return;
        var rasterLayers = CatgisDesktopApp.mapPanel.getRasterLayers();
        if (rasterLayers != null) {
            for (var entry : rasterLayers.entrySet()) {
                if (entry.getKey() instanceof RasterLayer) {
                    layerCombo.addItem(entry.getKey().getName());
                }
            }
        }
    }

    private void generate() {
        String layerName = (String) layerCombo.getSelectedItem();
        if (layerName == null) {
            statusLabel.setText("Seleccione una capa DEM.");
            return;
        }

        double azimuth, altitude, zFactor;
        try {
            azimuth = Double.parseDouble(azimuthField.getText().trim());
            altitude = Double.parseDouble(altitudeField.getText().trim());
            zFactor = Double.parseDouble(zFactorField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setText("Valores numericos invalidos.");
            return;
        }

        // Find the raster layer and its data
        var rasterLayers = CatgisDesktopApp.mapPanel.getRasterLayers();
        Layer targetLayer = null;
        LocalRasterData targetData = null;
        if (rasterLayers != null) {
            for (var entry : rasterLayers.entrySet()) {
                if (entry.getKey().getName().equals(layerName)) {
                    targetLayer = entry.getKey();
                    targetData = entry.getValue();
                    break;
                }
            }
        }
        if (targetLayer == null || targetData == null) {
            statusLabel.setText("Capa no encontrada.");
            return;
        }

        BufferedImage dem = targetData.getImage();
        if (dem == null) {
            statusLabel.setText("No se pudo leer la imagen DEM.");
            return;
        }

        Envelope envelope = targetData.getEnvelope();
        double cellSize = envelope.getWidth() / dem.getWidth();
        String sourceCrs = targetData.getSourceCRS();

        statusLabel.setText("Generando...");
        SwingUtilities.invokeLater(() -> {
            try {
                BufferedImage hillshade = HillshadeGenerator.generate(
                        dem, azimuth, altitude, cellSize, zFactor);

                if (hillshade == null) {
                    statusLabel.setText("Error al generar hillshade.");
                    return;
                }

                String newName = layerName + "_hillshade";
                RasterLayer hsLayer = new RasterLayer(newName, "");
                LocalRasterData hsData = new LocalRasterData(
                        hillshade, envelope, 1, true, sourceCrs);

                CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(hsLayer, hsData);

                statusLabel.setText("Hillshade generado: " + newName);
                dispose();
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
    }

    public static void open() {
        JFrame owner = CatgisDesktopApp.getMainFrame();
        new HillshadeDialog(owner).setVisible(true);
    }
}
