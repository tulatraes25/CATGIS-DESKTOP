package ar.com.catgis.renderer;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * TrueType/Glyph symbolizer for rendering font-based map symbols.
 * Supports rotation, scaling, halo effects, and multi-glyph compositions.
 */
public final class GlyphSymbolRenderer {

    private GlyphSymbolRenderer() {}

    /**
     * Render a single glyph as a symbol image.
     *
     * @param glyphChar  Unicode character to render
     * @param font       TrueType font
     * @param color      glyph color
     * @param size       output image size (square)
     * @param rotation   rotation in degrees
     * @param haloColor  halo/outline color (null for no halo)
     * @param haloWidth  halo width in pixels
     * @return rendered symbol image
     */
    public static BufferedImage renderGlyph(char glyphChar, Font font, Color color,
                                             int size, double rotation,
                                             Color haloColor, float haloWidth) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Center transform
        AffineTransform at = new AffineTransform();
        at.translate(size / 2.0, size / 2.0);
        at.rotate(Math.toRadians(rotation));
        g.setTransform(at);

        // Use font at appropriate size
        Font renderFont = font.deriveFont((float) (size * 0.7));
        GlyphVector glyphVector = renderFont.createGlyphVector(g.getFontRenderContext(),
                new char[]{glyphChar});
        Shape glyphShape = glyphVector.getOutline();

        // Center glyph
        Rectangle bounds = glyphShape.getBounds();
        double offsetX = -bounds.getCenterX();
        double offsetY = -bounds.getCenterY();
        glyphShape = AffineTransform.getTranslateInstance(offsetX, offsetY)
                .createTransformedShape(glyphShape);

        // Draw halo
        if (haloColor != null && haloWidth > 0) {
            g.setColor(haloColor);
            g.setStroke(new BasicStroke(haloWidth * 2 + 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(glyphShape);
        }

        // Fill glyph
        g.setColor(color);
        g.fill(glyphShape);

        g.dispose();
        return img;
    }

    /**
     * Render a text string as a symbol (for abbreviated markers).
     */
    public static BufferedImage renderTextSymbol(String text, Font font, Color color,
                                                  int width, int height,
                                                  Color haloColor, float haloWidth) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font renderFont = font.deriveFont((float) (height * 0.8));
        g.setFont(renderFont);
        FontMetrics fm = g.getFontMetrics();
        int tx = (width - fm.stringWidth(text)) / 2;
        int ty = (height - fm.getHeight()) / 2 + fm.getAscent();

        // Halo
        if (haloColor != null && haloWidth > 0) {
            g.setColor(haloColor);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g.drawString(text, tx + dx * haloWidth, ty + dy * haloWidth);
        }

        // Text
        g.setColor(color);
        g.drawString(text, tx, ty);

        g.dispose();
        return img;
    }

    /**
     * Common preset colors for halo effects.
     */
    public static final Color HALO_WHITE = new Color(255, 255, 255, 200);
    public static final Color HALO_BLACK = new Color(0, 0, 0, 180);
    public static final Color HALO_NONE = null;
}
