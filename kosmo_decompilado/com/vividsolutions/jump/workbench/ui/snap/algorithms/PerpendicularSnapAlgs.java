/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.vividsolutions.jump.workbench.ui.snap.algorithms;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.util.ArrayList;
import java.util.List;

public class PerpendicularSnapAlgs {
    public List<Coordinate> extractPerpendicularPoints(Coordinate originalPoint, Coordinate lastClickedPoint, Geometry geometry, double units) {
        ArrayList<Coordinate> vertices = new ArrayList<Coordinate>();
        Geometry candidate = VisiblePointsAndLinesCache.toPointsAndLines(geometry);
        int i = 0;
        while (i < candidate.getNumGeometries()) {
            Coordinate[] points = candidate.getGeometryN(i).getCoordinates();
            int k = 0;
            while (k < points.length - 1) {
                boolean existPerpendicularPoint;
                LineSegment lineSegment = new LineSegment(points[k], points[k + 1]);
                Coordinate perpendicularPoint = this.getPerpendicularPoint(lineSegment, lastClickedPoint);
                boolean bl = existPerpendicularPoint = perpendicularPoint != null;
                if (existPerpendicularPoint) {
                    boolean perpendicularPointIsInRange;
                    boolean bl2 = perpendicularPointIsInRange = perpendicularPoint.distance(originalPoint) < units;
                    if (perpendicularPointIsInRange) {
                        vertices.add(perpendicularPoint);
                    }
                }
                ++k;
            }
            ++i;
        }
        return vertices;
    }

    private Coordinate getPerpendicularPoint(LineSegment lineSegment, Coordinate lastClickedPoint) {
        boolean closestPointIsNotExtreme;
        if (lastClickedPoint == null) {
            return null;
        }
        Coordinate closestPoint = lineSegment.closestPoint(lastClickedPoint);
        boolean bl = closestPointIsNotExtreme = !lineSegment.p0.equals2D(closestPoint) && !lineSegment.p1.equals2D(closestPoint);
        if (closestPointIsNotExtreme) {
            return closestPoint;
        }
        return null;
    }
}

