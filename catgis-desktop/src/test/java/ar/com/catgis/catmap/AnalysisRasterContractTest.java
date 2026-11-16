package ar.com.catgis.catmap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Float analytical rasters keep values, NoData and unit end-to-end. */
class AnalysisRasterContractTest {

    @TempDir
    Path directory;

    private static AnalysisRaster ramp() {
        float[] values = new float[12];
        for (int i = 0; i < values.length; i++) {
            values[i] = i * 0.5f;
        }
        return new AnalysisRaster("Pendiente", "degrees", 4, 3, values,
                TerrainRasterAlgorithms.NO_DATA, 1000, 2000, 1040, 2030, "EPSG:32719");
    }

    @Test
    void statisticsIgnoreNoData() {
        float noData = TerrainRasterAlgorithms.NO_DATA;
        AnalysisRaster raster = new AnalysisRaster("d", "m", 2, 2,
                new float[]{1f, 2f, noData, 4f}, noData, 0, 0, 2, 2, "EPSG:4326");
        AnalysisRaster.Stats stats = raster.stats();
        assertEquals(3, stats.validCount());
        assertEquals(1, stats.noDataCount());
        assertEquals(1.0, stats.min(), 1.0e-6);
        assertEquals(4.0, stats.max(), 1.0e-6);
        assertEquals(7.0 / 3.0, stats.mean(), 1.0e-6);
        assertTrue(stats.standardDeviation() > 0);
        assertEquals("m", stats.unit());
    }

    @Test
    void geoTiffRoundTripPreservesGeometryCrsAndValues() throws Exception {
        AnalysisRaster raster = ramp();
        File file = directory.resolve("pendiente.tif").toFile();
        raster.exportGeoTiff(file);

        try (java.io.InputStream input = Files.newInputStream(file.toPath())) {
            GeoTiffReader reader = new GeoTiffReader(input);
            GridCoverage2D coverage = reader.read(null);
            try {
                assertEquals(4, coverage.getRenderedImage().getWidth());
                assertEquals(3, coverage.getRenderedImage().getHeight());
                ReferencedEnvelope envelope = new ReferencedEnvelope(
                        coverage.getEnvelope2D(), reader.getCoordinateReferenceSystem());
                assertEquals(1000, envelope.getMinX(), 1.0e-6);
                assertEquals(2000, envelope.getMinY(), 1.0e-6);
                assertEquals(1040, envelope.getMaxX(), 1.0e-6);
                assertEquals(2030, envelope.getMaxY(), 1.0e-6);
                assertTrue(org.geotools.referencing.CRS.toSRS(
                        reader.getCoordinateReferenceSystem()).contains("32719"));
            } finally {
                coverage.dispose(true);
                reader.dispose();
            }
        }
        try (var files = Files.list(directory)) {
            assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".part")));
        }
    }

    @Test
    void asciiGridRoundTripPreservesFloatValues() throws Exception {
        AnalysisRaster raster = ramp();
        File file = directory.resolve("pendiente.asc").toFile();
        raster.exportAsciiGrid(file);

        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        assertEquals("ncols 4", lines.get(0));
        assertEquals("nrows 3", lines.get(1));
        assertTrue(lines.get(2).startsWith("xllcorner 1000"));
        assertTrue(lines.get(3).startsWith("yllcorner 2000"));
        assertTrue(lines.get(4).startsWith("cellsize 10"));
        assertEquals(TerrainRasterAlgorithms.NO_DATA,
                Double.parseDouble(lines.get(5).split("\\s+")[1]), 1.0e-6);
        long values = lines.subList(6, lines.size()).stream()
                .filter(line -> !line.isBlank())
                .mapToLong(line -> line.trim().split("\\s+").length)
                .sum();
        assertEquals(12, values);
        assertEquals("0", lines.get(6).split("\\s+")[0]);
        assertEquals(0.5, Double.parseDouble(lines.get(6).split("\\s+")[1]), 1.0e-9);
    }

    @Test
    void invalidRastersAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnalysisRaster("d", "m", 0, 1, new float[0],
                        TerrainRasterAlgorithms.NO_DATA, 0, 0, 1, 1, "EPSG:4326"));
        assertThrows(IllegalArgumentException.class,
                () -> new AnalysisRaster("d", "m", 2, 2, new float[3],
                        TerrainRasterAlgorithms.NO_DATA, 0, 0, 1, 1, "EPSG:4326"));
        assertThrows(IllegalArgumentException.class,
                () -> new AnalysisRaster("d", "m", 2, 2, new float[4],
                        TerrainRasterAlgorithms.NO_DATA, 1, 0, 1, 1, "EPSG:4326"));
        assertThrows(IllegalArgumentException.class,
                () -> new AnalysisRaster("d", "m", 2, 2, new float[4],
                        TerrainRasterAlgorithms.NO_DATA, 0, 0, 1, 1, " "));
        AnalysisRaster raster = ramp();
        assertThrows(IndexOutOfBoundsException.class, () -> raster.valueAt(9, 9));
        assertEquals(10.0, raster.cellSizeX(), 1.0e-9);
        assertEquals(10.0, raster.cellSizeY(), 1.0e-9);
        AnalysisRaster empty = raster.emptyLike("Vacio", "m");
        assertEquals(12, empty.stats().noDataCount());
        assertEquals("EPSG:32719", empty.crsCode());
    }
}
