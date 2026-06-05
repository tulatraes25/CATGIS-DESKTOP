package ar.com.catgis.climate;

import ar.com.catgis.*;
import org.locationtech.jts.geom.*;
import java.awt.image.BufferedImage;

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
        this.mapPanel = CatgisDesktopApp.mapPanel;
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

        // Disclaimer panel about pixel sampling precision
        JPanel disclaimerPanel = new JPanel(new BorderLayout());
        JLabel disclaimerLabel = new JLabel(
            "<html><b>Nota:</b> Este an\u00e1lisis usa muestreo de p\u00edxeles de la imagen renderizada "
            + "(cada 2-4 p\u00edxeles), no estad\u00edsticas zonales raster completas. "
            + "La precisi\u00f3n es adecuada para informes ambientales preliminares, "
            + "no para an\u00e1lisis cient\u00edfico exacto.</html>");
        disclaimerLabel.setFont(disclaimerLabel.getFont().deriveFont(java.awt.Font.ITALIC, 10f));
        disclaimerLabel.setForeground(new java.awt.Color(100, 100, 100));
        disclaimerLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        disclaimerPanel.add(disclaimerLabel, BorderLayout.CENTER);
        disclaimerPanel.setBorder(BorderFactory.createTitledBorder("Precisi\u00f3n"));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(disclaimerPanel, BorderLayout.NORTH);
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
        if (CatgisDesktopApp.currentProject == null) return;
        for (Layer layer : CatgisDesktopApp.currentProject.getLayers()) {
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
            JOptionPane.showMessageDialog(this,
                    "Seleccioná una capa raster climática y una capa de área.",
                    "Análisis climático", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!(rasterLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this,
                    "La capa seleccionada no es un raster climático.",
                    "Análisis climático", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get the raster pixel samples
            LocalRasterData rasterData = mapPanel.getRasterData(rasterLayer);
            if (rasterData == null) {
                JOptionPane.showMessageDialog(this,
                        "No se pudieron obtener los datos del raster.",
                        "Análisis climático", JOptionPane.ERROR_MESSAGE);
                return;
            }

            var shapefileData = mapPanel.getShapefileData(areaLayer);
            if (shapefileData == null || shapefileData.getFeatures() == null) {
                JOptionPane.showMessageDialog(this,
                        "No se pudieron obtener los datos vectoriales del área.",
                        "Análisis climático", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BufferedImage image = rasterData.getImage();
            if (image == null) return;

            org.locationtech.jts.geom.Envelope rasterEnv = rasterData.getEnvelope();
            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

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
            for (var feature : shapefileData.getFeatures()) {
                Object geomObj = feature.getDefaultGeometry();
                if (!(geomObj instanceof org.locationtech.jts.geom.Polygon) && !(geomObj instanceof MultiPolygon)) continue;

                Geometry geom = (Geometry) geomObj;
                String featureName = feature.getAttribute("name") != null ? feature.getAttribute("name").toString()
                        : feature.getAttribute("nombre") != null ? feature.getAttribute("nombre").toString()
                        : "Área " + (processedCount + 1);

                // Sample pixels within polygon
                List<Double> pixelsInPoly = new ArrayList<>();
                int pxCount = 0;

                for (int y = 0; y < imgHeight; y += 2) {
                    for (int x = 0; x < imgWidth; x += 2) {
                        // Map pixel to world coordinates
                        double wx = rasterEnv.getMinX() + (x / (double) imgWidth) * rasterEnv.getWidth();
                        double wy = rasterEnv.getMaxY() - (y / (double) imgHeight) * rasterEnv.getHeight();

                        Coordinate pointCoord = new Coordinate(wx, wy);
                        org.locationtech.jts.geom.Point point = new GeometryFactory().createPoint(pointCoord);

                        if (geom.contains(point)) {
                            int rgb = image.getRGB(x, y);
                            int gray = (rgb >> 16) & 0xFF;

                            // Get actual value from the layer metadata or estimate
                            double val = estimatePixelValue(rasterLayer, gray, x, y, image, rasterEnv);
                            if (Double.isFinite(val)) {
                                pixelsInPoly.add(val);
                                pxCount++;
                            }
                        }
                    }
                }

                if (pixelsInPoly.isEmpty()) {
                    // Try coarser sampling
                    for (int y = 0; y < imgHeight; y += 4) {
                        for (int x = 0; x < imgWidth; x += 4) {
                            double wx = rasterEnv.getMinX() + (x / (double) imgWidth) * rasterEnv.getWidth();
                            double wy = rasterEnv.getMaxY() - (y / (double) imgHeight) * rasterEnv.getHeight();
                            Coordinate pointCoord = new Coordinate(wx, wy);
                            org.locationtech.jts.geom.Point point = new GeometryFactory().createPoint(pointCoord);
                            if (geom.contains(point)) {
                                int rgb = image.getRGB(x, y);
                                int gray = (rgb >> 16) & 0xFF;
                                double val = estimatePixelValue(rasterLayer, gray, x, y, image, rasterEnv);
                                if (Double.isFinite(val)) {
                                    pixelsInPoly.add(val);
                                    pxCount++;
                                }
                            }
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
                        result.pixelCount + " píxeles (muestreo aprox.)"
                });
                processedCount++;
            }

            if (processedCount == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron polígonos en la capa de área seleccionada.\n"
                                + "Verificá que sea una capa de polígonos.",
                        "Análisis climático", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (CatgisDesktopApp.statusBar != null) {
                    CatgisDesktopApp.statusBar.setMessage(
                            "Análisis climático completado: " + processedCount + " áreas procesadas.");
                }
            }

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error al ejecutar el análisis: " + ex.getMessage(),
                    "Análisis climático", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private double estimatePixelValue(Layer layer, int grayValue, int pixelX, int pixelY,
                                      BufferedImage image, org.locationtech.jts.geom.Envelope env) {
        // Try to recover original value from layer metadata
        Object minObj = layer.getUserData("climateMin");
        Object maxObj = layer.getUserData("climateMax");
        if (minObj instanceof Number && maxObj instanceof Number) {
            double min = ((Number) minObj).doubleValue();
            double max = ((Number) maxObj).doubleValue();
            return min + (grayValue / 255.0) * (max - min);
        }
        return grayValue; // Fallback: use gray value
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
            JOptionPane.showMessageDialog(this, "No hay resultados para copiar.",
                    "Copiar", JOptionPane.INFORMATION_MESSAGE);
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
            CatgisDesktopApp.statusBar.setMessage("Resultados copiados al portapapeles.");
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
            JOptionPane.showMessageDialog(this, "No hay resultados para exportar.",
                    "Exportar CSV", JOptionPane.INFORMATION_MESSAGE);
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
                CatgisDesktopApp.statusBar.setMessage("Análisis climático exportado: " + file.getName());
            }
            JOptionPane.showMessageDialog(this,
                    "Resultados exportados a:\n" + file.getAbsolutePath(),
                    "Exportar CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error al guardar el archivo: " + ex.getMessage(),
                    "Exportar CSV", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCatmap() {
        if (lastResults.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay resultados para enviar a CATMAP. Generá un análisis primero.",
                    "Enviar a CATMAP", JOptionPane.WARNING_MESSAGE);
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
            } catch (Exception ignored) {}

            // 3. If CATMAP not running, save for later + offer to open
            if (!sent) {
                Preferences prefs = Preferences.userNodeForPackage(ClimateAreaAnalysisDialog.class);
                prefs.put("pendingCatmapTable", tempFile.getAbsolutePath());

                int result = JOptionPane.showConfirmDialog(this,
                        "CATMAP no está abierto.\n\n"
                        + "¿Querés iniciar CATMAP Standalone con esta tabla\n"
                        + "para ubicarla en el layout?",
                        "Enviar a CATMAP", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    String catmapMain = "ar.com.catgis.catmap.Main";
                    try {
                        Runtime.getRuntime().exec(new String[]{
                                "java", "-cp", System.getProperty("java.class.path"),
                                catmapMain, "--import-table", tempFile.getAbsolutePath()
                        });
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                                "No se pudo iniciar CATMAP.\n"
                                + "La tabla quedó guardada en:\n"
                                + tempFile.getAbsolutePath(),
                                "Enviar a CATMAP", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Tabla climática enviada a CATMAP.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al enviar tabla a CATMAP: " + ex.getMessage(),
                    "Enviar a CATMAP", JOptionPane.ERROR_MESSAGE);
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
