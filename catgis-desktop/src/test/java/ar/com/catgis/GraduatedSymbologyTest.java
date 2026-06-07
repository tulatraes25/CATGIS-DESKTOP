package ar.com.catgis;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for graduated symbology classification algorithms:
 * Equal Interval, Quantile, and Natural Breaks (Jenks).
 */
public class GraduatedSymbologyTest {

    private static final List<Double> TEST_VALUES = Arrays.asList(
        1.0, 2.0, 3.0, 4.0, 5.0,
        10.0, 20.0, 30.0, 40.0, 50.0,
        100.0, 200.0, 300.0, 400.0, 500.0
    );

    @Test
    public void testEqualInterval() {
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setFieldName("test");
        sym.setNumClasses(3);
        sym.setMethod(GraduatedSymbology.ClassificationMethod.EQUAL_INTERVAL);
        sym.classify(TEST_VALUES);

        assertEquals(3, sym.getRules().size(), "Should generate 3 classes");
        assertTrue(sym.isConfigured(), "Should be configured after classification");

        GraduatedRangeRule first = sym.getRules().get(0);
        assertEquals(1.0, first.getMinValue(), 0.01);
        assertTrue(first.contains(1.0), "First rule should contain 1.0");

        GraduatedRangeRule last = sym.getRules().get(2);
        assertTrue(last.containsInclusive(500.0), "Last rule should contain max 500");
    }

    @Test
    public void testQuantile() {
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setNumClasses(5);
        sym.setMethod(GraduatedSymbology.ClassificationMethod.QUANTILE);
        sym.classify(TEST_VALUES);

        assertEquals(5, sym.getRules().size(), "Should generate 5 classes");
        GraduatedRangeRule first = sym.getRules().get(0);
        assertEquals(1.0, first.getMinValue(), 0.001);
    }

    @Test
    public void testNaturalBreaks() {
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setNumClasses(3);
        sym.setMethod(GraduatedSymbology.ClassificationMethod.NATURAL_BREAKS);
        sym.classify(TEST_VALUES);

        assertEquals(3, sym.getRules().size(), "Should generate 3 classes");

        // Jenks should detect natural groups
        GraduatedRangeRule first = sym.getRules().get(0);
        assertTrue(first.contains(5.0), "First class should contain 5");

        GraduatedRangeRule last = sym.getRules().get(2);
        assertTrue(last.containsInclusive(500.0), "Last class should contain 500");
    }

    @Test
    public void testGetRuleForValue() {
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setFieldName("test");
        sym.setNumClasses(3);
        sym.setMethod(GraduatedSymbology.ClassificationMethod.EQUAL_INTERVAL);
        sym.classify(TEST_VALUES);

        assertNotNull(sym.getRuleForValue(100.0), "Should find rule for value 100");
        // Last rule's max = 500 (max of data), so values > 500 don't match
        assertNull(sym.getRuleForValue(999.0), "Out-of-range values should not match");
        assertNotNull(sym.getRuleForValue(500.0), "Max value should match last rule");
    }

    @Test
    public void testUniformDistribution() {
        List<Double> uniform = Arrays.asList(42.0, 42.0, 42.0, 42.0, 42.0);
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setNumClasses(5);
        sym.classify(uniform);

        assertEquals(1, sym.getRules().size(), "Uniform data -> 1 class");
        assertEquals(42.0, sym.getRules().get(0).getMinValue(), 0.01);
    }

    @Test
    public void testEmptyData() {
        List<Double> empty = Arrays.asList();
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setNumClasses(3);
        sym.classify(empty);

        assertTrue(sym.getRules().isEmpty());
        assertFalse(sym.isConfigured());
    }

    @Test
    public void testSingleValue() {
        List<Double> single = Arrays.asList(7.0);
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setNumClasses(3);
        sym.classify(single);

        assertEquals(1, sym.getRules().size(), "Single value -> 1 class");
    }

    @Test
    public void testMultiClassWithTwoValues() {
        List<Double> twoValues = Arrays.asList(10.0, 100.0);
        GraduatedSymbology sym = new GraduatedSymbology();
        sym.setFieldName("test");
        sym.setNumClasses(5);
        sym.classify(twoValues);

        // Should produce at most 5 classes (requested) and at least 1
        int n = sym.getRules().size();
        assertTrue(n >= 1 && n <= 5, "Should produce 1-5 classes for 2 values with 5 requested");
        // First rule should contain 10, last should contain 100
        assertTrue(sym.getRules().get(0).containsInclusive(10.0), "First rule should contain 10");
        assertTrue(sym.getRules().get(n-1).containsInclusive(100.0), "Last rule should contain 100");
    }
}
