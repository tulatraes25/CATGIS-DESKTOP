/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.util.LRUCache;

public class RecordTableModelWithDataSource
extends AbstractTableModel {
    private String[] columnNames = null;
    private List keys = new ArrayList();
    private TableRecordDataSource table;
    private String sortedColumnName = null;
    private boolean sortAscending = false;
    private String pkName = null;
    private LRUCache cache = new LRUCache(100);

    public RecordTableModelWithDataSource(TableRecordDataSource recordCollection) {
        this.table = recordCollection;
        this.initialize();
    }

    public void initialize() {
        this.keys = this.table.getOrderedPrimaryKeyList();
        FeatureSchema schema = this.table.getSchema();
        this.pkName = schema.getPrimaryKeyName();
        this.columnNames = new String[schema.getAttributeCount()];
        int i = 0;
        while (i < schema.getAttributeCount()) {
            this.columnNames[i] = schema.getAttribute(i).getPublicName();
            ++i;
        }
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public int getRowCount() {
        return (int)this.table.size();
    }

    @Override
    public String getColumnName(int col) {
        return this.columnNames[col];
    }

    public void sort(String columnName) {
        this.sort(columnName, columnName.equals(this.sortedColumnName) ? !this.sortAscending : true);
    }

    public void sort(String columnName, boolean ascending) {
        this.sortAscending = ascending;
        this.sortedColumnName = columnName;
        this.keys = this.table.getSortKeys(columnName, ascending);
    }

    @Override
    public synchronized Object getValueAt(int row, int col) {
        int i;
        Object key = this.keys.get(row);
        Object value = this.cache.get(key);
        if (value != null) {
            return ((Record)value).getAttribute(col);
        }
        ArrayList keys_ = new ArrayList();
        if (row == 0) {
            i = 1;
            while (i < 100 && i < this.keys.size()) {
                keys_.add(this.keys.get(i));
                ++i;
            }
        } else if (row == this.keys.size()) {
            i = this.keys.size();
            while (i < 100 && i > 0) {
                keys_.add(this.keys.get(i));
                ++i;
            }
        } else {
            i = 1;
            while (i < 50 && row - i > 0) {
                keys_.add(this.keys.get(row - i));
                ++i;
            }
            int size = this.keys.size();
            int i2 = 1;
            while (i2 < 50 && row + i2 < size) {
                keys_.add(this.keys.get(row + i2));
                ++i2;
            }
        }
        Record record = this.table.getByPrimaryKey(key);
        this.cache.add(key, record);
        List<Record> features = this.table.getByPrimaryKey(keys_.toArray());
        for (Record element : features) {
            this.cache.add(element.getPrimaryKey(), element);
        }
        return record.getAttribute(col);
    }

    public Class getColumnClass(int c) {
        return this.table.getSchema().getAttributeType(c).toJavaClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public int getPrimaryKeyIndex() {
        FeatureSchema schema = this.table.getSchema();
        String pkName = schema.getPrimaryKeyName();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            if (this.columnNames[i].equals(pkName)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        this.fireTableCellUpdated(row, col);
    }

    public Object getSortedColumnName() {
        return this.sortedColumnName;
    }

    public boolean isSortAscending() {
        return this.sortAscending;
    }

    public void clear() {
        this.keys.clear();
        this.cache.clear();
        this.keys = this.table.getOrderedPrimaryKeyList();
    }

    public boolean isPKName(String currentColumn) {
        return currentColumn.equals(this.pkName);
    }
}

