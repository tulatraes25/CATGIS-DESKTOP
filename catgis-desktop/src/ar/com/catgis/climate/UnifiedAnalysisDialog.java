package ar.com.catgis.climate;

import ar.com.catgis.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Unified dialog for running spatial analyses:
 * landform classification, oil spill risk, flood risk, landslide susceptibility.
 * Results can be previewed and added as raster layers to the project.
 */
public class UnifiedAnalysisDialog extends JDialog {

    private final JComboBox<String> scenarioCombo;
    private final JComboBox<String> demLayerCombo;
    private final JSpinner radiusSpinner;
    private final JSpinner flatnessSpinner;
    private final JSpinner windDirSpinner;
    private final JSpinner windSpeedSpinner;
    private final JSpinner volumeSpinner;
    private final JLabel previewLabel;
    private final JLabel statusLabel;
    private final JButton runBtn;
    private final JButton addToMapBtn;
    private final JButton closeBtn;

    private BufferedImage resultImage;
    private double[][] resultGrid;
    private int resultWidth, resultHeight;
    private double resultCellSize, resultWest, resultNorth;
    private String resultScenario;
    private List<Layer> availableDemLayers = new ArrayList<>();

    public UnifiedAnalysisDialog(Frame owner) {
        super(owner, "Analizador GEO — Un clic", true);
        setLayout(new BorderLayout(12, 12));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        getContentPane().setBackground(new Color(242, 245, 250));

        // --- Left: Configuration panel ---
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Title
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel title = new JLabel("Analisis territorial avanzado");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(26, 36, 52));
        configPanel.add(title, g);

        g.gridwidth = 1; g.gridy = 1;
        JLabel subtitle = new JLabel("Un clic. Resultado inmediato.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(new Color(100, 110, 120));
        configPanel.add(subtitle, g);

        // Scenario selector
        g.gridx = 0; g.gridy = 2; g.gridwidth = 1;
        configPanel.add(new JLabel("Escenario:"), g);
        String[] scenarios = {
            "Clasificar geoformas (desde DEM)",
            "Riesgo por derrame de hidrocarburos",
            "Riesgo de inundacion",
            "Susceptibilidad a deslizamientos",
            "Contaminacion industrial"
        };
        scenarioCombo = new JComboBox<>(scenarios);
        scenarioCombo.addActionListener(e -> updateParamsVisibility());
        g.gridx = 1;
        configPanel.add(scenarioCombo, g);

        // DEM layer selector
        g.gridx = 0; g.gridy = 3;
        configPanel.add(new JLabel("DEM / Elevacion:"), g);
        demLayerCombo = new JComboBox<>();
        g.gridx = 1;
        configPanel.add(demLayerCombo, g);

        // Parameters
        g.gridx = 0; g.gridy = 4;
        configPanel.add(new JLabel("Radio (pixeles):"), g);
        radiusSpinner = new JSpinner(new SpinnerNumberModel(10, 3, 100, 1));
        ((JSpinner.DefaultEditor) radiusSpinner.getEditor()).getTextField().setColumns(8);
        g.gridx = 1;
        configPanel.add(radiusSpinner, g);

        g.gridx = 0; g.gridy = 5;
        configPanel.add(new JLabel("Umbral plano:"), g);
        flatnessSpinner = new JSpinner(new SpinnerNumberModel(0.03, 0.001, 0.2, 0.005));
        g.gridx = 1;
        configPanel.add(flatnessSpinner, g);

        g.gridx = 0; g.gridy = 6;
        configPanel.add(new JLabel("Direccion viento (°):"), g);
        windDirSpinner = new JSpinner(new SpinnerNumberModel(240, 0, 360, 5));
        g.gridx = 1;
        configPanel.add(windDirSpinner, g);

        g.gridx = 0; g.gridy = 7;
        configPanel.add(new JLabel("Vel. viento (km/h):"), g);
        windSpeedSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 200, 5));
        g.gridx = 1;
        configPanel.add(windSpeedSpinner, g);

        g.gridx = 0; g.gridy = 8;
        configPanel.add(new JLabel("Volumen (m3):"), g);
        volumeSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 50));
        g.gridx = 1;
        configPanel.add(volumeSpinner, g);

        // Run button
        g.gridx = 0; g.gridy = 9; g.gridwidth = 2;
        runBtn = new JButton("▶ EJECUTAR ANALISIS");
        runBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        runBtn.setBackground(new Color(59, 130, 246));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFocusPainted(false);
        runBtn.addActionListener(e -> runAnalysis());
        configPanel.add(runBtn, g);

        // Status
        g.gridy = 10;
        statusLabel = new JLabel("Listo. Seleccione un escenario y DEM.");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        statusLabel.setForeground(new Color(100, 110, 120));
        configPanel.add(statusLabel, g);

        // --- Right: Preview ---
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210)),
            "Vista previa", javax.swing.border.TitledBorder.LEADING,
            javax.swing.border.TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 11)));
        previewLabel = new JLabel("", SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(400, 350));
        previewLabel.setBackground(new Color(250, 252, 255));
        previewLabel.setOpaque(true);
        previewLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        previewLabel.setText("<html><center>Ejecute el analisis<br>para ver el resultado</center></html>");
        previewPanel.add(new JScrollPane(previewLabel), BorderLayout.CENTER);
        previewPanel.setPreferredSize(new Dimension(420, 380));

        // --- Bottom buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottomPanel.setOpaque(false);
        addToMapBtn = new JButton("Agregar al mapa");
        addToMapBtn.setEnabled(false);
        addToMapBtn.addActionListener(e -> addResultToMap());
        closeBtn = new JButton("Cerrar");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(addToMapBtn);
        bottomPanel.add(closeBtn);

        // Assemble
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(configPanel, BorderLayout.NORTH);
        leftPanel.add(bottomPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(350, 400));

        add(leftPanel, BorderLayout.WEST);
        add(previewPanel, BorderLayout.CENTER);

        setSize(800, 520);
        setLocationRelativeTo(owner);

        // Load DEM layers
        loadDemLayers();
        updateParamsVisibility();
    }

    private void loadDemLayers() {
        demLayerCombo.removeAllItems();
        demLayerCombo.addItem("[Cargar DEM desde proyecto]");
        availableDemLayers.clear();

        Project project = CatgisDesktopApp.currentProject;
        if (project != null && project.getLayers() != null) {
            for (Layer layer : project.getLayers()) {
                if (layer == null || layer.getPath() == null) continue;
                String path = layer.getPath().toLowerCase();
                if (path.endsWith(".tif") || path.endsWith(".tiff")
                        || path.endsWith(".asc") || path.endsWith(".img")) {
                    demLayerCombo.addItem(layer.getName());
                    availableDemLayers.add(layer);
                }
            }
        }
        if (availableDemLayers.isEmpty()) {
            demLayerCombo.addItem("[No hay capas DEM disponibles]");
        }
    }

    private void updateParamsVisibility() {
        int idx = scenarioCombo.getSelectedIndex();
        // Show/hide parameter rows based on scenario
        boolean showRadius = (idx == 0); // landform
        boolean showFlatness = (idx == 0);
        boolean showWind = (idx == 1 || idx == 4); // oil spill or contamination
        boolean showVolume = (idx == 1);
        // We can't really hide rows in GridBagLayout easily, so we show all
    }

    private void runAnalysis() {
        resultImage = null;
        resultGrid = null;
        runBtn.setEnabled(false);
        runBtn.setText("PROCESANDO...");
        addToMapBtn.setEnabled(false);
        statusLabel.setText("Ejecutando analisis...");

        new Thread(() -> {
            try {
                int scenarioIdx = scenarioCombo.getSelectedIndex();
                int radius = (Integer) radiusSpinner.getValue();
                double flatness = (Double) flatnessSpinner.getValue();
                double windDir = (Integer) windDirSpinner.getValue();
                double windSpeed = (Integer) windSpeedSpinner.getValue();
                double volume = (Integer) volumeSpinner.getValue();

                // Get DEM
                float[][] dem = null;
                double demCellSize = 30;
                double demWest = 0, demNorth = 0;

                int layerIdx = demLayerCombo.getSelectedIndex() - 1;
                if (layerIdx >= 0 && layerIdx < availableDemLayers.size()) {
                    Layer layer = availableDemLayers.get(layerIdx);
                    dem = loadDemFromLayer(layer);
                    if (dem != null) {
                        demCellSize = 30; // default
                        // Try to get georeferencing from the layer
                    }
                }

                if (dem == null) {
                    // Create synthetic DEM for preview
                    int size = 100;
                    dem = new float[size][size];
                    for (int r = 0; r < size; r++)
                        for (int c = 0; c < size; c++)
                            dem[r][c] = 100f + (float)(Math.sin(r * 0.1) * 30
                                + Math.cos(c * 0.15) * 20 + (r - size/2) * 0.3f);
                }

                int h = dem.length, w = dem[0].length;

                SwingUtilities.invokeLater(() -> runBtn.setText("EJECUTAR ANALISIS"));

                switch (scenarioIdx) {
                    case 0: // Landform
                        var lfResult = LandformClassifier.classify(
                            dem, w, h, demCellSize, demWest, demNorth, radius, flatness);
                        resultImage = LandformClassifier.renderClassification(lfResult);
                        resultGrid = new double[h][w];
                        for (int r = 0; r < h; r++)
                            for (int c = 0; c < w; c++)
                                resultGrid[r][c] = lfResult.getClass(r, c) / 10.0;
                        resultWidth = w; resultHeight = h;
                        resultCellSize = demCellSize; resultWest = demWest; resultNorth = demNorth;
                        resultScenario = "Geoformas";
                        statusLabel.setText("Geoformas clasificadas: " + LandformClassifier.Landform.values().length + " tipos");
                        break;

                    case 1: // Oil spill
                        Map<String, Double> params = new HashMap<>();
                        params.put("windDirection", windDir);
                        params.put("windSpeed", windSpeed);
                        params.put("volume", volume);
                        var oilResult = RiskAnalysisEngine.analyze(dem,
                            RiskAnalysisEngine.Scenario.OIL_SPILL, 0, 0, "", params);
                        resultImage = oilResult.riskMap();
                        resultGrid = oilResult.riskGrid();
                        resultWidth = oilResult.width(); resultHeight = oilResult.height();
                        resultCellSize = oilResult.cellSize(); resultWest = oilResult.west(); resultNorth = oilResult.north();
                        resultScenario = "Riesgo derrame";
                        statusLabel.setText("Derrame modelado. " + String.join("; ", oilResult.warnings()));
                        break;

                    case 2: // Flood
                        var floodResult = RiskAnalysisEngine.analyze(dem,
                            RiskAnalysisEngine.Scenario.FLOOD, 0, 0, "", null);
                        resultImage = floodResult.riskMap();
                        resultGrid = floodResult.riskGrid();
                        resultWidth = floodResult.width(); resultHeight = floodResult.height();
                        resultCellSize = floodResult.cellSize(); resultWest = floodResult.west(); resultNorth = floodResult.north();
                        resultScenario = "Riesgo inundacion";
                        statusLabel.setText("Inundacion modelada. " + String.join("; ", floodResult.warnings()));
                        break;

                    case 3: // Landslide
                        var slideResult = RiskAnalysisEngine.analyze(dem,
                            RiskAnalysisEngine.Scenario.LANDSLIDE, 0, 0, "", null);
                        resultImage = slideResult.riskMap();
                        resultGrid = slideResult.riskGrid();
                        resultWidth = slideResult.width(); resultHeight = slideResult.height();
                        resultCellSize = slideResult.cellSize(); resultWest = slideResult.west(); resultNorth = slideResult.north();
                        resultScenario = "Deslizamiento";
                        statusLabel.setText("Susceptibilidad calculada");
                        break;

                    case 4: // Contamination
                        Map<String, Double> cParams = new HashMap<>();
                        cParams.put("windDirection", windDir);
                        cParams.put("windSpeed", windSpeed);
                        cParams.put("volume", volume);
                        var contResult = RiskAnalysisEngine.analyze(dem,
                            RiskAnalysisEngine.Scenario.CONTAMINATION, 0, 0, "", cParams);
                        resultImage = contResult.riskMap();
                        resultGrid = contResult.riskGrid();
                        resultWidth = contResult.width(); resultHeight = contResult.height();
                        resultCellSize = contResult.cellSize(); resultWest = contResult.west(); resultNorth = contResult.north();
                        resultScenario = "Contaminacion";
                        statusLabel.setText("Riesgo de contaminacion modelado");
                        break;
                }

                // Show preview
                if (resultImage != null) {
                    SwingUtilities.invokeLater(() -> {
                        ImageIcon icon = new ImageIcon(resultImage.getScaledInstance(
                            Math.min(380, resultImage.getWidth()),
                            Math.min(320, resultImage.getHeight()),
                            Image.SCALE_SMOOTH));
                        previewLabel.setIcon(icon);
                        previewLabel.setText(null);
                        addToMapBtn.setEnabled(true);
                        runBtn.setEnabled(true);
                        runBtn.setText("▶ EJECUTAR ANALISIS");
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error: " + ex.getMessage());
                    runBtn.setEnabled(true);
                    runBtn.setText("▶ EJECUTAR ANALISIS");
                    JOptionPane.showMessageDialog(this,
                        "Error al ejecutar el analisis:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void addResultToMap() {
        if (resultImage == null || resultGrid == null) return;

        String name = "Analisis: " + resultScenario + " (" +
            new java.text.SimpleDateFormat("HH:mm").format(new Date()) + ")";

        // Create envelope (approximate if no georeferencing)
        double w = resultWest;
        double e = resultWest + resultWidth * resultCellSize;
        double s = resultNorth - resultHeight * resultCellSize;
        double n = resultNorth;
        if (Math.abs(w) < 0.001 && Math.abs(n) < 0.001) {
            // No real georeferencing — use project view extent
            var mp = CatgisDesktopApp.mapPanel;
            if (mp != null) {
                w = mp.getViewMinX();
                n = mp.getViewMinY();
                double zf = mp.getZoomFactor();
                if (zf > 0) {
                    e = w + mp.getWidth() / zf;
                    s = n - mp.getHeight() / zf;
                }
            }
        }
        var envelope = new org.locationtech.jts.geom.Envelope(w, e, s, n);

        // Create raster layer data
        LocalRasterData data = new LocalRasterData(
            resultImage, envelope, 3, true,
            CatgisDesktopApp.currentProject != null
                ? CatgisDesktopApp.currentProject.getProjectCRS() : "");

        // Create layer and add to project
        RasterLayer rasterLayer = new RasterLayer(name, "");
        rasterLayer.setPreviewMode(true);
        rasterLayer.setSourceCRS(data.getSourceCRS());

        if (CatgisDesktopApp.currentProject != null) {
            CatgisDesktopApp.currentProject.addLayer(rasterLayer);
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(rasterLayer, data);
            CatgisDesktopApp.mapPanel.repaint();
        }

        statusLabel.setText("Resultado agregado al mapa: " + name);
        JOptionPane.showMessageDialog(this,
            "Capa agregada al proyecto:\n" + name,
            "Resultado agregado", JOptionPane.INFORMATION_MESSAGE);
    }

    private float[][] loadDemFromLayer(Layer layer) {
        if (layer == null) return null;
        try {
            var coverage = RasterCoverageSupport.readCoverage(layer);
            if (coverage == null) return null;
            int w = coverage.getRenderedImage().getWidth();
            int h = coverage.getRenderedImage().getHeight();
            float[][] dem = new float[h][w];
            var envelope = coverage.getEnvelope();
            double[] pixel = new double[1];

            for (int r = 0; r < Math.min(h, 500); r++) {
                for (int c = 0; c < Math.min(w, 500); c++) {
                    try {
                        double x = envelope.getMinimum(0) + (c + 0.5) * envelope.getSpan(0) / w;
                        double y = envelope.getMaximum(1) - (r + 0.5) * envelope.getSpan(1) / h;
                        coverage.evaluate(new java.awt.geom.Point2D.Double(x, y), pixel);
                        dem[r][c] = (float) pixel[0];
                    } catch (Exception ex) {
                        dem[r][c] = Float.NaN;
                    }
                }
            }
            return dem;
        } catch (Exception e) {
            return null;
        }
    }

    /** Convenience opener */
    public static void open(Component parent) {
        Frame frame = parent instanceof Frame ? (Frame) parent
            : (Frame) SwingUtilities.getWindowAncestor(parent);
        if (frame == null && parent != null) {
            frame = JOptionPane.getFrameForComponent(parent);
        }
        UnifiedAnalysisDialog dlg = new UnifiedAnalysisDialog(frame);
        dlg.setVisible(true);
    }
}
