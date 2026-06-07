package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PostgisLoader {

    private PostgisLoader() {
    }

    public static void testConnection(PostgisConnectionInfo info) throws Exception {
        DataStore store = openDataStore(info);
        try {
            store.getTypeNames();
        } finally {
            store.dispose();
        }
    }

    public static List<PostgisFeatureTypeInfo> listFeatureTypes(PostgisConnectionInfo info) throws Exception {
        return listFeatureTypes(info, false);
    }

    public static List<PostgisFeatureTypeInfo> listCatserverFeatureTypes(PostgisConnectionInfo info) throws Exception {
        return listCatserverFeatureTypes(info, false);
    }

    public static List<PostgisFeatureTypeInfo> listCatserverFeatureTypes(PostgisConnectionInfo info, boolean allowGenericFallback) throws Exception {
        List<PostgisFeatureTypeInfo> curated = listFeatureTypesFromCatalogView(info);
        if (hasUsableCatserverCatalog(curated)) {
            return curated;
        }
        String requestedSchema = safeTrim(info != null ? info.getSchema() : "");
        List<PostgisFeatureTypeInfo> schemaSpatialLayers = listFeatureTypesViaJdbc(info, requestedSchema);
        if (!schemaSpatialLayers.isEmpty()) {
            return schemaSpatialLayers;
        }
        if (!requestedSchema.isBlank()) {
            List<PostgisFeatureTypeInfo> allSpatialLayers = listFeatureTypesViaJdbc(info, "");
            if (!allSpatialLayers.isEmpty()) {
                return allSpatialLayers;
            }
        }
        if (allowGenericFallback) {
            try {
                List<PostgisFeatureTypeInfo> generic = listFeatureTypes(info, true);
                if (!generic.isEmpty()) {
                    return generic;
                }
            } catch (Exception ignored) {
            }
        }
        return List.of();
    }

    public static List<PostgisFeatureTypeInfo> listFeatureTypes(PostgisConnectionInfo info, boolean allowCrossSchemaFallback) throws Exception {
        DataStore store = openDataStore(info);
        try {
            LinkedHashMap<String, PostgisFeatureTypeInfo> layers = new LinkedHashMap<>();
            String[] typeNames = store.getTypeNames();
            if (typeNames != null) {
                for (String typeName : typeNames) {
                    if (typeName == null || typeName.isBlank()) {
                        continue;
                    }
                    SimpleFeatureSource source = store.getFeatureSource(typeName);
                    SimpleFeatureType schema = source.getSchema();
                    String resolvedSchema = resolveSchemaName(typeName, info.getSchema());
                    String tableName = resolveTableName(typeName);
                    String geometryLabel = schema != null && schema.getGeometryDescriptor() != null
                            ? VectorLayerUtils.describeGeometryBinding(schema.getGeometryDescriptor().getType().getBinding())
                            : "";
                    String crsCode = resolveSchemaCrs(schema);
                    boolean writable = source instanceof SimpleFeatureStore;
                    PostgisFeatureTypeInfo entry = new PostgisFeatureTypeInfo(typeName, resolvedSchema, tableName, geometryLabel, crsCode, writable);
                    layers.put(buildLayerKey(entry), entry);
                }
            }

            if (layers.isEmpty()) {
                for (PostgisFeatureTypeInfo fallbackEntry : listFeatureTypesViaJdbc(info, info.getSchema())) {
                    layers.put(buildLayerKey(fallbackEntry), fallbackEntry);
                }
            }
            if (layers.isEmpty() && allowCrossSchemaFallback) {
                String requestedSchema = safeTrim(info.getSchema());
                if (!requestedSchema.isBlank()) {
                    for (PostgisFeatureTypeInfo fallbackEntry : listFeatureTypesViaJdbc(info, "")) {
                        layers.put(buildLayerKey(fallbackEntry), fallbackEntry);
                    }
                }
            }
            return new ArrayList<>(layers.values());
        } finally {
            store.dispose();
        }
    }

    public static List<String> listSchemas(PostgisConnectionInfo info) throws Exception {
        List<String> schemas = new ArrayList<>();
        String sql = """
                select distinct schema_name
                from (
                    select g.f_table_schema as schema_name
                    from public.geometry_columns g
                    union
                    select c.table_schema as schema_name
                    from information_schema.columns c
                    where c.udt_name in ('geometry', 'geography')
                ) x
                where schema_name is not null
                    and schema_name <> ''
                    and schema_name not in ('pg_catalog', 'information_schema')
                order by schema_name
                """;

        try (Connection connection = openJdbcConnection(info);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            applyQueryTimeout(statement);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String schema = safeTrim(rs.getString(1));
                    if (!schema.isBlank()) {
                        schemas.add(schema);
                    }
                }
            }
        }
        return schemas;
    }

    public static ShapefileData loadLayerData(PostgisLayer layer) throws Exception {
        if (layer == null) {
            return null;
        }
        PostgisConnectionInfo info = PostgisConnectionStore.applyStoredPassword(layer.toConnectionInfo());
        if (info == null || info.getPassword().isBlank()) {
            throw new IllegalArgumentException("No hay una clave PostGIS disponible para reconstruir esta capa. Volve a conectar o guardarla localmente.");
        }
        return loadLayerData(layer, info);
    }

    public static ShapefileData loadLayerData(PostgisLayer layer, PostgisConnectionInfo connectionInfo) throws Exception {
        if (layer == null) {
            return null;
        }
        DataStore store = openDataStore(connectionInfo);
        try {
            String typeName = layer.getTypeName();
            if (typeName == null || typeName.isBlank()) {
                throw new IllegalArgumentException("No se definio la tabla espacial PostGIS asociada a la capa.");
            }

            SimpleFeatureSource source = resolveFeatureSource(store, layer, connectionInfo);
            String queryTypeName = source.getSchema() != null && source.getSchema().getTypeName() != null
                    ? source.getSchema().getTypeName()
                    : typeName;
            Query query = new Query(queryTypeName);
            org.geotools.data.simple.SimpleFeatureCollection featureCollection = source.getFeatures(query);
            SimpleFeatureType schema = featureCollection.getSchema();
            List<SimpleFeature> features = new ArrayList<>();
            Envelope envelope = null;

            try {
                ReferencedEnvelope bounds = featureCollection.getBounds();
                if (bounds != null && !bounds.isEmpty()) {
                    envelope = new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
                }
            } catch (Exception ignored) {
            }

            try (FeatureIterator<SimpleFeature> it = featureCollection.features()) {
                while (it.hasNext()) {
                    SimpleFeature feature = it.next();
                    if (feature == null) {
                        continue;
                    }
                    SimpleFeature detached = SimpleFeatureBuilder.copy(feature);
                    features.add(detached);
                    if (envelope == null) {
                        Object geomObj = detached.getDefaultGeometry();
                        if (geomObj instanceof Geometry geometry && !geometry.isEmpty()) {
                            envelope = new Envelope(geometry.getEnvelopeInternal());
                        }
                    }
                }
            }

            if (layer.getSourceCRS() == null || layer.getSourceCRS().isBlank()) {
                layer.setSourceCRS(resolveSchemaCrs(schema));
            }
            if (layer.getGeometryTypeLabel() == null || layer.getGeometryTypeLabel().isBlank()) {
                layer.setGeometryTypeLabel(schema != null && schema.getGeometryDescriptor() != null
                        ? VectorLayerUtils.describeGeometryBinding(schema.getGeometryDescriptor().getType().getBinding())
                        : "");
            }
            if (layer.getTableName() == null || layer.getTableName().isBlank()) {
                layer.setTableName(resolveTableName(typeName));
            }
            if (layer.getSchemaName() == null || layer.getSchemaName().isBlank()) {
                layer.setSchemaName(resolveSchemaName(typeName, connectionInfo.getSchema()));
            }
            if (layer.getSourceName() == null || layer.getSourceName().isBlank()) {
                layer.setSourceName(connectionInfo.getDatabase());
            }

            VectorLayerUtils.populateFieldConfigs(layer, schema);

            ShapefileData data = new ShapefileData(
                    features,
                    envelope,
                    connectionInfo.getDatabase() + " :: " + typeName,
                    features.size(),
                    layer.isReadOnly()
                            ? "PostGIS cargado correctamente en modo solo lectura."
                            : "PostGIS cargado correctamente en modo editable.",
                    schema
            );
            CatserverLayerStyleSupport.applyIfNeeded(layer, data);
            return data;
        } finally {
            store.dispose();
        }
    }

    private static List<PostgisFeatureTypeInfo> listFeatureTypesViaJdbc(PostgisConnectionInfo info, String schemaFilter) throws Exception {
        LinkedHashMap<String, PostgisFeatureTypeInfo> layers = new LinkedHashMap<>();
        String geometryColumnsSql = """
                select
                    g.f_table_schema,
                    g.f_table_name,
                    coalesce(nullif(g.type, ''), 'GEOMETRY') as geometry_type,
                    case when g.srid > 0 then 'EPSG:' || g.srid else '' end as crs_code,
                    has_table_privilege(format('%I.%I', g.f_table_schema, g.f_table_name), 'INSERT')
                        and has_table_privilege(format('%I.%I', g.f_table_schema, g.f_table_name), 'UPDATE')
                        and has_table_privilege(format('%I.%I', g.f_table_schema, g.f_table_name), 'DELETE') as writable
                from public.geometry_columns g
                where (? is null or ? = '' or g.f_table_schema = ?)
                order by g.f_table_schema, g.f_table_name
                """;
        String informationSchemaSql = """
                select
                    c.table_schema,
                    c.table_name,
                    upper(coalesce(nullif(c.udt_name, ''), 'GEOMETRY')) as geometry_type,
                    has_table_privilege(format('%I.%I', c.table_schema, c.table_name), 'INSERT')
                        and has_table_privilege(format('%I.%I', c.table_schema, c.table_name), 'UPDATE')
                        and has_table_privilege(format('%I.%I', c.table_schema, c.table_name), 'DELETE') as writable
                from information_schema.columns c
                where c.udt_name in ('geometry', 'geography')
                    and c.table_schema not in ('pg_catalog', 'information_schema')
                    and (? is null or ? = '' or c.table_schema = ?)
                order by c.table_schema, c.table_name
                """;

        try (Connection connection = openJdbcConnection(info);
             PreparedStatement statement = connection.prepareStatement(geometryColumnsSql)) {
            String schema = schemaFilter != null ? schemaFilter.trim() : "";
            statement.setString(1, schema);
            statement.setString(2, schema);
            statement.setString(3, schema);
            applyQueryTimeout(statement);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String schemaName = safeTrim(rs.getString(1));
                    String tableName = safeTrim(rs.getString(2));
                    String geometryType = safeTrim(rs.getString(3));
                    String crsCode = safeTrim(rs.getString(4));
                    boolean writable = rs.getBoolean(5);
                    String typeName = !schemaName.isBlank() ? schemaName + "." + tableName : tableName;
                    PostgisFeatureTypeInfo entry = new PostgisFeatureTypeInfo(typeName, schemaName, tableName, geometryType, crsCode, writable);
                    layers.put(buildLayerKey(entry), entry);
                }
            }

            try (PreparedStatement fallbackStatement = connection.prepareStatement(informationSchemaSql)) {
                String fallbackSchema = schemaFilter != null ? schemaFilter.trim() : "";
                fallbackStatement.setString(1, fallbackSchema);
                fallbackStatement.setString(2, fallbackSchema);
                fallbackStatement.setString(3, fallbackSchema);
                applyQueryTimeout(fallbackStatement);

                try (ResultSet rs = fallbackStatement.executeQuery()) {
                    while (rs.next()) {
                        String schemaName = safeTrim(rs.getString(1));
                        String tableName = safeTrim(rs.getString(2));
                        String geometryType = safeTrim(rs.getString(3));
                        boolean writable = rs.getBoolean(4);
                        String typeName = !schemaName.isBlank() ? schemaName + "." + tableName : tableName;
                        PostgisFeatureTypeInfo entry = new PostgisFeatureTypeInfo(typeName, schemaName, tableName, geometryType, "", writable);
                        layers.putIfAbsent(buildLayerKey(entry), entry);
                    }
                }
            }
        }

        return new ArrayList<>(layers.values());
    }

    private static void applyQueryTimeout(PreparedStatement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.setQueryTimeout(20);
        } catch (Exception ignored) {
        }
    }

    private static List<PostgisFeatureTypeInfo> listFeatureTypesFromCatalogView(PostgisConnectionInfo info) throws Exception {
        List<PostgisFeatureTypeInfo> layers = new ArrayList<>();
        String sql = """
                select
                    c.schema_name,
                    c.table_name,
                    coalesce(c.display_name, c.table_name) as display_name,
                    coalesce(c.geometry_type, 'GEOMETRY') as geometry_type,
                    coalesce(c.crs_code, '') as crs_code,
                    coalesce(c.writable, false) as writable,
                    coalesce(c.load_default, true) as load_default
                from public.v_catserver_layers c
                order by c.load_order, c.schema_name, c.table_name
                """;

        try (Connection connection = openJdbcConnection(info);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            applyQueryTimeout(statement);
            try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String schemaName = safeTrim(rs.getString(1));
                String tableName = safeTrim(rs.getString(2));
                String displayName = safeTrim(rs.getString(3));
                String geometryType = safeTrim(rs.getString(4));
                String crsCode = safeTrim(rs.getString(5));
                boolean writable = rs.getBoolean(6);
                boolean loadByDefault = rs.getBoolean(7);
                String typeName = !schemaName.isBlank() ? schemaName + "." + tableName : tableName;
                layers.add(new PostgisFeatureTypeInfo(
                        typeName,
                        schemaName,
                        tableName,
                        displayName,
                        geometryType,
                        crsCode,
                        writable,
                        loadByDefault
                ));
            }
            }
        } catch (Exception ex) {
            String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
            if (message.contains("v_catserver_layers")
                    || message.contains("does not exist")
                    || message.contains("no existe")
                    || message.contains("permission denied")
                    || message.contains("permiso denegado")) {
                return layers;
            }
            return layers;
        }

        return layers;
    }

    private static boolean hasUsableCatserverCatalog(List<PostgisFeatureTypeInfo> layers) {
        if (layers == null || layers.isEmpty()) {
            return false;
        }
        for (PostgisFeatureTypeInfo layer : layers) {
            if (layer == null) {
                continue;
            }
            String table = safeTrim(layer.getTableName());
            if (!table.toLowerCase().startsWith("v_catserver_layers")) {
                return true;
            }
        }
        return false;
    }

    private static Connection openJdbcConnection(PostgisConnectionInfo info) throws Exception {
        Class.forName("org.postgresql.Driver");
        DriverManager.setLoginTimeout(8);
        String url = "jdbc:postgresql://" + info.getHost() + ":" + info.getPort() + "/" + info.getDatabase()
                + "?connectTimeout=8&socketTimeout=20&tcpKeepAlive=true";
        return DriverManager.getConnection(url, info.getUser(), info.getPassword());
    }

    private static String buildLayerKey(PostgisFeatureTypeInfo info) {
        return info.getSchemaName() + "|" + info.getTableName() + "|" + info.getTypeName();
    }

    private static String safeTrim(String value) {
        return value != null ? value.trim() : "";
    }

    static DataStore openDataStore(PostgisConnectionInfo info) throws Exception {
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
            throw new IllegalArgumentException("No se pudo crear la conexion PostGIS. Revisa host, base, usuario, clave y dependencia JDBC.");
        }
        return store;
    }

    private static String resolveSchemaCrs(SimpleFeatureType schema) {
        if (schema == null || schema.getCoordinateReferenceSystem() == null) {
            return "";
        }
        try {
            return CRSDefinitions.normalizeCode(CRS.toSRS(schema.getCoordinateReferenceSystem(), true));
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String resolveSchemaName(String typeName, String fallbackSchema) {
        if (typeName != null) {
            int idx = typeName.indexOf('.');
            if (idx > 0) {
                return typeName.substring(0, idx);
            }
        }
        return fallbackSchema != null && !fallbackSchema.isBlank() ? fallbackSchema : "public";
    }

    private static String resolveTableName(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return "";
        }
        int idx = typeName.lastIndexOf('.');
        return idx >= 0 ? typeName.substring(idx + 1) : typeName;
    }

    private static SimpleFeatureSource resolveFeatureSource(DataStore store, PostgisLayer layer, PostgisConnectionInfo info) throws Exception {
        List<String> candidates = new ArrayList<>();
        String typeName = layer.getTypeName();
        if (typeName != null && !typeName.isBlank()) {
            candidates.add(typeName);
        }
        if (layer.getSchemaName() != null && !layer.getSchemaName().isBlank() && layer.getTableName() != null && !layer.getTableName().isBlank()) {
            candidates.add(layer.getSchemaName() + "." + layer.getTableName());
        }
        if (info.getSchema() != null && !info.getSchema().isBlank() && layer.getTableName() != null && !layer.getTableName().isBlank()) {
            candidates.add(info.getSchema() + "." + layer.getTableName());
        }
        if (layer.getTableName() != null && !layer.getTableName().isBlank()) {
            candidates.add(layer.getTableName());
        }

        Exception last = null;
        for (String candidate : candidates) {
            try {
                return store.getFeatureSource(candidate);
            } catch (Exception ex) {
                last = ex;
            }
        }
        if (last != null) {
            throw last;
        }
        throw new IllegalArgumentException("No se pudo resolver la tabla espacial PostGIS para la capa.");
    }
}
