package ar.com.catgis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Graduated (classified) symbology for numeric fields.
 * Divides values into ranges (classes) with associated styles.
 */
public class GraduatedSymbology {

    public enum ClassificationMethod {
        EQUAL_INTERVAL("Intervalos iguales"),
        QUANTILE("Cuantiles"),
        NATURAL_BREAKS("Cortes naturales (Jenks)");

        private final String displayName;
        ClassificationMethod(String name) { this.displayName = name; }
        public String getDisplayName() { return displayName; }
    }

    private String fieldName = "";
    private int numClasses = 5;
    private ClassificationMethod method = ClassificationMethod.EQUAL_INTERVAL;
    private final List<GraduatedRangeRule> rules = new ArrayList<>();
    private String legendTitle = "Clasificacion";
    private String legendSubtitle = "Clasificacion graduada";

    // Color ramp
    private Color rampStartColor = new Color(239, 243, 255);  // light blue
    private Color rampEndColor = new Color(30, 64, 175);      // dark blue

    public String getFieldName() { return fieldName; }
    public void setFieldName(String f) { this.fieldName = f != null ? f.trim() : ""; }

    public int getNumClasses() { return numClasses; }
    public void setNumClasses(int n) { this.numClasses = Math.max(2, Math.min(20, n)); }

    public ClassificationMethod getMethod() { return method; }
    public void setMethod(ClassificationMethod m) { this.method = m != null ? m : ClassificationMethod.EQUAL_INTERVAL; }

    public List<GraduatedRangeRule> getRules() { return rules; }

    public String getLegendTitle() { return legendTitle; }
    public void setLegendTitle(String t) { this.legendTitle = t != null && !t.isBlank() ? t.trim() : "Clasificacion"; }

    public String getLegendSubtitle() { return legendSubtitle; }
    public void setLegendSubtitle(String t) { this.legendSubtitle = t != null && !t.isBlank() ? t.trim() : "Clasificacion graduada"; }

    public Color getRampStartColor() { return rampStartColor; }
    public void setRampStartColor(Color c) { if (c != null) this.rampStartColor = c; }

    public Color getRampEndColor() { return rampEndColor; }
    public void setRampEndColor(Color c) { if (c != null) this.rampEndColor = c; }

    public boolean isConfigured() {
        return fieldName != null && !fieldName.isBlank() && !rules.isEmpty();
    }

    public void clearRules() { rules.clear(); }

    /**
     * Find the matching rule for a numeric value.
     */
    public GraduatedRangeRule getRuleForValue(double value) {
        for (GraduatedRangeRule rule : rules) {
            if (rule.contains(value)) return rule;
        }
        // Check the last rule with inclusive max
        if (!rules.isEmpty()) {
            GraduatedRangeRule last = rules.get(rules.size() - 1);
            if (last.containsInclusive(value)) return last;
        }
        return null;
    }

    /**
     * Classify values and populate rules with auto-generated color ramp.
     */
    public void classify(List<Double> values) {
        if (values == null || values.isEmpty()) return;

        // Sort and compute statistics
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        double minVal = sorted.get(0);
        double maxVal = sorted.get(sorted.size() - 1);
        if (maxVal - minVal < 1e-15) {
            // All same value — single class
            rules.clear();
            GraduatedRangeRule rule = new GraduatedRangeRule(minVal, maxVal, String.valueOf(minVal));
            applyRampColor(rule, 0, 1);
            rules.add(rule);
            return;
        }

        // Compute class breaks
        double[] breaks;
        switch (method) {
            case QUANTILE:
                breaks = quantileBreaks(sorted, numClasses);
                break;
            case NATURAL_BREAKS:
                breaks = jenksBreaks(sorted, numClasses);
                break;
            case EQUAL_INTERVAL:
            default:
                breaks = equalIntervalBreaks(minVal, maxVal, numClasses);
                break;
        }

        // Build rules from breaks
        rules.clear();
        for (int i = 0; i < breaks.length - 1; i++) {
            double lo = breaks[i];
            double hi = breaks[i + 1];
            // Handle floating point rounding: if this is the last break and
            // values are all integers, ensure the last value is included
            if (i == breaks.length - 2) {
                hi = Math.max(hi, maxVal);
            }
            String label = String.format("%.2f - %.2f", lo, hi);
            GraduatedRangeRule rule = new GraduatedRangeRule(lo, hi, label);
            applyRampColor(rule, i, breaks.length - 1);
            rules.add(rule);
        }
    }

    private void applyRampColor(GraduatedRangeRule rule, int index, int total) {
        float ratio = total > 1 ? (float) index / (total - 1) : 0.5f;
        Color c = interpolateColor(rampStartColor, rampEndColor, ratio);
        rule.setPrimaryColor(c);
        // Slightly darker for border
        rule.setSecondaryColor(c.darker());
    }

    // ========== Classification algorithms ==========

    /**
     * Equal Interval: divides range into equal-sized intervals.
     */
    private static double[] equalIntervalBreaks(double min, double max, int classes) {
        double[] breaks = new double[classes + 1];
        double step = (max - min) / classes;
        for (int i = 0; i <= classes; i++) {
            breaks[i] = i < classes ? min + i * step : max;
        }
        return breaks;
    }

    /**
     * Quantile (equal count): each class has roughly the same number of features.
     */
    private static double[] quantileBreaks(List<Double> sorted, int classes) {
        int n = sorted.size();
        double[] breaks = new double[classes + 1];
        breaks[0] = sorted.get(0);
        for (int i = 1; i < classes; i++) {
            int idx = (int) Math.round((double) i * n / classes);
            idx = Math.max(0, Math.min(n - 1, idx));
            breaks[i] = sorted.get(idx);
        }
        breaks[classes] = sorted.get(n - 1);
        return breaks;
    }

    /**
     * Natural Breaks (Jenks): minimizes within-class variance.
     * Implements the Fisher-Jenks algorithm (Ckmeans 1D).
     */
    private static double[] jenksBreaks(List<Double> sorted, int classes) {
        int n = sorted.size();
        int k = Math.min(classes, n);

        if (k <= 1) {
            return new double[]{sorted.get(0), sorted.get(n - 1)};
        }

        // Matrix of class breaks and variance
        double[][] d = new double[n + 1][k + 1]; // error sum of squares
        int[][] b = new int[n + 1][k + 1];       // break positions

        // Initialize
        for (int i = 1; i <= n; i++) {
            d[i][1] = variance(sorted, 0, i);
            b[i][1] = 1;
        }

        // Fill matrices (k=2..K, i=1..n)
        for (int j = 2; j <= k; j++) {
            for (int i = j; i <= n; i++) {
                d[i][j] = Double.MAX_VALUE;
                // Limit search range for efficiency
                int minI = Math.max(1, i - n / (k - j + 1) * 2);
                int maxI = i - 1;
                for (int p = minI; p <= maxI; p++) {
                    double var = d[p][j - 1] + variance(sorted, p, i);
                    if (var < d[i][j]) {
                        d[i][j] = var;
                        b[i][j] = p;
                    }
                }
            }
        }

        // Extract breaks
        double[] breaks = new double[k + 1];
        breaks[0] = sorted.get(0);
        breaks[k] = sorted.get(n - 1);

        int bk = n;
        for (int j = k; j > 1; j--) {
            int bp = b[bk][j];
            breaks[j - 1] = sorted.get(Math.max(0, bp - 1));
            bk = bp;
        }

        return breaks;
    }

    /** Variance of sorted sublist [from..to) */
    private static double variance(List<Double> sorted, int from, int to) {
        if (to - from <= 1) return 0;
        double sum = 0;
        for (int i = from; i < to; i++) sum += sorted.get(i);
        double mean = sum / (to - from);
        double ss = 0;
        for (int i = from; i < to; i++) {
            double d = sorted.get(i) - mean;
            ss += d * d;
        }
        return ss;
    }

    /** Linear interpolation between two colors. */
    private static Color interpolateColor(Color a, Color b, float ratio) {
        float r = a.getRed() / 255f + (b.getRed() / 255f - a.getRed() / 255f) * ratio;
        float g = a.getGreen() / 255f + (b.getGreen() / 255f - a.getGreen() / 255f) * ratio;
        float bl = a.getBlue() / 255f + (b.getBlue() / 255f - a.getBlue() / 255f) * ratio;
        return new Color(
            Math.round(r * 255),
            Math.round(g * 255),
            Math.round(bl * 255)
        );
    }
}
