package ar.com.catgis.analysis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * pgRouting integration service for PostgreSQL network analysis.
 * <p>
 * Executes pgRouting SQL functions (dijkstra, astar, driving_distance,
 * tsp) against a PostgreSQL database with the pgrouting extension installed.
 * Requires a working PostGIS/pgrouting database connection.
 * </p>
 *
 * @see <a href="https://pgrouting.org/">pgRouting</a>
 */
public final class PgRoutingService {

    private PgRoutingService() {}

    public record PgRouteStep(int seq, int node, int edge, double cost, double aggCost,
                               String geomWkt) {}

    /**
     * Compute shortest path using pgr_dijkstra.
     *
     * @param dbUrl      JDBC URL (e.g., jdbc:postgresql://localhost:5432/gis)
     * @param user       database user
     * @param password   database password
     * @param edgeTable  table with source, target, cost columns
     * @param sourceId   start node ID
     * @param targetId   end node ID
     * @param directed   whether graph is directed
     */
    public static List<PgRouteStep> dijkstra(String dbUrl, String user, String password,
                                              String edgeTable, int sourceId, int targetId,
                                              boolean directed) throws Exception {
        String sql = "SELECT seq, node, edge, cost, agg_cost, ST_AsText(geom) as geom_wkt "
                + "FROM pgr_dijkstra("
                + "'SELECT id, source, target, cost"
                + (directed ? ", reverse_cost" : "")
                + " FROM " + edgeTable + "', "
                + sourceId + ", " + targetId + ", " + directed + ") AS di "
                + "LEFT JOIN " + edgeTable + " ON di.edge = " + edgeTable + ".id "
                + "ORDER BY seq";

        return executeRoutingQuery(dbUrl, user, password, sql);
    }

    /**
     * Compute service area using pgr_drivingDistance.
     */
    public static List<PgRouteStep> drivingDistance(String dbUrl, String user, String password,
                                                     String edgeTable, int sourceId,
                                                     double maxCost) throws Exception {
        String sql = "SELECT seq, node, edge, cost, agg_cost, ST_AsText(geom) as geom_wkt "
                + "FROM pgr_drivingDistance("
                + "'SELECT id, source, target, cost FROM " + edgeTable + "', "
                + sourceId + ", " + maxCost + ", false) AS dd "
                + "LEFT JOIN " + edgeTable + " ON dd.edge = " + edgeTable + ".id "
                + "ORDER BY seq";

        return executeRoutingQuery(dbUrl, user, password, sql);
    }

    /**
     * Compute K shortest paths using pgr_ksp.
     */
    public static List<List<PgRouteStep>> kShortestPaths(String dbUrl, String user,
                                                           String password, String edgeTable,
                                                           int sourceId, int targetId,
                                                           int k) throws Exception {
        String sql = "SELECT path_id, seq, node, edge, cost, agg_cost, ST_AsText(geom) as geom_wkt "
                + "FROM pgr_ksp("
                + "'SELECT id, source, target, cost FROM " + edgeTable + "', "
                + sourceId + ", " + targetId + ", " + k + ", false) AS ksp "
                + "LEFT JOIN " + edgeTable + " ON ksp.edge = " + edgeTable + ".id "
                + "ORDER BY path_id, seq";

        return executeRoutingQueryWithPaths(dbUrl, user, password, sql);
    }

    /**
     * Find the nearest edge to a point.
     */
    public static int nearestEdge(String dbUrl, String user, String password,
                                   String edgeTable, double lon, double lat) throws Exception {
        String sql = "SELECT id FROM " + edgeTable
                + " ORDER BY geom <-> ST_SetSRID(ST_MakePoint("
                + lon + ", " + lat + "), 4326) LIMIT 1";

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    /**
     * Check if pgRouting extension is installed.
     */
    public static boolean isPgRoutingAvailable(String dbUrl, String user, String password) {
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT 1 FROM pg_extension WHERE extname = 'pgrouting'")) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * List available edge tables suitable for pgRouting (have source, target, cost columns).
     */
    public static List<String> listRoutingTables(String dbUrl, String user, String password)
            throws Exception {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT table_name FROM information_schema.columns "
                + "WHERE column_name IN ('source', 'target', 'cost') "
                + "GROUP BY table_name HAVING COUNT(DISTINCT column_name) >= 2 "
                + "ORDER BY table_name";

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        }
        return tables;
    }

    // --- Private helpers ---

    private static List<PgRouteStep> executeRoutingQuery(
            String dbUrl, String user, String password, String sql) throws Exception {
        List<PgRouteStep> steps = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                steps.add(new PgRouteStep(
                        rs.getInt("seq"),
                        rs.getInt("node"),
                        rs.getInt("edge"),
                        rs.getDouble("cost"),
                        rs.getDouble("agg_cost"),
                        rs.getString("geom_wkt")));
            }
        }
        return steps;
    }

    private static List<List<PgRouteStep>> executeRoutingQueryWithPaths(
            String dbUrl, String user, String password, String sql) throws Exception {
        List<List<PgRouteStep>> paths = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<PgRouteStep> currentPath = new ArrayList<>();
            int currentPathId = -1;
            while (rs.next()) {
                int pathId = rs.getInt("path_id");
                if (pathId != currentPathId) {
                    if (!currentPath.isEmpty()) {
                        paths.add(currentPath);
                        currentPath = new ArrayList<>();
                    }
                    currentPathId = pathId;
                }
                currentPath.add(new PgRouteStep(
                        rs.getInt("seq"),
                        rs.getInt("node"),
                        rs.getInt("edge"),
                        rs.getDouble("cost"),
                        rs.getDouble("agg_cost"),
                        rs.getString("geom_wkt")));
            }
            if (!currentPath.isEmpty()) paths.add(currentPath);
        }
        return paths;
    }
}
