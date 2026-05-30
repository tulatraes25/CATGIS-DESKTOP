/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.TopologyException
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.dao;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.Layer;
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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;
import org.saig.core.dao.datasource.dbdatasource.utils.Field;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.iterators.ITableIterator;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.TableRelation;
import org.saig.jump.lang.I18N;

public abstract class TableDBRecordDataSource
extends TableRecordDataSource
implements Cloneable {
    private static final Logger LOGGER = Logger.getLogger(TableDBRecordDataSource.class);
    protected static final Map<Integer, Class<?>> TYPE_MAPPINGS = new HashMap();
    protected String name;
    protected String host;
    protected int port;
    protected String dataBaseName;
    protected String dataBaseSchema;
    protected String tableName;
    protected String user;
    protected String password;
    protected String pkName;
    protected IDBKeyResolver keyResolver;

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
        TYPE_MAPPINGS.put(new Integer(2004), Object.class);
        TYPE_MAPPINGS.put(new Integer(2005), Object.class);
        TYPE_MAPPINGS.put(new Integer(-2), Object.class);
    }

    public TableDBRecordDataSource() {
    }

    public TableDBRecordDataSource(String user, String password, String dataBaseName, String host, int port) {
        this.user = user;
        this.password = password;
        this.dataBaseName = dataBaseName;
        this.host = host;
        this.port = port;
        this.properties.put("USER", user);
        this.properties.put("ENCRYPTED_PASSWORD", TableDBRecordDataSource.getEncryptedPassword(password));
        this.properties.put("DATABASE_NAME", dataBaseName);
        this.properties.put("HOST", host);
        this.properties.put("PORT", new Integer(port));
        this.properties.put("DATASOURCE", this.getClass().getName());
    }

    @Override
    public void add(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.addAll(records);
    }

    @Override
    public synchronized void addAll(Collection<Record> records) throws Exception {
        this.addAll(records, false);
    }

    public synchronized void addAll(Collection<Record> records, boolean setId) throws Exception {
        this.addAll(records, setId, true);
    }

    public synchronized void addAll(Collection<Record> records, boolean setId, boolean checkVersionable) throws Exception {
        if (!this.editable || records == null) {
            return;
        }
        if (this.inMemory) {
            for (Record element : records) {
                this.newRecords.add(element);
                if (!this.deleteRecords.contains(element)) continue;
                this.deleteRecords.remove(element);
            }
            return;
        }
        Connection connection = null;
        PreparedStatement statement = null;
        Number id = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                connection.setAutoCommit(false);
                if (setId) {
                    id = this.getMaxID(connection);
                }
                Iterator<Record> recordsIterator = records.iterator();
                while (recordsIterator.hasNext()) {
                    try {
                        String sql;
                        Record record = recordsIterator.next();
                        if (setId || record.getPrimaryKey() == null && AttributeType.isNumeric(record.getSchema().getPrimaryKey().getType())) {
                            if (id == null) {
                                id = this.getMaxID(connection);
                            }
                            id = this.getNextID(id);
                        }
                        if (setId || record.getPrimaryKey() == null) {
                            record.setAttribute(this.schema.getPrimaryKeyName(), (Object)id);
                        }
                        if (checkVersionable && this.schema.isVersionable()) {
                            record.setAttribute(this.schema.getFieldStartDate(), (Object)new java.util.Date());
                        }
                        if ((sql = this.getSQLForInsert(record)) == null) continue;
                        statement = connection.prepareStatement(sql);
                        int position = 1;
                        int i = 0;
                        while (i < this.schema.getAttributeCount()) {
                            Attribute attr = this.schema.getAttribute(i);
                            if (!(attr instanceof AttributeCalculate)) {
                                Object value = record.getAttribute(i);
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
                        this.closeChannel(null, statement);
                    }
                    catch (TopologyException ex2) {
                        LOGGER.error((Object)ex2.getMessage());
                    }
                }
                connection.commit();
            }
            catch (SQLException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                this.closeChannel(null, statement);
                this.rollBackConnection(connection);
                throw ex;
            }
        }
        finally {
            this.closeConnection(connection);
        }
    }

    @Override
    public void update(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.updateAll(records);
    }

    @Override
    public void updateAll(Collection<Record> records) throws Exception {
        if (!this.editable || records == null) {
            return;
        }
        if (this.inMemory) {
            for (Record object : records) {
                if (!object.isUnsaved()) {
                    this.updateRecords.add(object);
                    if (!this.deleteRecords.contains(object)) continue;
                    this.deleteRecords.remove(object);
                    continue;
                }
                this.newRecords.add(object);
            }
            return;
        }
        ArrayList<Record> oldRecords = new ArrayList<Record>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                connection.setAutoCommit(false);
                for (Record record : records) {
                    String sqlForUpdate = this.getSQLForUpdate(record);
                    if (sqlForUpdate == null) continue;
                    if (this.schema.isVersionable() && this.schema.getHistoryField() != null) {
                        this.updateHistoryRecord(record, oldRecords);
                    }
                    preparedStatement = connection.prepareStatement(sqlForUpdate);
                    int position = 1;
                    int i = 0;
                    while (i < this.schema.getAttributeCount()) {
                        Attribute attr = this.schema.getAttribute(i);
                        if (!(attr instanceof AttributeCalculate)) {
                            Object value = record.getAttribute(i);
                            if (value == null || value.toString().trim().length() == 0) {
                                preparedStatement.setNull(position, this.toSQLType(this.schema.getAttribute(i).getType()));
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
                    this.closeChannel(null, preparedStatement);
                }
                connection.commit();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeChannel(null, preparedStatement);
                connection.rollback();
                throw e;
            }
        }
        finally {
            this.closeConnection(connection);
        }
        if (!oldRecords.isEmpty()) {
            this.addAll(oldRecords, true, false);
        }
    }

    @Override
    public synchronized void remove(Record record) throws Exception {
        ArrayList<Record> records = new ArrayList<Record>();
        records.add(record);
        this.removeAll(records);
    }

    @Override
    public synchronized void removeAll(Collection<Record> records) throws Exception {
        if (!this.editable || records == null) {
            return;
        }
        if (this.inMemory) {
            for (Record element : records) {
                if (this.newRecords.contains(element)) {
                    this.newRecords.remove(element);
                }
                if (this.updateRecords.contains(element)) {
                    this.updateRecords.remove(element);
                }
                this.deleteRecords.add(element);
            }
            return;
        }
        String pkNameEscaped = this.escapeAttributeName(this.schema.getPrimaryKeyName());
        boolean check = false;
        String sqlDelete = "";
        sqlDelete = !this.schema.isVersionable() ? "DELETE FROM " + this.getFullTableName() + " WHERE " + pkNameEscaped + " IN (" : "UPDATE " + this.getFullTableName() + " SET " + this.schema.getFieldEndDate() + "=? WHERE " + pkNameEscaped + " IN (";
        for (Record record : records) {
            if (record.isUnsaved()) continue;
            check = true;
            sqlDelete = String.valueOf(sqlDelete) + this.escapePkValueIfNeeded(record.getAttribute(this.pkName).toString()) + ",";
        }
        if (!check) {
            return;
        }
        sqlDelete = String.valueOf(sqlDelete.substring(0, sqlDelete.length() - 1)) + ")";
        Connection connection = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                connection.setAutoCommit(false);
                if (!this.schema.isVersionable()) {
                    Statement statement = null;
                    try {
                        statement = connection.createStatement();
                        statement.execute(sqlDelete);
                    }
                    finally {
                        this.closeChannel(null, statement);
                    }
                }
                PreparedStatement ps = null;
                try {
                    ps = connection.prepareStatement(sqlDelete);
                    ps.setObject(1, new Timestamp(System.currentTimeMillis()));
                    ps.execute();
                }
                finally {
                    this.closeChannel(null, ps);
                }
                connection.commit();
            }
            catch (SQLException e) {
                LOGGER.error((Object)sqlDelete, (Throwable)e);
                this.rollBackConnection(connection);
                throw e;
            }
        }
        finally {
            this.closeConnection(connection);
        }
    }

    protected abstract String escapePkValueIfNeeded(String var1);

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

    public Connection getConnection() throws Exception {
        return DataBaseConnectionFactory.getConnection(this);
    }

    public boolean checkConnection() {
        Connection con = null;
        boolean test = false;
        try {
            try {
                con = this.getConnection();
                test = true;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
        return test;
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

    public List<String> getAllTables(String schema) throws Exception {
        this.dataBaseSchema = schema;
        ArrayList<String> resultado = new ArrayList<String>();
        Connection con = null;
        try {
            con = this.getConnection();
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet rsBD = dmd.getTables(this.dataBaseName, schema, null, new String[]{"TABLE", "VIEW", "ALIAS", "SYNONYM"});
            while (rsBD.next()) {
                String table = rsBD.getString("TABLE_NAME");
                if (!this.allowTable(table)) continue;
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

    public List<String> getSchemas() throws Exception {
        Connection con = null;
        ArrayList<String> schemas = new ArrayList<String>();
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            DatabaseMetaData dmd = con.getMetaData();
            ResultSet res = dmd.getSchemas();
            while (res.next()) {
                String schemaName = res.getString(1);
                if (this.getAllTables(schemaName).size() <= 0) continue;
                schemas.add(res.getString(1));
            }
            res.close();
        }
        finally {
            this.closeConnection(con);
        }
        return schemas;
    }

    public List<Field> getCandidateFields(String schema, String table) throws SQLException {
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
                LOGGER.warn((Object)I18N.getMessage(TableDBRecordDataSource.class, "it-is-not-possible-to-obtain-the-fields-with-unique-restriction-{0}", new Object[]{StringUtils.trimToEmpty((String)sqlEx.getMessage())}));
                LOGGER.debug((Object)sqlEx);
            }
            ResultSet rs = dmd.getColumns(null, schema, table, "%");
            while (rs.next()) {
                AttributeType tipo = this.buildAttributeType(rs);
                if (tipo == null || tipo.equals(AttributeType.GEOMETRY)) continue;
                resultado.add(new Field(rs.getString(4), Field.FieldType.REGULAR_FIELD));
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
        this.properties.put("TABLE_NAME", tableName);
        this.setName(tableName);
    }

    @Override
    public List<Record> getRecords() {
        return this.getRecords(null, null, true);
    }

    @Override
    public List<Record> getRecords(String fieldOrdered) {
        return this.getRecords(fieldOrdered, null, true);
    }

    @Override
    public List<Record> getRecords(String fieldOrdered, Filter filter) {
        return this.getRecords(fieldOrdered, filter, true);
    }

    protected void buildFeatureSchema(Connection con) throws Exception {
        int COLUMN_NAME = 4;
        this.schema = new FeatureSchema();
        DatabaseMetaData dmd = con.getMetaData();
        ResultSet rs = dmd.getColumns(null, this.getDataBaseSchema(), this.tableName, "%");
        while (rs.next()) {
            AttributeType tipo = this.buildAttributeType(rs);
            if (tipo == null) {
                tipo = AttributeType.STRING;
            }
            String columnName = rs.getString(4);
            this.schema.addAttribute(columnName, tipo);
        }
        rs.close();
        if (this.schema.getAttributeCount() == 0) {
            throw new Exception(I18N.getMessage("org.saig.core.model.data.dao.TableDBRecordDataSource.the-table-or-view-{0}-does-not-exist", new Object[]{this.tableName}));
        }
        if (this.pkName != null && this.schema.hasAttribute(this.pkName)) {
            this.schema.getAttribute(this.pkName).setPrimaryKey(true);
        }
    }

    public Record readRecord(ResultSet resultSet) throws SQLException {
        Record record = new Record(this.schema);
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                Object objetoBaseDatos = resultSet.getObject(i + 1);
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

    protected void rollBackConnection(Connection con) {
        try {
            if (con != null) {
                con.rollback();
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)e);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getDataBaseName() {
        return this.dataBaseName;
    }

    public void setDataBaseName(String name) {
        if (StringUtils.isEmpty((String)name)) {
            name = null;
        }
        this.dataBaseName = name;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String name) {
        this.user = name;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String toString() {
        return String.valueOf(this.host) + "," + this.port + "," + this.dataBaseName + "," + this.dataBaseSchema + "," + this.user;
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

    @Override
    public Record getRecord(int index) throws Exception {
        ResultSet resultSet = null;
        Statement statement = null;
        Connection conn = null;
        Record record = null;
        try {
            try {
                conn = DataBaseConnectionFactory.getConnection(this);
                statement = conn.createStatement();
                String consulta = this.getLimitSQL(index, 1);
                resultSet = statement.executeQuery(consulta);
                if (resultSet.next()) {
                    record = this.readRecord(resultSet);
                }
                this.closeChannel(resultSet, statement);
            }
            catch (SQLException ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                this.closeChannel(resultSet, statement);
                throw ex;
            }
        }
        finally {
            this.closeConnection(conn);
        }
        return record;
    }

    protected abstract String getLimitSQL(int var1);

    public void buildSchema() throws Exception {
        Connection con = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            this.buildFeatureSchema(con);
        }
        finally {
            this.closeConnection(con);
        }
    }

    public List<Record> getBySQL(String whereSql) {
        ArrayList<Record> resultado = new ArrayList<Record>();
        whereSql = whereSql.replaceAll("WHERE", "");
        ITableIterator itRecords = null;
        try {
            itRecords = this.getIterator(whereSql);
            while (itRecords.hasNext()) {
                resultado.add(itRecords.next());
            }
        }
        finally {
            if (itRecords != null) {
                itRecords.close();
            }
        }
        return resultado;
    }

    protected String getSQLForInsert(Record record) {
        String selInsert = "INSERT INTO " + this.getFullTableName() + " (" + this.getBasicSQLInsert() + " ) values(";
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

    protected String getBasicSQLInsert() {
        String sql = "";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                sql = String.valueOf(sql) + this.escapeAttributeName(this.schema.getAttributeName(i)) + ",";
            }
            ++i;
        }
        sql = sql.substring(0, sql.length() - 1);
        return sql;
    }

    protected String getSQLForUpdate(Record record) {
        String selInsert = "UPDATE " + this.getFullTableName() + " SET ";
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            Attribute attr = this.schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate)) {
                selInsert = String.valueOf(selInsert) + this.escapeAttributeName(attr.getName()) + "=?,";
            }
            ++i;
        }
        selInsert = selInsert.substring(0, selInsert.length() - 1);
        selInsert = String.valueOf(selInsert) + " WHERE ";
        selInsert = String.valueOf(selInsert) + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + " = '" + record.getAttribute(this.schema.getPrimaryKeyIndex()).toString() + "'";
        return selInsert;
    }

    protected String getQueryByPrimaryKey(Object[] values) {
        if (values == null) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.the-key-set-is-null"));
            return this.getRaizConsultaTipo();
        }
        return String.valueOf(this.getRaizConsultaTipo()) + this.getSQLForWhereByRanges(values);
    }

    @Override
    public List<Record> getByAttribute(String[] names, Object[] values) {
        return this.getByAttribute(names, values, null, null, true);
    }

    @Override
    public List<Record> getByAttribute(String[] names, Object[] values, String orderField) {
        return this.getByAttribute(names, values, orderField, null, true);
    }

    @Override
    public List<Record> getByAttribute(String[] names, Object[] values, String orderField, Filter filter) {
        return this.getByAttribute(names, values, orderField, filter, true);
    }

    @Override
    public long size() {
        int size;
        block8: {
            size = -1;
            Statement statement = null;
            ResultSet resultSet = null;
            Connection connection = null;
            try {
                try {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    statement = connection.createStatement();
                    String sql = "SELECT COUNT(1) FROM " + this.getFullTableName();
                    if (this.schema.isVersionable()) {
                        Relation<?> rel;
                        String tableName;
                        Attribute attr = this.schema.getAttribute(this.schema.getFieldEndDate());
                        String endFieldDate = this.schema.getFieldEndDate();
                        if (attr.isCalculated() && !(tableName = this.getTableNameOfRelation(rel = ((AttributeCalculate)attr).getRelation(), true)).equals("")) {
                            endFieldDate = String.valueOf(tableName) + "." + this.escapeAttributeName(endFieldDate);
                        }
                        sql = String.valueOf(sql) + " WHERE " + endFieldDate + " IS NULL";
                    }
                    if ((resultSet = statement.executeQuery(sql)).next()) {
                        size = resultSet.getInt(1);
                    }
                    this.closeChannel(resultSet, statement);
                }
                catch (Exception ex) {
                    this.closeChannel(resultSet, statement);
                    LOGGER.error((Object)"", (Throwable)ex);
                    this.closeConnection(connection);
                    break block8;
                }
            }
            catch (Throwable throwable) {
                this.closeConnection(connection);
                throw throwable;
            }
            this.closeConnection(connection);
        }
        int trueSize = size;
        trueSize += this.newRecords.size();
        return trueSize -= this.deleteRecords.size();
    }

    protected String getRaizConsultaTipo() {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(this.getSQLRoot());
        sql.append(" FROM ");
        sql.append(this.getFullTableName());
        return sql.toString();
    }

    protected String getSQLRoot() {
        String solucion = "";
        int numAtributos = this.schema.getAttributeCount();
        int i = 0;
        while (i < numAtributos) {
            Attribute atributo = this.schema.getAttribute(i);
            if (!(atributo instanceof AttributeCalculate)) {
                solucion = String.valueOf(solucion) + this.escapeAttributeName(this.schema.getAttributeName(i)) + ",";
            }
            ++i;
        }
        if (StringUtils.isNotEmpty((String)solucion)) {
            solucion = solucion.substring(0, solucion.length() - 1);
        }
        return solucion;
    }

    @Override
    public Record getByPrimaryKey(Object key) {
        if (key instanceof Record) {
            return (Record)key;
        }
        List<Record> records = this.getByPrimaryKey(new Object[]{key});
        if (records == null || records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    @Override
    public List<Record> getByPrimaryKey(Object[] keys) {
        return this.getByPrimaryKey(keys, false);
    }

    public List<Record> getByPrimaryKey(Object[] keys, boolean ignoredUpdate) {
        ArrayList<Record> recordList = new ArrayList<Record>();
        if (ArrayUtils.isEmpty((Object[])keys)) {
            return recordList;
        }
        ArrayList<Object> realKeys = new ArrayList<Object>();
        int i = 0;
        while (i < keys.length) {
            if (keys[i] != null) {
                if (keys[i] instanceof Record) {
                    recordList.add((Record)keys[i]);
                } else {
                    realKeys.add(keys[i]);
                }
            }
            ++i;
        }
        Statement statement = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement();
                int size = realKeys.size();
                int cont = 0;
                int block_size = 5000;
                while (cont < size) {
                    int nextIndex = cont + block_size;
                    int maxIndex = nextIndex < size ? nextIndex : size;
                    Object[] currentKeys = realKeys.subList(cont, maxIndex).toArray();
                    resultSet = statement.executeQuery(this.getQueryByPrimaryKey(currentKeys));
                    while (resultSet.next()) {
                        if (!ignoredUpdate) {
                            recordList.add(this.getUpdatedRecord(this.readRecord(resultSet)));
                            continue;
                        }
                        recordList.add(this.readRecord(resultSet));
                    }
                    cont += block_size;
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
        return recordList;
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending) {
        ArrayList<SortedAttribute> sort;
        boolean isString;
        block10: {
            isString = this.schema.getAttribute(column).getType().toJavaClass().equals(String.class);
            sort = new ArrayList<SortedAttribute>();
            String sql = "";
            sql = column.equals(this.schema.getPrimaryKeyName()) ? "SELECT " + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + " FROM " + this.getFullTableName() : "SELECT " + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + "," + this.escapeAttributeName(column) + " FROM " + this.getFullTableName();
            if (this.schema.isVersionable()) {
                sql = String.valueOf(sql) + " WHERE " + this.schema.getEndDateFilter(this);
            }
            sql = String.valueOf(sql) + " ORDER BY " + this.escapeAttributeName(column);
            sql = ascending ? String.valueOf(sql) + " ASC" : String.valueOf(sql) + " DESC";
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                try {
                    connection = DataBaseConnectionFactory.getConnection(this);
                    statement = connection.createStatement(1003, 1007);
                    resultSet = statement.executeQuery(sql);
                    while (resultSet.next()) {
                        if (column.equals(this.schema.getPrimaryKeyName())) {
                            Object value = resultSet.getObject(column);
                            sort.add(new SortedAttribute(value, value, ascending, isString));
                            continue;
                        }
                        sort.add(new SortedAttribute(resultSet.getObject(column), resultSet.getObject(this.schema.getPrimaryKeyName()), ascending, isString));
                    }
                    this.closeChannel(resultSet, statement);
                }
                catch (Exception sqlException) {
                    LOGGER.error((Object)"", (Throwable)sqlException);
                    this.closeConnection(connection);
                    break block10;
                }
            }
            catch (Throwable throwable) {
                this.closeConnection(connection);
                throw throwable;
            }
            this.closeConnection(connection);
        }
        for (Record element : this.newRecords) {
            Object value = element.getAttribute(column);
            sort.add(new SortedAttribute(value, element, ascending, isString));
        }
        Collections.sort(sort);
        ArrayList<Object> result = new ArrayList<Object>();
        for (SortedAttribute element : sort) {
            result.add(element.getRecordNumber());
        }
        return result;
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        return this.getSortKeys(this.schema.getPrimaryKeyName(), true);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Connection con = null;
        Statement statement = null;
        String sql = "SELECT DISTINCT " + this.escapeAttributeName(field) + " FROM " + this.getFullTableName();
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
                this.closeChannel(res, statement);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
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
                this.closeChannel(res, statement);
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
    public List<Object> getFieldValue(String field, String fieldKey, Object value) {
        ArrayList<Object> values = new ArrayList<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Connection con = null;
        Statement statement = null;
        ResultSet res = null;
        String sql = "SELECT " + this.escapeAttributeName(field) + " FROM " + this.getFullTableName() + " WHERE " + this.escapeAttributeName(fieldKey) + " = ";
        sql = value instanceof String ? String.valueOf(sql) + "'" + value + "'" : String.valueOf(sql) + value;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                statement = con.createStatement(1003, 1007);
                res = statement.executeQuery(sql);
                res.next();
                values.add(res.getObject(1));
                this.closeChannel(res, statement);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeChannel(res, statement);
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
        return values;
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        HashMap<Object, RelationAttribute> values = new HashMap<Object, RelationAttribute>();
        int i = 0;
        while (i < fields.length) {
            if (!this.schema.hasAttribute(fields[i])) {
                return values;
            }
            ++i;
        }
        if (!this.schema.hasAttribute(fieldKey)) {
            return values;
        }
        ArrayList<AttributeCalculate> attrCalculate = new ArrayList<AttributeCalculate>();
        ArrayList<Attribute> attrNoCalculate = new ArrayList<Attribute>();
        int i2 = 0;
        while (i2 < fields.length) {
            Attribute attr = this.schema.getAttribute(fields[i2]);
            if (attr instanceof AttributeCalculate) {
                attrCalculate.add((AttributeCalculate)attr);
            } else {
                attrNoCalculate.add(attr);
            }
            ++i2;
        }
        String sql = "SELECT " + this.escapeAttributeName(fieldKey) + ",";
        int i3 = 0;
        while (i3 < attrNoCalculate.size()) {
            sql = String.valueOf(sql) + this.escapeAttributeName(((Attribute)attrNoCalculate.get(i3)).getName()) + ",";
            ++i3;
        }
        sql = sql.substring(0, sql.length() - 1);
        sql = String.valueOf(sql) + " FROM " + this.getFullTableName();
        Connection con = null;
        Statement statement = null;
        ResultSet res = null;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                statement = con.createStatement(1003, 1007);
                res = statement.executeQuery(sql);
                while (res.next()) {
                    RelationAttribute ra = new RelationAttribute();
                    int i4 = 0;
                    while (i4 < attrNoCalculate.size()) {
                        ra.setFieldValue(((Attribute)attrNoCalculate.get(i4)).getName(), res.getObject(((Attribute)attrNoCalculate.get(i4)).getName()));
                        ++i4;
                    }
                    values.put(res.getObject(1), ra);
                }
                this.closeChannel(res, statement);
            }
            catch (Exception e) {
                this.closeChannel(res, statement);
                this.closeConnection(con);
            }
        }
        finally {
            this.closeConnection(con);
        }
        return values;
    }

    protected abstract int toSQLType(AttributeType var1);

    protected abstract String getSQLForCreateTable();

    @Override
    public boolean createDataStore(Envelope vista, String geomColumn, int srid) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DataBaseConnectionFactory.getConnection(this);
            DatabaseMetaData dmd = connection.getMetaData();
            ResultSet rs = dmd.getColumns(null, this.getDataBaseSchema(), this.tableName, "%");
            boolean newTable = !rs.next();
            rs.close();
            if (newTable) {
                try {
                    statement = connection.createStatement();
                    String sqlForCreateTable = this.getSQLForCreateTable();
                    statement.execute(sqlForCreateTable);
                }
                finally {
                    this.closeChannel(null, statement);
                }
            }
            boolean bl = newTable;
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

    @Override
    public void commit() throws Exception {
        this.inMemory = false;
        try {
            if (this.newRecords.size() > 0) {
                LOGGER.debug((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.saving-{0}-new-features", new Object[]{new Integer(this.newRecords.size())}));
                this.addAll(this.newRecords);
                this.newRecords.clear();
            }
            if (this.deleteRecords.size() > 0) {
                ArrayList<Record> featuresFilter = new ArrayList<Record>();
                for (Record element : this.deleteRecords) {
                    if (element.isUnsaved()) continue;
                    featuresFilter.add(element);
                }
                LOGGER.debug((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.deleting-{0}-features", new Object[]{new Integer(featuresFilter.size())}));
                this.removeAll(featuresFilter);
                this.deleteRecords.clear();
            }
            if (this.updateRecords.size() > 0) {
                LOGGER.debug((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.updating-{0}-features", new Object[]{new Integer(this.updateRecords.size())}));
                this.updateAll(this.updateRecords);
                this.updateRecords.clear();
            }
        }
        finally {
            this.inMemory = true;
        }
    }

    @Override
    public Record getUpdatedRecord(Record record) {
        Record recordRes = null;
        if (this.updateRecords.contains(record)) {
            Iterator iter = this.updateRecords.iterator();
            while (iter.hasNext() && recordRes == null) {
                Record element = (Record)iter.next();
                if (!element.equals(record)) continue;
                recordRes = element;
            }
        } else {
            recordRes = record;
        }
        return recordRes;
    }

    public int hashCode() {
        return 17 + this.host.hashCode() + this.port + this.dataBaseName.hashCode() + this.user.hashCode() + this.password.hashCode();
    }

    @Override
    public void executeQuery(String sql) throws SQLException {
        Connection con = null;
        Statement st = null;
        try {
            con = DataBaseConnectionFactory.getConnection(this);
            st = con.createStatement();
            st.execute(sql);
            if (!con.getAutoCommit()) {
                con.commit();
            }
        }
        finally {
            this.closeChannel(null, st);
            this.closeConnection(con);
        }
    }

    public List<Object[]> executeQuery(String sql, int n) {
        ArrayList<Object[]> results;
        block7: {
            results = new ArrayList<Object[]>();
            Connection con = null;
            Statement st = null;
            ResultSet res = null;
            try {
                try {
                    con = DataBaseConnectionFactory.getConnection(this);
                    st = con.createStatement();
                    res = st.executeQuery(sql);
                    while (res.next()) {
                        Object[] fila = new Object[n];
                        int i = 1;
                        while (i <= n) {
                            fila[i - 1] = res.getObject(i);
                            ++i;
                        }
                        results.add(fila);
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)sql, (Throwable)e);
                    this.closeChannel(res, st);
                    this.closeConnection(con);
                    break block7;
                }
            }
            catch (Throwable throwable) {
                this.closeChannel(res, st);
                this.closeConnection(con);
                throw throwable;
            }
            this.closeChannel(res, st);
            this.closeConnection(con);
        }
        return results;
    }

    public int[] executeSQLInBatch(Collection<String> sqls) throws SQLException {
        int[] results = null;
        Connection con = null;
        Statement st = null;
        try {
            try {
                con = DataBaseConnectionFactory.getConnection(this);
                st = con.createStatement();
                for (String sql : sqls) {
                    st.addBatch(sql);
                }
                results = st.executeBatch();
                con.commit();
                this.closeChannel(null, st);
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.closeChannel(null, st);
                this.rollBackConnection(con);
                throw e;
            }
        }
        finally {
            this.closeConnection(con);
        }
        return results;
    }

    public String getDataBaseSchema() {
        return this.dataBaseSchema;
    }

    public void setDataBaseSchema(String name) {
        this.dataBaseSchema = StringUtils.isEmpty((String)name) ? null : name;
        if (this.dataBaseSchema != null) {
            this.properties.put("SCHEMA", this.dataBaseSchema);
        }
    }

    public String getPkName() {
        return this.pkName;
    }

    public void setPkName(String pkName) {
        block9: {
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
                        break block9;
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
        this.properties.put("PRIMARY_KEY_COLUMN_NAME", pkName);
        if (this.schema != null) {
            if (this.schema.getPrimaryKey() != null) {
                this.schema.getPrimaryKey().setPrimaryKey(false);
            }
            Attribute attr = this.schema.getAttribute(pkName);
            attr.setPrimaryKey(true);
        }
    }

    public abstract String getFullTableName();

    public ITableIterator getIterator(String sqlWhere) {
        return this.getIterator(sqlWhere, null);
    }

    public abstract ITableIterator getIterator(String var1, String var2);

    public String getTableNameOfRelation(Relation<?> rel) {
        return this.getTableNameOfRelation(rel, true);
    }

    public String getTableNameOfRelation(Relation<?> rel, boolean fullTableName) {
        String tableName = "";
        if (rel instanceof TableRelation) {
            Table table = ((TableRelation)rel).getTable();
            if (table.getDataSource() instanceof TableDBRecordDataSource) {
                TableDBRecordDataSource tableDS = (TableDBRecordDataSource)table.getDataSource();
                if (!(this.checkHost(tableDS.getHost()) && tableDS.getPort() == this.getPort() && tableDS.getDataBaseName().equals(this.getDataBaseName()) && tableDS.getUser().equals(this.getUser()) && tableDS.getPassword().equals(this.getPassword()))) {
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
        return otherHost.equalsIgnoreCase("localhost") && this.getHost().equals("127.0.0.1") || otherHost.equals("127.0.0.1") && this.getHost().equalsIgnoreCase("localhost") || otherHost.equals(this.getHost());
    }

    public abstract String processSQLExpressionSinComillas(String var1, String var2, String var3, String var4);

    protected void updateHistoryRecord(Record record, List<Record> oldRecords) {
        Record oldRecord = this.getByPrimaryKey(new Object[]{record.getPrimaryKey()}, true).get(0);
        Timestamp fechaOp = new Timestamp(System.currentTimeMillis());
        Record copyOldRecord = (Record)oldRecord.clone();
        copyOldRecord.setAttribute(this.schema.getFieldEndDate(), (Object)fechaOp);
        copyOldRecord.setAttribute(this.schema.getHistoryField(), record.getPrimaryKey());
        copyOldRecord.setAttribute(this.schema.getPrimaryKeyName(), null);
        oldRecords.add(copyOldRecord);
        record.setAttribute(this.schema.getFieldStartDate(), (Object)fechaOp);
    }

    protected Number getNextID(Number n) {
        if (n == null) {
            return null;
        }
        Class<?> pkClass = this.schema.getPrimaryKey().getType().toJavaClass();
        if (pkClass.equals(Long.class)) {
            return new Long(n.longValue() + 1L);
        }
        if (pkClass.equals(Integer.class)) {
            return new Integer(n.intValue() + 1);
        }
        if (pkClass.equals(BigDecimal.class)) {
            return new BigDecimal(n.doubleValue() + 1.0);
        }
        if (pkClass.equals(Float.class)) {
            return new Float(n.floatValue() + 1.0f);
        }
        return new Long(n.longValue() + 1L);
    }

    protected Number getMaxID(Connection conn) {
        Number gid = null;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            Object maxPK;
            statement = conn.createStatement();
            String consultaNextGID = "SELECT MAX(" + this.escapeAttributeName(this.schema.getPrimaryKeyName()) + ") AS PK FROM " + this.getFullTableName();
            resultSet = statement.executeQuery(consultaNextGID);
            if (resultSet.next() && (maxPK = resultSet.getObject("PK")) != null && Number.class.isAssignableFrom(maxPK.getClass())) {
                gid = (Number)resultSet.getObject("PK");
            }
            if (gid == null && AttributeType.isNumeric(this.schema.getPrimaryKey().getType())) {
                gid = (Number)FeatureUtil.getGoodAttribute(this.schema.getPrimaryKey().getType(), new Long(0L));
            }
            this.closeChannel(resultSet, statement);
        }
        catch (SQLException ex) {
            LOGGER.error((Object)"", (Throwable)ex);
            this.closeChannel(resultSet, statement);
            return gid;
        }
        return gid;
    }

    public List<Record> getBySQLWithOutFilter(String sql) {
        ArrayList<Record> resultado = new ArrayList<Record>();
        String consultaSQL = String.valueOf(this.getRaizConsultaTipo()) + " WHERE " + sql;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            try {
                connection = DataBaseConnectionFactory.getConnection(this);
                statement = connection.createStatement(1003, 1007);
                resultSet = statement.executeQuery(consultaSQL);
                while (resultSet.next()) {
                    Record record = this.readRecord(resultSet);
                    resultado.add(record);
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

    public abstract String getSQLByFilter(Filter var1, String[] var2, boolean var3);

    protected String escapeAttributeName(String attrName) {
        return attrName;
    }

    public abstract String getLimitSQL(int var1, int var2);

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

    protected boolean allowTable(String tableName) {
        return true;
    }

    public abstract Object clone();

    public abstract int getDefaultPort();
}

