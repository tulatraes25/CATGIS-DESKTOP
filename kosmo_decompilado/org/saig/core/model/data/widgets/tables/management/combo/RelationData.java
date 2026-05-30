/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.combo;

public class RelationData {
    private String code;
    private String value;
    private String tableName;
    private String type;
    public static final String TABLE = "table";
    public static final String LAYER = "layer";

    public RelationData(String code, String value, String tableName) {
        this.code = code;
        this.value = value;
        this.tableName = tableName;
        this.type = TABLE;
    }

    public String getCode() {
        return this.code;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getValue() {
        return this.value;
    }

    public String getType() {
        return this.type;
    }
}

