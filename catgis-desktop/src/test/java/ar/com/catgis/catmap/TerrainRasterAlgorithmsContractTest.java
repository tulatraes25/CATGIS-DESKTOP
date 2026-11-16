package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Terrain algorithms against fixtures with a known constant slope. */
class TerrainRasterAlgorithmsContractTest {

    private static final float NO_DATA = TerrainRasterAlgorithms.NO_DATA;
    private static final int SIZE = 9;

    /** Plane rising 1 m per 10 m cell towards the east. */
    private static AnalysisRaster eastRisingPlane() {
        float[] values = new float[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                values[y * SIZE + x] = x; // 1 m per cell, cell size 10 m
            }
        }
        return new AnalysisRaster("DEM", "m", SIZE, SIZE, values, NO_DATA,
                0, 0, SIZE * 10.0, SIZE * 10.0, "EPSG:32719");
    }

    private static AnalysisRaster flat() {
        float[] values = new float[SIZE * SIZE];
        return new AnalysisRaster("DEM", "m", SIZE, SIZE, values, NO_DATA,
                0, 0, SIZE * 10.0, SIZE * 10.0, "EPSG:32719");
    }

    @Test
    void eastRisingPlaneHasKnownSlope() {
        AnalysisRaster slope = TerrainRasterAlgorithms.slopeDegrees(eastRisingPlane());
        double expected = Math.toDegrees(Math.atan(1.0 / 10.0));
        for (int y = 1; y < SIZE - 1; y++) {
            for (int x = 1; x < SIZE - 1; x++) {
                assertEquals(expected, slope.valueAt(x, y), 1.0e-3);
            }
        }
        assertEquals("degrees", slope.unit());
        assertTrue(slope.isNoData(0, 0), "border cells stay NoData");
    }

    @Test
    void flatTerrainHasZeroSlopeAndConstantHillshade() {
        AnalysisRaster slope = TerrainRasterAlgorithms.slopeDegrees(flat());
        assertEquals(0.0, slope.valueAt(4, 4), 1.0e-6);
        AnalysisRaster shade = TerrainRasterAlgorithms.hillshade(flat());
        assertEquals(Math.cos(Math.toRadians(45)), shade.valueAt(4, 4), 1.0e-4);
        AnalysisRaster aspect = TerrainRasterAlgorithms.aspectDegrees(flat());
        assertTrue(aspect.isNoData(4, 4), "flat terrain has no aspect");
    }

    @Test
    void eastRisingPlaneFacesWest() {
        AnalysisRaster aspect = TerrainRasterAlgorithms.aspectDegrees(eastRisingPlane());
        assertEquals(270.0, aspect.valueAt(4, 4), 1.0e-3);
    }

    @Test
    void d8DirectionAndAccumulationFollowThePlane() {
        AnalysisRaster direction = TerrainRasterAlgorithms.flowDirection(eastRisingPlane());
        assertEquals(16.0, direction.valueAt(4, 4), 1.0e-6, "D8 W");
        AnalysisRaster accumulation =
                TerrainRasterAlgorithms.flowAccumulation(eastRisingPlane());
        assertEquals(SIZE, accumulation.valueAt(0, 4), 1.0e-4,
                "the western edge drains the whole row");
        assertEquals(SIZE - 1, accumulation.valueAt(1, 4), 1.0e-4);
        assertEquals(1.0, accumulation.valueAt(SIZE - 1, 4), 1.0e-4);
    }

    @Test
    void noDataCellsPropagateToNeighbours() {
        AnalysisRaster dem = eastRisingPlane();
        float[] values = dem.copyValues();
        values[4 * SIZE + 4] = NO_DATA;
        AnalysisRaster withHole = new AnalysisRaster("DEM", "m", SIZE, SIZE, values, NO_DATA,
                dem.minX(), dem.minY(), dem.maxX(), dem.maxY(), dem.crsCode());
        AnalysisRaster slope = TerrainRasterAlgorithms.slopeDegrees(withHole);
        assertTrue(slope.isNoData(4, 4));
        assertTrue(slope.isNoData(3, 4));
        assertTrue(slope.isNoData(5, 5));
        assertTrue(!slope.isNoData(2, 2));
    }

    @Test
    void hillshadeStaysInTheZeroOneRange() {
        AnalysisRaster shade = TerrainRasterAlgorithms.hillshade(eastRisingPlane());
        for (int y = 1; y < SIZE - 1; y++) {
            for (int x = 1; x < SIZE - 1; x++) {
                assertTrue(shade.valueAt(x, y) >= 0.0 && shade.valueAt(x, y) <= 1.0);
            }
        }
        assertEquals("fraction", shade.unit());
        assertEquals("d8-code", TerrainRasterAlgorithms.flowDirection(eastRisingPlane()).unit());
        assertEquals("cells", TerrainRasterAlgorithms.flowAccumulation(
                eastRisingPlane()).unit());
    }
}
