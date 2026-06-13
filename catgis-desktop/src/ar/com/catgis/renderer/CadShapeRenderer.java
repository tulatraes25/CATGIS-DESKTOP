package ar.com.catgis.renderer;

import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * CAD-style geometry renderer: wireframe, vertices, selection.
 * Uses JTS ShapeWriter for geometry-to-Shape conversion.
 * No OpenJUMP dependencies — pure JTS + Java2D.
 */
public final class CadShapeRenderer {

    private final ShapeWriter shapeWriter = new ShapeWriter();
    private final AffineTransform worldToScreen;

    private Color edgeColor = new Color(0, 120, 212);       // blue edges
    private Color vertexColor = new Color(212, 0, 0);       // red vertices
    private Color selectionColor = new Color(255, 200, 0);  // yellow selection
    private Color fillColor = new Color(0, 120, 212, 40);   // transparent blue fill

    private float edgeWidth = 2f;
    private float selectionWidth = 4f;
    private int vertexRadius = 4;
    private boolean showVertices = true;
    private boolean selected = false;

    public CadShapeRenderer(AffineTransform worldToScreen) {
        this.worldToScreen = (AffineTransform) worldToScreen.clone();
    }

    // ─── Configuration ──────────────────────────────────────────────

    public CadShapeRenderer setEdgeColor(Color c) { edgeColor = c; return this; }
    public CadShapeRenderer setVertexColor(Color c) { vertexColor = c; return this; }
    public CadShapeRenderer setSelectionColor(Color c) { selectionColor = c; return this; }
    public CadShapeRenderer setFillColor(Color c) { fillColor = c; return this; }
    public CadShapeRenderer setEdgeWidth(float w) { edgeWidth = w; return this; }
    public CadShapeRenderer setVertexRadius(int r) { vertexRadius = r; return this; }
    public CadShapeRenderer setShowVertices(boolean v) { showVertices = v; return this; }
    public CadShapeRenderer setSelected(boolean s) { selected = s; return this; }

    // ─── Rendering ──────────────────────────────────────────────────

    /**
     * Render a geometry in CAD wireframe style.
     */
    public void render(Graphics2D g2, Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) return;

        Shape shape = shapeWriter.toShape(geometry);
        AffineTransform oldTransform = g2.getTransform();
        g2.transform(worldToScreen);

        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            renderPolygon(g2, shape, geometry);
        } else if (geometry instanceof LineString || geometry instanceof MultiLineString) {
            renderLine(g2, shape);
        } else if (geometry instanceof Point) {
            renderPoint(g2, geometry);
        } else {
            // Generic: draw outline
            g2.setColor(edgeColor);
            g2.setStroke(new BasicStroke(edgeWidth));
            g2.draw(shape);
        }

        g2.setTransform(oldTransform);
    }

    private void renderPolygon(Graphics2D g2, Shape shape, Geometry geometry) {
        Graphics2D g = (Graphics2D) g2.create();
        // Transparent fill
        g.setColor(fillColor);
        g.fill(shape);

        // Selection highlight
        if (selected) {
            g.setColor(selectionColor);
            g.setStroke(new BasicStroke(selectionWidth));
            g.draw(shape);
        }

        // Wireframe edges
        g.setColor(edgeColor);
        g.setStroke(new BasicStroke(edgeWidth));
        g.draw(shape);

        // Vertices
        if (showVertices) {
            g.setColor(vertexColor);
            for (Coordinate c : geometry.getCoordinates()) {
                int sx = (int) (c.x);
                int sy = (int) (c.y);
                g.fillOval(sx - vertexRadius, sy - vertexRadius, vertexRadius * 2, vertexRadius * 2);
            }
        }
        g.dispose();
    }

    private void renderLine(Graphics2D g2, Shape shape) {
        Graphics2D g = (Graphics2D) g2.create();

        if (selected) {
            g.setColor(selectionColor);
            g.setStroke(new BasicStroke(selectionWidth));
            g.draw(shape);
        }

        g.setColor(edgeColor);
        g.setStroke(new BasicStroke(edgeWidth));
        g.draw(shape);

        g.dispose();
    }

    private void renderPoint(Graphics2D g2, Geometry geometry) {
        Coordinate c = geometry.getCoordinate();
        int sx = (int) c.x;
        int sy = (int) c.y;
        Graphics2D g = (Graphics2D) g2.create();
        g.setColor(selected ? selectionColor : vertexColor);
        int r = selected ? vertexRadius + 2 : vertexRadius;
        g.fillOval(sx - r, sy - r, r * 2, r * 2);
        g.dispose();
    }

    // ─── Static convenience ─────────────────────────────────────────

    /**
     * Quick CAD-style rendering without builder.
     */
    public static void renderCad(Graphics2D g2, Geometry geometry, AffineTransform transform, boolean selected) {
        new CadShapeRenderer(transform)
                .setSelected(selected)
                .render(g2, geometry);
    }
}
