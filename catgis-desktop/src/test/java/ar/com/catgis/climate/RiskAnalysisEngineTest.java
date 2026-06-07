package ar.com.catgis.climate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class RiskAnalysisEngineTest {

    @Test
    public void testOilSpillRisk() {
        int size = 30;
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = 100f + (r - size/2) * 2f; // gentle slope

        Map<String, Double> params = new HashMap<>();
        params.put("volume", 100.0);
        params.put("windDirection", 180.0);
        params.put("windSpeed", 30.0);

        var result = RiskAnalysisEngine.analyze(dem,
            RiskAnalysisEngine.Scenario.OIL_SPILL,
            -38.0, -68.0, "Pozo test", params);

        assertNotNull(result, "Risk result should not be null");
        assertNotNull(result.riskMap(), "Risk map should be generated");
        assertEquals(size, result.width(), "Width should match");
        assertEquals(size, result.height(), "Height should match");
        assertTrue(result.warnings().size() >= 1, "Should have analysis warnings");
    }

    @Test
    public void testFloodRisk() {
        int size = 20;
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = 100f;

        var result = RiskAnalysisEngine.analyze(dem,
            RiskAnalysisEngine.Scenario.FLOOD,
            0, 0, "Test", null);

        assertNotNull(result);
        assertNotNull(result.riskMap());
    }

    @Test
    public void testLandslideRisk() {
        int size = 20;
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = 100f + (float)(Math.sin(r * 0.5) * 50);

        var result = RiskAnalysisEngine.analyze(dem,
            RiskAnalysisEngine.Scenario.LANDSLIDE,
            0, 0, "Test", null);

        assertNotNull(result);
        assertEquals(RiskAnalysisEngine.Scenario.LANDSLIDE.spanish(), result.scenarioName());
    }

    @Test
    public void testNoDEM() {
        var result = RiskAnalysisEngine.analyze(null,
            RiskAnalysisEngine.Scenario.OIL_SPILL,
            0, 0, "No DEM", null);

        assertNotNull(result);
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("No DEM")),
            "Should warn about missing DEM");
    }
}
