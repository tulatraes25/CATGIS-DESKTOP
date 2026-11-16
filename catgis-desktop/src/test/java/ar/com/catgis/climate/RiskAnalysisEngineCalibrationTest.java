package ar.com.catgis.climate;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T25 calibration: WGS84 metrics, explicit parameters, classes and traceability. */
class RiskAnalysisEngineCalibrationTest {

    private static final int SIZE = 16;

    private static float[][] plane() {
        float[][] dem = new float[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                dem[r][c] = 100f + (r - SIZE / 2f) * 2f + c * 0.5f;
            }
        }
        return dem;
    }

    @Test
    void wgs84MetricsReplaceTheMagicConstant() {
        // Reference values: WGS84 metres per degree at the equator/pole.
        assertEquals(110574.0, GeographicMetrics.metersPerDegreeLatitude(0), 1.0);
        assertEquals(111694.0, GeographicMetrics.metersPerDegreeLatitude(90), 1.0);
        assertEquals(111320.0, GeographicMetrics.metersPerDegreeLongitude(0), 1.0);
        assertEquals(111320.0 / 2.0, GeographicMetrics.metersPerDegreeLongitude(60), 400.0);
        assertEquals(1.0 / GeographicMetrics.metersPerDegreeLatitude(-38),
                GeographicMetrics.degreesPerMeterLatitude(-38), 1.0e-12);
    }

    @Test
    void explicitCellSizeAndReturnPeriodArePersistedAndTraceable() {
        Map<String, Double> params = new HashMap<>();
        params.put("cellSizeMeters", 10.0);
        params.put("returnPeriodYears", 50.0);
        params.put("exposureFactor", 1.5);
        var result = RiskAnalysisEngine.analyze(plane(),
                RiskAnalysisEngine.Scenario.FLOOD, -38.0, -68.0, "Sitio", params);

        assertEquals(10.0, result.cellSize(), 1.0e-9);
        assertEquals(50, result.returnPeriodYears());
        assertEquals(1.5, result.exposureFactor(), 1.0e-9);
        String spec = result.parameterSpec();
        assertTrue(spec.contains("algorithm=" + RiskAnalysisEngine.ALGORITHM_VERSION));
        assertTrue(spec.contains("crs=" + RiskAnalysisEngine.SCENARIO_CRS));
        assertTrue(spec.contains("scenario=FLOOD"));
        assertTrue(spec.contains("cellSizeMeters=10.0"));
        assertTrue(spec.contains("returnPeriodYears=50"));
        assertTrue(spec.contains("exposureFactor=1.5"));
        assertTrue(spec.contains("susceptibilityClasses="));
        assertTrue(spec.contains("wildfire=withdrawn"));
        assertTrue(result.warnings().stream()
                .anyMatch(warning -> warning.contains("Periodo de retorno")));
    }

    @Test
    void defaultCellSizeIsDocumentedAndExplicit() {
        var result = RiskAnalysisEngine.analyze(plane(),
                RiskAnalysisEngine.Scenario.LANDSLIDE, 0, 0, "Sitio", (Map<String, Double>) null);
        assertEquals(RiskAnalysisEngine.DEFAULT_CELL_SIZE_METERS, result.cellSize(), 1.0e-9);
        assertEquals(RiskAnalysisEngine.DEFAULT_RETURN_PERIOD_YEARS, result.returnPeriodYears());
        assertEquals(1.0, result.exposureFactor(), 1.0e-9);
        // Cell centres use WGS84 metrics: west/north must be finite and consistent.
        assertTrue(Double.isFinite(result.west()));
        assertTrue(Double.isFinite(result.north()));
        assertTrue(result.riskGrid().length == SIZE);
        for (double[] row : result.riskGrid()) {
            for (double value : row) {
                assertTrue(value >= 0 && value <= 1);
            }
        }
    }

    @Test
    void susceptibilityClassesCoverTheIndexAndClassify() {
        List<RiskAnalysisEngine.SusceptibilityClass> classes =
                RiskAnalysisEngine.susceptibilityClasses();
        assertEquals(5, classes.size());
        assertEquals(0.0, classes.get(0).minIndex(), 1.0e-9);
        assertEquals(1.0, classes.get(4).maxIndex(), 1.0e-9);
        for (int i = 1; i < classes.size(); i++) {
            assertEquals(classes.get(i - 1).maxIndex(), classes.get(i).minIndex(), 1.0e-9);
        }
        assertEquals("Muy baja", RiskAnalysisEngine.classifySusceptibility(0.1));
        assertEquals("Baja", RiskAnalysisEngine.classifySusceptibility(0.3));
        assertEquals("Media", RiskAnalysisEngine.classifySusceptibility(0.5));
        assertEquals("Alta", RiskAnalysisEngine.classifySusceptibility(0.7));
        assertEquals("Muy alta", RiskAnalysisEngine.classifySusceptibility(0.9));
        assertEquals("Muy alta", RiskAnalysisEngine.classifySusceptibility(1.0));
        assertEquals("Muy baja", RiskAnalysisEngine.classifySusceptibility(-3));
    }

    @Test
    void wildfireWasWithdrawnAndParametersAreValidated() {
        assertThrows(IllegalArgumentException.class,
                () -> RiskAnalysisEngine.Scenario.valueOf("WILDFIRE"));
        assertThrows(IllegalArgumentException.class,
                () -> new RiskAnalysisEngine.RiskParameters(0, 100, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new RiskAnalysisEngine.RiskParameters(30, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new RiskAnalysisEngine.RiskParameters(30, 100, -1));
        assertThrows(IllegalArgumentException.class,
                () -> RiskAnalysisEngine.analyze(plane(), null, 0, 0, "x", (Map<String, Double>) null));
        assertFalse(RiskAnalysisEngine.susceptibilityClasses().isEmpty());
        assertNotNull(RiskAnalysisEngine.RiskParameters.defaults());
    }
}
