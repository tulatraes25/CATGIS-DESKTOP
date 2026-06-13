package ar.com.catgis;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Strategy interface for map interaction tools.
 * Each tool encapsulates mouse event handling for a specific interaction mode.
 */
interface MapTool {

    /** Called when this tool becomes active. */
    default void activate(MapPanel panel) {}

    /** Called when this tool is deactivated. */
    default void deactivate(MapPanel panel) {}

    /** Mouse pressed on the map. */
    void mousePressed(MouseEvent e, MapPanel panel);

    /** Mouse released on the map. */
    void mouseReleased(MouseEvent e, MapPanel panel);

    /** Mouse dragged on the map. */
    default void mouseDragged(MouseEvent e, MapPanel panel) {}

    /** Mouse moved on the map. */
    default void mouseMoved(MouseEvent e, MapPanel panel) {}

    /** Mouse clicked on the map. */
    default void mouseClicked(MouseEvent e, MapPanel panel) {}

    /** Mouse wheel moved on the map. */
    default void mouseWheelMoved(MouseWheelEvent e, MapPanel panel) {}

    /** Cursor to show when this tool is active. */
    default Cursor getCursor(MapPanel panel) { return Cursor.getDefaultCursor(); }

    /** Tool identifier for toolbar/menu binding. */
    String name();
}
