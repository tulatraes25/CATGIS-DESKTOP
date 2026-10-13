package ar.com.catgis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Actual installed GDAL CLI/GDAL data driver contract, independent of mocks.
 * Must run only in a deliberately provisioned Linux/macOS native-GDAL job.
 * No arbitrary PATH execution, external network fetches, or real user files.
 */
@Tag("native-gdal")
class GdalNativeCliRoundTripIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void gdalTranslateInfoAndWarpPreserveKnownRasterCellsAndGeoreference()
            throws Exception {
        assertEquals("1", System.getenv("CATGIS_NATIVE_GDAL_REQUIRED"),
                "Real GDAL CI must opt in and provision system GDAL.");
        Path asc = tempDir.resolve("elevation.asc");
        Files.writeString(asc, """
                ncols 3
                nrows 2
                xllcorner -70
                yllcorner -46
                cellsize 0.01
                NODATA_value -9999
                1 2 3
                4 5 6
                """, StandardCharsets.UTF_8);

        String translate = GdalSupport.resolve("gdal_translate.exe");
        String info = GdalSupport.resolve("gdalinfo.exe");
        String warp = GdalSupport.resolve("gdalwarp.exe");

        String version = run(List.of(info, "--version"));
        assertTrue(version.contains("GDAL"), "Installed GDAL must report a version.");
        Path raster = tempDir.resolve("elevation.tif");
        run(List.of(translate, "-of", "GTiff", "-a_srs", "EPSG:4326",
                asc.toString(), raster.toString()));
        assertTrue(Files.size(raster) > 100, "Real GeoTIFF must not be empty.");

        try (InputStream input = Files.newInputStream(raster)) {
            byte[] header = input.readNBytes(4);
            assertEquals(4, header.length);
            boolean little = header[0] == 'I' && header[1] == 'I'
                    && header[2] == 42 && header[3] == 0;
            boolean big = header[0] == 'M' && header[1] == 'M'
                    && header[2] == 0 && header[3] == 42;
            assertTrue(little || big, "GDAL output must be an actual TIFF.");
        }

        String rasterInfo = run(List.of(info, raster.toString()));
        assertTrue(rasterInfo.contains("Size is 3, 2"), "GeoTIFF pixel grid must be 3x2.");
        assertTrue(rasterInfo.contains("WGS 84"), "The test's EPSG:4326 must be present.");

        Path xyz = tempDir.resolve("elevation.xyz");
        run(List.of(translate, "-of", "XYZ", raster.toString(), xyz.toString()));
        List<String> rows = Files.readAllLines(xyz, StandardCharsets.UTF_8);
        assertEquals(6, rows.size(), "All original 3x2 raster cells must round-trip.");
        Set<Integer> elevations = rows.stream().map(String::trim).map(row -> {
            String[] numbers = row.split("\\s+");
            assertEquals(3, numbers.length, "XYZ row should have x y elevation.");
            return (int) Math.round(Double.parseDouble(numbers[2]));
        }).collect(Collectors.toSet());
        assertEquals(Set.of(1, 2, 3, 4, 5, 6), elevations);

        Path warped = tempDir.resolve("elevation-3857.tif");
        run(List.of(warp, "-overwrite", "-of", "GTiff",
                "-s_srs", "EPSG:4326", "-t_srs", "EPSG:3857",
                raster.toString(), warped.toString()));
        assertTrue(Files.size(warped) > 100, "Projected GeoTIFF must exist.");
        String warpedInfo = run(List.of(info, warped.toString()));
        assertTrue(warpedInfo.contains("3857"), "GDAL must actually reproject CRS.");
        assertFalse(warpedInfo.contains("Size is 0, 0"), "Projected raster cannot be empty.");
    }

    private static String run(List<String> arguments) throws Exception {
        ProcessBuilder command = new ProcessBuilder(arguments);
        command.redirectErrorStream(true);
        command.environment().put("PROJ_NETWORK", "OFF");
        Process process = command.start();
        byte[] bytes;
        try (InputStream output = process.getInputStream()) {
            bytes = output.readAllBytes();
        }
        boolean completed = process.waitFor(90, TimeUnit.SECONDS);
        if (!completed) {
            process.destroyForcibly();
            throw new IOException("Native GDAL command did not complete within 90 seconds.");
        }
        String text = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(),
                "Native GDAL exited nonzero: " + arguments.get(0) + "\n" + text);
        return text;
    }
}
