package ar.com.catgis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Portable north-oriented XYZ tile creation with safe transactional publication.
 * Uses installed modern GDAL or official legacy GDAL Python bindings.
 */
public final class Gdal2TilesService {

    private static final long TIMEOUT_SECONDS = 600; // 10 min for large rasters

    private Gdal2TilesService() {}

    /**
     * Generate north-oriented XYZ tiles without exposing partial results.
     *
     * @param inputFile  georeferenced raster (GeoTIFF, etc.)
     * @param outputDir  new or CATGIS-owned output directory (existing user data is never replaced)
     * @param zoomMin    minimum zoom level
     * @param zoomMax    maximum zoom level (use -1 for auto)
     * @return true on success
     */
    public static boolean generateTiles(File inputFile, File outputDir,
                                         int zoomMin, int zoomMax) {
        if (inputFile == null || outputDir == null || !inputFile.isFile()
                || SwingUtilities.isEventDispatchThread()) {
            return false;
        }
        try {
            TileBackend backend = resolveBackend();
            if (backend == null) {
                return false;
            }
            // GDAL receives a private staging path; only validated XYZ output
            // is published to the user-visible target via atomic rename.
            return GdalTileOutputTransaction.execute(
                    inputFile, outputDir, zoomMin, zoomMax, staging -> {
                        File stagedOutput = staging.toFile();
                        List<String> command = backend.unified()
                                ? buildUnifiedCommand(backend.executable(), inputFile,
                                        stagedOutput, zoomMin, zoomMax)
                                : buildCommand(backend.executable(), inputFile,
                                        stagedOutput, zoomMin, zoomMax);
                        ProcessBuilder builder = new ProcessBuilder(command);
                        builder.redirectErrorStream(true);
                        Process process = builder.start();
                        ExternalToolService.ToolResult result =
                                ExternalToolService.collectProcess(process,
                                        TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
                        return result.success();
                    });
        } catch (Exception error) {
            CatgisLogger.warn("GDAL XYZ staging setup failed", error);
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
        // gdal2tiles.py defaults to TMS (south-origin Y). CATGIS expects
        // slippy-map XYZ (north-origin Y). This is geospatial correctness,
        // not a cosmetic output option.
        cmd.add("--xyz");
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
     * Modern GDAL 3.11+ uses a compiled "gdal raster tile" command. The
     * classic Python module is an installed-capability fallback only.
     * GDAL 3.13+ can ship without a Python module on the same interpreter
     * that CATGIS uses for general-purpose user scripts.
     */
    static List<String> buildUnifiedCommand(String gdalExecutable, File inputFile,
                                             File outputDir, int zoomMin, int zoomMax) {
        if (gdalExecutable == null || gdalExecutable.isBlank()
                || inputFile == null || outputDir == null) {
            throw new IllegalArgumentException("GDAL, input raster and tile directory required.");
        }
        if (zoomMin < 0 || (zoomMax > 0 && zoomMax < zoomMin)) {
            throw new IllegalArgumentException("Invalid GDAL native tile zoom range.");
        }
        List<String> cmd = new ArrayList<>();
        cmd.add(gdalExecutable);
        cmd.add("raster");
        cmd.add("tile");
        cmd.add("--convention");
        cmd.add("xyz");
        cmd.add("--resampling");
        cmd.add("bilinear");
        cmd.add("--webviewer");
        cmd.add("none");
        if (zoomMax > 0) {
            cmd.add("--min-zoom");
            cmd.add(Integer.toString(zoomMin));
            cmd.add("--max-zoom");
            cmd.add(Integer.toString(zoomMax));
        }
        cmd.add(inputFile.getAbsolutePath());
        cmd.add(outputDir.getAbsolutePath());
        return List.copyOf(cmd);
    }

    /**
     * Capability check rather than simply checking whether Python exists.
     * Probes are bounded and must never run on Swing EDT.
     */
    public static boolean isAvailable() {
        return !SwingUtilities.isEventDispatchThread() && resolveBackend() != null;
    }

    private record TileBackend(boolean unified, String executable) {
    }

    private static TileBackend resolveBackend() {
        // Only a GDAL executable from the reviewed allowlisted directories
        // can be launched. "gdal raster tile" exists from GDAL 3.11.
        try {
            String gdal = GdalSupport.resolve("gdal");
            if (probe(List.of(gdal, "raster", "tile", "--help"))) {
                return new TileBackend(true, gdal);
            }
        } catch (GdalSupport.GdalNotAvailableException unavailable) {
            // Older GDAL distributions ship only the Python GDAL utilities.
        }

        String python = ar.com.catgis.scripting.ScriptEngine.getPythonPath();
        if (python != null && probe(List.of(python, "-c",
                "import osgeo.gdal; import osgeo_utils.gdal2tiles"))) {
            return new TileBackend(false, python);
        }
        return null;
    }

    private static boolean probe(List<String> command) {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            process = pb.start();
            return ExternalToolService.collectProcess(process, 10000L).success();
        } catch (Exception error) {
            return false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
