package ar.com.catgis;

import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineTileCache;
import ar.com.catgis.data.online.OnlineWmsLayer;
import ar.com.catgis.OnlineWmsImageCache;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.GradientFill;
import ar.com.catgis.data.vector.ShapefileData;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class OnlineLayerRenderer {

    private OnlineLayerRenderer() {}

    public static void drawOnlineTileLayer(MapPanel panel, Graphics2D g2, OnlineTileLayer layer, OnlineRasterSource source) {
        if (panel == null || layer == null || source == null || panel.getWidth() <= 0 || panel.getHeight() <= 0) {
            return;
        }

        String projectCRS = AppContext.project() != null ? AppContext.project().getProjectCRS() : "";
        if (projectCRS == null || projectCRS.isBlank()) {
            return;
        }

        Envelope projectView = new Envelope(
                panel.screenToWorldX(0),
                panel.screenToWorldX(panel.getWidth()),
                panel.screenToWorldY(panel.getHeight()),
                panel.screenToWorldY(0)
        );

        Envelope mercatorView = projectEnvelopeToMercator(panel, projectView, projectCRS);
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
                        MapPanel.OnlineTileFallback fallback = resolveFallbackOnlineTile(panel, source, zoom, tx, ty);
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

    public static MapPanel.OnlineTileFallback resolveFallbackOnlineTile(MapPanel panel, OnlineRasterSource source, int zoom, int x, int y) {
        if (source == null) {
            return null;
        }
        for (int fallbackZoom = zoom - 1; fallbackZoom >= source.getMinZoom(); fallbackZoom--) {
            int delta = zoom - fallbackZoom;
            int parentX = x >> delta;
            int parentY = y >> delta;
            BufferedImage parentTile = OnlineTileCache.getTile(source, fallbackZoom, parentX, parentY, panel != null ? panel::repaint : () -> {});
            if (parentTile == null) {
                continue;
            }

            int divisions = 1 << delta;
            int childX = x - (parentX << delta);
            int childY = y - (parentY << delta);
            int srcX = childX * parentTile.getWidth() / divisions;
            int srcY = childY * parentTile.getHeight() / divisions;
            int srcW = Math.max(1, parentTile.getWidth() / divisions);
            int srcH = Math.max(1, parentTile.getHeight() / divisions);

            srcW = Math.min(srcW, parentTile.getWidth() - srcX);
            srcH = Math.min(srcH, parentTile.getHeight() - srcY);
            if (srcW <= 0 || srcH <= 0) {
                continue;
            }
            return new MapPanel.OnlineTileFallback(parentTile, srcX, srcY, srcW, srcH);
        }
        return null;
    }

    public static void drawOnlineWmsLayer(MapPanel panel, Graphics2D g2, OnlineWmsLayer layer) {
        if (panel == null || layer == null || panel.getWidth() <= 8 || panel.getHeight() <= 8) {
            return;
        }

        String requestUrl = buildWmsGetMapUrl(panel, layer);
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

    public static String buildWmsGetMapUrl(MapPanel panel, OnlineWmsLayer layer) {
        String serviceUrl = layer.getServiceUrl();
        String layerNames = layer.getLayerNames();
        if (serviceUrl == null || serviceUrl.isBlank() || layerNames == null || layerNames.isBlank()) {
            return null;
        }

        String projectCrs = AppContext.project() != null ? AppContext.project().getProjectCRS() : "";
        String requestCrs = layer.getRequestCrs() != null && !layer.getRequestCrs().isBlank()
                ? layer.getRequestCrs()
                : (projectCrs != null && !projectCrs.isBlank() ? projectCrs : "EPSG:3857");

        Envelope projectView = new Envelope(
                panel.screenToWorldX(0),
                panel.screenToWorldX(panel.getWidth()),
                panel.screenToWorldY(panel.getHeight()),
                panel.screenToWorldY(0)
        );
        Envelope requestEnvelope = projectEnvelopeToCrs(panel, projectView, projectCrs, requestCrs);
        if (requestEnvelope == null || requestEnvelope.isNull()) {
            return null;
        }

        String version = layer.getVersion() != null && !layer.getVersion().isBlank() ? layer.getVersion() : "1.3.0";
        String bbox = buildWmsBbox(requestEnvelope, requestCrs, version);
        String crsParam = version.startsWith("1.3") ? "CRS" : "SRS";

        StringBuilder sb = new StringBuilder();
        sb.append(serviceUrl.trim());
        sb.append(serviceUrl.contains("?") ? (serviceUrl.endsWith("?") || serviceUrl.endsWith("&") ? "" : "&") : "?");
        sb.append("SERVICE=WMS");
        sb.append("&REQUEST=GetMap");
        sb.append("&VERSION=").append(urlEncode(version));
        sb.append("&LAYERS=").append(urlEncode(layerNames));
        sb.append("&STYLES=").append(urlEncode(layer.getStyleNames() != null ? layer.getStyleNames() : ""));
        sb.append("&FORMAT=").append(urlEncode(layer.getImageFormat() != null && !layer.getImageFormat().isBlank() ? layer.getImageFormat() : "image/png"));
        sb.append("&TRANSPARENT=").append(layer.isTransparent() ? "TRUE" : "FALSE");
        sb.append("&WIDTH=").append(Math.max(64, panel.getWidth()));
        sb.append("&HEIGHT=").append(Math.max(64, panel.getHeight()));
        sb.append("&").append(crsParam).append("=").append(urlEncode(requestCrs));
        sb.append("&BBOX=").append(urlEncode(bbox));
        return sb.toString();
    }

    public static Envelope projectEnvelopeToCrs(MapPanel panel, Envelope projectEnvelope, String sourceCrs, String targetCrs) {
        if (projectEnvelope == null || projectEnvelope.isNull()) {
            return null;
        }
        if (sourceCrs == null || sourceCrs.isBlank() || targetCrs == null || targetCrs.isBlank()) {
            return new Envelope(projectEnvelope);
        }
        if (sourceCrs.equalsIgnoreCase(targetCrs)) {
            return new Envelope(projectEnvelope);
        }

        double[][] corners = new double[][]{
                {projectEnvelope.getMinX(), projectEnvelope.getMinY()},
                {projectEnvelope.getMinX(), projectEnvelope.getMaxY()},
                {projectEnvelope.getMaxX(), projectEnvelope.getMinY()},
                {projectEnvelope.getMaxX(), projectEnvelope.getMaxY()}
        };

        Envelope transformed = null;
        for (double[] corner : corners) {
            double[] out = panel.transformPoint(corner[0], corner[1], sourceCrs, targetCrs);
            if (out == null || out.length < 2 || Double.isNaN(out[0]) || Double.isNaN(out[1])) {
                continue;
            }
            if (transformed == null) {
                transformed = new Envelope(out[0], out[0], out[1], out[1]);
            } else {
                transformed.expandToInclude(out[0], out[1]);
            }
        }
        return transformed;
    }

    public static String buildWmsBbox(Envelope env, String requestCrs, String version) {
        if (env == null) {
            return "";
        }
        boolean latLon130 = version != null && version.startsWith("1.3")
                && requestCrs != null
                && ("EPSG:4326".equalsIgnoreCase(requestCrs) || "CRS:84".equalsIgnoreCase(requestCrs));

        if (latLon130 && "EPSG:4326".equalsIgnoreCase(requestCrs)) {
            return formatNumberForRequest(env.getMinY()) + "," + formatNumberForRequest(env.getMinX()) + ","
                    + formatNumberForRequest(env.getMaxY()) + "," + formatNumberForRequest(env.getMaxX());
        }

        return formatNumberForRequest(env.getMinX()) + "," + formatNumberForRequest(env.getMinY()) + ","
                + formatNumberForRequest(env.getMaxX()) + "," + formatNumberForRequest(env.getMaxY());
    }

    public static String formatNumberForRequest(double value) {
        return String.format(Locale.US, "%.8f", value);
    }

    public static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text != null ? text : "", StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return text != null ? text : "";
        }
    }

    public static Envelope projectEnvelopeToMercator(MapPanel panel, Envelope projectEnvelope, String projectCRS) {
        if (projectEnvelope == null || projectEnvelope.isNull() || projectCRS == null || projectCRS.isBlank()) {
            return null;
        }

        double[][] corners = new double[][]{
                {projectEnvelope.getMinX(), projectEnvelope.getMinY()},
                {projectEnvelope.getMinX(), projectEnvelope.getMaxY()},
                {projectEnvelope.getMaxX(), projectEnvelope.getMinY()},
                {projectEnvelope.getMaxX(), projectEnvelope.getMaxY()}
        };

        Envelope mercator = null;
        for (double[] corner : corners) {
            double[] transformed = panel.transformPoint(corner[0], corner[1], projectCRS, "EPSG:3857");
            if (transformed == null || transformed.length < 2
                    || Double.isNaN(transformed[0]) || Double.isNaN(transformed[1])
                    || Double.isInfinite(transformed[0]) || Double.isInfinite(transformed[1])) {
                continue;
            }
            if (mercator == null) {
                mercator = new Envelope(transformed[0], transformed[0], transformed[1], transformed[1]);
            } else {
                mercator.expandToInclude(transformed[0], transformed[1]);
            }
        }

        return OnlineMapUtils.clampToWorld(mercator);
    }

    public static void drawOnlineAttribution(MapPanel panel, Graphics2D g2) {
        String attribution = buildVisibleOnlineAttribution(panel);
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

    public static String buildVisibleOnlineAttribution(MapPanel panel) {
        LinkedHashSet<String> parts = new LinkedHashSet<>();
        for (Map.Entry<Layer, OnlineRasterSource> entry : panel.onlineTileLayers.entrySet()) {
            Layer layer = entry.getKey();
            OnlineRasterSource source = entry.getValue();
            if (layer == null || source == null || !panel.layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }
            if (source.getAttribution() != null && !source.getAttribution().isBlank()) {
                parts.add(source.getAttribution().trim());
            }
        }
        for (Map.Entry<Layer, OnlineWmsLayer> entry : panel.onlineWmsLayers.entrySet()) {
            Layer layer = entry.getKey();
            OnlineWmsLayer wms = entry.getValue();
            if (layer == null || wms == null || !panel.layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }
            if (wms.getAttribution() != null && !wms.getAttribution().isBlank()) {
                parts.add(wms.getAttribution().trim());
            }
        }
        return String.join(" | ", parts);
    }

    public static void pushTileStatusToBar(MapPanel panel) {
        if (panel.layoutRenderMode) {
            return;
        }
        if (panel.onlineResolutionNotice != null && !panel.onlineResolutionNotice.isBlank()
                && CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(panel.onlineResolutionNotice);
        }
    }

    public static void drawPoint(MapPanel panel, Graphics2D g2, Point point, Color color, int size) {
        int x = panel.worldToScreenX(point.getX());
        int y = panel.worldToScreenY(point.getY());

        g2.setColor(color);
        g2.fillOval(x - size / 2, y - size / 2, size, size);

        g2.setColor(Color.BLACK);
        g2.drawOval(x - size / 2, y - size / 2, size, size);
    }

    public static void drawLineString(MapPanel panel, Graphics2D g2, LineString line, Color color, float width) {
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

    public static void drawPolygon(MapPanel panel, Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth) {
        drawPolygon(panel, g2, polygon, fillColor, borderColor, borderWidth, null);
    }

    public static void drawPolygon(MapPanel panel, Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth, GradientFill gradientFill) {
        Path2D exteriorPath = buildPathFromCoordinates(panel, polygon.getExteriorRing().getCoordinates());
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
            Path2D holePath = buildPathFromCoordinates(panel, polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.setColor(panel.getBackground());
                g2.fill(holePath);
            }
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.draw(exteriorPath);

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            Path2D holePath = buildPathFromCoordinates(panel, polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.draw(holePath);
            }
        }
    }

    public static Path2D buildPathFromCoordinates(MapPanel panel, Coordinate[] coords) {
        if (coords == null || coords.length == 0) {
            return null;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(panel.worldToScreenX(coords[0].x), panel.worldToScreenY(coords[0].y));

        for (int i = 1; i < coords.length; i++) {
            path.lineTo(panel.worldToScreenX(coords[i].x), panel.worldToScreenY(coords[i].y));
        }

        path.closePath();
        return path;
    }

    public static void drawTemporaryGeometry(MapPanel panel, Graphics2D g2, List<Coordinate> tempCoords, String mode, Color lineColor, Color fillColor) {
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

    public static void drawHeatmapOverlay(MapPanel panel, Graphics2D g2) {
        if (panel.shapefileLayers.isEmpty()) return;

        java.util.List<java.awt.geom.Point2D> allPoints = new java.util.ArrayList<>();
        int radius = 30;
        float opacity = 0.6f;
        boolean hasHeatmap = false;

        for (Layer layer : panel.layerManager.getRenderOrderLayers()) {
            if (layer == null || !panel.layerManager.isLayerEffectivelyVisible(layer)) continue;
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
            } catch (Exception ignored) { CatgisLogger.warn("OnlineLayerRenderer: operation failed", ignored); }
        }

        if (!hasHeatmap || allPoints.isEmpty()) return;

        int w = Math.max(1, panel.getWidth());
        int h = Math.max(1, panel.getHeight());
        BufferedImage heatmap = HeatmapRenderer.renderHeatmap(allPoints, w, h, radius, opacity);
        if (heatmap != null) {
            g2.drawImage(heatmap, 0, 0, null);
        }
    }

    public static void drawPointClusters(MapPanel panel, Graphics2D g2) {
        if (panel.shapefileLayers.isEmpty()) return;

        java.util.List<java.awt.geom.Point2D> allPoints = new java.util.ArrayList<>();
        int radius = 30;
        boolean hasClustering = false;

        for (Layer layer : panel.layerManager.getRenderOrderLayers()) {
            if (layer == null || !panel.layerManager.isLayerEffectivelyVisible(layer)) continue;
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
            } catch (Exception ignored) { CatgisLogger.warn("OnlineLayerRenderer: operation failed", ignored); }
        }

        if (!hasClustering || allPoints.isEmpty()) return;

        var clusters = PointClusterRenderer.clusterPoints(allPoints, radius);
        int w = Math.max(1, panel.getWidth());
        int h = Math.max(1, panel.getHeight());
        BufferedImage clusterImg = PointClusterRenderer.renderClusters(clusters, w, h);
        if (clusterImg != null) g2.drawImage(clusterImg, 0, 0, null);
    }
}
