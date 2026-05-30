/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.WKTWriter
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.dao.datasource.dbdatasource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.feature.ILayerIterator;
import com.vividsolutions.jump.workbench.model.Layer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.sql.ConnectionPoolDataSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.IPoolableDBDataSource;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator;
import org.saig.core.dao.datasource.dbdatasource.iterators.MySQLEditableIterator;
import org.saig.core.dao.datasource.dbdatasource.iterators.MySQLIterator;
import org.saig.core.dao.datasource.dbdatasource.utils.WKBParser2;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Filter;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.SQLEncoderException;
import org.saig.core.filter.SQLEncoderMySQL;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.TableRelation;
import org.saig.jump.lang.I18N;

public class MySQLDataSource
extends AbstractJDBCDataSource
implements IPoolableDBDataSource {
    private static final Logger LOGGER = Logger.getLogger(MySQLDataSource.class);
    public static final String ID = "MySQL";
    public static Map<AttributeType, String> attributeTypeToDBType = new HashMap<AttributeType, String>();
    public static Map<AttributeType, Integer> attributeTypeToSQLType = new HashMap<AttributeType, Integer>();

    static {
        attributeTypeToDBType.put(AttributeType.BIGINT, "BIGINT");
        attributeTypeToDBType.put(AttributeType.BIT, "TINYINT(1)");
        attributeTypeToDBType.put(AttributeType.BOOLEAN, "TINYINT(1)");
        attributeTypeToDBType.put(AttributeType.CHAR, "VARCHAR(1)");
        attributeTypeToDBType.put(AttributeType.DATE, "DATE");
        attributeTypeToDBType.put(AttributeType.DECIMAL, "INTEGER");
        attributeTypeToDBType.put(AttributeType.DOUBLE, "DOUBLE PRECISION");
        attributeTypeToDBType.put(AttributeType.FLOAT, "FLOAT");
        attributeTypeToDBType.put(AttributeType.GEOMETRY, "GEOMETRY");
        attributeTypeToDBType.put(AttributeType.INTEGER, "INTEGER");
        attributeTypeToDBType.put(AttributeType.LONG, "BIGINT");
        attributeTypeToDBType.put(AttributeType.LONGVARCHAR, "VARCHAR(1024)");
        attributeTypeToDBType.put(AttributeType.TEXT, "TEXT");
        attributeTypeToDBType.put(AttributeType.NUMERIC, "NUMERIC");
        attributeTypeToDBType.put(AttributeType.OBJECT, "BLOB");
        attributeTypeToDBType.put(AttributeType.REAL, "REAL");
        attributeTypeToDBType.put(AttributeType.SMALLINT, "SMALLINT");
        attributeTypeToDBType.put(AttributeType.STRING, "VARCHAR(1024)");
        attributeTypeToDBType.put(AttributeType.TIME, "time");
        attributeTypeToDBType.put(AttributeType.TIMESTAMP, "timestamp");
        attributeTypeToDBType.put(AttributeType.TINYINT, "TINYINT");
        attributeTypeToDBType.put(AttributeType.VARCHAR, "VARCHAR(1024)");
        attributeTypeToSQLType.put(AttributeType.BIGINT, new Integer(-5));
        attributeTypeToSQLType.put(AttributeType.BIT, new Integer(-6));
        attributeTypeToSQLType.put(AttributeType.BOOLEAN, new Integer(-6));
        attributeTypeToSQLType.put(AttributeType.CHAR, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.DATE, new Integer(91));
        attributeTypeToSQLType.put(AttributeType.DECIMAL, new Integer(3));
        attributeTypeToSQLType.put(AttributeType.DOUBLE, new Integer(8));
        attributeTypeToSQLType.put(AttributeType.FLOAT, new Integer(6));
        attributeTypeToSQLType.put(AttributeType.INTEGER, new Integer(4));
        attributeTypeToSQLType.put(AttributeType.LONG, new Integer(-5));
        attributeTypeToSQLType.put(AttributeType.LONGVARCHAR, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.TEXT, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.NUMERIC, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.OBJECT, new Integer(2004));
        attributeTypeToSQLType.put(AttributeType.REAL, new Integer(7));
        attributeTypeToSQLType.put(AttributeType.SMALLINT, new Integer(5));
        attributeTypeToSQLType.put(AttributeType.STRING, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.TIME, new Integer(92));
        attributeTypeToSQLType.put(AttributeType.TIMESTAMP, new Integer(93));
        attributeTypeToSQLType.put(AttributeType.TINYINT, new Integer(-6));
        attributeTypeToSQLType.put(AttributeType.VARCHAR, new Integer(12));
    }

    public MySQLDataSource() {
    }

    public MySQLDataSource(String host, int port, String databaseName, String tableName, String username, String password) {
        super(host, port, databaseName, username, password);
        this.setTableName(tableName);
    }

    public MySQLDataSource(String host, int port, String databaseName, String username, String password) {
        super(host, port, databaseName, username, password);
    }

    public static MySQLDataSource getInstance(String host, int port, String schema, String tableName, String username, String password) {
        return new MySQLDataSource(host, port, tableName, schema, username, password);
    }

    @Override
    public synchronized void updateAll(Collection<Feature> features) throws Exception {
        if (!this.editable || CollectionUtils.isEmpty(features)) {
            return;
        }
        if (this.inMemory) {
            for (Feature object : features) {
                if (!object.isUnsaved()) {
                    this.updateFeatures.remove(object);
                    this.updateFeatures.add(object);
                    if (!this.deletedFeatures.contains(object)) continue;
                    this.deletedFeatures.remove(object);
                    continue;
                }
                this.newFeatures.remove(object);
                this.newFeatures.add(object);
            }
            return;
        }
        ArrayList<Feature> oldFeatures = new ArrayList<Feature>();
        features = this.getCorrectGeometries(features);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                connection.setAutoCommit(false);
                for (Feature feature : features) {
                    String sqlForUpdate = this.getSQLForUpdate(feature);
                    if (sqlForUpdate == null) continue;
                    if (this.schema.isVersionable() && this.schema.getHistoryField() != null) {
                        this.updateHistoryFeature(feature, oldFeatures);
                    }
                    preparedStatement = connection.prepareStatement(sqlForUpdate);
                    FeatureSchema featureSchema = feature.getSchema();
                    int position = 1;
                    int i = 0;
                    while (i < featureSchema.getAttributeCount()) {
                        Attribute attr = this.schema.getAttribute(i);
                        if (!(attr instanceof AttributeCalculate) && !attr.getType().equals(AttributeType.GEOMETRY)) {
                            Object value = feature.getAttribute(i);
                            if (value == null || value.toString().trim().length() == 0) {
                                preparedStatement.setNull(position, this.toSQLType(featureSchema.getAttribute(i).getType()));
                            } else {
                                if (value instanceof java.util.Date) {
                                    AttributeType type = this.schema.getAttribute(i).getType();
                                    value = type == AttributeType.TIMESTAMP || type == AttributeType.TIME ? FeatureUtil.getGoodAttribute(type, value) : new Date(((java.util.Date)value).getTime());
                                }
                                preparedStatement.setObject(position, value);
                            }
                            ++position;
                        }
                        ++i;
                    }
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
                connection.commit();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                connection.rollback();
                throw e;
            }
        }
        finally {
            this.closeConnection(connection);
        }
        if (!oldFeatures.isEmpty()) {
            this.addAll(oldFeatures, true, false);
        }
    }

    @Override
    protected String getSQLForInsert(Feature feature) {
        String selInsert = "INSERT INTO " + this.getFullTableName() + " (" + this.getBasicSQLInsert(feature) + " ) VALUES (";
        WKTWriter geometryWriter = new WKTWriter();
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                if (attr.getType().equals(AttributeType.GEOMETRY)) {
                    Geometry geom = (Geometry)feature.getAttribute(i);
                    if (geom == null) {
                        geom = factory.buildGeometry(new ArrayList());
                    }
                    String wktGeometry = "GeometryFromText('" + geometryWriter.write(geom) + "', " + this.srid + ")";
                    selInsert = String.valueOf(selInsert) + wktGeometry + ",";
                } else {
                    selInsert = String.valueOf(selInsert) + "?,";
                }
            }
            ++i;
        }
        selInsert = selInsert.substring(0, selInsert.length() - 1);
        selInsert = String.valueOf(selInsert) + ")";
        return selInsert;
    }

    @Override
    protected String getSQLForUpdate(Feature feature) {
        String selInsert = "UPDATE " + this.getFullTableName() + " SET ";
        WKTWriter geometryWriter = new WKTWriter();
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                if (attr.getType().equals(AttributeType.GEOMETRY)) {
                    Geometry geom = (Geometry)feature.getAttribute(i);
                    if (geom == null) {
                        geom = factory.buildGeometry(new ArrayList());
                    }
                    String wktGeometry = "GeometryFromText('" + geometryWriter.write(geom) + "'," + this.srid + ")";
                    selInsert = String.valueOf(selInsert) + this.escapeAttributeName(attr.getName()) + "=" + wktGeometry + ",";
                } else {
                    selInsert = String.valueOf(selInsert) + this.escapeAttributeName(attr.getName()) + "=?,";
                }
            }
            ++i;
        }
        selInsert = selInsert.substring(0, selInsert.length() - 1);
        selInsert = String.valueOf(selInsert) + " WHERE ";
        selInsert = String.valueOf(selInsert) + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + " = '" + feature.getAttribute(this.schema.getPrimaryKeyIndex()).toString() + "'";
        return selInsert;
    }

    @Override
    public Feature readFeature(ResultSet resultSet) throws Exception {
        WKBParser2 parser = new WKBParser2();
        BasicFeature featPK = new BasicFeature(this.schema);
        Object objetoBaseDatos = null;
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.isAttributeForRead(i)) {
                Attribute attr = this.schema.getAttribute(i);
                if (attr.getType().equals(AttributeType.GEOMETRY)) {
                    byte[] data = resultSet.getBytes(this.schema.getAttributeName(i));
                    objetoBaseDatos = data == null ? factory.createGeometryCollection(null) : parser.parse(data);
                } else {
                    objetoBaseDatos = resultSet.getObject(attr.getName());
                    if (objetoBaseDatos != null && objetoBaseDatos.getClass().equals(String.class)) {
                        objetoBaseDatos = objetoBaseDatos.toString().trim();
                    }
                }
                featPK.setAttribute(i, objetoBaseDatos);
            }
            ++i;
        }
        return featPK;
    }

    @Override
    public Geometry readGeometry(ResultSet resultset) {
        WKBParser2 parser = new WKBParser2();
        Object objetoBaseDatos = null;
        try {
            byte[] data = resultset.getBytes(this.schema.getAttributeName(this.schema.getGeometryIndex()));
            objetoBaseDatos = data == null ? factory.createGeometryCollection(null) : parser.parse(data);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return objetoBaseDatos;
    }

    @Override
    protected String getSQLForCreateTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + this.getFullTableName() + "(";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            String name = this.schema.getAttributeName(i);
            AttributeType attributeType = this.schema.getAttributeType(i);
            String attributeTypeName = attributeTypeToDBType.get(attributeType);
            sql = String.valueOf(sql) + this.escapeAttributeName(name) + " " + attributeTypeName;
            if (this.schema.getGeometryIndex() == i || this.schema.getPrimaryKeyIndex() == i) {
                sql = String.valueOf(sql) + " NOT NULL ";
            }
            sql = String.valueOf(sql) + ",";
            ++i;
        }
        sql = String.valueOf(sql) + " PRIMARY KEY(" + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + "))";
        return sql;
    }

    @Override
    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
        Class type;
        block4: {
            int DATA_TYPE = 5;
            int TYPE_NAME = 6;
            try {
                int dataType = rs.getInt(5);
                String typeName = rs.getString(6);
                String typeNameLower = typeName.toLowerCase();
                if ((dataType == 1111 || dataType == -2) && ("geometry".equals(typeNameLower) || "point".equals(typeNameLower) || "linestring".equals(typeNameLower) || "polygon".equals(typeNameLower) || "multipoint".equals(typeNameLower) || "multilinestring".equals(typeNameLower) || "multipolygon".equals(typeNameLower) || "geometrycollection".equals(typeNameLower))) {
                    return AttributeType.toAttributeType(Geometry.class);
                }
                type = (Class)TYPE_MAPPINGS.get(new Integer(dataType));
                if (type != null) break block4;
                return null;
            }
            catch (SQLException e) {
                throw new IOException(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.MySQLDataSource.sql-exception-occurred")) + e.getMessage());
            }
        }
        return AttributeType.toAttributeType(type);
    }

    @Override
    protected String getSQLExpression(Filter filter) {
        String resultado = "";
        if (filter != null) {
            SQLEncoderMySQL encoder = new SQLEncoderMySQL(this.srid, this.schema.getGeometryType());
            try {
                resultado = encoder.encode(filter);
                resultado = resultado.replaceFirst("WHERE", "");
                ArrayList<Attribute> attrNoCalc = new ArrayList<Attribute>();
                ArrayList<AttributeCalculate> attrCal = new ArrayList<AttributeCalculate>();
                int i = 0;
                while (i < this.schema.getAttributeCount()) {
                    Attribute attr = this.schema.getAttribute(i);
                    if (attr.isCalculated()) {
                        attrCal.add((AttributeCalculate)attr);
                    } else {
                        attrNoCalc.add(attr);
                    }
                    ++i;
                }
                for (Attribute attr : attrNoCalc) {
                    resultado = this.processSQLExpression(resultado, attr.getName(), attr.getName(), this.getFullTableName());
                }
                for (AttributeCalculate attrCalculate : attrCal) {
                    Relation<?> rel = attrCalculate.getRelation();
                    String tableName = "";
                    if (rel instanceof TableRelation) {
                        Table table = ((TableRelation)rel).getTable();
                        if (table.getDataSource() instanceof TableDBRecordDataSource) {
                            TableDBRecordDataSource tableDS = (TableDBRecordDataSource)table.getDataSource();
                            if (!this.checkHost(tableDS.getHost()) || tableDS.getPort() != this.getPort() || !tableDS.getDataBaseName().equals(this.getDataBase()) || !tableDS.getUser().equals(this.getUserName()) || !tableDS.getPassword().equals(this.getPassword())) continue;
                            tableName = ((TableDBRecordDataSource)table.getDataSource()).getFullTableName();
                        }
                    } else {
                        LayerRelation layerRel = (LayerRelation)rel;
                        Layer layer = layerRel.getTargetLayer();
                        if (layer.isDataBaseDataSource()) {
                            AbstractJDBCDataSource layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor();
                            tableName = layerDS.getFullTableName();
                        }
                    }
                    if (tableName.equals("")) continue;
                    resultado = this.processSQLExpression(resultado, attrCalculate.getName(), attrCalculate.getRelationFieldName(), tableName);
                }
            }
            catch (SQLEncoderException e) {
                LOGGER.info((Object)"", (Throwable)e);
            }
        }
        return resultado;
    }

    @Override
    public String processSQLExpression(String sql, String name1, String name2, String tableName) {
        sql = sql.replaceAll("\\b" + name1 + "\\b = ", String.valueOf(tableName) + "." + name2 + " = ");
        sql = sql.replaceAll("'" + name1 + "' IS", String.valueOf(tableName) + "." + name2 + " IS");
        sql = sql.replaceAll("\\b" + name1 + "\\b >= ", String.valueOf(tableName) + "." + name2 + " >= ");
        sql = sql.replaceAll("\\b" + name1 + "\\b <= ", String.valueOf(tableName) + "." + name2 + " <= ");
        sql = sql.replaceAll("\\b" + name1 + "\\b > ", String.valueOf(tableName) + "." + name2 + " > ");
        sql = sql.replaceAll("\\b" + name1 + "\\b < ", String.valueOf(tableName) + "." + name2 + " < ");
        sql = sql.replaceAll("\\b" + name1 + "\\b != ", String.valueOf(tableName) + "." + name2 + " != ");
        sql = sql.replaceAll("\\b" + name1 + "\\b LIKE", String.valueOf(tableName) + "." + name2 + " LIKE");
        return sql;
    }

    @Override
    public String processSQLExpressionSinComillas(String sql, String name1, String name2, String tableName) {
        sql = sql.replaceAll("\\b" + name1 + "\\b = ", String.valueOf(tableName) + "." + name2 + " = ");
        sql = sql.replaceAll(String.valueOf(name1) + " IS", String.valueOf(tableName) + "." + name2 + " IS");
        sql = sql.replaceAll("\\b" + name1 + "\\b >= ", String.valueOf(tableName) + "." + name2 + " >= ");
        sql = sql.replaceAll("\\b" + name1 + "\\b <= ", String.valueOf(tableName) + "." + name2 + " <= ");
        sql = sql.replaceAll("\\b" + name1 + "\\b > ", String.valueOf(tableName) + "." + name2 + " > ");
        sql = sql.replaceAll("\\b" + name1 + "\\b < ", String.valueOf(tableName) + "." + name2 + " < ");
        sql = sql.replaceAll("\\b" + name1 + "\\b != ", String.valueOf(tableName) + "." + name2 + " != ");
        sql = sql.replaceAll("\\b" + name1 + "\\b LIKE", String.valueOf(tableName) + "." + name2 + " LIKE");
        return sql;
    }

    @Override
    protected String getSQLForQuery(Envelope envelope, Filter filter) {
        Filter sqlFilter = null;
        sqlFilter = filter != null ? (this.layerFilter != null ? ((Filter)((Cloneable)filter).clone()).and(this.layerFilter) : (Filter)((Cloneable)filter).clone()) : this.layerFilter;
        if (envelope == null && sqlFilter == null && !this.schema.isVersionable()) {
            return this.getRaizConsultaTipo();
        }
        String sqlQuery = String.valueOf(this.getRaizConsultaTipo()) + " WHERE ";
        if (envelope != null) {
            double xmin = envelope.getMinX();
            double xmax = envelope.getMaxX();
            double ymin = envelope.getMinY();
            double ymax = envelope.getMaxY();
            sqlQuery = String.valueOf(sqlQuery) + " intersects(" + this.getFullTableName() + "." + this.escapeAttributeName(this.geomColName) + ", GeomFromText('polygon((" + xmin + " " + ymin + ", " + xmin + " " + ymax + ", " + xmax + " " + ymax + ", " + xmax + " " + ymin + ", " + xmin + " " + ymin + "))')) AND ";
        }
        if (sqlFilter != null) {
            sqlQuery = String.valueOf(sqlQuery) + "(" + this.getSQLExpression(sqlFilter) + ") AND ";
        }
        sqlQuery = this.schema.isVersionable() ? String.valueOf(sqlQuery) + this.schema.getEndDateFilter(this) : sqlQuery.substring(0, sqlQuery.length() - 4);
        LOGGER.debug((Object)sqlQuery);
        return sqlQuery;
    }

    @Override
    protected String getSQLOnlyGeometryForQuery(Envelope rectangle, Filter filter, List<String> orderByFields, boolean asc, List<String> labels) {
        Filter sqlFilter = null;
        sqlFilter = filter != null ? (this.layerFilter != null ? ((Filter)((Cloneable)filter).clone()).and(this.layerFilter) : (Filter)((Cloneable)filter).clone()) : this.layerFilter;
        String geomColumnName = this.schema.getAttributeName(this.schema.getGeometryIndex());
        String sqlQuery = "SELECT asBinary(" + this.getFullTableName() + "." + this.escapeAttributeName(geomColumnName) + ") AS " + geomColumnName + ",";
        if (this.schema.getPrimaryKey() != null) {
            sqlQuery = String.valueOf(sqlQuery) + this.getFullTableName() + "." + this.schema.getPrimaryKeyName() + ",";
        }
        if (labels != null) {
            HashMap processedTableNames = new HashMap();
            for (String campo : labels) {
                Attribute attr = this.schema.getAttribute(campo);
                if (attr == null) {
                    LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.MySQLDataSource.field-{0}-not-valid", new Object[]{campo}));
                    continue;
                }
                if (attr.isCalculated()) {
                    Relation<?> rel = ((AttributeCalculate)attr).getRelation();
                    if (this.isCompatibleRelation(rel)) {
                        String tableName;
                        String aliasTableName = tableName = this.getTableNameOfRelation(rel);
                        HashSet<String> processedFieldNames = (HashSet<String>)processedTableNames.get(tableName);
                        if (processedTableNames.get(tableName) != null) {
                            if (processedFieldNames != null && !processedFieldNames.contains(rel.getSourceAttribute())) {
                                String tempTableName;
                                int cont = 1;
                                while (processedTableNames.get(aliasTableName = "`" + (tempTableName = tableName.substring(1, tableName.length() - 1)) + "_" + cont++ + "`") != null) {
                                }
                            } else {
                                if (processedFieldNames == null) {
                                    processedFieldNames = new HashSet();
                                }
                                processedFieldNames.add(rel.getSourceAttribute());
                            }
                        } else {
                            processedFieldNames = new HashSet<String>();
                            processedFieldNames.add(rel.getSourceAttribute());
                        }
                        sqlQuery = String.valueOf(sqlQuery) + aliasTableName + "." + this.escapeAttributeName(((AttributeCalculate)attr).getRelationFieldName()) + " AS " + this.escapeAttributeName(attr.getName()) + ",";
                        processedTableNames.put(aliasTableName, processedFieldNames);
                        continue;
                    }
                    LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.MySQLDataSource.attribute-{0}-is-not-compatible-with-this-datasource", new Object[]{attr.getName()}));
                    continue;
                }
                sqlQuery = attr.getType().equals(AttributeType.GEOMETRY) ? String.valueOf(sqlQuery) + "asBinary(" + this.getFullTableName() + "." + this.escapeAttributeName(campo) + ") AS " + campo + "," : String.valueOf(sqlQuery) + this.getFullTableName() + "." + this.escapeAttributeName(campo) + ",";
            }
        }
        sqlQuery = String.valueOf(sqlQuery.substring(0, sqlQuery.length() - 1)) + " FROM " + this.getTables();
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
        if (rectangle != null || sqlFilter != null || this.schema.isVersionable()) {
            sqlQuery = String.valueOf(sqlQuery) + " WHERE ";
            if (rectangle != null) {
                double xmin = rectangle.getMinX();
                double xmax = rectangle.getMaxX();
                double ymin = rectangle.getMinY();
                double ymax = rectangle.getMaxY();
                sqlQuery = String.valueOf(sqlQuery) + " intersects(" + this.getFullTableName() + "." + this.escapeAttributeName(this.geomColName) + ", GeomFromText('polygon((" + xmin + " " + ymin + ", " + xmin + " " + ymax + ", " + xmax + " " + ymax + ", " + xmax + " " + ymin + ", " + xmin + " " + ymin + "))')) AND ";
            }
            if (sqlFilter != null) {
                sqlQuery = String.valueOf(sqlQuery) + "(" + this.getSQLExpression(sqlFilter) + ") AND ";
            }
            sqlQuery = this.schema.isVersionable() ? this.schema.getEndDateFilter(this) : sqlQuery.substring(0, sqlQuery.length() - 4);
        }
        if (orderByFields != null && !orderByFields.isEmpty()) {
            sqlQuery = asc ? String.valueOf(sqlQuery) + " ORDER BY " + this.getOrderByExpression(orderByFields) + " ASC" : String.valueOf(sqlQuery) + " ORDER BY " + this.getOrderByExpression(orderByFields) + " DESC";
        }
        LOGGER.debug((Object)sqlQuery);
        return sqlQuery;
    }

    @Override
    public boolean lockFeatures(Collection<Feature> features) throws SQLException {
        return true;
    }

    @Override
    public void clearTransaction() throws SQLException {
    }

    @Override
    protected String getLimitSQL(int limit) {
        String sqlWhere = "";
        if (this.layerFilter != null) {
            sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
        }
        if (this.schema.isVersionable()) {
            sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
        }
        return String.valueOf(this.getRaizConsultaTipo()) + sqlWhere + " LIMIT 0," + limit;
    }

    @Override
    protected void buildGeometrySRID(Connection connection) {
        Connection con = null;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                Statement statement = con.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT SRID(" + this.escapeAttributeName(this.schema.getAttributeName(this.schema.getGeometryIndex())) + ") FROM " + this.getFullTableName() + " LIMIT 0,1");
                this.srid = resultSet.next() ? resultSet.getInt(1) : -1;
                this.closeChannel(resultSet, statement);
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
                this.srid = -1;
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
    }

    @Override
    public void createSpatialIndex() throws Exception {
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            Statement statement = con.createStatement();
            String sql = "ALTER TABLE " + this.getFullTableName() + " ADD SPATIAL INDEX(" + this.escapeAttributeName(this.geomColName) + ")";
            statement.execute(sql);
            this.closeChannel(null, statement);
        }
        finally {
            this.closeConnection(con);
        }
    }

    @Override
    public int toSQLType(AttributeType attrType) {
        return attributeTypeToSQLType.get(attrType);
    }

    @Override
    public Feature readOptimizedFeature(ResultSet resultSet, List<String> labels, boolean ignored) throws SQLException {
        WKBParser2 parser = new WKBParser2();
        BasicFeature featPK = new BasicFeature(this.schema);
        Object objetoBaseDatos = null;
        byte[] data = resultSet.getBytes(this.schema.getAttributeName(this.schema.getGeometryIndex()));
        objetoBaseDatos = data == null ? factory.createGeometryCollection(null) : parser.parse(data);
        featPK.setGeometry((Geometry)objetoBaseDatos);
        if (this.schema.getPrimaryKey() != null) {
            String pkName = this.schema.getPrimaryKeyName();
            objetoBaseDatos = resultSet.getObject(pkName);
            featPK.setAttribute(pkName, objetoBaseDatos);
        }
        if (labels != null) {
            HashSet<AttributeCalculate> attrsCalculates = new HashSet<AttributeCalculate>();
            for (String campo : labels) {
                Attribute attr = this.getSchema().getAttribute(campo);
                if (!attr.isCalculated() || this.isCompatibleRelation(((AttributeCalculate)attr).getRelation())) {
                    objetoBaseDatos = attr.getType().equals(AttributeType.GEOMETRY) ? ((data = resultSet.getBytes(attr.getName())) == null ? factory.createGeometryCollection(null) : parser.parse(data)) : resultSet.getObject(campo);
                    featPK.setAttribute(campo, objetoBaseDatos);
                    continue;
                }
                attrsCalculates.add((AttributeCalculate)attr);
            }
            for (AttributeCalculate attrCalculate : attrsCalculates) {
                Relation<?> relation = attrCalculate.getRelation();
                String linkAttribute = relation.getSourceAttribute();
                Object value = featPK.getAttribute(linkAttribute);
                Object relationValue = relation.getFieldValue(attrCalculate.getRelationFieldName(), value);
                featPK.setAttribute(attrCalculate.getName(), relationValue);
            }
        }
        if (!ignored) {
            return this.getRealFeature(featPK);
        }
        return featPK;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MySQLDataSource)) {
            return false;
        }
        AbstractJDBCDataSource otherDS = (AbstractJDBCDataSource)other;
        return this.getHostName().equals(otherDS.getHostName()) && this.getPort() == otherDS.getPort() && this.getDataBase().equals(otherDS.getDataBase()) && this.getUserName().equals(otherDS.getUserName()) && this.getPassword().equals(otherDS.getPassword());
    }

    @Override
    public void createTemporalFields(String startField, String endField, String versionField) throws SQLException {
        String sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + this.escapeAttributeName(startField) + " timestamp NULL DEFAULT current_timestamp";
        this.executeNonFeatureQuery(sql1);
        sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + this.escapeAttributeName(endField) + " timestamp NULL DEFAULT NULL";
        this.executeNonFeatureQuery(sql1);
        sql1 = "CREATE INDEX " + this.getFullTableName() + "_" + startField + "_idx ON " + this.getFullTableName() + "(" + this.escapeAttributeName(startField) + ")";
        this.executeNonFeatureQuery(sql1);
        sql1 = "CREATE INDEX " + this.getFullTableName() + "_" + endField + "_idx ON " + this.getFullTableName() + "(" + this.escapeAttributeName(endField) + ")";
        this.executeNonFeatureQuery(sql1);
        if (StringUtils.isNotEmpty((String)versionField)) {
            AttributeType pkType = this.getSchema().getPrimaryKey().getType();
            String attributeTypeName = attributeTypeToDBType.get(pkType);
            sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + this.escapeAttributeName(versionField) + " " + attributeTypeName;
            this.executeNonFeatureQuery(sql1);
        }
        this.labels = null;
    }

    @Override
    protected String getTables() {
        HashSet<String> tables = new HashSet<String>();
        HashMap processedTableNames = new HashMap();
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (attr instanceof AttributeCalculate) {
                AttributeCalculate attrCalculate = (AttributeCalculate)attr;
                Relation<?> rel = attrCalculate.getRelation();
                if (rel instanceof TableRelation) {
                    TableDBRecordDataSource tableDS;
                    TableRelation tableRel = (TableRelation)rel;
                    Table table = ((TableRelation)rel).getTable();
                    if (table.getDataSource() instanceof TableDBRecordDataSource && this.checkHost((tableDS = (TableDBRecordDataSource)table.getDataSource()).getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword())) {
                        String tableName = ((TableDBRecordDataSource)table.getDataSource()).getFullTableName();
                        HashSet<String> processedFieldNames = (HashSet<String>)processedTableNames.get(tableName);
                        String aliasTableName = "";
                        if (processedTableNames.get(tableName) != null) {
                            if (processedFieldNames != null && !processedFieldNames.contains(tableRel.getSourceAttribute())) {
                                String tempTableName;
                                int cont = 1;
                                while (processedTableNames.get(aliasTableName = "`" + (tempTableName = tableName.substring(1, tableName.length() - 1)) + "_" + cont++ + "`") != null) {
                                }
                            } else {
                                if (processedFieldNames == null) {
                                    processedFieldNames = new HashSet();
                                }
                                processedFieldNames.add(tableRel.getSourceAttribute());
                            }
                        } else {
                            processedFieldNames = new HashSet<String>();
                            processedFieldNames.add(tableRel.getSourceAttribute());
                        }
                        String subConsulta = "LEFT JOIN " + tableName;
                        if (!aliasTableName.equals("")) {
                            subConsulta = String.valueOf(subConsulta) + " AS " + aliasTableName;
                        }
                        subConsulta = String.valueOf(subConsulta) + " ON (";
                        subConsulta = String.valueOf(subConsulta) + this.getFullTableName() + "." + this.escapeAttributeName(tableRel.getSourceAttribute()) + "=";
                        subConsulta = !aliasTableName.equals("") ? String.valueOf(subConsulta) + aliasTableName : String.valueOf(subConsulta) + tableName;
                        subConsulta = String.valueOf(subConsulta) + "." + this.escapeAttributeName(tableRel.getAttributeTarget()) + ")";
                        tables.add(subConsulta);
                        if (!aliasTableName.equals("")) {
                            processedTableNames.put(aliasTableName, processedFieldNames);
                        } else {
                            processedTableNames.put(tableName, processedFieldNames);
                        }
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    LayerRelation layerRel = (LayerRelation)rel;
                    Layer layer = layerRel.getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this)) {
                        String subConsulta = "LEFT JOIN " + layerDS.getFullTableName() + " ON (";
                        subConsulta = String.valueOf(subConsulta) + this.getFullTableName() + "." + this.escapeAttributeName(layerRel.getSourceAttribute()) + "=" + layerDS.getFullTableName() + "." + this.escapeAttributeName(layerRel.getAttributeTarget()) + ")";
                        tables.add(subConsulta);
                    }
                }
            }
            ++i;
        }
        String resultado = String.valueOf(this.getFullTableName()) + " ";
        for (String element : tables) {
            resultado = String.valueOf(resultado) + element + " ";
        }
        return resultado;
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys, boolean ignoredUpdate) {
        ArrayList<Feature> featList = new ArrayList<Feature>();
        if (ArrayUtils.isEmpty((Object[])keys)) {
            return featList;
        }
        ArrayList<Object> realKeys = new ArrayList<Object>();
        int i = 0;
        while (i < keys.length) {
            if (keys[i] != null) {
                if (keys[i] instanceof Feature) {
                    featList.add((Feature)keys[i]);
                } else {
                    realKeys.add(keys[i]);
                }
            }
            ++i;
        }
        Filter filter = this.getFilterByPrimaryKey(realKeys.toArray());
        if (filter == null) {
            return featList;
        }
        String sql = this.getSQLOnlyGeometryForQuery(null, filter, null, true, this.getAllLabels());
        JDBCIterator iterator = null;
        try {
            try {
                if (this.editable) {
                    iterator = new MySQLEditableIterator(sql, filter, this, this.getAllLabels());
                    iterator.setIgnoredUpdate(ignoredUpdate);
                } else {
                    iterator = new MySQLIterator(sql, filter, this, this.getAllLabels());
                }
                while (iterator.hasNext()) {
                    Feature readFeature = iterator.next();
                    featList.add(readFeature);
                }
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                if (iterator != null) {
                    iterator.close();
                }
                return null;
            }
        }
        finally {
            if (iterator != null) {
                iterator.close();
            }
        }
        return featList;
    }

    @Override
    public String getFullTableName() {
        return "`" + this.tableName + "`";
    }

    @Override
    protected String getSQLRoot() {
        String solucion = "";
        int numAtributos = this.schema.getAttributeCount();
        int i = 0;
        while (i < numAtributos) {
            Attribute attr = this.schema.getAttribute(i);
            if (attr instanceof AttributeCalculate) {
                AttributeCalculate attrCalculate = (AttributeCalculate)attr;
                Relation<?> rel = attrCalculate.getRelation();
                if (rel instanceof TableRelation) {
                    TableDBRecordDataSource tableDS;
                    TableRelation tableRel = (TableRelation)rel;
                    if (tableRel.getTable().getDataSource() instanceof TableDBRecordDataSource && this.checkHost((tableDS = (TableDBRecordDataSource)tableRel.getTable().getDataSource()).getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword())) {
                        solucion = String.valueOf(solucion) + tableDS.getFullTableName() + "." + this.escapeAttributeName(attrCalculate.getRelationFieldName()) + " AS " + this.escapeAttributeName(attrCalculate.getName()) + ",";
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    LayerRelation layerRel = (LayerRelation)rel;
                    Layer layer = layerRel.getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this)) {
                        solucion = String.valueOf(solucion) + layerDS.getFullTableName() + "." + this.escapeAttributeName(attrCalculate.getRelationFieldName()) + " AS " + this.escapeAttributeName(attrCalculate.getName()) + ",";
                    }
                }
            } else {
                AttributeType attrType = this.schema.getAttributeType(i);
                solucion = attrType == AttributeType.GEOMETRY ? String.valueOf(solucion) + " asBinary(" + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getAttributeName(i)) + ") AS " + this.escapeAttributeName(this.schema.getAttributeName(i)) + "," : String.valueOf(solucion) + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getAttributeName(i)) + ",";
            }
            ++i;
        }
        return solucion.substring(0, solucion.length() - 1);
    }

    @Override
    protected String getSQLForDropTable() {
        String sql = "DROP TABLE IF EXISTS " + this.getFullTableName();
        return sql;
    }

    @Override
    public Object clone() {
        MySQLDataSource clone = new MySQLDataSource(this.getHostName(), this.getPort(), this.getDataBase(), this.getUserName(), this.getPassword());
        if (this.getSchema() != null) {
            clone.setSchema((FeatureSchema)this.getSchema().clone());
        }
        clone.dataBaseSchema = this.getDataBaseSchema();
        clone.tableName = this.getTableName();
        clone.pkName = this.getPkName();
        clone.geomColName = this.getGeomColName();
        clone.setInMemory(this.inMemory);
        clone.setEditable(this.isEditable());
        clone.size = this.size;
        clone.envelope = this.envelope;
        Filter layerFilter = null;
        if (this.getLayerFilter() != null) {
            layerFilter = (Filter)((Cloneable)this.getLayerFilter()).clone();
        }
        clone.setLayerFilter(layerFilter);
        clone.srid = this.getSrid();
        return clone;
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        if (!this.schema.isVersionable()) {
            return new ArrayList<Feature>();
        }
        ArrayList<Feature> features = new ArrayList<Feature>();
        try {
            CompareFilterImpl compareFilter1 = new CompareFilterImpl(14);
            compareFilter1.addLeftValue(new AttributeExpressionImpl2(this.schema.getHistoryField()));
            compareFilter1.addRightValue(new LiteralExpressionImpl(((Number)pkId).toString()));
            CompareFilterImpl compareFilter2 = new CompareFilterImpl(14);
            compareFilter2.addLeftValue(new AttributeExpressionImpl2(this.schema.getPrimaryKeyName()));
            compareFilter2.addRightValue(new LiteralExpressionImpl(((Number)pkId).toString()));
            Filter finalFilter = compareFilter1.or(compareFilter2);
            if (filter != null) {
                finalFilter = finalFilter.and(filter);
            }
            String sql = this.getRaizConsultaTipo();
            SQLEncoderMySQL encoder = new SQLEncoderMySQL();
            sql = String.valueOf(sql) + " " + encoder.encode(finalFilter);
            JDBCIterator it = null;
            try {
                it = this.editable ? new MySQLEditableIterator(sql, (Filter)((Cloneable)filter).clone(), this, this.getAllLabels()) : new MySQLIterator(sql, (Filter)((Cloneable)filter).clone(), this, this.getAllLabels());
                while (it.hasNext()) {
                    features.add(it.next());
                }
            }
            finally {
                if (it != null) {
                    it.close();
                }
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return features;
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, boolean ascending, List<String> labels) {
        Filter geometryFilter = this.getViewBoxFilter(rectangle);
        if (filter != null) {
            geometryFilter = geometryFilter != null ? geometryFilter.and((Filter)((Cloneable)filter).clone()) : (Filter)((Cloneable)filter).clone();
        }
        boolean check = this.checkIfFilterIsCompatible(filter);
        String sql = "";
        sql = check ? this.getSQLOnlyGeometryForQuery(rectangle, filter, orderByFields, ascending, labels) : this.getSQLOnlyGeometryForQuery(rectangle, null, orderByFields, ascending, labels);
        if (this.editable) {
            return new MySQLEditableIterator(sql, geometryFilter, this, labels, !check);
        }
        return new MySQLIterator(sql, geometryFilter, this, labels, !check);
    }

    @Override
    protected String getSQLForAggregateFunction(String field, String operatorName, int majorVersion, int minorVersion) {
        String functionName = (String)AGGREGATE_FUNCTIONS_MAP.get(operatorName);
        if (functionName == null) {
            return null;
        }
        if (("OP_VARIANCE".equals(operatorName) || "OP_STANDARD_DEVIANCE".equals(operatorName)) && (majorVersion > 5 || majorVersion == 5 && minorVersion >= 0)) {
            if ("OP_VARIANCE".equals(operatorName)) {
                functionName = "var_samp";
            } else if ("OP_STANDARD_DEVIANCE".equals(operatorName)) {
                functionName = "stddev_samp";
            }
        }
        return String.valueOf(functionName) + "(" + this.escapeAttributeName(field) + ")";
    }

    @Override
    protected boolean isCompatibleRelation(Relation<?> rel) {
        if (rel instanceof TableRelation) {
            Table table = ((TableRelation)rel).getTable();
            if (table.getDataSource() instanceof org.saig.core.model.data.dao.jdbc.MySQLDataSource) {
                TableDBRecordDataSource tableDS = (TableDBRecordDataSource)table.getDataSource();
                return this.checkHost(tableDS.getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword());
            }
        } else {
            AbstractJDBCDataSource layerDS;
            LayerRelation layerRel = (LayerRelation)rel;
            Layer layer = layerRel.getTargetLayer();
            if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()) instanceof PostGisDataSource) {
                return this.checkHost(layerDS.getHostName()) && layerDS.getPort() == this.getPort() && layerDS.getDataBase().equals(this.getDataBase()) && layerDS.getDataBaseSchema().equals(this.getDataBaseSchema()) && layerDS.getUserName().equals(this.getUserName()) && layerDS.getPassword().equals(this.getPassword());
            }
        }
        return false;
    }

    @Override
    protected String escapePkValueIfNeeded(String value) {
        return value;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPool() {
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(this.hostName);
        dataSource.setPortNumber(this.port);
        dataSource.setDatabaseName(this.dataBase);
        dataSource.setUser(this.userName);
        dataSource.setPassword(this.password);
        return dataSource;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDataBaseName() {
        return this.dataBase;
    }

    @Override
    public int getDefaultPort() {
        return 3306;
    }

    @Override
    protected String escapeAttributeName(String attrName) {
        return "`" + attrName + "`";
    }

    @Override
    public String convertAttributeTypeToDBType(AttributeType type) {
        return attributeTypeToDBType.get(type);
    }

    @Override
    public String getSQLForAlterColumnName(String oldName, String newName, AttributeType type) {
        StringBuffer sql = new StringBuffer();
        sql.append("ALTER TABLE " + this.getFullTableName() + " CHANGE COLUMN " + this.escapeAttributeName(oldName) + " " + this.escapeAttributeName(newName) + " " + attributeTypeToDBType.get(type));
        return sql.toString();
    }

    @Override
    public Envelope getViewBox() {
        if (this.envelope == null) {
            Envelope vista;
            block10: {
                vista = new Envelope();
                FeatureIterator geomIterator = null;
                try {
                    try {
                        geomIterator = this.queryGeometryIterator(null, this.layerFilter, null);
                        while (geomIterator.hasNext()) {
                            Geometry geom = geomIterator.next().getGeometry();
                            if (geom == null) continue;
                            vista.expandToInclude(geom.getEnvelopeInternal());
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (geomIterator != null) {
                            geomIterator.close();
                        }
                        break block10;
                    }
                }
                catch (Throwable throwable) {
                    if (geomIterator != null) {
                        geomIterator.close();
                    }
                    throw throwable;
                }
                if (geomIterator != null) {
                    geomIterator.close();
                }
            }
            this.envelope = vista;
        }
        LOGGER.debug((Object)("Vista " + this.envelope));
        Envelope fullEnvelope = this.expandEnvelope(this.envelope, this.getNewFeatures());
        return this.expandEnvelope(fullEnvelope, this.getUpdatedFeatures());
    }
}

