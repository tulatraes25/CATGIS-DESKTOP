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

public class OracleOCIDataStoreFactory
implements DataStoreFactorySpi {
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");
    static final DataStoreFactorySpi.Param DBTYPE = new DataStoreFactorySpi.Param("dbtype", String.class, "This must be 'oracle'.", true, (Object)"oracle");
    static final DataStoreFactorySpi.Param ALIAS = new DataStoreFactorySpi.Param("alias", String.class, "The alias to the oracle server, as defined in the tnsnames.ora file", true);
    static final DataStoreFactorySpi.Param PORT = new DataStoreFactorySpi.Param("port", String.class, "The port oracle is running on. (Default is 1521)", true, (Object)"1521");
    static final DataStoreFactorySpi.Param USER = new DataStoreFactorySpi.Param("user", String.class, "The user name to log in with.", true);
    static final DataStoreFactorySpi.Param PASSWD = new DataStoreFactorySpi.Param("passwd", String.class, "The password.", true);
    static final DataStoreFactorySpi.Param INSTANCE = new DataStoreFactorySpi.Param("instance", String.class, "The name of the Oracle instance to connect to.", true);
    static final DataStoreFactorySpi.Param SCHEMA = new DataStoreFactorySpi.Param("schema", String.class, "The schema name to narrow down the exposed tables (must be upper case).", false){

        public Object lookUp(Map map) throws IOException {
            if (!map.containsKey(this.key)) {
                if (this.required) {
                    throw new IOException(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleOCIDataStoreFactory.parameter-{0}-{1}-{2}-is-required-{3}")) + " " + this.key + " " + "is required" + ": " + this.description);
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
                throw new IOException(I18N.getString("org.geotools.data.oracle.OracleOCIDataStoreFactory.schema-must-be-supplied-in-uppercase"));
            }
            throw new IOException(I18N.getMessage("org.geotools.data.oracle.OracleOCIDataStoreFactory.string-required-for-parameter-{0}-{1}-not-{2}", new Object[]{this.key, ": ", value.getClass().getName()}));
        }
    };
    static final DataStoreFactorySpi.Param NAMESPACE = new DataStoreFactorySpi.Param("namespace", String.class, "The namespace to give the DataStore", false);

    public boolean canProcess(Map params) {
        return params.containsKey("dbtype") && params.get("dbtype").equals("oracle") && params.containsKey("alias") && params.containsKey("user") && params.containsKey("passwd");
    }

    public DataStore createDataStore(Map params) throws IOException {
        String alias = (String)ALIAS.lookUp(params);
        String user = (String)USER.lookUp(params);
        String passwd = (String)PASSWD.lookUp(params);
        String schema = (String)SCHEMA.lookUp(params);
        String dbtype = (String)DBTYPE.lookUp(params);
        if (!"oracle".equals(dbtype)) {
            throw new IOException(I18N.getMessage("org.geotools.data.oracle.OracleOCIDataStoreFactory.parameter-{0}-{1}-{2}-must-be-oracle", new Object[]{" '", dbtype, "' "}));
        }
        if (!this.canProcess(params)) {
            throw new IOException(I18N.getString("org.geotools.data.oracle.OracleOCIDataStoreFactory.cannot-connect-using-provided-parameters"));
        }
        try {
            LOGGER.fine(String.valueOf(I18N.getString("org.geotools.data.oracle.OracleOCIDataStoreFactory.creating-an-oci-oracle-connection-based-on-alias")) + "= " + alias);
            OracleConnectionFactory ocFactory = new OracleConnectionFactory(alias);
            ocFactory.setLogin(user, passwd);
            ConnectionPool pool = ocFactory.getConnectionPool();
            OracleDataStore dataStore = new OracleDataStore(pool, schema, new HashMap());
            return dataStore;
        }
        catch (SQLException ex) {
            throw new DataSourceException(I18N.getString("org.geotools.data.oracle.OracleOCIDataStoreFactory.error-creating-oracle-datasource"), (Throwable)ex);
        }
    }

    public DataStore createNewDataStore(Map params) throws IOException {
        throw new UnsupportedOperationException(I18N.getString("org.geotools.data.oracle.OracleOCIDataStoreFactory.oracle-cannot-create-a-new-database"));
    }

    public String getDisplayName() {
        return "Oracle (OCI)";
    }

    public String getDescription() {
        return "Oracle Spatial w/ OCI (thick) connection";
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
        return new DataStoreFactorySpi.Param[]{DBTYPE, ALIAS, USER, PASSWD, SCHEMA, NAMESPACE};
    }

    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}

