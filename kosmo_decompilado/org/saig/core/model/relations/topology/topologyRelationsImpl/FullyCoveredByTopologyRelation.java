/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.util.ArrayList;
import java.util.Collection;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.AbstractTopologyBinaryRelation;
import org.saig.jump.lang.I18N;

public class FullyCoveredByTopologyRelation
extends AbstractTopologyBinaryRelation {
    public static final String ID = "Fully covered by";
    public static final String NAME = I18N.getString(FullyCoveredByTopologyRelation.class, "fully-covered-by");

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
        return I18N.getString(this.getClass(), "this-operator-allows-to-check-that-every-element-in-the-source-layer-is-fully-covered-by-elements-in-the-target-layer");
    }

    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        Geometry featGem = feat.getGeometry();
        FeatureIterator itTarget = null;
        boolean check = false;
        try {
            try {
                itTarget = fcTarget.queryIterator(this.entryTargetFilter, featGem.getEnvelopeInternal());
                ArrayList<Geometry> geoms = new ArrayList<Geometry>();
                switch (this.checkStrategy) {
                    case 0: {
                        int cont = 0;
                        check = true;
                        while (itTarget.hasNext() && check) {
                            Feature candidate = itTarget.next();
                            check = check && !candidate.getGeometry().covers(featGem);
                            geoms.add(candidate.getGeometry());
                            ++cont;
                        }
                        if (check) {
                            GeometryCollection union = geomFact.createGeometryCollection(geoms.toArray(new Geometry[geoms.size()]));
                            union = union.buffer(0.0);
                            check = featGem.within((Geometry)union);
                        }
                        break;
                    }
                    case 1: {
                        while (itTarget.hasNext() && !check) {
                            Feature candidate = itTarget.next();
                            check = check || candidate.getGeometry().covers(featGem);
                            geoms.add(candidate.getGeometry());
                        }
                        if (!check) {
                            GeometryCollection union = geomFact.createGeometryCollection(geoms.toArray(new Geometry[geoms.size()]));
                            union = union.buffer(0.0);
                            check = featGem.within((Geometry)union);
                        }
                        break;
                    }
                    case 2: {
                        while (itTarget.hasNext() && check) {
                            Feature candidate = itTarget.next();
                            geoms.add(candidate.getGeometry());
                        }
                        GeometryCollection union = geomFact.createGeometryCollection(geoms.toArray(new Geometry[geoms.size()]));
                        union = union.buffer(0.0);
                        check = featGem.within((Geometry)union);
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itTarget != null) {
                    itTarget.close();
                }
            }
        }
        finally {
            if (itTarget != null) {
                itTarget.close();
            }
        }
        return check;
    }
}

