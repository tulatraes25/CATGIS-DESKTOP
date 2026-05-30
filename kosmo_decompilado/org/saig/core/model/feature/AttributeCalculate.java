/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.feature;

import com.vividsolutions.jump.feature.AttributeType;
import java.util.Map;
import java.util.Set;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.relations.Relation;

public class AttributeCalculate
extends Attribute {
    private Relation<?> relation;
    private String relationFieldName;

    public AttributeCalculate() {
    }

    public AttributeCalculate(String name, String publicName, String relationFieldName, boolean visibility, AttributeType type, Relation<?> relation) {
        super(name, publicName, visibility, type);
        this.relation = relation;
        this.relationFieldName = relationFieldName;
    }

    public AttributeCalculate(String name, AttributeType type, Relation<?> relation) {
        super(name, name, true, type);
        this.relation = relation;
        this.relationFieldName = name;
    }

    public Relation<?> getRelation() {
        return this.relation;
    }

    public void setRelation(Relation<?> relation) {
        this.relation = relation;
    }

    public Object[] getValues(Object value) {
        return this.relation.getRelationValues(value);
    }

    public Object getFieldValue(String field, Object value) {
        return this.relation.getFieldValue(field, value);
    }

    public Object getFieldValue(String field, String fieldKey, Object value) {
        return this.relation.getFieldValue(field, value);
    }

    public Set<Object> getDistintsValues(String field) {
        return this.relation.getDistintsValues(field);
    }

    public Set<Object> getDistintsValues(String field, int limite) {
        return this.relation.getDistintsValues(field, limite);
    }

    public Map<Object, Object> fillFieldValue(String field) {
        return this.relation.getFieldValues(field);
    }

    public String getRelationFieldName() {
        return this.relationFieldName;
    }

    public void setRelationFieldName(String relationFieldName) {
        this.relationFieldName = relationFieldName;
    }

    public Set<Object> getKeysForFieldValue(String field, Object value) {
        return this.relation.getKeysForFieldValue(field, value);
    }

    @Override
    public boolean isCalculated() {
        return true;
    }
}

