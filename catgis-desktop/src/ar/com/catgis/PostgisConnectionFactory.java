package ar.com.catgis;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized PostGIS DataStore creation with connection pooling.
 * Uses HikariCP to pool JDBC connections behind GeoTools DataStores.
 */
public final class PostgisConnectionFactory {

    private static final Map<String, HikariDataSource> poolCache = new ConcurrentHashMap<>();
    private static final int POOL_MAX = 5;
    private static final int POOL_MIN = 1;
    private static final byte[] POOL_KEY_SALT = createPoolKeySalt();

    private PostgisConnectionFactory() {
    }

    /**
     * Opens a GeoTools DataStore backed by a HikariCP connection pool.
     * The pool is keyed by connection fingerprint and reused across calls.
     * The caller is responsible for disposing it (store.dispose()).
     */
    public static DataStore openDataStore(PostgisConnectionInfo info) throws Exception {
        String validationMessage = PostgisErrorSupport.validateConnectionInfo(info, true);
        if (!validationMessage.isBlank()) {
            throw new IllegalArgumentException(validationMessage);
        }

        String fingerprint = poolKey(info);
        HikariDataSource ds;
        synchronized (poolCache) {
            ds = poolCache.get(fingerprint);
            if (ds == null || ds.isClosed()) {
                ds = createPool(info);
                poolCache.put(fingerprint, ds);
            }
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

        // Inject HikariCP DataSource — GeoTools reuses connections from the pool
        params.put("DataStoreFactory", "org.geotools.data.postgis.PostgisNGDataStoreFactory");
        params.put("DataSource", ds);

        DataStore store = DataStoreFinder.getDataStore(params);
        if (store == null) {
            throw new IllegalArgumentException(
                    "No se pudo crear la conexion PostGIS. Revisa host, base, usuario, clave y dependencia JDBC.");
        }
        return store;
    }

    // Session-scoped salted key: credential changes must never reuse an authenticated pool.
    // Neither the password nor its unsalted digest is retained in the cache key.
    static String poolKey(PostgisConnectionInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("Connection info required.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(POOL_KEY_SALT);
            digest.update(info.buildFingerprint().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(info.getPassword().getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private static byte[] createPoolKeySalt() {
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static HikariDataSource createPool(PostgisConnectionInfo info) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + info.getHost() + ":"
                + info.getPort() + "/" + info.getDatabase());
        config.setUsername(info.getUser());
        config.setPassword(info.getPassword());
        config.setMaximumPoolSize(POOL_MAX);
        config.setMinimumIdle(POOL_MIN);
        config.setConnectionTimeout(8000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }

    /**
     * Shut down all connection pools. Called at application exit.
     */
    public static void shutdown() {
        for (HikariDataSource ds : poolCache.values()) {
            try { ds.close(); } catch (Exception ignored) {}
        }
        poolCache.clear();
    }
}

