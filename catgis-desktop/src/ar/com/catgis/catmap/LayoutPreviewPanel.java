package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Simple preview panel for CATMAP standalone.
 * Renders the layout model to a BufferedImage and displays it.
 */
public class LayoutPreviewPanel extends JPanel {

    private final LayoutModel model;
    private final LayoutRenderContext renderContext;
    private double zoom = 1.0;
    private BufferedImage cachedRender;

    public LayoutPreviewPanel(LayoutModel model, LayoutRenderContext renderContext) {
        this.model = model;
        this.renderContext = renderContext;
        setBackground(new Color(0xF0F0F0));
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Render layout if needed
            if (cachedRender == null) {
                cachedRender = renderLayout();
            }

            if (cachedRender != null) {
                // Center the render in the panel
                int imgW = (int) (cachedRender.getWidth() * zoom);
                int imgH = (int) (cachedRender.getHeight() * zoom);
                int x = (getWidth() - imgW) / 2;
                int y = (getHeight() - imgH) / 2;

                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRect(x + 4, y + 4, imgW, imgH);

                // Draw page
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(cachedRender, x, y, imgW, imgH, null);

                // Draw border
                g2.setColor(Color.GRAY);
                g2.drawRect(x, y, imgW, imgH);
            } else {
                // Placeholder
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
                String msg = "Vista previa del layout";
                int tw = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, (getWidth() - tw) / 2, getHeight() / 2);
            }
        } finally {
            g2.dispose();
        }
    }

    private BufferedImage renderLayout() {
        int widthPx = (int) renderContext.mmToPx(renderContext.getPageWidthMm());
        int heightPx = (int) renderContext.mmToPx(renderContext.getPageHeightMm());

        BufferedImage image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            // White page background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, widthPx, heightPx);

            // Render all visible elements
            for (LayoutElement element : model.getVisibleElementsSortedByZ()) {
                element.render(g2, renderContext);
            }
        } finally {
            g2.dispose();
        }
        return image;
    }

    public void invalidateRender() {
        cachedRender = null;
        repaint();
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(0.1, Math.min(5.0, zoom));
        repaint();
    }

    public double getZoom() { return zoom; }
}
