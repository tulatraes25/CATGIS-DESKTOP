/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.ColumnBasedTableModel;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import javax.swing.event.TableModelEvent;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.LRUCache;
import org.saig.jump.lang.I18N;

public class TableTableModel
extends ColumnBasedTableModel {
    private static final Logger LOGGER = Logger.getLogger(LayerTableModel.class);
    public static final String GEOMETRY_COLUMN_NAME = "....";
    private Table table;
    private List keys = new ArrayList();
    private LRUCache cache = new LRUCache(100);
    private String sortedColumnName = null;
    private String pkName = null;
    private boolean sortAscending = false;
    private FeatureSchema schema;
    private boolean isSorted;

    public TableTableModel(Table table) {
        this.table = table;
        this.initColumns(table);
    }

    public Object getKey(int row) {
        return this.keys.get(row);
    }

    public int getRow(Object value) {
        return this.keys.indexOf(value);
    }

    public void initColumns(Table table) {
        this.schema = table.getSchema();
        this.pkName = this.schema.getPrimaryKeyName();
        ArrayList<ColumnBasedTableModel.Column> columns = new ArrayList<ColumnBasedTableModel.Column>();
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.schema.getVisibility(i).booleanValue() && this.schema.getAttributeType(i) != AttributeType.GEOMETRY) {
                final int j = i;
                columns.add(new MyColumn(this.schema.getAttribute(i), this.schema.getAttributeType(i).toJavaClass()){

                    @Override
                    protected Object getValue(Record record) {
                        if (record == null) {
                            return null;
                        }
                        return record.getAttribute(j);
                    }

                    @Override
                    protected void setValue(Object value, Record record) {
                        Object oldValue = record.getAttribute(j);
                        if (oldValue == null && value == null || oldValue != null && oldValue.equals(value)) {
                            return;
                        }
                        Record oldAttributes = (Record)record.clone();
                        Record newAttributes = (Record)record.clone();
                        newAttributes.setAttribute(j, value);
                    }
                });
            }
            ++i;
        }
        this.setColumns(columns);
    }

    private void setAttributesOf(Record record, Record attributes) throws Exception {
        int i = 0;
        while (i < record.getSchema().getAttributeCount()) {
            record.setAttribute(i, attributes.getAttribute(i));
            ++i;
        }
        this.table.update(record);
        this.table.fireTableChanged();
    }

    public Table getTable() {
        return this.table;
    }

    public Record getRecord(int row) {
        int i;
        Object key = this.keys.get(row);
        if (key instanceof Record) {
            return (Record)key;
        }
        Object value = this.cache.get(key);
        if (value != null) {
            return (Record)value;
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
        List<Record> records = this.table.getByPrimaryKeys(keys_.toArray());
        for (Record currentRecord : records) {
            if (currentRecord == null) continue;
            this.cache.add(currentRecord.getPrimaryKey(), currentRecord);
        }
        return record;
    }

    @Override
    public int getRowCount() {
        return this.keys.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    private boolean checkVersionableColumn(Table table, String attrName) {
        return attrName.equals(table.getEndDateField()) || attrName.equals(table.getStartDateField()) || attrName.equals(table.getHistoryField());
    }

    public void clear(boolean fireEvents) {
        this.keys.clear();
        this.cache.clear();
        if (fireEvents) {
            this.fireTableChanged(new TableModelEvent(this));
        }
    }

    public void clear() {
        this.clear(true);
    }

    public void removeAll(Collection<Record> recordsToRemove) {
        for (Record record : recordsToRemove) {
            int row = -1;
            if (this.keys.contains(record)) {
                row = this.keys.indexOf(record);
                this.keys.remove(record);
            } else {
                Object key = record.getPrimaryKey();
                row = this.keys.indexOf(key);
                if (row == -1) continue;
                this.keys.remove(key);
            }
            this.fireTableChanged(new TableModelEvent(this, row, row, -1, -1));
        }
    }

    public void addAll(Collection<Record> records) {
        int originalFeaturesSize = this.keys.size();
        ArrayList<Object> keys_ = new ArrayList<Object>();
        for (Record element : records) {
            if (element.isUnsaved()) {
                keys_.add(element);
                continue;
            }
            keys_.add(element.getPrimaryKey());
        }
        this.keys.addAll(keys_);
        if (this.sortedColumnName != null) {
            this.sort(this.sortedColumnName, this.sortAscending);
        }
        this.fireTableChanged(new TableModelEvent(this, originalFeaturesSize, this.keys.size() - 1, -1, 1));
    }

    public void addAllKeys(Collection newKeys) {
        int originalFeaturesSize = this.keys.size();
        this.keys.addAll(newKeys);
        if (this.sortedColumnName != null) {
            this.sort(this.sortedColumnName, this.sortAscending);
        }
        this.fireTableChanged(new TableModelEvent(this, originalFeaturesSize, this.keys.size() - 1, -1, 1));
    }

    public void dispose() {
        this.keys.clear();
    }

    public String getSortedColumnName() {
        return this.sortedColumnName;
    }

    public boolean isSortAscending() {
        return this.sortAscending;
    }

    public void sortSelectedRows(final boolean ascending, int[] selectedRows) {
        final Hashtable indexes = new Hashtable();
        int i = 0;
        while (i < selectedRows.length) {
            indexes.put(this.keys.get(selectedRows[i]), "");
            ++i;
        }
        Collections.sort(this.keys, new Comparator(){

            public int compare(Object o1, Object o2) {
                return this.ascendingCompare(o1, o2) * (ascending ? 1 : -1);
            }

            private int ascendingCompare(Object key1, Object key2) {
                boolean check1 = indexes.containsKey(key1);
                boolean check2 = indexes.containsKey(key2);
                if (check1 && check2 || !check1 && !check2) {
                    return 0;
                }
                if (!check1) {
                    return 1;
                }
                return -1;
            }
        });
        this.isSorted = true;
    }

    public void sort(String columnName) {
        String name;
        Attribute attr = this.schema.getPublicAttribute(columnName);
        this.sort(name, (name = attr.getName()).equals(this.sortedColumnName) ? !this.sortAscending : true);
    }

    public void explicitSort(String columnName, boolean ascending) {
        Attribute attr = this.schema.getPublicAttribute(columnName);
        String name = attr.getName();
        this.sort(name, ascending);
    }

    private void sort(String columnName, boolean ascending) {
        this.sortAscending = ascending;
        this.sortedColumnName = columnName;
        FeatureSchema schema = this.table.getSchema();
        if (!schema.hasAttribute(columnName)) {
            return;
        }
        long size = -1L;
        try {
            size = this.table.getDataSource().size();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if ((long)this.keys.size() == size) {
            LOGGER.debug((Object)I18N.getString(this.getClass(), "retrieving-all-keys"));
            this.keys = this.table.getSortKeys(columnName, ascending);
        } else {
            this.keys = this.table.getSortKeys(columnName, ascending);
        }
    }

    public String getType(int column) {
        return null;
    }

    public boolean isPKName(String currentColumn) {
        return currentColumn.equals(this.pkName);
    }

    public boolean isSort() {
        return this.sortedColumnName != null || this.isSorted;
    }

    public void updateAll(Collection<Record> records, Collection<Record> oldRecordClones) {
        for (Record currentRecord : records) {
            int index;
            if (!currentRecord.isUnsaved() || (index = this.keys.indexOf(currentRecord)) == -1) continue;
            this.keys.set(index, currentRecord);
        }
    }

    private abstract class MyColumn
    extends ColumnBasedTableModel.Column {
        public MyColumn(Attribute attribute, Class dataClass) {
            super(attribute, dataClass);
        }

        @Override
        public Object getValueAt(int rowIndex) {
            return this.getValue(TableTableModel.this.getRecord(rowIndex));
        }

        @Override
        public void setValueAt(Object value, int rowIndex) {
            this.setValue(value, TableTableModel.this.getRecord(rowIndex));
        }

        protected abstract Object getValue(Record var1);

        protected abstract void setValue(Object var1, Record var2);
    }
}

