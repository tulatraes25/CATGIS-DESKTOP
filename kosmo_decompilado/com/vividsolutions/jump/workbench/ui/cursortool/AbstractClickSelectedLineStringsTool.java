/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import org.saig.jump.lang.I18N;

public abstract class AbstractClickSelectedLineStringsTool
extends SpecifyFeaturesTool {
    protected static final String NO_SELECTED_LINESTRINGS_HERE_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.AbstractClickSelectedLineStringsTool.there-is-no-selected-lines-here");

    public AbstractClickSelectedLineStringsTool() {
        this.setViewClickBuffer(10);
    }

    protected void warnLayerNotEditable(Layer layer) {
        this.getWorkbench().getFrame().warnUser(I18N.getMessage("com.vividsolutions.jump.workbench.ui.cursortool.AbstractClickSelectedLineStringsTool.The-layer-{0}-is-not-editable", new Object[]{layer.getName()}));
    }

    @Override
    public String getName() {
        return super.getName().replaceAll("Line String", "LineString");
    }

    @Override
    protected Coordinate getModelSource() {
        return this.getModelDestination();
    }

    @Override
    protected Shape getShape(Point2D source, Point2D destination) throws Exception {
        return null;
    }

    protected Point getModelClickPoint() {
        return geomFac.createPoint(this.getModelDestination());
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        if (!this.check(this.checkFactory().createAtLeastNItemsMustBeSelectedCheck(1))) {
            return;
        }
        Collection<Feature> nearbyLineStringFeatures = CollectionUtil.select(CollectionUtil.concatenate(this.layerToSpecifiedFeaturesMap().values()), new Block(){

            @Override
            public Object yield(Object feature) {
                Feature feat = (Feature)feature;
                boolean isSimpleOrMultiLineStringFeature = feat.getGeometry() instanceof LineString || feat.getGeometry() instanceof MultiLineString;
                return AbstractClickSelectedLineStringsTool.this.getPanel().getSelectionManager().getFeaturesWithSelectedItems().contains(feature) && isSimpleOrMultiLineStringFeature ? Boolean.TRUE : Boolean.FALSE;
            }
        });
        if (nearbyLineStringFeatures.isEmpty()) {
            this.getWorkbench().getFrame().warnUser(NO_SELECTED_LINESTRINGS_HERE_MESSAGE);
            return;
        }
        this.gestureFinished(nearbyLineStringFeatures);
    }

    private EnableCheckFactory checkFactory() {
        return new EnableCheckFactory(this.getWorkbench().getContext());
    }

    protected abstract void gestureFinished(Collection<Feature> var1) throws Exception;

    protected Layer layer(Feature feature, Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap) {
        for (Layer layer : layerToSpecifiedFeaturesMap.keySet()) {
            Collection<Feature> features = layerToSpecifiedFeaturesMap.get(layer);
            if (!features.contains(feature)) continue;
            return layer;
        }
        Assert.shouldNeverReachHere();
        return null;
    }
}

