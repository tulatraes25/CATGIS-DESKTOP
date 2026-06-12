package ar.com.catgis.climate;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.raster.LocalRasterData;

import ar.com.catgis.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dialog for generating and exporting wind rose diagrams from
 * U and V wind component raster layers, or from synthetic data.
 */
public class WindRoseDialog extends JDialog {

    private final MapPanel mapPanel;
    private final WindRoseRenderer roseRenderer;
    private JComboBox<Layer> uComponentCombo;
    private JComboBox<Layer> vComponentCombo;
    private JCheckBox freqCb, speedCb, labelsCb;
    private JLabel infoLabel;

    public WindRoseDialog(Window owner) {
        super(owner, "Rosa de los vientos", ModalityType.APPLICATION_MODAL);
        this.mapPanel = CatgisDesktopApp.mapPanel;
        this.roseRenderer = new WindRoseRenderer();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        buildUI();
        pack();
        setSize(700, 550);
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner) {
        new WindRoseDialog(owner).setVisible(true);
    }

    private void buildUI() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // U component (eastward)
        gc.gridx = 0; gc.gridy = 0;
        topPanel.add(new JLabel("Componente U (este):"), gc);
        uComponentCombo = new JComboBox<>();
        populateRasterLayers(uComponentCombo);
        gc.gridx = 1; gc.gridy = 0;
        topPanel.add(uComponentCombo, gc);

        // V component (northward)
        gc.gridx = 0; gc.gridy = 1;
        topPanel.add(new JLabel("Componente V (norte):"), gc);
        vComponentCombo = new JComboBox<>();
        populateRasterLayers(vComponentCombo);
        gc.gridx = 1; gc.gridy = 1;
        topPanel.add(vComponentCombo, gc);

        // Options
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        freqCb = new JCheckBox("Mostrar frecuencia", true);
        speedCb = new JCheckBox("Mostrar velocidad media", true);
        labelsCb = new JCheckBox("Mostrar etiquetas", true);
        optionsPanel.add(freqCb);
        optionsPanel.add(speedCb);
        optionsPanel.add(labelsCb);
        topPanel.add(optionsPanel, gc);
        gc.gridwidth = 1;

        JButton computeBtn = new JButton("Calcular rosa de vientos");
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        topPanel.add(computeBtn, gc);
        gc.gridwidth = 1;

        // Info label
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        infoLabel = new JLabel("Selecciona las componentes U (zonal) y V (meridional) del viento.");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 11f));
        topPanel.add(infoLabel, gc);
        gc.gridwidth = 1;

        add(topPanel, BorderLayout.NORTH);

        // Center: wind rose
        JPanel rosePanel = new JPanel(new BorderLayout());
        rosePanel.setBorder(BorderFactory.createTitledBorder("Rosa de los vientos"));
        rosePanel.add(roseRenderer, BorderLayout.CENTER);
        add(rosePanel, BorderLayout.CENTER);

        // Bottom: export buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton pngBtn = new JButton("Exportar PNG (transparente)");
        JButton clipboardBtn = new JButton("Copiar al portapapeles");
        JButton closeBtn = new JButton("Cerrar");

        pngBtn.addActionListener(e -> exportPng());
        clipboardBtn.addActionListener(e -> copyToClipboard());
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(pngBtn);
        bottomPanel.add(clipboardBtn);
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Events
        freqCb.addActionListener(e -> roseRenderer.setShowFrequency(freqCb.isSelected()));
        speedCb.addActionListener(e -> roseRenderer.setShowSpeed(speedCb.isSelected()));
        labelsCb.addActionListener(e -> roseRenderer.setShowLabels(labelsCb.isSelected()));

        computeBtn.addActionListener(e -> computeWindRose());

        // Compute a demo if data is hard to find
        SwingUtilities.invokeLater(() -> {
            if (roseRenderer.getTotalCount() == 0) {
                computeDemoWindRose();
            }
        });
    }

    private void populateRasterLayers(JComboBox<Layer> combo) {
        combo.removeAllItems();
        if (AppContext.project() == null) return;
        for (Layer layer : AppContext.project().getLayers()) {
            if (layer instanceof RasterLayer) {
                combo.addItem(layer);
            }
        }
    }

    private void computeWindRose() {
        Layer uLayer = (Layer) uComponentCombo.getSelectedItem();
        Layer vLayer = (Layer) vComponentCombo.getSelectedItem();

        // Try to extract U/V components from the selected layers
        List<Float> uValues = new ArrayList<>();
        List<Float> vValues = new ArrayList<>();

        if (uLayer != null && vLayer != null) {
            extractWindData(uLayer, uValues, vLayer, vValues);
        }

        if (!uValues.isEmpty() && !vValues.isEmpty()) {
            float[] uArr = new float[uValues.size()];
            float[] vArr = new float[vValues.size()];
            for (int i = 0; i < uValues.size(); i++) uArr[i] = uValues.get(i);
            for (int i = 0; i < vValues.size(); i++) vArr[i] = vValues.get(i);
            roseRenderer.computeFromUVComponents(uArr, vArr);

            infoLabel.setText("Rosa generada con " + uValues.size() + " muestras de viento.");
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Rosa de los vientos calculada: " + roseRenderer.getTotalCount() + " muestras.");
            }
        } else {
            computeDemoWindRose();
        }
    }

    private void extractWindData(Layer uLayer, List<Float> uValues,
                                  Layer vLayer, List<Float> vValues) {
        // Extract pixel data from the raster layers
        try {
            LocalRasterData uData = mapPanel.getRasterData(uLayer);
            LocalRasterData vData = mapPanel.getRasterData(vLayer);

            if (uData != null && uData.getImage() != null && vData != null && vData.getImage() != null) {
                BufferedImage uImg = uData.getImage();
                BufferedImage vImg = vData.getImage();
                int w = Math.min(uImg.getWidth(), vImg.getWidth());
                int h = Math.min(uImg.getHeight(), vImg.getHeight());

                // Recover scaling from metadata
                double uMin = 0, uMax = 30, vMin = 0, vMax = 30;
                if (uLayer.getUserData("climateMin") instanceof Number)
                    uMin = ((Number) uLayer.getUserData("climateMin")).doubleValue();
                if (uLayer.getUserData("climateMax") instanceof Number)
                    uMax = ((Number) uLayer.getUserData("climateMax")).doubleValue();
                if (vLayer.getUserData("climateMin") instanceof Number)
                    vMin = ((Number) vLayer.getUserData("climateMin")).doubleValue();
                if (vLayer.getUserData("climateMax") instanceof Number)
                    vMax = ((Number) vLayer.getUserData("climateMax")).doubleValue();

                int step = Math.max(1, Math.min(w, h) / 30);
                for (int y = 0; y < h; y += step) {
                    for (int x = 0; x < w; x += step) {
                        int uRgb = uImg.getRGB(x, y);
                        int vRgb = vImg.getRGB(x, y);
                        int uGray = (uRgb >> 16) & 0xFF;
                        int vGray = (vRgb >> 16) & 0xFF;
                        float uVal = (float) (uMin + (uGray / 255.0) * (uMax - uMin));
                        float vVal = (float) (vMin + (vGray / 255.0) * (vMax - vMin));
                        uValues.add(uVal);
                        vValues.add(vVal);
                    }
                }
            }
        } catch (Exception ignored) { CatgisLogger.warn("WindRoseDialog: operation failed", ignored); }
    }

    private void computeDemoWindRose() {
        // Generate demo data for preview
        Random rand = new Random(42);
        int[] freq = new int[16];
        double[] speed = new double[16];
        int total = 0;

        // Prefer some dominant directions
        double[] weights = {
                5, 2, 1, 1, 2, 3, 2, 1,
                2, 1, 1, 2, 3, 4, 8, 6
        };

        for (int i = 0; i < 16; i++) {
            int count = (int) (weights[i] * (10 + rand.nextInt(20)));
            freq[i] = count;
            total += count;
            speed[i] = 2 + weights[i] * (0.5 + rand.nextDouble() * 1.5);
        }

        roseRenderer.setData(freq, speed);
        infoLabel.setText("Rosa de ejemplo (demo) - " + total + " muestras sintéticas."
                + " Cargá componentes U/V reales para generar datos genuinos.");
    }

    private void exportPng() {
        BufferedImage img = roseRenderer.renderToImage(600, 500);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar rosa de los vientos como PNG");
        chooser.setSelectedFile(new File("rosa_vientos.png"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getAbsolutePath() + ".png");
        }

        try {
            ImageIO.write(img, "png", file);
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Rosa de vientos exportada: " + file.getName());
            }
            JOptionPane.showMessageDialog(this,
                    "Rosa de vientos guardada como PNG con fondo transparente:\n"
                            + file.getAbsolutePath(),
                    "Exportar PNG", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Exportar PNG", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyToClipboard() {
        BufferedImage img = roseRenderer.renderToImage(300, 300);
        ImageSelection selection = new ImageSelection(img);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Rosa de vientos copiada al portapapeles.");
        }
    }

    /**
     * Helper class for copying BufferedImage to clipboard.
     */
    private static class ImageSelection implements java.awt.datatransfer.Transferable {
        private final Image image;
        ImageSelection(Image image) { this.image = image; }
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }
        @Override
        public Object getTransferData(DataFlavor flavor) {
            return image;
        }
    }
}
