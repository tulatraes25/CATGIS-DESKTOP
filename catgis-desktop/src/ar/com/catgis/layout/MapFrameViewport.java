package ar.com.catgis.layout;

import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.MapPanel;

/**
 * Independent viewport state for a map frame.
 * Each LayoutMap has its own viewport with extent, scale, and center.
 */
public class MapFrameViewport {

    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private double scaleDenominator = 10000;

    public MapFrameViewport() {
        // Default: a reasonable geographic extent
        this.minX = -100;
        this.minY = -100;
        this.maxX = 100;
        this.maxY = 100;
    }

    public MapFrameViewport(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // --- Getters/Setters ---

    public double getMinX() { return minX; }
    public void setMinX(double minX) { this.minX = minX; }

    public double getMinY() { return minY; }
    public void setMinY(double minY) { this.minY = minY; }

    public double getMaxX() { return maxX; }
    public void setMaxX(double maxX) { this.maxX = maxX; }

    public double getMaxY() { return maxY; }
    public void setMaxY(double maxY) { this.maxY = maxY; }

    public double getScaleDenominator() { return scaleDenominator; }
    public void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator > 0 ? scaleDenominator : 10000;
    }

    // --- Convenience methods ---

    public double getWidth() { return maxX - minX; }
    public double getHeight() { return maxY - minY; }

    public double getCenterX() { return (minX + maxX) / 2.0; }
    public double getCenterY() { return (minY + maxY) / 2.0; }

    public void setCenter(double cx, double cy) {
        double w = getWidth();
        double h = getHeight();
        minX = cx - w / 2.0;
        maxX = cx + w / 2.0;
        minY = cy - h / 2.0;
        maxY = cy + h / 2.0;
    }

    public void zoom(double factor) {
        double cx = getCenterX();
        double cy = getCenterY();
        double w = getWidth() / factor;
        double h = getHeight() / factor;
        minX = cx - w / 2.0;
        maxX = cx + w / 2.0;
        minY = cy - h / 2.0;
        maxY = cy + h / 2.0;
        scaleDenominator /= factor;
    }

    public void pan(double dxWorld, double dyWorld) {
        minX += dxWorld;
        maxX += dxWorld;
        minY += dyWorld;
        maxY += dyWorld;
    }

    public void fitToExtent(double targetMinX, double targetMinY, double targetMaxX, double targetMaxY) {
        this.minX = targetMinX;
        this.minY = targetMinY;
        this.maxX = targetMaxX;
        this.maxY = targetMaxY;
    }

    public void fitFromMainMap() {
        MapPanel map = CatgisDesktopApp.mapPanel;
        if (map != null) {
            this.minX = map.getViewMinX();
            this.minY = map.getViewMinY();
            double zf = map.getZoomFactor();
            if (zf <= 0) zf = 1;
            this.maxX = minX + 1000 * zf;
            this.maxY = minY + 1000 * zf;
        }
    }

    public MapFrameViewport copy() {
        return new MapFrameViewport(minX, minY, maxX, maxY);
    }

    @Override
    public String toString() {
        return String.format("Viewport[%.1f,%.1f -> %.1f,%.1f scale=1:%.0f]",
                minX, minY, maxX, maxY, scaleDenominator);
    }
}
