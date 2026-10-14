package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.ShapefileData;
import java.awt.Graphics2D;

/**
 * Owns the ordered heavyweight layer render dispatch formerly embedded in
 * MapPanel. Receives one graphics context; world labels and live UI overlays
 * are composed separately by MapPanel after the layer pass.
 */
final class MapPanelWorldLayerPainter {
    private MapPanelWorldLayerPainter() {}

    static void paint(MapPanel panel, Graphics2D g2) {
        for (Layer layer : panel.layerManager.getRenderOrderLayers()) {
            if (layer == null || !panel.layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }
            long layerStartedAt = System.nanoTime();
            String renderKind = "vector";

            OnlineRasterSource onlineSource = panel.onlineTileLayers.get(layer);
            if (layer instanceof OnlineTileLayer && onlineSource != null) {
                renderKind = "online-tile";
                panel.drawOnlineTileLayer(g2, (OnlineTileLayer) layer, onlineSource);
                panel.logSlowLayerPaint(layer, renderKind, layerStartedAt);
                continue;
            }

            OnlineWmsLayer wmsLayer = panel.onlineWmsLayers.get(layer);
            if (wmsLayer != null) {
                renderKind = "online-wms";
                panel.drawOnlineWmsLayer(g2, wmsLayer);
                panel.logSlowLayerPaint(layer, renderKind, layerStartedAt);
                continue;
            }

            LocalRasterData rasterData = panel.rasterLayers.get(layer);
            if (rasterData != null) {
                renderKind = "raster";
                panel.drawRasterLayer(g2, layer, rasterData);
                panel.logSlowLayerPaint(layer, renderKind, layerStartedAt);
                continue;
            }

            ShapefileData shapeData = panel.shapefileLayers.get(layer);
            if (shapeData != null) {
                panel.drawLayer(g2, layer, shapeData);
                panel.logSlowLayerPaint(layer, renderKind, layerStartedAt);
            }
        }

    }
}
