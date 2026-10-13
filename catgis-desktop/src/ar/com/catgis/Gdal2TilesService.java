package ar.com.catgis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tile map generation using gdal2tiles.py.
 * Requires GDAL (OSGeo4W) with Python bindings.
 */
public final class Gdal2TilesService {

    private static final long TIMEOUT_SECONDS = 600; // 10 min for large rasters

    private Gdal2TilesService() {}

    /**
     * Generate XYZ/TMS tiles from a raster using gdal2tiles.
     *
     * @param inputFile  georeferenced raster (GeoTIFF, etc.)
     * @param outputDir  output directory for tiles
     * @param zoomMin    minimum zoom level
     * @param zoomMax    maximum zoom level (use -1 for auto)
     * @return true on success
     */
    public static boolean generateTiles(File inputFile, File outputDir,
                                         int zoomMin, int zoomMax) {
        if (inputFile == null || outputDir == null) return false;
        if (!inputFile.exists()) return false;

        try {
            String python = resolvePython();
            if (python == null) return false;

            List<String> cmd = buildCommand(python, inputFile, outputDir,
                    zoomMin, zoomMax);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            // A bounded concurrent drain is essential: reading stdout to EOF
            // BEFORE waitFor(timeout) makes a stalled child bypass the timeout.
            ExternalToolService.ToolResult result = ExternalToolService.collectProcess(
                    p, TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            return result.success() && outputDir.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Build arguments without shell interpolation or orphan -z tokens.
     * The implicit (zoomMax <= 0) mode lets GDAL determine BOTH zoom bounds;
     * a fixed zoom range always supplies exactly one -z value.
     */
    static List<String> buildCommand(String python, File inputFile, File outputDir,
                                     int zoomMin, int zoomMax) {
        if (python == null || python.isBlank() || inputFile == null || outputDir == null) {
            throw new IllegalArgumentException("Python, input raster and tile directory required.");
        }
        if (zoomMin < 0 || zoomMax > 0 && zoomMax < zoomMin) {
            throw new IllegalArgumentException("Invalid GDAL tile zoom range.");
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(python);
        cmd.add("-m");
        cmd.add("osgeo_utils.gdal2tiles");
        if (zoomMax > 0) {
            cmd.add("-z");
            cmd.add(zoomMin + "-" + zoomMax);
        } else {
            cmd.add("--processes=4");
            cmd.add("--resampling=bilinear");
        }
        cmd.add(inputFile.getAbsolutePath());
        cmd.add(outputDir.getAbsolutePath());
        return List.copyOf(cmd);
    }

    /**
     * Check if gdal2tiles is available.
     */
    public static boolean isAvailable() {
        return resolvePython() != null;
    }

    private static String resolvePython() {
        return ar.com.catgis.scripting.ScriptEngine.getPythonPath();
    }
}
