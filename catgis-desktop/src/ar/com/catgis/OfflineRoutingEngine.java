package ar.com.catgis;

import org.locationtech.jts.geom.*;

import java.util.*;

/**
 * Offline network routing engine using Dijkstra algorithm.
 * Provides shortest path, service area, and network statistics.
 * Can be enhanced with external routing engines (GraphHopper, OSRM).
 */
public final class OfflineRoutingEngine {

    private OfflineRoutingEngine() {}

    public record RoutingResult(List<Coordinate> route, double totalDistance, String warnings) {}
    public record NetworkStatsResult(int nodeCount, int edgeCount, double totalLength, double avgDegree, double density) {}

    /**
     * Find shortest path between two points using Dijkstra.
     */
    public static RoutingResult shortestPath(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
                                              Coordinate start, Coordinate end, double snapTolerance) {
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        if (nodes.size() < 2) return new RoutingResult(Collections.emptyList(), 0, "Insufficient nodes.");

        int startIdx = findNearestNode(nodes, start);
        int endIdx = findNearestNode(nodes, end);
        if (startIdx < 0 || endIdx < 0) return new RoutingResult(Collections.emptyList(), 0, "Points outside network.");

        double[][] adj = buildAdjacencyMatrix(lineFeatures, nodes);
        double[] dist = new double[nodes.size()];
        int[] prev = new int[nodes.size()];
        boolean[] visited = new boolean[nodes.size()];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[startIdx] = 0;

        for (int iter = 0; iter < nodes.size(); iter++) {
            int u = -1;
            double minD = Double.MAX_VALUE;
            for (int i = 0; i < nodes.size(); i++) {
                if (!visited[i] && dist[i] < minD) { minD = dist[i]; u = i; }
            }
            if (u < 0 || dist[u] == Double.MAX_VALUE) break;
            visited[u] = true;
            if (u == endIdx) break;
            for (int v = 0; v < nodes.size(); v++) {
                if (visited[v] || adj[u][v] >= Double.MAX_VALUE) continue;
                double alt = dist[u] + adj[u][v];
                if (alt < dist[v]) { dist[v] = alt; prev[v] = u; }
            }
        }

        if (dist[endIdx] == Double.MAX_VALUE) return new RoutingResult(Collections.emptyList(), 0, "No path found.");
        List<Coordinate> route = new ArrayList<>();
        for (int at = endIdx; at != -1; at = prev[at]) route.add(nodes.get(at).coordinate());
        Collections.reverse(route);
        return new RoutingResult(route, dist[endIdx], "");
    }

    /**
     * Compute service area (nodes within distance).
     */
    public static List<Coordinate> serviceArea(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
                                                Coordinate center, double maxDistance, double snapTolerance) {
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        if (nodes.isEmpty()) return Collections.emptyList();
        int centerIdx = findNearestNode(nodes, center);
        if (centerIdx < 0) return Collections.emptyList();

        double[][] adj = buildAdjacencyMatrix(lineFeatures, nodes);
        double[] dist = new double[nodes.size()];
        boolean[] visited = new boolean[nodes.size()];
        Arrays.fill(dist, Double.MAX_VALUE);
        dist[centerIdx] = 0;

        for (int iter = 0; iter < nodes.size(); iter++) {
            int u = -1;
            double minD = Double.MAX_VALUE;
            for (int i = 0; i < nodes.size(); i++) {
                if (!visited[i] && dist[i] < minD) { minD = dist[i]; u = i; }
            }
            if (u < 0 || dist[u] > maxDistance) break;
            visited[u] = true;
            for (int v = 0; v < nodes.size(); v++) {
                if (visited[v] || adj[u][v] >= Double.MAX_VALUE) continue;
                double alt = dist[u] + adj[u][v];
                if (alt < dist[v]) dist[v] = alt;
            }
        }

        List<Coordinate> reachable = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            if (dist[i] <= maxDistance) reachable.add(nodes.get(i).coordinate());
        }
        return reachable;
    }

    /**
     * Compute all-pairs shortest paths.
     */
    public static double[][] allPairsShortestPaths(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
                                                    double snapTolerance) {
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        double[][] adj = buildAdjacencyMatrix(lineFeatures, nodes);
        int n = nodes.size();
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(dist[i], Double.MAX_VALUE);
            dist[i][i] = 0;
        }
        for (int s = 0; s < n; s++) {
            boolean[] visited = new boolean[n];
            for (int iter = 0; iter < n; iter++) {
                int u = -1;
                double minD = Double.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!visited[i] && dist[s][i] < minD) { minD = dist[s][i]; u = i; }
                }
                if (u < 0 || dist[s][u] == Double.MAX_VALUE) break;
                visited[u] = true;
                for (int v = 0; v < n; v++) {
                    if (visited[v] || adj[u][v] >= Double.MAX_VALUE) continue;
                    double alt = dist[s][u] + adj[u][v];
                    if (alt < dist[s][v]) dist[s][v] = alt;
                }
            }
        }
        return dist;
    }

    /**
     * Compute network statistics.
     */
    public static NetworkStatsResult computeStats(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
                                                   double snapTolerance) {
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        double totalLength = 0;
        int edgeCount = 0;
        for (org.geotools.api.feature.simple.SimpleFeature f : lineFeatures) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null) {
                totalLength += g.getLength();
                edgeCount += g.getCoordinates().length - 1;
            }
        }
        double avgDegree = nodes.isEmpty() ? 0 : (double) edgeCount * 2 / nodes.size();
        double density = nodes.size() > 1 ? (double) edgeCount / (nodes.size() * (nodes.size() - 1)) : 0;
        return new NetworkStatsResult(nodes.size(), edgeCount, totalLength, avgDegree, density);
    }

    // --- Internal helpers (delegate to NetworkAnalysisEngine) ---

    private static List<NetworkAnalysisEngine.NetworkPoint> buildGraph(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures, double snapTolerance) {
        Map<String, NetworkAnalysisEngine.NetworkPoint> nodeMap = new LinkedHashMap<>();
        GeometryFactory gf = new GeometryFactory();
        for (org.geotools.api.feature.simple.SimpleFeature feature : lineFeatures) {
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

    private static double[][] buildAdjacencyMatrix(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
                                                     List<NetworkAnalysisEngine.NetworkPoint> nodes) {
        int n = nodes.size();
        double[][] m = new double[n][n];
        for (int i = 0; i < n; i++) { Arrays.fill(m[i], Double.MAX_VALUE); m[i][i] = 0; }
        Map<String, Integer> keyToIdx = new HashMap<>();
        for (int i = 0; i < n; i++) keyToIdx.put(nodes.get(i).coordinate().getX() + "," + nodes.get(i).coordinate().getY(), i);
        for (org.geotools.api.feature.simple.SimpleFeature f : lineFeatures) {
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
