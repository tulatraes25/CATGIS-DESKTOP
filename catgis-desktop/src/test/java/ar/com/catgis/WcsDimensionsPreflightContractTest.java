package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Headless WCS chooser preflight with exact integer/overflow contracts. */
class WcsDimensionsPreflightContractTest {

    @Test
    void preservesExplicitValidDimensionsIncludingSpacesAndNotOnlyDefaults() {
        WcsDimensionsPreflight.Checked dims =
                WcsDimensionsPreflight.validate(" 3072 ", "2048");
        assertEquals(3072, dims.width());
        assertEquals(2048, dims.height());
        assertEquals(6_291_456L, dims.pixels());
        assertEquals(BigInteger.valueOf(25_165_824L), dims.roughBytes());
        assertFalse(WcsDimensionsPreflight.requiresConfirmation(dims, 512L * 1024 * 1024));
    }

    @Test
    void rejectsZeroNegativeEmptyNonNumericAndOutOfRangeValues() {
        String[] invalid = {"0", "-1", "", " ", "1.5", "NaN", "1e3",
                "99999999999999999999", "+512"};
        for (String value : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> WcsDimensionsPreflight.validate(value, "512"),
                    "Ancho invalido: " + value);
            assertThrows(IllegalArgumentException.class,
                    () -> WcsDimensionsPreflight.validate("512", value),
                    "Alto invalido: " + value);
        }
        assertThrows(IllegalArgumentException.class,
                () -> WcsDimensionsPreflight.validate(null, "512"));
        assertThrows(IllegalArgumentException.class,
                () -> WcsDimensionsPreflight.validate("512", null));
    }

    @Test
    void extremePositiveIntDimensionsCannotOverflowByteEstimate() {
        WcsDimensionsPreflight.Checked max = WcsDimensionsPreflight.validate(
                Integer.toString(Integer.MAX_VALUE),
                Integer.toString(Integer.MAX_VALUE));
        assertEquals((long) Integer.MAX_VALUE * Integer.MAX_VALUE, max.pixels());
        assertTrue(max.roughBytes().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
        assertTrue(WcsDimensionsPreflight.requiresConfirmation(max, 16L << 30));
        assertTrue(WcsDimensionsPreflight.warning(max, 16L << 30)
                .contains("Deseas continuar"));
    }

    @Test
    void informativeConfirmationIsRelativeToActualConfiguredWireBudget() {
        WcsDimensionsPreflight.Checked dims =
                WcsDimensionsPreflight.validate("10000", "10000");
        assertEquals(400_000_000L, dims.roughBytes().longValueExact());
        assertTrue(WcsDimensionsPreflight.requiresConfirmation(dims, 256L * 1024 * 1024));
        assertFalse(WcsDimensionsPreflight.requiresConfirmation(dims, 512L * 1024 * 1024));
        String warning = WcsDimensionsPreflight.warning(dims, 256L * 1024 * 1024);
        assertTrue(warning.contains("10000 x 10000"));
        assertTrue(warning.contains("banda"));
        assertTrue(warning.contains("256 MiB"));
        assertTrue(warning.contains("compresion"));
        assertTrue(warning.contains("cancelacion"));
    }

    @Test
    void positivePixelsAreNeverSilentlyClampedTo512Square() {
        WcsDimensionsPreflight.Checked small = WcsDimensionsPreflight.validate("1", "1");
        assertEquals(1, small.width());
        assertEquals(1, small.height());
        assertEquals(1, small.pixels());
        assertEquals(BigInteger.valueOf(4), small.roughBytes());

        WcsDimensionsPreflight.Checked large = WcsDimensionsPreflight.validate(
                "65536", "70000");
        assertEquals(65536, large.width());
        assertEquals(70000, large.height());
        assertEquals(4_587_520_000L, large.pixels());
        assertTrue(WcsDimensionsPreflight.requiresConfirmation(large, 512L * 1024 * 1024));
    }

    @Test
    void invalidBudgetMustNotSilentlyDisableWarnings() {
        WcsDimensionsPreflight.Checked dims = WcsDimensionsPreflight.validate("512", "512");
        assertThrows(IllegalArgumentException.class,
                () -> WcsDimensionsPreflight.requiresConfirmation(dims, 0));
        assertThrows(IllegalArgumentException.class,
                () -> WcsDimensionsPreflight.warning(dims, -1));
    }
}
