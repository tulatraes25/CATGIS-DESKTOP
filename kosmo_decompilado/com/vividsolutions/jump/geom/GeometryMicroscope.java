/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.geom.LineSegmentEnvelopeIntersector;
import com.vividsolutions.jump.geom.MicroscopePointAdjuster;
import com.vividsolutions.jump.util.CoordinateArrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeometryMicroscope {
    private List<Geometry> geomList;
    private Envelope env;
    private double minSep;

    public GeometryMicroscope(List<Geometry> geomList, Envelope env, double minSep) {
        this.geomList = geomList;
        this.env = env;
        this.minSep = minSep;
    }

    public List<Geometry> getAdjusted() {
        List<LineSegment> segList = this.getSegList();
        MicroscopePointAdjuster mpa = new MicroscopePointAdjuster(segList, this.env, this.minSep);
        Map<Coordinate, Coordinate> ptMap = mpa.getAdjustedPointMap();
        this.applyAdjustment(ptMap);
        return this.geomList;
    }

    private void applyAdjustment(Map<Coordinate, Coordinate> ptMap) {
        CoordinateAdjusterFilter coordAdjFilter = new CoordinateAdjusterFilter(ptMap);
        for (Geometry geom : this.geomList) {
            geom.apply((CoordinateFilter)coordAdjFilter);
        }
    }

    private List<LineSegment> getSegList() {
        ArrayList<LineSegment> segList = new ArrayList<LineSegment>();
        for (Geometry geom : this.geomList) {
            List<Coordinate[]> coordArrayList = CoordinateArrays.toCoordinateArrays(geom, false);
            this.addSegments(coordArrayList, segList);
        }
        return segList;
    }

    private void addSegments(List<Coordinate[]> coordArrayList, List<LineSegment> segList) {
        LineSegmentEnvelopeIntersector linesegEnvInt = new LineSegmentEnvelopeIntersector();
        for (Coordinate[] coord : coordArrayList) {
            int j = 0;
            while (j < coord.length - 1) {
                LineSegment seg = new LineSegment(coord[j], coord[j + 1]);
                if (linesegEnvInt.touches(seg, this.env)) {
                    segList.add(seg);
                }
                ++j;
            }
        }
    }

    public class CoordinateAdjusterFilter
    implements CoordinateFilter {
        protected Map<Coordinate, Coordinate> ptMap;

        public CoordinateAdjusterFilter(Map<Coordinate, Coordinate> ptMap) {
            this.ptMap = ptMap;
        }

        public void filter(Coordinate p) {
            Coordinate adj = this.ptMap.get(p);
            if (adj != null) {
                p.x = adj.x;
                p.y = adj.y;
            }
        }
    }
}

