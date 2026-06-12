package ar.com.catgis.layout;

import ar.com.catgis.CatgisLogger;
import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.Project;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.renderer.PolygonSymbolRenderer;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.renderer.labels.LabelExpressionEngine;
import ar.com.catgis.renderer.labels.LabelPlacementEngine;
import ar.com.catgis.data.online.OnlineWmsLayer;

import ar.com.catgis.*;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.FeatureIterator;
import org.geotools.data.simple.SimpleFeatureCollection;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Independent map frame renderer that renders directly from project layers.
 * Does NOT depend on MapPanel for rendering — uses project data directly.
 */
public class MapFrameRenderer {

    private final MapFrameViewport viewport;
    private final java.util.List<int[]> collisionBoxes = new ArrayList<>();
    private MapFrameViewport indicatorExtent; // For inset maps: shows where the main map is

    public MapFrameRenderer(MapFrameViewport viewport) {
        this.viewport = viewport;
    }

    /**
     * Set the indicator extent for inset maps.
     * This draws a rectangle showing where the main map's area is within this inset.
     */
    public void setIndicatorExtent(MapFrameViewport extent) {
        this.indicatorExtent = extent;
    }

    private void renderIndicatorRectangle(Graphics2D g2, int widthPx, int heightPx) {
        if (indicatorExtent == null) return;

        // Convert indicator extent to screen coordinates
        int ix1 = worldToScreenX(indicatorExtent.getMinX(), widthPx);
        int iy1 = worldToScreenY(indicatorExtent.getMaxY(), heightPx);
        int ix2 = worldToScreenX(indicatorExtent.getMaxX(), widthPx);
        int iy2 = worldToScreenY(indicatorExtent.getMinY(), heightPx);

        // Clamp to image bounds
        ix1 = Math.max(0, Math.min(widthPx, ix1));
        iy1 = Math.max(0, Math.min(heightPx, iy1));
        ix2 = Math.max(0, Math.min(widthPx, ix2));
        iy2 = Math.max(0, Math.min(heightPx, iy2));

        if (ix2 <= ix1 || iy2 <= iy1) return;

        // Draw semi-transparent fill
        g2.setColor(new Color(255, 0, 0, 30));
        g2.fillRect(ix1, iy1, ix2 - ix1, iy2 - iy1);

        // Draw border
        g2.setColor(new Color(220, 50, 50, 200));
        g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.drawRect(ix1, iy1, ix2 - ix1, iy2 - iy1);
    }

    /**
     * Render the map frame content at the given pixel dimensions.
     * This is the core render method that draws layers directly.
     */
    public BufferedImage render(int widthPx, int heightPx, int dpi) {
        if (widthPx < 10 || heightPx < 10) return null;

        Project project = AppContext.project();
        if (project == null) return null;

        // Check if there are online/raster layers — MapFrameRenderer only handles vectors.
        // For complex layers, delegate to MapPanel's renderMapViewImage which handles all types.
        boolean hasOnlineOrRaster = false;
        for (Layer layer : project.getLayers()) {
            if (layer == null || !layer.isVisible()) continue;
            if (layer instanceof ar.com.catgis.OnlineTileLayer
                    || layer instanceof ar.com.catgis.data.online.OnlineWmsLayer
                    || layer instanceof ar.com.catgis.RasterLayer) {
                hasOnlineOrRaster = true;
                break;
            }
        }
        if (hasOnlineOrRaster) {
            // Try to delegate to MapPanel (works in standalone and integrated mode)
            ar.com.catgis.MapPanel map = ar.com.catgis.CatgisDesktopApp.mapPanel;
            if (map != null) {
                double vpWidth = Math.max(viewport.getWidth(), 1);
                double zoomFactor = (double) widthPx / vpWidth;
                BufferedImage mapImage = map.renderMapViewImage(
                    viewport.getMinX(), viewport.getMinY(), zoomFactor,
                    widthPx, heightPx);
                if (mapImage != null) return mapImage;
            }
            // Fallback: render just the vectors we can handle
        }

        BufferedImage image = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        try {
            // White background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, widthPx, heightPx);

            // Render each visible layer
            for (Layer layer : project.getLayers()) {
                if (layer == null || !layer.isVisible()) continue;
                renderLayer(g2, layer, widthPx, heightPx, dpi);
            }

            // Render labels with collision detection
            collisionBoxes.clear();
            renderLabels(g2, project, widthPx, heightPx, dpi);

            // Heatmap overlay for point layers
            renderHeatmap(g2, project, widthPx, heightPx, dpi);

            // Point clusters
            renderClusters(g2, project, widthPx, heightPx);

            // Render indicator rectangle if this is an inset with a reference extent
            if (indicatorExtent != null) {
                renderIndicatorRectangle(g2, widthPx, heightPx);
            }

        } finally {
            g2.dispose();
        }
        return image;
    }

    private void renderLayer(Graphics2D g2, Layer layer, int widthPx, int heightPx, int dpi) {
        ShapefileData data = getShapefileData(layer);
        if (data == null) return;

        float opacity = layer.getOpacity();
        Graphics2D layerG2 = opacity >= 1.0f ? g2 : (Graphics2D) g2.create();
        if (opacity < 1.0f) {
            layerG2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, Math.max(0.1f, opacity)));
        }

        try {
            org.locationtech.jts.geom.GeometryFactory gf = new org.locationtech.jts.geom.GeometryFactory();
            SimpleFeatureCollection collection = data.getFeatureCollection();
            if (collection == null) return;

            try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
                while (iterator.hasNext()) {
                    org.geotools.api.feature.simple.SimpleFeature feature = iterator.next();
                    Object geomObj = feature.getDefaultGeometry();
                    if (geomObj == null || !(geomObj instanceof org.locationtech.jts.geom.Geometry)) continue;
                    org.locationtech.jts.geom.Geometry geometry = (org.locationtech.jts.geom.Geometry) geomObj;
                    if (geometry.isEmpty()) continue;

                    renderFeatureGeometry(layerG2, geometry, layer, feature, widthPx, heightPx);
                }
            }
        } finally {
            if (opacity < 1.0f) layerG2.dispose();
        }
    }

    private void renderFeatureGeometry(Graphics2D g2, org.locationtech.jts.geom.Geometry geometry,
                                        Layer layer, org.geotools.api.feature.simple.SimpleFeature feature,
                                        int widthPx, int heightPx) {
        if ((geometry instanceof org.locationtech.jts.geom.Point
                || geometry instanceof org.locationtech.jts.geom.MultiPoint)
                && layer.isClusteringEnabled()) {
            return; // Individual points suppressed when clustering is active
        }
        if (geometry instanceof org.locationtech.jts.geom.Point point) {
            int x = worldToScreenX(point.getX(), widthPx);
            int y = worldToScreenY(point.getY(), heightPx);
            renderPoint(g2, x, y, layer, feature);
        } else if (geometry instanceof org.locationtech.jts.geom.MultiPoint mp) {
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                org.locationtech.jts.geom.Geometry g = mp.getGeometryN(i);
                if (g instanceof org.locationtech.jts.geom.Point p) {
                    int x = worldToScreenX(p.getX(), widthPx);
                    int y = worldToScreenY(p.getY(), heightPx);
                    renderPoint(g2, x, y, layer, feature);
                }
            }
        } else if (geometry instanceof org.locationtech.jts.geom.LineString ls) {
            renderLineString(g2, ls, layer, feature, widthPx, heightPx);
        } else if (geometry instanceof org.locationtech.jts.geom.MultiLineString mls) {
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                org.locationtech.jts.geom.Geometry g = mls.getGeometryN(i);
                if (g instanceof org.locationtech.jts.geom.LineString line) {
                    renderLineString(g2, line, layer, feature, widthPx, heightPx);
                }
            }
        } else if (geometry instanceof org.locationtech.jts.geom.Polygon polygon) {
            renderPolygon(g2, polygon, layer, feature, widthPx, heightPx);
        } else if (geometry instanceof org.locationtech.jts.geom.MultiPolygon mp) {
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                org.locationtech.jts.geom.Geometry g = mp.getGeometryN(i);
                if (g instanceof org.locationtech.jts.geom.Polygon poly) {
                    renderPolygon(g2, poly, layer, feature, widthPx, heightPx);
                }
            }
        }
    }

    private void renderPoint(Graphics2D g2, int x, int y, Layer layer,
                             org.geotools.api.feature.simple.SimpleFeature feature) {
        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "point");
        int size = ar.com.catgis.LayerRenderHelper.resolveProportionalSize(layer, feature, categoryRule != null ? categoryRule.getPointSize() : layer.getPointSize());
        Color color = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getPointColor();

        String catId = categoryRule != null ? categoryRule.getCatalogSymbolId() : layer.getCatalogSymbolId();
        if (catId != null && !catId.isEmpty() && !"circle".equals(catId)) {
            PointSymbolCatalog.render(g2, catId, x, y, size + 4, color, color.darker(), 1.2f);
            return;
        }

        Layer.PointSymbolStyle style = categoryRule != null ? categoryRule.getPointSymbolStyle() : layer.getPointSymbolStyle();
        if (style == null) style = Layer.PointSymbolStyle.CIRCLE;
        PointSymbolRenderer.paint(g2, style, x, y, size, color, Color.BLACK);
    }

    private void renderLineString(Graphics2D g2, org.locationtech.jts.geom.LineString line,
                                   Layer layer, org.geotools.api.feature.simple.SimpleFeature feature,
                                   int widthPx, int heightPx) {
        org.locationtech.jts.geom.Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) return;

        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
        path.moveTo(worldToScreenX(coords[0].x, widthPx), worldToScreenY(coords[0].y, heightPx));
        for (int i = 1; i < coords.length; i++) {
            path.lineTo(worldToScreenX(coords[i].x, widthPx), worldToScreenY(coords[i].y, heightPx));
        }

        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "line");
        Color lineColor = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor();
        Layer.LineSymbolStyle lineStyle = categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle();
        float lineWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();

        g2.setColor(lineColor);
        g2.setStroke(LineSymbolRenderer.buildStroke(lineStyle, lineWidth));
        g2.draw(path);
    }

    private void renderPolygon(Graphics2D g2, org.locationtech.jts.geom.Polygon polygon,
                                Layer layer, org.geotools.api.feature.simple.SimpleFeature feature,
                                int widthPx, int heightPx) {
        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "polygon");
        Color fillColor = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor();
        Color borderColor = layer.getBorderColor();
        Layer.PolygonFillStyle fillStyle = categoryRule != null ? categoryRule.getPolygonFillStyle() : layer.getPolygonFillStyle();

        // Exterior ring
        java.awt.geom.Path2D exterior = new java.awt.geom.Path2D.Double();
        org.locationtech.jts.geom.Coordinate[] exteriorCoords = polygon.getExteriorRing().getCoordinates();
        if (exteriorCoords.length < 3) return;
        exterior.moveTo(worldToScreenX(exteriorCoords[0].x, widthPx), worldToScreenY(exteriorCoords[0].y, heightPx));
        for (int i = 1; i < exteriorCoords.length; i++) {
            exterior.lineTo(worldToScreenX(exteriorCoords[i].x, widthPx), worldToScreenY(exteriorCoords[i].y, heightPx));
        }
        exterior.closePath();

        // Fill
        java.awt.Paint paint = PolygonSymbolRenderer.buildPaint(fillStyle, fillColor, borderColor, 12);
        g2.setPaint(paint);
        g2.fill(exterior);

        // Border
        g2.setColor(borderColor);
        g2.setStroke(new java.awt.BasicStroke(Math.max(0.5f, layer.getLineWidth())));
        g2.draw(exterior);

        // Interior rings (holes)
        for (int r = 0; r < polygon.getNumInteriorRing(); r++) {
            org.locationtech.jts.geom.Coordinate[] holeCoords = polygon.getInteriorRingN(r).getCoordinates();
            if (holeCoords.length < 3) continue;
            java.awt.geom.Path2D hole = new java.awt.geom.Path2D.Double();
            hole.moveTo(worldToScreenX(holeCoords[0].x, widthPx), worldToScreenY(holeCoords[0].y, heightPx));
            for (int i = 1; i < holeCoords.length; i++) {
                hole.lineTo(worldToScreenX(holeCoords[i].x, widthPx), worldToScreenY(holeCoords[i].y, heightPx));
            }
            hole.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(hole);
            g2.setColor(borderColor);
            g2.draw(hole);
        }
    }

    private void renderLabels(Graphics2D g2, Project project, int widthPx, int heightPx, int dpi) {
        List<Object[]> allCandidates = new ArrayList<>();

        for (Layer layer : project.getLayers()) {
            if (layer == null || !layer.isVisible() || !layer.isLabelsVisible()) continue;

            String labelExpr = layer.getLabelExpression();
            boolean useExpression = (labelExpr != null && !labelExpr.isBlank());
            String labelField = useExpression ? null : layer.getLabelField();

            if (!useExpression && (labelField == null || labelField.isBlank())) continue;

            ShapefileData data = getShapefileData(layer);
            if (data == null) continue;
            SimpleFeatureCollection collection = data.getFeatureCollection();
            if (collection == null) continue;

            try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
                while (iterator.hasNext()) {
                    org.geotools.api.feature.simple.SimpleFeature feature = iterator.next();
                    String text;
                    if (useExpression) {
                        text = LabelExpressionEngine.evaluate(labelExpr, feature);
                    } else {
                        Object attrValue = feature.getAttribute(labelField);
                        text = attrValue != null ? String.valueOf(attrValue).trim() : "";
                    }
                    if (text == null || text.isEmpty()) continue;

                    Object geomObj = feature.getDefaultGeometry();
                    if (geomObj == null || !(geomObj instanceof org.locationtech.jts.geom.Geometry)) continue;
                    org.locationtech.jts.geom.Coordinate coord = getLabelCoordinate((org.locationtech.jts.geom.Geometry) geomObj);
                    if (coord == null) continue;

                    int sx = worldToScreenX(coord.x, widthPx);
                    int sy = worldToScreenY(coord.y, heightPx);
                    String geomType = LabelPlacementEngine.resolveGeometryType(geomObj.getClass());
                    allCandidates.add(new Object[]{text, sx, sy, geomType, layer.getLabelPriority()});
                }
            }
        }

        if (!allCandidates.isEmpty()) {
            // Use first layer's settings for font (simplified — in full impl, per-layer)
            Layer firstLabelLayer = null;
            for (Layer l : project.getLayers()) {
                if (l != null && l.isLabelsVisible() &&
                    ((l.getLabelExpression() != null && !l.getLabelExpression().isBlank()) ||
                     (l.getLabelField() != null && !l.getLabelField().isBlank()))) {
                    firstLabelLayer = l;
                    break;
                }
            }
            if (firstLabelLayer != null) {
                List<LabelPlacementEngine.ResolvedLabel> resolved =
                        LabelPlacementEngine.resolveLabels(g2, firstLabelLayer, allCandidates, collisionBoxes);
                for (LabelPlacementEngine.ResolvedLabel rl : resolved) {
                    drawResolvedLabel(g2, rl);
                }
            }
        }
    }

    private void drawResolvedLabel(Graphics2D g2, LabelPlacementEngine.ResolvedLabel rl) {
        Layer layer = rl.layer();
        int style = Font.PLAIN;
        if (layer.isLabelBold()) style |= Font.BOLD;
        if (layer.isLabelItalic()) style |= Font.ITALIC;
        Font font = new Font(layer.getLabelFontFamily(), style, layer.getLabelFontSize());
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int lx = rl.drawX();
        int ly = rl.drawY();
        int tw = rl.textWidth();

        // Background
        if (layer.isLabelBackgroundEnabled() && layer.getLabelBackgroundColor().getAlpha() > 0) {
            g2.setColor(layer.getLabelBackgroundColor());
            g2.fillRoundRect(lx - 4, ly - fm.getAscent() - 2, tw + 8, fm.getHeight() + 4, 6, 6);
        }

        // Halo
        if (layer.isLabelHaloEnabled() && layer.getLabelHaloColor().getAlpha() > 0) {
            g2.setColor(layer.getLabelHaloColor());
            float hw = layer.getLabelHaloWidth();
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(rl.text(), lx + dx * hw, ly + dy * hw);
        }

        // Text
        g2.setColor(layer.getLabelColor());
        g2.drawString(rl.text(), lx, ly);

        // Underline
        if (layer.isLabelUnderline()) {
            g2.setColor(layer.getLabelColor());
            g2.drawLine(lx, ly + 2, lx + tw, ly + 2);
        }
    }

    // --- Coordinate conversion ---

    private int worldToScreenX(double worldX, int widthPx) {
        double ratio = (worldX - viewport.getMinX()) / (viewport.getMaxX() - viewport.getMinX());
        return (int) (ratio * widthPx);
    }

    private int worldToScreenY(double worldY, int heightPx) {
        double ratio = 1.0 - (worldY - viewport.getMinY()) / (viewport.getMaxY() - viewport.getMinY());
        return (int) (ratio * heightPx);
    }

    // --- Helpers ---

    private org.locationtech.jts.geom.Coordinate getLabelCoordinate(org.locationtech.jts.geom.Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) return null;
        if (geometry instanceof org.locationtech.jts.geom.Point) return ((org.locationtech.jts.geom.Point) geometry).getCoordinate();
        if (geometry instanceof org.locationtech.jts.geom.Polygon) {
            org.locationtech.jts.geom.Point p = ((org.locationtech.jts.geom.Polygon) geometry).getInteriorPoint();
            return p != null ? p.getCoordinate() : geometry.getCentroid().getCoordinate();
        }
        if (geometry instanceof org.locationtech.jts.geom.LineString) {
            org.locationtech.jts.geom.Point p = geometry.getCentroid();
            return p != null ? p.getCoordinate() : null;
        }
        org.locationtech.jts.geom.Point centroid = geometry.getCentroid();
        return centroid != null ? centroid.getCoordinate() : null;
    }

    // resolveCategoryRule, resolveGraduatedRule, resolveBestRule, resolveProportionalSize
    // moved to LayerRenderHelper for shared use

    private void renderClusters(Graphics2D g2, Project project,
                                  int widthPx, int heightPx) {
        java.util.List<java.awt.geom.Point2D> allPoints = new java.util.ArrayList<>();
        int radius = 30;
        boolean hasClustering = false;

        for (Layer layer : project.getLayers()) {
            if (layer == null || !layer.isVisible() || !layer.isClusteringEnabled()) continue;
            hasClustering = true;
            radius = layer.getClusterRadius();

            ShapefileData data = getShapefileData(layer);
            if (data == null || data.getFeatureCollection() == null) continue;

            try (org.geotools.feature.FeatureIterator<org.geotools.api.feature.simple.SimpleFeature> it =
                    data.getFeatureCollection().features()) {
                while (it.hasNext()) {
                    org.geotools.api.feature.simple.SimpleFeature f = it.next();
                    Object geomObj = f.getDefaultGeometry();
                    if (geomObj instanceof org.locationtech.jts.geom.Point pt) {
                        allPoints.add(new java.awt.geom.Point2D.Double(
                            worldToScreenX(pt.getX(), widthPx),
                            worldToScreenY(pt.getY(), heightPx)));
                    } else if (geomObj instanceof org.locationtech.jts.geom.MultiPoint mp) {
                        for (int i = 0; i < mp.getNumGeometries(); i++) {
                            org.locationtech.jts.geom.Geometry g = mp.getGeometryN(i);
                            if (g instanceof org.locationtech.jts.geom.Point p) {
                                allPoints.add(new java.awt.geom.Point2D.Double(
                                    worldToScreenX(p.getX(), widthPx),
                                    worldToScreenY(p.getY(), heightPx)));
                            }
                        }
                    }
                }
            } catch (Exception ignored) { CatgisLogger.warn("MapFrameRenderer: operation failed", ignored); }
        }

        if (!hasClustering || allPoints.isEmpty()) return;

        var clusters = ar.com.catgis.PointClusterRenderer.clusterPoints(allPoints, radius);
        BufferedImage clusterImg = ar.com.catgis.PointClusterRenderer.renderClusters(
            clusters, widthPx, heightPx);
        if (clusterImg != null) g2.drawImage(clusterImg, 0, 0, null);
    }

    private void renderHeatmap(Graphics2D g2, Project project,
                               int widthPx, int heightPx, int dpi) {
        java.util.List<java.awt.geom.Point2D> allPoints = new java.util.ArrayList<>();
        int radius = 30;
        float opacity = 0.6f;
        boolean hasHeatmap = false;

        for (Layer layer : project.getLayers()) {
            if (layer == null || !layer.isVisible() || !layer.isHeatmapEnabled()) continue;
            hasHeatmap = true;
            radius = layer.getHeatmapRadius();
            opacity = layer.getHeatmapOpacity();

            ShapefileData data = getShapefileData(layer);
            if (data == null || data.getFeatureCollection() == null) continue;

            try (org.geotools.feature.FeatureIterator<org.geotools.api.feature.simple.SimpleFeature> it =
                    data.getFeatureCollection().features()) {
                while (it.hasNext()) {
                    org.geotools.api.feature.simple.SimpleFeature f = it.next();
                    Object geomObj = f.getDefaultGeometry();
                    if (geomObj instanceof org.locationtech.jts.geom.Point pt) {
                        int sx = worldToScreenX(pt.getX(), widthPx);
                        int sy = worldToScreenY(pt.getY(), heightPx);
                        allPoints.add(new java.awt.geom.Point2D.Double(sx, sy));
                    } else if (geomObj instanceof org.locationtech.jts.geom.MultiPoint mp) {
                        for (int i = 0; i < mp.getNumGeometries(); i++) {
                            org.locationtech.jts.geom.Geometry g = mp.getGeometryN(i);
                            if (g instanceof org.locationtech.jts.geom.Point p) {
                                int sx = worldToScreenX(p.getX(), widthPx);
                                int sy = worldToScreenY(p.getY(), heightPx);
                                allPoints.add(new java.awt.geom.Point2D.Double(sx, sy));
                            }
                        }
                    }
                }
            } catch (Exception ignored) { CatgisLogger.warn("MapFrameRenderer: operation failed", ignored); }
        }

        if (!hasHeatmap || allPoints.isEmpty()) return;

        BufferedImage heatmap = ar.com.catgis.HeatmapRenderer.renderHeatmap(
            allPoints, widthPx, heightPx, radius, opacity);
        if (heatmap != null) {
            g2.drawImage(heatmap, 0, 0, null);
        }
    }

    private ShapefileData getShapefileData(Layer layer) {
        MapPanel mapPanel = CatgisDesktopApp.mapPanel;
        ShapefileData data = mapPanel != null ? mapPanel.getShapefileData(layer) : null;
        if (data != null) {
            return data;
        }
        return VectorLayerUtils.ensureVectorData(layer);
    }
}
