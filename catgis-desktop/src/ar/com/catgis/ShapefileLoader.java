package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ShapefileLoader {

    public static ShapefileData load(String path) throws Exception {
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("Ruta de shapefile vacía.");
        }
        return load(new File(path));
    }

    public static ShapefileData loadShapefile(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData read(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData readShapefile(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData open(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData openShapefile(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData load(File file) throws Exception {
        if (file == null) {
            throw new RuntimeException("Archivo shapefile nulo.");
        }

        if (!file.exists()) {
            throw new RuntimeException("No existe el archivo: " + file.getAbsolutePath());
        }

        if (!file.getName().toLowerCase().endsWith(".shp")) {
            throw new RuntimeException("El archivo no es un shapefile (.shp): " + file.getAbsolutePath());
        }

        ShapefileDataStore store = null;

        try {
            store = new ShapefileDataStore(file.toURI().toURL());
            store.setCharset(StandardCharsets.UTF_8);

            String typeName = store.getTypeNames()[0];
            SimpleFeatureSource featureSource = store.getFeatureSource(typeName);
            SimpleFeatureCollection featureCollection = featureSource.getFeatures();
            SimpleFeatureType schema = featureSource.getSchema();

            List<SimpleFeature> features = new ArrayList<>();
            try (FeatureIterator<SimpleFeature> iterator = featureCollection.features()) {
                while (iterator.hasNext()) {
                    features.add(iterator.next());
                }
            }

            Envelope envelope = null;

            try {
                envelope = featureCollection.getBounds();
            } catch (Exception ignored) { CatgisLogger.warn("ShapefileLoader: operation failed", ignored); }

            if ((envelope == null || envelope.isNull()) && !features.isEmpty()) {
                for (SimpleFeature feature : features) {
                    if (feature == null) {
                        continue;
                    }

                    Object geomObj = feature.getDefaultGeometry();
                    if (geomObj instanceof Geometry) {
                        Geometry geometry = (Geometry) geomObj;
                        if (!geometry.isEmpty()) {
                            if (envelope == null) {
                                envelope = new Envelope(geometry.getEnvelopeInternal());
                            } else {
                                envelope.expandToInclude(geometry.getEnvelopeInternal());
                            }
                        }
                    }
                }
            }

            String sourceName = typeName != null ? typeName : file.getName();
            int featureCount = features.size();
            String message = "Shapefile cargado: " + file.getName() + " | entidades: " + featureCount;

            return new ShapefileData(features, envelope, sourceName, featureCount, message, schema);

        } finally {
            if (store != null) {
                try {
                    store.dispose();
                } catch (Exception ignored) { CatgisLogger.warn("ShapefileLoader: operation failed", ignored); }
            }
        }
    }

    public static String getCRSCode(File file) {
        ShapefileDataStore store = null;

        try {
            store = new ShapefileDataStore(file.toURI().toURL());
            CoordinateReferenceSystem crs = store.getSchema().getCoordinateReferenceSystem();
            if (crs == null) {
                return "";
            }

            String code = CRS.lookupIdentifier(crs, true);
            return code != null ? code : "";
        } catch (Exception ex) {
            return "";
        } finally {
            if (store != null) {
                try {
                    store.dispose();
                } catch (Exception ignored) { CatgisLogger.warn("ShapefileLoader: operation failed", ignored); }
            }
        }
    }

    public static ShapefileData loadShapefile(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData read(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData readShapefile(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData open(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData openShapefile(File file) throws Exception {
        return load(file);
    }
}
