package ar.com.catgis;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

            List<String> cmd = new ArrayList<>();
            cmd.add(python);
            cmd.add("-m");
            cmd.add("gdal2tiles");
            cmd.add("-z");
            cmd.add(zoomMin + "-" + (zoomMax > 0 ? zoomMax : ""));
            if (zoomMax <= 0) {
                cmd.set(cmd.size() - 1, "--processes=4");
                // Remove the -z flag and use auto zoom detection
                cmd.remove(cmd.size() - 1);
                cmd.add("--resampling=bilinear");
            } else {
                cmd.set(cmd.size() - 1, zoomMin + "-" + zoomMax);
            }
            cmd.add(inputFile.getAbsolutePath());
            cmd.add(outputDir.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            boolean finished = p.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0 && outputDir.exists();
        } catch (Exception e) {
            return false;
        }
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
