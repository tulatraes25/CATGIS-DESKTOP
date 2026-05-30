/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.collections.CollectionUtils
 */
package es.kosmo.core.model.relations.topology.impl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import es.kosmo.core.model.relations.topology.OperationType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class NoGapsTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "No gaps";
    public static final String NAME = I18N.getString("es.kosmo.core.model.relations.topology.impl.NoGapsTopologyRelation.No-gaps-between-areas");
    private static final double BUFFER_TOLERANCE = 0.001;
    private static final double CANDIDATE_TOLERANCE = 50.0;
    private Geometry startTransactionGeom;
    private Geometry endTransactionGeom;

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
        return I18N.getString("es.kosmo.core.model.relations.topology.impl.NoGapsTopologyRelation.This-operator-allows-to-check-that-there-are-not-gaps-between-features-from-a-continuous-layer");
    }

    public boolean checkAll(Filter filter) {
        boolean checkAll = true;
        this.errors = new ArrayList();
        HashSet<Object> procesados = new HashSet<Object>();
        List<LineString> holes = this.getHoles();
        Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
        if (this.startTransactionGeom != null && !this.startTransactionGeom.isEmpty()) {
            checkAll = this.checkTransaction();
        } else {
            FeatureIterator it = null;
            try {
                try {
                    FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
                    it = fc.queryIterator(filter, null);
                    while (it.hasNext()) {
                        boolean check;
                        Feature feat;
                        block23: {
                            feat = it.next();
                            if (this.alphanumericFilter != null && !this.alphanumericFilter.contains(feat)) {
                                checkAll = false;
                                this.addErrorFeature(feat, AbstractTopologyRelation.FILTER_REASON_ERROR);
                                continue;
                            }
                            Geometry geom = feat.getGeometry();
                            Geometry bufferCandidate = geom.buffer(50.0);
                            check = true;
                            if (!procesados.contains(feat.getPrimaryKey())) {
                                FeatureIterator itCandidates = null;
                                try {
                                    try {
                                        itCandidates = fc.queryIterator(bufferCandidate.getEnvelopeInternal());
                                        ArrayList<Geometry> candidates = new ArrayList<Geometry>();
                                        while (itCandidates.hasNext()) {
                                            Feature currentFeat = itCandidates.next();
                                            if (currentFeat.equals(feat)) continue;
                                            candidates.add(currentFeat.getGeometry());
                                        }
                                        if (CollectionUtils.isNotEmpty(candidates)) {
                                            candidates.add(geom.buffer(0.001));
                                            GeometryCollection geomCol = geomFact.createGeometryCollection(candidates.toArray(new Geometry[0]));
                                            Geometry union = geomCol.buffer(0.0);
                                            check = this.checkHoles(geom, union, holes);
                                        }
                                        procesados.add(feat.getPrimaryKey());
                                    }
                                    catch (Exception e) {
                                        LOGGER.error((Object)"", (Throwable)e);
                                        if (itCandidates != null) {
                                            itCandidates.close();
                                        }
                                        break block23;
                                    }
                                }
                                catch (Throwable throwable) {
                                    if (itCandidates != null) {
                                        itCandidates.close();
                                    }
                                    throw throwable;
                                }
                                if (itCandidates != null) {
                                    itCandidates.close();
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
        }
        return checkAll;
    }

    protected boolean checkTransaction() {
        List<LineString> startHoles = this.extractHoles(this.startTransactionGeom);
        List<LineString> endHoles = this.extractHoles(this.endTransactionGeom);
        ArrayList<LineString> wrongHoles = new ArrayList<LineString>();
        if (CollectionUtils.isNotEmpty(endHoles)) {
            for (LineString hole : endHoles) {
                if (startHoles.contains(hole)) continue;
                wrongHoles.add(hole);
            }
        }
        for (LineString wrongHole : wrongHoles) {
            BasicFeature f = new BasicFeature(errorSchema);
            Polygon pol = geomFact.createPolygon(wrongHole.getCoordinates());
            f.setGeometry((Geometry)pol);
            this.addErrorFeature(f, AbstractTopologyRelation.TOPOLOGY_REASON_ERROR);
        }
        return CollectionUtils.isEmpty(wrongHoles);
    }

    protected List<LineString> extractHoles(Geometry g) {
        ArrayList<LineString> holes = new ArrayList<LineString>();
        int i = 0;
        while (i < g.getNumGeometries()) {
            Polygon pol;
            Geometry currentGeom = g.getGeometryN(i);
            if (currentGeom instanceof Polygon && (pol = (Polygon)currentGeom).getNumInteriorRing() > 0) {
                int j = 0;
                while (j < pol.getNumInteriorRing()) {
                    holes.add(pol.getInteriorRingN(j));
                    ++j;
                }
            }
            ++i;
        }
        return holes;
    }

    private List<LineString> getHoles() {
        return null;
    }

    protected boolean checkHoles(Geometry sourceGeom, Geometry unionGeom, List<LineString> holes) {
        boolean check = true;
        int i = 0;
        while (i < unionGeom.getNumGeometries() && check) {
            Geometry currentGeom = unionGeom.getGeometryN(i);
            double distance = sourceGeom.distance(currentGeom);
            if (distance == 0.0) {
                Polygon pol = (Polygon)currentGeom;
                if (pol.getNumInteriorRing() > 0) {
                    int j = 0;
                    while (j < pol.getNumInteriorRing() & check) {
                        LineString interiorRingN = pol.getInteriorRingN(j);
                        if (CollectionUtils.isNotEmpty(holes) && !holes.contains(interiorRingN)) {
                            check = sourceGeom.distance((Geometry)interiorRingN) > 50.0;
                        }
                        ++j;
                    }
                }
            } else if (distance < 50.0) {
                check = false;
            }
            ++i;
        }
        return check;
    }

    @Override
    public boolean checkAll() {
        return this.checkAll(this.getEntrySourceFilter());
    }

    /*
     * Unable to fully structure code
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
        geom = feat.getGeometry();
        bufferGeom = geom.buffer(50.0);
        fc = sourceLayer.getUltimateFeatureCollectionWrapper();
        check = true;
        itCandidates = null;
        try {
            try {
                itCandidates = fc.queryIterator(null, bufferGeom.getEnvelopeInternal());
                candidates = new ArrayList<Geometry>();
                while (itCandidates.hasNext()) {
                    currentFeat = itCandidates.next();
                    if (currentFeat.equals(feat) || features.contains(currentFeat)) continue;
                    candidates.add(currentFeat.getGeometry());
                }
                if (!CollectionUtils.isNotEmpty(candidates)) return check;
                if (!OperationType.REMOVE.equals((Object)this.operationType)) {
                    candidates.add(geom.buffer(0.001));
                    for (Feature f : features) {
                        candidates.add(f.getGeometry());
                    }
                }
                geomCol = NoGapsTopologyRelation.geomFact.createGeometryCollection(candidates.toArray(new Geometry[0]));
                union = geomCol.buffer(0.0);
                i = 0;
                if (true) ** GOTO lbl43
                do {
                    if ((distance = geom.distance(currentGeom = union.getGeometryN(i))) == 0.0) {
                        pol = (Polygon)currentGeom;
                        if (pol.getNumInteriorRing() > 0) {
                            j = 0;
                            while (j < pol.getNumInteriorRing() & check) {
                                interiorRingN = pol.getInteriorRingN(j);
                                check = geom.distance((Geometry)interiorRingN) > 50.0;
                                ++j;
                            }
                        }
                    } else if (distance < 50.0) {
                        check = false;
                    }
                    ++i;
lbl43:
                    // 2 sources

                    if (i >= union.getNumGeometries()) return check;
                } while (check);
                return check;
            }
            catch (Exception e) {
                NoGapsTopologyRelation.LOGGER.error((Object)"", (Throwable)e);
                if (itCandidates == null) return check;
                itCandidates.close();
            }
            return check;
        }
        finally {
            if (itCandidates != null) {
                itCandidates.close();
            }
        }
    }

    @Override
    public boolean checkValidGeometryType(int geomType) {
        return geomType == 5 || geomType == 4;
    }

    public void setStartTransactionGeom(Geometry g) {
        this.startTransactionGeom = g;
    }

    public void setEndTransactionGeom(Geometry g) {
        this.endTransactionGeom = g;
    }
}

