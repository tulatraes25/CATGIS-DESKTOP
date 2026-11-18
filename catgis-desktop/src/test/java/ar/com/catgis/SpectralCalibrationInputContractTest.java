package ar.com.catgis;

import ar.com.catgis.catmap.AnalysisRaster;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#139: explicit calibrated-input contract and calibrated-vs-raw semantics. */
class SpectralCalibrationInputContractTest {

    @Test
    void ambiguousRawInputsAreRejectedUntilCalibrationIsDeclared() {
        IllegalArgumentException undeclared = assertThrows(IllegalArgumentException.class,
                () -> SpectralCalibrationInput.parse(false,
                        "1", "0", "", "1", "0", "", "manual"));
        assertTrue(undeclared.getMessage().contains("calibradas"), undeclared.getMessage());

        assertThrows(IllegalArgumentException.class, () -> SpectralCalibrationInput.parse(true,
                "", "0", "", "1", "0", "", "manual"));
        assertThrows(IllegalArgumentException.class, () -> SpectralCalibrationInput.parse(true,
                "0", "0", "", "1", "0", "", "manual"));
        assertThrows(IllegalArgumentException.class, () -> SpectralCalibrationInput.parse(true,
                "abc", "0", "", "1", "0", "", "manual"));
        assertThrows(IllegalArgumentException.class, () -> SpectralCalibrationInput.parse(true,
                "1", "NaN", "", "1", "0", "", "manual"));
    }

    @Test
    void declaredCalibrationCarriesScaleOffsetNoDataAndProvenance() {
        var input = SpectralCalibrationInput.parse(true,
                "0.0000275", "-0.2", "0", "0.0001", "0.5", "", "Landsat L2 declarado");
        assertTrue(input.bandA().calibrated());
        assertEquals(0.0000275, input.bandA().scaleFactor(), 0.0);
        assertEquals(-0.2, input.bandA().addOffset(), 0.0);
        assertEquals(0f, input.bandA().noData(), 0f);
        assertEquals(0.0001, input.bandB().scaleFactor(), 0.0);
        assertEquals("Landsat L2 declarado", input.provenance());

        var defaults = SpectralCalibrationInput.parse(true,
                "1", "0", "", "1", "0", "", "");
        assertFalse(defaults.provenance().isBlank());
    }

    @Test
    void additiveOffsetMateriallyChangesTheIndexResult() {
        AnalysisRaster rawA = raster("rawA", 0.3f);
        AnalysisRaster rawB = raster("rawB", 0.1f);
        AnalysisRaster noOffset = SpectralIndexEngine.computeAnalysis(rawA, rawB, "NDVI",
                SpectralIndexEngine.BandCalibration.reflectance(1.0, 0.0, null, "test"),
                SpectralIndexEngine.BandCalibration.reflectance(1.0, 0.0, null, "test"));
        AnalysisRaster withOffset = SpectralIndexEngine.computeAnalysis(rawA, rawB, "NDVI",
                SpectralIndexEngine.BandCalibration.reflectance(1.0, -0.05, null, "test"),
                SpectralIndexEngine.BandCalibration.reflectance(1.0, -0.05, null, "test"));
        double a = noOffset.valueAt(0, 0);
        double b = withOffset.valueAt(0, 0);
        assertTrue(Double.isFinite(a) && Double.isFinite(b));
        assertTrue(Math.abs(b - a) > 0.1,
                "offset must materially change NDVI: raw=" + a + " offset=" + b);
    }

    @Test
    void declaredNoDataPropagatesAsNoDataInTheResult() {
        AnalysisRaster rawA = raster("rawA", -9999f);
        AnalysisRaster rawB = raster("rawB", 0.2f);
        var calibration = SpectralIndexEngine.BandCalibration.reflectance(
                1.0, 0.0, -9999f, "NoData declarado");
        AnalysisRaster result = SpectralIndexEngine.computeAnalysis(rawA, rawB, "NDVI",
                calibration,
                SpectralIndexEngine.BandCalibration.reflectance(1.0, 0.0, null, "test"));
        assertTrue(result.isNoData(0, 0));
        assertEquals(1L, result.stats().noDataCount());
    }

    private static AnalysisRaster raster(String name, float value) {
        return new AnalysisRaster(name, "reflectance", 1, 1, new float[]{value},
                Float.NaN, 0, 0, 1, 1, "EPSG:4326");
    }
}
