package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import java.util.List;

public class DrainageExtractionDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JTextField thresholdField;
    private final JTextField minimumBranchLengthField;
    private final JComboBox<DrainageExtractionService.AnalysisDetail> detailCombo;
    private final JComboBox<DrainageExtractionService.HydrologicConditioning> conditioningCombo;
    private final JComboBox<DrainageExtractionService.CleanupLevel> cleanupCombo;
    private final JTextField outputNameField;
    private final JLabel technicalHintLabel;

    public DrainageExtractionDialog(Frame owner, Layer rasterLayer) {
        super(owner, I18n.t("Generar escorrentias..."), true);

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        rasterCombo = new JComboBox<>(TopographyWorkflowSupport.getAvailableRasterLayers().toArray(new Layer[0]));
        if (rasterLayer != null) {
            rasterCombo.setSelectedItem(rasterLayer);
        } else if (TopographyWorkflowSupport.resolvePreferredRasterLayer() != null) {
            rasterCombo.setSelectedItem(TopographyWorkflowSupport.resolvePreferredRasterLayer());
        }

        thresholdField = new JTextField("30", 14);
        minimumBranchLengthField = new JTextField("150", 14);
        detailCombo = new JComboBox<>(DrainageExtractionService.AnalysisDetail.values());
        detailCombo.setSelectedItem(DrainageExtractionService.AnalysisDetail.BALANCED);
        conditioningCombo = new JComboBox<>(DrainageExtractionService.HydrologicConditioning.values());
        conditioningCombo.setSelectedItem(DrainageExtractionService.HydrologicConditioning.ADVANCED);
        cleanupCombo = new JComboBox<>(DrainageExtractionService.CleanupLevel.values());
        cleanupCombo.setSelectedItem(DrainageExtractionService.CleanupLevel.STRONG);
        outputNameField = new JTextField(buildDefaultOutputName(), 26);
        technicalHintLabel = new JLabel();

        rasterCombo.addActionListener(e -> {
            outputNameField.setText(buildDefaultOutputName());
            updateTechnicalHint();
        });
        detailCombo.addActionListener(e -> updateTechnicalHint());
        conditioningCombo.addActionListener(e -> updateTechnicalHint());
        cleanupCombo.addActionListener(e -> updateTechnicalHint());
        updateTechnicalHint();

        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        pack();
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
        panel.add(new JLabel(I18n.t("Umbral de acumulacion (celdas):")), gc);
        gc.gridy++;
        panel.add(thresholdField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Detalle de analisis:")), gc);
        gc.gridy++;
        panel.add(detailCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Tratamiento hidrologico:")), gc);
        gc.gridy++;
        panel.add(conditioningCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Longitud minima de ramal (m):")), gc);
        gc.gridy++;
        panel.add(minimumBranchLengthField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Limpieza de red:")), gc);
        gc.gridy++;
        panel.add(cleanupCombo, gc);

        gc.gridy++;
        technicalHintLabel.setBorder(BorderFactory.createTitledBorder(I18n.t("Lectura tecnica")));
        panel.add(technicalHintLabel, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Nombre de capa resultante:")), gc);
        gc.gridy++;
        panel.add(outputNameField, gc);

        gc.gridy++;
        JLabel help = new JLabel(I18n.t("Breach + priority flood + flats: acondiciona el DEM, talla depresiones suaves, resuelve zonas planas y extrae una red de drenaje mas coherente."));
        help.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panel.add(help, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton generateButton = new JButton(I18n.t("Generar drenaje"));
        generateButton.addActionListener(e -> startGeneration());

        panel.add(cancelButton);
        panel.add(generateButton);
        DialogKeyboardSupport.install(this, generateButton, this::dispose);
        return panel;
    }

    private void startGeneration() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (!(rasterLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Selecciona un DEM raster valido para calcular escorrentias."));
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

        final double minimumBranchLength;
        try {
            minimumBranchLength = Double.parseDouble(minimumBranchLengthField.getText().trim().replace(",", "."));
            if (minimumBranchLength < 0d) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("La longitud minima del ramal debe ser numerica y mayor o igual a cero."));
            return;
        }

        final String outputName = outputNameField.getText().trim();
        if (outputName.isBlank()) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes indicar un nombre para la capa de drenaje."));
            return;
        }

        final DrainageExtractionService.AnalysisDetail detail =
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem();
        final DrainageExtractionService.HydrologicConditioning conditioning =
                (DrainageExtractionService.HydrologicConditioning) conditioningCombo.getSelectedItem();
        final DrainageExtractionService.CleanupLevel cleanupLevel =
                (DrainageExtractionService.CleanupLevel) cleanupCombo.getSelectedItem();

        new SwingWorker<DrainageExtractionService.GeneratedDrainageLayer, Void>() {
            @Override
            protected DrainageExtractionService.GeneratedDrainageLayer doInBackground() throws Exception {
                return DrainageExtractionService.generateDrainage(
                        rasterLayer,
                        threshold,
                        outputName,
                        detail,
                        conditioning,
                        minimumBranchLength,
                        cleanupLevel
                );
            }

            @Override
            protected void done() {
                try {
                    DrainageExtractionService.GeneratedDrainageLayer result = get();
                    addResultLayer(result);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            DrainageExtractionDialog.this,
                            I18n.t("No se pudieron calcular las escorrentias:") + "\n"
                                    + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()),
                            I18n.t("Escorrentias"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addResultLayer(DrainageExtractionService.GeneratedDrainageLayer result) {
        if (result == null) {
            return;
        }
        if (CatgisDesktopApp.currentProject == null) {
            CatgisDesktopApp.currentProject = new Project(I18n.t("Proyecto actual"));
        }
        CatgisDesktopApp.currentProject.addLayer(result.layer());
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.addLayer(result.layer());
            CatgisDesktopApp.layersPanel.selectLayer(result.layer());
        }
        ShapefileData projectedData = TopographyWorkflowSupport.projectVectorDataToCurrentProject(result.layer(), result.data());
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(result.layer(), projectedData);
            CatgisDesktopApp.mapPanel.showOpenedFile(result.layer().getName());
        }
        result.layer().setFeatureCount(projectedData != null ? projectedData.getFeatureCount() : result.layer().getFeatureCount());
        TopographyWorkflowSupport.placeLayersAtFront(List.of(result.layer()));
        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.selectLayer(result.layer());
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Escorrentias generadas: ") + result.layer().getName());
        }
    }

    private String buildDefaultOutputName() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        String rasterName = layer != null ? layer.getName() : "DEM";
        return "Escorrentias - " + rasterName;
    }

    private void updateTechnicalHint() {
        DrainageExtractionService.AnalysisDetail detail =
                (DrainageExtractionService.AnalysisDetail) detailCombo.getSelectedItem();
        DrainageExtractionService.HydrologicConditioning conditioning =
                (DrainageExtractionService.HydrologicConditioning) conditioningCombo.getSelectedItem();
        DrainageExtractionService.CleanupLevel cleanup =
                (DrainageExtractionService.CleanupLevel) cleanupCombo.getSelectedItem();
        technicalHintLabel.setText("<html><div style='width:320px'>"
                + "<b>Motor hidrologico:</b> "
                + (detail != null ? detail.toString() : "Equilibrado")
                + " | "
                + (conditioning != null ? conditioning.toString() : "Avanzado")
                + " | limpieza "
                + (cleanup != null ? cleanup.toString().toLowerCase(java.util.Locale.ROOT) : "fuerte")
                + ".<br><br>"
                + TopographyWorkflowSupport.buildRasterOperationalGuidanceFragment((Layer) rasterCombo.getSelectedItem(), "drainage")
                + "</div></html>");
    }

    public static void open() {
        Layer preferred = TopographyWorkflowSupport.resolvePreferredRasterLayer();
        if (preferred == null) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), I18n.t("No hay capas raster disponibles para calcular escorrentias."));
            return;
        }
        open(preferred);
    }

    public static void open(Layer rasterLayer) {
        if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), I18n.t("No hay capas raster disponibles para calcular escorrentias."));
            return;
        }
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.getMainFrameSafe());
        DrainageExtractionDialog dialog = new DrainageExtractionDialog(owner, rasterLayer);
        dialog.setVisible(true);
    }
}
