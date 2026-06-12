package ar.com.catgis.renderer;
import ar.com.catgis.core.model.Layer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared line symbol renderer used by map, properties dialog, categorized symbology, and CATMAP legend.
 */
public final class LineSymbolRenderer {

    private LineSymbolRenderer() {}

    // Cache for BasicStroke objects to avoid per-feature allocation
    private static final int STROKE_CACHE_MAX = 128;
    private static final Map<Long, BasicStroke> strokeCache = java.util.Collections.synchronizedMap(
            new LinkedHashMap<>(STROKE_CACHE_MAX, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, BasicStroke> eldest) {
                    return size() > STROKE_CACHE_MAX;
                }
            });

    /** Preview icon for combo boxes */
    public static BufferedImage buildPreview(Layer.LineSymbolStyle style, Color color, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color != null ? color : Color.BLACK);
            g.setStroke(buildStroke(style, 2f));
            g.drawLine(4, height / 2, width - 4, height / 2);
            if (style == Layer.LineSymbolStyle.DOUBLE_LINE) {
                g.drawLine(4, height / 2 + 4, width - 4, height / 2 + 4);
            }
        } finally {
            g.dispose();
        }
        return img;
    }

    /** Draw a line segment using the given style. Used by legend and map. */
    public static void paint(Graphics2D g2, Layer.LineSymbolStyle style, int x1, int y1, int x2, int y2, Color color, float width) {
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setColor(color != null ? color : Color.BLACK);
            copy.setStroke(buildStroke(style, width));
            copy.drawLine(x1, y1, x2, y2);
            if (style == Layer.LineSymbolStyle.DOUBLE_LINE) {
                copy.drawLine(x1, y1 + 4, x2, y2 + 4);
            }
        } finally {
            copy.dispose();
        }
    }

    /** Build a BasicStroke for the given line style (cached) */
    public static BasicStroke buildStroke(Layer.LineSymbolStyle style, float baseWidth) {
        Layer.LineSymbolStyle s = style != null ? style : Layer.LineSymbolStyle.SOLID;
        float w = Math.max(0.5f, baseWidth);
        long key = ((long) s.ordinal() << 32) | Float.floatToRawIntBits(w);
        BasicStroke cached = strokeCache.get(key);
        if (cached != null) return cached;
        BasicStroke stroke = createStroke(s, w);
        strokeCache.put(key, stroke);
        return stroke;
    }

    private static BasicStroke createStroke(Layer.LineSymbolStyle s, float w) {
        return switch (s) {
            case SOLID -> new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case DASHED -> new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{10f, 7f}, 0f);
            case DOTTED -> new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{2f, 6f}, 0f);
            case DASH_DOT -> new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{10f, 5f, 2f, 5f}, 0f);
            case DASH_DOT_DOT -> new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{12f, 4f, 2f, 4f, 2f, 4f}, 0f);
            case DOUBLE_LINE -> new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case BOLD -> new BasicStroke(w * 2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case THIN -> new BasicStroke(Math.max(0.5f, w * 0.4f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case PATH_PRIMARY -> new BasicStroke(w * 1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            case PATH_SECONDARY -> new BasicStroke(w * 1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 5f}, 0f);
            case BOUNDARY -> new BasicStroke(w * 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{15f, 4f, 3f, 4f}, 0f);
            case FENCE -> new BasicStroke(w * 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 3f, 1.5f, 3f}, 0f);
            case WATERCOURSE -> new BasicStroke(w * 1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{6f, 4f, 2f, 4f}, 0f);
            case DUCT -> new BasicStroke(w * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{20f, 5f}, 0f);
            case AXIS -> new BasicStroke(w * 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{25f, 5f, 4f, 5f}, 0f);
            case PROFILE -> new BasicStroke(w * 0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{14f, 3f, 3f, 3f}, 0f);
            case EASEMENT -> new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{4f, 8f}, 0f);
            case BORDERED -> new BasicStroke(w * 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        };
    }

    public static void clearCache() { strokeCache.clear(); }
}
