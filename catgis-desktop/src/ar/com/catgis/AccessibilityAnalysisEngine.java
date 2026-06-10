package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;

import java.util.*;

/**
 * Accessibility analysis engine.
 * Computes isochrones (areas reachable within a given time/distance) from points.
 */
public final class AccessibilityAnalysisEngine {

    private AccessibilityAnalysisEngine() {}

    public record IsochroneResult(List<Polygon> polygons, Map<Integer, Double> zoneAreas) {}

    /**
     * Compute isochrones from a network and origin points.
     * Uses Dijkstra to find reachable nodes, then builds polygons.
     */
    public static IsochroneResult computeIsochrones(
            List<SimpleFeature> lineFeatures,
            Coordinate origin,
            double[] distanceThresholds,
            double snapTolerance) {

        Map<Integer, List<Coordinate>> reachableByThreshold = new LinkedHashMap<>();
        for (int i = 0; i < distanceThresholds.length; i++) {
            reachableByThreshold.put(i, new ArrayList<>());
        }

        // Find all nodes reachable at each threshold
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        if (nodes.isEmpty()) return new IsochroneResult(Collections.emptyList(), Collections.emptyMap());

        int originIdx = findNearestNode(nodes, origin);
        if (originIdx < 0) return new IsochroneResult(Collections.emptyList(), Collections.emptyMap());

        double[][] adj = buildAdjacencyMatrix(lineFeatures, nodes);
        double[] dist = new double[nodes.size()];
        boolean[] visited = new boolean[nodes.size()];
        Arrays.fill(dist, Double.MAX_VALUE);
        dist[originIdx] = 0;

        for (int iter = 0; iter < nodes.size(); iter++) {
            int u = -1;
            double minD = Double.MAX_VALUE;
            for (int i = 0; i < nodes.size(); i++) {
                if (!visited[i] && dist[i] < minD) { minD = dist[i]; u = i; }
            }
            double maxThreshold = distanceThresholds[distanceThresholds.length - 1];
            if (u < 0 || dist[u] > maxThreshold) break;
            visited[u] = true;

            for (int v = 0; v < nodes.size(); v++) {
                if (visited[v] || adj[u][v] >= Double.MAX_VALUE) continue;
                double alt = dist[u] + adj[u][v];
                if (alt < dist[v]) dist[v] = alt;
            }
        }

        // Assign nodes to thresholds
        for (int i = 0; i < nodes.size(); i++) {
            for (int t = 0; t < distanceThresholds.length; t++) {
                if (dist[i] <= distanceThresholds[t]) {
                    reachableByThreshold.get(t).add(nodes.get(i).coordinate());
                }
            }
        }

        // Build convex hull polygons for each threshold
        List<Polygon> polygons = new ArrayList<>();
        Map<Integer, Double> zoneAreas = new LinkedHashMap<>();
        GeometryFactory gf = new GeometryFactory();

        for (int t = 0; t < distanceThresholds.length; t++) {
            List<Coordinate> reachable = reachableByThreshold.get(t);
            if (reachable.size() < 3) continue;

            GeometryCollection gc = gf.createGeometryCollection(
                    reachable.stream().map(c -> gf.createPoint(c)).toArray(Geometry[]::new));
            Geometry hull = gc.convexHull();

            if (hull instanceof Polygon polygon) {
                polygons.add(polygon);
                zoneAreas.put(t, polygon.getArea());
            }
        }

        return new IsochroneResult(polygons, zoneAreas);
    }

    /**
     * Compute accessibility statistics between specific points.
     * Uses the points parameter to find nearest network nodes.
     */
    public static Map<String, Double> computeAccessibilityStats(
            List<SimpleFeature> lineFeatures,
            List<Coordinate> points,
            double snapTolerance) {

        // Build network graph
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        if (nodes.isEmpty() || points == null || points.isEmpty()) {
            Map<String, Double> empty = new LinkedHashMap<>();
            empty.put("total_pairs", 0.0);
            empty.put("connected_pairs", 0.0);
            empty.put("connectivity", 0.0);
            return empty;
        }

        // Find nearest network node for each point
        int[] nodeIndices = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            nodeIndices[i] = findNearestNode(nodes, points.get(i));
        }

        // Compute all-pairs shortest paths
        double[][] matrix = NetworkAnalysisEngine.allPairsShortestPaths(lineFeatures, snapTolerance);

        // Compute stats only between the specified points
        double totalDistance = 0;
        double maxDistance = 0;
        double minDistance = Double.MAX_VALUE;
        int connectedPairs = 0;
        int totalPairs = 0;

        for (int i = 0; i < nodeIndices.length; i++) {
            for (int j = i + 1; j < nodeIndices.length; j++) {
                totalPairs++;
                int ni = nodeIndices[i];
                int nj = nodeIndices[j];
                if (ni >= 0 && nj >= 0 && ni < matrix.length && nj < matrix.length
                        && matrix[ni][nj] < Double.MAX_VALUE) {
                    totalDistance += matrix[ni][nj];
                    if (matrix[ni][nj] > maxDistance) maxDistance = matrix[ni][nj];
                    if (matrix[ni][nj] < minDistance) minDistance = matrix[ni][nj];
                    connectedPairs++;
                }
            }
        }

        Map<String, Double> stats = new LinkedHashMap<>();
        stats.put("total_pairs", (double) totalPairs);
        stats.put("connected_pairs", (double) connectedPairs);
        stats.put("connectivity", totalPairs > 0 ? (double) connectedPairs / totalPairs * 100 : 0);
        stats.put("avg_distance", connectedPairs > 0 ? totalDistance / connectedPairs : 0);
        stats.put("max_distance", maxDistance);
        stats.put("min_distance", connectedPairs > 0 ? minDistance : 0);
        stats.put("total_distance", totalDistance);

        return stats;
    }

    // --- Internal helpers ---

    private static List<NetworkAnalysisEngine.NetworkPoint> buildGraph(List<SimpleFeature> lineFeatures, double snapTolerance) {
        Map<String, NetworkAnalysisEngine.NetworkPoint> nodeMap = new LinkedHashMap<>();
        GeometryFactory gf = new GeometryFactory();
        for (SimpleFeature feature : lineFeatures) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom == null) continue;
            for (Coordinate c : geom.getCoordinates()) {
                String key = snapTolerance <= 0 ? c.getX() + "," + c.getY() :
                        Math.round(c.getX() / snapTolerance) + "," + Math.round(c.getY() / snapTolerance);
                nodeMap.putIfAbsent(key, new NetworkAnalysisEngine.NetworkPoint(nodeMap.size(), gf.createPoint(c).getCoordinate()));
            }
        }
        return new ArrayList<>(nodeMap.values());
    }

    private static double[][] buildAdjacencyMatrix(List<SimpleFeature> lineFeatures,
                                                     List<NetworkAnalysisEngine.NetworkPoint> nodes) {
        int n = nodes.size();
        double[][] m = new double[n][n];
        for (int i = 0; i < n; i++) { Arrays.fill(m[i], Double.MAX_VALUE); m[i][i] = 0; }
        Map<String, Integer> keyToIdx = new HashMap<>();
        for (int i = 0; i < n; i++) keyToIdx.put(nodes.get(i).coordinate().getX() + "," + nodes.get(i).coordinate().getY(), i);
        for (SimpleFeature f : lineFeatures) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g == null) continue;
            Coordinate[] coords = g.getCoordinates();
            for (int i = 0; i < coords.length - 1; i++) {
                Integer idx1 = keyToIdx.get(coords[i].getX() + "," + coords[i].getY());
                Integer idx2 = keyToIdx.get(coords[i+1].getX() + "," + coords[i+1].getY());
                if (idx1 != null && idx2 != null) {
                    double d = coords[i].distance(coords[i+1]);
                    m[idx1][idx2] = Math.min(m[idx1][idx2], d);
                    m[idx2][idx1] = Math.min(m[idx2][idx1], d);
                }
            }
        }
        return m;
    }

    private static int findNearestNode(List<NetworkAnalysisEngine.NetworkPoint> nodes, Coordinate target) {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            double d = nodes.get(i).coordinate().distance(target);
            if (d < bestDist) { bestDist = d; best = i; }
        }
        return best;
    }
}
