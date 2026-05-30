/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.core.check.self;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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

public class NoSelfOverlappingCheck
extends AbstractSelfTopologyCheck {
    private static final String REASON = I18N.getString("org.saig.core.check.self.NoSelfOverlappingCheck.No-self-overlapping-check");
    private static final String CONDITION = "no_self_overlapping";

    public NoSelfOverlappingCheck(Layer sourceLayer) {
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
                    if (feat.getGeometry().isSimple()) continue;
                    boolean check = false;
                    if (feat.getGeometry() instanceof LineString) {
                        check = this.checkLineString((LineString)feat.getGeometry());
                    } else if (feat.getGeometry() instanceof MultiLineString) {
                        MultiLineString multiLine = (MultiLineString)feat.getGeometry();
                        int i = 0;
                        while (i < multiLine.getNumGeometries() && !check) {
                            check = this.checkLineString((LineString)multiLine.getGeometryN(i));
                            ++i;
                        }
                    }
                    if (!check) continue;
                    incorrectFeatures.add(this.generateIncidentFeature(feat, false));
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

    private boolean checkLineString(LineString line) {
        Coordinate[] coords = line.getCoordinates();
        LineSegment segment1 = new LineSegment(coords[0], coords[1]);
        boolean check = false;
        int i = 2;
        while (i < coords.length && !check) {
            LineSegment segment2 = new LineSegment(coords[i - 1], coords[i]);
            check = segment2.overlaps(segment1);
            if (!check) {
                segment1 = (LineSegment)segment2.clone();
            }
            ++i;
        }
        return check;
    }

    @Override
    protected String generateReason() {
        return REASON;
    }

    @Override
    protected String generateConditionFromFilter() {
        return "no_self_overlapping(" + this.sourceLayer.getName() + ")";
    }

    private class LineSegment
    implements Cloneable {
        private LineString line;

        public LineSegment(Coordinate c1, Coordinate c2) {
            GeometryFactory geomFact = new GeometryFactory();
            this.line = geomFact.createLineString(new Coordinate[]{c1, c2});
        }

        public boolean overlaps(LineSegment other) {
            Point startPoint = other.getLineString().getStartPoint();
            return startPoint.intersects((Geometry)this.line);
        }

        public LineString getLineString() {
            return this.line;
        }

        public Object clone() {
            Coordinate[] coords = this.line.getCoordinates();
            return new LineSegment(coords[0], coords[1]);
        }
    }
}

