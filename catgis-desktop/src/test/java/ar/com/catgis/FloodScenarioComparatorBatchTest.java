package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FloodScenarioComparatorBatchTest {

    @Test
    void parsesMultipleRainfallScenariosForBatchComparison() {
        List<Double> values = FloodScenarioService.parseRainfallScenarioList("20, 50, 100");
        assertEquals(List.of(20d, 50d, 100d), values);
    }
}
