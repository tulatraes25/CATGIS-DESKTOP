/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.gui.swing.dataComponents.tables;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListComponent;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;

public class JTableListScrollPane
extends JScrollPane
implements DataListComponent<Record> {
    private static final long serialVersionUID = 1L;
    private Table table;
    private String field;
    private String keyField;
    private Object keyValue;
    private JList list;
    private String fieldOrdered;
    private Filter filter;

    public JTableListScrollPane(Table table, String field, String keyField, int width, int height) {
        this(table, field, keyField, width, height, null);
    }

    public JTableListScrollPane(Table table, String field, String keyField, int width, int height, String fieldOrdered) {
        super(22, 31);
        this.table = table;
        this.field = field;
        this.keyField = keyField;
        this.fieldOrdered = fieldOrdered;
        this.list = new JList();
        this.setMinimumSize(new Dimension(width, height));
        this.setPreferredSize(new Dimension(width, height));
        this.setViewportView(this.list);
        this.refresh();
    }

    public JList getList() {
        return this.list;
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            this.list.setSelectedValue(key, true);
        }
    }

    @Override
    public List<Record> getRowsByValue(Object value) {
        if (value != null) {
            return this.table.getByAttribute(new String[]{this.field}, new Object[]{value}, this.fieldOrdered, this.filter);
        }
        return null;
    }

    @Override
    public void refresh() {
        this.list.removeAll();
        List<Record> records = null;
        if (this.keyValue == null) {
            this.list.setListData(new Vector());
            return;
        }
        records = this.table.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, this.fieldOrdered, this.filter);
        if (CollectionUtils.isEmpty(records)) {
            this.list.setListData(new Vector());
            return;
        }
        Object[] values = new Object[records.size()];
        int cont = 0;
        for (Record element : records) {
            values[cont] = element.getAttribute(this.field);
            ++cont;
        }
        this.list.setListData(values);
    }

    @Override
    public Object getKeyValue() {
        Record record = this.getValue();
        if (record == null) {
            return record;
        }
        return record.getAttribute(this.keyField);
    }

    @Override
    public Record getValue() {
        Object selectedItem = this.list.getSelectedValue();
        List<Record> records = this.table.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, this.fieldOrdered, this.filter);
        return records.get(0);
    }

    @Override
    public List<Record> getValues() {
        ArrayList<Record> values = new ArrayList<Record>();
        Object[] selectedValues = this.list.getSelectedValues();
        int i = 0;
        while (i < selectedValues.length) {
            Object selectedItem = selectedValues[i];
            List<Record> records = this.table.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, this.fieldOrdered, this.filter);
            values.add(records.get(0));
            ++i;
        }
        return values;
    }

    public void setKeyValue(Object keyValue) {
        this.keyValue = keyValue;
        this.refresh();
    }

    @Override
    public Record getValueByKey(Object key) {
        List<Record> records = this.table.getByAttribute(new String[]{this.keyField}, new Object[]{key}, this.fieldOrdered, this.filter);
        if (CollectionUtils.isEmpty(records)) {
            return null;
        }
        return records.get(0);
    }

    @Override
    public void clear() {
        this.list.removeAll();
        this.keyValue = null;
        this.list.setListData(new Vector());
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}

