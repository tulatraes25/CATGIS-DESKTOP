/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.postgresql.ds.PGConnectionPoolDataSource
 */
package org.saig.core.model.data.dao.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.ConnectionPoolDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.IPoolableDBDataSource;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.LogicFilterImpl;
import org.saig.core.filter.NullFilterImpl;
import org.saig.core.filter.SQLEncoderPostgisGeos;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.data.dao.jdbc.iterators.PostgreSQLTableIterator;
import org.saig.jump.lang.I18N;

public class PostgreSQLDataSource
extends TableDBRecordDataSource
implements IPoolableDBDataSource {
    public static final Logger LOGGER = Logger.getLogger(PostgreSQLDataSource.class);
    public static final String ID = "PostgreSQL";

    public PostgreSQLDataSource() {
    }

    public PostgreSQLDataSource(String host, int port, String databaseName, String username, String password) {
        super(username, password, databaseName, host, port);
    }

    @Override
    public int toSQLType(AttributeType attrType) {
        int sqlType = PostGisDataSource.attributeTypeToSQLType.get(attrType);
        return sqlType;
    }

    @Override
    protected String getLimitSQL(int limit) {
        return String.valueOf(this.getRaizConsultaTipo()) + " LIMIT " + limit;
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limite) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Connection con = null;
        Statement statement = null;
        String sql = "SELECT DISTINCT " + this.escapeAttributeName(field) + " FROM " + this.getFullTableName() + " LIMIT " + limite;
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
        String sql = "CREATE TABLE " + this.getFullTableName() + "(";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            String name = this.schema.getAttributeName(i);
            AttributeType attributeType = this.schema.getAttributeType(i);
            String attributeTypeName = PostGisDataSource.attributeTypeToDBType.get(attributeType);
            sql = String.valueOf(sql) + this.escapeAttributeName(name) + " " + attributeTypeName;
            if (this.schema.getGeometryIndex() == i) {
                sql = String.valueOf(sql) + " NOT NULL";
            } else if (this.schema.getPrimaryKeyIndex() == i) {
                sql = String.valueOf(sql) + " NOT NULL ";
            }
            sql = String.valueOf(sql) + ",";
            ++i;
        }
        sql = String.valueOf(sql) + " PRIMARY KEY (" + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + "))";
        return sql;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public List<Record> getRecords(String fieldOrdered, Filter filter, boolean ascending) {
        block19: {
            records = new ArrayList<Record>();
            iterator = null;
            sqlOrderBy = null;
            sqlWhere = null;
            queryFilter = filter;
            try {
                try {
                    if (filter != null || this.schema.isVersionable()) {
                        sqlWhere = "";
                        if (filter != null) {
                            encoder = new SQLEncoderPostgisGeos();
                            try {
                                filtroStr = encoder.encode(filter);
                                filtroStr = filtroStr.replaceFirst("WHERE", "");
                                sqlWhere = String.valueOf(sqlWhere) + " " + filtroStr;
                            }
                            catch (Exception e) {
                                PostgreSQLDataSource.LOGGER.error((Object)"", (Throwable)e);
                            }
                        }
                        if (this.schema.isVersionable()) {
                            if (filter != null) {
                                sqlWhere = String.valueOf(sqlWhere) + " AND ";
                            }
                            strEndDateFilter = this.schema.getEndDateFilter(this);
                            sqlWhere = String.valueOf(sqlWhere) + strEndDateFilter;
                            try {
                                endDateFilter = this.buildEndDateFilter(strEndDateFilter);
                                queryFilter = queryFilter == null ? endDateFilter : new LogicFilterImpl(queryFilter, endDateFilter, 2);
                            }
                            catch (Exception e) {
                                PostgreSQLDataSource.LOGGER.error((Object)I18N.getMessage(this.getClass(), "could-not-build-filter-{0}-{1}", new Object[]{strEndDateFilter, e.getMessage()}));
                            }
                        }
                    }
                    if (fieldOrdered != null) {
                        sqlOrderBy = this.escapeAttributeName(fieldOrdered);
                        sqlOrderBy = ascending != false ? String.valueOf(sqlOrderBy) + " ASC" : String.valueOf(sqlOrderBy) + " DESC";
                    }
                    iterator = this.getIterator(sqlWhere, sqlOrderBy, queryFilter);
                    if (fieldOrdered == null || !this.schema.hasAttribute(fieldOrdered) || this.newRecords.size() <= 0 && this.updateRecords.size() <= 0) ** GOTO lbl50
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
                    break block19;
lbl-1000:
                    // 1 sources

                    {
                        records.add(iterator.next());
lbl50:
                        // 2 sources

                        ** while (iterator.hasNext())
                    }
lbl51:
                    // 1 sources

                }
                catch (Exception e) {
                    PostgreSQLDataSource.LOGGER.error((Object)"", (Throwable)e);
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
        Filter queryFilter = filter;
        try {
            if (filter != null || this.schema.isVersionable()) {
                sqlWhere = "";
                if (filter != null) {
                    SQLEncoderPostgisGeos encoder = new SQLEncoderPostgisGeos();
                    try {
                        String filtroStr = encoder.encode(filter);
                        filtroStr = filtroStr.replaceFirst("WHERE", "");
                        sqlWhere = String.valueOf(sqlWhere) + " " + filtroStr;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
                if (this.schema.isVersionable()) {
                    if (filter != null) {
                        sqlWhere = String.valueOf(sqlWhere) + " AND ";
                    }
                    String strEndDateFilter = this.schema.getEndDateFilter(this);
                    sqlWhere = String.valueOf(sqlWhere) + strEndDateFilter;
                    try {
                        Filter endDateFilter = this.buildEndDateFilter(strEndDateFilter);
                        queryFilter = queryFilter == null ? endDateFilter : new LogicFilterImpl(queryFilter, endDateFilter, 2);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)I18N.getMessage(this.getClass(), "could-not-build-filter-{0}-{1}", new Object[]{strEndDateFilter, e.getMessage()}));
                    }
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
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public List<Record> getByAttribute(String[] names, Object[] values, String orderField, Filter filter, boolean ascending) {
        ArrayList<Record> records = new ArrayList<Record>();
        ITableIterator iterator = null;
        String sqlOrderBy = null;
        String sqlWhere = "";
        Filter queryFilter = filter;
        try {
            try {
                if (filter != null || this.schema.isVersionable()) {
                    if (filter != null) {
                        SQLEncoderPostgisGeos encoder = new SQLEncoderPostgisGeos();
                        try {
                            String filtroStr = encoder.encode(filter);
                            filtroStr = filtroStr.replaceFirst("WHERE", "");
                            sqlWhere = String.valueOf(sqlWhere) + " " + filtroStr;
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                        }
                    }
                    if (this.schema.isVersionable()) {
                        if (filter != null) {
                            sqlWhere = String.valueOf(sqlWhere) + " AND ";
                        }
                        String strEndDateFilter = this.schema.getEndDateFilter(this);
                        sqlWhere = String.valueOf(sqlWhere) + strEndDateFilter;
                        try {
                            Filter endDateFilter = this.buildEndDateFilter(strEndDateFilter);
                            queryFilter = queryFilter == null ? endDateFilter : new LogicFilterImpl(queryFilter, endDateFilter, 2);
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)I18N.getMessage(this.getClass(), "could-not-build-filter-{0}-{1}", new Object[]{strEndDateFilter, e.getMessage()}));
                        }
                    }
                }
                if (sqlWhere.length() > 0) {
                    sqlWhere = String.valueOf(sqlWhere) + " AND ";
                }
                String attrValueStrFilter = "";
                String attrFilter = "";
                int i = 0;
                while (true) {
                    if (i >= names.length) {
                        if (attrValueStrFilter.length() <= 0) break;
                        attrValueStrFilter = attrValueStrFilter.substring(0, attrValueStrFilter.length() - 5);
                        attrFilter = attrFilter.substring(0, attrFilter.length() - 5);
                        try {
                            Filter attrValueFilter = (Filter)ExpressionBuilder.parse(this.schema, attrFilter.replaceAll("\"", ""));
                            if (queryFilter == null) {
                                queryFilter = attrValueFilter;
                                break;
                            }
                            queryFilter = new LogicFilterImpl(queryFilter, attrValueFilter, 2);
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)I18N.getMessage(this.getClass(), "could-not-build-filter-{0}-{1}", new Object[]{attrFilter, e.getMessage()}));
                        }
                        break;
                    }
                    if (values[i] == null) {
                        attrValueStrFilter = String.valueOf(attrValueStrFilter) + " " + this.escapeAttributeName(names[i]) + " IS NULL AND ";
                        attrFilter = String.valueOf(attrFilter) + " ISNULL(" + this.escapeAttributeName(names[i]) + ") AND ";
                    } else if (values[i].getClass().equals(String.class)) {
                        attrValueStrFilter = String.valueOf(attrValueStrFilter) + this.escapeAttributeName(names[i]) + "= '" + values[i] + "' AND ";
                        attrFilter = String.valueOf(attrFilter) + this.escapeAttributeName(names[i]) + "= '" + values[i] + "' AND ";
                    } else {
                        attrValueStrFilter = String.valueOf(attrValueStrFilter) + this.escapeAttributeName(names[i]) + "=" + values[i].toString() + " AND ";
                        attrFilter = String.valueOf(attrFilter) + this.escapeAttributeName(names[i]) + "=" + values[i].toString() + " AND ";
                    }
                    ++i;
                }
                sqlWhere = attrValueStrFilter.length() > 0 ? String.valueOf(sqlWhere) + attrValueStrFilter : sqlWhere.substring(0, sqlWhere.length() - 5);
                if (orderField != null) {
                    sqlOrderBy = this.escapeAttributeName(orderField);
                    sqlOrderBy = ascending ? String.valueOf(sqlOrderBy) + " ASC" : String.valueOf(sqlOrderBy) + " DESC";
                }
                iterator = this.getIterator(sqlWhere, sqlOrderBy, queryFilter);
                if (orderField != null && this.schema.hasAttribute(orderField) && (this.newRecords.size() > 0 || this.updateRecords.size() > 0)) {
                    boolean isString = this.schema.getAttribute(orderField).getType().toJavaClass().equals(String.class);
                    ArrayList<SortedAttribute> sortedRecords = new ArrayList<SortedAttribute>();
                    while (true) {
                        if (!iterator.hasNext()) break;
                        Record currentRecord = iterator.next();
                        sortedRecords.add(new SortedAttribute(currentRecord.getAttribute(orderField), currentRecord, ascending, isString));
                    }
                    Collections.sort(sortedRecords);
                    Iterator iter = sortedRecords.iterator();
                    while (true) {
                        if (!iter.hasNext()) {
                            return records;
                        }
                        SortedAttribute attr = (SortedAttribute)iter.next();
                        records.add((Record)attr.getRecordNumber());
                    }
                }
                while (true) {
                    if (!iterator.hasNext()) {
                        return records;
                    }
                    records.add(iterator.next());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (iterator == null) return records;
                iterator.close();
                return records;
            }
        }
        finally {
            if (iterator != null) {
                iterator.close();
            }
        }
    }

    public boolean equals(Object other) {
        if (!(other instanceof PostgreSQLDataSource)) {
            return false;
        }
        TableDBRecordDataSource otherDS = (TableDBRecordDataSource)other;
        return this.checkHost(otherDS.getHost()) && this.getPort() == otherDS.getPort() && this.getDataBaseName().equals(otherDS.getDataBaseName()) && this.getUser().equals(otherDS.getUser()) && this.getPassword().equals(otherDS.getPassword());
    }

    @Override
    public String getFullTableName() {
        if (StringUtils.isEmpty((String)this.dataBaseSchema)) {
            return "\"" + this.tableName + "\"";
        }
        return "\"" + this.dataBaseSchema + "\"" + "." + "\"" + this.tableName + "\"";
    }

    @Override
    public ITableIterator getIterator() {
        return new PostgreSQLTableIterator(this.getRaizConsultaTipo(), this);
    }

    @Override
    public ITableIterator getIterator(String sqlWhere, String sqlOrderBy) {
        Filter filter = null;
        try {
            if (sqlWhere.length() > 0) {
                filter = (Filter)ExpressionBuilder.parse(this.schema, sqlWhere.replaceAll("\"", ""));
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)I18N.getMessage(this.getClass(), "could-not-build-filter-{0}-{1}", new Object[]{sqlWhere, e.getMessage()}));
        }
        return this.getIterator(sqlWhere, sqlOrderBy, filter);
    }

    public ITableIterator getIterator(String sqlWhere, String sqlOrderBy, Filter filter) {
        String sql = this.getRaizConsultaTipo();
        if (sqlWhere != null) {
            sql = String.valueOf(sql) + " WHERE " + sqlWhere;
        }
        if (sqlOrderBy != null) {
            sql = String.valueOf(sql) + " ORDER BY " + sqlOrderBy;
        }
        return new PostgreSQLTableIterator(sql, filter, this);
    }

    @Override
    public String processSQLExpressionSinComillas(String sql, String name1, String name2, String tableName) {
        sql = sql.replaceAll(name1, String.valueOf(tableName) + ".\"" + name2 + "\"");
        return sql;
    }

    private Filter buildEndDateFilter(String endDateFilterString) throws IllegalFilterException {
        Filter endDateFilter = null;
        String endDateField = this.schema.getFieldEndDate();
        AttributeExpressionImpl2 endDateAttr = new AttributeExpressionImpl2(endDateField);
        NullFilterImpl nullFilter = new NullFilterImpl();
        nullFilter.setNullCheckValue(endDateAttr);
        if (endDateFilterString.contains(" OR ")) {
            LiteralExpressionImpl dateExpr = new LiteralExpressionImpl(this.schema.getVersionableViewDate().toString());
            CompareFilterImpl compareFilter = new CompareFilterImpl(16);
            compareFilter.addLeftValue(endDateAttr);
            compareFilter.addRightValue(dateExpr);
            endDateFilter = nullFilter.or(compareFilter);
        } else {
            endDateFilter = nullFilter;
        }
        return endDateFilter;
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
        SQLEncoderPostgisGeos encoder = new SQLEncoderPostgisGeos();
        sql = String.valueOf(sql) + " " + encoder.encode(finalFilter);
        ITableIterator it = null;
        try {
            it = new PostgreSQLTableIterator(sql, finalFilter, this);
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
    protected String escapeAttributeName(String attrName) {
        return "\"" + attrName + "\"";
    }

    @Override
    public Object clone() {
        PostgreSQLDataSource newDS = new PostgreSQLDataSource(this.host, this.port, this.dataBaseName, this.user, this.password);
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
        boolean mustEscapeValue = !Number.class.isAssignableFrom(this.schema.getAttributeType(this.pkName).toJavaClass());
        String result = "";
        result = mustEscapeValue ? "'" + value + "'" : value;
        return result;
    }

    @Override
    public ConnectionPoolDataSource createConnectionPool() throws SQLException {
        PGConnectionPoolDataSource dataSource = new PGConnectionPoolDataSource();
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
        return 5432;
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
            passw = PostgreSQLDataSource.getDecryptedPassword(encryptPassw);
        }
        String hostName = (String)properties.get("HOST");
        Integer port = (Integer)properties.get("PORT");
        String pkColumnName = (String)properties.get("PRIMARY_KEY_COLUMN_NAME");
        PostgreSQLDataSource postgreSQLDS = new PostgreSQLDataSource(hostName, port, databaseName, userName, passw);
        postgreSQLDS.setDataBaseSchema(schemaName);
        postgreSQLDS.setTableName(tableName);
        postgreSQLDS.buildSchema();
        postgreSQLDS.setPkName(pkColumnName);
        return postgreSQLDS;
    }

    @Override
    protected AttributeType buildAttributeType(ResultSet rs) throws SQLException {
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
            throw new SQLException(String.valueOf(I18N.getString("org.saig.core.dao.datasource.dbdatasource.PostGisDataSource.sql-exception-ocurred")) + " " + e.getMessage());
        }
    }
}

