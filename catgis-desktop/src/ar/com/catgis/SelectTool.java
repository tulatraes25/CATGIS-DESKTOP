package ar.com.catgis;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

/**
 * Select tool: click to select features, drag to box-select.
 */
class SelectTool implements MapTool {

    @Override public String name() { return "SELECT"; }

    @Override public void mousePressed(MouseEvent e, MapPanel panel) {}
    @Override public void mouseReleased(MouseEvent e, MapPanel panel) {}

    @Override public void mouseClicked(MouseEvent e, MapPanel panel) {
        if (panel.suppressNextSelectClick) {
            panel.suppressNextSelectClick = false;
            return;
        }
        panel.selectFeatureForEditing(e.getX(), e.getY(), e.isControlDown());
    }

    @Override public Cursor getCursor(MapPanel panel) {
        return Cursor.getDefaultCursor();
    }
}
