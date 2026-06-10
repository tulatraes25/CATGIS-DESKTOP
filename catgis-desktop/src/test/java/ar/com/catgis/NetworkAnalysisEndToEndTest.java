package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-07: NetworkAnalysisDialog end-to-end.
 * Tests all 6 operations with a known small network and validates results.
 */
class NetworkAnalysisEndToEndTest {

    private static final GeometryFactory GF = new GeometryFactory();
    private SimpleFeatureType lineType;
    private List<SimpleFeature> network;

    @BeforeEach
    void setUp() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("roads");
        tb.add("the_geom", LineString.class);
        tb.add("id", Integer.class);
        lineType = tb.buildFeatureType();

        // Simple network: A(0,0)--B(2,0)--C(4,0)--D(6,0)
        //                 |                        |
        //                 E(0,2)--F(2,2)--G(4,2)--H(6,2)
        network = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        addLine(fb, new Coordinate[]{new Coordinate(0,0), new Coordinate(2,0)}, 1);
        addLine(fb, new Coordinate[]{new Coordinate(2,0), new Coordinate(4,0)}, 2);
        addLine(fb, new Coordinate[]{new Coordinate(4,0), new Coordinate(6,0)}, 3);
        addLine(fb, new Coordinate[]{new Coordinate(0,0), new Coordinate(0,2)}, 4);
        addLine(fb, new Coordinate[]{new Coordinate(0,2), new Coordinate(2,2)}, 5);
        addLine(fb, new Coordinate[]{new Coordinate(2,2), new Coordinate(4,2)}, 6);
        addLine(fb, new Coordinate[]{new Coordinate(4,2), new Coordinate(6,2)}, 7);
        addLine(fb, new Coordinate[]{new Coordinate(2,0), new Coordinate(2,2)}, 8);
        addLine(fb, new Coordinate[]{new Coordinate(4,0), new Coordinate(4,2)}, 9);
    }

    private void addLine(SimpleFeatureBuilder fb, Coordinate[] coords, int id) {
        fb.add(GF.createLineString(coords));
        fb.add(id);
        network.add(fb.buildFeature("road." + id));
    }

    @Test
    void shortestPathFindsOptimalRoute() {
        var path = NetworkAnalysisEngine.shortestPath(network, new Coordinate(0,0), new Coordinate(6,0), 1.0);
        assertNotNull(path);
        assertEquals(6.0, path.totalDistance(), 0.01);
        assertFalse(path.route().isEmpty());
    }

    @Test
    void costMatrixIsSymmetric() {
        List<Coordinate> points = List.of(
                new Coordinate(0,0), new Coordinate(2,0), new Coordinate(4,0));
        double[][] matrix = NetworkAnalysisEngine.computeCostMatrix(network, points, 1.0);
        assertEquals(3, matrix.length);
        assertEquals(0.0, matrix[0][0], 0.01);
        assertEquals(matrix[0][1], matrix[1][0], 0.01);
    }

    @Test
    void serviceAreaFindsReachableNodes() {
        var area = NetworkAnalysisEngine.serviceArea(network, new Coordinate(2,0), 3.0, 1.0);
        assertNotNull(area);
        assertTrue(area.size() >= 3);
    }

    @Test
    void centralityReturnsNonNegativeValues() {
        double[] centrality = NetworkAnalysisEngine.betweennessCentrality(network, 1.0);
        assertNotNull(centrality);
        assertEquals(8, centrality.length);
        for (double c : centrality) {
            assertTrue(c >= 0, "Centrality should be non-negative");
        }
    }

    @Test
    void allPairsReturnsCorrectSize() {
        double[][] matrix = NetworkAnalysisEngine.allPairsShortestPaths(network, 1.0);
        assertNotNull(matrix);
        assertEquals(8, matrix.length);
        assertEquals(8, matrix[0].length);
    }

    @Test
    void networkStatsReturnsValidData() {
        var stats = NetworkAnalysisEngine.computeStats(network, 1.0);
        assertNotNull(stats);
        assertTrue(stats.nodeCount() > 0);
        assertTrue(stats.edgeCount() > 0);
        assertTrue(stats.totalLength() > 0);
    }
}
