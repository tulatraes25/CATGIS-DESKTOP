package ar.com.catgis;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Raster reprojection using gdalwarp.
 * Requires GDAL (OSGeo4W or OSGeo4W64).
 */
public final class RasterReprojectionService {

    private static final long TIMEOUT_SECONDS = 300;

    private RasterReprojectionService() {}

    /**
     * Reproject a raster to a target CRS.
     *
     * @param inputFile  source raster file
     * @param outputFile destination raster file (will be overwritten)
     * @param targetCrs  target CRS (e.g. "EPSG:32720")
     * @param resampling resampling method (near, bilinear, cubic, cubicspline, lanczos)
     * @return true on success
     */
    public static boolean reproject(File inputFile, File outputFile, String targetCrs, String resampling) {
        if (inputFile == null || outputFile == null || targetCrs == null || targetCrs.isBlank()) return false;
        if (!inputFile.exists()) return false;

        try {
            String gdalwarp = GdalSupport.resolve("gdalwarp.exe");
            List<String> cmd = new ArrayList<>();
            cmd.add(gdalwarp);
            cmd.add("-t_srs");
            cmd.add(targetCrs);
            cmd.add("-r");
            cmd.add(resampling != null ? resampling : "bilinear");
            cmd.add("-of");
            cmd.add("GTiff");
            cmd.add("-co");
            cmd.add("COMPRESS=LZW");
            cmd.add("-overwrite");
            cmd.add(inputFile.getAbsolutePath());
            cmd.add(outputFile.getAbsolutePath());

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
            return p.exitValue() == 0 && outputFile.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if gdalwarp is available.
     */
    public static boolean isAvailable() {
        try {
            GdalSupport.resolve("gdalwarp.exe");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Orthorectify a satellite image using RPC coefficients stored in the raster.
     * Uses gdalwarp with -rpc flag for automatic RPC-based orthorectification.
     *
     * @param inputFile   raw satellite image with RPC metadata
     * @param outputFile  orthorectified output
     * @param demFile     optional DEM for terrain correction (can be null)
     * @param resampling  resampling method (bilinear, cubic, etc.)
     * @return true on success
     */
    public static boolean orthorectify(File inputFile, File outputFile, File demFile, String resampling) {
        if (inputFile == null || outputFile == null) return false;
        if (!inputFile.exists()) return false;

        try {
            String gdalwarp = GdalSupport.resolve("gdalwarp.exe");
            List<String> cmd = new ArrayList<>();
            cmd.add(gdalwarp);
            cmd.add("-rpc");
            cmd.add("-to");
            cmd.add("RPC_DEM_MISSING_VALUE=0");
            if (demFile != null && demFile.exists()) {
                cmd.add("-rpc_dem");
                cmd.add(demFile.getAbsolutePath());
            }
            cmd.add("-r");
            cmd.add(resampling != null ? resampling : "bilinear");
            cmd.add("-of");
            cmd.add("GTiff");
            cmd.add("-co");
            cmd.add("COMPRESS=LZW");
            cmd.add("-overwrite");
            cmd.add(inputFile.getAbsolutePath());
            cmd.add(outputFile.getAbsolutePath());

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
            return p.exitValue() == 0 && outputFile.exists();
        } catch (Exception e) {
            return false;
        }
    }
}
