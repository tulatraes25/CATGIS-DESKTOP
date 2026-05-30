/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.ExtendPostGisDataSource;
import org.saig.core.model.data.dao.TableDBRecordDataSource;

public class DataBaseDataSourceFactory {
    private static final Logger LOGGER = Logger.getLogger(DataBaseDataSourceFactory.class);
    private static Map<String, Class<? extends AbstractJDBCDataSource>> registeredLayerDatasources = new HashMap<String, Class<? extends AbstractJDBCDataSource>>();
    private static Map<String, Class<? extends TableDBRecordDataSource>> registeredTableDatasources = new HashMap<String, Class<? extends TableDBRecordDataSource>>();
    private static Map<String, Integer> datasourceToDefaultPortMap = new HashMap<String, Integer>();
    public static final String OPTIMIZED_LOADING = "optimized_loading";

    public static void registerLayerJDBCDataSource(AbstractJDBCDataSource dataSource) {
        registeredLayerDatasources.put(dataSource.getID(), dataSource.getClass());
        datasourceToDefaultPortMap.put(dataSource.getID(), dataSource.getDefaultPort());
    }

    public static void registerTableJDBCDataSource(TableDBRecordDataSource dataSource) {
        registeredTableDatasources.put(dataSource.getID(), dataSource.getClass());
        datasourceToDefaultPortMap.put(dataSource.getID(), dataSource.getDefaultPort());
    }

    public static int getDefaultPort(String datasourceId) {
        if (datasourceToDefaultPortMap.containsKey(datasourceId)) {
            return datasourceToDefaultPortMap.get(datasourceId);
        }
        return -1;
    }

    public static Collection<String> getRegisteredLayerJDBCDataSources() {
        return registeredLayerDatasources.keySet();
    }

    public static Collection<String> getRegisteredTableJDBCDataSources() {
        return registeredTableDatasources.keySet();
    }

    public static TableDBRecordDataSource createTableDataSource(String id, String host, int port, String databaseName, String userName, String password, Map<String, Object> advancedProperties) {
        Class<? extends TableDBRecordDataSource> dataSourceClass = DataBaseDataSourceFactory.getTableDataSourceClass(id, advancedProperties);
        TableDBRecordDataSource newDS = null;
        try {
            newDS = dataSourceClass.newInstance();
            newDS.setHost(host);
            newDS.setPort(port);
            newDS.setDataBaseName(databaseName);
            newDS.setUser(userName);
            newDS.setPassword(password);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return newDS;
    }

    private static Class<? extends TableDBRecordDataSource> getTableDataSourceClass(String id, Map<String, Object> advancedProperties) {
        return registeredTableDatasources.get(id);
    }

    public static AbstractJDBCDataSource createLayerDataSource(String id, String host, int port, String databaseName, String userName, String password, Map<String, Object> advancedProperties) {
        Class<? extends AbstractJDBCDataSource> dataSourceClass = DataBaseDataSourceFactory.getLayerDataSourceClass(id, advancedProperties);
        AbstractJDBCDataSource newDS = null;
        try {
            newDS = dataSourceClass.newInstance();
            newDS.setHostName(host);
            newDS.setPort(port);
            newDS.setDataBase(databaseName);
            newDS.setUserName(userName);
            newDS.setPassword(password);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return newDS;
    }

    private static Class<? extends AbstractJDBCDataSource> getLayerDataSourceClass(String id, Map<String, Object> advancedProperties) {
        Class datasourceClass = null;
        Object optimized_loading = null;
        if (advancedProperties != null) {
            optimized_loading = advancedProperties.get(OPTIMIZED_LOADING);
        }
        datasourceClass = optimized_loading != null && (Boolean)optimized_loading != false && "PostgreSQL".equals(id) ? ExtendPostGisDataSource.class : registeredLayerDatasources.get(id);
        return datasourceClass;
    }
}

