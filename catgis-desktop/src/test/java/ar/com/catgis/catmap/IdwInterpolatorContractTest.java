package ar.com.catgis.catmap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Own IDW implementation: known surfaces, exact nodes, gaps and validation. */
class IdwInterpolatorContractTest {

    /** Samples of the known plane z = 0.5x + 0.25y on a regular 5x5 grid. */
    private static List<IdwInterpolator.Sample> planeSamples() {
        List<IdwInterpolator.Sample> samples = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                double x = i * 25.0;
                double y = j * 25.0;
                samples.add(new IdwInterpolator.Sample(x, y, 0.5 * x + 0.25 * y));
            }
        }
        return samples;
    }

    @Test
    void interpolatesTheKnownPlane() {
        AnalysisRaster raster = IdwInterpolator.interpolate(planeSamples(), "IDW", "mm",
                0, 0, 100, 100, 10, 2.0, 8, 0, "EPSG:32719");
        assertEquals(10, raster.width());
        assertEquals(10, raster.height());
        double maxError = 0;
        double sumError = 0;
        long count = 0;
        for (int row = 0; row < raster.height(); row++) {
            double y = raster.maxY() - (row + 0.5) * raster.cellSizeY();
            for (int col = 0; col < raster.width(); col++) {
                double x = raster.minX() + (col + 0.5) * raster.cellSizeX();
                double expected = 0.5 * x + 0.25 * y;
                double actual = raster.valueAt(col, row);
                if (raster.isNoData(col, row)) {
                    continue;
                }
                double error = Math.abs(actual - expected);
                maxError = Math.max(maxError, error);
                sumError += error;
                count++;
            }
        }
        assertTrue(count > 90);
        assertTrue(maxError < 4.0, "max error was " + maxError);
        assertTrue(sumError / count < 1.3, "mean error was " + (sumError / count));
    }

    @Test
    void exactSampleNodesKeepTheirValue() {
        List<IdwInterpolator.Sample> samples = List.of(
                new IdwInterpolator.Sample(5, 5, 100),
                new IdwInterpolator.Sample(15, 5, 200),
                new IdwInterpolator.Sample(10, 15, 300));
        AnalysisRaster raster = IdwInterpolator.interpolate(samples, "IDW", "m",
                0, 0, 20, 20, 10, 2.0, 3, 0, "EPSG:32719");
        // Cell centres: (5,15), (15,15), (5,5), (15,5)...
        assertEquals(100.0, raster.valueAt(0, 1), 1.0e-3);
        assertEquals(200.0, raster.valueAt(1, 1), 1.0e-3);
    }

    @Test
    void boundedRadiusLeavesGapsAsNoData() {
        List<IdwInterpolator.Sample> samples = List.of(
                new IdwInterpolator.Sample(0, 0, 10),
                new IdwInterpolator.Sample(0, 1, 20),
                new IdwInterpolator.Sample(1, 0, 30));
        AnalysisRaster raster = IdwInterpolator.interpolate(samples, "IDW", "m",
                0, 0, 5, 5, 1, 2.0, 3, 2.0, "EPSG:32719");
        assertTrue(raster.stats().noDataCount() > 0,
                "cells farther than the radius must stay NoData");
        assertTrue(raster.stats().validCount() > 0);
    }

    @Test
    void validationFailsClosed() {
        List<IdwInterpolator.Sample> two = List.of(
                new IdwInterpolator.Sample(0, 0, 1),
                new IdwInterpolator.Sample(1, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> IdwInterpolator.interpolate(
                two, "IDW", "m", 0, 0, 10, 10, 1, 2, 3, 0, "EPSG:32719"));
        assertThrows(IllegalArgumentException.class, () -> IdwInterpolator.interpolate(
                planeSamples(), "IDW", "m", 10, 0, 10, 10, 1, 2, 3, 0, "EPSG:32719"));
        assertThrows(IllegalArgumentException.class, () -> IdwInterpolator.interpolate(
                planeSamples(), "IDW", "m", 0, 0, 10, 10, 0, 2, 3, 0, "EPSG:32719"));
        assertThrows(IllegalArgumentException.class, () -> IdwInterpolator.interpolate(
                planeSamples(), "IDW", "m", 0, 0, 10, 10, 1, -1, 3, 0, "EPSG:32719"));
        assertThrows(IllegalArgumentException.class, () -> IdwInterpolator.interpolate(
                planeSamples(), "IDW", "m", 0, 0, 10, 10, 1, 2, 0, 0, "EPSG:32719"));
        assertThrows(IllegalArgumentException.class, () -> IdwInterpolator.interpolate(
                planeSamples(), "IDW", "m", 0, 0, 10, 10, 1, 2, 3, 0, " "));
        assertThrows(IllegalArgumentException.class, () -> IdwInterpolator.interpolate(
                planeSamples(), "IDW", "m", 0, 0, 100_000, 100_000, 1, 2, 3, 0,
                "EPSG:32719"));
    }

    @Test
    void resultExportsAsGeoTiff() throws Exception {
        AnalysisRaster raster = IdwInterpolator.interpolate(planeSamples(), "IDW", "mm",
                0, 0, 100, 100, 10, 2.0, 8, 0, "EPSG:32719");
        File file = File.createTempFile("catgis-idw-", ".tif");
        file.deleteOnExit();
        raster.exportGeoTiff(file);
        assertTrue(file.length() > 0);
        try (java.io.InputStream input = Files.newInputStream(file.toPath())) {
            GeoTiffReader reader = new GeoTiffReader(input);
            GridCoverage2D coverage = reader.read(null);
            try {
                assertEquals(10, coverage.getRenderedImage().getWidth());
                assertEquals(10, coverage.getRenderedImage().getHeight());
                assertTrue(org.geotools.referencing.CRS.toSRS(
                        reader.getCoordinateReferenceSystem()).contains("32719"));
            } finally {
                coverage.dispose(true);
                reader.dispose();
            }
        }
    }

    @Test
    void descriptionIsReported() {
        assertEquals("IDW power=2.0 vecinos=8 radio=sin limite",
                IdwInterpolator.describe(2.0, 8, 0));
        assertEquals("IDW power=3.0 vecinos=4 radio=10.000",
                IdwInterpolator.describe(3.0, 4, 10.0));
    }
}
