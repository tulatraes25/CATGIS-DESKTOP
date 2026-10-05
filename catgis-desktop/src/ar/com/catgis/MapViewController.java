package ar.com.catgis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.locationtech.jts.geom.Envelope;

/**
 * Manages map view state: extent, zoom, pan, history, coordinate conversion.
 * Extracted from MapPanel to reduce its responsibilities.
 */
public class MapViewController {

    // View state
    private double viewMinX = 0;
    private double viewMinY = 0;
    private double zoomFactor = 1.0;

    // History
    private final List<ViewState> viewHistory = new ArrayList<>();
    private int viewHistoryIndex = -1;
    private boolean navigatingViewHistory = false;

    // Panel dimensions (set by MapPanel)
    private int panelWidth = 800;
    private int panelHeight = 600;

    // Callback for repaint
    private Runnable repaintCallback;
    private Runnable scaleUpdateCallback;

    // Optional: whether map has loaded content (for scale denominator)
    private BooleanSupplier hasLoadedContent = () -> false;

    // --- View state getters/setters ---

    public double getViewMinX() { return viewMinX; }
    public double getViewMinY() { return viewMinY; }
    public double getZoomFactor() { return zoomFactor; }

    public void setViewMinX(double v) { viewMinX = v; }
    public void setViewMinY(double v) { viewMinY = v; }
    public void setZoomFactor(double v) { zoomFactor = v; }

    public void setPanelSize(int width, int height) {
        this.panelWidth = width;
        this.panelHeight = height;
    }

    // --- Callbacks ---

    public void setRepaintCallback(Runnable callback) { this.repaintCallback = callback; }
    public void setScaleUpdateCallback(Runnable callback) { this.scaleUpdateCallback = callback; }
    public void setHasLoadedContent(BooleanSupplier supplier) { this.hasLoadedContent = supplier; }

    // --- Zoom ---

    public void zoomIn() {
        rememberCurrentView();
        double cx = viewMinX + panelWidth / (2.0 * zoomFactor);
        double cy = viewMinY + panelHeight / (2.0 * zoomFactor);
        applyZoom(1.2, cx, cy);
    }

    public void zoomOut() {
        rememberCurrentView();
        double cx = viewMinX + panelWidth / (2.0 * zoomFactor);
        double cy = viewMinY + panelHeight / (2.0 * zoomFactor);
        applyZoom(1.0 / 1.2, cx, cy);
    }

    public void applyZoom(double factor, double anchorWorldX, double anchorWorldY) {
        double newZoom = zoomFactor * factor;
        if (newZoom <= 0 || Double.isNaN(newZoom) || Double.isInfinite(newZoom)) return;
        viewMinX = anchorWorldX - (anchorWorldX - viewMinX) / factor;
        viewMinY = anchorWorldY - (anchorWorldY - viewMinY) / factor;
        zoomFactor = newZoom;
        notifyScaleUpdate();
        repaint();
    }

    public void handleZoom(double wheelRotation, int screenX, int screenY) {
        rememberCurrentView();
        double factor = wheelRotation < 0 ? 1.2 : 1.0 / 1.2;
        double worldX = screenToWorldX(screenX);
        double worldY = screenToWorldY(screenY);
        applyZoom(factor, worldX, worldY);
        rememberCurrentView();
    }

    // --- Pan ---

    public void pan(double dxPixels, double dyPixels) {
        viewMinX -= dxPixels / zoomFactor;
        viewMinY += dyPixels / zoomFactor;
        repaint();
    }

    public void panTo(double worldX, double worldY) {
        viewMinX = worldX - panelWidth / (2.0 * zoomFactor);
        viewMinY = worldY - panelHeight / (2.0 * zoomFactor);
        repaint();
    }

    // --- Fit to extent ---

    public void fitToEnvelope(Envelope env) {
        if (env == null || env.isNull()) return;
        double width = env.getWidth();
        double height = env.getHeight();
        if (width <= 0) width = 10;
        if (height <= 0) height = 10;

        double pw = panelWidth > 0 ? panelWidth : 800;
        double ph = panelHeight > 0 ? panelHeight : 600;

        double scaleX = (pw * 0.85) / width;
        double scaleY = (ph * 0.85) / height;
        zoomFactor = Math.min(scaleX, scaleY);
        if (zoomFactor <= 0 || Double.isInfinite(zoomFactor) || Double.isNaN(zoomFactor)) {
            zoomFactor = 1.0;
        }

        double extraWorldWidth = pw / zoomFactor - width;
        double extraWorldHeight = ph / zoomFactor - height;
        viewMinX = env.getMinX() - extraWorldWidth / 2.0;
        viewMinY = env.getMinY() - extraWorldHeight / 2.0;
        notifyScaleUpdate();
        repaint();
    }

    public void fitToEnvelope(double envMinX, double envMinY, double envMaxX, double envMaxY) {
        fitToEnvelope(new org.locationtech.jts.geom.Envelope(envMinX, envMaxX, envMinY, envMaxY));
    }

    // --- Scale ---

    public double getCurrentScaleDenominator() {
        if (zoomFactor <= 0d || panelWidth <= 0 || !hasLoadedContent.getAsBoolean()) {
            return 0d;
        }

        int screenDpi = MapUtilities.safeScreenDpi();
        double mppu = estimateMetersPerProjectUnit();
        if (mppu <= 0d) {
            return 0d;
        }

        double groundMetersPerPixel = mppu / zoomFactor;
        double screenMetersPerPixel = 0.0254d / Math.max(1, screenDpi);
        double denominator = groundMetersPerPixel / screenMetersPerPixel;
        if (Double.isFinite(denominator) && denominator > 0d) {
            return denominator;
        }
        return 0d;
    }

    private double estimateMetersPerProjectUnit() {
        String projectCrs = AppContext.project() != null
                ? CRSDefinitions.normalizeCode(AppContext.project().getProjectCRS())
                : "";
        if (projectCrs == null || projectCrs.isBlank()) {
            return 0d;
        }
        if (MapUtilities.isGeographicProjectCrs()) {
            double centerX = viewMinX + (Math.max(1, panelWidth) / (2d * Math.max(zoomFactor, 0.000001d)));
            double centerY = viewMinY + (Math.max(1, panelHeight) / (2d * Math.max(zoomFactor, 0.000001d)));
            double[] geographic = MapUtilities.reprojectPoint(centerX, centerY, projectCrs, "EPSG:4326");
            double centerLat = geographic != null && geographic.length >= 2 ? geographic[1] : centerY;
            double metersPerDegreeLon = 111320d * Math.cos(Math.toRadians(centerLat));
            return Math.max(1d, Math.abs(metersPerDegreeLon));
        }
        return 1d;
    }

    public void applyScale(double targetDenominator) {
        if (targetDenominator <= 0) return;
        double currentScale = getCurrentScaleDenominator();
        if (currentScale <= 0) return;
        double factor = currentScale / targetDenominator;
        double cx = viewMinX + panelWidth / (2.0 * zoomFactor);
        double cy = viewMinY + panelHeight / (2.0 * zoomFactor);
        applyZoom(factor, cx, cy);
    }

    // --- Coordinate conversion ---

    public int getPanelWidth() { return panelWidth; }
    public int getPanelHeight() { return panelHeight; }

    public int worldToScreenX(double worldX) {
        return (int) Math.round((worldX - viewMinX) * zoomFactor);
    }

    public int worldToScreenY(double worldY) {
        return (int) Math.round(panelHeight - (worldY - viewMinY) * zoomFactor);
    }

    public double screenToWorldX(int screenX) {
        return viewMinX + screenX / zoomFactor;
    }

    public double screenToWorldY(int screenY) {
        return viewMinY + (panelHeight - screenY) / zoomFactor;
    }

    // --- History ---

    public void rememberCurrentView() {
        rememberViewState(viewMinX, viewMinY, zoomFactor);
    }

    private void rememberViewState(double minX, double minY, double zoom) {
        if (navigatingViewHistory) return;
        // Trim forward history
        while (viewHistory.size() - 1 > viewHistoryIndex) {
            viewHistory.remove(viewHistory.size() - 1);
        }
        viewHistory.add(new ViewState(minX, minY, zoom));
        if (viewHistory.size() > 50) {
            viewHistory.remove(0);
        }
        viewHistoryIndex = viewHistory.size() - 1;
    }

    /** Public entry — remember a specific view state by coordinates. */
    public void rememberView(double minX, double minY, double zoom) {
        rememberViewState(minX, minY, zoom);
    }

    public boolean canZoomPrevious() { return viewHistoryIndex > 0; }
    public boolean canZoomNext() { return viewHistoryIndex < viewHistory.size() - 1; }

    public void zoomPrevious() {
        if (!canZoomPrevious()) return;
        navigatingViewHistory = true;
        viewHistoryIndex--;
        ViewState vs = viewHistory.get(viewHistoryIndex);
        restoreView(vs.minX, vs.minY, vs.zoom);
        navigatingViewHistory = false;
    }

    public void zoomNext() {
        if (!canZoomNext()) return;
        navigatingViewHistory = true;
        viewHistoryIndex++;
        ViewState vs = viewHistory.get(viewHistoryIndex);
        restoreView(vs.minX, vs.minY, vs.zoom);
        navigatingViewHistory = false;
    }

    public void restoreView(double minX, double minY, double zoom) {
        viewMinX = minX;
        viewMinY = minY;
        zoomFactor = zoom;
        notifyScaleUpdate();
        repaint();
    }

    public void restoreViewOrReset(double minX, double minY, double zoom, boolean hasSavedView) {
        if (hasSavedView) {
            restoreView(minX, minY, zoom);
        } else {
            resetView();
        }
    }

    public void resetView() {
        rememberCurrentView();
        viewMinX = 0;
        viewMinY = 0;
        zoomFactor = 1.0;
        notifyScaleUpdate();
        repaint();
    }

    // --- View envelope ---

    public double[] getCurrentViewEnvelope() {
        return new double[]{
            viewMinX, viewMinY,
            viewMinX + panelWidth / zoomFactor,
            viewMinY + panelHeight / zoomFactor
        };
    }

    // --- Internal ---

    private void repaint() {
        if (repaintCallback != null) repaintCallback.run();
    }

    private void notifyScaleUpdate() {
        if (scaleUpdateCallback != null) scaleUpdateCallback.run();
    }

    // --- Inner class ---

    public static class ViewState {
        public final double minX, minY, zoom;
        public ViewState(double minX, double minY, double zoom) {
            this.minX = minX; this.minY = minY; this.zoom = zoom;
        }
        public boolean isSameAs(double x, double y, double z) {
            return Double.compare(minX, x) == 0 && Double.compare(minY, y) == 0 && Double.compare(zoom, z) == 0;
        }
    }
}
