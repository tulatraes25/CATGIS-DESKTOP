package ar.com.catgis.layout;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.core.model.Project;

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
    // Dynamic text: if set, text is evaluated as an expression at render time
    private String dynamicExpression = null;
    // Halo (text outline)
    private Color haloColor = new Color(255, 255, 255, 200);
    private float haloWidth = 0f; // 0 = no halo
    private boolean underlined = false;
    private String groupId;

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
    public Color getHaloColor() { return haloColor; }
    public void setHaloColor(Color c) { if (c != null) haloColor = c; }
    public float getHaloWidth() { return haloWidth; }
    public void setHaloWidth(float w) { haloWidth = Math.max(0, w); }
    public boolean isUnderlined() { return underlined; }
    public void setUnderlined(boolean u) { underlined = u; }
    @Override public String getGroupId() { return groupId; }
    @Override public void setGroupId(String gid) { this.groupId = gid; }

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
        String renderText = dynamicExpression != null && !dynamicExpression.isEmpty()
                ? resolveExpression(dynamicExpression, ctx)
                : text;
        if (renderText == null || renderText.isEmpty()) return;
        int x = ctx.mmToPxInt(boundsMm.x);
        int y = ctx.mmToPxInt(boundsMm.y);
        int w = ctx.mmToPxInt(boundsMm.width);
        int h = ctx.mmToPxInt(boundsMm.height);

        // Use the new common background rendering (handles shadow, bg, border, radius)
        renderBackground(g2, new java.awt.Rectangle(x, y, w, h));

        // Keep legacy bgColor/borderColor for backward compatibility
        if (bgColor.getAlpha() > 0 && getBgColor().getAlpha() == 0) {
            g2.setColor(bgColor);
            g2.fillRect(x, y, w, h);
        }
        if (borderWidth > 0 && borderColor.getAlpha() > 0 && getBorderColor().getAlpha() == 0) {
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(borderWidth));
            g2.drawRect(x, y, w, h);
        }

        g2.setFont(font);
        int tx = x + paddingPx;
        int ty = y + g2.getFontMetrics().getAscent() + paddingPx;

        // Halo (text outline)
        if (haloWidth > 0 && haloColor.getAlpha() > 0) {
            g2.setColor(haloColor);
            java.awt.Font haloFont = font.deriveFont(font.getSize2D() + haloWidth * 0.5f);
            g2.setFont(haloFont);
            // Draw text in 4 directions for simple outline effect
            g2.drawString(renderText, tx - (int)haloWidth, ty);
            g2.drawString(renderText, tx + (int)haloWidth, ty);
            g2.drawString(renderText, tx, ty - (int)haloWidth);
            g2.drawString(renderText, tx, ty + (int)haloWidth);
            g2.setFont(font);
        }

        g2.setColor(color);
        g2.drawString(renderText, tx, ty);

        // Underline
        if (underlined) {
            int textW = g2.getFontMetrics().stringWidth(text);
            g2.setStroke(new java.awt.BasicStroke(Math.max(1, font.getSize2D() / 12f)));
            g2.drawLine(tx, ty + 2, tx + textW, ty + 2);
        }
    }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        return boundsMm.contains(xMm, yMm);
    }

    // --- Dynamic expression resolution ---

    /**
     * Resolve a dynamic expression like "Mapa - {date}" into a concrete string.
     * Available tokens:
     *   {date}     — current date (dd/MM/yyyy)
     *   {time}     — current time (HH:mm)
     *   {project}  — current project name (from CatgisDesktopApp.currentProject)
     *   {crs}      — project CRS
     *   {page}     — current page number
     *   {scale}    — approximate scale (requires map frame context)
     */
    public static String resolveExpression(String expr, LayoutRenderContext ctx) {
        if (expr == null || expr.isEmpty()) return expr;
        String result = expr;

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (result.contains("{date}")) {
            result = result.replace("{date}", now.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        if (result.contains("{time}")) {
            result = result.replace("{time}", now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }
        // Try to get project info from static context
        if (result.contains("{project}")) {
            String projName = "";
            try {
                ar.com.catgis.core.model.Project proj = ar.com.catgis.CatgisDesktopApp.currentProject;
                if (proj != null) {
                    String name = proj.getName();
                    if (name != null && !name.isBlank()) projName = name;
                }
            } catch (Exception ignored) {}
            result = result.replace("{project}", projName);
        }
        if (result.contains("{crs}")) {
            String crsText = "";
            try {
                ar.com.catgis.core.model.Project proj = ar.com.catgis.CatgisDesktopApp.currentProject;
                if (proj != null && proj.getProjectCRS() != null) {
                    crsText = proj.getProjectCRS();
                }
            } catch (Exception ignored) {}
            result = result.replace("{crs}", crsText);
        }
        if (result.contains("{page}")) {
            result = result.replace("{page}", "1"); // Single page for now
        }

        return result;
    }
}
