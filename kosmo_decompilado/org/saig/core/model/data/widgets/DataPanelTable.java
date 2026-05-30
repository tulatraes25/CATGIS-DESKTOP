/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooserCellEditor
 */
package org.saig.core.model.data.widgets;

import com.toedter.calendar.JDateChooserCellEditor;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.saig.core.model.data.widgets.RecordSchemaTableModel;
import org.saig.jump.widgets.util.DateNumberCellRenderer;

public class DataPanelTable
extends JTable {
    private static final long serialVersionUID = 1L;
    private final Color LIGHT_GRAY = new Color(230, 230, 230);
    private final Color LIGHT_YELLOW = new Color(255, 255, 176);
    private DateNumberCellRenderer myTableCellRenderer = new DateNumberCellRenderer();

    public DataPanelTable(RecordSchemaTableModel model) {
        this.setModel(model);
        GUIUtil.doNotRoundDoubles(this);
        this.setDefaultEditor(Date.class, (TableCellEditor)new JDateChooserCellEditor());
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        Object value;
        Class<?> columnClass = this.getModel().getColumnClass(column);
        if (columnClass != null && columnClass.equals(Boolean.class) && (value = this.getModel().getValueAt(row, column)) != null && value instanceof Boolean) {
            return new TableCellRenderer(){

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component comp = table.getDefaultRenderer(Boolean.class).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (!table.isRowSelected(row)) {
                        comp.setBackground(row % 2 == 0 ? DataPanelTable.this.LIGHT_YELLOW : DataPanelTable.this.LIGHT_GRAY);
                    }
                    return comp;
                }
            };
        }
        DateNumberCellRenderer renderer = this.myTableCellRenderer;
        if (this.isRowSelected(row)) {
            ((JComponent)renderer).setBackground(Color.YELLOW);
        } else {
            ((JComponent)renderer).setBackground(row % 2 == 0 ? this.LIGHT_YELLOW : this.LIGHT_GRAY);
        }
        return renderer;
    }
}

