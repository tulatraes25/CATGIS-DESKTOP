package ar.com.catgis.layout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public class RulerRenderer {
    private static final int RULER_SIZE = 18;
    private static final int TICK_MAJOR = 10;
    private static final int TICK_MINOR = 4;
    private static final double MAJOR_MM = 10;
    private static final double MINOR_MM = 1;
    private static final Color RULER_BG = new Color(0xE8EBEF);
    private static final Color RULER_FG = new Color(0x555555);
    private static final Font RULER_FONT = new Font("SansSerif", Font.PLAIN, 8);

    public static int getRulerSize() { return RULER_SIZE; }

    public static void render(Graphics2D g, int offsetX, int offsetY, int viewW, int viewH,
                               double pageWmm, double pageHmm, double dpi, double scale) {
        double pxPerMm = dpi / 25.4 * scale;

        g.setColor(RULER_BG);
        g.fillRect(offsetX + RULER_SIZE, offsetY, viewW - RULER_SIZE, RULER_SIZE);
        g.fillRect(offsetX, offsetY + RULER_SIZE, RULER_SIZE, viewH - RULER_SIZE);
        g.fillRect(offsetX, offsetY, RULER_SIZE, RULER_SIZE);

        g.setColor(RULER_FG);
        g.setFont(RULER_FONT);

        // Top ruler ticks
        for (double mm = 0; mm <= pageWmm; mm += MINOR_MM) {
            int px = offsetX + RULER_SIZE + (int)(mm * pxPerMm);
            if (px > viewW) break;
            boolean isMajor = mm % MAJOR_MM < 0.001;
            int tick = isMajor ? TICK_MAJOR : TICK_MINOR;
            int top = offsetY + RULER_SIZE - tick;
            g.draw(new Line2D.Double(px, top, px, offsetY + RULER_SIZE));
        }

        // Top ruler labels
        for (double mm = 0; mm <= pageWmm; mm += MAJOR_MM) {
            int px = offsetX + RULER_SIZE + (int)(mm * pxPerMm);
            if (px > viewW) break;
            String label = String.valueOf((int)mm);
            int lw = g.getFontMetrics().stringWidth(label);
            g.drawString(label, px - lw/2, offsetY + RULER_SIZE - 2);
        }

        // Left ruler ticks
        for (double mm = 0; mm <= pageHmm; mm += MINOR_MM) {
            int py = offsetY + RULER_SIZE + (int)(mm * pxPerMm);
            if (py > viewH) break;
            boolean isMajor = mm % MAJOR_MM < 0.001;
            int tick = isMajor ? TICK_MAJOR : TICK_MINOR;
            int left = offsetX + RULER_SIZE - tick;
            g.draw(new Line2D.Double(left, py, offsetX + RULER_SIZE, py));
        }

        // Left ruler labels
        for (double mm = 0; mm <= pageHmm; mm += MAJOR_MM) {
            int py = offsetY + RULER_SIZE + (int)(mm * pxPerMm);
            if (py > viewH) break;
            String label = String.valueOf((int)mm);
            AffineTransform old = g.getTransform();
            g.translate(offsetX + RULER_SIZE - 2, py);
            g.rotate(-Math.PI / 2);
            int lw = g.getFontMetrics().stringWidth(label);
            g.drawString(label, -lw/2, 0);
            g.setTransform(old);
        }
    }

    public static GuideLine.Orientation rulerHitTest(int mx, int my, int offsetX, int offsetY, int viewW, int viewH) {
        if (my >= offsetY && my < offsetY + RULER_SIZE && mx >= offsetX + RULER_SIZE) {
            return GuideLine.Orientation.VERTICAL;
        }
        if (mx >= offsetX && mx < offsetX + RULER_SIZE && my >= offsetY + RULER_SIZE) {
            return GuideLine.Orientation.HORIZONTAL;
        }
        return null;
    }
}
