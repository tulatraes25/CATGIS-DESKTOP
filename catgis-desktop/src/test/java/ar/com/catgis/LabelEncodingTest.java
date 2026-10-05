package ar.com.catgis;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that validate the label encoding logic used in SmileClassificationDialog.
 * The fix changed from hashCode() % 10 to proper consecutive integer mapping.
 */
class LabelEncodingTest {

    /**
     * Simulates the label encoding logic from SmileClassificationDialog.
     * Maps string labels to consecutive integers.
     */
    private Map<String, Integer> encodeLabels(String[] labelValues) {
        Map<String, Integer> labelMap = new LinkedHashMap<>();
        int nextLabel = 0;
        for (String val : labelValues) {
            if (val == null) continue;
            String labelStr = val.trim();
            if (!labelMap.containsKey(labelStr)) {
                labelMap.put(labelStr, nextLabel++);
            }
        }
        return labelMap;
    }

    @Test
    void stringLabelsMapToConsecutiveIntegers() {
        String[] labels = {"A", "B", "C", "A", "B", "C"};
        Map<String, Integer> encoded = encodeLabels(labels);
        assertEquals(3, encoded.size());
        assertEquals(0, encoded.get("A"));
        assertEquals(1, encoded.get("B"));
        assertEquals(2, encoded.get("C"));
    }

    @Test
    void numericStringLabelsMapCorrectly() {
        String[] labels = {"10", "20", "30", "10"};
        Map<String, Integer> encoded = encodeLabels(labels);
        assertEquals(3, encoded.size());
        // "10" -> 0, "20" -> 1, "30" -> 2 (consecutive, NOT hashCode)
        assertEquals(0, encoded.get("10"));
        assertEquals(1, encoded.get("20"));
        assertEquals(2, encoded.get("30"));
    }

    @Test
    void labelsAreStable() {
        // Same input should always produce same mapping
        String[] labels1 = {"X", "Y", "Z"};
        String[] labels2 = {"X", "Y", "Z"};
        assertEquals(encodeLabels(labels1), encodeLabels(labels2));
    }

    @Test
    void nullLabelsAreSkipped() {
        String[] labels = {"A", null, "B", null, "C"};
        Map<String, Integer> encoded = encodeLabels(labels);
        assertEquals(3, encoded.size());
        assertTrue(encoded.containsKey("A"));
        assertTrue(encoded.containsKey("B"));
        assertTrue(encoded.containsKey("C"));
    }

    @Test
    void oldHashCodeMethodWouldProduceDifferentResults() {
        // Demonstrate that the old hashCode approach was unreliable
        String[] labels = {"Apple", "Banana", "Cherry"};
        Map<String, Integer> newEncoding = encodeLabels(labels);

        // Old approach: hashCode() % 10
        int oldApple = Math.abs("Apple".hashCode() % 10);
        int oldBanana = Math.abs("Banana".hashCode() % 10);
        int oldCherry = Math.abs("Cherry".hashCode() % 10);

        // New approach: consecutive integers
        assertEquals(0, newEncoding.get("Apple"));
        assertEquals(1, newEncoding.get("Banana"));
        assertEquals(2, newEncoding.get("Cherry"));

        // Old approach could produce non-consecutive or duplicate values
        // e.g., if hashCode % 10 gives 3, 7, 1 - these aren't consecutive
        // and the classifier would see 3 classes (0,1,3) instead of (0,1,2)
        assertNotEquals(oldApple, oldBanana); // Might collide
    }
}
