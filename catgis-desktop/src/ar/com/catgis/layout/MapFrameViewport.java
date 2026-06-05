package ar.com.catgis.layout;

import ar.com.catgis.CatgisDesktopApp;
import ar.com.catgis.Layer;
import ar.com.catgis.MapPanel;
import ar.com.catgis.OnlineTileLayer;
import ar.com.catgis.OnlineWmsLayer;
import ar.com.catgis.Project;
import ar.com.catgis.ShapefileData;
import ar.com.catgis.VectorLayerUtils;
import org.locationtech.jts.geom.Envelope;

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

    public boolean fitFromMainMap() {
        MapPanel map = CatgisDesktopApp.mapPanel;
        if (map != null) {
            Envelope env = map.getCurrentViewEnvelope();
            if (env != null && !env.isNull() && env.getWidth() > 0 && env.getHeight() > 0) {
                // Check if this view actually contains the project data.
                // In standalone mode, MapPanel is created fresh with default view
                // (viewMinX=0, viewMinY=0, zoomFactor=1) which gives an envelope
                // around (0,0) — this is wrong for real-world data.
                Project project = CatgisDesktopApp.currentProject;
                boolean hasData = project != null && project.getLayers() != null
                    && project.getLayers().stream().anyMatch(l -> l != null);

                if (hasData) {
                    // Get combined envelope of visible layers
                    Envelope layersEnv = null;
                    for (Layer layer : project.getLayers()) {
                        if (layer == null || !layer.isVisible()) continue;
                        Envelope le = resolveLayerEnvelope(layer);
                        if (le != null && !le.isNull()) {
                            if (layersEnv == null) layersEnv = new Envelope(le);
                            else layersEnv.expandToInclude(le);
                        }
                    }
                    // If the map view doesn't intersect the data at all,
                    // it's an uninitialized/empty panel — use layers extent instead
                    if (layersEnv != null && !env.intersects(layersEnv)) {
                        double expandX = Math.max(layersEnv.getWidth() * 0.08d, 1d);
                        double expandY = Math.max(layersEnv.getHeight() * 0.08d, 1d);
                        this.minX = layersEnv.getMinX() - expandX;
                        this.minY = layersEnv.getMinY() - expandY;
                        this.maxX = layersEnv.getMaxX() + expandX;
                        this.maxY = layersEnv.getMaxY() + expandY;
                        return true;
                    }
                    // Also check if the main map view is suspiciously large
                    // (indicates default zoomFactor=1 with data in local area)
                    if (layersEnv != null
                            && (env.getWidth() > layersEnv.getWidth() * 100
                             || env.getHeight() > layersEnv.getHeight() * 100)) {
                        double expandX = Math.max(layersEnv.getWidth() * 0.08d, 1d);
                        double expandY = Math.max(layersEnv.getHeight() * 0.08d, 1d);
                        this.minX = layersEnv.getMinX() - expandX;
                        this.minY = layersEnv.getMinY() - expandY;
                        this.maxX = layersEnv.getMaxX() + expandX;
                        this.maxY = layersEnv.getMaxY() + expandY;
                        return true;
                    }
                }

                this.minX = env.getMinX();
                this.minY = env.getMinY();
                this.maxX = env.getMaxX();
                this.maxY = env.getMaxY();
                return true;
            }
        }
        return fitFromProjectLayers();
    }

    public boolean fitFromProjectLayers() {
        Project project = CatgisDesktopApp.currentProject;
        if (project == null || project.getLayers() == null || project.getLayers().isEmpty()) {
            return false;
        }

        Envelope combined = null;
        for (Layer layer : project.getLayers()) {
            if (layer == null || !layer.isVisible()) {
                continue;
            }

            Envelope env = resolveLayerEnvelope(layer);
            if (env == null || env.isNull() || env.getWidth() <= 0 || env.getHeight() <= 0) {
                continue;
            }

            if (combined == null) {
                combined = new Envelope(env);
            } else {
                combined.expandToInclude(env);
            }
        }

        if (combined == null || combined.isNull()) {
            return false;
        }

        double expandX = Math.max(combined.getWidth() * 0.08d, 1d);
        double expandY = Math.max(combined.getHeight() * 0.08d, 1d);
        combined.expandBy(expandX, expandY);
        this.minX = combined.getMinX();
        this.minY = combined.getMinY();
        this.maxX = combined.getMaxX();
        this.maxY = combined.getMaxY();
        return true;
    }

    private Envelope resolveLayerEnvelope(Layer layer) {
        if (layer == null) {
            return null;
        }

        if (layer instanceof OnlineWmsLayer wmsLayer) {
            if (Double.isFinite(wmsLayer.getExtentMinX())
                    && Double.isFinite(wmsLayer.getExtentMinY())
                    && Double.isFinite(wmsLayer.getExtentMaxX())
                    && Double.isFinite(wmsLayer.getExtentMaxY())) {
                return new Envelope(
                        wmsLayer.getExtentMinX(),
                        wmsLayer.getExtentMaxX(),
                        wmsLayer.getExtentMinY(),
                        wmsLayer.getExtentMaxY()
                );
            }
        }

        // For online tile layers, try to get the current view from MapPanel
        // instead of returning the entire world extent
        if (layer instanceof OnlineTileLayer) {
            MapPanel map = CatgisDesktopApp.mapPanel;
            if (map != null) {
                Envelope viewEnv = map.getCurrentViewEnvelope();
                if (viewEnv != null && !viewEnv.isNull() && viewEnv.getWidth() > 0 && viewEnv.getHeight() > 0) {
                    return new Envelope(viewEnv);
                }
            }
            // Only fall back to world extent if no view available
            return null;
        }

        ShapefileData data = VectorLayerUtils.ensureVectorData(layer);
        if (data != null && data.getEnvelope() != null && !data.getEnvelope().isNull()) {
            return new Envelope(data.getEnvelope());
        }
        return null;
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
