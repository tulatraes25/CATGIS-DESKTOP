package ar.com.catgis;

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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class ContourGenerationDialog extends JDialog {

    private final JComboBox<Layer> rasterCombo;
    private final JTextField intervalField;
    private final JTextField indexEveryField;
    private final JTextField outputNameField;
    private final JCheckBox simplifyCheck;
    private final JCheckBox smoothCheck;

    public ContourGenerationDialog(Frame owner, Layer rasterLayer) {
        super(owner, I18n.t("Generar curvas de nivel..."), true);

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        rasterCombo = new JComboBox<>(TopographyWorkflowSupport.getAvailableRasterLayers().toArray(new Layer[0]));
        if (rasterLayer != null) {
            rasterCombo.setSelectedItem(rasterLayer);
        } else if (TopographyWorkflowSupport.resolvePreferredRasterLayer() != null) {
            rasterCombo.setSelectedItem(TopographyWorkflowSupport.resolvePreferredRasterLayer());
        }
        intervalField = new JTextField("10", 16);
        indexEveryField = new JTextField("5", 16);
        outputNameField = new JTextField("Curvas " + intervalField.getText().trim() + "m - " + resolveRasterLabel(), 26);
        simplifyCheck = new JCheckBox(I18n.t("Simplificar lineas"), true);
        smoothCheck = new JCheckBox(I18n.t("Suavizar lineas"), false);
        rasterCombo.addActionListener(e -> updateDefaultOutputName());
        intervalField.addActionListener(e -> updateDefaultOutputName());
        intervalField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDefaultOutputName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDefaultOutputName();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDefaultOutputName();
            }
        });

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

        panel.add(new JLabel(I18n.t("Raster origen:")), gc);
        gc.gridy++;
        panel.add(rasterCombo, gc);
        gc.gridy++;
        panel.add(new JLabel(I18n.t("Equidistancia:")), gc);
        gc.gridy++;
        panel.add(intervalField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Curva indice cada:")), gc);
        gc.gridy++;
        panel.add(indexEveryField, gc);

        gc.gridy++;
        panel.add(new JLabel(I18n.t("Nombre de capa resultante:")), gc);
        gc.gridy++;
        panel.add(outputNameField, gc);

        gc.gridy++;
        panel.add(simplifyCheck, gc);
        gc.gridy++;
        panel.add(smoothCheck, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(I18n.t("Cancelar"));
        cancelButton.addActionListener(e -> dispose());

        JButton generateButton = new JButton(I18n.t("Generar"));
        generateButton.addActionListener(e -> startGeneration());

        panel.add(cancelButton);
        panel.add(generateButton);
        DialogKeyboardSupport.install(this, generateButton, this::dispose);
        return panel;
    }

    private void startGeneration() {
        Layer rasterLayer = (Layer) rasterCombo.getSelectedItem();
        if (!(rasterLayer instanceof RasterLayer)) {
            JOptionPane.showMessageDialog(this, I18n.t("Selecciona un raster DEM valido para generar curvas."));
            return;
        }

        final double interval;
        try {
            interval = Double.parseDouble(intervalField.getText().trim().replace(",", "."));
            if (interval <= 0) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("La equidistancia debe ser numerica y mayor a cero."));
            return;
        }

        final int indexEvery;
        try {
            indexEvery = Integer.parseInt(indexEveryField.getText().trim());
            if (indexEvery < 2) {
                throw new NumberFormatException();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("La curva indice debe repetirse cada 2 o mas curvas."));
            return;
        }

        final String outputName = outputNameField.getText().trim();
        if (outputName.isBlank()) {
            JOptionPane.showMessageDialog(this, I18n.t("Debes indicar un nombre para la capa de curvas."));
            return;
        }

        new SwingWorker<ContourGenerationService.GeneratedContourLayer, Void>() {
            @Override
            protected ContourGenerationService.GeneratedContourLayer doInBackground() throws Exception {
                return ContourGenerationService.generateContours(
                        rasterLayer,
                        interval,
                        indexEvery,
                        outputName,
                        simplifyCheck.isSelected(),
                        smoothCheck.isSelected()
                );
            }

            @Override
            protected void done() {
                try {
                    ContourGenerationService.GeneratedContourLayer result = get();
                    addResultLayer(result);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ContourGenerationDialog.this,
                            I18n.t("No se pudieron generar las curvas de nivel:") + "\n" + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()),
                            I18n.t("Curvas de nivel"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private String resolveRasterLabel() {
        Layer layer = (Layer) rasterCombo.getSelectedItem();
        return layer != null ? layer.getName() : "DEM";
    }

    private void updateDefaultOutputName() {
        String intervalText = intervalField.getText() != null ? intervalField.getText().trim() : "";
        if (intervalText.isBlank()) {
            intervalText = "?";
        }
        outputNameField.setText("Curvas " + intervalText + "m - " + resolveRasterLabel());
    }

    private void addResultLayer(ContourGenerationService.GeneratedContourLayer result) {
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
            CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(result.layer(), result.data());
            CatgisDesktopApp.mapPanel.showOpenedFile(result.layer().getName());
        }
        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(I18n.t("Curvas de nivel generadas: ") + result.layer().getName());
        }
    }

    public static void open() {
        Layer preferred = TopographyWorkflowSupport.resolvePreferredRasterLayer();
        if (preferred == null) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), I18n.t("No hay capas raster disponibles para generar curvas."));
            return;
        }
        open(preferred);
    }

    public static void open(Layer rasterLayer) {
        if (TopographyWorkflowSupport.getAvailableRasterLayers().isEmpty()) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), I18n.t("No hay capas raster disponibles para generar curvas."));
            return;
        }
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.getMainFrameSafe());
        ContourGenerationDialog dialog = new ContourGenerationDialog(owner, rasterLayer);
        dialog.setVisible(true);
    }
}
