/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource;

import es.kosmo.core.crs.CrsRepositoryManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.ConnectionPoolManager;
import org.saig.core.dao.datasource.dbdatasource.IPoolableDBDataSource;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.jump.lang.I18N;

public class DataBaseConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(DataBaseConnectionFactory.class);
    private static Hashtable<String, ConnectionPoolManager> connectionPoolMap = new Hashtable();
    private static Map<String, Hashtable<String, String>> dbConnections = new HashMap<String, Hashtable<String, String>>();
    private static Map<String, Hashtable<String, String[]>> dbPropertiesConnections = new HashMap<String, Hashtable<String, String[]>>();
    private static final int DEFAULT_MAX_CONNECTIONS = 10;

    public static synchronized Connection getConnection(AbstractJDBCDataSource dataSource) throws SQLException {
        String key = String.valueOf(dataSource.toString()) + "," + dataSource.getPassword();
        String id = dataSource.getID();
        ConnectionPoolManager connectionPoolManager = null;
        if (connectionPoolMap.containsKey(key)) {
            connectionPoolManager = connectionPoolMap.get(key);
        } else {
            Hashtable<String, Object> connectionProperties;
            IPoolableDBDataSource dbPoolable;
            if (dataSource instanceof IPoolableDBDataSource) {
                dbPoolable = (IPoolableDBDataSource)((Object)dataSource);
                connectionPoolManager = new ConnectionPoolManager(dbPoolable.createConnectionPool(), 10);
                connectionPoolMap.put(key, connectionPoolManager);
                Hashtable<String, String> connections = dbConnections.get(id);
                if (connections == null) {
                    connections = new Hashtable();
                }
                connections.put(dbPoolable.getDataBaseName(), key);
                dbConnections.put(id, connections);
                connectionProperties = dbPropertiesConnections.get(id);
                if (connectionProperties == null) {
                    connectionProperties = new Hashtable();
                }
            } else {
                try {
                    return dataSource.getConnection();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    return null;
                }
            }
            connectionProperties.put(dbPoolable.getDataBaseName(), new String[]{dataSource.getUserName(), dataSource.getPassword(), dataSource.getHostName(), Integer.toString(dataSource.getPort())});
            dbPropertiesConnections.put(id, connectionProperties);
            LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory.connection-created-for-{0}", new Object[]{dataSource.toString()}));
        }
        if (connectionPoolManager != null) {
            try {
                Connection con = connectionPoolManager.getConnection();
                return con;
            }
            catch (SQLException ex) {
                connectionPoolMap.remove(key);
                Hashtable<String, String> cons = dbConnections.get(id);
                cons.remove(key);
                Hashtable<String, String[]> conProperties = dbPropertiesConnections.get(id);
                conProperties.remove(dataSource.getDataBase());
                throw ex;
            }
        }
        return null;
    }

    public static synchronized Connection getConnection(TableDBRecordDataSource dataSource) throws SQLException {
        String key = String.valueOf(dataSource.toString()) + "," + dataSource.getPassword();
        String id = dataSource.getID();
        ConnectionPoolManager connectionPoolManager = null;
        if (connectionPoolMap.containsKey(key)) {
            connectionPoolManager = connectionPoolMap.get(key);
        } else {
            Hashtable<String, Object> connectionProperties;
            IPoolableDBDataSource dbPoolable;
            if (dataSource instanceof IPoolableDBDataSource) {
                dbPoolable = (IPoolableDBDataSource)((Object)dataSource);
                connectionPoolManager = new ConnectionPoolManager(dbPoolable.createConnectionPool(), 10);
                connectionPoolMap.put(key, connectionPoolManager);
                Hashtable<String, String> connections = dbConnections.get(id);
                if (connections == null) {
                    connections = new Hashtable();
                }
                connections.put(dbPoolable.getDataBaseName(), key);
                dbConnections.put(id, connections);
                connectionProperties = dbPropertiesConnections.get(id);
                if (connectionProperties == null) {
                    connectionProperties = new Hashtable();
                }
            } else {
                try {
                    return dataSource.getConnection();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    return null;
                }
            }
            connectionProperties.put(dbPoolable.getDataBaseName(), new String[]{dataSource.getUser(), dataSource.getPassword(), dataSource.getHost(), Integer.toString(dataSource.getPort())});
            dbPropertiesConnections.put(id, connectionProperties);
            LOGGER.warn((Object)I18N.getMessage("org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory.connection-created-for-{0}", new Object[]{dataSource.toString()}));
        }
        if (connectionPoolManager != null) {
            try {
                Connection con = connectionPoolManager.getConnection();
                return con;
            }
            catch (SQLException ex) {
                connectionPoolMap.remove(key);
                Hashtable<String, String> cons = dbConnections.get(id);
                cons.remove(key);
                Hashtable<String, String[]> conProperties = dbPropertiesConnections.get(id);
                conProperties.remove(dataSource.getDataBaseName());
                throw ex;
            }
        }
        return null;
    }

    public static String[] getAllAvaliablesConnections(String id) {
        Set<String> values = null;
        Hashtable<String, String> connections = dbConnections.get(id);
        if (connections != null) {
            values = connections.keySet();
            String[] schemas = new String[values.size()];
            values.toArray(schemas);
            return schemas;
        }
        return null;
    }

    public static String[] getPropertiesConnection(String id, String dBName) {
        if (StringUtils.isEmpty((String)dBName)) {
            return null;
        }
        Hashtable<String, String[]> connectionProperties = dbPropertiesConnections.get(id);
        if (connectionProperties != null) {
            return connectionProperties.get(dBName);
        }
        return null;
    }

    public static void clearDataBaseConnections() {
        LOGGER.info((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.DataBaseConnectionFactory.cleaning-the-connection-pool"));
        for (ConnectionPoolManager connectionPool : connectionPoolMap.values()) {
            try {
                connectionPool.dispose();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        try {
            CrsRepositoryManager.getInstance().close();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        connectionPoolMap.clear();
        dbConnections.clear();
        dbPropertiesConnections.clear();
    }
}

