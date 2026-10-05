package ar.com.catgis.layout;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class LayoutGraticule implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;

    private String linkedMapId;
    private double intervalX = 1.0;  // degrees or meters
    private double intervalY = 1.0;
    private boolean isGeographic = true; // true = degrees, false = meters
    private Color lineColor = new Color(37, 99, 235, 80);
    private Color labelColor = new Color(37, 99, 235);
    private float lineWidth = 0.5f;
    private Font labelFont = new Font("SansSerif", Font.BOLD, 8);
    private boolean showLabels = true;
    private int decimalPlaces = 2;

    public LayoutGraticule(String id, double xMm, double yMm, double wMm, double hMm) {
        this.id = id; this.name = id;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
    }

    public void setLinkedMapId(String id) { linkedMapId = id; }
    public String getLinkedMapId() { return linkedMapId; }
    public void setIntervalX(double v) { intervalX = Math.max(0.001, v); }
    public double getIntervalX() { return intervalX; }
    public void setIntervalY(double v) { intervalY = Math.max(0.001, v); }
    public double getIntervalY() { return intervalY; }
    public void setGeographic(boolean b) { isGeographic = b; }
    public boolean isGeographic() { return isGeographic; }
    public void setLineColor(Color c) { if (c != null) lineColor = c; }
    public Color getLineColor() { return lineColor; }
    public void setLineWidth(float w) { lineWidth = Math.max(0, w); }
    public float getLineWidth() { return lineWidth; }
    public void setShowLabels(boolean b) { showLabels = b; }
    public boolean isShowLabels() { return showLabels; }
    public Font getLabelFont() { return labelFont; }
    public void setLabelFont(Font f) { if (f != null) labelFont = f; }

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
    @Override public boolean containsMm(double x, double y) { return boundsMm.contains(x, y); }

    @Override
    public void render(Graphics2D g2, LayoutRenderContext ctx) {
        int x = ctx.mmToPxInt(boundsMm.x), y = ctx.mmToPxInt(boundsMm.y);
        int w = ctx.mmToPxInt(boundsMm.width), h = ctx.mmToPxInt(boundsMm.height);
        if (w < 20 || h < 20) return;

        // Compute nice grid lines within bounds using interval
        // For now, use a fixed grid based on interval ratio
        double pxPerMm = ctx.getDpi() / 25.4;
        int cols = Math.max(1, (int)(boundsMm.width / intervalX));
        int rows = Math.max(1, (int)(boundsMm.height / intervalY));

        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(lineWidth));

        for (int i = 1; i < cols; i++) {
            int gx = x + (w * i) / cols;
            g2.drawLine(gx, y, gx, y + h);
        }
        for (int i = 1; i < rows; i++) {
            int gy = y + (h * i) / rows;
            g2.drawLine(x, gy, x + w, gy);
        }

        if (showLabels) {
            Font sFont = labelFont.deriveFont((float)(labelFont.getSize2D() * ctx.getDpi() / 72));
            g2.setFont(sFont); g2.setColor(labelColor);
            for (int i = 0; i <= cols; i++) {
                int gx = x + (w * i) / Math.max(1, cols);
                double val = i * intervalX;
                String label = isGeographic ? String.format("%." + decimalPlaces + "f\u00B0", val) : String.format("%.0f", val);
                g2.drawString(label, gx + 2, y - 3);
            }
            for (int i = 0; i <= rows; i++) {
                int gy = y + (h * i) / Math.max(1, rows);
                double val = i * intervalY;
                String label = isGeographic ? String.format("%." + decimalPlaces + "f\u00B0", val) : String.format("%.0f", val);
                g2.drawString(label, x - g2.getFontMetrics().stringWidth(label) - 3, gy + 4);
            }
        }
    }
}
