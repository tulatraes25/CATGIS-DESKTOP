/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.dao.util;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import org.saig.core.model.data.Record;
import org.saig.core.model.feature.Attribute;

public class RecordUtil {
    public static Record copyRecord(FeatureSchema schema, Record source) {
        Record target = new Record(schema);
        int i = 0;
        while (i < schema.getAttributeCount()) {
            String name = schema.getAttributeName(i);
            Object value = source.getAttribute(name);
            if (value != null) {
                AttributeType attrType = schema.getAttributeType(name);
                if (attrType.equals(AttributeType.DATE) || attrType.equals(AttributeType.TIMESTAMP) || attrType.equals(AttributeType.TIME)) {
                    target.setAttribute(name, value);
                } else {
                    target.setAttribute(name, FeatureUtil.getGoodAttribute(schema.getAttributeType(name), value));
                }
            }
            ++i;
        }
        return target;
    }

    public static boolean copyRecord(FeatureSchema schema, Record source, Record target) {
        if (target == null || !source.getSchema().equals(target.getSchema())) {
            return false;
        }
        int i = 0;
        while (i < schema.getAttributeCount()) {
            String name = schema.getAttributeName(i);
            Object value = source.getAttribute(name);
            if (value != null) {
                AttributeType attrType = schema.getAttributeType(name);
                if (attrType.equals(AttributeType.DATE) || attrType.equals(AttributeType.TIMESTAMP) || attrType.equals(AttributeType.TIME)) {
                    target.setAttribute(name, value);
                } else {
                    target.setAttribute(name, FeatureUtil.getGoodAttribute(schema.getAttributeType(name), value));
                }
            }
            ++i;
        }
        return true;
    }

    public static boolean fullEquals(Record recordSelected, Record recordClone) {
        boolean equals = true;
        FeatureSchema schema = recordSelected.getSchema();
        int i = 0;
        while (i < schema.getAttributeCount() && equals) {
            Object value1 = recordSelected.getAttribute(i);
            Object value2 = recordClone.getAttribute(i);
            equals = value1 != null && value2 != null ? equals && value1.equals(value2) : value1 == null && value2 == null;
            ++i;
        }
        return equals;
    }

    public static void copyOnlyExistentAttributes(Record source, Record target, boolean ignorePKAttr) {
        FeatureSchema sourceSchema = source.getSchema();
        FeatureSchema targetSchema = target.getSchema();
        int i = 0;
        while (i < sourceSchema.getAttributeCount()) {
            Attribute attr = sourceSchema.getAttribute(i);
            if (!(ignorePKAttr && attr.isPrimaryKey() || !targetSchema.hasAttribute(attr.getName()))) {
                target.setAttribute(attr.getName(), source.getAttribute(attr.getName()));
            }
            ++i;
        }
    }

    public static Record toRecord(FeatureSchema tableSchema, Feature feat) {
        Record r = new Record(tableSchema);
        FeatureSchema featSchema = feat.getSchema();
        int i = 0;
        while (i < tableSchema.getAttributeCount()) {
            Attribute attr = tableSchema.getAttribute(i);
            if (featSchema.hasAttribute(attr.getName())) {
                r.setAttribute(attr.getName(), FeatureUtil.getGoodAttribute(tableSchema.getAttributeType(attr.getName()), feat.getAttribute(attr.getName())));
            }
            ++i;
        }
        return r;
    }
}

