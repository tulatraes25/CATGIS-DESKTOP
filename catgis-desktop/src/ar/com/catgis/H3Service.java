package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * H3-like hexagonal indexing service for CATGIS.
 * Pure Java implementation (no external dependencies).
 */
public final class H3Service {

    private H3Service() {}

    public record HexBin(String hexIndex, int count, double centerX, double centerY) {}

    /**
     * Compute hex index for a coordinate at given resolution.
     * Resolution: 1=coarse, 10=fine.
     */
    public static String latLngToH3(double lat, double lng, int resolution) {
        double cellSize = getCellSize(resolution);
        int col = (int) Math.floor(lng / cellSize);
        int row = (int) Math.floor(lat / cellSize);
        return "h3_" + row + "_" + col;
    }

    /**
     * Get hex cell boundary as a polygon.
     */
    public static Polygon h3ToBoundary(String h3Index, int resolution) {
        double cellSize = getCellSize(resolution);
        String[] parts = h3Index.split("_");
        if (parts.length < 3) return null;

        int row = Integer.parseInt(parts[1]);
        int col = Integer.parseInt(parts[2]);

        double cx = col * cellSize;
        double cy = row * cellSize;
        double hw = cellSize / 2;
        double hh = cellSize * Math.sqrt(3) / 2;

        Coordinate[] coords = new Coordinate[7];
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i - Math.PI / 6;
            coords[i] = new Coordinate(cx + hw * Math.cos(angle), cy + hh * Math.sin(angle));
        }
        coords[6] = new Coordinate(coords[0].x, coords[0].y);

        return GF.createPolygon(coords);
    }

    /**
     * Bin points into hexagonal cells.
     */
    public static List<HexBin> hexBin(List<SimpleFeature> points, int resolution) {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, double[]> centers = new HashMap<>();
        double cellSize = getCellSize(resolution);

        for (SimpleFeature f : points) {
            Geometry g = (Geometry) f.getDefaultGeometry();
            if (g == null) continue;
            Point p = g instanceof Point pt ? pt : g.getCentroid();

            int col = (int) Math.floor(p.getX() / cellSize);
            int row = (int) Math.floor(p.getY() / cellSize);
            String key = "h3_" + row + "_" + col;
            counts.merge(key, 1, Integer::sum);
            centers.putIfAbsent(key, new double[]{p.getX(), p.getY()});
        }

        List<HexBin> bins = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            double[] center = centers.get(entry.getKey());
            bins.add(new HexBin(entry.getKey(), entry.getValue(), center[0], center[1]));
        }
        return bins;
    }

    /**
     * Get all hex cells within a bounding box.
     */
    public static List<String> polygonToH3(Envelope envelope, int resolution) {
        List<String> cells = new ArrayList<>();
        double cellSize = getCellSize(resolution);

        for (double lat = envelope.getMinY(); lat <= envelope.getMaxY(); lat += cellSize) {
            for (double lng = envelope.getMinX(); lng <= envelope.getMaxX(); lng += cellSize) {
                String cell = latLngToH3(lat, lng, resolution);
                if (!cells.contains(cell)) cells.add(cell);
            }
        }
        return cells;
    }

    /**
     * Get hex cell center coordinates.
     */
    public static double[] h3ToCenter(String h3Index, int resolution) {
        String[] parts = h3Index.split("_");
        if (parts.length < 3) return null;
        double cellSize = getCellSize(resolution);
        int row = Integer.parseInt(parts[1]);
        int col = Integer.parseInt(parts[2]);
        return new double[]{col * cellSize + cellSize / 2, row * cellSize + cellSize / 2};
    }

    private static double getCellSize(int resolution) {
        // Approximate cell size in degrees based on resolution
        // Resolution 1 ≈ 1107km, Resolution 10 ≈ 25m
        return 110.0 / Math.pow(2, resolution);
    }

    private static final GeometryFactory GF = new GeometryFactory();
}
