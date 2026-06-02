package ar.com.catgis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public final class PointSymbolRenderer {

    private PointSymbolRenderer() {
    }

    public static BufferedImage buildPreview(Layer.PointSymbolStyle style, Color fill, int canvasSize, int symbolSize) {
        BufferedImage image = new BufferedImage(canvasSize, canvasSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            paint(g2, style, canvasSize / 2, canvasSize / 2, symbolSize, fill, Color.BLACK);
        } finally {
            g2.dispose();
        }
        return image;
    }

    public static void paint(Graphics2D g2, Layer.PointSymbolStyle style, int centerX, int centerY, int size, Color fill, Color stroke) {
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Layer.PointSymbolStyle resolved = style != null ? style : Layer.PointSymbolStyle.CIRCLE;
            Color safeFill = fill != null ? fill : new Color(59, 130, 246);
            Color safeStroke = stroke != null ? stroke : new Color(33, 33, 33);
            int safeSize = Math.max(6, size);
            int half = safeSize / 2;

            copy.setColor(safeFill);
            copy.setStroke(new BasicStroke(Math.max(1.2f, safeSize / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (resolved) {
                case CIRCLE -> {
                    copy.fillOval(centerX - half, centerY - half, safeSize, safeSize);
                    copy.setColor(safeStroke);
                    copy.drawOval(centerX - half, centerY - half, safeSize, safeSize);
                }
                case SQUARE -> {
                    copy.fillRect(centerX - half, centerY - half, safeSize, safeSize);
                    copy.setColor(safeStroke);
                    copy.drawRect(centerX - half, centerY - half, safeSize, safeSize);
                }
                case DIAMOND -> fillAndStroke(copy, diamond(centerX, centerY, half), safeFill, safeStroke);
                case TRIANGLE -> fillAndStroke(copy, triangle(centerX, centerY, half, false), safeFill, safeStroke);
                case TRIANGLE_INVERTED -> fillAndStroke(copy, triangle(centerX, centerY, half, true), safeFill, safeStroke);
                case TARGET -> {
                    copy.fillOval(centerX - half, centerY - half, safeSize, safeSize);
                    copy.setColor(Color.WHITE);
                    int inner = Math.max(4, safeSize / 2);
                    copy.fillOval(centerX - inner / 2, centerY - inner / 2, inner, inner);
                    copy.setColor(safeStroke);
                    copy.drawOval(centerX - half, centerY - half, safeSize, safeSize);
                    copy.drawLine(centerX - half - 2, centerY, centerX + half + 2, centerY);
                    copy.drawLine(centerX, centerY - half - 2, centerX, centerY + half + 2);
                }
                case PIN, LOCATION -> {
                    Path2D pin = new Path2D.Double();
                    pin.moveTo(centerX, centerY + half + 3);
                    pin.lineTo(centerX + half, centerY - half / 2d);
                    pin.quadTo(centerX + half + 1, centerY - half - 2, centerX, centerY - half);
                    pin.quadTo(centerX - half - 1, centerY - half - 2, centerX - half, centerY - half / 2d);
                    pin.closePath();
                    fillAndStroke(copy, pin, safeFill, safeStroke);
                    copy.setColor(new Color(255, 255, 255, 190));
                    int inner = Math.max(4, safeSize / 3);
                    copy.fillOval(centerX - inner / 2, centerY - half / 2, inner, inner);
                }
                case FLAG, ACCESS -> {
                    copy.setColor(safeStroke);
                    copy.drawLine(centerX - half / 2, centerY + half, centerX - half / 2, centerY - half);
                    Path2D flag = new Path2D.Double();
                    flag.moveTo(centerX - half / 2d, centerY - half + 1);
                    flag.lineTo(centerX + half, centerY - half / 3d);
                    flag.lineTo(centerX - half / 2d, centerY);
                    flag.closePath();
                    copy.setColor(safeFill);
                    copy.fill(flag);
                    copy.setColor(safeStroke);
                    copy.draw(flag);
                }
                case STAR -> fillAndStroke(copy, star(centerX, centerY, half, Math.max(2, half / 2), 5), safeFill, safeStroke);
                case STAR_6 -> fillAndStroke(copy, star(centerX, centerY, half, Math.max(2, half / 2), 6), safeFill, safeStroke);
                case WELL -> {
                    Path2D derrick = new Path2D.Double();
                    derrick.moveTo(centerX, centerY - half);
                    derrick.lineTo(centerX + half - 1, centerY + half);
                    derrick.lineTo(centerX - half + 1, centerY + half);
                    derrick.closePath();
                    copy.setColor(safeStroke);
                    copy.draw(derrick);
                    copy.drawLine(centerX - half + 2, centerY + half, centerX + half - 2, centerY + half);
                    copy.drawLine(centerX - half / 2, centerY, centerX + half / 2, centerY);
                    copy.drawLine(centerX - half / 4, centerY - half / 2 + 1, centerX + half / 4, centerY - half / 2 + 1);
                    copy.drawLine(centerX - half / 2, centerY, centerX, centerY + half);
                    copy.drawLine(centerX + half / 2, centerY, centerX, centerY + half);
                }
                case CROSS -> {
                    copy.fillRect(centerX - Math.max(1, half / 4), centerY - half, Math.max(2, safeSize / 4), safeSize);
                    copy.fillRect(centerX - half, centerY - Math.max(1, half / 4), safeSize, Math.max(2, safeSize / 4));
                    copy.setColor(safeStroke);
                    copy.drawRect(centerX - Math.max(1, half / 4), centerY - half, Math.max(2, safeSize / 4), safeSize);
                    copy.drawRect(centerX - half, centerY - Math.max(1, half / 4), safeSize, Math.max(2, safeSize / 4));
                }
                case CROSS_DIAGONAL -> {
                    copy.setColor(safeStroke);
                    copy.drawLine(centerX - half, centerY - half, centerX + half, centerY + half);
                    copy.drawLine(centerX - half, centerY + half, centerX + half, centerY - half);
                }
                case HEXAGON -> fillAndStroke(copy, regularPolygon(centerX, centerY, 6, half), safeFill, safeStroke);
                case PENTAGON -> fillAndStroke(copy, regularPolygon(centerX, centerY, 5, half), safeFill, safeStroke);
                case ARROW_UP -> fillAndStroke(copy, arrow(centerX, centerY, half, true), safeFill, safeStroke);
                case ARROW_DOWN -> fillAndStroke(copy, arrow(centerX, centerY, half, false), safeFill, safeStroke);
                case CAMERA -> {
                    copy.fillRoundRect(centerX - half, centerY - half / 2, safeSize, Math.max(6, safeSize - 4), 5, 5);
                    copy.fillRect(centerX - half / 2, centerY - half, Math.max(4, safeSize / 2), Math.max(3, safeSize / 4));
                    copy.setColor(Color.WHITE);
                    int lens = Math.max(4, safeSize / 3);
                    copy.fillOval(centerX - lens / 2, centerY - lens / 2, lens, lens);
                    copy.setColor(safeStroke);
                    copy.drawRoundRect(centerX - half, centerY - half / 2, safeSize, Math.max(6, safeSize - 4), 5, 5);
                    copy.drawRect(centerX - half / 2, centerY - half, Math.max(4, safeSize / 2), Math.max(3, safeSize / 4));
                }
                case TOWER -> {
                    Path2D tower = new Path2D.Double();
                    tower.moveTo(centerX, centerY - half);
                    tower.lineTo(centerX + half / 2d, centerY + half);
                    tower.lineTo(centerX - half / 2d, centerY + half);
                    tower.closePath();
                    copy.setColor(safeStroke);
                    copy.draw(tower);
                    copy.drawLine(centerX, centerY - half, centerX, centerY + half);
                    copy.drawLine(centerX - half / 4, centerY, centerX + half / 4, centerY);
                    copy.drawLine(centerX - half / 3, centerY + half / 2, centerX + half / 3, centerY + half / 2);
                }
                case RING -> {
                    copy.setColor(safeStroke);
                    copy.setStroke(new BasicStroke(Math.max(2f, safeSize / 5f)));
                    copy.drawOval(centerX - half, centerY - half, safeSize, safeSize);
                }
                case DOUBLE_CIRCLE -> {
                    copy.fillOval(centerX - half, centerY - half, safeSize, safeSize);
                    copy.setColor(safeStroke);
                    copy.drawOval(centerX - half, centerY - half, safeSize, safeSize);
                    int inner = Math.max(4, safeSize - 6);
                    copy.drawOval(centerX - inner / 2, centerY - inner / 2, inner, inner);
                }
                case RECTANGLE_H -> {
                    int w = safeSize + Math.max(4, safeSize / 2);
                    int h = Math.max(6, safeSize - 4);
                    copy.fillRect(centerX - w / 2, centerY - h / 2, w, h);
                    copy.setColor(safeStroke);
                    copy.drawRect(centerX - w / 2, centerY - h / 2, w, h);
                }
                case RECTANGLE_V -> {
                    int w = Math.max(6, safeSize - 4);
                    int h = safeSize + Math.max(4, safeSize / 2);
                    copy.fillRect(centerX - w / 2, centerY - h / 2, w, h);
                    copy.setColor(safeStroke);
                    copy.drawRect(centerX - w / 2, centerY - h / 2, w, h);
                }
                case ALERT -> {
                    fillAndStroke(copy, triangle(centerX, centerY, half, false), new Color(245, 158, 11), safeStroke);
                    copy.setColor(Color.WHITE);
                    copy.setStroke(new BasicStroke(Math.max(1.4f, safeSize / 8f)));
                    copy.drawLine(centerX, centerY - half / 2, centerX, centerY + half / 4);
                    copy.fillOval(centerX - 1, centerY + half / 2 - 1, 3, 3);
                }
                case SAMPLING -> {
                    fillAndStroke(copy, droplet(centerX, centerY, half), new Color(34, 197, 94), safeStroke);
                    copy.setColor(Color.WHITE);
                    copy.fillOval(centerX - 2, centerY - 1, 4, 4);
                }
                case CONTROL -> {
                    copy.fillOval(centerX - half, centerY - half, safeSize, safeSize);
                    copy.setColor(Color.WHITE);
                    copy.drawLine(centerX - half, centerY, centerX + half, centerY);
                    copy.drawLine(centerX, centerY - half, centerX, centerY + half);
                    copy.setColor(safeStroke);
                    copy.drawOval(centerX - half, centerY - half, safeSize, safeSize);
                }
            }
        } finally {
            copy.dispose();
        }
    }

    private static void fillAndStroke(Graphics2D g2, Path2D path, Color fill, Color stroke) {
        g2.setColor(fill);
        g2.fill(path);
        g2.setColor(stroke);
        g2.draw(path);
    }

    private static Path2D triangle(double centerX, double centerY, double radius, boolean inverted) {
        Path2D path = new Path2D.Double();
        if (!inverted) {
            path.moveTo(centerX, centerY - radius);
            path.lineTo(centerX + radius, centerY + radius);
            path.lineTo(centerX - radius, centerY + radius);
        } else {
            path.moveTo(centerX, centerY + radius);
            path.lineTo(centerX + radius, centerY - radius);
            path.lineTo(centerX - radius, centerY - radius);
        }
        path.closePath();
        return path;
    }

    private static Path2D diamond(double centerX, double centerY, double radius) {
        Path2D path = new Path2D.Double();
        path.moveTo(centerX, centerY - radius);
        path.lineTo(centerX + radius, centerY);
        path.lineTo(centerX, centerY + radius);
        path.lineTo(centerX - radius, centerY);
        path.closePath();
        return path;
    }

    private static Path2D regularPolygon(double centerX, double centerY, int sides, double radius) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < sides; i++) {
            double angle = Math.toRadians(-90 + (360d / sides) * i);
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    private static Path2D star(double centerX, double centerY, double outerRadius, double innerRadius, int points) {
        Path2D path = new Path2D.Double();
        int vertices = points * 2;
        for (int i = 0; i < vertices; i++) {
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            double angle = Math.toRadians(-90 + (360d / vertices) * i);
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + Math.sin(angle) * radius;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    private static Path2D arrow(double centerX, double centerY, double radius, boolean up) {
        Path2D path = new Path2D.Double();
        if (up) {
            path.moveTo(centerX, centerY - radius);
            path.lineTo(centerX + radius, centerY);
            path.lineTo(centerX + radius / 3d, centerY);
            path.lineTo(centerX + radius / 3d, centerY + radius);
            path.lineTo(centerX - radius / 3d, centerY + radius);
            path.lineTo(centerX - radius / 3d, centerY);
            path.lineTo(centerX - radius, centerY);
        } else {
            path.moveTo(centerX, centerY + radius);
            path.lineTo(centerX + radius, centerY);
            path.lineTo(centerX + radius / 3d, centerY);
            path.lineTo(centerX + radius / 3d, centerY - radius);
            path.lineTo(centerX - radius / 3d, centerY - radius);
            path.lineTo(centerX - radius / 3d, centerY);
            path.lineTo(centerX - radius, centerY);
        }
        path.closePath();
        return path;
    }

    private static Path2D droplet(double centerX, double centerY, double radius) {
        Path2D path = new Path2D.Double();
        path.moveTo(centerX, centerY + radius);
        path.curveTo(centerX + radius * 0.8, centerY + radius * 0.2, centerX + radius, centerY - radius * 0.5, centerX, centerY - radius);
        path.curveTo(centerX - radius, centerY - radius * 0.5, centerX - radius * 0.8, centerY + radius * 0.2, centerX, centerY + radius);
        path.closePath();
        return path;
    }
}
