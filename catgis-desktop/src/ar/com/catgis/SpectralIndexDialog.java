package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.catmap.AnalysisRaster;
import ar.com.catgis.data.raster.LocalRasterData;
import org.locationtech.jts.geom.Envelope;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.io.File;
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
    private final JCheckBox declaredCalibrationCheck;
    private final JTextField scaleAField;
    private final JTextField offsetAField;
    private final JTextField noDataAField;
    private final JTextField scaleBField;
    private final JTextField offsetBField;
    private final JTextField noDataBField;
    private final JTextField provenanceField;
    private final JButton saveAnalyticalButton;
    private AnalysisRaster lastAnalytical;

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

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        declaredCalibrationCheck = new JCheckBox(
                "Declaro que las bandas de entrada ya estan calibradas a reflectancia");
        form.add(declaredCalibrationCheck, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 5; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Banda A scale / offset / NoData:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        scaleAField = new JTextField("1", 5);
        offsetAField = new JTextField("0", 5);
        noDataAField = new JTextField("", 5);
        form.add(rowPanel(scaleAField, offsetAField, noDataAField), gbc);

        gbc.gridy = 6; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Banda B scale / offset / NoData:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        scaleBField = new JTextField("1", 5);
        offsetBField = new JTextField("0", 5);
        noDataBField = new JTextField("", 5);
        form.add(rowPanel(scaleBField, offsetBField, noDataBField), gbc);

        gbc.gridy = 7; gbc.gridx = 0; gbc.weightx = 0;
        form.add(new JLabel("Procedencia:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        provenanceField = new JTextField("Declaracion manual del usuario", 18);
        form.add(provenanceField, gbc);

        // The form and description share the center; footer remains independently visible.

        // Description
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createTitledBorder("Descripcion y Formula"));
        JPanel contentPanel = new JPanel(new BorderLayout(8, 8));
        contentPanel.add(form, BorderLayout.NORTH);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(descriptionScroll, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton computeButton = new JButton("Calcular indice");
        computeButton.addActionListener(e -> computeIndex());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        saveAnalyticalButton = new JButton("Guardar GeoTIFF analitico...");
        saveAnalyticalButton.setEnabled(false);
        saveAnalyticalButton.addActionListener(e -> saveAnalytical());
        footer.add(computeButton);
        footer.add(saveAnalyticalButton);
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
        int requiredBands = SpectralIndexEngine.requiredBandCount(si.id());
        String availability = requiredBands > 2
                ? "\n\nRequiere " + requiredBands
                    + " bandas. Este flujo de dos bandas lo bloquea para evitar un resultado incorrecto."
                : "";
        descriptionArea.setText(si.description()
                + "\n\nFormula: " + si.formula()
                + "\n\nBanda A: " + si.bandA()
                + "\nBanda B: " + si.bandB()
                + "\n\nRango de valores: "
                + java.util.Arrays.toString(SpectralIndexEngine.getIndexRange(si.id()))
                + "\nPaleta: " + SpectralIndexEngine.getColorRampName(si.id())
                + availability);
    }

    private void refreshRasterLayers() {
        rasterLayerCombo.removeAllItems();
        if (AppContext.mapPanel() == null) return;
        for (Layer layer : AppContext.mapPanel().getRenderOrderLayers()) {
            if (layer instanceof RasterLayer) {
                rasterLayerCombo.addItem(layer.getName());
            }
        }
    }

    private void computeIndex() {
        int idx = indexCombo.getSelectedIndex();
        if (idx < 0 || idx >= indices.size()) {
            NotificationManager.warn(this, null, "Selecciona un indice.");
            return;
        }
        SpectralIndexEngine.SpectralIndex si = indices.get(idx);
        int requiredBands = SpectralIndexEngine.requiredBandCount(si.id());
        if (requiredBands != 2) {
            statusLabel.setText(si.id() + " requiere " + requiredBands + " bandas.");
            statusLabel.setForeground(Color.RED);
            NotificationManager.warn(this,
                    "Bandas adicionales requeridas",
                    si.name() + " requiere " + requiredBands
                            + " bandas. Este flujo de dos bandas lo bloquea para evitar un resultado incorrecto.");
            return;
        }

        SpectralCalibrationInput.Input calibration;
        try {
            calibration = SpectralCalibrationInput.parse(
                    declaredCalibrationCheck.isSelected(),
                    scaleAField.getText(), offsetAField.getText(), noDataAField.getText(),
                    scaleBField.getText(), offsetBField.getText(), noDataBField.getText(),
                    provenanceField.getText());
        } catch (IllegalArgumentException invalid) {
            statusLabel.setText("Calibracion requerida: " + invalid.getMessage());
            statusLabel.setForeground(Color.RED);
            NotificationManager.warn(this, "Calibracion espectral requerida", invalid.getMessage());
            return;
        }

        String layerName = (String) rasterLayerCombo.getSelectedItem();
        if (layerName == null) {
            NotificationManager.warn(this, null, "Selecciona una capa raster.");
            return;
        }
        int bandA = (int) bandASpinner.getValue();
        int bandB = (int) bandBSpinner.getValue();
        Layer targetLayer = null;
        for (Layer layer : AppContext.mapPanel().getRenderOrderLayers()) {
            if (layer.getName().equals(layerName) && layer instanceof RasterLayer) {
                targetLayer = layer;
                break;
            }
        }
        if (targetLayer == null) return;
        LocalRasterData data = AppContext.mapPanel().getRasterData(targetLayer);
        if (data == null) {
            NotificationManager.warn(this, null, "No hay datos raster para esta capa.");
            return;
        }
        BufferedImage image = data.getImage();
        if (image == null) {
            NotificationManager.warn(this, null, "No se pudo leer la imagen raster.");
            return;
        }
        int actualBands = image.getRaster().getNumBands();
        if (bandA > actualBands || bandB > actualBands) {
            NotificationManager.warn(this, "Bandas invalidas",
                    "La imagen tiene " + actualBands + " bandas. Selecciona bandas entre 1 y "
                            + actualBands + ".");
            return;
        }

        AnalysisRaster rawA = rawBandRaster(data, image, bandA, calibration.bandA(), "A");
        AnalysisRaster rawB = rawBandRaster(data, image, bandB, calibration.bandB(), "B");
        AnalysisRaster result = SpectralIndexEngine.computeAnalysis(
                rawA, rawB, si.id(), calibration.bandA(), calibration.bandB());
        AnalysisRaster.Stats stats = result.stats();
        if (stats.validCount() == 0) {
            statusLabel.setText("Sin celdas validas calibradas; no se agrega capa.");
            statusLabel.setForeground(Color.RED);
            NotificationManager.warn(this, "Sin datos calibrados",
                    "El indice no tiene celdas validas tras calibracion/NoData. "
                            + "Revisa scale_factor, add_offset y NoData.");
            return;
        }
        BufferedImage preview = previewImage(result, stats);
        if (preview == null) {
            NotificationManager.warn(this, null, "No se pudo generar la vista del resultado.");
            return;
        }

        String resultName = si.id() + "_" + layerName;
        LocalRasterData resultData = new LocalRasterData(preview, data.getEnvelope(), 1, true,
                data.getSourceCRS());
        resultData.setAnalytical(result);
        Layer resultLayer = new RasterLayer(resultName, "");
        AppContext.mapPanel().addOrUpdateRasterLayer(resultLayer, resultData);
        lastAnalytical = result;
        saveAnalyticalButton.setEnabled(true);

        statusLabel.setText("Indice " + si.id() + " calibrado: " + stats.validCount()
                + " celdas validas, " + stats.noDataCount() + " NoData.");
        statusLabel.setForeground(new Color(0, 128, 0));
        NotificationManager.info(this, "Indice calibrado agregado",
                "Indice " + si.name() + " calculado sobre reflectancia calibrada.\n\n"
                        + "Formula: " + si.formula() + "\n"
                        + "Calibracion A: scale=" + calibration.bandA().scaleFactor()
                        + ", offset=" + calibration.bandA().addOffset()
                        + ", NoData=" + calibration.bandA().noData() + "\n"
                        + "Calibracion B: scale=" + calibration.bandB().scaleFactor()
                        + ", offset=" + calibration.bandB().addOffset()
                        + ", NoData=" + calibration.bandB().noData() + "\n"
                        + "Procedencia: " + calibration.provenance() + "\n"
                        + "Rango valido: " + stats.min() + " .. " + stats.max()
                        + " (" + stats.validCount() + " celdas)\n\n"
                        + "La capa agregada es una vista de color; el resultado analitico float "
                        + "se conserva y puede guardarse como GeoTIFF (NoData/CRS incluidos) "
                        + "con el boton 'Guardar GeoTIFF analitico'.");
    }

    private static JPanel rowPanel(JTextField first, JTextField second, JTextField third) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.add(first);
        row.add(second);
        row.add(third);
        return row;
    }

    private static AnalysisRaster rawBandRaster(LocalRasterData data, BufferedImage image,
                                                 int band1Based,
                                                 SpectralIndexEngine.BandCalibration calibration,
                                                 String label) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[] values = new float[width * height];
        float[] row = new float[width];
        Raster raster = image.getRaster();
        for (int y = 0; y < height; y++) {
            raster.getSamples(0, y, width, 1, band1Based - 1, row);
            System.arraycopy(row, 0, values, y * width, width);
        }
        float noData = calibration.noData() != null
                ? calibration.noData() : Float.NaN;
        Envelope envelope = data.getEnvelope();
        return new AnalysisRaster("banda_" + label + "_raw", "raw_DN", width, height,
                values, noData, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY(), data.getSourceCRS());
    }

    private static BufferedImage previewImage(AnalysisRaster result, AnalysisRaster.Stats stats) {
        int width = result.width();
        int height = result.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double span = stats.max() - stats.min();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (result.isNoData(x, y)) {
                    image.setRGB(x, y, new Color(32, 32, 32).getRGB());
                    continue;
                }
                double normalized = span > 0
                        ? (result.valueAt(x, y) - stats.min()) / span : 0.5;
                int value = (int) Math.round(Math.max(0.0, Math.min(1.0, normalized)) * 255);
                image.setRGB(x, y, new Color(value, 255 - value, value / 2).getRGB());
            }
        }
        return image;
    }

    private void saveAnalytical() {
        if (lastAnalytical == null) {
            NotificationManager.warn(this, null, "Calcula primero un indice calibrado.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar resultado analitico GeoTIFF");
        chooser.setSelectedFile(new File(lastAnalytical.name() + ".tif"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            lastAnalytical.exportGeoTiff(chooser.getSelectedFile());
            NotificationManager.info(this, "Resultado analitico guardado",
                    "GeoTIFF float con NoData/CRS escrito en:\n"
                            + chooser.getSelectedFile().getAbsolutePath());
        } catch (Exception error) {
            AppErrorSupport.logFailure("No se pudo exportar el resultado espectral", error);
            AppErrorSupport.showErrorDialog(this, "Indice espectral",
                    "No se pudo guardar el GeoTIFF analitico.", error);
        }
    }

}