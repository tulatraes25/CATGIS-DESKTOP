/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.datasource;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.saig.core.dao.datasource.dbdatasource.utils.Field;

public class ConnectionInformation {
    private String schemaName;
    private String tableName;
    private String pkName;
    private String geometryColumnName;
    private List<Field> fields;
    private boolean optimizedLoading;

    public ConnectionInformation(String schema, String table, String pk, String geomColumnName) {
        this.schemaName = schema;
        this.tableName = table;
        this.pkName = pk;
        this.geometryColumnName = geomColumnName;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPkName() {
        return this.pkName;
    }

    public void setPkName(String pkName) {
        this.pkName = pkName;
    }

    public String toString() {
        if (StringUtils.isNotEmpty((String)this.schemaName)) {
            return String.valueOf(this.schemaName) + "." + this.tableName;
        }
        return this.tableName;
    }

    public List<Field> getFields() {
        return this.fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public boolean isOptimizedLoading() {
        return this.optimizedLoading;
    }

    public void setOptimizedLoading(boolean optimizedLoadingSelected) {
        this.optimizedLoading = optimizedLoadingSelected;
    }

    public String getGeometryColumnName() {
        return this.geometryColumnName;
    }

    public void setGeometryColumnName(String name) {
        this.geometryColumnName = name;
    }

    public List<Field> getCandidatePkFields() {
        List<Field> result = this.getFieldsByType(Field.FieldType.PK_FIELD);
        result.addAll(this.getFieldsByType(Field.FieldType.UNIQUE_FIELD));
        if (CollectionUtils.isEmpty(result)) {
            result = this.getFieldsByType(Field.FieldType.REGULAR_FIELD);
        }
        return result;
    }

    public List<Field> getCandidateGeometryFields() {
        return this.getFieldsByType(Field.FieldType.GEOMETRIC_FIELD);
    }

    private List<Field> getFieldsByType(Field.FieldType type) {
        ArrayList<Field> result = new ArrayList<Field>();
        for (Field currentField : this.fields) {
            if (!currentField.getType().equals((Object)type)) continue;
            result.add(currentField);
        }
        return result;
    }
}

