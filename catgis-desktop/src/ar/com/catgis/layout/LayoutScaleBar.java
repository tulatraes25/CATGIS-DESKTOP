package ar.com.catgis.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class LayoutScaleBar implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;
    private double metersPerUnit = 1;
    private String unitLabel = "m";
    private int segments = 4;
    private Color color = new Color(50, 50, 55);
    private Font font = new Font("SansSerif", Font.PLAIN, 9);

    public LayoutScaleBar(String id, double xMm, double yMm, double wMm, double hMm) {
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
        int x = ctx.mmToPxInt(boundsMm.x);
        int y = ctx.mmToPxInt(boundsMm.y);
        int w = ctx.mmToPxInt(boundsMm.width);
        int barH = Math.max(ctx.mmToPxInt(2.5), 8);
        double scale = ctx.getDpi() / 72.0;

        // Calculate nice round segment length
        double maxMeters = boundsMm.width / 1000.0 * 5000; // rough estimate
        double segMeters = 1;
        double[] steps = {1, 2, 5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000};
        for (double step : steps) {
            if (step * segments / maxMeters * boundsMm.width <= boundsMm.width * 1.1) segMeters = step;
        }
        double totalMeters = segMeters * segments;
        double totalPx = (totalMeters / maxMeters) * boundsMm.width * ctx.getDpi() / 25.4;
        if (totalPx > w * 1.1) totalPx = w * 0.9;

        Font sFont = font.deriveFont((float)(font.getSize2D() * scale));
        g2.setFont(sFont);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        double segPx = totalPx / segments;
        int barY = y + fm.getHeight() + 4;

        // Draw segments alternating black/white
        for (int i = 0; i < segments; i++) {
            int sx = (int)(x + i * segPx);
            g2.setColor(i % 2 == 0 ? color : Color.WHITE);
            g2.fillRect(sx, barY, (int)segPx, barH);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRect(sx, barY, (int)segPx, barH);
            String label = String.valueOf((int)(i * segMeters));
            if (segMeters >= 1000) label = String.valueOf((int)(i * segMeters / 1000)) + "k";
            int lw = fm.stringWidth(label);
            g2.drawString(label, sx + (int)(segPx - lw) / 2, y + fm.getAscent());
        }
        // Right end label
        String totalLabel = String.valueOf((int)totalMeters);
        if (totalMeters >= 1000) totalLabel = String.valueOf((int)(totalMeters / 1000)) + "k";
        totalLabel += " " + unitLabel;
        g2.drawString(totalLabel, x + (int)totalPx + 4, barY + barH / 2 + fm.getAscent() / 2);
        boundsMm.height = (barY + barH - y + 4) / ctx.getDpi() * 25.4;
    }

    @Override public boolean containsMm(double xMm, double yMm) { return boundsMm.contains(xMm, yMm); }
}
