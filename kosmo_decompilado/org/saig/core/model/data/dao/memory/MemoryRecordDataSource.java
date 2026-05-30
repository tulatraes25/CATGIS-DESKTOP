/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.memory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.memory.iterators.TableInMemoryIterator;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.core.util.RecordComparatorByFieldNames;
import org.saig.jump.lang.I18N;

public class MemoryRecordDataSource
extends TableRecordDataSource {
    private static final Logger LOGGER = Logger.getLogger(MemoryRecordDataSource.class);
    public static final String ID = "Memory";
    private List<Record> records = new ArrayList<Record>();
    private String name;

    public MemoryRecordDataSource(Collection<Record> newRecords, FeatureSchema schema) {
        this.schema = schema;
        this.inMemory = true;
        if (newRecords != null) {
            for (Record record : newRecords) {
                Long key = new Long(this.records.size());
                record.setAttribute(schema.getPrimaryKeyIndex(), (Object)key);
                this.records.add(record);
            }
        }
    }

    public void clear() {
        this.records = new ArrayList<Record>();
        this.newRecords.clear();
        this.updateRecords.clear();
        this.deleteRecords.clear();
    }

    @Override
    public void add(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.addAll(records);
    }

    public void addWithPK(Record record) {
        this.records.add(record);
    }

    @Override
    public void addAll(Collection<Record> records) throws Exception {
        if (this.inMemory) {
            for (Record element : records) {
                if (element.isUnsaved()) {
                    this.newRecords.add(element);
                } else {
                    this.updateRecords.add(element);
                }
                if (!this.deleteRecords.contains(element)) continue;
                this.deleteRecords.remove(element);
            }
            return;
        }
    }

    private Record getRecordOfSet(Record record, Set<Record> recordsSet) {
        for (Record setRecord : recordsSet) {
            if (!setRecord.equals(record)) continue;
            return record;
        }
        return null;
    }

    @Override
    public void commit() throws Exception {
        Long key;
        this.inMemory = false;
        int index = 0;
        ArrayList<Record> resultados = new ArrayList<Record>();
        while (!this.records.isEmpty()) {
            Record record = this.records.get(0);
            try {
                if (this.updateRecords.contains(record)) {
                    Record updateRecord = this.getRecordOfSet(record, this.updateRecords);
                    key = new Long(index);
                    updateRecord.setAttribute(this.schema.getPrimaryKeyIndex(), (Object)key);
                    ++index;
                    resultados.add(updateRecord);
                    continue;
                }
                if (this.deleteRecords.contains(record)) continue;
                Long key2 = new Long(index);
                record.setAttribute(this.schema.getPrimaryKeyIndex(), (Object)key2);
                ++index;
                resultados.add(record);
            }
            finally {
                this.records.remove(0);
            }
        }
        for (Record record : this.newRecords) {
            key = new Long(index);
            record.setAttribute(this.schema.getPrimaryKeyIndex(), (Object)key);
            ++index;
            resultados.add(record);
        }
        this.updateRecords.clear();
        this.deleteRecords.clear();
        this.newRecords.clear();
        this.records = resultados;
        this.inMemory = true;
    }

    @Override
    public boolean createDataStore(Envelope vista, String geomColumn, int srid) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void executeQuery(String sqlQuery) throws SQLException {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public List<Record> getByAttribute(String[] fields, Object[] values) {
        return this.getByAttribute(fields, values, null, null, true);
    }

    @Override
    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField) {
        return this.getByAttribute(fields, values, orderField, null, true);
    }

    @Override
    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField, Filter filter) {
        return this.getByAttribute(fields, values, orderField, filter, true);
    }

    @Override
    public List<Record> getByAttribute(String[] names, Object[] values, String orderField, Filter filter, boolean ascending) {
        if (names == null || values == null || names.length != values.length) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.model.data.dao.memory.MemoryRecordDataSource.Names-and-value-can-not-be-null-and-must-have-the-same-size"));
        }
        ArrayList<Record> resultado = new ArrayList<Record>();
        ITableIterator it = null;
        try {
            it = this.getIterator(filter);
            while (it.hasNext()) {
                Record record = it.next();
                boolean condition = true;
                int i = 0;
                while (i < names.length && condition) {
                    condition = record.getAttribute(names[i]).equals(values[i]);
                    ++i;
                }
                if (!condition) continue;
                resultado.add(record);
            }
            if (orderField != null && !orderField.isEmpty()) {
                ArrayList<String> campos = new ArrayList<String>();
                campos.add(orderField);
                RecordComparatorByFieldNames comp = new RecordComparatorByFieldNames(campos, true, false, ascending, true);
                Collections.sort(resultado, comp);
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return resultado;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Record getByPrimaryKey(Object key) {
        if (key == null) {
            return null;
        }
        if (key instanceof Record) {
            return (Record)key;
        }
        boolean encontrado = false;
        Record rec = null;
        ITableIterator it = null;
        try {
            it = this.getIterator();
            while (it.hasNext() && !encontrado) {
                rec = it.next();
                encontrado = key.equals(rec.getPrimaryKey());
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        if (encontrado) return rec;
        return null;
    }

    @Override
    public List<Record> getByPrimaryKey(Object[] keys) {
        ArrayList<Record> result = new ArrayList<Record>();
        HashSet<Object> keySet = new HashSet<Object>(Arrays.asList(keys));
        ITableIterator it = null;
        try {
            it = this.getIterator();
            while (it.hasNext()) {
                Record rec = it.next();
                if (!keySet.contains(rec.getPrimaryKey())) continue;
                result.add(rec);
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return result;
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.getDistintsValues(field, Integer.MAX_VALUE);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Set<Object> getDistintsValues(String field, int limite) {
        TreeSet<Object> values = new TreeSet<Object>();
        ITableIterator it = null;
        int cont = 0;
        try {
            try {
                it = this.getIterator();
                while (it.hasNext()) {
                    if (cont >= limite) {
                        return values;
                    }
                    Object value = it.next().getAttribute(field);
                    if (value == null) continue;
                    values.add(value);
                    ++cont;
                }
                return values;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it == null) return values;
                it.close();
                return values;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public List<Object> getFieldValue(String field, String fieldKey, Object value) {
        if (!this.schema.hasAttribute(field)) {
            return null;
        }
        ArrayList<Object> resultados = new ArrayList<Object>();
        ITableIterator it = null;
        try {
            try {
                it = this.getIterator();
                while (it.hasNext()) {
                    Record record = it.next();
                    Object result = record.getAttribute(field);
                    if (result == null) continue;
                    resultados.add(result);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return resultados;
    }

    @Override
    public List<Record> getHistoryOfElement(Object pkId, Filter filter) throws Exception {
        throw new I18NUnsupportedOperationException(I18N.getString(this.getClass(), "operation-not-supported"));
    }

    @Override
    public ITableIterator getIterator() {
        return this.getIterator(null);
    }

    public ITableIterator getIterator(Filter filter) {
        return new TableInMemoryIterator(this, filter);
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        HashMap<Object, RelationAttribute> values = new HashMap<Object, RelationAttribute>();
        if (!this.schema.hasAttribute(fieldKey)) {
            return values;
        }
        int i = 0;
        while (i < fields.length) {
            String field = fields[i];
            if (!this.schema.hasAttribute(field)) {
                return values;
            }
            ++i;
        }
        ArrayList<AttributeCalculate> attrCalculate = new ArrayList<AttributeCalculate>();
        ArrayList<Attribute> attrNoCalculate = new ArrayList<Attribute>();
        int i2 = 0;
        while (i2 < fields.length) {
            String field = fields[i2];
            Attribute attr = this.schema.getAttribute(field);
            if (attr instanceof AttributeCalculate) {
                attrCalculate.add((AttributeCalculate)attr);
            } else {
                attrNoCalculate.add(attr);
            }
            ++i2;
        }
        ITableIterator it = null;
        try {
            try {
                it = this.getIterator();
                while (it.hasNext()) {
                    Record record = it.next();
                    RelationAttribute ra = new RelationAttribute();
                    int i3 = 0;
                    while (i3 < attrNoCalculate.size()) {
                        ra.setFieldValue(((Attribute)attrNoCalculate.get(i3)).getName(), record.getAttribute(((Attribute)attrNoCalculate.get(i3)).getName()));
                        ++i3;
                    }
                    values.put(record.getAttribute(fieldKey), ra);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return values;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        HashSet<Object> pkSet = new HashSet<Object>();
        for (Record rec : this.records) {
            Object key = rec.getPrimaryKey();
            if (key == null) continue;
            pkSet.add(key);
        }
        pkSet.removeAll(this.getKeys(this.deleteRecords));
        ArrayList<Object> orderedPkList = new ArrayList<Object>(pkSet);
        Collections.sort(orderedPkList);
        return orderedPkList;
    }

    @Override
    public Record getRecord(int index) throws Exception {
        Record record = null;
        if (index < this.records.size()) {
            record = this.records.get(index);
            record.setAttribute(this.schema.getPrimaryKeyName(), (Object)new Long(index));
            if (this.deleteRecords.contains(record)) {
                return null;
            }
            if (this.updateRecords.contains(record)) {
                for (Record element : this.updateRecords) {
                    if (!element.equals(record)) continue;
                    return element;
                }
            }
        }
        return record;
    }

    public List<Record> getInternalRecords() {
        return this.records;
    }

    @Override
    public List<Record> getRecords() {
        return this.getByAttribute(new String[0], new Object[0]);
    }

    @Override
    public List<Record> getRecords(String fieldOrdered) {
        return this.getByAttribute(new String[0], new Object[0], fieldOrdered);
    }

    @Override
    public List<Record> getRecords(String fieldOrdered, Filter filter) {
        return this.getByAttribute(new String[0], new Object[0], fieldOrdered, filter);
    }

    @Override
    public List<Record> getRecords(String fieldOrdered, Filter filter, boolean ascending) {
        return this.getByAttribute(new String[0], new Object[0], fieldOrdered, filter, ascending);
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending) {
        boolean isString = this.schema.getAttribute(column).getType().toJavaClass().equals(String.class);
        int indexColumn = this.schema.getAttributeIndex(column);
        ArrayList<SortedAttribute> sort = new ArrayList<SortedAttribute>();
        try {
            List<Object> keys = this.getOrderedPrimaryKeyList();
            int i = 0;
            while (i < keys.size()) {
                Number n = (Number)keys.get(i);
                Record record = this.getRecord(n.intValue());
                Object value = record.getAttribute(column);
                sort.add(new SortedAttribute(value, n, ascending, isString));
                ++i;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        for (Record element : this.newRecords) {
            Object value = element.getAttribute(indexColumn);
            sort.add(new SortedAttribute(value, element, ascending, isString));
        }
        Collections.sort(sort);
        ArrayList<Object> result = new ArrayList<Object>();
        for (SortedAttribute element : sort) {
            result.add(element.getRecordNumber());
        }
        return result;
    }

    @Override
    public void remove(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.removeAll(records);
    }

    @Override
    public void removeAll(Collection<Record> records) throws Exception {
        if (!this.editable || records == null) {
            return;
        }
        if (this.inMemory) {
            for (Record element : records) {
                if (this.newRecords.contains(element)) {
                    this.newRecords.remove(element);
                }
                if (this.updateRecords.contains(element)) {
                    this.updateRecords.remove(element);
                }
                this.deleteRecords.add(element);
            }
            return;
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long size() {
        return this.records.size() - this.deleteRecords.size() + this.newRecords.size();
    }

    @Override
    public void update(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.updateAll(records);
    }

    @Override
    public void updateAll(Collection<Record> records) throws Exception {
        if (this.inMemory) {
            for (Record object : records) {
                if (!object.isUnsaved()) {
                    this.updateRecords.remove(object);
                    this.updateRecords.add(object);
                    if (!this.deleteRecords.contains(object)) continue;
                    this.deleteRecords.remove(object);
                    continue;
                }
                this.newRecords.remove(object);
                this.newRecords.add(object);
            }
            return;
        }
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public TableRecordDataSource buildFromProperties(Map<String, Object> properties) throws Exception {
        return new MemoryRecordDataSource(new ArrayList<Record>(), new FeatureSchema());
    }
}

