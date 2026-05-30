/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import es.kosmo.desktop.gui.components.ModelComboBox;
import java.awt.Component;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ModelComboBoxCellRenderer
extends AbstractCellEditor
implements TableCellEditor,
TableCellRenderer {
    private static final long serialVersionUID = 1L;
    protected ModelComboBox combo = new ModelComboBox();
    protected Object value;

    public void init(List<?> models, String fieldToShow) {
        this.combo.init(models, fieldToShow);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.combo.refresh();
        this.combo.setSelectedItem(value);
        this.value = value;
        return this.combo;
    }

    @Override
    public Object getCellEditorValue() {
        return this.combo.getSelectedItem();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.value = value;
        JLabel label = new JLabel(value != null ? value.toString() : "");
        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
            label.setOpaque(true);
        }
        return label;
    }
}

