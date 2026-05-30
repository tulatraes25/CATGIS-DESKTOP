/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import java.util.Collections;

public class FeatureEvent {
    private Layer layer;
    private FeatureEventType type;
    private Collection<Feature> features;
    private Collection<Feature> oldFeatureClones;

    public FeatureEvent(Collection<Feature> features, FeatureEventType type, Layer layer, Collection<Feature> oldFeatureClones) {
        Assert.isTrue((layer != null ? 1 : 0) != 0);
        Assert.isTrue((type != null ? 1 : 0) != 0);
        Assert.isTrue((type == FeatureEventType.GEOMETRY_MODIFIED && oldFeatureClones != null || type != FeatureEventType.GEOMETRY_MODIFIED && oldFeatureClones == null ? 1 : 0) != 0);
        this.layer = layer;
        this.type = type;
        this.features = features;
        this.oldFeatureClones = oldFeatureClones;
    }

    public Layer getLayer() {
        return this.layer;
    }

    public FeatureEventType getType() {
        return this.type;
    }

    public Collection<Feature> getFeatures() {
        return Collections.unmodifiableCollection(this.features);
    }

    public Collection<Feature> getOldFeatureClones() {
        return Collections.unmodifiableCollection(this.oldFeatureClones);
    }
}

