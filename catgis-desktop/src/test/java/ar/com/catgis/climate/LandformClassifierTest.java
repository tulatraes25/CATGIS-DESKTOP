package ar.com.catgis.climate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LandformClassifier geomorphon algorithm.
 * Uses Gaussian peaks/pits/ridges for realistic terrain shapes.
 */
public class LandformClassifierTest {

    private float[][] createGaussianPeak(int size) {
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = 100f + (float)(Math.exp(
                    -Math.pow((r-size/2)/2.5, 2) - Math.pow((c-size/2)/2.5, 2)) * 40);
        return dem;
    }

    private float[][] createGaussianPit(int size) {
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = 100f - (float)(Math.exp(
                    -Math.pow((r-size/2)/2.5, 2) - Math.pow((c-size/2)/2.5, 2)) * 40);
        return dem;
    }

    private float[][] createRidge(int size) {
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = 100f + (float)(Math.exp(-Math.pow((r-size/2)/1.5, 2)) * 40);
        return dem;
    }

    @Test
    public void testPeakDetection() {
        int size = 21;
        float[][] dem = createGaussianPeak(size);
        var result = LandformClassifier.classify(dem, size, size, 1, 0, 0, 6, 0.03);

        var center = result.getLandform(size/2, size/2);
        assertEquals(LandformClassifier.Landform.PEAK, center,
            "Gaussian peak center should be PEAK");
    }

    @Test
    public void testPitDetection() {
        int size = 21;
        float[][] dem = createGaussianPit(size);
        var result = LandformClassifier.classify(dem, size, size, 1, 0, 0, 6, 0.03);

        var center = result.getLandform(size/2, size/2);
        assertEquals(LandformClassifier.Landform.PIT, center,
            "Gaussian pit center should be PIT");
    }

    @Test
    public void testFlatDetection() {
        int size = 10;
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = 100f;

        var result = LandformClassifier.classify(dem, size, size, 1, 0, 0, 3, 0.02);

        int flat = 0;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (result.getLandform(r, c) == LandformClassifier.Landform.FLAT) flat++;
        assertTrue(flat > size * size * 0.7, "Flat DEM should be mostly FLAT");
    }

    @Test
    public void testRidgeDetection() {
        int size = 21;
        float[][] dem = createRidge(size);
        var result = LandformClassifier.classify(dem, size, size, 1, 0, 0, 6, 0.03);

        int ridge = 0;
        for (int c = 5; c < size - 5; c++) {
            var l = result.getLandform(size/2, c);
            if (l == LandformClassifier.Landform.RIDGE || l == LandformClassifier.Landform.PEAK)
                ridge++;
        }
        assertTrue(ridge >= 5, "Ridge crest should have ridge/peak cells (got " + ridge + ")");
    }

    @Test
    public void testNaNData() {
        int size = 5;
        float[][] dem = new float[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                dem[r][c] = Float.NaN;

        var result = LandformClassifier.classify(dem, size, size, 1, 0, 0, 3, 0.02);
        int total = 0;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (result.getLandform(r, c) == LandformClassifier.Landform.FLAT) total++;
        assertEquals(size * size, total, "NaN cells should default to FLAT");
    }

    @Test
    public void testResultMetadata() {
        var dem = createGaussianPeak(10);
        var result = LandformClassifier.classify(dem, 10, 10, 30, 500000, 6000000, 5, 0.03);
        assertEquals(10, result.width());
        assertEquals(10, result.height());
        assertEquals(30.0, result.cellSize(), 0.001);
        assertEquals(500000, result.west(), 0.001);
        assertEquals(6000000, result.north(), 0.001);
        assertEquals(5, result.searchRadius());
    }
}
