/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.data.DataSourceException
 *  org.geotools.data.DataStore
 *  org.geotools.data.DataStoreFactorySpi
 *  org.geotools.data.DataStoreFactorySpi$Param
 *  org.geotools.data.jdbc.ConnectionPool
 */
package org.geotools.data.oracle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.oracle.OracleConnectionFactory;
import org.geotools.data.oracle.OracleDataStore;
import org.saig.jump.lang.I18N;

public class OracleDataStoreFactory
implements DataStoreFactorySpi {
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    static final DataStoreFactorySpi.Param DBTYPE = new DataStoreFactorySpi.Param("dbtype", String.class, "This must be 'oracle'.", true, (Object)"oracle");
    static final DataStoreFactorySpi.Param HOST = new DataStoreFactorySpi.Param("host", String.class, "The host name of the server.", true);
    static final DataStoreFactorySpi.Param PORT = new DataStoreFactorySpi.Param("port", String.class, "The port oracle is running on. (Default is 1521)", true, (Object)"1521");
    static final DataStoreFactorySpi.Param USER = new DataStoreFactorySpi.Param("user", String.class, "The user name to log in with.", true);
    static final DataStoreFactorySpi.Param PASSWD = new DataStoreFactorySpi.Param("passwd", String.class, "The password.", true);
    static final DataStoreFactorySpi.Param INSTANCE = new DataStoreFactorySpi.Param("instance", String.class, "The name of the Oracle instance to connect to.", true);
    static final DataStoreFactorySpi.Param SCHEMA = new DataStoreFactorySpi.Param("schema", String.class, "The schema name to narrow down the exposed tables (must be upper case).", false){

        public Object lookUp(Map map) throws IOException {
            if (!map.containsKey(this.key)) {
                if (this.required) {
                    throw new IOException(I18N.getMessage("org.geotools.data.oracle.OracleDataStoreFactory.parameter-{0}-is-requiered-{1}-{2}", new Object[]{this.key, ": ", this.description}));
                }
                return null;
            }
            Object value = map.get(this.key);
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                String text = (String)value;
                if (text == null) {
                    return null;
                }
                if (text.equals(text.toUpperCase())) {
                    return text;
                }
                throw new IOException(I18N.getString("org.geotools.data.oracle.OracleDataStoreFactory.schema-must-be-supplied-in-uppercase"));
            }
            throw new IOException(I18N.getMessage("org.geotools.data.oracle.OracleDataStoreFactory.string-required-for-parameter-{0}-{1}-not-{2}", new Object[]{this.key, ": ", value.getClass().getName()}));
        }
    };
    static final DataStoreFactorySpi.Param NAMESPACE = new DataStoreFactorySpi.Param("namespace", String.class, "The namespace to give the DataStore", false);

    public boolean canProcess(Map params) {
        if (params != null) {
            DataStoreFactorySpi.Param[] arrayParameters = this.getParametersInfo();
            int i = 0;
            while (i < arrayParameters.length) {
                DataStoreFactorySpi.Param param = arrayParameters[i];
                if (!params.containsKey(param.key)) {
                    if (param.required) {
                        return false;
                    }
                } else {
                    Object value;
                    try {
                        value = param.lookUp(params);
                    }
                    catch (IOException e) {
                        LOGGER.warning(String.valueOf(param.key) + ":" + e);
                        return false;
                    }
                    if (value == null ? param.required : !param.type.isInstance(value)) {
                        return false;
                    }
                }
                ++i;
            }
        } else {
            return false;
        }
        return ((String)params.get("dbtype")).equalsIgnoreCase("oracle");
    }

    public DataStore createDataStore(Map params) throws IOException {
        String host = (String)HOST.lookUp(params);
        String port = (String)PORT.lookUp(params);
        String instance = (String)INSTANCE.lookUp(params);
        String user = (String)USER.lookUp(params);
        String passwd = (String)PASSWD.lookUp(params);
        String schema = (String)SCHEMA.lookUp(params);
        String dbtype = (String)DBTYPE.lookUp(params);
        if (!"oracle".equals(dbtype)) {
            throw new IOException(I18N.getString("org.geotools.data.oracle.OracleDataStoreFactory.parameter-dbtype-must-be-oracle"));
        }
        if (!this.canProcess(params)) {
            throw new IOException(I18N.getString("org.geotools.data.oracle.OracleDataStoreFactory.cannot-connect-using-provided-parameters"));
        }
        try {
            OracleConnectionFactory ocFactory = new OracleConnectionFactory(host, port, instance);
            ocFactory.setLogin(user, passwd);
            ConnectionPool pool = ocFactory.getConnectionPool();
            OracleDataStore dataStore = new OracleDataStore(pool, schema, new HashMap());
            return dataStore;
        }
        catch (SQLException ex) {
            throw new DataSourceException(I18N.getString("org.geotools.data.oracle.OracleDataStoreFactory.error-creating-oracle-datasource"), (Throwable)ex);
        }
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException(I18N.getString("org.geotools.data.oracle.OracleDataStoreFactory.oracle-cannot-create-a-new-database"));
    }

    public String getDisplayName() {
        return "Oracle";
    }

    public String getDescription() {
        return "Oracle Spatial Database";
    }

    public boolean isAvailable() {
        try {
            Class.forName(JDBC_DRIVER);
        }
        catch (ClassNotFoundException cnfe) {
            return false;
        }
        return true;
    }

    public DataStoreFactorySpi.Param[] getParametersInfo() {
        return new DataStoreFactorySpi.Param[]{DBTYPE, HOST, PORT, USER, PASSWD, INSTANCE, SCHEMA, NAMESPACE};
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}

