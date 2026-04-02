package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DrawFeatureBuilder {

    public static ShapefileData buildSingleGeometryLayer(Geometry geometry, String layerName) throws Exception {
        if (geometry == null || geometry.isEmpty()) {
            throw new RuntimeException("No hay geometria para convertir en capa.");
        }

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("draw_layer");
        typeBuilder.add("the_geom", geometry.getClass());
        typeBuilder.add("id", String.class);
        typeBuilder.add("tipo", String.class);

        SimpleFeatureType featureType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

        List<SimpleFeature> features = new ArrayList<>();
        featureBuilder.add(geometry);
        featureBuilder.add("1");
        featureBuilder.add(geometry.getGeometryType());

        SimpleFeature feature = featureBuilder.buildFeature("1");
        features.add(feature);

        Envelope env = geometry.getEnvelopeInternal();

        return new ShapefileData(
                features,
                env != null ? new Envelope(env) : null,
                layerName,
                1,
                "Geometria digitalizada",
                featureType
        );
    }

    public static ShapefileData buildPointLayer(List<Coordinate> coordinates, String layerName) throws Exception {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new RuntimeException("No hay puntos para convertir en capa.");
        }

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("draw_points");
        typeBuilder.add("the_geom", Point.class);
        typeBuilder.add("id", String.class);
        typeBuilder.add("tipo", String.class);
        typeBuilder.add("coord_x", Double.class);
        typeBuilder.add("coord_y", Double.class);

        SimpleFeatureType featureType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        GeometryFactory gf = new GeometryFactory();

        List<SimpleFeature> features = new ArrayList<>();
        Envelope env = new Envelope();

        int i = 1;
        for (Coordinate coordinate : coordinates) {
            Point point = gf.createPoint(coordinate);

            featureBuilder.add(point);
            featureBuilder.add(String.valueOf(i));
            featureBuilder.add("Point");
            featureBuilder.add(coordinate.x);
            featureBuilder.add(coordinate.y);

            SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(i));
            features.add(feature);

            env.expandToInclude(point.getCoordinate());
            featureBuilder.reset();
            i++;
        }

        return new ShapefileData(
                features,
                env,
                layerName,
                features.size(),
                "Puntos digitalizados",
                featureType
        );
    }

    public static ShapefileData buildMultiPointLayer(List<Coordinate> coordinates, String layerName) throws Exception {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new RuntimeException("No hay puntos para convertir en capa multipunto.");
        }

        GeometryFactory gf = new GeometryFactory();
        Point[] points = new Point[coordinates.size()];

        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = gf.createPoint(coordinates.get(i));
        }

        Geometry multiPoint = gf.createMultiPoint(points);
        return buildSingleGeometryLayer(multiPoint, layerName);
    }

    public static Geometry buildPoint(Coordinate coordinate) {
        if (coordinate == null) {
            return null;
        }

        GeometryFactory gf = new GeometryFactory();
        return gf.createPoint(coordinate);
    }

    public static Geometry buildLine(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return null;
        }

        GeometryFactory gf = new GeometryFactory();
        return gf.createLineString(coordinates.toArray(new Coordinate[0]));
    }

    public static Geometry buildPolygon(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 3) {
            return null;
        }

        List<Coordinate> coords = new ArrayList<>(coordinates);

        Coordinate first = coords.get(0);
        Coordinate last = coords.get(coords.size() - 1);

        if (!first.equals2D(last)) {
            coords.add(new Coordinate(first.x, first.y));
        }

        GeometryFactory gf = new GeometryFactory();
        return gf.createPolygon(coords.toArray(new Coordinate[0]));
    }

    public static ShapefileData buildEmptyLayer(String layerName,
                                                Class<? extends Geometry> geometryClass,
                                                List<FieldConfig> fieldConfigs,
                                                String crsCode) throws Exception {
        SimpleFeatureType featureType = buildFeatureType(layerName, geometryClass, fieldConfigs, crsCode);
        return new ShapefileData(
                new ArrayList<>(),
                new Envelope(),
                layerName,
                0,
                "Capa vectorial creada",
                featureType
        );
    }

    public static SimpleFeatureType buildFeatureType(String layerName,
                                                     Class<? extends Geometry> geometryClass,
                                                     List<FieldConfig> fieldConfigs,
                                                     String crsCode) throws Exception {
        if (geometryClass == null) {
            throw new RuntimeException("No se pudo determinar el tipo geometrico.");
        }

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(safeTypeName(layerName));

        if (crsCode != null && !crsCode.isBlank()) {
            CoordinateReferenceSystem crs = CRS.decode(CRSDefinitions.normalizeCode(crsCode), true);
            typeBuilder.setCRS(crs);
        }

        typeBuilder.add("the_geom", geometryClass);

        Map<String, FieldConfig> uniqueFields = new LinkedHashMap<>();
        if (fieldConfigs != null) {
            for (FieldConfig config : fieldConfigs) {
                if (config == null) {
                    continue;
                }
                String fieldName = config.getFieldName() != null ? config.getFieldName().trim() : "";
                if (fieldName.isBlank()) {
                    continue;
                }
                uniqueFields.putIfAbsent(fieldName, config);
            }
        }

        for (FieldConfig config : uniqueFields.values()) {
            if (config.getLength() > 0) {
                typeBuilder.length(config.getLength());
            }
            typeBuilder.add(config.getFieldName(), resolveAttributeClass(config.getTypeName()));
        }

        return typeBuilder.buildFeatureType();
    }

    public static Class<? extends Geometry> resolveGeometryClass(String geometryKind) {
        if (geometryKind == null) {
            return null;
        }

        String kind = geometryKind.trim().toUpperCase();
        switch (kind) {
            case "PUNTO":
            case "POINT":
            case "MULTIPOINT":
                return Point.class;
            case "LINEA":
            case "LINE":
            case "LINESTRING":
            case "MULTILINESTRING":
                return LineString.class;
            case "POLIGONO":
            case "POLYGON":
            case "MULTIPOLYGON":
                return Polygon.class;
            default:
                return null;
        }
    }

    public static String resolveGeometryFamily(Class<?> geometryClass) {
        if (geometryClass == null) {
            return "";
        }
        if (Point.class.isAssignableFrom(geometryClass) || MultiPoint.class.isAssignableFrom(geometryClass)) {
            return "POINT";
        }
        if (LineString.class.isAssignableFrom(geometryClass) || MultiLineString.class.isAssignableFrom(geometryClass)) {
            return "LINE";
        }
        if (Polygon.class.isAssignableFrom(geometryClass) || MultiPolygon.class.isAssignableFrom(geometryClass)) {
            return "POLYGON";
        }
        return "";
    }

    public static Class<?> resolveAttributeClass(String typeName) {
        String normalized = FieldConfig.normalizeTypeName(typeName);
        switch (normalized) {
            case "Integer":
                return Integer.class;
            case "Long":
                return Long.class;
            case "Float":
                return Float.class;
            case "Double":
                return Double.class;
            case "Boolean":
                return Boolean.class;
            case "Date":
                return java.util.Date.class;
            case "Timestamp":
                return java.util.Date.class;
            case "String":
            default:
                return String.class;
        }
    }

    private static String safeTypeName(String text) {
        if (text == null || text.isBlank()) {
            return "layer";
        }
        String safe = text.replaceAll("[^A-Za-z0-9_]+", "_").trim();
        if (safe.isBlank()) {
            safe = "layer";
        }
        if (Character.isDigit(safe.charAt(0))) {
            safe = "layer_" + safe;
        }
        return safe;
    }
}
