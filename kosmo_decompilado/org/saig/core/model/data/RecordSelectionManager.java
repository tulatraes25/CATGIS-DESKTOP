/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordSelectionListener;
import org.saig.core.model.data.Table;

public class RecordSelectionManager {
    private Map<Table, Collection<Record>> tableToSelectedRecordsMap = new HashMap<Table, Collection<Record>>();
    private List<RecordSelectionListener> listeners = new ArrayList<RecordSelectionListener>();

    public void selectRecords(Table table, Collection<Record> recordsToSelect) {
        if (this.tableToSelectedRecordsMap.containsKey(table)) {
            this.tableToSelectedRecordsMap.put(table, new HashSet<Record>(recordsToSelect));
        } else {
            this.tableToSelectedRecordsMap.put(table, new HashSet<Record>(recordsToSelect));
        }
        this.fireRecordSelectionChanged();
    }

    public void unselectRecords(Table table, Collection<Record> recordsToUnselect) {
        if (this.tableToSelectedRecordsMap.containsKey(table)) {
            Collection<Record> recordsSelected = this.tableToSelectedRecordsMap.get(table);
            recordsSelected.removeAll(recordsToUnselect);
        }
        this.fireRecordSelectionChanged();
    }

    public void clearSelection(boolean fireSelectionChanged) {
        this.tableToSelectedRecordsMap.clear();
        if (fireSelectionChanged) {
            this.fireRecordSelectionChanged();
        }
    }

    public void clearSelection(Table table, boolean fireSelectionChanged) {
        this.tableToSelectedRecordsMap.remove(table);
        if (fireSelectionChanged) {
            this.fireRecordSelectionChanged();
        }
    }

    public void fireRecordSelectionChanged() {
        for (RecordSelectionListener listener : new ArrayList<RecordSelectionListener>(this.listeners)) {
            listener.selectionChanged();
        }
    }

    public void addSelectionListener(RecordSelectionListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeSelectionListener(RecordSelectionListener listener) {
        this.listeners.remove(listener);
    }

    public Collection<Record> getRecordSelection(Table table) {
        return this.tableToSelectedRecordsMap.get(table);
    }
}

