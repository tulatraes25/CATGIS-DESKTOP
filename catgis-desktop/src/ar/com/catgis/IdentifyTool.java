package ar.com.catgis;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

/**
 * Identify tool: click on features to show attribute information.
 */
class IdentifyTool implements MapTool {

    @Override public String name() { return "IDENTIFY"; }

    @Override public void mousePressed(MouseEvent e, MapPanel panel) {}
    @Override public void mouseReleased(MouseEvent e, MapPanel panel) {}

    @Override public void mouseClicked(MouseEvent e, MapPanel panel) {
        panel.identifyFeature(e.getX(), e.getY());
    }

    @Override public Cursor getCursor(MapPanel panel) {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }
}
