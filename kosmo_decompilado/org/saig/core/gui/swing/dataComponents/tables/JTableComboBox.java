/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.gui.swing.dataComponents.tables;

import com.vividsolutions.jump.feature.AttributeType;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JComboBox;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.gui.swing.dataComponents.DataListComponent;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.jump.lang.I18N;

public class JTableComboBox
extends JComboBox
implements DataListComponent<Record>,
ItemListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JTableComboBox.class);
    protected Table table;
    protected String field;
    protected String keyField;
    protected Object keyValue;
    protected Collator collator = Collator.getInstance(I18N.getLocale());
    protected String fieldOrdered;
    protected Filter filter;

    public JTableComboBox(Table table, String keyField, String fieldToShow) {
        this(table, keyField, fieldToShow, fieldToShow, null);
    }

    public JTableComboBox(Table table, String keyField, String fieldToShow, String fieldOrdered) {
        this(table, keyField, fieldToShow, fieldOrdered, null);
    }

    public JTableComboBox(Table table, String keyField, String fieldToShow, String fieldOrdered, Filter filter) {
        this.table = table;
        this.field = fieldToShow;
        this.keyField = keyField;
        this.fieldOrdered = fieldOrdered;
        this.filter = filter;
        this.refresh();
    }

    @Override
    public void selectItemByValue(Object key) {
        if (key != null) {
            if (this.keyField.equals(this.field)) {
                this.setSelectedItem(key);
            } else {
                List<Record> records = this.table.getByAttribute(new String[]{this.keyField}, new Object[]{key}, this.fieldOrdered, this.filter);
                if (records != null && !records.isEmpty()) {
                    Record record = records.get(0);
                    if (record.getAttribute(this.field) != null) {
                        this.setSelectedItem(record.getAttribute(this.field));
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
        if (value != null) {
            return this.table.getByAttribute(new String[]{this.field}, new Object[]{value}, this.fieldOrdered, this.filter);
        }
        return null;
    }

    @Override
    public void refresh() {
        this.removeAllItems();
        List<Record> records = null;
        records = this.keyValue == null ? this.table.getRecords(this.fieldOrdered, this.filter) : this.table.getByAttribute(new String[]{this.keyField}, new Object[]{this.keyValue}, this.fieldOrdered, this.filter);
        AbstractSet recordValues = new LinkedHashSet();
        if (CollectionUtils.isNotEmpty(records)) {
            Object fieldValue;
            AttributeType tipo = records.get(0).getSchema().getAttributeType(this.field);
            if (this.fieldOrdered == null && (tipo == AttributeType.STRING || tipo == AttributeType.VARCHAR || tipo == AttributeType.LONGVARCHAR || tipo == AttributeType.TEXT)) {
                recordValues = new TreeSet<Object>(this.collator);
            }
            for (Record element : records) {
                fieldValue = element.getAttribute(this.field);
                if (fieldValue != null) {
                    recordValues.add(fieldValue);
                    continue;
                }
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.gui.swing.dataComponents.tables.JTableComboBox.the-field-{0}-for-the-record-{1}-is-null", new Object[]{this.field, element.getPrimaryKey()}));
            }
            for (Record element : records) {
                fieldValue = element.getAttribute(this.field);
                if (fieldValue == null) {
                    LOGGER.warn((Object)I18N.getMessage("org.saig.core.gui.swing.dataComponents.tables.JTableComboBox.the-field-{0}-for-the-record-{1}-is-null", new Object[]{this.field, element.getPrimaryKey()}));
                    continue;
                }
                recordValues.add(element.getAttribute(this.field));
            }
        }
        this.addItem("----------");
        for (Object item : recordValues) {
            this.addItem(item);
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
        List<Record> records = this.table.getByAttribute(new String[]{this.field}, new Object[]{selectedItem}, this.fieldOrdered, this.filter);
        if (CollectionUtils.isEmpty(records)) {
            LOGGER.warn((Object)I18N.getMessage("org.saig.core.gui.swing.dataComponents.tables.JTableComboBox.error-while-recovering-the-associated-value-to-the-field-{0}-and-the-value{1}", new Object[]{this.field, selectedItem}));
            return null;
        }
        return records.get(0);
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
}

