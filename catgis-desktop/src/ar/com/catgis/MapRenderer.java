package ar.com.catgis;

import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineTileCache;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.core.model.GradientFill;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.renderer.PolygonSymbolRenderer;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.renderer.labels.LabelExpressionEngine;
import ar.com.catgis.renderer.labels.LabelPlacementEngine;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapRenderer {

    private final MapPanel panel;

    public MapRenderer(MapPanel panel) {
        this.panel = panel;
    }

    public void render(Graphics2D g2, int width, int height) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        panel.onlineResolutionNoticeVisible = false;
        panel.onlineResolutionNotice = "";

        if (panel.shapefileLayers.isEmpty() && panel.rasterLayers.isEmpty() && panel.onlineTileLayers.isEmpty() && panel.onlineWmsLayers.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString(panel.openedFileText, 20, 30);
        }

        for (Layer layer : panel.getRenderOrderLayers()) {
            if (layer == null || !panel.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            OnlineRasterSource onlineSource = panel.onlineTileLayers.get(layer);
            if (layer instanceof OnlineTileLayer && onlineSource != null) {
                drawOnlineTileLayer(g2, (OnlineTileLayer) layer, onlineSource);
                continue;
            }

            OnlineWmsLayer wmsLayer = panel.onlineWmsLayers.get(layer);
            if (wmsLayer != null) {
                drawOnlineWmsLayer(g2, wmsLayer);
                continue;
            }

            LocalRasterData rasterData = panel.rasterLayers.get(layer);
            if (rasterData != null) {
                drawRasterLayer(g2, layer, rasterData);
                continue;
            }

            ShapefileData shapeData = panel.shapefileLayers.get(layer);
            if (shapeData != null) {
                drawLayer(g2, layer, shapeData);
            }
        }

        drawAllLabels(g2);
        drawHeatmapOverlay(g2);
        drawPointClusters(g2);

        if (!panel.layoutRenderMode) {
            drawAttributeTableSelections(g2);
            if (panel.selectedLayer != null && panel.selectedFeature != null) {
                drawSelectedFeature(g2, panel.selectedFeature, panel.selectedLayer);
                if (panel.featureEditMode) {
                    drawEditableVertices(g2, panel.selectedFeature, panel.selectedLayer);
                }
            }
            drawSelectionFlash(g2);
            drawPins(g2);
            drawCurrentSketch(g2);
            drawCurrentMeasurement(g2);
            drawFeatureEditSketch(g2);
            drawTopographicProfileCapture(g2);
            drawSnapPreview(g2);
            drawSelectionBox(g2);
        }
        if (!panel.layoutRenderMode) {
            drawOnlineAttribution(g2);
            if (panel.mapDecorations != null) {
                String crsDesc = CatgisDesktopApp.currentProject != null
                        ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
                panel.mapDecorations.render(g2, width, height,
                        panel.getCurrentViewEnvelope(),
                        panel.getCurrentScaleDenominator(),
                        crsDesc);
            }
        }
    }

    void drawCursorBadge(Graphics2D g2, Color halo, Color badgeFill, Color stroke) {
        g2.setColor(halo);
        g2.fillOval(13, 13, 16, 16);
        g2.setColor(badgeFill);
        g2.fillOval(14, 14, 14, 14);
        g2.setColor(stroke);
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawOval(14, 14, 14, 14);
    }

    void drawSelectionBadge(Graphics2D g2, Color ink) {
        Object geomObj = panel.selectedFeature != null ? panel.selectedFeature.getDefaultGeometry() : null;
        g2.setColor(ink);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (geomObj instanceof Point || geomObj instanceof MultiPoint) {
            g2.fillOval(18, 18, 4, 4);
            return;
        }
        if (geomObj instanceof Polygon || geomObj instanceof MultiPolygon) {
            g2.drawRect(17, 17, 6, 6);
            return;
        }
        g2.drawLine(17, 23, 24, 17);
    }

    void drawCursorPointer(Graphics2D g2) {
        Path2D.Double outline = new Path2D.Double();
        outline.moveTo(2, 2);
        outline.lineTo(2, 22);
        outline.lineTo(7, 17);
        outline.lineTo(10, 26);
        outline.lineTo(13, 24);
        outline.lineTo(10, 16);
        outline.lineTo(16, 16);
        outline.closePath();

        g2.setColor(new Color(255, 255, 255, 245));
        g2.fill(outline);
        g2.setColor(new Color(30, 30, 34));
        g2.setStroke(new BasicStroke(1.1f));
        g2.draw(outline);
        g2.drawLine(6, 18, 9, 24);
    }

    public void drawPointClusters(Graphics2D g2) {
        if (panel.shapefileLayers.isEmpty()) return;

        java.util.List<java.awt.geom.Point2D> allPoints = new java.util.ArrayList<>();
        int radius = 30;
        boolean hasClustering = false;

        for (Layer layer : panel.getRenderOrderLayers()) {
            if (layer == null || !panel.isLayerEffectivelyVisible(layer)) continue;
            if (!layer.isClusteringEnabled()) continue;
            hasClustering = true;
            radius = layer.getClusterRadius();

            ShapefileData data = panel.shapefileLayers.get(layer);
            if (data == null) continue;

            try (org.geotools.feature.FeatureIterator<
                    org.geotools.api.feature.simple.SimpleFeature> it =
                    data.getFeatureCollection().features()) {
                while (it.hasNext()) {
                    org.geotools.api.feature.simple.SimpleFeature f = it.next();
                    Object geomObj = f.getDefaultGeometry();
                    if (geomObj instanceof org.locationtech.jts.geom.Point pt) {
                        allPoints.add(new java.awt.geom.Point2D.Double(
                            panel.worldToScreenX(pt.getX()), panel.worldToScreenY(pt.getY())));
                    } else if (geomObj instanceof org.locationtech.jts.geom.MultiPoint mp) {
                        for (int i = 0; i < mp.getNumGeometries(); i++) {
                            org.locationtech.jts.geom.Geometry g = mp.getGeometryN(i);
                            if (g instanceof org.locationtech.jts.geom.Point p) {
                                allPoints.add(new java.awt.geom.Point2D.Double(
                                    panel.worldToScreenX(p.getX()), panel.worldToScreenY(p.getY())));
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        if (!hasClustering || allPoints.isEmpty()) return;

        var clusters = PointClusterRenderer.clusterPoints(allPoints, radius);
        int w = Math.max(1, panel.getWidth());
        int h = Math.max(1, panel.getHeight());
        BufferedImage clusterImg = PointClusterRenderer.renderClusters(clusters, w, h);
        if (clusterImg != null) g2.drawImage(clusterImg, 0, 0, null);
    }

    public void drawHeatmapOverlay(Graphics2D g2) {
        if (panel.shapefileLayers.isEmpty()) return;

        java.util.List<java.awt.geom.Point2D> allPoints = new java.util.ArrayList<>();
        int radius = 30;
        float opacity = 0.6f;
        boolean hasHeatmap = false;

        for (Layer layer : panel.getRenderOrderLayers()) {
            if (layer == null || !panel.isLayerEffectivelyVisible(layer)) continue;
            if (!layer.isHeatmapEnabled()) continue;
            hasHeatmap = true;
            radius = layer.getHeatmapRadius();
            opacity = layer.getHeatmapOpacity();

            ShapefileData data = panel.shapefileLayers.get(layer);
            if (data == null) continue;

            try (org.geotools.feature.FeatureIterator<
                    org.geotools.api.feature.simple.SimpleFeature> it =
                    data.getFeatureCollection().features()) {
                while (it.hasNext()) {
                    org.geotools.api.feature.simple.SimpleFeature f = it.next();
                    Object geomObj = f.getDefaultGeometry();
                    if (geomObj instanceof org.locationtech.jts.geom.Point pt) {
                        int sx = panel.worldToScreenX(pt.getX());
                        int sy = panel.worldToScreenY(pt.getY());
                        allPoints.add(new java.awt.geom.Point2D.Double(sx, sy));
                    } else if (geomObj instanceof org.locationtech.jts.geom.MultiPoint mp) {
                        for (int i = 0; i < mp.getNumGeometries(); i++) {
                            org.locationtech.jts.geom.Geometry g = mp.getGeometryN(i);
                            if (g instanceof org.locationtech.jts.geom.Point p) {
                                int sx = panel.worldToScreenX(p.getX());
                                int sy = panel.worldToScreenY(p.getY());
                                allPoints.add(new java.awt.geom.Point2D.Double(sx, sy));
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        if (!hasHeatmap || allPoints.isEmpty()) return;

        int w = Math.max(1, panel.getWidth());
        int h = Math.max(1, panel.getHeight());
        BufferedImage heatmap = HeatmapRenderer.renderHeatmap(allPoints, w, h, radius, opacity);
        if (heatmap != null) {
            g2.drawImage(heatmap, 0, 0, null);
        }
    }

    public void drawSelectionBox(Graphics2D g2) {
        if (!panel.selectionBoxActive || !panel.selectionBoxDragging) {
            return;
        }

        Rectangle bounds = panel.getSelectionBoxBounds();
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setColor(new Color(59, 130, 246, 38));
            copy.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            copy.setColor(new Color(37, 99, 235, 210));
            copy.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f));
            copy.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        } finally {
            copy.dispose();
        }
    }

    public void drawSelectionFlash(Graphics2D g2) {
        if (panel.selectionFlashGeometry == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - panel.selectionFlashStartedAt;
        if (elapsed >= panel.SELECTION_FLASH_DURATION_MS) {
            panel.selectionFlashGeometry = null;
            panel.selectionFlashTimer.stop();
            return;
        }

        Coordinate focus = panel.resolveSelectionFlashCoordinate(panel.selectionFlashGeometry);
        if (focus == null) {
            return;
        }

        float progress = elapsed / (float) panel.SELECTION_FLASH_DURATION_MS;
        float baseAlpha = 1f - progress;
        int x = panel.worldToScreenX(focus.x);
        int y = panel.worldToScreenY(focus.y);

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.12f, baseAlpha * 0.42f)));
            copy.setColor(new Color(255, 215, 0));
            int glow = Math.round(10f + (progress * 30f));
            copy.fillOval(x - (glow / 2), y - (glow / 2), glow, glow);

            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.08f, baseAlpha * 0.75f)));
            copy.setColor(new Color(255, 225, 90));
            copy.setStroke(new BasicStroke(2.4f));
            int ringA = Math.round(18f + (progress * 34f));
            int ringB = Math.round(8f + (progress * 18f));
            copy.drawOval(x - (ringA / 2), y - (ringA / 2), ringA, ringA);
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.04f, baseAlpha * 0.5f)));
            copy.drawOval(x - (ringB / 2), y - (ringB / 2), ringB, ringB);
        } finally {
            copy.dispose();
        }
    }

    public void drawRasterLayer(Graphics2D g2, Layer layer, LocalRasterData data) {
        if (data == null || data.getImage() == null) {
            return;
        }

        Envelope env = panel.getRasterEnvelope(layer, data);
        if (env == null || env.isNull()) {
            return;
        }

        int x1 = panel.worldToScreenX(env.getMinX());
        int y1 = panel.worldToScreenY(env.getMaxY());
        int x2 = panel.worldToScreenX(env.getMaxX());
        int y2 = panel.worldToScreenY(env.getMinY());

        int drawX = Math.min(x1, x2);
        int drawY = Math.min(y1, y2);
        int drawW = Math.abs(x2 - x1);
        int drawH = Math.abs(y2 - y1);

        if (drawW <= 1 || drawH <= 1) {
            return;
        }

        MapPanel.RasterStyle style = panel.getOrCreateRasterStyle(layer, Math.max(1, data.getBandCount()));
        BufferedImage display = panel.getCachedDisplayImage(layer, data, style);
        if (display == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, style.opacity));
            copy.drawImage(display, drawX, drawY, drawW, drawH, null);
        } finally {
            copy.dispose();
        }
    }

    public void drawOnlineTileLayer(Graphics2D g2, OnlineTileLayer layer, OnlineRasterSource source) {
        if (layer == null || source == null || panel.getWidth() <= 0 || panel.getHeight() <= 0) {
            return;
        }

        String projectCRS = CatgisDesktopApp.currentProject != null ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
        if (projectCRS == null || projectCRS.isBlank()) {
            return;
        }

        Envelope projectView = new Envelope(
                panel.screenToWorldX(0),
                panel.screenToWorldX(panel.getWidth()),
                panel.screenToWorldY(panel.getHeight()),
                panel.screenToWorldY(0)
        );

        Envelope mercatorView = panel.projectEnvelopeToMercator(projectView, projectCRS);
        if (mercatorView == null || mercatorView.isNull()) {
            return;
        }

        int desiredZoom = OnlineMapUtils.estimateZoom(mercatorView, panel.getWidth(), panel.getHeight(), source);
        int zoom = OnlineMapUtils.chooseZoom(mercatorView, panel.getWidth(), panel.getHeight(), source);
        boolean detailLimited = desiredZoom > source.getMaxZoom();
        boolean usedFallbackTile = false;
        int renderedTiles = 0;
        OnlineMapUtils.TileRange range = OnlineMapUtils.calculateTileRange(mercatorView, zoom);
        if (range.tileCount() <= 0) {
            return;
        }

        for (int tx = range.minX; tx <= range.maxX; tx++) {
            for (int ty = range.minY; ty <= range.maxY; ty++) {
                Envelope tileMercator = OnlineMapUtils.tileBounds(tx, ty, zoom);
                Envelope tileProject = panel.reprojectEnvelopeIfNeeded(tileMercator, "EPSG:3857", projectCRS);
                if (tileProject == null || tileProject.isNull()) {
                    continue;
                }

                int x1 = panel.worldToScreenX(tileProject.getMinX());
                int y1 = panel.worldToScreenY(tileProject.getMaxY());
                int x2 = panel.worldToScreenX(tileProject.getMaxX());
                int y2 = panel.worldToScreenY(tileProject.getMinY());

                int drawX = Math.min(x1, x2);
                int drawY = Math.min(y1, y2);
                int drawW = Math.abs(x2 - x1);
                int drawH = Math.abs(y2 - y1);

                if (drawW <= 1 || drawH <= 1) {
                    continue;
                }

                BufferedImage tile = OnlineTileCache.getTile(source, zoom, tx, ty, panel::repaint);
                Graphics2D copy = (Graphics2D) g2.create();
                try {
                    float opacity = layer.getOpacity();
                    copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, Math.min(1f, opacity))));
                    if (tile != null) {
                        copy.drawImage(tile, drawX, drawY, drawW, drawH, null);
                        renderedTiles++;
                    } else {
                        MapPanel.OnlineTileFallback fallback = panel.resolveFallbackOnlineTile(source, zoom, tx, ty);
                        if (fallback == null) {
                            continue;
                        }
                        usedFallbackTile = true;
                        renderedTiles++;
                        copy.drawImage(
                                fallback.image,
                                drawX,
                                drawY,
                                drawX + drawW,
                                drawY + drawH,
                                fallback.srcX,
                                fallback.srcY,
                                fallback.srcX + fallback.srcW,
                                fallback.srcY + fallback.srcH,
                                null
                        );
                    }
                } finally {
                    copy.dispose();
                }
            }
        }

        if (renderedTiles <= 0) {
            String failure = OnlineTileCache.getRecentSourceFailure(source.getId(), 120000L);
            if (failure != null && !failure.isBlank()) {
                panel.onlineResolutionNoticeVisible = true;
                panel.onlineResolutionNotice = "No se pudieron descargar teselas de " + source.getName() + ": " + failure;
                panel.pushTileStatusToBar();
            } else if (range.tileCount() > 0) {
                panel.onlineResolutionNoticeVisible = true;
                panel.onlineResolutionNotice = "Cargando teselas de " + source.getName() + "...";
                panel.pushTileStatusToBar();
            }
        } else if (detailLimited) {
            panel.onlineResolutionNoticeVisible = true;
            panel.onlineResolutionNotice = "Zoom mayor al detalle disponible en " + source.getName() + ". Se muestra la ultima resolucion util.";
            panel.pushTileStatusToBar();
        } else if (usedFallbackTile && !panel.onlineResolutionNoticeVisible) {
            panel.onlineResolutionNoticeVisible = true;
            panel.onlineResolutionNotice = "Algunas teselas no estan disponibles en este zoom. Se mantiene el ultimo detalle disponible.";
            panel.pushTileStatusToBar();
        }
    }

    public void drawOnlineWmsLayer(Graphics2D g2, OnlineWmsLayer layer) {
        if (layer == null || panel.getWidth() <= 8 || panel.getHeight() <= 8) {
            return;
        }

        String requestUrl = panel.buildWmsGetMapUrl(layer);
        if (requestUrl == null || requestUrl.isBlank()) {
            return;
        }

        BufferedImage image = OnlineWmsImageCache.getImage(requestUrl, layer.getImageFormat(), panel::repaint);
        if (image == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, Math.min(1f, layer.getOpacity()))));
            copy.drawImage(image, 0, 0, panel.getWidth(), panel.getHeight(), null);
        } finally {
            copy.dispose();
        }
    }

    public void drawOnlineAttribution(Graphics2D g2) {
        String attribution = panel.buildVisibleOnlineAttribution();
        if (attribution.isBlank()) {
            return;
        }

        FontMetrics metrics = g2.getFontMetrics(g2.getFont().deriveFont(11f));
        int padding = 8;
        int textWidth = metrics.stringWidth(attribution);
        int textHeight = metrics.getHeight();
        int boxWidth = textWidth + padding * 2;
        int boxHeight = textHeight + 4;
        int x = Math.max(8, panel.getWidth() - boxWidth - 10);
        int y = Math.max(20, panel.getHeight() - boxHeight - 10);

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setColor(new Color(255, 255, 255, 215));
            copy.fillRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            copy.setColor(new Color(55, 65, 81, 235));
            copy.drawRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            copy.setColor(new Color(31, 41, 55));
            copy.setFont(copy.getFont().deriveFont(11f));
            copy.drawString(attribution, x + padding, y + metrics.getAscent() + 2);
        } finally {
            copy.dispose();
        }
    }

    public void drawPins(Graphics2D g2) {
        for (PinMarker pin : panel.pins) {
            int x = panel.worldToScreenX(pin.getX());
            int y = panel.worldToScreenY(pin.getY());

            g2.setColor(Color.RED);
            g2.fillOval(x - 6, y - 6, 12, 12);

            g2.setColor(Color.WHITE);
            g2.fillOval(x - 3, y - 3, 6, 6);

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x, y + 6, x, y + 16);

            String label = "P" + pin.getId();
            g2.setColor(Color.BLACK);
            g2.drawString(label, x + 8, y - 8);
        }
    }

    public void drawCurrentSketch(Graphics2D g2) {
        if (!panel.isDrawingActive()) {
            return;
        }

        drawPendingDrawingSessionGeometries(g2);

        if ("CONTINUE_LINE".equalsIgnoreCase(panel.drawingMode) && !panel.drawingContinuationEndpointChosen) {
            drawContinuationEndpointHints(g2);
            return;
        }

        Coordinate previewCoordinate = panel.resolveInteractivePreviewCoordinate();

        if ("POINT".equalsIgnoreCase(panel.drawingMode) || "MULTIPOINT".equalsIgnoreCase(panel.drawingMode)) {
            for (Coordinate c : panel.drawingCoordinates) {
                int x = panel.worldToScreenX(c.x);
                int y = panel.worldToScreenY(c.y);
                g2.setColor(Color.MAGENTA);
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }

            if (previewCoordinate != null) {
                int x = panel.worldToScreenX(previewCoordinate.x);
                int y = panel.worldToScreenY(previewCoordinate.y);
                g2.setColor(new Color(255, 0, 255, 120));
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(panel.drawingCoordinates);
        if (previewCoordinate != null) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        if ("CIRCLE".equalsIgnoreCase(panel.drawingMode) || "CIRCLE_3P".equalsIgnoreCase(panel.drawingMode)) {
            Geometry previewGeometry = "CIRCLE".equalsIgnoreCase(panel.drawingMode)
                    ? panel.buildCircleGeometry(tempCoords)
                    : panel.buildCircleThreePointGeometry(tempCoords);
            if (previewGeometry != null) {
                drawPendingDrawingGeometry(g2, previewGeometry);
                return;
            }
        }

        if ("RECTANGLE".equalsIgnoreCase(panel.drawingMode)) {
            tempCoords = panel.buildRectangleCoordinates(tempCoords);
        }

        if (tempCoords.isEmpty()) {
            return;
        }

        drawTemporaryGeometry(
                g2,
                tempCoords,
                "RECTANGLE".equalsIgnoreCase(panel.drawingMode) ? "POLYGON" : panel.drawingMode,
                Color.MAGENTA,
                new Color(255, 0, 255, 40)
        );
    }

    public void drawPendingDrawingSessionGeometries(Graphics2D g2) {
        if (panel.pendingDrawingSessionGeometries.isEmpty()) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            for (Geometry geometry : panel.pendingDrawingSessionGeometries) {
                drawPendingDrawingGeometry(copy, geometry);
            }
        } finally {
            copy.dispose();
        }
    }

    public void drawPendingDrawingGeometry(Graphics2D g2, Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, Color.MAGENTA, 10);
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint multiPoint = (MultiPoint) geometry;
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                Geometry child = multiPoint.getGeometryN(i);
                if (child instanceof Point) {
                    drawPoint(g2, (Point) child, Color.MAGENTA, 10);
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, Color.MAGENTA, 2.2f);
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multiLineString = (MultiLineString) geometry;
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                Geometry child = multiLineString.getGeometryN(i);
                if (child instanceof LineString) {
                    drawLineString(g2, (LineString) child, Color.MAGENTA, 2.2f);
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            drawPolygon(g2, (Polygon) geometry, new Color(255, 0, 255, 40), Color.MAGENTA, 2f);
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Geometry child = multiPolygon.getGeometryN(i);
                if (child instanceof Polygon) {
                    drawPolygon(g2, (Polygon) child, new Color(255, 0, 255, 40), Color.MAGENTA, 2f);
                }
            }
        }
    }

    public void drawContinuationEndpointHints(Graphics2D g2) {
        if (!"CONTINUE_LINE".equalsIgnoreCase(panel.drawingMode)
                || panel.drawingContinuationBaseCoordinates == null
                || panel.drawingContinuationBaseCoordinates.length < 2
                || panel.drawingContinuationLayer == null) {
            return;
        }

        Coordinate start = panel.toProjectCoordinate(panel.drawingContinuationBaseCoordinates[0], panel.drawingContinuationLayer);
        Coordinate end = panel.toProjectCoordinate(
                panel.drawingContinuationBaseCoordinates[panel.drawingContinuationBaseCoordinates.length - 1],
                panel.drawingContinuationLayer
        );
        if (start == null || end == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawContinuationEndpointHint(copy, start, "A");
            drawContinuationEndpointHint(copy, end, "B");
        } finally {
            copy.dispose();
        }
    }

    public void drawContinuationEndpointHint(Graphics2D g2, Coordinate coordinate, String label) {
        int x = panel.worldToScreenX(coordinate.x);
        int y = panel.worldToScreenY(coordinate.y);

        g2.setColor(new Color(37, 99, 235, 58));
        g2.fillOval(x - 12, y - 12, 24, 24);
        g2.setColor(new Color(29, 78, 216));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawOval(x - 9, y - 9, 18, 18);
        g2.setColor(Color.WHITE);
        g2.fillOval(x - 4, y - 4, 8, 8);
        g2.setColor(new Color(29, 78, 216));
        g2.drawString(label, x + 10, y - 10);
    }

    public void drawCurrentMeasurement(Graphics2D g2) {
        if (!panel.isMeasurementActive() || panel.measurementCoordinates.isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(panel.measurementCoordinates);
        Coordinate previewCoordinate = panel.resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        drawTemporaryGeometry(g2, tempCoords, panel.measurementMode, Color.CYAN, new Color(0, 255, 255, 40));
    }

    public void drawFeatureEditSketch(Graphics2D g2) {
        if (!panel.featureEditMode) {
            return;
        }

        if (panel.isCadLineConstructionMode()) {
            drawCadOperationPreview(g2);
            return;
        }

        if (panel.EDIT_OP_ADJACENT_POLYGON.equals(panel.featureEditOperation)) {
            drawAdjacentPolygonPreview(g2);
            return;
        }

        if (panel.featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(panel.featureEditSketchCoordinates);
        Coordinate previewCoordinate = panel.resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null && panel.isFeatureEditSketchMode()) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        String mode = panel.EDIT_OP_HOLE.equals(panel.featureEditOperation) ? "POLYGON" : "LINE";
        drawTemporaryGeometry(g2, tempCoords, mode, new Color(14, 116, 144), new Color(14, 165, 233, 48));
    }

    public void drawTopographicProfileCapture(Graphics2D g2) {
        if (!panel.topographicProfileCaptureActive) {
            return;
        }
        List<Coordinate> tempCoords = new ArrayList<>(panel.topographicProfileCaptureCoordinates);
        Coordinate previewCoordinate = panel.resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null && !tempCoords.isEmpty()) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }
        if (tempCoords.isEmpty()) {
            return;
        }
        drawTemporaryGeometry(g2, tempCoords, "LINE", new Color(180, 83, 9), new Color(245, 158, 11, 48));
    }

    public void drawAdjacentPolygonPreview(Graphics2D g2) {
        if (panel.selectedFeature == null || panel.selectedLayer == null
                || panel.adjacentPolygonSegmentStart == null || panel.adjacentPolygonSegmentEnd == null) {
            return;
        }

        drawAdjacentBaseSegment(g2);

        Coordinate previewCoordinate = panel.resolveInteractivePreviewCoordinate();
        if (previewCoordinate == null) {
            return;
        }

        Object geomObj = panel.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry sourceGeometry)) {
            return;
        }

        Coordinate sourcePreview = panel.toSourceCoordinate(previewCoordinate.x, previewCoordinate.y, panel.selectedLayer);
        Geometry previewGeometry = panel.buildAdjacentPolygonGeometry(
                sourceGeometry,
                panel.adjacentPolygonSegmentStart,
                panel.adjacentPolygonSegmentEnd,
                sourcePreview
        );
        if (previewGeometry == null || previewGeometry.isEmpty()) {
            return;
        }

        Geometry displayGeometry = panel.reprojectGeometryIfNeeded(panel.selectedLayer, previewGeometry);
        if (!(displayGeometry instanceof Polygon previewPolygon)) {
            return;
        }

        drawPolygon(g2, previewPolygon, new Color(34, 197, 94, 56), new Color(21, 128, 61), 2f);
    }

    public void drawAdjacentBaseSegment(Graphics2D g2) {
        if (panel.adjacentPolygonSegmentStart == null || panel.adjacentPolygonSegmentEnd == null || panel.selectedLayer == null) {
            return;
        }

        GeometryFactory factory = new GeometryFactory();
        LineString baseSegment = factory.createLineString(new Coordinate[]{
                new Coordinate(panel.adjacentPolygonSegmentStart),
                new Coordinate(panel.adjacentPolygonSegmentEnd)
        });
        Geometry displaySegment = panel.reprojectGeometryIfNeeded(panel.selectedLayer, baseSegment);
        if (!(displaySegment instanceof LineString lineString)) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            drawLineString(copy, lineString, new Color(21, 128, 61), 3.2f);
        } finally {
            copy.dispose();
        }
    }

    public void drawCadOperationPreview(Graphics2D g2) {
        if (panel.selectedFeature == null || panel.selectedLayer == null) {
            return;
        }

        if ((panel.EDIT_OP_EXTEND_LINE.equals(panel.featureEditOperation) || panel.EDIT_OP_SHORTEN_LINE.equals(panel.featureEditOperation))
                && !panel.cadReferenceEndpointChosen) {
            drawSelectedLineEndpointHints(g2);
            return;
        }

        if ((panel.EDIT_OP_PARALLEL.equals(panel.featureEditOperation) || panel.EDIT_OP_PERPENDICULAR.equals(panel.featureEditOperation))
                && panel.cadReferenceSegmentStart != null && panel.cadReferenceSegmentEnd != null) {
            drawCadReferenceSegment(g2);
        }

        Coordinate previewCoordinate = panel.resolveInteractivePreviewCoordinate();
        if (previewCoordinate == null) {
            return;
        }

        Coordinate sourcePreview = panel.toSourceCoordinate(previewCoordinate.x, previewCoordinate.y, panel.selectedLayer);
        Geometry previewGeometry = null;
        Geometry selectedGeometry = panel.extractFeatureGeometryCopy(panel.selectedFeature);
        if (selectedGeometry == null || sourcePreview == null) {
            return;
        }

        if (panel.EDIT_OP_EXTEND_LINE.equals(panel.featureEditOperation)) {
            previewGeometry = panel.buildAdjustedSelectedLineGeometry(selectedGeometry, sourcePreview, true);
        } else if (panel.EDIT_OP_SHORTEN_LINE.equals(panel.featureEditOperation)) {
            previewGeometry = panel.buildAdjustedSelectedLineGeometry(selectedGeometry, sourcePreview, false);
        } else if (panel.EDIT_OP_PARALLEL.equals(panel.featureEditOperation) && panel.cadReferenceSegmentStart != null && panel.cadReferenceSegmentEnd != null) {
            previewGeometry = panel.buildParallelLineGeometry(panel.cadReferenceSegmentStart, panel.cadReferenceSegmentEnd, sourcePreview);
        } else if (panel.EDIT_OP_PERPENDICULAR.equals(panel.featureEditOperation) && panel.cadReferenceSegmentStart != null && panel.cadReferenceSegmentEnd != null) {
            previewGeometry = panel.buildPerpendicularLineGeometry(panel.cadReferenceSegmentStart, panel.cadReferenceSegmentEnd, sourcePreview);
        }

        if (previewGeometry == null || previewGeometry.isEmpty()) {
            return;
        }

        Geometry displayGeometry = panel.reprojectGeometryIfNeeded(panel.selectedLayer, previewGeometry);
        if (displayGeometry == null || displayGeometry.isEmpty()) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (displayGeometry instanceof LineString lineString) {
                drawLineString(copy, lineString, new Color(22, 163, 74), 2.6f);
            } else if (displayGeometry instanceof MultiLineString multiLineString) {
                for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                    Geometry child = multiLineString.getGeometryN(i);
                    if (child instanceof LineString lineString) {
                        drawLineString(copy, lineString, new Color(22, 163, 74), 2.6f);
                    }
                }
            }
        } finally {
            copy.dispose();
        }
    }

    public void drawSelectedLineEndpointHints(Graphics2D g2) {
        Geometry geometry = panel.extractFeatureGeometryCopy(panel.selectedFeature);
        Coordinate[] baseCoordinates = panel.extractContinuableLineCoordinates(geometry);
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            return;
        }

        Coordinate start = panel.toProjectCoordinate(baseCoordinates[0], panel.selectedLayer);
        Coordinate end = panel.toProjectCoordinate(baseCoordinates[baseCoordinates.length - 1], panel.selectedLayer);
        if (start == null || end == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawContinuationEndpointHint(copy, start, "A");
            drawContinuationEndpointHint(copy, end, "B");
        } finally {
            copy.dispose();
        }
    }

    public void drawCadReferenceSegment(Graphics2D g2) {
        if (panel.cadReferenceSegmentStart == null || panel.cadReferenceSegmentEnd == null || panel.selectedLayer == null) {
            return;
        }

        Coordinate displayStart = panel.toProjectCoordinate(panel.cadReferenceSegmentStart, panel.selectedLayer);
        Coordinate displayEnd = panel.toProjectCoordinate(panel.cadReferenceSegmentEnd, panel.selectedLayer);
        if (displayStart == null || displayEnd == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(new Color(59, 130, 246, 220));
            copy.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            copy.drawLine(
                    panel.worldToScreenX(displayStart.x),
                    panel.worldToScreenY(displayStart.y),
                    panel.worldToScreenX(displayEnd.x),
                    panel.worldToScreenY(displayEnd.y)
            );
        } finally {
            copy.dispose();
        }
    }

    public void drawSnapPreview(Graphics2D g2) {
        if (!panel.snapEnabled || panel.snapPreviewCoordinate == null || !(panel.isDrawingActive() || panel.isMeasurementActive() || panel.featureEditMode)) {
            return;
        }

        int x = panel.worldToScreenX(panel.snapPreviewCoordinate.x);
        int y = panel.worldToScreenY(panel.snapPreviewCoordinate.y);
        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(new Color(16, 185, 129, 52));
            copy.fillOval(x - 9, y - 9, 18, 18);
            copy.setColor(new Color(5, 150, 105, 220));
            copy.setStroke(new BasicStroke(2f));
            copy.drawOval(x - 7, y - 7, 14, 14);
            copy.drawLine(x - 10, y, x + 10, y);
            copy.drawLine(x, y - 10, x, y + 10);
            copy.setColor(Color.WHITE);
            copy.setStroke(new BasicStroke(1.2f));
            copy.drawOval(x - 3, y - 3, 6, 6);
        } finally {
            copy.dispose();
        }
    }

    public void drawLayer(Graphics2D g2, Layer layer, ShapefileData data) {
        if (data == null || data.getFeatureCollection() == null) {
            return;
        }

        float opacity = layer.getOpacity();
        Graphics2D vecG2 = opacity >= 1.0f ? g2 : (Graphics2D) g2.create();
        if (opacity < 1.0f) {
            vecG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, opacity)));
        }

        try {
            boolean editingLayer = panel.isLayerArmedForEditing(layer);
            panel.forEachVisibleFeatureGeometry(List.of(layer), "Error al dibujar la capa ", (currentLayer, featureGeometry) -> {
                if (editingLayer) {
                    drawGeometryForEditingLayer(vecG2, featureGeometry.geometry(), currentLayer);
                } else {
                    drawGeometry(vecG2, featureGeometry.geometry(), currentLayer, featureGeometry.feature());
                }
            });
        } finally {
            if (opacity < 1.0f) vecG2.dispose();
        }
    }

    public void drawGeometry(Graphics2D g2, Geometry geometry, Layer layer, SimpleFeature feature) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if ((geometry instanceof Point || geometry instanceof MultiPoint)
                && layer.isClusteringEnabled()) {
            // Individual points are suppressed when clustering is active
            return;
        }

        if (geometry instanceof Point) {
            drawStyledPoint(g2, (Point) geometry, layer, feature);
            drawLabelForFeature(g2, layer, feature, panel.worldToScreenX(((Point)geometry).getX()), panel.worldToScreenY(((Point)geometry).getY()));
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawStyledPoint(g2, (Point) g, layer, feature);
                    drawLabelForFeature(g2, layer, feature, panel.worldToScreenX(((Point)g).getX()), panel.worldToScreenY(((Point)g).getY()));
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawStyledLineString(g2, (LineString) geometry, layer, feature);
            // Label at line midpoint
            Coordinate[] lc = ((LineString)geometry).getCoordinates();
            if (lc.length >= 2) { int mi = lc.length / 2; drawLabelForFeature(g2, layer, feature, panel.worldToScreenX(lc[mi].x), panel.worldToScreenY(lc[mi].y)); }
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString ml = (MultiLineString) geometry;
            for (int i = 0; i < ml.getNumGeometries(); i++) {
                Geometry g = ml.getGeometryN(i);
                if (g instanceof LineString) {
                    drawStyledLineString(g2, (LineString) g, layer, feature);
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            drawStyledPolygon(g2, (Polygon) geometry, layer, feature);
            // Label at polygon interior point
            try {
                org.locationtech.jts.geom.Point ip = geometry.getInteriorPoint();
                if (ip != null) drawLabelForFeature(g2, layer, feature, panel.worldToScreenX(ip.getX()), panel.worldToScreenY(ip.getY()));
            } catch (Exception ignored) {}
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon) {
                    drawStyledPolygon(g2, (Polygon) g, layer, feature);
                }
            }
            return;
        }

        Point centroid = geometry.getCentroid();
        if (centroid != null) {
            drawPoint(g2, centroid, layer.getPointColor(), Math.max(4, layer.getPointSize() - 2));
        }
    }

    public void drawGeometryForEditingLayer(Graphics2D g2, Geometry geometry, Layer layer) {
        Color strongRed = new Color(220, 38, 38);
        Color softRed = new Color(248, 113, 113, 70);

        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, strongRed, Math.max(layer.getPointSize() + 1, 9));
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawPoint(g2, (Point) g, strongRed, Math.max(layer.getPointSize() + 1, 9));
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, strongRed, Math.max(layer.getLineWidth(), 2.2f));
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString ml = (MultiLineString) geometry;
            for (int i = 0; i < ml.getNumGeometries(); i++) {
                Geometry g = ml.getGeometryN(i);
                if (g instanceof LineString) {
                    drawLineString(g2, (LineString) g, strongRed, Math.max(layer.getLineWidth(), 2.2f));
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            drawPolygon(g2, (Polygon) geometry, softRed, strongRed, Math.max(layer.getLineWidth(), 2.2f));
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon) {
                    drawPolygon(g2, (Polygon) g, softRed, strongRed, Math.max(layer.getLineWidth(), 2.2f));
                }
            }
        }
    }

    public void drawLabelForFeature(Graphics2D g2, Layer layer, SimpleFeature feature, int x, int y) {
        // Labels are now rendered in batch via drawAllLabels() with collision detection.
        // This method is intentionally a no-op to avoid double rendering.
    }

    public void drawStyledPoint(Graphics2D g2, Point point, Layer layer, SimpleFeature feature) {
        int x = panel.worldToScreenX(point.getX());
        int y = panel.worldToScreenY(point.getY());
        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "point");
        int size = ar.com.catgis.LayerRenderHelper.resolveProportionalSize(layer, feature, categoryRule != null ? categoryRule.getPointSize() : layer.getPointSize());
        Color color = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getPointColor();

        // Use catalog symbol if available
        String catId = categoryRule != null ? categoryRule.getCatalogSymbolId() : layer.getCatalogSymbolId();
        if (catId != null && !catId.isEmpty() && !"circle".equals(catId)) {
            PointSymbolCatalog.render(g2, catId, x, y, size + 4, color, color.darker(), 1.2f);
            return;
        }

        if (categoryRule == null && PointGraphicSymbolSupport.paintLayerSymbol(g2, layer, x, y, Math.max(14, size + 6))) {
            return;
        }
        Layer.PointSymbolStyle style = categoryRule != null ? categoryRule.getPointSymbolStyle() : layer.getPointSymbolStyle();
        if (style == null) {
            style = Layer.PointSymbolStyle.CIRCLE;
        }
        PointSymbolRenderer.paint(g2, style, x, y, size, color, Color.BLACK);
    }

    public void drawStyledLineString(Graphics2D g2, LineString line, Layer layer, SimpleFeature feature) {
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) {
            return;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(panel.worldToScreenX(coords[0].x), panel.worldToScreenY(coords[0].y));
        for (int i = 1; i < coords.length; i++) {
            path.lineTo(panel.worldToScreenX(coords[i].x), panel.worldToScreenY(coords[i].y));
        }

        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "line");
        Color lineColor = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor();
        Layer.LineSymbolStyle lineStyle = categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle();
        float lineWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
        g2.setColor(lineColor);
        g2.setStroke(LineSymbolRenderer.buildStroke(lineStyle, lineWidth));
        g2.draw(path);
    }

    public void drawStyledPolygon(Graphics2D g2, Polygon polygon, Layer layer, SimpleFeature feature) {
        Path2D exteriorPath = panel.buildPathFromCoordinates(polygon.getExteriorRing().getCoordinates());
        if (exteriorPath == null) {
            return;
        }

        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "polygon");
        Paint oldPaint = g2.getPaint();
        Layer.PolygonFillStyle fillStyle = categoryRule != null ? categoryRule.getPolygonFillStyle() : layer.getPolygonFillStyle();
        Color fc = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor();
        Color bc = categoryRule != null ? (categoryRule.getSecondaryColor() != null ? categoryRule.getSecondaryColor() : fc.darker()) : layer.getLineColor();

        // Gradient fill support
        GradientFill gradient = layer.getGradientFill();
        if (gradient != null && categoryRule == null) {
            Paint gp = gradient.createPaint(exteriorPath.getBounds2D());
            g2.setPaint(gp);
            g2.fill(exteriorPath);
        } else {
            g2.setPaint(PolygonSymbolRenderer.buildPaint(fillStyle, fc, bc, 12));
            if (fillStyle != Layer.PolygonFillStyle.OUTLINE_ONLY) {
                g2.fill(exteriorPath);
            }
        }

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = panel.buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.setColor(panel.getBackground());
                g2.fill(holePath);
            }
        }

        g2.setPaint(oldPaint);
        Color borderColor = categoryRule != null && categoryRule.getSecondaryColor() != null
                ? categoryRule.getSecondaryColor()
                : layer.getBorderColor();
        float borderWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
        Layer.LineSymbolStyle borderStyle = categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle();
        g2.setColor(borderColor);
        g2.setStroke(LineSymbolRenderer.buildStroke(borderStyle, borderWidth));
        g2.draw(exteriorPath);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = panel.buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.draw(holePath);
            }
        }
    }

    public void drawPoint(Graphics2D g2, Point point, Color color, int size) {
        int x = panel.worldToScreenX(point.getX());
        int y = panel.worldToScreenY(point.getY());

        g2.setColor(color);
        g2.fillOval(x - size / 2, y - size / 2, size, size);

        g2.setColor(Color.BLACK);
        g2.drawOval(x - size / 2, y - size / 2, size, size);
    }

    public void drawLineString(Graphics2D g2, LineString line, Color color, float width) {
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) {
            return;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(panel.worldToScreenX(coords[0].x), panel.worldToScreenY(coords[0].y));

        for (int i = 1; i < coords.length; i++) {
            path.lineTo(panel.worldToScreenX(coords[i].x), panel.worldToScreenY(coords[i].y));
        }

        g2.setColor(color);
        g2.setStroke(new BasicStroke(width));
        g2.draw(path);
    }

    public void drawPolygon(Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth) {
        drawPolygon(g2, polygon, fillColor, borderColor, borderWidth, null);
    }

    public void drawPolygon(Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth, GradientFill gradientFill) {
        Path2D exteriorPath = panel.buildPathFromCoordinates(polygon.getExteriorRing().getCoordinates());
        if (exteriorPath == null) {
            return;
        }

        if (gradientFill != null) {
            Rectangle2D bounds = exteriorPath.getBounds2D();
            Paint gp = gradientFill.createPaint(bounds);
            g2.setPaint(gp);
        } else {
            g2.setColor(fillColor);
        }
        g2.fill(exteriorPath);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = panel.buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.setColor(panel.getBackground());
                g2.fill(holePath);
            }
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.draw(exteriorPath);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = panel.buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.draw(holePath);
            }
        }
    }

    public void drawSelectedFeature(Graphics2D g2, SimpleFeature feature, Layer layer) {
        if (!panel.isFeatureVisibleInLayer(layer, feature)) {
            return;
        }
        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry geometry = panel.reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        boolean editing = panel.featureEditMode && layer == panel.selectedLayer && feature == panel.selectedFeature;
        Color haloColor = editing ? new Color(185, 28, 28, 210) : new Color(0, 170, 255, 180);
        Color selectionColor = editing ? new Color(239, 68, 68) : Color.YELLOW;

        if (geometry instanceof Point) {
            drawPoint(g2, (Point) geometry, haloColor, 18);
            drawPoint(g2, (Point) geometry, selectionColor, 10);
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawPoint(g2, (Point) g, haloColor, 18);
                    drawPoint(g2, (Point) g, selectionColor, 10);
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawLineString(g2, (LineString) geometry, haloColor, 6f);
            drawLineString(g2, (LineString) geometry, selectionColor, 3f);
            return;
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString ml = (MultiLineString) geometry;
            for (int i = 0; i < ml.getNumGeometries(); i++) {
                Geometry g = ml.getGeometryN(i);
                if (g instanceof LineString) {
                    drawLineString(g2, (LineString) g, haloColor, 6f);
                    drawLineString(g2, (LineString) g, selectionColor, 3f);
                }
            }
            return;
        }

        if (geometry instanceof Polygon) {
            Color outerFill = editing ? new Color(248, 113, 113, 46) : new Color(0, 170, 255, 45);
            Color innerFill = editing ? new Color(254, 202, 202, 74) : new Color(255, 255, 0, 65);
            drawPolygon(g2, (Polygon) geometry, outerFill, haloColor, 5f);
            drawPolygon(g2, (Polygon) geometry, innerFill, selectionColor, 2.5f);
            return;
        }

        if (geometry instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon) {
                    Color outerFill = editing ? new Color(248, 113, 113, 46) : new Color(0, 170, 255, 45);
                    Color innerFill = editing ? new Color(254, 202, 202, 74) : new Color(255, 255, 0, 65);
                    drawPolygon(g2, (Polygon) g, outerFill, haloColor, 5f);
                    drawPolygon(g2, (Polygon) g, innerFill, selectionColor, 2.5f);
                }
            }
        }
    }

    public void drawAttributeTableSelections(Graphics2D g2) {
        if (panel.tableSelectionIds.isEmpty()) {
            return;
        }

        for (Map.Entry<Layer, List<String>> entry : panel.tableSelectionIds.entrySet()) {
            Layer layer = entry.getKey();
            if (layer == null || !panel.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = panel.getShapefileData(layer);
            if (data == null || data.getFeatures() == null) {
                continue;
            }

            List<String> ids = entry.getValue();
            if (ids == null || ids.isEmpty()) {
                continue;
            }

            for (String featureId : ids) {
                SimpleFeature feature = panel.findFeatureById(data.getFeatures(), featureId);
                if (feature == null) {
                    continue;
                }
                if (layer == panel.selectedLayer && panel.selectedFeature != null && panel.sameFeatureId(panel.selectedFeature, featureId)) {
                    continue;
                }
                drawSelectedFeature(g2, feature, layer);
            }
        }
    }

    public void drawEditableVertices(Graphics2D g2, SimpleFeature feature, Layer layer) {
        Geometry geometry = panel.getEditableDisplayGeometry(feature, layer);
        if (geometry == null) {
            return;
        }

        Coordinate[] vertices = panel.getEditableVertexCoordinates(geometry);
        if (vertices == null || vertices.length == 0) {
            return;
        }

        for (int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            if (c == null) {
                continue;
            }

            int x = panel.worldToScreenX(c.x);
            int y = panel.worldToScreenY(c.y);
            boolean activeMoveVertex = i == panel.activeEditVertexIndex;
            boolean joinTarget = panel.EDIT_OP_JOIN_VERTEX.equals(panel.featureEditOperation) && i == panel.joinTargetVertexIndex;
            int size = activeMoveVertex ? 12 : (joinTarget ? 14 : 10);

            if (activeMoveVertex) {
                g2.setColor(new Color(255, 102, 0, 240));
            } else if (joinTarget) {
                g2.setColor(new Color(37, 99, 235, 235));
            } else {
                g2.setColor(new Color(220, 38, 38, 220));
            }
            g2.fillOval(x - size / 2, y - size / 2, size, size);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(x - size / 2, y - size / 2, size, size);
        }
    }

    public void drawAllLabels(Graphics2D g2) {
        panel.globalLabelBoxes.clear();
        Object prevHint = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Layer layer : panel.getRenderOrderLayers()) {
            if (layer == null || !panel.isLayerEffectivelyVisible(layer)) continue;
            if (!layer.isLabelsVisible()) continue;
            if (layer.getLabelField() == null || layer.getLabelField().isBlank()) continue;
            if (!layer.isLabelVisibleAtScale(panel.getCurrentScaleDenominator())) continue;

            ShapefileData shapeData = panel.shapefileLayers.get(layer);
            if (shapeData == null) continue;
            SimpleFeatureCollection collection = shapeData.getFeatureCollection();
            if (collection == null) continue;

            // Resolve expression or field for label text
            String labelExpr = layer.getLabelExpression();
            boolean useExpression = (labelExpr != null && !labelExpr.isBlank());
            String labelField = useExpression ? null : layer.getLabelField();

            // Collect all label candidates for this layer
            List<Object[]> candidates = new ArrayList<>();
            panel.forEachVisibleFeatureGeometry(List.of(layer), "", (currentLayer, featureGeometry) -> {
                String text;
                if (useExpression) {
                    text = LabelExpressionEngine.evaluate(labelExpr, featureGeometry.feature());
                } else {
                    Object attrValue = featureGeometry.feature().getAttribute(labelField);
                    text = attrValue != null ? String.valueOf(attrValue).trim() : "";
                }
                if (text == null || text.isEmpty()) return;

                Coordinate coord = panel.getLabelCoordinate(featureGeometry.geometry());
                if (coord == null) return;

                int sx = panel.worldToScreenX(coord.x);
                int sy = panel.worldToScreenY(coord.y);
                String geomType = LabelPlacementEngine.resolveGeometryType(featureGeometry.geometry().getClass());
                candidates.add(new Object[]{text, sx, sy, geomType, currentLayer.getLabelPriority()});
            });

            if (candidates.isEmpty()) continue;

            // Resolve placements with collision detection
            List<LabelPlacementEngine.ResolvedLabel> resolved =
                    LabelPlacementEngine.resolveLabels(g2, layer, candidates, panel.globalLabelBoxes);

            // Render resolved labels
            for (LabelPlacementEngine.ResolvedLabel rl : resolved) {
                drawResolvedLabel(g2, rl);
            }
        }

        if (prevHint != null) g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, prevHint);
    }

    public void drawResolvedLabel(Graphics2D g2, LabelPlacementEngine.ResolvedLabel rl) {
        Layer layer = rl.layer();
        Font font;
        Color textColor = Color.BLACK;
        Color haloColor = Color.WHITE;
        float haloWidth = 2f;
        boolean haloEnabled = true;
        boolean underline = false;
        boolean bgEnabled = false;
        Color bgColor = new Color(255, 255, 255, 180);

        if (layer != null) {
            int style = Font.PLAIN;
            if (layer.isLabelBold()) style |= Font.BOLD;
            if (layer.isLabelItalic()) style |= Font.ITALIC;
            font = new Font(layer.getLabelFontFamily(), style, layer.getLabelFontSize());
            textColor = layer.getLabelColor();
            haloColor = layer.getLabelHaloColor();
            haloWidth = layer.getLabelHaloWidth();
            haloEnabled = layer.isLabelHaloEnabled();
            underline = layer.isLabelUnderline();
            bgEnabled = layer.isLabelBackgroundEnabled();
            bgColor = layer.getLabelBackgroundColor();
        } else {
            font = g2.getFont();
        }

        g2.setFont(font);
        int lx = rl.drawX();
        int ly = rl.drawY();
        int tw = rl.textWidth();
        int th = rl.textHeight();
        FontMetrics fm = g2.getFontMetrics();

        // Background
        if (bgEnabled && bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(lx - 4, ly - fm.getAscent() - 2, tw + 8, th + 4, 6, 6);
        }

        // Halo
        if (haloEnabled && haloColor.getAlpha() > 0) {
            g2.setColor(haloColor);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(rl.text(), lx + dx * haloWidth, ly + dy * haloWidth);
        }

        // Text
        g2.setColor(textColor);
        g2.drawString(rl.text(), lx, ly);

        // Underline
        if (underline) {
            g2.setColor(textColor);
            g2.drawLine(lx, ly + 2, lx + tw, ly + 2);
        }
    }

    public void drawLabels(Graphics2D g2, Layer layer, ShapefileData data) {
        if (layer == null || data == null) return;
        if (!panel.isLayerEffectivelyVisible(layer)) return;
        if (!layer.isLabelsVisible()) return;
        if (!layer.isLabelVisibleAtScale(panel.getCurrentScaleDenominator())) return;

        String labelExpr = layer.getLabelExpression();
        boolean useExpression = (labelExpr != null && !labelExpr.isBlank());
        String labelField = useExpression ? null : layer.getLabelField();

        if (!useExpression && (labelField == null || labelField.isBlank())) return;

        SimpleFeatureCollection collection = data.getFeatureCollection();
        if (collection == null) return;

        Object prevHint = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        panel.forEachVisibleFeatureGeometry(List.of(layer), "Error al dibujar etiquetas para la capa ", (currentLayer, featureGeometry) -> {
            String text;
            if (useExpression) {
                text = LabelExpressionEngine.evaluate(labelExpr, featureGeometry.feature());
            } else {
                Object attrValue = featureGeometry.feature().getAttribute(labelField);
                text = attrValue != null ? String.valueOf(attrValue).trim() : "";
            }
            if (text == null || text.isEmpty()) return;

            Coordinate labelCoordinate = panel.getLabelCoordinate(featureGeometry.geometry());
            if (labelCoordinate == null) return;

            int x = panel.worldToScreenX(labelCoordinate.x);
            int y = panel.worldToScreenY(labelCoordinate.y);
            drawLabelWithSettings(g2, text, x, y, currentLayer);
        });

        if (prevHint != null) g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, prevHint);
    }

    public void drawTextWithHalo(Graphics2D g2, String text, int x, int y) {
        drawLabelWithSettings(g2, text, x, y, null);
    }

    public void drawLabelWithSettings(Graphics2D g2, String text, int x, int y, Layer layer) {
        Font font;
        Color textColor = Color.BLACK;
        Color haloColor = Color.WHITE;
        float haloWidth = 2f;
        boolean haloEnabled = true;
        boolean underline = false;
        boolean bgEnabled = false;
        Color bgColor = new Color(255, 255, 255, 180);
        int offX = 0, offY = 0;

        if (layer != null) {
            int style = Font.PLAIN;
            if (layer.isLabelBold()) style |= Font.BOLD;
            if (layer.isLabelItalic()) style |= Font.ITALIC;
            font = new Font(layer.getLabelFontFamily(), style, layer.getLabelFontSize());
            textColor = layer.getLabelColor();
            haloColor = layer.getLabelHaloColor();
            haloWidth = layer.getLabelHaloWidth();
            haloEnabled = layer.isLabelHaloEnabled();
            underline = layer.isLabelUnderline();
            bgEnabled = layer.isLabelBackgroundEnabled();
            bgColor = layer.getLabelBackgroundColor();
            offX = layer.getLabelOffsetX();
            offY = layer.getLabelOffsetY();
        } else {
            font = g2.getFont();
        }

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getHeight();
        int drawX = x + offX - tw / 2;
        int drawY = y + offY;

        // Background
        if (bgEnabled && bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(drawX - 4, drawY - fm.getAscent() - 2, tw + 8, th + 4, 6, 6);
        }

        // Halo
        if (haloEnabled && haloColor.getAlpha() > 0) {
            g2.setColor(haloColor);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(text, drawX + dx * haloWidth, drawY + dy * haloWidth);
        }

        // Text
        g2.setColor(textColor);
        g2.drawString(text, drawX, drawY);

        // Underline
        if (underline) {
            g2.setColor(textColor);
            g2.drawLine(drawX, drawY + 2, drawX + tw, drawY + 2);
        }
    }

    public void drawTemporaryGeometry(Graphics2D g2, List<Coordinate> tempCoords, String mode, Color lineColor, Color fillColor) {
        if (tempCoords.isEmpty()) {
            return;
        }

        if (tempCoords.size() < 2) {
            Coordinate c = tempCoords.get(0);
            int x = panel.worldToScreenX(c.x);
            int y = panel.worldToScreenY(c.y);
            g2.setColor(lineColor);
            g2.fillOval(x - 4, y - 4, 8, 8);
            return;
        }

        Path2D path = new Path2D.Double();
        Coordinate first = tempCoords.get(0);
        path.moveTo(panel.worldToScreenX(first.x), panel.worldToScreenY(first.y));

        for (int i = 1; i < tempCoords.size(); i++) {
            Coordinate c = tempCoords.get(i);
            path.lineTo(panel.worldToScreenX(c.x), panel.worldToScreenY(c.y));
        }

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(lineColor);

        if (("POLYGON".equalsIgnoreCase(mode) || "AREA".equalsIgnoreCase(mode)) && tempCoords.size() >= 3) {
            g2.setColor(fillColor);
            g2.fill(path);
            g2.setColor(lineColor);
        }

        g2.draw(path);

        for (Coordinate c : tempCoords) {
            int x = panel.worldToScreenX(c.x);
            int y = panel.worldToScreenY(c.y);
            g2.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    public void drawOnlineResolutionNotice(Graphics2D g2) {
        if (!panel.onlineResolutionNoticeVisible || panel.onlineResolutionNotice == null || panel.onlineResolutionNotice.isBlank()) {
            return;
        }

        Font font = g2.getFont().deriveFont(Font.BOLD, 11f);
        FontMetrics metrics = g2.getFontMetrics(font);
        int padding = 8;
        int textWidth = metrics.stringWidth(panel.onlineResolutionNotice);
        int textHeight = metrics.getHeight();
        int boxWidth = textWidth + padding * 2;
        int boxHeight = textHeight + 6;
        int x = 10;
        int y = 10;

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setFont(font);
            copy.setColor(new Color(255, 248, 230, 230));
            copy.fillRoundRect(x, y, boxWidth, boxHeight, 12, 12);
            copy.setColor(new Color(196, 138, 17, 220));
            copy.drawRoundRect(x, y, boxWidth, boxHeight, 12, 12);
            copy.setColor(new Color(120, 53, 15));
            copy.drawString(panel.onlineResolutionNotice, x + padding, y + metrics.getAscent() + 3);
        } finally {
            copy.dispose();
        }
    }

    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor, int renderWidth, int renderHeight, boolean includeDecorations) {
        if (renderZoomFactor <= 0) {
            return null;
        }

        renderWidth = Math.max(1, renderWidth);
        renderHeight = Math.max(1, renderHeight);

        double oldViewMinX = panel.viewMinX;
        double oldViewMinY = panel.viewMinY;
        double oldZoomFactor = panel.zoomFactor;
        boolean oldLayoutRenderMode = panel.layoutRenderMode;
        int oldWidth = panel.getWidth();
        int oldHeight = panel.getHeight();

        BufferedImage image = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            if (oldWidth <= 1 || oldHeight <= 1) {
                panel.setSize(renderWidth, renderHeight);
            }
            panel.viewMinX = renderViewMinX;
            panel.viewMinY = renderViewMinY;
            panel.zoomFactor = renderZoomFactor;
            panel.layoutRenderMode = !includeDecorations;
            render(g2, renderWidth, renderHeight);
            if (includeDecorations && panel.mapDecorations != null) {
                String crsDesc = CatgisDesktopApp.currentProject != null
                        ? CatgisDesktopApp.currentProject.getProjectCRS() : "";
                panel.mapDecorations.render(g2, renderWidth, renderHeight,
                        panel.getCurrentViewEnvelope(),
                        panel.getCurrentScaleDenominator(),
                        crsDesc);
            }
            return image;
        } finally {
            g2.dispose();
            panel.viewMinX = oldViewMinX;
            panel.viewMinY = oldViewMinY;
            panel.zoomFactor = oldZoomFactor;
            panel.layoutRenderMode = oldLayoutRenderMode;
            if (oldWidth <= 1 || oldHeight <= 1) {
                panel.setSize(oldWidth, oldHeight);
            }
        }
    }

    public BufferedImage renderMapViewImage(double renderViewMinX, double renderViewMinY, double renderZoomFactor) {
        int renderWidth = Math.max(1, panel.getWidth());
        int renderHeight = Math.max(1, panel.getHeight());
        if (renderWidth <= 1 || renderHeight <= 1) {
            renderWidth = 1200;
            renderHeight = 800;
        }
        return renderMapViewImage(renderViewMinX, renderViewMinY, renderZoomFactor, renderWidth, renderHeight, false);
    }

    public void writeWorldFile(File imageFile) throws Exception {
        MapRenderer.writeWorldFile(imageFile, panel.viewMinX, panel.viewMinY, panel.zoomFactor, panel.getWidth(), panel.getHeight());
    }
    public static void writeWorldFile(File imageFile, double viewMinX, double viewMinY,
                                       double zoomFactor, int imgWidth, int imgHeight) throws Exception {
        if (zoomFactor <= 0 || imgWidth <= 0 || imgHeight <= 0) return;

        double pixelSizeX = 1.0 / zoomFactor;
        double pixelSizeY = pixelSizeX;
        double upperLeftX = viewMinX;
        double upperLeftY = viewMinY + imgHeight * pixelSizeY;

        String name = imageFile.getName().toLowerCase();
        String ext;
        if (name.endsWith(".tif") || name.endsWith(".tiff")) ext = ".tfw";
        else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) ext = ".jgw";
        else if (name.endsWith(".png")) ext = ".pgw";
        else ext = ".wld";

        File wf = new File(imageFile.getParent(), getBaseName(imageFile.getName()) + ext);
        try (java.io.PrintWriter pw = new java.io.PrintWriter(wf, "UTF-8")) {
            pw.println(java.lang.String.format(java.util.Locale.US, "%.15f", pixelSizeX));
            pw.println("0.0");
            pw.println("0.0");
            pw.println(java.lang.String.format(java.util.Locale.US, "%.15f", -pixelSizeY));
            pw.println(java.lang.String.format(java.util.Locale.US, "%.15f", upperLeftX));
            pw.println(java.lang.String.format(java.util.Locale.US, "%.15f", upperLeftY));
        }
    }

    public static String getBaseName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
