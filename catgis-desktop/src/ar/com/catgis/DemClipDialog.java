package ar.com.catgis;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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

public class DemClipDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JComboBox<ClipTarget> clipTargetCombo;
    private final JComboBox<Layer> polygonMaskCombo;
    private final JTextField outputNameField;
    private final JTextField outputFileField;
    private final JLabel hintLabel;

    public DemClipDialog(Frame owner, Layer rasterLayer) {
        super(owner, I18n.t("Recortar DEM..."), true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        rasterCombo = new JComboBox<>(TopographyWorkflowSupport.getAvailableRasterLayers().toArray(new Layer[0]));
        if (rasterLayer != null) {
            rasterCombo.setSelectedItem(rasterLayer);
        } else if (TopographyWorkflowSupport.resolvePreferredRasterLayer() != null) {
            rasterCombo.setSelectedItem(TopographyWorkflowSupport.resolvePreferredRasterLayer());
        }

        clipTargetCombo = new JComboBox<>(ClipTarget.values());
        polygonMaskCombo = new JComboBox<>();
        outputNameField = new JTextField(buildDefaultOutputName(), 28);
        outputFileField = new JTextField(buildDefaultOutputFile().getAbsolutePath(), 28);
        hintLabel = new JLabel();

        rasterCombo.addActionListener(e -> {
            refreshDefaults();
            refreshClipTargetState();
        });
        clipTargetCombo.addActionListener(e -> refreshClipTargetState());

        refreshPolygonMaskModel();
        refreshClipTargetState();

        add(WindowLayoutSupport.createVerticalScrollPane(buildForm(), 740, 460), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 800, 600, 720, 500);
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
        panel.add(new JLabel(I18n.t("Area de recorte:")), gc);
        gc.gridy++;
        panel.add(clipTargetCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Mascara poligonal:")), gc);
        gc.gridy++;
        panel.add(polygonMaskCombo, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Nombre de capa resultante:")), gc);
        gc.gridy++;
        panel.add(outputNameField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Guardar DEM recortado como:")), gc);
        gc.gridy++;
        JPanel outputPanel = new JPanel(new BorderLayout(6, 0));
        outputPanel.add(outputFileField, BorderLayout.CENTER);
        JButton browseButton = new JButton(I18n.t("Buscar..."));
        browseButton.addActionListener(e -> chooseOutputFile());
        outputPanel.add(browseButton, BorderLayout.EAST);
        panel.add(outputPanel, gc);

        gc.gridy++;
        hintLabel.setBorder(BorderFactory.createTitledBorder(I18n.t("Uso tecnico")));
        panel.add(hintLabel, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton clipButton = new JButton(I18n.t("Generar DEM recortado"));
        clipButton.addActionListener(e -> startClip());

        panel.add(cancelButton);
        panel.add(clipButton);
        DialogKeyboardSupport.install(this, clipButton, this::dispose);
        return panel;
    }

    private void refreshDefaults() {
        outputNameField.setText(buildDefaultOutputName());
        outputFileField.setText(buildDefaultOutputFile().getAbsolutePath());
    }

    private void refreshClipTargetState() {
        ClipTarget target = (ClipTarget) clipTargetCombo.getSelectedItem();
        boolean polygonMode = target == ClipTarget.POLYGON_MASK;
        polygonMaskCombo.setEnabled(polygonMode);
        hintLabel.setText("<html><div style='width:340px'>"
                + (polygonMode
                ? I18n.t("Usa una capa poligonal como mascara para sacar mar, bordes o zonas irrelevantes antes de curvas, drenaje y cuencas.")
                : I18n.t("Recorta el DEM segun la vista actual del proyecto. Es una forma rapida de aislar el area de trabajo antes del analisis hidrologico."))
                + "<br><br>"
                + TopographyWorkflowSupport.buildRasterOperationalGuidanceFragment((Layer) rasterCombo.getSelectedItem(), "clip")
                + "</div></html>");
    }

    private void refreshPolygonMaskModel() {
        List<Layer> polygonLayers = new ArrayList<>();
        for (Layer layer : VectorLayerUtils.getVectorLayers()) {
            if ("POLYGON".equalsIgnoreCase(VectorLayerUtils.resolveGeometryFamily(layer))) {
                polygonLayers.add(layer);
            }
        }
        polygonMaskCombo.setModel(new DefaultComboBoxModel<>(polygonLayers.toArray(new Layer[0])));
    }

    private void chooseOutputFile() {
        JFileChooser chooser = FileChooserSupport.createChooser("clip-dem-output", I18n.t("Guardar DEM recortado como"));
        chooser.setSelectedFile(buildDefaultOutputFile());
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null && !file.getName().toLowerCase(Locale.ROOT).endsWith(".tif")) {
                File parent = file.getParentFile();
                file = parent != null ? new File(parent, file.getName() + ".tif") : new File(file.getName() + ".tif");
            }
            outputFileField.setText(file != null ? file.getAbsolutePath() : "");
            FileChooserSupport.rememberFile("clip-dem-output", file);
        }
    }

    private void startClip() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (!(rasterLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes elegir un DEM raster valido para recortar."));
            return;
        }

        ClipTarget target = (ClipTarget) clipTargetCombo.getSelectedItem();
        Envelope envelope;
        Geometry maskGeometry = null;
        try {
            if (target == ClipTarget.POLYGON_MASK) {
                Layer maskLayer = (Layer) polygonMaskCombo.getSelectedItem();
                if (maskLayer == null) {
                    JOptionPane.showMessageDialog(this, I18n.t("Debes elegir una mascara poligonal para recortar el DEM."));
                    return;
                }
                ShapefileData maskData = CatgisDesktopApp.mapPanel != null
                        ? CatgisDesktopApp.mapPanel.getShapefileData(maskLayer)
                        : null;
                if (maskData == null) {
                    maskData = VectorLayerUtils.ensureVectorData(maskLayer);
                    maskData = TopographyWorkflowSupport.projectVectorDataToCurrentProject(maskLayer, maskData);
                }
                if (maskData == null || maskData.getFeatures() == null || maskData.getFeatures().isEmpty()) {
                    JOptionPane.showMessageDialog(this, I18n.t("La mascara poligonal no tiene geometria util para recortar el DEM."));
                    return;
                }
                if (!"POLYGON".equalsIgnoreCase(VectorLayerUtils.resolveGeometryFamily(maskData))) {
                    JOptionPane.showMessageDialog(this, I18n.t("La capa elegida para mascara debe ser poligonal."));
                    return;
                }
                List<Geometry> geometries = new ArrayList<>();
                for (var feature : maskData.getFeatures()) {
                    if (feature != null && feature.getDefaultGeometry() instanceof Geometry geometry && !geometry.isEmpty()) {
                        geometries.add((Geometry) geometry.copy());
                    }
                }
                if (geometries.isEmpty()) {
                    JOptionPane.showMessageDialog(this, I18n.t("La mascara poligonal no tiene geometria valida para el recorte."));
                    return;
                }
                maskGeometry = UnaryUnionOp.union(geometries);
                envelope = maskGeometry != null ? new Envelope(maskGeometry.getEnvelopeInternal()) : null;
            } else {
                envelope = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getCurrentViewEnvelope() : null;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("No se pudo preparar el area de recorte del DEM:") + "\n" + ex.getMessage());
            return;
        }

        if (envelope == null || envelope.isNull()) {
            JOptionPane.showMessageDialog(this, I18n.t("El area de recorte del DEM no es valida."));
            return;
        }
        final Envelope finalEnvelope = new Envelope(envelope);
        final Geometry finalMaskGeometry = maskGeometry != null ? (Geometry) maskGeometry.copy() : null;

        String outputName = outputNameField.getText().trim();
        if (outputName.isBlank()) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes indicar un nombre para el DEM recortado."));
            return;
        }
        final String finalOutputName = outputName;

        String outputText = outputFileField.getText().trim();
        if (outputText.isBlank()) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes indicar un archivo de salida para el DEM recortado."));
            return;
        }
        final File outputFile = new File(outputText);

        new SwingWorker<DemClipService.ClipResult, Void>() {
            @Override
            protected DemClipService.ClipResult doInBackground() throws Exception {
                return DemClipService.clipDem(rasterLayer, finalEnvelope, finalMaskGeometry, finalOutputName, outputFile);
            }

            @Override
            protected void done() {
                try {
                    DemClipService.ClipResult result = get();
                    addResultLayer(result);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            DemClipDialog.this,
                            I18n.t("No se pudo recortar el DEM:") + "\n" + cause.getMessage(),
                            I18n.t("Recortar DEM"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void addResultLayer(DemClipService.ClipResult result) {
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
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.addOrUpdateRasterLayer(result.layer(), result.rasterData());
            CatgisDesktopApp.mapPanel.showOpenedFile(result.layer().getName());
            CatgisDesktopApp.mapPanel.zoomToLayer(result.layer());
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("DEM recortado generado: ") + result.layer().getName());
        }
    }

    private String buildDefaultOutputName() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        return "DEM recortado - " + (layer != null ? layer.getName() : "DEM");
    }

    private File buildDefaultOutputFile() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        File source = layer != null && layer.getPath() != null && !layer.getPath().isBlank()
                ? new File(layer.getPath())
                : null;
        String baseName = source != null ? source.getName() : "dem_clip.tif";
        String normalized = baseName.toLowerCase(Locale.ROOT).endsWith(".tif")
                ? baseName.substring(0, baseName.length() - 4)
                : baseName.toLowerCase(Locale.ROOT).endsWith(".tiff")
                ? baseName.substring(0, baseName.length() - 5)
                : baseName;
        File parent = source != null && source.getParentFile() != null
                ? source.getParentFile()
                : null;
        File suggested = new File(parent != null ? parent : new File("."), normalized + "_recortado.tif");
        return FileChooserSupport.resolveSuggestedFile("clip-dem-output", suggested);
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
        DemClipDialog dialog = new DemClipDialog(owner, rasterLayer);
        dialog.setVisible(true);
    }

    private enum ClipTarget {
        CURRENT_VIEW("Area visible actual"),
        POLYGON_MASK("Mascara poligonal");

        private final String label;

        ClipTarget(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
