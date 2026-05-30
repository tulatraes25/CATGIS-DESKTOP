/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.definition;

public class Column {
    private boolean mandatory = false;
    private String name = null;
    private String relationTable = null;
    private String relationField = null;
    private String relationFieldToShow = null;
    private int size = 0;

    public String getRelationTable() {
        return this.relationTable;
    }

    public void setRelationTable(String relationTable) {
        this.relationTable = relationTable;
    }

    public String getRelationField() {
        return this.relationField;
    }

    public void setRelationField(String relationField) {
        this.relationField = relationField;
    }

    public String getRelationFieldToShow() {
        return this.relationFieldToShow;
    }

    public void setRelationFieldToShow(String relationFieldToShow) {
        this.relationFieldToShow = relationFieldToShow;
    }

    public boolean isMandatory() {
        return this.mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

