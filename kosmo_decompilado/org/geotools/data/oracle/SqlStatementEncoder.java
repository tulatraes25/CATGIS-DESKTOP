/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.feature.AttributeType
 *  org.geotools.feature.FeatureType
 *  org.geotools.filter.Filter
 *  org.geotools.filter.SQLEncoder
 *  org.geotools.filter.SQLEncoderException
 */
package org.geotools.data.oracle;

import java.util.logging.Logger;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoder;
import org.geotools.filter.SQLEncoderException;

final class SqlStatementEncoder {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
    private SQLEncoder whereEncoder;
    private String fidColumn;
    private String tableName;

    SqlStatementEncoder(SQLEncoder whereEncoder, String tablename, String fidColumn) {
        this.whereEncoder = whereEncoder;
        this.tableName = tablename;
        this.fidColumn = fidColumn;
    }

    String makeInsertSQL(FeatureType featureType) {
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(this.tableName);
        sql.append("(");
        sql.append(this.fidColumn);
        sql.append(",");
        AttributeType[] attributeTypes = featureType.getAttributeTypes();
        int i = 0;
        while (i < attributeTypes.length) {
            sql.append(attributeTypes[i].getName());
            if (i < attributeTypes.length - 1) {
                sql.append(",");
            } else {
                sql.append(")");
            }
            ++i;
        }
        sql.append(" VALUES (?,");
        i = 0;
        while (i < attributeTypes.length) {
            sql.append("?");
            if (i < attributeTypes.length - 1) {
                sql.append(",");
            } else {
                sql.append(")");
            }
            ++i;
        }
        return sql.toString();
    }

    String makeSelectSQL(AttributeType[] attrTypes, Filter filter, int maxFeatures, boolean useMax) throws SQLEncoderException {
        LOGGER.finer("Creating sql for Query: mf=" + maxFeatures + " filter=" + filter + " useMax=" + useMax);
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT ");
        sqlBuffer.append(this.fidColumn);
        int i = 0;
        while (i < attrTypes.length) {
            sqlBuffer.append(", ");
            sqlBuffer.append(attrTypes[i].getName());
            ++i;
        }
        sqlBuffer.append(" FROM ");
        sqlBuffer.append(this.tableName);
        if (filter != null && filter != Filter.NONE) {
            String where = this.whereEncoder.encode(filter);
            sqlBuffer.append(" ");
            sqlBuffer.append(where);
            if (useMax && maxFeatures > 0) {
                sqlBuffer.append(" and ROWNUM <= ");
                sqlBuffer.append(maxFeatures);
            }
        } else if (useMax && maxFeatures > 0) {
            sqlBuffer.append(" WHERE ROWNUM <= ");
            sqlBuffer.append(maxFeatures);
        }
        String sqlStmt = sqlBuffer.toString();
        LOGGER.finer("sqlString = " + sqlStmt);
        return sqlStmt;
    }

    String makeModifyTemplate(AttributeType[] attributeTypes) {
        StringBuffer buffer = new StringBuffer("UPDATE ");
        buffer.append(this.tableName);
        buffer.append(" SET ");
        int i = 0;
        while (i < attributeTypes.length) {
            buffer.append(attributeTypes[i].getName());
            buffer.append(" = ? ");
            if (i < attributeTypes.length - 1) {
                buffer.append(", ");
            } else {
                buffer.append(" ");
            }
            ++i;
        }
        return buffer.toString();
    }

    String makeModifyTemplate(AttributeType[] attributeTypes, Filter filter) throws SQLEncoderException {
        String whereClause = this.whereEncoder.encode(filter);
        return String.valueOf(this.makeModifyTemplate(attributeTypes)) + " " + whereClause;
    }

    String makeDeleteSQL(Filter filter) throws SQLEncoderException {
        return "DELETE FROM " + this.tableName + " " + this.whereEncoder.encode(filter);
    }
}

