package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class LayerPropertiesDialog extends JDialog {

    private final Layer layer;

    private final JTextField nameField;
    private final JTextField pathField;
    private final JCheckBox visibleCheck;
    private final JCheckBox labelsVisibleCheck;
    private final JComboBox<String> labelFieldCombo;

    private final JButton fillColorButton;
    private final JButton borderColorButton;
    private final JButton lineColorButton;
    private final JButton pointColorButton;

    private final JSpinner lineWidthSpinner;
    private final JSpinner pointSizeSpinner;
    private final JComboBox<Layer.PointSymbolStyle> pointStyleCombo;
    private final JComboBox<Layer.LineSymbolStyle> lineStyleCombo;
    private final JComboBox<Layer.PolygonFillStyle> polygonStyleCombo;

    private final Color fillColor;
    private final Color borderColor;
    private final Color lineColor;
    private final Color pointColor;

    public LayerPropertiesDialog(Frame owner, Layer layer) {
        super(owner, "Propiedades de capa", true);
        this.layer = layer;

        this.fillColor = layer.getFillColor();
        this.borderColor = layer.getBorderColor();
        this.lineColor = layer.getLineColor();
        this.pointColor = layer.getPointColor();

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        JLabel title = new JLabel("Configuracion general");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(title, gbc);
        row++;

        gbc.gridwidth = 1;
        nameField = new JTextField(layer.getName(), 24);
        addRow(formPanel, gbc, row++, "Nombre", nameField);

        pathField = new JTextField(buildPathDisplay(layer), 24);
        pathField.setEditable(false);
        pathField.setToolTipText(layer.getPath());
        addRow(formPanel, gbc, row++, "Ubicacion en disco", pathField);

        visibleCheck = new JCheckBox("Capa visible", layer.isVisible());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(visibleCheck, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        row++;

        JLabel styleTitle = new JLabel("Simbologia");
        styleTitle.setFont(styleTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(styleTitle, gbc);
        row++;
        gbc.gridwidth = 1;

        fillColorButton = createColorButton(fillColor, "Color de relleno");
        addRow(formPanel, gbc, row++, "Relleno", fillColorButton);

        borderColorButton = createColorButton(borderColor, "Color de borde");
        addRow(formPanel, gbc, row++, "Borde", borderColorButton);

        lineColorButton = createColorButton(lineColor, "Color de linea");
        addRow(formPanel, gbc, row++, "Linea", lineColorButton);

        lineWidthSpinner = new JSpinner(new SpinnerNumberModel((double) layer.getLineWidth(), 0.5, 20.0, 0.5));
        addRow(formPanel, gbc, row++, "Grosor linea", lineWidthSpinner);

        lineStyleCombo = new JComboBox<>(Layer.LineSymbolStyle.values());
        lineStyleCombo.setSelectedItem(layer.getLineSymbolStyle());
        addRow(formPanel, gbc, row++, "Estilo de linea", lineStyleCombo);

        fillColorButton.setToolTipText("Color principal de relleno");

        polygonStyleCombo = new JComboBox<>(Layer.PolygonFillStyle.values());
        polygonStyleCombo.setSelectedItem(layer.getPolygonFillStyle());
        addRow(formPanel, gbc, row++, "Estilo de poligono", polygonStyleCombo);

        pointColorButton = createColorButton(pointColor, "Color de punto");
        addRow(formPanel, gbc, row++, "Punto", pointColorButton);

        pointSizeSpinner = new JSpinner(new SpinnerNumberModel(layer.getPointSize(), 1, 40, 1));
        addRow(formPanel, gbc, row++, "Tamano punto", pointSizeSpinner);

        pointStyleCombo = new JComboBox<>(Layer.PointSymbolStyle.values());
        pointStyleCombo.setSelectedItem(layer.getPointSymbolStyle());
        pointStyleCombo.setRenderer(new PointStyleRenderer());
        addRow(formPanel, gbc, row++, "Estilo de punto", pointStyleCombo);

        JButton categorizedButton = new JButton("Simbologia por campo...");
        categorizedButton.addActionListener(e -> CategorizedSymbologyDialog.open(layer));
        addRow(formPanel, gbc, row++, "Tematica", categorizedButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        row++;

        JLabel labelsTitle = new JLabel("Etiquetas");
        labelsTitle.setFont(labelsTitle.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(labelsTitle, gbc);
        row++;
        gbc.gridwidth = 1;

        labelsVisibleCheck = new JCheckBox("Mostrar etiquetas", layer.isLabelsVisible());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(labelsVisibleCheck, gbc);
        row++;

        List<String> fields = null;
        ShapefileData data = CatgisDesktopApp.mapPanel != null ? CatgisDesktopApp.mapPanel.getShapefileData(layer) : null;
        if (data != null) {
            fields = data.getAttributeNames();
        }
        labelFieldCombo = new JComboBox<>();
        if (fields != null && !fields.isEmpty()) {
            for (String field : fields) {
                labelFieldCombo.addItem(field);
            }
            if (layer.getLabelField() != null) {
                labelFieldCombo.setSelectedItem(layer.getLabelField());
            }
        } else {
            labelFieldCombo.addItem("(sin campos disponibles)");
            labelFieldCombo.setEnabled(false);
        }
        addRow(formPanel, gbc, row++, "Campo etiqueta", labelFieldCombo);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton applyButton = new JButton("Aplicar");
        JButton okButton = new JButton("Aceptar");
        JButton cancelButton = new JButton("Cancelar");

        applyButton.addActionListener(e -> applyChanges());
        okButton.addActionListener(e -> {
            if (applyChanges()) {
                dispose();
            }
        });
        cancelButton.addActionListener(e -> dispose());

        buttons.add(applyButton);
        buttons.add(okButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setSize(Math.max(getWidth(), 560), Math.max(getHeight(), 620));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
    }

    private JButton createColorButton(Color initial, String title) {
        JButton button = new JButton("Elegir...");
        button.setBackground(initial);
        button.setOpaque(true);
        button.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(this, title, button.getBackground());
            if (selected != null) {
                button.setBackground(selected);
            }
        });
        return button;
    }

    private class PointStyleRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Layer.PointSymbolStyle style = value instanceof Layer.PointSymbolStyle
                    ? (Layer.PointSymbolStyle) value
                    : Layer.PointSymbolStyle.CIRCLE;
            label.setIcon(new ImageIcon(buildPointStylePreview(style, pointColor)));
            label.setIconTextGap(8);
            return label;
        }
    }

    private BufferedImage buildPointStylePreview(Layer.PointSymbolStyle style, Color color) {
        BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setColor(color != null ? color : Color.BLUE);
            int x = 11;
            int y = 11;
            int size = 12;
            int half = size / 2;
            switch (style) {
                case SQUARE -> {
                    g2.fillRect(x - half, y - half, size, size);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(x - half, y - half, size, size);
                }
                case DIAMOND -> {
                    Path2D diamond = new Path2D.Double();
                    diamond.moveTo(x, y - half);
                    diamond.lineTo(x + half, y);
                    diamond.lineTo(x, y + half);
                    diamond.lineTo(x - half, y);
                    diamond.closePath();
                    g2.fill(diamond);
                    g2.setColor(Color.BLACK);
                    g2.draw(diamond);
                }
                case TRIANGLE -> {
                    Path2D triangle = new Path2D.Double();
                    triangle.moveTo(x, y - half);
                    triangle.lineTo(x + half, y + half);
                    triangle.lineTo(x - half, y + half);
                    triangle.closePath();
                    g2.fill(triangle);
                    g2.setColor(Color.BLACK);
                    g2.draw(triangle);
                }
                case TARGET -> {
                    g2.fillOval(x - half, y - half, size, size);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - 3, y - 3, 6, 6);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(x - half, y - half, size, size);
                    g2.drawLine(x - half - 2, y, x + half + 2, y);
                    g2.drawLine(x, y - half - 2, x, y + half + 2);
                }
                case PIN -> {
                    Path2D pin = new Path2D.Double();
                    pin.moveTo(x, y + half + 2);
                    pin.lineTo(x + half, y - 1);
                    pin.quadTo(x + half + 1, y - half - 1, x, y - half);
                    pin.quadTo(x - half - 1, y - half - 1, x - half, y - 1);
                    pin.closePath();
                    g2.fill(pin);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - 2, y - half + 3, 5, 5);
                    g2.setColor(Color.BLACK);
                    g2.draw(pin);
                }
                case FLAG -> {
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawLine(x - 3, y + half, x - 3, y - half);
                    Path2D flag = new Path2D.Double();
                    flag.moveTo(x - 3, y - half + 1);
                    flag.lineTo(x + half, y - half / 2d);
                    flag.lineTo(x - 3, y);
                    flag.closePath();
                    g2.fill(flag);
                }
                case STAR -> {
                    Path2D star = buildStar(x, y, half, Math.max(2, half / 2));
                    g2.fill(star);
                    g2.setColor(Color.BLACK);
                    g2.draw(star);
                }
                case WELL -> {
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D derrick = new Path2D.Double();
                    derrick.moveTo(x, y - half);
                    derrick.lineTo(x + half - 1, y + half);
                    derrick.lineTo(x - half + 1, y + half);
                    derrick.closePath();
                    g2.draw(derrick);
                    g2.drawLine(x - half + 2, y + half, x + half - 2, y + half);
                    g2.drawLine(x - half / 2, y, x + half / 2, y);
                    g2.drawLine(x - half / 2, y, x, y + half);
                    g2.drawLine(x + half / 2, y, x, y + half);
                }
                default -> {
                    g2.fillOval(x - half, y - half, size, size);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(x - half, y - half, size, size);
                }
            }
        } finally {
            g2.dispose();
        }
        return image;
    }

    private Path2D buildStar(double centerX, double centerY, double outerRadius, double innerRadius) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            double angle = Math.toRadians(-90 + (i * 36));
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    private boolean applyChanges() {
        String newName = nameField.getText() != null ? nameField.getText().trim() : "";
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la capa no puede estar vacio.");
            return false;
        }

        layer.setName(newName);
        layer.setVisible(visibleCheck.isSelected());

        layer.setFillColor(withAlpha(fillColorButton.getBackground(), 120));
        layer.setBorderColor(borderColorButton.getBackground());
        layer.setLineColor(lineColorButton.getBackground());
        layer.setLineWidth(((Double) lineWidthSpinner.getValue()).floatValue());
        layer.setLineSymbolStyle((Layer.LineSymbolStyle) lineStyleCombo.getSelectedItem());
        layer.setPolygonFillStyle((Layer.PolygonFillStyle) polygonStyleCombo.getSelectedItem());
        layer.setPointColor(pointColorButton.getBackground());
        layer.setPointSize((Integer) pointSizeSpinner.getValue());
        layer.setPointSymbolStyle((Layer.PointSymbolStyle) pointStyleCombo.getSelectedItem());

        if (labelsVisibleCheck.isSelected() && labelFieldCombo.isEnabled() && labelFieldCombo.getSelectedItem() != null) {
            layer.setLabelsVisible(true);
            layer.setLabelField(labelFieldCombo.getSelectedItem().toString());
        } else {
            layer.setLabelsVisible(false);
            layer.setLabelField(null);
        }

        CatgisDesktopApp.markProjectDirty();

        if (CatgisDesktopApp.layersPanel != null) {
            CatgisDesktopApp.layersPanel.refreshLayerList();
        }
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.repaint();
        }
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage("Propiedades actualizadas: " + layer.getName());
        }
        return true;
    }

    private Color withAlpha(Color color, int alpha) {
        if (color == null) {
            return new Color(120, 170, 255, alpha);
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private String buildPathDisplay(Layer layer) {
        if (layer == null || layer.getPath() == null || layer.getPath().isBlank()) {
            return "Sin archivo asociado";
        }

        String path = layer.getPath().trim();
        String lower = path.toLowerCase();
        boolean looksStored = new java.io.File(path).isAbsolute()
                || new java.io.File(path).exists()
                || lower.endsWith(".shp")
                || lower.endsWith(".geojson")
                || lower.endsWith(".json")
                || lower.endsWith(".kml")
                || lower.endsWith(".tif")
                || lower.endsWith(".tiff")
                || lower.endsWith(".img")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".bmp")
                || lower.endsWith(".gif");

        return looksStored ? path : "Sin archivo asociado";
    }

    public static void open(Layer layer) {
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.layersPanel);
        LayerPropertiesDialog dialog = new LayerPropertiesDialog(owner, layer);
        dialog.setVisible(true);
    }
}
