/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oracle.jdbc.pool.OracleConnectionPoolDataSource
 *  org.geotools.data.jdbc.ConnectionPool
 *  org.geotools.data.jdbc.ConnectionPoolManager
 */
package org.geotools.data.oracle;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;

public class OracleConnectionFactory {
    private static final String JDBC_PATH = "jdbc:oracle:thin:@";
    private static final String OCI_PATH = "jdbc:oracle:oci:@";
    private static Map<String, OracleConnectionPoolDataSource> dataSources = new HashMap<String, OracleConnectionPoolDataSource>();
    private String dbUrl;
    private String username = "";
    private String passwd = "";

    public OracleConnectionFactory(String alias) {
        this.dbUrl = OCI_PATH + alias;
    }

    public OracleConnectionFactory(String host, String port, String instance) {
        this.dbUrl = instance.startsWith("(") ? JDBC_PATH + instance : (instance.startsWith("/") ? "jdbc:oracle:thin:@//" + host + ":" + port + instance : JDBC_PATH + host + ":" + port + ":" + instance);
    }

    public ConnectionPool getConnectionPool(String user, String pass) throws SQLException {
        String poolKey = String.valueOf(this.dbUrl) + user + pass;
        OracleConnectionPoolDataSource poolDataSource = dataSources.get(poolKey);
        if (poolDataSource == null) {
            poolDataSource = new OracleConnectionPoolDataSource();
            poolDataSource.setURL(this.dbUrl);
            poolDataSource.setUser(user);
            poolDataSource.setPassword(pass);
            Properties props = poolDataSource.getConnectionProperties();
            if (props == null) {
                props = new Properties();
            }
            props.setProperty("includeSynonyms", "true");
            poolDataSource.setConnectionProperties(props);
            dataSources.put(poolKey, poolDataSource);
        }
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        ConnectionPool connectionPool = manager.getConnectionPool((ConnectionPoolDataSource)poolDataSource);
        return connectionPool;
    }

    public ConnectionPool getConnectionPool() throws SQLException {
        return this.getConnectionPool(this.username, this.passwd);
    }

    public void setLogin(String user, String pass) {
        this.username = user;
        this.passwd = pass;
    }
}

