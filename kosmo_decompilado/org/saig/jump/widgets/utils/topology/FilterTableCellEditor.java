/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils.topology;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.saig.core.filter.Filter;
import org.saig.jump.widgets.utils.topology.FilterTableCellRenderer;

public class FilterTableCellEditor
extends AbstractCellEditor
implements TableCellEditor {
    private static final long serialVersionUID = 1L;
    private FilterTableCellRenderer cellRenderer = new FilterTableCellRenderer(this);
    private Filter currentFilter;

    @Override
    public Object getCellEditorValue() {
        return this.cellRenderer.getFilter();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.currentFilter = (Filter)value;
        this.cellRenderer.setFilter(this.currentFilter);
        return this.cellRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }
}

