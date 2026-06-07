package ar.com.catgis;

import java.awt.Color;

/**
 * Configuration for label rendering on a layer.
 * Extracted from Layer to reduce its field count.
 */
public class LabelConfig {

    private boolean visible = false;
    private String field;
    private String expression;
    private String fontFamily = "SansSerif";
    private int fontSize = 10;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private Color color = Color.BLACK;
    private boolean haloEnabled = false;
    private Color haloColor = new Color(255, 255, 255, 200);
    private float haloWidth = 2f;
    private int offsetX = 0;
    private int offsetY = 0;
    private String placement = "AUTO";
    private String placementMode = "AUTO";
    private int priority = 5;
    private boolean collisionAvoid = true;
    private boolean backgroundEnabled = false;
    private Color backgroundColor = new Color(255, 255, 255, 180);
    private double minScale = 0;
    private double maxScale = 0;

    // --- Getters/Setters ---

    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { visible = v; }
    public String getField() { return field; }
    public void setField(String f) { field = f; }
    public String getExpression() { return expression; }
    public void setExpression(String e) { expression = e; }
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String f) { if (f != null) fontFamily = f; }
    public int getFontSize() { return fontSize; }
    public void setFontSize(int s) { fontSize = Math.max(6, s); }
    public boolean isBold() { return bold; }
    public void setBold(boolean b) { bold = b; }
    public boolean isItalic() { return italic; }
    public void setItalic(boolean b) { italic = b; }
    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean b) { underline = b; }
    public Color getColor() { return color; }
    public void setColor(Color c) { if (c != null) color = c; }
    public boolean isHaloEnabled() { return haloEnabled; }
    public void setHaloEnabled(boolean b) { haloEnabled = b; }
    public Color getHaloColor() { return haloColor; }
    public void setHaloColor(Color c) { if (c != null) haloColor = c; }
    public float getHaloWidth() { return haloWidth; }
    public void setHaloWidth(float w) { haloWidth = Math.max(0.5f, w); }
    public int getOffsetX() { return offsetX; }
    public void setOffsetX(int x) { offsetX = x; }
    public int getOffsetY() { return offsetY; }
    public void setOffsetY(int y) { offsetY = y; }
    public String getPlacement() { return placement; }
    public void setPlacement(String p) { if (p != null) placement = p; }
    public String getPlacementMode() { return placementMode; }
    public void setPlacementMode(String m) { if (m != null) placementMode = m; }
    public int getPriority() { return priority; }
    public void setPriority(int p) { priority = Math.max(1, Math.min(10, p)); }
    public boolean isCollisionAvoid() { return collisionAvoid; }
    public void setCollisionAvoid(boolean b) { collisionAvoid = b; }
    public boolean isBackgroundEnabled() { return backgroundEnabled; }
    public void setBackgroundEnabled(boolean b) { backgroundEnabled = b; }
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color c) { if (c != null) backgroundColor = c; }
    public double getMinScale() { return minScale; }
    public void setMinScale(double s) { minScale = s >= 0 ? s : 0; }
    public double getMaxScale() { return maxScale; }
    public void setMaxScale(double s) { maxScale = s >= 0 ? s : 0; }

    public boolean isVisibleAtScale(double scaleDenominator) {
        if (scaleDenominator <= 0) return true;
        if (minScale > 0 && scaleDenominator < minScale) return false;
        if (maxScale > 0 && scaleDenominator > maxScale) return false;
        return true;
    }

    /**
     * Create a copy of this config.
     */
    public LabelConfig copy() {
        LabelConfig copy = new LabelConfig();
        copy.visible = visible;
        copy.field = field;
        copy.expression = expression;
        copy.fontFamily = fontFamily;
        copy.fontSize = fontSize;
        copy.bold = bold;
        copy.italic = italic;
        copy.underline = underline;
        copy.color = new Color(color.getRGB());
        copy.haloEnabled = haloEnabled;
        copy.haloColor = new Color(haloColor.getRGB());
        copy.haloWidth = haloWidth;
        copy.offsetX = offsetX;
        copy.offsetY = offsetY;
        copy.placement = placement;
        copy.placementMode = placementMode;
        copy.priority = priority;
        copy.collisionAvoid = collisionAvoid;
        copy.backgroundEnabled = backgroundEnabled;
        copy.backgroundColor = new Color(backgroundColor.getRGB());
        copy.minScale = minScale;
        copy.maxScale = maxScale;
        return copy;
    }
}
