package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class DrawFeatureBuilder {

    public static ShapefileData buildSingleGeometryLayer(Geometry geometry, String layerName) throws Exception {
        if (geometry == null || geometry.isEmpty()) {
            throw new RuntimeException("No hay geometría para convertir en capa.");
        }

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("draw_layer");
        typeBuilder.add("the_geom", geometry.getClass());
        typeBuilder.add("id", String.class);
        typeBuilder.add("tipo", String.class);

        var featureType = typeBuilder.buildFeatureType();
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
                "Geometría digitalizada"
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

        var featureType = typeBuilder.buildFeatureType();
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
                "Puntos digitalizados"
        );
    }


    public static ShapefileData buildMultiPointLayer(List<Coordinate> coordinates, String layerName) throws Exception {
        if (coordinates == null || coordinates.isEmpty()) {
            throw new RuntimeException("No hay puntos para convertir en capa multipunto.");
        }

        GeometryFactory gf = new GeometryFactory();
        Point[] points = new Point[coordinates.size()];
        Envelope env = new Envelope();

        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate coordinate = coordinates.get(i);
            points[i] = gf.createPoint(coordinate);
            env.expandToInclude(coordinate);
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
}