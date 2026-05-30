/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.TableInfoModelListener;
import org.saig.core.model.data.widgets.TableTableModel;

public class TableInfoModel {
    private Table table;
    private TableTableModel model;
    private List<TableInfoModelListener> listeners = new ArrayList<TableInfoModelListener>();

    public void dispose() {
        this.clear();
    }

    public void add(Table newTable, Collection<Record> records) {
        boolean tableNew;
        boolean bl = tableNew = this.table == null;
        if (this.model == null) {
            this.model = new TableTableModel(newTable);
        }
        this.model.addAllKeys(records);
        if (tableNew) {
            this.table = newTable;
            for (TableInfoModelListener listener : this.listeners) {
                listener.tableAdded(this.model);
            }
        }
    }

    public void addKeys(Table newTable, Collection keys) {
        boolean tableNew;
        boolean bl = tableNew = this.table == null;
        if (this.model == null) {
            this.model = new TableTableModel(newTable);
        }
        this.model.addAllKeys(keys);
        if (tableNew) {
            this.table = newTable;
            for (TableInfoModelListener listener : this.listeners) {
                listener.tableAdded(this.model);
            }
        }
    }

    public void clear() {
        this.model.dispose();
        for (TableInfoModelListener listener : this.listeners) {
            listener.tableRemoved(this.model);
        }
        this.table = null;
    }

    public void addListener(TableInfoModelListener listener) {
        this.listeners.add(listener);
    }

    public Table getTable() {
        return this.table;
    }

    public TableTableModel getTableTableModel() {
        return this.model;
    }
}

