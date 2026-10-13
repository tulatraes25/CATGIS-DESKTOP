package ar.com.catgis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An actual tile generator/GeoTIFF/PNG contract, not a mocked command
 * builder. The GDAL and Python installation is deliberately provisioned by
 * the Linux, Intel Mac and Apple Silicon GitHub Certification runners.
 */
@Tag("native-gdal")
class Gdal2TilesRealXyzIntegrationTest {

    @TempDir
    Path temporary;

    @Test
    void geographicRasterGeneratesNorthOrientedXyzPngTiles() throws Exception {
        assertEquals("1", System.getenv("CATGIS_NATIVE_GDAL_REQUIRED"),
                "The real tile test must only run on provisioned native GDAL CI.");

        // The upper northern hemisphere distinguishes XYZ tile-Y indices 0/1
        // from the classic gdal2tiles.py default TMS indices 2/3 at zoom 2.
        Path asc = temporary.resolve("north-hemisphere.asc");
        StringBuilder data = new StringBuilder(
                "ncols 16\nnrows 16\nxllcorner -90\nyllcorner 50\n"
                        + "cellsize 2\nNODATA_value 0\n");
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                data.append(20 + row * 4 + col).append(col == 15 ? '\n' : ' ');
            }
        }
        Files.writeString(asc, data, StandardCharsets.UTF_8);
        Path geotiff = temporary.resolve("north-hemisphere.tif");
        runNative(List.of(GdalSupport.resolve("gdal_translate.exe"), "-of", "GTiff",
                "-ot", "Byte", "-a_srs", "EPSG:4326",
                asc.toString(), geotiff.toString()));
        assertTrue(Files.size(geotiff) > 128, "Native input GeoTIFF must exist.");

        Path output = temporary.resolve("XYZ tiles with spaces");
        assertTrue(Gdal2TilesService.isAvailable(),
                "The selected Python/GDAL tile backend must be truly installed.");
        assertTrue(Gdal2TilesService.generateTiles(
                        geotiff.toFile(), output.toFile(), 2, 2),
                "Real GDAL must complete 2/2 tile generation, not just build argv.");
        assertTrue(Files.isDirectory(output));

        List<Path> pngs = new ArrayList<>();
        try (Stream<Path> files = Files.walk(output, 6)) {
            files.filter(p -> p.toString().endsWith(".png")).forEach(pngs::add);
        }
        assertFalse(pngs.isEmpty(), "GDAL must create actual PNG tiles.");

        int colorful = 0;
        for (Path png : pngs) {
            Path relative = output.relativize(png);
            assertEquals(3, relative.getNameCount(),
                    "Tile paths must be zoom/X/Y.png only: " + relative);
            assertEquals("2", relative.getName(0).toString(),
                    "Only requested zoom 2 tiles should be generated.");
            int x = Integer.parseInt(relative.getName(1).toString());
            int y = Integer.parseInt(relative.getName(2).toString().replace(".png", ""));
            assertTrue(x >= 0 && x <= 3, "Zoom 2 X tile index outside 0..3: " + x);
            assertTrue(y == 0 || y == 1,
                    "Expected north-origin XYZ Y=0..1, not TMS south-origin: " + relative);
            BufferedImage image = ImageIO.read(png.toFile());
            assertNotNull(image, "GDAL tile must be real decodable PNG: " + relative);
            assertEquals(256, image.getWidth());
            assertEquals(256, image.getHeight());
            // The 32-degree source extent occupies less than half of one
            // zoom-2 WebMercator tile's width; the centre of an otherwise
            // valid tile may therefore be transparent. Inspect the actual
            // source-covered pixels rather than assuming the centre is data.
            boolean containsSourcePixels = false;
            for (int row = 0; row < image.getHeight() && !containsSourcePixels; row++) {
                for (int col = 0; col < image.getWidth(); col++) {
                    int pixel = image.getRGB(col, row);
                    if ((pixel >>> 24) != 0 && (pixel & 0x00ffffff) != 0) {
                        containsSourcePixels = true;
                        break;
                    }
                }
            }
            if (containsSourcePixels) {
                colorful++;
            }
        }
        assertTrue(colorful > 0,
                "At least one generated 256px tile must contain actual source pixels.");
    }

    private void runNative(List<String> argv) throws Exception {
        Path captured = temporary.resolve("source-gdal.log");
        ProcessBuilder builder = new ProcessBuilder(argv);
        builder.redirectErrorStream(true);
        builder.redirectOutput(captured.toFile());
        builder.environment().put("PROJ_NETWORK", "OFF");
        Process process = builder.start();
        try {
            if (!process.waitFor(90, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("Native raster source creation exceeded 90 seconds.");
            }
            assertEquals(0, process.exitValue(),
                    "Native GDAL source creation failed: "
                            + Files.readString(captured, StandardCharsets.UTF_8));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
