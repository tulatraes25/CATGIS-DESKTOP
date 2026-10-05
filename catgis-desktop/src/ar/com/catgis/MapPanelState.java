package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.data.vector.ShapefileData;
import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.awt.Graphics2D;
import java.util.List;

/**
 * Read-only interface for accessing MapPanel state.
 * Used by extracted classes to avoid direct coupling to MapPanel.
 */
public interface MapPanelState {
    String getDrawingMode();
    List<Coordinate> getDrawingCoordinates();
    Layer getSelectedLayer();
    SimpleFeature getSelectedFeature();
    Layer getActiveVectorEditingLayer();
    ShapefileData getShapefileData(Layer layer);
    double getWorldToScreenX(double worldX);
    double getWorldToScreenY(double worldY);
    double getScreenToWorldX(int screenX);
    double getScreenToWorldY(int screenY);
    int getMapWidth();
    int getMapHeight();
    String getCurrentScaleDenominator();
    Envelope getCurrentViewEnvelope();
    Coordinate toProjectCoordinate(Coordinate c, Layer layer);
    Geometry reprojectGeometryIfNeeded(Geometry geometry, String sourceCrs);
    List<Layer> getVisibleLayers();
    List<Layer> getSnapCandidateLayers();
}
