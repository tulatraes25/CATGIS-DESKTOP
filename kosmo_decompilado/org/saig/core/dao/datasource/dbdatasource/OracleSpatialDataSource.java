/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.PrecisionModel
 *  com.vividsolutions.jts.geom.TopologyException
 *  oracle.jdbc.OracleConnection
 *  oracle.jdbc.pool.OracleConnectionPoolDataSource
 *  oracle.sql.ARRAY
 *  oracle.sql.Datum
 *  oracle.sql.STRUCT
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.dao.datasource.dbdatasource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.feature.ILayerIterator;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import java.io.IOException;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.geotools.data.oracle.sdo.GeometryConverter;
import org.opengis.util.Cloneable;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.IPoolableDBDataSource;
import org.saig.core.dao.datasource.dbdatasource.iterators.JDBCIterator;
import org.saig.core.dao.datasource.dbdatasource.iterators.OracleEditableIterator;
import org.saig.core.dao.datasource.dbdatasource.iterators.OracleIterator;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;
import org.saig.core.dao.datasource.dbdatasource.utils.EpsgToOracleCodeConverter;
import org.saig.core.dao.datasource.dbdatasource.utils.EpsgToOracleCodeNotFoundException;
import org.saig.core.dao.datasource.dbdatasource.utils.OracleToEpsgCodeNotFoundException;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Filter;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.SQLEncoderException;
import org.saig.core.filter.SQLEncoderOracle;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.jdbc.OracleDataSource;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.TableRelation;
import org.saig.jump.lang.I18N;

public class OracleSpatialDataSource
extends AbstractJDBCDataSource
implements IPoolableDBDataSource {
    public static final Logger LOGGER = Logger.getLogger(OracleSpatialDataSource.class);
    public static final String ID = "Oracle";
    public static Map<AttributeType, String> attributeTypeToDBType = new HashMap<AttributeType, String>();
    public static Map<AttributeType, Integer> attributeTypeToSQLType = new HashMap<AttributeType, Integer>();

    static {
        attributeTypeToDBType.put(AttributeType.BIGINT, "NUMBER(12,0)");
        attributeTypeToDBType.put(AttributeType.BIT, "NUMBER(1,0)");
        attributeTypeToDBType.put(AttributeType.BOOLEAN, "NUMBER(1,0)");
        attributeTypeToDBType.put(AttributeType.CHAR, "VARCHAR2(1)");
        attributeTypeToDBType.put(AttributeType.DATE, "DATE");
        attributeTypeToDBType.put(AttributeType.DECIMAL, "NUMBER(8,0)");
        attributeTypeToDBType.put(AttributeType.DOUBLE, "NUMBER(14,2)");
        attributeTypeToDBType.put(AttributeType.FLOAT, "NUMBER(12,2)");
        attributeTypeToDBType.put(AttributeType.GEOMETRY, "MDSYS.SDO_GEOMETRY");
        attributeTypeToDBType.put(AttributeType.INTEGER, "NUMBER(8,0)");
        attributeTypeToDBType.put(AttributeType.LONG, "NUMBER(12,0)");
        attributeTypeToDBType.put(AttributeType.LONGVARCHAR, "VARCHAR2(1024)");
        attributeTypeToDBType.put(AttributeType.TEXT, "VARCHAR2(1024)");
        attributeTypeToDBType.put(AttributeType.NUMERIC, "NUMBER");
        attributeTypeToDBType.put(AttributeType.OBJECT, "BLOB");
        attributeTypeToDBType.put(AttributeType.REAL, "NUMBER(12,2)");
        attributeTypeToDBType.put(AttributeType.SMALLINT, "NUMBER(2,0)");
        attributeTypeToDBType.put(AttributeType.STRING, "VARCHAR2(1024)");
        attributeTypeToDBType.put(AttributeType.TIME, "TIME");
        attributeTypeToDBType.put(AttributeType.TIMESTAMP, "TIMESTAMP");
        attributeTypeToDBType.put(AttributeType.TINYINT, "NUMBER(1,0)");
        attributeTypeToDBType.put(AttributeType.VARCHAR, "VARCHAR2(1024)");
        attributeTypeToSQLType.put(AttributeType.BIGINT, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.BIT, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.BOOLEAN, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.CHAR, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.DATE, new Integer(91));
        attributeTypeToSQLType.put(AttributeType.DECIMAL, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.DOUBLE, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.FLOAT, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.INTEGER, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.LONG, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.LONGVARCHAR, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.TEXT, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.NUMERIC, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.OBJECT, new Integer(2004));
        attributeTypeToSQLType.put(AttributeType.REAL, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.SMALLINT, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.STRING, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.TIME, new Integer(92));
        attributeTypeToSQLType.put(AttributeType.TIMESTAMP, new Integer(93));
        attributeTypeToSQLType.put(AttributeType.TINYINT, new Integer(2));
        attributeTypeToSQLType.put(AttributeType.VARCHAR, new Integer(12));
    }

    public OracleSpatialDataSource() {
    }

    public OracleSpatialDataSource(String host, int port, String databaseName, String tableName, String username, String password) {
        super(host, port, databaseName, username, password);
        this.setTableName(tableName);
    }

    public OracleSpatialDataSource(String host, int port, String databaseName, String username, String password) {
        super(host, port, databaseName, username, password);
    }

    public static OracleSpatialDataSource getInstance(String host, int port, String schema, String tableName, String username, String password) {
        return new OracleSpatialDataSource(host, port, schema, tableName, username, password);
    }

    @Override
    public Envelope getViewBox() {
        block14: {
            if (this.envelope == null) {
                Connection connection = null;
                Statement statement = null;
                ResultSet res = null;
                try {
                    try {
                        connection = DataBaseConnectionFactory.getConnection(this);
                        statement = connection.createStatement();
                        if (this.layerFilter == null && !this.schema.isVersionable()) {
                            res = statement.executeQuery("SELECT DIMINFO FROM MDSYS.ALL_SDO_GEOM_METADATA WHERE TABLE_NAME='" + this.tableName.toUpperCase() + "'");
                            if (res.next()) {
                                ARRAY sdoDimArray = (ARRAY)res.getObject(1);
                                Datum[] da = sdoDimArray.getOracleArray();
                                STRUCT sx = (STRUCT)da[0];
                                STRUCT sy = (STRUCT)da[1];
                                double minx = Double.parseDouble(sx.getAttributes()[1].toString());
                                double maxx = Double.parseDouble(sx.getAttributes()[2].toString());
                                double miny = Double.parseDouble(sy.getAttributes()[1].toString());
                                double maxy = Double.parseDouble(sy.getAttributes()[2].toString());
                                this.envelope = new Envelope(minx, maxx, miny, maxy);
                            } else {
                                LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.There-is-no-row-in-ALL_SDO_GEOM_METADATA"));
                                this.envelope = new Envelope();
                            }
                        } else {
                            String sqlEnvelopeQuery = "SELECT (SDO_AGGR_MBR(" + this.getGeomColName() + ")) FROM " + this.tableName.toUpperCase();
                            String sqlWhere = "";
                            if (this.layerFilter != null) {
                                sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
                            }
                            if (this.schema.isVersionable()) {
                                sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                            }
                            sqlEnvelopeQuery = String.valueOf(sqlEnvelopeQuery) + sqlWhere;
                            LOGGER.debug((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.Calculating-the-envelope-by-using-the-query-{0}", new Object[]{sqlEnvelopeQuery}));
                            res = statement.executeQuery(sqlEnvelopeQuery);
                            if (res.next()) {
                                GeometryConverter converter = new GeometryConverter(connection);
                                STRUCT sdoGeometry = (STRUCT)res.getObject(1);
                                Geometry geom = converter.asGeometry(sdoGeometry);
                                this.envelope = geom.getEnvelopeInternal();
                            } else {
                                LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.The-envelope-could-not-be-calculated-by-using-the-query-{0}", new Object[]{sqlEnvelopeQuery}));
                                this.envelope = new Envelope();
                            }
                        }
                    }
                    catch (SQLException ex) {
                        LOGGER.error((Object)"", (Throwable)ex);
                        this.closeChannel(res, statement);
                        this.closeConnection(connection);
                        break block14;
                    }
                }
                catch (Throwable throwable) {
                    this.closeChannel(res, statement);
                    this.closeConnection(connection);
                    throw throwable;
                }
                this.closeChannel(res, statement);
                this.closeConnection(connection);
            }
        }
        Envelope fullEnvelope = this.expandEnvelope(this.envelope, this.getNewFeatures());
        return this.expandEnvelope(fullEnvelope, this.getUpdatedFeatures());
    }

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        Envelope env;
        block10: {
            env = new Envelope();
            Connection connection = null;
            Statement statement = null;
            ResultSet res = null;
            try {
                try {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    statement = connection.createStatement();
                    String sqlEnvelopeQuery = "SELECT (SDO_AGGR_MBR(" + this.getGeomColName() + ")) FROM " + this.tableName.toUpperCase();
                    String sqlWhere = "";
                    if (filter != null) {
                        sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(filter);
                    }
                    if (this.layerFilter != null) {
                        sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter) : String.valueOf(sqlWhere) + " AND " + this.getSQLExpression(this.layerFilter);
                    }
                    if (this.schema.isVersionable()) {
                        sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                    }
                    sqlEnvelopeQuery = String.valueOf(sqlEnvelopeQuery) + sqlWhere;
                    LOGGER.debug((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.Calculating-the-envelope-by-using-the-query-{0}", new Object[]{sqlEnvelopeQuery}));
                    res = statement.executeQuery(sqlEnvelopeQuery);
                    if (res.next()) {
                        GeometryConverter converter = new GeometryConverter(connection);
                        STRUCT sdoGeometry = (STRUCT)res.getObject(1);
                        Geometry geom = converter.asGeometry(sdoGeometry);
                        env = geom.getEnvelopeInternal();
                    } else {
                        LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.The-envelope-could-not-be-calculated-by-using-the-query-{0}", new Object[]{sqlEnvelopeQuery}));
                    }
                }
                catch (SQLException ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                    this.closeChannel(res, statement);
                    this.closeConnection(connection);
                    break block10;
                }
            }
            catch (Throwable throwable) {
                this.closeChannel(res, statement);
                this.closeConnection(connection);
                throw throwable;
            }
            this.closeChannel(res, statement);
            this.closeConnection(connection);
        }
        return env;
    }

    @Override
    public synchronized void addAll(Collection<Feature> features) throws Exception {
        this.addAll(features, false);
    }

    @Override
    public synchronized void addAll(Collection<Feature> features, boolean setId) throws Exception {
        this.addAll(features, setId, true);
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
        Feature feature2 = null;
        try {
            try {
                if (this.connectionTransaction == null) {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    connection.setAutoCommit(false);
                } else {
                    connection = this.connectionTransaction;
                }
                GeometryConverter converter = new GeometryConverter(connection, factory);
                IDBKeyResolver keyResolver = this.getKeyResolver(connection);
                features = this.getCorrectGeometries(features);
                for (Feature feature2 : features) {
                    if (setId || feature2.getPrimaryKey() == null) {
                        this.processFeaturePrimaryKey(keyResolver, connection, feature2);
                    }
                    try {
                        String sql;
                        if (feature2.getGeometry() == null) {
                            feature2.setGeometry(factory.buildGeometry(new ArrayList()));
                        }
                        if (checkVersionable && this.schema.isVersionable()) {
                            feature2.setAttribute(this.schema.getFieldStartDate(), (Object)new java.util.Date());
                        }
                        if ((sql = this.getSQLForInsert(feature2)) == null) continue;
                        statement = connection.prepareStatement(sql);
                        FeatureSchema schema = feature2.getSchema();
                        int position = 1;
                        int i = 0;
                        while (i < schema.getAttributeCount()) {
                            Attribute attr = schema.getAttribute(i);
                            if (!(attr instanceof AttributeCalculate)) {
                                if (attr.getType().equals(AttributeType.GEOMETRY)) {
                                    Geometry geom = (Geometry)feature2.getAttribute(i);
                                    if (geom == null) {
                                        geom = factory.buildGeometry(new ArrayList());
                                    }
                                    STRUCT struct = converter.toSDO(geom, this.srid);
                                    statement.setObject(position, struct);
                                } else {
                                    Object value = feature2.getAttribute(i);
                                    if (value == null || value.toString().trim().length() == 0) {
                                        statement.setNull(position, this.toSQLType(schema.getAttribute(i).getType()));
                                    }
                                    if (value instanceof java.util.Date) {
                                        AttributeType type = schema.getAttribute(i).getType();
                                        value = type == AttributeType.TIMESTAMP || type == AttributeType.TIME ? FeatureUtil.getGoodAttribute(type, value) : new Date(((java.util.Date)value).getTime());
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
                        LOGGER.error((Object)ex2.getMessage());
                    }
                }
                if (this.connectionTransaction == null) {
                    connection.commit();
                }
            }
            catch (SQLException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                if (feature2 != null) {
                    LOGGER.error((Object)("Ha fallado la feature " + feature2));
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
    public boolean createDataStore(Envelope vista, String geomColumn, int srid, boolean addRestrictions, boolean is3d) throws SQLException {
        String tableName = this.tableName.toUpperCase();
        String sqlExist = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE '" + tableName + "'";
        String sqlCreate = this.getSQLForCreateTable();
        String pkConstraint = "ALTER TABLE " + this.getFullTableName() + " ADD CONSTRAINT " + tableName + "_PK PRIMARY KEY(" + this.schema.getPrimaryKeyName() + ")";
        String sdoGeomMetadata = srid == -1 ? "INSERT INTO USER_SDO_GEOM_METADATA VALUES ('" + tableName + "','" + geomColumn + "'," + "MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X'," + vista.getMinX() + "," + vista.getMaxX() + ", 0.00000005)," + "MDSYS.SDO_DIM_ELEMENT('Y', " + vista.getMinY() + "," + vista.getMaxY() + ", 0.00000005)" + (is3d ? ",MDSYS.SDO_DIM_ELEMENT('Z',-20000,20000,0.00000005)" : "") + "),NULL)" : "INSERT INTO USER_SDO_GEOM_METADATA VALUES ('" + tableName + "','" + geomColumn + "'," + "MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X'," + vista.getMinX() + "," + vista.getMaxX() + ", 0.00000005)," + "MDSYS.SDO_DIM_ELEMENT('Y', " + vista.getMinY() + "," + vista.getMaxY() + ", 0.00000005)" + (is3d ? ",MDSYS.SDO_DIM_ELEMENT('Z',-20000,20000,0.00000005)" : "") + ")," + srid + ")";
        Connection connection = null;
        Statement statement = null;
        ResultSet res = null;
        try {
            connection = DataBaseConnectionFactory.getConnection(this);
            statement = connection.createStatement();
            res = statement.executeQuery(sqlExist);
            if (!res.next()) {
                statement.execute(sqlCreate);
                statement.execute(sdoGeomMetadata);
                statement.execute(pkConstraint);
                connection.commit();
                this.newTable = true;
            } else {
                this.newTable = false;
            }
            this.closeChannel(res, statement);
            boolean bl = this.newTable;
            return bl;
        }
        catch (SQLException ex) {
            LOGGER.error((Object)"", (Throwable)ex);
            this.closeChannel(res, statement);
            throw ex;
        }
        finally {
            this.closeConnection(connection);
        }
    }

    @Override
    public void initialize() throws Exception {
        this.obtenerAdapters();
        super.initialize();
    }

    @Override
    public void initialize(FeatureSchema schema, Envelope vista, int srid, boolean addRestrictions, boolean is3d) throws SQLException {
        this.obtenerAdapters();
        super.initialize(schema, vista, srid, addRestrictions, is3d);
    }

    @Override
    public void initialize(boolean ignoreGeometryType) throws SQLException {
        this.obtenerAdapters();
        super.initialize(ignoreGeometryType);
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
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
                if (this.connectionTransaction == null) {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    connection.setAutoCommit(false);
                } else {
                    connection = this.connectionTransaction;
                }
                GeometryConverter converter = new GeometryConverter(connection, factory);
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
                        if (!(attr instanceof AttributeCalculate)) {
                            if (attr.getType().equals(AttributeType.GEOMETRY)) {
                                Geometry geom = (Geometry)feature.getAttribute(i);
                                if (geom == null) {
                                    geom = factory.buildGeometry(new ArrayList());
                                }
                                STRUCT struct = converter.toSDO(geom, this.srid);
                                preparedStatement.setObject(position, struct);
                            } else {
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
    protected String getSQLForInsert(Feature feature) throws TopologyException {
        String selInsert = "INSERT INTO " + this.getFullTableName() + " (" + this.getBasicSQLInsert(feature) + " ) VALUES (";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                selInsert = String.valueOf(selInsert) + "?,";
            }
            ++i;
        }
        selInsert = selInsert.substring(0, selInsert.length() - 1);
        selInsert = String.valueOf(selInsert) + ")";
        return selInsert;
    }

    @Override
    protected String getSQLForUpdate(Feature feature) throws TopologyException {
        String selInsert = "UPDATE " + this.getFullTableName() + " SET ";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                selInsert = String.valueOf(selInsert) + attr.getName() + "=?,";
            }
            ++i;
        }
        selInsert = selInsert.substring(0, selInsert.length() - 1);
        selInsert = String.valueOf(selInsert) + " WHERE ";
        selInsert = String.valueOf(selInsert) + this.schema.getPrimaryKeyName() + " = '" + feature.getAttribute(this.schema.getPrimaryKeyName()).toString() + "'";
        return selInsert;
    }

    @Override
    protected String getSQLExpression(Filter filter) {
        String resultado = "";
        if (filter != null) {
            SQLEncoderOracle encoder = new SQLEncoderOracle(this.srid);
            try {
                resultado = encoder.encode(filter);
                resultado = StringUtil.replace(resultado, "\"", "", true);
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
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return resultado;
    }

    @Override
    public String processSQLExpression(String sql, String name1, String name2, String tableName) {
        sql = sql.replaceAll("\\b" + name1 + "\\b = ", String.valueOf(tableName) + "." + name2 + " = ");
        sql = sql.replaceAll("\\b" + name1 + "\\b > ", String.valueOf(tableName) + "." + name2 + " > ");
        sql = sql.replaceAll("\\b" + name1 + "\\b < ", String.valueOf(tableName) + "." + name2 + " < ");
        sql = sql.replaceAll("\\b" + name1 + "\\b >= ", String.valueOf(tableName) + "." + name2 + " >= ");
        sql = sql.replaceAll("\\b" + name1 + "\\b <= ", String.valueOf(tableName) + "." + name2 + " <= ");
        sql = sql.replaceAll("\\b" + name1 + "\\b != ", String.valueOf(tableName) + "." + name2 + " != ");
        sql = sql.replaceAll("\\b" + name1 + "\\b IS", String.valueOf(tableName) + "." + name2 + " IS");
        sql = sql.replaceAll("\\b" + name1 + "\\b LIKE", String.valueOf(tableName) + "." + name2 + " LIKE");
        return sql;
    }

    @Override
    public String processSQLExpressionSinComillas(String sql, String name1, String name2, String tableName) {
        return this.processSQLExpression(sql, name1, name2, tableName);
    }

    @Override
    protected String getSQLForQuery(Envelope envelope, Filter filter) {
        String sqlJoin;
        Filter sqlFilter = null;
        sqlFilter = filter != null ? (this.layerFilter != null ? ((Filter)((Cloneable)filter).clone()).and(this.layerFilter) : (Filter)((Cloneable)filter).clone()) : this.layerFilter;
        if (envelope == null && sqlFilter == null && !this.schema.isVersionable()) {
            return this.getRaizConsultaTipo();
        }
        String consultaSQL = String.valueOf(this.getRaizConsultaTipo()) + " WHERE ";
        if (envelope != null) {
            consultaSQL = String.valueOf(consultaSQL) + " mdsys.sdo_relate(" + this.getFullTableName() + "." + this.schema.getAttributeName(this.schema.getGeometryIndex()) + ",mdsys.sdo_geometry(3," + this.srid + ",NULL," + "mdsys.sdo_elem_info_array(1,3,3)," + "mdsys.sdo_ordinate_array(" + envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + "," + envelope.getMaxY() + ")),'mask=anyinteract querytype=window') = 'TRUE' AND ";
        }
        if ((sqlJoin = this.getSQLJoin()).length() > 0) {
            consultaSQL = String.valueOf(consultaSQL) + sqlJoin + " AND ";
        }
        if (sqlFilter != null) {
            consultaSQL = String.valueOf(consultaSQL) + "(" + this.getSQLExpression(sqlFilter) + ") AND ";
        }
        consultaSQL = this.schema.isVersionable() ? String.valueOf(consultaSQL) + this.schema.getEndDateFilter(this) : consultaSQL.substring(0, consultaSQL.length() - 4);
        LOGGER.debug((Object)consultaSQL);
        return consultaSQL;
    }

    @Override
    protected String getSQLOnlyGeometryForQuery(Envelope rectangle, Filter filter, List<String> orderByFields, boolean asc, List<String> labels) {
        Filter sqlFilter = null;
        sqlFilter = filter != null ? (this.layerFilter != null ? ((Filter)((Cloneable)filter).clone()).and(this.layerFilter) : (Filter)((Cloneable)filter).clone()) : this.layerFilter;
        String geomColumnName = this.schema.getAttributeName(this.schema.getGeometryIndex());
        String sqlQuery = "SELECT " + this.getFullTableName() + "." + geomColumnName + ",";
        if (this.schema.getPrimaryKey() != null) {
            sqlQuery = String.valueOf(sqlQuery) + this.getFullTableName() + "." + this.schema.getPrimaryKeyName() + ",";
        }
        if (labels != null) {
            for (String campo : labels) {
                Attribute attr = this.schema.getAttribute(campo);
                if (attr == null) {
                    LOGGER.error((Object)(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.The-attribute-{0}-is-null")) + campo + " ES NULO"));
                    continue;
                }
                if (attr.isCalculated()) {
                    Relation<?> rel = ((AttributeCalculate)attr).getRelation();
                    if (this.isCompatibleRelation(rel)) {
                        String tableName = this.getTableNameOfRelation(((AttributeCalculate)attr).getRelation());
                        sqlQuery = String.valueOf(sqlQuery) + tableName + "." + ((AttributeCalculate)attr).getRelationFieldName() + " AS " + attr.getName() + ",";
                        continue;
                    }
                    LOGGER.warn((Object)I18N.getMessage(OracleSpatialDataSource.class, "attribute-{0}-is-not-compatible-with-this-data-source", new Object[]{attr.getName()}));
                    continue;
                }
                sqlQuery = String.valueOf(sqlQuery) + this.getFullTableName() + "." + campo + ",";
            }
        }
        sqlQuery = String.valueOf(sqlQuery.substring(0, sqlQuery.length() - 1)) + " FROM ";
        String[] tables = this.getSQLTables();
        int i = 0;
        while (i < tables.length) {
            sqlQuery = String.valueOf(sqlQuery) + tables[i] + ",";
            ++i;
        }
        sqlQuery = sqlQuery.substring(0, sqlQuery.length() - 1);
        if (rectangle != null || sqlFilter != null || this.schema.isVersionable()) {
            String sqlJoin;
            sqlQuery = String.valueOf(sqlQuery) + " WHERE ";
            if (rectangle != null) {
                double xmin = rectangle.getMinX();
                double xmax = rectangle.getMaxX();
                double ymin = rectangle.getMinY();
                double ymax = rectangle.getMaxY();
                sqlQuery = String.valueOf(sqlQuery) + " mdsys.sdo_relate(" + this.getFullTableName() + "." + geomColumnName + ",mdsys.sdo_geometry(3," + this.srid + ",NULL," + "mdsys.sdo_elem_info_array(1,3,3)," + "mdsys.sdo_ordinate_array(" + xmin + "," + ymin + "," + xmax + "," + ymax + ")),'mask=anyinteract querytype=window') = 'TRUE' AND ";
            }
            if ((sqlJoin = this.getSQLJoin()).length() > 0) {
                sqlQuery = String.valueOf(sqlQuery) + sqlJoin + " AND ";
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

    protected String[] getSQLTables() {
        String[] sqlTables = null;
        HashSet<String> tables = new HashSet<String>();
        tables.add(this.getFullTableName());
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (attr instanceof AttributeCalculate) {
                AttributeCalculate attrCalculate = (AttributeCalculate)attr;
                Relation<?> rel = attrCalculate.getRelation();
                if (rel instanceof TableRelation) {
                    TableDBRecordDataSource tableDS;
                    Table table = ((TableRelation)rel).getTable();
                    if (table.getDataSource() instanceof TableDBRecordDataSource && this.checkHost((tableDS = (TableDBRecordDataSource)table.getDataSource()).getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword())) {
                        String tableName = ((TableDBRecordDataSource)table.getDataSource()).getFullTableName();
                        tables.add(tableName);
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    Layer layer = ((LayerRelation)rel).getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this)) {
                        tables.add(layerDS.getFullTableName());
                    }
                }
            }
            ++i;
        }
        sqlTables = new String[tables.size()];
        tables.toArray(sqlTables);
        return sqlTables;
    }

    protected String getSQLJoin() {
        String sqlJoin = "";
        Hashtable<String, String> procesados = new Hashtable<String, String>();
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (attr instanceof AttributeCalculate) {
                AttributeCalculate attrCalculate = (AttributeCalculate)attr;
                Relation<?> rel = attrCalculate.getRelation();
                if (rel instanceof TableRelation) {
                    TableDBRecordDataSource tableDS;
                    TableRelation tableRel = (TableRelation)rel;
                    if (tableRel.getTable().getDataSource() instanceof TableDBRecordDataSource && this.checkHost((tableDS = (TableDBRecordDataSource)tableRel.getTable().getDataSource()).getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword()) && (!procesados.containsKey(tableRel.getSourceAttribute()) || !tableRel.getAttributeTarget().equals(procesados.get(tableRel.getSourceAttribute())))) {
                        sqlJoin = String.valueOf(sqlJoin) + this.getFullTableName() + "." + tableRel.getSourceAttribute() + "=" + tableDS.getFullTableName() + "." + tableRel.getAttributeTarget() + "(+) AND ";
                        procesados.put(tableRel.getSourceAttribute(), tableRel.getAttributeTarget());
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    LayerRelation layerRel = (LayerRelation)rel;
                    Layer layer = layerRel.getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this) && (!procesados.containsKey(layerRel.getSourceAttribute()) || !layerRel.getAttributeTarget().equals(procesados.get(layerRel.getSourceAttribute())))) {
                        sqlJoin = String.valueOf(sqlJoin) + this.getFullTableName() + "." + layerRel.getSourceAttribute() + "=" + layerDS.getFullTableName() + "." + layerRel.getAttributeTarget() + "(+) AND ";
                        procesados.put(layerRel.getSourceAttribute(), layerRel.getAttributeTarget());
                    }
                }
            }
            ++i;
        }
        if (sqlJoin.length() > 0) {
            sqlJoin = sqlJoin.substring(0, sqlJoin.length() - 4);
        }
        return sqlJoin;
    }

    @Override
    public Feature readFeature(ResultSet resultSet) throws SQLException {
        BasicFeature feature = new BasicFeature(this.schema);
        Object objetoBaseDatos = null;
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.isAttributeForRead(i)) {
                Attribute attr = this.schema.getAttribute(i);
                objetoBaseDatos = attr.getType().equals(AttributeType.GEOMETRY) ? this.readGeometry(resultSet) : this.getValue(resultSet, attr.getName());
                feature.setAttribute(i, objetoBaseDatos);
            }
            ++i;
        }
        return feature;
    }

    @Override
    public Feature readOptimizedFeature(ResultSet resultSet, List<String> labels, boolean ignored) throws SQLException {
        BasicFeature featPK = new BasicFeature(this.schema);
        Object objetoBaseDatos = null;
        Geometry geometryObject = this.readGeometry(resultSet);
        featPK.setGeometry(geometryObject);
        if (this.schema.getPrimaryKey() != null) {
            String pkName = this.schema.getPrimaryKeyName();
            objetoBaseDatos = this.getValue(resultSet, this.schema.getPrimaryKeyName());
            featPK.setAttribute(pkName, objetoBaseDatos);
        }
        if (labels != null) {
            HashSet<AttributeCalculate> attrsCalculates = new HashSet<AttributeCalculate>();
            for (String label : labels) {
                Attribute attr = this.getSchema().getAttribute(label);
                if (!attr.isCalculated() || this.isCompatibleRelation(((AttributeCalculate)attr).getRelation())) {
                    objetoBaseDatos = this.getValue(resultSet, label);
                    featPK.setAttribute(label, objetoBaseDatos);
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

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Geometry readGeometry(ResultSet resultSet) {
        Geometry geometry;
        Connection con = null;
        try {
            Object object = resultSet.getObject(this.schema.getAttributeName(this.schema.getGeometryIndex()));
            con = this.getConnection();
            GeometryConverter converter = new GeometryConverter(con, factory);
            object = converter.asGeometry((STRUCT)object);
            geometry = (Geometry)object;
            if (con == null) return geometry;
        }
        catch (Exception ex1) {
            try {
                LOGGER.error((Object)"", (Throwable)ex1);
                if (con == null) return factory.createGeometryCollection(null);
            }
            catch (Throwable throwable) {
                if (con == null) throw throwable;
                this.closeConnection(con);
                throw throwable;
            }
            this.closeConnection(con);
            return factory.createGeometryCollection(null);
        }
        this.closeConnection(con);
        return geometry;
    }

    protected Object getValue(ResultSet rs, String fieldName) throws SQLException {
        Object val = null;
        int fieldId = rs.findColumn(fieldName);
        Object bdObj = rs.getObject(fieldName);
        if (bdObj != null) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnType = metaData.getColumnType(fieldId);
            switch (columnType) {
                case 91: {
                    val = rs.getDate(fieldId);
                    break;
                }
                case 93: {
                    val = rs.getTimestamp(fieldId);
                    break;
                }
                default: {
                    val = bdObj;
                    break;
                }
            }
        }
        return val;
    }

    private void obtenerAdapters() throws SQLException {
        OracleConnection connection = null;
        try {
            connection = (OracleConnection)DataBaseConnectionFactory.getConnection(this);
            this.buildGeometrySRID((Connection)connection);
            factory = this.srid != -1 ? new GeometryFactory(new PrecisionModel(), this.srid) : new GeometryFactory();
        }
        finally {
            this.closeConnection((Connection)connection);
        }
    }

    @Override
    protected AttributeType buildAttributeType(ResultSet rs) throws IOException {
        Class<?> type;
        block4: {
            int DATA_TYPE = 5;
            int TYPE_NAME = 6;
            try {
                int dataType = rs.getInt(5);
                String typeName = rs.getString(6);
                if (typeName.equals("SDO_GEOMETRY")) {
                    return AttributeType.toAttributeType(Geometry.class);
                }
                type = AbstractJDBCDataSource.TYPE_MAPPINGS.get(new Integer(dataType));
                if (type != null) break block4;
                return null;
            }
            catch (SQLException e) {
                throw new IOException(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.Oracle9iDataSource.sql-exception-occurred")) + " " + e.getMessage());
            }
        }
        return AttributeType.toAttributeType(type);
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
        sqlWhere = StringUtils.isEmpty((String)sqlWhere) ? " WHERE rownum<=" + limit : String.valueOf(sqlWhere) + " AND rownum<=" + limit;
        return String.valueOf(this.getRaizConsultaTipo()) + sqlWhere;
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
                        solucion = String.valueOf(solucion) + tableDS.getFullTableName() + "." + attrCalculate.getRelationFieldName() + " " + attr.getName() + ",";
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    LayerRelation layerRel = (LayerRelation)rel;
                    Layer layer = layerRel.getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this)) {
                        solucion = String.valueOf(solucion) + layerDS.getFullTableName() + "." + attrCalculate.getRelationFieldName() + " " + attr.getName() + ",";
                    }
                }
            } else {
                solucion = String.valueOf(solucion) + this.getFullTableName() + "." + this.schema.getAttributeName(i) + ",";
            }
            ++i;
        }
        return solucion.substring(0, solucion.length() - 1);
    }

    @Override
    protected String getRaizConsultaTipo() {
        String sql = "SELECT ";
        sql = String.valueOf(sql) + this.getSQLRoot();
        sql = String.valueOf(sql) + " FROM ";
        String[] tables = this.getSQLTables();
        int i = 0;
        while (i < tables.length) {
            sql = String.valueOf(sql) + tables[i] + ",";
            ++i;
        }
        return sql.substring(0, sql.length() - 1);
    }

    @Override
    protected void buildGeometrySRID(Connection connection) {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement(1003, 1007);
            resultSet = statement.executeQuery("SELECT SRID FROM MDSYS.ALL_SDO_GEOM_METADATA WHERE TABLE_NAME = '" + this.tableName.toUpperCase() + "'");
            if (resultSet.next()) {
                this.srid = resultSet.getInt(1);
                if (this.srid == 0) {
                    this.srid = -1;
                }
            } else {
                this.srid = -1;
            }
            this.closeChannel(resultSet, statement);
        }
        catch (Exception sqlException) {
            LOGGER.error((Object)"", (Throwable)sqlException);
            this.closeChannel(resultSet, statement);
        }
    }

    @Override
    public List<String> getAllGeometriesTables(String schema) throws SQLException, IOException {
        ArrayList<String> resultado = new ArrayList<String>();
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT TABLE_NAME FROM MDSYS.ALL_SDO_GEOM_METADATA WHERE OWNER='" + schema + "'");
            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                if (!this.allowTable(tableName)) continue;
                resultado.add(tableName);
            }
            this.closeChannel(resultSet, statement);
        }
        finally {
            this.closeConnection(con);
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
                    iterator = new OracleEditableIterator(sql, filter, this, this.getAllLabels());
                    iterator.setIgnoredUpdate(ignoredUpdate);
                } else {
                    iterator = new OracleIterator(sql, filter, this, this.getAllLabels());
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
    protected void buildFeatureSchema(Connection con) throws Exception {
        int COLUMN_NAME = 4;
        this.schema = new FeatureSchema();
        DatabaseMetaData dmd = con.getMetaData();
        String dbSchema = this.getDataBaseSchema();
        if (dbSchema != null) {
            dbSchema = dbSchema.toUpperCase();
        }
        ResultSet rs = dmd.getColumns(null, dbSchema, this.tableName.toUpperCase(), "%");
        while (rs.next()) {
            AttributeType tipo = this.buildAttributeType(rs);
            String columnName = rs.getString(4).toLowerCase();
            if (!this.schema.hasAttribute(columnName)) {
                this.schema.addAttribute(columnName, tipo);
                continue;
            }
            LOGGER.warn((Object)(String.valueOf(I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.Trying-to-add-repeated-attribute-{0}-to-the-schema", new Object[]{columnName})) + ". " + I18N.getString("org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource.It-will-be-ignored")));
        }
        rs.close();
        if (this.schema.getAttributeCount() == 0) {
            throw new Exception(I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.the-table-or-view-{0}-does-not-exist", new Object[]{this.tableName}));
        }
        if (this.pkName != null) {
            this.schema.getAttribute(this.pkName).setPrimaryKey(true);
        }
        this.resetLabels();
    }

    @Override
    public void createSpatialIndex() throws Exception {
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            Statement statement = con.createStatement();
            String sql = "CREATE INDEX " + this.tableName.toUpperCase() + "_idx " + "ON " + this.getFullTableName() + "(" + this.geomColName + ") " + "INDEXTYPE IS MDSYS.SPATIAL_INDEX";
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
        String baseSql = String.valueOf(this.getRaizConsultaTipo()) + " WHERE " + this.getFullTableName() + "." + pkName + " in (";
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
                con.commit();
                if (!con.isClosed()) {
                    con.close();
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
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
    protected String getSQLForCreateTable() {
        String sqlCreate = "CREATE TABLE   " + this.getFullTableName() + "(";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            sqlCreate = String.valueOf(sqlCreate) + this.schema.getAttributeName(i) + " " + attributeTypeToDBType.get(this.schema.getAttributeType(i));
            if (this.schema.getGeometryIndex() == i || this.schema.getPrimaryKeyIndex() == i) {
                sqlCreate = String.valueOf(sqlCreate) + " NOT NULL ";
            }
            sqlCreate = String.valueOf(sqlCreate) + ",";
            ++i;
        }
        sqlCreate = String.valueOf(sqlCreate.substring(0, sqlCreate.length() - 1)) + ")";
        return sqlCreate;
    }

    @Override
    public int toSQLType(AttributeType attrType) {
        int sqlType = attributeTypeToSQLType.get(attrType);
        return sqlType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OracleSpatialDataSource)) {
            return false;
        }
        OracleSpatialDataSource otherDS = (OracleSpatialDataSource)other;
        return this.getHostName().equals(otherDS.getHostName()) && this.getPort() == otherDS.getPort() && this.getDataBase().equals(otherDS.getDataBase()) && this.getUserName().equals(otherDS.getUserName()) && this.getPassword().equals(otherDS.getPassword());
    }

    @Override
    public int hashCode() {
        return this.hostName.hashCode() * this.port * this.userName.hashCode() * this.password.hashCode();
    }

    @Override
    public void createTemporalFields(String startField, String endField, String versionField) throws SQLException {
        String sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + startField + " timestamp DEFAULT current_timestamp";
        this.executeNonFeatureQuery(sql1);
        sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + endField + " timestamp";
        this.executeNonFeatureQuery(sql1);
        sql1 = "CREATE INDEX " + this.tableName + "_" + startField + "_idx ON " + this.getFullTableName() + "(" + startField + ")";
        this.executeNonFeatureQuery(sql1);
        sql1 = "CREATE INDEX " + this.tableName + "_" + endField + "_idx ON " + this.getFullTableName() + "(" + endField + ")";
        this.executeNonFeatureQuery(sql1);
        if (versionField != null && !versionField.trim().equals("")) {
            AttributeType pkType = this.getSchema().getPrimaryKey().getType();
            String attributeTypeName = attributeTypeToDBType.get(pkType);
            sql1 = "ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + versionField + " " + attributeTypeName;
            this.executeNonFeatureQuery(sql1);
        }
        this.labels = null;
    }

    @Override
    public void setPkName(String pkName) {
        block18: {
            if (pkName == null || pkName.equals("")) {
                Connection con = null;
                try {
                    try {
                        con = DataBaseConnectionFactory.getConnection(this);
                        DatabaseMetaData dmd = con.getMetaData();
                        ResultSet resPK = dmd.getPrimaryKeys(null, this.getDataBaseSchema(), this.tableName);
                        if (resPK.next()) {
                            pkName = resPK.getString(4);
                        }
                        resPK.close();
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (con != null) {
                            try {
                                con.close();
                            }
                            catch (SQLException e2) {
                                LOGGER.error((Object)"", (Throwable)e2);
                            }
                        }
                        break block18;
                    }
                }
                catch (Throwable throwable) {
                    if (con != null) {
                        try {
                            con.close();
                        }
                        catch (SQLException e) {
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                    }
                    throw throwable;
                }
                if (con != null) {
                    try {
                        con.close();
                    }
                    catch (SQLException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
            }
        }
        this.pkName = pkName.toLowerCase();
        if (this.schema != null) {
            if (this.schema.getPrimaryKey() != null) {
                this.schema.getPrimaryKey().setPrimaryKey(false);
            }
            Attribute attr = this.schema.getAttribute(this.pkName);
            attr.setPrimaryKey(true);
        }
    }

    @Override
    public String getFullTableName() {
        if (StringUtils.isEmpty((String)this.dataBaseSchema)) {
            return this.tableName.toUpperCase();
        }
        return String.valueOf(this.dataBaseSchema) + "." + this.tableName.toUpperCase();
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        ArrayList<Object> keys;
        block12: {
            keys = new ArrayList<Object>();
            String sql = "SELECT " + this.getFullTableName() + "." + this.schema.getPrimaryKeyName() + " FROM ";
            String[] tables = this.getSQLTables();
            int i = 0;
            while (i < tables.length) {
                sql = String.valueOf(sql) + tables[i] + ",";
                ++i;
            }
            sql = sql.substring(0, sql.length() - 1);
            String sqlJoin = this.getSQLJoin();
            if (sqlJoin.length() > 0 || this.layerFilter != null || this.schema.isVersionable()) {
                String sqlWhere = "";
                if (sqlJoin.length() > 0) {
                    sqlWhere = String.valueOf(sqlWhere) + "WHERE " + sqlJoin;
                }
                if (this.layerFilter != null) {
                    sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter) : String.valueOf(sqlWhere) + " AND " + this.getSQLExpression(this.layerFilter);
                }
                if (this.schema.isVersionable()) {
                    sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                }
                sql = String.valueOf(sql) + sqlWhere;
            }
            sql = String.valueOf(sql) + " ORDER BY " + this.schema.getPrimaryKeyName();
            Connection connection = null;
            Statement statement = null;
            try {
                try {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    statement = connection.createStatement();
                    ResultSet res = statement.executeQuery(sql);
                    while (res.next()) {
                        keys.add(res.getObject(1));
                    }
                    res.close();
                    statement.close();
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
        for (Feature element : this.deletedFeatures) {
            keys.remove(element.getPrimaryKey());
        }
        keys.addAll(this.newFeatures);
        return keys;
    }

    @Override
    public int size() {
        block12: {
            if (this.size == -1) {
                Connection conn = null;
                Statement statement = null;
                ResultSet resultset = null;
                try {
                    try {
                        conn = DataBaseConnectionFactory.getConnection(this);
                        statement = conn.createStatement();
                        String sql = "SELECT COUNT(1) FROM ";
                        String[] tables = this.getSQLTables();
                        int i = 0;
                        while (i < tables.length) {
                            sql = String.valueOf(sql) + tables[i] + ",";
                            ++i;
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        String sqlJoin = this.getSQLJoin();
                        if (sqlJoin.length() > 0 || this.layerFilter != null || this.schema.isVersionable()) {
                            String sqlWhere = "";
                            if (sqlJoin.length() > 0) {
                                sql = String.valueOf(sql) + " WHERE " + sqlJoin;
                            }
                            if (this.layerFilter != null) {
                                sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter) : String.valueOf(sqlWhere) + " AND " + this.getSQLExpression(this.layerFilter);
                            }
                            if (this.schema.isVersionable()) {
                                sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                            }
                            sql = String.valueOf(sql) + sqlWhere;
                        }
                        if ((resultset = statement.executeQuery(sql)).next()) {
                            this.size = resultset.getInt(1);
                        }
                        this.closeChannel(resultset, statement);
                    }
                    catch (SQLException ex) {
                        this.closeChannel(resultset, statement);
                        LOGGER.error((Object)"", (Throwable)ex);
                        this.closeConnection(conn);
                        break block12;
                    }
                }
                catch (Throwable throwable) {
                    this.closeConnection(conn);
                    throw throwable;
                }
                this.closeConnection(conn);
            }
        }
        int trueSize = this.size;
        trueSize += this.newFeatures.size();
        return trueSize -= this.deletedFeatures.size();
    }

    @Override
    public void removeDataStore() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement();
                String sqlForDropTable = this.getSQLForDropTable();
                String sqlForDropSDOMetadataRow = this.getSQLForDropSDOMetadataRow();
                statement.execute(sqlForDropTable);
                statement.execute(sqlForDropSDOMetadataRow);
                statement.close();
            }
            catch (SQLException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                throw ex;
            }
        }
        finally {
            this.closeConnection(connection);
        }
    }

    @Override
    protected String getSQLForDropTable() {
        String sql = "DROP TABLE " + this.getFullTableName();
        return sql;
    }

    private String getSQLForDropSDOMetadataRow() {
        String sql = "DELETE FROM MDSYS.USER_SDO_GEOM_METADATA WHERE TABLE_NAME = '" + this.tableName.toUpperCase() + "'";
        return sql;
    }

    @Override
    public Object clone() {
        OracleSpatialDataSource clone = new OracleSpatialDataSource(this.getHostName(), this.getPort(), this.getDataBase(), this.getUserName(), this.getPassword());
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
            SQLEncoderOracle encoder = new SQLEncoderOracle(-1);
            sql = String.valueOf(sql) + " " + encoder.encode(finalFilter);
            JDBCIterator it = null;
            try {
                boolean check = this.checkIfFilterIsCompatible(filter);
                it = this.editable ? new OracleEditableIterator(sql, (Filter)((Cloneable)filter).clone(), this, this.getAllLabels(), !check) : new OracleIterator(sql, (Filter)((Cloneable)filter).clone(), this, this.getAllLabels(), !check);
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
            return new OracleEditableIterator(sql, geometryFilter, this, labels, !check);
        }
        return new OracleIterator(sql, geometryFilter, this, labels, !check);
    }

    public static void main(String[] args) throws Exception {
        int testOracleSRID = 4326;
        int testEPSGCode = 4326;
        try {
            int epsgCode = EpsgToOracleCodeConverter.getInstance().oracleCodeToEPSGCode(testOracleSRID);
            LOGGER.info((Object)(String.valueOf(I18N.getString(OracleSpatialDataSource.class, "found-epsg-code-for-the-oracle-code")) + testOracleSRID + " -> " + epsgCode));
        }
        catch (OracleToEpsgCodeNotFoundException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            int oracleCode = EpsgToOracleCodeConverter.getInstance().epsgCodeToOracleCode(testEPSGCode);
            LOGGER.info((Object)(String.valueOf(I18N.getString(OracleSpatialDataSource.class, "found-oracle-srid-for-the-oracle-code")) + testEPSGCode + " -> " + oracleCode));
        }
        catch (EpsgToOracleCodeNotFoundException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    protected boolean isCompatibleRelation(Relation<?> rel) {
        if (rel instanceof TableRelation) {
            Table table = ((TableRelation)rel).getTable();
            if (table.getDataSource() instanceof OracleDataSource) {
                TableDBRecordDataSource tableDS = (TableDBRecordDataSource)table.getDataSource();
                return this.checkHost(tableDS.getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getDataBaseSchema().equals(this.getDataBaseSchema()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword());
            }
        } else {
            AbstractJDBCDataSource layerDS;
            LayerRelation layerRel = (LayerRelation)rel;
            Layer layer = layerRel.getTargetLayer();
            if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()) instanceof OracleSpatialDataSource) {
                return this.checkHost(layerDS.getHostName()) && layerDS.getPort() == this.getPort() && layerDS.getDataBase().equals(this.getDataBase()) && layerDS.getDataBaseSchema().equals(this.getDataBaseSchema()) && layerDS.getUserName().equals(this.getUserName()) && layerDS.getPassword().equals(this.getPassword());
            }
        }
        return false;
    }

    @Override
    protected boolean allowTable(String tableName) {
        if (tableName.endsWith("$")) {
            return false;
        }
        if (tableName.startsWith("BIN$")) {
            return false;
        }
        if (tableName.startsWith("XDB$")) {
            return false;
        }
        if (tableName.startsWith("DR$")) {
            return false;
        }
        if (tableName.startsWith("DEF$")) {
            return false;
        }
        if (tableName.startsWith("SDO_")) {
            return false;
        }
        if (tableName.startsWith("WM$")) {
            return false;
        }
        if (tableName.startsWith("WK$")) {
            return false;
        }
        if (tableName.startsWith("AW$")) {
            return false;
        }
        if (tableName.startsWith("AQ$")) {
            return false;
        }
        if (tableName.startsWith("APPLY$")) {
            return false;
        }
        if (tableName.startsWith("REPCAT$")) {
            return false;
        }
        if (tableName.startsWith("CWM$")) {
            return false;
        }
        if (tableName.startsWith("CWM2$")) {
            return false;
        }
        if (tableName.startsWith("EXF$")) {
            return false;
        }
        if (tableName.startsWith("DM$")) {
            return false;
        }
        if (tableName.startsWith("DRV$")) {
            return false;
        }
        if (tableName.startsWith("KU$")) {
            return false;
        }
        if (tableName.startsWith("V_$")) {
            return false;
        }
        if (tableName.startsWith("OL$")) {
            return false;
        }
        if (tableName.startsWith("WRI$")) {
            return false;
        }
        if (tableName.startsWith("GV_$")) {
            return false;
        }
        return !tableName.startsWith("V$");
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
                    String sqlStatement = "SELECT meta.SDO_LAYER_GTYPE FROM MDSYS.ALL_SDO_INDEX_INFO info INNER JOIN MDSYS.USER_SDO_INDEX_METADATA meta ON info.INDEX_NAME = meta.SDO_INDEX_NAME WHERE info.TABLE_NAME = '" + this.tableName + "' " + "AND info.COLUMN_NAME = '" + StringUtils.upperCase((String)this.geomColName) + "' " + "AND info.TABLE_OWNER = '" + this.dataBaseSchema + "'";
                    resultSet = statement.executeQuery(sqlStatement);
                    if (resultSet.next()) {
                        String type = resultSet.getString(1);
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
                        } else if (type.equalsIgnoreCase("LINE") || type.equalsIgnoreCase("CURVE")) {
                            this.schema.setGeometryType(3);
                            ok = true;
                        } else if (type.equalsIgnoreCase("MULTILINE") || type.equalsIgnoreCase("MULTICURVE")) {
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
    protected String escapePkValueIfNeeded(String value) {
        return value;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPool() throws SQLException {
        OracleConnectionPoolDataSource dataSource = new OracleConnectionPoolDataSource();
        dataSource.setDriverType("thin");
        dataSource.setServerName(this.hostName);
        dataSource.setPortNumber(this.port);
        dataSource.setServiceName(this.dataBase);
        dataSource.setUser(this.userName);
        dataSource.setPassword(this.password);
        Properties props = dataSource.getConnectionProperties();
        if (props == null) {
            props = new Properties();
        }
        props.setProperty("includeSynonyms", "true");
        dataSource.setConnectionProperties(props);
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
        return 1521;
    }

    @Override
    public String convertAttributeTypeToDBType(AttributeType type) {
        return attributeTypeToDBType.get(type);
    }
}

