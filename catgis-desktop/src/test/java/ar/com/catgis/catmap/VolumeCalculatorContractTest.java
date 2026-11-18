package ar.com.catgis.catmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cut/fill volumes against analytic fixtures with exact expected values. */
class VolumeCalculatorContractTest {

    private static final float NO_DATA = TerrainRasterAlgorithms.NO_DATA;

    /** 5x5 grid, 10 m cells, z = 1 m * column (cell area 100 m2). */
    private static AnalysisRaster plane() {
        float[] values = new float[25];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                values[y * 5 + x] = x;
            }
        }
        return new AnalysisRaster("DEM", "m", 5, 5, values, NO_DATA,
                0, 0, 50, 50, "EPSG:32719");
    }

    @Test
    void cutAgainstAPlaneIsExact() {
        VolumeCalculator.VolumeResult result =
                VolumeCalculator.againstPlane(plane(), 2.0);
        // Deltas per column: -2,-1,0,1,2 => cut = (1+2)*5*100 = 1500,
        // fill = (1+2)*5*100 = 1500, net = 0.
        assertEquals(25, result.validCells());
        assertEquals(2500, result.areaM2(), 1.0e-9);
        assertEquals(1500.0, result.cutM3(), 1.0e-9);
        assertEquals(1500.0, result.fillM3(), 1.0e-9);
        assertEquals(0.0, result.netM3(), 1.0e-9);
        assertEquals(-2.0, result.minDeltaM(), 1.0e-9);
        assertEquals(2.0, result.maxDeltaM(), 1.0e-9);
        assertEquals(0.0, result.meanDeltaM(), 1.0e-9);
    }

    @Test
    void betweenTwoDemsComputesNetCut() {
        AnalysisRaster existing = plane();
        float[] raised = existing.copyValues();
        for (int i = 0; i < raised.length; i++) {
            raised[i] += 2f;
        }
        AnalysisRaster proposed = new AnalysisRaster("Propuesto", "m", 5, 5, raised,
                NO_DATA, 0, 0, 50, 50, "EPSG:32719");

        VolumeCalculator.VolumeResult result =
                VolumeCalculator.between(existing, proposed);
        assertEquals(25, result.validCells());
        // proposed - existing = +2 m everywhere => material to FILL, not cut.
        assertEquals(0.0, result.cutM3(), 1.0e-9);
        assertEquals(5000.0, result.fillM3(), 1.0e-9);
        assertEquals(-5000.0, result.netM3(), 1.0e-9);

        VolumeCalculator.VolumeResult reversed =
                VolumeCalculator.between(proposed, existing);
        assertEquals(5000.0, reversed.cutM3(), 1.0e-9);
        assertEquals(0.0, reversed.fillM3(), 1.0e-9);
        assertEquals(5000.0, reversed.netM3(), 1.0e-9);
    }

    @Test
    void noDataCellsAreExcluded() {
        float[] values = new float[25];
        for (int i = 0; i < values.length; i++) {
            values[i] = 1f;
        }
        values[0] = NO_DATA;
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", 5, 5, values, NO_DATA,
                0, 0, 50, 50, "EPSG:32719");
        VolumeCalculator.VolumeResult result =
                VolumeCalculator.againstPlane(dem, 0.0);
        assertEquals(24, result.validCells());
        assertEquals(2400.0, result.cutM3(), 1.0e-9);
    }

    @Test
    void differenceRasterPreservesDeltasAndNoData() {
        AnalysisRaster existing = plane();
        float[] proposedValues = new float[25];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                proposedValues[y * 5 + x] = x + (y == 0 ? 1f : 0f);
            }
        }
        proposedValues[12] = NO_DATA;
        AnalysisRaster proposed = new AnalysisRaster("Propuesto", "m", 5, 5,
                proposedValues, NO_DATA, 0, 0, 50, 50, "EPSG:32719");
        AnalysisRaster difference = VolumeCalculator.differenceRaster(existing, proposed);
        assertEquals(1.0, difference.valueAt(0, 0), 1.0e-6);
        assertEquals(0.0, difference.valueAt(1, 1), 1.0e-6);
        assertTrue(difference.isNoData(2, 2));
        assertEquals("m", difference.unit());
    }

    @Test
    void geographicCellsUseMetricArea() {
        // 2x2 cells of 0.0005 deg at the equator: ~55.66 m x ~55.29 m each.
        float[] values = {10f, 10f, 10f, 10f};
        AnalysisRaster dem = new AnalysisRaster("DEM", "m", 2, 2, values, NO_DATA,
                0, 0, 0.001, 0.001, "EPSG:4326");
        VolumeCalculator.VolumeResult result =
                VolumeCalculator.againstPlane(dem, 0.0);
        double expectedCellArea = ar.com.catgis.climate.GeographicMetrics
                .metersPerDegreeLongitude(0.0005) * 0.0005
                * ar.com.catgis.climate.GeographicMetrics
                        .metersPerDegreeLatitude(0.0005) * 0.0005;
        assertEquals(4 * expectedCellArea, result.areaM2(), expectedCellArea * 4 * 1.0e-6);
        assertTrue(result.areaM2() > 10_000, "degrees must not be treated as metres");
        assertEquals(10.0 * result.areaM2(), result.cutM3(), 1.0e-3);
    }

    @Test
    void geometryMismatchAndInvalidInputsFailClosed() {
        AnalysisRaster small = new AnalysisRaster("small", "m", 4, 5,
                new float[20], NO_DATA, 0, 0, 50, 50, "EPSG:32719");
        assertThrows(IllegalArgumentException.class,
                () -> VolumeCalculator.between(plane(), small));
        AnalysisRaster otherCrs = new AnalysisRaster("otro", "m", 5, 5,
                new float[25], NO_DATA, 0, 0, 50, 50, "EPSG:4326");
        assertThrows(IllegalArgumentException.class,
                () -> VolumeCalculator.between(plane(), otherCrs));
        assertThrows(IllegalArgumentException.class,
                () -> VolumeCalculator.againstPlane(null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> VolumeCalculator.againstPlane(plane(), Double.NaN));
        float[] noData = new float[25];
        java.util.Arrays.fill(noData, NO_DATA);
        AnalysisRaster allNoData = new AnalysisRaster("nd", "m", 5, 5, noData,
                NO_DATA, 0, 0, 50, 50, "EPSG:32719");
        assertThrows(IllegalStateException.class,
                () -> VolumeCalculator.againstPlane(allNoData, 0));
    }
}
