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

public class CrossesTopologyRelation
extends AbstractTopologyBinaryRelation {
    public static final String ID = "Crosses";
    public static final String NAME = I18N.getString(CrossesTopologyRelation.class, "crosses-with");

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
        return String.valueOf(I18N.getString(this.getClass(), "this-operator-allows-to-check-that-each-element-in-the-source-layer-crosses-with-at-least-one-element-in-the-target-layer")) + " " + I18N.getString(this.getClass(), "an-element-a-crosses-with-another-element-b-if-they-share-at-least-one-interior-point-but-not-all-of-them");
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
                            if (!itTarget.hasNext() || check) {
                                if (!check) return check;
                            }
                            Feature candidate = itTarget.next();
                            check = check && !featGem.crosses(candidate.getGeometry());
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
                            check = check || featGem.crosses(candidate.getGeometry());
                        }
                        return check;
                    }
                    case 2: {
                        check = true;
                        while (itTarget.hasNext()) {
                            if (!check) break block2;
                            Feature candidate = itTarget.next();
                            check = check && featGem.crosses(candidate.getGeometry());
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

