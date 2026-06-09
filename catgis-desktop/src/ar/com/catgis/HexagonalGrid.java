package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;

import java.util.*;

/**
 * H3-like hexagonal grid indexing using JTS.
 * Provides hexagonal binning for spatial analysis.
 */
public final class HexagonalGrid {

    private HexagonalGrid() {}

    public record HexCell(int row, int col, int count, double centerX, double centerY) {}

    /**
     * Create a hexagonal grid covering an envelope.
     * Returns hex cells with feature counts.
     */
    public static List<HexCell> createHexGrid(Envelope envelope, double cellSize, List<SimpleFeature> points) {
        List<HexCell> cells = new ArrayList<>();
        if (envelope == null || cellSize <= 0) return cells;

        double hexWidth = cellSize * 2;
        double hexHeight = cellSize * Math.sqrt(3);
        int cols = (int) Math.ceil(envelope.getWidth() / hexWidth) + 2;
        int rows = (int) Math.ceil(envelope.getHeight() / hexHeight) + 2;

        double startX = envelope.getMinX() - hexWidth;
        double startY = envelope.getMinY() - hexHeight;

        // Build grid centers
        Map<String, Integer> countMap = new HashMap<>();
        Map<String, double[]> centerMap = new HashMap<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double cx = startX + c * hexWidth + (r % 2 == 0 ? 0 : hexWidth / 2);
                double cy = startY + r * hexHeight;
                String key = r + "," + c;
                countMap.put(key, 0);
                centerMap.put(key, new double[]{cx, cy});
            }
        }

        // Count points in each hex cell
        if (points != null) {
            for (SimpleFeature f : points) {
                Geometry g = (Geometry) f.getDefaultGeometry();
                if (g == null) continue;
                Point p = g instanceof Point ? (Point) g : g.getCentroid();

                int col = (int) Math.round((p.getX() - startX) / hexWidth);
                int row = (int) Math.round((p.getY() - startY) / hexHeight);
                if (col % 2 != 0 && row % 2 == 0) row--;
                if (col % 2 == 0 && row % 2 != 0) row--;

                String key = row + "," + col;
                countMap.merge(key, 1, Integer::sum);
            }
        }

        // Build result cells
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() > 0) {
                String[] parts = entry.getKey().split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                double[] center = centerMap.get(entry.getKey());
                cells.add(new HexCell(row, col, entry.getValue(), center[0], center[1]));
            }
        }

        return cells;
    }

    /**
     * Convert hex grid to polygon geometries.
     */
    public static List<Geometry> hexCellsToPolygons(List<HexCell> cells, double cellSize) {
        List<Geometry> polygons = new ArrayList<>();
        if (cells == null) return polygons;

        double hexWidth = cellSize * 2;
        double hexHeight = cellSize * Math.sqrt(3);
        GeometryFactory gf = new GeometryFactory();

        for (HexCell cell : cells) {
            double cx = cell.centerX();
            double cy = cell.centerY();
            Coordinate[] ring = new Coordinate[7];
            for (int i = 0; i < 6; i++) {
                double angle = Math.PI / 3 * i - Math.PI / 6;
                ring[i] = new Coordinate(cx + hexWidth / 2 * Math.cos(angle),
                        cy + hexHeight / 2 * Math.sin(angle));
            }
            ring[6] = new Coordinate(ring[0].x, ring[0].y);
            polygons.add(gf.createPolygon(ring));
        }
        return polygons;
    }

    /**
     * Compute H3-like index for a coordinate.
     * Returns a string like "rXcY" representing the hex cell.
     */
    public static String h3Index(double x, double y, double cellSize, Envelope bounds) {
        double hexWidth = cellSize * 2;
        double hexHeight = cellSize * Math.sqrt(3);
        int col = (int) Math.round((x - bounds.getMinX()) / hexWidth);
        int row = (int) Math.round((y - bounds.getMinY()) / hexHeight);
        return "r" + row + "c" + col;
    }

    /**
     * Resolve H3 index to center coordinates.
     */
    public static double[] h3ToCenter(String index, double cellSize, Envelope bounds) {
        double hexWidth = cellSize * 2;
        double hexHeight = cellSize * Math.sqrt(3);
        int row = Integer.parseInt(index.substring(1, index.indexOf('c')));
        int col = Integer.parseInt(index.substring(index.indexOf('c') + 1));
        double cx = bounds.getMinX() + col * hexWidth + (row % 2 == 0 ? 0 : hexWidth / 2);
        double cy = bounds.getMinY() + row * hexHeight;
        return new double[]{cx, cy};
    }
}
