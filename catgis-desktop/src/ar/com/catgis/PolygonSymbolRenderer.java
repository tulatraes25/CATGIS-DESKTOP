package ar.com.catgis;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared polygon symbol renderer used by map, properties dialog, categorized symbology, and CATMAP legend.
 */
public final class PolygonSymbolRenderer {

    private PolygonSymbolRenderer() {}

    // Cache for TexturePaint objects to avoid per-polygon allocation
    private static final int PAINT_CACHE_MAX = 64;
    private static final Map<Long, Paint> paintCache = new LinkedHashMap<>(PAINT_CACHE_MAX, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Paint> eldest) {
            return size() > PAINT_CACHE_MAX;
        }
    };

    /** Return a TexturePaint for use with g2.fill(path) in map rendering */
    public static Paint buildPaint(Layer.PolygonFillStyle style, Color fillColor, Color borderColor, int tileSize) {
        long key = paintCacheKey(style, fillColor, borderColor, tileSize);
        Paint cached = paintCache.get(key);
        if (cached != null) return cached;

        BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            paint(g, style, 0, 0, tileSize, tileSize, fillColor, borderColor);
        } finally {
            g.dispose();
        }
        Paint paint = new TexturePaint(img, new java.awt.Rectangle(0, 0, tileSize, tileSize));
        paintCache.put(key, paint);
        return paint;
    }

    private static long paintCacheKey(Layer.PolygonFillStyle style, Color fillColor, Color borderColor, int tileSize) {
        long h = 17;
        h = h * 31 + (style != null ? style.ordinal() : 0);
        h = h * 31 + (fillColor != null ? fillColor.getRGB() : 0);
        h = h * 31 + (borderColor != null ? borderColor.getRGB() : 0);
        h = h * 31 + tileSize;
        return h;
    }

    public static void clearCache() { paintCache.clear(); }

    /** Preview icon for combo boxes */
    public static BufferedImage buildPreview(Layer.PolygonFillStyle style, Color fillColor, Color borderColor, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paint(g, style, 2, 2, width - 4, height - 4, fillColor, borderColor);
        } finally {
            g.dispose();
        }
        return img;
    }

    /** Paint a polygon fill pattern */
    public static void paint(Graphics2D g2, Layer.PolygonFillStyle style, int x, int y, int w, int h, Color fillColor, Color borderColor) {
        Color f = fillColor != null ? fillColor : new Color(100, 150, 200);
        Color b = borderColor != null ? borderColor : Color.BLACK;
        Layer.PolygonFillStyle s = style != null ? style : Layer.PolygonFillStyle.SOLID;

        // Fill base
        if (s == Layer.PolygonFillStyle.OUTLINE_ONLY || s == Layer.PolygonFillStyle.TRANSPARENT) {
            // No fill
        } else if (s == Layer.PolygonFillStyle.SOFT_SHADOW) {
            g2.setColor(new Color(f.getRed(), f.getGreen(), f.getBlue(), 40));
            g2.fillRect(x + 2, y + 2, w - 2, h - 2);
        } else if (s == Layer.PolygonFillStyle.SATELLITE_OVERLAY) {
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRect(x, y, w, h);
        } else {
            g2.setColor(new Color(f.getRed(), f.getGreen(), f.getBlue(), s == Layer.PolygonFillStyle.INFRASTRUCTURE ? 80 : 120));
            g2.fillRect(x, y, w, h);
        }

        // Pattern overlay
        g2.setColor(b);
        g2.setStroke(new BasicStroke(0.8f));
        int size = 10;
        switch (s) {
            case DIAGONAL_HATCH -> { for (int i = -h; i < w + h; i += 4) g2.drawLine(x + i, y, x + i - h, y + h); }
            case DIAGONAL_REVERSE -> { for (int i = -h; i < w + h; i += 4) g2.drawLine(x + i, y + h, x + i + h, y); }
            case CROSS_HATCH -> {
                for (int i = -h; i < w + h; i += 4) g2.drawLine(x + i, y, x + i - h, y + h);
                for (int i = -h; i < w + h; i += 4) g2.drawLine(x + i, y + h, x + i + h, y);
            }
            case DOTS -> { for (int dx = 3; dx < w; dx += 6) for (int dy = 3; dy < h; dy += 6) g2.fillOval(x + dx, y + dy, 2, 2); }
            case HORIZONTAL_LINES -> { for (int dy = 2; dy < h; dy += 4) g2.drawLine(x, y + dy, x + w, y + dy); }
            case VERTICAL_LINES -> { for (int dx = 2; dx < w; dx += 4) g2.drawLine(x + dx, y, x + dx, y + h); }
            case ENVIRONMENTAL -> { g2.setColor(new Color(34, 139, 34, 80)); for (int dy = 2; dy < h; dy += 5) g2.drawLine(x, y + dy, x + w, y + dy); }
            case WATER -> { g2.setColor(new Color(30, 144, 255, 80)); for (int dy = 2; dy < h; dy += 5) g2.drawLine(x, y + dy, x + w, y + dy); }
            case VEGETATION -> { g2.setColor(new Color(60, 179, 113, 80)); for (int dx = 3; dx < w; dx += 8) for (int dy = 3; dy < h; dy += 8) g2.fillOval(x + dx, y + dy, 3, 3); }
            case PARCEL -> { g2.drawLine(x + w/2, y, x + w/2, y + h); g2.drawLine(x, y + h/2, x + w, y + h/2); }
            case RESTRICTION -> { g2.setColor(new Color(255, 69, 0, 100)); for (int i = -h; i < w + h; i += 6) g2.drawLine(x + i, y, x + i - h, y + h); }
            case BUFFER_SOFT -> { g2.setColor(new Color(100, 149, 237, 50)); g2.fillOval(x + w/4, y + h/4, w/2, h/2); }
            default -> {}
        }

        // Border
        if (s != Layer.PolygonFillStyle.OUTLINE_ONLY || true) {
            g2.setColor(b);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x, y, w, h);
        }
    }
}
