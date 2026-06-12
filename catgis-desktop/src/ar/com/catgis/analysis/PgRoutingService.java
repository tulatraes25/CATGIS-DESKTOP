package ar.com.catgis.analysis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * pgRouting integration for network analysis via PostgreSQL.
 * <p>
 * Requires pgRouting extension installed in the target database.
 * Validates table names against the database catalog to prevent SQL injection.
 * </p>
 */
public final class PgRoutingService {

    private PgRoutingService() {}

    public record PgRouteStep(int seq, int node, int edge, double cost,
                               double aggCost, String geomWkt) {}

    /**
     * Check if pgRouting extension is installed.
     */
    public static boolean isAvailable(String dbUrl, String user, String password) {
        try (Connection c = DriverManager.getConnection(dbUrl, user, password);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT 1 FROM pg_extension WHERE extname = 'pgrouting'")) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * List tables with source/target/cost columns suitable for routing.
     */
    public static List<String> listRoutingTables(String dbUrl, String user, String password)
            throws SQLException {
        List<String> tables = new ArrayList<>();
        String sql = "SELECT table_schema || '.' || table_name AS fqn "
                + "FROM information_schema.columns "
                + "WHERE column_name IN ('source', 'target', 'cost') "
                + "GROUP BY table_schema, table_name "
                + "HAVING COUNT(DISTINCT column_name) >= 2 "
                + "ORDER BY fqn";
        try (Connection c = DriverManager.getConnection(dbUrl, user, password);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) tables.add(rs.getString("fqn"));
        }
        return tables;
    }

    /**
     * Shortest path using pgr_dijkstra.
     * @param qualifiedTable schema-qualified table name (validated against catalog)
     */
    public static List<PgRouteStep> dijkstra(String dbUrl, String user, String password,
                                              String qualifiedTable, int sourceId,
                                              int targetId) throws Exception {
        validateTable(dbUrl, user, password, qualifiedTable);

        String sql = "SELECT seq, node, edge, cost, agg_cost, ST_AsText(geom) AS geom_wkt "
                + "FROM pgr_dijkstra("
                + "'SELECT id, source, target, cost FROM " + qualifiedTable + "', "
                + "?, ?, false) AS di "
                + "LEFT JOIN " + qualifiedTable + " ON di.edge = " + qualifiedTable + ".id "
                + "ORDER BY seq";

        List<PgRouteStep> steps = new ArrayList<>();
        try (Connection c = DriverManager.getConnection(dbUrl, user, password);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            ps.setInt(2, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    steps.add(new PgRouteStep(
                            rs.getInt("seq"), rs.getInt("node"), rs.getInt("edge"),
                            rs.getDouble("cost"), rs.getDouble("agg_cost"),
                            rs.getString("geom_wkt")));
                }
            }
        }
        return steps;
    }

    /**
     * Validate a table name against SQL injection. Package-private for testing.
     */
    static boolean isValidTableName(String name) {
        return name != null && name.matches("^[a-zA-Z_][a-zA-Z0-9_\\.]*$");
    }

    private static void validateTable(String dbUrl, String user, String password,
                                       String qualifiedTable) throws Exception {
        if (!isValidTableName(qualifiedTable)) {
            throw new IllegalArgumentException("Invalid table name: " + qualifiedTable);
        }
        List<String> valid = listRoutingTables(dbUrl, user, password);
        if (!valid.contains(qualifiedTable)) {
            throw new IllegalArgumentException(
                    "Table '" + qualifiedTable + "' not found or missing routing columns");
        }
    }
}
