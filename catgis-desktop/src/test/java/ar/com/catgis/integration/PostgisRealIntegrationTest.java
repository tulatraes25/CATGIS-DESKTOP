package ar.com.catgis.integration;

import ar.com.catgis.PostgisConnectionFactory;
import ar.com.catgis.PostgisConnectionInfo;
import ar.com.catgis.PostgisFeatureTypeInfo;
import ar.com.catgis.PostgisLayer;
import ar.com.catgis.PostgisLoader;
import ar.com.catgis.PostgisTableWriteMode;
import ar.com.catgis.PostgisWriteService;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("external-postgis")
class PostgisRealIntegrationTest {

    private static final String SOURCE_TABLE = "catgis_ci_points";
    private static final String COPY_TABLE = "catgis_ci_copy";

    @BeforeAll
    static void prepareDatabase() throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcUrl(), user(), password());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS postgis");
            statement.execute("DROP TABLE IF EXISTS public." + COPY_TABLE);
            statement.execute("DROP TABLE IF EXISTS public." + SOURCE_TABLE);
            statement.execute("CREATE TABLE public." + SOURCE_TABLE
                    + " (id serial PRIMARY KEY, name text NOT NULL, geom geometry(Point,4326) NOT NULL)");
            statement.execute("INSERT INTO public." + SOURCE_TABLE + " (name, geom) VALUES "
                    + "('A', ST_SetSRID(ST_MakePoint(-67.48,-45.86),4326)),"
                    + "('B', ST_SetSRID(ST_MakePoint(-67.50,-45.85),4326))");
        }
    }

    @AfterAll
    static void cleanDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection(jdbcUrl(), user(), password());
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS public." + COPY_TABLE);
            statement.execute("DROP TABLE IF EXISTS public." + SOURCE_TABLE);
        } finally {
            PostgisConnectionFactory.shutdown();
        }
    }

    @Test
    void connectsDiscoversAndLoadsRealPostgisGeometry() throws Exception {
        PostgisConnectionInfo info = connectionInfo();

        PostgisLoader.testConnection(info);
        assertTrue(PostgisLoader.listSchemas(info).stream()
                .anyMatch(schema -> "public".equalsIgnoreCase(schema)));

        List<PostgisFeatureTypeInfo> types = PostgisLoader.listFeatureTypes(info, true);
        PostgisFeatureTypeInfo sourceType = types.stream()
                .filter(type -> SOURCE_TABLE.equalsIgnoreCase(type.getTableName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("PostGIS table not discovered: " + SOURCE_TABLE));

        assertFalse(sourceType.getGeometryTypeLabel().isBlank());
        assertTrue(sourceType.getCrsCode().isBlank()
                        || sourceType.getCrsCode().equalsIgnoreCase("EPSG:4326"),
                "Unexpected CRS: " + sourceType.getCrsCode());

        PostgisLayer layer = new PostgisLayer("CI points");
        layer.setConnectionInfo(info);
        layer.setSchemaName(sourceType.getSchemaName());
        layer.setTableName(sourceType.getTableName());
        layer.setTypeName(sourceType.getTypeName());
        layer.setReadOnly(false);

        ShapefileData data = PostgisLoader.loadLayerData(layer, info);
        assertNotNull(data);
        assertEquals(2, data.getFeatureCount());
        assertNotNull(data.getEnvelope());
        assertEquals(-67.50d, data.getEnvelope().getMinX(), 1e-6);
        assertEquals(-67.48d, data.getEnvelope().getMaxX(), 1e-6);
    }

    @Test
    void writesAndReadsBackThroughCatgisPostgisService() throws Exception {
        PostgisConnectionInfo info = connectionInfo();
        PostgisFeatureTypeInfo sourceType = PostgisLoader.listFeatureTypes(info, true).stream()
                .filter(type -> SOURCE_TABLE.equalsIgnoreCase(type.getTableName()))
                .findFirst()
                .orElseThrow();

        PostgisLayer sourcePostgisLayer = new PostgisLayer("CI source");
        sourcePostgisLayer.setConnectionInfo(info);
        sourcePostgisLayer.setSchemaName(sourceType.getSchemaName());
        sourcePostgisLayer.setTableName(sourceType.getTableName());
        sourcePostgisLayer.setTypeName(sourceType.getTypeName());
        sourcePostgisLayer.setReadOnly(false);
        ShapefileData sourceData = PostgisLoader.loadLayerData(sourcePostgisLayer, info);

        Layer sourceLayer = new Layer("CI source", "memory://postgis-ci", "VECTOR");
        sourceLayer.setSourceCRS("EPSG:4326");

        PostgisWriteService.WriteResult result = PostgisWriteService.writeLayer(
                new PostgisWriteService.WriteRequest(
                        sourceLayer,
                        sourceData,
                        info,
                        "public",
                        COPY_TABLE,
                        PostgisTableWriteMode.CREATE_NEW,
                        true
                )
        );

        assertTrue(result.createdTable());
        assertEquals(2, result.writtenFeatureCount());
        assertEquals(COPY_TABLE, result.layer().getTableName());

        try (Connection connection = DriverManager.getConnection(jdbcUrl(), user(), password());
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT count(*) FROM public." + COPY_TABLE)) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
        }

        PostgisFeatureTypeInfo copyType = PostgisLoader.listFeatureTypes(info, true).stream()
                .filter(type -> COPY_TABLE.equalsIgnoreCase(type.getTableName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Written table not discoverable"));

        PostgisLayer copyLayer = new PostgisLayer("CI copy");
        copyLayer.setConnectionInfo(info);
        copyLayer.setSchemaName(copyType.getSchemaName());
        copyLayer.setTableName(copyType.getTableName());
        copyLayer.setTypeName(copyType.getTypeName());
        copyLayer.setReadOnly(false);

        ShapefileData reloaded = PostgisLoader.loadLayerData(copyLayer, info);
        assertEquals(2, reloaded.getFeatureCount());
    }

    private static PostgisConnectionInfo connectionInfo() {
        PostgisConnectionInfo info = new PostgisConnectionInfo();
        info.setHost(host());
        info.setPort(Integer.parseInt(requiredEnv("CATGIS_TEST_POSTGIS_PORT")));
        info.setDatabase(requiredEnv("CATGIS_TEST_POSTGIS_DB"));
        info.setSchema("public");
        info.setUser(user());
        info.setPassword(password());
        info.setRememberPassword(false);
        return info;
    }

    private static String jdbcUrl() {
        return "jdbc:postgresql://" + host() + ":" + requiredEnv("CATGIS_TEST_POSTGIS_PORT")
                + "/" + requiredEnv("CATGIS_TEST_POSTGIS_DB");
    }

    private static String host() {
        return requiredEnv("CATGIS_TEST_POSTGIS_HOST");
    }

    private static String user() {
        return requiredEnv("CATGIS_TEST_POSTGIS_USER");
    }

    private static String password() {
        return requiredEnv("CATGIS_TEST_POSTGIS_PASSWORD");
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required CI environment variable: " + name);
        }
        return value;
    }
}
