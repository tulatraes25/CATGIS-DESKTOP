package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonLoader {

    public static ShapefileData load(String path) throws Exception {
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("Ruta de GeoJSON vacía.");
        }
        return load(new File(path));
    }

    public static ShapefileData loadGeoJson(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData read(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData open(String path) throws Exception {
        return load(path);
    }

    public static ShapefileData load(File file) throws Exception {
        if (file == null) {
            throw new RuntimeException("Archivo GeoJSON nulo.");
        }

        if (!file.exists()) {
            throw new RuntimeException("No existe el archivo: " + file.getAbsolutePath());
        }

        String lowerName = file.getName().toLowerCase();
        if (!lowerName.endsWith(".geojson") && !lowerName.endsWith(".json")) {
            throw new RuntimeException("El archivo no es GeoJSON: " + file.getAbsolutePath());
        }

        FeatureJSON featureJSON = new FeatureJSON();
        SimpleFeatureType detectedType = null;
        try (FileReader reader = new FileReader(file)) {
            detectedType = featureJSON.readFeatureCollectionSchema(reader, true);
        } catch (Exception ignored) {
        }
        if (detectedType != null) {
            featureJSON.setFeatureType(detectedType);
        }

        SimpleFeatureCollection featureCollection;
        try (FileReader reader = new FileReader(file)) {
            featureCollection = (SimpleFeatureCollection) featureJSON.readFeatureCollection(reader);
        }

        if (featureCollection == null) {
            throw new RuntimeException("No se pudo leer el GeoJSON: " + file.getAbsolutePath());
        }

        List<SimpleFeature> features = new ArrayList<>();
        try (FeatureIterator<SimpleFeature> iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                features.add(iterator.next());
            }
        }

        Envelope envelope = null;

        try {
            envelope = featureCollection.getBounds();
        } catch (Exception ignored) {
        }

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

        String sourceName = file.getName();
        int featureCount = features.size();
        String message = "GeoJSON cargado: " + file.getName() + " | entidades: " + featureCount;

        return new ShapefileData(features, envelope, sourceName, featureCount, message, featureCollection.getSchema());
    }

    public static ShapefileData loadGeoJson(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData read(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData open(File file) throws Exception {
        return load(file);
    }
}
