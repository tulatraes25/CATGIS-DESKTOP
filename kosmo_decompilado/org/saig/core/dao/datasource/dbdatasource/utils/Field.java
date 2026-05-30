/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.dbdatasource.utils;

public class Field
implements Comparable<Field> {
    private String name;
    private FieldType type;

    public Field(String fieldName, FieldType fieldType) {
        this.name = fieldName;
        this.type = fieldType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldType getType() {
        return this.type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Field)) {
            return false;
        }
        Field field = (Field)obj;
        if (this.getName() == null) {
            return field.getName() == null;
        }
        return this.getName().equals(field.getName());
    }

    @Override
    public int compareTo(Field obj) {
        if (this.getName() == null) {
            return 0;
        }
        return this.getName().compareTo(obj.getName());
    }

    public int hashCode() {
        int hashCode = 17;
        if (this.getName() != null) {
            hashCode += this.getName().hashCode();
        }
        return hashCode;
    }

    public boolean isPrimaryKey() {
        return this.type == FieldType.PK_FIELD;
    }

    public boolean isGeometricField() {
        return this.type == FieldType.GEOMETRIC_FIELD;
    }

    public static enum FieldType {
        PK_FIELD,
        UNIQUE_FIELD,
        GEOMETRIC_FIELD,
        REGULAR_FIELD;

    }
}

