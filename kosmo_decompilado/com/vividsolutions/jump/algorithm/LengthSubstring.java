/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateList
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.algorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.algorithm.LocatePoint;
import org.saig.jump.lang.I18N;

public class LengthSubstring {
    private LineString line;

    public LengthSubstring(LineString line) {
        this.line = line;
    }

    public static LineString getSubstring(LineString line, double startLength, double endLength) {
        LengthSubstring ls = new LengthSubstring(line);
        return ls.getSubstring(startLength, endLength);
    }

    public LineString getSubstring(double startDistance, double endDistance) {
        Assert.isTrue((startDistance <= endDistance ? 1 : 0) != 0, (String)I18N.getString("com.vividsolutions.jump.algorithm.LengthSubstring.inverted-distances-are-not-currently-supported"));
        Coordinate[] coordinates = this.line.getCoordinates();
        if (endDistance <= 0.0) {
            return this.line.getFactory().createLineString(new Coordinate[]{coordinates[0], coordinates[0]});
        }
        if (startDistance >= this.line.getLength()) {
            return this.line.getFactory().createLineString(new Coordinate[]{coordinates[coordinates.length - 1], coordinates[coordinates.length - 1]});
        }
        if (startDistance < 0.0) {
            startDistance = 0.0;
        }
        return this.computeSubstring(startDistance, endDistance);
    }

    private LineString computeSubstring(double startDistance, double endDistance) {
        Coordinate[] coordinates = this.line.getCoordinates();
        CoordinateList newCoordinates = new CoordinateList();
        double segmentStartDistance = 0.0;
        double segmentEndDistance = 0.0;
        int i = 0;
        LineSegment segment = new LineSegment();
        while (i < coordinates.length - 1 && endDistance > segmentEndDistance) {
            segment.p0 = coordinates[i];
            segment.p1 = coordinates[i + 1];
            ++i;
            segmentStartDistance = segmentEndDistance;
            if (startDistance > (segmentEndDistance = segmentStartDistance + segment.getLength())) continue;
            if (startDistance >= segmentStartDistance && startDistance < segmentEndDistance) {
                newCoordinates.add(LocatePoint.pointAlongSegment(segment.p0, segment.p1, startDistance - segmentStartDistance), false);
            }
            if (endDistance >= segmentEndDistance) {
                newCoordinates.add(new Coordinate(segment.p1), false);
            }
            if (!(endDistance >= segmentStartDistance) || !(endDistance < segmentEndDistance)) continue;
            newCoordinates.add(LocatePoint.pointAlongSegment(segment.p0, segment.p1, endDistance - segmentStartDistance), false);
        }
        Coordinate[] newCoordinateArray = newCoordinates.toCoordinateArray();
        if (newCoordinateArray.length <= 1) {
            newCoordinateArray = new Coordinate[]{newCoordinateArray[0], newCoordinateArray[0]};
        }
        return this.line.getFactory().createLineString(newCoordinateArray);
    }
}

