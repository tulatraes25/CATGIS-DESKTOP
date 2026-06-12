package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Loader for SpatiaLite databases using GeoTools DataStore API.
 * <p>
 * SpatiaLite is a spatial extension for SQLite. Uses GeoTools'
 * spatialite/geopkg DataStore drivers when available, with
 * fallback to basic SQLite header validation.
 * </p>
 */
public final class SpatiaLiteLoader {

    private SpatiaLiteLoader() {}

    /**
     * List spatial tables using GeoTools DataStore or header validation.
     */
    public static List<SpatiaLiteFeatureTypeInfo> listFeatureTypes(
            SpatiaLiteConnectionInfo info) throws Exception {
        List<SpatiaLiteFeatureTypeInfo> types = new ArrayList<>();
        File dbFile = new File(info.getFilePath());
        if (!dbFile.exists()) {
            throw new Exception("Archivo no existe: " + info.getFilePath());
        }

        if (!isSqliteDatabase(dbFile)) {
            throw new Exception("El archivo no es una base de datos SQLite valida.");
        }

        // Try GeoTools SpatiaLite DataStore
        DataStore store = openSpatiaLiteStore(dbFile);
        if (store == null) store = openGeoPackageStore(dbFile);

        if (store != null) {
            try {
                for (String name : store.getTypeNames()) {
                    try {
                        SimpleFeatureType schema = store.getSchema(name);
                        String geomCol = schema.getGeometryDescriptor() != null
                                ? schema.getGeometryDescriptor().getLocalName() : "";
                        String geomType = schema.getGeometryDescriptor() != null
                                ? schema.getGeometryDescriptor().getType().getBinding().getSimpleName()
                                : "Geometry";
                        types.add(new SpatiaLiteFeatureTypeInfo(
                                name, geomType, geomCol, 4326, false));
                    } catch (Exception ignored) {
                        types.add(new SpatiaLiteFeatureTypeInfo(
                                name, "Geometry", "", 4326, false));
                    }
                }
                store.dispose();
                return types;
            } catch (Exception e) {
                store.dispose();
            }
        }

        // Fallback placeholder
        if (types.isEmpty()) {
            types.add(new SpatiaLiteFeatureTypeInfo(
                    "(se requiere mod_spatialite o driver JDBC)",
                    "unknown", "", 0, false));
        }
        return types;
    }

    /**
     * Load features from a SpatiaLite table using GeoTools DataStore.
     */
    public static ShapefileData loadLayerData(SpatiaLiteLayer layer) throws Exception {
        if (layer == null || layer.getTableName() == null || layer.getTableName().isBlank()) {
            throw new IllegalArgumentException("Tabla no especificada.");
        }

        File dbFile = new File(layer.getPath());
        if (!dbFile.exists()) {
            throw new Exception("Archivo no existe: " + layer.getPath());
        }

        DataStore store = openSpatiaLiteStore(dbFile);
        if (store == null) store = openGeoPackageStore(dbFile);

        if (store != null) {
            return loadFromStore(store, layer);
        }

        // Fallback: empty metadata
        layer.setSourceCRS("EPSG:4326");
        layer.setGeometryTypeLabel("SpatiaLite");
        layer.setResolvedCrs("EPSG:4326");
        layer.setSourceName(layer.getTableName());

        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName(layer.getTableName());
        return new ShapefileData(
                new ArrayList<>(), new Envelope(), layer.getTableName(), 0,
                "SpatiaLite: " + layer.getTableName()
                        + " (se requiere mod_spatialite o driver JDBC para datos)",
                tb.buildFeatureType());
    }

    // --- DataStore helpers ---

    private static DataStore openSpatiaLiteStore(File dbFile) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "spatialite");
            params.put("database", dbFile.getAbsolutePath());
            return DataStoreFinder.getDataStore(params);
        } catch (Exception e) {
            return null;
        }
    }

    private static DataStore openGeoPackageStore(File dbFile) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "geopkg");
            params.put("database", dbFile.getAbsolutePath());
            return DataStoreFinder.getDataStore(params);
        } catch (Exception e) {
            return null;
        }
    }

    private static ShapefileData loadFromStore(DataStore store, SpatiaLiteLayer layer)
            throws Exception {
        try {
            String typeName = layer.getTableName();
            String[] names = store.getTypeNames();
            if (names.length == 0) {
                store.dispose();
                throw new Exception("No se encontraron tablas en la base de datos.");
            }

            // Match table name or use first
            boolean found = false;
            for (String n : names) {
                if (n.equalsIgnoreCase(typeName)) { typeName = n; found = true; break; }
            }
            if (!found) typeName = names[0];

            SimpleFeatureCollection collection = store.getFeatureSource(typeName).getFeatures();
            List<SimpleFeature> features = new ArrayList<>();
            Envelope envelope = new Envelope();

            try (SimpleFeatureIterator it = collection.features()) {
                while (it.hasNext()) {
                    SimpleFeature sf = it.next();
                    features.add(sf);
                    Geometry g = (Geometry) sf.getDefaultGeometry();
                    if (g != null && !g.isEmpty()) {
                        envelope.expandToInclude(g.getEnvelopeInternal());
                    }
                }
            }

            SimpleFeatureType schema = collection.getSchema();

            layer.setSourceCRS("EPSG:4326");
            layer.setGeometryTypeLabel(
                    schema.getGeometryDescriptor() != null
                            ? schema.getGeometryDescriptor().getType().getBinding().getSimpleName()
                            : "Geometry");
            layer.setSourceName(typeName);

            store.dispose();
            return new ShapefileData(features, envelope, typeName, features.size(),
                    "SpatiaLite: " + typeName + " (" + features.size() + " features)", schema);
        } catch (Exception e) {
            store.dispose();
            throw e;
        }
    }

    private static boolean isSqliteDatabase(File dbFile) {
        if (dbFile.length() < 100) return false;
        try (FileInputStream fis = new FileInputStream(dbFile)) {
            byte[] header = new byte[16];
            if (fis.read(header) < 16) return false;
            return new String(header, 0, 16).trim().startsWith("SQLite format 3");
        } catch (Exception e) {
            return false;
        }
    }
}
