package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.raster.RasterCoverageSupport;
import ar.com.catgis.core.model.Layer;

import org.geotools.coverage.grid.GridCoverage2D;
import org.locationtech.jts.geom.Envelope;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BooleanRiskDialog extends JDialog {

    private final JComboBox<Layer> demCombo;
    private final JComboBox<Layer> soilCombo;
    private final JTextField baseNameField;
    private final JComboBox<DrainageExtractionService.AnalysisDetail> detailCombo;
    private final JComboBox<DrainageExtractionService.HydrologicConditioning> conditioningCombo;
    private final JComboBox<BooleanRiskService.ComparisonMode> slopeModeCombo;
    private final JTextField slopeValueAField;
    private final JTextField slopeValueBField;
    private final JComboBox<BooleanRiskService.ComparisonMode> soilModeCombo;
    private final JTextField soilValueAField;
    private final JTextField soilValueBField;
    private final JComboBox<BooleanRiskService.LogicMode> logicModeCombo;
    private final JCheckBox slopeMaskCheck;
    private final JCheckBox soilMaskCheck;
    private final JCheckBox vectorizeCheck;
    private final JLabel noteLabel;
    private String overlapCacheKey = "";
    private Boolean overlapCacheValue;

    public BooleanRiskDialog(Frame owner) {
        super(owner, I18n.t("Riesgo booleano..."), true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        demCombo = new JComboBox<>(buildDemModel());
        soilCombo = new JComboBox<>(buildSoilModel());
        baseNameField = new JTextField("", 28);
        detailCombo = new JComboBox<>(DrainageExtractionService.AnalysisDetail.values());
        detailCombo.setSelectedItem(DrainageExtractionService.AnalysisDetail.BALANCED);
        conditioningCombo = new JComboBox<>(DrainageExtractionService.HydrologicConditioning.values());
        conditioningCombo.setSelectedItem(DrainageExtractionService.HydrologicConditioning.ROBUST);
        slopeModeCombo = new JComboBox<>(BooleanRiskService.ComparisonMode.values());
        slopeModeCombo.setSelectedItem(BooleanRiskService.ComparisonMode.GREATER_THAN);
        slopeValueAField = new JTextField("12", 10);
        slopeValueBField = new JTextField("25", 10);
        soilModeCombo = new JComboBox<>(BooleanRiskService.ComparisonMode.values());
        soilModeCombo.setSelectedItem(BooleanRiskService.ComparisonMode.GREATER_THAN);
        soilValueAField = new JTextField("250", 10);
        soilValueBField = new JTextField("400", 10);
        logicModeCombo = new JComboBox<>(BooleanRiskService.LogicMode.values());
        logicModeCombo.setSelectedItem(BooleanRiskService.LogicMode.AND);
        slopeMaskCheck = new JCheckBox(I18n.t("Generar mascara de pendiente"), true);
        soilMaskCheck = new JCheckBox(I18n.t("Generar mascara de suelo"), true);
        vectorizeCheck = new JCheckBox(I18n.t("Vectorizar zonas positivas"), true);
        noteLabel = new JLabel();
        noteLabel.setForeground(new Color(70, 82, 96));
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.PLAIN, 12f));

        demCombo.addActionListener(e -> {
            resetOverlapCache();
            refreshSoilModel();
            updateRuleFields();
            updateNote();
        });
        soilCombo.addActionListener(e -> {
            resetOverlapCache();
            updateNote();
        });
        detailCombo.addActionListener(e -> updateNote());
        conditioningCombo.addActionListener(e -> updateNote());
        slopeModeCombo.addActionListener(e -> {
            updateRuleFields();
            updateNote();
        });
        soilModeCombo.addActionListener(e -> {
            updateRuleFields();
            updateNote();
        });
        logicModeCombo.addActionListener(e -> updateNote());
        slopeValueAField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateNote));
        slopeValueBField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateNote));
        soilValueAField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateNote));
        soilValueBField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateNote));
        slopeMaskCheck.addActionListener(e -> updateNote());
        soilMaskCheck.addActionListener(e -> updateNote());
        vectorizeCheck.addActionListener(e -> updateNote());

        ensureValidSelections();
        updateRuleFields();
        updateNote();

        add(WindowLayoutSupport.createVerticalScrollPane(buildForm(), 780, 520), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 860, 680, 760, 560);
        setLocationRelativeTo(owner);
    }

    public static void open() {
        java.awt.Window owner = CatgisDesktopApp.getMainFrameSafe();
        new BooleanRiskDialog(owner instanceof Frame ? (Frame) owner : null).setVisible(true);
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

        panel.add(new JLabel(I18n.t("DEM base:")), gc);
        gc.gridy++;
        panel.add(demCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Raster de suelos:")), gc);
        gc.gridy++;
        panel.add(soilCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Base / nombre de salida:")), gc);
        gc.gridy++;
        panel.add(baseNameField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Detalle de analisis DEM:")), gc);
        gc.gridy++;
        panel.add(detailCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Tratamiento de lectura DEM:")), gc);
        gc.gridy++;
        panel.add(conditioningCombo, gc);

        gc.gridy++;
        panel.add(buildRulePanel(
                I18n.t("Regla de pendiente (grados)"),
                slopeModeCombo,
                slopeValueAField,
                slopeValueBField
        ), gc);

        gc.gridy++;
        panel.add(buildRulePanel(
                I18n.t("Regla de suelo (valor raster)"),
                soilModeCombo,
                soilValueAField,
                soilValueBField
        ), gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Logica booleana:")), gc);
        gc.gridy++;
        panel.add(logicModeCombo, gc);

        gc.gridy++;
        panel.add(slopeMaskCheck, gc);
        gc.gridy++;
        panel.add(soilMaskCheck, gc);
        gc.gridy++;
        panel.add(vectorizeCheck, gc);

        gc.gridy++;
        noteLabel.setBorder(BorderFactory.createTitledBorder(I18n.t("Lectura tecnica")));
        panel.add(noteLabel, gc);
        return panel;
    }

    private JPanel buildRulePanel(String title,
                                  JComboBox<BooleanRiskService.ComparisonMode> modeCombo,
                                  JTextField valueAField,
                                  JTextField valueBField) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;
        panel.add(modeCombo, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        panel.add(valueAField, gc);
        gc.gridx = 2;
        gc.weightx = 1;
        panel.add(valueBField, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton generateButton = new JButton(I18n.t("Generar riesgo"));
        generateButton.addActionListener(e -> startGeneration());

        panel.add(cancelButton);
        panel.add(generateButton);
        DialogKeyboardSupport.install(this, generateButton, this::dispose);
        return panel;
    }

    private void startGeneration() {
        Layer demLayer = (Layer) demCombo.getSelectedItem();
        Layer soilLayer = (Layer) soilCombo.getSelectedItem();
        if (!(demLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes elegir un DEM raster valido."));
            return;
        }
        if (!isValidDemBaseLayer(demLayer)) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.t("Debes elegir un DEM base real, no una capa raster derivada como inundacion, pendiente, aspecto o mascaras.")
            );
            return;
        }
        if (!(soilLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes elegir un raster de suelos valido."));
            return;
        }
        if (demLayer == soilLayer) {
            JOptionPane.showMessageDialog(this, I18n.t("El DEM y el raster de suelos deben ser capas distintas."));
            return;
        }
        if (!layersIntersectOperationally(demLayer, soilLayer)) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.t("El DEM base y el raster de suelos seleccionados no se superponen espacialmente. Elige un raster de suelos descargado para esa misma zona del DEM.")
            );
            return;
        }

        final BooleanRiskService.RiskRule slopeRule;
        final BooleanRiskService.RiskRule soilRule;
        try {
            slopeRule = buildRule(
                    (BooleanRiskService.ComparisonMode) slopeModeCombo.getSelectedItem(),
                    slopeValueAField.getText(),
                    slopeValueBField.getText()
            );
            soilRule = buildRule(
                    (BooleanRiskService.ComparisonMode) soilModeCombo.getSelectedItem(),
                    soilValueAField.getText(),
                    soilValueBField.getText()
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), I18n.t("Riesgo booleano"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        BooleanRiskService.RiskRequest request = new BooleanRiskService.RiskRequest(
                demLayer,
                soilLayer,
                baseNameField.getText().trim(),
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem(),
                (DrainageExtractionService.HydrologicConditioning) conditioningCombo.getSelectedItem(),
                slopeRule,
                soilRule,
                (BooleanRiskService.LogicMode) logicModeCombo.getSelectedItem(),
                slopeMaskCheck.isSelected(),
                soilMaskCheck.isSelected(),
                vectorizeCheck.isSelected()
        );

        new SwingWorker<BooleanRiskService.RiskResult, Void>() {
            @Override
            protected BooleanRiskService.RiskResult doInBackground() throws Exception {
                return BooleanRiskService.generateRisk(request);
            }

            @Override
            protected void done() {
                try {
                    BooleanRiskService.RiskResult result = get();
                    addResults(result, request);
                    showResultSummary(result, request);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String message = cause.getMessage() != null ? cause.getMessage() : I18n.t("Error no especificado.");
                    if (message.contains("El raster de suelos no intersecta")) {
                        message = I18n.t("El DEM base y el raster de suelos seleccionados no se superponen espacialmente. Usa un raster de suelos descargado para esa misma zona del DEM.");
                    }
                    JOptionPane.showMessageDialog(
                            BooleanRiskDialog.this,
                            I18n.t("No se pudo generar el riesgo booleano preliminar:") + "\n" + message,
                            I18n.t("Riesgo booleano"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addResults(BooleanRiskService.RiskResult result, BooleanRiskService.RiskRequest request) {
        if (result == null) {
            return;
        }
        if (CatgisDesktopApp.currentProject == null) {
            CatgisDesktopApp.currentProject = new Project(I18n.t("Proyecto actual"));
        }

        List<Layer> frontOrder = new ArrayList<>();
        Layer lastAdded = null;

        if (result.vectorLayer() != null) {
            CatgisDesktopApp.currentProject.addLayer(result.vectorLayer().layer());
            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.addLayer(result.vectorLayer().layer());
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(result.vectorLayer().layer(), result.vectorLayer().data());
            }
            frontOrder.add(result.vectorLayer().layer());
            lastAdded = result.vectorLayer().layer();
        }

        for (BooleanRiskService.GeneratedRasterLayer raster : result.rasterLayers()) {
            CatgisDesktopApp.currentProject.addLayer(raster.layer());
            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.addLayer(raster.layer());
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(raster.layer(), raster.data());
            }
            frontOrder.add(raster.layer());
            lastAdded = raster.layer();
        }

        TopographyWorkflowSupport.placeLayersAtFront(frontOrder);
        CatgisDesktopApp.markProjectDirty();
        if (lastAdded != null) {
            if (CatgisDesktopApp.layersPanel != null) {
                CatgisDesktopApp.layersPanel.selectLayer(lastAdded);
            }
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.showOpenedFile(lastAdded.getName());
            }
        }
        if (CatgisDesktopApp.statusBar != null) {
            String areaText = Double.isFinite(result.positiveAreaHectares())
                    ? String.format(Locale.US, "%.2f ha", result.positiveAreaHectares())
                    : "? ha";
            String message = result.positiveCellCount() > 0
                    ? I18n.t("Riesgo booleano preliminar generado: ")
                    + result.positiveCellCount()
                    + " celdas positivas | "
                    + areaText
                    + " | "
                    + request.logicMode()
                    : I18n.t("Riesgo booleano preliminar sin positivos: revisa umbrales, area y capas seleccionadas.");
            AppContext.setStatusMessage(message);
        }
    }

    private BooleanRiskService.RiskRule buildRule(BooleanRiskService.ComparisonMode mode,
                                                  String rawA,
                                                  String rawB) {
        double valueA = parseRequiredDouble(rawA, I18n.t("El valor principal de la regla no es valido."));
        double valueB = valueA;
        if (mode == BooleanRiskService.ComparisonMode.BETWEEN) {
            valueB = parseRequiredDouble(rawB, I18n.t("La regla entre requiere un segundo valor valido."));
        }
        return BooleanRiskService.RiskRule.from(mode, valueA, valueB);
    }

    private double parseRequiredDouble(String rawValue, String message) {
        try {
            return Double.parseDouble((rawValue != null ? rawValue.trim() : "").replace(",", "."));
        } catch (Exception ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private void updateRuleFields() {
        slopeValueBField.setEnabled(BooleanRiskService.ComparisonMode.BETWEEN.equals(slopeModeCombo.getSelectedItem()));
        soilValueBField.setEnabled(BooleanRiskService.ComparisonMode.BETWEEN.equals(soilModeCombo.getSelectedItem()));
    }

    private void updateNote() {
        Layer demLayer = (Layer) demCombo.getSelectedItem();
        Layer soilLayer = (Layer) soilCombo.getSelectedItem();
        String projectCrs = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "EPSG:4326";
        String slopeRule = describeRule((BooleanRiskService.ComparisonMode) slopeModeCombo.getSelectedItem(), slopeValueAField.getText(), slopeValueBField.getText(), "deg");
        String soilRule = describeRule((BooleanRiskService.ComparisonMode) soilModeCombo.getSelectedItem(), soilValueAField.getText(), soilValueBField.getText(), "raw");
        String outputs = buildPlannedOutputs();
        String soilHint = buildSoilValueHint(soilLayer);
        String overlapHint = buildOverlapHint(demLayer, soilLayer);
        String html = "<html><div style='width:430px'>"
                + "<b>" + I18n.t("DEM base:") + "</b> " + (demLayer != null ? demLayer.getName() : I18n.t("No definido")) + "<br>"
                + "<b>" + I18n.t("Suelo:") + "</b> " + (soilLayer != null ? soilLayer.getName() : I18n.t("No definido")) + "<br>"
                + "<b>" + I18n.t("CRS operativo del proyecto:") + "</b> " + projectCrs + "<br><br>"
                + I18n.t("Usa como base un DEM raster real o recortado. No conviene usar capas derivadas como inundacion, pendientes, aspecto o mascaras.") + "<br><br>"
                + I18n.t("CATGIS recalcula la pendiente numerica desde el DEM y muestrea el raster de suelos sobre ese mismo grid operativo.") + "<br><br>"
                + "<b>" + I18n.t("Pendiente:") + "</b> " + slopeRule + "<br>"
                + "<b>" + I18n.t("Suelo:") + "</b> " + soilRule + "<br>"
                + "<b>" + I18n.t("Logica:") + "</b> " + logicModeCombo.getSelectedItem() + "<br>"
                + "<b>" + I18n.t("Salidas previstas:") + "</b> " + outputs + "<br><br>"
                + overlapHint + "<br><br>"
                + soilHint + "<br><br>"
                + TopographyWorkflowSupport.buildRasterOperationalGuidanceFragment(demLayer, "risk")
                + "<br><br>"
                + I18n.t("El raster principal queda persistente dentro del proyecto; la vectorizacion positiva se guarda como SHP reutilizable cuando esta activada.")
                + "<br><br>"
                + I18n.t("Salida territorial preliminar. No reemplaza estudio geotecnico ni cartografia de detalle parcelario.")
                + "</div></html>";
        noteLabel.setText(html);
    }

    private String describeRule(BooleanRiskService.ComparisonMode mode, String rawA, String rawB, String units) {
        String a = rawA != null && !rawA.isBlank() ? rawA.trim() : "?";
        String b = rawB != null && !rawB.isBlank() ? rawB.trim() : "?";
        String suffix = units != null && !units.isBlank() ? " " + units : "";
        if (mode == BooleanRiskService.ComparisonMode.BETWEEN) {
            return "entre " + a + " y " + b + suffix;
        }
        return mode + " " + a + suffix;
    }

    private DefaultComboBoxModel<Layer> buildDemModel() {
        List<Layer> primary = new ArrayList<>();
        List<Layer> secondary = new ArrayList<>();
        for (Layer layer : TopographyWorkflowSupport.getAvailableRasterLayers()) {
            if (!isNonDerivedRaster(layer)) {
                continue;
            }
            if (TopographyWorkflowSupport.isDemLikeRaster(layer)) {
                primary.add(layer);
            } else {
                secondary.add(layer);
            }
        }
        List<Layer> candidates = new ArrayList<>(primary);
        if (candidates.isEmpty()) {
            candidates.addAll(secondary);
        }
        return new DefaultComboBoxModel<>(candidates.toArray(new Layer[0]));
    }

    private DefaultComboBoxModel<Layer> buildSoilModel() {
        List<Layer> candidates = new ArrayList<>();
        Layer selectedDem = (Layer) demCombo.getSelectedItem();
        for (Layer layer : TopographyWorkflowSupport.getAvailableRasterLayers()) {
            if (!isNonDerivedRaster(layer) || layer == selectedDem) {
                continue;
            }
            if (looksLikeSoilRaster(layer)) {
                candidates.add(layer);
            }
        }
        if (candidates.isEmpty()) {
            for (Layer layer : TopographyWorkflowSupport.getAvailableRasterLayers()) {
                if (isNonDerivedRaster(layer) && layer != selectedDem && !TopographyWorkflowSupport.isDemLikeRaster(layer)) {
                    candidates.add(layer);
                }
            }
        }
        if (candidates.isEmpty()) {
            for (Layer layer : TopographyWorkflowSupport.getAvailableRasterLayers()) {
                if (isNonDerivedRaster(layer) && layer != selectedDem) {
                    candidates.add(layer);
                }
            }
        }
        List<Layer> intersecting = new ArrayList<>();
        List<Layer> nonIntersecting = new ArrayList<>();
        for (Layer layer : candidates) {
            if (layersIntersectOperationally(selectedDem, layer)) {
                intersecting.add(layer);
            } else {
                nonIntersecting.add(layer);
            }
        }
        Comparator<Layer> byName = Comparator.comparing(layer -> layer.getName() != null ? layer.getName().toLowerCase(Locale.ROOT) : "");
        intersecting.sort(byName);
        nonIntersecting.sort(byName);
        intersecting.addAll(nonIntersecting);
        return new DefaultComboBoxModel<>(intersecting.toArray(new Layer[0]));
    }

    private void refreshSoilModel() {
        Layer currentSelection = (Layer) soilCombo.getSelectedItem();
        resetOverlapCache();
        soilCombo.setModel(buildSoilModel());
        if (currentSelection != null && layersIntersectOperationally((Layer) demCombo.getSelectedItem(), currentSelection)) {
            soilCombo.setSelectedItem(currentSelection);
        }
        if (soilCombo.getSelectedItem() == null && soilCombo.getItemCount() > 0) {
            soilCombo.setSelectedIndex(0);
        }
    }

    private boolean looksLikeSoilRaster(Layer layer) {
        if (layer == null) {
            return false;
        }
        String text = ((layer.getName() != null ? layer.getName() : "") + " "
                + (layer.getSourceName() != null ? layer.getSourceName() : "") + " "
                + (layer.getPath() != null ? layer.getPath() : "")).toLowerCase(Locale.ROOT);
        return text.contains("soil")
                || text.contains("suelo")
                || text.contains("soilgrids")
                || text.contains("arcilla")
                || text.contains("arena")
                || text.contains("limo")
                || text.contains("carbon");
    }

    private boolean isValidDemBaseLayer(Layer layer) {
        return isNonDerivedRaster(layer)
                && !looksLikeSoilRaster(layer)
                && !looksLikeDerivedTopographyRaster(layer);
    }

    private boolean isNonDerivedRaster(Layer layer) {
        return layer instanceof RasterLayer rasterLayer && !rasterLayer.isDerivedLayer();
    }

    private boolean looksLikeDerivedTopographyRaster(Layer layer) {
        if (layer == null) {
            return false;
        }
        String text = ((layer.getName() != null ? layer.getName() : "") + " "
                + (layer.getSourceName() != null ? layer.getSourceName() : "") + " "
                + (layer.getPath() != null ? layer.getPath() : "")).toLowerCase(Locale.ROOT);
        return text.contains("inundacion preliminar")
                || text.contains("flood")
                || text.contains("hillshade")
                || text.contains("pendiente")
                || text.contains("aspecto")
                || text.contains("direccion de flujo")
                || text.contains("acumulacion de flujo")
                || text.contains("mascara pendiente")
                || text.contains("mascara suelo")
                || text.contains("riesgo booleano")
                || text.contains("preliminary_boolean_risk")
                || text.contains("preliminary_flood");
    }

    private void ensureValidSelections() {
        if ((demCombo.getSelectedItem() == null || !isValidDemBaseLayer((Layer) demCombo.getSelectedItem()))
                && demCombo.getItemCount() > 0) {
            demCombo.setSelectedIndex(0);
        }
        if ((soilCombo.getSelectedItem() == null || soilCombo.getSelectedItem() == demCombo.getSelectedItem())
                && soilCombo.getItemCount() > 0) {
            soilCombo.setSelectedIndex(0);
        }
    }

    private String buildDefaultBaseName() {
        return "";
    }

    private String buildEffectiveBaseName() {
        return baseNameField.getText() != null && !baseNameField.getText().isBlank()
                ? baseNameField.getText().trim()
                : "riesgo preliminar";
    }

    private void showResultSummary(BooleanRiskService.RiskResult result, BooleanRiskService.RiskRequest request) {
        JOptionPane.showMessageDialog(
                this,
                buildResultSummary(result, request),
                I18n.t("Riesgo booleano"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String buildResultSummary(BooleanRiskService.RiskResult result, BooleanRiskService.RiskRequest request) {
        StringBuilder sb = new StringBuilder("<html><div style='width:420px'>");
        sb.append("<b>").append(I18n.t("Riesgo booleano preliminar generado")).append("</b><br><br>")
                .append("<b>").append(I18n.t("DEM:")).append("</b> ").append(request.demRasterLayer().getName()).append("<br>")
                .append("<b>").append(I18n.t("Suelo:")).append("</b> ").append(request.soilRasterLayer().getName()).append("<br>")
                .append("<b>").append(I18n.t("CRS operativo:")).append("</b> ").append(result.demGrid().sourceCrsCode()).append("<br>")
                .append("<b>").append(I18n.t("Pendiente:")).append("</b> ").append(request.slopeRule().describe("deg")).append("<br>")
                .append("<b>").append(I18n.t("Suelo:")).append("</b> ").append(request.soilRule().describe("raw")).append("<br>")
                .append("<b>").append(I18n.t("Logica:")).append("</b> ").append(request.logicMode()).append("<br><br>")
                .append("<b>").append(I18n.t("Celdas positivas pendientes:")).append("</b> ").append(result.positiveSlopeCellCount()).append("<br>")
                .append("<b>").append(I18n.t("Celdas positivas suelos:")).append("</b> ").append(result.positiveSoilCellCount()).append("<br>")
                .append("<b>").append(I18n.t("Celdas positivas finales:")).append("</b> ").append(result.positiveCellCount()).append("<br>")
                .append("<b>").append(I18n.t("Superficie positiva aprox.:")).append("</b> ").append(formatArea(result.positiveAreaHectares())).append("<br>")
                .append("<b>").append(I18n.t("Muestras de suelo cruzadas:")).append("</b> ").append(result.intersectingSoilSamples()).append("<br>");

        if (request.vectorizePositiveZones()) {
            sb.append("<b>").append(I18n.t("Zonas vectoriales:")).append("</b> ").append(result.vectorFeatureCount()).append("<br>");
            if (result.vectorLayer() != null && result.vectorLayer().layer() != null && result.vectorLayer().layer().getPath() != null && !result.vectorLayer().layer().getPath().isBlank()) {
                sb.append("<b>").append(I18n.t("Salida SHP:")).append("</b> ").append(result.vectorLayer().layer().getPath()).append("<br>");
            }
        } else {
            sb.append("<b>").append(I18n.t("Vectorizacion:")).append("</b> ").append(I18n.t("No solicitada")).append("<br>");
        }

        if (result.positiveCellCount() == 0) {
            sb.append("<br>").append(I18n.t("No se detectaron celdas positivas con las reglas actuales. Conviene revisar umbrales, area de trabajo o resolucion del raster."));
        }
        sb.append("</div></html>");
        return sb.toString();
    }

    private String buildPlannedOutputs() {
        List<String> outputs = new ArrayList<>();
        String baseName = buildEffectiveBaseName();
        outputs.add("Riesgo preliminar - " + baseName);
        if (slopeMaskCheck.isSelected()) {
            outputs.add("Mascara pendiente - " + baseName);
        }
        if (soilMaskCheck.isSelected()) {
            outputs.add("Mascara suelo - " + baseName);
        }
        if (vectorizeCheck.isSelected()) {
            outputs.add("Zonas riesgo preliminar - " + baseName + " (SHP)");
        }
        return String.join(" | ", outputs);
    }

    private String buildSoilValueHint(Layer soilLayer) {
        String name = soilLayer != null ? soilLayer.getName() : "";
        String source = soilLayer != null ? soilLayer.getSourceName() : "";
        String text = (name + " " + source).toLowerCase(Locale.ROOT);
        if (text.contains("soilgrids")) {
            return I18n.t("El raster de suelos parece venir de SoilGrids: interpreta umbrales sobre valores continuos modelados y no como cartografia edafologica parcelaria.");
        }
        return I18n.t("El umbral de suelo se aplica sobre el valor numerico real del raster cargado. Revisa fuente, leyenda o rango antes de fijar el criterio.");
    }

    private String buildOverlapHint(Layer demLayer, Layer soilLayer) {
        if (demLayer == null || soilLayer == null) {
            return I18n.t("Revisa que el DEM y el suelo correspondan a la misma zona antes de correr el cruce.");
        }
        return layersIntersectOperationally(demLayer, soilLayer)
                ? I18n.t("El DEM y el raster de suelos se superponen en la vista operativa del proyecto.")
                : I18n.t("Atencion: el DEM y el raster de suelos NO se superponen espacialmente. El cruce booleano va a fallar hasta que uses capas descargadas para la misma zona.");
    }

    private String formatArea(double hectares) {
        return Double.isFinite(hectares)
                ? String.format(Locale.US, "%.2f ha", hectares)
                : "? ha";
    }

    private void resetOverlapCache() {
        overlapCacheKey = "";
        overlapCacheValue = null;
    }

    private boolean layersIntersectOperationally(Layer demLayer, Layer soilLayer) {
        String key = buildOverlapCacheKey(demLayer, soilLayer);
        if (key.equals(overlapCacheKey) && overlapCacheValue != null) {
            return overlapCacheValue;
        }
        boolean result = computeLayerOverlapOperationally(demLayer, soilLayer);
        overlapCacheKey = key;
        overlapCacheValue = result;
        return result;
    }

    private String buildOverlapCacheKey(Layer first, Layer second) {
        String firstKey = first != null ? (first.getPath() + "|" + first.getName() + "|" + first.getSourceCRS()) : "null";
        String secondKey = second != null ? (second.getPath() + "|" + second.getName() + "|" + second.getSourceCRS()) : "null";
        return firstKey + "||" + secondKey;
    }

    private boolean computeLayerOverlapOperationally(Layer demLayer, Layer soilLayer) {
        if (demLayer == null || soilLayer == null) {
            return true;
        }
        try {
            GridCoverage2D demCoverage = RasterCoverageSupport.readCoverage(demLayer);
            GridCoverage2D soilCoverage = RasterCoverageSupport.readCoverageNative(soilLayer);
            Envelope demEnvelope = extractCoverageEnvelope(demCoverage);
            Envelope soilEnvelope = extractCoverageEnvelope(soilCoverage);
            if (demEnvelope != null && soilEnvelope != null && !demEnvelope.isNull() && !soilEnvelope.isNull()) {
                String demCrs = RasterCoverageSupport.resolveCoverageCrsCode(demCoverage, demLayer);
                String soilCrs = RasterCoverageSupport.resolveCoverageCrsCode(soilCoverage, soilLayer);
                Envelope soilInDemCrs = RasterCoverageSupport.reprojectEnvelope(soilEnvelope, soilCrs, demCrs);
                if (soilInDemCrs != null && !soilInDemCrs.isNull()) {
                    return demEnvelope.intersects(soilInDemCrs);
                }
            }
        } catch (Exception ignored) { CatgisLogger.warn("BooleanRiskDialog: operation failed", ignored); }
        return layersIntersectInProjectView(demLayer, soilLayer);
    }

    private Envelope extractCoverageEnvelope(GridCoverage2D coverage) {
        if (coverage == null || coverage.getEnvelope2D() == null) {
            return null;
        }
        return new Envelope(
                coverage.getEnvelope2D().getMinX(),
                coverage.getEnvelope2D().getMaxX(),
                coverage.getEnvelope2D().getMinY(),
                coverage.getEnvelope2D().getMaxY()
        );
    }

    private boolean layersIntersectInProjectView(Layer first, Layer second) {
        if (first == null || second == null || CatgisDesktopApp.mapPanel == null) {
            return true;
        }
        LocalRasterData firstData = CatgisDesktopApp.mapPanel.getRasterData(first);
        LocalRasterData secondData = CatgisDesktopApp.mapPanel.getRasterData(second);
        if (firstData == null || secondData == null || firstData.getEnvelope() == null || secondData.getEnvelope() == null) {
            return true;
        }
        return firstData.getEnvelope().intersects(secondData.getEnvelope());
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
}
