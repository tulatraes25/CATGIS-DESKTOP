package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class GpxLoader {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private GpxLoader() {
    }

    public static GpxImportResult load(File file) throws Exception {
        Document document = parse(file);
        String baseName = stripExtension(file.getName());
        return new GpxImportResult(
                buildWaypoints(document, file.getName(), baseName),
                buildTracks(document, file.getName(), baseName),
                buildRoutes(document, file.getName(), baseName)
        );
    }

    public static ShapefileData load(File file, GpxLayer.ContentKind kind) throws Exception {
        return load(file).get(kind);
    }

    private static Document parse(File file) throws Exception {
        if (file == null) {
            throw new RuntimeException("Archivo GPX nulo.");
        }
        if (!file.exists()) {
            throw new RuntimeException("No existe el archivo: " + file.getAbsolutePath());
        }
        if (!file.getName().toLowerCase().endsWith(".gpx")) {
            throw new RuntimeException("El archivo no es GPX: " + file.getAbsolutePath());
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (Exception ignored) {
        }
        return factory.newDocumentBuilder().parse(file);
    }

    private static ShapefileData buildWaypoints(Document document, String sourceName, String baseName) {
        SimpleFeatureType schema = buildWaypointSchema();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        List<SimpleFeature> features = new ArrayList<>();

        Element root = document != null ? document.getDocumentElement() : null;
        if (root != null) {
            List<Element> waypoints = directChildren(root, "wpt");
            int index = 1;
            for (Element waypoint : waypoints) {
                Double lat = parseDouble(waypoint.getAttribute("lat"));
                Double lon = parseDouble(waypoint.getAttribute("lon"));
                if (lat == null || lon == null) {
                    continue;
                }
                Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
                builder.add(point);
                builder.add(textOfChild(waypoint, "name"));
                builder.add(parseDouble(textOfChild(waypoint, "ele")));
                builder.add(textOfChild(waypoint, "time"));
                builder.add(textOfChild(waypoint, "sym"));
                builder.add(textOfChild(waypoint, "type"));
                builder.add(textOfChild(waypoint, "desc"));
                features.add(builder.buildFeature("gpx-wpt-" + index++));
                builder.reset();
            }
        }

        return new ShapefileData(
                features,
                computeEnvelope(features),
                sourceName,
                features.size(),
                "GPX cargado | waypoints: " + features.size(),
                schema
        );
    }

    private static ShapefileData buildTracks(Document document, String sourceName, String baseName) {
        SimpleFeatureType schema = buildTrackSchema();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        List<SimpleFeature> features = new ArrayList<>();

        Element root = document != null ? document.getDocumentElement() : null;
        if (root != null) {
            List<Element> tracks = directChildren(root, "trk");
            int index = 1;
            for (Element track : tracks) {
                List<LineString> segments = new ArrayList<>();
                for (Element segment : directChildren(track, "trkseg")) {
                    List<Coordinate> coordinates = new ArrayList<>();
                    for (Element point : directChildren(segment, "trkpt")) {
                        Double lat = parseDouble(point.getAttribute("lat"));
                        Double lon = parseDouble(point.getAttribute("lon"));
                        if (lat != null && lon != null) {
                            coordinates.add(new Coordinate(lon, lat));
                        }
                    }
                    if (coordinates.size() >= 2) {
                        segments.add(GEOMETRY_FACTORY.createLineString(coordinates.toArray(new Coordinate[0])));
                    }
                }

                if (segments.isEmpty()) {
                    continue;
                }

                MultiLineString geometry = GEOMETRY_FACTORY.createMultiLineString(segments.toArray(new LineString[0]));
                builder.add(geometry);
                builder.add(valueOrFallback(textOfChild(track, "name"), baseName + " track " + index));
                builder.add(parseDouble(firstTrackPointValue(track, "ele")));
                builder.add(textOfChild(track, "type"));
                builder.add(textOfChild(track, "desc"));
                builder.add(segments.size());
                features.add(builder.buildFeature("gpx-trk-" + index++));
                builder.reset();
            }
        }

        return new ShapefileData(
                features,
                computeEnvelope(features),
                sourceName,
                features.size(),
                "GPX cargado | tracks: " + features.size(),
                schema
        );
    }

    private static ShapefileData buildRoutes(Document document, String sourceName, String baseName) {
        SimpleFeatureType schema = buildRouteSchema();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        List<SimpleFeature> features = new ArrayList<>();

        Element root = document != null ? document.getDocumentElement() : null;
        if (root != null) {
            List<Element> routes = directChildren(root, "rte");
            int index = 1;
            for (Element route : routes) {
                List<Coordinate> coordinates = new ArrayList<>();
                for (Element point : directChildren(route, "rtept")) {
                    Double lat = parseDouble(point.getAttribute("lat"));
                    Double lon = parseDouble(point.getAttribute("lon"));
                    if (lat != null && lon != null) {
                        coordinates.add(new Coordinate(lon, lat));
                    }
                }
                if (coordinates.size() < 2) {
                    continue;
                }

                LineString geometry = GEOMETRY_FACTORY.createLineString(coordinates.toArray(new Coordinate[0]));
                builder.add(geometry);
                builder.add(valueOrFallback(textOfChild(route, "name"), baseName + " route " + index));
                builder.add(parseDouble(textOfChild(route, "number")));
                builder.add(textOfChild(route, "type"));
                builder.add(textOfChild(route, "desc"));
                builder.add(coordinates.size());
                features.add(builder.buildFeature("gpx-rte-" + index++));
                builder.reset();
            }
        }

        return new ShapefileData(
                features,
                computeEnvelope(features),
                sourceName,
                features.size(),
                "GPX cargado | routes: " + features.size(),
                schema
        );
    }

    private static SimpleFeatureType buildWaypointSchema() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("gpx_waypoints");
        applyCrs(builder, "EPSG:4326");
        builder.add("the_geom", Point.class);
        builder.add("name", String.class);
        builder.add("elevation_m", Double.class);
        builder.add("time_utc", String.class);
        builder.add("symbol", String.class);
        builder.add("type_name", String.class);
        builder.add("description", String.class);
        return builder.buildFeatureType();
    }

    private static SimpleFeatureType buildTrackSchema() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("gpx_tracks");
        applyCrs(builder, "EPSG:4326");
        builder.add("the_geom", MultiLineString.class);
        builder.add("name", String.class);
        builder.add("start_ele_m", Double.class);
        builder.add("type_name", String.class);
        builder.add("description", String.class);
        builder.add("segment_count", Integer.class);
        return builder.buildFeatureType();
    }

    private static SimpleFeatureType buildRouteSchema() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("gpx_routes");
        applyCrs(builder, "EPSG:4326");
        builder.add("the_geom", LineString.class);
        builder.add("name", String.class);
        builder.add("number", Double.class);
        builder.add("type_name", String.class);
        builder.add("description", String.class);
        builder.add("point_count", Integer.class);
        return builder.buildFeatureType();
    }

    private static List<Element> directChildren(Element parent, String localName) {
        List<Element> children = new ArrayList<>();
        if (parent == null) {
            return children;
        }
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element) {
                String nodeLocal = element.getLocalName() != null ? element.getLocalName() : element.getTagName();
                if (localName.equalsIgnoreCase(nodeLocal)) {
                    children.add(element);
                }
            }
        }
        return children;
    }

    private static String textOfChild(Element parent, String localName) {
        for (Element child : directChildren(parent, localName)) {
            String text = child.getTextContent();
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        }
        return "";
    }

    private static String firstTrackPointValue(Element track, String localName) {
        for (Element segment : directChildren(track, "trkseg")) {
            for (Element point : directChildren(segment, "trkpt")) {
                String value = textOfChild(point, localName);
                if (!value.isBlank()) {
                    return value;
                }
            }
        }
        return "";
    }

    private static Double parseDouble(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim().replace(",", "."));
        } catch (Exception ex) {
            return null;
        }
    }

    private static Envelope computeEnvelope(List<SimpleFeature> features) {
        Envelope envelope = null;
        if (features == null) {
            return null;
        }
        for (SimpleFeature feature : features) {
            if (feature == null) {
                continue;
            }
            Object geometryObject = feature.getDefaultGeometry();
            if (geometryObject instanceof Geometry geometry && !geometry.isEmpty()) {
                if (envelope == null) {
                    envelope = new Envelope(geometry.getEnvelopeInternal());
                } else {
                    envelope.expandToInclude(geometry.getEnvelopeInternal());
                }
            }
        }
        return envelope;
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "GPX";
        }
        int index = name.lastIndexOf('.');
        return index > 0 ? name.substring(0, index) : name;
    }

    private static String valueOrFallback(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private static void applyCrs(SimpleFeatureTypeBuilder builder, String crsCode) {
        try {
            builder.setCRS(CRSDefinitions.decode(crsCode, true));
        } catch (Exception ignored) {
        }
    }
}
