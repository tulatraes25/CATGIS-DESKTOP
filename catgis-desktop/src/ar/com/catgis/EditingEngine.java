package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages vector feature editing operations.
 * Extracted from MapPanel to reduce its responsibilities.
 */
public class EditingEngine {

    private boolean editMode = false;
    private Layer editingLayer = null;
    private SimpleFeature editingFeature = null;
    private final List<Geometry> editSketchParts = new ArrayList<>();
    private Geometry editSketchGeometry = null;
    private boolean vertexEditActive = false;
    private int editingVertexIndex = -1;

    // --- Getters ---

    public boolean isEditMode() { return editMode; }
    public Layer getEditingLayer() { return editingLayer; }
    public SimpleFeature getEditingFeature() { return editingFeature; }
    public List<Geometry> getEditSketchParts() { return editSketchParts; }
    public Geometry getEditSketchGeometry() { return editSketchGeometry; }
    public boolean isVertexEditActive() { return vertexEditActive; }
    public int getEditingVertexIndex() { return editingVertexIndex; }

    // --- Edit mode ---

    public void startEditing(Layer layer, SimpleFeature feature) {
        editMode = true;
        editingLayer = layer;
        editingFeature = feature;
        editSketchParts.clear();
        editSketchGeometry = null;
    }

    public void stopEditing() {
        editMode = false;
        editingLayer = null;
        editingFeature = null;
        editSketchParts.clear();
        editSketchGeometry = null;
        vertexEditActive = false;
        editingVertexIndex = -1;
    }

    // --- Sketch ---

    public void addSketchPoint(double worldX, double worldY) {
        if (!editMode) return;
        GeometryFactory gf = new GeometryFactory();
        editSketchParts.add(gf.createPoint(new Coordinate(worldX, worldY)));
    }

    public void addSketchLinePoint(double worldX, double worldY) {
        if (!editMode) return;
        if (editSketchParts.isEmpty()) {
            GeometryFactory gf = new GeometryFactory();
            editSketchParts.add(gf.createPoint(new Coordinate(worldX, worldY)));
        } else {
            Geometry last = editSketchParts.get(editSketchParts.size() - 1);
            if (last instanceof Point p) {
                GeometryFactory gf = new GeometryFactory();
                editSketchParts.set(0, gf.createLineString(new Coordinate[]{
                        p.getCoordinate(), new Coordinate(worldX, worldY)}));
            }
        }
    }

    public void finishSketch() {
        if (editSketchParts.isEmpty()) return;
        GeometryFactory gf = new GeometryFactory();
        if (editSketchParts.size() == 1) {
            editSketchGeometry = editSketchParts.get(0);
        } else {
            List<Coordinate> coords = new ArrayList<>();
            for (Geometry g : editSketchParts) {
                if (g instanceof Point p) coords.add(p.getCoordinate());
                else if (g instanceof LineString ls) {
                    for (Coordinate c : ls.getCoordinates()) coords.add(c);
                }
            }
            if (coords.size() >= 2) {
                editSketchGeometry = gf.createLineString(coords.toArray(new Coordinate[0]));
            }
        }
    }

    public void cancelSketch() {
        editSketchParts.clear();
        editSketchGeometry = null;
    }

    // --- Vertex editing ---

    public void startVertexEdit(int vertexIndex) {
        vertexEditActive = true;
        editingVertexIndex = vertexIndex;
    }

    public void moveVertex(Geometry geometry, int vertexIndex, double newX, double newY) {
        if (geometry == null || vertexIndex < 0) return;
        Coordinate[] coords = geometry.getCoordinates();
        if (vertexIndex >= coords.length) return;
        coords[vertexIndex].x = newX;
        coords[vertexIndex].y = newY;
    }

    public void stopVertexEdit() {
        vertexEditActive = false;
        editingVertexIndex = -1;
    }
}
