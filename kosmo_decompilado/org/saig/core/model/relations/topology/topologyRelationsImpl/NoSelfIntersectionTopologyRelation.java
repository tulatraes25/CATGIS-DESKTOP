/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Collection;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class NoSelfIntersectionTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "No self intersection";
    public static final String NAME = I18N.getString(NoSelfIntersectionTopologyRelation.class, "no-self-intersection");

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
        return I18N.getString(this.getClass(), "this-operator-allows-to-check-that-do-not-exist-elements-in-a-layer-that-self-intersect-id-est-they-do-not-form-loops");
    }

    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        return feat.getGeometry().isSimple();
    }

    @Override
    public boolean checkValidGeometryType(int geomType) {
        return geomType == 3 || geomType == 2;
    }
}

