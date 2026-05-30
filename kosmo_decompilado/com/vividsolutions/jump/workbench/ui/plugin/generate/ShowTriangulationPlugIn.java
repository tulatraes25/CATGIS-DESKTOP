/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 */
package com.vividsolutions.jump.workbench.ui.plugin.generate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.warp.Triangle;
import com.vividsolutions.jump.warp.Triangulator;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.warp.WarpingPanel;
import com.vividsolutions.jump.workbench.ui.warp.WarpingVectorLayerFinder;
import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class ShowTriangulationPlugIn
extends AbstractPlugIn {
    private static final Color GOLD = new Color(255, 192, 0, 150);
    private Triangulator triangulator = new Triangulator();
    private WarpingPanel warpingPanel;
    public static final String SOURCE_LAYER_NAME = I18N.getString("workbench.ui.plugin.generate.ShowTriangulationPlugIn.initial-triangulation");
    public static final String DESTINATION_LAYER_NAME = I18N.getString("workbench.ui.plugin.generate.ShowTriangulationPlugIn.final-triangulation");
    private static final String WARP_ID_NAME = "WARP_ID";
    private GeometryFactory factory = new GeometryFactory();

    public ShowTriangulationPlugIn(WarpingPanel warpingPanel) {
        this.warpingPanel = warpingPanel;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    public EnableCheck createEnableCheck(WorkbenchContext context) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(context);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
    }

    private Layer sourceLayer(LayerManagerProxy layerManagerProxy) {
        return layerManagerProxy.getLayerManager().getLayer(SOURCE_LAYER_NAME);
    }

    private Layer destinationLayer(LayerManagerProxy layerManagerProxy) {
        return layerManagerProxy.getLayerManager().getLayer(DESTINATION_LAYER_NAME);
    }

    private WarpingVectorLayerFinder warpingVectorLayerFinder(LayerManagerProxy proxy) {
        return new WarpingVectorLayerFinder(proxy);
    }

    private Envelope envelopeOfTails(Collection vectors) {
        Envelope envelope = new Envelope();
        for (LineString vector : vectors) {
            envelope.expandToInclude(vector.getCoordinateN(0));
        }
        return envelope;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        context.getLayerManager().getUndoableEditReceiver().reportNothingToUndoYet();
        this.execute(this.createCommand(context.getWorkbenchContext(), true), context);
        return true;
    }

    private UndoableCommand createCommand(final WorkbenchContext context, final boolean createLayersIfNonExistent) throws Exception {
        Envelope datasetEnvelope = new Envelope();
        if (this.warpingPanel.currentSourceLayer() != null) {
            datasetEnvelope = this.warpingPanel.currentSourceLayer().getFeatureCollectionWrapper().getEnvelope();
        }
        if (datasetEnvelope.isNull()) {
            datasetEnvelope = this.envelopeOfTails(this.warpingVectorLayerFinder(context).getVectors());
        }
        if (datasetEnvelope.isNull()) {
            return UndoableCommand.DUMMY;
        }
        if (datasetEnvelope.getWidth() == 0.0) {
            datasetEnvelope.expandToInclude(new Coordinate(datasetEnvelope.getMinX() + 1.0, datasetEnvelope.getMinY()));
            datasetEnvelope.expandToInclude(new Coordinate(datasetEnvelope.getMinX() - 1.0, datasetEnvelope.getMinY()));
        }
        if (datasetEnvelope.getHeight() == 0.0) {
            datasetEnvelope.expandToInclude(new Coordinate(datasetEnvelope.getMinX(), datasetEnvelope.getMinY() + 1.0));
            datasetEnvelope.expandToInclude(new Coordinate(datasetEnvelope.getMinX(), datasetEnvelope.getMinY() - 1.0));
        }
        Map<Triangle, Triangle> triangleMap = this.triangulator.triangleMap(datasetEnvelope, this.warpingVectorLayerFinder(context).getVectors(), new DummyTaskMonitor());
        List[] sourceAndDestinationTriangles = CollectionUtil.keysAndCorrespondingValues(triangleMap);
        final FeatureCollection sourceFeatureCollection = this.toFeatureCollection(sourceAndDestinationTriangles[0]);
        final FeatureCollection destinationFeatureCollection = this.toFeatureCollection(sourceAndDestinationTriangles[1]);
        return ShowTriangulationPlugIn.addUndo(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                if (ShowTriangulationPlugIn.this.sourceLayer(context) != null) {
                    ShowTriangulationPlugIn.this.sourceLayer(context).setFeatureCollection(sourceFeatureCollection);
                    ShowTriangulationPlugIn.this.sourceLayer(context).setVisible(true);
                }
                if (ShowTriangulationPlugIn.this.sourceLayer(context) == null && createLayersIfNonExistent) {
                    Layer sourceLayer = context.getLayerManager().addLayer(StandardCategoryNames.WORKING, SOURCE_LAYER_NAME, sourceFeatureCollection);
                    ShowTriangulationPlugIn.this.init(sourceLayer, Color.gray, 150, 1);
                }
                if (ShowTriangulationPlugIn.this.destinationLayer(context) != null) {
                    ShowTriangulationPlugIn.this.destinationLayer(context).setFeatureCollection(destinationFeatureCollection);
                    ShowTriangulationPlugIn.this.destinationLayer(context).setVisible(true);
                }
                if (ShowTriangulationPlugIn.this.destinationLayer(context) == null && createLayersIfNonExistent) {
                    Layer destinationLayer = context.getLayerManager().addLayer(StandardCategoryNames.WORKING, DESTINATION_LAYER_NAME, destinationFeatureCollection);
                    ShowTriangulationPlugIn.this.init(destinationLayer, GOLD, 255, 1);
                }
            }

            @Override
            public void unexecute() throws Exception {
            }
        }, context);
    }

    public UndoableCommand addLayerGeneration(final UndoableCommand wrappeeCommand, final WorkbenchContext context, final boolean createLayersIfNonExistent) {
        return new UndoableCommand(wrappeeCommand.getName()){
            private UndoableCommand layerGenerationCommand;
            {
                super($anonymous0);
                this.layerGenerationCommand = null;
            }

            private UndoableCommand layerGenerationCommand() throws Exception {
                if (this.layerGenerationCommand == null) {
                    this.layerGenerationCommand = ShowTriangulationPlugIn.this.createCommand(context, createLayersIfNonExistent);
                }
                return this.layerGenerationCommand;
            }

            @Override
            public void execute() throws Exception {
                wrappeeCommand.execute();
                this.layerGenerationCommand().execute();
            }

            @Override
            public void unexecute() throws Exception {
                this.layerGenerationCommand().unexecute();
                wrappeeCommand.unexecute();
            }
        };
    }

    public static UndoableCommand addUndo(UndoableCommand wrappeeCommand, LayerManagerProxy proxy) throws Exception {
        return Layer.addUndo(DESTINATION_LAYER_NAME, proxy, Layer.addUndo(SOURCE_LAYER_NAME, proxy, wrappeeCommand));
    }

    private FeatureCollection toFeatureCollection(Collection triangles) throws Exception {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        featureSchema.addAttribute(WARP_ID_NAME, AttributeType.INTEGER, new Boolean(true));
        FeatureDataset featureCollection = new FeatureDataset(featureSchema);
        int j = 0;
        for (Triangle t : triangles) {
            Feature feature = FeatureUtil.toFeature((Geometry)this.factory.createPolygon(t.toLinearRing(), null), featureSchema);
            feature.setAttribute(WARP_ID_NAME, (Object)new Integer(++j));
            featureCollection.add(feature);
        }
        return featureCollection;
    }

    private void init(Layer layer, Color color, int alpha, int lineWidth) {
        boolean firingEvents = layer.getLayerManager().isFiringEvents();
        layer.getLayerManager().setFiringEvents(false);
        try {
            layer.getBasicStyle().setLineColor(color);
            layer.getBasicStyle().setFillColor(color);
            layer.getBasicStyle().setAlpha(alpha);
            layer.getBasicStyle().setLineWidth(lineWidth);
            layer.getBasicStyle().setRenderingFill(false);
            layer.getVertexStyle().setEnabled(true);
            layer.getVertexStyle().setSize(4);
            layer.getLabelStyle().setEnabled(true);
            layer.getLabelStyle().setColor(color);
            layer.getLabelStyle().setFont(new Font("Dialog", 0, 12));
            layer.getLabelStyle().setAttribute(WARP_ID_NAME);
            layer.getLabelStyle().setHeight(12.0);
            layer.getLabelStyle().setScaling(false);
            layer.getLabelStyle().setHidingOverlappingLabels(false);
        }
        finally {
            layer.getLayerManager().setFiringEvents(firingEvents);
        }
        layer.fireAppearanceChanged();
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("Triangle.gif");
    }
}

