package ar.com.catgis.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class GuideLine {
    public enum Orientation { HORIZONTAL, VERTICAL }

    public final String id;
    public double mmPos;
    public final Orientation orientation;
    public boolean locked = false;
    private static final Color GUIDE_COLOR = new Color(0x3388FF);
    private static final float GUIDE_WIDTH = 1.0f;

    public GuideLine(String id, double mmPos, Orientation orientation) {
        this.id = id;
        this.mmPos = mmPos;
        this.orientation = orientation;
    }

    public void render(Graphics2D g, int pageX, int pageY, int drawW, int drawH, double dpi, double scale) {
        g.setColor(GUIDE_COLOR);
        g.setStroke(new BasicStroke(GUIDE_WIDTH));
        double pxPerMm = dpi / 25.4 * scale;
        double px = mmPos * pxPerMm;
        if (orientation == Orientation.VERTICAL) {
            int x = pageX + (int) Math.round(px);
            g.draw(new Line2D.Double(x, pageY, x, pageY + drawH));
        } else {
            int y = pageY + (int) Math.round(px);
            g.draw(new Line2D.Double(pageX, y, pageX + drawW, y));
        }
    }

    public boolean containsPx(double pxX, double pxY, int pageX, int pageY, int drawW, int drawH, double dpi, double scale, double tolerance) {
        if (locked) return false;
        double pxPerMm = dpi / 25.4 * scale;
        double guidePx = mmPos * pxPerMm;
        if (orientation == Orientation.VERTICAL) {
            int gx = pageX + (int) Math.round(guidePx);
            return pxY >= pageY && pxY <= pageY + drawH && Math.abs(pxX - gx) <= tolerance;
        } else {
            int gy = pageY + (int) Math.round(guidePx);
            return pxX >= pageX && pxX <= pageX + drawW && Math.abs(pxY - gy) <= tolerance;
        }
    }
}
