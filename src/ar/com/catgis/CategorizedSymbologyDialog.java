package ar.com.catgis;

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
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashSet;
import java.util.Set;

public class CategorizedSymbologyDialog extends JDialog {

    private final Layer layer;
    private final ShapefileData data;
    private final String geometryFamily;
    private final boolean polygonMode;
    private final CategorizedSymbology workingCopy;

    private final JComboBox<String> fieldCombo;
    private final JTextField legendTitleField;
    private final JTextField legendSubtitleField;
    private final JPanel rowsPanel;

    public CategorizedSymbologyDialog(Frame owner, Layer layer, ShapefileData data) {
        super(owner, "Simbologia por campo", true);
        this.layer = layer;
        this.data = data;
        this.geometryFamily = resolveGeometryFamily(layer, data);
        this.polygonMode = "POLYGON".equalsIgnoreCase(geometryFamily);
        this.workingCopy = copyOf(polygonMode ? layer.getPolygonCategorizedSymbology() : layer.getLineCategorizedSymbology());

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        fieldCombo = new JComboBox<>();
        if (data != null) {
            for (String name : data.getAttributeNames()) {
                fieldCombo.addItem(name);
            }
        }
        if (workingCopy.getFieldName() != null && !workingCopy.getFieldName().isBlank()) {
            fieldCombo.setSelectedItem(workingCopy.getFieldName());
        }

        legendTitleField = new JTextField(workingCopy.getLegendTitle(), 22);
        legendSubtitleField = new JTextField(workingCopy.getLegendSubtitle(), 22);
        rowsPanel = new JPanel(new GridBagLayout());
        rowsPanel.setOpaque(false);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(rowsPanel), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        rebuildRows();
        setSize(760, 560);
        setLocationRelativeTo(owner);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        panel.add(new JLabel("Campo:"), gc);
        gc.gridy++;
        panel.add(fieldCombo, gc);

        gc.gridy++;
        JButton loadButton = new JButton("Cargar categorias");
        loadButton.addActionListener(e -> loadCategoriesFromField());
        panel.add(loadButton, gc);

        gc.gridy++;
        panel.add(new JLabel("Titulo de leyenda temática:"), gc);
        gc.gridy++;
        panel.add(legendTitleField, gc);

        gc.gridy++;
        panel.add(new JLabel("Subtitulo:"), gc);
        gc.gridy++;
        panel.add(legendSubtitleField, gc);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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

    private void loadCategoriesFromField() {
        Object selected = fieldCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un campo.");
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
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        if (workingCopy.getRules().isEmpty()) {
            rowsPanel.add(new JLabel("Todavia no hay categorias cargadas."), gc);
            rowsPanel.revalidate();
            rowsPanel.repaint();
            return;
        }

        for (CategoryStyleRule rule : workingCopy.getRules().values()) {
            JPanel row = buildRow(rule);
            rowsPanel.add(row, gc);
            gc.gridy++;
        }
        gc.weighty = 1;
        rowsPanel.add(new JLabel(""), gc);
        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private JPanel buildRow(CategoryStyleRule rule) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 4, 3, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        row.add(new JLabel(rule.getValue()), gc);

        gc.gridx++;
        JButton primaryButton = colorButton(rule.getPrimaryColor(), "Color principal", selected -> rule.setPrimaryColor(selected));
        row.add(primaryButton, gc);

        if (polygonMode) {
            gc.gridx++;
            JButton secondaryButton = colorButton(rule.getSecondaryColor(), "Color borde", selected -> rule.setSecondaryColor(selected));
            row.add(secondaryButton, gc);
        }

        gc.gridx++;
        if (polygonMode) {
            JComboBox<Layer.PolygonFillStyle> styleCombo = new JComboBox<>(Layer.PolygonFillStyle.values());
            styleCombo.setSelectedItem(rule.getPolygonFillStyle());
            styleCombo.addActionListener(e -> rule.setPolygonFillStyle((Layer.PolygonFillStyle) styleCombo.getSelectedItem()));
            row.add(styleCombo, gc);
        } else {
            JComboBox<Layer.LineSymbolStyle> styleCombo = new JComboBox<>(Layer.LineSymbolStyle.values());
            styleCombo.setSelectedItem(rule.getLineStyle());
            styleCombo.addActionListener(e -> rule.setLineStyle((Layer.LineSymbolStyle) styleCombo.getSelectedItem()));
            row.add(styleCombo, gc);
        }

        return row;
    }

    private JButton colorButton(Color color, String title, java.util.function.Consumer<Color> consumer) {
        JButton button = new JButton("Color");
        button.setOpaque(true);
        button.setBackground(color);
        button.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(this, title, button.getBackground());
            if (selected != null) {
                button.setBackground(selected);
                consumer.accept(selected);
            }
        });
        return button;
    }

    private void applyChanges() {
        if (fieldCombo.getSelectedItem() != null && !workingCopy.getRules().isEmpty()) {
            workingCopy.setFieldName(resolveFieldName(String.valueOf(fieldCombo.getSelectedItem())));
        }
        workingCopy.setLegendTitle(legendTitleField.getText());
        workingCopy.setLegendSubtitle(legendSubtitleField.getText());

        CategorizedSymbology target = polygonMode ? layer.getPolygonCategorizedSymbology() : layer.getLineCategorizedSymbology();
        target.clearRules();
        target.setFieldName(workingCopy.getFieldName());
        target.setLegendTitle(workingCopy.getLegendTitle());
        target.setLegendSubtitle(workingCopy.getLegendSubtitle());
        for (CategoryStyleRule rule : workingCopy.getRules().values()) {
            CategoryStyleRule targetRule = target.getOrCreateRule(rule.getValue());
            targetRule.setPrimaryColor(rule.getPrimaryColor());
            targetRule.setSecondaryColor(rule.getSecondaryColor());
            targetRule.setLineStyle(rule.getLineStyle());
            targetRule.setPolygonFillStyle(rule.getPolygonFillStyle());
        }

        CatgisDesktopApp.markProjectDirty();
        if (CatgisDesktopApp.mapPanel != null) {
            CatgisDesktopApp.mapPanel.repaint();
        }
        dispose();
    }

    private String resolveFieldName(String requestedName) {
        return FeatureAttributeResolver.resolveFieldName(data != null ? data.getSchema() : null, requestedName);
    }

    private static String resolveGeometryFamily(Layer layer, ShapefileData data) {
        String fromData = VectorLayerUtils.resolveGeometryFamily(data);
        if (!fromData.isBlank()) {
            return fromData;
        }
        String type = layer != null ? layer.getType() : "";
        if (type == null) {
            return "";
        }
        String normalized = type.toUpperCase();
        if (normalized.contains("POLYGON")) {
            return "POLYGON";
        }
        if (normalized.contains("LINE")) {
            return "LINE";
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
        if (!polygonMode) {
            rule.setLineStyle(index % 2 == 0 ? Layer.LineSymbolStyle.SOLID : Layer.LineSymbolStyle.DASHED);
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
            targetRule.setPolygonFillStyle(rule.getPolygonFillStyle());
        }
        return copy;
    }

    public static void open(Layer layer) {
        if (layer == null || CatgisDesktopApp.mapPanel == null) {
            return;
        }
        ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(layer);
        String geometryFamily = resolveGeometryFamily(layer, data);
        if (!"LINE".equalsIgnoreCase(geometryFamily) && !"POLYGON".equalsIgnoreCase(geometryFamily)) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), "La simbologia por campo en esta etapa esta disponible para lineas y poligonos.");
            return;
        }
        if (data == null) {
            JOptionPane.showMessageDialog(CatgisDesktopApp.getMainFrameSafe(), "La capa seleccionada no tiene datos vectoriales cargados.");
            return;
        }
        Frame owner = JOptionPane.getFrameForComponent(CatgisDesktopApp.layersPanel);
        CategorizedSymbologyDialog dialog = new CategorizedSymbologyDialog(owner, layer, data);
        dialog.setVisible(true);
    }
}
