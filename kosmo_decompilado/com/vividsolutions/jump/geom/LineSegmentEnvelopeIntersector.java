/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.LineIntersector
 *  com.vividsolutions.jts.algorithm.RobustLineIntersector
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;

public class LineSegmentEnvelopeIntersector {
    private static final LineIntersector lineInt = new RobustLineIntersector();

    public boolean touches(LineSegment seg, Envelope env) {
        return this.touches(seg.p0, seg.p1, env);
    }

    public boolean touches(Coordinate p0, Coordinate p1, Envelope env) {
        Envelope lineEnv = new Envelope(p0, p1);
        if (!lineEnv.intersects(env)) {
            return false;
        }
        if (env.contains(p0)) {
            return true;
        }
        if (env.contains(p1)) {
            return true;
        }
        Coordinate env0 = new Coordinate(env.getMinX(), env.getMinY());
        Coordinate env1 = new Coordinate(env.getMinX(), env.getMaxY());
        Coordinate env2 = new Coordinate(env.getMaxX(), env.getMaxY());
        Coordinate env3 = new Coordinate(env.getMaxX(), env.getMinY());
        lineInt.computeIntersection(p0, p1, env0, env1);
        if (lineInt.hasIntersection()) {
            return true;
        }
        lineInt.computeIntersection(p0, p1, env1, env2);
        if (lineInt.hasIntersection()) {
            return true;
        }
        lineInt.computeIntersection(p0, p1, env2, env3);
        if (lineInt.hasIntersection()) {
            return true;
        }
        lineInt.computeIntersection(p0, p1, env3, env0);
        return lineInt.hasIntersection();
    }
}

