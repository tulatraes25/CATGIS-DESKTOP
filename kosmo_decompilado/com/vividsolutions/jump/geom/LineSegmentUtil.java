/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public class LineSegmentUtil {
    public static LineSegment project(LineSegment tgt, LineSegment seg) {
        double pf0 = tgt.projectionFactor(seg.p0);
        double pf1 = tgt.projectionFactor(seg.p1);
        if (pf0 >= 1.0 && pf1 >= 1.0) {
            return null;
        }
        if (pf0 <= 0.0 && pf1 <= 0.0) {
            return null;
        }
        Coordinate newp0 = tgt.project(seg.p0);
        if (pf0 < 0.0) {
            newp0 = tgt.p0;
        }
        if (pf0 > 1.0) {
            newp0 = tgt.p1;
        }
        Coordinate newp1 = tgt.project(seg.p1);
        if (pf1 < 0.0) {
            newp1 = tgt.p0;
        }
        if (pf1 > 1.0) {
            newp1 = tgt.p1;
        }
        return new LineSegment(newp0, newp1);
    }

    public static double hausdorffDistance(LineSegment seg0, LineSegment seg1) {
        double hausdorffDist = seg0.distance(seg1.p0);
        double dist = seg0.distance(seg1.p1);
        if (dist > hausdorffDist) {
            hausdorffDist = dist;
        }
        if ((dist = seg1.distance(seg0.p0)) > hausdorffDist) {
            hausdorffDist = dist;
        }
        if ((dist = seg1.distance(seg0.p1)) > hausdorffDist) {
            hausdorffDist = dist;
        }
        return hausdorffDist;
    }

    public static LineString asGeometry(GeometryFactory factory, LineSegment seg) {
        Coordinate[] coord = new Coordinate[]{new Coordinate(seg.p0), new Coordinate(seg.p1)};
        LineString line = factory.createLineString(coord);
        return line;
    }
}

