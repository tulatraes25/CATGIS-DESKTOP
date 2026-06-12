package ar.com.catgis.core.model;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UI panel for editing Data-Defined Overrides.
 * <p>
 * Provides a table-based editor for property→expression mappings.
 * Can be embedded in layer properties or symbology dialogs.
 * </p>
 */
public class DataDefinedOverridesPanel extends JPanel {

    private final DataDefinedOverrides model;
    private final JTable table;
    private final OverrideTableModel tableModel;
    private final JButton addButton;
    private final JButton removeButton;
    private final List<String> availableProperties;

    public DataDefinedOverridesPanel(DataDefinedOverrides model, List<String> availableProperties) {
        this.model = model;
        this.availableProperties = availableProperties != null ? availableProperties : new ArrayList<>();
        setLayout(new BorderLayout(4, 4));
        setBorder(new TitledBorder("Data-Defined Overrides"));

        // Table
        tableModel = new OverrideTableModel();
        table = new JTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(400, 120));
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        addButton = new JButton("+ Add");
        removeButton = new JButton("- Remove");
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Actions
        addButton.addActionListener(e -> addOverride());
        removeButton.addActionListener(e -> removeOverride());

        refreshFromModel();
    }

    private void addOverride() {
        String property = null;
        if (!availableProperties.isEmpty()) {
            property = (String) JOptionPane.showInputDialog(
                    this, "Select property:", "Add Override",
                    JOptionPane.PLAIN_MESSAGE, null,
                    availableProperties.toArray(),
                    availableProperties.get(0));
        } else {
            property = JOptionPane.showInputDialog(this, "Property name:", "Add Override");
        }
        if (property == null || property.isBlank()) return;

        String expression = JOptionPane.showInputDialog(this,
                "Expression (e.g., [population] > 1000 ? 14 : 10):",
                "Expression for " + property);
        if (expression == null || expression.isBlank()) return;

        model.addOverride(property, expression);
        refreshFromModel();
    }

    private void removeOverride() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String property = tableModel.getValueAt(row, 0).toString();
            model.removeOverride(property);
            refreshFromModel();
        }
    }

    public void refreshFromModel() {
        tableModel.setEntries(new ArrayList<>(model.getOverrides().entrySet()));
    }

    public DataDefinedOverrides getModel() {
        return model;
    }

    // --- Table model ---

    private static class OverrideTableModel extends AbstractTableModel {
        private final String[] columns = {"Property", "Expression"};
        private List<Map.Entry<String, String>> entries = new ArrayList<>();

        void setEntries(List<Map.Entry<String, String>> entries) {
            this.entries = entries;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return entries.size(); }

        @Override
        public int getColumnCount() { return 2; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            var entry = entries.get(row);
            return col == 0 ? entry.getKey() : entry.getValue();
        }
    }
}
