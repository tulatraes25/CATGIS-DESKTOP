/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao.jdbc;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.ConnectionPoolDataSource;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.IPoolableDBDataSource;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Filter;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.SQLEncoderMySQL;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.jdbc.iterators.MySQLTableIterator;
import org.saig.jump.lang.I18N;

public class MySQLDataSource
extends TableDBRecordDataSource
implements IPoolableDBDataSource {
    public static final Logger LOGGER = Logger.getLogger(MySQLDataSource.class);
    public static final String ID = "MySQL";

    public MySQLDataSource() {
    }

    public MySQLDataSource(String host, int port, String databaseName, String username, String password) {
        super(username, password, databaseName, host, port);
    }

    @Override
    protected int toSQLType(AttributeType attrType) {
        int sqlType = org.saig.core.dao.datasource.dbdatasource.MySQLDataSource.attributeTypeToSQLType.get(attrType);
        return sqlType;
    }

    @Override
    protected String getLimitSQL(int limit) {
        return String.valueOf(this.getRaizConsultaTipo()) + " LIMIT 0," + limit;
    }

    @Override
    protected String getSQLForCreateTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + this.tableName + "(";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            String name = this.schema.getAttributeName(i);
            AttributeType attributeType = this.schema.getAttributeType(i);
            String attributeTypeName = org.saig.core.dao.datasource.dbdatasource.MySQLDataSource.attributeTypeToDBType.get(attributeType);
            sql = String.valueOf(sql) + this.escapeAttributeName(name) + " " + attributeTypeName;
            if (this.schema.getPrimaryKeyIndex() == i) {
                sql = String.valueOf(sql) + " NOT NULL ";
            }
            sql = String.valueOf(sql) + ",";
            ++i;
        }
        sql = String.valueOf(sql) + " PRIMARY KEY(" + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + "))";
        return sql;
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
                            encoder = new SQLEncoderMySQL();
                            try {
                                filterStr = encoder.encode(filter);
                                filterStr = filterStr.replaceFirst("WHERE", "");
                                sqlWhere = String.valueOf(sqlWhere) + " " + filterStr;
                            }
                            catch (Exception e) {
                                MySQLDataSource.LOGGER.error((Object)"", (Throwable)e);
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
                    if (fieldOrdered == null || !this.schema.hasAttribute(fieldOrdered) || this.newRecords.size() <= 0 && this.updateRecords.size() <= 0) ** GOTO lbl42
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
lbl42:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl43:
                    // 1 sources

                }
                catch (Exception e) {
                    MySQLDataSource.LOGGER.error((Object)"", (Throwable)e);
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
                    SQLEncoderMySQL encoder = new SQLEncoderMySQL();
                    try {
                        String filterStr = encoder.encode(filter);
                        filterStr = filterStr.replaceFirst("WHERE", "");
                        sqlWhere = String.valueOf(sqlWhere) + " " + filterStr;
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
        block24: {
            records = new ArrayList<Record>();
            iterator = null;
            sqlOrderBy = null;
            sqlWhere = "";
            try {
                try {
                    if (filter != null || this.schema.isVersionable()) {
                        sqlWhere = "";
                        if (filter != null) {
                            encoder = new SQLEncoderMySQL();
                            try {
                                sqlWhere = String.valueOf(sqlWhere) + " " + encoder.encode(filter);
                            }
                            catch (Exception e) {
                                MySQLDataSource.LOGGER.error((Object)"", (Throwable)e);
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
                    while (i < names.length) {
                        if (values[i] == null) {
                            sqlWhere = String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + " IS NULL AND ";
                        } else if (values[i].getClass().equals(String.class) || values[i].getClass().equals(Date.class)) {
                            sqlWhere = String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + "= '" + values[i] + "' AND ";
                        } else {
                            value = values[i];
                            sqlWhere = String.valueOf(sqlWhere) + this.escapeAttributeName(names[i]) + "='" + value.toString() + "' AND ";
                        }
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
                    if (orderField == null || !this.schema.hasAttribute(orderField) || this.newRecords.size() <= 0 && this.updateRecords.size() <= 0) ** GOTO lbl56
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
                    break block24;
lbl-1000:
                    // 1 sources

                    {
                        records.add(iterator.next());
lbl56:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl57:
                    // 1 sources

                }
                catch (Exception e) {
                    MySQLDataSource.LOGGER.error((Object)"", (Throwable)e);
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
        if (!(other instanceof MySQLDataSource)) {
            return false;
        }
        TableDBRecordDataSource otherDS = (TableDBRecordDataSource)other;
        return this.checkHost(otherDS.getHost()) && this.getPort() == otherDS.getPort() && this.getDataBaseName().equals(otherDS.getDataBaseName()) && this.getUser().equals(otherDS.getUser()) && this.getPassword().equals(otherDS.getPassword());
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limite) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Connection con = null;
        Statement statement = null;
        String sql = "SELECT DISTINCT " + this.escapeAttributeName(field) + " FROM " + this.getFullTableName() + " LIMIT 0," + limite;
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
        return "`" + this.tableName + "`";
    }

    @Override
    public ITableIterator getIterator() {
        return new MySQLTableIterator(this.getRaizConsultaTipo(), this);
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
        return new MySQLTableIterator(sql, this);
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
        SQLEncoderMySQL encoder = new SQLEncoderMySQL();
        sql = String.valueOf(sql) + " " + encoder.encode(finalFilter);
        ITableIterator it = null;
        try {
            it = new MySQLTableIterator(sql, finalFilter, this);
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
    public List<String> getAllTables(String schema) throws Exception {
        this.dataBaseSchema = schema;
        ArrayList<String> resultado = new ArrayList<String>();
        Connection con = null;
        try {
            con = this.getConnection();
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rsBD = dmd.getTables(this.dataBaseName, this.dataBaseSchema, null, new String[]{"TABLE", "VIEW"});
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
    protected String escapeAttributeName(String attrName) {
        return "`" + attrName + "`";
    }

    @Override
    public Object clone() {
        MySQLDataSource newDS = new MySQLDataSource(this.host, this.port, this.dataBaseName, this.user, this.password);
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
    protected String escapePkValueIfNeeded(String value) {
        return value;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPool() {
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(this.host);
        dataSource.setPortNumber(this.port);
        dataSource.setDatabaseName(this.dataBaseName);
        dataSource.setUser(this.user);
        dataSource.setPassword(this.password);
        return dataSource;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public int getDefaultPort() {
        return 3306;
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
            passw = MySQLDataSource.getDecryptedPassword(encryptPassw);
        }
        String hostName = (String)properties.get("HOST");
        Integer port = (Integer)properties.get("PORT");
        String pkColumnName = (String)properties.get("PRIMARY_KEY_COLUMN_NAME");
        MySQLDataSource mysqlDS = new MySQLDataSource(hostName, port, databaseName, userName, passw);
        mysqlDS.setTableName(tableName);
        mysqlDS.buildSchema();
        mysqlDS.setPkName(pkColumnName);
        return mysqlDS;
    }
}

