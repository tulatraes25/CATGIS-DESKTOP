package ar.com.catgis.climate;
import ar.com.catgis.core.model.Layer;

import org.locationtech.jts.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Automatic landform classification from DEM using geomorphons.
 * Classifies each DEM cell into one of 10 landform types:
 * PEAK, RIDGE, SHOULDER, SPUR, CONVEX_SLOPE, FLAT,
 * CONCAVE_SLOPE, HOLLOW, FOOTSLOPE, VALLEY, PIT
 * <p>
 * Algorithm: Pattern recognition in 8 directions at multiple scales,
 * based on the Geomorphons approach (Jasiewicz & Stepinski 2013).
 */
public final class LandformClassifier {

    private LandformClassifier() {}

    /** Landform types with names in Spanish */
    public enum Landform {
        PEAK("Cima / Pico"),
        RIDGE("Loma / Divisoria"),
        SHOULDER("Hombro"),
        SPUR("Espolón"),
        CONVEX_SLOPE("Ladera convexa"),
        FLAT("Superficie plana"),
        CONCAVE_SLOPE("Ladera cóncava"),
        HOLLOW("Depresión"),
        FOOTSLOPE("Pie de ladera"),
        VALLEY("Valle / Cárcava"),
        PIT("Hondonada");

        private final String spanish;
        Landform(String s) { this.spanish = s; }
        public String spanish() { return spanish; }
    }

    /**
     * Result of classification: labeled image + mapping to landform types.
     */
    public record ClassificationResult(
            int[][] labels,         // [row][col] → Landform.ordinal()
            int width,              // pixel width
            int height,             // pixel height
            double cellSize,        // DEM cell size in map units
            double west,            // West boundary (min X)
            double north,           // North boundary (max Y)
            int searchRadius,       // Radius used (pixels)
            double flatnessTreshold // Flatness threshold (DEM units)
    ) {
        public int getClass(int row, int col) { return labels[row][col]; }
        public Landform getLandform(int row, int col) {
            return Landform.values()[Math.max(0, Math.min(Landform.values().length - 1, labels[row][col]))];
        }
    }

    /**
     * Classify a DEM into landform types using geomorphons.
     *
     * @param dem      2D float array of elevation values [row][col]
     * @param cols     Number of columns
     * @param rows     Number of rows
     * @param cellSize DEM cell size (map units per pixel)
     * @param west     West boundary (min X)
     * @param north    North boundary (max Y)
     * @param radius   Search radius in pixels (3-50 recommended)
     * @param flatness Flatness threshold as fraction of elevation range (0.01-0.05)
     * @return ClassificationResult
     */
    public static ClassificationResult classify(
            float[][] dem, int cols, int rows,
            double cellSize, double west, double north,
            int radius, double flatness) {

        // Compute elevation range for flatness threshold
        float minEl = Float.MAX_VALUE, maxEl = -Float.MAX_VALUE;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                float v = dem[r][c];
                if (Float.isNaN(v) || Float.isInfinite(v)) continue;
                if (v < minEl) minEl = v;
                if (v > maxEl) maxEl = v;
            }
        float range = Math.max(maxEl - minEl, 1f);
        float flatThreshold = (float) (flatness * range);

        int[][] labels = new int[rows][cols];
        int[] directions = {0, 45, 90, 135, 180, 225, 270, 315}; // degrees

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float center = dem[r][c];
                if (Float.isNaN(center) || Float.isInfinite(center)) {
                    labels[r][c] = Landform.FLAT.ordinal();
                    continue;
                }
                labels[r][c] = classifyCell(dem, cols, rows,
                    r, c, center, radius, flatThreshold, directions);
            }
        }

        // Clean up: remove single-pixel noise
        labels = majorityFilter(labels, cols, rows, 2);

        return new ClassificationResult(labels, cols, rows,
            cellSize, west, north, radius, flatness);
    }

    /**
     * Classify a single DEM cell using the 8-direction ternary pattern.
     */
    private static int classifyCell(float[][] dem, int cols, int rows,
                                     int r, int c, float center,
                                     int radius, float flatThreshold,
                                     int[] directions) {

        // Build ternary pattern: for each of 8 directions, determine +1/-1/0
        int pattern = 0;
        boolean allFlat = true;

        for (int d = 0; d < 8; d++) {
            double angle = Math.toRadians(directions[d]);
            int dr = (int) Math.round(-Math.cos(angle) * radius);
            int dc = (int) Math.round(Math.sin(angle) * radius);

            int nr = r + dr;
            int nc = c + dc;

            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) {
                pattern = (pattern << 1) | 0; // treat as same (flat)
                continue;
            }

            float neighbor = dem[nr][nc];
            if (Float.isNaN(neighbor) || Float.isInfinite(neighbor)) {
                pattern = (pattern << 1) | 0;
                continue;
            }

            float diff = neighbor - center;
            int bit;
            if (diff > flatThreshold) {
                bit = 1;   // higher
                allFlat = false;
            } else if (diff < -flatThreshold) {
                bit = 2;   // lower
                allFlat = false;
            } else {
                bit = 0;   // same (flat)
            }
            pattern = (pattern << 2) | bit;
        }

        // All flat neighbors
        if (allFlat) return Landform.FLAT.ordinal();

        // Decode pattern into landform
        return decodeGeomorphon(pattern);
    }

    /**
     * Decode 16-bit geomorphon pattern (8 directions × 2 bits) into landform.
     * Pattern: [NW][W][SW][S][SE][E][NE][N] × 2 bits each
     * Bits: 0=flat, 1=higher, 2=lower
     */
    private static int decodeGeomorphon(int pattern) {
        // Count transitions and patterns
        int higherCount = 0, lowerCount = 0, flatCount = 0;

        // Also track runs of consecutive same-direction values
        int maxHigherRun = 0, maxLowerRun = 0, currentHigherRun = 0, currentLowerRun = 0;

        // Analyze each of 8 directions
        for (int i = 0; i < 8; i++) {
            int bits = (pattern >> (14 - i * 2)) & 3;
            if (bits == 1) { // higher
                higherCount++;
                currentHigherRun++;
                currentLowerRun = 0;
                maxHigherRun = Math.max(maxHigherRun, currentHigherRun);
            } else if (bits == 2) { // lower
                lowerCount++;
                currentLowerRun++;
                currentHigherRun = 0;
                maxLowerRun = Math.max(maxLowerRun, currentLowerRun);
            } else { // flat
                flatCount++;
                currentHigherRun = 0;
                currentLowerRun = 0;
            }
        }

        // Also wrap-around run (circular)
        // This handles cases where the run wraps from last direction to first
        // For simplicity, we use heuristic rules

        // Classification logic:
        if (lowerCount >= 7) return Landform.PEAK.ordinal();          // All neighbors lower → peak
        if (higherCount >= 7) return Landform.PIT.ordinal();           // All neighbors higher → pit
        if (flatCount >= 6) return Landform.FLAT.ordinal();            // Mostly flat
        
        if (lowerCount >= 5 && higherCount <= 2) return Landform.RIDGE.ordinal();   // Most neighbors lower → ridge
        if (higherCount >= 5 && lowerCount <= 2) return Landform.VALLEY.ordinal();  // Most neighbors higher → valley

        // Check for shoulder/convex (higher in 2-4 opposite directions then lower)
        if (higherCount >= 3 && lowerCount >= 3) {
            // If higher and lower alternate → slope
            // Count transitions between higher and lower
            return Landform.CONVEX_SLOPE.ordinal();
        }

        if (higherCount >= 2 && lowerCount >= 2) {
            // Mixed pattern with specific spatial arrangement
            // Check if higher directions are adjacent (spur/ridge) or opposite
            if (maxHigherRun >= 2) return Landform.SPUR.ordinal();
            if (maxLowerRun >= 2) return Landform.HOLLOW.ordinal();
            return Landform.CONCAVE_SLOPE.ordinal();
        }

        if (higherCount >= 1 && lowerCount >= 1) {
            return Landform.SHOULDER.ordinal();
        }

        if (higherCount > 0) return Landform.RIDGE.ordinal();
        if (lowerCount > 0) return Landform.FOOTSLOPE.ordinal();

        return Landform.FLAT.ordinal();
    }

    /**
     * Simple majority filter to remove noise.
     * For each cell, if the majority of neighbors disagree, replace with majority class.
     */
    private static int[][] majorityFilter(int[][] labels, int cols, int rows, int windowRadius) {
        int[][] result = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Map<Integer, Integer> counts = new HashMap<>();
                for (int dr = -windowRadius; dr <= windowRadius; dr++) {
                    for (int dc = -windowRadius; dc <= windowRadius; dc++) {
                        int nr = r + dr, nc = c + dc;
                        if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                        counts.merge(labels[nr][nc], 1, Integer::sum);
                    }
                }
                // Pick the most common class
                result[r][c] = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(labels[r][c]);
            }
        }
        return result;
    }

    /**
     * Convert classification result to a colored visualization image.
     */
    public static BufferedImage renderClassification(ClassificationResult result) {
        java.awt.Color[] COLORS = {
            new java.awt.Color(180, 0, 0),      // PEAK - red
            new java.awt.Color(220, 120, 0),    // RIDGE - orange
            new java.awt.Color(240, 200, 80),   // SHOULDER - yellow
            new java.awt.Color(200, 180, 100),  // SPUR - tan
            new java.awt.Color(160, 200, 140),  // CONVEX_SLOPE - light green
            new java.awt.Color(200, 210, 180),  // FLAT - beige
            new java.awt.Color(100, 180, 120),  // CONCAVE_SLOPE - green
            new java.awt.Color(80, 160, 180),   // HOLLOW - teal
            new java.awt.Color(60, 120, 200),   // FOOTSLOPE - blue
            new java.awt.Color(40, 60, 160),    // VALLEY - dark blue
            new java.awt.Color(100, 30, 120),   // PIT - purple
        };

        BufferedImage img = new BufferedImage(result.width(), result.height(),
            BufferedImage.TYPE_INT_RGB);
        for (int r = 0; r < result.height(); r++) {
            for (int c = 0; c < result.width(); c++) {
                int l = Math.max(0, Math.min(result.labels[r][c], COLORS.length - 1));
                img.setRGB(c, r, COLORS[l].getRGB());
            }
        }
        return img;
    }

    /**
     * Convert classification result to a vector polygon layer.
     */
    public static List<org.locationtech.jts.geom.Polygon> toPolygons(
            ClassificationResult result, boolean groupByClass) {

        int[][] labels = result.labels;
        int w = result.width(), h = result.height();
        double cellSize = result.cellSize();
        double west = result.west();
        double north = result.north();

        // Simple approach: create a polygon for each cell (simplified)
        // For production, use a proper region-growing algorithm
        List<org.locationtech.jts.geom.Polygon> polys = new ArrayList<>();
        GeometryFactory gf = new GeometryFactory();

        boolean[][] visited = new boolean[h][w];

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (visited[r][c]) continue;
                int landformClass = labels[r][c];
                visited[r][c] = true;

                // Simple flood-fill to group contiguous cells of same class
                List<int[]> region = new ArrayList<>();
                Queue<int[]> queue = new LinkedList<>();
                queue.add(new int[]{r, c});
                region.add(new int[]{r, c});

                while (!queue.isEmpty()) {
                    int[] p = queue.poll();
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int nr = p[0] + dr, nc = p[1] + dc;
                            if (nr < 0 || nr >= h || nc < 0 || nc >= w) continue;
                            if (visited[nr][nc]) continue;
                            if (labels[nr][nc] != landformClass) continue;
                            visited[nr][nc] = true;
                            queue.add(new int[]{nr, nc});
                            region.add(new int[]{nr, nc});
                        }
                    }
                }

                // Convert region to polygon (simplified: bounding box)
                // For production, use a proper concave hull / alpha shape
                if (region.size() >= 10) { // Minimum 10 cells to form a polygon
                    double minX = west + region.stream().mapToDouble(p -> p[1] * cellSize).min().orElse(0);
                    double maxX = west + region.stream().mapToDouble(p -> (p[1] + 1) * cellSize).max().orElse(0);
                    double minY = north - region.stream().mapToDouble(p -> (p[0] + 1) * cellSize).max().orElse(0);
                    double maxY = north - region.stream().mapToDouble(p -> p[0] * cellSize).min().orElse(0);

                    Coordinate[] coords = {
                        new Coordinate(minX, minY),
                        new Coordinate(maxX, minY),
                        new Coordinate(maxX, maxY),
                        new Coordinate(minX, maxY),
                        new Coordinate(minX, minY)
                    };
                    polys.add(gf.createPolygon(coords));
                }
            }
        }
        return polys;
    }
}
