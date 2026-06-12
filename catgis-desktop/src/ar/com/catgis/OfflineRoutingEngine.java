package ar.com.catgis;

import org.locationtech.jts.geom.*;

import java.util.*;

/**
 * Offline network routing engine using Dijkstra algorithm.
 * <p>
 * Provides shortest path, service area, network statistics, and
 * a pluggable routing backend for GraphHopper/OSRM integration.
 * </p>
 */
public final class OfflineRoutingEngine {

    private OfflineRoutingEngine() {}

    public record RoutingResult(List<Coordinate> route, double totalDistance,
                                 String warnings, long timeMs) {
        public RoutingResult(List<Coordinate> route, double totalDistance, String warnings) {
            this(route, totalDistance, warnings, 0);
        }
    }
    public record NetworkStatsResult(int nodeCount, int edgeCount, double totalLength,
                                      double avgDegree, double density) {}

    /**
     * Pluggable routing backend for external engines.
     */
    @FunctionalInterface
    public interface RoutingBackend {
        RoutingResult findRoute(List<org.geotools.api.feature.simple.SimpleFeature> network,
                                 Coordinate start, Coordinate end, double snapTolerance);
    }

    private static RoutingBackend externalBackend;

    /**
     * Set an external routing backend (e.g., GraphHopper).
     */
    public static void setRoutingBackend(RoutingBackend backend) {
        externalBackend = backend;
    }

    /**
     * Get the current routing backend.
     */
    public static RoutingBackend getRoutingBackend() {
        return externalBackend;
    }

    /**
     * Find shortest path between two points.
     * Tries external backend first, falls back to Dijkstra.
     */
    public static RoutingResult shortestPath(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
                                              Coordinate start, Coordinate end, double snapTolerance) {
        long t0 = System.currentTimeMillis();

        // Try external backend
        if (externalBackend != null) {
            try {
                RoutingResult result = externalBackend.findRoute(lineFeatures, start, end, snapTolerance);
                if (result != null && !result.route().isEmpty()) {
                    return new RoutingResult(result.route(), result.totalDistance(),
                            result.warnings(), System.currentTimeMillis() - t0);
                }
            } catch (Exception ignored) { CatgisLogger.warn("OfflineRoutingEngine: operation failed", ignored); }
        }

        // Dijkstra fallback
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        if (nodes.size() < 2) return new RoutingResult(Collections.emptyList(), 0,
                "Insufficient nodes.", System.currentTimeMillis() - t0);

        int startIdx = findNearestNode(nodes, start);
        int endIdx = findNearestNode(nodes, end);
        if (startIdx < 0 || endIdx < 0) return new RoutingResult(Collections.emptyList(), 0,
                "Points outside network.", System.currentTimeMillis() - t0);

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

        if (dist[endIdx] == Double.MAX_VALUE) return new RoutingResult(Collections.emptyList(), 0,
                "No path found.", System.currentTimeMillis() - t0);
        List<Coordinate> route = new ArrayList<>();
        for (int at = endIdx; at != -1; at = prev[at]) route.add(nodes.get(at).coordinate());
        Collections.reverse(route);
        return new RoutingResult(route, dist[endIdx], "",
                System.currentTimeMillis() - t0);
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

    /**
     * Compute betweenness centrality for all nodes.
     * Higher values = more important nodes in the network.
     *
     * NOTE: This is a simplified Brandes algorithm implementation.
     * It uses a single predecessor per node for back-propagation.
     * For networks with multiple equal-cost shortest paths, the
     * results may be approximate. For rigorous betweenness centrality,
     * a full Brandes implementation with all-pairs shortest paths
     * and proper path counting would be needed.
     */
    public static double[] betweennessCentrality(List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
                                                   double snapTolerance) {
        List<NetworkAnalysisEngine.NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        int n = nodes.size();
        if (n == 0) return new double[0];

        double[][] adj = buildAdjacencyMatrix(lineFeatures, nodes);
        double[] centrality = new double[n];

        for (int s = 0; s < n; s++) {
            double[] dist = new double[n];
            int[] prev = new int[n];
            int[] sigma = new int[n];
            double[] delta = new double[n];
            boolean[] visited = new boolean[n];
            java.util.Arrays.fill(dist, Double.MAX_VALUE);
            java.util.Arrays.fill(prev, -1);
            java.util.Arrays.fill(sigma, 0);
            dist[s] = 0;
            sigma[s] = 1;

            java.util.List<Integer> stack = new java.util.ArrayList<>();

            for (int iter = 0; iter < n; iter++) {
                int u = -1;
                double minD = Double.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!visited[i] && dist[i] < minD) { minD = dist[i]; u = i; }
                }
                if (u < 0 || dist[u] == Double.MAX_VALUE) break;
                visited[u] = true;
                stack.add(u);
                for (int v = 0; v < n; v++) {
                    if (visited[v] || adj[u][v] >= Double.MAX_VALUE) continue;
                    double alt = dist[u] + adj[u][v];
                    if (alt < dist[v]) { dist[v] = alt; prev[v] = u; sigma[v] = sigma[u]; }
                    else if (alt == dist[v]) { sigma[v] += sigma[u]; }
                }
            }

            while (!stack.isEmpty()) {
                int w = stack.remove(stack.size() - 1);
                if (prev[w] >= 0 && sigma[w] > 0) {
                    delta[prev[w]] += (1.0 + delta[w]) * sigma[prev[w]] / (double) sigma[w];
                }
                if (w != s) centrality[w] += delta[w];
            }
        }

        double norm = n > 2 ? (n - 1.0) * (n - 2.0) : 1.0;
        for (int i = 0; i < n; i++) centrality[i] /= norm;
        return centrality;
    }

    /**
     * Compute connectivity metrics for the network.
     */
    public static java.util.Map<String, Double> connectivityAnalysis(
            List<org.geotools.api.feature.simple.SimpleFeature> lineFeatures,
            double snapTolerance) {

        double[][] matrix = allPairsShortestPaths(lineFeatures, snapTolerance);
        int n = matrix.length;

        int connectedPairs = 0;
        double totalDistance = 0;
        double maxDistance = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j] < Double.MAX_VALUE) {
                    connectedPairs++;
                    totalDistance += matrix[i][j];
                    if (matrix[i][j] > maxDistance) maxDistance = matrix[i][j];
                    if (matrix[i][j] < minDistance) minDistance = matrix[i][j];
                }
            }
        }

        int totalPairs = n * (n - 1) / 2;
        java.util.Map<String, Double> stats = new java.util.LinkedHashMap<>();
        stats.put("total_nodes", (double) n);
        stats.put("total_pairs", (double) totalPairs);
        stats.put("connected_pairs", (double) connectedPairs);
        stats.put("connectivity", totalPairs > 0 ? (double) connectedPairs / totalPairs * 100 : 0);
        stats.put("avg_distance", connectedPairs > 0 ? totalDistance / connectedPairs : 0);
        stats.put("max_distance", maxDistance);
        stats.put("min_distance", connectedPairs > 0 ? minDistance : 0);

        return stats;
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
