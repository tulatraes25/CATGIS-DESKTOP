package ar.com.catgis;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized PostGIS DataStore creation.
 * Single point for connection parameters, timeout, and pooling configuration.
 */
public final class PostgisConnectionFactory {

    private PostgisConnectionFactory() {
    }

    /**
     * Opens a new GeoTools DataStore for the given connection info.
     * The caller is responsible for disposing it (store.dispose()).
     */
    public static DataStore openDataStore(PostgisConnectionInfo info) throws Exception {
        String validationMessage = PostgisErrorSupport.validateConnectionInfo(info, true);
        if (!validationMessage.isBlank()) {
            throw new IllegalArgumentException(validationMessage);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", info.getHost());
        params.put("port", info.getPort());
        params.put("database", info.getDatabase());
        if (info.getSchema() != null && !info.getSchema().isBlank()) {
            params.put("schema", info.getSchema());
        }
        params.put("user", info.getUser());
        params.put("passwd", info.getPassword());
        params.put("validate connections", Boolean.TRUE);
        params.put("Expose primary keys", Boolean.TRUE);

        DataStore store = DataStoreFinder.getDataStore(params);
        if (store == null) {
            throw new IllegalArgumentException(
                    "No se pudo crear la conexion PostGIS. Revisa host, base, usuario, clave y dependencia JDBC.");
        }
        return store;
    }
}
