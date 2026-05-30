/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.memory.MemoryRecordDataSource;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationAttribute;

public class Table {
    private String name;
    private boolean versionable = false;
    private boolean enabled = true;
    private boolean visible;
    private String startDateField;
    private String endDateField;
    private String historyField;
    private Timestamp versionableViewDate;
    private Map<String, Object> properties;
    private int frameLocationX;
    private int frameLocationY;
    private int frameWidth;
    private int frameHeight;
    private TableRecordDataSource dataSource;
    private Map<String, Relation<?>> relations = new HashMap();
    private boolean internal = false;

    public Table(TableRecordDataSource dataSource) {
        this.properties = dataSource.getProperties();
        this.name = dataSource.getName();
        if (JUMPWorkbench.getTable(this.name) != null) {
            int cont = 1;
            do {
                this.name = String.valueOf(dataSource.getName()) + " (" + cont++ + ")";
            } while (JUMPWorkbench.getTable(this.name) != null);
        }
        this.visible = true;
        this.dataSource = dataSource;
    }

    public Table() {
    }

    public void addRelation(Relation<?> relation) {
        LayerRelation layerRelation;
        this.relations.put(relation.getRelationName(), relation);
        if (relation instanceof LayerRelation && !(layerRelation = (LayerRelation)relation).getTargetLayer().isEnabled()) {
            return;
        }
        if (this.dataSource != null) {
            this.dataSource.getSchema().addRelation(relation);
        }
    }

    public Relation<?> getRelation(String name) {
        return this.relations.get(name);
    }

    public boolean hasRelation(String name) {
        return this.relations.containsKey(name);
    }

    public boolean hasRelations() {
        return !this.relations.isEmpty();
    }

    public Collection<Relation<?>> getAllRelations() {
        return this.relations.values();
    }

    public Map<String, Relation<?>> getRelations() {
        return this.relations;
    }

    public void setRelations(Map<String, Relation<?>> relations) {
        for (Relation<?> relation : relations.values()) {
            this.addRelation(relation);
        }
    }

    public void removeAllRelations(Collection<Relation<?>> relations) {
        for (Relation<?> element : relations) {
            this.removeRelation(element);
        }
    }

    public void removeRelation(Relation<?> relation) {
        if (this.relations.containsKey(relation.getRelationName())) {
            this.relations.remove(relation.getRelationName());
            this.dataSource.getSchema().removeRelation(relation);
            relation.destroy();
        }
    }

    public Record getRecord(int index) throws Exception {
        return this.dataSource.getRecord(index);
    }

    public List<Record> getRecords() {
        return this.dataSource.getRecords();
    }

    public List<Record> getRecords(String fieldOrdered) {
        return this.dataSource.getRecords(fieldOrdered);
    }

    public List<Record> getRecords(String fieldOrdered, Filter filter) {
        return this.dataSource.getRecords(fieldOrdered, filter);
    }

    public List<Record> getRecords(String fieldOrdered, Filter filter, boolean ascending) {
        return this.dataSource.getRecords(fieldOrdered, filter, ascending);
    }

    public boolean isEmpty() {
        return this.dataSource.isEmpty();
    }

    public void add(Record record) throws Exception {
        this.dataSource.add(record);
    }

    public void addAll(Collection<Record> records) throws Exception {
        this.dataSource.addAll(records);
    }

    public void removeAll(Collection<Record> records) throws Exception {
        this.dataSource.removeAll(records);
    }

    public void remove(Record record) throws Exception {
        this.dataSource.remove(record);
    }

    public void updateAll(Collection<Record> records) throws Exception {
        this.dataSource.updateAll(records);
    }

    public void update(Record record) throws Exception {
        this.dataSource.update(record);
    }

    public Record getByPrimaryKey(Object pkValue) {
        return this.dataSource.getByPrimaryKey(pkValue);
    }

    public List<Record> getByPrimaryKeys(Object[] pkValues) {
        return this.dataSource.getByPrimaryKey(pkValues);
    }

    public List<Record> getByAttribute(String[] fields, Object[] values) {
        return this.dataSource.getByAttribute(fields, values);
    }

    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField) {
        return this.dataSource.getByAttribute(fields, values, orderField);
    }

    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField, Filter filter) {
        return this.dataSource.getByAttribute(fields, values, orderField, filter);
    }

    public List<Object> getRelationValues(String fieldName, String pkName, Object value) {
        return this.dataSource.getFieldValue(fieldName, pkName, value);
    }

    public FeatureSchema getSchema() {
        if (this.dataSource != null) {
            return this.dataSource.getSchema();
        }
        return null;
    }

    public long size() {
        return this.dataSource.size();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String toString() {
        return this.name;
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setDataSource(TableRecordDataSource trds) {
        this.dataSource = trds;
        this.properties = trds.getProperties();
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public void setFrameLocationX(int frameLocationX) {
        this.frameLocationX = frameLocationX;
    }

    public void setFrameLocationY(int frameLocationY) {
        this.frameLocationY = frameLocationY;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return this.frameHeight;
    }

    public int getFrameLocationX() {
        return this.frameLocationX;
    }

    public int getFrameLocationY() {
        return this.frameLocationY;
    }

    public int getFrameWidth() {
        return this.frameWidth;
    }

    public void commit() throws Exception {
        this.dataSource.commit();
    }

    public void rollback() {
        this.dataSource.rollback();
    }

    public TableRecordDataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Object> getKeys() {
        return this.dataSource.getOrderedPrimaryKeyList();
    }

    public List<Object> getSortKeys(String column, boolean ascending) {
        return this.dataSource.getSortKeys(column, ascending);
    }

    public List<Object> getFieldValue(String field, String fieldKey, Object value) {
        return this.dataSource.getFieldValue(field, fieldKey, value);
    }

    public Set<Object> getDistintsValues(String field) {
        return this.dataSource.getDistintsValues(field);
    }

    public Set<Object> getDistintsValues(String field, int limit) {
        return this.dataSource.getDistintsValues(field, limit);
    }

    public Map<Object, RelationAttribute> getMapFieldsValues(String[] field, String fieldKey) {
        if (this.dataSource == null) {
            return null;
        }
        return this.dataSource.getMapFieldsValues(field, fieldKey);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Table)) {
            return false;
        }
        Table otherRel = (Table)other;
        return this.getName().equals(otherRel.getName());
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        result = result * 37 + this.getName().hashCode();
        return result;
    }

    public void fireTableChanged() {
        this.isEnabled();
    }

    public boolean isVersionable() {
        return this.versionable;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
        if (this.dataSource != null) {
            this.dataSource.setVersionable(versionable);
        }
        if (!versionable) {
            this.startDateField = null;
            this.endDateField = null;
            this.versionableViewDate = null;
        }
    }

    public boolean getVersionable() {
        return this.versionable;
    }

    public boolean isDataBaseDataSource() {
        return this.getDataSource() instanceof TableDBRecordDataSource;
    }

    public String getStartDateField() {
        return this.startDateField;
    }

    public void setStartDateField(String fieldStartDate) {
        this.startDateField = fieldStartDate;
        if (this.getSchema() != null) {
            this.getSchema().setFieldStartDate(fieldStartDate);
        }
    }

    public String getEndDateField() {
        return this.endDateField;
    }

    public void setEndDateField(String fieldEndDate) {
        this.endDateField = fieldEndDate;
        if (this.getSchema() != null) {
            this.getSchema().setFieldEndDate(fieldEndDate);
        }
    }

    public String getHistoryField() {
        return this.historyField;
    }

    public void setHistoryField(String historyField) {
        this.historyField = historyField;
        if (this.getSchema() != null) {
            this.getSchema().setHistoryField(historyField);
        }
    }

    public Timestamp getVersionableViewDate() {
        return this.versionableViewDate;
    }

    public void setVersionableViewDate(Timestamp versionableViewDate) {
        this.versionableViewDate = versionableViewDate;
        if (this.getSchema() != null) {
            this.getSchema().setVersionableViewDate(versionableViewDate);
        }
    }

    public boolean isInternal() {
        return this.internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean hasReadableDataSource() {
        return this.dataSource != null && !(this.dataSource instanceof MemoryRecordDataSource);
    }
}

