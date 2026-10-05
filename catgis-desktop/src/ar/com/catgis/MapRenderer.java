package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.core.model.GradientFill;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.raster.LocalRasterData;
import ar.com.catgis.renderer.PolygonSymbolRenderer;
import ar.com.catgis.renderer.LineSymbolRenderer;
import ar.com.catgis.renderer.labels.LabelExpressionEngine;
import ar.com.catgis.renderer.labels.LabelPlacementEngine;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MapRenderer {

    private final MapPanel map;

    MapRenderer(MapPanel map) {
        this.map = map;
    }

    // ── Primitive draw helpers (thin delegates to OnlineLayerRenderer) ──────

    void drawPoint(Graphics2D g2, Point point, Color color, int size) {
        OnlineLayerRenderer.drawPoint(map, g2, point, color, size);
    }

    void drawLineString(Graphics2D g2, LineString line, Color color, float width) {
        OnlineLayerRenderer.drawLineString(map, g2, line, color, width);
    }

    void drawPolygon(Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth) {
        drawPolygon(g2, polygon, fillColor, borderColor, borderWidth, null);
    }

    void drawPolygon(Graphics2D g2, Polygon polygon, Color fillColor, Color borderColor, float borderWidth, GradientFill gradientFill) {
        OnlineLayerRenderer.drawPolygon(map, g2, polygon, fillColor, borderColor, borderWidth, gradientFill);
    }

    Path2D buildPathFromCoordinates(Coordinate[] coords) {
        return OnlineLayerRenderer.buildPathFromCoordinates(map, coords);
    }

    void drawTemporaryGeometry(Graphics2D g2, List<Coordinate> tempCoords, String mode, Color lineColor, Color fillColor) {
        OnlineLayerRenderer.drawTemporaryGeometry(map, g2, tempCoords, mode, lineColor, fillColor);
    }

    // ── 1. drawPins ─────────────────────────────────────────────────────────

    void drawPins(Graphics2D g2) {
        for (PinMarker pin : map.pinManager.getPins()) {
            int x = map.worldToScreenX(pin.getX());
            int y = map.worldToScreenY(pin.getY());

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

    // ── 2. drawCurrentSketch ────────────────────────────────────────────────

    void drawCurrentSketch(Graphics2D g2) {
        if (!map.isDrawingActive()) {
            return;
        }

        drawPendingDrawingSessionGeometries(g2);

        if ("CONTINUE_LINE".equalsIgnoreCase(map.drawingToolManager.drawingMode) && !map.drawingToolManager.drawingContinuationEndpointChosen) {
            drawContinuationEndpointHints(g2);
            return;
        }

        Coordinate previewCoordinate = map.resolveInteractivePreviewCoordinate();

        if ("POINT".equalsIgnoreCase(map.drawingToolManager.drawingMode) || "MULTIPOINT".equalsIgnoreCase(map.drawingToolManager.drawingMode)) {
            for (Coordinate c : map.drawingToolManager.drawingCoordinates) {
                int x = map.worldToScreenX(c.x);
                int y = map.worldToScreenY(c.y);
                g2.setColor(Color.MAGENTA);
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }

            if (previewCoordinate != null) {
                int x = map.worldToScreenX(previewCoordinate.x);
                int y = map.worldToScreenY(previewCoordinate.y);
                g2.setColor(new Color(255, 0, 255, 120));
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - 5, y - 5, 10, 10);
            }
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(map.drawingToolManager.drawingCoordinates);
        if (previewCoordinate != null) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        if ("CIRCLE".equalsIgnoreCase(map.drawingToolManager.drawingMode) || "CIRCLE_3P".equalsIgnoreCase(map.drawingToolManager.drawingMode)) {
            Geometry previewGeometry = "CIRCLE".equalsIgnoreCase(map.drawingToolManager.drawingMode)
                    ? map.buildCircleGeometry(tempCoords)
                    : map.buildCircleThreePointGeometry(tempCoords);
            if (previewGeometry != null) {
                drawPendingDrawingGeometry(g2, previewGeometry);
                return;
            }
        }

        if ("RECTANGLE".equalsIgnoreCase(map.drawingToolManager.drawingMode)) {
            tempCoords = map.buildRectangleCoordinates(tempCoords);
        }

        if (tempCoords.isEmpty()) {
            return;
        }

        drawTemporaryGeometry(
                g2,
                tempCoords,
                "RECTANGLE".equalsIgnoreCase(map.drawingToolManager.drawingMode) ? "POLYGON" : map.drawingToolManager.drawingMode,
                Color.MAGENTA,
                new Color(255, 0, 255, 40)
        );
    }

    // ── 3. drawPendingDrawingSessionGeometries ──────────────────────────────

    void drawPendingDrawingSessionGeometries(Graphics2D g2) {
        if (map.drawingToolManager.pendingDrawingSessionGeometries.isEmpty()) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            for (Geometry geometry : map.drawingToolManager.pendingDrawingSessionGeometries) {
                drawPendingDrawingGeometry(copy, geometry);
            }
        } finally {
            copy.dispose();
        }
    }

    // ── 4. drawPendingDrawingGeometry ───────────────────────────────────────

    void drawPendingDrawingGeometry(Graphics2D g2, Geometry geometry) {
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

    // ── 5. drawContinuationEndpointHints ────────────────────────────────────

    void drawContinuationEndpointHints(Graphics2D g2) {
        if (!"CONTINUE_LINE".equalsIgnoreCase(map.drawingToolManager.drawingMode)
                || map.drawingToolManager.drawingContinuationBaseCoordinates == null
                || map.drawingToolManager.drawingContinuationBaseCoordinates.length < 2
                || map.drawingToolManager.drawingContinuationLayer == null) {
            return;
        }

        Coordinate start = map.toProjectCoordinate(map.drawingToolManager.drawingContinuationBaseCoordinates[0], map.drawingToolManager.drawingContinuationLayer);
        Coordinate end = map.toProjectCoordinate(
                map.drawingToolManager.drawingContinuationBaseCoordinates[map.drawingToolManager.drawingContinuationBaseCoordinates.length - 1],
                map.drawingToolManager.drawingContinuationLayer
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

    // ── 6. drawContinuationEndpointHint ─────────────────────────────────────

    void drawContinuationEndpointHint(Graphics2D g2, Coordinate coordinate, String label) {
        int x = map.worldToScreenX(coordinate.x);
        int y = map.worldToScreenY(coordinate.y);

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

    // ── 7. drawCurrentMeasurement ───────────────────────────────────────────

    void drawCurrentMeasurement(Graphics2D g2) {
        if (!map.isMeasurementActive() || map.measurementTool.getPoints().isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(map.measurementTool.getPoints());
        Coordinate previewCoordinate = map.resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        drawTemporaryGeometry(g2, tempCoords, map.getMeasurementMode(), Color.CYAN, new Color(0, 255, 255, 40));
    }

    // ── 8. drawFeatureEditSketch ────────────────────────────────────────────

    void drawFeatureEditSketch(Graphics2D g2) {
        if (!map.featureEditMode) {
            return;
        }

        if (map.isCadLineConstructionMode()) {
            drawCadOperationPreview(g2);
            return;
        }

        if (MapPanel.EDIT_OP_ADJACENT_POLYGON.equals(map.featureEditOperation)) {
            drawAdjacentPolygonPreview(g2);
            return;
        }

        if (map.featureEditSketchCoordinates.isEmpty()) {
            return;
        }

        List<Coordinate> tempCoords = new ArrayList<>(map.featureEditSketchCoordinates);
        Coordinate previewCoordinate = map.resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null && map.isFeatureEditSketchMode()) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }

        String mode = MapPanel.EDIT_OP_HOLE.equals(map.featureEditOperation) ? "POLYGON" : "LINE";
        drawTemporaryGeometry(g2, tempCoords, mode, new Color(14, 116, 144), new Color(14, 165, 233, 48));
    }

    // ── 9. drawTopographicProfileCapture ────────────────────────────────────

    void drawTopographicProfileCapture(Graphics2D g2) {
        if (!map.topographicProfileTool.isActive()) {
            return;
        }
        List<Coordinate> tempCoords = new ArrayList<>(map.topographicProfileTool.getCoordinates());
        Coordinate previewCoordinate = map.resolveInteractivePreviewCoordinate();
        if (previewCoordinate != null && !tempCoords.isEmpty()) {
            tempCoords.add(new Coordinate(previewCoordinate));
        }
        if (tempCoords.isEmpty()) {
            return;
        }
        drawTemporaryGeometry(g2, tempCoords, "LINE", new Color(180, 83, 9), new Color(245, 158, 11, 48));
    }

    // ── 10. drawAdjacentPolygonPreview ──────────────────────────────────────

    void drawAdjacentPolygonPreview(Graphics2D g2) {
        if (map.selectedFeature == null || map.selectedLayer == null
                || map.adjacentPolygonSegmentStart == null || map.adjacentPolygonSegmentEnd == null) {
            return;
        }

        drawAdjacentBaseSegment(g2);

        Coordinate previewCoordinate = map.resolveInteractivePreviewCoordinate();
        if (previewCoordinate == null) {
            return;
        }

        Object geomObj = map.selectedFeature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry sourceGeometry)) {
            return;
        }

        Coordinate sourcePreview = map.toSourceCoordinate(previewCoordinate.x, previewCoordinate.y, map.selectedLayer);
        Geometry previewGeometry = map.buildAdjacentPolygonGeometry(
                sourceGeometry,
                map.adjacentPolygonSegmentStart,
                map.adjacentPolygonSegmentEnd,
                sourcePreview
        );
        if (previewGeometry == null || previewGeometry.isEmpty()) {
            return;
        }

        Geometry displayGeometry = map.reprojectGeometryIfNeeded(map.selectedLayer, previewGeometry);
        if (!(displayGeometry instanceof Polygon previewPolygon)) {
            return;
        }

        drawPolygon(g2, previewPolygon, new Color(34, 197, 94, 56), new Color(21, 128, 61), 2f);
    }

    // ── 11. drawAdjacentBaseSegment ─────────────────────────────────────────

    void drawAdjacentBaseSegment(Graphics2D g2) {
        if (map.adjacentPolygonSegmentStart == null || map.adjacentPolygonSegmentEnd == null || map.selectedLayer == null) {
            return;
        }

        GeometryFactory factory = new GeometryFactory();
        LineString baseSegment = factory.createLineString(new Coordinate[]{
                new Coordinate(map.adjacentPolygonSegmentStart),
                new Coordinate(map.adjacentPolygonSegmentEnd)
        });
        Geometry displaySegment = map.reprojectGeometryIfNeeded(map.selectedLayer, baseSegment);
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

    // ── 12. drawCadOperationPreview ─────────────────────────────────────────

    void drawCadOperationPreview(Graphics2D g2) {
        if (map.selectedFeature == null || map.selectedLayer == null) {
            return;
        }

        if ((MapPanel.EDIT_OP_EXTEND_LINE.equals(map.featureEditOperation) || MapPanel.EDIT_OP_SHORTEN_LINE.equals(map.featureEditOperation))
                && !map.cadReferenceEndpointChosen) {
            drawSelectedLineEndpointHints(g2);
            return;
        }

        if ((MapPanel.EDIT_OP_PARALLEL.equals(map.featureEditOperation) || MapPanel.EDIT_OP_PERPENDICULAR.equals(map.featureEditOperation))
                && map.cadReferenceSegmentStart != null && map.cadReferenceSegmentEnd != null) {
            drawCadReferenceSegment(g2);
        }

        Coordinate previewCoordinate = map.resolveInteractivePreviewCoordinate();
        if (previewCoordinate == null) {
            return;
        }

        Coordinate sourcePreview = map.toSourceCoordinate(previewCoordinate.x, previewCoordinate.y, map.selectedLayer);
        Geometry previewGeometry = null;
        Geometry selectedGeometry = map.extractFeatureGeometryCopy(map.selectedFeature);
        if (selectedGeometry == null || sourcePreview == null) {
            return;
        }

        if (MapPanel.EDIT_OP_EXTEND_LINE.equals(map.featureEditOperation)) {
            previewGeometry = map.buildAdjustedSelectedLineGeometry(selectedGeometry, sourcePreview, true, map.cadReferenceFromStart);
        } else if (MapPanel.EDIT_OP_SHORTEN_LINE.equals(map.featureEditOperation)) {
            previewGeometry = map.buildAdjustedSelectedLineGeometry(selectedGeometry, sourcePreview, false, map.cadReferenceFromStart);
        } else if (MapPanel.EDIT_OP_PARALLEL.equals(map.featureEditOperation) && map.cadReferenceSegmentStart != null && map.cadReferenceSegmentEnd != null) {
            previewGeometry = map.buildParallelLineGeometry(map.cadReferenceSegmentStart, map.cadReferenceSegmentEnd, sourcePreview);
        } else if (MapPanel.EDIT_OP_PERPENDICULAR.equals(map.featureEditOperation) && map.cadReferenceSegmentStart != null && map.cadReferenceSegmentEnd != null) {
            previewGeometry = map.buildPerpendicularLineGeometry(map.cadReferenceSegmentStart, map.cadReferenceSegmentEnd, sourcePreview);
        }

        if (previewGeometry == null || previewGeometry.isEmpty()) {
            return;
        }

        Geometry displayGeometry = map.reprojectGeometryIfNeeded(map.selectedLayer, previewGeometry);
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

    // ── 13. drawSelectedLineEndpointHints ───────────────────────────────────

    void drawSelectedLineEndpointHints(Graphics2D g2) {
        Geometry geometry = map.extractFeatureGeometryCopy(map.selectedFeature);
        Coordinate[] baseCoordinates = map.extractContinuableLineCoordinates(geometry);
        if (baseCoordinates == null || baseCoordinates.length < 2) {
            return;
        }

        Coordinate start = map.toProjectCoordinate(baseCoordinates[0], map.selectedLayer);
        Coordinate end = map.toProjectCoordinate(baseCoordinates[baseCoordinates.length - 1], map.selectedLayer);
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

    // ── 14. drawCadReferenceSegment ─────────────────────────────────────────

    void drawCadReferenceSegment(Graphics2D g2) {
        if (map.cadReferenceSegmentStart == null || map.cadReferenceSegmentEnd == null || map.selectedLayer == null) {
            return;
        }

        Coordinate displayStart = map.toProjectCoordinate(map.cadReferenceSegmentStart, map.selectedLayer);
        Coordinate displayEnd = map.toProjectCoordinate(map.cadReferenceSegmentEnd, map.selectedLayer);
        if (displayStart == null || displayEnd == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(new Color(59, 130, 246, 220));
            copy.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            copy.drawLine(
                    map.worldToScreenX(displayStart.x),
                    map.worldToScreenY(displayStart.y),
                    map.worldToScreenX(displayEnd.x),
                    map.worldToScreenY(displayEnd.y)
            );
        } finally {
            copy.dispose();
        }
    }

    // ── 15. drawSnapPreview ─────────────────────────────────────────────────

    void drawSnapPreview(Graphics2D g2) {
        Coordinate snapCoord = map.snapManager.getSnapPreviewCoordinate();
        if (!map.snapManager.isSnapEnabled() || snapCoord == null || !(map.isDrawingActive() || map.isMeasurementActive() || map.featureEditMode)) {
            return;
        }

        int x = map.worldToScreenX(snapCoord.x);
        int y = map.worldToScreenY(snapCoord.y);
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

    // ── 16. drawLayer ───────────────────────────────────────────────────────

    void drawLayer(Graphics2D g2, Layer layer, ShapefileData data) {
        if (data == null || data.getFeatureCollection() == null) {
            return;
        }

        float opacity = layer.getOpacity();
        Graphics2D vecG2 = opacity >= 1.0f ? g2 : (Graphics2D) g2.create();
        if (opacity < 1.0f) {
            vecG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, opacity)));
        }

        try {
            boolean editingLayer = map.isLayerArmedForEditing(layer);
            map.forEachVisibleFeatureGeometry(List.of(layer), "Error al dibujar la capa ", (currentLayer, featureGeometry) -> {
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

    // ── 17. drawGeometry ────────────────────────────────────────────────────

    void drawGeometry(Graphics2D g2, Geometry geometry, Layer layer, SimpleFeature feature) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if ((geometry instanceof Point || geometry instanceof MultiPoint)
                && layer.isClusteringEnabled()) {
            return;
        }

        if (geometry instanceof Point) {
            drawStyledPoint(g2, (Point) geometry, layer, feature);
            drawLabelForFeature(g2, layer, feature, map.worldToScreenX(((Point)geometry).getX()), map.worldToScreenY(((Point)geometry).getY()));
            return;
        }

        if (geometry instanceof MultiPoint) {
            MultiPoint mp = (MultiPoint) geometry;
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point) {
                    drawStyledPoint(g2, (Point) g, layer, feature);
                    drawLabelForFeature(g2, layer, feature, map.worldToScreenX(((Point)g).getX()), map.worldToScreenY(((Point)g).getY()));
                }
            }
            return;
        }

        if (geometry instanceof LineString) {
            drawStyledLineString(g2, (LineString) geometry, layer, feature);
            Coordinate[] lc = ((LineString)geometry).getCoordinates();
            if (lc.length >= 2) { int mi = lc.length / 2; drawLabelForFeature(g2, layer, feature, map.worldToScreenX(lc[mi].x), map.worldToScreenY(lc[mi].y)); }
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
            try {
                org.locationtech.jts.geom.Point ip = geometry.getInteriorPoint();
                if (ip != null) drawLabelForFeature(g2, layer, feature, map.worldToScreenX(ip.getX()), map.worldToScreenY(ip.getY()));
            } catch (Exception ignored) { CatgisLogger.warn("MapRenderer: operation failed", ignored); }
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

    private void drawLabelForFeature(Graphics2D g2, Layer layer, SimpleFeature feature, int x, int y) {
        // Labels are now rendered in batch via drawAllLabels() with collision detection.
        // This method is intentionally a no-op to avoid double rendering.
    }

    // ── 18. drawGeometryForEditingLayer ─────────────────────────────────────

    void drawGeometryForEditingLayer(Graphics2D g2, Geometry geometry, Layer layer) {
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

    // ── 19. drawStyledPoint ─────────────────────────────────────────────────

    void drawStyledPoint(Graphics2D g2, Point point, Layer layer, SimpleFeature feature) {
        int x = map.worldToScreenX(point.getX());
        int y = map.worldToScreenY(point.getY());
        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "point");
        int size = ar.com.catgis.LayerRenderHelper.resolveProportionalSize(layer, feature, categoryRule != null ? categoryRule.getPointSize() : layer.getPointSize());
        Color color = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getPointColor();

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

    // ── 20. drawStyledLineString ────────────────────────────────────────────

    void drawStyledLineString(Graphics2D g2, LineString line, Layer layer, SimpleFeature feature) {
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) {
            return;
        }

        Path2D path = new Path2D.Double();
        path.moveTo(map.worldToScreenX(coords[0].x), map.worldToScreenY(coords[0].y));
        for (int i = 1; i < coords.length; i++) {
            path.lineTo(map.worldToScreenX(coords[i].x), map.worldToScreenY(coords[i].y));
        }

        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "line");
        Color lineColor = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor();
        Layer.LineSymbolStyle lineStyle = categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle();
        float lineWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();
        g2.setColor(lineColor);
        g2.setStroke(LineSymbolRenderer.buildStroke(lineStyle, lineWidth));
        g2.draw(path);
    }

    // ── 21. drawStyledPolygon ───────────────────────────────────────────────

    void drawStyledPolygon(Graphics2D g2, Polygon polygon, Layer layer, SimpleFeature feature) {
        Path2D exteriorPath = buildPathFromCoordinates(polygon.getExteriorRing().getCoordinates());
        if (exteriorPath == null) {
            return;
        }

        CategoryStyleRule categoryRule = ar.com.catgis.LayerRenderHelper.resolveBestRule(layer, feature, "polygon");
        Paint oldPaint = g2.getPaint();
        Layer.PolygonFillStyle fillStyle = categoryRule != null ? categoryRule.getPolygonFillStyle() : layer.getPolygonFillStyle();
        Color fc = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor();
        Color bc = categoryRule != null ? (categoryRule.getSecondaryColor() != null ? categoryRule.getSecondaryColor() : fc.darker()) : layer.getLineColor();

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
            Path2D holePath = buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.setColor(map.getBackground());
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
            Path2D holePath = buildPathFromCoordinates(polygon.getInteriorRingN(i).getCoordinates());
            if (holePath != null) {
                g2.draw(holePath);
            }
        }
    }

    // ── 22. drawSelectedFeature ─────────────────────────────────────────────

    void drawSelectedFeature(Graphics2D g2, SimpleFeature feature, Layer layer) {
        if (!map.isFeatureVisibleInLayer(layer, feature)) {
            return;
        }
        Object geomObj = feature.getDefaultGeometry();
        if (!(geomObj instanceof Geometry)) {
            return;
        }

        Geometry geometry = map.reprojectGeometryIfNeeded(layer, (Geometry) geomObj);
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        boolean editing = map.featureEditMode && layer == map.selectedLayer && feature == map.selectedFeature;
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

    // ── 23. drawEditableVertices ────────────────────────────────────────────

    void drawEditableVertices(Graphics2D g2, SimpleFeature feature, Layer layer) {
        Geometry geometry = map.getEditableDisplayGeometry(feature, layer);
        if (geometry == null) {
            return;
        }

        Coordinate[] vertices = map.getEditableVertexCoordinates(geometry);
        if (vertices == null || vertices.length == 0) {
            return;
        }

        for (int i = 0; i < vertices.length; i++) {
            Coordinate c = vertices[i];
            if (c == null) {
                continue;
            }

            int x = map.worldToScreenX(c.x);
            int y = map.worldToScreenY(c.y);
            boolean activeMoveVertex = i == map.activeEditVertexIndex;
            boolean joinTarget = MapPanel.EDIT_OP_JOIN_VERTEX.equals(map.featureEditOperation) && i == map.joinTargetVertexIndex;
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

    // ── 24. drawAttributeTableSelections ────────────────────────────────────

    void drawAttributeTableSelections(Graphics2D g2) {
        if (map.tableSelectionIds.isEmpty()) {
            return;
        }

        for (Map.Entry<Layer, List<String>> entry : map.tableSelectionIds.entrySet()) {
            Layer layer = entry.getKey();
            if (layer == null || !map.layerManager.isLayerEffectivelyVisible(layer)) {
                continue;
            }

            ShapefileData data = map.getShapefileData(layer);
            if (data == null || data.getFeatures() == null) {
                continue;
            }

            List<String> ids = entry.getValue();
            if (ids == null || ids.isEmpty()) {
                continue;
            }

            for (String featureId : ids) {
                SimpleFeature feature = map.findFeatureById(data.getFeatures(), featureId);
                if (feature == null) {
                    continue;
                }
                if (layer == map.selectedLayer && map.selectedFeature != null && map.sameFeatureId(map.selectedFeature, featureId)) {
                    continue;
                }
                drawSelectedFeature(g2, feature, layer);
            }
        }
    }

    // ── 25. drawSelectionBox ────────────────────────────────────────────────

    void drawSelectionBox(Graphics2D g2) {
        if (!map.selectionBoxActive || !map.selectionBoxDragging) {
            return;
        }

        Rectangle bounds = map.getSelectionBoxBounds();
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

    // ── 26. drawSelectionFlash ──────────────────────────────────────────────

    void drawSelectionFlash(Graphics2D g2) {
        if (map.selectionFlashGeometry == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - map.selectionFlashStartedAt;
        if (elapsed >= MapPanel.SELECTION_FLASH_DURATION_MS) {
            map.selectionFlashGeometry = null;
            map.selectionFlashTimer.stop();
            return;
        }

        Coordinate focus = map.resolveSelectionFlashCoordinate(map.selectionFlashGeometry);
        if (focus == null) {
            return;
        }

        float progress = elapsed / (float) MapPanel.SELECTION_FLASH_DURATION_MS;
        float baseAlpha = 1f - progress;
        int x = map.worldToScreenX(focus.x);
        int y = map.worldToScreenY(focus.y);

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

    // ── 27. drawRasterLayer ─────────────────────────────────────────────────

    void drawRasterLayer(Graphics2D g2, Layer layer, LocalRasterData data) {
        long startedAt = System.nanoTime();
        if (data == null || data.getImage() == null) {
            return;
        }

        Envelope env = map.getRasterEnvelope(layer, data);
        if (env == null || env.isNull()) {
            return;
        }

        int x1 = map.worldToScreenX(env.getMinX());
        int y1 = map.worldToScreenY(env.getMaxY());
        int x2 = map.worldToScreenX(env.getMaxX());
        int y2 = map.worldToScreenY(env.getMinY());

        int drawX = Math.min(x1, x2);
        int drawY = Math.min(y1, y2);
        int drawW = Math.abs(x2 - x1);
        int drawH = Math.abs(y2 - y1);

        if (drawW <= 1 || drawH <= 1) {
            return;
        }

        MapPanel.RasterStyle style = map.getOrCreateRasterStyle(layer, Math.max(1, data.getBandCount()));
        BufferedImage display = map.getCachedDisplayImage(layer, data, style);
        if (display == null) {
            return;
        }

        Graphics2D copy = (Graphics2D) g2.create();
        try {
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, style.opacity));
            copy.drawImage(display, drawX, drawY, drawW, drawH, null);
        } finally {
            copy.dispose();
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
            if (elapsedMs >= 75L) {
                CatgisLogger.info("[EMERGENCY-PERF] drawRasterLayer took " + elapsedMs + " ms"
                        + " layer=" + (layer != null ? layer.getName() : "<null>")
                        + " sourceCrs=" + (layer != null ? layer.getSourceCRS() : "")
                        + " displayCrs=" + data.getDisplayCRS()
                        + " image=" + data.getImage().getWidth() + "x" + data.getImage().getHeight()
                        + " draw=" + drawW + "x" + drawH
                        + " edt=" + javax.swing.SwingUtilities.isEventDispatchThread());
            }
        }
    }

    // ── 28. drawAllLabels ───────────────────────────────────────────────────

    void drawAllLabels(Graphics2D g2) {
        // Early return: check if ANY layer has labels enabled before iterating features
        boolean anyLabelsEnabled = false;
        for (Layer layer : map.layerManager.getRenderOrderLayers()) {
            if (layer != null && map.layerManager.isLayerEffectivelyVisible(layer)
                    && layer.isLabelsVisible()
                    && layer.getLabelField() != null && !layer.getLabelField().isBlank()
                    && layer.isLabelVisibleAtScale(map.getCurrentScaleDenominator())) {
                anyLabelsEnabled = true;
                break;
            }
        }
        if (!anyLabelsEnabled) return;

        map.globalLabelBoxes.clear();
        Object prevHint = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Layer layer : map.layerManager.getRenderOrderLayers()) {
            if (layer == null || !map.layerManager.isLayerEffectivelyVisible(layer)) continue;
            if (!layer.isLabelsVisible()) continue;
            if (layer.getLabelField() == null || layer.getLabelField().isBlank()) continue;
            if (!layer.isLabelVisibleAtScale(map.getCurrentScaleDenominator())) continue;

            ShapefileData shapeData = map.shapefileLayers.get(layer);
            if (shapeData == null) continue;
            org.geotools.data.simple.SimpleFeatureCollection collection = shapeData.getFeatureCollection();
            if (collection == null) continue;

            String labelExpr = layer.getLabelExpression();
            boolean useExpression = (labelExpr != null && !labelExpr.isBlank());
            String labelField = useExpression ? null : layer.getLabelField();

            List<Object[]> candidates = new ArrayList<>();
            map.forEachVisibleFeatureGeometry(List.of(layer), "", (currentLayer, featureGeometry) -> {
                String text;
                if (useExpression) {
                    text = LabelExpressionEngine.evaluate(labelExpr, featureGeometry.feature());
                } else {
                    Object attrValue = featureGeometry.feature().getAttribute(labelField);
                    text = attrValue != null ? String.valueOf(attrValue).trim() : "";
                }
                if (text == null || text.isEmpty()) return;

                Coordinate coord = map.getLabelCoordinate(featureGeometry.geometry());
                if (coord == null) return;

                int sx = map.worldToScreenX(coord.x);
                int sy = map.worldToScreenY(coord.y);
                String geomType = LabelPlacementEngine.resolveGeometryType(featureGeometry.geometry().getClass());
                candidates.add(new Object[]{text, sx, sy, geomType, currentLayer.getLabelPriority()});
            });

            if (candidates.isEmpty()) continue;

            List<LabelPlacementEngine.ResolvedLabel> resolved =
                    LabelPlacementEngine.resolveLabels(g2, layer, candidates, map.globalLabelBoxes);

            for (LabelPlacementEngine.ResolvedLabel rl : resolved) {
                drawResolvedLabel(g2, rl);
            }
        }

        if (prevHint != null) g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, prevHint);
    }

    // ── 29. drawLabelWithSettings ───────────────────────────────────────────

    void drawLabelWithSettings(Graphics2D g2, String text, int x, int y, Layer layer) {
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

        if (bgEnabled && bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(drawX - 4, drawY - fm.getAscent() - 2, tw + 8, th + 4, 6, 6);
        }

        if (haloEnabled && haloColor.getAlpha() > 0) {
            g2.setColor(haloColor);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(text, drawX + dx * haloWidth, drawY + dy * haloWidth);
        }

        g2.setColor(textColor);
        g2.drawString(text, drawX, drawY);

        if (underline) {
            g2.setColor(textColor);
            g2.drawLine(drawX, drawY + 2, drawX + tw, drawY + 2);
        }
    }

    // ── 30. drawResolvedLabel ───────────────────────────────────────────────

    void drawResolvedLabel(Graphics2D g2, LabelPlacementEngine.ResolvedLabel rl) {
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

        if (bgEnabled && bgColor.getAlpha() > 0) {
            g2.setColor(bgColor);
            g2.fillRoundRect(lx - 4, ly - fm.getAscent() - 2, tw + 8, th + 4, 6, 6);
        }

        if (haloEnabled && haloColor.getAlpha() > 0) {
            g2.setColor(haloColor);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(rl.text(), lx + dx * haloWidth, ly + dy * haloWidth);
        }

        g2.setColor(textColor);
        g2.drawString(rl.text(), lx, ly);

        if (underline) {
            g2.setColor(textColor);
            g2.drawLine(lx, ly + 2, lx + tw, ly + 2);
        }
    }
}
