package ar.com.catgis;

/**
 * Owns MapPanel's bounded pan preview lifecycle and invalidation policy.
 * Keeps session bookkeeping out of the hotspot view class.
 */
final class MapPanSceneController {
    private final MapPanel panel;
    private final MapPanScenePreview scene = new MapPanScenePreview();
    private boolean panRepaintRequest;
    private boolean exporting;

    MapPanSceneController(MapPanel panel) {
        this.panel = panel;
    }

    MapPanScenePreview scene() {
        return scene;
    }

    boolean isExporting() {
        return exporting;
    }

    void setExporting(boolean value) {
        exporting = value;
        scene.invalidate();
    }

    void begin() {
        if (!scene.isActive()) {
            scene.begin();
        }
    }

    void requestPanRepaint() {
        panRepaintRequest = true;
        try {
            panel.repaint();
        } finally {
            panRepaintRequest = false;
        }
    }

    void beforeRepaint() {
        if (!panRepaintRequest) {
            scene.invalidate();
        }
    }

    void end() {
        if (!scene.isActive()) {
            return;
        }
        MapPanScenePreview.Metrics m = scene.end();
        if (m.captures() != 0 || m.reuses() != 0 || m.fallbacks() != 0) {
            CatgisLogger.info("[P0-PAN] captures=" + m.captures()
                    + " previewPaints=" + m.reuses()
                    + " fallbackPaints=" + m.fallbacks()
                    + " invalidations=" + m.invalidations()
                    + " captureMs=" + m.captureNanos() / 1_000_000L);
        }
        panel.repaint();
    }

    boolean isWorldSceneEligible() {
        return (!panel.shapefileLayers.isEmpty() || !panel.rasterLayers.isEmpty()
                    || !panel.onlineTileLayers.isEmpty()
                    || !panel.onlineWmsLayers.isEmpty())
                && !panel.featureEditMode && !panel.draggingPin
                && !panel.movingSelectedFeatures
                && !panel.selectionBoxActive && !panel.isDrawingActive()
                && !panel.isMeasurementActive()
                && !panel.topographicProfileTool.isActive()
                && !panel.cadEngine.cadPlacementDragActive
                && !panel.cadEngine.pointCaptureActive
                && panel.selectionFlashGeometry == null;
    }
}
