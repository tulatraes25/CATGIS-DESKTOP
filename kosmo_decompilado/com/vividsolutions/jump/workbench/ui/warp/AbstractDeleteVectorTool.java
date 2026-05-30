/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.model.AbstractVectorLayerFinder;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractDeleteVectorTool
extends SpecifyFeaturesTool {
    public AbstractDeleteVectorTool() {
        this.setViewClickBuffer(6);
    }

    protected void showAnimation(Collection<Feature> vectorFeatures) {
        try {
            Animations.drawExpandingRings(this.getPanel().getViewport().toViewPoints(this.centres(vectorFeatures)), true, this.getColor(), this.getPanel(), new float[]{15.0f, 15.0f});
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            // empty catch block
        }
    }

    private Collection<Coordinate> centres(Collection<Feature> vectorFeatures) {
        ArrayList<Coordinate> centers = new ArrayList<Coordinate>();
        for (Feature vectorFeature : vectorFeatures) {
            Envelope envelope = vectorFeature.getGeometry().getEnvelopeInternal();
            if (envelope.isNull()) continue;
            centers.add(EnvelopeUtil.centre(envelope));
        }
        return centers;
    }

    protected abstract AbstractVectorLayerFinder createVectorLayerFinder(LayerManagerProxy var1);

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        AbstractVectorLayerFinder finder = this.createVectorLayerFinder(this.getPanel());
        if (finder.getLayer() == null) {
            return;
        }
        if (!this.layerToSpecifiedFeaturesMap().containsKey(finder.getLayer())) {
            return;
        }
        this.execute(this.createCommand());
    }

    protected UndoableCommand createCommand() throws Exception {
        final AbstractVectorLayerFinder finder = this.createVectorLayerFinder(this.getPanel());
        final Collection<Feature> vectorFeaturesToDelete = this.layerToSpecifiedFeaturesMap().get(finder.getLayer());
        Assert.isTrue((vectorFeaturesToDelete != null ? 1 : 0) != 0);
        Assert.isTrue((!vectorFeaturesToDelete.isEmpty() ? 1 : 0) != 0);
        return new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                finder.getLayer().getFeatureCollectionWrapper().removeAll(vectorFeaturesToDelete);
                AbstractDeleteVectorTool.this.showAnimation(vectorFeaturesToDelete);
            }

            @Override
            public void unexecute() throws Exception {
                finder.getLayer().getFeatureCollectionWrapper().addAll(vectorFeaturesToDelete);
            }
        };
    }
}

