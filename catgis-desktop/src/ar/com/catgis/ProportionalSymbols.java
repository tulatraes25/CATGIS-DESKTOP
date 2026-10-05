package ar.com.catgis;

/**
 * Proportional symbol configuration for point layers.
 * Maps a numeric field to symbol size (min-max range).
 */
public class ProportionalSymbols {

    private String fieldName = "";
    private double minValue = 0;
    private double maxValue = 100;
    private int minSize = 4;
    private int maxSize = 40;
    private boolean enabled = false;
    private boolean scaleByArea = false; // true = area scales with value, false = radius

    public String getFieldName() { return fieldName; }
    public void setFieldName(String f) { this.fieldName = f != null ? f.trim() : ""; }

    public double getMinValue() { return minValue; }
    public void setMinValue(double v) { this.minValue = v; }

    public double getMaxValue() { return maxValue; }
    public void setMaxValue(double v) { this.maxValue = v; }

    public int getMinSize() { return minSize; }
    public void setMinSize(int s) { this.minSize = Math.max(2, s); }

    public int getMaxSize() { return maxSize; }
    public void setMaxSize(int s) { this.maxSize = Math.max(minSize + 2, s); }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { this.enabled = e; }

    public boolean isScaleByArea() { return scaleByArea; }
    public void setScaleByArea(boolean s) { this.scaleByArea = s; }

    public boolean isConfigured() {
        return enabled && fieldName != null && !fieldName.isBlank() && maxValue > minValue;
    }

    /**
     * Calculate symbol size for a given value.
     * Returns the interpolated size between minSize and maxSize.
     */
    public int getSizeForValue(double value) {
        if (!isConfigured() || value <= minValue) return minSize;
        if (value >= maxValue) return maxSize;

        double ratio = (value - minValue) / (maxValue - minValue);
        if (scaleByArea) {
            // Size proportional to area (radius = sqrt(area/pi))
            double minArea = Math.PI * (minSize / 2.0) * (minSize / 2.0);
            double maxArea = Math.PI * (maxSize / 2.0) * (maxSize / 2.0);
            double area = minArea + (maxArea - minArea) * ratio;
            return (int) Math.round(Math.sqrt(area / Math.PI) * 2);
        } else {
            // Linear interpolation of radius
            return (int) Math.round(minSize + (maxSize - minSize) * ratio);
        }
    }
}
