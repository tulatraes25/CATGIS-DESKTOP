package ar.com.catgis;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

public class RasterDisplaySettingsDialog extends JDialog {

    private final Layer layer;
    private final MapPanel mapPanel;

    public RasterDisplaySettingsDialog(Component parent, Layer layer) {
        super(SwingUtilities.getWindowAncestor(parent) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(parent) : null,
                "Ajustes de visualizacion raster", true);
        this.layer = layer;
        this.mapPanel = AppContext.mapPanel();
        build();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public RasterDisplaySettingsDialog(Layer layer) {
        this((Component) null, layer);
    }

    public static void open(Component parent, Layer layer) {
        new RasterDisplaySettingsDialog(parent, layer);
    }

    private void build() {
        boolean isRaster = layer instanceof RasterLayer;
        LocalRasterData data = isRaster && mapPanel != null ? mapPanel.getRasterData(layer) : null;
        float currentOpacity = layer.getOpacity();

        final int bandCount;
        final TopographyWorkflowSupport.RasterVisualPreset preset;
        final MapPanel.RasterStyle style;

        if (isRaster && data != null) {
            style = mapPanel.getOrCreateRasterStyle(layer, Math.max(1, data.getBandCount()));
            bandCount = Math.max(1, data.getBandCount());
            preset = TopographyWorkflowSupport.resolveRasterVisualPreset(layer, bandCount);
        } else {
            style = null;
            bandCount = 1;
            preset = null;
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JSlider opacitySlider = new JSlider(0, 100, Math.round((style != null ? style.opacity : currentOpacity) * 100f));
        opacitySlider.setMajorTickSpacing(25);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);

        JCheckBox grayCheck = new JCheckBox("Escala de grises", style != null && style.grayscale);
        JCheckBox autoContrastCheck = new JCheckBox("Contraste automatico", style != null && style.autoContrast);

        Integer[] bands = new Integer[bandCount];
        for (int i = 0; i < bandCount; i++) bands[i] = i + 1;
        JComboBox<Integer> redCombo = new JComboBox<>(bands);
        JComboBox<Integer> greenCombo = new JComboBox<>(bands);
        JComboBox<Integer> blueCombo = new JComboBox<>(bands);
        if (style != null) {
            redCombo.setSelectedItem(Math.min(style.redBand + 1, bandCount));
            greenCombo.setSelectedItem(Math.min(style.greenBand + 1, bandCount));
            blueCombo.setSelectedItem(Math.min(style.blueBand + 1, bandCount));
        }
        boolean multiBand = bandCount >= 3 && preset != null && preset.allowBandSelection();
        redCombo.setEnabled(multiBand);
        greenCombo.setEnabled(multiBand);
        blueCombo.setEnabled(multiBand);

        int y = 0;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Opacidad"), gc);
        gc.gridx = 1; gc.gridy = y++; form.add(opacitySlider, gc);

        if (isRaster && data != null) {
            gc.gridx = 0; gc.gridy = y; gc.gridwidth = 2; form.add(grayCheck, gc); y++;
            gc.gridx = 0; gc.gridy = y; gc.gridwidth = 2; form.add(autoContrastCheck, gc); y++;
            gc.gridwidth = 1;

            gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Banda R"), gc);
            gc.gridx = 1; form.add(redCombo, gc); y++;
            gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Banda G"), gc);
            gc.gridx = 1; form.add(greenCombo, gc); y++;
            gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Banda B"), gc);
            gc.gridx = 1; form.add(blueCombo, gc); y++;

            String helpText = "<html><div style='width:300px'><b>" + (preset != null ? preset.title() : layer.getType()) + ":</b> "
                    + (preset != null ? preset.description() : "Capa raster")
                    + "<br><br><b>Opacidad sugerida:</b> "
                    + String.format(Locale.US, "%d%%", preset != null ? preset.recommendedOpacityPercent() : 100)
                    + (multiBand ? "<br><br>Las bandas RGB se pueden ajustar manualmente." : "<br><br>La composicion RGB no necesita ajuste para esta capa.")
                    + "</div></html>";
            JLabel help = new JLabel(helpText);
            gc.gridx = 0; gc.gridy = y; gc.gridwidth = 2; form.add(help, gc);
        }

        JButton apply = new JButton("Aplicar");
        JButton close = new JButton("Cerrar");
        JButton reset = new JButton("Restablecer sugerido");

        apply.addActionListener(e -> {
            float opacity = opacitySlider.getValue() / 100f;
            if (isRaster && data != null) {
                boolean gray = grayCheck.isSelected();
                boolean auto = autoContrastCheck.isSelected();
                int r = ((Integer) redCombo.getSelectedItem()) - 1;
                int g = ((Integer) greenCombo.getSelectedItem()) - 1;
                int b = ((Integer) blueCombo.getSelectedItem()) - 1;
                mapPanel.applyRasterStyle(layer, opacity, gray, auto, r, g, b);
            } else {
                layer.setOpacity(opacity);
                CatgisDesktopApp.markProjectDirty();
                if (mapPanel != null) mapPanel.repaint();
            }
            if (CatgisDesktopApp.statusBar != null) {
                AppContext.setStatusMessage("Visualizacion actualizada: " + layer.getName());
            }
        });

        reset.addActionListener(e -> {
            if (isRaster && data != null && preset != null) {
                opacitySlider.setValue(Math.max(0, Math.min(100, preset.recommendedOpacityPercent())));
                grayCheck.setSelected(preset.grayscale());
                autoContrastCheck.setSelected(preset.autoContrast());
                redCombo.setSelectedItem(1);
                greenCombo.setSelectedItem(Math.min(2, bandCount));
                blueCombo.setSelectedItem(Math.min(3, bandCount));
                mapPanel.applyRasterStyle(
                        layer,
                        Math.max(0f, Math.min(1f, preset.recommendedOpacityPercent() / 100f)),
                        preset.grayscale(),
                        preset.autoContrast(),
                        0,
                        Math.min(1, bandCount - 1),
                        Math.min(2, bandCount - 1)
                );
            } else {
                opacitySlider.setValue(100);
                layer.setOpacity(1.0f);
                CatgisDesktopApp.markProjectDirty();
                if (mapPanel != null) mapPanel.repaint();
            }
        });

        close.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(reset);
        buttons.add(apply);
        buttons.add(close);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setResizable(false);
    }
}
