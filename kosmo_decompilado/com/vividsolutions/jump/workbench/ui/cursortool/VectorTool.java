/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public abstract class VectorTool
extends NClickTool {
    public VectorTool() {
        super(2);
        this.setStroke(new BasicStroke(1.0f));
        this.allowSnapping();
    }

    protected Feature feature(LineString lineString, Layer layer, UndoableCommand command) {
        Feature feature = FeatureUtil.toFeature((Geometry)lineString, layer.getFeatureCollectionWrapper().getFeatureSchema());
        feature.setGeometry((Geometry)lineString);
        return feature;
    }

    protected LineString lineString(Coordinate source, Coordinate destination) throws NoninvertibleTransformException {
        return geomFac.createLineString(new Coordinate[]{source, destination});
    }

    protected Shape getShape(Point2D source, Point2D destination) {
        return new Line2D.Double(source, destination);
    }

    protected abstract AbstractVectorLayerFinder createVectorLayerFinder(LayerManagerProxy var1);

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        this.getPanel().setViewportInitialized(true);
        this.execute(this.createCommand());
    }

    protected UndoableCommand createCommand() throws NoninvertibleTransformException {
        final AbstractVectorLayerFinder vectorLayerFinder = this.createVectorLayerFinder(this.getPanel());
        final boolean vectorLayerExistedOriginally = vectorLayerFinder.getLayer() != null;
        final LineString lineString = this.lineString(this.getModelSource(), this.getModelDestination());
        return new UndoableCommand(this.getName()){
            private Feature vector;
            private boolean vectorLayerVisibleOriginally;

            @Override
            public void execute() throws Exception {
                if (!vectorLayerExistedOriginally) {
                    vectorLayerFinder.createLayer();
                }
                if (this.vector == null) {
                    this.vector = VectorTool.this.feature(lineString, vectorLayerFinder.getLayer(), this);
                }
                vectorLayerFinder.getLayer().getFeatureCollectionWrapper().add(this.vector);
                this.vectorLayerVisibleOriginally = vectorLayerFinder.getLayer().isVisible();
                vectorLayerFinder.getLayer().setVisible(true);
            }

            @Override
            public void unexecute() throws Exception {
                vectorLayerFinder.getLayer().setVisible(this.vectorLayerVisibleOriginally);
                vectorLayerFinder.getLayer().getFeatureCollectionWrapper().remove(this.vector);
                if (!vectorLayerExistedOriginally) {
                    VectorTool.this.getPanel().getLayerManager().remove(vectorLayerFinder.getLayer());
                }
            }
        };
    }
}

