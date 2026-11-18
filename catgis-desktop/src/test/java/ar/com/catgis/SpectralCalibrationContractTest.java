package ar.com.catgis;

import ar.com.catgis.catmap.AnalysisRaster;
import ar.com.catgis.catmap.TerrainRasterAlgorithms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** T3/#139: spectral indices require declared reflectance calibration and NoData. */
class SpectralCalibrationContractTest {

    private static AnalysisRaster raster(String name, float[] values, String crs) {
        return new AnalysisRaster(name, "reflectance", 2, 2, values,
                TerrainRasterAlgorithms.NO_DATA, 0, 0, 20, 20, crs);
    }

    private static SpectralIndexEngine.BandCalibration scaled() {
        return SpectralIndexEngine.BandCalibration.reflectance(1.0e-4, 0.0, -9999f,
                "Sentinel-2 L2A scale");
    }

    @Test
    void ndviUsesScaledReflectanceOracle() {
        AnalysisRaster nir = raster("NIR", new float[]{8000, 8000, 8000, 8000}, "EPSG:32619");
        AnalysisRaster red = raster("Red", new float[]{2000, 2000, 2000, 2000}, "EPSG:32619");

        AnalysisRaster ndvi = SpectralIndexEngine.computeAnalysis(nir, red, "NDVI", scaled(), scaled());

        assertEquals(0.6, ndvi.valueAt(0, 0), 1.0e-6,
                "(0.8-0.2)/(0.8+0.2) after scale_factor");
        assertEquals("EPSG:32619", ndvi.crsCode());
        assertEquals(0.0, ndvi.minX(), 1.0e-9);
        assertEquals(20.0, ndvi.maxY(), 1.0e-9);
        assertEquals(4, ndvi.stats().validCount());
    }

    @Test
    void uncalibratedBandsAreRejectedBeforeAnyClaim() {
        AnalysisRaster nir = raster("NIR", new float[]{100, 100, 100, 100}, "EPSG:32619");
        AnalysisRaster red = raster("Red", new float[]{50, 50, 50, 50}, "EPSG:32619");

        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> SpectralIndexEngine.computeAnalysis(nir, red, "NDVI",
                        SpectralIndexEngine.BandCalibration.uncalibrated("DN sin metadata"),
                        scaled()));
        assertTrue(failure.getMessage().contains("calibracion"), failure.getMessage());
        assertTrue(failure.getMessage().contains("DN sin metadata"), failure.getMessage());
    }

    @Test
    void fillValueBecomesNoDataInsteadOfZeroIndex() {
        AnalysisRaster nir = raster("NIR",
                new float[]{8000, 8000, -9999, 8000}, "EPSG:32619");
        AnalysisRaster red = raster("Red", new float[]{2000, 2000, 2000, 2000}, "EPSG:32619");

        AnalysisRaster ndvi = SpectralIndexEngine.computeAnalysis(nir, red, "NDVI", scaled(), scaled());

        assertTrue(ndvi.isNoData(0, 1), "fill value must propagate as NoData");
        assertEquals(3, ndvi.stats().validCount());
        assertEquals(1, ndvi.stats().noDataCount());
        assertEquals(0.6, ndvi.valueAt(0, 0), 1.0e-6);
    }

    @Test
    void nonReflectanceValuesBecomeNoData() {
        AnalysisRaster nir = raster("NIR", new float[]{50000, 8000, 8000, 8000}, "EPSG:32619");
        AnalysisRaster red = raster("Red", new float[]{2000, 2000, 2000, 2000}, "EPSG:32619");

        AnalysisRaster ndvi = SpectralIndexEngine.computeAnalysis(nir, red, "NDVI", scaled(), scaled());

        assertTrue(ndvi.isNoData(0, 0), "reflectance > 1 is not a valid professional sample");
        assertEquals(3, ndvi.stats().validCount());
    }

    @Test
    void eviUsesThreeCalibratedBands() {
        AnalysisRaster nir = raster("NIR", new float[]{8000, 8000, 8000, 8000}, "EPSG:32619");
        AnalysisRaster red = raster("Red", new float[]{2000, 2000, 2000, 2000}, "EPSG:32619");
        AnalysisRaster blue = raster("Blue", new float[]{1000, 1000, 1000, 1000}, "EPSG:32619");

        AnalysisRaster evi = SpectralIndexEngine.computeAnalysis(nir, red, blue, "EVI",
                scaled(), scaled(), scaled());

        double expected = 2.5 * (0.8 - 0.2) / (0.8 + 6 * 0.2 - 7.5 * 0.1 + 1);
        assertEquals(expected, evi.valueAt(1, 1), 1.0e-6);
        assertEquals(4, evi.stats().validCount());
    }

    @Test
    void mismatchedGridsFailClosed() {
        AnalysisRaster nir = new AnalysisRaster("NIR", "reflectance", 2, 2,
                new float[]{8000, 8000, 8000, 8000}, TerrainRasterAlgorithms.NO_DATA,
                0, 0, 20, 20, "EPSG:32619");
        AnalysisRaster red = new AnalysisRaster("Red", "reflectance", 2, 2,
                new float[]{2000, 2000, 2000, 2000}, TerrainRasterAlgorithms.NO_DATA,
                0, 0, 20, 20, "EPSG:4326");

        assertThrows(IllegalArgumentException.class,
                () -> SpectralIndexEngine.computeAnalysis(nir, red, "NDVI", scaled(), scaled()));
    }
}
