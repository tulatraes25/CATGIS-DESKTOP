package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Path2D;

/**
 * Renders vector features on the map.
 * Extracted from MapPanel to reduce its responsibilities.
 */
public class FeatureRenderer {

    private final MapViewController viewController;

    public FeatureRenderer(MapViewController viewController) {
        this.viewController = viewController;
    }

    /**
     * Render a geometry with the given layer's symbology.
     */
    public void renderGeometry(Graphics2D g2, Geometry geometry, Layer layer, SimpleFeature feature) {
        if (geometry == null || geometry.isEmpty()) return;

        if (geometry instanceof Point point) {
            renderPoint(g2, point, layer, feature);
        } else if (geometry instanceof MultiPoint mp) {
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Point p) renderPoint(g2, p, layer, feature);
            }
        } else if (geometry instanceof LineString ls) {
            renderLineString(g2, ls, layer, feature);
        } else if (geometry instanceof MultiLineString mls) {
            for (int i = 0; i < mls.getNumGeometries(); i++) {
                Geometry g = mls.getGeometryN(i);
                if (g instanceof LineString line) renderLineString(g2, line, layer, feature);
            }
        } else if (geometry instanceof Polygon polygon) {
            renderPolygon(g2, polygon, layer, feature);
        } else if (geometry instanceof MultiPolygon mp) {
            for (int i = 0; i < mp.getNumGeometries(); i++) {
                Geometry g = mp.getGeometryN(i);
                if (g instanceof Polygon poly) renderPolygon(g2, poly, layer, feature);
            }
        }
    }

    private void renderPoint(Graphics2D g2, Point point, Layer layer, SimpleFeature feature) {
        int x = viewController.worldToScreenX(point.getX());
        int y = viewController.worldToScreenY(point.getY());

        CategoryStyleRule categoryRule = resolveCategoryRule(layer.getPointCategorizedSymbology(), feature);
        int size = Math.max(4, categoryRule != null ? categoryRule.getPointSize() : layer.getPointSize());
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

    private void renderLineString(Graphics2D g2, LineString line, Layer layer, SimpleFeature feature) {
        Coordinate[] coords = line.getCoordinates();
        if (coords.length < 2) return;

        Path2D path = new Path2D.Double();
        path.moveTo(viewController.worldToScreenX(coords[0].x), viewController.worldToScreenY(coords[0].y));
        for (int i = 1; i < coords.length; i++) {
            path.lineTo(viewController.worldToScreenX(coords[i].x), viewController.worldToScreenY(coords[i].y));
        }

        CategoryStyleRule categoryRule = resolveCategoryRule(layer.getLineCategorizedSymbology(), feature);
        Color lineColor = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getLineColor();
        Layer.LineSymbolStyle lineStyle = categoryRule != null ? categoryRule.getLineStyle() : layer.getLineSymbolStyle();
        float lineWidth = categoryRule != null ? categoryRule.getLineWidth() : layer.getLineWidth();

        g2.setColor(lineColor);
        g2.setStroke(LineSymbolRenderer.buildStroke(lineStyle, lineWidth));
        g2.draw(path);
    }

    private void renderPolygon(Graphics2D g2, Polygon polygon, Layer layer, SimpleFeature feature) {
        CategoryStyleRule categoryRule = resolveCategoryRule(layer.getPolygonCategorizedSymbology(), feature);
        Color fillColor = categoryRule != null ? categoryRule.getPrimaryColor() : layer.getFillColor();
        Color borderColor = layer.getBorderColor();
        Layer.PolygonFillStyle fillStyle = categoryRule != null ? categoryRule.getPolygonFillStyle() : layer.getPolygonFillStyle();

        // Exterior ring
        Coordinate[] exteriorCoords = polygon.getExteriorRing().getCoordinates();
        if (exteriorCoords.length < 3) return;
        Path2D exterior = new Path2D.Double();
        exterior.moveTo(viewController.worldToScreenX(exteriorCoords[0].x), viewController.worldToScreenY(exteriorCoords[0].y));
        for (int i = 1; i < exteriorCoords.length; i++) {
            exterior.lineTo(viewController.worldToScreenX(exteriorCoords[i].x), viewController.worldToScreenY(exteriorCoords[i].y));
        }
        exterior.closePath();

        // Fill
        Paint paint = PolygonSymbolRenderer.buildPaint(fillStyle, fillColor, borderColor, 12);
        g2.setPaint(paint);
        g2.fill(exterior);

        // Border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(Math.max(0.5f, layer.getLineWidth())));
        g2.draw(exterior);

        // Interior rings (holes)
        for (int r = 0; r < polygon.getNumInteriorRing(); r++) {
            Coordinate[] holeCoords = polygon.getInteriorRingN(r).getCoordinates();
            if (holeCoords.length < 3) continue;
            Path2D hole = new Path2D.Double();
            hole.moveTo(viewController.worldToScreenX(holeCoords[0].x), viewController.worldToScreenY(holeCoords[0].y));
            for (int i = 1; i < holeCoords.length; i++) {
                hole.lineTo(viewController.worldToScreenX(holeCoords[i].x), viewController.worldToScreenY(holeCoords[i].y));
            }
            hole.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(hole);
            g2.setColor(borderColor);
            g2.draw(hole);
        }
    }

    private CategoryStyleRule resolveCategoryRule(CategorizedSymbology symbology, SimpleFeature feature) {
        if (symbology == null || symbology.getRules().isEmpty()) return null;
        String value = null;
        try {
            String fieldName = symbology.getFieldName();
            if (fieldName != null && !fieldName.isBlank()) {
                Object v = feature.getAttribute(fieldName);
                if (v != null) value = v.toString();
            }
        } catch (Exception ignored) {}
        if (value == null) return null;
        for (CategoryStyleRule rule : symbology.getRules().values()) {
            if (value.equals(rule.getValue())) return rule;
        }
        return null;
    }
}
