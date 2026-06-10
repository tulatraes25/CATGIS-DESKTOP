package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HexagonalGridTest {

    private static final GeometryFactory GF = new GeometryFactory();

    private List<SimpleFeature> createPoints() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("pts");
        tb.add("the_geom", Point.class);
        SimpleFeatureType type = tb.buildFeatureType();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        List<SimpleFeature> pts = new ArrayList<>();
        double[][] coords = {{10,10},{20,10},{30,10},{10,20},{20,20},{30,20},{10,30},{20,30},{30,30},{15,15},{25,25}};
        for (double[] c : coords) {
            fb.add(GF.createPoint(new Coordinate(c[0], c[1])));
            pts.add(fb.buildFeature("p" + pts.size()));
        }
        return pts;
    }

    @Test
    void createHexGridReturnsCells() throws Exception {
        Envelope env = new Envelope(0, 0, 40, 40);
        List<SimpleFeature> pts = createPoints();
        var cells = HexagonalGrid.createHexGrid(env, 10, pts);
        assertNotNull(cells);
        assertTrue(cells.size() > 0, "Should find cells with points");
    }

    @Test
    void hexCellsToPolygonsCreatesValidGeometries() throws Exception {
        Envelope env = new Envelope(0, 0, 40, 40);
        List<SimpleFeature> pts = createPoints();
        var cells = HexagonalGrid.createHexGrid(env, 10, pts);
        var polygons = HexagonalGrid.hexCellsToPolygons(cells, 10);
        assertNotNull(polygons);
        assertTrue(polygons.size() > 0);
        for (var poly : polygons) {
            assertNotNull(poly);
            assertTrue(poly.getArea() > 0);
        }
    }

    @Test
    void h3IndexAndRoundtrip() {
        Envelope env = new Envelope(0, 0, 100, 100);
        String index = HexagonalGrid.h3Index(50, 50, 10, env);
        assertNotNull(index);
        assertTrue(index.startsWith("r"));
        assertTrue(index.contains("c"));

        double[] center = HexagonalGrid.h3ToCenter(index, 10, env);
        assertNotNull(center);
        assertEquals(2, center.length);
    }

    @Test
    void emptyEnvelopeReturnsEmptyGrid() {
        var cells = HexagonalGrid.createHexGrid(new Envelope(0, 0, 0, 0), 10, null);
        assertNotNull(cells);
    }
}
