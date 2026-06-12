package ar.com.catgis;

/**
 * Shared contract for coordinate conversion and repaint.
 * Extracted from MapPanel to reduce coupling with collaborators.
 */
interface MapViewportContext {
    void repaint();
    double screenToWorldX(int screenX);
    double screenToWorldY(int screenY);
    int worldToScreenX(double worldX);
    int worldToScreenY(double worldY);
}
