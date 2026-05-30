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

public class LocatePoint {
    private Coordinate pt;
    private int index;

    public static Coordinate pointAlongSegment(LineSegment seg, double length) {
        return LocatePoint.pointAlongSegment(seg.p0, seg.p1, length);
    }

    public static Coordinate pointAlongSegment(Coordinate p0, Coordinate p1, double length) {
        double segLen = p1.distance(p0);
        double frac = length / segLen;
        if (frac <= 0.0) {
            return p0;
        }
        if (frac >= 1.0) {
            return p1;
        }
        double x = (p1.x - p0.x) * frac + p0.x;
        double y = (p1.y - p0.y) * frac + p0.y;
        return new Coordinate(x, y);
    }

    public static Coordinate pointAlongSegmentByFraction(Coordinate p0, Coordinate p1, double frac) {
        if (frac <= 0.0) {
            return p0;
        }
        if (frac >= 1.0) {
            return p1;
        }
        double x = (p1.x - p0.x) * frac + p0.x;
        double y = (p1.y - p0.y) * frac + p0.y;
        return new Coordinate(x, y);
    }

    public static Coordinate pointAlongLine(LineString line, double length) {
        LocatePoint loc = new LocatePoint(line, length);
        return loc.getPoint();
    }

    public LocatePoint(LineString line, double length) {
        this.compute(line, length);
    }

    private void compute(LineString line, double length) {
        double totalLength = 0.0;
        Coordinate[] coord = line.getCoordinates();
        int i = 0;
        while (i < coord.length - 1) {
            Coordinate p1 = coord[i + 1];
            Coordinate p0 = coord[i];
            double segLen = p1.distance(p0);
            if (totalLength + segLen > length) {
                this.pt = LocatePoint.pointAlongSegment(p0, p1, length - totalLength);
                this.index = i;
                return;
            }
            totalLength += segLen;
            ++i;
        }
        this.pt = new Coordinate(coord[coord.length - 1]);
        this.index = coord.length;
    }

    public Coordinate getPoint() {
        return this.pt;
    }

    public int getIndex() {
        return this.index;
    }
}

