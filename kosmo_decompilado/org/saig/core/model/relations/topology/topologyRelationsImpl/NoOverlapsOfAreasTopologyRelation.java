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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class NoOverlapsOfAreasTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "No overlaps of areas";
    public static final String NAME = I18N.getString(NoOverlapsOfAreasTopologyRelation.class, "no-overlaps-of-areas");

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
        return String.valueOf(I18N.getString(this.getClass(), "this-operator-allows-to-check-that-no-elements-overlaping-another-elements-in-the-same-layer-exists")) + I18N.getString(this.getClass(), "an-element-will-be-valid-if-it-does-not-overlap-with-another-element-in-the-same-layer-id-est-it-has-not-any-point-in-common-or-its-intersection-match-up-with-none-of-them");
    }

    public boolean checkAll(Filter filter) {
        boolean checkAll = true;
        this.errors = new ArrayList();
        HashSet<Object> procesados = new HashSet<Object>();
        Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
        FeatureIterator it = null;
        try {
            try {
                FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
                it = fc.queryIterator(filter, null);
                while (it.hasNext()) {
                    boolean check;
                    Feature feat;
                    block20: {
                        feat = it.next();
                        if (this.alphanumericFilter != null && !this.alphanumericFilter.contains(feat)) {
                            checkAll = false;
                            this.addErrorFeature(feat, AbstractTopologyRelation.FILTER_REASON_ERROR);
                            continue;
                        }
                        Geometry geom = feat.getGeometry();
                        check = true;
                        if (!procesados.contains(feat.getPrimaryKey())) {
                            FeatureIterator itCandidatos = null;
                            try {
                                try {
                                    itCandidatos = fc.queryIterator(feat.getGeometry().getEnvelopeInternal());
                                    while (itCandidatos.hasNext() && check) {
                                        Feature candidato = itCandidatos.next();
                                        if (candidato.equals(feat)) continue;
                                        Geometry candGeom = candidato.getGeometry();
                                        boolean bl = check = check && !geom.overlaps(candGeom);
                                        if (!check) continue;
                                        Geometry intersection = geom.intersection(candGeom);
                                        boolean bl2 = check = check && (intersection.isEmpty() || intersection.getDimension() != 2);
                                    }
                                    procesados.add(feat.getPrimaryKey());
                                }
                                catch (Exception e) {
                                    LOGGER.error((Object)"", (Throwable)e);
                                    if (itCandidatos != null) {
                                        itCandidatos.close();
                                    }
                                    break block20;
                                }
                            }
                            catch (Throwable throwable) {
                                if (itCandidatos != null) {
                                    itCandidatos.close();
                                }
                                throw throwable;
                            }
                            if (itCandidatos != null) {
                                itCandidatos.close();
                            }
                        }
                    }
                    if (!check) {
                        this.addErrorFeature(feat, AbstractTopologyRelation.TOPOLOGY_REASON_ERROR);
                    }
                    boolean bl = checkAll = checkAll && check;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null && it instanceof FeatureIterator) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null && it instanceof FeatureIterator) {
                it.close();
            }
        }
        return checkAll;
    }

    @Override
    public boolean checkAll() {
        return this.checkAll(this.getEntrySourceFilter());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
        Geometry geom = feat.getGeometry();
        FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
        boolean check = true;
        FeatureIterator itCandidatos = null;
        try {
            try {
                itCandidatos = fc.queryIterator(null, feat.getGeometry().getEnvelopeInternal());
                while (itCandidatos.hasNext()) {
                    if (!check) {
                        return check;
                    }
                    Feature candidato = itCandidatos.next();
                    if (candidato.equals(feat) || features.contains(candidato)) continue;
                    Geometry candGeom = candidato.getGeometry();
                    if (!(check = check && !geom.overlaps(candGeom))) continue;
                    Geometry intersection = geom.intersection(candGeom);
                    check = check && (intersection.isEmpty() || intersection.getDimension() != 2);
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
        return geomType == 5 || geomType == 4;
    }
}

