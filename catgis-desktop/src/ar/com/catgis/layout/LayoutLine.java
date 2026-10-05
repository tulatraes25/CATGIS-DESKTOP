package ar.com.catgis.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class LayoutLine implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;
    private double x1Mm, y1Mm, x2Mm, y2Mm;
    private Color color = new Color(59, 130, 246);
    private float lineWidth = 2f;
    private float[] dashPattern = null; // null = solid

    public LayoutLine(String id, double x1Mm, double y1Mm, double x2Mm, double y2Mm) {
        this.id = id; this.name = id;
        this.x1Mm = x1Mm; this.y1Mm = y1Mm; this.x2Mm = x2Mm; this.y2Mm = y2Mm;
        updateBounds();
    }

    private void updateBounds() {
        double x = Math.min(x1Mm, x2Mm), y = Math.min(y1Mm, y2Mm);
        double w = Math.abs(x2Mm - x1Mm), h = Math.abs(y2Mm - y1Mm);
        this.boundsMm = new Rectangle2D.Double(x, y, Math.max(w, 2), Math.max(h, 2));
    }

    public void setEndpoints(double x1, double y1, double x2, double y2) { x1Mm=x1; y1Mm=y1; x2Mm=x2; y2Mm=y2; updateBounds(); }
    public Color getColor() { return color; }
    public void setColor(Color c) { if (c != null) color = c; }
    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float w) { lineWidth = Math.max(0.5f, w); }
    public void setDashed(boolean dashed) { dashPattern = dashed ? new float[]{6, 4} : null; }
    public boolean isDashed() { return dashPattern != null; }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public void setName(String n) { name = n; }
    @Override public Rectangle2D.Double getBoundsMm() { return boundsMm; }
    @Override public void setBoundsMm(double x, double y, double w, double h) {
        double ow = Math.abs(x2Mm - x1Mm), oh = Math.abs(y2Mm - y1Mm);
        if (ow > 0 && oh > 0) {
            double sx = w / ow, sy = h / oh;
            x1Mm = x + (x1Mm - boundsMm.x) * sx; y1Mm = y + (y1Mm - boundsMm.y) * sy;
            x2Mm = x + (x2Mm - boundsMm.x) * sx; y2Mm = y + (y2Mm - boundsMm.y) * sy;
        }
        updateBounds();
    }
    @Override public int getZOrder() { return zOrder; }
    @Override public void setZOrder(int z) { zOrder = z; }
    @Override public boolean isVisible() { return visible; }
    @Override public void setVisible(boolean v) { visible = v; }
    @Override public boolean isLocked() { return locked; }
    @Override public void setLocked(boolean l) { locked = l; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean s) { selected = s; }

    @Override
    public void render(Graphics2D g2, LayoutRenderContext ctx) {
        int px1 = ctx.mmToPxInt(x1Mm), py1 = ctx.mmToPxInt(y1Mm);
        int px2 = ctx.mmToPxInt(x2Mm), py2 = ctx.mmToPxInt(y2Mm);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dashPattern, 0f));
        g2.draw(new Line2D.Double(px1, py1, px2, py2));
    }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        double d = ptLineDist(x1Mm, y1Mm, x2Mm, y2Mm, xMm, yMm);
        double toleranceMm = Math.max(3, lineWidth * 0.5);
        return d <= toleranceMm && boundsMm.contains(xMm, yMm);
    }

    private static double ptLineDist(double x1, double y1, double x2, double y2, double px, double py) {
        double dx = x2 - x1, dy = y2 - y1;
        double len2 = dx*dx + dy*dy;
        if (len2 == 0) return Math.hypot(px - x1, py - y1);
        double t = ((px - x1) * dx + (py - y1) * dy) / len2;
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(px - (x1 + t*dx), py - (y1 + t*dy));
    }
}
