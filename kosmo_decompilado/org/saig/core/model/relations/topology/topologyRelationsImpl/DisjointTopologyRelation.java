/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.Collection;
import org.saig.core.model.relations.topology.AbstractTopologyBinaryRelation;
import org.saig.jump.lang.I18N;

public class DisjointTopologyRelation
extends AbstractTopologyBinaryRelation {
    public static final String ID = "Disjoint";
    public static final String NAME = I18N.getString(DisjointTopologyRelation.class, "disjoint");

    public DisjointTopologyRelation() {
        this.checkStrategy = 2;
    }

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
        return I18N.getString(this.getClass(), "this-operator-allows-to-check-that-for-each-element-in-the-source-layer-does-not-exist-any-element-in-the-target-layer-that-share-any-common-point-with-it-an-element-a-is-disjoint-to-an-element-b-if-they-do-not-share-any-common-points");
    }

    /*
     * Unable to fully structure code
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        featGem = feat.getGeometry();
        itTarget = null;
        check = false;
        cont = 0;
        try {
            try {
                itTarget = fcTarget.queryIterator(this.entryTargetFilter, featGem.getEnvelopeInternal());
                switch (this.checkStrategy) {
                    case 0: {
                        check = true;
                        while (itTarget.hasNext() && check) {
                            candidate = itTarget.next();
                            check = check != false && featGem.disjoint(candidate.getGeometry()) == false;
                            ++cont;
                        }
                        if (!check) return check;
                        check = cont == fcTarget.size();
                        return check;
                    }
                    case 1: {
                        while (itTarget.hasNext() && !check) {
                            candidate = itTarget.next();
                            check = check != false || featGem.disjoint(candidate.getGeometry()) != false;
                            ++cont;
                        }
                        if (check) return check;
                        if (cont != fcTarget.size()) {
                            v0 = check = this.entryTargetFilter == null;
                        }
                        if (check) return check;
                        itTarget.close();
                        itTarget = fcTarget.queryIterator(this.entryTargetFilter, null);
                        if (true) ** GOTO lbl35
                        do {
                            candidate = itTarget.next();
                            v1 = check = check != false || featGem.disjoint(candidate.getGeometry()) != false;
lbl35:
                            // 2 sources

                            if (!itTarget.hasNext()) return check;
                        } while (!check);
                        return check;
                    }
                    case 2: {
                        check = true;
                        if (true) ** GOTO lbl44
                        do {
                            candidate = itTarget.next();
                            v2 = check = check != false && featGem.disjoint(candidate.getGeometry()) != false;
lbl44:
                            // 2 sources

                            if (!itTarget.hasNext()) return check;
                        } while (check);
                    }
                    default: {
                        return check;
                    }
                }
            }
            catch (Exception e) {
                DisjointTopologyRelation.LOGGER.error((Object)"", (Throwable)e);
                if (itTarget == null) return check;
                itTarget.close();
            }
            return check;
        }
        finally {
            if (itTarget != null) {
                itTarget.close();
            }
        }
    }
}

