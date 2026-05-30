/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;

public class LayerListenerImplement
implements LayerListener {
    protected Layer layer;

    public LayerListenerImplement(Layer layer) {
        this.layer = layer;
    }

    @Override
    public void featuresChanged(FeatureEvent e) {
        if (e.getLayer() == this.layer) {
            this.layer.setFeatureCollectionModified(true);
            if (e.getType() != FeatureEventType.ATTRIBUTES_MODIFIED || this.layer.getBlackboard().getBoolean(Layer.FIRING_APPEARANCE_CHANGED_ON_ATTRIBUTE_CHANGE)) {
                this.layer.fireAppearanceChanged();
            }
        }
    }

    public Layer getLayer() {
        return this.layer;
    }

    @Override
    public void layerChanged(LayerEvent e) {
        if (e.getLayerable() == this.layer && e.getType() == LayerEventType.COMMITED) {
            this.layer.fireAppearanceChanged();
        }
    }

    @Override
    public void categoryChanged(CategoryEvent e) {
    }

    public void dispose() {
        this.layer = null;
    }
}

