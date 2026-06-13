package ar.com.catgis;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

/**
 * Pan/drag tool: click and drag to move the map view.
 */
class MoveTool implements MapTool {

    private boolean dragging;
    private int lastX, lastY;

    @Override public String name() { return "MOVE"; }

    @Override public void mousePressed(MouseEvent e, MapPanel panel) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            dragging = true;
            panel.captureViewDragStart();
            lastX = e.getX();
            lastY = e.getY();
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
    }

    @Override public void mouseReleased(MouseEvent e, MapPanel panel) {
        if (dragging) {
            dragging = false;
            panel.rememberViewState(panel.dragStartViewMinX, panel.dragStartViewMinY,
                    panel.viewController.getZoomFactor());
        }
    }

    @Override public void mouseDragged(MouseEvent e, MapPanel panel) {
        if (!dragging || panel.isDrawingActive() || panel.isMeasurementActive()) return;
        int dx = e.getX() - lastX;
        int dy = e.getY() - lastY;
        panel.shiftViewByPixels(dx, dy);
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override public Cursor getCursor(MapPanel panel) {
        return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
}
