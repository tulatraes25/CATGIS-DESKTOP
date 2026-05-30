/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Collection;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class OnlySinglePartFeaturesTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "Only single part features";
    public static final String NAME = I18N.getString(OnlySinglePartFeaturesTopologyRelation.class, "only-single-part-features");

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
        return I18N.getString(this.getClass(), "allows-to-check-that-there-are-no-elements-of-type-multi-xxx-in-a-layer-elements-with-geometries-of-types-multiple-multipoint-multiline-or-multipolygon-will-be-marked-as-not-valid");
    }

    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        return feat.getGeometry().getNumGeometries() == 1;
    }
}

