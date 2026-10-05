package ar.com.catgis.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class LayoutEllipse implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;
    private Color fillColor = new Color(59, 130, 246, 60);
    private Color borderColor = new Color(59, 130, 246);
    private float borderWidth = 1.5f;

    public LayoutEllipse(String id, double xMm, double yMm, double wMm, double hMm) {
        this.id = id; this.name = id;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
    }

    public Color getFillColor() { return fillColor; }
    public void setFillColor(Color c) { if (c != null) fillColor = c; }
    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color c) { if (c != null) borderColor = c; }
    public float getBorderWidth() { return borderWidth; }
    public void setBorderWidth(float w) { borderWidth = Math.max(0, w); }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public void setName(String n) { name = n; }
    @Override public Rectangle2D.Double getBoundsMm() { return boundsMm; }
    @Override public void setBoundsMm(double x, double y, double w, double h) { boundsMm.setRect(x, y, w, h); }
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
        int x = ctx.mmToPxInt(boundsMm.x);
        int y = ctx.mmToPxInt(boundsMm.y);
        int w = ctx.mmToPxInt(boundsMm.width);
        int h = ctx.mmToPxInt(boundsMm.height);
        if (w < 2 || h < 2) return;

        if (fillColor.getAlpha() > 0) {
            g2.setColor(fillColor);
            g2.fill(new Ellipse2D.Double(x, y, w, h));
        }
        if (borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.draw(new Ellipse2D.Double(x, y, w, h));
        }
    }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        double cx = boundsMm.x + boundsMm.width / 2;
        double cy = boundsMm.y + boundsMm.height / 2;
        double rx = boundsMm.width / 2;
        double ry = boundsMm.height / 2;
        if (rx <= 0 || ry <= 0) return false;
        double dx = (xMm - cx) / rx;
        double dy = (yMm - cy) / ry;
        return dx * dx + dy * dy <= 1.0;
    }
}
