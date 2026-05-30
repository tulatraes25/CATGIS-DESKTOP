/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.TopologyException
 *  com.vividsolutions.jts.io.WKTWriter
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 *  org.postgis.PGbox2d
 *  org.postgis.PGbox3d
 *  org.postgresql.ds.PGConnectionPoolDataSource
 */
package org.saig.core.dao.datasource.dbdatasource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.feature.ILayerIterator;
import com.vividsolutions.jump.workbench.model.Layer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.ConnectionPoolDataSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.postgis.PGbox2d;
import org.postgis.PGbox3d;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.IPoolableDBDataSource;
import org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator;
import org.saig.core.dao.datasource.dbdatasource.iterators.PostGisEditableIterator;
import org.saig.core.dao.datasource.dbdatasource.iterators.PostGisIterator;
import org.saig.core.dao.datasource.dbdatasource.iterators.full.PostGisFullIterator;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;
import org.saig.core.dao.datasource.dbdatasource.utils.WKBParser2;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.SQLEncoderException;
import org.saig.core.filter.SQLEncoderPostgisGeos;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.jdbc.PostgreSQLDataSource;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.TableRelation;
import org.saig.jump.lang.I18N;

public class PostGisDataSource
extends AbstractJDBCDataSource
implements IPoolableDBDataSource {
    public static final Logger LOGGER = Logger.getLogger(PostGisDataSource.class);
    public static final String ID = "PostgreSQL";
    public static Map<AttributeType, String> attributeTypeToDBType = new HashMap<AttributeType, String>();
    public static Map<AttributeType, Integer> attributeTypeToSQLType = new HashMap<AttributeType, Integer>();
    protected static final String ASSTEWKB_FUNCTION_NAME = "st_asewkb";
    protected static final String ASEWKB_FUNCTION_NAME = "asewkb";
    protected static final String ASBINARY_FUNCTION_NAME = "asbinary";
    protected static final String GEOMETRY_FROM_TEXT_FUNCTION_NAME = "geometryfromtext";
    protected static final String ST_GEOMETRY_FROM_TEXT_FUNCTION_NAME = "st_geometryfromtext";
    protected static final String EXTENT_FUNCTION_NAME = "extent";
    protected static final String ST_EXTENT_FUNCTION_NAME = "st_extent";
    protected static final String SRID_FUNCTION_NAME = "srid";
    protected static final String ST_SRID_FUNCTION_NAME = "st_srid";
    protected static final String DISTANCE_FUNCTION_NAME = "distance";
    protected static final String ST_DISTANCE_FUNCTION_NAME = "st_distance";
    protected String queryFunction = "st_asewkb";
    protected String geometryFromTextFunction = "st_geometryfromtext";
    protected String extentFunction = "st_extent";
    protected String sridFunction = "st_srid";
    protected String distanceFunction = "st_distance";

    static {
        attributeTypeToDBType.put(AttributeType.BIGINT, "bigint");
        attributeTypeToDBType.put(AttributeType.BIT, "bit");
        attributeTypeToDBType.put(AttributeType.BOOLEAN, "boolean");
        attributeTypeToDBType.put(AttributeType.CHAR, "character(1)");
        attributeTypeToDBType.put(AttributeType.DATE, "date");
        attributeTypeToDBType.put(AttributeType.DECIMAL, "decimal");
        attributeTypeToDBType.put(AttributeType.DOUBLE, "double precision");
        attributeTypeToDBType.put(AttributeType.FLOAT, "float(6)");
        attributeTypeToDBType.put(AttributeType.GEOMETRY, "geometry");
        attributeTypeToDBType.put(AttributeType.INTEGER, "integer");
        attributeTypeToDBType.put(AttributeType.LONG, "int8");
        attributeTypeToDBType.put(AttributeType.LONGVARCHAR, "text");
        attributeTypeToDBType.put(AttributeType.TEXT, "text");
        attributeTypeToDBType.put(AttributeType.NUMERIC, "numeric");
        attributeTypeToDBType.put(AttributeType.OBJECT, "bytea");
        attributeTypeToDBType.put(AttributeType.REAL, "real");
        attributeTypeToDBType.put(AttributeType.SMALLINT, "smallint");
        attributeTypeToDBType.put(AttributeType.STRING, "text");
        attributeTypeToDBType.put(AttributeType.TIME, "time");
        attributeTypeToDBType.put(AttributeType.TIMESTAMP, "timestamp");
        attributeTypeToDBType.put(AttributeType.TINYINT, "int2");
        attributeTypeToDBType.put(AttributeType.VARCHAR, "text");
        attributeTypeToDBType.put(AttributeType.TIME, "time");
        attributeTypeToSQLType.put(AttributeType.BIGINT, new Integer(-5));
        attributeTypeToSQLType.put(AttributeType.BIT, new Integer(-7));
        attributeTypeToSQLType.put(AttributeType.BOOLEAN, new Integer(16));
        attributeTypeToSQLType.put(AttributeType.CHAR, new Integer(1));
        attributeTypeToSQLType.put(AttributeType.DATE, new Integer(91));
        attributeTypeToSQLType.put(AttributeType.DECIMAL, new Integer(3));
        attributeTypeToSQLType.put(AttributeType.DOUBLE, new Integer(8));
        attributeTypeToSQLType.put(AttributeType.FLOAT, new Integer(6));
        attributeTypeToSQLType.put(AttributeType.INTEGER, new Integer(4));
        attributeTypeToSQLType.put(AttributeType.LONG, new Integer(4));
        attributeTypeToSQLType.put(AttributeType.LONGVARCHAR, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.TEXT, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.NUMERIC, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.OBJECT, new Integer(2004));
        attributeTypeToSQLType.put(AttributeType.REAL, new Integer(7));
        attributeTypeToSQLType.put(AttributeType.SMALLINT, new Integer(5));
        attributeTypeToSQLType.put(AttributeType.STRING, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.TIME, new Integer(92));
        attributeTypeToSQLType.put(AttributeType.TIMESTAMP, new Integer(93));
        attributeTypeToSQLType.put(AttributeType.TINYINT, new Integer(4));
        attributeTypeToSQLType.put(AttributeType.VARCHAR, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.TIME, new Integer(92));
    }

    public PostGisDataSource() {
    }

    public PostGisDataSource(String host, int port, String databaseName, String tableName, String username, String password) {
        super(host, port, databaseName, username, password);
        this.setTableName(tableName);
    }

    public PostGisDataSource(String host, int port, String databaseName, String username, String password) {
        super(host, port, databaseName, username, password);
    }

    public static PostGisDataSource getInstance(String host, int port, String schema, String tableName, String username, String password) {
        return new PostGisDataSource(host, port, schema, tableName, username, password);
    }

    @Override
    protected boolean buildGeometryType(Connection con) {
        boolean useSuperMethod;
        boolean ok;
        block20: {
            ResultSet resultSet = null;
            Statement statement = null;
            ok = false;
            useSuperMethod = false;
            try {
                try {
                    statement = con.createStatement(1003, 1007);
                    resultSet = statement.executeQuery("SELECT type, coord_dimension FROM \"public\".\"geometry_columns\" WHERE f_table_schema='" + this.dataBaseSchema + "' AND f_table_name='" + this.tableName + "' AND f_geometry_column='" + this.geomColName + "'");
                    String type = "";
                    int coordDim = 2;
                    if (resultSet.next()) {
                        type = resultSet.getString(1);
                        coordDim = resultSet.getInt(2);
                        this.set3d(coordDim == 3);
                        if (type.equalsIgnoreCase("POINT")) {
                            this.schema.setGeometryType(1);
                            ok = true;
                        } else if (type.equalsIgnoreCase("MULTIPOINT")) {
                            this.schema.setGeometryType(8);
                            ok = true;
                        } else if (type.equalsIgnoreCase("POLYGON")) {
                            this.schema.setGeometryType(5);
                            ok = true;
                        } else if (type.equalsIgnoreCase("MULTIPOLYGON")) {
                            this.schema.setGeometryType(4);
                            ok = true;
                        } else if (type.equalsIgnoreCase("LINESTRING")) {
                            this.schema.setGeometryType(3);
                            ok = true;
                        } else if (type.equalsIgnoreCase("MULTILINESTRING")) {
                            this.schema.setGeometryType(2);
                            ok = true;
                        } else {
                            useSuperMethod = true;
                        }
                    } else {
                        useSuperMethod = true;
                    }
                }
                catch (Exception sqlException) {
                    LOGGER.error((Object)"", (Throwable)sqlException);
                    useSuperMethod = true;
                    this.closeChannel(resultSet, statement);
                    break block20;
                }
            }
            catch (Throwable throwable) {
                this.closeChannel(resultSet, statement);
                throw throwable;
            }
            this.closeChannel(resultSet, statement);
        }
        if (useSuperMethod) {
            ok = super.buildGeometryType(con);
        }
        return ok;
    }

    @Override
    public synchronized void addAll(Collection<Feature> features, boolean setId, boolean checkVersionable) throws Exception {
        if (!this.editable || CollectionUtils.isEmpty(features)) {
            return;
        }
        if (this.inMemory) {
            for (Feature element : features) {
                if (element.isUnsaved()) {
                    this.newFeatures.add(element);
                } else {
                    this.updateFeatures.add(element);
                }
                if (!this.deletedFeatures.contains(element)) continue;
                this.deletedFeatures.remove(element);
            }
            return;
        }
        Connection connection = null;
        PreparedStatement statement = null;
        Object object = null;
        String sql = "";
        try {
            try {
                if (this.connectionTransaction == null) {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    connection.setAutoCommit(false);
                } else {
                    connection = this.connectionTransaction;
                }
                IDBKeyResolver keyResolver = this.getKeyResolver(connection);
                features = this.getCorrectGeometries(features);
                for (Feature feature : features) {
                    if (setId || feature.getPrimaryKey() == null) {
                        this.processFeaturePrimaryKey(keyResolver, connection, feature);
                    }
                    try {
                        if (checkVersionable && this.schema.isVersionable()) {
                            feature.setAttribute(this.schema.getFieldStartDate(), (Object)new java.util.Date());
                        }
                        if ((sql = this.getSQLForInsert(feature)) == null) continue;
                        statement = connection.prepareStatement(sql);
                        int position = 1;
                        int i = 0;
                        while (i < this.schema.getAttributeCount()) {
                            Attribute attr = this.schema.getAttribute(i);
                            if (!(attr instanceof AttributeCalculate) && !attr.getType().equals(AttributeType.GEOMETRY)) {
                                Object value = feature.getAttribute(attr.getName());
                                if (value == null || value.toString().trim().length() == 0) {
                                    statement.setNull(position, this.toSQLType(this.schema.getAttribute(i).getType()));
                                } else {
                                    if (value instanceof java.util.Date) {
                                        AttributeType type = this.schema.getAttribute(i).getType();
                                        value = type == AttributeType.TIMESTAMP || type == AttributeType.TIME ? FeatureUtil.getGoodAttribute(type, value) : new Date(((java.util.Date)value).getTime());
                                    } else if (attr.getType().equals(AttributeType.BOOLEAN)) {
                                        if (value instanceof Number) {
                                            value = new Boolean(((Number)value).intValue() != 0);
                                        } else if (value instanceof String) {
                                            String strValue = (String)value;
                                            value = strValue.length() == 1 ? Boolean.valueOf(!strValue.toLowerCase().equals("f")) : Boolean.valueOf(!strValue.toLowerCase().startsWith("fals"));
                                        }
                                    }
                                    statement.setObject(position, value);
                                }
                                ++position;
                            }
                            ++i;
                        }
                        statement.execute();
                        statement.close();
                    }
                    catch (TopologyException ex2) {
                        LOGGER.error((Object)"", (Throwable)ex2);
                    }
                }
                if (this.connectionTransaction == null) {
                    connection.commit();
                }
            }
            catch (SQLException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                if (object != null) {
                    LOGGER.warn((Object)object.toString());
                }
                this.closeChannel(null, statement);
                this.connectionRollback(connection);
                throw ex;
            }
        }
        finally {
            if (this.connectionTransaction == null) {
                this.closeConnection(connection);
            }
        }
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
        Feature feature2 = null;
        try {
            try {
                if (this.connectionTransaction == null) {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    connection.setAutoCommit(false);
                } else {
                    connection = this.connectionTransaction;
                }
                for (Feature feature2 : features) {
                    String sqlForUpdate = this.getSQLForUpdate(feature2);
                    if (sqlForUpdate == null) continue;
                    if (this.schema.isVersionable() && this.schema.getHistoryField() != null) {
                        this.updateHistoryFeature(feature2, oldFeatures);
                    }
                    preparedStatement = connection.prepareStatement(sqlForUpdate);
                    FeatureSchema featureSchema = feature2.getSchema();
                    int position = 1;
                    int i = 0;
                    while (i < featureSchema.getAttributeCount()) {
                        Attribute attr = this.schema.getAttribute(i);
                        if (!(attr instanceof AttributeCalculate) && !attr.getType().equals(AttributeType.GEOMETRY)) {
                            Object value = feature2.getAttribute(i);
                            if (value == null || value.toString().trim().length() == 0) {
                                preparedStatement.setNull(position, this.toSQLType(featureSchema.getAttribute(i).getType()));
                            } else {
                                if (value instanceof java.util.Date) {
                                    AttributeType type = this.schema.getAttribute(i).getType();
                                    value = type == AttributeType.TIMESTAMP || type == AttributeType.TIME ? FeatureUtil.getGoodAttribute(type, value) : new Date(((java.util.Date)value).getTime());
                                } else if (attr.getType().equals(AttributeType.BOOLEAN)) {
                                    if (value instanceof Number) {
                                        value = new Boolean(((Number)value).intValue() != 0);
                                    } else if (value instanceof String) {
                                        String strValue = (String)value;
                                        value = strValue.length() == 1 ? Boolean.valueOf(!strValue.toLowerCase().equals("f")) : Boolean.valueOf(!strValue.toLowerCase().startsWith("fals"));
                                    }
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
                if (this.connectionTransaction == null) {
                    connection.commit();
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                LOGGER.warn((Object)I18N.getMessage(PostGisDataSource.class, "update-of-feature-{0}-failed", new Object[]{feature2}));
                this.closeChannel(null, preparedStatement);
                connection.rollback();
                throw e;
            }
        }
        finally {
            if (this.connectionTransaction == null) {
                this.closeConnection(connection);
            }
        }
        if (!oldFeatures.isEmpty()) {
            this.addAll(oldFeatures, true, false);
        }
    }

    @Override
    protected String getSQLForQuery(Envelope envelope, Filter filter) {
        Filter sqlFilter = null;
        sqlFilter = filter != null ? (this.layerFilter != null ? ((Filter)((Cloneable)filter).clone()).and(this.layerFilter) : (Filter)((Cloneable)filter).clone()) : this.layerFilter;
        if (envelope == null && sqlFilter == null && !this.schema.isVersionable()) {
            return this.getRaizConsultaTipo();
        }
        String consultaSQL = String.valueOf(this.getRaizConsultaTipo()) + " WHERE ";
        if (envelope != null) {
            consultaSQL = String.valueOf(consultaSQL) + this.getFullTableName() + "." + this.escapeAttributeName(this.geomColName) + " && " + this.geometryFromTextFunction + "('POLYGON((" + envelope.getMinX() + " " + envelope.getMinY() + ", " + envelope.getMaxX() + " " + envelope.getMinY() + ", " + envelope.getMaxX() + " " + envelope.getMaxY() + ", " + envelope.getMinX() + " " + envelope.getMaxY() + ", " + envelope.getMinX() + " " + envelope.getMinY() + "))'," + this.srid + ") AND ";
        }
        if (sqlFilter != null) {
            consultaSQL = String.valueOf(consultaSQL) + "(" + this.getSQLExpression(sqlFilter) + ") AND ";
        }
        consultaSQL = this.schema.isVersionable() ? String.valueOf(consultaSQL) + this.schema.getEndDateFilter(this) : consultaSQL.substring(0, consultaSQL.length() - 4);
        LOGGER.debug((Object)consultaSQL);
        return consultaSQL;
    }

    @Override
    protected String getSQLExpression(Filter filter) {
        String sqlExpression = "";
        if (filter != null) {
            SQLEncoderPostgisGeos encoder = new SQLEncoderPostgisGeos(this.srid, this.schema.getGeometryType());
            try {
                sqlExpression = encoder.encode(filter);
                sqlExpression = sqlExpression.replaceFirst("WHERE", "");
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
                    sqlExpression = this.processSQLExpression(sqlExpression, attr.getName(), attr.getName(), this.getFullTableName());
                }
                for (AttributeCalculate attrCalculate : attrCal) {
                    Relation<?> rel = attrCalculate.getRelation();
                    String tableName = this.getTableNameOfRelation(rel);
                    if (tableName.equals("")) continue;
                    sqlExpression = this.processSQLExpression(sqlExpression, attrCalculate.getName(), attrCalculate.getRelationFieldName(), tableName);
                }
            }
            catch (SQLEncoderException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return sqlExpression;
    }

    @Override
    public String processSQLExpression(String sql, String name1, String name2, String tableName) {
        StringBuffer buf = new StringBuffer();
        String searchStr = "\"" + name1 + "\"";
        int index = sql.indexOf(searchStr);
        int currPos = 0;
        while (index != -1) {
            buf.append(sql.substring(currPos, index));
            currPos = index + searchStr.length();
            if (sql.length() > currPos && sql.charAt(currPos) != '.') {
                buf.append(String.valueOf(tableName) + ".\"" + name2 + "\"");
            } else {
                buf.append(searchStr);
            }
            index = sql.indexOf("\"" + name1 + "\"", currPos);
        }
        buf.append(sql.substring(currPos));
        return buf.toString();
    }

    @Override
    public String processSQLExpressionSinComillas(String sql, String name1, String name2, String tableName) {
        StringBuffer buf = new StringBuffer();
        String searchStr = name1;
        int index = sql.indexOf(searchStr);
        int currPos = 0;
        while (index != -1) {
            buf.append(sql.substring(currPos, index));
            currPos = index + searchStr.length();
            if (sql.length() > currPos && sql.charAt(currPos) != '.' && sql.charAt(currPos) != '\"') {
                buf.append(String.valueOf(tableName) + ".\"" + name2 + "\"");
            } else {
                buf.append(searchStr);
            }
            index = sql.indexOf("\"" + name1 + "\"", currPos);
        }
        buf.append(sql.substring(currPos));
        return buf.toString();
    }

    @Override
    protected String getSQLOnlyGeometryForQuery(Envelope rectangle, Filter filter, List<String> orderByFields, boolean asc, List<String> labels) {
        Filter sqlFilter = null;
        sqlFilter = filter != null ? (this.layerFilter != null ? ((Filter)((Cloneable)filter).clone()).and(this.layerFilter) : (Filter)((Cloneable)filter).clone()) : this.layerFilter;
        String sqlQuery = "SELECT " + this.getGeometryQueryString() + ",";
        if (this.schema.getPrimaryKey() != null) {
            sqlQuery = String.valueOf(sqlQuery) + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + ",";
        }
        if (labels != null) {
            HashMap processedTableNames = new HashMap();
            for (String campo : labels) {
                Attribute attr = this.schema.getAttribute(campo);
                if (attr == null) continue;
                if (attr.isCalculated()) {
                    Relation<?> rel = ((AttributeCalculate)attr).getRelation();
                    if (this.isCompatibleRelation(rel)) {
                        String tableName = this.getTableNameOfRelation(rel, true);
                        String simpleTableName = this.getTableNameOfRelation(rel, false);
                        String aliasTableName = tableName;
                        HashSet<String> processedFieldNames = (HashSet<String>)processedTableNames.get(tableName);
                        if (processedTableNames.get(tableName) != null) {
                            if (processedFieldNames != null && !processedFieldNames.contains(rel.getSourceAttribute())) {
                                int cont = 1;
                                while (processedTableNames.get(aliasTableName = "\"" + simpleTableName + "_" + cont++ + "\"") != null) {
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
                    LOGGER.warn((Object)I18N.getMessage(PostGisDataSource.class, "attribute-{0}-is-not-compatible-with-this-data-source", new Object[]{attr.getName()}));
                    continue;
                }
                sqlQuery = attr.getType().equals(AttributeType.GEOMETRY) ? String.valueOf(sqlQuery) + this.queryFunction + "(" + this.getFullTableName() + "." + this.escapeAttributeName(attr.getName()) + ",'XDR') AS " + this.escapeAttributeName(campo) + "," : String.valueOf(sqlQuery) + this.getFullTableName() + "." + this.escapeAttributeName(campo) + ",";
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
                sqlQuery = String.valueOf(sqlQuery) + this.getFullTableName() + "." + this.escapeAttributeName(this.geomColName) + " && " + this.geometryFromTextFunction + "('POLYGON((" + xmin + " " + ymin + ", " + xmax + " " + ymin + ", " + xmax + " " + ymax + ", " + xmin + " " + ymax + ", " + xmin + " " + ymin + "))'," + this.srid + ") AND ";
            }
            if (sqlFilter != null) {
                sqlQuery = String.valueOf(sqlQuery) + "(" + this.getSQLExpression(sqlFilter) + ") AND ";
            }
            sqlQuery = this.schema.isVersionable() ? String.valueOf(sqlQuery) + this.schema.getEndDateFilter(this) : sqlQuery.substring(0, sqlQuery.length() - 4);
        }
        if (orderByFields != null && !orderByFields.isEmpty()) {
            sqlQuery = asc ? String.valueOf(sqlQuery) + " ORDER BY " + this.getOrderByExpression(orderByFields) + " ASC" : String.valueOf(sqlQuery) + " ORDER BY " + this.getOrderByExpression(orderByFields) + " DESC";
        }
        LOGGER.debug((Object)sqlQuery);
        return sqlQuery;
    }

    @Override
    protected String getSQLRoot() {
        String sqlRoot = "";
        int numAtributos = this.schema.getAttributeCount();
        int geomIndex = this.schema.getGeometryIndex();
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
                        sqlRoot = String.valueOf(sqlRoot) + tableDS.getFullTableName() + "." + this.escapeAttributeName(attrCalculate.getRelationFieldName()) + " AS " + this.escapeAttributeName(attrCalculate.getName()) + ",";
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    LayerRelation layerRel = (LayerRelation)rel;
                    Layer layer = layerRel.getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this)) {
                        sqlRoot = String.valueOf(sqlRoot) + layerDS.getFullTableName() + "." + this.escapeAttributeName(attrCalculate.getRelationFieldName()) + " AS " + this.escapeAttributeName(attrCalculate.getName()) + ",";
                    }
                }
            } else {
                AttributeType attrType = this.schema.getAttributeType(i);
                sqlRoot = attrType == AttributeType.GEOMETRY ? (geomIndex == i ? String.valueOf(sqlRoot) + " " + this.getGeometryQueryString() + "," : String.valueOf(sqlRoot) + this.queryFunction + "(" + this.getFullTableName() + "." + this.escapeAttributeName(attr.getName()) + ",'XDR') AS " + this.escapeAttributeName(attr.getName()) + ",") : String.valueOf(sqlRoot) + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getAttributeName(i)) + ",";
            }
            ++i;
        }
        return sqlRoot.substring(0, sqlRoot.length() - 1);
    }

    @Override
    protected String getSQLForInsert(Feature feature) throws TopologyException {
        String sqlInsert = "INSERT INTO " + this.getFullTableName() + " (" + this.getBasicSQLInsert(feature) + " ) VALUES (";
        int coordDim = 2;
        if (this.is3d) {
            coordDim = 3;
        }
        WKTWriter geometryWriter = new WKTWriter(coordDim);
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                if (attr.getType().equals(AttributeType.GEOMETRY)) {
                    Geometry geom = (Geometry)feature.getAttribute(i);
                    String wktGeometry = null;
                    wktGeometry = geom == null || geom.isEmpty() ? String.valueOf(this.geometryFromTextFunction) + "(" + "null" + "," + this.srid + ")" : String.valueOf(this.geometryFromTextFunction) + "('" + geometryWriter.write(geom) + "'," + this.srid + ")";
                    sqlInsert = String.valueOf(sqlInsert) + wktGeometry + ",";
                } else {
                    sqlInsert = String.valueOf(sqlInsert) + "?,";
                }
            }
            ++i;
        }
        sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1);
        sqlInsert = String.valueOf(sqlInsert) + ")";
        return sqlInsert;
    }

    @Override
    protected String getSQLForUpdate(Feature feature) {
        String sqlUpdate = "UPDATE " + this.getFullTableName() + " SET ";
        int coordDim = 2;
        if (this.is3d) {
            coordDim = 3;
        }
        WKTWriter geometryWriter = new WKTWriter(coordDim);
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                if (attr.getType().equals(AttributeType.GEOMETRY)) {
                    Geometry geom = (Geometry)feature.getAttribute(i);
                    String wktGeometry = null;
                    wktGeometry = geom == null || geom.isEmpty() ? String.valueOf(this.geometryFromTextFunction) + "(" + "null" + "," + this.srid + ")" : String.valueOf(this.geometryFromTextFunction) + "('" + geometryWriter.write(geom) + "'," + this.srid + ")";
                    sqlUpdate = String.valueOf(sqlUpdate) + this.escapeAttributeName(attr.getName()) + "=" + wktGeometry + ",";
                } else {
                    sqlUpdate = String.valueOf(sqlUpdate) + this.escapeAttributeName(attr.getName()) + "=?,";
                }
            }
            ++i;
        }
        sqlUpdate = sqlUpdate.substring(0, sqlUpdate.length() - 1);
        sqlUpdate = String.valueOf(sqlUpdate) + " WHERE ";
        sqlUpdate = String.valueOf(sqlUpdate) + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + " = '" + feature.getAttribute(this.schema.getPrimaryKeyIndex()).toString() + "'";
        return sqlUpdate;
    }

    @Override
    public Feature readFeature(ResultSet resultSet) throws SQLException {
        WKBParser2 parser = new WKBParser2();
        BasicFeature feature = new BasicFeature(this.schema);
        Object databaseObject = null;
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.isAttributeForRead(i)) {
                byte[] data;
                Attribute attr = this.schema.getAttribute(i);
                databaseObject = attr.getType().equals(AttributeType.GEOMETRY) ? ((data = resultSet.getBytes(this.schema.getAttributeName(i))) == null ? factory.createGeometryCollection(null) : parser.parse(data)) : this.getValue(resultSet, attr.getName());
                feature.setAttribute(i, databaseObject);
            }
            ++i;
        }
        return feature;
    }

    @Override
    public Feature readOptimizedFeature(ResultSet resultSet, List<String> labels, boolean ignoredUpdate) throws Exception {
        WKBParser2 parser = new WKBParser2();
        BasicFeature feature = new BasicFeature(this.schema);
        Object databaseObject = null;
        byte[] data = resultSet.getBytes(this.schema.getAttributeName(this.schema.getGeometryIndex()));
        databaseObject = data == null ? factory.createGeometryCollection(null) : parser.parse(data);
        feature.setGeometry((Geometry)databaseObject);
        if (this.schema.getPrimaryKey() != null) {
            String pkName = this.schema.getPrimaryKeyName();
            databaseObject = this.getValue(resultSet, pkName);
            feature.setAttribute(pkName, databaseObject);
        }
        if (labels != null) {
            HashSet<AttributeCalculate> attrsCalculates = new HashSet<AttributeCalculate>();
            for (String campo : labels) {
                Attribute attr = this.getSchema().getAttribute(campo);
                if (attr == null) continue;
                if (!(attr.isCalculated() && !this.isCompatibleRelation(((AttributeCalculate)attr).getRelation()) || attr.getType().equals(AttributeType.GEOMETRY))) {
                    databaseObject = this.getValue(resultSet, campo);
                    feature.setAttribute(campo, databaseObject);
                    continue;
                }
                if (attr.getType().equals(AttributeType.GEOMETRY)) {
                    byte[] dataGeom = resultSet.getBytes(attr.getName());
                    databaseObject = dataGeom == null ? factory.createGeometryCollection(null) : parser.parse(dataGeom);
                    feature.setAttribute(campo, databaseObject);
                    continue;
                }
                attrsCalculates.add((AttributeCalculate)attr);
            }
            for (AttributeCalculate attrCalculate : attrsCalculates) {
                Relation<?> relation = attrCalculate.getRelation();
                String linkAttribute = relation.getSourceAttribute();
                Object value = feature.getAttribute(linkAttribute);
                Object relationValue = relation.getFieldValue(attrCalculate.getRelationFieldName(), value);
                feature.setAttribute(attrCalculate.getName(), relationValue);
            }
        }
        if (!ignoredUpdate) {
            return this.getRealFeature(feature);
        }
        return feature;
    }

    @Override
    public Geometry readGeometry(ResultSet resultset) {
        WKBParser2 parser = new WKBParser2();
        Object databaseObject = null;
        try {
            byte[] data = resultset.getBytes(this.schema.getAttributeName(this.schema.getGeometryIndex()));
            databaseObject = data == null ? factory.createGeometryCollection(null) : parser.parse(data);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return databaseObject;
    }

    @Override
    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
        int DATA_TYPE = 5;
        int TYPE_NAME = 6;
        try {
            int dataType = rs.getInt(5);
            String typeName = rs.getString(6);
            if (typeName.equals("geometry")) {
                return AttributeType.toAttributeType(Geometry.class);
            }
            Class type = (Class)TYPE_MAPPINGS.get(new Integer(dataType));
            if (type == null) {
                return AttributeType.OBJECT;
            }
            return AttributeType.toAttributeType(type);
        }
        catch (SQLException e) {
            throw new IOException(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.PostGisDataSource.sql-exception-ocurred")) + " " + e.getMessage());
        }
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
        return String.valueOf(this.getRaizConsultaTipo()) + sqlWhere + " LIMIT " + limit;
    }

    @Override
    protected String getSQLForCreateTable() {
        String sql = "CREATE TABLE " + this.getFullTableName() + "(";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.schema.getGeometryIndex() != i) {
                String name = this.schema.getAttributeName(i);
                AttributeType attributeType = this.schema.getAttributeType(i);
                String attributeTypeName = attributeTypeToDBType.get(attributeType);
                sql = String.valueOf(sql) + this.escapeAttributeName(name) + " " + attributeTypeName;
                if (this.schema.getPrimaryKeyIndex() == i) {
                    sql = String.valueOf(sql) + " NOT NULL ";
                }
                sql = String.valueOf(sql) + ",";
            }
            ++i;
        }
        sql = String.valueOf(sql) + " PRIMARY KEY (" + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + "))";
        return sql;
    }

    @Override
    public boolean createDataStore(Envelope vista, String geomColumn, int srid, boolean addRestrictions, boolean is3d) throws SQLException {
        this.srid = srid;
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DataBaseConnectionFactory.getConnection(this);
            DatabaseMetaData dmd = connection.getMetaData();
            ResultSet rs = dmd.getColumns(null, this.getDataBaseSchema(), this.tableName, "%");
            this.newTable = !rs.next();
            rs.close();
            if (this.newTable) {
                statement = connection.createStatement();
                String sqlForCreateTable = this.getSQLForCreateTable();
                statement.execute(sqlForCreateTable);
                statement.execute(this.getAddGeometryColumnStatement(addRestrictions, is3d));
                statement.close();
            }
            boolean bl = this.newTable;
            return bl;
        }
        catch (SQLException ex) {
            LOGGER.error((Object)"", (Throwable)ex);
            throw ex;
        }
        finally {
            this.closeConnection(connection);
        }
    }

    private String getAddGeometryColumnStatement(boolean addRestrictions, boolean is3d) {
        String result = "SELECT addgeometrycolumn('" + this.dataBase + "','" + this.dataBaseSchema + "','" + this.tableName + "','" + this.geomColName + "'," + this.srid + ",'";
        String geomType = "GEOMETRY";
        if (addRestrictions) {
            if (this.schema.getGeometryType() == 5) {
                geomType = "POLYGON";
            } else if (this.schema.getGeometryType() == 4) {
                geomType = "MULTIPOLYGON";
            } else if (this.schema.getGeometryType() == 1) {
                geomType = "POINT";
            } else if (this.schema.getGeometryType() == 8) {
                geomType = "MULTIPOINT";
            } else if (this.schema.getGeometryType() == 3) {
                geomType = "LINESTRING";
            } else if (this.schema.getGeometryType() == 2) {
                geomType = "MULTILINESTRING";
            }
        }
        result = String.valueOf(result) + geomType + "'";
        result = is3d ? String.valueOf(result) + ",3)" : String.valueOf(result) + ",2)";
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PostGisDataSource)) {
            return false;
        }
        PostGisDataSource dataSource = (PostGisDataSource)obj;
        return this.hostName.equals(dataSource.hostName) && this.port == dataSource.port && this.dataBase.equals(dataSource.dataBase) && this.userName.equals(dataSource.userName) && this.password.equals(dataSource.password);
    }

    @Override
    public int hashCode() {
        return this.hostName.hashCode() * this.port * this.userName.hashCode() * this.password.hashCode();
    }

    @Override
    protected void buildGeometrySRID(Connection connection) {
        block7: {
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                try {
                    statement = connection.createStatement(1003, 1007);
                    resultSet = statement.executeQuery("SELECT srid FROM geometry_columns WHERE f_table_schema='" + this.dataBaseSchema + "' AND f_table_name='" + this.tableName + "'");
                    if (resultSet.next()) {
                        this.srid = resultSet.getInt(1);
                    } else {
                        resultSet.close();
                        resultSet = statement.executeQuery("SELECT " + this.sridFunction + "(" + this.escapeAttributeName(this.schema.getAttributeName(this.schema.getGeometryIndex())) + ") FROM " + this.getFullTableName() + " LIMIT 1");
                        this.srid = resultSet.next() ? resultSet.getInt(1) : -1;
                    }
                }
                catch (Exception sqlException) {
                    LOGGER.error((Object)"", (Throwable)sqlException);
                    this.closeChannel(resultSet, statement);
                    break block7;
                }
            }
            catch (Throwable throwable) {
                this.closeChannel(resultSet, statement);
                throw throwable;
            }
            this.closeChannel(resultSet, statement);
        }
    }

    protected void buildGeometryColumnName(Connection connection) {
        if (StringUtils.isEmpty((String)this.geomColName)) {
            List<String> geomColumnNames = this.getCandidateGeometryColumnName(connection);
            if (CollectionUtils.isNotEmpty(geomColumnNames)) {
                this.geomColName = geomColumnNames.get(0);
            } else {
                LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.PostGisDataSource.The-geometric-column-could-not-be-found-in-the-geometry_columns-table"));
            }
        }
    }

    protected List<String> getCandidateGeometryColumnName(Connection connection) {
        ArrayList<String> candidateGeometryColumnNameList;
        block8: {
            candidateGeometryColumnNameList = new ArrayList<String>();
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                try {
                    statement = connection.createStatement(1003, 1007);
                    resultSet = statement.executeQuery("SELECT DISTINCT f_geometry_column FROM geometry_columns WHERE f_table_schema='" + this.dataBaseSchema + "' AND f_table_name='" + this.tableName + "'");
                    if (resultSet.next()) {
                        candidateGeometryColumnNameList.add(resultSet.getString(1));
                        while (resultSet.next()) {
                            candidateGeometryColumnNameList.add(resultSet.getString(1));
                        }
                    } else {
                        LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.PostGisDataSource.The-geometric-column-could-not-be-found-in-the-geometry_columns-table"));
                    }
                }
                catch (Exception sqlException) {
                    LOGGER.error((Object)"", (Throwable)sqlException);
                    this.closeChannel(resultSet, statement);
                    break block8;
                }
            }
            catch (Throwable throwable) {
                this.closeChannel(resultSet, statement);
                throw throwable;
            }
            this.closeChannel(resultSet, statement);
        }
        return candidateGeometryColumnNameList;
    }

    @Override
    public void createSpatialIndex() throws Exception {
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            Statement statement = con.createStatement();
            String sql = "CREATE INDEX \"" + this.tableName + "_idx\" ON " + this.getFullTableName() + " USING GIST (" + this.escapeAttributeName(this.geomColName) + ")";
            statement.execute(sql);
            this.closeChannel(null, statement);
        }
        finally {
            this.closeConnection(con);
        }
    }

    @Override
    public boolean lockFeatures(Collection<Feature> features) throws SQLException {
        HashSet<Number> keysLocks = new HashSet<Number>();
        String pkName = this.schema.getPrimaryKeyName();
        String baseSql = String.valueOf(this.getRaizConsultaTipo()) + " WHERE " + this.getFullTableName() + "." + this.escapeAttributeName(pkName) + " IN (";
        ArrayList<String> sqlsToLock = new ArrayList<String>();
        boolean check = false;
        String currentSQL = baseSql;
        int cont = 0;
        for (Feature feature : features) {
            Number pk;
            if (feature.isUnsaved() || this.lockedFeatures.get((pk = (Number)feature.getAttribute(pkName)).intValue())) continue;
            keysLocks.add(pk);
            check = true;
            currentSQL = String.valueOf(currentSQL) + "'" + feature.getAttribute(pkName).toString() + "',";
            if (++cont % 500 != 0 || !check) continue;
            currentSQL = String.valueOf(currentSQL.substring(0, currentSQL.length() - 1)) + ")";
            currentSQL = String.valueOf(currentSQL) + " FOR UPDATE NOWAIT";
            sqlsToLock.add(currentSQL);
            currentSQL = baseSql;
            check = false;
        }
        if (check) {
            currentSQL = String.valueOf(currentSQL.substring(0, currentSQL.length() - 1)) + ")";
            currentSQL = String.valueOf(currentSQL) + " FOR UPDATE NOWAIT";
            sqlsToLock.add(currentSQL);
        }
        if (sqlsToLock.isEmpty()) {
            return false;
        }
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            con.setAutoCommit(false);
            con.setTransactionIsolation(2);
            for (String lockSQL : sqlsToLock) {
                Statement st = con.createStatement();
                st.executeQuery(lockSQL);
                st.close();
            }
        }
        finally {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            }
            catch (Exception exception) {}
        }
        if (this.connectionTransaction == null) {
            this.beginTransaction();
        }
        Statement st = null;
        try {
            for (String lockSQL : sqlsToLock) {
                st = this.connectionTransaction.createStatement();
                st.executeQuery(lockSQL);
                st.close();
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.clearTransaction();
            throw e;
        }
        for (Number key : keysLocks) {
            this.lockedFeatures.set(key.intValue());
        }
        return true;
    }

    @Override
    public int toSQLType(AttributeType attrType) {
        int sqlType = attributeTypeToSQLType.get(attrType);
        return sqlType;
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
            return new PostGisEditableIterator(sql, geometryFilter, this, labels, !check);
        }
        return new PostGisIterator(sql, geometryFilter, this, labels, !check);
    }

    public FeatureIterator queryIteratorByDistance(Point geom, double distance) {
        Envelope env = geom.buffer(0.001).getEnvelopeInternal();
        env.expandBy(distance);
        Filter geometryFilter = this.getViewBoxFilter(env);
        String sql = this.getSQLOnlyGeometryForQuery(env, null, null, true, this.labels);
        sql = String.valueOf(sql) + " AND " + this.distanceFunction + "(" + this.geometryFromTextFunction + "('POINT(" + geom.getX() + " " + geom.getY() + ")', " + this.srid + "), " + this.getFullTableName() + "." + this.escapeAttributeName(this.geomColName) + ") <= " + distance;
        if (this.editable) {
            return new PostGisEditableIterator(sql, geometryFilter, this, this.labels);
        }
        return new PostGisIterator(sql, geometryFilter, this, this.labels);
    }

    @Override
    public Envelope getViewBox() {
        block12: {
            if (this.envelope == null) {
                Connection connection = null;
                try {
                    try {
                        connection = DataBaseConnectionFactory.getConnection(this);
                        Statement s = connection.createStatement();
                        String sql = "SELECT " + this.extentFunction + "(" + this.getFullTableName() + "." + this.escapeAttributeName(this.getGeomColName()) + ") AS \"FullExtent\" FROM " + this.getTables();
                        String sqlWhere = "";
                        if (this.layerFilter != null) {
                            sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
                        }
                        if (this.schema.isVersionable()) {
                            sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                        }
                        sql = String.valueOf(sql) + sqlWhere;
                        ResultSet r = s.executeQuery(sql);
                        r.next();
                        String strAux = r.getString(1);
                        if (strAux == null) {
                            this.envelope = new Envelope();
                        } else if (strAux.startsWith("BOX3D")) {
                            PGbox3d regeom = new PGbox3d(strAux);
                            this.envelope = new Envelope(regeom.getLLB().x, regeom.getURT().x, regeom.getLLB().y, regeom.getURT().y);
                        } else {
                            PGbox2d regeom = new PGbox2d(strAux);
                            this.envelope = new Envelope(regeom.getLLB().x, regeom.getURT().x, regeom.getLLB().y, regeom.getURT().y);
                        }
                        r.close();
                        s.close();
                    }
                    catch (SQLException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        this.closeConnection(connection);
                        break block12;
                    }
                }
                catch (Throwable throwable) {
                    this.closeConnection(connection);
                    throw throwable;
                }
                this.closeConnection(connection);
            }
        }
        Envelope fullEnvelope = this.expandEnvelope(this.envelope, this.getNewFeatures());
        return this.expandEnvelope(fullEnvelope, this.getUpdatedFeatures());
    }

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        Envelope envelope = new Envelope();
        Connection connection = null;
        Statement s = null;
        ResultSet r = null;
        try {
            connection = DataBaseConnectionFactory.getConnection(this);
            s = connection.createStatement();
            String sql = "SELECT " + this.extentFunction + "(" + this.escapeAttributeName(this.getGeomColName()) + ") AS \"FullExtent\" FROM " + this.getTables();
            String sqlWhere = "";
            if (filter != null) {
                sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(filter);
            }
            if (this.schema.isVersionable()) {
                sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
            }
            sql = String.valueOf(sql) + sqlWhere;
            r = s.executeQuery(sql);
            r.next();
            String strAux = r.getString(1);
            if (strAux == null) {
                envelope = new Envelope();
            } else if (strAux.startsWith("BOX3D")) {
                PGbox3d regeom = new PGbox3d(strAux);
                envelope = new Envelope(regeom.getLLB().x, regeom.getURT().x, regeom.getLLB().y, regeom.getURT().y);
            } else {
                PGbox2d regeom = new PGbox2d(strAux);
                envelope = new Envelope(regeom.getLLB().x, regeom.getURT().x, regeom.getLLB().y, regeom.getURT().y);
            }
            r.close();
            r = null;
            s.close();
            s = null;
        }
        finally {
            if (r != null) {
                try {
                    r.close();
                }
                catch (Exception exception) {}
            }
            if (s != null) {
                try {
                    s.close();
                }
                catch (Exception exception) {}
            }
            this.closeConnection(connection);
        }
        Envelope fullEnvelope = this.expandEnvelope(envelope, this.getNewFeatures());
        return this.expandEnvelope(fullEnvelope, this.getUpdatedFeatures());
    }

    @Override
    public String getFullTableName() {
        if (StringUtils.isEmpty((String)this.dataBaseSchema)) {
            return "\"" + this.tableName + "\"";
        }
        return "\"" + this.dataBaseSchema + "\".\"" + this.tableName + "\"";
    }

    @Override
    public void createTemporalFields(String startField, String endField, String versionField) throws SQLException {
        String sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + this.escapeAttributeName(startField) + " timestamp DEFAULT current_timestamp";
        this.executeNonFeatureQuery(sql1);
        sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + this.escapeAttributeName(endField) + " timestamp";
        this.executeNonFeatureQuery(sql1);
        sql1 = "CREATE INDEX " + this.tableName.toLowerCase() + "_" + startField + "_idx ON " + this.getFullTableName() + "(" + this.escapeAttributeName(startField) + ")";
        this.executeNonFeatureQuery(sql1);
        sql1 = "CREATE INDEX " + this.tableName.toLowerCase() + "_" + endField + "_idx ON " + this.getFullTableName() + "(" + this.escapeAttributeName(endField) + ")";
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
    protected String getDefaultSQLKeyResolver() {
        return "SELECT COALESCE(MAX(" + this.getFullTableName() + "." + this.escapeAttributeName(this.pkName) + "),0) AS " + this.escapeAttributeName(this.pkName) + " FROM " + this.getFullTableName();
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
                        if (!this.getFullTableName().equals(tableName)) {
                            String simpleTableName = ((TableDBRecordDataSource)table.getDataSource()).getTableName();
                            HashSet<String> processedFieldNames = (HashSet<String>)processedTableNames.get(tableName);
                            String aliasTableName = "";
                            if (processedTableNames.get(tableName) != null) {
                                if (processedFieldNames != null && !processedFieldNames.contains(tableRel.getSourceAttribute())) {
                                    int cont = 1;
                                    while (processedTableNames.get(aliasTableName = "\"" + simpleTableName + "_" + cont++ + "\"") != null) {
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
                                subConsulta = String.valueOf(subConsulta) + " AS \"" + aliasTableName + "\"";
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
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    LayerRelation layerRel = (LayerRelation)rel;
                    Layer layer = layerRel.getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this) && !this.getFullTableName().equals(layerDS.getFullTableName())) {
                        String subConsulta = "LEFT JOIN " + layerDS.getFullTableName() + " ON (";
                        subConsulta = String.valueOf(subConsulta) + this.getFullTableName() + "." + this.escapeAttributeName(layerRel.getSourceAttribute()) + "=" + layerDS.getFullTableName() + "." + this.escapeAttributeName(layerRel.getAttributeTarget()) + ")";
                        tables.add(subConsulta);
                    }
                }
            }
            ++i;
        }
        StringBuffer resultado = new StringBuffer(String.valueOf(this.getFullTableName()) + " ");
        Iterator iter = tables.iterator();
        while (iter.hasNext()) {
            resultado.append(String.valueOf((String)iter.next()) + " ");
        }
        return resultado.toString();
    }

    @Override
    public void commit() throws Exception {
        if (this.connectionTransaction != null) {
            this.endTransaction();
            return;
        }
        this.inMemory = false;
        try {
            if (CollectionUtils.isNotEmpty((Collection)this.newFeatures)) {
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.saving-{0}-new-features", new Object[]{new Integer(this.newFeatures.size())}));
                this.addAll(this.newFeatures);
                this.envelope = new Envelope(this.expandEnvelope(this.envelope, this.getNewFeatures()));
                this.newFeatures.clear();
            }
            if (CollectionUtils.isNotEmpty((Collection)this.deletedFeatures)) {
                ArrayList<Feature> featuresFilter = new ArrayList<Feature>();
                for (Feature element : this.deletedFeatures) {
                    if (element.isUnsaved()) continue;
                    featuresFilter.add(element);
                }
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.deleting-{0}-features", new Object[]{new Integer(featuresFilter.size())}));
                this.removeAll(featuresFilter);
                this.deletedFeatures.clear();
            }
            if (this.updateFeatures.size() > 0) {
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.updating-{0}-features", new Object[]{new Integer(this.updateFeatures.size())}));
                this.updateAll(this.updateFeatures);
                this.envelope = new Envelope(this.expandEnvelope(this.envelope, this.getUpdatedFeatures()));
                this.updateFeatures.clear();
            }
            this.size = -1;
        }
        finally {
            this.inMemory = true;
        }
    }

    @Override
    public void endTransaction() throws SQLException {
        LOGGER.info((Object)"/*****************END TRANSACTION********************/");
        this.inMemory = false;
        try {
            try {
                if (CollectionUtils.isNotEmpty((Collection)this.newFeatures)) {
                    LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.saving-{0}-new-features", new Object[]{new Integer(this.newFeatures.size())}));
                    this.addAll(this.newFeatures);
                    this.envelope = new Envelope(this.expandEnvelope(this.envelope, this.getNewFeatures()));
                    this.newFeatures.clear();
                }
                if (CollectionUtils.isNotEmpty((Collection)this.deletedFeatures)) {
                    ArrayList<Feature> featuresFilter = new ArrayList<Feature>();
                    for (Feature element : this.deletedFeatures) {
                        if (element.isUnsaved()) continue;
                        featuresFilter.add(element);
                    }
                    LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.deleting-{0}-features", new Object[]{new Integer(featuresFilter.size())}));
                    this.removeAll(featuresFilter);
                    this.deletedFeatures.clear();
                }
                if (CollectionUtils.isNotEmpty((Collection)this.updateFeatures)) {
                    LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.updating-{0}-features", new Object[]{new Integer(this.updateFeatures.size())}));
                    this.updateAll(this.updateFeatures);
                    this.envelope = new Envelope(this.expandEnvelope(this.envelope, this.getUpdatedFeatures()));
                    this.updateFeatures.clear();
                }
                this.connectionTransaction.commit();
                this.size = -1;
            }
            catch (Exception e) {
                this.rollback(false);
                throw new SQLException(e.getMessage());
            }
        }
        finally {
            this.clearTransaction();
            this.inMemory = true;
        }
    }

    @Override
    public List<String> getAllGeometriesTables(String schema) throws SQLException, IOException {
        ArrayList<String> geometryTablesList = new ArrayList<String>();
        Connection con = null;
        try {
            this.dataBaseSchema = schema;
            con = DataBaseConnectionFactory.getConnection(this);
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery("SELECT DISTINCT f_table_name FROM geometry_columns WHERE f_table_schema='" + schema + "' order by f_table_name");
            while (res.next()) {
                String table = res.getString(1);
                if (!this.allowTable(table)) continue;
                geometryTablesList.add(table);
            }
            this.closeChannel(res, st);
            if (CollectionUtils.isNotEmpty(geometryTablesList)) {
                DatabaseMetaData dmd = con.getMetaData();
                ResultSet rsBD = dmd.getTables(this.dataBase, schema, null, new String[]{"TABLE", "VIEW"});
                ArrayList<String> realTables = new ArrayList<String>();
                while (rsBD.next()) {
                    String table = rsBD.getString("TABLE_NAME");
                    if (!this.allowTable(table)) continue;
                    realTables.add(table);
                }
                this.closeChannel(rsBD, null);
                geometryTablesList.retainAll(realTables);
            }
        }
        catch (Throwable throwable) {
            this.closeConnection(con);
            throw throwable;
        }
        this.closeConnection(con);
        return geometryTablesList;
    }

    @Override
    protected String getSQLForDropTable() {
        String sql = "SELECT dropgeometrytable('" + this.dataBase + "','" + this.dataBaseSchema + "','" + this.tableName + "')";
        return sql;
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
                    iterator = new PostGisEditableIterator(sql, filter, this, this.getAllLabels());
                    iterator.setIgnoredUpdate(ignoredUpdate);
                } else {
                    iterator = new PostGisIterator(sql, filter, this, this.getAllLabels());
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

    protected Object getValue(ResultSet rs, String fieldName) throws SQLException {
        Object data = null;
        Object val = null;
        int fieldId = rs.findColumn(fieldName);
        byte[] byteBuf = rs.getBytes(fieldId);
        if (byteBuf != null) {
            ByteBuffer buf = ByteBuffer.wrap(byteBuf);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnType = metaData.getColumnType(fieldId);
            switch (columnType) {
                case -1: 
                case 1: 
                case 12: {
                    val = rs.getString(fieldId);
                    break;
                }
                case 6: 
                case 7: {
                    val = Float.valueOf(rs.getFloat(fieldId));
                    break;
                }
                case 8: {
                    val = rs.getDouble(fieldId);
                    break;
                }
                case 4: {
                    val = rs.getInt(fieldId);
                    break;
                }
                case -5: {
                    val = rs.getLong(fieldId);
                    break;
                }
                case -7: {
                    val = rs.getObject(fieldId);
                    break;
                }
                case 16: {
                    val = rs.getBoolean(fieldId);
                    break;
                }
                case -6: 
                case 5: {
                    val = rs.getShort(fieldId);
                    break;
                }
                case 91: {
                    val = rs.getDate(fieldId);
                    break;
                }
                case 93: {
                    val = rs.getTimestamp(fieldId);
                    break;
                }
                case 2: {
                    val = rs.getBigDecimal(fieldId);
                    break;
                }
                default: {
                    LOGGER.warn((Object)(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.PostGisDataSource.Unknow-data-type")) + " -> " + columnType));
                    val = data;
                    break;
                }
            }
        }
        return val;
    }

    @Override
    public Object clone() {
        PostGisDataSource clone = new PostGisDataSource(this.getHostName(), this.getPort(), this.getDataBase(), this.getUserName(), this.getPassword());
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
        if (this.keyResolver != null) {
            clone.setKeyResolver(this.keyResolver.clone());
        }
        return clone;
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        ArrayList<Feature> features = new ArrayList<Feature>();
        if (!this.schema.isVersionable()) {
            return features;
        }
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
            SQLEncoderPostgisGeos encoder = new SQLEncoderPostgisGeos();
            sql = String.valueOf(sql) + " " + encoder.encode(finalFilter);
            JDBCIterator it = null;
            try {
                boolean check = this.checkIfFilterIsCompatible(filter);
                it = this.editable ? new PostGisEditableIterator(sql, (Filter)((Cloneable)filter).clone(), this, this.getAllLabels(), !check) : new PostGisIterator(sql, (Filter)((Cloneable)filter).clone(), this, this.getAllLabels(), !check);
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
        return new PostGisFullIterator(envelope, filter, fieldsToOrdered, ascending, this);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        String sqlFilter = "";
        int i = 0;
        while (i < names.length) {
            if (values[i] != null) {
                sqlFilter = String.valueOf(sqlFilter) + names[i] + "='" + values[i].toString() + "' AND ";
            }
            ++i;
        }
        if (sqlFilter.length() > 0) {
            sqlFilter = sqlFilter.substring(0, sqlFilter.length() - 5);
        }
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        FeatureIterator it = null;
        try {
            try {
                Filter newFilter = null;
                if (sqlFilter.length() > 0) {
                    newFilter = (Filter)ExpressionBuilder.parse(this.schema, sqlFilter);
                    if (filter != null) {
                        newFilter = FilterFactory.createFilterFactory().createLogicFilter(filter, newFilter, (short)2);
                    }
                } else if (filter != null) {
                    newFilter = filter;
                }
                ArrayList<String> orderByFields = new ArrayList<String>();
                if (fieldOrdered != null) {
                    orderByFields.add(fieldOrdered);
                }
                it = this.queryGeometryIterator(null, newFilter, orderByFields, ascending, this.getAllLabels());
                while (it.hasNext()) {
                    resultado.add(it.next());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                ArrayList<Feature> arrayList = resultado;
                if (it != null) {
                    it.close();
                }
                return arrayList;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return resultado;
    }

    protected void setPostGISFunctions() {
        String postgisVersionQuery = "SELECT postgis_lib_version()";
        Connection connection = null;
        Statement statement = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement();
                ResultSet res = statement.executeQuery(postgisVersionQuery);
                if (res.next()) {
                    String postgisVersion = res.getString(1);
                    if (StringUtils.isNotEmpty((String)postgisVersion) && postgisVersion.charAt(0) == '0') {
                        this.queryFunction = ASBINARY_FUNCTION_NAME;
                        this.geometryFromTextFunction = GEOMETRY_FROM_TEXT_FUNCTION_NAME;
                        this.sridFunction = SRID_FUNCTION_NAME;
                        LOGGER.warn((Object)I18N.getMessage(PostGisDataSource.class, "postgis-version-{0}-function-{1}-will-be-used-to-recover-geometry-from-data-base", new Object[]{postgisVersion, this.queryFunction}));
                    } else if (postgisVersion.startsWith("1.0")) {
                        this.queryFunction = ASEWKB_FUNCTION_NAME;
                        this.geometryFromTextFunction = GEOMETRY_FROM_TEXT_FUNCTION_NAME;
                        this.sridFunction = SRID_FUNCTION_NAME;
                        LOGGER.warn((Object)I18N.getMessage(PostGisDataSource.class, "postgis-version-{0}-function-{1}-will-be-used-to-recover-geometry-from-data-base", new Object[]{postgisVersion, this.queryFunction}));
                    }
                }
                this.closeChannel(res, statement);
            }
            catch (Exception ex) {
                LOGGER.warn((Object)ex);
                this.closeConnection(connection);
            }
        }
        finally {
            this.closeConnection(connection);
        }
    }

    @Override
    public void initialize(FeatureSchema schema, Envelope envelope, int srid, boolean addRestrictions, boolean is3d) throws SQLException {
        super.initialize(schema, envelope, srid, addRestrictions, is3d);
        this.setPostGISFunctions();
    }

    @Override
    public void initialize(boolean ignoreGeometryType) throws SQLException {
        super.initialize(ignoreGeometryType);
        this.setPostGISFunctions();
    }

    @Override
    protected String getPrimaryOrUniqueKey(Connection con) {
        String candidatePKName = this.getPrimaryOrUniqueKey("p", con);
        if (candidatePKName == null) {
            candidatePKName = this.getPrimaryOrUniqueKey("u", con);
        }
        return candidatePKName != null ? candidatePKName : super.getPrimaryOrUniqueKey(con);
    }

    private String getPrimaryOrUniqueKey(String indexType, Connection con) {
        String pkName;
        block21: {
            String sql = "SELECT a.attname as column_name, t.typname as data_type, CASE WHEN cc.contype='p' THEN 'PRI' WHEN cc.contype='u' THEN 'UNI' WHEN cc.contype='f' THEN 'FK' ELSE '' END AS key, CASE WHEN a.attnotnull=false THEN 'YES' ELSE 'NO' END AS is_nullable, CASE WHEN a.attlen='-1' THEN (a.atttypmod - 4) ELSE a.attlen END as max_length, d.adsrc as column_default FROM pg_catalog.pg_attribute a LEFT JOIN pg_catalog.pg_type t ON t.oid = a.atttypid LEFT JOIN pg_catalog.pg_class c ON c.oid = a.attrelid LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace LEFT JOIN pg_catalog.pg_constraint cc ON cc.conrelid = c.oid AND cc.conkey[1] = a.attnum LEFT JOIN pg_catalog.pg_attrdef d ON d.adrelid = c.oid AND a.attnum = d.adnum WHERE n.nspname=? AND c.relname = ? AND a.attnum > 0 AND t.oid = a.atttypid AND cc.contype=?";
            pkName = null;
            PreparedStatement ps = null;
            ResultSet res = null;
            try {
                try {
                    ps = con.prepareStatement(sql);
                    ps.setString(1, this.dataBaseSchema);
                    ps.setString(2, this.tableName);
                    ps.setString(3, indexType);
                    res = ps.executeQuery();
                    if (res.next()) {
                        pkName = res.getString("column_name");
                    }
                    res.close();
                    ps.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (ps != null) {
                        try {
                            ps.close();
                        }
                        catch (SQLException e2) {
                            LOGGER.error((Object)"", (Throwable)e2);
                        }
                    }
                    if (res == null) break block21;
                    try {
                        res.close();
                    }
                    catch (SQLException e3) {
                        LOGGER.error((Object)"", (Throwable)e3);
                    }
                }
            }
            finally {
                if (ps != null) {
                    try {
                        ps.close();
                    }
                    catch (SQLException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
                if (res != null) {
                    try {
                        res.close();
                    }
                    catch (SQLException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
            }
        }
        return pkName;
    }

    @Override
    protected boolean isCompatibleRelation(Relation<?> rel) {
        if (rel instanceof TableRelation) {
            Table table = ((TableRelation)rel).getTable();
            if (table.getDataSource() instanceof PostgreSQLDataSource) {
                TableDBRecordDataSource tableDS = (TableDBRecordDataSource)table.getDataSource();
                return this.checkHost(tableDS.getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getDataBaseSchema().equals(this.getDataBaseSchema()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword());
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
    protected void buildFeatureSchema(Connection con) throws Exception {
        super.buildFeatureSchema(con);
        this.buildGeometryColumnName(con);
        if (this.geomColName != null) {
            this.schema.setGeometryIndex(this.schema.getAttributeIndex(this.geomColName));
        }
    }

    protected String getGeometryQueryString() {
        String geomColumnName = this.schema.getAttributeName(this.schema.getGeometryIndex());
        return String.valueOf(this.queryFunction) + "(" + this.getFullTableName() + "." + this.escapeAttributeName(geomColumnName) + ",'XDR') AS " + this.escapeAttributeName(geomColumnName);
    }

    @Override
    protected String escapePkValueIfNeeded(String value) {
        boolean mustEscapeValue = !Number.class.isAssignableFrom(this.schema.getAttributeType(this.pkName).toJavaClass());
        String result = "";
        result = mustEscapeValue ? "'" + value + "'" : value;
        return result;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPool() throws SQLException {
        PGConnectionPoolDataSource dataSource = new PGConnectionPoolDataSource();
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
        return 5432;
    }

    @Override
    protected String escapeAttributeName(String attrName) {
        return "\"" + attrName + "\"";
    }

    @Override
    public String convertAttributeTypeToDBType(AttributeType type) {
        return attributeTypeToDBType.get(type);
    }
}

