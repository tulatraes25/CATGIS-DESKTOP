package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Basin morphometry against synthetic basins with known geometry. */
class BasinMorphometryContractTest {

    private static final float NO_DATA = TerrainRasterAlgorithms.NO_DATA;
    private static final int SIZE = 20;

    /** Plane rising 1.5 m per 10 m cell towards the east: it drains west. */
    private static AnalysisRaster[] eastRisingPlane() {
        float[] values = new float[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                values[y * SIZE + x] = x * 1.5f;
            }
        }
        return grids(values, SIZE, SIZE);
    }

    private static AnalysisRaster[] grids(float[] values, int width, int height) {
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", width, height, values, NO_DATA,
                0, 0, width * 10.0, height * 10.0, "EPSG:32719");
        AnalysisRaster direction = TerrainRasterAlgorithms.flowDirection(dem);
        AnalysisRaster accumulation = TerrainRasterAlgorithms.flowAccumulation(dem);
        AnalysisRaster slope = TerrainRasterAlgorithms.slopeDegrees(dem);
        return new AnalysisRaster[]{dem, direction, accumulation, slope};
    }

    /** Two flanks draining into a central valley row that drains west. */
    private static AnalysisRaster[] valley() {
        int size = 5;
        float[] values = new float[size * size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                values[y * size + x] = 10f * Math.abs(y - 2) + x;
            }
        }
        return grids(values, size, size);
    }

    @Test
    void eastRisingPlaneBasinHasKnownGeometry() {
        AnalysisRaster[] grids = eastRisingPlane();
        BasinMorphometry.BasinStats stats = BasinMorphometry.compute(
                grids[0], grids[1], grids[2], grids[3], 0, 10, 2);

        assertEquals(20, stats.cellCount(), "the outlet drains its whole row");
        assertEquals(0.002, stats.areaKm2(), 1.0e-9);
        assertEquals(0.42, stats.perimeterKm(), 1.0e-6);
        assertEquals(0.0, stats.minElevationM(), 1.0e-6);
        assertEquals(28.5, stats.maxElevationM(), 1.0e-6);
        assertEquals(14.25, stats.meanElevationM(), 1.0e-6);
        assertEquals(28.5, stats.reliefM(), 1.0e-6);
        assertEquals(Math.toDegrees(Math.atan(0.15)), stats.meanSlopeDegrees(), 0.05);
        assertEquals(1, stats.strahlerOrder(), "a single chain has order 1");
        assertEquals(0.18, stats.channelLengthKm(), 1.0e-6);
        assertEquals(90.0, stats.drainageDensityKmPerKm2(), 1.0e-3);
    }

    @Test
    void flatDemBasinIsASingleCell() {
        float[] values = new float[SIZE * SIZE];
        AnalysisRaster[] grids = grids(values, SIZE, SIZE);
        BasinMorphometry.BasinStats stats = BasinMorphometry.compute(
                grids[0], grids[1], grids[2], grids[3], 5, 5, 2);
        assertEquals(1, stats.cellCount());
        assertEquals(0.0001, stats.areaKm2(), 1.0e-12);
        assertEquals(0.04, stats.perimeterKm(), 1.0e-9);
        assertEquals(0, stats.strahlerOrder(), "no channel reaches the threshold");
        assertEquals(0.0, stats.channelLengthKm(), 1.0e-12);
    }

    @Test
    void valleyBasinBranchesIntoStrahlerTwo() {
        AnalysisRaster[] grids = valley();
        BasinMorphometry.BasinStats stats = BasinMorphometry.compute(
                grids[0], grids[1], grids[2], grids[3], 0, 2, 2);
        assertEquals(25, stats.cellCount(), "both flanks drain into the valley");
        assertEquals(0.0025, stats.areaKm2(), 1.0e-12);
        assertEquals(2, stats.strahlerOrder(), "two tributaries produce order 2");
        assertTrue(stats.drainageDensityKmPerKm2() > 0);
        assertTrue(stats.meanSlopeDegrees() > 0);
    }

    @Test
    void geometryAndOutletAreValidated() {
        AnalysisRaster[] grids = eastRisingPlane();
        assertThrows(IllegalArgumentException.class, () -> BasinMorphometry.compute(
                null, grids[1], grids[2], grids[3], 0, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> BasinMorphometry.compute(
                grids[0], grids[1], grids[2], grids[3], -1, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> BasinMorphometry.compute(
                grids[0], grids[1], grids[2], grids[3], SIZE, 0, 2));

        AnalysisRaster small = new AnalysisRaster("small", "m", 3, 3,
                new float[9], NO_DATA, 0, 0, 30, 30, "EPSG:32719");
        assertThrows(IllegalArgumentException.class, () -> BasinMorphometry.compute(
                grids[0], small, grids[2], grids[3], 0, 0, 2));
    }

    @Test
    void noDataOutletIsRejectedAndHelpersWork() {
        AnalysisRaster[] grids = eastRisingPlane();
        float[] values = grids[0].copyValues();
        values[10 * SIZE] = NO_DATA;
        AnalysisRaster withHole = new AnalysisRaster("DEM", "m", SIZE, SIZE, values, NO_DATA,
                0, 0, SIZE * 10.0, SIZE * 10.0, "EPSG:32719");
        assertThrows(IllegalArgumentException.class, () -> BasinMorphometry.compute(
                withHole, grids[1], grids[2], grids[3], 0, 10, 2));

        assertEquals(20, BasinMorphometry.basinCellCount(grids[1], grids[0], 0, 10));
        assertEquals(19, BasinMorphometry.channelCells(grids[1], grids[2], grids[0],
                0, 10, 2).size());
    }
    private static AnalysisRaster[] gridsGeographic(float[] values, int width, int height) {
        double cellDegrees = 0.01;
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", width, height, values, NO_DATA,
                0, 0, width * cellDegrees, height * cellDegrees, "EPSG:4326");
        AnalysisRaster direction = TerrainRasterAlgorithms.flowDirection(dem);
        AnalysisRaster accumulation = TerrainRasterAlgorithms.flowAccumulation(dem);
        float[] flat = new float[width * height];
        AnalysisRaster slope = new AnalysisRaster("Pendiente", "degrees", width, height,
                flat, NO_DATA, 0, 0, width * cellDegrees, height * cellDegrees,
                "EPSG:4326");
        return new AnalysisRaster[]{dem, direction, accumulation, slope};
    }

    @Test
    void geographicBasinUsesMetricAreaAndLengths() {
        float[] values = new float[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                values[y * SIZE + x] = x * 1.5f;
            }
        }
        AnalysisRaster[] grids = gridsGeographic(values, SIZE, SIZE);
        BasinMorphometry.BasinStats stats = BasinMorphometry.compute(
                grids[0], grids[1], grids[2], grids[3], 0, 10, 2);

        double centerLat = SIZE * 0.01 / 2.0;
        double metersPerDegLon = ar.com.catgis.climate.GeographicMetrics
                .metersPerDegreeLongitude(centerLat);
        double metersPerDegLat = ar.com.catgis.climate.GeographicMetrics
                .metersPerDegreeLatitude(centerLat);
        double cellAreaM2 = metersPerDegLon * 0.01 * metersPerDegLat * 0.01;

        assertEquals(20 * cellAreaM2 / 1.0e6, stats.areaKm2(), 1.0e-9);
        assertTrue(stats.areaKm2() > 10.0,
                "degree cells must be converted to metres (area=" + stats.areaKm2() + ")");
        double expectedPerimeterKm = (2 * SIZE * 0.01 * metersPerDegLon
                + 2 * 0.01 * metersPerDegLat) / 1000.0;
        assertEquals(expectedPerimeterKm, stats.perimeterKm(), expectedPerimeterKm * 1.0e-6);
        assertTrue(stats.channelLengthKm() > 10.0,
                "channel length must use metres, not degrees (" + stats.channelLengthKm() + ")");
    }
}