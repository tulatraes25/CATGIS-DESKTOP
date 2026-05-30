/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.jdbc;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.filter.Filter;
import org.saig.core.filter.SQLEncoder;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.jdbc.iterators.JDBCTableIterator;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;

public class MDBDataSource
extends TableDBRecordDataSource {
    public static final Logger LOGGER = Logger.getLogger(MDBDataSource.class);
    public static final String ID = "MS Access";
    public static final Map<Integer, Class<?>> TYPE_MAPPINGS = new HashMap();
    public static Map<AttributeType, Integer> attributeTypeToSQLType = new HashMap<AttributeType, Integer>();
    private String fileName;

    static {
        TYPE_MAPPINGS.put(new Integer(12), String.class);
        TYPE_MAPPINGS.put(new Integer(1), String.class);
        TYPE_MAPPINGS.put(new Integer(-1), String.class);
        TYPE_MAPPINGS.put(new Integer(-7), Boolean.class);
        TYPE_MAPPINGS.put(new Integer(16), Boolean.class);
        TYPE_MAPPINGS.put(new Integer(-6), Short.class);
        TYPE_MAPPINGS.put(new Integer(5), Short.class);
        TYPE_MAPPINGS.put(new Integer(4), Integer.class);
        TYPE_MAPPINGS.put(new Integer(-5), Long.class);
        TYPE_MAPPINGS.put(new Integer(7), Float.class);
        TYPE_MAPPINGS.put(new Integer(6), Double.class);
        TYPE_MAPPINGS.put(new Integer(8), Double.class);
        TYPE_MAPPINGS.put(new Integer(3), BigDecimal.class);
        TYPE_MAPPINGS.put(new Integer(2), BigDecimal.class);
        TYPE_MAPPINGS.put(new Integer(91), Date.class);
        TYPE_MAPPINGS.put(new Integer(92), Time.class);
        TYPE_MAPPINGS.put(new Integer(93), Timestamp.class);
        TYPE_MAPPINGS.put(2004, Object.class);
        TYPE_MAPPINGS.put(2005, Object.class);
        TYPE_MAPPINGS.put(-2, Object.class);
        TYPE_MAPPINGS.put(-3, Object.class);
        TYPE_MAPPINGS.put(-4, Object.class);
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
        attributeTypeToSQLType.put(AttributeType.TIMESTAMP, new Integer(91));
        attributeTypeToSQLType.put(AttributeType.TIME, new Integer(91));
        attributeTypeToSQLType.put(AttributeType.TINYINT, new Integer(-6));
        attributeTypeToSQLType.put(AttributeType.VARCHAR, new Integer(12));
        attributeTypeToSQLType.put(AttributeType.BIGDECIMAL, new Integer(3));
    }

    public MDBDataSource() {
    }

    public MDBDataSource(String fileName, String user, String password) {
        super(user, password, "", "", -1);
        this.fileName = fileName;
        this.properties.put("FILE", fileName);
    }

    @Override
    public Connection getConnection() throws Exception {
        File file = new File(this.fileName.trim());
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException(I18N.getMessage("org.saig.core.model.data.dao.jdbc.MDBDataSource.File-{0}-not-found", new Object[]{this.fileName.trim()}));
        }
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
        database = String.valueOf(database) + this.fileName.trim() + ";DriverID=22;READONLY=true}";
        return DriverManager.getConnection(database, this.user, this.password);
    }

    @Override
    protected AttributeType buildAttributeType(ResultSet rs) throws SQLException {
        Class<?> type;
        block3: {
            int DATA_TYPE = 5;
            try {
                int dataType = rs.getInt(5);
                type = TYPE_MAPPINGS.get(new Integer(dataType));
                if (type != null) break block3;
                return null;
            }
            catch (SQLException e) {
                throw new SQLException(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.MySQLDataSource.sql-exception-occurred")) + e.getMessage());
            }
        }
        return AttributeType.toAttributeType(type);
    }

    public void buildSchema(String pkName) throws Exception {
        Connection con = null;
        try {
            con = this.getConnection();
            this.buildFeatureSchema(con, pkName);
            this.properties.put("PRIMARY_KEY_COLUMN_NAME", pkName);
        }
        finally {
            this.closeConnection(con);
        }
    }

    protected void buildFeatureSchema(Connection con, String pkName) throws Exception {
        int COLUMN_NAME = 4;
        this.schema = new FeatureSchema();
        try {
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rs = dmd.getColumns(null, null, this.tableName, "%");
            while (rs.next()) {
                AttributeType tipo = this.buildAttributeType(rs);
                if (tipo == null) {
                    tipo = AttributeType.STRING;
                } else if (tipo.equals(AttributeType.GEOMETRY)) continue;
                String columnName = rs.getString(4);
                this.schema.addAttribute(columnName, tipo);
            }
            rs.close();
            if (this.schema.getAttributeCount() == 0) {
                throw new Exception(I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.MySQLDataSource.the-table-or-view-{0}-does-not-exist", new Object[]{this.tableName}));
            }
            if (pkName != null) {
                this.schema.getAttribute(pkName).setPrimaryKey(true);
                this.pkName = pkName;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public List<String> getColumns(String tableName) {
        ArrayList<String> columns;
        block17: {
            columns = new ArrayList<String>();
            int COLUMN_NAME = 4;
            Connection con = null;
            try {
                try {
                    con = this.getConnection();
                    DatabaseMetaData dmd = con.getMetaData();
                    ResultSet rs = dmd.getColumns(null, null, tableName, "%");
                    while (rs.next()) {
                        AttributeType tipo = this.buildAttributeType(rs);
                        if (tipo == null) {
                            tipo = AttributeType.STRING;
                        } else if (tipo.equals(AttributeType.GEOMETRY)) continue;
                        String columnName = rs.getString(4);
                        columns.add(columnName);
                    }
                    rs.close();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (con != null) {
                        try {
                            con.close();
                        }
                        catch (SQLException e2) {
                            e2.printStackTrace();
                        }
                    }
                    break block17;
                }
            }
            catch (Throwable throwable) {
                if (con != null) {
                    try {
                        con.close();
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                throw throwable;
            }
            if (con != null) {
                try {
                    con.close();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(columns, Collator.getInstance(I18N.getLocale()));
        return columns;
    }

    @Override
    protected String getLimitSQL(int limit) {
        StringBuffer sql = new StringBuffer("SELECT TOP " + limit + " ");
        sql.append(this.getSQLRoot());
        sql.append(" FROM ");
        sql.append(this.getFullTableName());
        return sql.toString();
    }

    @Override
    protected String getSQLForCreateTable() {
        throw new I18NUnsupportedOperationException();
    }

    public boolean equals(Object other) {
        if (!(other instanceof MDBDataSource)) {
            return false;
        }
        TableDBRecordDataSource otherDS = (TableDBRecordDataSource)other;
        return this.checkHost(otherDS.getHost()) && this.getPort() == otherDS.getPort() && this.getDataBaseName().equals(otherDS.getDataBaseName()) && this.getUser().equals(otherDS.getUser()) && this.getPassword().equals(otherDS.getPassword());
    }

    @Override
    protected int toSQLType(AttributeType attrType) {
        return attributeTypeToSQLType.get(attrType);
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
                            encoder = new SQLEncoder();
                            try {
                                sqlWhere = String.valueOf(sqlWhere) + " " + encoder.encode(filter);
                            }
                            catch (Exception e) {
                                MDBDataSource.LOGGER.error((Object)"", (Throwable)e);
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
                    if (fieldOrdered == null || !this.schema.hasAttribute(fieldOrdered) || this.newRecords.size() <= 0 && this.updateRecords.size() <= 0) ** GOTO lbl40
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
lbl40:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl41:
                    // 1 sources

                }
                catch (Exception e) {
                    MDBDataSource.LOGGER.error((Object)"", (Throwable)e);
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
                    SQLEncoder encoder = new SQLEncoder();
                    try {
                        sqlWhere = String.valueOf(sqlWhere) + " " + encoder.encode(filter);
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
            if (!ArrayUtils.isEmpty((Object[])fieldsOrdered)) {
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
    public List<Record> getByAttribute(String[] fields, Object[] values, String orderField, Filter filter, boolean ascending) {
        block22: {
            records = new ArrayList<Record>();
            iterator = null;
            sqlOrderBy = null;
            sqlWhere = "";
            try {
                try {
                    if (filter != null || this.schema.isVersionable()) {
                        if (filter != null) {
                            encoder = new SQLEncoder();
                            try {
                                sqlWhere = String.valueOf(sqlWhere) + " " + encoder.encode(filter);
                            }
                            catch (Exception e) {
                                MDBDataSource.LOGGER.error((Object)"", (Throwable)e);
                            }
                        }
                        if (this.schema.isVersionable()) {
                            if (filter != null) {
                                sqlWhere = String.valueOf(sqlWhere) + " AND ";
                            }
                            sqlWhere = String.valueOf(sqlWhere) + this.schema.getEndDateFilter(this);
                        }
                    }
                    if (sqlWhere.length() > 0) {
                        sqlWhere = String.valueOf(sqlWhere) + " AND ";
                    }
                    i = 0;
                    while (i < fields.length) {
                        fieldName = fields[i];
                        value = values[i];
                        if (value == null) {
                            sqlWhere = String.valueOf(sqlWhere) + " isNull(" + this.escapeAttributeName(fieldName) + ")";
                        } else {
                            sqlWhere = String.valueOf(sqlWhere) + this.escapeAttributeName(fieldName) + " = ";
                            sqlWhere = value instanceof String != false ? String.valueOf(sqlWhere) + "'" + value + "'" : String.valueOf(sqlWhere) + value;
                        }
                        sqlWhere = String.valueOf(sqlWhere) + " AND ";
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
                    if (orderField == null || !this.schema.hasAttribute(orderField) || this.newRecords.size() <= 0 && this.updateRecords.size() <= 0) ** GOTO lbl55
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
                    break block22;
lbl-1000:
                    // 1 sources

                    {
                        records.add(iterator.next());
lbl55:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl56:
                    // 1 sources

                }
                catch (Exception e) {
                    MDBDataSource.LOGGER.error((Object)"", (Throwable)e);
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
    public Set<Object> getDistintsValues(String field, int limite) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Connection con = null;
        Statement statement = null;
        String sql = "SELECT TOP " + limite + " DISTINCT " + this.escapeAttributeName(field) + " FROM " + this.getFullTableName();
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
    public String getFullTableName() {
        return "[" + this.tableName + "]";
    }

    @Override
    public List<String> getAllTables(String schema) throws Exception {
        this.dataBaseSchema = null;
        ArrayList<String> resultado = new ArrayList<String>();
        Connection con = null;
        try {
            con = this.getConnection();
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rsBD = dmd.getTables(null, this.dataBaseSchema, null, new String[]{"VIEW", "TABLE"});
            while (rsBD.next()) {
                String table = rsBD.getString("TABLE_NAME");
                resultado.add(table);
            }
            rsBD.close();
        }
        finally {
            this.closeConnection(con);
        }
        Collections.sort(resultado, Collator.getInstance(I18N.getLocale()));
        return resultado;
    }

    @Override
    public ITableIterator getIterator() {
        return new JDBCTableIterator(this.getRaizConsultaTipo(), this);
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
        return new JDBCTableIterator(sql, this);
    }

    @Override
    public String processSQLExpressionSinComillas(String sql, String name1, String name2, String tableName) {
        return sql;
    }

    @Override
    public List<Record> getHistoryOfElement(Object pkId, Filter filter) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    protected String escapeAttributeName(String attrName) {
        return "[" + attrName + "]";
    }

    @Override
    public Object clone() {
        MDBDataSource newDS = new MDBDataSource(this.fileName, this.user, this.password);
        newDS.setHost(this.getHost());
        newDS.setPort(this.getPort());
        newDS.setDataBaseName(this.getDataBaseName());
        newDS.setDataBaseSchema(this.getDataBaseSchema());
        newDS.setName(this.getName());
        newDS.setTableName(this.getTableName());
        newDS.setPkName(this.getPkName());
        newDS.setInMemory(this.inMemory);
        newDS.setEditable(this.isEditable());
        if (this.getSchema() != null) {
            newDS.setSchema((FeatureSchema)this.getSchema().clone());
        }
        return newDS;
    }

    @Override
    public String getLimitSQL(int startingIndex, int numberOfElements) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    protected String escapePkValueIfNeeded(String value) {
        return value;
    }

    @Override
    public int getDefaultPort() {
        return 0;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public TableRecordDataSource buildFromProperties(Map<String, Object> properties) throws Exception {
        String path = (String)properties.get("FILE");
        String userName = (String)properties.get("USER");
        String tableName = (String)properties.get("TABLE_NAME");
        String passw = (String)properties.get("PASSWORD");
        String encryptPassw = (String)properties.get("ENCRYPTED_PASSWORD");
        if (passw == null && encryptPassw != null) {
            passw = MDBDataSource.getDecryptedPassword(encryptPassw);
        }
        String pkColumnName = (String)properties.get("PRIMARY_KEY_COLUMN_NAME");
        MDBDataSource mdbDS = new MDBDataSource(path, userName, passw);
        mdbDS.setTableName(tableName);
        mdbDS.buildSchema(pkColumnName);
        return mdbDS;
    }
}

