package ar.com.catgis;

/**
 * A style rule for a single range in graduated symbology.
 * Reuses the same style properties as CategoryStyleRule.
 */
public class GraduatedRangeRule extends CategoryStyleRule {

    private double minValue;
    private double maxValue;
    private String label;

    public GraduatedRangeRule(double minValue, double maxValue, String label) {
        super(label != null ? label : formatLabel(minValue, maxValue));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.label = label != null ? label : formatLabel(minValue, maxValue);
    }

    public double getMinValue() { return minValue; }
    public void setMinValue(double v) { this.minValue = v; }

    public double getMaxValue() { return maxValue; }
    public void setMaxValue(double v) { this.maxValue = v; }

    public String getLabel() { return label; }
    public void setLabel(String l) { this.label = l != null ? l : formatLabel(minValue, maxValue); }

    public boolean contains(double value) {
        // Last range includes max, others are [min, max)
        return value >= minValue && value < maxValue;
    }

    public boolean containsInclusive(double value) {
        return value >= minValue && value <= maxValue;
    }

    private static String formatLabel(double min, double max) {
        return String.format("%.2f - %.2f", min, max);
    }
}
