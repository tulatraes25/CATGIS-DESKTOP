package ar.com.catgis.layout;

import ar.com.catgis.Layer;
import ar.com.catgis.PointSymbolCatalog;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LayoutLegend implements LayoutElement {

    public static class LegendItem {
        public String label;
        public Color color;
        public String geometryType;
        public boolean included = true;
        public String displayName;
        public String catalogSymbolId;
        public Layer.PointSymbolStyle pointSymbolStyle;
        public Layer.LineSymbolStyle lineSymbolStyle;
        public Layer.PolygonFillStyle polygonFillStyle;
        public Color strokeColor;

        public LegendItem(String label, Color color, String type) {
            this.label = label; this.displayName = label; this.color = color; this.geometryType = type;
        }
    }

    private final String id;
    private String name;
    private Rectangle2D.Double boundsMm;
    private int zOrder;
    private boolean visible = true, locked, selected;
    private boolean autoHeight = true;
    private boolean wrapText = true;
    private boolean showBackground = false;
    private boolean showBorder = false;
    private int cornerRadius = 4;
    private float bgOpacity = 0.85f;
    private float borderThickness = 1f;
    private Color bgColor = new Color(250, 252, 255);
    private Color borderColor = new Color(180, 190, 200);

    private String title = "Leyenda";
    private String subtitle = "";
    private Font titleFont = new Font("SansSerif", Font.BOLD, 12);
    private Font subtitleFont = new Font("SansSerif", Font.PLAIN, 9);
    private Font itemFont = new Font("SansSerif", Font.PLAIN, 10);
    private Color titleColor = new Color(26, 36, 52);
    private Color subtitleColor = new Color(88, 98, 112);
    private Color itemColor = new Color(50, 55, 65);

    private double paddingMm = 2.5;
    private double itemGapMm = 1.5;
    private double symbolTextGapMm = 2.0;
    private double symbolSizeMm = 3.0;
    private int columns = 1;
    private boolean fitTextToFrame = false;
    private double minFontPt = 6;

    private final List<LegendItem> items = new ArrayList<>();

    public LayoutLegend(String id, double xMm, double yMm, double wMm, double hMm) {
        this.id = id; this.name = id;
        this.boundsMm = new Rectangle2D.Double(xMm, yMm, wMm, hMm);
    }

    public void setItems(List<LegendItem> items) { this.items.clear(); if (items != null) this.items.addAll(items); }
    public List<LegendItem> getItems() { return items; }
    public List<LegendItem> getIncludedItems() {
        List<LegendItem> result = new ArrayList<>();
        for (LegendItem item : items) if (item.included) result.add(item);
        return result;
    }

    public void moveItemUp(int index) {
        if (index > 0 && index < items.size()) {
            LegendItem temp = items.get(index);
            items.set(index, items.get(index - 1));
            items.set(index - 1, temp);
        }
    }

    public void moveItemDown(int index) {
        if (index >= 0 && index < items.size() - 1) {
            LegendItem temp = items.get(index);
            items.set(index, items.get(index + 1));
            items.set(index + 1, temp);
        }
    }

    public void setTitle(String t) { this.title = t; }
    public String getTitle() { return title; }
    public void setSubtitle(String s) { this.subtitle = s; }
    public String getSubtitle() { return subtitle; }
    public void setTitleFont(Font f) { this.titleFont = f; }
    public Font getTitleFont() { return titleFont; }
    public void setSubtitleFont(Font f) { this.subtitleFont = f; }
    public Font getSubtitleFont() { return subtitleFont; }
    public void setItemFont(Font f) { this.itemFont = f; }
    public Font getItemFont() { return itemFont; }
    public void setTitleColor(Color c) { this.titleColor = c; }
    public Color getTitleColor() { return titleColor; }
    public void setItemColor(Color c) { this.itemColor = c; }
    public Color getItemColor() { return itemColor; }
    public void setAutoHeight(boolean b) { this.autoHeight = b; }
    public boolean isAutoHeight() { return autoHeight; }
    public void setShowBackground(boolean b) { this.showBackground = b; }
    public boolean isShowBackground() { return showBackground; }
    public void setShowBorder(boolean b) { this.showBorder = b; }
    public boolean isShowBorder() { return showBorder; }
    public void setBgColor(Color c) { this.bgColor = c; }
    public Color getBgColor() { return bgColor; }
    public void setBgOpacity(float o) { this.bgOpacity = Math.max(0, Math.min(1, o)); }
    public float getBgOpacity() { return bgOpacity; }
    public void setBorderColor(Color c) { this.borderColor = c; }
    public Color getBorderColor() { return borderColor; }
    public void setBorderThickness(float t) { this.borderThickness = t; }
    public float getBorderThickness() { return borderThickness; }
    public void setCornerRadius(int r) { this.cornerRadius = r; }
    public double getPaddingMm() { return paddingMm; }
    public void setPaddingMm(double p) { this.paddingMm = p; }
    public double getSymbolSizeMm() { return symbolSizeMm; }
    public void setSymbolSizeMm(double s) { this.symbolSizeMm = s; }
    public int getColumns() { return columns; }
    public void setColumns(int c) { this.columns = Math.max(1, c); }
    public void setFitTextToFrame(boolean b) { this.fitTextToFrame = b; }

    public static boolean isBasemapName(String name) {
        if (name == null) return false;
        String n = name.toLowerCase();
        return n.contains("osm") || n.contains("esri") || n.contains("world") || n.contains("imagery")
                || n.contains("satellite") || n.contains("tiles") || n.contains("base") || n.contains("mapa base");
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
        List<LegendItem> activeItems = getIncludedItems();
        int xPx = ctx.mmToPxInt(boundsMm.x);
        int yPx = ctx.mmToPxInt(boundsMm.y);
        int wPx = ctx.mmToPxInt(boundsMm.width);
        double scale = ctx.getDpi() / 72.0;

        Font sTitleFont = deriveFont(titleFont, scale);
        Font sSubFont = subtitle != null && !subtitle.isEmpty() ? deriveFont(subtitleFont, scale) : null;
        Font sItemFont = deriveFont(itemFont, scale);
        if (fitTextToFrame && activeItems.size() > 0) {
            sItemFont = fitFont(wPx, sItemFont, activeItems, scale, ctx);
        }
        int symPx = (int)(symbolSizeMm / 25.4 * ctx.getDpi());
        int padPx = (int)(paddingMm / 25.4 * ctx.getDpi());
        int symTextGapPx = (int)(symbolTextGapMm / 25.4 * ctx.getDpi());

        int contentH = calcHeight(activeItems, sTitleFont, sSubFont, sItemFont, symPx, padPx, symTextGapPx, wPx);
        int drawH = autoHeight ? contentH : ctx.mmToPxInt(boundsMm.height);
        if (autoHeight) boundsMm.height = (drawH / ctx.getDpi()) * 25.4;

        if (showBackground || showBorder) {
            Graphics2D bg = (Graphics2D) g2.create();
            try {
                bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgOpacity));
                if (showBackground) {
                    bg.setColor(bgColor);
                    if (cornerRadius > 0) bg.fillRoundRect(xPx, yPx, wPx, drawH, cornerRadius * 2, cornerRadius * 2);
                    else bg.fillRect(xPx, yPx, wPx, drawH);
                }
                if (showBorder) {
                    bg.setColor(borderColor);
                    bg.setStroke(new BasicStroke(borderThickness));
                    if (cornerRadius > 0) bg.drawRoundRect(xPx, yPx, wPx, drawH, cornerRadius * 2, cornerRadius * 2);
                    else bg.drawRect(xPx, yPx, wPx, drawH);
                }
            } finally { bg.dispose(); }
        }

        int curY = yPx + padPx;
        g2.setFont(sTitleFont);
        g2.setColor(titleColor);
        FontMetrics tfm = g2.getFontMetrics();
        g2.drawString(title, xPx + padPx, curY + tfm.getAscent());
        curY += tfm.getHeight();

        if (sSubFont != null) {
            curY += 2;
            g2.setFont(sSubFont);
            g2.setColor(subtitleColor);
            FontMetrics sfm = g2.getFontMetrics();
            g2.drawString(subtitle, xPx + padPx, curY + sfm.getAscent());
            curY += sfm.getHeight();
        }
        curY += padPx / 2;

        g2.setFont(sItemFont);
        g2.setColor(itemColor);
        FontMetrics ifm = g2.getFontMetrics();
        int textX = xPx + padPx + symPx + symTextGapPx;
        int textW = wPx - padPx - symPx - symTextGapPx - padPx;

        for (LegendItem item : activeItems) {
            int itemTop = curY;
            int symY = curY;
            renderSymbol(g2, xPx + padPx, symY, symPx, item);
            g2.setColor(itemColor);

            String display = item.displayName != null ? item.displayName : item.label;
            if (wrapText && textW > 20) {
                List<String> lines = wrapLines(display, ifm, textW);
                for (String line : lines) {
                    g2.drawString(line, textX, curY + ifm.getAscent());
                    curY += ifm.getHeight() + 1;
                }
            } else {
                g2.drawString(display, textX, curY + ifm.getAscent());
                curY += ifm.getHeight() + 1;
            }
            if (curY - itemTop < symPx) curY = itemTop + symPx;
            curY += (int)(itemGapMm / 25.4 * ctx.getDpi());
        }
    }

    private Font fitFont(int wPx, Font baseFont, List<LegendItem> items, double scale, LayoutRenderContext ctx) {
        float size = baseFont.getSize2D();
        while (size > minFontPt * scale) {
            Font test = baseFont.deriveFont(size);
            int h = calcHeight(items, null, null, test, (int)(symbolSizeMm/25.4*ctx.getDpi()),
                    (int)(paddingMm/25.4*ctx.getDpi()), (int)(symbolTextGapMm/25.4*ctx.getDpi()), wPx);
            if (h <= ctx.mmToPxInt(boundsMm.height) || !autoHeight && h <= ctx.mmToPxInt(boundsMm.height)) break;
            size -= 0.5f;
        }
        return baseFont.deriveFont(Math.max(size, (float)(minFontPt * scale)));
    }

    private int calcHeight(List<LegendItem> items, Font titleF, Font subF, Font itemF, int symPx, int padPx, int gapPx, int wPx) {
        BufferedImage d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = d.createGraphics();
        int h = padPx;
        if (titleF != null) { g.setFont(titleF); h += g.getFontMetrics().getHeight(); }
        if (subF != null) { g.setFont(subF); h += g.getFontMetrics().getHeight() + 2; }
        h += padPx / 2;
        if (itemF != null) {
            g.setFont(itemF);
            FontMetrics fm = g.getFontMetrics();
            int textW = wPx - padPx - symPx - gapPx - padPx;
            for (LegendItem item : items) {
                String display = item.displayName != null ? item.displayName : item.label;
                int lines = textW > 20 ? Math.max(1, wrapLines(display, fm, textW).size()) : 1;
                h += Math.max(symPx, (fm.getHeight() + 1) * lines + 1) + (int)(itemGapMm / 25.4 * 200);
            }
        }
        h += padPx;
        g.dispose();
        return h;
    }

    private Font deriveFont(Font base, double scale) {
        return base.deriveFont((float)(base.getSize2D() * scale));
    }

    private void renderSymbol(Graphics2D g, int sx, int sy, int size, LegendItem item) {
        Color c = item.color != null ? item.color : Color.GRAY;
        Color stroke = item.strokeColor != null ? item.strokeColor : c.darker();
        String type = item.geometryType != null ? item.geometryType.toUpperCase() : "";
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (type.contains("POINT")) {
            int cx = sx + (size / 2);
            int cy = sy + (size / 2);
            if (item.catalogSymbolId != null && !item.catalogSymbolId.isBlank() && !"circle".equalsIgnoreCase(item.catalogSymbolId)) {
                PointSymbolCatalog.render(g, item.catalogSymbolId, cx, cy, Math.max(10, size), c, stroke, 1.2f);
                return;
            }
            Layer.PointSymbolStyle pointStyle = item.pointSymbolStyle != null ? item.pointSymbolStyle : Layer.PointSymbolStyle.CIRCLE;
            switch (pointStyle) {
                case SQUARE -> {
                    g.fillRect(sx + 1, sy + 1, size - 2, size - 2);
                    g.setColor(stroke);
                    g.drawRect(sx + 1, sy + 1, size - 2, size - 2);
                }
                case DIAMOND -> {
                    java.awt.geom.Path2D diamond = new java.awt.geom.Path2D.Double();
                    diamond.moveTo(cx, sy);
                    diamond.lineTo(sx + size, cy);
                    diamond.lineTo(cx, sy + size);
                    diamond.lineTo(sx, cy);
                    diamond.closePath();
                    g.fill(diamond);
                    g.setColor(stroke);
                    g.draw(diamond);
                }
                case TRIANGLE, TRIANGLE_INVERTED -> {
                    java.awt.geom.Path2D triangle = new java.awt.geom.Path2D.Double();
                    if (pointStyle == Layer.PointSymbolStyle.TRIANGLE_INVERTED) {
                        triangle.moveTo(sx, sy);
                        triangle.lineTo(sx + size, sy);
                        triangle.lineTo(cx, sy + size);
                    } else {
                        triangle.moveTo(cx, sy);
                        triangle.lineTo(sx + size, sy + size);
                        triangle.lineTo(sx, sy + size);
                    }
                    triangle.closePath();
                    g.fill(triangle);
                    g.setColor(stroke);
                    g.draw(triangle);
                }
                case TARGET, RING, DOUBLE_CIRCLE -> {
                    g.setColor(stroke);
                    g.drawOval(sx + 1, sy + 1, size - 2, size - 2);
                    if (pointStyle == Layer.PointSymbolStyle.DOUBLE_CIRCLE) {
                        g.drawOval(sx + 4, sy + 4, size - 8, size - 8);
                    } else if (pointStyle == Layer.PointSymbolStyle.TARGET) {
                        g.drawLine(cx, sy, cx, sy + size);
                        g.drawLine(sx, cy, sx + size, cy);
                        g.setColor(c);
                        g.fillOval(sx + (size / 3), sy + (size / 3), size / 3, size / 3);
                    }
                }
                case CROSS, CROSS_DIAGONAL -> {
                    g.setColor(stroke);
                    if (pointStyle == Layer.PointSymbolStyle.CROSS_DIAGONAL) {
                        g.drawLine(sx + 1, sy + 1, sx + size - 1, sy + size - 1);
                        g.drawLine(sx + size - 1, sy + 1, sx + 1, sy + size - 1);
                    } else {
                        g.drawLine(cx, sy + 1, cx, sy + size - 1);
                        g.drawLine(sx + 1, cy, sx + size - 1, cy);
                    }
                }
                default -> {
                    g.fillOval(sx + 1, sy + 1, size - 2, size - 2);
                    g.setColor(stroke);
                    g.drawOval(sx + 1, sy + 1, size - 2, size - 2);
                }
            }
        } else if (type.contains("LINE")) {
            int ly = sy + size / 2;
            Layer.LineSymbolStyle lineStyle = item.lineSymbolStyle != null ? item.lineSymbolStyle : Layer.LineSymbolStyle.SOLID;
            if (lineStyle == Layer.LineSymbolStyle.DOUBLE_LINE) {
                g.setColor(c);
                g.setStroke(new BasicStroke(1.8f));
                g.drawLine(sx + 1, ly - 2, sx + size - 1, ly - 2);
                g.drawLine(sx + 1, ly + 2, sx + size - 1, ly + 2);
            } else {
                g.setColor(c);
                g.setStroke(buildLineStroke(2.2f, lineStyle));
                g.drawLine(sx + 1, ly, sx + size - 1, ly);
            }
        } else if (type.contains("POLYGON")) {
            Layer.PolygonFillStyle fillStyle = item.polygonFillStyle != null ? item.polygonFillStyle : Layer.PolygonFillStyle.SOLID;
            Paint oldPaint = g.getPaint();
            if (fillStyle != Layer.PolygonFillStyle.OUTLINE_ONLY && fillStyle != Layer.PolygonFillStyle.TRANSPARENT) {
                g.setPaint(buildPolygonPaint(c, stroke, fillStyle));
                g.fillRect(sx + 1, sy + 1, size - 2, size - 2);
                g.setPaint(oldPaint);
            }
            g.setColor(stroke);
            g.drawRect(sx + 1, sy + 1, size - 2, size - 2);
        } else {
            g.fillRect(sx, sy, size, size);
            g.setColor(stroke);
            g.drawRect(sx, sy, size, size);
        }
    }

    private BasicStroke buildLineStroke(float width, Layer.LineSymbolStyle style) {
        if (style == null) {
            return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        return switch (style) {
            case DASHED -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{7f, 5f}, 0f);
            case DOTTED -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{1f, 4f}, 0f);
            case DASH_DOT -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 4f, 1.5f, 4f}, 0f);
            case DASH_DOT_DOT -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 4f, 1.5f, 3f, 1.5f, 4f}, 0f);
            case BOLD -> new BasicStroke(Math.max(width, 3.2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case THIN -> new BasicStroke(Math.max(1.1f, width * 0.7f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case FENCE -> new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{10f, 3f, 2f, 3f}, 0f);
            case PATH_SECONDARY -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{5f, 5f}, 0f);
            case WATERCOURSE -> new BasicStroke(Math.max(width, 2.6f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case DUCT -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{10f, 4f}, 0f);
            case PROFILE, AXIS, EASEMENT, BOUNDARY, BORDERED, PATH_PRIMARY, SOLID -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case DOUBLE_LINE -> new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        };
    }

    private java.awt.Paint buildPolygonPaint(Color fill, Color stroke, Layer.PolygonFillStyle style) {
        if (style == null || style == Layer.PolygonFillStyle.SOLID) {
            return fill;
        }
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 90));
            g.fillRect(0, 0, 10, 10);
            g.setColor(stroke);
            switch (style) {
                case DIAGONAL_HATCH, ENVIRONMENTAL, BUFFER_SOFT -> {
                    g.drawLine(-2, 9, 9, -2);
                    g.drawLine(2, 11, 11, 2);
                }
                case CROSS_HATCH, INFRASTRUCTURE, PARCEL -> {
                    g.drawLine(0, 5, 10, 5);
                    g.drawLine(5, 0, 5, 10);
                }
                case DOTS, VEGETATION -> {
                    g.fillOval(2, 2, 2, 2);
                    g.fillOval(6, 6, 2, 2);
                }
                case HORIZONTAL_LINES, WATER -> {
                    g.drawLine(0, 3, 10, 3);
                    g.drawLine(0, 7, 10, 7);
                }
                case VERTICAL_LINES, RESTRICTION -> {
                    g.drawLine(3, 0, 3, 10);
                    g.drawLine(7, 0, 7, 10);
                }
                case DIAGONAL_REVERSE, SATELLITE_OVERLAY -> {
                    g.drawLine(0, 0, 10, 10);
                    g.drawLine(0, 4, 6, 10);
                }
                case SOFT_SHADOW -> {
                    g.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 140));
                    g.fillRect(0, 0, 10, 10);
                }
                default -> {
                }
            }
        } finally {
            g.dispose();
        }
        return new java.awt.TexturePaint(img, new java.awt.Rectangle(0, 0, 10, 10));
    }

    private List<String> wrapLines(String text, FontMetrics fm, int maxW) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        StringBuilder cur = new StringBuilder();
        for (String w : text.split(" ")) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(test) <= maxW) {
                if (cur.length() > 0) cur.append(" ");
                cur.append(w);
            } else {
                if (cur.length() > 0) lines.add(cur.toString());
                cur = new StringBuilder(w);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }

    @Override
    public boolean containsMm(double xMm, double yMm) {
        return boundsMm.contains(xMm, yMm);
    }
}
