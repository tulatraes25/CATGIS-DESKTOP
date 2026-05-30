/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jump.feature.AbstractBasicFeature;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import java.util.HashMap;
import java.util.Map;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.relations.Relation;

public class BasicFeature
extends AbstractBasicFeature {
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    public BasicFeature(FeatureSchema featureSchema) {
        super(featureSchema);
    }

    @Override
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = new HashMap<String, Object>(attributes);
    }

    @Override
    public void setAttribute(int attributeIndex, Object newAttribute) {
        if (attributeIndex == this.schema.getPrimaryKeyIndex() && newAttribute != null && newAttribute instanceof Number) {
            this.pkAsInt = ((Number)newAttribute).intValue();
        }
        String attributeName = this.schema.getAttributeName(attributeIndex);
        this.attributes.put(attributeName, newAttribute);
    }

    @Override
    public void setAttributeCorrectType(int attributeIndex, Object newAttribute) {
        if (attributeIndex == this.schema.getPrimaryKeyIndex() && newAttribute != null && newAttribute instanceof Number) {
            this.pkAsInt = ((Number)newAttribute).intValue();
        }
        String attributeName = this.schema.getAttributeName(attributeIndex);
        AttributeType type = this.schema.getAttributeType(attributeIndex);
        Object goodAttribute = FeatureUtil.getGoodAttribute(type, newAttribute);
        this.attributes.put(attributeName, goodAttribute);
    }

    @Override
    public Object getAttribute(int i) {
        Object value_ = this.attributes.get(this.schema.getAttributeName(i));
        if (value_ != null && value_ != NULLVALUE) {
            return value_;
        }
        if (value_ == NULLVALUE) {
            return null;
        }
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
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<String, Object>(this.attributes);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!BasicFeature.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        AbstractBasicFeature basicFeat = (AbstractBasicFeature)obj;
        Attribute pk = this.schema.getPrimaryKey();
        Attribute pkOther = basicFeat.schema.getPrimaryKey();
        if (pk == null && pkOther == null) {
            return this.getID() == basicFeat.getID();
        }
        if (pk == null && pkOther != null || pk != null && pkOther == null) {
            return false;
        }
        Object pkValue = this.getAttribute(pk.getName());
        Object otherPkValue = ((Feature)obj).getAttribute(pkOther.getName());
        if (pkValue == null && otherPkValue == null) {
            return this.getID() == basicFeat.getID();
        }
        if (pkValue == null && otherPkValue != null || pkValue != null && otherPkValue == null) {
            return false;
        }
        return pkValue.equals(otherPkValue);
    }

    public String toString() {
        String result = "";
        String pkName = this.getSchema().getPrimaryKeyName();
        if (pkName != null) {
            result = "[" + pkName + " (PK) = " + this.attributes.get(pkName) + "]" + ", ";
        }
        for (String attName : this.attributes.keySet()) {
            if (pkName != null && pkName.equals(attName)) continue;
            Object value = this.attributes.get(attName);
            result = String.valueOf(result) + "[" + attName + "=" + value + "]" + ", ";
        }
        result = String.valueOf(result) + "[ID = " + this.getID() + "]";
        return result;
    }

    public int hashCode() {
        Attribute pk = this.schema.getPrimaryKey();
        if (pk != null) {
            Object pkValue = this.getAttribute(pk.getName());
            if (pkValue != null) {
                return pkValue.hashCode();
            }
            return new Integer(this.getID()).hashCode();
        }
        return new Integer(this.getID()).hashCode();
    }

    @Override
    public Object getPrimaryKey() {
        Attribute pkAtt = this.schema.getPrimaryKey();
        if (pkAtt != null) {
            return this.getAttribute(pkAtt.getName());
        }
        return null;
    }
}

