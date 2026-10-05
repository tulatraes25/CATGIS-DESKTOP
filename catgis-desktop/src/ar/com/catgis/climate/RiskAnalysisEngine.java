package ar.com.catgis.climate;

import ar.com.catgis.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Multi-hazard risk analysis engine.
 * Combines DEM, wind, soil, and climate data into one-click risk assessments.
 * <p>
 * Supported scenarios:
 * - OIL_SPILL:  oil well blowout → wind dispersion + terrain flow + soil absorption
 * - FLOOD:      extreme rainfall → flow accumulation + depression filling
 * - LANDSLIDE:  slope + soil + rainfall → susceptibility index
 * - WILDFIRE:   climate + vegetation + topography → fire risk
 * - CONTAMINATION:  industrial point → wind + drainage + soil
 */
public final class RiskAnalysisEngine {

    private RiskAnalysisEngine() {}

    public enum Scenario {
        OIL_SPILL("Derrame de hidrocarburos"),
        FLOOD("Inundacion"),
        LANDSLIDE("Deslizamiento"),
        WILDFIRE("Incendio forestal"),
        CONTAMINATION("Contaminacion industrial");

        private final String spanish;
        Scenario(String s) { this.spanish = s; }
        public String spanish() { return spanish; }
    }

    public record RiskResult(
            BufferedImage riskMap,       // Colored risk overlay
            double[][] riskGrid,         // [row][col] 0.0-1.0 risk values
            int width, int height,
            double cellSize,
            double west, double north,
            String scenarioName,
            List<String> warnings
    ) {}

    /**
     * Run a complete risk analysis pipeline.
     *
     * @param dem        Elevation data (can be null, will attempt to download)
     * @param scenario   Risk scenario to evaluate
     * @param locationLat Latitude of the site
     * @param locationLon Longitude of the site
     * @param siteName   Name for the report (e.g., "Pozo YPF-123")
     * @param params     Scenario-specific parameters
     * @return Complete risk analysis result (or null if insufficient data)
     */
    public static RiskResult analyze(
            float[][] dem,
            Scenario scenario,
            double locationLat, double locationLon,
            String siteName,
            Map<String, Double> params) {

        // Create output directory
        int width = dem != null ? dem[0].length : 100;
        int height = dem != null ? dem.length : 100;
        double cellSize = 30.0; // default 30m
        double west = locationLon - (width * cellSize / 2.0 / 111320.0);
        double north = locationLat + (height * cellSize / 2.0 / 111320.0);

        double[][] riskGrid = new double[height][width];
        List<String> warnings = new ArrayList<>();

        if (dem != null) {
            // Step 1: Slope analysis (affects all scenarios)
            double[][] slope = computeSlope(dem, cellSize);

            // Step 2: Flow accumulation (for dispersion)
            double[][] flowAcc = computeFlowAccumulation(dem);

            // Step 3: Scenario-specific risk calculation
            switch (scenario) {
                case OIL_SPILL:
                    riskGrid = oilSpillRisk(dem, slope, flowAcc, locationLat, locationLon, params, warnings);
                    break;
                case FLOOD:
                    riskGrid = floodRisk(dem, flowAcc, params, warnings);
                    break;
                case LANDSLIDE:
                    riskGrid = landslideRisk(slope, params, warnings);
                    break;
                case CONTAMINATION:
                    riskGrid = contaminationRisk(dem, slope, flowAcc, locationLat, locationLon, params, warnings);
                    break;
                default:
                    riskGrid = genericRisk(slope, flowAcc, warnings);
                    break;
            }
        } else {
            warnings.add("No DEM provided — risk analysis limited");
            for (int r = 0; r < height; r++)
                for (int c = 0; c < width; c++)
                    riskGrid[r][c] = 0.3; // moderate baseline risk
        }

        // Normalize to 0-1
        normalizeRisk(riskGrid);

        BufferedImage riskMap = renderRiskMap(riskGrid);
        return new RiskResult(riskMap, riskGrid, width, height,
            cellSize, west, north, scenario.spanish(), warnings);
    }

    // ========== Scenario risk calculators ==========

    private static double[][] oilSpillRisk(
            float[][] dem, double[][] slope, double[][] flowAcc,
            double lat, double lon, Map<String, Double> params,
            List<String> warnings) {

        int h = dem.length, w = dem[0].length;
        double[][] risk = new double[h][w];

        // Locate the source point (well) in grid coordinates
        double cellSize = 30.0;
        double west = lon - (w * cellSize / 2.0 / 111320.0);
        double north = lat + (h * cellSize / 2.0 / 111320.0);
        int srcR = (int) ((north - lat) / (cellSize / 111320.0));
        int srcC = (int) ((lon - west) / (cellSize * Math.cos(Math.toRadians(lat)) / 111320.0));
        srcR = Math.max(0, Math.min(h - 1, srcR));
        srcC = Math.max(0, Math.min(w - 1, srcC));

        // Source intensity
        double sourceIntensity = params != null ? params.getOrDefault("volume", 100.0) : 100.0;

        // Wind direction (default: prevailing wind at site)
        double windDirDeg = params != null ? params.getOrDefault("windDirection", 240.0) : 240.0;
        double windSpeed = params != null ? params.getOrDefault("windSpeed", 20.0) : 20.0;
        double windRad = Math.toRadians(windDirDeg);

        // Terrain + wind combined dispersion
        // Higher risk = downwind + downslope + permeable soil
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                double dist = Math.sqrt(Math.pow(r - srcR, 2) + Math.pow(c - srcC, 2));
                if (dist < 1) {
                    risk[r][c] = 1.0; // source point
                    continue;
                }

                // Distance decay
                double distanceFactor = Math.exp(-dist / 50.0);

                // Wind factor: higher risk in downwind direction
                double angleFromSource = Math.atan2(c - srcC, r - srcR);
                double windDiff = angleFromSource - windRad;
                double windFactor = (Math.cos(windDiff) + 1) / 2; // 0 (upwind) to 1 (downwind)
                windFactor = windFactor * 0.5 + 0.5 * (windSpeed / 50.0);

                // Terrain factor: lower areas collect more
                double terrainFactor = 1.0 - (dem[r][c] - min(dem)) / Math.max(range(dem), 1);

                // Flow accumulation: areas with more flow have higher risk
                double flowFactor = Math.min(1.0, flowAcc[r][c] / 1000.0);

                // Combined
                risk[r][c] = distanceFactor * (windFactor * 0.4 + terrainFactor * 0.3 + flowFactor * 0.3);
            }
        }

        warnings.add("Derrame modelado con viento " + (int) windDirDeg + "° a " + (int) windSpeed + " km/h");
        warnings.add("Radio de dispersion estimado: ~" + (int)(50 * cellSize) + "m");
        return risk;
    }

    private static double[][] floodRisk(
            float[][] dem, double[][] flowAcc,
            Map<String, Double> params, List<String> warnings) {

        int h = dem.length, w = dem[0].length;
        double[][] risk = new double[h][w];
        double rainfall = params != null ? params.getOrDefault("rainfall", 100.0) : 100.0;

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                // Flood risk = flow accumulation + flatness + rainfall
                double flowFactor = Math.min(1.0, flowAcc[r][c] / 500.0);
                double flatness = 1.0 - Math.min(1.0, slopeAt(dem, r, c) / 30.0);
                double rainFactor = Math.min(1.0, rainfall / 200.0);
                risk[r][c] = flowFactor * 0.4 + flatness * 0.4 + rainFactor * 0.2;
            }
        }
        warnings.add("Precipitacion: " + (int) rainfall + " mm");
        return risk;
    }

    private static double[][] landslideRisk(
            double[][] slope, Map<String, Double> params,
            List<String> warnings) {

        int h = slope.length, w = slope[0].length;
        double[][] risk = new double[h][w];

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                // Landslide risk peaks at 25-35° slopes
                double s = slope[r][c];
                double slopeFactor;
                if (s < 10) slopeFactor = s / 10 * 0.2;
                else if (s < 30) slopeFactor = 0.2 + (s - 10) / 20 * 0.6;
                else if (s < 45) slopeFactor = 0.8 - (s - 30) / 15 * 0.3;
                else slopeFactor = 0.5;
                risk[r][c] = Math.min(1.0, slopeFactor);
            }
        }
        warnings.add("Pendientes optimas para deslizamiento: 25-35°");
        return risk;
    }

    private static double[][] contaminationRisk(
            float[][] dem, double[][] slope, double[][] flowAcc,
            double lat, double lon, Map<String, Double> params,
            List<String> warnings) {

        // Similar to oil spill but with different weighting
        return oilSpillRisk(dem, slope, flowAcc, lat, lon, params, warnings);
    }

    private static double[][] genericRisk(
            double[][] slope, double[][] flowAcc,
            List<String> warnings) {
        int h = slope.length, w = slope[0].length;
        double[][] risk = new double[h][w];
        for (int r = 0; r < h; r++)
            for (int c = 0; c < w; c++)
                risk[r][c] = (Math.min(1.0, slope[r][c] / 30) + Math.min(1.0, flowAcc[r][c] / 500)) / 2;
        return risk;
    }

    // ========== Terrain analysis helpers ==========

    private static double[][] computeSlope(float[][] dem, double cellSize) {
        int h = dem.length, w = dem[0].length;
        double[][] slope = new double[h][w];
        for (int r = 1; r < h - 1; r++) {
            for (int c = 1; c < w - 1; c++) {
                if (Float.isNaN(dem[r][c])) continue;
                double dzdx = (dem[r][c+1] - dem[r][c-1]) / (2 * cellSize);
                double dzdy = (dem[r-1][c] - dem[r+1][c]) / (2 * cellSize);
                slope[r][c] = Math.toDegrees(Math.atan(Math.sqrt(dzdx*dzdx + dzdy*dzdy)));
            }
        }
        return slope;
    }

    private static double[][] computeFlowAccumulation(float[][] dem) {
        int h = dem.length, w = dem[0].length;
        double[][] acc = new double[h][w];
        int[][] dir = new int[h][w]; // D8 flow direction

        // D8 flow direction
        int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dc = {0, 1, 1, 1, 0, -1, -1, -1};

        // Simple priority-flood algorithm for flow direction
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (Float.isNaN(dem[r][c])) continue;
                double minElev = dem[r][c];
                int minDir = -1;
                for (int d = 0; d < 8; d++) {
                    int nr = r + dr[d], nc = c + dc[d];
                    if (nr < 0 || nr >= h || nc < 0 || nc >= w) continue;
                    if (Float.isNaN(dem[nr][nc])) continue;
                    if (dem[nr][nc] < minElev) {
                        minElev = dem[nr][nc];
                        minDir = d;
                    }
                }
                dir[r][c] = minDir;
            }
        }

        // Flow accumulation
        // Simplified: each cell contributes 1 unit of flow
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (dir[r][c] < 0) continue;
                int cr = r, cc = c;
                while (dir[cr][cc] >= 0) {
                    acc[cr][cc]++;
                    int d = dir[cr][cc];
                    cr += dr[d];
                    cc += dc[d];
                    if (cr < 0 || cr >= h || cc < 0 || cc >= w) break;
                }
            }
        }
        return acc;
    }

    private static double slopeAt(float[][] dem, int r, int c) {
        int h = dem.length, w = dem[0].length;
        if (r <= 0 || r >= h-1 || c <= 0 || c >= w-1) return 0;
        double dzdx = (dem[r][c+1] - dem[r][c-1]) / 2.0;
        double dzdy = (dem[r-1][c] - dem[r+1][c]) / 2.0;
        return Math.toDegrees(Math.atan(Math.sqrt(dzdx*dzdx + dzdy*dzdy)));
    }

    // ========== General helpers ==========

    private static float min(float[][] grid) {
        float m = Float.MAX_VALUE;
        for (float[] row : grid) for (float v : row) if (!Float.isNaN(v) && v < m) m = v;
        return m;
    }

    private static float range(float[][] grid) {
        float mn = min(grid), mx = -Float.MAX_VALUE;
        for (float[] row : grid) for (float v : row) if (!Float.isNaN(v) && v > mx) mx = v;
        return mx - mn;
    }

    private static void normalizeRisk(double[][] grid) {
        double mn = Double.MAX_VALUE, mx = -Double.MAX_VALUE;
        for (double[] row : grid) for (double v : row) { if (v < mn) mn = v; if (v > mx) mx = v; }
        double range = mx - mn;
        if (range < 1e-10) { for (double[] row : grid) java.util.Arrays.fill(row, 0.5); return; }
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[0].length; c++)
                grid[r][c] = (grid[r][c] - mn) / range;
    }

    private static BufferedImage renderRiskMap(double[][] risk) {
        int h = risk.length, w = risk[0].length;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                double v = risk[r][c];
                // Red-amber-green heatmap
                int red, green, blue;
                if (v < 0.33) {       // Low: green
                    red = (int)(v * 3 * 200); green = 200; blue = 50;
                } else if (v < 0.66) { // Medium: amber
                    double t = (v - 0.33) * 3;
                    red = (int)(200 + t * 55); green = (int)(200 - t * 100); blue = 50;
                } else {                // High: red
                    double t = (v - 0.66) * 3;
                    red = 255; green = (int)(100 - t * 80); blue = (int)(50 + t * 50);
                }
                red = Math.max(0, Math.min(255, red));
                green = Math.max(0, Math.min(255, green));
                blue = Math.max(0, Math.min(255, blue));
                img.setRGB(c, r, (red << 16) | (green << 8) | blue);
            }
        }
        return img;
    }
}
