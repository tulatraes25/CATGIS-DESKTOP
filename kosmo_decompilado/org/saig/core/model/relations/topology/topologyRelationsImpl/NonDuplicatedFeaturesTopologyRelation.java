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
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class NonDuplicatedFeaturesTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "Non duplicated features";
    public static final String NAME = I18N.getString("org.saig.core.model.relations.topology.topologyRelationsImpl.NonDuplicatedFeaturesTopologyRelation.No-duplicated");
    public static final double BUFFER_DISTANCE = 0.001;

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
        return I18N.getString("org.saig.core.model.relations.topology.topologyRelationsImpl.NonDuplicatedFeaturesTopologyRelation.Checks-that-there-are-not-duplicated-features-inside-the-layer");
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) throws Exception {
        Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
        Geometry geom = feat.getGeometry();
        Geometry bufferedFeatGeom = geom.buffer(0.001);
        FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
        boolean check = true;
        FeatureIterator itCandidatos = null;
        try {
            try {
                itCandidatos = fc.queryIterator(this.getEntrySourceFilter(), feat.getGeometry().getEnvelopeInternal());
                while (itCandidatos.hasNext()) {
                    if (!check) {
                        return check;
                    }
                    Feature candidato = itCandidatos.next();
                    if (candidato.equals(feat) || features.contains(candidato)) continue;
                    Geometry candGeom = candidato.getGeometry();
                    check = check && !bufferedFeatGeom.contains(candGeom);
                }
                return check;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (itCandidatos == null) return check;
                itCandidatos.close();
                return check;
            }
        }
        finally {
            if (itCandidatos != null) {
                itCandidatos.close();
            }
        }
    }

    @Override
    public boolean checkValidGeometryType(int geomType) {
        return geomType == 3 || geomType == 2;
    }
}

