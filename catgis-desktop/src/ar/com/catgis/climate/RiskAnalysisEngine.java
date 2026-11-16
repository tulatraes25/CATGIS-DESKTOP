package ar.com.catgis.climate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Multi-hazard risk analysis engine.
 * <p>
 * Supported scenarios: oil spill, flood, landslide and contamination.
 * <p>
 * Calibration and traceability (REQ-PROD-9): the grid cell size is an explicit
 * parameter (persisted in {@link RiskResult#parameterSpec()}), degree/metre
 * conversions use WGS84 ellipsoid metrics ({@link GeographicMetrics}) instead of
 * a hard-coded 111320 constant, results expose susceptibility classes and an
 * exposure factor, and the rainfall design return period is part of the
 * parameters. The former WILDFIRE scenario was withdrawn because it had no
 * calibrated model (it is not part of {@link Scenario} anymore).
 */
public final class RiskAnalysisEngine {

    /** Algorithm identity persisted with every result. */
    public static final String ALGORITHM_VERSION = "risk-analysis/1";
    /** Assumed geographic CRS for the lat/lon interface. */
    public static final String SCENARIO_CRS = "EPSG:4326";
    /** Documented fallback cell size when the caller does not provide one. */
    public static final double DEFAULT_CELL_SIZE_METERS = 30.0;
    /** Default design return period for rainfall-driven scenarios (years). */
    public static final int DEFAULT_RETURN_PERIOD_YEARS = 100;

    private RiskAnalysisEngine() {}

    public enum Scenario {
        OIL_SPILL("Derrame de hidrocarburos"),
        FLOOD("Inundacion"),
        LANDSLIDE("Deslizamiento"),
        CONTAMINATION("Contaminacion industrial");

        private final String spanish;

        Scenario(String s) {
            this.spanish = s;
        }

        public String spanish() {
            return spanish;
        }
    }

    /** Explicit, persistable risk parameters. */
    public record RiskParameters(double cellSizeMeters, int returnPeriodYears,
                                 double exposureFactor) {
        public RiskParameters {
            if (!Double.isFinite(cellSizeMeters) || cellSizeMeters <= 0) {
                throw new IllegalArgumentException("cellSizeMeters must be positive.");
            }
            if (returnPeriodYears <= 0) {
                throw new IllegalArgumentException("returnPeriodYears must be positive.");
            }
            if (!Double.isFinite(exposureFactor) || exposureFactor < 0) {
                throw new IllegalArgumentException("exposureFactor must be >= 0.");
            }
        }

        public static RiskParameters defaults() {
            return new RiskParameters(DEFAULT_CELL_SIZE_METERS, DEFAULT_RETURN_PERIOD_YEARS, 1.0);
        }

        public static RiskParameters fromMap(Map<String, Double> params) {
            if (params == null) {
                return defaults();
            }
            double cellSize = first(params, DEFAULT_CELL_SIZE_METERS, "cellSizeMeters", "cellSize");
            int returnPeriod = (int) Math.round(
                    first(params, DEFAULT_RETURN_PERIOD_YEARS, "returnPeriodYears", "returnPeriod"));
            double exposure = first(params, 1.0, "exposureFactor", "exposure");
            return new RiskParameters(cellSize, returnPeriod, exposure);
        }

        private static double first(Map<String, Double> params, double fallback,
                                    String... keys) {
            for (String key : keys) {
                Double value = params.get(key);
                if (value != null && Double.isFinite(value)) {
                    return value;
                }
            }
            return fallback;
        }
    }

    /** Susceptibility class over the normalised 0-1 risk index. */
    public record SusceptibilityClass(String name, double minIndex, double maxIndex) {}

    /** Five calibrated susceptibility classes covering the 0-1 index. */
    public static List<SusceptibilityClass> susceptibilityClasses() {
        return List.of(
                new SusceptibilityClass("Muy baja", 0.00, 0.20),
                new SusceptibilityClass("Baja", 0.20, 0.40),
                new SusceptibilityClass("Media", 0.40, 0.60),
                new SusceptibilityClass("Alta", 0.60, 0.80),
                new SusceptibilityClass("Muy alta", 0.80, 1.00));
    }

    /** Class name for a normalised risk value. */
    public static String classifySusceptibility(double riskIndex) {
        double value = Math.max(0.0, Math.min(1.0, riskIndex));
        List<SusceptibilityClass> classes = susceptibilityClasses();
        for (int i = 0; i < classes.size(); i++) {
            SusceptibilityClass current = classes.get(i);
            boolean last = i == classes.size() - 1;
            if (last || (value >= current.minIndex() && value < current.maxIndex())) {
                return current.name();
            }
        }
        return classes.get(classes.size() - 1).name();
    }

    public record RiskResult(
            BufferedImage riskMap,
            double[][] riskGrid,
            int width, int height,
            double cellSize,
            double west, double north,
            String scenarioName,
            List<String> warnings,
            String parameterSpec,
            int returnPeriodYears,
            double exposureFactor,
            List<SusceptibilityClass> susceptibilityClasses
    ) {}

    public static RiskResult analyze(float[][] dem, Scenario scenario,
                                     double locationLat, double locationLon,
                                     String siteName, Map<String, Double> params) {
        return analyzeWithParameters(dem, scenario, locationLat, locationLon, siteName,
                RiskParameters.fromMap(params));
    }

    public static RiskResult analyzeWithParameters(float[][] dem, Scenario scenario,
                                     double locationLat, double locationLon,
                                     String siteName, RiskParameters parameters) {
        if (scenario == null) {
            throw new IllegalArgumentException("El escenario de riesgo es obligatorio.");
        }
        RiskParameters risk = parameters != null ? parameters : RiskParameters.defaults();
        double cellSize = risk.cellSizeMeters();
        int width = dem != null ? dem[0].length : 100;
        int height = dem != null ? dem.length : 100;
        double metersPerDegreeLat = GeographicMetrics.metersPerDegreeLatitude(locationLat);
        double metersPerDegreeLon = GeographicMetrics.metersPerDegreeLongitude(locationLat);
        double west = locationLon - (width * cellSize / 2.0 / metersPerDegreeLon);
        double north = locationLat + (height * cellSize / 2.0 / metersPerDegreeLat);

        double[][] riskGrid = new double[height][width];
        List<String> warnings = new ArrayList<>();

        if (dem != null) {
            double[][] slope = computeSlope(dem, cellSize);
            double[][] flowAcc = computeFlowAccumulation(dem);
            switch (scenario) {
                case OIL_SPILL -> riskGrid = oilSpillRisk(dem, slope, flowAcc,
                        locationLat, locationLon, cellSize, paramsOf(risk), warnings);
                case FLOOD -> riskGrid = floodRisk(dem, flowAcc, paramsOf(risk), warnings);
                case LANDSLIDE -> riskGrid = landslideRisk(slope, warnings);
                case CONTAMINATION -> riskGrid = oilSpillRisk(dem, slope, flowAcc,
                        locationLat, locationLon, cellSize, paramsOf(risk), warnings);
            }
        } else {
            warnings.add("No DEM provided — risk analysis limited");
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    riskGrid[r][c] = 0.3;
                }
            }
        }

        for (double[] row : riskGrid) {
            for (int c = 0; c < row.length; c++) {
                row[c] *= risk.exposureFactor();
            }
        }
        normalizeRisk(riskGrid);
        warnings.add("Periodo de retorno de diseno: " + risk.returnPeriodYears()
                + " anos; factor de exposicion: " + risk.exposureFactor());
        String parameterSpec = buildParameterSpec(scenario, risk);
        BufferedImage riskMap = renderRiskMap(riskGrid);
        return new RiskResult(riskMap, riskGrid, width, height, cellSize, west, north,
                scenario.spanish(), warnings, parameterSpec, risk.returnPeriodYears(),
                risk.exposureFactor(), susceptibilityClasses());
    }

    private static Map<String, Double> paramsOf(RiskParameters risk) {
        Map<String, Double> map = new java.util.HashMap<>();
        map.put("cellSizeMeters", risk.cellSizeMeters());
        map.put("returnPeriodYears", (double) risk.returnPeriodYears());
        map.put("exposureFactor", risk.exposureFactor());
        return map;
    }

    private static String buildParameterSpec(Scenario scenario, RiskParameters risk) {
        StringBuilder classes = new StringBuilder();
        for (SusceptibilityClass item : susceptibilityClasses()) {
            if (classes.length() > 0) {
                classes.append(';');
            }
            classes.append(item.name()).append(':')
                    .append(String.format(Locale.ROOT, "%.2f", item.minIndex()))
                    .append('-')
                    .append(String.format(Locale.ROOT, "%.2f", item.maxIndex()));
        }
        return "algorithm=" + ALGORITHM_VERSION
                + ";crs=" + SCENARIO_CRS
                + ";scenario=" + scenario.name()
                + ";cellSizeMeters=" + risk.cellSizeMeters()
                + ";returnPeriodYears=" + risk.returnPeriodYears()
                + ";exposureFactor=" + risk.exposureFactor()
                + ";susceptibilityClasses=" + classes
                + ";wildfire=withdrawn";
    }

    // ========== Scenario risk calculators ==========

    private static double[][] oilSpillRisk(float[][] dem, double[][] slope,
                                           double[][] flowAcc, double lat, double lon,
                                           double cellSize, Map<String, Double> params,
                                           List<String> warnings) {
        int h = dem.length;
        int w = dem[0].length;
        double[][] risk = new double[h][w];
        double metersPerDegreeLat = GeographicMetrics.metersPerDegreeLatitude(lat);
        double metersPerDegreeLon = GeographicMetrics.metersPerDegreeLongitude(lat);
        double west = lon - (w * cellSize / 2.0 / metersPerDegreeLon);
        double north = lat + (h * cellSize / 2.0 / metersPerDegreeLat);
        int srcR = (int) ((north - lat) / (cellSize / metersPerDegreeLat));
        int srcC = (int) ((lon - west) / (cellSize / metersPerDegreeLon));
        srcR = Math.max(0, Math.min(h - 1, srcR));
        srcC = Math.max(0, Math.min(w - 1, srcC));

        double sourceIntensity = params != null ? params.getOrDefault("volume", 100.0) : 100.0;
        double windDirDeg = params != null ? params.getOrDefault("windDirection", 240.0) : 240.0;
        double windSpeed = params != null ? params.getOrDefault("windSpeed", 20.0) : 20.0;
        double windRad = Math.toRadians(windDirDeg);

        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                double dist = Math.sqrt(Math.pow(r - srcR, 2) + Math.pow(c - srcC, 2));
                if (dist < 1) {
                    risk[r][c] = 1.0;
                    continue;
                }
                double distanceFactor = Math.exp(-dist / 50.0);
                double angleFromSource = Math.atan2(c - srcC, r - srcR);
                double windDiff = angleFromSource - windRad;
                double windFactor = (Math.cos(windDiff) + 1) / 2;
                windFactor = windFactor * 0.5 + 0.5 * (windSpeed / 50.0);
                double terrainFactor = 1.0 - (dem[r][c] - min(dem)) / Math.max(range(dem), 1);
                double flowFactor = Math.min(1.0, flowAcc[r][c] / 1000.0);
                risk[r][c] = distanceFactor
                        * (windFactor * 0.4 + terrainFactor * 0.3 + flowFactor * 0.3);
            }
        }
        warnings.add("Derrame modelado con viento " + (int) windDirDeg + "° a "
                + (int) windSpeed + " km/h");
        warnings.add("Radio de dispersion estimado: ~" + (int) (50 * cellSize) + "m");
        warnings.add("Volumen declarado: " + (int) sourceIntensity);
        return risk;
    }

    private static double[][] floodRisk(float[][] dem, double[][] flowAcc,
                                        Map<String, Double> params, List<String> warnings) {
        int h = dem.length;
        int w = dem[0].length;
        double[][] risk = new double[h][w];
        double rainfall = params != null ? params.getOrDefault("rainfall", 100.0) : 100.0;
        int returnPeriod = params != null
                ? (int) Math.round(params.getOrDefault("returnPeriodYears", 100.0)) : 100;
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                double flowFactor = Math.min(1.0, flowAcc[r][c] / 500.0);
                double flatness = 1.0 - Math.min(1.0, slopeAt(dem, r, c) / 30.0);
                double rainFactor = Math.min(1.0, rainfall / 200.0);
                risk[r][c] = flowFactor * 0.4 + flatness * 0.4 + rainFactor * 0.2;
            }
        }
        warnings.add("Precipitacion de diseno: " + (int) rainfall + " mm / "
                + returnPeriod + " anos");
        return risk;
    }

    private static double[][] landslideRisk(double[][] slope, List<String> warnings) {
        int h = slope.length;
        int w = slope[0].length;
        double[][] risk = new double[h][w];
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                double s = slope[r][c];
                double slopeFactor;
                if (s < 10) {
                    slopeFactor = s / 10 * 0.2;
                } else if (s < 30) {
                    slopeFactor = 0.2 + (s - 10) / 20 * 0.6;
                } else if (s < 45) {
                    slopeFactor = 0.8 - (s - 30) / 15 * 0.3;
                } else {
                    slopeFactor = 0.5;
                }
                risk[r][c] = Math.min(1.0, slopeFactor);
            }
        }
        warnings.add("Pendientes optimas para deslizamiento: 25-35°");
        return risk;
    }

    // ========== Terrain analysis helpers ==========

    private static double[][] computeSlope(float[][] dem, double cellSize) {
        int h = dem.length;
        int w = dem[0].length;
        double[][] slope = new double[h][w];
        for (int r = 1; r < h - 1; r++) {
            for (int c = 1; c < w - 1; c++) {
                if (Float.isNaN(dem[r][c])) {
                    continue;
                }
                double dzdx = (dem[r][c + 1] - dem[r][c - 1]) / (2 * cellSize);
                double dzdy = (dem[r - 1][c] - dem[r + 1][c]) / (2 * cellSize);
                slope[r][c] = Math.toDegrees(Math.atan(Math.sqrt(dzdx * dzdx + dzdy * dzdy)));
            }
        }
        return slope;
    }

    private static double[][] computeFlowAccumulation(float[][] dem) {
        int h = dem.length;
        int w = dem[0].length;
        double[][] acc = new double[h][w];
        int[][] dir = new int[h][w];
        int[] dr = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dc = {0, 1, 1, 1, 0, -1, -1, -1};
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (Float.isNaN(dem[r][c])) {
                    continue;
                }
                double minElev = dem[r][c];
                int minDir = -1;
                for (int d = 0; d < 8; d++) {
                    int nr = r + dr[d];
                    int nc = c + dc[d];
                    if (nr < 0 || nr >= h || nc < 0 || nc >= w) {
                        continue;
                    }
                    if (Float.isNaN(dem[nr][nc])) {
                        continue;
                    }
                    if (dem[nr][nc] < minElev) {
                        minElev = dem[nr][nc];
                        minDir = d;
                    }
                }
                dir[r][c] = minDir;
            }
        }
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                if (dir[r][c] < 0) {
                    continue;
                }
                int cr = r;
                int cc = c;
                while (dir[cr][cc] >= 0) {
                    acc[cr][cc]++;
                    int d = dir[cr][cc];
                    cr += dr[d];
                    cc += dc[d];
                    if (cr < 0 || cr >= h || cc < 0 || cc >= w) {
                        break;
                    }
                }
            }
        }
        return acc;
    }

    private static double slopeAt(float[][] dem, int r, int c) {
        int h = dem.length;
        int w = dem[0].length;
        if (r <= 0 || r >= h - 1 || c <= 0 || c >= w - 1) {
            return 0;
        }
        double dzdx = (dem[r][c + 1] - dem[r][c - 1]) / 2.0;
        double dzdy = (dem[r - 1][c] - dem[r + 1][c]) / 2.0;
        return Math.toDegrees(Math.atan(Math.sqrt(dzdx * dzdx + dzdy * dzdy)));
    }

    // ========== General helpers ==========

    private static float min(float[][] grid) {
        float m = Float.MAX_VALUE;
        for (float[] row : grid) {
            for (float v : row) {
                if (!Float.isNaN(v) && v < m) {
                    m = v;
                }
            }
        }
        return m;
    }

    private static float range(float[][] grid) {
        float mn = min(grid);
        float mx = -Float.MAX_VALUE;
        for (float[] row : grid) {
            for (float v : row) {
                if (!Float.isNaN(v) && v > mx) {
                    mx = v;
                }
            }
        }
        return mx - mn;
    }

    private static void normalizeRisk(double[][] grid) {
        double mn = Double.MAX_VALUE;
        double mx = -Double.MAX_VALUE;
        for (double[] row : grid) {
            for (double v : row) {
                if (v < mn) {
                    mn = v;
                }
                if (v > mx) {
                    mx = v;
                }
            }
        }
        double range = mx - mn;
        if (range < 1e-10) {
            for (double[] row : grid) {
                Arrays.fill(row, 0.5);
            }
            return;
        }
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                grid[r][c] = (grid[r][c] - mn) / range;
            }
        }
    }

    private static BufferedImage renderRiskMap(double[][] risk) {
        int h = risk.length;
        int w = risk[0].length;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int r = 0; r < h; r++) {
            for (int c = 0; c < w; c++) {
                double v = risk[r][c];
                int red;
                int green;
                int blue;
                if (v < 0.33) {
                    red = (int) (v * 3 * 200);
                    green = 200;
                    blue = 50;
                } else if (v < 0.66) {
                    double t = (v - 0.33) * 3;
                    red = (int) (200 + t * 55);
                    green = (int) (200 - t * 100);
                    blue = 50;
                } else {
                    double t = (v - 0.66) * 3;
                    red = 255;
                    green = (int) (100 - t * 80);
                    blue = (int) (50 + t * 50);
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
