/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.model.data;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Map;
import org.saig.core.model.data.Record;

public class RecordToFeatureWrapper
implements Feature {
    protected Record internalRecord;

    public RecordToFeatureWrapper(Record record) {
        this.internalRecord = record;
    }

    @Override
    public Object clone() {
        return this.clone(true);
    }

    @Override
    public Feature clone(boolean deep) {
        return this.clone(deep, false);
    }

    @Override
    public Feature clone(boolean deep, boolean generateID) {
        Record clone = (Record)this.internalRecord.clone();
        return new RecordToFeatureWrapper(clone);
    }

    @Override
    public Object getAttribute(int i) {
        return this.internalRecord.getAttribute(i);
    }

    @Override
    public Object getAttribute(String name) {
        return this.internalRecord.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.internalRecord.getAttributes();
    }

    @Override
    public double getDouble(int attributeIndex) {
        Number attr = (Number)this.internalRecord.getAttribute(attributeIndex);
        return attr.doubleValue();
    }

    @Override
    public Geometry getGeometry() {
        return null;
    }

    @Override
    public int getID() {
        return this.internalRecord.getID();
    }

    @Override
    public int getInteger(int attributeIndex) {
        Number attr = (Number)this.internalRecord.getAttribute(attributeIndex);
        return attr.intValue();
    }

    @Override
    public FeatureCollection getParent() {
        return null;
    }

    @Override
    public Object getPrimaryKey() {
        return this.internalRecord.getPrimaryKey();
    }

    @Override
    public int getPrimaryKeyAsInt() {
        Number pk = (Number)this.internalRecord.getPrimaryKey();
        return pk.intValue();
    }

    @Override
    public FeatureSchema getSchema() {
        return this.internalRecord.getSchema();
    }

    @Override
    public String getString(int attributeIndex) {
        return this.internalRecord.getAttribute(attributeIndex).toString();
    }

    @Override
    public String getString(String attributeName) {
        return this.internalRecord.getAttribute(attributeName).toString();
    }

    @Override
    public boolean isUnsaved() {
        return this.internalRecord.isUnsaved();
    }

    @Override
    public void setAttribute(int attributeIndex, Object newAttribute) {
        this.internalRecord.setAttribute(attributeIndex, newAttribute);
    }

    @Override
    public void setAttributeCorrectType(int attributeIndex, Object newAttribute) {
        this.internalRecord.setAttributeCorrectType(attributeIndex, newAttribute);
    }

    @Override
    public void setAttribute(String attributeName, Object newAttribute) {
        this.internalRecord.setAttribute(attributeName, newAttribute);
    }

    @Override
    public void setAttributeCorrectType(String attributeName, Object newAttribute) {
        this.internalRecord.setAttributeCorrectType(attributeName, newAttribute);
    }

    @Override
    public void setAttributes(Map<String, Object> attributes) {
        this.internalRecord.setAttributes(attributes);
    }

    @Override
    public void setGeometry(Geometry geometry) {
    }

    @Override
    public void setID(int n) {
    }

    @Override
    public void setParent(FeatureCollection fc) {
    }

    @Override
    public void setSchema(FeatureSchema schema) {
        this.internalRecord.setSchema(schema);
    }

    @Override
    public int compareTo(Feature o) {
        int result = 0;
        result = this.getPrimaryKey() == null ? -1 : (((RecordToFeatureWrapper)o).getPrimaryKey() == null ? 1 : (this.getSchema().getPrimaryKey().getClass().isAssignableFrom(Comparable.class) ? ((Comparable)this.getPrimaryKey()).compareTo(((RecordToFeatureWrapper)o).getPrimaryKey()) : this.getPrimaryKey().toString().compareTo(((RecordToFeatureWrapper)o).getPrimaryKey().toString())));
        return result;
    }

    public void setInternalRecord(Record internalRecord) {
        this.internalRecord = internalRecord;
    }

    public Record getInternalRecord() {
        return this.internalRecord;
    }
}

