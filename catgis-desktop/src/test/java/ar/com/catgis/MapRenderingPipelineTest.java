package ar.com.catgis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapRenderingPipelineTest {

    @Test
    void scaleSampleRetainsDecimalPrecisionBeforeClamping() {
        assertEquals(64, MapRenderingPipeline.scaleSample(12.5, 10.0, 20.0, true));
        assertEquals(51, MapRenderingPipeline.scaleSample(12.0, 10.0, 20.0, true));
    }
}
