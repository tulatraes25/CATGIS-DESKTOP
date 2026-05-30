/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.core.model.relations.topology.topologyRelationsImpl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.Collection;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.AbstractTopologyRelation;
import org.saig.jump.lang.I18N;

public class OnlyConnectedAtEndPointsTopologyRelation
extends AbstractTopologyRelation {
    public static final String ID = "Only connected at end points";
    public static final String NAME = I18N.getString(OnlyConnectedAtEndPointsTopologyRelation.class, "only-connected-at-end-points");

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
        return I18N.getString(this.getClass(), "this-operator-allows-to-check-that-if-two-elements-in-the-same-layer-intersects-they-intersect-only-at-initial-or-final-points");
    }

    protected boolean checkPointIsStartOrEndOfLine(Geometry geom, Point p) {
        boolean check = false;
        if (geom instanceof LineString) {
            LineString line = (LineString)geom;
            check = line.getStartPoint().equals((Geometry)p) || line.getEndPoint().equals((Geometry)p);
        } else if (geom instanceof MultiLineString) {
            MultiLineString multiLine = (MultiLineString)geom;
            int i = 0;
            while (i < multiLine.getNumGeometries() && !check) {
                LineString line = (LineString)multiLine.getGeometryN(i);
                check = check || line.getStartPoint().equals((Geometry)p) || line.getEndPoint().equals((Geometry)p);
                ++i;
            }
        }
        return check;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected boolean checkFeature(Feature feat, Collection<Feature> features, FeatureCollection fcTarget) {
        boolean check = true;
        FeatureIterator itCandidatos = null;
        try {
            try {
                Layer sourceLayer = JUMPWorkbench.getLayer(this.getSourceLayerName());
                FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
                itCandidatos = fc.queryIterator(null, feat.getGeometry().getEnvelopeInternal());
                while (itCandidatos.hasNext()) {
                    if (!check) {
                        return check;
                    }
                    Feature candidato = itCandidatos.next();
                    if (candidato.equals(feat) || !candidato.getGeometry().intersects(feat.getGeometry())) continue;
                    Geometry result = candidato.getGeometry().intersection(feat.getGeometry());
                    if (!(result instanceof Point)) {
                        check = false;
                        continue;
                    }
                    Point pointResult = (Point)result;
                    if (this.checkPointIsStartOrEndOfLine(candidato.getGeometry(), pointResult) && this.checkPointIsStartOrEndOfLine(feat.getGeometry(), pointResult)) continue;
                    check = false;
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

