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
import java.io.IOException;
import java.util.*;

/**
 * Loader for SpatiaLite databases using GeoTools DataStore API.
 * <p>
 * SpatiaLite is a spatial extension for SQLite. Uses GeoTools'
 * spatialite/geopkg DataStore drivers when available.
 * <p>
 * Validation is strict: corrupt or unsupported files throw
 * {@link UnsupportedFormatException} with a clear user-facing message.
 * Callers should invoke {@link #validateFile(File)} before loading.
 */
public final class SpatiaLiteLoader {

    private SpatiaLiteLoader() {
    }

    // ---- Public API ----

    /**
     * Validate a SQLite / SpatiaLite database file.
     *
     * @param file the .sqlite / .db file to check
     * @return validation result with status and message
     */
    public static ValidationResult validateFile(File file) {
        if (file == null) {
            return ValidationResult.invalid("Archivo no especificado.");
        }
        if (!file.exists()) {
            return ValidationResult.invalid("El archivo no existe: " + file.getAbsolutePath());
        }

        if (file.length() < 100) {
            return ValidationResult.invalid(
                    "El archivo es demasiado pequeño para ser SQLite ("
                            + file.length() + " bytes).");
        }

        try {
            if (!isSqliteDatabase(file)) {
                return ValidationResult.invalid(
                        "El archivo no es una base de datos SQLite válida "
                                + "(encabezado incorrecto).");
            }
        } catch (IOException e) {
            CatgisLogger.warn("validateFile I/O error: " + file.getAbsolutePath(), e);
            return ValidationResult.invalid("Error de lectura: " + e.getMessage());
        }

        // Try to open as SpatiaLite or GeoPackage to detect spatial tables
        DataStore store = tryOpenSpatiaLiteStore(file);
        if (store == null) {
            store = tryOpenGeoPackageStore(file);
        }

        if (store != null) {
            try {
                String[] names = store.getTypeNames();
                int spatialCount = 0;
                StringBuilder sb = new StringBuilder();
                for (String name : names) {
                    try {
                        SimpleFeatureType schema = store.getSchema(name);
                        if (schema != null && schema.getGeometryDescriptor() != null) {
                            spatialCount++;
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(name);
                        }
                    } catch (Exception ignored) {
                        // schema lookup failed, skip this table
                    }
                }
                store.dispose();

                if (spatialCount > 0) {
                    return ValidationResult.valid(
                            "Base de datos espacial válida — " + spatialCount
                                    + " tabla(s) espacial(es): " + sb);
                }
                return ValidationResult.valid(
                        "Base de datos SQLite válida, sin tablas espaciales detectadas. "
                                + "Se requiere mod_spatialite o extensión GeoPackage.");
            } catch (Exception e) {
                CatgisLogger.warn("validateFile: error listing spatial tables", e);
                store.dispose();
            }
        }

        return ValidationResult.valid(
                "Base de datos SQLite válida. "
                        + "No se pudo confirmar soporte espacial "
                        + "(se requiere mod_spatialite o driver GeoPackage).");
    }

    /**
     * List spatial tables using GeoTools DataStore.
     *
     * @throws UnsupportedFormatException if the file is not a valid SQLite database
     */
    public static List<SpatiaLiteFeatureTypeInfo> listFeatureTypes(
            SpatiaLiteConnectionInfo info) throws UnsupportedFormatException {
        List<SpatiaLiteFeatureTypeInfo> types = new ArrayList<>();
        File dbFile = new File(info.getFilePath());

        CatgisLogger.debug("SpatiaLiteLoader.listFeatureTypes: " + dbFile.getAbsolutePath());

        if (!dbFile.exists()) {
            throw new UnsupportedFormatException("Archivo no existe: " + info.getFilePath());
        }

        try {
            if (!isSqliteDatabase(dbFile)) {
                throw new UnsupportedFormatException(
                        "El archivo no es una base de datos SQLite válida.");
            }
        } catch (IOException e) {
            throw new UnsupportedFormatException(
                    "Error al leer el archivo: " + e.getMessage(), e);
        }

        DataStore store = tryOpenSpatiaLiteStore(dbFile);
        if (store == null) {
            store = tryOpenGeoPackageStore(dbFile);
        }

        if (store != null) {
            try {
                CatgisLogger.debug("SpatiaLite DataStore opened, reading type names");
                for (String name : store.getTypeNames()) {
                    try {
                        SimpleFeatureType schema = store.getSchema(name);
                        String geomCol = schema.getGeometryDescriptor() != null
                                ? schema.getGeometryDescriptor().getLocalName() : "";
                        String geomType = schema.getGeometryDescriptor() != null
                                ? schema.getGeometryDescriptor().getType().getBinding().getSimpleName()
                                : "Geometry";
                        boolean hasGeom = schema.getGeometryDescriptor() != null;
                        types.add(new SpatiaLiteFeatureTypeInfo(
                                name, geomType, geomCol, 4326, false));
                        CatgisLogger.debug("SpatiaLite table: " + name
                                + " geom=" + geomType + "(" + geomCol + ")");
                    } catch (Exception e) {
                        CatgisLogger.warn("SpatiaLite: failed to read schema for table '"
                                + name + "'", e);
                        types.add(new SpatiaLiteFeatureTypeInfo(
                                name, "Geometry", "", 4326, false));
                    }
                }
                store.dispose();
                CatgisLogger.info("SpatiaLite: " + types.size()
                        + " table(s) found in " + dbFile.getName());
                return types;
            } catch (Exception e) {
                store.dispose();
                CatgisLogger.warn("SpatiaLite: DataStore type listing failed", e);
            }
        }

        // No usable DataStore
        CatgisLogger.warn("SpatiaLite: no DataStore available for " + dbFile.getName(), null);
        throw new UnsupportedFormatException(
                "No se pudo abrir la base de datos como SpatiaLite o GeoPackage. "
                        + "Verifique que mod_spatialite esté instalado o que el archivo "
                        + "sea un GeoPackage válido.");
    }

    /**
     * Load features from a SpatiaLite table using GeoTools DataStore.
     *
     * @throws UnsupportedFormatException if the file or table is invalid
     */
    public static ShapefileData loadLayerData(SpatiaLiteLayer layer)
            throws UnsupportedFormatException, IOException {
        if (layer == null || layer.getTableName() == null || layer.getTableName().isBlank()) {
            throw new UnsupportedFormatException("Tabla no especificada.");
        }

        File dbFile = new File(layer.getPath());

        CatgisLogger.debug("SpatiaLiteLoader.loadLayerData: " + dbFile.getAbsolutePath()
                + " table=" + layer.getTableName());

        if (!dbFile.exists()) {
            throw new UnsupportedFormatException("Archivo no existe: " + layer.getPath());
        }

        try {
            if (!isSqliteDatabase(dbFile)) {
                throw new UnsupportedFormatException(
                        "El archivo no es una base de datos SQLite válida.");
            }
        } catch (IOException e) {
            throw new UnsupportedFormatException(
                    "Error al leer el archivo: " + e.getMessage(), e);
        }

        DataStore store = tryOpenSpatiaLiteStore(dbFile);
        if (store == null) {
            store = tryOpenGeoPackageStore(dbFile);
        }

        if (store != null) {
            return loadFromStore(store, layer);
        }

        throw new UnsupportedFormatException(
                "No se pudo abrir la base de datos como SpatiaLite o GeoPackage. "
                        + "Verifique que mod_spatialite esté instalado o que el archivo "
                        + "sea un GeoPackage válido.");
    }

    // --- DataStore helpers ---

    private static DataStore tryOpenSpatiaLiteStore(File dbFile) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "spatialite");
            params.put("database", dbFile.getAbsolutePath());
            DataStore store = DataStoreFinder.getDataStore(params);
            CatgisLogger.debug("SpatiaLite DataStore opened (spatialite): " + dbFile.getName());
            return store;
        } catch (Exception e) {
            CatgisLogger.debug("SpatiaLite: spatialite DataStore not available for "
                    + dbFile.getName() + " — " + e.getMessage());
            return null;
        }
    }

    private static DataStore tryOpenGeoPackageStore(File dbFile) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("dbtype", "geopkg");
            params.put("database", dbFile.getAbsolutePath());
            DataStore store = DataStoreFinder.getDataStore(params);
            CatgisLogger.debug("SpatiaLite DataStore opened (geopkg): " + dbFile.getName());
            return store;
        } catch (Exception e) {
            CatgisLogger.debug("SpatiaLite: geopkg DataStore not available for "
                    + dbFile.getName() + " — " + e.getMessage());
            return null;
        }
    }

    private static ShapefileData loadFromStore(DataStore store, SpatiaLiteLayer layer)
            throws UnsupportedFormatException, IOException {
        try {
            String typeName = layer.getTableName();
            String[] names = store.getTypeNames();
            if (names == null || names.length == 0) {
                store.dispose();
                throw new UnsupportedFormatException(
                        "No se encontraron tablas en la base de datos.");
            }

            boolean found = false;
            for (String n : names) {
                if (n.equalsIgnoreCase(typeName)) {
                    typeName = n;
                    found = true;
                    break;
                }
            }
            if (!found) {
                CatgisLogger.warn("SpatiaLite: table '" + layer.getTableName()
                        + "' not found, using first available: " + names[0], null);
                typeName = names[0];
            }

            CatgisLogger.debug("SpatiaLite: loading features from table '" + typeName + "'");

            SimpleFeatureCollection collection =
                    store.getFeatureSource(typeName).getFeatures();
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
            if (schema.getGeometryDescriptor() != null) {
                layer.setGeometryTypeLabel(
                        schema.getGeometryDescriptor().getType().getBinding().getSimpleName());
            } else {
                layer.setGeometryTypeLabel("Geometry");
            }
            layer.setSourceName(typeName);

            store.dispose();

            CatgisLogger.info("SpatiaLite loaded: " + typeName
                    + " → " + features.size() + " features");

            return new ShapefileData(features, envelope, typeName, features.size(),
                    "SpatiaLite: " + typeName + " (" + features.size() + " features)", schema);
        } catch (UnsupportedFormatException e) {
            store.dispose();
            throw e;
        } catch (Exception e) {
            store.dispose();
            CatgisLogger.error("SpatiaLite: failed to load layer '" + layer.getTableName() + "'", e);
            throw new UnsupportedFormatException(
                    "Error al cargar la capa SpatiaLite: " + e.getMessage(), e);
        }
    }

    // --- SQLite header validation ---

    private static boolean isSqliteDatabase(File dbFile) throws IOException {
        if (dbFile.length() < 100) {
            return false;
        }
        byte[] header = new byte[16];
        try (FileInputStream fis = new FileInputStream(dbFile)) {
            int bytesRead = fis.read(header);
            if (bytesRead < 16) {
                return false;
            }
        }
        // SQLite magic: "SQLite format 3\0" (16 bytes, null-terminated)
        String magic = new String(header, 0, 16, java.nio.charset.StandardCharsets.US_ASCII);
        return magic.startsWith("SQLite format 3");
    }

}
