package ar.com.catgis;

import ar.com.catgis.catmap.AnalysisRaster;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.catmap.TerrainRasterAlgorithms;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T20: the conditioned DEM is exportable as a float raster with traceability. */
class ConditionedDemExportContractTest {

    private static final float NO_DATA = TerrainRasterAlgorithms.NO_DATA;
    private static final int SIZE = 12;

    /** Plane rising towards the east with a deep pit in the middle. */
    private File pitDem() throws Exception {
        float[] values = new float[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                values[y * SIZE + x] = x * 1.5f;
            }
        }
        values[6 * SIZE + 6] = -50f;
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", SIZE, SIZE, values, NO_DATA,
                0, 0, SIZE * 10.0, SIZE * 10.0, "EPSG:32719");
        File file = File.createTempFile("catgis-pit-dem-", ".tif");
        file.deleteOnExit();
        dem.exportGeoTiff(file);
        return file;
    }

    private File demWithNoData() throws Exception {
        float[] values = new float[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                values[y * SIZE + x] = x + y;
            }
        }
        values[5 * SIZE + 5] = NO_DATA;
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", SIZE, SIZE, values, NO_DATA,
                0, 0, SIZE * 10.0, SIZE * 10.0, "EPSG:32719");
        File file = File.createTempFile("catgis-nodata-dem-", ".tif");
        file.deleteOnExit();
        dem.exportGeoTiff(file);
        return file;
    }

    @Test
    void conditioningFillsThePitAndKeepsTheNativeGeometry() throws Exception {
        File dem = pitDem();
        DrainageExtractionService.ConditionedDemExport export =
                DrainageExtractionService.buildConditionedDem(
                        new RasterLayer("DEM", dem.getAbsolutePath()),
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.ROBUST);

        AnalysisRaster raster = export.raster();
        assertEquals(SIZE, raster.width());
        assertEquals(SIZE, raster.height());
        assertEquals("m", raster.unit());
        assertEquals("EPSG:32719", export.sourceCrsCode());
        assertEquals(DrainageExtractionService.AnalysisDetail.NATIVE, export.detail());
        assertEquals(DrainageExtractionService.HydrologicConditioning.ROBUST,
                export.conditioning());
        double conditionedPit = raster.valueAt(6, 6);
        assertTrue(conditionedPit > -50f, "the pit must be filled");
        assertTrue(conditionedPit >= 0.0 && conditionedPit <= 12.0,
                "filled to a neighbour elevation, got " + conditionedPit);
        assertTrue(raster.valueAt(0, 0) < raster.valueAt(SIZE - 1, 0),
                "the plane keeps its slope");
    }

    @Test
    void noDataCellsArePreserved() throws Exception {
        File dem = demWithNoData();
        DrainageExtractionService.ConditionedDemExport export =
                DrainageExtractionService.buildConditionedDem(
                        new RasterLayer("DEM", dem.getAbsolutePath()),
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.ROBUST);
        assertTrue(export.raster().isNoData(5, 5));
        assertEquals(1, export.raster().stats().noDataCount());
        assertEquals(SIZE * SIZE - 1, export.raster().stats().validCount());
    }

    @Test
    void conditionedDemExportsAsGeoTiff() throws Exception {
        File dem = pitDem();
        DrainageExtractionService.ConditionedDemExport export =
                DrainageExtractionService.buildConditionedDem(
                        new RasterLayer("DEM", dem.getAbsolutePath()),
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.BASIC);
        assertEquals(DrainageExtractionService.HydrologicConditioning.BASIC,
                export.conditioning());
        File out = File.createTempFile("catgis-conditioned-", ".tif");
        out.deleteOnExit();
        export.raster().exportGeoTiff(out);
        assertTrue(out.length() > 0);

        try (java.io.InputStream input = Files.newInputStream(out.toPath())) {
            GeoTiffReader reader = new GeoTiffReader(input);
            GridCoverage2D coverage = reader.read(null);
            try {
                assertEquals(SIZE, coverage.getRenderedImage().getWidth());
                assertEquals(SIZE, coverage.getRenderedImage().getHeight());
                assertTrue(org.geotools.referencing.CRS.toSRS(
                        reader.getCoordinateReferenceSystem()).contains("32719"));
            } finally {
                coverage.dispose(true);
                reader.dispose();
            }
        }
    }

    @Test
    void conditioningIsCancellableAndRejectsNonRasterLayers() throws Exception {
        File dem = pitDem();
        assertThrows(CancellationException.class, () ->
                DrainageExtractionService.buildConditionedDem(
                        new RasterLayer("DEM", dem.getAbsolutePath()),
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.ROBUST,
                        () -> true));
        assertThrows(IllegalArgumentException.class, () ->
                DrainageExtractionService.buildConditionedDem(
                        new Layer("no-raster", dem.getAbsolutePath(), "VECTOR"),
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.ROBUST));
    }
}
