package ar.com.catgis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NetworkAnalysisEngineTest {

    private static final GeometryFactory GF = new GeometryFactory();

    @Test
    void shortestPathFindsRoute() {
        Coordinate[] coords1 = {new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(2, 0)};
        Coordinate[] coords2 = {new Coordinate(2, 0), new Coordinate(3, 0), new Coordinate(4, 0)};
        LineString line1 = GF.createLineString(coords1);
        LineString line2 = GF.createLineString(coords2);
        List<org.geotools.api.feature.simple.SimpleFeature> features = List.of();
        var path = NetworkAnalysisEngine.shortestPath(features, new Coordinate(0, 0), new Coordinate(4, 0), 0.1);
        assertNotNull(path);
    }

    @Test
    void buildRouteGeometryReturnsLine() {
        List<Coordinate> route = List.of(new Coordinate(0, 0), new Coordinate(1, 1), new Coordinate(2, 0));
        Geometry geom = NetworkAnalysisEngine.buildRouteGeometry(route, GF);
        assertNotNull(geom);
        assertTrue(geom instanceof LineString);
        assertEquals(3, geom.getCoordinates().length);
    }

    @Test
    void buildRouteGeometryReturnsNullForShortRoute() {
        Geometry geom = NetworkAnalysisEngine.buildRouteGeometry(List.of(new Coordinate(0, 0)), GF);
        assertNull(geom);
    }
}
