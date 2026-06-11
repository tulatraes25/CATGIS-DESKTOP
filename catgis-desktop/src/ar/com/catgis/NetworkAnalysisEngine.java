package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;

import java.util.*;

public final class NetworkAnalysisEngine {

    private NetworkAnalysisEngine() {}

    public record NetworkPath(List<Coordinate> route, double totalDistance, String warnings) {}

    public record NetworkPoint(int featureIndex, Coordinate coordinate) {}

    public static NetworkPath shortestPath(List<SimpleFeature> lineFeatures, Coordinate start, Coordinate end, double snapTolerance) {
        List<NetworkPoint> allNodes = buildGraph(lineFeatures, snapTolerance);
        if (allNodes.size() < 2) return new NetworkPath(Collections.emptyList(), 0, "Menos de 2 nodos en la red.");
        int startIdx = findNearestNode(allNodes, start);
        int endIdx = findNearestNode(allNodes, end);
        if (startIdx < 0 || endIdx < 0) return new NetworkPath(Collections.emptyList(), 0, "Punto de inicio o fin fuera de la red.");
        double[][] adjacency = buildAdjacencyMatrix(lineFeatures, allNodes);
        double[] dist = new double[allNodes.size()];
        int[] prev = new int[allNodes.size()];
        boolean[] visited = new boolean[allNodes.size()];
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[startIdx] = 0;
        for (int iter = 0; iter < allNodes.size(); iter++) {
            int u = -1;
            double minDist = Double.MAX_VALUE;
            for (int i = 0; i < allNodes.size(); i++) {
                if (!visited[i] && dist[i] < minDist) { minDist = dist[i]; u = i; }
            }
            if (u < 0 || dist[u] == Double.MAX_VALUE) break;
            visited[u] = true;
            if (u == endIdx) break;
            for (int v = 0; v < allNodes.size(); v++) {
                if (visited[v] || adjacency[u][v] >= Double.MAX_VALUE) continue;
                double alt = dist[u] + adjacency[u][v];
                if (alt < dist[v]) { dist[v] = alt; prev[v] = u; }
            }
        }
        if (dist[endIdx] == Double.MAX_VALUE) return new NetworkPath(Collections.emptyList(), 0, "No se encontro ruta entre los puntos.");
        List<Coordinate> route = new ArrayList<>();
        for (int at = endIdx; at != -1; at = prev[at]) route.add(allNodes.get(at).coordinate());
        Collections.reverse(route);
        return new NetworkPath(route, dist[endIdx], "");
    }

    public static double[][] computeCostMatrix(List<SimpleFeature> lineFeatures, List<Coordinate> points, double snapTolerance) {
        double[][] matrix = new double[points.size()][points.size()];
        for (int i = 0; i < points.size(); i++) {
            for (int j = 0; j < points.size(); j++) {
                if (i == j) { matrix[i][j] = 0; continue; }
                NetworkPath path = shortestPath(lineFeatures, points.get(i), points.get(j), snapTolerance);
                matrix[i][j] = path.totalDistance() > 0 ? path.totalDistance() : Double.POSITIVE_INFINITY;
            }
        }
        return matrix;
    }

    private static List<NetworkPoint> buildGraph(List<SimpleFeature> lineFeatures, double snapTolerance) {
        Map<String, NetworkPoint> nodeMap = new LinkedHashMap<>();
        GeometryFactory gf = new GeometryFactory();
        for (SimpleFeature feature : lineFeatures) {
            Geometry geom = (Geometry) feature.getDefaultGeometry();
            if (geom == null) continue;
            for (Coordinate c : geom.getCoordinates()) {
                String key = snapCoordinate(c, snapTolerance);
                nodeMap.putIfAbsent(key, new NetworkPoint(nodeMap.size(), gf.createPoint(c).getCoordinate()));
            }
        }
        return new ArrayList<>(nodeMap.values());
    }

    private static String snapCoordinate(Coordinate c, double tolerance) {
        if (tolerance <= 0) return c.getX() + "," + c.getY();
        long sx = Math.round(c.getX() / tolerance);
        long sy = Math.round(c.getY() / tolerance);
        return sx + "," + sy;
    }

    private static double[][] buildAdjacencyMatrix(List<NetworkPoint> nodes) {
        int n = nodes.size();
        double[][] m = new double[n][n];
        for (int i = 0; i < n; i++) { Arrays.fill(m[i], Double.MAX_VALUE); m[i][i] = 0; }
        Map<String, Integer> keyToIdx = new HashMap<>();
        for (int i = 0; i < n; i++) keyToIdx.put(nodes.get(i).coordinate().getX() + "," + nodes.get(i).coordinate().getY(), i);
        return m;
    }

    private static int findNearestNode(List<NetworkPoint> nodes, Coordinate target) {
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < nodes.size(); i++) {
            double d = nodes.get(i).coordinate().distance(target);
            if (d < bestDist) { bestDist = d; best = i; }
        }
        return best;
    }

    public static Geometry buildRouteGeometry(List<Coordinate> route, GeometryFactory gf) {
        if (route == null || route.size() < 2) return null;
        return gf.createLineString(route.toArray(new Coordinate[0]));
    }

    /**
     * Find the closest node to a given point.
     */
    public static int findClosestNode(List<SimpleFeature> lineFeatures, Coordinate point, double snapTolerance) {
        List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        return findNearestNode(nodes, point);
    }

    /**
     * Find the K nearest nodes to a given point.
     */
    public static List<Integer> findKNearestNodes(List<SimpleFeature> lineFeatures, Coordinate point, int k, double snapTolerance) {
        List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        List<Integer> nearest = new ArrayList<>();
        for (int i = 0; i < Math.min(k, nodes.size()); i++) {
            double minDist = Double.MAX_VALUE;
            int minIdx = -1;
            for (int j = 0; j < nodes.size(); j++) {
                if (nearest.contains(j)) continue;
                double d = nodes.get(j).coordinate().distance(point);
                if (d < minDist) { minDist = d; minIdx = j; }
            }
            if (minIdx >= 0) nearest.add(minIdx);
        }
        return nearest;
    }

    /**
     * Compute nearest facility for multiple destinations.
     * Returns the nearest node index for each destination.
     */
    public static List<Integer> nearestFacility(List<SimpleFeature> lineFeatures,
                                                 List<Coordinate> facilities,
                                                 double snapTolerance) {
        List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        double[][] adj = buildAdjacencyMatrix(lineFeatures, nodes);
        List<Integer> nearestNodes = new ArrayList<>();

        for (Coordinate facility : facilities) {
            int facilityNode = findNearestNode(nodes, facility);
            if (facilityNode < 0) { nearestNodes.add(-1); continue; }

            // Find nearest connected node from the facility
            double minDist = Double.MAX_VALUE;
            int nearest = -1;
            for (int v = 0; v < nodes.size(); v++) {
                if (v != facilityNode && adj[facilityNode][v] < Double.MAX_VALUE) {
                    if (adj[facilityNode][v] < minDist) {
                        minDist = adj[facilityNode][v];
                        nearest = v;
                    }
                }
            }
            nearestNodes.add(nearest);
        }
        return nearestNodes;
    }

    /**
     * Compute network efficiency (ratio of direct distances to shortest path distances).
     */
    public static double computeEfficiency(List<SimpleFeature> lineFeatures, double snapTolerance) {
        double[][] matrix = allPairsShortestPaths(lineFeatures, snapTolerance);
        int n = matrix.length;
        double totalDirect = 0;
        double totalPath = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j] < Double.MAX_VALUE) {
                    totalPath += matrix[i][j];
                    // Approximate direct distance as Euclidean
                    List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
                    if (i < nodes.size() && j < nodes.size()) {
                        totalDirect += nodes.get(i).coordinate().distance(nodes.get(j).coordinate());
                    }
                }
            }
        }
        return totalPath > 0 ? totalDirect / totalPath : 0;
    }

    /**
     * Compute service area (isochrone) from a point.
     * Returns all nodes reachable within a given distance.
     */
    public static List<Coordinate> serviceArea(List<SimpleFeature> lineFeatures, Coordinate center,
                                                double maxDistance, double snapTolerance) {
        List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        if (nodes.isEmpty()) return Collections.emptyList();
        int centerIdx = findNearestNode(nodes, center);
        if (centerIdx < 0) return Collections.emptyList();

        double[][] adj = buildAdjacencyMatrix(lineFeatures, nodes);
        double[] dist = new double[nodes.size()];
        Arrays.fill(dist, Double.MAX_VALUE);
        dist[centerIdx] = 0;
        boolean[] visited = new boolean[nodes.size()];

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
     * Compute betweenness centrality for all nodes.
     * Higher values = more important nodes in the network.
     */
    public static double[] betweennessCentrality(List<SimpleFeature> lineFeatures, double snapTolerance) {
        List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
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
            Arrays.fill(dist, Double.MAX_VALUE);
            Arrays.fill(prev, -1);
            Arrays.fill(sigma, 0);
            dist[s] = 0;
            sigma[s] = 1;

            List<Integer> stack = new ArrayList<>();

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

            // Back-propagation
            while (!stack.isEmpty()) {
                int w = stack.remove(stack.size() - 1);
                if (prev[w] >= 0 && sigma[w] > 0) {
                    delta[prev[w]] += (1.0 + delta[w]) * sigma[prev[w]] / (double) sigma[w];
                }
                if (w != s) centrality[w] += delta[w];
            }
        }

        // Normalize
        double norm = n > 2 ? (n - 1.0) * (n - 2.0) : 1.0;
        for (int i = 0; i < n; i++) centrality[i] /= norm;

        return centrality;
    }

    /**
     * Compute all-pairs shortest paths.
     */
    public static double[][] allPairsShortestPaths(List<SimpleFeature> lineFeatures, double snapTolerance) {
        List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
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
    public record NetworkStats(int nodeCount, int edgeCount, double totalLength, double avgDegree, double density) {}

    public static NetworkStats computeStats(List<SimpleFeature> lineFeatures, double snapTolerance) {
        List<NetworkPoint> nodes = buildGraph(lineFeatures, snapTolerance);
        double totalLength = 0;
        for (SimpleFeature f : lineFeatures) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null) totalLength += g.getLength();
        }
        int edgeCount = 0;
        for (SimpleFeature f : lineFeatures) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g != null) edgeCount += g.getCoordinates().length - 1;
        }
        double avgDegree = nodes.isEmpty() ? 0 : (double) edgeCount * 2 / nodes.size();
        double density = nodes.size() > 1 ? (double) edgeCount / (nodes.size() * (nodes.size() - 1)) : 0;
        return new NetworkStats(nodes.size(), edgeCount, totalLength, avgDegree, density);
    }

    private static double[][] buildAdjacencyMatrix(List<SimpleFeature> lineFeatures, List<NetworkPoint> nodes) {
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
                String k1 = coords[i].getX() + "," + coords[i].getY();
                String k2 = coords[i+1].getX() + "," + coords[i+1].getY();
                Integer idx1 = keyToIdx.get(k1);
                Integer idx2 = keyToIdx.get(k2);
                if (idx1 != null && idx2 != null) {
                    double d = coords[i].distance(coords[i+1]);
                    m[idx1][idx2] = Math.min(m[idx1][idx2], d);
                    m[idx2][idx1] = Math.min(m[idx2][idx1], d);
                }
            }
        }
        return m;
    }
}
