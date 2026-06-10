package ar.com.catgis.core.model;

import ar.com.catgis.renderer.labels.LabelExpressionEngine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data-Defined Overrides for label and symbology properties.
 * Allows any property to be driven by an attribute value or expression.
 */
public class DataDefinedOverrides {

    private final Map<String, String> overrides = new LinkedHashMap<>();

    public DataDefinedOverrides() {}

    /**
     * Add an override for a property.
     * @param property Property name (e.g., "fontSize", "fontColor", "labelText")
     * @param expression Expression to evaluate (e.g., "[population] > 1000 ? 14 : 10")
     */
    public void addOverride(String property, String expression) {
        if (property != null && expression != null && !property.isBlank() && !expression.isBlank()) {
            overrides.put(property, expression);
        }
    }

    /**
     * Remove an override for a property.
     */
    public void removeOverride(String property) {
        overrides.remove(property);
    }

    /**
     * Check if a property has an override.
     */
    public boolean hasOverride(String property) {
        return overrides.containsKey(property);
    }

    /**
     * Get the expression for a property.
     */
    public String getExpression(String property) {
        return overrides.get(property);
    }

    /**
     * Get all overrides.
     */
    public Map<String, String> getOverrides() {
        return new LinkedHashMap<>(overrides);
    }

    /**
     * Clear all overrides.
     */
    public void clear() {
        overrides.clear();
    }

    /**
     * Evaluate an override for a feature.
     * Returns the evaluated value as a string, or null if no override exists.
     */
    public String evaluateOverride(String property, org.geotools.api.feature.simple.SimpleFeature feature) {
        String expression = overrides.get(property);
        if (expression == null || feature == null) return null;
        try {
            return LabelExpressionEngine.evaluate(expression, feature);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if any overrides exist.
     */
    public boolean isEmpty() {
        return overrides.isEmpty();
    }

    /**
     * Get the number of overrides.
     */
    public int size() {
        return overrides.size();
    }

    /**
     * Serialize to string (pipe-delimited).
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : overrides.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Deserialize from string.
     */
    public static DataDefinedOverrides deserialize(String data) {
        DataDefinedOverrides dd = new DataDefinedOverrides();
        if (data == null || data.isBlank()) return dd;
        for (String pair : data.split(";")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                dd.overrides.put(parts[0], parts[1]);
            }
        }
        return dd;
    }
}
