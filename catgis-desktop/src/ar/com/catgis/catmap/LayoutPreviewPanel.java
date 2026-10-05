package ar.com.catgis.catmap;

import ar.com.catgis.layout.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Preview panel for CATMAP standalone with drag & drop support.
 */
public class LayoutPreviewPanel extends JPanel {

    private final LayoutModel model;
    private final LayoutRenderContext renderContext;
    private double zoom = 1.0;
    private BufferedImage cachedRender;

    // Interaction state
    private LayoutElement selectedElement = null;
    private int dragStartX, dragStartY;
    private double dragStartElemX, dragStartElemY;
    private boolean isDragging = false;

    // Callback for selection changes
    private java.util.function.Consumer<LayoutElement> selectionCallback;

    public LayoutPreviewPanel(LayoutModel model, LayoutRenderContext renderContext) {
        this.model = model;
        this.renderContext = renderContext;
        setBackground(new Color(0xF0F0F0));
        setPreferredSize(new Dimension(800, 600));
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        // Mouse listener for selection and drag
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LayoutElement hit = findElementAt(e.getX(), e.getY());
                if (hit != null && !hit.isLocked()) {
                    selectedElement = hit;
                    dragStartX = e.getX();
                    dragStartY = e.getY();
                    dragStartElemX = hit.getBoundsMm().x;
                    dragStartElemY = hit.getBoundsMm().y;
                    isDragging = true;
                    if (selectionCallback != null) selectionCallback.accept(hit);
                } else {
                    selectedElement = null;
                    if (selectionCallback != null) selectionCallback.accept(null);
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging && selectedElement != null) {
                    double dxMm = (e.getX() - dragStartX) / (renderContext.getDpi() / 25.4 * zoom);
                    double dyMm = (e.getY() - dragStartY) / (renderContext.getDpi() / 25.4 * zoom);
                    selectedElement.setBoundsMm(
                            dragStartElemX + dxMm,
                            dragStartElemY + dyMm,
                            selectedElement.getBoundsMm().width,
                            selectedElement.getBoundsMm().height
                    );
                    invalidateRender();
                }
            }
        });

        // Mouse wheel for zoom
        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 1.0 / 1.1;
            setZoom(zoom * factor);
        });
    }

    private LayoutElement findElementAt(int px, int py) {
        // Convert panel coordinates to mm
        double mmX = (px - getPageX()) / (renderContext.getDpi() / 25.4 * zoom);
        double mmY = (py - getPageY()) / (renderContext.getDpi() / 25.4 * zoom);

        // Check elements in reverse z-order (top first)
        for (LayoutElement el : model.getVisibleElementsSortedByZ()) {
            if (el.getBoundsMm().contains(mmX, mmY)) {
                return el;
            }
        }
        return null;
    }

    private int getPageX() {
        if (cachedRender == null) return 0;
        int imgW = (int) (cachedRender.getWidth() * zoom);
        return (getWidth() - imgW) / 2;
    }

    private int getPageY() {
        if (cachedRender == null) return 0;
        int imgH = (int) (cachedRender.getHeight() * zoom);
        return (getHeight() - imgH) / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (cachedRender == null) {
                cachedRender = renderLayout();
            }

            if (cachedRender != null) {
                int imgW = (int) (cachedRender.getWidth() * zoom);
                int imgH = (int) (cachedRender.getHeight() * zoom);
                int x = (getWidth() - imgW) / 2;
                int y = (getHeight() - imgH) / 2;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRect(x + 4, y + 4, imgW, imgH);

                // Page
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(cachedRender, x, y, imgW, imgH, null);

                // Border
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(x, y, imgW, imgH);

                // Selection highlight
                if (selectedElement != null) {
                    Rectangle2D.Double b = selectedElement.getBoundsMm();
                    double mmToPx = renderContext.getDpi() / 25.4 * zoom;
                    int sx = x + (int) (b.x * mmToPx);
                    int sy = y + (int) (b.y * mmToPx);
                    int sw = (int) (b.width * mmToPx);
                    int sh = (int) (b.height * mmToPx);
                    g2.setColor(new Color(59, 130, 246, 80));
                    g2.fillRect(sx, sy, sw, sh);
                    g2.setColor(new Color(59, 130, 246));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(sx, sy, sw, sh);

                    // Resize handles
                    int hs = 6;
                    g2.setColor(Color.WHITE);
                    g2.fillRect(sx - hs/2, sy - hs/2, hs, hs);
                    g2.fillRect(sx + sw - hs/2, sy - hs/2, hs, hs);
                    g2.fillRect(sx - hs/2, sy + sh - hs/2, hs, hs);
                    g2.fillRect(sx + sw - hs/2, sy + sh - hs/2, hs, hs);
                    g2.setColor(new Color(59, 130, 246));
                    g2.drawRect(sx - hs/2, sy - hs/2, hs, hs);
                    g2.drawRect(sx + sw - hs/2, sy - hs/2, hs, hs);
                    g2.drawRect(sx - hs/2, sy + sh - hs/2, hs, hs);
                    g2.drawRect(sx + sw - hs/2, sy + sh - hs/2, hs, hs);
                }
            } else {
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
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, widthPx, heightPx);

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

    public LayoutElement getSelectedElement() { return selectedElement; }

    public void setSelectionCallback(java.util.function.Consumer<LayoutElement> callback) {
        this.selectionCallback = callback;
    }

    public void clearSelection() {
        selectedElement = null;
        repaint();
    }
}
