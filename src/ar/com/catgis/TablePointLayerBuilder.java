package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TablePointLayerBuilder {

    public static ShapefileData build(
            TablePointData tableData,
            String xField,
            String yField,
            String sourceCRS,
            String labelField
    ) throws Exception {

        if (tableData == null || tableData.isEmpty()) {
            throw new RuntimeException("La tabla no tiene datos.");
        }

        if (xField == null || xField.isBlank() || yField == null || yField.isBlank()) {
            throw new RuntimeException("Debe seleccionar campos X e Y.");
        }

        GeometryFactory geometryFactory = new GeometryFactory();

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("table_points");
        typeBuilder.add("the_geom", Point.class);

        for (String col : tableData.getColumns()) {
            typeBuilder.add(col, String.class);
        }

        var featureType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);

        List<SimpleFeature> features = new ArrayList<>();
        org.locationtech.jts.geom.Envelope envelope = null;

        int id = 1;

        for (Map<String, String> row : tableData.getRows()) {
            String xText = row.getOrDefault(xField, "");
            String yText = row.getOrDefault(yField, "");

            if (xText.isBlank() || yText.isBlank()) {
                continue;
            }

            double x = parseCoordinate(xText);
            double y = parseCoordinate(yText);

            Point point = geometryFactory.createPoint(new Coordinate(x, y));

            featureBuilder.add(point);

            for (String col : tableData.getColumns()) {
                featureBuilder.add(row.getOrDefault(col, ""));
            }

            SimpleFeature feature = featureBuilder.buildFeature(String.valueOf(id++));
            features.add(feature);
            featureBuilder.reset();

            if (envelope == null) {
                envelope = new org.locationtech.jts.geom.Envelope(point.getEnvelopeInternal());
            } else {
                envelope.expandToInclude(point.getEnvelopeInternal());
            }
        }

        if (features.isEmpty()) {
            throw new RuntimeException("No se pudieron crear puntos válidos con las columnas seleccionadas.");
        }

        String sourceName = "Tabla de puntos";
        String message = "Tabla convertida a puntos | entidades: " + features.size();
        return new ShapefileData(features, envelope, sourceName, features.size(), message);
    }

    private static double parseCoordinate(String text) {
        String value = text.trim().replace(",", ".");
        return Double.parseDouble(value);
    }
}