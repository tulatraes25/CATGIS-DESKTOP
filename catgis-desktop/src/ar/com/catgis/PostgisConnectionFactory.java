package ar.com.catgis;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized PostGIS DataStore creation with connection pooling.
 * Uses HikariCP to pool JDBC connections behind GeoTools DataStores.
 */
public final class PostgisConnectionFactory {

    private static final Map<String, PoolEntry> poolCache = new ConcurrentHashMap<>();
    private static final int POOL_MAX = 5;
    private static final int POOL_MIN = 1;
    private static final byte[] POOL_KEY_SALT = createPoolKeySalt();

    private record PoolEntry(String credentialKey, HikariDataSource dataSource) {
    }

    private PostgisConnectionFactory() {
    }

    /**
     * Opens a GeoTools DataStore backed by a HikariCP connection pool.
     * A credential rotation invalidates and closes the previous endpoint pool.
     * The caller is responsible for disposing the returned store.
     */
    public static DataStore openDataStore(PostgisConnectionInfo info) throws Exception {
        String validationMessage = PostgisErrorSupport.validateConnectionInfo(info, true);
        if (!validationMessage.isBlank()) {
            throw new IllegalArgumentException(validationMessage);
        }

        String endpointKey = identityDigest(info, false);
        String credentialKey = poolKey(info);
        HikariDataSource ds;
        synchronized (poolCache) {
            PoolEntry entry = poolCache.get(endpointKey);
            if (entry != null && (!credentialKey.equals(entry.credentialKey())
                    || entry.dataSource().isClosed())) {
                entry.dataSource().close();
                poolCache.remove(endpointKey);
                entry = null;
            }
            if (entry == null) {
                entry = new PoolEntry(credentialKey, createPool(info));
                poolCache.put(endpointKey, entry);
            }
            ds = entry.dataSource();
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

        // Inject HikariCP DataSource — GeoTools reuses connections from the pool.
        params.put("DataStoreFactory", "org.geotools.data.postgis.PostgisNGDataStoreFactory");
        params.put("DataSource", ds);

        DataStore store = DataStoreFinder.getDataStore(params);
        if (store == null) {
            synchronized (poolCache) {
                PoolEntry entry = poolCache.get(endpointKey);
                if (entry != null && entry.dataSource() == ds) {
                    poolCache.remove(endpointKey);
                    ds.close();
                }
            }
            throw new IllegalArgumentException(
                    "No se pudo crear la conexion PostGIS. Revisa host, base, usuario, clave y dependencia JDBC.");
        }
        return store;
    }

    // A session-scoped, salted digest, never a plaintext/unsalted credential key.
    // Database, schema and user remain case-sensitive, as in PostgreSQL.
    static String poolKey(PostgisConnectionInfo info) {
        return identityDigest(info, true);
    }

    private static String identityDigest(PostgisConnectionInfo info, boolean includePassword) {
        if (info == null) {
            throw new IllegalArgumentException("Connection info required.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(POOL_KEY_SALT);
            appendComponent(digest, info.getHost().toLowerCase(Locale.ROOT));
            appendComponent(digest, Integer.toString(info.getPort()));
            appendComponent(digest, info.getDatabase());
            appendComponent(digest, info.getSchema());
            appendComponent(digest, info.getUser());
            if (includePassword) {
                appendComponent(digest, info.getPassword());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private static void appendComponent(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
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

    /** Shut down all connection pools. Called at application exit. */
    public static void shutdown() {
        synchronized (poolCache) {
            for (PoolEntry entry : poolCache.values()) {
                try {
                    entry.dataSource().close();
                } catch (Exception ignored) {
                }
            }
            poolCache.clear();
        }
    }
}
