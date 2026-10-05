package ar.com.catgis.layout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface LayoutElement {

    String getId();
    String getName();
    void setName(String name);
    java.awt.geom.Rectangle2D.Double getBoundsMm();
    void setBoundsMm(double x, double y, double w, double h);
    int getZOrder();
    void setZOrder(int z);
    boolean isVisible();
    void setVisible(boolean v);
    boolean isLocked();
    void setLocked(boolean locked);
    boolean isSelected();
    void setSelected(boolean sel);

    void render(Graphics2D g2, LayoutRenderContext ctx);
    boolean containsMm(double xMm, double yMm);

    // --- Grouping ---

    default String getGroupId() { return null; }
    default void setGroupId(String groupId) {}

    // --- Common appearance properties ---

    default Color getBorderColor() { return new Color(0, 0, 0, 0); }
    default void setBorderColor(Color c) {}
    default float getBorderWidth() { return 0f; }
    default void setBorderWidth(float w) {}
    default Color getBgColor() { return new Color(0, 0, 0, 0); }
    default void setBgColor(Color c) {}
    default Color getShadowColor() { return new Color(0, 0, 0, 0); }
    default void setShadowColor(Color c) {}
    default int getShadowOffsetX() { return 0; }
    default void setShadowOffsetX(int x) {}
    default int getShadowOffsetY() { return 0; }
    default void setShadowOffsetY(int y) {}
    default int getShadowBlur() { return 0; }
    default void setShadowBlur(int b) {}
    default int getCornerRadius() { return 0; }
    default void setCornerRadius(int r) {}

    /** Render common background, border, and shadow for this element.
     *  Call this from render() implementations before drawing content. */
    default void renderBackground(Graphics2D g2, java.awt.geom.Rectangle2D bounds) {
        int x = (int)bounds.getX(), y = (int)bounds.getY();
        int w = (int)bounds.getWidth(), h = (int)bounds.getHeight();
        int radius = getCornerRadius();

        // Shadow
        Color sc = getShadowColor();
        if (sc.getAlpha() > 0) {
            int sx = getShadowOffsetX(), sy = getShadowOffsetY(), blur = getShadowBlur();
            g2.setColor(sc);
            if (radius > 0) {
                g2.fillRoundRect(x + sx, y + sy, w, h, radius, radius);
            } else {
                g2.fillRect(x + sx, y + sy, w, h);
            }
        }

        // Background
        Color bg = getBgColor();
        if (bg.getAlpha() > 0) {
            g2.setColor(bg);
            if (radius > 0) {
                g2.fillRoundRect(x, y, w, h, radius, radius);
            } else {
                g2.fillRect(x, y, w, h);
            }
        }

        // Border
        float bw = getBorderWidth();
        if (bw > 0) {
            Color bc = getBorderColor();
            if (bc.getAlpha() > 0) {
                g2.setColor(bc);
                g2.setStroke(new java.awt.BasicStroke(bw));
                if (radius > 0) {
                    g2.drawRoundRect(x, y, w, h, radius, radius);
                } else {
                    g2.drawRect(x, y, w, h);
                }
            }
        }
    }
}
