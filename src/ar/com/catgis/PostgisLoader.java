package ar.com.catgis;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.Query;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
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
        DataStore store = openDataStore(info);
        try {
            List<PostgisFeatureTypeInfo> layers = new ArrayList<>();
            String[] typeNames = store.getTypeNames();
            if (typeNames == null) {
                return layers;
            }

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
                layers.add(new PostgisFeatureTypeInfo(typeName, resolvedSchema, tableName, geometryLabel, crsCode));
            }
            return layers;
        } finally {
            store.dispose();
        }
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

            SimpleFeatureSource source = store.getFeatureSource(typeName);
            Query query = new Query(typeName);
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

            return new ShapefileData(
                    features,
                    envelope,
                    connectionInfo.getDatabase() + " :: " + typeName,
                    features.size(),
                    "PostGIS cargado correctamente en modo lectura.",
                    schema
            );
        } finally {
            store.dispose();
        }
    }

    private static DataStore openDataStore(PostgisConnectionInfo info) throws Exception {
        String validationMessage = PostgisErrorSupport.validateConnectionInfo(info, true);
        if (!validationMessage.isBlank()) {
            throw new IllegalArgumentException(validationMessage);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("host", info.getHost());
        params.put("port", info.getPort());
        params.put("database", info.getDatabase());
        params.put("schema", info.getSchema());
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
}
