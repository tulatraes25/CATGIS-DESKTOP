package ar.com.catgis.renderer;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Shield and road symbol rendering for cartographic output.
 * Generates shield icons (route markers) and styled labels.
 */
public final class ShieldSymbolRenderer {

    private ShieldSymbolRenderer() {}

    /**
     * Render a highway shield icon (rounded rectangle with text).
     *
     * @param text      shield text (route number)
     * @param bgColor   background color
     * @param textColor text color
     * @param width     shield width in pixels
     * @param height    shield height in pixels
     * @return rendered shield image
     */
    public static BufferedImage renderShield(String text, Color bgColor, Color textColor,
                                              int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background shield
        g.setColor(bgColor);
        g.fill(new RoundRectangle2D.Double(1, 1, width - 2, height - 2, 8, 8));

        // Dark border
        g.setColor(bgColor.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Double(1, 1, width - 2, height - 2, 8, 8));

        // Text centered
        if (text != null && !text.isBlank()) {
            g.setColor(textColor);
            Font font = new Font("SansSerif", Font.BOLD, Math.min(height - 6, width / (text.length() + 1) * 2));
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int tx = (width - fm.stringWidth(text)) / 2;
            int ty = (height - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, tx, ty);
        }
        g.dispose();
        return img;
    }

    /**
     * Render a circular shield (roundabout, junction marker).
     */
    public static BufferedImage renderCircleShield(String text, Color bgColor, Color textColor, int diameter) {
        BufferedImage img = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(bgColor);
        g.fillOval(1, 1, diameter - 2, diameter - 2);
        g.setColor(bgColor.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(1, 1, diameter - 2, diameter - 2);

        if (text != null && !text.isBlank()) {
            g.setColor(textColor);
            Font font = new Font("SansSerif", Font.BOLD, diameter / 3);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            int tx = (diameter - fm.stringWidth(text)) / 2;
            int ty = (diameter - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, tx, ty);
        }
        g.dispose();
        return img;
    }

    /**
     * Predefined shield styles for common route types.
     */
    public enum ShieldStyle {
        INTERSTATE(new Color(0, 51, 160), Color.WHITE),     // blue bg, white text
        US_HIGHWAY(Color.WHITE, Color.BLACK),                // white bg, black text
        STATE_ROUTE(Color.WHITE, Color.BLACK),               // white circle
        TOLL_ROAD(new Color(0, 100, 0), Color.WHITE),       // green bg
        COUNTY_ROAD(new Color(210, 180, 140), Color.BLACK);  // tan bg

        public final Color bg;
        public final Color text;

        ShieldStyle(Color bg, Color text) { this.bg = bg; this.text = text; }
    }

    /**
     * Render a shield using a predefined style.
     */
    public static BufferedImage renderStyledShield(String text, ShieldStyle style, int width, int height) {
        if (style == ShieldStyle.STATE_ROUTE) {
            return renderCircleShield(text, style.bg, style.text, width);
        }
        return renderShield(text, style.bg, style.text, width, height);
    }
}
