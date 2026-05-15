package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseFieldCalculatorTest {

    @Test
    void resolvesMetricMeasurementsForGeographicData() throws Exception {
        ReleaseTestSupport.clearAppContext();
        Layer layer = ReleaseTestSupport.buildVectorLayer("MetricSmoke", null, "EPSG:4326");
        Polygon polygon = ReleaseTestSupport.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 0),
                new Coordinate(1, 1),
                new Coordinate(0, 1),
                new Coordinate(0, 0)
        });
        LineString line = ReleaseTestSupport.GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(0, 1)
        });

        double lengthMeters = VectorMeasurementSupport.resolveLengthMeters(layer, line);
        double areaSquareMeters = VectorMeasurementSupport.resolveAreaSquareMeters(layer, polygon);
        double perimeterMeters = VectorMeasurementSupport.resolvePerimeterMeters(layer, polygon);

        ShapefileData data = ReleaseTestSupport.buildPointData(
                "metric_smoke",
                "EPSG:4326",
                new Coordinate(-68.85, -32.89),
                "Pozo A",
                7
        );
        ShapefileData augmented = VectorAttributeSupport.addField(data, "area_m2", "Double");

        assertTrue(Double.isFinite(lengthMeters) && lengthMeters > 0d);
        assertTrue(Double.isFinite(areaSquareMeters) && areaSquareMeters > 0d);
        assertTrue(Double.isFinite(perimeterMeters) && perimeterMeters > 0d);
        assertTrue(augmented.getSchema() != null && augmented.getSchema().getDescriptor("area_m2") != null);
    }

    @Test
    void parsesBasicNumericExpressions() {
        AttributeTableWindow.BasicExpressionParser parser =
                new AttributeTableWindow.BasicExpressionParser("round(pow(3,2) / 2) + max(4, 1)");

        double value = parser.parse();

        assertEquals(8d, value);
    }
}
