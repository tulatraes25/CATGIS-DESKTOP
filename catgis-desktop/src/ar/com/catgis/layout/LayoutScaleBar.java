package ar.com.catgis.layout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

    private double mapScaleDenominator = 10000;

    public double getMapScaleDenominator() { return mapScaleDenominator; }
    public void setMapScaleDenominator(double d) { this.mapScaleDenominator = d > 0 ? d : 10000; }
    public double getMetersPerUnit() { return metersPerUnit; }
    public void setMetersPerUnit(double v) { metersPerUnit = v; }
    public String getUnitLabel() { return unitLabel; }
    public void setUnitLabel(String v) { unitLabel = v; }
    public int getSegments() { return segments; }
    public void setSegments(int v) { segments = Math.max(2, v); }
    public Color getColor() { return color; }
    public void setColor(Color c) { if (c != null) color = c; }
    public Font getFont() { return font; }
    public void setFont(Font f) { if (f != null) font = f; }

    @Override
    public void render(Graphics2D g2, LayoutRenderContext ctx) {
        int x = ctx.mmToPxInt(boundsMm.x);
        int y = ctx.mmToPxInt(boundsMm.y);
        int w = ctx.mmToPxInt(boundsMm.width);
        if (w < 20 || boundsMm.width <= 0 || mapScaleDenominator <= 0) return;
        int barH = Math.max(ctx.mmToPxInt(3.0), 10);

        double maxMeters = (boundsMm.width / 1000.0) * mapScaleDenominator;
        double[] steps = {1, 2, 5, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000};
        double idealSegMeters = maxMeters / segments;
        double segMeters = steps[0];
        for (double step : steps) if (step <= idealSegMeters * 1.2) segMeters = step;
        double totalMeters = segMeters * segments;
        double totalPx = (totalMeters / maxMeters) * w;
        totalPx = Math.max(w * 0.4, Math.min(w * 1.02, totalPx));

        Font sFont = font.deriveFont((float) Math.max(7, Math.min(font.getSize2D(), barH * 0.6)));
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(sFont);
        FontMetrics fm = g2.getFontMetrics();
        double segPx = totalPx / segments;
        int barY = y + fm.getHeight() + 4;

        // Professional alternating segments
        for (int i = 0; i < segments; i++) {
            int sx = (int)(x + i * segPx);
            int sw = (int) Math.ceil((i == segments - 1 ? totalPx - i * segPx : segPx));
            g2.setColor(i % 2 == 0 ? color : Color.WHITE);
            g2.fillRect(sx, barY, sw, barH);
            g2.setColor(new Color(0xCCCED4));
            g2.setStroke(new BasicStroke(0.6f));
            g2.drawRect(sx, barY, sw, barH);
            // Segment label centered below
            String label = formatScaleLabel(i * segMeters);
            int lw = fm.stringWidth(label);
            g2.setColor(color);
            g2.drawString(label, sx + (sw - lw) / 2, y + fm.getAscent());
        }
        // Total label at right end
        String totalLabel = formatScaleLabel(totalMeters);
        g2.setColor(color);
        g2.drawString(totalLabel, x + (int)totalPx + 5, barY + barH/2 + fm.getAscent()/2 - 1);

        // Draw scale ratio below: "1:25,000"
        g2.setFont(sFont.deriveFont((float)(sFont.getSize2D() * 0.85)));
        String ratio = "1:" + String.format("%,.0f", mapScaleDenominator);
        int rw = g2.getFontMetrics().stringWidth(ratio);
        g2.setColor(new Color(0x8B95A5));
        g2.drawString(ratio, x + (w - rw) / 2, barY + barH + fm.getHeight() + 4);
    }

    private String formatScaleLabel(double meters) {
        if (meters >= 1000) return String.valueOf(Math.round(meters / 1000)) + "k m";
        return String.valueOf((int) meters) + " m";
    }

    @Override public boolean containsMm(double xMm, double yMm) { return boundsMm.contains(xMm, yMm); }
}
