/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.SystemLayerFinder;
import java.awt.Color;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class FenceLayerFinder
extends SystemLayerFinder {
    private static final Logger LOGGER = Logger.getLogger(FenceLayerFinder.class);
    public static final String LAYER_NAME = I18N.getString("workbench.model.FenceLayerFinder.fence");

    public FenceLayerFinder(LayerManagerProxy layerManagerProxy) {
        super(LAYER_NAME, layerManagerProxy);
    }

    public Geometry getFence() {
        try {
            if (this.getLayer() == null || this.getLayer().getFeatureCollectionWrapper().isEmpty()) {
                return null;
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
        return this.getLayer().getFeatureCollectionWrapper().getFeaturesSamples(1).get(0).getGeometry();
    }

    @Override
    protected void applyStyles(Layer layer) {
        layer.getBasicStyle().setLineColor(Color.blue);
        layer.getBasicStyle().setFillColor(Color.blue);
        layer.getBasicStyle().setAlpha(128);
        layer.getBasicStyle().setRenderingLine(true);
        layer.getBasicStyle().setRenderingFill(true);
        layer.setDrawingLast(true);
        layer.addStyles(layer.getStyles());
    }

    private Feature toFeature(Geometry fence, FeatureSchema schema) {
        Feature feature = FeatureUtil.toFeature(fence, schema);
        return feature;
    }

    public void clearFence() throws Exception {
        if (this.getLayer() == null) {
            this.createLayer();
        }
        FeatureCollection fc = this.getLayer().getUltimateFeatureCollectionWrapper();
        fc.clear();
        this.getLayer().fireAppearanceChanged();
    }

    public void setFence(Geometry fence) throws Exception {
        if (this.getLayer() == null) {
            this.createLayer();
        }
        if (fence != null) {
            FeatureCollection fc = this.getLayer().getUltimateFeatureCollectionWrapper();
            fc.clear();
            Feature feat = this.toFeature(fence, this.getLayer().getFeatureCollectionWrapper().getFeatureSchema());
            if (fc instanceof FeatureDataset) {
                ((FeatureDataset)fc).addWithNewKey(feat);
            } else {
                try {
                    fc.add(feat);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        }
        this.getLayer().fireAppearanceChanged();
    }

    public void dispose() {
        this.layerManagerProxy = null;
    }
}

