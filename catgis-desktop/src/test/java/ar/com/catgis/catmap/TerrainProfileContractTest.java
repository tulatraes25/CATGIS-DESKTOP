package ar.com.catgis.catmap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Terrain profile against analytic DEM fixtures. */
class TerrainProfileContractTest {

    private static final float NO_DATA = TerrainRasterAlgorithms.NO_DATA;

    /** 11x11 grid, 10 m cells, z rises 1 m per column (0..10 m). */
    private static AnalysisRaster plane() {
        float[] values = new float[121];
        for (int y = 0; y < 11; y++) {
            for (int x = 0; x < 11; x++) {
                values[y * 11 + x] = x;
            }
        }
        return new AnalysisRaster("DEM", "m", 11, 11, values, NO_DATA,
                0, 0, 110, 110, "EPSG:32719");
    }

    @Test
    void profileOnAKnownPlaneIsExact() {
        TerrainProfile.Result result = TerrainProfile.sample(
                plane(), 5, 55, 105, 55, 11);
        assertEquals(100.0, result.lengthM(), 1.0e-9);
        assertEquals(0.0, result.minElevationM(), 1.0e-6);
        assertEquals(10.0, result.maxElevationM(), 1.0e-6);
        assertEquals(10.0, result.reliefM(), 1.0e-6);
        double expectedSlope = Math.toDegrees(Math.atan(1.0 / 10.0));
        assertEquals(expectedSlope, result.meanSlopeDegrees(), 0.05);
        assertEquals(expectedSlope, result.maxSlopeDegrees(), 0.05);
        assertEquals(10.0, result.cumulativeAscentM(), 1.0e-6);
        assertEquals(0.0, result.cumulativeDescentM(), 1.0e-6);
        assertEquals(11, result.samples().size());
        assertEquals(0.0, result.samples().get(0).elevationM(), 0.3);
        assertEquals(10.0, result.samples().get(10).elevationM(), 0.3);
    }

    @Test
    void hypsometricCurveIsMonotonic() {
        TerrainProfile.Result result = TerrainProfile.sample(
                plane(), 5, 55, 105, 55, 41, 10);
        assertEquals(11, result.hypsometricCurve().size());
        double previousArea = Double.POSITIVE_INFINITY;
        for (TerrainProfile.HypsometricPoint point : result.hypsometricCurve()) {
            assertTrue(point.relativeArea() >= 0 && point.relativeArea() <= 1);
            assertTrue(point.relativeArea() <= previousArea + 1.0e-9,
                    "relative area must not increase with height");
            previousArea = point.relativeArea();
        }
        assertEquals(1.0, result.hypsometricCurve().get(0).relativeArea(), 1.0e-9);
        assertTrue(result.hypsometricCurve().get(10).relativeArea() <= 0.1,
                "only the summit cells remain at the top band");
    }

    @Test
    void profileCanBeExportedAsA3DLine() {
        TerrainProfile.Result result = TerrainProfile.sample(
                plane(), 5, 55, 105, 55, 11);
        LineString line = result.profile3D();
        assertNotNull(line);
        assertEquals(11, line.getNumPoints());
        Coordinate first = line.getCoordinateN(0);
        Coordinate last = line.getCoordinateN(10);
        assertEquals(0.0, first.getZ(), 0.3);
        assertEquals(10.0, last.getZ(), 0.3);
        assertTrue(last.x > first.x);
    }

    @Test
    void noDataCellsBreakTheProfileWithoutInvalidMetrics() {
        float[] values = plane().copyValues();
        values[5 * 11 + 5] = NO_DATA;
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", 11, 11, values, NO_DATA,
                0, 0, 110, 110, "EPSG:32719");
        TerrainProfile.Result result = TerrainProfile.sample(
                dem, 5, 55, 105, 55, 11);
        assertTrue(result.samples().stream()
                .anyMatch(sample -> !Double.isFinite(sample.elevationM())));
        assertTrue(Double.isFinite(result.meanElevationM()));
        assertTrue(Double.isFinite(result.meanSlopeDegrees()));
        assertEquals(10.0, result.reliefM(), 1.0e-6);
    }

    @Test
    void invalidInputsFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> TerrainProfile.sample(null, 0, 0, 10, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> TerrainProfile.sample(plane(), -5, 0, 10, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> TerrainProfile.sample(plane(), 0, 0, 500, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> TerrainProfile.sample(plane(), 10, 10, 10, 10, 10));
        assertThrows(IllegalArgumentException.class,
                () -> TerrainProfile.sample(plane(), 0, 0, 10, 10, 1));
        assertThrows(IllegalArgumentException.class,
                () -> TerrainProfile.sample(plane(), 0, 0, 10, 10, 5, 1));
    }
}
