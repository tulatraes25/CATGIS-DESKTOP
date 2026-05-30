/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import java.util.List;

public class SingleSegmentExpander {
    private Coordinate[] adjPt = new Coordinate[2];

    public static Envelope getInsetEnvelope(Envelope env, double insetPct) {
        double inset;
        double insetX = env.getWidth() * insetPct / 2.0;
        double insetY = env.getWidth() * insetPct / 2.0;
        if (insetY < (inset = insetX)) {
            inset = insetY;
        }
        return new Envelope(env.getMinX() + inset, env.getMaxX() - inset, env.getMinY() + inset, env.getMaxY() - inset);
    }

    public boolean isApplicable(List<LineSegment> segList, List<Coordinate> ptList) {
        if (segList.size() < 1) {
            return false;
        }
        LineSegment seg = segList.get(0);
        return this.allSegsEqual(seg, segList) && this.allPtsInSeg(seg, ptList);
    }

    private boolean allSegsEqual(LineSegment seg, List<LineSegment> segList) {
        for (LineSegment seg2 : segList) {
            if (seg.equalsTopo(seg2)) continue;
            return false;
        }
        return true;
    }

    private boolean allPtsInSeg(LineSegment seg, List<Coordinate> ptList) {
        for (Coordinate pt : ptList) {
            if (seg.p0.equals((Object)pt)) {
                return true;
            }
            if (!seg.p1.equals((Object)pt)) continue;
            return true;
        }
        return false;
    }

    public Coordinate[] expandSegment(LineSegment seg, Envelope env) {
        Envelope insetEnv = SingleSegmentExpander.getInsetEnvelope(env, 0.2);
        double dx = seg.p1.x - seg.p0.x;
        double dy = seg.p1.y - seg.p0.y;
        if (Math.abs(dx) <= 1.0E-6) {
            double y0 = insetEnv.getMinY();
            double y1 = insetEnv.getMaxY();
            if (seg.p0.y < seg.p1.y) {
                y0 = insetEnv.getMaxY();
                y1 = insetEnv.getMinY();
            }
            this.adjPt[0] = new Coordinate(seg.p0.x, y0);
            this.adjPt[1] = new Coordinate(seg.p0.x, y1);
            return this.adjPt;
        }
        if (Math.abs(dy) <= 1.0E-6) {
            double x0 = insetEnv.getMinX();
            double x1 = insetEnv.getMaxX();
            if (seg.p0.x < seg.p1.x) {
                x0 = insetEnv.getMaxX();
                x1 = insetEnv.getMinX();
            }
            this.adjPt[0] = new Coordinate(x0, seg.p0.y);
            this.adjPt[1] = new Coordinate(x1, seg.p0.y);
            return this.adjPt;
        }
        this.adjPt[0] = this.rayEnvIntersection(seg.p0, seg.p1, insetEnv);
        this.adjPt[1] = this.rayEnvIntersection(seg.p1, seg.p0, insetEnv);
        return this.adjPt;
    }

    private Coordinate rayEnvIntersection(Coordinate p0, Coordinate p1, Envelope env) {
        Coordinate x0 = this.segIntX(p0, p1, env.getMinX(), env.getMinY(), env.getMaxY());
        if (x0 != null) {
            return x0;
        }
        Coordinate x1 = this.segIntX(p0, p1, env.getMaxX(), env.getMinY(), env.getMaxY());
        if (x1 != null) {
            return x1;
        }
        Coordinate y0 = this.segIntY(p0, p1, env.getMinY(), env.getMinX(), env.getMaxX());
        if (y0 != null) {
            return y0;
        }
        Coordinate y1 = this.segIntY(p0, p1, env.getMaxY(), env.getMinX(), env.getMaxX());
        if (y1 != null) {
            return y1;
        }
        return null;
    }

    private double dotProduct(Coordinate p, Coordinate p0, Coordinate p1) {
        double dx0 = p0.x - p.x;
        double dy0 = p0.y - p.y;
        double dx1 = p1.x - p.x;
        double dy1 = p1.y - p.y;
        return dx0 * dx1 + dy0 * dy1;
    }

    private Coordinate segIntX(Coordinate p0, Coordinate p1, double x, double miny, double maxy) {
        Coordinate intPt;
        double m = (p1.y - p0.y) / (p1.x - p0.x);
        double y2 = m * (x - p0.x) + p0.y;
        if (y2 > miny && y2 < maxy && this.dotProduct(p0, p1, intPt = new Coordinate(x, y2)) < 0.0) {
            return intPt;
        }
        return null;
    }

    private Coordinate segIntY(Coordinate p0, Coordinate p1, double y, double minx, double maxx) {
        Coordinate intPt;
        double m = (p1.x - p0.x) / (p1.y - p0.y);
        double x2 = m * (y - p0.y) + p0.x;
        if (x2 > minx && x2 < maxx && this.dotProduct(p0, p1, intPt = new Coordinate(x2, y)) < 0.0) {
            return intPt;
        }
        return null;
    }
}

