package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
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

public class BasinFromOutletDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JComboBox<OutletSourceMode> sourceModeCombo;
    private final JComboBox<Layer> pointLayerCombo;
    private final JTextField baseNameField;
    private final JComboBox<DrainageExtractionService.AnalysisDetail> detailCombo;
    private final JComboBox<DrainageExtractionService.HydrologicConditioning> conditioningCombo;
    private final JTextField thresholdField;
    private final JTextField snapRadiusField;
    private final JTextField xField;
    private final JTextField yField;
    private final JLabel sourceLabel;
    private final JLabel technicalHintLabel;
    private final JButton selectedPointButton;
    private final JButton captureButton;

    private Coordinate capturedCoordinate = null;
    private String capturedCrs = "";

    public BasinFromOutletDialog(Frame owner, Layer rasterLayer) {
        super(owner, I18n.t("Cuenca desde outlet..."), true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        rasterCombo = new JComboBox<>(TopographyWorkflowSupport.getAvailableRasterLayers().toArray(new Layer[0]));
        if (rasterLayer != null) {
            rasterCombo.setSelectedItem(rasterLayer);
        } else if (TopographyWorkflowSupport.resolvePreferredRasterLayer() != null) {
            rasterCombo.setSelectedItem(TopographyWorkflowSupport.resolvePreferredRasterLayer());
        }

        sourceModeCombo = new JComboBox<>(OutletSourceMode.values());
        pointLayerCombo = new JComboBox<>();
        baseNameField = new JTextField(buildDefaultBaseName(), 26);
        detailCombo = new JComboBox<>(DrainageExtractionService.AnalysisDetail.values());
        detailCombo.setSelectedItem(DrainageExtractionService.AnalysisDetail.BALANCED);
        conditioningCombo = new JComboBox<>(DrainageExtractionService.HydrologicConditioning.values());
        conditioningCombo.setSelectedItem(DrainageExtractionService.HydrologicConditioning.ADVANCED);
        thresholdField = new JTextField("30", 12);
        snapRadiusField = new JTextField("6", 12);
        xField = new JTextField("", 18);
        yField = new JTextField("", 18);
        xField.setEditable(false);
        yField.setEditable(false);
        sourceLabel = new JLabel(I18n.t("Sin point definido"));
        technicalHintLabel = new JLabel();
        selectedPointButton = new JButton(I18n.t("Usar punto seleccionado"));
        captureButton = new JButton(I18n.t("Capturar en mapa..."));

        rasterCombo.addActionListener(e -> {
            baseNameField.setText(buildDefaultBaseName());
            updateTechnicalHint();
        });
        detailCombo.addActionListener(e -> updateTechnicalHint());
        conditioningCombo.addActionListener(e -> updateTechnicalHint());
        sourceModeCombo.addActionListener(e -> refreshSourceModeState());
        selectedPointButton.addActionListener(e -> loadSelectedPoint());
        captureButton.addActionListener(e -> capturePointOnMap());
        refreshPointLayerModel();
        refreshSourceModeState();
        updateTechnicalHint();

        add(WindowLayoutSupport.createVerticalScrollPane(buildForm(), 760, 500), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        tryPrefillFromSelectedPoint();
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 840, 680, 740, 540);
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

        panel.add(new JLabel(I18n.t("Raster DEM origen:")), gc);
        gc.gridy++;
        panel.add(rasterCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Modo de outlets:")), gc);
        gc.gridy++;
        panel.add(sourceModeCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Capa puntual de outlets:")), gc);
        gc.gridy++;
        panel.add(pointLayerCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Prefijo/base de salida:")), gc);
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
        panel.add(new JLabel(I18n.t("Umbral de ajuste a canal (celdas):")), gc);
        gc.gridy++;
        panel.add(thresholdField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Radio de ajuste (celdas):")), gc);
        gc.gridy++;
        panel.add(snapRadiusField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Coordenada outlet/pour point:")), gc);
        gc.gridy++;
        JPanel coordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets = new Insets(0, 0, 4, 6);
        cc.gridx = 0;
        cc.gridy = 0;
        cc.anchor = GridBagConstraints.WEST;
        coordPanel.add(new JLabel("X"), cc);
        cc.gridx = 1;
        cc.weightx = 1;
        cc.fill = GridBagConstraints.HORIZONTAL;
        coordPanel.add(xField, cc);
        cc.gridx = 0;
        cc.gridy = 1;
        cc.weightx = 0;
        cc.fill = GridBagConstraints.NONE;
        coordPanel.add(new JLabel("Y"), cc);
        cc.gridx = 1;
        cc.weightx = 1;
        cc.fill = GridBagConstraints.HORIZONTAL;
        coordPanel.add(yField, cc);
        panel.add(coordPanel, gc);

        gc.gridy++;
        panel.add(sourceLabel, gc);

        gc.gridy++;
        JPanel capturePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        capturePanel.add(selectedPointButton);
        capturePanel.add(captureButton);
        panel.add(capturePanel, gc);

        gc.gridy++;
        JLabel help = new JLabel(I18n.t("Delimita la cuenca que aporta a un outlet/pour point, ajustando el punto al flujo mas cercano."));
        help.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(help, gc);

        gc.gridy++;
        technicalHintLabel.setBorder(BorderFactory.createTitledBorder(I18n.t("Lectura tecnica")));
        panel.add(technicalHintLabel, gc);

        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton generateButton = new JButton(I18n.t("Generar cuenca"));
        generateButton.addActionListener(e -> startGeneration());

        panel.add(cancelButton);
        panel.add(generateButton);
        DialogKeyboardSupport.install(this, generateButton, this::dispose);
        return panel;
    }

    private void tryPrefillFromSelectedPoint() {
        loadSelectedPoint(false);
    }

    private void refreshPointLayerModel() {
        List<Layer> pointLayers = new ArrayList<>();
        for (Layer layer : VectorLayerUtils.getVectorLayers()) {
            if ("POINT".equalsIgnoreCase(VectorLayerUtils.resolveGeometryFamily(layer))) {
                pointLayers.add(layer);
            }
        }
        pointLayerCombo.setModel(new DefaultComboBoxModel<>(pointLayers.toArray(new Layer[0])));
    }

    private void refreshSourceModeState() {
        OutletSourceMode mode = (OutletSourceMode) sourceModeCombo.getSelectedItem();
        boolean pointLayerMode = mode == OutletSourceMode.POINT_LAYER;
        pointLayerCombo.setEnabled(pointLayerMode);
        xField.setEnabled(!pointLayerMode);
        yField.setEnabled(!pointLayerMode);
        selectedPointButton.setEnabled(!pointLayerMode);
        captureButton.setEnabled(!pointLayerMode);
        sourceLabel.setText(pointLayerMode
                ? I18n.t("La capa puntual se ajustara outlet por outlet al drenaje mas cercano.")
                : (capturedCoordinate != null
                ? sourceLabel.getText()
                : I18n.t("Sin point definido")));
    }

    private void loadSelectedPoint() {
        loadSelectedPoint(true);
    }

    private void loadSelectedPoint(boolean showWarning) {
        if (CatgisDesktopApp.mapPanel == null) {
            if (showWarning) {
                JOptionPane.showMessageDialog(this, I18n.t("No hay mapa activo para leer la seleccion actual."));
            }
            return;
        }
        SimpleFeature feature = CatgisDesktopApp.mapPanel.getSelectedFeatureRef();
        Layer layer = CatgisDesktopApp.mapPanel.getSelectedLayerRef();
        Coordinate coordinate = extractRepresentativePoint(feature);
        if (coordinate == null) {
            if (showWarning) {
                JOptionPane.showMessageDialog(this, I18n.t("Selecciona primero una entidad puntual para usarla como outlet."));
            }
            return;
        }
        capturedCoordinate = new Coordinate(coordinate);
        capturedCrs = layer != null ? layer.getSourceCRS() : "";
        updateCoordinateFields(I18n.t("Punto seleccionado"), capturedCoordinate, capturedCrs);
    }

    private void capturePointOnMap() {
        if (CatgisDesktopApp.mapPanel == null) {
            JOptionPane.showMessageDialog(this, I18n.t("No hay mapa activo para capturar un point."));
            return;
        }
        setVisible(false);
        CatgisDesktopApp.mapPanel.startPointCapture(new MapPanel.MapPointCaptureHandler() {
            @Override
            public void onPointCaptured(Coordinate coordinate, String sourceCrs) {
                capturedCoordinate = coordinate != null ? new Coordinate(coordinate) : null;
                capturedCrs = sourceCrs != null ? sourceCrs : "";
                updateCoordinateFields(I18n.t("Capturado en mapa"), capturedCoordinate, capturedCrs);
                SwingUtilities.invokeLater(() -> {
                    BasinFromOutletDialog.this.setVisible(true);
                    BasinFromOutletDialog.this.toFront();
                });
            }

            @Override
            public void onCaptureCanceled() {
                SwingUtilities.invokeLater(() -> {
                    BasinFromOutletDialog.this.setVisible(true);
                    BasinFromOutletDialog.this.toFront();
                });
            }
        });
    }

    private void updateCoordinateFields(String sourceText, Coordinate coordinate, String sourceCrs) {
        if (coordinate == null) {
            xField.setText("");
            yField.setText("");
            sourceLabel.setText(I18n.t("Sin point definido"));
            return;
        }
        xField.setText(String.format(Locale.US, "%.6f", coordinate.x));
        yField.setText(String.format(Locale.US, "%.6f", coordinate.y));
        String crsText = (sourceCrs != null && !sourceCrs.isBlank()) ? sourceCrs : "sin CRS";
        sourceLabel.setText(sourceText + " | " + crsText);
    }

    private void startGeneration() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (!(rasterLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Selecciona un DEM raster valido."));
            return;
        }
        final OutletSourceMode sourceMode = (OutletSourceMode) sourceModeCombo.getSelectedItem();

        final int threshold;
        final int snapRadius;
        try {
            threshold = Integer.parseInt(thresholdField.getText().trim());
            if (threshold < 2) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("El umbral de acumulacion debe ser un entero mayor o igual a 2."));
            return;
        }
        try {
            snapRadius = Integer.parseInt(snapRadiusField.getText().trim());
            if (snapRadius < 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("El radio de ajuste debe ser un entero mayor o igual a 0."));
            return;
        }

        final String baseName = baseNameField.getText().trim();
        final DrainageExtractionService.AnalysisDetail detail =
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem();
        final DrainageExtractionService.HydrologicConditioning conditioning =
                (DrainageExtractionService.HydrologicConditioning) conditioningCombo.getSelectedItem();

        final TerrainHydrologyAnalysisService.PourPointRequest singleRequest;
        final TerrainHydrologyAnalysisService.BatchPourPointRequest batchRequest;
        if (sourceMode == OutletSourceMode.POINT_LAYER) {
            Layer outletLayer = (Layer) pointLayerCombo.getSelectedItem();
            if (outletLayer == null) {
                JOptionPane.showMessageDialog(this, I18n.t("Debes elegir una capa puntual con uno o mas outlets."));
                return;
            }
            ShapefileData outletData = CatgisDesktopApp.mapPanel != null
                    ? CatgisDesktopApp.mapPanel.getShapefileData(outletLayer)
                    : null;
            if (outletData == null) {
                outletData = VectorLayerUtils.ensureVectorData(outletLayer);
            }
            outletData = TopographyWorkflowSupport.projectVectorDataToCurrentProject(outletLayer, outletData);
            if (outletData == null || outletData.getFeatures() == null || outletData.getFeatures().isEmpty()) {
                JOptionPane.showMessageDialog(this, I18n.t("La capa de outlets no tiene puntos utiles para delimitar cuencas."));
                return;
            }
            singleRequest = null;
            batchRequest = new TerrainHydrologyAnalysisService.BatchPourPointRequest(
                    rasterLayer,
                    baseName,
                    detail,
                    conditioning,
                    threshold,
                    snapRadius,
                    outletData,
                    outletLayer.getSourceCRS(),
                    outletLayer.getName()
            );
        } else {
            if (capturedCoordinate == null) {
                JOptionPane.showMessageDialog(this, I18n.t("Debes indicar un outlet/pour point desde seleccion o captura."));
                return;
            }
            singleRequest = new TerrainHydrologyAnalysisService.PourPointRequest(
                    rasterLayer,
                    baseName,
                    detail,
                    conditioning,
                    threshold,
                    snapRadius,
                    new Coordinate(capturedCoordinate),
                    capturedCrs
            );
            batchRequest = null;
        }

        new SwingWorker<OutletGenerationResult, Void>() {
            @Override
            protected OutletGenerationResult doInBackground() throws Exception {
                if (batchRequest != null) {
                    TerrainHydrologyAnalysisService.BatchPourPointResult result =
                            TerrainHydrologyAnalysisService.generateBasinsFromOutletLayer(batchRequest);
                    List<TerrainHydrologyAnalysisService.GeneratedVectorLayer> layers = new ArrayList<>();
                    if (result.outletsLayer() != null) {
                        layers.add(result.outletsLayer());
                    }
                    if (result.basinsLayer() != null) {
                        layers.add(result.basinsLayer());
                    }
                    return new OutletGenerationResult(
                            layers,
                            I18n.format("Cuencas por outlets generadas: {0} de {1} puntos procesados.",
                                    result.generatedCount(),
                                    result.requestedCount())
                    );
                }

                TerrainHydrologyAnalysisService.PourPointResult result =
                        TerrainHydrologyAnalysisService.generateBasinFromPourPoint(singleRequest);
                List<TerrainHydrologyAnalysisService.GeneratedVectorLayer> layers = new ArrayList<>();
                if (result.outletLayer() != null) {
                    layers.add(result.outletLayer());
                }
                if (result.basinLayer() != null) {
                    layers.add(result.basinLayer());
                }
                return new OutletGenerationResult(
                        layers,
                        I18n.format("Cuenca por outlet generada: {0} celdas aportantes.", result.basinCellCount())
                );
            }

            @Override
            protected void done() {
                try {
                    OutletGenerationResult result = get();
                    addResults(result);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            BasinFromOutletDialog.this,
                            I18n.t("No se pudo delimitar la cuenca desde el outlet:") + "\n" + cause.getMessage(),
                            I18n.t("Cuenca desde outlet"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addResults(OutletGenerationResult result) {
        if (result == null || result.layers() == null || result.layers().isEmpty()) {
            return;
        }
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project(I18n.t("Proyecto actual")));
        }
        Layer lastAdded = null;
        List<Layer> desiredFrontOrder = new ArrayList<>();

        for (TerrainHydrologyAnalysisService.GeneratedVectorLayer generated : result.layers()) {
            if (generated == null || generated.layer() == null || generated.data() == null) {
                continue;
            }
            AppContext.project().addLayer(generated.layer());
            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.addLayer(generated.layer());
            }
            ShapefileData projected = TopographyWorkflowSupport.projectVectorDataToCurrentProject(generated.layer(), generated.data());
            persistGeneratedLayerIfNeeded(generated.layer(), projected);
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(generated.layer(), projected);
            }
            generated.layer().setFeatureCount(projected != null ? projected.getFeatureCount() : generated.layer().getFeatureCount());
            desiredFrontOrder.add(generated.layer());
            lastAdded = generated.layer();
        }
        TopographyWorkflowSupport.placeLayersAtFront(desiredFrontOrder);

        if (lastAdded != null && CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.selectLayer(lastAdded);
        }
        if (lastAdded != null && CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.showOpenedFile(lastAdded.getName());
        }

        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(result.statusMessage());
        }
    }

    private void persistGeneratedLayerIfNeeded(Layer layer, ShapefileData data) {
        if (layer == null || data == null || ExportVectorLayerAction.hasSupportedVectorPath(layer)) {
            return;
        }
        try {
            File outputFile = buildManagedOutputFile(layer);
            if (ExportVectorLayerAction.saveLayerDataToFile(layer, data, outputFile, this, false)) {
                layer.setPath(outputFile.getAbsolutePath());
            }
        } catch (Exception ignored) { CatgisLogger.warn("BasinFromOutletDialog: operation failed", ignored); }
    }

    private File buildManagedOutputFile(Layer layer) {
        File baseDirectory = null;
        if (AppContext.project() != null && AppContext.project().getProjectFile() != null) {
            File projectFile = AppContext.project().getProjectFile();
            baseDirectory = projectFile != null ? projectFile.getParentFile() : null;
        }
        if (baseDirectory == null || !baseDirectory.isDirectory()) {
            baseDirectory = new File(System.getProperty("java.io.tmpdir"), "catgis-hydrology-cache");
        } else {
            baseDirectory = new File(baseDirectory, "catgis-hydrology-cache");
        }
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        return new File(baseDirectory, sanitizeFileStem(layer.getName()) + ".geojson");
    }

    private String sanitizeFileStem(String value) {
        if (value == null || value.isBlank()) {
            return "capa_hidrologica";
        }
        return value
                .replaceAll("[\\\\/:*?\"<>|]+", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
    }

    private Coordinate extractRepresentativePoint(SimpleFeature feature) {
        if (feature == null) {
            return null;
        }
        Object geometryObject = feature.getDefaultGeometry();
        if (!(geometryObject instanceof Geometry geometry) || geometry.isEmpty()) {
            return null;
        }
        if (geometry instanceof Point point) {
            return point.getCoordinate();
        }
        Point centroid = geometry.getCentroid();
        return centroid != null ? centroid.getCoordinate() : null;
    }

    private String buildDefaultBaseName() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        return layer != null ? layer.getName() : "DEM";
    }

    private void updateTechnicalHint() {
        technicalHintLabel.setText("<html><div style='width:320px'>"
                + "La cuenca por outlet usa el DEM operativo del proyecto y ajusta el punto al drenaje mas cercano dentro del radio indicado."
                + "<br><br>Tambien puede trabajar por lote desde una capa puntual para generar varias cuencas/subcuencas en una sola corrida."
                + "<br><br>"
                + TopographyWorkflowSupport.buildRasterOperationalGuidanceFragment((Layer) rasterCombo.getSelectedItem(), "basin")
                + "</div></html>");
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
        BasinFromOutletDialog dialog = new BasinFromOutletDialog(owner, rasterLayer);
        dialog.setVisible(true);
    }

    private enum OutletSourceMode {
        SELECTED_OR_CAPTURED("Punto seleccionado o capturado"),
        POINT_LAYER("Capa puntual / varios outlets");

        private final String label;

        OutletSourceMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private record OutletGenerationResult(List<TerrainHydrologyAnalysisService.GeneratedVectorLayer> layers,
                                          String statusMessage) {
    }
}
