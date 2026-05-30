/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Collection;
import org.saig.core.model.relations.topology.AbstractTopologyBinaryRelation;
import org.saig.jump.lang.I18N;

public class BboxTopologyRelation
extends AbstractTopologyBinaryRelation {
    public static final String ID = "Bbox";
    public static final String NAME = I18N.getString(BboxTopologyRelation.class, "bounding-box");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return I18N.getString(this.getClass(), "this-operator-allows-to-check-that-for-each-element-in-the-source-layer-bounding-box-its-geometry-intersects-with-at-least-one-bounding-box-of-any-geometry-in-the-target-layer-id-est-the-bounding-boxes-share-at-least-one-common-point");
    }

    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) throws Exception {
        return fcTarget.query(feat.getGeometry().getEnvelopeInternal(), this.entryTargetFilter).size() > 0;
    }
}

