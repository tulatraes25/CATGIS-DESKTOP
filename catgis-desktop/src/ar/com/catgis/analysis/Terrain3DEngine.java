package ar.com.catgis.analysis;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D terrain engine foundation for CATGIS.
 * <p>
 * Provides height map generation, terrain profile calculation,
 * slope/aspect shading, and hillshade from DEM rasters.
 * Foundation for future WebGL/OpenGL 3D rendering.
 * </p>
 */
public final class Terrain3DEngine {

    private Terrain3DEngine() {}

    /** 3D coordinate with elevation. */
    public record Point3D(double x, double y, double z) {}

    /** Terrain profile result with elevation series. */
    public record TerrainProfile(List<Point3D> points, double minElevation,
                                  double maxElevation, double totalDistance,
                                  double avgSlope) {}

    // ─── Height Map ────────────────────────────────────────────────────

    /**
     * Extract a height map from a DEM raster.
     * Returns a 2D float array [rows][cols] with elevation values.
     */
    public static float[][] extractHeightMap(BufferedImage dem) {
        if (dem == null) return new float[0][0];
        int w = dem.getWidth();
        int h = dem.getHeight();
        float[][] heightMap = new float[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                heightMap[y][x] = dem.getRaster().getSample(x, y, 0);
            }
        }
        return heightMap;
    }

    /**
     * Normalize height map values to 0-1 range.
     */
    public static float[][] normalizeHeightMap(float[][] heightMap) {
        if (heightMap.length == 0) return heightMap;
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float[] row : heightMap) {
            for (float v : row) {
                if (v < min) min = v;
                if (v > max) max = v;
            }
        }
        float range = max - min;
        if (range == 0) return heightMap;

        int h = heightMap.length;
        int w = heightMap[0].length;
        float[][] normalized = new float[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                normalized[y][x] = (heightMap[y][x] - min) / range;
            }
        }
        return normalized;
    }

    // ─── Hillshade ─────────────────────────────────────────────────────

    /**
     * Generate hillshade from a height map.
     *
     * @param heightMap normalized height map [0-1]
     * @param azimuth   light azimuth in degrees (0=N, 90=E)
     * @param altitude  light altitude in degrees (0=horizon, 90=zenith)
     * @param cellSize  cell size in map units
     * @param zFactor   vertical exaggeration
     * @return grayscale image with hillshade values 0-255
     */
    public static BufferedImage hillshade(float[][] heightMap, double azimuth,
                                           double altitude, double cellSize,
                                           double zFactor) {
        if (heightMap.length == 0) return null;
        int rows = heightMap.length;
        int cols = heightMap[0].length;

        double azRad = Math.toRadians(360 - azimuth + 90);
        double altRad = Math.toRadians(altitude);

        BufferedImage shade = new BufferedImage(cols, rows, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = shade.getRaster();

        for (int y = 1; y < rows - 1; y++) {
            for (int x = 1; x < cols - 1; x++) {
                double dzdx = ((heightMap[y][x + 1] - heightMap[y][x - 1]) / (2 * cellSize)) * zFactor;
                double dzdy = ((heightMap[y + 1][x] - heightMap[y - 1][x]) / (2 * cellSize)) * zFactor;

                double slope = Math.atan(Math.sqrt(dzdx * dzdx + dzdy * dzdy));
                double aspect = Math.atan2(dzdy, -dzdx);
                if (aspect < 0) aspect += 2 * Math.PI;

                double hs = Math.cos(altRad) * Math.cos(slope)
                        + Math.sin(altRad) * Math.sin(slope)
                        * Math.cos(azRad - aspect);

                int val = (int) Math.max(0, Math.min(255, hs * 255));
                raster.setSample(x, y, 0, val);
            }
        }
        return shade;
    }

    // ─── Terrain Profile ───────────────────────────────────────────────

    /**
     * Sample terrain profile along a line.
     */
    public static TerrainProfile profile(float[][] heightMap, double resolution,
                                          double startX, double startY,
                                          double endX, double endY) {
        List<Point3D> points = new ArrayList<>();
        double dx = endX - startX;
        double dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        int steps = Math.max(2, (int) (distance / resolution));

        double minElev = Double.MAX_VALUE;
        double maxElev = -Double.MAX_VALUE;
        double totalDist = 0;
        double prevElev = Double.NaN;

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = startX + dx * t;
            double y = startY + dy * t;

            int col = (int) x;
            int row = (int) y;
            if (row >= 0 && row < heightMap.length && col >= 0 && col < heightMap[0].length) {
                double elev = heightMap[row][col];
                points.add(new Point3D(x, y, elev));
                if (elev < minElev) minElev = elev;
                if (elev > maxElev) maxElev = elev;
                if (!Double.isNaN(prevElev)) {
                    totalDist += Math.sqrt(resolution * resolution + (elev - prevElev) * (elev - prevElev));
                }
                prevElev = elev;
            }
        }

        double avgSlope = points.size() > 1
                ? Math.abs(points.get(points.size() - 1).z - points.get(0).z) / distance
                : 0;

        return new TerrainProfile(points, minElev, maxElev, totalDist, avgSlope);
    }

    // ─── 3D Transform ──────────────────────────────────────────────────

    /**
     * Project a 3D point to 2D screen coordinates (isometric view).
     */
    public static double[] project3Dto2D(double x, double y, double z,
                                          double scale, double angleDeg,
                                          int screenWidth, int screenHeight) {
        double angle = Math.toRadians(angleDeg);
        double screenX = (x - y) * Math.cos(angle) * scale + screenWidth / 2.0;
        double screenY = (x + y) * Math.sin(angle) * scale - z * scale * 0.5 + screenHeight / 2.0;
        return new double[]{screenX, screenY};
    }

    /**
     * Compute slope in degrees at a point.
     */
    public static double slopeAt(float[][] heightMap, int x, int y, double cellSize) {
        if (heightMap.length == 0 || x <= 0 || y <= 0
                || x >= heightMap[0].length - 1 || y >= heightMap.length - 1) return 0;

        double dzdx = (heightMap[y][x + 1] - heightMap[y][x - 1]) / (2 * cellSize);
        double dzdy = (heightMap[y + 1][x] - heightMap[y - 1][x]) / (2 * cellSize);
        return Math.toDegrees(Math.atan(Math.sqrt(dzdx * dzdx + dzdy * dzdy)));
    }

    /**
     * Compute aspect in degrees (0=N, 90=E, 180=S, 270=W) at a point.
     */
    public static double aspectAt(float[][] heightMap, int x, int y, double cellSize) {
        if (heightMap.length == 0 || x <= 0 || y <= 0
                || x >= heightMap[0].length - 1 || y >= heightMap.length - 1) return -1;

        double dzdx = (heightMap[y][x + 1] - heightMap[y][x - 1]) / (2 * cellSize);
        double dzdy = (heightMap[y + 1][x] - heightMap[y - 1][x]) / (2 * cellSize);
        double aspect = Math.toDegrees(Math.atan2(dzdy, -dzdx));
        return (aspect < 0) ? aspect + 360 : aspect;
    }
}
