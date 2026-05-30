/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.editors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.tables.JTableComboBox;
import org.saig.core.model.data.Table;
import org.saig.jump.widgets.util.validating.NullComboBoxValidator;

public class MultiRowJTableComboBoxEditor
extends AbstractCellEditor
implements TableCellEditor {
    private static final long serialVersionUID = 1L;
    private DefaultCellEditor[] editors;
    private int row;

    public MultiRowJTableComboBoxEditor(Table table, String keyField, String fieldToShow, String fieldOrderer, int rows, boolean allowNulls) {
        this.editors = new DefaultCellEditor[rows];
        int i = 0;
        while (i < rows) {
            JTableComboBox cBox = new JTableComboBox(table, keyField, fieldToShow, fieldOrderer);
            cBox.setSelectedItem("----------");
            int row = i;
            this.editors[row] = new DefaultCellEditor(cBox);
            cBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    MultiRowJTableComboBoxEditor.this.stopCellEditing();
                }
            });
            if (!allowNulls) {
                cBox.setInputVerifier(new NullComboBoxValidator(null, cBox));
            }
            ++i;
        }
    }

    public void setFilter(int row, Filter filter) {
        JTableComboBox cBox = (JTableComboBox)this.editors[row].getComponent();
        cBox.setFilter(filter);
        if (cBox.getValue() == null) {
            cBox.setSelectedItem("----------");
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;
        return this.editors[row].getTableCellEditorComponent(table, "", isSelected, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return ((JTableComboBox)this.editors[this.row].getComponent()).getSelectedItem();
    }

    public Object getCellEditorValue(int rowNumber) {
        return ((JTableComboBox)this.editors[rowNumber].getComponent()).getSelectedItem();
    }

    public void setRenderer(ListCellRenderer renderer) {
        if (renderer != null) {
            int i = 0;
            while (i < this.editors.length) {
                ((JTableComboBox)this.editors[i].getComponent()).setRenderer(renderer);
                ++i;
            }
        }
    }
}

