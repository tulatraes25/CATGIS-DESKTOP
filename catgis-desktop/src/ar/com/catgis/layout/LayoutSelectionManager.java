package ar.com.catgis.layout;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages selection, hit-testing, drag, resize, and marquee selection
 * for layout elements. Delegates hit-testing to LayoutModel.
 */
public class LayoutSelectionManager {

    public enum Handle { NONE, MOVE, NW, NE, SW, SE, N, S, E, W }

    private final LayoutModel model;
    private final int handleSize = 8;

    // Drag state
    private LayoutElement dragElement;
    private double dragStartX, dragStartY;
    private double dragOrigX, dragOrigY, dragOrigW, dragOrigH;
    private Handle activeHandle = Handle.NONE;

    // Marquee state
    private Point marqueeStart;
    private Point marqueeEnd;
    private boolean marqueeActive;

    // Hover state
    private LayoutElement hoveredElement;
    private Handle hoveredHandle = Handle.NONE;
    private double lastHoverX, lastHoverY;

    public LayoutSelectionManager(LayoutModel model) {
        this.model = model;
    }

    // --- Selection ---

    public LayoutElement getSelected() { return model.getSelected(); }

    public void select(LayoutElement el) {
        model.clearSelection();
        if (el != null) el.setSelected(true);
    }

    public void toggleSelection(LayoutElement el) {
        if (el != null) el.setSelected(!el.isSelected());
    }

    public void clearSelection() {
        model.clearSelection();
    }

    // --- Hit testing ---

    public LayoutElement hitTest(double xMm, double yMm) {
        return model.findElementAtMm(xMm, yMm);
    }

    public Handle hitTestHandle(LayoutElement el, double xMm, double yMm) {
        if (el == null) return Handle.NONE;
        Rectangle2D.Double b = el.getBoundsMm();
        double hs = handleSize; // in mm (approximate)

        // Corners
        if (near(xMm, yMm, b.x, b.y, hs)) return Handle.NW;
        if (near(xMm, yMm, b.x + b.width, b.y, hs)) return Handle.NE;
        if (near(xMm, yMm, b.x, b.y + b.height, hs)) return Handle.SW;
        if (near(xMm, yMm, b.x + b.width, b.y + b.height, hs)) return Handle.SE;

        // Edges
        if (nearX(xMm, b.x, hs) && between(yMm, b.y, b.y + b.height)) return Handle.W;
        if (nearX(xMm, b.x + b.width, hs) && between(yMm, b.y, b.y + b.height)) return Handle.E;
        if (nearY(yMm, b.y, hs) && between(xMm, b.x, b.x + b.width)) return Handle.N;
        if (nearY(yMm, b.y + b.height, hs) && between(xMm, b.x, b.x + b.width)) return Handle.S;

        return Handle.MOVE;
    }

    // --- Drag ---

    public void startDrag(double xMm, double yMm) {
        dragElement = model.findTopmostElementAtMm(xMm, yMm);
        if (dragElement == null) return;
        dragStartX = xMm;
        dragStartY = yMm;
        Rectangle2D.Double b = dragElement.getBoundsMm();
        dragOrigX = b.x;
        dragOrigY = b.y;
        dragOrigW = b.width;
        dragOrigH = b.height;
        activeHandle = hitTestHandle(dragElement, xMm, yMm);
        select(dragElement);
    }

    public void updateDrag(double xMm, double yMm) {
        if (dragElement == null || dragElement.isLocked()) return;
        double dx = xMm - dragStartX;
        double dy = yMm - dragStartY;

        switch (activeHandle) {
            case MOVE -> {
                String gid = dragElement.getGroupId();
                if (gid != null) {
                    model.translateGroup(gid, dx, dy);
                    dragStartX = xMm;
                    dragStartY = yMm;
                } else {
                    dragElement.setBoundsMm(dragOrigX + dx, dragOrigY + dy, dragOrigW, dragOrigH);
                }
            }
            case NW -> dragElement.setBoundsMm(dragOrigX + dx, dragOrigY + dy, dragOrigW - dx, dragOrigH - dy);
            case NE -> dragElement.setBoundsMm(dragOrigX, dragOrigY + dy, dragOrigW + dx, dragOrigH - dy);
            case SW -> dragElement.setBoundsMm(dragOrigX + dx, dragOrigY, dragOrigW - dx, dragOrigH + dy);
            case SE -> dragElement.setBoundsMm(dragOrigX, dragOrigY, dragOrigW + dx, dragOrigH + dy);
            case N -> dragElement.setBoundsMm(dragOrigX, dragOrigY + dy, dragOrigW, dragOrigH - dy);
            case S -> dragElement.setBoundsMm(dragOrigX, dragOrigY, dragOrigW, dragOrigH + dy);
            case E -> dragElement.setBoundsMm(dragOrigX, dragOrigY, dragOrigW + dx, dragOrigH);
            case W -> dragElement.setBoundsMm(dragOrigX + dx, dragOrigY, dragOrigW - dx, dragOrigH);
        }
        // Clamp minimum size
        Rectangle2D.Double b = dragElement.getBoundsMm();
        if (b.width < 5) dragElement.setBoundsMm(b.x, b.y, 5, b.height);
        if (b.height < 5) dragElement.setBoundsMm(b.x, b.y, b.width, 5);
    }

    public void endDrag() {
        if (dragElement != null) {
            model.saveSnapshot();
        }
        dragElement = null;
        activeHandle = Handle.NONE;
    }

    public boolean isDragging() { return dragElement != null; }
    public LayoutElement getDragElement() { return dragElement; }
    public Handle getActiveHandle() { return activeHandle; }

    // --- Marquee ---

    public void startMarquee(double xMm, double yMm) {
        marqueeStart = new Point((int) xMm, (int) yMm);
        marqueeActive = true;
    }

    public void updateMarquee(double xMm, double yMm) {
        if (!marqueeActive) return;
        marqueeEnd = new Point((int) xMm, (int) yMm);
    }

    public List<LayoutElement> endMarquee() {
        marqueeActive = false;
        if (marqueeStart == null || marqueeEnd == null) return List.of();

        Rectangle2D.Double rect = marqueeRect();
        List<LayoutElement> selected = new ArrayList<>();
        for (LayoutElement el : model.getElements()) {
            if (el.isVisible() && el.getBoundsMm().intersects(rect)) {
                el.setSelected(true);
                selected.add(el);
            }
        }
        return selected;
    }

    public boolean isMarqueeActive() { return marqueeActive; }
    public Rectangle2D.Double marqueeRect() {
        if (marqueeStart == null || marqueeEnd == null) return null;
        double x = Math.min(marqueeStart.x, marqueeEnd.x);
        double y = Math.min(marqueeStart.y, marqueeEnd.y);
        double w = Math.abs(marqueeEnd.x - marqueeStart.x);
        double h = Math.abs(marqueeEnd.y - marqueeStart.y);
        return new Rectangle2D.Double(x, y, w, h);
    }

    // --- Hover ---

    public void updateHover(double xMm, double yMm) {
        lastHoverX = xMm;
        lastHoverY = yMm;
        hoveredElement = model.findHoverAtMm(xMm, yMm);
        if (hoveredElement != null && !hoveredElement.isLocked()) {
            hoveredHandle = hitTestHandle(hoveredElement, xMm, yMm);
        } else {
            hoveredHandle = Handle.NONE;
        }
    }

    public LayoutElement getHoveredElement() { return hoveredElement; }
    public Handle getHoveredHandle() { return hoveredHandle; }

    // --- Cursor ---

    public Cursor getCursor() {
        return switch (hoveredHandle) {
            case NW, SE -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            case NE, SW -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            case N, S -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            case E, W -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            case MOVE -> Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            default -> Cursor.getDefaultCursor();
        };
    }

    // --- Helpers ---

    private static boolean near(double x, double y, double px, double py, double tol) {
        return Math.abs(x - px) < tol && Math.abs(y - py) < tol;
    }
    private static boolean nearX(double x, double px, double tol) { return Math.abs(x - px) < tol; }
    private static boolean nearY(double y, double py, double tol) { return Math.abs(y - py) < tol; }
    private static boolean between(double v, double lo, double hi) { return v >= lo && v <= hi; }
}
