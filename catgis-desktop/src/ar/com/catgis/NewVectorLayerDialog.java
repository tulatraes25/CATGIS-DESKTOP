package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NewVectorLayerDialog extends JDialog {

    private static final String[] FIELD_TYPES = {
            "String", "Integer", "Long", "Float", "Double", "Date", "Timestamp", "Boolean"
    };

    private final JTextField nameField;
    private final JTextField pathField;
    private final JComboBox<String> geometryCombo;
    private final DefaultTableModel fieldModel;
    private Result result;

    private NewVectorLayerDialog(Window owner, String geometryHint) {
        super(owner, "Nueva capa vectorial", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Nombre de capa:");
        nameField = new JTextField("nueva_capa");

        JLabel geometryLabel = new JLabel("Tipo geometrico:");
        geometryCombo = new JComboBox<>(new String[]{"Punto", "Linea", "Poligono"});
        preselectGeometry(geometryHint);

        JLabel pathLabel = new JLabel("Archivo SHP:");
        pathField = new JTextField();
        JButton browseButton = new JButton("Buscar...");
        browseButton.addActionListener(e -> choosePath());

        JLabel fieldsLabel = new JLabel("Campos iniciales:");
        JLabel helpLabel = new JLabel("<html><span style='color:#555'>Shapefile: nombres de campo de hasta 10 caracteres, letras, numeros o guion bajo.</span></html>");

        fieldModel = new DefaultTableModel(new Object[]{"Campo", "Tipo", "Longitud", "Precision"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        JTable fieldTable = new JTable(fieldModel);
        fieldTable.setRowHeight(24);
        fieldTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        fieldTable.setCellSelectionEnabled(true);
        fieldTable.setShowGrid(true);
        fieldTable.setGridColor(new Color(188, 194, 204));
        fieldTable.setIntercellSpacing(new java.awt.Dimension(1, 1));
        fieldTable.setFillsViewportHeight(true);
        fieldTable.setFont(fieldTable.getFont().deriveFont(Font.PLAIN, 12f));
        JTableHeader header = fieldTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(new Color(236, 240, 245));
        header.setForeground(new Color(36, 46, 62));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
        fieldTable.getColumnModel().getColumn(1).setCellEditor(new javax.swing.DefaultCellEditor(new JComboBox<>(FIELD_TYPES)));
        fieldTable.getColumnModel().getColumn(0).setPreferredWidth(180);
        fieldTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        fieldTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        fieldTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        fieldModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() != TableModelEvent.UPDATE || e.getColumn() != 1) {
                    return;
                }
                int row = e.getFirstRow();
                if (row < 0 || row >= fieldModel.getRowCount()) {
                    return;
                }
                String typeName = String.valueOf(fieldModel.getValueAt(row, 1));
                if (isBlankCell(row, 2)) {
                    fieldModel.setValueAt(defaultLengthForType(typeName), row, 2);
                }
                if (isBlankCell(row, 3)) {
                    fieldModel.setValueAt(defaultPrecisionForType(typeName), row, 3);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fieldTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new java.awt.Color(220, 224, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JButton addFieldButton = new JButton("Agregar campo");
        addFieldButton.addActionListener(e -> fieldModel.addRow(new Object[]{"", "String", 80, 0}));

        JButton removeFieldButton = new JButton("Quitar campo");
        removeFieldButton.addActionListener(e -> {
            int row = fieldTable.getSelectedRow();
            if (row >= 0) {
                fieldModel.removeRow(row);
            }
        });

        JPanel fieldButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        fieldButtons.setOpaque(false);
        fieldButtons.add(addFieldButton);
        fieldButtons.add(removeFieldButton);

        fieldModel.addRow(new Object[]{"", "String", 80, 0});

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        content.add(nameLabel, gc);
        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1; gc.gridwidth = 2;
        content.add(nameField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.gridwidth = 1;
        content.add(geometryLabel, gc);
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1; gc.gridwidth = 2;
        content.add(geometryCombo, gc);

        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0; gc.gridwidth = 1;
        content.add(pathLabel, gc);
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1;
        content.add(pathField, gc);
        gc.gridx = 2; gc.gridy = 2; gc.weightx = 0;
        content.add(browseButton, gc);

        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0; gc.gridwidth = 3;
        content.add(fieldsLabel, gc);

        gc.gridx = 0; gc.gridy = 4; gc.weightx = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.BOTH; gc.weighty = 1;
        content.add(scrollPane, gc);

        gc.gridx = 0; gc.gridy = 5; gc.weightx = 1; gc.gridwidth = 3; gc.weighty = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        content.add(fieldButtons, gc);

        gc.gridx = 0; gc.gridy = 6; gc.weightx = 1; gc.gridwidth = 3;
        content.add(helpLabel, gc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton acceptButton = new JButton("Crear capa");
        JButton cancelButton = new JButton("Cancelar");
        acceptButton.addActionListener(e -> accept());
        cancelButton.addActionListener(e -> dispose());
        footer.add(acceptButton);
        footer.add(cancelButton);

        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, acceptButton, this::dispose);
        setSize(720, 430);
        setLocationRelativeTo(owner);
    }

    public static Result open(Window owner, String geometryHint) {
        NewVectorLayerDialog dialog = new NewVectorLayerDialog(owner, geometryHint);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void preselectGeometry(String geometryHint) {
        String normalized = geometryHint != null ? geometryHint.trim().toUpperCase(Locale.ROOT) : "";
        if ("POINT".equals(normalized) || "PUNTO".equals(normalized)) {
            geometryCombo.setSelectedItem("Punto");
        } else if ("LINE".equals(normalized) || "LINEA".equals(normalized)) {
            geometryCombo.setSelectedItem("Linea");
        } else if ("POLYGON".equals(normalized) || "POLIGONO".equals(normalized)) {
            geometryCombo.setSelectedItem("Poligono");
        }
    }

    private void choosePath() {
        JFileChooser chooser = FileChooserSupport.createChooser("vector-save", "Guardar nueva capa shapefile");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Shapefile (*.shp)", "shp"));

        String suggestedName = sanitizeFileName(nameField.getText());
        chooser.setSelectedFile(FileChooserSupport.resolveSuggestedFile(
                "vector-save",
                new File((suggestedName.isBlank() ? "nueva_capa" : suggestedName) + ".shp")
        ));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".shp")) {
            file = new File(file.getAbsolutePath() + ".shp");
        }
        FileChooserSupport.rememberFile("vector-save", file);
        pathField.setText(file.getAbsolutePath());
    }

    private void accept() {
        String layerName = nameField.getText() != null ? nameField.getText().trim() : "";
        if (layerName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Defini un nombre de capa.");
            return;
        }

        String rawPath = pathField.getText() != null ? pathField.getText().trim() : "";
        if (rawPath.isBlank()) {
            JOptionPane.showMessageDialog(this, "Elegi donde guardar el shapefile.");
            return;
        }

        File file = new File(rawPath);
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".shp")) {
            file = new File(file.getAbsolutePath() + ".shp");
        }

        List<FieldConfig> fields = new ArrayList<>();
        Set<String> usedNames = new LinkedHashSet<>();
        for (int row = 0; row < fieldModel.getRowCount(); row++) {
            Object fieldValue = fieldModel.getValueAt(row, 0);
            Object typeValue = fieldModel.getValueAt(row, 1);
            Object lengthValue = fieldModel.getValueAt(row, 2);
            Object precisionValue = fieldModel.getValueAt(row, 3);
            String fieldName = fieldValue != null ? String.valueOf(fieldValue).trim() : "";
            String typeName = typeValue != null ? String.valueOf(typeValue).trim() : "String";

            if (fieldName.isBlank()) {
                continue;
            }
            if (!fieldName.matches("[A-Za-z][A-Za-z0-9_]{0,9}")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Campo invalido: \"" + fieldName + "\".\nUsa hasta 10 caracteres, empezando con letra."
                );
                return;
            }
            String lower = fieldName.toLowerCase(Locale.ROOT);
            if ("the_geom".equals(lower) || "geom".equals(lower)) {
                JOptionPane.showMessageDialog(this, "No uses nombres reservados de geometria.");
                return;
            }
            if (!usedNames.add(lower)) {
                JOptionPane.showMessageDialog(this, "Hay campos repetidos: " + fieldName);
                return;
            }

            try {
                FieldConfig config = new FieldConfig(fieldName, typeName);
                config.setLength(parseNonNegativeInt(lengthValue, defaultLengthForType(typeName), "longitud", fieldName));
                config.setPrecision(parseNonNegativeInt(precisionValue, defaultPrecisionForType(typeName), "precision", fieldName));
                fields.add(config);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                return;
            }
        }

        result = new Result(
                layerName,
                file,
                normalizeGeometryKey(String.valueOf(geometryCombo.getSelectedItem())),
                fields
        );
        dispose();
    }

    private String normalizeGeometryKey(String text) {
        String normalized = text != null ? text.trim().toUpperCase(Locale.ROOT) : "";
        switch (normalized) {
            case "PUNTO":
                return "POINT";
            case "LINEA":
                return "LINE";
            case "POLIGONO":
                return "POLYGON";
            default:
                return normalized;
        }
    }

    private String sanitizeFileName(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("[\\\\/:*?\"<>|]+", "_");
    }

    private boolean isBlankCell(int row, int column) {
        Object value = fieldModel.getValueAt(row, column);
        return value == null || String.valueOf(value).trim().isBlank();
    }

    private int defaultLengthForType(String typeName) {
        String normalized = FieldConfig.normalizeTypeName(typeName);
        switch (normalized) {
            case "Integer":
                return 10;
            case "Long":
                return 18;
            case "Float":
                return 18;
            case "Double":
                return 24;
            case "Boolean":
                return 1;
            case "Date":
                return 10;
            case "Timestamp":
                return 19;
            case "String":
            default:
                return 80;
        }
    }

    private int defaultPrecisionForType(String typeName) {
        String normalized = FieldConfig.normalizeTypeName(typeName);
        switch (normalized) {
            case "Float":
                return 6;
            case "Double":
                return 8;
            default:
                return 0;
        }
    }

    private int parseNonNegativeInt(Object rawValue, int defaultValue, String label, String fieldName) {
        if (rawValue == null || String.valueOf(rawValue).trim().isBlank()) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(String.valueOf(rawValue).trim());
            if (parsed < 0) {
                throw new NumberFormatException("negativo");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor invalido de " + label + " para el campo " + fieldName + ".");
        }
    }

    public static class Result {
        private final String layerName;
        private final File file;
        private final String geometryKind;
        private final List<FieldConfig> fieldConfigs;

        Result(String layerName, File file, String geometryKind, List<FieldConfig> fieldConfigs) {
            this.layerName = layerName;
            this.file = file;
            this.geometryKind = geometryKind;
            this.fieldConfigs = fieldConfigs != null ? fieldConfigs : new ArrayList<>();
        }

        public String getLayerName() {
            return layerName;
        }

        public File getFile() {
            return file;
        }

        public String getGeometryKind() {
            return geometryKind;
        }

        public List<FieldConfig> getFieldConfigs() {
            return fieldConfigs;
        }
    }
}
