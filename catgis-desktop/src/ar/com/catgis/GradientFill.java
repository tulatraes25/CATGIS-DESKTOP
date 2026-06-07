package ar.com.catgis;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Gradient fill configuration for polygon symbology.
 * Supports linear and radial gradients with multiple color stops.
 * <p>
 * Usage:
 * <pre>
 *   GradientFill fill = new GradientFill(GradientFill.Type.LINEAR,
 *       new float[]{0f, 1f},
 *       new Color[]{Color.RED, Color.BLUE},
 *       45.0); // 45 degree angle
 *   layer.setGradientFill(fill);
 * </pre>
 * </p>
 */
public class GradientFill {

    public enum Type { LINEAR, RADIAL }

    private Type type = Type.LINEAR;
    private float[] fractions;
    private Color[] colors;
    private double angle; // degrees, for linear
    private double radiusRatio = 1.0; // for radial
    private float opacity = 1.0f;
    private boolean cyclic = false;

    // Default constructor creates a blue-to-lightblue gradient
    public GradientFill() {
        this(Type.LINEAR,
                new float[]{0f, 1f},
                new Color[]{new Color(41, 128, 185), new Color(174, 214, 241)},
                45.0);
    }

    public GradientFill(Type type, float[] fractions, Color[] colors, double angle) {
        this.type = type;
        this.fractions = fractions;
        this.colors = colors;
        this.angle = angle;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public float[] getFractions() { return fractions; }
    public void setFractions(float[] fractions) { this.fractions = fractions; }

    public Color[] getColors() { return colors; }
    public void setColors(Color[] colors) { this.colors = colors; }

    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }

    public double getRadiusRatio() { return radiusRatio; }
    public void setRadiusRatio(double radiusRatio) { this.radiusRatio = radiusRatio; }

    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = Math.max(0, Math.min(1, opacity)); }

    public boolean isCyclic() { return cyclic; }
    public void setCyclic(boolean cyclic) { this.cyclic = cyclic; }

    // ─── Paint creation ────────────────────────────────────────────────

    /**
     * Creates an AWT Paint object from this gradient configuration,
     * fitted to the given bounding rectangle.
     */
    public Paint createPaint(Rectangle2D bounds) {
        if (bounds == null || fractions == null || colors == null
                || fractions.length == 0 || colors.length == 0) {
            return colors != null && colors.length > 0 ? colors[0] : Color.GRAY;
        }

        float[] clampedFractions = fractions.clone();
        for (int i = 0; i < clampedFractions.length; i++) {
            clampedFractions[i] = Math.max(0, Math.min(1, clampedFractions[i]));
        }

        Color[] clampedColors = colors.clone();
        if (opacity < 1f) {
            for (int i = 0; i < clampedColors.length; i++) {
                int r = clampedColors[i].getRed();
                int g = clampedColors[i].getGreen();
                int b = clampedColors[i].getBlue();
                int a = Math.round(clampedColors[i].getAlpha() * opacity);
                clampedColors[i] = new Color(r, g, b, Math.max(0, Math.min(255, a)));
            }
        }

        MultipleGradientPaint.CycleMethod cycle = cyclic
                ? MultipleGradientPaint.CycleMethod.REPEAT
                : MultipleGradientPaint.CycleMethod.NO_CYCLE;

        try {
            if (type == Type.LINEAR) {
                return createLinearPaint(bounds, clampedFractions, clampedColors, cycle);
            } else {
                return createRadialPaint(bounds, clampedFractions, clampedColors, cycle);
            }
        } catch (Exception e) {
            return colors.length > 0 ? colors[0] : Color.GRAY;
        }
    }

    private Paint createLinearPaint(Rectangle2D bounds,
                                    float[] fractions, Color[] colors,
                                    MultipleGradientPaint.CycleMethod cycle) {
        double cx = bounds.getCenterX();
        double cy = bounds.getCenterY();
        double w = bounds.getWidth() / 2;
        double h = bounds.getHeight() / 2;
        double rad = Math.toRadians(angle);

        double dx = w * Math.cos(rad);
        double dy = h * Math.sin(rad);

        Point2D start = new Point2D.Double(cx - dx, cy - dy);
        Point2D end = new Point2D.Double(cx + dx, cy + dy);

        try {
            return new LinearGradientPaint(start, end, fractions, colors, cycle);
        } catch (Exception e) {
            return new GradientPaint(start, colors[0], end, colors[colors.length - 1], cyclic);
        }
    }

    private Paint createRadialPaint(Rectangle2D bounds,
                                    float[] fractions, Color[] colors,
                                    MultipleGradientPaint.CycleMethod cycle) {
        double cx = bounds.getCenterX();
        double cy = bounds.getCenterY();
        double radius = Math.max(bounds.getWidth(), bounds.getHeight()) / 2 * radiusRatio;

        try {
            return new RadialGradientPaint(
                    new Point2D.Double(cx, cy),
                    (float) Math.max(1, radius),
                    fractions, colors, cycle);
        } catch (Exception e) {
            return colors.length > 0 ? colors[0] : Color.GRAY;
        }
    }

    // ─── Precision colors ──────────────────────────────────────────────

    /** Predifined gradient presets */
    public static GradientFill heatMapGradient() {
        return new GradientFill(Type.LINEAR,
                new float[]{0f, 0.33f, 0.66f, 1f},
                new Color[]{new Color(0, 0, 255), Color.CYAN, Color.YELLOW, Color.RED},
                90.0);
    }

    public static GradientFill terrainGradient() {
        return new GradientFill(Type.LINEAR,
                new float[]{0f, 0.25f, 0.5f, 0.75f, 1f},
                new Color[]{new Color(34, 139, 34), new Color(154, 205, 50),
                        new Color(210, 180, 140), new Color(139, 90, 43),
                        new Color(255, 255, 255)},
                90.0);
    }

    public static GradientFill oceanGradient() {
        return new GradientFill(Type.LINEAR,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(0, 50, 150), new Color(30, 144, 255), new Color(135, 206, 250)},
                90.0);
    }

    public static GradientFill sunriseGradient() {
        return new GradientFill(Type.RADIAL,
                new float[]{0f, 0.6f, 1f},
                new Color[]{Color.YELLOW, Color.ORANGE, new Color(139, 0, 0)},
                0.0);
    }
}
