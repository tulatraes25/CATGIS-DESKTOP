package ar.com.catgis.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;

public class LayoutNorthArrow implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;
    private Color color = new Color(50, 50, 55);

    public LayoutNorthArrow(String id, double xMm, double yMm, double wMm, double hMm) {
        this.id = id; this.name = id;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public void setName(String n) { name = n; }
    @Override public Rectangle2D.Double getBoundsMm() { return boundsMm; }
    @Override public void setBoundsMm(double x, double y, double w, double h) { boundsMm.setRect(x, y, w, h); }
    @Override public int getZOrder() { return zOrder; }
    @Override public void setZOrder(int z) { this.zOrder = z; }
    @Override public boolean isVisible() { return visible; }
    @Override public void setVisible(boolean v) { this.visible = v; }
    @Override public boolean isLocked() { return locked; }
    @Override public void setLocked(boolean l) { this.locked = l; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean s) { this.selected = s; }

    @Override
    public void render(Graphics2D g2, LayoutRenderContext ctx) {
        int cx = ctx.mmToPxInt(boundsMm.x + boundsMm.width / 2);
        int cy = ctx.mmToPxInt(boundsMm.y + boundsMm.height / 2);
        int r = Math.min(ctx.mmToPxInt(boundsMm.width), ctx.mmToPxInt(boundsMm.height)) / 2 - 2;

        // Filled arrow
        Polygon top = new Polygon();
        top.addPoint(cx, cy - r);
        top.addPoint(cx - r / 3, cy);
        top.addPoint(cx + r / 3, cy);
        g2.setColor(color);
        g2.fillPolygon(top);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawPolygon(top);

        // N label
        g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, (float)(r * 0.6)));
        int nw = g2.getFontMetrics().stringWidth("N");
        g2.drawString("N", cx - nw / 2, cy - r - 2);

        // Bottom half (lighter)
        Polygon bottom = new Polygon();
        bottom.addPoint(cx, cy + r);
        bottom.addPoint(cx - r / 3, cy);
        bottom.addPoint(cx + r / 3, cy);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g2.fillPolygon(bottom);
    }

    @Override public boolean containsMm(double xMm, double yMm) { return boundsMm.contains(xMm, yMm); }
}
