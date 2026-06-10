package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for SpatiaLite databases.
 * SpatiaLite is a spatial extension for SQLite.
 * This loader provides basic validation and metadata.
 * Full loading requires the mod_spatialite native extension.
 */
public final class SpatiaLiteLoader {

    private SpatiaLiteLoader() {}

    /**
     * List spatial tables in a SpatiaLite database.
     */
    public static List<SpatiaLiteFeatureTypeInfo> listFeatureTypes(SpatiaLiteConnectionInfo info) throws Exception {
        List<SpatiaLiteFeatureTypeInfo> types = new ArrayList<>();
        File dbFile = new File(info.getFilePath());
        if (!dbFile.exists()) {
            throw new Exception("Archivo no existe: " + info.getFilePath());
        }

        // Basic validation: file must be > 100 bytes (SQLite header)
        if (dbFile.length() < 100) {
            throw new Exception("Archivo demasiado pequeno para ser una base de datos SpatiaLite.");
        }

        // Read SQLite header to verify it's a valid database
        try (FileInputStream fis = new FileInputStream(dbFile)) {
            byte[] header = new byte[16];
            if (fis.read(header) < 16) {
                throw new Exception("No se pudo leer el header del archivo.");
            }
            String magic = new String(header, 0, 16).trim();
            if (!magic.startsWith("SQLite format 3")) {
                throw new Exception("El archivo no es una base de datos SQLite valida.");
            }
        }

        // Provide a placeholder entry
        types.add(new SpatiaLiteFeatureTypeInfo(
                "(carga completa requiere mod_spatialite)", "unknown", "", 0, false));

        return types;
    }

    /**
     * Load a SpatiaLite table as a ShapefileData.
     * Returns empty data with metadata for now.
     */
    public static ShapefileData loadLayerData(SpatiaLiteLayer layer) throws Exception {
        if (layer == null || layer.getTableName().isBlank()) {
            throw new IllegalArgumentException("Tabla no especificada.");
        }

        File dbFile = new File(layer.getPath());
        if (!dbFile.exists()) {
            throw new Exception("Archivo no existe: " + layer.getPath());
        }

        // Return empty data with metadata
        layer.setSourceCRS("EPSG:4326");
        layer.setGeometryTypeLabel("SpatiaLite");
        layer.setResolvedCrs("EPSG:4326");
        layer.setSourceName(layer.getTableName());

        org.geotools.feature.simple.SimpleFeatureTypeBuilder tb = new org.geotools.feature.simple.SimpleFeatureTypeBuilder();
        tb.setName(layer.getTableName());
        return new ShapefileData(
                new ArrayList<>(),
                new Envelope(),
                layer.getTableName(),
                0,
                "SpatiaLite: " + layer.getTableName() + " (carga basica - mod_spatialite requerido para datos)",
                tb.buildFeatureType());
    }
}
