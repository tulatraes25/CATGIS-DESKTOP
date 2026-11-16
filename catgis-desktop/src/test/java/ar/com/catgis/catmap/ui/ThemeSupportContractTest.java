package ar.com.catgis.catmap.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Theme preference resolution and HiDPI scale helpers (headless-safe). */
class ThemeSupportContractTest {

    @Test
    void storedPreferenceWinsOverSystemHint() {
        assertTrue(ThemeSupport.resolveDark(true, false));
        assertFalse(ThemeSupport.resolveDark(false, true));
        assertTrue(ThemeSupport.resolveDark(null, true));
        assertFalse(ThemeSupport.resolveDark(null, false));
    }

    @Test
    void fontScaleIsClampedToUsableRange() {
        assertEquals(1.0, ThemeSupport.fontScaleFor(1.0), 1.0e-9);
        assertEquals(1.5, ThemeSupport.fontScaleFor(1.5), 1.0e-9);
        assertEquals(1.0, ThemeSupport.fontScaleFor(0.5), 1.0e-9);
        assertEquals(1.0, ThemeSupport.fontScaleFor(0.0), 1.0e-9);
        assertEquals(1.0, ThemeSupport.fontScaleFor(Double.NaN), 1.0e-9);
        assertEquals(2.0, ThemeSupport.fontScaleFor(3.0), 1.0e-9);
    }

    @Test
    void descriptionReportsThemeAndScale() {
        String light = ThemeSupport.describe(false, 1.0);
        assertTrue(light.contains("claro"));
        assertTrue(light.contains("HiDPI x1.00"));
        String dark = ThemeSupport.describe(true, 1.5);
        assertTrue(dark.contains("oscuro"));
        assertTrue(dark.contains("HiDPI x1.50"));
    }

    @Test
    void uiScaleIsAFinitePositiveFactor() {
        double scale = ThemeSupport.uiScale();
        assertTrue(Double.isFinite(scale));
        assertTrue(scale >= 1.0 && scale <= 2.0);
    }
}
