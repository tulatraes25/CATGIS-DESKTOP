package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

/**
 * Quick style panel: dockable side panel for rapid layer styling
 * without opening the full LayerPropertiesDialog.
 * Inspired by QGIS's layer styling panel.
 */
public class QuickStylePanel extends JPanel {

    private Layer currentLayer;
    private boolean updatingProgrammatically = false;

    // Widgets
    private JLabel layerNameLabel;
    private JComboBox<String> styleTypeCombo;
    private StyleColorButton fillColorBtn;
    private StyleColorButton lineColorBtn;
    private StyleColorButton pointColorBtn;
    private JSlider opacitySlider;
    private JSpinner lineWidthSpinner;
    private JSpinner pointSizeSpinner;
    private JCheckBox labelsVisibleCheck;
    private JComboBox<String> labelFieldCombo;
    private JCheckBox visibleCheck;
    private JPanel previewPanel;
    private JLabel previewLabel;

    // Listeners
    private final ItemListener comboListener = e -> {
        if (!updatingProgrammatically) applyToLayer();
    };

    public QuickStylePanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(41, 128, 185));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        layerNameLabel = new JLabel("Sin capa seleccionada");
        layerNameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        layerNameLabel.setForeground(Color.WHITE);
        header.add(layerNameLabel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(buildStyleForm(), BorderLayout.CENTER);
        content.add(buildPreviewPanel(), BorderLayout.SOUTH);

        add(new JScrollPane(content), BorderLayout.CENTER);

        // Listen for layer selection changes
        installLayerSelectionWatcher();
    }

    private JPanel buildStyleForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(2, 0, 2, 0);
        int row = 0;

        // Visibility
        visibleCheck = new JCheckBox("Capa visible");
        visibleCheck.setOpaque(false);
        visibleCheck.addActionListener(e -> applyToLayer());
        gbc.gridy = row++; form.add(visibleCheck, gbc);

        // Style type
        addSection(form, gbc, row++, "Tipo de simbolo");
        styleTypeCombo = new JComboBox<>(new String[]{"Simple", "Categorizado", "Graduado"});
        styleTypeCombo.addItemListener(comboListener);
        gbc.gridy = row++; form.add(styleTypeCombo, gbc);

        // Fill color
        addRow(form, gbc, row++, "Relleno", () -> {
            fillColorBtn = new StyleColorButton(new Color(65, 105, 225), "Color de relleno");
            fillColorBtn.addActionListener(e -> applyToLayer());
            return fillColorBtn;
        });

        // Line color
        addRow(form, gbc, row++, "Linea", () -> {
            lineColorBtn = new StyleColorButton(new Color(30, 60, 180), "Color de linea");
            lineColorBtn.addActionListener(e -> applyToLayer());
            return lineColorBtn;
        });

        // Point color
        addRow(form, gbc, row++, "Punto", () -> {
            pointColorBtn = new StyleColorButton(new Color(220, 50, 50), "Color de punto");
            pointColorBtn.addActionListener(e -> applyToLayer());
            return pointColorBtn;
        });

        // Opacity
        addRow(form, gbc, row++, "Opacidad", () -> {
            opacitySlider = new JSlider(0, 100, 100);
            opacitySlider.setOpaque(false);
            opacitySlider.addChangeListener(e -> {
                if (!updatingProgrammatically) applyToLayer();
            });
            return opacitySlider;
        });

        // Line width
        addRow(form, gbc, row++, "Grosor linea", () -> {
            lineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 20.0, 0.5));
            lineWidthSpinner.addChangeListener(e -> {
                if (!updatingProgrammatically) applyToLayer();
            });
            return lineWidthSpinner;
        });

        // Point size
        addRow(form, gbc, row++, "Tamano punto", () -> {
            pointSizeSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 40, 1));
            pointSizeSpinner.addChangeListener(e -> {
                if (!updatingProgrammatically) applyToLayer();
            });
            return pointSizeSpinner;
        });

        // Labels section
        addSection(form, gbc, row++, "Etiquetas");
        labelsVisibleCheck = new JCheckBox("Mostrar etiquetas");
        labelsVisibleCheck.setOpaque(false);
        labelsVisibleCheck.addActionListener(e -> applyToLayer());
        gbc.gridy = row++; form.add(labelsVisibleCheck, gbc);

        addRow(form, gbc, row++, "Campo", () -> {
            labelFieldCombo = new JComboBox<>();
            labelFieldCombo.addItemListener(comboListener);
            return labelFieldCombo;
        });

        // Filler
        gbc.gridy = row++; gbc.weighty = 1;
        form.add(new JLabel(""), gbc);

        return form;
    }

    private JPanel buildPreviewPanel() {
        previewPanel = new JPanel(new BorderLayout(0, 4));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Vista previa"));

        previewLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(241, 245, 249);
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (currentLayer == null || !currentLayer.isVisible()) {
                    g2.setColor(new Color(180, 180, 180));
                    g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
                    String t = "Sin capa seleccionada";
                    g2.drawString(t, (getWidth() - g2.getFontMetrics().stringWidth(t)) / 2, getHeight() / 2);
                    g2.dispose();
                    return;
                }

                // Draw sample symbol
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                String type = currentLayer.getType();
                Color fc = fillColorBtn != null ? fillColorBtn.getColor() : new Color(65, 105, 225);
                Color lc = lineColorBtn != null ? lineColorBtn.getColor() : new Color(30, 60, 180);
                float lw = lineWidthSpinner != null ? ((Number) lineWidthSpinner.getValue()).floatValue() : 1f;
                int ps = pointSizeSpinner != null ? (Integer) pointSizeSpinner.getValue() : 6;

                if (type != null && type.equalsIgnoreCase("POINT")) {
                    // Draw circle
                    g2.setColor(fc);
                    g2.fillOval(cx - ps, cy - ps, ps * 2, ps * 2);
                    g2.setStroke(new java.awt.BasicStroke(lw));
                    g2.setColor(lc);
                    g2.drawOval(cx - ps, cy - ps, ps * 2, ps * 2);
                } else if (type != null && type.equalsIgnoreCase("LINE")) {
                    g2.setStroke(new java.awt.BasicStroke(lw, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g2.setColor(lc);
                    g2.drawLine(cx - 40, cy, cx + 40, cy);
                } else {
                    // Default: polygon
                    g2.setColor(fc);
                    g2.fillRect(cx - 30, cy - 20, 60, 40);
                    g2.setStroke(new java.awt.BasicStroke(lw));
                    g2.setColor(lc);
                    g2.drawRect(cx - 30, cy - 20, 60, 40);
                }
                g2.dispose();
            }
        };
        previewLabel.setPreferredSize(new Dimension(200, 80));
        previewLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        return previewPanel;
    }

    // --- Helper methods ---

    private void addSection(JPanel form, GridBagConstraints gbc, int row, String title) {
        gbc.gridy = row;
        JLabel label = new JLabel("── " + title + " ──");
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(100, 100, 100));
        form.add(label, gbc);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, ComponentProvider provider) {
        JPanel rowPanel = new JPanel(new BorderLayout(6, 0));
        rowPanel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setPreferredSize(new Dimension(90, 20));
        rowPanel.add(lbl, BorderLayout.WEST);
        rowPanel.add(provider.create(), BorderLayout.CENTER);
        gbc.gridy = row;
        form.add(rowPanel, gbc);
    }

    @FunctionalInterface
    private interface ComponentProvider {
        Component create();
    }

    // --- Style color button ---

    private static class StyleColorButton extends JButton {
        private Color color;

        StyleColorButton(Color initial, String tooltip) {
            this.color = initial;
            setToolTipText(tooltip);
            setPreferredSize(new Dimension(60, 22));
            setContentAreaFilled(false);
            setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
            addActionListener(e -> {
                Color c = JColorChooser.showDialog(StyleColorButton.this,
                        "Seleccionar color", color);
                if (c != null) {
                    setColor(c);
                }
            });
        }

        void setColor(Color c) {
            this.color = c;
            repaint();
            fireActionPerformed(new java.awt.event.ActionEvent(this, 0, "color"));
        }

        Color getColor() { return color; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 4, 4);
            g2.setColor(color.darker());
            g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 4, 4);
            g2.dispose();
        }
    }

    // --- Layer management ---

    public void setLayer(Layer layer) {
        this.currentLayer = layer;
        updatingProgrammatically = true;

        if (layer == null) {
            layerNameLabel.setText("Sin capa seleccionada");
            setEnabled(false);
            updatingProgrammatically = false;
            return;
        }

        setEnabled(true);
        layerNameLabel.setText(layer.getName());

        // Load values from layer
        visibleCheck.setSelected(layer.isVisible());

        if (fillColorBtn != null) {
            fillColorBtn.setColor(layer.getFillColor() != null ? layer.getFillColor() : new Color(65, 105, 225));
        }
        if (lineColorBtn != null) {
            lineColorBtn.setColor(layer.getLineColor() != null ? layer.getLineColor() : new Color(30, 60, 180));
        }
        if (pointColorBtn != null) {
            pointColorBtn.setColor(layer.getPointColor() != null ? layer.getPointColor() : new Color(220, 50, 50));
        }
        if (opacitySlider != null) {
            opacitySlider.setValue((int) (layer.getOpacity() * 100));
        }
        if (lineWidthSpinner != null) {
            lineWidthSpinner.setValue((double) layer.getLineWidth());
        }
        if (pointSizeSpinner != null) {
            pointSizeSpinner.setValue(layer.getPointSize());
        }

        // Labels
        labelsVisibleCheck.setSelected(layer.isLabelsVisible());

        // Populate field combo
        if (labelFieldCombo != null) {
            labelFieldCombo.removeAllItems();
            labelFieldCombo.addItem("(ninguno)");
            ShapefileData data = CatgisDesktopApp.mapPanel != null
                    ? CatgisDesktopApp.mapPanel.getShapefileData(layer) : null;
            if (data != null) {
                for (String attr : data.getAttributeNames()) {
                    labelFieldCombo.addItem(attr);
                }
            }
            if (layer.getLabelField() != null) {
                labelFieldCombo.setSelectedItem(layer.getLabelField());
            }
        }

        updatingProgrammatically = false;
        previewLabel.repaint();
    }

    private void applyToLayer() {
        if (currentLayer == null || updatingProgrammatically) return;

        currentLayer.setVisible(visibleCheck.isSelected());

        if (fillColorBtn != null) {
            currentLayer.setFillColor(fillColorBtn.getColor());
            currentLayer.setPointColor(fillColorBtn.getColor());
        }
        if (lineColorBtn != null) {
            currentLayer.setLineColor(lineColorBtn.getColor());
        }
        if (pointColorBtn != null) {
            currentLayer.setPointColor(pointColorBtn.getColor());
        }
        if (opacitySlider != null) {
            currentLayer.setOpacity(opacitySlider.getValue() / 100f);
        }
        if (lineWidthSpinner != null) {
            currentLayer.setLineWidth(((Number) lineWidthSpinner.getValue()).floatValue());
        }
        if (pointSizeSpinner != null) {
            currentLayer.setPointSize((Integer) pointSizeSpinner.getValue());
        }

        // Labels
        currentLayer.setLabelsVisible(labelsVisibleCheck.isSelected());
        if (labelFieldCombo != null && labelFieldCombo.getSelectedItem() != null) {
            String selected = (String) labelFieldCombo.getSelectedItem();
            currentLayer.setLabelField("(ninguno)".equals(selected) ? null : selected);
        }

        // Refresh
        CatgisDesktopApp.mapPanel.repaint();
        CatgisDesktopApp.layersPanel.refreshLayerList();
        previewLabel.repaint();
    }

    private void installLayerSelectionWatcher() {
        // Poll-free: wait for the panel to be shown, then use layerList's listener
        addHierarchyListener(e -> {
            if (isShowing() && CatgisDesktopApp.layersPanel != null) {
                Layer selected = CatgisDesktopApp.layersPanel.getSelectedLayer();
                if (selected != currentLayer) {
                    setLayer(selected);
                }
            }
        });
    }
}
