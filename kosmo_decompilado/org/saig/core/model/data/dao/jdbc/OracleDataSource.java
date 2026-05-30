/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oracle.jdbc.pool.OracleConnectionPoolDataSource
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.jdbc;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.StringUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.ConnectionPoolDataSource;
import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.IPoolableDBDataSource;
import org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Filter;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.SQLEncoderOracle;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.jdbc.iterators.OracleTableIterator;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.jump.lang.I18N;

public class OracleDataSource
extends TableDBRecordDataSource
implements IPoolableDBDataSource {
    public static final Logger LOGGER = Logger.getLogger(OracleDataSource.class);
    public static final String ID = "Oracle";

    public OracleDataSource() {
    }

    public OracleDataSource(String host, int port, String databaseName, String username, String password) {
        super(username, password, databaseName, host, port);
    }

    @Override
    public int toSQLType(AttributeType attrType) {
        return OracleSpatialDataSource.attributeTypeToSQLType.get(attrType);
    }

    @Override
    protected String getLimitSQL(int limit) {
        return String.valueOf(this.getRaizConsultaTipo()) + " where rownum<=" + limit;
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limite) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Connection con = null;
        Statement statement = null;
        String sql = "SELECT DISTINCT " + this.escapeAttributeName(field) + " FROM " + this.getFullTableName() + " WHERE rownum<=" + limite;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                statement = con.createStatement(1003, 1007);
                ResultSet res = statement.executeQuery(sql);
                while (res.next()) {
                    Object value = res.getObject(1);
                    if (value == null) continue;
                    values.add(value);
                }
                res.close();
                statement.close();
            }
            catch (Exception e) {
                this.closeChannel(null, statement);
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
        return values;
    }

    @Override
    protected String getSQLForCreateTable() {
        String sqlCreate = "CREATE TABLE " + this.getFullTableName() + "(";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            sqlCreate = String.valueOf(sqlCreate) + this.escapeAttributeName(this.schema.getAttributeName(i)) + " " + OracleSpatialDataSource.attributeTypeToDBType.get(this.schema.getAttributeType(i));
            if (this.schema.getPrimaryKeyIndex() == i) {
                sqlCreate = String.valueOf(sqlCreate) + " NOT NULL ";
            }
            sqlCreate = String.valueOf(sqlCreate) + ",";
            ++i;
        }
        sqlCreate = String.valueOf(sqlCreate.substring(0, sqlCreate.length() - 1)) + ")";
        return sqlCreate;
    }

    @Override
    protected void buildFeatureSchema(Connection con) throws Exception {
        int COLUMN_NAME = 4;
        this.schema = new FeatureSchema();
        DatabaseMetaData dmd = con.getMetaData();
        String dbSchema = this.dataBaseSchema;
        if (dbSchema != null) {
            dbSchema = dbSchema.toUpperCase();
        }
        ResultSet rs = dmd.getColumns(null, dbSchema, this.tableName.toUpperCase(), "%");
        while (rs.next()) {
            AttributeType tipo = this.buildAttributeType(rs);
            if (tipo == null) {
                tipo = AttributeType.STRING;
            } else if (tipo.equals(AttributeType.GEOMETRY)) continue;
            String columnName = rs.getString(4).toLowerCase();
            this.schema.addAttribute(columnName, tipo);
        }
        rs.close();
        if (this.schema.getAttributeCount() == 0) {
            throw new Exception(I18N.getMessage("org.saig.core.model.data.dao.TableDBRecordDataSource.the-table-or-view-{0}-does-not-exist", new Object[]{this.tableName}));
        }
        if (this.pkName != null) {
            this.schema.getAttribute(this.pkName).setPrimaryKey(true);
        }
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public List<Record> getRecords(String fieldOrdered, Filter filter, boolean ascending) {
        block17: {
            records = new ArrayList<Record>();
            iterator = null;
            sqlOrderBy = null;
            sqlWhere = null;
            try {
                try {
                    if (filter != null || this.schema.isVersionable()) {
                        sqlWhere = "";
                        if (filter != null) {
                            encoder = new SQLEncoderOracle(-1);
                            try {
                                resultado = encoder.encode(filter);
                                resultado = StringUtil.replace(resultado, "\"", "", true);
                                resultado = resultado.replaceFirst("WHERE", "");
                                sqlWhere = String.valueOf(sqlWhere) + " " + resultado;
                            }
                            catch (Exception e) {
                                OracleDataSource.LOGGER.error((Object)"", (Throwable)e);
                            }
                        }
                        if (this.schema.isVersionable()) {
                            if (filter != null) {
                                sqlWhere = String.valueOf(sqlWhere) + " AND ";
                            }
                            sqlWhere = String.valueOf(sqlWhere) + this.schema.getEndDateFilter(this);
                        }
                    }
                    if (fieldOrdered != null) {
                        sqlOrderBy = this.escapeAttributeName(fieldOrdered);
                        sqlOrderBy = ascending != false ? String.valueOf(sqlOrderBy) + " ASC" : String.valueOf(sqlOrderBy) + " DESC";
                    }
                    iterator = this.getIterator(sqlWhere, sqlOrderBy);
                    if (fieldOrdered == null || !this.schema.hasAttribute(fieldOrdered) || this.newRecords.size() <= 0 && this.updateRecords.size() <= 0) ** GOTO lbl43
                    isString = this.schema.getAttribute(fieldOrdered).getType().toJavaClass().equals(String.class);
                    sortedRecords = new ArrayList<SortedAttribute>();
                    while (iterator.hasNext()) {
                        currentRecord = iterator.next();
                        sortedRecords.add(new SortedAttribute(currentRecord.getAttribute(fieldOrdered), currentRecord, ascending, isString));
                    }
                    Collections.sort(sortedRecords);
                    for (SortedAttribute attr : sortedRecords) {
                        records.add((Record)attr.getRecordNumber());
                    }
                    break block17;
lbl-1000:
                    // 1 sources

                    {
                        records.add(iterator.next());
lbl43:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl44:
                    // 1 sources

                }
                catch (Exception e) {
                    OracleDataSource.LOGGER.error((Object)"", (Throwable)e);
                    if (iterator != null) {
                        iterator.close();
                    }
                }
            }
            finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }
        return records;
    }

    @Override
    public String getSQLByFilter(Filter filter, String[] fieldsOrdered, boolean ascending) {
        String sql = "";
        String sqlOrderBy = "";
        String sqlWhere = null;
        try {
            if (filter != null || this.schema.isVersionable()) {
                sqlWhere = "";
                if (filter != null) {
                    SQLEncoderOracle encoder = new SQLEncoderOracle(-1);
                    try {
                        String resultado = encoder.encode(filter);
                        resultado = StringUtil.replace(resultado, "\"", "", true);
                        resultado = resultado.replaceFirst("WHERE", "");
                        sqlWhere = String.valueOf(sqlWhere) + " " + resultado;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
                if (this.schema.isVersionable()) {
                    if (filter != null) {
                        sqlWhere = String.valueOf(sqlWhere) + " AND ";
                    }
                    sqlWhere = String.valueOf(sqlWhere) + this.schema.getEndDateFilter(this);
                }
            }
            if (fieldsOrdered != null && fieldsOrdered.length > 0) {
                int i = 0;
                while (i < fieldsOrdered.length) {
                    sqlOrderBy = String.valueOf(sqlOrderBy) + this.escapeAttributeName(fieldsOrdered[i]) + ",";
                    ++i;
                }
                sqlOrderBy = sqlOrderBy.substring(0, sqlOrderBy.length() - 1);
                sqlOrderBy = ascending ? String.valueOf(sqlOrderBy) + " ASC" : String.valueOf(sqlOrderBy) + " DESC";
            }
            sql = this.getRaizConsultaTipo();
            if (sqlWhere != null) {
                sql = String.valueOf(sql) + " WHERE " + sqlWhere;
            }
            if (sqlOrderBy != null) {
                sql = String.valueOf(sql) + " ORDER BY " + sqlOrderBy;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return sql;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public List<Record> getByAttribute(String[] names, Object[] values, String orderField, Filter filter, boolean ascending) {
        block20: {
            records = new ArrayList<Record>();
            iterator = null;
            sqlOrderBy = null;
            sqlWhere = "";
            try {
                try {
                    if (filter != null || this.schema.isVersionable()) {
                        if (filter != null) {
                            encoder = new SQLEncoderOracle(-1);
                            try {
                                resultado = encoder.encode(filter);
                                resultado = StringUtil.replace(resultado, "\"", "", true);
                                sqlWhere = String.valueOf(sqlWhere) + " " + resultado;
                            }
                            catch (Exception e) {
                                OracleDataSource.LOGGER.error((Object)"", (Throwable)e);
                            }
                        }
                        if (this.schema.isVersionable()) {
                            if (filter != null) {
                                sqlWhere = String.valueOf(sqlWhere) + " AND ";
                            }
                            sqlWhere = String.valueOf(sqlWhere) + this.schema.getEndDateFilter(this);
                        }
                        sqlWhere = sqlWhere.replaceFirst("WHERE ", "");
                    }
                    if (sqlWhere.length() > 0) {
                        sqlWhere = String.valueOf(sqlWhere) + " AND ";
                    }
                    i = 0;
                    while (i < names.length) {
                        sqlWhere = values[i] == null ? String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + " IS NULL AND " : (values[i].getClass().equals(String.class) ? String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + "= '" + values[i] + "' AND " : (values[i].getClass().equals(Boolean.class) ? ((value = (Boolean)values[i]).booleanValue() ? String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + "= 1 AND " : String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + "= 0 AND ") : String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + "=" + values[i].toString() + " AND "));
                        ++i;
                    }
                    if (sqlWhere.length() > 0) {
                        sqlWhere = sqlWhere.substring(0, sqlWhere.length() - 5);
                    }
                    if (orderField != null) {
                        sqlOrderBy = this.escapeAttributeName(orderField);
                        sqlOrderBy = ascending != false ? String.valueOf(sqlOrderBy) + " ASC" : String.valueOf(sqlOrderBy) + " DESC";
                    }
                    iterator = this.getIterator(sqlWhere, sqlOrderBy);
                    if (orderField == null || !this.schema.hasAttribute(orderField) || this.newRecords.size() <= 0 && this.updateRecords.size() <= 0) ** GOTO lbl51
                    isString = this.schema.getAttribute(orderField).getType().toJavaClass().equals(String.class);
                    sortedRecords = new ArrayList<SortedAttribute>();
                    while (iterator.hasNext()) {
                        currentRecord = iterator.next();
                        sortedRecords.add(new SortedAttribute(currentRecord.getAttribute(orderField), currentRecord, ascending, isString));
                    }
                    Collections.sort(sortedRecords);
                    for (SortedAttribute attr : sortedRecords) {
                        records.add((Record)attr.getRecordNumber());
                    }
                    break block20;
lbl-1000:
                    // 1 sources

                    {
                        records.add(iterator.next());
lbl51:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl52:
                    // 1 sources

                }
                catch (Exception e) {
                    OracleDataSource.LOGGER.error((Object)"", (Throwable)e);
                    if (iterator != null) {
                        iterator.close();
                    }
                }
            }
            finally {
                if (iterator != null) {
                    iterator.close();
                }
            }
        }
        return records;
    }

    public boolean equals(Object other) {
        if (!(other instanceof OracleDataSource)) {
            return false;
        }
        TableDBRecordDataSource otherDS = (TableDBRecordDataSource)other;
        return this.checkHost(otherDS.getHost()) && this.getPort() == otherDS.getPort() && this.getDataBaseName().equals(otherDS.getDataBaseName()) && this.getUser().equals(otherDS.getUser()) && this.getPassword().equals(otherDS.getPassword());
    }

    @Override
    public Record readRecord(ResultSet resultSet) throws SQLException {
        Record record = new Record(this.schema);
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                Object objetoBaseDatos = this.getValue(resultSet, i + 1);
                if (objetoBaseDatos != null && objetoBaseDatos.getClass().equals(String.class)) {
                    String value = objetoBaseDatos.toString().trim();
                    objetoBaseDatos = value.equals("") ? null : value;
                }
                record.setAttribute(i, objetoBaseDatos);
            }
            ++i;
        }
        return record;
    }

    protected Object getValue(ResultSet rs, int fieldId) throws SQLException {
        Object val = null;
        Object bdObj = rs.getObject(fieldId);
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

    @Override
    public void setPkName(String pkName) {
        block18: {
            if (pkName == null || pkName.equals("")) {
                Connection con = null;
                try {
                    try {
                        con = DataBaseConnectionFactory.getConnection(this);
                        DatabaseMetaData dmd = con.getMetaData();
                        ResultSet resPK = dmd.getPrimaryKeys(null, this.getDataBaseSchema(), this.tableName.toUpperCase());
                        if (resPK.next()) {
                            pkName = resPK.getString(4).toLowerCase();
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
        this.properties.put("PRIMARY_KEY_COLUMN_NAME", this.pkName);
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
            return this.tableName;
        }
        return String.valueOf(this.dataBaseSchema) + "." + this.tableName;
    }

    @Override
    public ITableIterator getIterator() {
        return new OracleTableIterator(this.getRaizConsultaTipo(), this);
    }

    @Override
    public ITableIterator getIterator(String sqlWhere, String sqlOrderBy) {
        String sql = this.getRaizConsultaTipo();
        if (sqlWhere != null) {
            sql = String.valueOf(sql) + " WHERE " + sqlWhere;
        }
        if (sqlOrderBy != null) {
            sql = String.valueOf(sql) + " ORDER BY " + sqlOrderBy;
        }
        return new OracleTableIterator(sql, this);
    }

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
    public List<Record> getHistoryOfElement(Object pkId, Filter filter) throws Exception {
        if (!this.schema.isVersionable()) {
            throw new Exception(I18N.getString(this.getClass(), "The-layer-has-not-got-its-time-variable-enabled"));
        }
        ArrayList<Record> records = new ArrayList<Record>();
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
        ITableIterator it = null;
        try {
            it = new OracleTableIterator(sql, finalFilter, this);
            while (it.hasNext()) {
                records.add(it.next());
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return records;
    }

    @Override
    public Object clone() {
        OracleDataSource newDS = new OracleDataSource(this.host, this.port, this.dataBaseName, this.user, this.password);
        newDS.dataBaseSchema = this.getDataBaseSchema();
        newDS.name = this.getName();
        newDS.tableName = this.getTableName();
        newDS.pkName = this.getPkName();
        newDS.setInMemory(this.inMemory);
        newDS.setEditable(this.isEditable());
        if (this.getSchema() != null) {
            newDS.setSchema((FeatureSchema)this.getSchema().clone());
        }
        return newDS;
    }

    @Override
    public String getLimitSQL(int startingIndex, int numberOfElements) {
        return String.valueOf(this.getRaizConsultaTipo()) + " ORDER BY " + this.escapeAttributeName(this.pkName) + " ASC LIMIT " + numberOfElements + " OFFSET " + startingIndex;
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
    protected String escapePkValueIfNeeded(String value) {
        return value;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPool() throws SQLException {
        OracleConnectionPoolDataSource dataSource = new OracleConnectionPoolDataSource();
        dataSource.setDriverType("thin");
        dataSource.setServerName(this.host);
        dataSource.setPortNumber(this.port);
        dataSource.setServiceName(this.dataBaseName);
        dataSource.setUser(this.user);
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
    public int getDefaultPort() {
        return 1521;
    }

    @Override
    public TableRecordDataSource buildFromProperties(Map<String, Object> properties) throws Exception {
        String userName = (String)properties.get("USER");
        String passw = (String)properties.get("PASSWORD");
        String encryptPassw = (String)properties.get("ENCRYPTED_PASSWORD");
        String tableName = (String)properties.get("TABLE_NAME");
        String databaseName = (String)properties.get("DATABASE_NAME");
        String schemaName = (String)properties.get("SCHEMA");
        if (databaseName == null) {
            databaseName = schemaName;
            schemaName = "";
        }
        if (passw == null && encryptPassw != null) {
            passw = OracleDataSource.getDecryptedPassword(encryptPassw);
        }
        String hostName = (String)properties.get("HOST");
        Integer port = (Integer)properties.get("PORT");
        String pkColumnName = (String)properties.get("PRIMARY_KEY_COLUMN_NAME");
        OracleDataSource oracleDS = new OracleDataSource(hostName, port, databaseName, userName, passw);
        oracleDS.setDataBaseSchema(schemaName);
        oracleDS.setTableName(tableName);
        oracleDS.buildSchema();
        oracleDS.setPkName(pkColumnName);
        return oracleDS;
    }
}

