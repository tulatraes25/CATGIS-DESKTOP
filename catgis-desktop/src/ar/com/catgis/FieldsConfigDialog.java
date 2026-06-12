package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeatureType;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

public class FieldsConfigDialog extends JDialog {

    private static final String[] AVAILABLE_TYPES = {
            "String", "Integer", "Long", "Float", "Double", "Date", "Timestamp", "Boolean"
    };

    private final Layer layer;
    private final ShapefileData data;
    private final JTable table;
    private final DefaultTableModel model;
    private final List<String> fieldNames = new ArrayList<>();

    public FieldsConfigDialog(Window owner, Layer layer, ShapefileData data) {
        super(owner, "Ver/Editar campos: " + (layer != null ? layer.getName() : "Capa"), ModalityType.APPLICATION_MODAL);
        this.layer = layer;
        this.data = data;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        header.setBackground(new Color(248, 250, 252));

        JLabel title = new JLabel("Ver/Editar campos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(32, 42, 58));

        JLabel subtitle = new JLabel("Definí tipo lógico, alias, visibilidad y edición permitida por campo.");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11f));
        subtitle.setForeground(new Color(95, 105, 120));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "Nombre del campo", "Tipo de datos", "Nombre público", "Visibilidad", "Editable"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2 || column == 3 || column == 4;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(100, 30));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12.5f));
        table.getTableHeader().setBackground(new Color(239, 243, 247));
        table.getTableHeader().setForeground(new Color(40, 50, 65));

        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 251, 253));
                    c.setForeground(new Color(32, 42, 58));
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(0, 6, 0, 6));
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        };

        for (int i = 0; i < 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(textRenderer);
        }

        JComboBox<String> typeCombo = new JComboBox<>(AVAILABLE_TYPES);
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCombo));

        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);

        loadFields();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        footer.setBackground(new Color(248, 250, 252));

        JButton selectAllVisibleButton = new JButton("Mostrar todos");
        JButton selectAllEditableButton = new JButton("Editar todos");
        JButton applyButton = new JButton("Aplicar");
        JButton closeButton = new JButton("Cerrar");

        selectAllVisibleButton.addActionListener(e -> setBooleanColumn(3, true));
        selectAllEditableButton.addActionListener(e -> setBooleanColumn(4, true));
        applyButton.addActionListener(e -> applyChanges());
        closeButton.addActionListener(e -> dispose());

        footer.add(selectAllVisibleButton);
        footer.add(selectAllEditableButton);
        footer.add(applyButton);
        footer.add(closeButton);

        add(footer, BorderLayout.SOUTH);

        setSize(580, 320);
        setLocationRelativeTo(owner);
    }

    public static void open(Layer layer, ShapefileData data) {
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        FieldsConfigDialog dialog = new FieldsConfigDialog(owner, layer, data);
        dialog.setVisible(true);
    }

    private void loadFields() {
        model.setRowCount(0);
        fieldNames.clear();

        if (data == null || data.getFeatureCollection() == null) {
            JOptionPane.showMessageDialog(this, "La capa no tiene estructura de campos disponible.");
            return;
        }

        SimpleFeatureType schema = data.getFeatureCollection().getSchema();
        if (schema == null) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el esquema de la capa.");
            return;
        }

        schema.getAttributeDescriptors().forEach(descriptor -> {
            String fieldName = descriptor.getLocalName();
            if ("the_geom".equalsIgnoreCase(fieldName) || "geom".equalsIgnoreCase(fieldName)) {
                return;
            }

            String typeName = descriptor.getType() != null && descriptor.getType().getBinding() != null
                    ? descriptor.getType().getBinding().getSimpleName()
                    : "Object";

            FieldConfig config = layer.getOrCreateFieldConfig(fieldName, typeName);

            fieldNames.add(fieldName);
            model.addRow(new Object[]{
                    fieldName,
                    config.getTypeName(),
                    config.getPublicName(),
                    config.isVisible(),
                    config.isEditable()
            });
        });
    }

    private void setBooleanColumn(int column, boolean value) {
        for (int row = 0; row < model.getRowCount(); row++) {
            model.setValueAt(value, row, column);
        }
    }

    private void applyChanges() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            Object typeValue = model.getValueAt(i, 1);
            FieldConfig config = layer.getOrCreateFieldConfig(fieldName, typeValue != null ? String.valueOf(typeValue) : "String");

            Object publicName = model.getValueAt(i, 2);
            Object visible = model.getValueAt(i, 3);
            Object editable = model.getValueAt(i, 4);

            config.setTypeName(typeValue != null ? String.valueOf(typeValue) : "String");
            config.setPublicName(publicName != null ? String.valueOf(publicName) : fieldName);
            config.setVisible(Boolean.TRUE.equals(visible));
            config.setEditable(Boolean.TRUE.equals(editable));
        }

        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage("Configuración de campos actualizada: " + layer.getName());
        }

        dispose();
    }
}
