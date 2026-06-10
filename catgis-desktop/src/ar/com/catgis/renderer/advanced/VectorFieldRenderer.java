package ar.com.catgis.renderer.advanced;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/**
 * Advanced renderer for vector field visualization.
 * Renders arrows showing direction and magnitude at each point.
 */
public final class VectorFieldRenderer {

    private VectorFieldRenderer() {}

    /**
     * Draw a vector arrow at a point.
     * @param g2 Graphics2D context
     * @param x Center X in screen coordinates
     * @param y Center Y in screen coordinates
     * @param angle Angle in radians (0 = right, PI/2 = up)
     * @param magnitude Length of the arrow (in pixels)
     * @param color Arrow color
     */
    public static void drawVectorArrow(Graphics2D g2, int x, int y, double angle, double magnitude, Color color) {
        if (magnitude < 1) return;

        double headSize = Math.min(magnitude * 0.3, 8);
        double endX = x + magnitude * Math.cos(angle);
        double endY = y - magnitude * Math.sin(angle);

        // Shaft
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x, y, (int) endX, (int) endY);

        // Arrowhead
        double a1 = angle + Math.PI * 0.85;
        double a2 = angle - Math.PI * 0.85;
        int hx1 = (int) (endX + headSize * Math.cos(a1));
        int hy1 = (int) (endY - headSize * Math.sin(a1));
        int hx2 = (int) (endX + headSize * Math.cos(a2));
        int hy2 = (int) (endY - headSize * Math.sin(a2));

        Path2D arrow = new Path2D.Double();
        arrow.moveTo(endX, endY);
        arrow.lineTo(hx1, hy1);
        arrow.lineTo(hx2, hy2);
        arrow.closePath();

        g2.setColor(color);
        g2.fill(arrow);
    }

    /**
     * Draw a displacement arrow showing movement from one point to another.
     */
    public static void drawDisplacementArrow(Graphics2D g2, double fromX, double fromY,
                                              double toX, double toY, Color color) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        if (magnitude < 1) return;

        double angle = Math.atan2(-dy, dx);
        drawVectorArrow(g2, (int) fromX, (int) fromY, angle, magnitude, color);
    }

    /**
     * Draw a barb (wind barb) at a point.
     * @param g2 Graphics2D context
     * @param x Center X
     * @param y Center Y
     * @param speed Wind speed (0-100, mapped to barb length)
     * @param direction Direction in degrees (meteorological: 0=N, 90=E)
     * @param color Barb color
     */
    public static void drawWindBarb(Graphics2D g2, int x, int y, double speed,
                                     double direction, Color color) {
        if (speed < 1) return;

        double length = Math.min(speed * 0.5, 30);
        double angle = Math.toRadians(direction - 90);

        double endX = x + length * Math.cos(angle);
        double endY = y - length * Math.sin(angle);

        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x, y, (int) endX, (int) endY);

        // Barb flags
        int flagCount = (int) (speed / 10);
        double flagSize = 4;
        for (int i = 1; i <= Math.min(flagCount, 5); i++) {
            double fx = x + (length * i / (flagCount + 1)) * Math.cos(angle);
            double fy = y - (length * i / (flagCount + 1)) * Math.sin(angle);
            double perpAngle = angle + Math.PI / 2;
            int fx2 = (int) (fx + flagSize * Math.cos(perpAngle));
            int fy2 = (int) (fy - flagSize * Math.sin(perpAngle));
            g2.drawLine((int) fx, (int) fy, fx2, fy2);
        }
    }
}
