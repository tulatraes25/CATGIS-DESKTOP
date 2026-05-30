/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
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
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataHistoryComponent;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.jump.lang.I18N;

public class JTableHistoryWithDataSourceListScrollPane
extends JScrollPane
implements DataHistoryComponent<Record> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JTableHistoryWithDataSourceListScrollPane.class);
    private Object historyKeyValue;
    private TableDBRecordDataSource table;
    private String[] fields;
    private JList list;
    private Filter filter;
    private Collator collator = Collator.getInstance(I18N.getLocale());
    private String pattern;
    private Map<String, Object> valuesToKey;

    public JTableHistoryWithDataSourceListScrollPane(TableDBRecordDataSource table, String[] fields, int rows, int cols, String pattern, Object historyKeyValue) {
        super(22, 31);
        this.table = table;
        this.fields = fields;
        this.list = new JList();
        this.setMinimumSize(new Dimension(rows, cols));
        this.setPreferredSize(new Dimension(rows, cols));
        this.setViewportView(this.list);
        this.pattern = pattern != null ? pattern : this.getDefaultPattern(fields.length);
        this.valuesToKey = new HashMap<String, Object>();
        this.historyKeyValue = historyKeyValue;
        this.refresh();
    }

    @Override
    public void clear() {
        this.list.removeAll();
        this.historyKeyValue = null;
        this.list.setListData(new Vector());
    }

    @Override
    public Record getValue() {
        Object selectedItem = this.list.getSelectedValue();
        Object key = this.valuesToKey.get(selectedItem);
        return this.table.getByPrimaryKey(key);
    }

    public Object getKeyValue() {
        Record record = this.getValue();
        if (record == null) {
            return record;
        }
        return record.getPrimaryKey();
    }

    @Override
    public void refresh() {
        this.list.removeAll();
        this.valuesToKey.clear();
        List<Object> records = new ArrayList();
        try {
            records = this.table.getHistoryOfElement(this.historyKeyValue, this.filter);
            if (records.isEmpty()) {
                this.list.setListData(new Vector());
                return;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        Vector<String> recordValues = new Vector<String>();
        if (records != null && records.size() > 0) {
            for (Record record : records) {
                String valueToShow = this.applyPattern(this.getValuesOfFieldsToShow(record));
                this.valuesToKey.put(valueToShow, record.getPrimaryKey());
                recordValues.add(valueToShow);
            }
            Collections.sort(recordValues, this.collator);
        }
        this.list.setListData(recordValues);
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Object getHistoryKeyValue() {
        return this.historyKeyValue;
    }

    @Override
    public List<Record> getHistoryOfElement() {
        try {
            return this.table.getHistoryOfElement(this.historyKeyValue, this.filter);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return new ArrayList<Record>();
        }
    }

    @Override
    public void setHistoryKeyValue(Object key) {
        this.historyKeyValue = key;
        this.refresh();
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

    public JList getList() {
        return this.list;
    }
}

