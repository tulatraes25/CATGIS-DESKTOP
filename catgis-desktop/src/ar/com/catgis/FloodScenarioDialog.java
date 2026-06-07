package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FloodScenarioDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JTextField baseNameField;
    private final JComboBox<DrainageExtractionService.AnalysisDetail> detailCombo;
    private final JComboBox<DrainageExtractionService.HydrologicConditioning> conditioningCombo;
    private final JTextField thresholdField;
    private final JTextField rainfallField;
    private final JTextField runoffField;
    private final JTextField minimumDepthField;
    private final JCheckBox exportGeoTiffCheck;
    private final JTextField exportFolderField;
    private final JLabel noteLabel;
    private final JCheckBox bringToFrontCheck;

    public FloodScenarioDialog(Frame owner, Layer rasterLayer) {
        super(owner, I18n.t("Inundacion por lluvia..."), true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        rasterCombo = new JComboBox<>(TopographyWorkflowSupport.getAvailableRasterLayers().toArray(new Layer[0]));
        if (rasterLayer != null) {
            rasterCombo.setSelectedItem(rasterLayer);
        } else if (TopographyWorkflowSupport.resolvePreferredRasterLayer() != null) {
            rasterCombo.setSelectedItem(TopographyWorkflowSupport.resolvePreferredRasterLayer());
        }
        baseNameField = new JTextField(buildDefaultBaseName(), 28);
        detailCombo = new JComboBox<>(DrainageExtractionService.AnalysisDetail.values());
        detailCombo.setSelectedItem(DrainageExtractionService.AnalysisDetail.BALANCED);
        conditioningCombo = new JComboBox<>(DrainageExtractionService.HydrologicConditioning.values());
        conditioningCombo.setSelectedItem(DrainageExtractionService.HydrologicConditioning.ADVANCED);
        thresholdField = new JTextField("30", 12);
        rainfallField = new JTextField("50", 12);
        runoffField = new JTextField("70", 12);
        minimumDepthField = new JTextField("15", 12);
        exportGeoTiffCheck = new JCheckBox(I18n.t("Exportar escenario(s) a GeoTIFF"), false);
        exportFolderField = new JTextField(buildDefaultExportDirectory().getAbsolutePath(), 28);
        bringToFrontCheck = new JCheckBox(I18n.t("Subir escenario al frente"), true);
        noteLabel = new JLabel();

        rasterCombo.addActionListener(e -> {
            baseNameField.setText(buildDefaultBaseName());
            updateNote();
        });
        detailCombo.addActionListener(e -> updateNote());
        conditioningCombo.addActionListener(e -> updateNote());
        rainfallField.addActionListener(e -> updateNote());
        runoffField.addActionListener(e -> updateNote());
        minimumDepthField.addActionListener(e -> updateNote());
        exportGeoTiffCheck.addActionListener(e -> refreshExportControls());
        updateNote();
        refreshExportControls();

        add(WindowLayoutSupport.createVerticalScrollPane(buildForm(), 760, 500), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 840, 660, 740, 520);
        setLocationRelativeTo(owner);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        panel.add(new JLabel(I18n.t("DEM raster origen:")), gc);
        gc.gridy++;
        panel.add(rasterCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Base / nombre del escenario:")), gc);
        gc.gridy++;
        panel.add(baseNameField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Detalle de analisis:")), gc);
        gc.gridy++;
        panel.add(detailCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Tratamiento hidrologico:")), gc);
        gc.gridy++;
        panel.add(conditioningCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Lluvia del escenario (mm):")), gc);
        gc.gridy++;
        panel.add(rainfallField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Coeficiente de escorrentia (%):")), gc);
        gc.gridy++;
        panel.add(runoffField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Umbral de acumulacion (celdas):")), gc);
        gc.gridy++;
        panel.add(thresholdField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Profundidad minima visible (mm):")), gc);
        gc.gridy++;
        panel.add(minimumDepthField, gc);

        gc.gridy++;
        panel.add(exportGeoTiffCheck, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Carpeta GeoTIFF de salida:")), gc);
        gc.gridy++;
        JPanel exportPanel = new JPanel(new BorderLayout(6, 0));
        exportPanel.add(exportFolderField, BorderLayout.CENTER);
        JButton browseButton = new JButton(I18n.t("Buscar..."));
        browseButton.addActionListener(e -> chooseExportFolder());
        exportPanel.add(browseButton, BorderLayout.EAST);
        panel.add(exportPanel, gc);

        gc.gridy++;
        panel.add(bringToFrontCheck, gc);

        gc.gridy++;
        noteLabel.setBorder(BorderFactory.createTitledBorder(I18n.t("Criterio tecnico")));
        panel.add(noteLabel, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton generateButton = new JButton(I18n.t("Generar escenario"));
        generateButton.addActionListener(e -> startGeneration());

        panel.add(cancelButton);
        panel.add(generateButton);
        DialogKeyboardSupport.install(this, generateButton, this::dispose);
        return panel;
    }

    private void updateNote() {
        String rainfall = rainfallField.getText() != null ? rainfallField.getText().trim() : "50";
        String runoff = runoffField.getText() != null ? runoffField.getText().trim() : "70";
        noteLabel.setText("<html><div style='width:360px'>"
                + I18n.t("Modelo simplificado: combina retencion topografica local (depresiones del DEM) y concentracion relativa por acumulacion.")
                + "<br><br><b>" + I18n.t("Escenario actual:") + "</b> "
                + rainfall + " mm | "
                + runoff + "% " + I18n.t("de escorrentia efectiva.")
                + "<br><br>" + I18n.t("Puedes comparar varios escenarios escribiendo valores separados por coma, por ejemplo 20, 50, 100.")
                + "<br><br>" + I18n.t("Sirve para aproximacion territorial preliminar. No reemplaza modelacion hidraulica 1D/2D ni calibracion hidrometeorologica.")
                + "<br><br>"
                + TopographyWorkflowSupport.buildRasterOperationalGuidanceFragment((Layer) rasterCombo.getSelectedItem(), "flood")
                + "</div></html>");
    }

    private void refreshExportControls() {
        boolean enabled = exportGeoTiffCheck.isSelected();
        exportFolderField.setEnabled(enabled);
    }

    private void chooseExportFolder() {
        JFileChooser chooser = FileChooserSupport.createChooser("flood-scenario-export", I18n.t("Carpeta de salida GeoTIFF"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(buildDefaultExportDirectory());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File directory = chooser.getSelectedFile();
            if (directory != null) {
                exportFolderField.setText(directory.getAbsolutePath());
                FileChooserSupport.rememberFile("flood-scenario-export", directory);
            }
        }
    }

    private void startGeneration() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (!(rasterLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes elegir un DEM raster valido para el escenario de inundacion."));
            return;
        }

        final int threshold;
        try {
            threshold = Integer.parseInt(thresholdField.getText().trim());
            if (threshold < 2) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("El umbral de acumulacion debe ser un entero mayor o igual a 2."));
            return;
        }

        final List<Double> rainfallScenarios;
        try {
            rainfallScenarios = FloodScenarioService.parseRainfallScenarioList(rainfallField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("La lluvia del escenario debe ser numerica, mayor a cero y puede tener varios valores separados por coma."));
            return;
        }

        final double runoffCoefficient;
        try {
            double parsed = Double.parseDouble(runoffField.getText().trim().replace(",", "."));
            if (parsed > 1d) {
                parsed = parsed / 100d;
            }
            runoffCoefficient = Math.max(0.01d, Math.min(1d, parsed));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("El coeficiente de escorrentia debe ser numerico. Puedes poner 70 o 0.70."));
            return;
        }

        final double minimumDepthMillimeters;
        try {
            minimumDepthMillimeters = Double.parseDouble(minimumDepthField.getText().trim().replace(",", "."));
            if (minimumDepthMillimeters < 0d) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("La profundidad minima visible debe ser numerica y mayor o igual a cero."));
            return;
        }

        final boolean exportGeoTiff = exportGeoTiffCheck.isSelected();
        final File exportDirectory;
        if (exportGeoTiff) {
            String folderText = exportFolderField.getText() != null ? exportFolderField.getText().trim() : "";
            if (folderText.isBlank()) {
                JOptionPane.showMessageDialog(this, I18n.t("Debes indicar una carpeta de salida para exportar GeoTIFF."));
                return;
            }
            exportDirectory = new File(folderText);
        } else {
            exportDirectory = null;
        }

        final String baseName = baseNameField.getText().trim();
        final DrainageExtractionService.AnalysisDetail detail =
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem();
        final DrainageExtractionService.HydrologicConditioning conditioning =
                (DrainageExtractionService.HydrologicConditioning) conditioningCombo.getSelectedItem();

        new SwingWorker<ScenarioBatchResult, Void>() {
            @Override
            protected ScenarioBatchResult doInBackground() throws Exception {
                List<FloodScenarioService.FloodScenarioResult> scenarios = new ArrayList<>();
                List<File> exportedFiles = new ArrayList<>();
                for (double rainfallMillimeters : rainfallScenarios) {
                    FloodScenarioService.FloodScenarioRequest request =
                            new FloodScenarioService.FloodScenarioRequest(
                                    rasterLayer,
                                    baseName,
                                    detail,
                                    conditioning,
                                    threshold,
                                    rainfallMillimeters,
                                    runoffCoefficient,
                                    minimumDepthMillimeters
                            );
                    FloodScenarioService.FloodScenarioResult result = FloodScenarioService.generateScenario(request);
                    scenarios.add(result);
                    if (exportGeoTiff) {
                        File outputFile = new File(exportDirectory, FloodScenarioService.buildScenarioExportFileName(result));
                        exportedFiles.add(FloodScenarioService.exportScenarioDepthGeoTiff(result, outputFile));
                    }
                }
                return new ScenarioBatchResult(scenarios, exportedFiles);
            }

            @Override
            protected void done() {
                try {
                    ScenarioBatchResult result = get();
                    addResults(result);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            FloodScenarioDialog.this,
                            I18n.t("No se pudo generar el escenario preliminar de inundacion:") + "\n" + cause.getMessage(),
                            I18n.t("Inundacion por lluvia"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addResults(ScenarioBatchResult batchResult) {
        if (batchResult == null || batchResult.results() == null || batchResult.results().isEmpty()) {
            return;
        }
        if (CatgisDesktopApp.currentProject == null) {
            CatgisDesktopApp.currentProject = new Project(I18n.t("Proyecto actual"));
        }
        List<Layer> addedLayers = new ArrayList<>();
        FloodScenarioService.FloodScenarioResult lastResult = null;
        for (FloodScenarioService.FloodScenarioResult result : batchResult.results()) {
            if (result == null) {
                continue;
            }
            CatgisDesktopApp.currentProject.addLayer(result.layer());
            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.addLayer(result.layer());
                CatgisDesktopApp.layersPanel.selectLayer(result.layer());
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(result.layer(), result.data());
                CatgisDesktopApp.mapPanel.showOpenedFile(result.layer().getName());
            }
            addedLayers.add(result.layer());
            lastResult = result;
        }
        if (bringToFrontCheck.isSelected()) {
            TopographyWorkflowSupport.placeLayersAtFront(addedLayers);
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null && lastResult != null) {
            if (batchResult.results().size() == 1) {
                String areaText = Double.isFinite(lastResult.affectedAreaSquareMeters())
                        ? I18n.format("Escenario preliminar de inundacion generado: {0} ha potencialmente afectadas.",
                        String.format(Locale.US, "%.2f", lastResult.affectedAreaSquareMeters() / 10000d))
                        : I18n.t("Escenario preliminar de inundacion generado.");
                CatgisDesktopApp.statusBar.setMessage(areaText);
            } else {
                CatgisDesktopApp.statusBar.setMessage(I18n.t("Escenarios preliminares de inundacion generados: ") + batchResult.results().size());
            }
        }
        if (batchResult.results().size() > 1 || !batchResult.exportedFiles().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    buildBatchSummary(batchResult),
                    I18n.t("Inundacion por lluvia"),
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private String buildDefaultBaseName() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        return layer != null ? layer.getName() : "DEM";
    }

    private File buildDefaultExportDirectory() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        if (layer != null && layer.getPath() != null && !layer.getPath().isBlank()) {
            File sourceFile = new File(layer.getPath());
            File parent = sourceFile.getParentFile();
            if (parent != null && parent.isDirectory()) {
                return parent;
            }
        }
        File remembered = FileChooserSupport.resolveSuggestedFile("flood-scenario-export", new File("inundacion_preliminar.tif"));
        File directory = remembered != null ? remembered.getParentFile() : null;
        return directory != null && directory.isDirectory() ? directory : new File(System.getProperty("user.home", "."));
    }

    private String buildBatchSummary(ScenarioBatchResult batchResult) {
        StringBuilder sb = new StringBuilder("<html><div style='width:380px'>");
        if (batchResult.results().size() > 1) {
            sb.append("<b>")
                    .append(I18n.t("Comparador preliminar de escenarios"))
                    .append("</b><br><br>");
        } else {
            sb.append("<b>")
                    .append(I18n.t("Escenario preliminar generado"))
                    .append("</b><br><br>");
        }
        for (FloodScenarioService.FloodScenarioResult result : batchResult.results()) {
            if (result == null || result.request() == null) {
                continue;
            }
            double hectares = Double.isFinite(result.affectedAreaSquareMeters())
                    ? result.affectedAreaSquareMeters() / 10000d
                    : Double.NaN;
            sb.append(result.layer().getName())
                    .append(": ")
                    .append(Double.isFinite(hectares) ? String.format(Locale.US, "%.2f ha", hectares) : "? ha")
                    .append(" | ")
                    .append(String.format(Locale.US, "%.2f m", result.maxDepthMeters()))
                    .append(" ")
                    .append(I18n.t("de profundidad maxima."))
                    .append("<br>");
        }
        if (!batchResult.exportedFiles().isEmpty()) {
            sb.append("<br><b>")
                    .append(I18n.t("GeoTIFF exportados:"))
                    .append("</b> ")
                    .append(batchResult.exportedFiles().size());
        }
        sb.append("</div></html>");
        return sb.toString();
    }

    public static void open() {
        Layer preferred = TopographyWorkflowSupport.resolvePreferredRasterLayer();
        if (preferred == null) {
            TopographyWorkflowSupport.showNoRasterMessage();
            return;
        }
        open(preferred);
    }

    public static void open(Layer rasterLayer) {
        if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
            TopographyWorkflowSupport.showNoRasterMessage();
            return;
        }
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.getMainFrameSafe());
        FloodScenarioDialog dialog = new FloodScenarioDialog(owner, rasterLayer);
        dialog.setVisible(true);
    }

    private record ScenarioBatchResult(List<FloodScenarioService.FloodScenarioResult> results,
                                       List<File> exportedFiles) {
    }
}
