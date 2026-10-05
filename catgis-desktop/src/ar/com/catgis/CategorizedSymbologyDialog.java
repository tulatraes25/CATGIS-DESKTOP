package ar.com.catgis;
import ar.com.catgis.data.vector.VectorLayerUtils;

import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.simple.SimpleFeature;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.geom.Path2D;
import java.util.LinkedHashSet;
import java.util.Set;
import ar.com.catgis.core.model.Layer;

public class CategorizedSymbologyDialog extends JDialog {

    private final Layer layer;
    private final ShapefileData data;
    private final boolean pointMode;
    private final boolean polygonMode;
    private final CategorizedSymbology workingCopy;

    private final JComboBox<String> fieldCombo;
    private final JTextField legendTitleField;
    private final JTextField legendSubtitleField;
    private final JPanel rowsPanel;
    private final JLabel categoriesStatusLabel;

    public CategorizedSymbologyDialog(Frame owner, Layer layer, ShapefileData data) {
        super(owner, "Simbologia por campo", true);
        this.layer = layer;
        this.data = data;
        String geometryFamily = resolveGeometryFamily(layer, data);
        this.pointMode = "POINT".equalsIgnoreCase(geometryFamily);
        this.polygonMode = "POLYGON".equalsIgnoreCase(geometryFamily);
        this.workingCopy = copyOf(resolveTargetSymbology());

        setLayout(new BorderLayout(12, 12));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(new Color(246, 248, 251));

        fieldCombo = new JComboBox<>();
        if (data != null) {
            for (String name : data.getAttributeNames()) {
                fieldCombo.addItem(name);
            }
        }
        if (workingCopy.getFieldName() != null && !workingCopy.getFieldName().isBlank()) {
            fieldCombo.setSelectedItem(workingCopy.getFieldName());
        }

        legendTitleField = new JTextField(workingCopy.getLegendTitle(), 20);
        legendSubtitleField = new JTextField(workingCopy.getLegendSubtitle(), 20);
        rowsPanel = new JPanel(new GridBagLayout());
        rowsPanel.setOpaque(false);
        categoriesStatusLabel = new JLabel("0 categorias");
        categoriesStatusLabel.setForeground(new Color(71, 85, 105));
        categoriesStatusLabel.setFont(categoriesStatusLabel.getFont().deriveFont(Font.BOLD, 11.5f));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        rebuildRows();
        setMinimumSize(new Dimension(860, 500));
        setSize(960, 540);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel title = new JLabel("Simbologia por atributo");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(23, 35, 55));

        JLabel subtitle = new JLabel("<html><b>Capa:</b> " + escapeHtml(layer.getName())
                + " &nbsp;&nbsp; <b>Geometria:</b> " + geometryLabel() + "</html>");
        subtitle.setForeground(new Color(88, 98, 112));

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(title, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.CENTER);

        JLabel badge = new JLabel(geometryLabel(), SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setPreferredSize(new Dimension(124, 34));
        badge.setBackground(new Color(226, 232, 240));
        badge.setForeground(new Color(30, 41, 59));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(148, 163, 184)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        panel.add(text, BorderLayout.CENTER);
        panel.add(badge, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.add(buildConfigCard(), BorderLayout.WEST);
        panel.add(buildCategoriesCard(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildConfigCard() {
        JPanel panel = buildCardPanel();
        panel.setPreferredSize(new Dimension(300, 0));
        GridBagConstraints gc = createConstraints();
        int row = 0;
        addLabel(panel, gc, row++, "Configuracion tematica", true);
        addNote(panel, gc, row++, "<html>Selecciona un campo, detecta sus categorias y ajusta la simbologia sin salir del mapa.</html>");
        addLabel(panel, gc, row++, "Campo tematico", false);
        addComponent(panel, gc, row++, fieldCombo);

        JButton loadButton = new JButton("Generar categorias");
        loadButton.addActionListener(e -> loadCategoriesFromField());
        addComponent(panel, gc, row++, loadButton);

        addLabel(panel, gc, row++, "Titulo de leyenda", false);
        addComponent(panel, gc, row++, legendTitleField);
        addLabel(panel, gc, row++, "Subtitulo", false);
        addComponent(panel, gc, row++, legendSubtitleField);
        addNote(panel, gc, row++, "<html><b>Ejemplos:</b> pozos por tipo, lineas por terreno o poligonos por estado.</html>");
        gc.gridy = row;
        gc.weighty = 1;
        panel.add(new JLabel(""), gc);
        return panel;
    }

    private JPanel buildCategoriesCard() {
        JPanel panel = buildCardPanel();
        panel.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Categorias detectadas");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        header.add(title, BorderLayout.WEST);
        header.add(categoriesStatusLabel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(rowsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        JButton clearButton = new JButton("Quitar simbologia");
        clearButton.addActionListener(e -> {
            workingCopy.clearRules();
            workingCopy.setFieldName("");
            rebuildRows();
        });

        JButton applyButton = new JButton("Aplicar");
        applyButton.addActionListener(e -> applyChanges());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        panel.add(clearButton);
        panel.add(applyButton);
        panel.add(cancelButton);
        return panel;
    }

    private JPanel buildCardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    private GridBagConstraints createConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        return gc;
    }

    private void addLabel(JPanel panel, GridBagConstraints gc, int row, String text, boolean title) {
        JLabel label = new JLabel(text);
        if (title) {
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
            label.setForeground(new Color(30, 41, 59));
        }
        gc.gridy = row;
        panel.add(label, gc);
    }

    private void addNote(JPanel panel, GridBagConstraints gc, int row, String html) {
        JLabel label = new JLabel(html);
        label.setForeground(new Color(88, 98, 112));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11.5f));
        gc.gridy = row;
        panel.add(label, gc);
    }

    private void addComponent(JPanel panel, GridBagConstraints gc, int row, Component component) {
        gc.gridy = row;
        panel.add(component, gc);
    }

    private void loadCategoriesFromField() {
        Object selected = fieldCombo.getSelectedItem();
        if (selected == null) {
            NotificationManager.warn(this, null, "Selecciona un campo.");
            return;
        }
        workingCopy.clearRules();
        workingCopy.setFieldName(resolveFieldName(String.valueOf(selected)));
        workingCopy.setLegendTitle(legendTitleField.getText());
        workingCopy.setLegendSubtitle(legendSubtitleField.getText());

        Set<String> values = new LinkedHashSet<>();
        if (data != null) {
            for (SimpleFeature feature : data.getFeatures()) {
                if (feature == null) {
                    continue;
                }
                Object value = FeatureAttributeResolver.resolveAttribute(feature, workingCopy.getFieldName());
                values.add(CategorizedSymbology.valueKey(value != null ? String.valueOf(value) : null));
            }
        }

        int index = 0;
        for (String value : values) {
            CategoryStyleRule rule = workingCopy.getOrCreateRule(value);
            applyDefaultPalette(rule, index++);
        }
        rebuildRows();
    }

    private void rebuildRows() {
        rowsPanel.removeAll();
        GridBagConstraints gc = createConstraints();
        gc.insets = new Insets(0, 0, 10, 0);
        int row = 0;
        int count = workingCopy.getRules().size();
        categoriesStatusLabel.setText(count + (count == 1 ? " categoria" : " categorias"));

        if (count == 0) {
            gc.gridy = row++;
            rowsPanel.add(new JLabel("<html><b>Todavia no hay categorias cargadas.</b><br/>Selecciona un campo y pulsa <b>Generar categorias</b>.</html>"), gc);
        } else {
            for (CategoryStyleRule rule : workingCopy.getRules().values()) {
                gc.gridy = row++;
                rowsPanel.add(buildRow(rule), gc);
            }
        }
        gc.gridy = row;
        gc.weighty = 1;
        rowsPanel.add(new JLabel(""), gc);
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private JPanel buildRow(CategoryStyleRule rule) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel preview = createPreviewPanel(rule);
        preview.setPreferredSize(new Dimension(64, 42));
        row.add(preview, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        JLabel title = new JLabel(rule.getValue());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(new Color(30, 41, 59));
        center.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        JButton primaryButton = colorButton(rule.getPrimaryColor(), "Color principal", rule::setPrimaryColor);
        controls.add(primaryButton);

        if (pointMode) {
            JComboBox<Layer.PointSymbolStyle> styleCombo = new JComboBox<>(Layer.PointSymbolStyle.values());
            styleCombo.setSelectedItem(rule.getPointSymbolStyle());
            styleCombo.addActionListener(e -> {
                rule.setPointSymbolStyle((Layer.PointSymbolStyle) styleCombo.getSelectedItem());
                preview.repaint();
            });
            controls.add(styleCombo);
            JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(rule.getPointSize(), 4, 36, 1));
            sizeSpinner.addChangeListener(e -> {
                rule.setPointSize((Integer) sizeSpinner.getValue());
                preview.repaint();
            });
            controls.add(sizeSpinner);
            primaryButton.addActionListener(e -> preview.repaint());
        } else {
            if (polygonMode) {
                JButton secondaryButton = colorButton(rule.getSecondaryColor(), "Color borde", rule::setSecondaryColor);
                secondaryButton.addActionListener(e -> preview.repaint());
                controls.add(secondaryButton);
                JComboBox<Layer.PolygonFillStyle> styleCombo = new JComboBox<>(Layer.PolygonFillStyle.values());
                styleCombo.setSelectedItem(rule.getPolygonFillStyle());
                styleCombo.addActionListener(e -> {
                    rule.setPolygonFillStyle((Layer.PolygonFillStyle) styleCombo.getSelectedItem());
                    preview.repaint();
                });
                controls.add(styleCombo);
            } else {
                JComboBox<Layer.LineSymbolStyle> styleCombo = new JComboBox<>(Layer.LineSymbolStyle.values());
                styleCombo.setSelectedItem(rule.getLineStyle());
                styleCombo.addActionListener(e -> {
                    rule.setLineStyle((Layer.LineSymbolStyle) styleCombo.getSelectedItem());
                    preview.repaint();
                });
                controls.add(styleCombo);
            }
            JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel((double) rule.getLineWidth(), 0.5, 12.0, 0.5));
            widthSpinner.addChangeListener(e -> {
                rule.setLineWidth(((Double) widthSpinner.getValue()).floatValue());
                preview.repaint();
            });
            controls.add(widthSpinner);
            primaryButton.addActionListener(e -> preview.repaint());
        }

        center.add(controls, BorderLayout.CENTER);
        row.add(center, BorderLayout.CENTER);
        return row;
    }

    private JPanel createPreviewPanel(CategoryStyleRule rule) {
        return new JPanel() {
            {
                setOpaque(true);
                setBackground(new Color(248, 250, 252));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    if (pointMode) {
                        drawPointSample(g2, rule);
                    } else if (polygonMode) {
                        drawPolygonSample(g2, rule);
                    } else {
                        drawLineSample(g2, rule);
                    }
                } finally {
                    g2.dispose();
                }
            }
        };
    }

    private void drawPointSample(Graphics2D g2, CategoryStyleRule rule) {
        int x = 32;
        int y = 20;
        int size = Math.max(8, Math.min(18, rule.getPointSize()));
        PointSymbolRenderer.paint(
                g2,
                rule.getPointSymbolStyle(),
                x,
                y,
                size,
                rule.getPrimaryColor(),
                Color.BLACK
        );
    }

    private void drawLineSample(Graphics2D g2, CategoryStyleRule rule) {
        g2.setColor(rule.getPrimaryColor());
        g2.setStroke(buildPreviewStroke(rule.getLineStyle(), rule.getLineWidth()));
        Path2D path = new Path2D.Double();
        path.moveTo(8, 30);
        path.curveTo(18, 8, 38, 32, 56, 10);
        g2.draw(path);
    }

    private void drawPolygonSample(Graphics2D g2, CategoryStyleRule rule) {
        int x = 10;
        int y = 8;
        int w = 42;
        int h = 22;
        g2.setColor(rule.getPrimaryColor());
        if (rule.getPolygonFillStyle() != Layer.PolygonFillStyle.OUTLINE_ONLY) {
            g2.fillRoundRect(x, y, w, h, 10, 10);
        }
        g2.setColor(rule.getSecondaryColor());
        if (rule.getPolygonFillStyle() == Layer.PolygonFillStyle.DIAGONAL_HATCH) {
            for (int i = -h; i < w; i += 7) {
                g2.drawLine(x + i, y + h, x + i + h, y);
            }
        } else if (rule.getPolygonFillStyle() == Layer.PolygonFillStyle.CROSS_HATCH) {
            for (int yy = y + 4; yy < y + h; yy += 6) {
                g2.drawLine(x + 4, yy, x + w - 4, yy);
            }
            for (int xx = x + 4; xx < x + w; xx += 6) {
                g2.drawLine(xx, y + 4, xx, y + h - 4);
            }
        }
        g2.setStroke(new BasicStroke(Math.max(1f, rule.getLineWidth())));
        g2.drawRoundRect(x, y, w, h, 10, 10);
    }

    private BasicStroke buildPreviewStroke(Layer.LineSymbolStyle style, float width) {
        float safeWidth = Math.max(1f, width);
        if (style == Layer.LineSymbolStyle.DASHED) {
            return new BasicStroke(safeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{12f, 8f}, 0f);
        }
        if (style == Layer.LineSymbolStyle.DOTTED) {
            return new BasicStroke(safeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{2f, 8f}, 0f);
        }
        if (style == Layer.LineSymbolStyle.DASH_DOT) {
            return new BasicStroke(safeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{12f, 6f, 2f, 6f}, 0f);
        }
        return new BasicStroke(safeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private JButton colorButton(Color color, String title, java.util.function.Consumer<Color> consumer) {
        JButton button = new JButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(106, 28));
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        updateColorButtonAppearance(button, color);
        button.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(this, title, selectedColor(button));
            if (selected != null) {
                updateColorButtonAppearance(button, selected);
                consumer.accept(selected);
            }
        });
        return button;
    }

    private void updateColorButtonAppearance(JButton button, Color color) {
        Color safe = color != null ? color : Color.WHITE;
        button.putClientProperty("catgis.color", safe);
        button.setText(String.format("#%02X%02X%02X", safe.getRed(), safe.getGreen(), safe.getBlue()));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228)),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
    }

    private Color selectedColor(JButton button) {
        Object value = button.getClientProperty("catgis.color");
        return value instanceof Color color ? color : Color.WHITE;
    }

    private void applyChanges() {
        if (fieldCombo.getSelectedItem() != null && !workingCopy.getRules().isEmpty()) {
            workingCopy.setFieldName(resolveFieldName(String.valueOf(fieldCombo.getSelectedItem())));
        }
        workingCopy.setLegendTitle(legendTitleField.getText());
        workingCopy.setLegendSubtitle(legendSubtitleField.getText());

        CategorizedSymbology target = resolveTargetSymbology();
        target.clearRules();
        target.setFieldName(workingCopy.getFieldName());
        target.setLegendTitle(workingCopy.getLegendTitle());
        target.setLegendSubtitle(workingCopy.getLegendSubtitle());
        for (CategoryStyleRule rule : workingCopy.getRules().values()) {
            CategoryStyleRule targetRule = target.getOrCreateRule(rule.getValue());
            targetRule.setPrimaryColor(rule.getPrimaryColor());
            targetRule.setSecondaryColor(rule.getSecondaryColor());
            targetRule.setLineStyle(rule.getLineStyle());
            targetRule.setLineWidth(rule.getLineWidth());
            targetRule.setPolygonFillStyle(rule.getPolygonFillStyle());
            targetRule.setPointSymbolStyle(rule.getPointSymbolStyle());
            targetRule.setPointSize(rule.getPointSize());
        }

        CatgisDesktopApp.markProjectDirty();
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().repaint();
        }
        dispose();
    }

    private String resolveFieldName(String requestedName) {
        return FeatureAttributeResolver.resolveFieldName(data != null ? data.getSchema() : null, requestedName);
    }

    private String geometryLabel() {
        return pointMode ? "Puntos" : polygonMode ? "Poligonos" : "Lineas";
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String resolveGeometryFamily(Layer layer, ShapefileData data) {
        String fromData = VectorLayerUtils.resolveGeometryFamily(data);
        if (!fromData.isBlank()) {
            return fromData;
        }
        String type = layer != null ? layer.getType() : "";
        String normalized = type != null ? type.toUpperCase() : "";
        if (normalized.contains("POLYGON")) {
            return "POLYGON";
        }
        if (normalized.contains("LINE")) {
            return "LINE";
        }
        if (normalized.contains("POINT")) {
            return "POINT";
        }
        return "";
    }

    private void applyDefaultPalette(CategoryStyleRule rule, int index) {
        Color[] palette = new Color[]{
                new Color(22, 163, 74),
                new Color(202, 138, 4),
                new Color(220, 38, 38),
                new Color(37, 99, 235),
                new Color(147, 51, 234),
                new Color(8, 145, 178)
        };
        Color primary = palette[index % palette.length];
        rule.setPrimaryColor(primary);
        rule.setSecondaryColor(primary.darker());
        if (pointMode) {
            Layer.PointSymbolStyle[] pointPalette = new Layer.PointSymbolStyle[]{
                    Layer.PointSymbolStyle.WELL,
                    Layer.PointSymbolStyle.PIN,
                    Layer.PointSymbolStyle.CIRCLE,
                    Layer.PointSymbolStyle.SQUARE,
                    Layer.PointSymbolStyle.DIAMOND,
                    Layer.PointSymbolStyle.TARGET
            };
            rule.setPointSymbolStyle(pointPalette[index % pointPalette.length]);
            rule.setPointSize(10 + (index % 3));
        } else if (!polygonMode) {
            rule.setLineStyle(index % 2 == 0 ? Layer.LineSymbolStyle.SOLID : Layer.LineSymbolStyle.DASHED);
            rule.setLineWidth(index % 3 == 0 ? 1.8f : index % 3 == 1 ? 2.4f : 3.0f);
        } else if (index % 3 == 1) {
            rule.setPolygonFillStyle(Layer.PolygonFillStyle.DIAGONAL_HATCH);
        } else if (index % 3 == 2) {
            rule.setPolygonFillStyle(Layer.PolygonFillStyle.CROSS_HATCH);
        }
    }

    private static CategorizedSymbology copyOf(CategorizedSymbology source) {
        CategorizedSymbology copy = new CategorizedSymbology();
        if (source == null) {
            return copy;
        }
        copy.setFieldName(source.getFieldName());
        copy.setLegendTitle(source.getLegendTitle());
        copy.setLegendSubtitle(source.getLegendSubtitle());
        for (CategoryStyleRule rule : source.getRules().values()) {
            CategoryStyleRule targetRule = copy.getOrCreateRule(rule.getValue());
            targetRule.setPrimaryColor(rule.getPrimaryColor());
            targetRule.setSecondaryColor(rule.getSecondaryColor());
            targetRule.setLineStyle(rule.getLineStyle());
            targetRule.setLineWidth(rule.getLineWidth());
            targetRule.setPolygonFillStyle(rule.getPolygonFillStyle());
            targetRule.setPointSymbolStyle(rule.getPointSymbolStyle());
            targetRule.setPointSize(rule.getPointSize());
        }
        return copy;
    }

    private CategorizedSymbology resolveTargetSymbology() {
        if (pointMode) {
            return layer.getPointCategorizedSymbology();
        }
        return polygonMode ? layer.getPolygonCategorizedSymbology() : layer.getLineCategorizedSymbology();
    }

    public static void open(Layer layer) {
        open(CatgisDesktopApp.layersPanel, layer);
    }

    public static void open(Component parent, Layer layer) {
        if (layer == null || AppContext.mapPanel() == null) {
            return;
        }
        ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(layer);
        String geometryFamily = resolveGeometryFamily(layer, data);
        if (!"POINT".equalsIgnoreCase(geometryFamily)
                && !"LINE".equalsIgnoreCase(geometryFamily)
                && !"POLYGON".equalsIgnoreCase(geometryFamily)) {
            NotificationManager.warn(parent, null, "La simbologia por campo en esta etapa esta disponible para puntos, lineas y poligonos.");
            return;
        }
        if (data == null) {
            NotificationManager.warn(parent, null, "La capa seleccionada no tiene datos vectoriales cargados.");
            return;
        }
        Window window = parent != null ? javax.swing.SwingUtilities.getWindowAncestor(parent) : null;
        Frame owner = window instanceof Frame ? (Frame) window : JOptionPane.getFrameForComponent(parent);
        CategorizedSymbologyDialog dialog = new CategorizedSymbologyDialog(owner, layer, data);
        dialog.setLocationRelativeTo(parent != null ? parent : owner);
        dialog.setVisible(true);
    }
}