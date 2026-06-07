package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class CadInternalLayersDialog extends JDialog {

    private final CadInternalLayersTableModel tableModel;
    private Result result = new Result(false, List.of());

    private CadInternalLayersDialog(Component owner, Layer layer, ShapefileData data) {
        super(javax.swing.JOptionPane.getFrameForComponent(owner), "Capas internas CAD", true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        header.setBackground(new Color(247, 250, 252));
        JLabel title = new JLabel("Capas internas CAD");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitle = new JLabel("<html>Controla las capas internas del archivo CAD sin romper la referencia completa. "
                + "Las capas ocultas dejan de dibujarse, de participar en zoom a capa y en la seleccion visual.</html>");
        subtitle.setForeground(new Color(75, 85, 99));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        List<EditableCadLayerRow> rows = new ArrayList<>();
        for (CadLayerSupport.InternalCadLayerInfo info : CadLayerSupport.buildInternalLayerInfo(data, layer)) {
            rows.add(new EditableCadLayerRow(info.visible(), info.name(), info.featureCount(), info.textCount(), info.entityTypes()));
        }
        tableModel = new CadInternalLayersTableModel(rows);

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(0).setMaxWidth(72);
        table.getColumnModel().getColumn(2).setMaxWidth(90);
        table.getColumnModel().getColumn(3).setMaxWidth(90);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(760, 340));
        add(scrollPane, BorderLayout.CENTER);

        JLabel footer = new JLabel(buildFooterText(rows));
        footer.setHorizontalAlignment(SwingConstants.LEFT);
        footer.setForeground(new Color(55, 65, 81));

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.add(footer, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton showAll = new JButton("Mostrar todo");
        showAll.addActionListener(e -> tableModel.setAllVisible(true));
        JButton hideAll = new JButton("Ocultar todo");
        hideAll.addActionListener(e -> tableModel.setAllVisible(false));
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> dispose());
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> {
            result = new Result(true, tableModel.hiddenLayerNames());
            dispose();
        });
        buttons.add(showAll);
        buttons.add(hideAll);
        buttons.add(cancel);
        buttons.add(apply);
        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public static Result open(Component owner, Layer layer, ShapefileData data) {
        CadInternalLayersDialog dialog = new CadInternalLayersDialog(owner, layer, data);
        dialog.setVisible(true);
        return dialog.result;
    }

    private String buildFooterText(List<EditableCadLayerRow> rows) {
        if (rows.isEmpty()) {
            return "No se detectaron capas internas CAD.";
        }
        int hidden = 0;
        int features = 0;
        for (EditableCadLayerRow row : rows) {
            if (!row.visible) {
                hidden++;
            }
            features += row.featureCount;
        }
        return "Capas internas detectadas: " + rows.size()
                + " | Elementos CAD: " + features
                + " | Ocultas al abrir: " + hidden;
    }

    public record Result(boolean approved, List<String> hiddenLayerNames) {
    }

    private static final class EditableCadLayerRow {
        private boolean visible;
        private final String name;
        private final int featureCount;
        private final int textCount;
        private final String entityTypes;

        private EditableCadLayerRow(boolean visible, String name, int featureCount, int textCount, String entityTypes) {
            this.visible = visible;
            this.name = name;
            this.featureCount = featureCount;
            this.textCount = textCount;
            this.entityTypes = entityTypes;
        }
    }

    private static final class CadInternalLayersTableModel extends AbstractTableModel {
        private final List<EditableCadLayerRow> rows;
        private final String[] columns = {"Visible", "Capa CAD", "Elementos", "Textos", "Tipos"};

        private CadInternalLayersTableModel(List<EditableCadLayerRow> rows) {
            this.rows = rows != null ? rows : new ArrayList<>();
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EditableCadLayerRow row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.visible;
                case 1 -> row.name;
                case 2 -> String.valueOf(row.featureCount);
                case 3 -> String.valueOf(row.textCount);
                case 4 -> row.entityTypes;
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != 0 || rowIndex < 0 || rowIndex >= rows.size()) {
                return;
            }
            rows.get(rowIndex).visible = Boolean.TRUE.equals(aValue);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        private void setAllVisible(boolean visible) {
            for (EditableCadLayerRow row : rows) {
                row.visible = visible;
            }
            fireTableDataChanged();
        }

        private List<String> hiddenLayerNames() {
            List<String> names = new ArrayList<>();
            for (EditableCadLayerRow row : rows) {
                if (!row.visible) {
                    names.add(row.name);
                }
            }
            return names;
        }
    }
}
