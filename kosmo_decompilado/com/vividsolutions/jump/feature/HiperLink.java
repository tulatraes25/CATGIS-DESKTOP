/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.feature;

public class HiperLink {
    private String fieldValue;
    private String fieldDescription;

    public HiperLink(String fieldName) {
        this.fieldValue = fieldName;
    }

    public String getFieldWithHiperLink() {
        return this.fieldValue;
    }

    public String getFieldDescription() {
        return this.fieldDescription;
    }

    public void setFieldDescription(String fieldName) {
        this.fieldDescription = fieldName;
    }
}

