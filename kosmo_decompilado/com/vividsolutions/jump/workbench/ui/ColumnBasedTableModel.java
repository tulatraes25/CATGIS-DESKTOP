/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.saig.core.model.feature.Attribute;

public abstract class ColumnBasedTableModel
implements TableModel {
    private List<Column> columns = new ArrayList<Column>();
    private List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private boolean firingEvents = true;

    protected Column getColumn(int columnIndex) {
        return this.columns.get(columnIndex);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        this.listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        this.listeners.remove(l);
    }

    public Collection<TableModelListener> getTableModelListeners() {
        return this.listeners;
    }

    @Override
    public int getColumnCount() {
        return this.columns.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return this.getColumn(columnIndex).getAttribute().getPublicName();
    }

    public int indexOfColumn(String name) {
        int i = 0;
        while (i < this.columns.size()) {
            Column column = this.columns.get(i);
            if (column.getAttribute().getPublicName().equals(name)) {
                return i;
            }
            ++i;
        }
        Assert.shouldNeverReachHere((String)name);
        return -1;
    }

    protected void setColumns(Collection<Column> columns) {
        this.columns.clear();
        this.columns.addAll(columns);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return this.getColumn(columnIndex).getDataClass();
    }

    protected void setFiringEvents(boolean firingEvents) {
        this.firingEvents = firingEvents;
    }

    protected boolean isFiringEvents() {
        return this.firingEvents;
    }

    protected void fireTableChanged(TableModelEvent e) {
        if (!this.firingEvents) {
            return;
        }
        for (TableModelListener listener : this.listeners) {
            listener.tableChanged(e);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.getColumn(columnIndex).getValueAt(rowIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        this.getColumn(columnIndex).setValueAt(aValue, rowIndex);
    }

    public abstract class Column {
        private Attribute attribute;
        private Class<?> dataClass;

        public Column(Attribute attribute, Class<?> dataClass) {
            this.attribute = attribute;
            this.dataClass = dataClass;
        }

        public Class<?> getDataClass() {
            return this.dataClass;
        }

        public abstract Object getValueAt(int var1);

        public abstract void setValueAt(Object var1, int var2);

        public Attribute getAttribute() {
            return this.attribute;
        }
    }
}

