/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.algorithm.CentralEndpointIntersector
 *  com.vividsolutions.jts.algorithm.HCoordinate
 *  com.vividsolutions.jts.algorithm.LineIntersector
 *  com.vividsolutions.jts.algorithm.NotRepresentableException
 *  com.vividsolutions.jts.algorithm.RobustLineIntersector
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.PrecisionModel
 */
package es.axios.udig.ui.editingtools.precisionparallels.internal;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.CentralEndpointIntersector;
import com.vividsolutions.jts.algorithm.HCoordinate;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.NotRepresentableException;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.PrecisionModel;
import es.axios.udig.ui.editingtools.precisionparallels.internal.OffsetVertexList;
import java.util.ArrayList;

public class OffsetBuilder {
    public static OffsetPosition CURRENT_POSITION = OffsetPosition.POSITION_UPPER;
    private double filletAngleQuantum;
    private static final double MIN_CURVE_VERTEX_FACTOR = 1.0E-6;
    private double distance = 0.0;
    private OffsetVertexList vertexList;
    private LineIntersector li;
    private Coordinate s0;
    private Coordinate s1;
    private Coordinate s2;
    private LineSegment seg0 = new LineSegment();
    private LineSegment seg1 = new LineSegment();
    private LineSegment offset0 = new LineSegment();
    private LineSegment offset1 = new LineSegment();
    private int side = 0;
    private Boolean lastOutsideTurn = null;
    private PrecisionModel precisionModel;
    private int startPosition;
    private Coordinate[][] inputLines = new Coordinate[2][2];

    public OffsetBuilder(OffsetPosition offsetPosition, int startPosition) {
        CURRENT_POSITION = offsetPosition;
        this.startPosition = startPosition;
        this.precisionModel = new PrecisionModel();
        this.li = new RobustLineIntersector();
        this.filletAngleQuantum = 1.5707963267948966;
    }

    public ArrayList<Coordinate> getLineCurve(Coordinate[] inputPts, double distance) {
        ArrayList<Coordinate> arrayList = new ArrayList<Coordinate>();
        if (distance <= 0.0) {
            return arrayList;
        }
        this.init(distance);
        this.computeParallelLineCurve(inputPts);
        arrayList = this.vertexList.getList();
        return arrayList;
    }

    public Coordinate getInitialCoordinate() {
        return this.vertexList.getInitialCoor();
    }

    private void init(double distance) {
        this.distance = distance;
        this.vertexList = new OffsetVertexList();
        this.vertexList.setPrecisionModel(this.precisionModel);
        this.vertexList.setMinimumVertexDistance(distance * 1.0E-6);
    }

    private void computeParallelLineCurve(Coordinate[] inputPts) {
        int n = inputPts.length - 1;
        if (CURRENT_POSITION == OffsetPosition.POSITION_UPPER) {
            this.initSideSegments(inputPts[0], inputPts[1], this.startPosition);
            int i = 2;
            while (i <= n) {
                this.addNextSegment(inputPts[i], true);
                ++i;
            }
            this.addLastSegmentCW();
        } else {
            this.initSideSegments(inputPts[n], inputPts[n - 1], this.startPosition);
            int i = n - 2;
            while (i >= 0) {
                this.addNextSegment(inputPts[i], true);
                --i;
            }
            this.addLastSegmentCCW();
        }
    }

    private void addLastSegmentCW() {
        if (this.lastOutsideTurn != null && this.lastOutsideTurn.booleanValue() || this.vertexList.size() == 0) {
            this.vertexList.addPt(this.offset1.p0, false);
        }
        this.vertexList.addPt(this.offset1.p1, false);
    }

    private void addLastSegmentCCW() {
        if (this.lastOutsideTurn != null && this.lastOutsideTurn.booleanValue() || this.vertexList.size() == 0) {
            this.vertexList.addPt(this.offset1.p0, false);
        }
        this.vertexList.addPt(this.offset1.p1, false);
    }

    private void initSideSegments(Coordinate s1, Coordinate s2, int side) {
        this.s1 = s1;
        this.s2 = s2;
        this.side = side;
        this.seg1.setCoordinates(s1, s2);
        this.computeOffsetSegment(this.seg1, side, this.distance, this.offset1);
    }

    private void computeOffsetSegment(LineSegment seg, int side, double distance, LineSegment offset) {
        int sideSign = side == 1 ? 1 : -1;
        double dx = seg.p1.x - seg.p0.x;
        double dy = seg.p1.y - seg.p0.y;
        double len = Math.sqrt(dx * dx + dy * dy);
        double ux = (double)sideSign * distance * dx / len;
        double uy = (double)sideSign * distance * dy / len;
        offset.p0.x = seg.p0.x - uy;
        offset.p0.y = seg.p0.y + ux;
        offset.p1.x = seg.p1.x - uy;
        offset.p1.y = seg.p1.y + ux;
    }

    private void addNextSegment(Coordinate p, boolean addStartPoint) {
        boolean outsideTurn;
        this.s0 = this.s1;
        this.s1 = this.s2;
        this.s2 = p;
        this.seg0.setCoordinates(this.s0, this.s1);
        this.computeOffsetSegment(this.seg0, this.side, this.distance, this.offset0);
        this.seg1.setCoordinates(this.s1, this.s2);
        this.computeOffsetSegment(this.seg1, this.side, this.distance, this.offset1);
        if (this.s1.equals((Object)this.s2)) {
            return;
        }
        int orientation = CGAlgorithms.computeOrientation((Coordinate)this.s0, (Coordinate)this.s1, (Coordinate)this.s2);
        boolean bl = outsideTurn = orientation == -1 && this.side == 1 || orientation == 1 && this.side == 2;
        if (this.lastOutsideTurn == null) {
            this.lastOutsideTurn = outsideTurn;
        }
        if (orientation == 0) {
            this.addCollinear(addStartPoint);
        } else if (outsideTurn) {
            if (this.lastOutsideTurn.booleanValue()) {
                this.vertexList.addPt(this.offset0.p0, false);
            }
            this.addOutsideTurn(addStartPoint);
        } else {
            if (this.vertexList.size() == 0) {
                this.vertexList.addPt(this.offset0.p0, false);
            }
            if (this.lastOutsideTurn.booleanValue()) {
                this.vertexList.addPt(this.offset0.p0, false);
            }
            this.addInsideTurn();
        }
        this.lastOutsideTurn = outsideTurn;
    }

    private void addCollinear(boolean addStartPoint) {
        this.li.computeIntersection(this.s0, this.s1, this.s1, this.s2);
        int numInt = this.li.getIntersectionNum();
        if (numInt >= 2) {
            this.addFillet(this.s1, this.offset0.p1, this.offset1.p0, -1, this.distance);
        }
    }

    private void addFillet(Coordinate p, Coordinate p0, Coordinate p1, int direction, double radius) {
        double dx0 = p0.x - p.x;
        double dy0 = p0.y - p.y;
        double startAngle = Math.atan2(dy0, dx0);
        double dx1 = p1.x - p.x;
        double dy1 = p1.y - p.y;
        double endAngle = Math.atan2(dy1, dx1);
        if (direction == -1) {
            if (startAngle <= endAngle) {
                startAngle += Math.PI * 2;
            }
        } else if (startAngle >= endAngle) {
            startAngle -= Math.PI * 2;
        }
        this.vertexList.addPt(p0, false);
        this.addFillet(p, startAngle, endAngle, direction, radius);
        this.vertexList.addPt(p1, false);
    }

    private void addCornerPoint(LineSegment offset0, LineSegment offset1, boolean b) {
        Coordinate pt = new Coordinate();
        pt = this.intersection(offset0.p0, offset0.p1, offset1.p0, offset1.p1);
        this.vertexList.addPt(pt, b);
    }

    private void addFillet(Coordinate p, double startAngle, double endAngle, int direction, double radius) {
        int directionFactor = direction == -1 ? -1 : 1;
        double totalAngle = Math.abs(startAngle - endAngle);
        int nSegs = (int)(totalAngle / this.filletAngleQuantum + 0.5);
        if (nSegs < 1) {
            return;
        }
        double initAngle = 0.0;
        double currAngleInc = totalAngle / (double)nSegs;
        double currAngle = initAngle;
        Coordinate pt = new Coordinate();
        while (currAngle < totalAngle) {
            double angle = startAngle + (double)directionFactor * currAngle;
            pt.x = p.x + radius * Math.cos(angle);
            pt.y = p.y + radius * Math.sin(angle);
            this.vertexList.addPt(pt, false);
            currAngle += currAngleInc;
        }
    }

    private void addOutsideTurn(boolean addStartPoint) {
        if (addStartPoint) {
            this.vertexList.addPt(this.offset0.p1, false);
        }
        this.addCornerPoint(this.offset0, this.offset1, true);
    }

    private void addInsideTurn() {
        this.li.computeIntersection(this.offset0.p0, this.offset0.p1, this.offset1.p0, this.offset1.p1);
        if (this.li.hasIntersection()) {
            this.vertexList.addPt(this.li.getIntersection(0), false);
        } else {
            this.addCornerPoint(this.offset0, this.offset1, false);
        }
    }

    private Coordinate intersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
        this.inputLines[0][0] = p1;
        this.inputLines[0][1] = p2;
        this.inputLines[1][0] = q1;
        this.inputLines[1][1] = q2;
        Coordinate intPt = this.intersectionWithNormalization(p1, p2, q1, q2);
        if (this.precisionModel != null) {
            this.precisionModel.makePrecise(intPt);
        }
        return intPt;
    }

    private Coordinate intersectionWithNormalization(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
        Coordinate n1 = new Coordinate(p1);
        Coordinate n2 = new Coordinate(p2);
        Coordinate n3 = new Coordinate(q1);
        Coordinate n4 = new Coordinate(q2);
        Coordinate normPt = new Coordinate();
        this.normalizeToEnvCentre(n1, n2, n3, n4, normPt);
        Coordinate intPt = this.safeHCoordinateIntersection(n1, n2, n3, n4);
        intPt.x += normPt.x;
        intPt.y += normPt.y;
        return intPt;
    }

    private void normalizeToEnvCentre(Coordinate n00, Coordinate n01, Coordinate n10, Coordinate n11, Coordinate normPt) {
        double minX0 = n00.x < n01.x ? n00.x : n01.x;
        double minY0 = n00.y < n01.y ? n00.y : n01.y;
        double maxX0 = n00.x > n01.x ? n00.x : n01.x;
        double maxY0 = n00.y > n01.y ? n00.y : n01.y;
        double minX1 = n10.x < n11.x ? n10.x : n11.x;
        double minY1 = n10.y < n11.y ? n10.y : n11.y;
        double maxX1 = n10.x > n11.x ? n10.x : n11.x;
        double maxY1 = n10.y > n11.y ? n10.y : n11.y;
        double intMinX = minX0 > minX1 ? minX0 : minX1;
        double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
        double intMinY = minY0 > minY1 ? minY0 : minY1;
        double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;
        double intMidX = (intMinX + intMaxX) / 2.0;
        double intMidY = (intMinY + intMaxY) / 2.0;
        normPt.x = intMidX;
        normPt.y = intMidY;
        n00.x -= normPt.x;
        n00.y -= normPt.y;
        n01.x -= normPt.x;
        n01.y -= normPt.y;
        n10.x -= normPt.x;
        n10.y -= normPt.y;
        n11.x -= normPt.x;
        n11.y -= normPt.y;
    }

    private Coordinate safeHCoordinateIntersection(Coordinate p1, Coordinate p2, Coordinate q1, Coordinate q2) {
        Coordinate intPt = null;
        try {
            intPt = HCoordinate.intersection((Coordinate)p1, (Coordinate)p2, (Coordinate)q1, (Coordinate)q2);
        }
        catch (NotRepresentableException e) {
            intPt = CentralEndpointIntersector.getIntersection((Coordinate)p1, (Coordinate)p2, (Coordinate)q1, (Coordinate)q2);
        }
        return intPt;
    }

    public static enum OffsetPosition {
        POSITION_UPPER,
        POSITION_UNDER;

    }
}

