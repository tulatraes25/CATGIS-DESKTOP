/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.core.gui.swing.dataComponents.tables;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JComboBox;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListWithPatternComponent;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.jump.lang.I18N;

public class JTablePatternWithDataSourceComboBox
extends JComboBox
implements DataListWithPatternComponent<Record>,
ItemListener {
    private static final long serialVersionUID = 1L;
    private TableRecordDataSource table;
    private String[] fields;
    private String keyField;
    private Object keyValue;
    private Collator collator = Collator.getInstance(I18N.getLocale());
    private String fieldOrdered;
    private Filter filter;
    private String pattern;
    private Map<String, Object> valuesToKey;

    public JTablePatternWithDataSourceComboBox(TableRecordDataSource table, String keyField, String[] fields, String fieldOrdered, Filter filter, String pattern) {
        this.table = table;
        this.fields = fields;
        this.keyField = keyField;
        this.fieldOrdered = fieldOrdered;
        this.filter = filter;
        this.pattern = pattern != null ? pattern : this.getDefaultPattern(fields.length);
        this.valuesToKey = new HashMap<String, Object>();
        this.refresh();
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            if (this.fields != null && this.fields.length == 1 && this.keyField.equals(this.fields[0])) {
                this.setSelectedItem(key);
            } else {
                List<Record> records = this.table.getByAttribute(new String[]{this.keyField}, new Object[]{key}, this.fieldOrdered, this.filter);
                if (records != null && !records.isEmpty()) {
                    Record record = records.get(0);
                    String value = this.applyPattern(this.getValuesOfFieldsToShow(record));
                    if (record != null && value != null) {
                        this.setSelectedItem(value);
                    } else {
                        this.setSelectedItem("----------");
                    }
                } else {
                    this.setSelectedItem("----------");
                }
            }
        } else {
            this.setSelectedItem("----------");
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
        this.removeAllItems();
        this.valuesToKey.clear();
        List<Record> records = null;
        records = this.keyValue == null ? this.table.getRecords(this.fieldOrdered, this.filter) : this.table.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, this.fieldOrdered, this.filter);
        Vector<String> recordValues = new Vector<String>();
        if (CollectionUtils.isNotEmpty(records)) {
            for (Record record : records) {
                String valueToShow = this.applyPattern(this.getValuesOfFieldsToShow(record));
                this.valuesToKey.put(valueToShow, record.getPrimaryKey());
                recordValues.add(valueToShow);
            }
            Collections.sort(recordValues, this.collator);
        }
        this.addItem("----------");
        for (Record record : recordValues) {
            this.addItem(record);
        }
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
        Object selectedItem = this.getSelectedItem();
        if (selectedItem == null || selectedItem.equals("----------")) {
            return null;
        }
        Object key = this.valuesToKey.get(selectedItem);
        return this.table.getByPrimaryKey(key);
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
        this.setSelectedItem("----------");
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
        this.refresh();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (this.getInputVerifier() != null) {
            this.getInputVerifier().verify(this);
        }
    }

    @Override
    public List<Record> getValues() {
        ArrayList<Record> values = new ArrayList<Record>();
        values.add(this.getValue());
        return values;
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

