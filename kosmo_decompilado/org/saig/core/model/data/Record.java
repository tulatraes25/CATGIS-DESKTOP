/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.util.HashMap;
import java.util.Map;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.relations.Relation;

public class Record
implements Comparable<Record>,
Cloneable {
    private int id = FeatureUtil.nextID();
    private FeatureSchema schema;
    private static String NULLVALUE = "@NULL";
    private Map<String, Object> attributes;

    public Record(FeatureSchema schema) {
        this.schema = schema;
        this.attributes = new HashMap<String, Object>();
    }

    public FeatureSchema getSchema() {
        return this.schema;
    }

    public void setSchema(FeatureSchema schema) {
        this.schema = schema;
    }

    public Object getAttribute(int i) {
        Attribute attribute = this.schema.getAttribute(i);
        if (attribute instanceof AttributeCalculate) {
            AttributeCalculate attCal = (AttributeCalculate)attribute;
            Relation<?> relation = attCal.getRelation();
            String linkAttribute = relation.getSourceAttribute();
            Object value = this.getAttribute(linkAttribute);
            Object relationValue = relation.getFieldValue(attCal.getRelationFieldName(), value);
            if (relationValue == null) {
                this.attributes.put(attCal.getName(), NULLVALUE);
            } else {
                this.attributes.put(attCal.getName(), relationValue);
            }
            return relationValue;
        }
        Object value_ = this.attributes.get(this.schema.getAttributeName(i));
        if (value_ != null && value_ != NULLVALUE) {
            return value_;
        }
        if (value_ == NULLVALUE) {
            return null;
        }
        return null;
    }

    public Object getPrimaryKey() {
        int index = this.schema.getPrimaryKeyIndex();
        return this.getAttribute(index);
    }

    public Map<String, Object> getAttributes() {
        return new HashMap<String, Object>(this.attributes);
    }

    public Object getAttribute(String name) {
        return this.getAttribute(this.schema.getAttributeIndex(name));
    }

    public void setAttribute(String attributeName, Object newAttribute) {
        int attributeIndex = this.schema.getAttributeIndex(attributeName);
        this.setAttribute(attributeIndex, newAttribute);
    }

    public void setAttributeCorrectType(String attributeName, Object newAttribute) {
        int attributeIndex = this.schema.getAttributeIndex(attributeName);
        this.setAttributeCorrectType(attributeIndex, newAttribute);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = new HashMap<String, Object>(attributes);
    }

    public void setAttribute(int attributeIndex, Object newAttribute) {
        String attributeName = this.schema.getAttributeName(attributeIndex);
        this.attributes.put(attributeName, newAttribute);
    }

    public void setAttributeCorrectType(int attributeIndex, Object newAttribute) {
        AttributeType type = this.schema.getAttributeType(attributeIndex);
        Object goodAttribute = FeatureUtil.getGoodAttribute(type, newAttribute);
        String attributeName = this.schema.getAttributeName(attributeIndex);
        this.attributes.put(attributeName, goodAttribute);
    }

    public Object clone() {
        Record clone = new Record(this.schema);
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.schema.getAttributeType(i) != AttributeType.GEOMETRY) {
                clone.setAttribute(i, this.getAttribute(i));
            }
            ++i;
        }
        return clone;
    }

    public boolean isUnsaved() {
        Attribute pk = this.schema.getPrimaryKey();
        if (pk == null) {
            return false;
        }
        Object pkValue = this.getAttribute(pk.getName());
        return pkValue == null;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(Record.class)) {
            return false;
        }
        Record otherRecord = (Record)obj;
        Attribute pk = this.schema.getPrimaryKey();
        Attribute pkOther = otherRecord.schema.getPrimaryKey();
        if (pk == null && pkOther == null) {
            return this.getID() == otherRecord.getID();
        }
        if (pk == null && pkOther != null || pk != null && pkOther == null) {
            return false;
        }
        Object pkValue = this.getAttribute(pk.getName());
        Object otherPkValue = ((Record)obj).getAttribute(pkOther.getName());
        if (pkValue == null && otherPkValue == null) {
            return this.getID() == otherRecord.getID();
        }
        if (pkValue == null && otherPkValue != null || pkValue != null && otherPkValue == null) {
            return false;
        }
        return pkValue.equals(otherPkValue);
    }

    public int hashCode() {
        Object pk = this.getPrimaryKey();
        if (pk == null) {
            return 0;
        }
        return pk.hashCode();
    }

    public int getID() {
        return this.id;
    }

    @Override
    public int compareTo(Record o) {
        int result = 0;
        result = this.getPrimaryKey() == null ? -1 : (o.getPrimaryKey() == null ? 1 : (this.schema.getPrimaryKey().getClass().isAssignableFrom(Comparable.class) ? ((Comparable)this.getPrimaryKey()).compareTo(o.getPrimaryKey()) : this.getPrimaryKey().toString().compareTo(o.getPrimaryKey().toString())));
        return result;
    }

    public String toString() {
        String result = "";
        String pkName = this.getSchema().getPrimaryKeyName();
        if (pkName != null) {
            result = "[" + pkName + " (PK) = " + this.attributes.get(pkName) + "]" + ",";
        }
        for (String attName : this.attributes.keySet()) {
            if (pkName != null && pkName.equals(attName)) continue;
            Object value = this.attributes.get(attName);
            result = String.valueOf(result) + "[" + attName + "=" + value + "]" + ",";
        }
        result = String.valueOf(result) + "[ID = " + this.getID() + "]";
        return result;
    }
}

