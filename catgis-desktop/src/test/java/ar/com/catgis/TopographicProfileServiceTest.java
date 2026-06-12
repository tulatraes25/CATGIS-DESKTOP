package ar.com.catgis;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopographicProfileServiceTest {

    @Test
    void hypsometricCurve_emptyInput() {
        List<TopographicProfileService.HypsometricPoint> curve =
                TopographicProfileService.computeHypsometricCurve(new double[0], 10);
        assertTrue(curve.isEmpty());
    }

    @Test
    void hypsometricCurve_flatDem() {
        double[] dem = new double[100];
        for (int i = 0; i < 100; i++) dem[i] = 100.0;
        List<TopographicProfileService.HypsometricPoint> curve =
                TopographicProfileService.computeHypsometricCurve(dem, 10);
        assertTrue(curve.isEmpty()); // min == max, no curve
    }

    @Test
    void hypsometricCurve_slopingDem() {
        // Linear slope: 0..99
        double[] dem = new double[100];
        for (int i = 0; i < 100; i++) dem[i] = i;
        List<TopographicProfileService.HypsometricPoint> curve =
                TopographicProfileService.computeHypsometricCurve(dem, 10);
        assertEquals(10, curve.size());
        // First point (highest elevation) should have small area fraction
        assertTrue(curve.get(0).areaFraction() < 0.3);
        // Last point (lowest elevation) should have 100% area
        assertEquals(1.0, curve.get(9).areaFraction(), 0.01);
    }

    @Test
    void hypsometricCurve_ignoresNaN() {
        double[] dem = new double[4];
        dem[0] = 10; dem[1] = Double.NaN; dem[2] = 20; dem[3] = 30;
        List<TopographicProfileService.HypsometricPoint> curve =
                TopographicProfileService.computeHypsometricCurve(dem, 5);
        assertFalse(curve.isEmpty());
    }

    @Test
    void hypsometricIntegral_flatDistribution() {
        double[] dem = new double[1000];
        for (int i = 0; i < 1000; i++) dem[i] = i;
        List<TopographicProfileService.HypsometricPoint> curve =
                TopographicProfileService.computeHypsometricCurve(dem, 20);
        double hi = TopographicProfileService.hypsometricIntegral(curve);
        // For a uniform distribution, HI ≈ 0.5
        assertEquals(0.5, hi, 0.05);
    }

    @Test
    void hypsometricIntegral_emptyCurve() {
        assertTrue(Double.isNaN(
                TopographicProfileService.hypsometricIntegral(List.of())));
    }
}
