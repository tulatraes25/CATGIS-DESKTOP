package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class TerrainHydrologyAnalysisDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JTextField baseNameField;
    private final JComboBox<DrainageExtractionService.AnalysisDetail> detailCombo;
    private final JComboBox<DrainageExtractionService.HydrologicConditioning> conditioningCombo;
    private final JTextField thresholdField;
    private final JTextField minimumBasinField;
    private final JLabel detailHintLabel;
    private final JLabel conditioningHintLabel;
    private final JLabel thresholdHintLabel;
    private final JLabel blendHintLabel;
    private final JCheckBox hillshadeCheck;
    private final JCheckBox slopeCheck;
    private final JCheckBox aspectCheck;
    private final JCheckBox flowDirectionCheck;
    private final JCheckBox accumulationCheck;
    private final JCheckBox streamOrderLinesCheck;
    private final JCheckBox basinsCheck;
    private final JCheckBox outletsCheck;
    private final JCheckBox arrowsCheck;
    private boolean thresholdAuto = true;
    private boolean minimumBasinAuto = true;
    private boolean adjustingRecommendedValues;

    public TerrainHydrologyAnalysisDialog(Frame owner, Layer rasterLayer) {
        super(owner, I18n.t("Analisis topohidrologico..."), true);
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
        minimumBasinField = new JTextField("120", 12);
        detailHintLabel = createHintLabel();
        conditioningHintLabel = createHintLabel();
        thresholdHintLabel = createHintLabel();
        blendHintLabel = createHintLabel();

        hillshadeCheck = new JCheckBox(I18n.t("Hillshade"), true);
        slopeCheck = new JCheckBox(I18n.t("Pendiente"), true);
        aspectCheck = new JCheckBox(I18n.t("Aspecto"), true);
        flowDirectionCheck = new JCheckBox(I18n.t("Direccion de flujo"), true);
        accumulationCheck = new JCheckBox(I18n.t("Acumulacion de flujo"), true);
        streamOrderLinesCheck = new JCheckBox(I18n.t("Orden de cauces"), true);
        basinsCheck = new JCheckBox(I18n.t("Cuencas / subcuencas"), true);
        outletsCheck = new JCheckBox(I18n.t("Outlets / puntos de salida"), true);
        arrowsCheck = new JCheckBox(I18n.t("Flechas de flujo"), true);

        rasterCombo.addActionListener(e -> {
            baseNameField.setText(buildDefaultBaseName());
            updateGuidance();
        });
        detailCombo.addActionListener(e -> {
            applyRecommendedParameters(false);
            updateGuidance();
        });
        conditioningCombo.addActionListener(e -> updateGuidance());
        hillshadeCheck.addActionListener(e -> updateGuidance());
        thresholdField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if (!adjustingRecommendedValues) {
                thresholdAuto = false;
            }
            updateGuidance();
        }));
        minimumBasinField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if (!adjustingRecommendedValues) {
                minimumBasinAuto = false;
            }
            updateGuidance();
        }));
        applyRecommendedParameters(true);
        updateGuidance();

        add(WindowLayoutSupport.createVerticalScrollPane(buildForm(), 760, 500), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
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
        panel.add(new JLabel(I18n.t("Umbral de red / acumulacion (celdas):")), gc);
        gc.gridy++;
        panel.add(thresholdField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Tamano minimo de cuenca (celdas):")), gc);
        gc.gridy++;
        panel.add(minimumBasinField, gc);

        gc.gridy++;
        JPanel guidancePanel = new JPanel(new GridBagLayout());
        guidancePanel.setOpaque(false);
        guidancePanel.setBorder(BorderFactory.createTitledBorder(I18n.t("Lectura tecnica")));
        GridBagConstraints infoGc = new GridBagConstraints();
        infoGc.gridx = 0;
        infoGc.gridy = 0;
        infoGc.anchor = GridBagConstraints.WEST;
        infoGc.fill = GridBagConstraints.HORIZONTAL;
        infoGc.insets = new Insets(2, 2, 2, 2);
        infoGc.weightx = 1;
        guidancePanel.add(detailHintLabel, infoGc);
        infoGc.gridy++;
        guidancePanel.add(conditioningHintLabel, infoGc);
        infoGc.gridy++;
        guidancePanel.add(thresholdHintLabel, infoGc);
        infoGc.gridy++;
        guidancePanel.add(blendHintLabel, infoGc);
        panel.add(guidancePanel, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Salidas raster:")), gc);
        gc.gridy++;
        panel.add(hillshadeCheck, gc);
        gc.gridy++;
        panel.add(slopeCheck, gc);
        gc.gridy++;
        panel.add(aspectCheck, gc);
        gc.gridy++;
        panel.add(flowDirectionCheck, gc);
        gc.gridy++;
        panel.add(accumulationCheck, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Salidas vectoriales:")), gc);
        gc.gridy++;
        panel.add(streamOrderLinesCheck, gc);
        gc.gridy++;
        panel.add(basinsCheck, gc);
        gc.gridy++;
        panel.add(outletsCheck, gc);
        gc.gridy++;
        panel.add(arrowsCheck, gc);

        gc.gridy++;
        JLabel help = new JLabel(I18n.t("Genera relieve, flujo, orden de cauces, cuencas y flechas poligonales desde el mismo DEM."));
        help.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(help, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton generateButton = new JButton(I18n.t("Generar analisis"));
        generateButton.addActionListener(e -> startGeneration());

        panel.add(cancelButton);
        panel.add(generateButton);
        DialogKeyboardSupport.install(this, generateButton, this::dispose);
        return panel;
    }

    private void startGeneration() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (!(rasterLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Selecciona un DEM raster valido para el analisis topohidrologico."));
            return;
        }

        int threshold;
        int minimumBasinCells;
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
            minimumBasinCells = Integer.parseInt(minimumBasinField.getText().trim());
            if (minimumBasinCells < 8) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("El tamano minimo de cuenca debe ser un entero mayor o igual a 8."));
            return;
        }

        TerrainHydrologyAnalysisService.AnalysisRequest request = new TerrainHydrologyAnalysisService.AnalysisRequest(
                rasterLayer,
                baseNameField.getText().trim(),
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem(),
                (DrainageExtractionService.HydrologicConditioning) conditioningCombo.getSelectedItem(),
                threshold,
                minimumBasinCells,
                hillshadeCheck.isSelected(),
                slopeCheck.isSelected(),
                aspectCheck.isSelected(),
                flowDirectionCheck.isSelected(),
                accumulationCheck.isSelected(),
                streamOrderLinesCheck.isSelected(),
                basinsCheck.isSelected(),
                outletsCheck.isSelected(),
                arrowsCheck.isSelected()
        );

        new SwingWorker<TerrainHydrologyAnalysisService.AnalysisResult, Void>() {
            @Override
            protected TerrainHydrologyAnalysisService.AnalysisResult doInBackground() throws Exception {
                return TerrainHydrologyAnalysisService.generateAnalysis(request);
            }

            @Override
            protected void done() {
                try {
                    TerrainHydrologyAnalysisService.AnalysisResult result = get();
                    addResults(result);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            TerrainHydrologyAnalysisDialog.this,
                            I18n.t("No se pudo generar el analisis topohidrologico:") + "\n" + cause.getMessage(),
                            I18n.t("Analisis topohidrologico"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addResults(TerrainHydrologyAnalysisService.AnalysisResult result) {
        if (result == null) {
            return;
        }
        if (AppContext.project() == null) {
            AppContext.setCurrentProject(new Project(I18n.t("Proyecto actual")));
        }

        Layer lastAdded = null;
        List<Layer> desiredFrontOrder = new ArrayList<>();
        for (TerrainHydrologyAnalysisService.GeneratedRasterLayer raster : result.rasterLayers()) {
            AppContext.project().addLayer(raster.layer());
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.addLayer(raster.layer());
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(raster.layer(), raster.data());
            }
            lastAdded = raster.layer();
        }

        for (TerrainHydrologyAnalysisService.GeneratedVectorLayer vector : result.vectorLayers()) {
            ShapefileData projectedData = TopographyWorkflowSupport.projectVectorDataToCurrentProject(vector.layer(), vector.data());
            AppContext.project().addLayer(vector.layer());
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.addLayer(vector.layer());
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(vector.layer(), projectedData);
            }
            vector.layer().setFeatureCount(projectedData != null ? projectedData.getFeatureCount() : vector.layer().getFeatureCount());
            lastAdded = vector.layer();
        }

        for (int i = result.vectorLayers().size() - 1; i >= 0; i--) {
            desiredFrontOrder.add(result.vectorLayers().get(i).layer());
        }
        for (int i = result.rasterLayers().size() - 1; i >= 0; i--) {
            desiredFrontOrder.add(result.rasterLayers().get(i).layer());
        }
        TopographyWorkflowSupport.placeLayersAtFront(desiredFrontOrder);

        if (lastAdded != null) {
            if (CatgisDesktopApp.layersPanel != null) {
                AppContext.selectLayer(lastAdded);
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.showOpenedFile(lastAdded.getName());
            }
        }

        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(
                    I18n.format("Analisis topohidrologico generado: {0} raster(es), {1} vector(es).",
                            result.rasterLayers().size(),
                            result.vectorLayers().size())
            );
        }
    }

    private String buildDefaultBaseName() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        return layer != null ? layer.getName() : "DEM";
    }

    private JLabel createHintLabel() {
        JLabel label = new JLabel();
        label.setForeground(new Color(82, 92, 108));
        return label;
    }

    private void updateGuidance() {
        DrainageExtractionService.AnalysisDetail detail =
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem();
        DrainageExtractionService.HydrologicConditioning conditioning =
                (DrainageExtractionService.HydrologicConditioning) conditioningCombo.getSelectedItem();
        int threshold = safeParsePositiveInt(thresholdField.getText(), 30);
        int minimumBasinCells = safeParsePositiveInt(minimumBasinField.getText(), 120);

        detailHintLabel.setText("<html><b>" + I18n.t("Detalle:") + "</b> " + describeDetail(detail) + "</html>");
        conditioningHintLabel.setText("<html><b>" + I18n.t("Tratamiento:") + "</b> " + describeConditioning(conditioning) + "</html>");
        thresholdHintLabel.setText("<html><b>" + I18n.t("Lectura de umbrales:") + "</b> "
                + describeThreshold(threshold, minimumBasinCells) + "</html>");
        blendHintLabel.setText("<html><b>" + I18n.t("Composicion raster:") + "</b> "
                + (hillshadeCheck.isSelected()
                ? I18n.t("Hillshade sale con opacidad inicial baja y bordes mas limpios para mezclarlo mejor con pendiente, aspecto o acumulacion.")
                : I18n.t("Si activas hillshade, CATGIS lo genera listo para mezclar visualmente con otros raster con opacidad recomendada."))
                + "<br><br>"
                + TopographyWorkflowSupport.buildRasterOperationalGuidanceFragment((Layer) rasterCombo.getSelectedItem(), "drainage")
                + "</html>");
    }

    private void applyRecommendedParameters(boolean force) {
        DrainageExtractionService.AnalysisDetail detail =
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem();
        int recommendedThreshold = recommendedThreshold(detail);
        int recommendedMinimumBasin = recommendedMinimumBasin(detail);
        adjustingRecommendedValues = true;
        try {
            if (force || thresholdAuto || thresholdField.getText() == null || thresholdField.getText().isBlank()) {
                thresholdField.setText(String.valueOf(recommendedThreshold));
                thresholdAuto = true;
            }
            if (force || minimumBasinAuto || minimumBasinField.getText() == null || minimumBasinField.getText().isBlank()) {
                minimumBasinField.setText(String.valueOf(recommendedMinimumBasin));
                minimumBasinAuto = true;
            }
        } finally {
            adjustingRecommendedValues = false;
        }
    }

    private String describeDetail(DrainageExtractionService.AnalysisDetail detail) {
        if (detail == null) {
            return I18n.t("Equilibrado para trabajo general.");
        }
        return switch (detail) {
            case FAST -> I18n.t("Rapido y liviano para revisiones regionales o DEM grandes. Suele trabajar mejor con umbrales mas altos.");
            case BALANCED -> I18n.t("Equilibrado para la mayoria de trabajos; buen punto medio entre detalle, limpieza y tiempo.");
            case DETAILED -> I18n.t("Mas fino para sectores chicos o estudios locales; conviene acompañarlo con umbrales mas bajos y areas acotadas.");
        };
    }

    private String describeConditioning(DrainageExtractionService.HydrologicConditioning conditioning) {
        if (conditioning == null) {
            return I18n.t("Robusto, recomendado para la mayoria de los DEM.");
        }
        return switch (conditioning) {
            case BASIC -> I18n.t("Mas directo y rapido, pero puede dejar sumideros o planos conflictivos.");
            case ROBUST -> I18n.t("Recomendado para uso general: mejora planos y depresiones sin exagerar el modelado.");
            case ADVANCED -> I18n.t("Mas fuerte sobre flats y depresiones; util cuando el drenaje sale muy trabado o poco coherente.");
        };
    }

    private String describeThreshold(int threshold, int minimumBasinCells) {
        String networkDescription;
        if (threshold <= 15) {
            networkDescription = I18n.t("umbral muy bajo: red densa, sensible a microrelieve y posible ruido; recomendable para estudios locales");
        } else if (threshold <= 45) {
            networkDescription = I18n.t("umbral medio: buen compromiso para detalle local o intermedio");
        } else if (threshold <= 120) {
            networkDescription = I18n.t("umbral alto: prioriza cauces principales, limpia ramales menores y reduce aspecto cuadriculado");
        } else {
            networkDescription = I18n.t("umbral muy alto: deja solo drenajes mayores");
        }

        String basinDescription;
        if (minimumBasinCells <= 80) {
            basinDescription = I18n.t("cuencas minimas chicas, mas fragmentacion");
        } else if (minimumBasinCells <= 220) {
            basinDescription = I18n.t("cuencas minimas intermedias, uso general");
        } else {
            basinDescription = I18n.t("cuencas minimas grandes, resultado mas sintetico");
        }
        return networkDescription + " | " + basinDescription
                + " | " + I18n.t("si el drenaje sale muy trabado o cuadriculado, sube el umbral o usa tratamiento avanzado");
    }

    private int recommendedThreshold(DrainageExtractionService.AnalysisDetail detail) {
        if (detail == null) {
            return 30;
        }
        return switch (detail) {
            case FAST -> 60;
            case BALANCED -> 30;
            case DETAILED -> 12;
        };
    }

    private int recommendedMinimumBasin(DrainageExtractionService.AnalysisDetail detail) {
        if (detail == null) {
            return 120;
        }
        return switch (detail) {
            case FAST -> 240;
            case BALANCED -> 120;
            case DETAILED -> 60;
        };
    }

    private int safeParsePositiveInt(String text, int fallback) {
        try {
            int value = Integer.parseInt(text != null ? text.trim() : "");
            return Math.max(1, value);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static final class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        private SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
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
        TerrainHydrologyAnalysisDialog dialog = new TerrainHydrologyAnalysisDialog(owner, rasterLayer);
        dialog.setVisible(true);
    }
}
