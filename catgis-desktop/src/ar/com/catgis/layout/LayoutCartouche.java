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
    private Font titleFont = new Font("SansSerif", Font.BOLD, 9);
    private Font labelFont = new Font("SansSerif", Font.BOLD, 7);
    private Font valueFont = new Font("SansSerif", Font.PLAIN, 7);
    private Color titleColor = new Color(0x1F2937);
    private Color labelColor = new Color(0x4B5563);
    private Color valueColor = new Color(0x1F2937);
    private Color titleBgColor = new Color(45, 55, 72);
    private Color titleTextColor = Color.WHITE;
    private Color bgColor = new Color(255, 255, 255, 230);
    private Color borderColor = new Color(45, 55, 72);
    private float borderWidth = 0.8f;
    private double paddingMm = 1.5;
    private boolean showBorder = true;
    private boolean showTitleBar = true;

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

        // Background
        if (bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(x, y, w, h, 3, 3);
        }

        int cy = y;

        // Title bar
        if (showTitleBar) {
            int titleH = ctx.mmToPxInt(4);
            g2.setColor(titleBgColor);
            g2.fillRoundRect(x, y, w, titleH, 3, 3);
            g2.setColor(titleBgColor);
            g2.fillRect(x, y + titleH - 3, w, 3);
            Font sTitleFont = titleFont.deriveFont((float)(titleFont.getSize2D() * ctx.getDpi() / 72));
            g2.setFont(sTitleFont);
            g2.setColor(titleTextColor);
            FontMetrics tfm = g2.getFontMetrics();
            String title = "Datos Cartograficos";
            int titleY = y + (titleH + tfm.getAscent() - tfm.getDescent()) / 2;
            g2.drawString(title, x + pad, titleY);
            cy = y + titleH + 2;
        }

        // Border
        if (showBorder && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(x, y, w, h, 3, 3);
        }

        // Fields
        Font sLabelFont = labelFont.deriveFont((float)(labelFont.getSize2D() * ctx.getDpi() / 72));
        Font sValueFont = valueFont.deriveFont((float)(valueFont.getSize2D() * ctx.getDpi() / 72));
        FontMetrics lfm = g2.getFontMetrics(sLabelFont);
        int rowH = lfm.getHeight() + 3;
        int labelW = Math.min(w / 3, ctx.mmToPxInt(22));
        int valueX = x + pad + labelW + pad;

        for (Map.Entry<String, String> e : fields.entrySet()) {
            if (cy + rowH > y + h - 2) break;
            g2.setFont(sLabelFont);
            g2.setColor(labelColor);
            g2.drawString(e.getKey() + ":", x + pad, cy + lfm.getAscent());
            g2.setFont(sValueFont);
            g2.setColor(valueColor);
            String val = e.getValue();
            if (val == null || val.isEmpty()) val = "-";
            FontMetrics vfm = g2.getFontMetrics();
            String clipped = val;
            int maxValW = w - pad - valueX + x;
            if (vfm.stringWidth(clipped) > maxValW && maxValW > 0) {
                while (clipped.length() > 1 && vfm.stringWidth(clipped + "...") > maxValW) {
                    clipped = clipped.substring(0, clipped.length() - 1);
                }
                clipped += "...";
            }
            g2.drawString(clipped, valueX, cy + lfm.getAscent());
            cy += rowH;
        }

        boundsMm.height = ((cy - y + pad) / ctx.getDpi()) * 25.4;
    }
}
