package ar.com.catgis.layout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class LayoutLabel implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true;
    private boolean locked;
    private boolean selected;
    private String text;
    private Font font = new Font("SansSerif", Font.PLAIN, 14);
    private Color color = Color.BLACK;
    private Color bgColor = new Color(0, 0, 0, 0); // transparent by default
    private Color borderColor = new Color(0, 0, 0, 0);
    private float borderWidth = 0f;
    private int paddingPx = 0;
    // Dynamic text: if set, text is evaluated as an expression
    private String dynamicExpression = null;

    public LayoutLabel(String id, String text, double xMm, double yMm, double wMm, double hMm) {
        this.id = id;
        this.name = id;
        this.text = text;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Font getFont() { return font; }
    public void setFont(Font f) { this.font = f; }
    public Color getColor() { return color; }
    public void setColor(Color c) { this.color = c; }
    public Color getBgColor() { return bgColor; }
    public void setBgColor(Color c) { if (c != null) bgColor = c; }
    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color c) { if (c != null) borderColor = c; }
    public float getBorderWidth() { return borderWidth; }
    public void setBorderWidth(float w) { borderWidth = Math.max(0, w); }
    public int getPaddingPx() { return paddingPx; }
    public void setPaddingPx(int p) { paddingPx = Math.max(0, p); }
    public String getDynamicExpression() { return dynamicExpression; }
    public void setDynamicExpression(String expr) { this.dynamicExpression = expr; }

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
    @Override public void setLocked(boolean locked) { this.locked = locked; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean sel) { this.selected = sel; }

    @Override
    public void render(Graphics2D g2, LayoutRenderContext ctx) {
        if (text == null || text.isEmpty()) return;
        int x = ctx.mmToPxInt(boundsMm.x);
        int y = ctx.mmToPxInt(boundsMm.y);
        int w = ctx.mmToPxInt(boundsMm.width);
        int h = ctx.mmToPxInt(boundsMm.height);

        if (bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRect(x, y, w, h);
        }
        if (borderWidth > 0 && borderColor.getAlpha() > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(borderWidth));
            g2.drawRect(x, y, w, h);
        }

        g2.setFont(font);
        g2.setColor(color);
        int tx = x + paddingPx;
        int ty = y + g2.getFontMetrics().getAscent() + paddingPx;
        g2.drawString(text, tx, ty);
    }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        return boundsMm.contains(xMm, yMm);
    }
}
