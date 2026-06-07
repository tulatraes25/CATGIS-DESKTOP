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
        double[][] adjacency = buildAdjacencyMatrix(allNodes);
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
}
