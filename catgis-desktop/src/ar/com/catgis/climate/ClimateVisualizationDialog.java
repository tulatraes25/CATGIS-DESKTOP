package ar.com.catgis.climate;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.Layer;

import ar.com.catgis.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Dialog for applying climate symbology (colormaps) to raster layers.
 * Displays a gradient preview and allows adjusting opacity before applying.
 */
public class ClimateVisualizationDialog extends JDialog {

    private final RasterLayer layer;
    private final MapPanel mapPanel;
    private ClimateColormaps.ClimateVariable selectedVariable = ClimateColormaps.ClimateVariable.TEMPERATURE;
    private final JPanel gradientPreview;
    private final JSlider opacitySlider;
    private final JComboBox<ClimateColormaps.ClimateVariable> variableCombo;

    public ClimateVisualizationDialog(Window owner, RasterLayer layer) {
        super(owner, "Simbología climática - " + layer.getName(), ModalityType.APPLICATION_MODAL);
        this.layer = layer;
        this.mapPanel = CatgisDesktopApp.mapPanel;

        // Detect variable type from layer metadata
        String varType = layer.getUserData("climateVariableType") != null
                ? layer.getUserData("climateVariableType").toString() : "";
        if (!varType.isEmpty()) {
            try {
                selectedVariable = ClimateColormaps.ClimateVariable.valueOf(varType);
            } catch (Exception ignored) { CatgisLogger.warn("ClimateVisualizationDialog: operation failed", ignored); }
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Variable type selector
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(new JLabel("Tipo de variable:"), gc);
        variableCombo = new JComboBox<>(ClimateColormaps.ClimateVariable.values());
        variableCombo.setSelectedItem(selectedVariable);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        form.add(variableCombo, gc);

        // Gradient preview
        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2; gc.weightx = 1;
        gradientPreview = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGradientPreview(g);
            }
        };
        gradientPreview.setPreferredSize(new Dimension(300, 40));
        gradientPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        form.add(gradientPreview, gc);
        gc.gridwidth = 1;

        // Opacity slider
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(new JLabel("Opacidad:"), gc);
        opacitySlider = new JSlider(0, 100, 80);
        opacitySlider.setMajorTickSpacing(25);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1;
        form.add(opacitySlider, gc);

        // Legend info
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        JLabel legendLabel = new JLabel(" ");
        legendLabel.setName("legendLabel");
        gc.weightx = 1;
        form.add(legendLabel, gc);
        // We'll update the legend when variable changes

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton applyBtn = new JButton("Aplicar simbología");
        JButton closeBtn = new JButton("Cerrar");
        buttons.add(applyBtn);
        buttons.add(closeBtn);
        add(buttons, BorderLayout.SOUTH);

        // Events
        variableCombo.addActionListener(e -> {
            selectedVariable = (ClimateColormaps.ClimateVariable) variableCombo.getSelectedItem();
            updateLegendLabel(legendLabel);
            gradientPreview.repaint();
        });

        applyBtn.addActionListener(e -> applySymbology());

        closeBtn.addActionListener(e -> dispose());

        // Initial legend update
        updateLegendLabel(legendLabel);

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setVisible(true);
    }

    public static void open(Window owner, RasterLayer layer) {
        new ClimateVisualizationDialog(owner, layer);
    }

    private void updateLegendLabel(JLabel label) {
        String rangeStr;
        switch (selectedVariable) {
            case TEMPERATURE:
                rangeStr = "-10°C a 45°C (azul -> rojo)";
                break;
            case PRECIPITATION:
                rangeStr = "0 mm a 500 mm (blanco -> púrpura)";
                break;
            case WIND_SPEED:
                rangeStr = "0 m/s a 30 m/s (blanco -> rojo)";
                break;
            case PRESSURE:
                rangeStr = "960 hPa a 1050 hPa (púrpura -> amarillo)";
                break;
            default:
                rangeStr = "Rango personalizado";
                break;
        }
        label.setText("Rango: " + rangeStr);
    }

    private void drawGradientPreview(Graphics g) {
        int width = gradientPreview.getWidth();
        int height = gradientPreview.getHeight();
        if (width <= 0 || height <= 0) return;

        Color[] cmap = getColormapForVariable();
        if (cmap == null) return;

        for (int x = 0; x < width; x++) {
            int idx = Math.min(x * 255 / Math.max(1, width - 1), 255);
            g.setColor(cmap[idx]);
            g.fillRect(x, 0, 1, height);
        }

        // Draw value labels
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        FontMetrics fm = g.getFontMetrics();
        String minStr, maxStr;
        switch (selectedVariable) {
            case TEMPERATURE: minStr = "-10°C"; maxStr = "45°C"; break;
            case PRECIPITATION: minStr = "0 mm"; maxStr = "500 mm"; break;
            case WIND_SPEED: minStr = "0 m/s"; maxStr = "30 m/s"; break;
            case PRESSURE: minStr = "960 hPa"; maxStr = "1050 hPa"; break;
            default: minStr = "Min"; maxStr = "Max"; break;
        }
        g.drawString(minStr, 2, height - 3);
        g.drawString(maxStr, width - fm.stringWidth(maxStr) - 2, height - 3);
    }

    private Color[] getColormapForVariable() {
        switch (selectedVariable) {
            case TEMPERATURE: return ClimateColormaps.createTemperatureColormap();
            case PRECIPITATION: return ClimateColormaps.createPrecipitationColormap();
            case WIND_SPEED: return ClimateColormaps.createWindSpeedColormap();
            case PRESSURE: return ClimateColormaps.createPressureColormap();
            default: return ClimateColormaps.createTemperatureColormap();
        }
    }

    private void applySymbology() {
        ClimateColormaps.applyToLayer(layer, selectedVariable, mapPanel);
        float opacity = opacitySlider.getValue() / 100f;
        var data = mapPanel.getRasterData(layer);
        int bandCount = data != null ? Math.max(1, data.getBandCount()) : 1;
        MapPanel.RasterStyle style = mapPanel.getOrCreateRasterStyle(layer, bandCount);
        style.opacity = opacity;
        mapPanel.repaint();

        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(
                    "Simbología climática aplicada: " + selectedVariable + " | " + layer.getName());
        }
        dispose();
    }
}
