/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.topologyRelationsImpl.OnlyConnectedAtEndPointsTopologyRelation;
import org.saig.jump.lang.I18N;

public class MustBeConnectedAtEndPointsTopologyRelation
extends OnlyConnectedAtEndPointsTopologyRelation {
    public static final String ID = "Must be connected at end points";
    public static final String NAME = I18N.getString(MustBeConnectedAtEndPointsTopologyRelation.class, "must-be-connected-at-end-points");

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
        return I18N.getString(this.getClass(), "this-operator-allows-to-check-that-all-elements-in-a-layer-are-connected-in-their-extreme-points-making-a-net");
    }

    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        boolean check = true;
        FeatureIterator itCandidatos = null;
        try {
            try {
                Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
                FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
                if (fc.size() > 1) {
                    itCandidatos = fc.queryIterator(null, feat.getGeometry().getEnvelopeInternal());
                    boolean candidatesFound = false;
                    while (itCandidatos.hasNext() && check) {
                        Feature candidato = itCandidatos.next();
                        candidatesFound = true;
                        boolean singleConnectionFound = true;
                        if (!candidato.equals(feat) && candidato.getGeometry().intersects(feat.getGeometry())) {
                            Geometry result = candidato.getGeometry().intersection(feat.getGeometry());
                            if (!(result instanceof Point)) {
                                singleConnectionFound = false;
                            } else {
                                Point pointResult = (Point)result;
                                if (!this.checkPointIsStartOrEndOfLine(candidato.getGeometry(), pointResult) || !this.checkPointIsStartOrEndOfLine(feat.getGeometry(), pointResult)) {
                                    singleConnectionFound = false;
                                }
                            }
                        }
                        check = singleConnectionFound;
                    }
                    check = check && candidatesFound;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itCandidatos != null) {
                    itCandidatos.close();
                }
            }
        }
        finally {
            if (itCandidatos != null) {
                itCandidatos.close();
            }
        }
        return check;
    }

    @Override
    public boolean checkValidGeometryType(int geomType) {
        return geomType == 3 || geomType == 2;
    }
}

