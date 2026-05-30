/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.context;

import com.vividsolutions.jump.feature.Feature;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.PostGisDataSource;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.jump.lang.I18N;

public class GenericContext {
    private static final Logger LOGGER = Logger.getLogger(GenericContext.class);
    protected static GenericContext instance;
    protected String host;
    protected String port;
    protected String user;
    protected String password;
    protected String schema;
    protected String dataBase;
    protected String dataSourceClassKey;
    protected Hashtable<TablePkKey, TableDBRecordDataSource> tablesDataSources = new Hashtable();
    protected Hashtable<String, String> tablePkHashTable = new Hashtable();
    protected Hashtable<String, IDBKeyResolver> tableNameKeyResolverMap = new Hashtable();
    protected Hashtable<String, String> internalNameToPublicName = new Hashtable();

    public AbstractJDBCDataSource getGeometryTableDataSource(String tableName, String pkName) throws SQLException {
        PostGisDataSource geom_ds = new PostGisDataSource(this.host, Integer.parseInt(this.port), this.dataBase, tableName, this.user, this.password);
        geom_ds.setDataBaseSchema(this.schema);
        ((AbstractJDBCDataSource)geom_ds).initialize(false);
        geom_ds.setPkName(pkName);
        return geom_ds;
    }

    public Connection getDirectConnection() throws SQLException {
        return this.getDirectConnection(this.dataBase);
    }

    public Connection getDirectConnection(String dataBaseName) throws SQLException {
        PostGisDataSource geom_ds = new PostGisDataSource(this.host, Integer.parseInt(this.port), dataBaseName, null, this.user, this.password);
        geom_ds.setDataBaseSchema(this.schema);
        return geom_ds.getConnection();
    }

    public AbstractJDBCDataSource getGeometryTableDataSource(String tableName) throws SQLException {
        PostGisDataSource geom_ds = new PostGisDataSource(this.host, Integer.parseInt(this.port), this.dataBase, tableName, this.user, this.password);
        geom_ds.setDataBaseSchema(this.schema);
        ((AbstractJDBCDataSource)geom_ds).initialize(false);
        geom_ds.setPkName(this.getPrimaryKey(tableName));
        return geom_ds;
    }

    public AbstractJDBCDataSource getGeometryTableDataSource(String tableName, String schemaName, String pkName) throws SQLException {
        LOGGER.warn((Object)(String.valueOf(I18N.getString("org.saig.core.context.GenericContext.Establishing-the-connection-with-the-database")) + ": " + I18N.getString("org.saig.core.context.GenericContext.Host") + ": " + this.host + ", " + I18N.getString("org.saig.core.context.GenericContext.Port") + ": " + this.port + ", " + I18N.getString("org.saig.core.context.GenericContext.Database") + ": " + this.dataBase + ", " + I18N.getString("org.saig.core.context.GenericContext.User") + ": " + this.user + ", " + I18N.getString("org.saig.core.context.GenericContext.Password") + ":" + this.password));
        PostGisDataSource geom_ds = new PostGisDataSource(this.host, Integer.parseInt(this.port), this.dataBase, this.user, this.password);
        geom_ds.setTableName(tableName);
        geom_ds.setDataBaseSchema(schemaName);
        ((AbstractJDBCDataSource)geom_ds).initialize(false);
        geom_ds.setPkName(pkName);
        return geom_ds;
    }

    public AbstractJDBCDataSource getGeometryTableDataSource(String host, int port, String dataBase, String user, String password, String tableName, String schemaName) throws SQLException {
        LOGGER.warn((Object)(String.valueOf(I18N.getString("org.saig.core.context.GenericContext.Establishing-the-connection-with-the-database")) + ": " + I18N.getString("org.saig.core.context.GenericContext.Host") + ": " + host + ", " + I18N.getString("org.saig.core.context.GenericContext.Port") + ": " + port + ", " + I18N.getString("org.saig.core.context.GenericContext.Database") + ": " + dataBase + ", " + I18N.getString("org.saig.core.context.GenericContext.User") + ": " + user + ", " + I18N.getString("org.saig.core.context.GenericContext.Password") + ":" + password));
        PostGisDataSource geom_ds = new PostGisDataSource(host, port, dataBase, user, password);
        geom_ds.setTableName(tableName);
        geom_ds.setDataBaseSchema(schemaName);
        ((AbstractJDBCDataSource)geom_ds).initialize(false);
        return geom_ds;
    }

    public String getPrimaryKey(String tableName) {
        return this.tablePkHashTable.get(tableName);
    }

    public String getPublicName(String internalName) {
        return this.internalNameToPublicName.get(internalName);
    }

    public TableDBRecordDataSource getTableDataSource(String tableName, String pkName) throws Exception {
        TablePkKey key = new TablePkKey(tableName, pkName);
        if (this.tablesDataSources.containsKey(key)) {
            return this.tablesDataSources.get(key);
        }
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("USER", this.user);
        properties.put("PASSWORD", this.password);
        properties.put("TABLE_NAME", tableName);
        properties.put("DATABASE_NAME", this.dataBase);
        properties.put("SCHEMA", this.schema);
        properties.put("HOST", this.host);
        properties.put("PORT", Integer.parseInt(this.port));
        properties.put("PRIMARY_KEY_COLUMN_NAME", pkName);
        properties.put("DATASOURCE", this.dataSourceClassKey);
        TableDBRecordDataSource ds = (TableDBRecordDataSource)TableRecordDataSource.buildTableRecordDataSourceFromProperties(properties);
        this.tablesDataSources.put(key, ds);
        return ds;
    }

    public TableDBRecordDataSource getTableDataSource(String schemaName, String tableName, String pkName) throws Exception {
        TablePkKey key = new TablePkKey(schemaName, tableName, pkName);
        if (this.tablesDataSources.containsKey(key)) {
            return this.tablesDataSources.get(key);
        }
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("USER", this.user);
        properties.put("PASSWORD", this.password);
        properties.put("TABLE_NAME", tableName);
        properties.put("DATABASE_NAME", this.dataBase);
        properties.put("SCHEMA", schemaName);
        properties.put("HOST", this.host);
        properties.put("PORT", Integer.parseInt(this.port));
        properties.put("PRIMARY_KEY_COLUMN_NAME", pkName);
        properties.put("DATASOURCE", this.dataSourceClassKey);
        TableDBRecordDataSource ds = (TableDBRecordDataSource)TableRecordDataSource.buildTableRecordDataSourceFromProperties(properties);
        this.tablesDataSources.put(key, ds);
        return ds;
    }

    public TableDBRecordDataSource getTableDataSource(String tableName) {
        try {
            return this.getTableDataSource(tableName, this.getPrimaryKey(tableName));
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public IDBKeyResolver getTableKeyResolver(String tableName) {
        if (this.tableNameKeyResolverMap.containsKey(tableName)) {
            return this.tableNameKeyResolverMap.get(tableName);
        }
        return null;
    }

    public static GenericContext getGenericContext() {
        return instance;
    }

    public Record getRecordByPK(Object key, String tableName) {
        if (key != null) {
            TableDBRecordDataSource ds = this.getTableDataSource(tableName);
            return ds.getByPrimaryKey(key);
        }
        return null;
    }

    public Feature getFeatureByPK(Object key, String layerName) {
        Feature feat = null;
        if (key != null) {
            try {
                AbstractJDBCDataSource ds = this.getGeometryTableDataSource(layerName);
                feat = ds.getByPrimaryKey(key);
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return feat;
    }

    protected class TablePkKey {
        private String tableName;
        private String pkName;
        private String schemaName;

        public TablePkKey(String tableName, String pkName) {
            this.tableName = tableName;
            this.pkName = pkName;
        }

        public TablePkKey(String schemaName, String tableName, String pkName) {
            this.tableName = tableName;
            this.pkName = pkName;
            this.schemaName = schemaName;
        }

        public String getTableName() {
            return this.tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getPkName() {
            return this.pkName;
        }

        public void setPkName(String pkName) {
            this.pkName = pkName;
        }

        public String getSchemaName() {
            return this.schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + (this.pkName == null ? 0 : this.pkName.hashCode());
            result = 31 * result + (this.schemaName == null ? 0 : this.schemaName.hashCode());
            result = 31 * result + (this.tableName == null ? 0 : this.tableName.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            TablePkKey other = (TablePkKey)obj;
            if (this.pkName == null ? other.pkName != null : !this.pkName.equals(other.pkName)) {
                return false;
            }
            if (this.schemaName == null ? other.schemaName != null : !this.schemaName.equals(other.schemaName)) {
                return false;
            }
            return !(this.tableName == null ? other.tableName != null : !this.tableName.equals(other.tableName));
        }
    }
}

