package ar.com.catgis.layout;

import java.awt.*;
import java.util.List;

/**
 * Canvas rendering engine for layout composition.
 * Renders LayoutElements from a LayoutModel onto a Graphics2D surface.
 * Delegates element-specific rendering to LayoutElement.render().
 */
public class CanvasRenderer {

    private final LayoutModel model;
    private final Rectangle pageBounds;
    private boolean showGrid = true;
    private boolean showSelection = true;
    private double scale = 1.0;
    private Color gridColor = new Color(200, 210, 220);
    private LayoutRenderContext renderContext = new LayoutRenderContext(
            LayoutRenderContext.Mode.PREVIEW, 150, 297, 210);

    public CanvasRenderer(LayoutModel model, Rectangle pageBounds) {
        this.model = model;
        this.pageBounds = new Rectangle(pageBounds);
    }

    public CanvasRenderer setShowGrid(boolean v) { showGrid = v; return this; }
    public CanvasRenderer setShowSelection(boolean v) { showSelection = v; return this; }
    public CanvasRenderer setScale(double v) { scale = v; return this; }
    public CanvasRenderer setGridColor(Color c) { gridColor = c; return this; }
    public CanvasRenderer setRenderContext(LayoutRenderContext ctx) { renderContext = ctx; return this; }
    public LayoutRenderContext getRenderContext() { return renderContext; }

    /**
     * Render the page background and all visible elements sorted by z-order.
     */
    public void render(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Page background
        g2.setColor(Color.WHITE);
        g2.fillRect(pageBounds.x, pageBounds.y, pageBounds.width, pageBounds.height);

        // Grid
        if (showGrid) {
            drawGrid(g2);
        }

        // Page border
        g2.setColor(new Color(180, 190, 204));
        g2.setStroke(new BasicStroke(0.7f));
        g2.drawRect(pageBounds.x, pageBounds.y, pageBounds.width, pageBounds.height);

        // Render elements sorted by z-order
        List<LayoutElement> elements = model.getVisibleElementsSortedByZ();
        for (LayoutElement el : elements) {
            renderElement(g2, el);
        }

        g2.dispose();
    }

    /**
     * Render a single element with selection and lock indicators.
     */
    public void renderElement(Graphics2D g, LayoutElement el) {
        if (el == null || !el.isVisible()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            // Delegate to element's own render method
            el.render(g2, renderContext);

            // Post-render: selection highlight
            if (showSelection && el.isSelected()) {
                java.awt.geom.Rectangle2D.Double b = el.getBoundsMm();
                int x = pageBounds.x + (int) (b.x * scale);
                int y = pageBounds.y + (int) (b.y * scale);
                int w = (int) (b.width * scale);
                int h = (int) (b.height * scale);
                g2.setColor(new Color(59, 130, 246, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(x, y, w - 1, h - 1);
            }

            // Locked indicator
            if (el.isLocked()) {
                java.awt.geom.Rectangle2D.Double b = el.getBoundsMm();
                int x = pageBounds.x + (int) (b.x * scale);
                int y = pageBounds.y + (int) (b.y * scale);
                int w = (int) (b.width * scale);
                int h = (int) (b.height * scale);
                g2.setColor(new Color(160, 160, 160, 100));
                g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, new float[]{4, 4}, 0));
                g2.drawRect(x + 1, y + 1, w - 3, h - 3);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawGrid(Graphics2D g) {
        int gridSpacing = 20;
        g.setColor(gridColor);
        g.setStroke(new BasicStroke(0.3f));
        for (int x = pageBounds.x + gridSpacing; x < pageBounds.x + pageBounds.width; x += gridSpacing) {
            g.drawLine(x, pageBounds.y, x, pageBounds.y + pageBounds.height);
        }
        for (int y = pageBounds.y + gridSpacing; y < pageBounds.y + pageBounds.height; y += gridSpacing) {
            g.drawLine(pageBounds.x, y, pageBounds.x + pageBounds.width, y);
        }
    }
}
