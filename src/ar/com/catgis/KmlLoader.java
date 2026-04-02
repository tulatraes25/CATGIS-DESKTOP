package ar.com.catgis;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xsd.Parser;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class KmlLoader {

    public static ShapefileData load(String path) throws Exception {
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("Ruta de KML vacía.");
        }
        return load(new File(path));
    }

    public static ShapefileData loadKml(String path) throws Exception {
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
            throw new RuntimeException("Archivo KML nulo.");
        }

        if (!file.exists()) {
            throw new RuntimeException("No existe el archivo: " + file.getAbsolutePath());
        }

        if (!file.getName().toLowerCase().endsWith(".kml")) {
            throw new RuntimeException("El archivo no es KML: " + file.getAbsolutePath());
        }

        Parser parser = new Parser(new KMLConfiguration());
        Object parsed;

        try (FileInputStream input = new FileInputStream(file)) {
            parsed = parser.parse(input);
        }

        List<SimpleFeature> features = new ArrayList<>();
        collectSimpleFeatures(parsed, features);

        Envelope envelope = null;

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

        String sourceName = file.getName();
        int featureCount = features.size();
        String message = "KML cargado: " + file.getName() + " | entidades: " + featureCount;

        SimpleFeatureType schema = !features.isEmpty() ? features.get(0).getFeatureType() : null;
        return new ShapefileData(features, envelope, sourceName, featureCount, message, schema);
    }

    private static void collectSimpleFeatures(Object obj, List<SimpleFeature> features) {
        if (obj == null) {
            return;
        }

        if (obj instanceof SimpleFeature) {
            SimpleFeature feature = (SimpleFeature) obj;

            Object geomObj = feature.getDefaultGeometry();
            if (geomObj instanceof Geometry) {
                Geometry geometry = (Geometry) geomObj;
                if (!geometry.isEmpty()) {
                    features.add(feature);
                }
            }

            for (Property property : feature.getProperties()) {
                Object value = property.getValue();
                if (value != null && value != obj) {
                    collectSimpleFeatures(value, features);
                }
            }
            return;
        }

        if (obj instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) obj) {
                collectSimpleFeatures(item, features);
            }
        }
    }

    public static ShapefileData loadKml(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData read(File file) throws Exception {
        return load(file);
    }

    public static ShapefileData open(File file) throws Exception {
        return load(file);
    }
}
