package ar.com.catgis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Pure factory for creating custom editing cursors.
 * All methods are stateless and static — no MapPanel dependencies.
 */
class EditingCursorFactory {

    static Cursor createBadgeCursor(String symbol, Color ink, Color badgeFill) {
        return createToolCursor(symbol, ink, badgeFill, Color.WHITE);
    }

    static Cursor createToolCursor(String symbol, Color ink, Color badgeFill, Color halo) {
        try {
            int size = 32;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawCursorPointer(g2);

            drawCursorBadge(g2, halo, badgeFill, ink);

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tx = 15 + ((12 - fm.stringWidth(symbol)) / 2);
            int ty = 15 + (((12 - fm.getHeight()) / 2) + fm.getAscent()) - 1;
            g2.drawString(symbol, tx, ty);
            g2.dispose();
            return Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(2, 1), "catgis-" + symbol);
        } catch (Exception ex) {
            return Cursor.getDefaultCursor();
        }
    }

    static Cursor createScissorCursor() {
        try {
            int size = 32;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawCursorPointer(g2);

            drawCursorBadge(g2, new Color(219, 234, 254), new Color(219, 234, 254), new Color(35, 76, 155));
            g2.setColor(new Color(35, 76, 155));
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(16, 16, 4, 4);
            g2.drawOval(21, 20, 4, 4);
            g2.drawLine(18, 18, 25, 14);
            g2.drawLine(23, 22, 26, 25);
            g2.drawLine(20, 18, 22, 21);
            g2.drawLine(23, 17, 20, 22);
            g2.dispose();

            return Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(2, 1), "catgis-cut");
        } catch (Exception ex) {
            return Cursor.getDefaultCursor();
        }
    }

    static Cursor createSelectionCursor(Object geomObj) {
        try {
            int size = 32;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawCursorPointer(g2);
            drawCursorBadge(g2, new Color(219, 234, 254), new Color(219, 234, 254), new Color(30, 64, 175));
            drawSelectionBadge(g2, new Color(30, 64, 175), geomObj);
            g2.dispose();
            return Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(2, 1), "catgis-select-edit");
        } catch (Exception ex) {
            return Cursor.getDefaultCursor();
        }
    }

    static void drawCursorBadge(Graphics2D g2, Color halo, Color badgeFill, Color stroke) {
        g2.setColor(halo);
        g2.fillOval(13, 13, 16, 16);
        g2.setColor(badgeFill);
        g2.fillOval(14, 14, 14, 14);
        g2.setColor(stroke);
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawOval(14, 14, 14, 14);
    }

    static void drawSelectionBadge(Graphics2D g2, Color ink, Object geomObj) {
        g2.setColor(ink);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (geomObj instanceof Point || geomObj instanceof MultiPoint) {
            g2.fillOval(18, 18, 4, 4);
            return;
        }
        if (geomObj instanceof Polygon || geomObj instanceof MultiPolygon) {
            g2.drawRect(17, 17, 6, 6);
            return;
        }
        g2.drawLine(17, 23, 24, 17);
    }

    static void drawCursorPointer(Graphics2D g2) {
        Path2D.Double outline = new Path2D.Double();
        outline.moveTo(2, 2);
        outline.lineTo(2, 22);
        outline.lineTo(7, 17);
        outline.lineTo(10, 26);
        outline.lineTo(13, 24);
        outline.lineTo(10, 16);
        outline.lineTo(16, 16);
        outline.closePath();

        g2.setColor(new Color(255, 255, 255, 245));
        g2.fill(outline);
        g2.setColor(new Color(30, 30, 34));
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(outline);
        g2.drawLine(6, 18, 9, 24);
    }
}
