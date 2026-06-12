package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QueryBuilderDialog extends JDialog {

    private final Layer layer;
    private final ShapefileData data;
    private final JComboBox<FieldOption> fieldCombo;
    private final JComboBox<Operator> operatorCombo;
    private final JTextField valueField;
    private final JLabel resultLabel;
    private final JCheckBox focusTableCheck;
    private final JCheckBox zoomToResultCheck;

    private QueryBuilderDialog(Window owner, Layer layer, ShapefileData data) {
        super(owner, "Constructor de consultas: " + (layer != null ? layer.getName() : "Capa"), ModalityType.MODELESS);
        this.layer = layer;
        this.data = data;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(560, 250);
        setMinimumSize(new Dimension(500, 220));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(6, 6));
        getRootPane().setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Constructor de consultas");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(28, 37, 54));

        JLabel subtitleLabel = new JLabel("Seleccioná entidades por campo y condición, y mandalas al mapa o a la tabla.");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 11f));
        subtitleLabel.setForeground(new Color(95, 105, 120));

        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(2));
        topPanel.add(subtitleLabel);
        add(topPanel, BorderLayout.NORTH);

        List<FieldOption> fieldOptions = buildFieldOptions(layer, data);
        if (fieldOptions.isEmpty()) {
            throw new IllegalStateException("La capa no tiene campos disponibles para consultar.");
        }

        fieldCombo = new JComboBox<>(fieldOptions.toArray(new FieldOption[0]));
        operatorCombo = new JComboBox<>(Operator.values());
        valueField = new JTextField();
        valueField.setColumns(18);
        resultLabel = new JLabel("Listo para consultar.");
        resultLabel.setForeground(new Color(70, 80, 95));
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.PLAIN, 10.5f));

        focusTableCheck = new JCheckBox("Marcar también en tabla");
        focusTableCheck.setOpaque(false);
        focusTableCheck.setSelected(true);

        zoomToResultCheck = new JCheckBox("Zoom al resultado");
        zoomToResultCheck.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 232)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Campo"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(fieldCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Condición"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(operatorCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Valor"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(valueField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        optionsPanel.setOpaque(false);
        optionsPanel.add(focusTableCheck);
        optionsPanel.add(zoomToResultCheck);
        formPanel.add(optionsPanel, gbc);

        gbc.gridy = 4;
        resultLabel.setBorder(BorderFactory.createEmptyBorder(4, 1, 0, 1));
        formPanel.add(resultLabel, gbc);

        add(formPanel, BorderLayout.CENTER);

        JButton previewButton = new JButton("Probar");
        JButton selectButton = new JButton("Seleccionar");
        JButton clearButton = new JButton("Limpiar");
        JButton closeButton = new JButton("Cerrar");

        previewButton.addActionListener(e -> previewQuery());
        selectButton.addActionListener(e -> applyQuery());
        clearButton.addActionListener(e -> clearQuerySelection());
        closeButton.addActionListener(e -> dispose());
        operatorCombo.addActionListener(e -> updateValueFieldState());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(previewButton);
        buttonPanel.add(selectButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        updateValueFieldState();
    }

    public static void open(Layer layer) {
        if (layer == null) {
            JOptionPane.showMessageDialog(null, "No hay una capa seleccionada.");
            return;
        }

        ShapefileData data = OpenAttributeTableAction.ensureLayerDataAvailable(layer);
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            JOptionPane.showMessageDialog(null, "La capa seleccionada no tiene atributos consultables.");
            return;
        }

        try {
            Window owner = CatgisDesktopApp.getMainFrameSafe();
            QueryBuilderDialog dialog = new QueryBuilderDialog(owner, layer, data);
            dialog.setVisible(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Constructor de consultas", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateValueFieldState() {
        Operator operator = (Operator) operatorCombo.getSelectedItem();
        boolean requiresValue = operator == null || operator.requiresValue;
        valueField.setEnabled(requiresValue);
        if (!requiresValue) {
            valueField.setText("");
        }
    }

    private void previewQuery() {
        List<String> featureIds = evaluateQuery();
        resultLabel.setText(featureIds.size() + " resultado(s) sobre " + data.getFeatures().size() + " entidad(es).");
    }

    private void applyQuery() {
        List<String> featureIds = evaluateQuery();
        resultLabel.setText(featureIds.size() + " resultado(s) seleccionados.");

        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().syncSelectionFromAttributeTable(layer, featureIds);
            if (!featureIds.isEmpty() && zoomToResultCheck.isSelected()) {
                AppContext.mapPanel().zoomToFeatureSelection(layer, featureIds);
            }
        }

        AttributeTableWindow tableWindow = OpenAttributeTableAction.getOpenWindow(layer);
        if (tableWindow == null && focusTableCheck.isSelected()) {
            tableWindow = OpenAttributeTableAction.openTable(layer);
        }
        if (tableWindow != null) {
            tableWindow.selectFeatureIds(featureIds);
            if (focusTableCheck.isSelected()) {
                tableWindow.toFront();
                tableWindow.requestFocus();
            }
        }

        if (CatgisDesktopApp.statusBar != null) {
            if (featureIds.isEmpty()) {
                AppContext.setStatusMessage("La consulta no encontró entidades en " + layer.getName() + ".");
            } else {
                AppContext.setStatusMessage("Consulta aplicada en " + layer.getName() + ": " + featureIds.size() + " entidad(es).");
            }
        }
    }

    private void clearQuerySelection() {
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().syncSelectionFromAttributeTable(layer, new ArrayList<>());
        }
        AttributeTableWindow tableWindow = OpenAttributeTableAction.getOpenWindow(layer);
        if (tableWindow != null) {
            tableWindow.clearMapLinkedSelection();
        }
        resultLabel.setText("Selección limpia.");
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Selección de consulta limpiada para " + layer.getName() + ".");
        }
    }

    private List<String> evaluateQuery() {
        FieldOption fieldOption = (FieldOption) fieldCombo.getSelectedItem();
        Operator operator = (Operator) operatorCombo.getSelectedItem();
        String expectedText = valueField.getText() != null ? valueField.getText().trim() : "";

        if (fieldOption == null || operator == null) {
            return new ArrayList<>();
        }
        if (operator.requiresValue && expectedText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribí un valor para ejecutar la consulta.");
            SwingUtilities.invokeLater(valueField::requestFocusInWindow);
            return new ArrayList<>();
        }

        List<String> matchingIds = new ArrayList<>();
        List<SimpleFeature> features = data.getFeatures();
        if (features == null) {
            return matchingIds;
        }

        for (SimpleFeature feature : features) {
            if (feature == null) {
                continue;
            }
            Object attributeValue = feature.getAttribute(fieldOption.rawName);
            if (matches(attributeValue, operator, expectedText)) {
                matchingIds.add(feature.getID());
            }
        }
        return matchingIds;
    }

    private boolean matches(Object attributeValue, Operator operator, String expectedText) {
        if (operator == Operator.IS_NULL) {
            return attributeValue == null || String.valueOf(attributeValue).trim().isEmpty();
        }
        if (operator == Operator.IS_NOT_NULL) {
            return attributeValue != null && !String.valueOf(attributeValue).trim().isEmpty();
        }

        String actualText = attributeValue != null ? String.valueOf(attributeValue).trim() : "";
        String expected = expectedText != null ? expectedText.trim() : "";
        String actualLower = actualText.toLowerCase(Locale.ROOT);
        String expectedLower = expected.toLowerCase(Locale.ROOT);

        switch (operator) {
            case CONTAINS:
                return actualLower.contains(expectedLower);
            case STARTS_WITH:
                return actualLower.startsWith(expectedLower);
            case ENDS_WITH:
                return actualLower.endsWith(expectedLower);
            case EQUALS:
                return compareValues(attributeValue, expected) == 0;
            case NOT_EQUALS:
                return compareValues(attributeValue, expected) != 0;
            case GREATER_THAN:
                return compareValues(attributeValue, expected) > 0;
            case GREATER_OR_EQUAL:
                return compareValues(attributeValue, expected) >= 0;
            case LESS_THAN:
                return compareValues(attributeValue, expected) < 0;
            case LESS_OR_EQUAL:
                return compareValues(attributeValue, expected) <= 0;
            default:
                return false;
        }
    }

    private int compareValues(Object attributeValue, String expected) {
        Double actualNumber = toDouble(attributeValue);
        Double expectedNumber = toDouble(expected);
        if (actualNumber != null && expectedNumber != null) {
            return Double.compare(actualNumber, expectedNumber);
        }

        Long actualDate = toDateMillis(attributeValue);
        Long expectedDate = toDateMillis(expected);
        if (actualDate != null && expectedDate != null) {
            return Long.compare(actualDate, expectedDate);
        }

        String actualText = attributeValue != null ? String.valueOf(attributeValue).trim().toLowerCase(Locale.ROOT) : "";
        String expectedText = expected != null ? expected.trim().toLowerCase(Locale.ROOT) : "";
        return actualText.compareTo(expectedText);
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long toDateMillis(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).getTime();
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }

        String[] patterns = {
                "dd/MM/yyyy",
                "d/M/yyyy",
                "dd-MM-yyyy",
                "d-M-yyyy",
                "yyyy-MM-dd",
                "yyyy/MM/dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                return sdf.parse(text).getTime();
            } catch (Exception ignored) { CatgisLogger.warn("QueryBuilderDialog: operation failed", ignored); }
        }
        return null;
    }

    private static List<FieldOption> buildFieldOptions(Layer layer, ShapefileData data) {
        List<FieldOption> options = new ArrayList<>();
        if (data == null || data.getFeatures() == null || data.getFeatures().isEmpty()) {
            return options;
        }

        SimpleFeature firstFeature = data.getFeatures().get(0);
        if (firstFeature == null) {
            return options;
        }

        for (Property property : firstFeature.getProperties()) {
            String name = property.getName().toString();
            if ("the_geom".equalsIgnoreCase(name) || "geom".equalsIgnoreCase(name)) {
                continue;
            }
            FieldConfig config = layer.getOrCreateFieldConfig(name, "");
            String publicName = config.getPublicName();
            String label = publicName != null && !publicName.isBlank() && !publicName.equals(name)
                    ? publicName + " (" + name + ")"
                    : name;
            options.add(new FieldOption(name, label));
        }
        return options;
    }

    private enum Operator {
        CONTAINS("contiene", true),
        EQUALS("igual a", true),
        NOT_EQUALS("distinto de", true),
        STARTS_WITH("empieza con", true),
        ENDS_WITH("termina con", true),
        GREATER_THAN("mayor que", true),
        GREATER_OR_EQUAL("mayor o igual que", true),
        LESS_THAN("menor que", true),
        LESS_OR_EQUAL("menor o igual que", true),
        IS_NULL("es nulo", false),
        IS_NOT_NULL("no es nulo", false);

        private final String label;
        private final boolean requiresValue;

        Operator(String label, boolean requiresValue) {
            this.label = label;
            this.requiresValue = requiresValue;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class FieldOption {
        private final String rawName;
        private final String label;

        private FieldOption(String rawName, String label) {
            this.rawName = rawName;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
