/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeTableModel;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class WFSAttributeComboboxEditor
extends AbstractCellEditor
implements TableCellEditor {
    private static final long serialVersionUID = 1L;
    private JComboBox columnCombobox;

    @Override
    public Object getCellEditorValue() {
        return this.columnCombobox.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        WFSFeatureTypeTableModel model = (WFSFeatureTypeTableModel)table.getModel();
        this.columnCombobox = new JComboBox<Object>(model.getValuesFor(row, column));
        this.columnCombobox.setRenderer(model.getCellRendererFor(row, column));
        if (this.columnCombobox.getModel().getSize() == 1) {
            this.columnCombobox.setSelectedIndex(0);
        }
        return this.columnCombobox;
    }
}

