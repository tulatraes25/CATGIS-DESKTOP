/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 */
package com.vividsolutions.jump.algorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public class LengthToPoint {
    private double minDistanceToPoint;
    private double locationLength;

    public static double lengthAlongSegment(LineSegment seg, Coordinate pt) {
        double projFactor = seg.projectionFactor(pt);
        double len = 0.0;
        len = projFactor <= 0.0 ? 0.0 : (projFactor <= 1.0 ? projFactor * seg.getLength() : seg.getLength());
        return len;
    }

    public static double length(LineString line, Coordinate inputPt) {
        LengthToPoint lp = new LengthToPoint(line, inputPt);
        return lp.getLength();
    }

    public LengthToPoint(LineString line, Coordinate inputPt) {
        this.computeLength(line, inputPt);
    }

    public double getLength() {
        return this.locationLength;
    }

    private void computeLength(LineString line, Coordinate inputPt) {
        this.minDistanceToPoint = Double.MAX_VALUE;
        double baseLocationDistance = 0.0;
        Coordinate[] pts = line.getCoordinates();
        LineSegment seg = new LineSegment();
        int i = 0;
        while (i < pts.length - 1) {
            seg.p0 = pts[i];
            seg.p1 = pts[i + 1];
            this.updateLength(seg, inputPt, baseLocationDistance);
            baseLocationDistance += seg.getLength();
            ++i;
        }
    }

    private void updateLength(LineSegment seg, Coordinate inputPt, double segStartLocationDistance) {
        double dist = seg.distance(inputPt);
        if (dist > this.minDistanceToPoint) {
            return;
        }
        this.minDistanceToPoint = dist;
        double projFactor = seg.projectionFactor(inputPt);
        this.locationLength = projFactor <= 0.0 ? segStartLocationDistance : (projFactor <= 1.0 ? segStartLocationDistance + projFactor * seg.getLength() : segStartLocationDistance + seg.getLength());
    }
}

