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
import com.vividsolutions.jump.geom.SingleSegmentExpander;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MicroscopePointAdjuster {
    private static final Coordinate origin = new Coordinate(0.0, 0.0, 0.0);
    private List<LineSegment> segList;
    private Envelope env;
    private double minSep;
    private Map<Coordinate, Coordinate> adjPtMap = new TreeMap<Coordinate, Coordinate>();

    public MicroscopePointAdjuster(List<LineSegment> segList, Envelope env, double minSep) {
        this.segList = segList;
        this.env = env;
        this.minSep = minSep;
    }

    public Map<Coordinate, Coordinate> getAdjustedPointMap() {
        this.computeAdjustments();
        return this.adjPtMap;
    }

    private void computeAdjustments() {
        List<Coordinate> ptsInEnv = this.findPointsInEnv(this.env);
        SingleSegmentExpander ssex = new SingleSegmentExpander();
        List<LineSegment> segsInEnv = this.findSegmentsInEnv(this.env);
        if (ssex.isApplicable(segsInEnv, ptsInEnv)) {
            LineSegment seg = segsInEnv.get(0);
            Coordinate[] adjPt = ssex.expandSegment(seg, this.env);
            this.adjPtMap.put(new Coordinate(seg.p0), adjPt[0]);
            this.adjPtMap.put(new Coordinate(seg.p1), adjPt[1]);
        } else {
            this.computeAdjustedPtMap(ptsInEnv);
        }
    }

    public List<LineSegment> adjustSegments() {
        List<Coordinate> ptsInEnv = this.findPointsInEnv(this.env);
        this.computeAdjustedPtMap(ptsInEnv);
        return this.adjustSegs();
    }

    private List<Coordinate> findPointsInEnv(Envelope env) {
        ArrayList<Coordinate> ptsInEnv = new ArrayList<Coordinate>();
        for (LineSegment seg : this.segList) {
            if (env.contains(seg.p0)) {
                ptsInEnv.add(seg.p0);
            }
            if (!env.contains(seg.p1)) continue;
            ptsInEnv.add(seg.p1);
        }
        return ptsInEnv;
    }

    private List<LineSegment> findSegmentsInEnv(Envelope env) {
        ArrayList<LineSegment> segsInEnv = new ArrayList<LineSegment>();
        for (LineSegment seg : this.segList) {
            if (!env.contains(seg.p0) || !env.contains(seg.p1)) continue;
            segsInEnv.add(seg);
        }
        return segsInEnv;
    }

    private void computeAdjustedPtMap(List<Coordinate> ptsInEnv) {
        for (Coordinate pt : ptsInEnv) {
            Coordinate adjPt = this.computeAdjustment(pt);
            if (adjPt.equals((Object)pt)) continue;
            this.adjPtMap.put(new Coordinate(pt), adjPt);
        }
    }

    private List<LineSegment> adjustSegs() {
        ArrayList<LineSegment> adjSegList = new ArrayList<LineSegment>();
        for (LineSegment seg : this.segList) {
            LineSegment adjSeg = new LineSegment();
            adjSeg.p0 = this.adjustPt(seg.p0);
            adjSeg.p1 = this.adjustPt(seg.p1);
            adjSegList.add(adjSeg);
        }
        return adjSegList;
    }

    private Coordinate adjustPt(Coordinate p) {
        Coordinate adjMapPt = this.adjPtMap.get(p);
        if (adjMapPt != null) {
            return new Coordinate(adjMapPt);
        }
        return new Coordinate(p);
    }

    private Coordinate computeAdjustment(Coordinate p) {
        Coordinate adjVec = new Coordinate();
        for (LineSegment seg : this.segList) {
            double dist = seg.distance(p);
            if (!(dist < this.minSep)) continue;
            Coordinate adjWeightVec = this.adjustmentWeightVector(p, seg);
            adjVec.x += adjWeightVec.x;
            adjVec.y += adjWeightVec.y;
        }
        Coordinate adjPt = new Coordinate(p);
        adjPt.x += adjVec.x;
        adjPt.y += adjVec.y;
        return adjPt;
    }

    private Coordinate adjustmentWeightVector(Coordinate p, LineSegment seg) {
        if (p.equals((Object)seg.p0)) {
            return this.adjWeightEndPoint(p, seg.p1);
        }
        if (p.equals((Object)seg.p1)) {
            return this.adjWeightEndPoint(p, seg.p0);
        }
        return this.adjWeightSegmentProximity(p, seg);
    }

    private Coordinate adjWeightEndPoint(Coordinate p, Coordinate p2) {
        Coordinate adjWeightVec = new Coordinate();
        adjWeightVec.x = p.x - p2.x;
        adjWeightVec.y = p.y - p2.y;
        double len = adjWeightVec.distance(origin);
        if (len > this.minSep) {
            return origin;
        }
        double scale = this.minSep / len;
        adjWeightVec.x *= scale;
        adjWeightVec.y *= scale;
        return adjWeightVec;
    }

    private Coordinate adjWeightSegmentProximity(Coordinate p, LineSegment seg) {
        Coordinate proj = seg.project(p);
        Coordinate adjWeightVec = new Coordinate();
        adjWeightVec.x = p.x - proj.x;
        adjWeightVec.y = p.y - proj.y;
        double len = adjWeightVec.distance(origin);
        double scale = this.minSep / len;
        adjWeightVec.x *= scale;
        adjWeightVec.y *= scale;
        return adjWeightVec;
    }
}

