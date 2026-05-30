/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.gui.swing.dataComponents.tables;

import java.awt.Dimension;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListWithPatternComponent;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.jump.lang.I18N;

public class JTablePatternWithDataSourceListScrollPane
extends JScrollPane
implements DataListWithPatternComponent<Record> {
    private static final long serialVersionUID = 1L;
    private TableDBRecordDataSource table;
    private String[] fields;
    private String keyField;
    private Object keyValue;
    private JList list;
    private String fieldOrdered;
    private Filter filter;
    private Collator collator = Collator.getInstance(I18N.getLocale());
    private String pattern;
    private Map<String, Object> valuesToKey;

    public JTablePatternWithDataSourceListScrollPane(TableDBRecordDataSource table, String[] fields, String keyField, int rows, int cols, String fieldOrdered, String pattern) {
        super(22, 31);
        this.table = table;
        this.fields = fields;
        this.keyField = keyField;
        this.fieldOrdered = fieldOrdered;
        this.list = new JList();
        this.setMinimumSize(new Dimension(rows, cols));
        this.setPreferredSize(new Dimension(rows, cols));
        this.setViewportView(this.list);
        this.pattern = pattern != null ? pattern : this.getDefaultPattern(fields.length);
        this.valuesToKey = new HashMap<String, Object>();
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
        return this.getRowsByValues(new Object[]{value});
    }

    @Override
    public List<Record> getRowsByValues(Object[] values) {
        if (values != null) {
            ArrayList<Object> keys = new ArrayList<Object>();
            int i = 0;
            while (i < values.length) {
                keys.add(this.valuesToKey.get(values[i]));
                ++i;
            }
            return this.table.getByPrimaryKey(keys.toArray());
        }
        return null;
    }

    @Override
    public void refresh() {
        this.list.removeAll();
        this.valuesToKey.clear();
        List<Record> records = null;
        records = this.keyValue == null ? this.table.getRecords(this.fieldOrdered, this.filter) : this.table.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, this.fieldOrdered, this.filter);
        if (CollectionUtils.isEmpty(records)) {
            this.list.setListData(new Vector());
            return;
        }
        Vector<String> recordValues = new Vector<String>();
        for (Record element : records) {
            String valueToShow = this.applyPattern(this.getValuesOfFieldsToShow(element));
            this.valuesToKey.put(valueToShow, element.getPrimaryKey());
            recordValues.add(valueToShow);
        }
        Collections.sort(recordValues, this.collator);
        this.list.setListData(recordValues);
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
        Object key = this.valuesToKey.get(selectedItem);
        return this.table.getByPrimaryKey(key);
    }

    @Override
    public List<Record> getValues() {
        ArrayList<Record> values = new ArrayList<Record>();
        Object[] selectedValues = this.list.getSelectedValues();
        int i = 0;
        while (i < selectedValues.length) {
            Object selectedItem = selectedValues[i];
            Object key = this.valuesToKey.get(selectedItem);
            values.add(this.table.getByPrimaryKey(key));
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
        this.refresh();
    }

    @Override
    public String applyPattern(Object[] values) {
        MessageFormat mf = new MessageFormat(this.pattern);
        return mf.format(values);
    }

    private String getDefaultPattern(int length) {
        String defaultPattern = "{0}";
        int i = 1;
        while (i < length) {
            defaultPattern = String.valueOf(defaultPattern) + " - {" + i + "}";
            ++i;
        }
        return defaultPattern;
    }

    private Object[] getValuesOfFieldsToShow(Record feat) {
        Object[] values = new Object[this.fields.length];
        int i = 0;
        while (i < this.fields.length) {
            values[i] = feat.getAttribute(this.fields[i]);
            ++i;
        }
        return values;
    }
}

