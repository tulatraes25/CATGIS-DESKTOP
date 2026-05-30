/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.plugin.AddNewLayerPlugIn;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;

public class FeatureDrawingUtil {
    public static final Color DRAWING_COLOR = Color.BLACK;
    public static final Stroke DRAWING_STROKE = new BasicStroke(2.0f);
    private LayerNamePanelProxy layerNamePanelProxy;
    private GeometryEditor editor = new GeometryEditor();

    public FeatureDrawingUtil(LayerNamePanelProxy layerNamePanelProxy) {
        this.layerNamePanelProxy = layerNamePanelProxy;
    }

    private Collection<Feature> selectedFeaturesContaining(Polygon polygon, LayerViewPanel panel) {
        if (this.layerNamePanelProxy.getLayerNamePanel().chooseEditableLayer() == null) {
            return new ArrayList<Feature>();
        }
        ArrayList<Feature> selectedFeaturesContainingPolygon = new ArrayList<Feature>();
        for (Feature feature : panel.getSelectionManager().getFeaturesWithSelectedItems(this.layerNamePanelProxy.getLayerNamePanel().chooseEditableLayer())) {
            if (feature.getGeometry().getClass() == GeometryCollection.class || !feature.getGeometry().getEnvelopeInternal().contains(polygon.getEnvelopeInternal()) || !feature.getGeometry().contains((Geometry)polygon)) continue;
            selectedFeaturesContainingPolygon.add(feature);
        }
        return selectedFeaturesContainingPolygon;
    }

    private void createHole(Polygon hole, Collection<Feature> features, Layer layer, LayerViewPanel panel, boolean rollingBackEdits, String transactionName) throws Exception {
        Assert.isTrue((hole.getNumInteriorRing() == 0 ? 1 : 0) != 0);
        EditTransaction transaction = new EditTransaction(features, transactionName, layer, rollingBackEdits, false, panel);
        int i = 0;
        while (i < transaction.size()) {
            transaction.setGeometry(i, transaction.getGeometry(i).difference((Geometry)hole));
            ++i;
        }
        transaction.commit();
    }

    public Layer layer(LayerViewPanel layerViewPanel) {
        if (this.layerNamePanelProxy.getLayerNamePanel().chooseEditableLayer() == null) {
            Layer layer = layerViewPanel.getLayerManager().addLayer(StandardCategoryNames.WORKING, I18N.getString("workbench.ui.cursortool.editing.FeatureDrawingUtil.new"), AddNewLayerPlugIn.createBlankFeatureCollection(5));
            layer.setEditable(true);
            layerViewPanel.getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.FeatureDrawingUtil.no-layer-is-editable-creating-new-editable-layer"));
        }
        return this.layerNamePanelProxy.getLayerNamePanel().chooseEditableLayer();
    }

    public UndoableCommand createAddCommand(Geometry geometry, boolean rollingBackEdits, LayerViewPanel layerViewPanel, AbstractCursorTool tool) {
        return this.createAddCommand(geometry, rollingBackEdits, layerViewPanel, tool, this.layer(layerViewPanel), null, null);
    }

    public UndoableCommand createAddCommand(Geometry geometry, boolean rollingBackEdits, LayerViewPanel layerViewPanel, AbstractCursorTool tool, String[] fields, Object[] values) {
        return this.createAddCommand(geometry, rollingBackEdits, layerViewPanel, tool, this.layer(layerViewPanel), fields, values);
    }

    public UndoableCommand createAddCommand(Geometry geometry, boolean rollingBackEdits, LayerViewPanel layerViewPanel, AbstractCursorTool tool, Layer targetLayer, String[] fields, Object[] values) {
        if (rollingBackEdits && !geometry.isValid()) {
            layerViewPanel.getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.FeatureDrawingUtil.draw-feature-tool-topology-error"));
            return null;
        }
        layerViewPanel.setViewportInitialized(true);
        final Layer layer = targetLayer;
        FeatureSchema schema = layer.getFeatureCollectionWrapper().getFeatureSchema();
        int geometryType = FeatureSchema.getGeometryType(geometry);
        if (geometryType != schema.getGeometryType() && schema.getGeometryType() != 15 && (geometryType == 5 ? schema.getGeometryType() != 4 : geometryType == 1 && schema.getGeometryType() != 8)) {
            return null;
        }
        if (schema.getClass().equals(FeatureSchema.class)) {
            Feature featureAux = null;
            Geometry geom = this.editor.removeRepeatedPoints(geometry);
            if (geometryType == 4) {
                GeometryFactory geomfac = new GeometryFactory();
                featureAux = FeatureUtil.toFeature((Geometry)geomfac.createMultiPolygon(new Polygon[]{(Polygon)geom}), schema);
            } else {
                featureAux = FeatureUtil.toFeature(geom, schema);
            }
            final Feature feature = featureAux;
            this.setFieldValues(feature, fields, values);
            return new UndoableCommand(tool.getName()){

                @Override
                public void execute() throws Exception {
                    JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().clear();
                    try {
                        layer.getFeatureCollectionWrapper().add(feature);
                        ArrayList<Feature> newFeaturesToSelect = new ArrayList<Feature>();
                        newFeaturesToSelect.add(feature);
                        JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(layer, newFeaturesToSelect);
                    }
                    catch (TopologyRelationException e) {
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    }
                }

                @Override
                public void unexecute() throws Exception {
                    layer.getFeatureCollectionWrapper().remove(feature);
                }
            };
        }
        final Feature feature = FeatureUtil.toFeature(this.editor.removeRepeatedPoints(geometry), layer.getFeatureCollectionWrapper().getFeatureSchema());
        this.setFieldValues(feature, fields, values);
        return new UndoableCommand(tool.getName()){

            @Override
            public void execute() throws Exception {
                JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().clear();
                layer.getFeatureCollectionWrapper().add(feature);
                JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(layer, feature);
            }

            @Override
            public void unexecute() throws Exception {
                layer.getFeatureCollectionWrapper().remove(feature);
            }
        };
    }

    private void setFieldValues(Feature feature, String[] fields, Object[] values) {
        if (fields != null && values != null && fields.length == values.length) {
            int i = 0;
            while (i < fields.length) {
                feature.setAttribute(fields[i], values[i]);
                ++i;
            }
        }
    }

    public CursorTool prepare(final AbstractCursorTool drawFeatureTool, boolean allowSnapping) {
        drawFeatureTool.setColor(DRAWING_COLOR);
        drawFeatureTool.setStroke(DRAWING_STROKE);
        if (allowSnapping) {
            drawFeatureTool.allowSnapping();
        }
        return new DelegatingTool(drawFeatureTool){

            @Override
            public String getName() {
                return drawFeatureTool.getName();
            }

            @Override
            public Cursor getCursor() {
                return new Cursor(1);
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void setActivate(boolean activate) {
            }

            @Override
            public boolean isActivate() {
                return false;
            }

            @Override
            public boolean checkConditions() {
                return true;
            }
        };
    }

    public void drawRing(Polygon polygon, boolean rollingBackEdits, AbstractCursorTool tool, LayerViewPanel panel) throws Exception {
        Collection<Feature> selectedFeaturesContainingPolygon = this.selectedFeaturesContaining(polygon, panel);
        if (selectedFeaturesContainingPolygon.isEmpty()) {
            AbstractPlugIn.execute(this.createAddCommand((Geometry)polygon, rollingBackEdits, panel, tool), panel);
        } else {
            this.createHole(polygon, selectedFeaturesContainingPolygon, this.layer(panel), panel, rollingBackEdits, tool.getName());
        }
    }
}

