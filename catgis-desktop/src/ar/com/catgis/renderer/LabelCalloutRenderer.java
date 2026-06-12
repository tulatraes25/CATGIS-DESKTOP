package ar.com.catgis.renderer;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * Label callouts (leader lines) for dense urban cartography.
 * Draws lines from labels to their anchor points when labels are offset.
 */
public final class LabelCalloutRenderer {

    private LabelCalloutRenderer() {}

    /**
     * Render a leader line from a label position to an anchor point.
     *
     * @param g          graphics context
     * @param labelX     label center X
     * @param labelY     label baseline Y
     * @param anchorX    feature anchor X
     * @param anchorY    feature anchor Y
     * @param color      line color
     * @param lineWidth  line width
     */
    public static void drawLeaderLine(Graphics2D g,
                                       int labelX, int labelY,
                                       int anchorX, int anchorY,
                                       Color color, float lineWidth) {
        if (g == null || color == null) return;
        Graphics2D copy = (Graphics2D) g.create();
        try {
            copy.setColor(color);
            copy.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Find the closest edge of an imaginary label box
            int boxW = 80, boxH = 16; // approximate label size
            int edgeX = labelX, edgeY = labelY;

            // Determine which side of the box is closest to the anchor
            int dx = anchorX - labelX;
            int dy = anchorY - labelY;
            if (Math.abs(dx) * boxH > Math.abs(dy) * boxW) {
                edgeX += (dx > 0) ? boxW / 2 : -boxW / 2;
            } else {
                edgeY += (dy > 0) ? boxH / 2 : -boxH / 2;
            }

            // Draw line with small gap from anchor
            double gap = 3;
            double angle = Math.atan2(anchorY - edgeY, anchorX - edgeX);
            int startX = (int) (anchorX - gap * Math.cos(angle));
            int startY = (int) (anchorY - gap * Math.sin(angle));

            copy.drawLine(startX, startY, edgeX, edgeY);

            // Small dot at anchor
            copy.fillOval(anchorX - 2, anchorY - 2, 4, 4);
        } finally {
            copy.dispose();
        }
    }

    /**
     * Render a callout bubble with leader line (speech-bubble style).
     *
     * @param g         graphics context
     * @param text      label text
     * @param anchorX   feature anchor X
     * @param anchorY   feature anchor Y
     * @param offsetX   bubble offset from anchor in pixels
     * @param offsetY   bubble offset from anchor in pixels
     * @param bgColor   bubble background
     * @param textColor text color
     * @param font      label font
     */
    public static void drawCalloutBubble(Graphics2D g, String text,
                                          int anchorX, int anchorY,
                                          int offsetX, int offsetY,
                                          Color bgColor, Color textColor, Font font) {
        if (g == null || text == null || text.isBlank()) return;
        Graphics2D copy = (Graphics2D) g.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (font != null) copy.setFont(font);
            FontMetrics fm = copy.getFontMetrics();
            int textW = fm.stringWidth(text);
            int textH = fm.getHeight();
            int pad = 4;
            int bw = textW + pad * 2;
            int bh = textH + pad * 2;
            int bx = anchorX + offsetX - bw / 2;
            int by = anchorY + offsetY - bh;

            // Bubble background
            copy.setColor(bgColor);
            copy.fillRoundRect(bx, by, bw, bh, 6, 6);

            // Bubble border
            copy.setColor(bgColor.darker());
            copy.setStroke(new BasicStroke(1f));
            copy.drawRoundRect(bx, by, bw, bh, 6, 6);

            // Leader line
            int tailX = anchorX + offsetX;
            int tailY = anchorY + offsetY - bh;
            copy.drawLine(anchorX, anchorY, tailX, tailY);

            // Text
            copy.setColor(textColor);
            copy.drawString(text, bx + pad, by + pad + fm.getAscent());

            // Anchor dot
            copy.setColor(bgColor.darker());
            copy.fillOval(anchorX - 2, anchorY - 2, 5, 5);
        } finally {
            copy.dispose();
        }
    }
}
