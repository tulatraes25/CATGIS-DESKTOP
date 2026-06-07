package ar.com.catgis.renderer.decorations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Renders feature decorators: arrows on lines, charts on points, vertex markers.
 * Inspired by Kosmo's decorator system but simplified for CATGIS.
 *
 * <h3>Decorator types per geometry:</h3>
 * <ul>
 *   <li><b>Lines:</b> StartArrow, MidArrow, EndArrow, Feathers, VertexMarkers</li>
 *   <li><b>Points:</b> PieChart, BarChart, CircleMarker</li>
 *   <li><b>Polygons:</b> VertexMarkers, (future) LabelCallout</li>
 * </ul>
 */
public final class FeatureDecoratorRenderer {

    private FeatureDecoratorRenderer() {}

    // ─── Line decorators ───────────────────────────────────────────────

    /**
     * Draws an arrowhead at the end of a line segment.
     */
    public static void drawEndArrow(Graphics2D g, double x1, double y1,
                                    double x2, double y2,
                                    double arrowSize, Color color, boolean filled) {
        if (x1 == x2 && y1 == y2) return;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double aSize = Math.max(6, arrowSize);
        double angleDelta = Math.toRadians(25);

        Path2D arrow = new Path2D.Double();
        arrow.moveTo(x2, y2);
        arrow.lineTo(x2 - aSize * Math.cos(angle - angleDelta),
                y2 - aSize * Math.sin(angle - angleDelta));
        if (filled) {
            arrow.lineTo(x2 - aSize * 0.6 * Math.cos(angle),
                    y2 - aSize * 0.6 * Math.sin(angle));
            arrow.lineTo(x2 - aSize * Math.cos(angle + angleDelta),
                    y2 - aSize * Math.sin(angle + angleDelta));
            arrow.closePath();
            g.fill(arrow);
        } else {
            arrow.moveTo(x2, y2);
            arrow.lineTo(x2 - aSize * Math.cos(angle + angleDelta),
                    y2 - aSize * Math.sin(angle + angleDelta));
            g.draw(arrow);
        }
    }

    /**
     * Draws an arrowhead at the start of a line segment.
     */
    public static void drawStartArrow(Graphics2D g, double x1, double y1,
                                      double x2, double y2,
                                      double arrowSize, Color color, boolean filled) {
        drawEndArrow(g, x2, y2, x1, y1, arrowSize, color, filled);
    }

    /**
     * Draws feather/barb decorations along a line.
     */
    public static void drawFeathers(Graphics2D g, double x1, double y1,
                                    double x2, double y2,
                                    int count, double featherSize, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.2f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 1; i <= count; i++) {
            double t = i / (double) (count + 1);
            double px = x1 + (x2 - x1) * t;
            double py = y1 + (y2 - y1) * t;
            double angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI / 2;
            double fx = px + featherSize * Math.cos(angle);
            double fy = py + featherSize * Math.sin(angle);
            g.drawLine((int) px, (int) py, (int) fx, (int) fy);
        }
    }

    // ─── Vertex markers ────────────────────────────────────────────────

    public enum VertexStyle { CIRCLE, SQUARE, DIAMOND, CROSS, X, NONE }

    /**
     * Draws a marker at each vertex of a coordinate list.
     */
    public static void drawVertexMarkers(Graphics2D g, List<Coordinate> coords,
                                         VertexStyle style, int size, Color color) {
        if (coords == null || coords.isEmpty()) return;
        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int s = Math.max(2, size);
        for (Coordinate c : coords) {
            int x = (int) c.x;
            int y = (int) c.y;
            switch (style) {
                case CIRCLE -> g.drawOval(x - s / 2, y - s / 2, s, s);
                case SQUARE -> g.drawRect(x - s / 2, y - s / 2, s, s);
                case DIAMOND -> {
                    int[] xs = {x, x + s / 2, x, x - s / 2};
                    int[] ys = {y - s / 2, y, y + s / 2, y};
                    g.drawPolygon(xs, ys, 4);
                }
                case CROSS -> {
                    g.drawLine(x - s / 2, y - s / 2, x + s / 2, y + s / 2);
                    g.drawLine(x + s / 2, y - s / 2, x - s / 2, y + s / 2);
                }
                case X -> g.drawLine(x - s / 2, y, x + s / 2, y);
                default -> {}
            }
        }
    }

    // ─── Chart markers (pie chart on point features) ───────────────────

    /**
     * Draws a simple pie chart marker at a point.
     * Values are normalized to sum to 1.0 for pie proportions.
     */
    public static void drawPieChartMarker(Graphics2D g, double cx, double cy,
                                          double radius, double[] values,
                                          Color[] colors) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double total = Arrays.stream(values).sum();
        if (total <= 0) return;

        double startAngle = -Math.PI / 2;
        for (int i = 0; i < values.length && i < colors.length; i++) {
            double slice = (values[i] / total) * 2 * Math.PI;
            Color c = colors[i % colors.length];

            // Build pie slice path
            Path2D slicePath = new Path2D.Double();
            slicePath.moveTo(cx, cy);
            int segments = Math.max(3, (int) (slice / 0.1));
            for (int s = 0; s <= segments; s++) {
                double theta = startAngle + slice * s / segments;
                double px = cx + radius * Math.cos(theta);
                double py = cy + radius * Math.sin(theta);
                if (s == 0) slicePath.lineTo(px, py);
                else slicePath.lineTo(px, py);
            }
            slicePath.closePath();

            g.setColor(c);
            g.fill(slicePath);
            g.setColor(c.darker());
            g.setStroke(new BasicStroke(0.8f));
            g.draw(slicePath);

            startAngle += slice;
        }

        // Outline
        g.setColor(new Color(60, 60, 60));
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval((int) (cx - radius), (int) (cy - radius),
                (int) (radius * 2), (int) (radius * 2));
    }

    /**
     * Draws a simple bar chart marker at a point.
     */
    public static void drawBarChartMarker(Graphics2D g, double cx, double cy,
                                          double barWidth, double maxHeight,
                                          double[] values, Color[] colors) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double maxVal = Arrays.stream(values).max().orElse(1);
        if (maxVal <= 0) maxVal = 1;

        double totalWidth = values.length * (barWidth + 2);
        double startX = cx - totalWidth / 2;

        for (int i = 0; i < values.length && i < colors.length; i++) {
            double barH = (values[i] / maxVal) * maxHeight;
            Color c = colors[i % colors.length];
            double bx = startX + i * (barWidth + 2);
            double by = cy - barH;

            g.setColor(c);
            g.fill(new Rectangle2D.Double(bx, by, barWidth, barH));
            g.setColor(c.darker());
            g.setStroke(new BasicStroke(0.8f));
            g.draw(new Rectangle2D.Double(bx, by, barWidth, barH));
        }
    }

    // ─── Line label callout (balloon) ──────────────────────────────────

    /**
     * Draws a callout/balloon at the midpoint of a line.
     */
    public static void drawCallout(Graphics2D g, double x, double y,
                                   String text, Color fillColor, Color textColor) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();

        int pad = 4;
        int bx = (int) x - tw / 2 - pad;
        int by = (int) y - th - pad - 6;
        int bw = tw + pad * 2;
        int bh = th + pad * 2;

        // Balloon
        g.setColor(fillColor);
        g.fillRoundRect(bx, by, bw, bh, 6, 6);
        g.setColor(fillColor.darker());
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(bx, by, bw, bh, 6, 6);

        // Pointer triangle
        int[] ptx = {(int) x - 4, (int) x + 4, (int) x};
        int[] pty = {by + bh, by + bh, by + bh + 6};
        g.fillPolygon(ptx, pty, 3);

        // Text
        g.setColor(textColor);
        g.drawString(text, (int) x - tw / 2, (int) y - th / 2 - 4);
    }

    // ─── Coordinate type ──────────────────────────────────────────────

    /**
     * Simple coordinate for vertex markers (avoid JTS dependency in render context).
     */
    public record Coordinate(double x, double y) {}
}
