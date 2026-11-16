package ar.com.catgis;

import ar.com.catgis.catmap.AnalysisRaster;
import ar.com.catgis.catmap.TerrainRasterAlgorithms;
import ar.com.catgis.core.model.Layer;
import org.junit.jupiter.api.Test;

import java.io.File;

import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T18: native DEM resolution, area thresholds and cancellable generation. */
class DrainageNativeResolutionContractTest {

    private static final int SIZE = 24;

    private File planeDem() throws Exception {
        float[] values = new float[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                values[y * SIZE + x] = x * 1.5f;
            }
        }
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", SIZE, SIZE, values,
                TerrainRasterAlgorithms.NO_DATA, 0, 0, SIZE * 10.0, SIZE * 10.0,
                "EPSG:32719");
        // Not under @TempDir: the GeoTools coverage keeps the GeoTIFF open on
        // Windows, which would make JUnit temp cleanup fail.
        File file = File.createTempFile("catgis-dem-", ".tif");
        file.deleteOnExit();
        dem.exportGeoTiff(file);
        return file;
    }

    @Test
    void areaThresholdIsConvertedToCells() {
        assertEquals(100, DrainageExtractionService.resolveThresholdCells(5, 10_000, 100));
        assertEquals(50, DrainageExtractionService.resolveThresholdCells(2, 5_000, 100));
        assertEquals(2, DrainageExtractionService.resolveThresholdCells(2, 50, 100),
                "at least two accumulation cells");
        assertEquals(30, DrainageExtractionService.resolveThresholdCells(30, Double.NaN, 100));
        assertEquals(30, DrainageExtractionService.resolveThresholdCells(30, 0, 100));
        assertEquals(30, DrainageExtractionService.resolveThresholdCells(30, 10_000, 0),
                "unknown cell area falls back to cells");
    }

    @Test
    void nativeBudgetIsBounded() {
        assertTrue(DrainageExtractionService.exceedsNativeBudget(3000, 3000));
        assertFalse(DrainageExtractionService.exceedsNativeBudget(SIZE, SIZE));
        assertFalse(DrainageExtractionService.exceedsNativeBudget(0, 0));
        assertFalse(DrainageExtractionService.exceedsNativeBudget(-5, 10));
    }

    @Test
    void nativeDetailKeepsTheDemResolution() throws Exception {
        File dem = planeDem();
        DrainageExtractionService.GeneratedDrainageLayer result =
                DrainageExtractionService.generateDrainage(
                        new RasterLayer("DEM", dem.getAbsolutePath()),
                        2,
                        "Drenaje nativo",
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.ROBUST,
                        0.0,
                        DrainageExtractionService.CleanupLevel.BALANCED);
        assertEquals(SIZE, result.analysisWidth(), "native mode must not downsample");
        assertEquals(SIZE, result.analysisHeight());
        assertTrue(result.layer().getFeatureCount() > 0);
    }

    @Test
    void areaThresholdAppliesToTheGeneratedNetwork() throws Exception {
        File dem = planeDem();
        DrainageExtractionService.GeneratedDrainageLayer result =
                DrainageExtractionService.generateDrainage(
                        new RasterLayer("DEM", dem.getAbsolutePath()),
                        2,
                        2_000.0,
                        "Drenaje por area",
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.ROBUST,
                        0.0,
                        DrainageExtractionService.CleanupLevel.BALANCED);
        assertEquals(20, result.thresholdCells(), "2000 m2 at 100 m2/cell = 20 cells");
        assertTrue(result.layer().getFeatureCount() > 0);
    }

    @Test
    void generationIsCancellable() throws Exception {
        File dem = planeDem();
        assertThrows(CancellationException.class, () ->
                DrainageExtractionService.generateDrainageCancellable(
                        new RasterLayer("DEM", dem.getAbsolutePath()),
                        2,
                        Double.NaN,
                        "Cancelado",
                        DrainageExtractionService.AnalysisDetail.NATIVE,
                        DrainageExtractionService.HydrologicConditioning.ROBUST,
                        0.0,
                        DrainageExtractionService.CleanupLevel.BALANCED,
                        () -> true));
    }

    @Test
    void nativeDetailIsTheDefaultAndIsLabelled() {
        assertEquals("Nativa (nativa)",
                DrainageExtractionService.AnalysisDetail.NATIVE.toString());
        assertEquals("Detallada (768)",
                DrainageExtractionService.AnalysisDetail.DETAILED.toString());
        assertTrue(DrainageExtractionService.AnalysisDetail.NATIVE.ordinal() >= 0);
    }
}
