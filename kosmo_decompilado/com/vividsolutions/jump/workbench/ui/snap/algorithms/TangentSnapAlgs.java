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

public class TangentSnapAlgs {
    public Coordinate[] getTangentPoints(Geometry toSpecificGeometry, Coordinate externalPoint) {
        Coordinate centralPoint;
        Coordinate[] coordinates = toSpecificGeometry.getCoordinates();
        if (coordinates.length == 0) {
            return null;
        }
        if (coordinates.length == 1) {
            return coordinates;
        }
        Coordinate leftPoint = centralPoint = toSpecificGeometry.getCentroid().getCoordinate();
        Coordinate rightPoint = centralPoint;
        Coordinate[] coordinateArray = coordinates;
        int n = coordinates.length;
        int n2 = 0;
        while (n2 < n) {
            Coordinate coord = coordinateArray[n2];
            if (this.isLeft(coord, externalPoint, centralPoint)) {
                if (this.isLeft(coord, externalPoint, leftPoint)) {
                    leftPoint = coord;
                }
            } else if (!this.isLeft(coord, externalPoint, rightPoint)) {
                rightPoint = coord;
            }
            ++n2;
        }
        return new Coordinate[]{leftPoint, rightPoint};
    }

    private boolean isLeft(Coordinate c, Coordinate sega, Coordinate segb) {
        LineSegment ls = new LineSegment(sega, segb);
        return ls.orientationIndex(c) == 1;
    }
}

