/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.TopologyException
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.model.IQueryable;
import com.vividsolutions.jump.workbench.model.Layer;
import es.kosmo.core.dao.UnknownGeometryTypeException;
import es.kosmo.core.geometry.filters.ZCoordinateCountFilter;
import es.kosmo.core.utils.FeatureSchemaUtils;
import es.kosmo.core.utils.GeometryUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.crypt.CryptManager;
import org.saig.core.crypt.CryptManagerFactory;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.MySQLDataSource;
import org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.DBByNextGIDKeyResolver;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;
import org.saig.core.dao.datasource.dbdatasource.utils.Field;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterUtil;
import org.saig.core.filter.GeometryFilterImpl;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.TableRelation;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public abstract class AbstractJDBCDataSource
extends AbstractDataSource
implements Cloneable,
IQueryable {
    public static final Logger LOGGER = Logger.getLogger(AbstractJDBCDataSource.class);
    protected static final Map<Integer, Class<?>> TYPE_MAPPINGS = new HashMap();
    protected static final Map<String, String> AGGREGATE_FUNCTIONS_MAP = new HashMap<String, String>();
    public static final String USER_KEY = "USER";
    public static final String PASSWORD_KEY = "PASSWORD";
    public static final String TABLE_NAME_KEY = "TABLE_NAME";
    public static final String SCHEMA_KEY = "SCHEMA";
    public static final String DATABASE_KEY = "DATABASE";
    public static final String PRIMARY_KEY_COLUMN_NAME = "PRIMARY_KEY_COLUMN_NAME";
    public static final String HOST_KEY = "HOST";
    public static final String PORT_KEY = "PORT";
    public static final String DATASOURCE_CLASS_KEY = "DATASOURCE";
    protected String hostName;
    protected int port;
    protected String dataBase;
    protected String dataBaseSchema;
    protected String userName;
    protected String password;
    protected String tableName;
    protected String pkName;
    protected Connection connectionTransaction;
    protected int srid = -1;
    protected boolean iniciado = false;
    protected boolean newTable = false;
    protected List<String> labels;
    protected IDBKeyResolver keyResolver;

    static {
        TYPE_MAPPINGS.put(12, String.class);
        TYPE_MAPPINGS.put(1, String.class);
        TYPE_MAPPINGS.put(-1, String.class);
        TYPE_MAPPINGS.put(-7, Boolean.class);
        TYPE_MAPPINGS.put(16, Boolean.class);
        TYPE_MAPPINGS.put(-6, Short.class);
        TYPE_MAPPINGS.put(5, Short.class);
        TYPE_MAPPINGS.put(4, Integer.class);
        TYPE_MAPPINGS.put(-5, Long.class);
        TYPE_MAPPINGS.put(7, Float.class);
        TYPE_MAPPINGS.put(6, Double.class);
        TYPE_MAPPINGS.put(8, Double.class);
        TYPE_MAPPINGS.put(3, BigDecimal.class);
        TYPE_MAPPINGS.put(2, BigDecimal.class);
        TYPE_MAPPINGS.put(91, Date.class);
        TYPE_MAPPINGS.put(92, Time.class);
        TYPE_MAPPINGS.put(93, Timestamp.class);
        TYPE_MAPPINGS.put(2004, Object.class);
        TYPE_MAPPINGS.put(2005, Object.class);
        TYPE_MAPPINGS.put(-2, Object.class);
        TYPE_MAPPINGS.put(-3, Object.class);
        TYPE_MAPPINGS.put(-4, Object.class);
        AGGREGATE_FUNCTIONS_MAP.put("OP_COUNT", "count");
        AGGREGATE_FUNCTIONS_MAP.put("OP_AVG", "avg");
        AGGREGATE_FUNCTIONS_MAP.put("OP_SUM", "sum");
        AGGREGATE_FUNCTIONS_MAP.put("OP_MIN", "min");
        AGGREGATE_FUNCTIONS_MAP.put("OP_MAX", "max");
        AGGREGATE_FUNCTIONS_MAP.put("OP_VARIANCE", "variance");
        AGGREGATE_FUNCTIONS_MAP.put("OP_STANDARD_DEVIANCE", "stddev");
    }

    public AbstractJDBCDataSource() {
    }

    public AbstractJDBCDataSource(String host, int port, String databaseName, String username, String password) {
        this.hostName = host;
        this.port = port;
        this.dataBase = databaseName;
        this.userName = username;
        this.password = password;
    }

    @Override
    public synchronized void add(Feature feature) throws Exception {
        ArrayList<Feature> featuresToAdd = new ArrayList<Feature>();
        featuresToAdd.add(feature);
        this.addAll(featuresToAdd);
    }

    @Override
    public synchronized void addAll(Collection<Feature> features) throws Exception {
        this.addAll(features, false);
    }

    public synchronized void addAll(Collection<Feature> features, boolean setId) throws Exception {
        this.addAll(features, setId, true);
    }

    public synchronized void addAll(Collection<Feature> features, boolean setId, boolean checkVersionable) throws Exception {
        if (!this.editable || features == null) {
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
                    HashSet keysColumns = new HashSet();
                    if (setId || feature.getPrimaryKey() == null) {
                        this.processFeaturePrimaryKey(keyResolver, connection, feature);
                    }
                    try {
                        String sql;
                        if (checkVersionable && this.schema.isVersionable()) {
                            feature.setAttribute(this.schema.getFieldStartDate(), (Object)new java.util.Date());
                        }
                        if ((sql = this.getSQLForInsert(feature)) == null) continue;
                        statement = connection.prepareStatement(sql);
                        int position = 1;
                        int i = 0;
                        while (i < this.schema.getAttributeCount()) {
                            Attribute attr = this.schema.getAttribute(i);
                            if (!(attr instanceof AttributeCalculate || attr.getType().equals(AttributeType.GEOMETRY) || keysColumns.contains(attr.getName()))) {
                                Object value = feature.getAttribute(attr.getName());
                                if (value == null || value.toString().trim().length() == 0) {
                                    statement.setNull(position, this.toSQLType(this.schema.getAttribute(i).getType()));
                                } else {
                                    if (value instanceof java.util.Date) {
                                        AttributeType type = this.schema.getAttribute(i).getType();
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

    protected void processFeaturePrimaryKey(IDBKeyResolver keyResolver, Connection conn, Feature feature) throws Exception {
        Object[] pkValues = keyResolver.getKey(conn);
        int i = 0;
        while (i < pkValues.length) {
            Object[] pkColumnValue = (Object[])pkValues[i];
            String pkName = (String)pkColumnValue[0];
            Object pkValue = pkColumnValue[1];
            Attribute pkAttr = this.schema.getAttribute(pkName);
            pkValue = FeatureUtil.getGoodAttribute(pkAttr.getType(), pkValue);
            feature.setAttribute(pkName, pkValue);
            ++i;
        }
    }

    public void resetSize() {
        this.size = -1;
    }

    protected abstract String getSQLForInsert(Feature var1);

    public abstract Feature readFeature(ResultSet var1) throws Exception;

    public abstract Feature readOptimizedFeature(ResultSet var1, List<String> var2, boolean var3) throws Exception;

    public abstract Geometry readGeometry(ResultSet var1);

    public String getDataBase() {
        return this.dataBase;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public int getPort() {
        return this.port;
    }

    public String getPkName() {
        return this.pkName;
    }

    public void setPkName(String pkName) {
        block10: {
            if (StringUtils.isEmpty((String)pkName)) {
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
                        this.closeConnection(con);
                        break block10;
                    }
                }
                catch (Throwable throwable) {
                    this.closeConnection(con);
                    throw throwable;
                }
                this.closeConnection(con);
            }
        }
        this.pkName = pkName;
        if (this.schema != null) {
            Attribute attr;
            if (this.schema.getPrimaryKey() != null) {
                this.schema.getPrimaryKey().setPrimaryKey(false);
            }
            if ((attr = this.schema.getAttribute(this.pkName)) != null) {
                attr.setPrimaryKey(true);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return DataBaseConnectionFactory.getConnection(this);
    }

    public String getPassword() {
        return this.password;
    }

    public String getEncryptedPassword() {
        String encryptedPassword = "";
        try {
            CryptManager manager = CryptManagerFactory.getManager("Password based encryption");
            encryptedPassword = manager.encrypt(this.password);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return encryptedPassword;
    }

    public String getHostName() {
        return this.hostName;
    }

    protected String getDefaultSQLKeyResolver() {
        return "SELECT COALESCE(MAX(" + this.escapeAttributeName(this.pkName) + "),0) AS " + this.escapeAttributeName(this.pkName) + " FROM " + this.getFullTableName();
    }

    protected IDBKeyResolver getKeyResolver(Connection conn) throws Exception {
        if (this.keyResolver == null) {
            DBByNextGIDKeyResolver defaultKeyResolver = new DBByNextGIDKeyResolver(conn, this.getDefaultSQLKeyResolver(), this.pkName);
            return defaultKeyResolver;
        }
        return this.keyResolver;
    }

    protected void closeChannel(ResultSet resultset, Statement statement) {
        if (resultset != null) {
            try {
                resultset.close();
            }
            catch (SQLException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
            }
        }
    }

    protected String getRaizConsultaTipo() {
        String sql = "SELECT ";
        sql = String.valueOf(sql) + this.getSQLRoot();
        sql = String.valueOf(sql) + " FROM " + this.getTables();
        return sql;
    }

    protected String getTables() {
        HashSet<String> tables = new HashSet<String>();
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
                        String subConsulta = "LEFT JOIN " + this.escapeAttributeName(tableName) + " ON (";
                        subConsulta = String.valueOf(subConsulta) + this.getFullTableName() + "." + this.escapeAttributeName(tableRel.getSourceAttribute()) + "=" + tableName + "." + this.escapeAttributeName(tableRel.getAttributeTarget()) + ")";
                        tables.add(subConsulta);
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
                        solucion = String.valueOf(solucion) + tableDS.getFullTableName() + "." + this.escapeAttributeName(attrCalculate.getRelationFieldName()) + " AS " + attrCalculate.getName() + ",";
                    }
                } else {
                    AbstractJDBCDataSource layerDS;
                    LayerRelation layerRel = (LayerRelation)rel;
                    Layer layer = layerRel.getTargetLayer();
                    if (layer.isDataBaseDataSource() && (layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor()).equals(this)) {
                        solucion = String.valueOf(solucion) + layerDS.getFullTableName() + "." + this.escapeAttributeName(attrCalculate.getRelationFieldName()) + " AS " + attrCalculate.getName() + ",";
                    }
                }
            } else {
                AttributeType attrType = this.schema.getAttributeType(i);
                solucion = attrType == AttributeType.GEOMETRY ? String.valueOf(solucion) + " asText(" + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getAttributeName(i)) + ") AS " + this.schema.getAttributeName(i) + "," : String.valueOf(solucion) + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getAttributeName(i)) + ",";
            }
            ++i;
        }
        return solucion.substring(0, solucion.length() - 1);
    }

    protected void connectionRollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            }
            catch (SQLException ex) {
                LOGGER.error((Object)ex);
            }
        }
    }

    protected void closeConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
            }
        }
        catch (SQLException ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
        catch (Exception ex) {
            LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    protected abstract String getSQLForCreateTable();

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

    protected String getBasicSQLInsert(Feature feature) {
        FeatureSchema schema = feature.getSchema();
        String sql = "";
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute attr = schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                sql = String.valueOf(sql) + this.escapeAttributeName(schema.getAttributeName(i)) + ",";
            }
            ++i;
        }
        sql = sql.substring(0, sql.length() - 1);
        return sql;
    }

    @Override
    public int size() throws Exception {
        if (this.size == -1) {
            Connection conn = null;
            Statement statement = null;
            ResultSet resultset = null;
            try {
                try {
                    conn = DataBaseConnectionFactory.getConnection(this);
                    statement = conn.createStatement();
                    String sql = "SELECT COUNT(1) FROM " + this.getTables();
                    String sqlWhere = "";
                    if (this.layerFilter != null) {
                        sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
                    }
                    if (this.schema.isVersionable()) {
                        sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
                    }
                    if ((resultset = statement.executeQuery(sql = String.valueOf(sql) + sqlWhere)).next()) {
                        this.size = resultset.getInt(1);
                    }
                    this.closeChannel(resultset, statement);
                }
                catch (SQLException ex) {
                    this.closeChannel(resultset, statement);
                    LOGGER.error((Object)"", (Throwable)ex);
                    throw ex;
                }
            }
            finally {
                this.closeConnection(conn);
            }
        }
        int trueSize = this.size;
        trueSize += this.newFeatures.size();
        return trueSize -= this.deletedFeatures.size();
    }

    @Override
    public synchronized void removeByPKs(List<Object> pks) throws Exception {
        if (!this.editable || CollectionUtils.isEmpty(pks)) {
            return;
        }
        if (this.inMemory) {
            throw new Exception(I18N.getString(AbstractJDBCDataSource.class, "unsupported-memory-access"));
        }
        String pkName = this.schema.getPrimaryKeyName();
        boolean check = false;
        String sqlDelete = "";
        sqlDelete = !this.schema.isVersionable() ? "DELETE FROM " + this.getFullTableName() + " WHERE " + this.escapeAttributeName(pkName) + " IN (" : "UPDATE " + this.getFullTableName() + " SET " + this.escapeAttributeName(this.schema.getFieldEndDate()) + "=? WHERE " + this.escapeAttributeName(pkName) + " IN (";
        for (Object pk : pks) {
            check = true;
            sqlDelete = String.valueOf(sqlDelete) + this.escapePkValueIfNeeded(pk.toString()) + ",";
        }
        if (!check) {
            return;
        }
        sqlDelete = String.valueOf(sqlDelete.substring(0, sqlDelete.length() - 1)) + ")";
        this.executeDelete(sqlDelete);
    }

    protected void executeDelete(String sqlDelete) throws Exception {
        Connection connection = null;
        try {
            try {
                if (this.connectionTransaction == null) {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    connection.setAutoCommit(false);
                } else {
                    connection = this.connectionTransaction;
                }
                if (!this.schema.isVersionable()) {
                    Statement statement = connection.createStatement();
                    statement.execute(sqlDelete);
                    statement.close();
                } else {
                    PreparedStatement ps = connection.prepareStatement(sqlDelete);
                    ps.setObject(1, new Timestamp(System.currentTimeMillis()));
                    ps.execute();
                    ps.close();
                }
                if (this.connectionTransaction == null) {
                    connection.commit();
                }
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.connectionRollback(connection);
                throw e;
            }
        }
        finally {
            if (this.connectionTransaction == null) {
                this.closeConnection(connection);
            }
        }
    }

    @Override
    public synchronized void removeAll(Collection<Feature> features) throws Exception {
        if (!this.editable || CollectionUtils.isEmpty(features)) {
            return;
        }
        if (this.inMemory) {
            for (Feature element : features) {
                if (this.newFeatures.contains(element)) {
                    this.newFeatures.remove(element);
                    continue;
                }
                if (this.updateFeatures.contains(element)) {
                    this.updateFeatures.remove(element);
                }
                this.deletedFeatures.add(element);
            }
            return;
        }
        String pkName = this.schema.getPrimaryKeyName();
        boolean check = false;
        String sqlDelete = "";
        sqlDelete = !this.schema.isVersionable() ? "DELETE FROM " + this.getFullTableName() + " WHERE " + this.escapeAttributeName(pkName) + " IN (" : "UPDATE " + this.getFullTableName() + " SET " + this.escapeAttributeName(this.schema.getFieldEndDate()) + "=? WHERE " + this.escapeAttributeName(pkName) + " IN (";
        for (Feature feature : features) {
            if (feature.isUnsaved()) continue;
            check = true;
            sqlDelete = String.valueOf(sqlDelete) + this.escapePkValueIfNeeded(feature.getAttribute(pkName).toString()) + ",";
        }
        if (!check) {
            return;
        }
        sqlDelete = String.valueOf(sqlDelete.substring(0, sqlDelete.length() - 1)) + ")";
        this.executeDelete(sqlDelete);
    }

    protected String escapePkValueIfNeeded(String value) {
        return value;
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values) {
        return this.getByAttribute(names, values, null, null);
    }

    public List<Object[]> executeNonFeatureQuery(String sql, int n) {
        ArrayList<Object[]> results = new ArrayList<Object[]>();
        Connection con = null;
        try {
            try {
                if (this.connectionTransaction == null) {
                    con = DataBaseConnectionFactory.getConnection(this);
                    con.setAutoCommit(false);
                } else {
                    con = this.connectionTransaction;
                }
                Statement st = con.createStatement();
                ResultSet res = st.executeQuery(sql);
                while (res.next()) {
                    Object[] fila = new Object[n];
                    int i = 1;
                    while (i <= n) {
                        fila[i - 1] = res.getObject(i);
                        ++i;
                    }
                    results.add(fila);
                }
                this.closeChannel(res, st);
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (this.connectionTransaction == null) {
                    this.closeConnection(con);
                }
            }
        }
        finally {
            if (this.connectionTransaction == null) {
                this.closeConnection(con);
            }
        }
        return results;
    }

    public void executeNonFeatureQuery(String sql) throws SQLException {
        this.executeNonFeatureQuery(sql, true);
    }

    public void executeNonFeatureQuery(String sql, boolean commitChanges) throws SQLException {
        Connection con = null;
        try {
            if (this.connectionTransaction == null) {
                con = DataBaseConnectionFactory.getConnection(this);
                con.setAutoCommit(false);
            } else {
                con = this.connectionTransaction;
            }
            Statement st = con.createStatement();
            st.execute(sql);
            if (commitChanges && !con.getAutoCommit()) {
                con.commit();
            }
            this.closeChannel(null, st);
        }
        finally {
            if (this.connectionTransaction == null) {
                this.closeConnection(con);
            }
        }
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered) {
        return this.getByAttribute(names, values, fieldOrdered, null);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, Filter filter) {
        return this.getByAttribute(names, values, fieldOrdered, true, filter);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, boolean ascending) {
        return this.getByAttribute(atributeNames, atributeValues, fieldOrdered, ascending, null);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        String sqlFilter = "";
        int i = 0;
        while (i < names.length) {
            if (values[i] != null) {
                Object value = values[i];
                if (value instanceof java.util.Date) {
                    value = this.schema.getAttribute(names[i]).getType() == AttributeType.TIMESTAMP ? new Timestamp(((java.util.Date)value).getTime()) : new Date(((java.util.Date)value).getTime());
                }
                sqlFilter = String.valueOf(sqlFilter) + names[i] + "='" + value.toString() + "' AND ";
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

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        Envelope vista = new Envelope();
        FeatureIterator geomIterator = null;
        try {
            geomIterator = this.queryGeometryIterator(null, filter, null);
            while (geomIterator.hasNext()) {
                Geometry geom = geomIterator.next().getGeometry();
                if (geom == null) continue;
                vista.expandToInclude(geom.getEnvelopeInternal());
            }
        }
        finally {
            if (geomIterator != null) {
                geomIterator.close();
            }
        }
        Envelope fullEnvelope = this.expandEnvelope(vista, this.getNewFeatures());
        return this.expandEnvelope(fullEnvelope, this.getUpdatedFeatures());
    }

    protected Envelope expandEnvelope(Envelope envelope, Collection<Feature> features) {
        if (CollectionUtils.isEmpty(features)) {
            return new Envelope(envelope);
        }
        Envelope fullEnvelope = new Envelope();
        if (envelope != null) {
            fullEnvelope.expandToInclude(envelope);
        }
        for (Feature element : features) {
            if (element.getGeometry() == null) continue;
            fullEnvelope.expandToInclude(element.getGeometry().getEnvelopeInternal());
        }
        return fullEnvelope;
    }

    @Override
    public FeatureIterator getFeaturesIterator() {
        return this.queryGeometryIterator(null, null, this.getAllLabels());
    }

    public FeatureIterator getIteratorByPrimaryKey(Object[] keys) {
        return this.queryGeometryIterator(null, this.getFilterByPrimaryKey(keys), this.getAllLabels());
    }

    protected String getGeomEnvelopeText(Envelope rectangle) {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] coordenadas = new Coordinate[]{new Coordinate(rectangle.getMinX(), rectangle.getMinY()), new Coordinate(rectangle.getMaxX(), rectangle.getMinY()), new Coordinate(rectangle.getMaxX(), rectangle.getMaxY()), new Coordinate(rectangle.getMinX(), rectangle.getMaxY()), new Coordinate(rectangle.getMinX(), rectangle.getMinY())};
        Polygon geom = factory.createPolygon(factory.createLinearRing(coordenadas), null);
        return geom.toText();
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle) {
        return this.queryGeometryIterator(rectangle, null, this.getAllLabels());
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle, Filter filter) {
        return this.queryGeometryIterator(rectangle, filter, this.getAllLabels());
    }

    protected abstract String getSQLForQuery(Envelope var1, Filter var2);

    public String getSQLForQuery(Envelope rectangle, Filter filter, String[] orderByFields, boolean asc) {
        ArrayList<String> fields = new ArrayList<String>();
        if (orderByFields != null) {
            int i = 0;
            while (i < orderByFields.length) {
                fields.add(orderByFields[i]);
                ++i;
            }
        }
        return this.getSQLOnlyGeometryForQuery(rectangle, filter, fields, asc, this.getAllLabels());
    }

    protected abstract String getSQLOnlyGeometryForQuery(Envelope var1, Filter var2, List<String> var3, boolean var4, List<String> var5);

    public boolean isInitialized() {
        return this.iniciado;
    }

    @Override
    public List<Feature> query(Envelope view, Filter filter) {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        FeatureIterator itFeatures = null;
        try {
            try {
                itFeatures = this.queryIterator(view, filter);
                while (itFeatures.hasNext()) {
                    Feature feature = itFeatures.next();
                    if (feature == null) continue;
                    resultado.add(feature);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itFeatures != null) {
                    itFeatures.close();
                }
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        return resultado;
    }

    @Override
    public List<Feature> query(Filter filter) throws Exception {
        return this.query(null, filter);
    }

    @Override
    public List<Feature> query(Envelope envelope) throws Exception {
        return this.query(envelope, null);
    }

    protected abstract String getLimitSQL(int var1);

    protected abstract AttributeType buildAttributeType(ResultSet var1) throws IOException;

    protected boolean buildGeometryType(Connection con) {
        boolean found;
        block7: {
            ResultSet res = null;
            Statement st = null;
            found = false;
            int geomType = 0;
            try {
                try {
                    st = con.createStatement();
                    String sql = this.getLimitSQL(100);
                    res = st.executeQuery(sql);
                    while (res.next() && !found) {
                        Feature feat = this.readFeature(res);
                        geomType = this.schema.buildGeometryType(feat.getGeometry());
                        if (geomType == 0) continue;
                        found = true;
                    }
                    if (found) {
                        this.schema.setGeometryType(geomType);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    this.closeChannel(res, st);
                    break block7;
                }
            }
            catch (Throwable throwable) {
                this.closeChannel(res, st);
                throw throwable;
            }
            this.closeChannel(res, st);
        }
        return found;
    }

    protected abstract void buildGeometrySRID(Connection var1);

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        ResultSet res = null;
        Statement st = null;
        Connection con = null;
        ArrayList<Feature> features = new ArrayList<Feature>();
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                st = con.createStatement();
                String sql = this.getLimitSQL(n);
                res = st.executeQuery(sql);
                while (res.next()) {
                    features.add(this.getUpdatedFeature(this.readFeature(res)));
                }
                this.closeChannel(res, st);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeChannel(res, st);
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
        return features;
    }

    public List<String> getSchemas() throws Exception {
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            ArrayList<String> schemas = new ArrayList<String>();
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet res = dmd.getSchemas();
            while (res.next()) {
                String schemaName = res.getString(1);
                if (!CollectionUtils.isNotEmpty(this.getAllGeometriesTables(schemaName))) continue;
                schemas.add(res.getString(1));
            }
            res.close();
            ArrayList<String> arrayList = schemas;
            return arrayList;
        }
        finally {
            if (con != null) {
                con.close();
            }
        }
    }

    protected void buildFeatureSchema(Connection con) throws Exception {
        int COLUMN_NAME = 4;
        this.schema = new FeatureSchema();
        DatabaseMetaData dmd = con.getMetaData();
        dmd.getDatabaseProductVersion();
        ResultSet rs = dmd.getColumns(null, this.getDataBaseSchema(), this.tableName, "%");
        while (rs.next()) {
            AttributeType tipo = this.buildAttributeType(rs);
            String columnName = rs.getString(4);
            this.schema.addAttribute(columnName, tipo);
        }
        rs.close();
        if (this.schema.getAttributeCount() == 0) {
            throw new Exception(I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.the-table-or-view-{0}-does-not-exist", new Object[]{this.tableName}));
        }
        if (this.pkName == null) {
            this.pkName = this.getPrimaryOrUniqueKey(con);
            if (this.pkName != null) {
                this.schema.getAttribute(this.pkName).setPrimaryKey(true);
            } else {
                LOGGER.warn((Object)I18N.getMessage(AbstractJDBCDataSource.class, "no-key-for-table-{0}-was-found", new Object[]{this.tableName}));
            }
        } else {
            this.schema.getAttribute(this.pkName).setPrimaryKey(true);
        }
        if (this.geomColName != null && this.schema.hasAttribute(this.geomColName)) {
            this.schema.setGeometryIndex(this.schema.getAttributeIndex(this.geomColName));
        }
        this.resetLabels();
    }

    protected String getPrimaryOrUniqueKey(Connection con) {
        HashSet<String> resultado = new HashSet<String>();
        try {
            DatabaseMetaData dmd = con.getMetaData();
            String primaryKeyName = "";
            ResultSet resPK = dmd.getPrimaryKeys(null, this.getDataBaseSchema(), this.tableName);
            while (resPK.next()) {
                primaryKeyName = resPK.getString(4);
                resultado.add(primaryKeyName);
            }
            resPK.close();
            resPK = dmd.getIndexInfo(null, this.getDataBaseSchema(), this.tableName, true, false);
            while (resPK.next()) {
                primaryKeyName = resPK.getString(9);
                resultado.add(primaryKeyName);
            }
            resPK.close();
        }
        catch (SQLException e) {
            LOGGER.error((Object)I18N.getString(AbstractJDBCDataSource.class, "error-retrieving-metadata"), (Throwable)e);
        }
        if (resultado.size() > 0) {
            return (String)resultado.iterator().next();
        }
        return null;
    }

    public List<String> getAllGeometriesTables(String schema) throws Exception {
        ArrayList<String> resultado = new ArrayList<String>();
        Connection con = null;
        try {
            this.dataBaseSchema = schema;
            con = DataBaseConnectionFactory.getConnection(this);
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rsBD = dmd.getTables(this.dataBase, schema, null, new String[]{"TABLE", "VIEW"});
            ArrayList<String> tablas = new ArrayList<String>();
            while (rsBD.next()) {
                String table = rsBD.getString(TABLE_NAME_KEY);
                if (!this.allowTable(table)) continue;
                tablas.add(table);
            }
            rsBD.close();
            for (String tableName : tablas) {
                ResultSet rs = dmd.getColumns(null, schema, tableName, "%");
                boolean isGeometry = false;
                while (rs.next() && !isGeometry) {
                    AttributeType tipo = this.buildAttributeType(rs);
                    if (tipo == null || !tipo.equals(AttributeType.GEOMETRY)) continue;
                    isGeometry = true;
                }
                rs.close();
                if (!isGeometry) continue;
                resultado.add(tableName);
            }
        }
        catch (Throwable throwable) {
            this.closeConnection(con);
            throw throwable;
        }
        this.closeConnection(con);
        return resultado;
    }

    public List<Field> getCandidateFields(String schema, String table) throws SQLException, IOException {
        HashSet<Field> resultado = new HashSet<Field>();
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet resPK = dmd.getPrimaryKeys(null, schema, table);
            while (resPK.next()) {
                resultado.add(new Field(resPK.getString(4), Field.FieldType.PK_FIELD));
            }
            resPK.close();
            try {
                resPK = dmd.getIndexInfo(null, schema, table, true, false);
                while (resPK.next()) {
                    if (resPK.getString(9) == null) continue;
                    resultado.add(new Field(resPK.getString(9), Field.FieldType.UNIQUE_FIELD));
                }
                resPK.close();
            }
            catch (SQLException sqlEx) {
                LOGGER.warn((Object)I18N.getMessage(AbstractJDBCDataSource.class, "it-is-not-possible-to-obtain-the-fields-with-unique-restrictions-{0}", new Object[]{StringUtils.trimToEmpty((String)sqlEx.getMessage())}));
                LOGGER.debug((Object)sqlEx);
            }
            ResultSet rs = dmd.getColumns(null, schema, table, "%");
            while (rs.next()) {
                AttributeType tipo = this.buildAttributeType(rs);
                if (tipo != null && !tipo.equals(AttributeType.GEOMETRY)) {
                    resultado.add(new Field(rs.getString(4), Field.FieldType.REGULAR_FIELD));
                    continue;
                }
                if (tipo == null) continue;
                resultado.add(new Field(rs.getString(4), Field.FieldType.GEOMETRIC_FIELD));
            }
            rs.close();
        }
        finally {
            this.closeConnection(con);
        }
        ArrayList<Field> retList = new ArrayList<Field>();
        retList.addAll(resultado);
        Collections.sort(retList);
        return retList;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void initialize(FeatureSchema schema, Envelope envelope, int srid, boolean addRestrictions, boolean is3d) throws SQLException {
        this.initialize(schema, envelope, srid, addRestrictions, is3d, false);
    }

    public void initialize(FeatureSchema schema, Envelope envelope, int srid, boolean addRestrictions, boolean is3d, boolean ignoreUnknownGeometryType) throws SQLException {
        this.schema = schema;
        this.srid = srid;
        Connection con = null;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                this.geomColName = schema.getAttributeName(schema.getGeometryIndex());
                this.pkName = schema.getPrimaryKeyName();
                this.is3d = is3d;
                boolean tableCreated = this.createDataStore(envelope, this.geomColName, srid, addRestrictions, is3d);
                this.updateFeatures = new HashSet();
                this.newFeatures = new HashSet();
                this.deletedFeatures = new HashSet();
                this.lockedFeatures = new BitSet();
                this.inMemory = true;
                if (tableCreated || this.size() == 0) {
                    this.schema.setGeometryType(schema.getGeometryType());
                } else if (!this.buildGeometryType(con) && !ignoreUnknownGeometryType) {
                    throw new SQLException(I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.Geometry-type-can-not-be-determined"));
                }
                this.iniciado = true;
            }
            catch (Exception e) {
                LOGGER.error((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.conection-refused"), (Throwable)e);
                throw new SQLException(e.getMessage());
            }
        }
        finally {
            this.closeConnection(con);
        }
    }

    public void initialize() throws SQLException, Exception {
        this.initialize(false);
    }

    public Connection getDirectConnection() throws SQLException {
        return DataBaseConnectionFactory.getConnection(this);
    }

    public void initialize(boolean ignoreGeometryType) throws SQLException {
        this.envelope = null;
        Connection con = null;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                this.buildFeatureSchema(con);
                this.geomColName = this.schema.getAttributeName(this.schema.getGeometryIndex());
                if (!this.buildGeometryType(con) && !ignoreGeometryType) {
                    throw new UnknownGeometryTypeException();
                }
                this.buildGeometrySRID(con);
                this.pkName = this.schema.getPrimaryKeyName();
                this.updateFeatures = new HashSet();
                this.newFeatures = new HashSet();
                this.deletedFeatures = new HashSet();
                this.lockedFeatures = new BitSet();
                this.inMemory = true;
                this.iniciado = true;
            }
            catch (Exception e) {
                if (e instanceof UnknownGeometryTypeException) {
                    throw (UnknownGeometryTypeException)e;
                }
                LOGGER.error((Object)"", (Throwable)e);
                throw new SQLException(e.getMessage());
            }
        }
        finally {
            this.closeConnection(con);
        }
    }

    public String toString() {
        return String.valueOf(this.hostName) + "," + this.port + "," + this.dataBase + "," + this.dataBaseSchema + "," + this.userName;
    }

    public void setDataBase(String dataBase) {
        this.dataBase = dataBase;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        String decryptedPassword = "";
        try {
            CryptManager manager = CryptManagerFactory.getManager("Password based encryption");
            decryptedPassword = manager.decrypt(encryptedPassword);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.password = decryptedPassword;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        ArrayList<Object> keys;
        block9: {
            keys = new ArrayList<Object>();
            String sql = "SELECT " + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + " FROM " + this.getTables();
            String sqlWhere = "";
            if (this.layerFilter != null) {
                sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
            }
            if (this.schema.isVersionable()) {
                sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
            }
            sql = String.valueOf(sql) + sqlWhere;
            sql = String.valueOf(sql) + " ORDER BY " + this.getFullTableName() + "." + this.escapeAttributeName(this.schema.getPrimaryKeyName());
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
                    break block9;
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

    public void beginTransaction() throws SQLException {
        LOGGER.info((Object)"/*****************BEGIN TRANSACTION********************/");
        this.connectionTransaction = DataBaseConnectionFactory.getConnection(this);
        this.connectionTransaction.setAutoCommit(false);
        this.connectionTransaction.setTransactionIsolation(2);
    }

    public void endTransaction() throws Exception {
        LOGGER.info((Object)"/*****************END TRANSACTION********************/");
        this.inMemory = false;
        try {
            try {
                if (this.newFeatures.size() > 0) {
                    LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.saving-{0}-new-features", new Object[]{new Integer(this.newFeatures.size())}));
                    this.addAll(this.newFeatures);
                    this.envelope = new Envelope(this.expandEnvelope(this.envelope, this.getNewFeatures()));
                    this.newFeatures.clear();
                }
                if (this.deletedFeatures.size() > 0) {
                    ArrayList<Feature> featuresFilter = new ArrayList<Feature>();
                    for (Feature element : this.deletedFeatures) {
                        if (element.isUnsaved()) continue;
                        featuresFilter.add(element);
                    }
                    LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.deleting-{0}-features", new Object[]{featuresFilter.size()}));
                    this.removeAll(featuresFilter);
                    this.deletedFeatures.clear();
                }
                if (this.updateFeatures.size() > 0) {
                    LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.updating-{0}-features", new Object[]{this.updateFeatures.size()}));
                    this.updateAll(this.updateFeatures);
                    this.envelope = new Envelope(this.expandEnvelope(this.envelope, this.getUpdatedFeatures()));
                    this.updateFeatures.clear();
                }
                this.connectionTransaction.commit();
                this.size = -1;
            }
            catch (SQLException e) {
                this.rollback(false);
                throw e;
            }
        }
        finally {
            this.clearTransaction();
            this.inMemory = true;
        }
    }

    public abstract boolean lockFeatures(Collection<Feature> var1) throws SQLException;

    public void clearTransaction() throws SQLException {
        this.lockedFeatures.clear();
        if (this.connectionTransaction != null) {
            this.connectionTransaction.close();
            this.connectionTransaction = null;
            LOGGER.info((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.transaction-cleaned"));
        }
    }

    @Override
    public abstract void updateAll(Collection<Feature> var1) throws Exception;

    protected abstract String getSQLForUpdate(Feature var1);

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
            if (CollectionUtils.isNotEmpty((Collection)this.updateFeatures)) {
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.updating-{0}-features", new Object[]{this.updateFeatures.size()}));
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
    public Feature getByPrimaryKey(Object key) {
        if (key instanceof Feature) {
            return (Feature)key;
        }
        List<Feature> features = this.getByPrimaryKey(new Object[]{key});
        if (CollectionUtils.isEmpty(features)) {
            return null;
        }
        return features.get(0);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys) {
        return this.getByPrimaryKey(keys, false);
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
        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement();
                resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    Feature readFeature = this.readFeature(resultSet);
                    if (!ignoredUpdate) {
                        featList.add(this.getUpdatedFeature(readFeature));
                        continue;
                    }
                    featList.add(readFeature);
                }
                this.closeChannel(resultSet, statement);
            }
            catch (Exception ex) {
                this.closeChannel(resultSet, statement);
                LOGGER.error((Object)"", (Throwable)ex);
                this.closeConnection(connection);
                return null;
            }
        }
        finally {
            this.closeConnection(connection);
        }
        return featList;
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        TreeSet<SortedAttribute> sort;
        boolean isString;
        block11: {
            isString = this.schema.getAttribute(column).getType().toJavaClass().equals(String.class);
            sort = new TreeSet<SortedAttribute>();
            Filter filter = this.getFilterByPrimaryKey(values);
            ArrayList<String> orderByList = new ArrayList<String>();
            orderByList.add(column);
            String sql = this.getSQLOnlyGeometryForQuery(null, filter, orderByList, ascending, this.getAllLabels());
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                try {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    statement = connection.createStatement(1003, 1007);
                    resultSet = statement.executeQuery(sql);
                    while (resultSet.next()) {
                        Feature feat = this.getUpdatedFeature(this.readFeature(resultSet));
                        sort.add(new SortedAttribute(feat.getAttribute(column), feat.getPrimaryKey(), ascending, isString));
                    }
                    this.closeChannel(resultSet, statement);
                }
                catch (Exception sqlException) {
                    LOGGER.error((Object)"", (Throwable)sqlException);
                    this.closeConnection(connection);
                    break block11;
                }
            }
            catch (Throwable throwable) {
                this.closeConnection(connection);
                throw throwable;
            }
            this.closeConnection(connection);
        }
        block5: for (Feature element : this.newFeatures) {
            if (values != null) {
                int i = 0;
                while (i < values.length) {
                    if (values[i] == element) {
                        Object value = element.getAttribute(column);
                        sort.add(new SortedAttribute(value, element, ascending, isString));
                        continue block5;
                    }
                    ++i;
                }
                continue;
            }
            Object value = element.getAttribute(column);
            sort.add(new SortedAttribute(value, element, ascending, isString));
        }
        ArrayList<Object> result = new ArrayList<Object>();
        for (SortedAttribute element : sort) {
            result.add(element.getRecordNumber());
        }
        return result;
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.getDistintsValues(field, Integer.MAX_VALUE);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.getDistintsValues(expr, Integer.MAX_VALUE);
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Attribute attr = this.schema.getAttribute(field);
        if (attr instanceof AttributeCalculate) {
            return ((AttributeCalculate)attr).getDistintsValues(((AttributeCalculate)attr).getRelationFieldName(), limit);
        }
        String sql = "SELECT DISTINCT " + this.getFullTableName() + "." + this.escapeAttributeName(field) + " FROM " + this.getTables();
        String sqlWhere = "";
        if (this.layerFilter != null) {
            sqlWhere = String.valueOf(sqlWhere) + " WHERE " + this.getSQLExpression(this.layerFilter);
        }
        if (this.schema.isVersionable()) {
            sqlWhere = sqlWhere.isEmpty() ? String.valueOf(sqlWhere) + " WHERE " + this.schema.getEndDateFilter(this) : String.valueOf(sqlWhere) + " AND " + this.schema.getEndDateFilter(this);
        }
        sql = String.valueOf(sql) + sqlWhere;
        sql = String.valueOf(sql) + " ORDER BY " + this.escapeAttributeName(field);
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        int cuenta = 0;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement(1003, 1007);
                resultSet = statement.executeQuery(sql);
                while (resultSet.next() && cuenta < limit) {
                    Object value = resultSet.getObject(field);
                    if (value == null) continue;
                    values.add(value);
                    ++cuenta;
                }
                this.closeChannel(resultSet, statement);
            }
            catch (Exception sqlException) {
                this.closeChannel(resultSet, statement);
                LOGGER.error((Object)"", (Throwable)sqlException);
                this.closeConnection(connection);
            }
        }
        finally {
            this.closeConnection(connection);
        }
        return values;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        TreeSet<Object> values = new TreeSet<Object>();
        FeatureIterator itFeatures = null;
        try {
            try {
                itFeatures = this.getFeaturesIterator();
                while (itFeatures.hasNext()) {
                    if (values.size() >= limit) {
                        return values;
                    }
                    Object value = expr.getValue(itFeatures.next());
                    if (value == null) continue;
                    values.add(value);
                }
                return values;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itFeatures == null) return values;
                itFeatures.close();
                return values;
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
    }

    @Override
    public Object getFieldValue(String field, String fieldKey, Object value) {
        if (!this.schema.hasAttribute(field)) {
            return null;
        }
        Object result = null;
        Attribute attr = this.schema.getAttribute(field);
        if (attr instanceof AttributeCalculate) {
            return ((AttributeCalculate)attr).getRelation().getFieldValue(field, value);
        }
        String sqlFilter = String.valueOf(fieldKey) + " = " + value;
        Filter filter = null;
        try {
            filter = (Filter)ExpressionBuilder.parse(sqlFilter);
        }
        catch (ParseException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
        FeatureIterator it = null;
        try {
            try {
                it = this.queryGeometryIterator(null, filter, this.getAllLabels());
                while (it.hasNext()) {
                    Feature feat = it.next();
                    result = feat.getAttribute(field);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return result;
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        HashMap<Object, RelationAttribute> values = new HashMap<Object, RelationAttribute>();
        if (!this.schema.hasAttribute(fieldKey)) {
            return values;
        }
        int i = 0;
        while (i < fields.length) {
            String field = fields[i];
            if (!this.schema.hasAttribute(field)) {
                return values;
            }
            ++i;
        }
        ArrayList<AttributeCalculate> attrCalculate = new ArrayList<AttributeCalculate>();
        ArrayList<Attribute> attrNoCalculate = new ArrayList<Attribute>();
        int i2 = 0;
        while (i2 < fields.length) {
            String field = fields[i2];
            Attribute attr = this.schema.getAttribute(field);
            if (attr instanceof AttributeCalculate) {
                attrCalculate.add((AttributeCalculate)attr);
            } else {
                attrNoCalculate.add(attr);
            }
            ++i2;
        }
        FeatureIterator it = null;
        try {
            try {
                it = this.getFeaturesIterator();
                while (it.hasNext()) {
                    Feature feat = it.next();
                    RelationAttribute ra = new RelationAttribute();
                    int i3 = 0;
                    while (i3 < attrNoCalculate.size()) {
                        ra.setFieldValue(((Attribute)attrNoCalculate.get(i3)).getName(), feat.getAttribute(((Attribute)attrNoCalculate.get(i3)).getName()));
                        ++i3;
                    }
                    values.put(feat.getAttribute(fieldKey), ra);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return values;
    }

    /*
     * Exception decompiling
     */
    @Override
    public List<Feature> getFeatures() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public List<Feature> getBySQL(String whereSql) {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        Filter filter = null;
        FeatureIterator it = null;
        try {
            try {
                filter = (Filter)ExpressionBuilder.parse(this.schema, whereSql);
                it = this.queryGeometryIterator(null, filter, null, this.getAllLabels());
                while (it.hasNext()) {
                    resultado.add(it.next());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return resultado;
    }

    public List<Feature> getBySQLWithOutFilter(String whereSql) {
        ArrayList<Feature> resultado = new ArrayList<Feature>();
        String consultaSQL = String.valueOf(this.getRaizConsultaTipo()) + " WHERE " + whereSql;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement(1003, 1007);
                resultSet = statement.executeQuery(consultaSQL);
                while (resultSet.next()) {
                    Feature feature = this.readFeature(resultSet);
                    resultado.add(feature);
                }
                this.closeChannel(resultSet, statement);
            }
            catch (Exception sqlException) {
                LOGGER.error((Object)"", (Throwable)sqlException);
                this.closeConnection(connection);
            }
        }
        finally {
            this.closeConnection(connection);
        }
        return resultado;
    }

    public boolean isNewTable() {
        return this.newTable;
    }

    public abstract int toSQLType(AttributeType var1);

    @Override
    public void update(Feature feature) throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        this.updateAll(features);
    }

    public abstract boolean equals(Object var1);

    public int hashCode() {
        return 17 + this.getHostName().hashCode() + this.getPort() + this.getDataBase().hashCode() + this.getUserName().hashCode() + this.getPassword().hashCode();
    }

    protected boolean isAttributeForRead(int i) {
        Attribute attr = this.schema.getAttribute(i);
        if (attr instanceof AttributeCalculate) {
            AttributeCalculate attrCalculate = (AttributeCalculate)attr;
            Relation<?> rel = attrCalculate.getRelation();
            if (rel instanceof TableRelation) {
                TableDBRecordDataSource tableDS;
                TableRelation tableRel = (TableRelation)rel;
                if (!(!(tableRel.getTable().getDataSource() instanceof TableDBRecordDataSource) || this.checkHost((tableDS = (TableDBRecordDataSource)tableRel.getTable().getDataSource()).getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword()))) {
                    return false;
                }
            } else {
                LayerRelation layerRel = (LayerRelation)rel;
                Layer layer = layerRel.getTargetLayer();
                if (layer.isDataBaseDataSource()) {
                    AbstractJDBCDataSource layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor();
                    if (!layerDS.equals(this)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public List<String> getAllLabels() {
        if (this.labels == null) {
            this.labels = new ArrayList<String>();
            int i = 0;
            while (i < this.schema.getAttributeCount()) {
                if (this.isAttributeForRead(i) && i != this.schema.getGeometryIndex()) {
                    this.labels.add(this.schema.getAttributeName(i));
                }
                ++i;
            }
        }
        return this.labels;
    }

    protected Filter getViewBoxFilter(Envelope envelope) {
        GeometryFilterImpl filter = null;
        if (envelope != null) {
            try {
                AttributeExpressionImpl2 attribute = new AttributeExpressionImpl2(this.getSchema().getAttributeName(this.getSchema().getGeometryIndex()));
                LiteralExpressionImpl geometry = new LiteralExpressionImpl(EnvelopeUtil.toGeometry(envelope));
                GeometryFilterImpl geomFilter = new GeometryFilterImpl(4);
                geomFilter.addLeftGeometry(attribute);
                geomFilter.addRightGeometry(geometry);
                filter = geomFilter;
            }
            catch (IllegalFilterException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return filter;
    }

    public int getSrid() {
        return this.srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public String getTableNameOfRelation(Relation<?> rel) {
        return this.getTableNameOfRelation(rel, true);
    }

    protected abstract boolean isCompatibleRelation(Relation<?> var1);

    public String getTableNameOfRelation(Relation<?> rel, boolean fullTableName) {
        String tableName = "";
        if (rel instanceof TableRelation) {
            Table table = ((TableRelation)rel).getTable();
            if (table.getDataSource() instanceof TableDBRecordDataSource) {
                TableDBRecordDataSource tableDS = (TableDBRecordDataSource)table.getDataSource();
                if (!(this.checkHost(tableDS.getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBase()) && tableDS.getUser().equals(this.getUserName()) && tableDS.getPassword().equals(this.getPassword()))) {
                    return tableName;
                }
                tableName = fullTableName ? ((TableDBRecordDataSource)table.getDataSource()).getFullTableName() : ((TableDBRecordDataSource)table.getDataSource()).getTableName();
            }
        } else {
            LayerRelation layerRel = (LayerRelation)rel;
            Layer layer = layerRel.getTargetLayer();
            if (layer.isDataBaseDataSource()) {
                AbstractJDBCDataSource layerDS = (AbstractJDBCDataSource)((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor();
                if (fullTableName) {
                    tableName = layerDS.getFullTableName();
                } else {
                    layerDS.getTableName();
                }
            }
        }
        return tableName;
    }

    protected boolean checkHost(String otherHost) {
        return otherHost.equalsIgnoreCase("localhost") && this.getHostName().equals("127.0.0.1") || otherHost.equals("127.0.0.1") && this.getHostName().equalsIgnoreCase("localhost") || otherHost.equals(this.getHostName());
    }

    public abstract String processSQLExpression(String var1, String var2, String var3, String var4);

    public abstract String processSQLExpressionSinComillas(String var1, String var2, String var3, String var4);

    protected void updateHistoryFeature(Feature feat, List<Feature> oldFeatures) {
        List<Feature> candidates = this.getByPrimaryKey(new Object[]{feat.getPrimaryKey()}, true);
        if (CollectionUtils.isEmpty(candidates)) {
            LOGGER.warn((Object)I18N.getMessage(AbstractJDBCDataSource.class, "element-with-key-{0}-was-not-found", new Object[]{feat.getPrimaryKey()}));
            return;
        }
        Feature oldFeature = this.getByPrimaryKey(new Object[]{feat.getPrimaryKey()}, true).get(0);
        Timestamp fechaOp = new Timestamp(System.currentTimeMillis());
        Feature copyOldFeature = oldFeature.clone(true);
        copyOldFeature.setAttribute(this.schema.getFieldEndDate(), (Object)fechaOp);
        copyOldFeature.setAttribute(this.schema.getHistoryField(), feat.getPrimaryKey());
        copyOldFeature.setAttribute(this.schema.getPrimaryKeyName(), null);
        oldFeatures.add(copyOldFeature);
        feat.setAttribute(this.schema.getFieldStartDate(), (Object)fechaOp);
    }

    protected String getOrderByExpression(List<String> orderByFileds) {
        String orderByExpression = "";
        ArrayList<Attribute> attrNoCalc = new ArrayList<Attribute>();
        ArrayList<AttributeCalculate> attrCal = new ArrayList<AttributeCalculate>();
        int i = 0;
        while (i < orderByFileds.size()) {
            Attribute attr = this.schema.getAttribute(orderByFileds.get(i));
            if (attr.isCalculated()) {
                attrCal.add((AttributeCalculate)attr);
            } else {
                attrNoCalc.add(attr);
            }
            ++i;
        }
        for (Attribute attr : attrNoCalc) {
            orderByExpression = String.valueOf(orderByExpression) + this.getFullTableName() + "." + this.escapeAttributeName(attr.getName()) + ",";
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
            orderByExpression = String.valueOf(orderByExpression) + tableName + "." + this.escapeAttributeName(attrCalculate.getRelationFieldName()) + ",";
        }
        if (orderByExpression.length() > 0) {
            orderByExpression = orderByExpression.substring(0, orderByExpression.length() - 1);
        }
        return orderByExpression;
    }

    public static AbstractJDBCDataSource buildJDBCDataSourceFromProperties(Map properties) throws Exception {
        AbstractJDBCDataSource trds = null;
        String datasourceClass = (String)properties.get(DATASOURCE_CLASS_KEY);
        if (datasourceClass != null) {
            String user = (String)properties.get(USER_KEY);
            String passw = (String)properties.get(PASSWORD_KEY);
            String tableName = (String)properties.get(TABLE_NAME_KEY);
            String databaseName = (String)properties.get(DATABASE_KEY);
            String schemaName = (String)properties.get(SCHEMA_KEY);
            if (databaseName == null) {
                databaseName = schemaName;
                schemaName = "";
            }
            String hostName = (String)properties.get(HOST_KEY);
            Integer port = (Integer)properties.get(PORT_KEY);
            String pkColumnName = (String)properties.get(PRIMARY_KEY_COLUMN_NAME);
            if (datasourceClass.equals(MySQLDataSource.class.getName())) {
                trds = new MySQLDataSource(hostName, port, databaseName, user, passw);
                trds.setTableName(tableName);
                trds.setPkName(pkColumnName);
                ((MySQLDataSource)trds).initialize(true);
            } else if (datasourceClass.equals(OracleSpatialDataSource.class.getName())) {
                trds = new OracleSpatialDataSource(hostName, port, databaseName, user, passw);
                trds.setDataBaseSchema(schemaName);
                trds.setTableName(tableName);
                trds.setPkName(pkColumnName);
                trds.initialize(true);
            } else if (datasourceClass.equals(PostGisDataSource.class.getName())) {
                trds = new PostGisDataSource(hostName, port, databaseName, user, passw);
                ((PostGisDataSource)trds).setDataBaseSchema(schemaName);
                ((PostGisDataSource)trds).setTableName(tableName);
                ((PostGisDataSource)trds).setPkName(pkColumnName);
                ((PostGisDataSource)trds).initialize(true);
            }
        }
        return trds;
    }

    public abstract void createTemporalFields(String var1, String var2, String var3) throws SQLException;

    public String getDataBaseSchema() {
        return this.dataBaseSchema;
    }

    public void setDataBaseSchema(String dataBaseSchemaName) {
        if (StringUtils.isEmpty((String)dataBaseSchemaName)) {
            this.dataBaseSchema = null;
        }
        this.dataBaseSchema = dataBaseSchemaName;
    }

    public abstract String getFullTableName();

    public void resetLabels() {
        this.labels = null;
    }

    protected boolean checkGeometryType(Geometry geom) {
        Class<?> schemaGeometry = FeatureSchemaUtils.getGeometryClass(this.schema);
        return geom.getClass().equals(schemaGeometry);
    }

    protected boolean checkZ(Geometry geometry) {
        ZCoordinateCountFilter filter = new ZCoordinateCountFilter();
        geometry.apply((CoordinateFilter)filter);
        return !this.is3d() && filter.getCount() == 0;
    }

    protected Collection<Feature> getCorrectGeometries(Collection<Feature> features) throws Exception {
        ArrayList<Feature> correctFeatures = new ArrayList<Feature>();
        for (Feature element : features) {
            Geometry geom = element.getGeometry();
            if (!this.checkGeometryType(geom)) {
                geom = GeometryUtils.convertToGoodGeometry(this.schema, geom);
            }
            if (!this.checkZ(geom)) {
                geom = GeometryUtils.applyZFilter(geom, this.is3d());
            }
            element.setGeometry(geom);
            correctFeatures.add(element);
        }
        return correctFeatures;
    }

    public void removeDataStore() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement();
                String sqlForDropTable = this.getSQLForDropTable();
                statement.execute(sqlForDropTable);
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

    protected abstract String getSQLForDropTable();

    @Override
    public FeatureIterator queryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields) {
        return this.queryGeometryIterator(rectangle, filtro, orderByFields, this.getAllLabels());
    }

    @Override
    public abstract Object clone();

    protected abstract String getSQLExpression(Filter var1);

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        int majorVersion = this.getDatabaseMajorVersion();
        int minorVersion = this.getDatabaseMinorVersion();
        StringBuffer consultaSQL = new StringBuffer();
        int numberOfFields = 0;
        if (resultStatPairs != null) {
            resultStatPairs.clear();
        }
        consultaSQL.append("SELECT ");
        if (!CollectionUtils.isEmpty(groupByFields)) {
            for (String queriedField : groupByFields) {
                consultaSQL.append(String.valueOf(this.escapeAttributeName(queriedField)) + ", ");
                if (resultStatPairs != null) {
                    resultStatPairs.add(new CalculateStatsDialog.StatPair(queriedField, null));
                }
                ++numberOfFields;
            }
        }
        for (String currentField : operatorsByFieldMap.keySet()) {
            Set<String> operatorsForCurrentField = operatorsByFieldMap.get(currentField);
            for (String currentOp : operatorsForCurrentField) {
                String sqlForAggregateFunction = this.getSQLForAggregateFunction(currentField, currentOp, majorVersion, minorVersion);
                if (sqlForAggregateFunction == null) continue;
                consultaSQL.append(sqlForAggregateFunction);
                consultaSQL.append(", ");
                if (resultStatPairs != null) {
                    resultStatPairs.add(new CalculateStatsDialog.StatPair(currentField, currentOp));
                }
                ++numberOfFields;
            }
        }
        if (numberOfFields <= 0) {
            return new ArrayList<Object[]>();
        }
        consultaSQL.delete(consultaSQL.length() - 2, consultaSQL.length());
        consultaSQL.append(" FROM " + this.getTables());
        consultaSQL.append(this.getSQLForWhereByRanges(keys));
        consultaSQL.append(this.getSQLForGroupByClause(groupByFields));
        consultaSQL.append(this.getSQLForOrderByClause(groupByFields));
        return this.executeNonFeatureQuery(consultaSQL.toString(), numberOfFields);
    }

    protected String getSQLForAggregateFunction(String field, String operatorName, int majorVersion, int minorVersion) {
        String functionName = AGGREGATE_FUNCTIONS_MAP.get(operatorName);
        if (functionName == null) {
            return null;
        }
        return String.valueOf(functionName) + "(" + this.escapeAttributeName(field) + ")";
    }

    protected String getSQLForGroupByClause(List<String> groupByFields) {
        StringBuffer groupByClause = new StringBuffer();
        if (!CollectionUtils.isEmpty(groupByFields)) {
            groupByClause.append(" GROUP BY ");
            for (String currentField : groupByFields) {
                groupByClause.append(String.valueOf(this.escapeAttributeName(currentField)) + ", ");
            }
            groupByClause.delete(groupByClause.length() - 2, groupByClause.length());
        }
        return groupByClause.toString();
    }

    protected String getSQLForOrderByClause(List<String> orderByFields) {
        StringBuffer orderByClause = new StringBuffer();
        if (!CollectionUtils.isEmpty(orderByFields)) {
            orderByClause.append(" ORDER BY ");
            for (String currentField : orderByFields) {
                orderByClause.append(String.valueOf(this.escapeAttributeName(currentField)) + ", ");
            }
            orderByClause.delete(orderByClause.length() - 2, orderByClause.length());
        }
        return orderByClause.toString();
    }

    public int getDatabaseMajorVersion() {
        Connection con = null;
        int majorVersion = -1;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                DatabaseMetaData dbmd = con.getMetaData();
                majorVersion = dbmd.getDatabaseMajorVersion();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
        return majorVersion;
    }

    public int getDatabaseMinorVersion() {
        Connection con = null;
        int minorVersion = -1;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                DatabaseMetaData dbmd = con.getMetaData();
                minorVersion = dbmd.getDatabaseMinorVersion();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
        return minorVersion;
    }

    protected String getSQLForWhereByRanges(Object[] keys) {
        StringBuffer whereByRangeClause = new StringBuffer();
        if (!ArrayUtils.isEmpty((Object[])keys)) {
            whereByRangeClause.append(" WHERE ");
            if (AttributeType.isNumeric(this.schema.getPrimaryKey().getType())) {
                List<long[]> ranges = this.getRanges(keys);
                for (long[] range : ranges) {
                    String currentRange = "(" + this.escapeAttributeName(this.pkName) + " >= " + range[0] + " AND " + this.escapeAttributeName(this.pkName) + " <= " + range[1] + ")";
                    whereByRangeClause.append(currentRange);
                    whereByRangeClause.append(" OR ");
                }
            } else {
                int i = 0;
                while (i < keys.length) {
                    whereByRangeClause.append(String.valueOf(this.escapeAttributeName(this.pkName)) + " = '" + keys[i] + "'");
                    whereByRangeClause.append(" OR ");
                    ++i;
                }
            }
            whereByRangeClause.delete(whereByRangeClause.length() - 3, whereByRangeClause.length());
        }
        return whereByRangeClause.toString();
    }

    protected boolean checkIfFilterIsCompatible(Filter filter) {
        Set<String> filterLabels = FilterUtil.getLabelsFromFilter(filter, this.schema);
        boolean check = true;
        Iterator<String> iter = filterLabels.iterator();
        while (iter.hasNext() && check) {
            String campo = iter.next();
            Attribute attr = this.schema.getAttribute(campo);
            if (attr == null || !attr.isCalculated()) continue;
            Relation<?> rel = ((AttributeCalculate)attr).getRelation();
            check &= this.isCompatibleRelation(rel);
        }
        return check;
    }

    protected boolean allowTable(String tableName) {
        return true;
    }

    public IDBKeyResolver getKeyResolver() {
        return this.keyResolver;
    }

    public void setKeyResolver(IDBKeyResolver keyResolver) {
        this.keyResolver = keyResolver;
    }

    public abstract String getID();

    public abstract int getDefaultPort();

    protected String escapeAttributeName(String attrName) {
        return attrName;
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, List<String> labels) {
        return this.queryGeometryIterator(rectangle, null, null, true, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> labels) {
        return this.queryGeometryIterator(rectangle, filter, null, true, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, List<String> labels) {
        return this.queryGeometryIterator(rectangle, filter, orderByFields, true, labels);
    }

    public FeatureIterator getGeometriesIterator() {
        return this.queryGeometryIterator(null, null, null);
    }

    @Override
    public void rollback(boolean deleteAllChanges) {
        super.rollback(deleteAllChanges);
        if (this.connectionTransaction != null) {
            try {
                if (this.connectionTransaction != null) {
                    this.connectionTransaction.rollback();
                }
            }
            catch (SQLException e) {
                LOGGER.error((Object)e);
            }
        }
    }

    public String getSQLForAddColumn(String name, AttributeType type) {
        StringBuffer sql = new StringBuffer();
        sql.append("ALTER TABLE " + this.getFullTableName() + " ADD COLUMN " + this.escapeAttributeName(name) + " " + this.convertAttributeTypeToDBType(type));
        return sql.toString();
    }

    public String getSQLForAlterColumnType(String name, AttributeType type) {
        StringBuffer sql = new StringBuffer();
        sql.append("ALTER TABLE " + this.getFullTableName() + " ALTER COLUMN " + this.escapeAttributeName(name) + " TYPE " + this.convertAttributeTypeToDBType(type));
        return sql.toString();
    }

    public String getSQLForAlterColumnName(String oldName, String newName, AttributeType type) {
        StringBuffer sql = new StringBuffer();
        sql.append("ALTER TABLE " + this.getFullTableName() + " RENAME COLUMN " + this.escapeAttributeName(oldName) + " TO " + this.escapeAttributeName(newName));
        return sql.toString();
    }

    public String getSQLForDropColumn(String name) {
        StringBuffer sql = new StringBuffer();
        sql.append("ALTER TABLE " + this.getFullTableName() + " DROP COLUMN " + this.escapeAttributeName(name));
        return sql.toString();
    }

    public abstract String convertAttributeTypeToDBType(AttributeType var1);
}

