/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations;

import java.util.HashMap;
import java.util.Map;

public class RelationAttribute {
    private Map<String, Object> fieldValues = new HashMap<String, Object>();

    public Object getFieldValue(String fieldName) {
        return this.fieldValues.get(fieldName);
    }

    public void setFieldValue(String fieldName, Object value) {
        this.fieldValues.put(fieldName, value);
    }
}

