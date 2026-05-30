/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Collection;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.AbstractTopologyBinaryRelation;
import org.saig.jump.lang.I18N;

public class OverlapsTopologyRelation
extends AbstractTopologyBinaryRelation {
    public static final String ID = "Overlaps";
    public static final String NAME = I18N.getString(OverlapsTopologyRelation.class, "overlaps-with");

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
        return String.valueOf(I18N.getString(this.getClass(), "this-operator-allows-to-check-that-for-each-element-in-the-source-layer-exists-at-least-one-element-in-the-target-layer-that-overlaps-with-it")) + I18N.getString(this.getClass(), "one-element-a-overlaps-with-another-element-b-if-both-of-them-has-some-but-not-all-of-their-points-in-common-they-have-the-same-dimension-and-the-intersection-of-their-interiors-has-the-same-dimension-than-their-geometries");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        Geometry featGem = feat.getGeometry();
        FeatureIterator itTarget = null;
        boolean check = false;
        try {
            try {
                itTarget = fcTarget.queryIterator(this.entryTargetFilter, featGem.getEnvelopeInternal());
                block2 : switch (this.checkStrategy) {
                    case 0: {
                        int cont = 0;
                        check = true;
                        while (true) {
                            if (!itTarget.hasNext() || !check) {
                                if (!check) return check;
                            }
                            Feature candidate = itTarget.next();
                            check = check && !candidate.getGeometry().overlaps(featGem);
                            ++cont;
                        }
                        check = cont == fcTarget.size();
                        return check;
                    }
                    case 1: {
                        while (itTarget.hasNext()) {
                            if (check) {
                                return check;
                            }
                            Feature candidate = itTarget.next();
                            check = check || candidate.getGeometry().overlaps(featGem);
                        }
                        return check;
                    }
                    case 2: {
                        check = true;
                        while (itTarget.hasNext()) {
                            if (!check) break block2;
                            Feature candidate = itTarget.next();
                            check = check && candidate.getGeometry().overlaps(featGem);
                        }
                        return check;
                    }
                }
                return check;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itTarget == null) return check;
                itTarget.close();
                return check;
            }
        }
        finally {
            if (itTarget != null) {
                itTarget.close();
            }
        }
    }
}

