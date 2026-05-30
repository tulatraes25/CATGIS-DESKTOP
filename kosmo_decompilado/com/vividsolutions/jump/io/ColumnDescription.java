/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.AttributeType;
import org.xml.sax.Attributes;

public class ColumnDescription {
    public static final int VALUE_IS_BODY = 1;
    public static final int VALUE_IS_ATT = 2;
    String columnName;
    String tagName;
    boolean tagNeedsAttribute = false;
    String attributeName;
    boolean tagAttributeNeedsValue = false;
    String attributeValue;
    int valueType = 1;
    String valueAttribute;
    AttributeType type;

    public void setType(AttributeType t) {
        this.type = t == null ? AttributeType.STRING : t;
    }

    public AttributeType getType() {
        return this.type;
    }

    public void setColumnName(String colname) {
        this.columnName = colname;
    }

    public void setTagName(String tagname) {
        this.tagName = tagname;
    }

    public void setTagAttribute(String attName, String attValue) {
        this.attributeName = attName;
        this.attributeValue = attValue;
        this.tagNeedsAttribute = true;
        this.tagAttributeNeedsValue = true;
    }

    public void setTagAttribute(String attName) {
        this.attributeName = attName;
        this.tagNeedsAttribute = true;
    }

    public void setValueAttribute(String attName) {
        this.valueAttribute = attName;
        this.valueType = 2;
    }

    int lookupAttribute(Attributes atts, String att_name) {
        int t = 0;
        while (t < atts.getLength()) {
            if (atts.getQName(t).equalsIgnoreCase(att_name)) {
                return t;
            }
            ++t;
        }
        return -1;
    }

    public int match(String XMLtagName, Attributes xmlAtts) {
        if (XMLtagName.compareToIgnoreCase(this.tagName) == 0) {
            if (this.tagNeedsAttribute) {
                int attindex = this.lookupAttribute(xmlAtts, this.attributeName);
                if (attindex == -1) {
                    return 0;
                }
                if (this.tagAttributeNeedsValue && xmlAtts.getValue(attindex).compareToIgnoreCase(this.attributeValue) != 0) {
                    return 0;
                }
            }
            return this.valueType;
        }
        return 0;
    }
}

