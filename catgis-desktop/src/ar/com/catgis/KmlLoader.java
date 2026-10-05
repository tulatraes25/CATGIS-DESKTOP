package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;

import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xsd.Parser;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

        String lowerName = file.getName().toLowerCase();
        if (!lowerName.endsWith(".kml") && !lowerName.endsWith(".kmz")) {
            throw new RuntimeException("El archivo no es KML/KMZ: " + file.getAbsolutePath());
        }

        Object parsed = parseKmlContent(file);

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

    private static Object parseKmlContent(File file) throws Exception {
        Parser parser = new Parser(new KMLConfiguration());
        String lowerName = file.getName().toLowerCase();
        if (lowerName.endsWith(".kmz")) {
            try (ZipFile zipFile = new ZipFile(file)) {
                ZipEntry entry = zipFile.getEntry("doc.kml");
                if (entry == null) {
                    entry = zipFile.stream()
                            .filter(item -> !item.isDirectory() && item.getName().toLowerCase().endsWith(".kml"))
                            .findFirst()
                            .orElse(null);
                }
                if (entry == null) {
                    throw new RuntimeException("El KMZ no contiene un archivo KML.");
                }
                try (InputStream input = zipFile.getInputStream(entry)) {
                    return parser.parse(input);
                }
            }
        }

        try (FileInputStream input = new FileInputStream(file)) {
            return parser.parse(input);
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
