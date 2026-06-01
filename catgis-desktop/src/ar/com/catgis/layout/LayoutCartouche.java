package ar.com.catgis.layout;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutCartouche implements LayoutElement {

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;

    private final Map<String, String> fields = new LinkedHashMap<>();
    private Font labelFont = new Font("SansSerif", Font.BOLD, 8);
    private Font valueFont = new Font("SansSerif", Font.PLAIN, 8);
    private Color labelColor = new Color(0x4B5563);
    private Color valueColor = new Color(0x1F2937);
    private Color bgColor = new Color(255, 255, 255, 0);
    private Color borderColor = new Color(0xD1D5DB);
    private float borderWidth = 0.5f;
    private double paddingMm = 1.5;
    private boolean showBorder = true;

    public LayoutCartouche(String id, double xMm, double yMm, double wMm, double hMm) {
        this.id = id; this.name = id;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
        fields.put("Estudio", "");
        fields.put("Proyecto", "");
        fields.put("Empresa", "");
        fields.put("Cartografo", "");
        fields.put("Fuente", "Vista actual del proyecto");
        fields.put("Coord.", "");
    }

    public void setField(String key, String value) { fields.put(key, value != null ? value : ""); }
    public String getField(String key) { return fields.getOrDefault(key, ""); }
    public Map<String, String> getFields() { return fields; }

    public Font getLabelFont() { return labelFont; }
    public void setLabelFont(Font f) { if (f != null) labelFont = f; }
    public Font getValueFont() { return valueFont; }
    public void setValueFont(Font f) { if (f != null) valueFont = f; }
    public Color getBgColor() { return bgColor; }
    public void setBgColor(Color c) { if (c != null) bgColor = c; }
    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color c) { if (c != null) borderColor = c; }
    public float getBorderWidth() { return borderWidth; }
    public void setBorderWidth(float w) { borderWidth = Math.max(0, w); }
    public boolean isShowBorder() { return showBorder; }
    public void setShowBorder(boolean b) { showBorder = b; }
    public double getPaddingMm() { return paddingMm; }
    public void setPaddingMm(double p) { paddingMm = p; }

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
        int pad = ctx.mmToPxInt(paddingMm);

        if (bgColor.getAlpha() > 0) { g2.setColor(bgColor); g2.fillRect(x, y, w, h); }
        if (showBorder && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRect(x, y, w, h);
        }

        Font sLabelFont = labelFont.deriveFont((float)(labelFont.getSize2D() * ctx.getDpi() / 72));
        Font sValueFont = valueFont.deriveFont((float)(valueFont.getSize2D() * ctx.getDpi() / 72));
        int rowH = g2.getFontMetrics(sLabelFont).getHeight() + 2;
        int labelW = w / 3;
        int cy = y + pad + rowH;

        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (cy > y + h - 4) break;
            g2.setFont(sLabelFont); g2.setColor(labelColor);
            String lbl = e.getKey() + ":";
            g2.drawString(lbl, x + pad, cy);
            g2.setFont(sValueFont); g2.setColor(valueColor);
            String val = e.getValue();
            if (val == null || val.isEmpty()) val = "-";
            g2.drawString(val, x + pad + labelW, cy);
            cy += rowH + 2;
        }

        boundsMm.height = ((cy - y) / ctx.getDpi()) * 25.4;
    }
}
