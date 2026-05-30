/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.core.check.self;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.model.Layer;
import java.util.ArrayList;
import java.util.List;
import org.saig.core.check.self.AbstractSelfTopologyCheck;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryMessage;

public class OnlyConnectedAtEndPointsCheck
extends AbstractSelfTopologyCheck {
    private static final String REASON = I18N.getString("org.saig.core.check.self.OnlyConnectedAtEndPointsCheck.Only-connected-at-end-points-check");
    private static final String CONDITION = "only_connected_at_end_points";

    public OnlyConnectedAtEndPointsCheck(Layer sourceLayer) {
        this.sourceLayer = sourceLayer;
    }

    @Override
    public List<Feature> execute(List<SummaryMessage> messageList) {
        ArrayList<Feature> incorrectFeatures = new ArrayList<Feature>();
        this.incidentSchema = this.generateIncidentSchema(this.sourceLayer.getGeometryType());
        FeatureIterator it = null;
        try {
            try {
                FeatureCollection fc = this.sourceLayer.getUltimateFeatureCollectionWrapper();
                it = fc.iterator();
                while (it.hasNext()) {
                    Feature feat = it.next();
                    FeatureIterator itCandidatos = null;
                    try {
                        try {
                            boolean check = false;
                            itCandidatos = fc.queryIterator(feat.getGeometry().getEnvelopeInternal());
                            while (itCandidatos.hasNext() && !check) {
                                Feature candidato = itCandidatos.next();
                                if (candidato.equals(feat) || !candidato.getGeometry().intersects(feat.getGeometry())) continue;
                                Geometry result = candidato.getGeometry().intersection(feat.getGeometry());
                                if (!(result instanceof Point)) {
                                    check = true;
                                    continue;
                                }
                                Point pointResult = (Point)result;
                                if (this.checkPointIsStartOrEndOfLine(candidato.getGeometry(), pointResult)) continue;
                                check = true;
                            }
                            if (check) {
                                incorrectFeatures.add(this.generateIncidentFeature(feat, false));
                            }
                        }
                        catch (Exception e) {
                            messageList.add(this.buildCheckErrorMessage(feat, e));
                            if (itCandidatos == null) continue;
                            itCandidatos.close();
                            continue;
                        }
                    }
                    catch (Throwable throwable) {
                        if (itCandidatos != null) {
                            itCandidatos.close();
                        }
                        throw throwable;
                    }
                    if (itCandidatos == null) continue;
                    itCandidatos.close();
                }
            }
            catch (Exception e) {
                messageList.add(this.buildCheckErrorMessage(null, e));
                ArrayList<Feature> arrayList = incorrectFeatures;
                if (it != null) {
                    it.close();
                }
                return arrayList;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return incorrectFeatures;
    }

    private boolean checkPointIsStartOrEndOfLine(Geometry geom, Point p) {
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

    @Override
    protected String generateReason() {
        return REASON;
    }

    @Override
    protected String generateConditionFromFilter() {
        return "only_connected_at_end_points(" + this.sourceLayer.getName() + ")";
    }
}

