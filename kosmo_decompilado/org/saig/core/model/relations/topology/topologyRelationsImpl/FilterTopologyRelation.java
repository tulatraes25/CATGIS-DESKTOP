/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Collection;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class FilterTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "no-topological-relation";
    public static final String NAME = I18N.getString(FilterTopologyRelation.class, "no-topological-relation");

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
        return I18N.getString(this.getClass(), "only-considers-alphanumeric-and-or-geospatial-conditions");
    }

    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        return true;
    }
}

