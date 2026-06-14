package ar.com.catgis.climate;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.RasterCoverageSupport;

import ar.com.catgis.*;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Dialog for zonal statistics analysis over environmental areas.
 * Computes mean, min, max, standard deviation, sum, and pixel count
 * for a climate raster layer within each AID/AII polygon.
 */
public class ClimateAreaAnalysisDialog extends JDialog {

    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private static final DecimalFormat DF_LONG = new DecimalFormat("#.######");

    private final MapPanel mapPanel;
    private JComboBox<Layer> rasterCombo;
    private JComboBox<Layer> areaCombo;
    private JCheckBox meanCb, minCb, maxCb, stdCb, sumCb, countCb;
    private DefaultTableModel resultsTableModel;
    private JTable resultsTable;
    private List<AnalysisResult> lastResults = new ArrayList<>();
    private JLabel periodInfoLabel;
    private String currentPeriod = "";

    public ClimateAreaAnalysisDialog(Window owner) {
        super(owner, "Análisis climático por áreas (AID/AII)", ModalityType.APPLICATION_MODAL);
        this.mapPanel = AppContext.mapPanel();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        buildUI();
        pack();
        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner) {
        new ClimateAreaAnalysisDialog(owner).setVisible(true);
    }

    private void buildUI() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Raster layer selection
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        topPanel.add(new JLabel("1. Capa climática raster:"), gc);
        rasterCombo = new JComboBox<>();
        populateRasterLayers();
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        topPanel.add(rasterCombo, gc);

        // Period info
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2; gc.weightx = 1;
        periodInfoLabel = new JLabel(" ");
        periodInfoLabel.setFont(periodInfoLabel.getFont().deriveFont(java.awt.Font.ITALIC));
        periodInfoLabel.setForeground(new java.awt.Color(80, 90, 110));
        topPanel.add(periodInfoLabel, gc);
        gc.gridwidth = 1;

        rasterCombo.addActionListener(e -> updatePeriodInfo());

        // Area layer selection
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        topPanel.add(new JLabel("2. Capa de área (AID/AII):"), gc);
        areaCombo = new JComboBox<>();
        populateAreaLayers();
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1;
        topPanel.add(areaCombo, gc);

        // Statistics selection
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        topPanel.add(new JLabel("3. Estadísticas a calcular:"), gc);
        gc.gridwidth = 1;

        JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        meanCb = new JCheckBox("Media", true);
        minCb = new JCheckBox("Mínimo", true);
        maxCb = new JCheckBox("Máximo", true);
        stdCb = new JCheckBox("Desv. estándar", true);
        sumCb = new JCheckBox("Suma", false);
        countCb = new JCheckBox("Conteo píxeles", false);
        statPanel.add(meanCb);
        statPanel.add(minCb);
        statPanel.add(maxCb);
        statPanel.add(stdCb);
        statPanel.add(sumCb);
        statPanel.add(countCb);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        topPanel.add(statPanel, gc);
        gc.gridwidth = 1;

        JButton analyzeBtn = new JButton("Generar análisis");
        analyzeBtn.addActionListener(this::runAnalysis);
        gc.gridx = 0; gc.gridy = 5; gc.gridwidth = 2;
        topPanel.add(analyzeBtn, gc);
        gc.gridwidth = 1;

        add(topPanel, BorderLayout.NORTH);

        // Results table
        resultsTableModel = new DefaultTableModel(new String[]{
                "Área", "Variable", "Período", "Media", "Mínimo", "Máximo",
                "Desv. estándar", "Suma", "Píxeles", "Observaciones"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultsTable.setFillsViewportHeight(true);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton clipBtn = new JButton("Copiar al portapapeles");
        JButton csvBtn = new JButton("Exportar CSV");
        JButton catmapBtn = new JButton("Enviar tabla a CATMAP");
        JButton closeBtn = new JButton("Cerrar");

        clipBtn.addActionListener(e -> copyToClipboard());
        csvBtn.addActionListener(e -> exportToCsv());
        catmapBtn.addActionListener(e -> exportToCatmap());
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(clipBtn);
        bottomPanel.add(csvBtn);
        bottomPanel.add(catmapBtn);
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void populateRasterLayers() {
        rasterCombo.removeAllItems();
        if (AppContext.project() == null) return;
        for (Layer layer : AppContext.project().getLayers()) {
            if (layer instanceof RasterLayer) {
                Object climateVar = layer.getUserData("climateVariable");
                String suffix = climateVar != null ? " [" + climateVar + "]" : "";
                rasterCombo.addItem(layer);
                rasterCombo.setToolTipText(layer.getName() + suffix);
            }
        }
        if (rasterCombo.getItemCount() == 0) {
            rasterCombo.addItem(null); // placeholder
        }
    }

    private void populateAreaLayers() {
        areaCombo.removeAllItems();
        List<EnvironmentalAreaMarker.MarkedArea> marked = EnvironmentalAreaMarker.getMarkedAreaLayers();
        for (EnvironmentalAreaMarker.MarkedArea ma : marked) {
            areaCombo.addItem(ma.layer());
        }
        if (areaCombo.getItemCount() == 0) {
            // Show regular polygon layers too
            for (Layer layer : EnvironmentalAreaMarker.getPolygonLayers()) {
                areaCombo.addItem(layer);
            }
        }
        if (areaCombo.getItemCount() == 0) {
            areaCombo.addItem(null);
        }
    }

    private void runAnalysis(ActionEvent e) {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        Layer areaLayer = (Layer) areaCombo.getSelectedItem();
        if (rasterLayer == null || areaLayer == null) {
            NotificationManager.warn(this,
                    "Análisis climático",
                    "Seleccioná una capa raster climática y una capa de área.");
            return;
        }

        if (!(rasterLayer instanceof RasterLayer)) {
            NotificationManager.warn(this,
                    "Análisis climático",
                    "La capa seleccionada no es un raster climático.");
            return;
        }

        try {
            // Get the raw GridCoverage2D for precise pixel evaluation
            GridCoverage2D coverage = RasterCoverageSupport.readCoverage(rasterLayer);
            if (coverage == null) {
                NotificationManager.error(this,
                        "Análisis climático",
                        "No se pudo leer el raster como GridCoverage2D.");
                return;
            }

            var shapefileData = mapPanel.getShapefileData(areaLayer);
            if (shapefileData == null || shapefileData.getFeatures() == null) {
                NotificationManager.error(this,
                        "Análisis climático",
                        "No se pudieron obtener los datos vectoriales del área.");
                return;
            }

            org.geotools.geometry.jts.ReferencedEnvelope gridEnv = new org.geotools.geometry.jts.ReferencedEnvelope(
                    coverage.getEnvelope2D());
            CoordinateReferenceSystem coverageCrs = coverage.getCoordinateReferenceSystem2D();
            int gridW = coverage.getRenderedImage().getWidth();
            int gridH = coverage.getRenderedImage().getHeight();

            boolean doMean = meanCb.isSelected();
            boolean doMin = minCb.isSelected();
            boolean doMax = maxCb.isSelected();
            boolean doStd = stdCb.isSelected();
            boolean doSum = sumCb.isSelected();
            boolean doCount = countCb.isSelected();

            String variableName = rasterLayer.getUserData("climateVariable") != null
                    ? rasterLayer.getUserData("climateVariable").toString()
                    : rasterLayer.getName();

            // Read period metadata
            currentPeriod = rasterLayer.getUserData("climatePeriodLabel") != null
                    ? rasterLayer.getUserData("climatePeriodLabel").toString() : "";
            String agg = rasterLayer.getUserData("climateAggregation") != null
                    ? " (" + rasterLayer.getUserData("climateAggregation").toString() + ")" : "";
            if (!currentPeriod.isEmpty()) currentPeriod += agg;

            resultsTableModel.setRowCount(0);
            lastResults.clear();

            EnvironmentalAreaMarker.AreaType areaType = EnvironmentalAreaMarker.getAreaType(areaLayer);
            String areaLabel = areaType != null ? areaType.getLabel() : (areaLayer != null ? areaLayer.getName() : "Área");

            int processedCount = 0;
            int numBands = coverage.getSampleDimensions().length;
            double[] pixelBuffer = new double[numBands];

            for (var feature : shapefileData.getFeatures()) {
                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof org.locationtech.jts.geom.Polygon) && !(geomObj instanceof MultiPolygon)) continue;

                Geometry geom = (Geometry) geomObj;
                String featureName = feature.getAttribute("name") != null ? feature.getAttribute("name").toString()
                        : feature.getAttribute("nombre") != null ? feature.getAttribute("nombre").toString()
                        : "Área " + (processedCount + 1);

                // Reproject polygon to coverage CRS if needed
                Geometry evalGeom = geom;
                try {
                    String layerCrs = areaLayer.getSourceCRS();
                    if (layerCrs != null && !layerCrs.isBlank() && coverageCrs != null) {
                        String coverageCrsCode = RasterCoverageSupport.resolveCoverageCrsCode(coverage, rasterLayer);
                        if (coverageCrsCode != null && !coverageCrsCode.equals(layerCrs)) {
                            var transform = org.geotools.referencing.CRS.findMathTransform(
                                    org.geotools.referencing.CRS.decode(layerCrs),
                                    org.geotools.referencing.CRS.decode(coverageCrsCode), true);
                            evalGeom = JTS.transform(geom, transform);
                        }
                    }
                } catch (Exception ignored) { CatgisLogger.warn("ClimateAreaAnalysisDialog: operation failed", ignored); }

                // Evaluate GridCoverage2D at grid cell centers within the polygon
                List<Double> pixelsInPoly = new ArrayList<>();
                int pxCount = 0;
                int step = Math.max(1, Math.min(gridW, gridH) / 500);

                for (int gy = 0; gy < gridH; gy += step) {
                    for (int gx = 0; gx < gridW; gx += step) {
                        double wx = gridEnv.getMinX() + ((gx + 0.5) / gridW) * gridEnv.getWidth();
                        double wy = gridEnv.getMaxY() - ((gy + 0.5) / gridH) * gridEnv.getHeight();

                        if (evalGeom.contains(new GeometryFactory().createPoint(new Coordinate(wx, wy)))) {
                            try {
                                double[] result = coverage.evaluate(
                                        new java.awt.geom.Point2D.Double(wx, wy), pixelBuffer);
                                if (result != null && result.length > 0 && Double.isFinite(result[0])) {
                                    pixelsInPoly.add(result[0]);
                                    pxCount++;
                                }
                            } catch (Exception ignored) { CatgisLogger.warn("ClimateAreaAnalysisDialog: operation failed", ignored); }
                        }
                    }
                }

                // Compute statistics
                AnalysisResult result = computeStatistics(
                        pixelsInPoly, areaLabel, featureName, variableName,
                        doMean, doMin, doMax, doStd, doSum, doCount);
                lastResults.add(result);

                resultsTableModel.addRow(new Object[]{
                        areaLabel + " - " + featureName,
                        variableName,
                        currentPeriod.isEmpty() ? "-" : currentPeriod,
                        doMean ? DF.format(result.mean) : "-",
                        doMin ? DF.format(result.min) : "-",
                        doMax ? DF.format(result.max) : "-",
                        doStd ? DF.format(result.stdDev) : "-",
                        doSum ? DF.format(result.sum) : "-",
                        doCount ? String.valueOf(result.pixelCount) : "-",
                        result.pixelCount + " píxeles evaluados sobre " + (pxCount > 0 ? pxCount : "sin datos")
                });
                processedCount++;
            }

            if (processedCount == 0) {
                NotificationManager.info(this,
                        "Análisis climático",
                        "No se encontraron polígonos en la capa de área seleccionada.\n"
                                + "Verificá que sea una capa de polígonos.");
            } else {
                if (CatgisDesktopApp.statusBar != null) {
                    AppContext.setStatusMessage(
                            "Análisis climático completado: " + processedCount + " áreas procesadas.");
                }
            }

        } catch (Exception ex) {
            NotificationManager.error(this,
                    "Análisis climático",
                    "Error al ejecutar el análisis: " + ex.getMessage());
        }
    }

    private AnalysisResult computeStatistics(List<Double> values, String areaLabel, String featureName,
                                             String variableName, boolean doMean, boolean doMin,
                                             boolean doMax, boolean doStd, boolean doSum, boolean doCount) {
        double mean = 0, minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;
        double sum = 0, sumSq = 0, stdDev = 0;
        int count = values.size();

        for (double v : values) {
            sum += v;
            sumSq += v * v;
            if (v < minVal) minVal = v;
            if (v > maxVal) maxVal = v;
        }

        if (count > 0) {
            mean = sum / count;
            stdDev = Math.sqrt((sumSq - (sum * sum) / count) / count);
        } else {
            minVal = Double.NaN;
            maxVal = Double.NaN;
        }
        if (minVal == Double.MAX_VALUE) minVal = Double.NaN;
        if (maxVal == -Double.MAX_VALUE) maxVal = Double.NaN;

        return new AnalysisResult(areaLabel, featureName, variableName, mean, minVal, maxVal, stdDev, sum, count);
    }

    private void copyToClipboard() {
        if (lastResults.isEmpty()) {
            NotificationManager.info(this, "Copiar", "No hay resultados para copiar.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Análisis climático por áreas\n");
        sb.append("===========================\n\n");
        for (AnalysisResult r : lastResults) {
            sb.append(r.toString()).append("\n");
        }
        StringSelection sel = new StringSelection(sb.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Resultados copiados al portapapeles.");
        }
    }

    private void updatePeriodInfo() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (rasterLayer == null) {
            periodInfoLabel.setText(" ");
            return;
        }
        String period = rasterLayer.getUserData("climatePeriodLabel") != null
                ? rasterLayer.getUserData("climatePeriodLabel").toString() : "";
        String agg = rasterLayer.getUserData("climateAggregation") != null
                ? " (" + rasterLayer.getUserData("climateAggregation").toString() + ")" : "";
        String start = rasterLayer.getUserData("climatePeriodStart") != null
                ? " desde " + rasterLayer.getUserData("climatePeriodStart").toString() : "";
        String end = rasterLayer.getUserData("climatePeriodEnd") != null
                ? " hasta " + rasterLayer.getUserData("climatePeriodEnd").toString() : "";

        if (!period.isEmpty()) {
            periodInfoLabel.setText("Período: " + period + agg + start + end);
        } else if (!start.isEmpty() || !end.isEmpty()) {
            periodInfoLabel.setText("Período:" + start + end + agg);
        } else {
            periodInfoLabel.setText("Sin información de período (climatología estática)");
        }
    }

    private void exportToCsv() {
        if (lastResults.isEmpty()) {
            NotificationManager.info(this, "Exportar CSV", "No hay resultados para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar análisis climático como CSV");
        chooser.setSelectedFile(new File("analisis_climatico.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Área,Característica,Variable,Período,Media,Mínimo,Máximo,Desv.Estándar,Suma,Píxeles");
            for (AnalysisResult r : lastResults) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%d%n",
                        r.areaLabel, r.featureName, r.variableName,
                        currentPeriod.isEmpty() ? "-" : currentPeriod,
                        DF.format(r.mean), DF.format(r.min), DF.format(r.max),
                        DF.format(r.stdDev), DF.format(r.sum), r.pixelCount);
            }
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Análisis climático exportado: " + file.getName());
            }
            NotificationManager.info(this,
                    "Exportar CSV",
                    "Resultados exportados a:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            NotificationManager.error(this,
                    "Exportar CSV",
                    "Error al guardar el archivo: " + ex.getMessage());
        }
    }

    private void exportToCatmap() {
        if (lastResults.isEmpty()) {
            NotificationManager.warn(this,
                    "Enviar a CATMAP",
                    "No hay resultados para enviar a CATMAP. Generá un análisis primero.");
            return;
        }

        try {
            // 1. Export results to temp CSV with UTF-8 BOM
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "catgis");
            if (!tempDir.exists()) tempDir.mkdirs();
            File tempFile = new File(tempDir,
                    "tabla_climatica_" + System.currentTimeMillis() + ".csv");

            try (PrintWriter pw = new PrintWriter(tempFile, "UTF-8")) {
                pw.write("\uFEFF");
                pw.println("Área;Variable;Período;Media;Mínimo;Máximo;Desv.Estándar;Suma;Píxeles");
                for (AnalysisResult r : lastResults) {
                    pw.printf("%s;%s;%s;%s;%s;%s;%s;%s;%d%n",
                            csvEscape(r.areaLabel),
                            csvEscape(r.variableName),
                            currentPeriod.isEmpty() ? "-" : csvEscape(currentPeriod),
                            DF.format(r.mean), DF.format(r.min), DF.format(r.max),
                            DF.format(r.stdDev), DF.format(r.sum), r.pixelCount);
                }
            }

            // 2. Try to send via socket to CATMAP
            boolean sent = false;
            try (Socket socket = new Socket("127.0.0.1", 8899)) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out.println("ADD_TABLE|" + tempFile.getAbsolutePath());
                String response = in.readLine();
                if (response != null && response.contains("ok")) {
                    sent = true;
                }
            } catch (Exception ignored) { CatgisLogger.warn("ClimateAreaAnalysisDialog: operation failed", ignored); }

            // 3. If CATMAP not running, save for later + offer to open
            if (!sent) {
                Preferences prefs = Preferences.userNodeForPackage(ClimateAreaAnalysisDialog.class);
                prefs.put("pendingCatmapTable", tempFile.getAbsolutePath());

                boolean yes = NotificationManager.confirm(this,
                        "Enviar a CATMAP",
                        "CATMAP no está abierto.\n\n"
                        + "¿Querés iniciar CATMAP Standalone con esta tabla\n"
                        + "para ubicarla en el layout?");

                if (yes) {
                    String catmapMain = "ar.com.catgis.catmap.Main";
                    try {
                        Runtime.getRuntime().exec(new String[]{
                                "java", "-cp", System.getProperty("java.class.path"),
                                catmapMain, "--import-table", tempFile.getAbsolutePath()
                        });
                    } catch (Exception ex) {
                        NotificationManager.info(this,
                                "Enviar a CATMAP",
                                "No se pudo iniciar CATMAP.\n"
                                + "La tabla quedó guardada en:\n"
                                + tempFile.getAbsolutePath());
                    }
                }
            }

            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Tabla climática enviada a CATMAP.");
            }

        } catch (Exception ex) {
            NotificationManager.error(this,
                    "Enviar a CATMAP",
                    "Error al enviar tabla a CATMAP: " + ex.getMessage());
        }
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private record AnalysisResult(
            String areaLabel, String featureName, String variableName,
            double mean, double min, double max, double stdDev, double sum, int pixelCount
    ) {
        @Override
        public String toString() {
            return areaLabel + " | " + featureName + " | " + variableName
                    + " | Media: " + DF.format(mean)
                    + " | Mín: " + DF.format(min)
                    + " | Máx: " + DF.format(max)
                    + " | Desv: " + DF.format(stdDev)
                    + " | Suma: " + DF.format(sum)
                    + " | Píxeles: " + pixelCount;
        }
    }
}
