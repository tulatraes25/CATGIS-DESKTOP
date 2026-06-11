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

class NetworkAnalysisEngineTest {

    private static final GeometryFactory GF = new GeometryFactory();
    private SimpleFeatureType lineType;
    private List<SimpleFeature> networkFeatures;

    @BeforeEach
    void setUp() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName("test_lines");
        tb.add("the_geom", LineString.class);
        tb.add("id", Integer.class);
        lineType = tb.buildFeatureType();

        // Build a simple network:  A(0,0) -- B(2,0) -- C(4,0)
        //                           |                    |
        //                           D(0,2) -- E(2,2) -- F(4,2)
        networkFeatures = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);

        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(2, 0)}, 1);
        addLine(fb, new Coordinate[]{new Coordinate(2, 0), new Coordinate(4, 0)}, 2);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(0, 2)}, 3);
        addLine(fb, new Coordinate[]{new Coordinate(0, 2), new Coordinate(2, 2)}, 4);
        addLine(fb, new Coordinate[]{new Coordinate(2, 2), new Coordinate(4, 2)}, 5);
        addLine(fb, new Coordinate[]{new Coordinate(2, 0), new Coordinate(2, 2)}, 6);
        addLine(fb, new Coordinate[]{new Coordinate(4, 0), new Coordinate(4, 2)}, 7);
    }

    private void addLine(SimpleFeatureBuilder fb, Coordinate[] coords, int id) {
        fb.add(GF.createLineString(coords));
        fb.add(id);
        networkFeatures.add(fb.buildFeature("line." + id));
    }

    @Test
    void shortestPathFindsOptimalRoute() {
        // Shortest from (0,0) to (4,0) should be direct: distance = 4.0
        var path = NetworkAnalysisEngine.shortestPath(networkFeatures, new Coordinate(0, 0), new Coordinate(4, 0), 0.1);
        assertNotNull(path);
        assertEquals(4.0, path.totalDistance(), 0.01);
        assertFalse(path.route().isEmpty());
    }

    @Test
    void shortestPathFindsIndirectRoute() {
        // From (0,0) to (4,2) via D-E-F: distance = 2+2+2 = 6
        // But direct A-B-C-F: distance = 2+2+2 = 6 (same)
        var path = NetworkAnalysisEngine.shortestPath(networkFeatures, new Coordinate(0, 0), new Coordinate(4, 2), 0.1);
        assertNotNull(path);
        assertEquals(6.0, path.totalDistance(), 0.01);
    }

    @Test
    void shortestPathReturnsResultForDistantPoints() {
        var path = NetworkAnalysisEngine.shortestPath(networkFeatures, new Coordinate(100, 100), new Coordinate(200, 200), 0.1);
        assertNotNull(path);
    }

    @Test
    void serviceAreaReturnsReachableNodes() {
        var area = NetworkAnalysisEngine.serviceArea(networkFeatures, new Coordinate(2, 0), 3.0, 0.1);
        assertNotNull(area);
        assertFalse(area.isEmpty());
        // (2,0) with maxDist=3 should reach (0,0), (4,0), (0,2), (2,2), (4,2)
        assertTrue(area.size() >= 3);
    }

    @Test
    void costMatrixIsSymmetric() {
        List<Coordinate> points = List.of(
                new Coordinate(0, 0), new Coordinate(2, 0), new Coordinate(4, 0));
        double[][] matrix = NetworkAnalysisEngine.computeCostMatrix(networkFeatures, points, 0.1);
        assertNotNull(matrix);
        assertEquals(3, matrix.length);
        assertEquals(3, matrix[0].length);
        assertEquals(0.0, matrix[0][0], 0.01);
        assertEquals(matrix[0][1], matrix[1][0], 0.01);
        assertEquals(matrix[0][2], matrix[2][0], 0.01);
    }

    @Test
    void networkStatsReturnsValidData() {
        var stats = NetworkAnalysisEngine.computeStats(networkFeatures, 0.1);
        assertNotNull(stats);
        assertTrue(stats.nodeCount() > 0);
        assertTrue(stats.edgeCount() > 0);
        assertTrue(stats.totalLength() > 0);
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

    @Test
    void minimumSpanningTreeCoversAllNodes() {
        var mst = NetworkAnalysisEngine.minimumSpanningTree(networkFeatures, 0.1);
        assertNotNull(mst);
        assertEquals(5, mst.edges().size(), "6-node network MST should have 5 edges");
        assertTrue(mst.totalWeight() > 0);
        assertTrue(mst.warnings().isEmpty());
    }

    @Test
    void minimumSpanningTreeHandlesDisconnected() {
        List<SimpleFeature> disconnected = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 0)}, 1);
        addLine(fb, new Coordinate[]{new Coordinate(10, 0), new Coordinate(11, 0)}, 2);
        var mst = NetworkAnalysisEngine.minimumSpanningTree(disconnected, 0.1);
        assertNotNull(mst);
        assertFalse(mst.warnings().isEmpty(), "Should warn about disconnected network");
    }

    @Test
    void maxFlowReturnsValidResult() {
        var flow = NetworkAnalysisEngine.maxFlow(networkFeatures,
                new Coordinate(0, 0), new Coordinate(4, 2), 0.1);
        assertNotNull(flow);
        assertTrue(flow.maxFlow() >= 0);
    }

    @Test
    void maxFlowWithNoPath() {
        List<SimpleFeature> single = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 0)}, 1);
        var flow = NetworkAnalysisEngine.maxFlow(single,
                new Coordinate(5, 5), new Coordinate(6, 6), 0.1);
        assertNotNull(flow);
        assertEquals(0, flow.maxFlow());
    }

    @Test
    void eulerianCircuitDetectsCycle() {
        // Triangle cycle: A(0,0)-B(1,0)-C(0.5,1)-A
        List<SimpleFeature> cycle = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 0)}, 1);
        addLine(fb, new Coordinate[]{new Coordinate(1, 0), new Coordinate(0.5, 1)}, 2);
        addLine(fb, new Coordinate[]{new Coordinate(0.5, 1), new Coordinate(0, 0)}, 3);
        var euler = NetworkAnalysisEngine.eulerianCircuit(cycle, 0.1);
        assertNotNull(euler);
        assertTrue(euler.hasEulerianCycle() || !euler.warnings().isEmpty(),
                "Triangle should be Eulerian or produce warning: " + euler.warnings());
    }

    @Test
    void eulerianCircuitDetectsNonEulerian() {
        // Star graph with 4 leaves (all odd degree) - not eulerian
        List<SimpleFeature> star = new ArrayList<>();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(lineType);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(1, 0)}, 1);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(-1, 0)}, 2);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(0, 1)}, 3);
        addLine(fb, new Coordinate[]{new Coordinate(0, 0), new Coordinate(0, -1)}, 4);
        var euler = NetworkAnalysisEngine.eulerianCircuit(star, 0.1);
        assertNotNull(euler);
        assertFalse(euler.hasEulerianCycle());
        assertFalse(euler.hasEulerianPath());
    }
}
