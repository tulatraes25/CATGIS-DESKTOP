/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.locate.SimplePointInAreaLocator
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryComponentFilter
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.algorithm.locate.SimplePointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.geom.LineSegmentEnvelopeIntersector;

public class EnvelopeIntersector {
    private static LineSegmentEnvelopeIntersector lineSegmentEnvelopeIntersector = new LineSegmentEnvelopeIntersector();

    public static boolean intersects(Geometry geometry, Envelope envelope) {
        if (envelope.isNull()) {
            return false;
        }
        if (!envelope.intersects(geometry.getEnvelopeInternal())) {
            return false;
        }
        if (geometry instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection)geometry;
            int i = 0;
            while (i < gc.getNumGeometries()) {
                if (EnvelopeIntersector.intersects(gc.getGeometryN(i), envelope)) {
                    return true;
                }
                ++i;
            }
            return false;
        }
        if (EnvelopeIntersector.intersectsBoundary(geometry, envelope)) {
            return true;
        }
        if (geometry instanceof Polygon) {
            return EnvelopeIntersector.contains((Polygon)geometry, new Coordinate(envelope.getMinX(), envelope.getMinY()));
        }
        return false;
    }

    private static boolean contains(Polygon polygon, Coordinate c) {
        return SimplePointInAreaLocator.containsPointInPolygon((Coordinate)c, (Polygon)polygon);
    }

    private static boolean intersectsBoundary(Geometry geometry, final Envelope envelope) {
        final BooleanWrapper intersects = new BooleanWrapper(false);
        geometry.apply(new GeometryComponentFilter(){

            public void filter(Geometry geometry) {
                Coordinate[] coordinates = geometry.getCoordinates();
                if (intersects.value) {
                    return;
                }
                if (envelope.contains(coordinates[0])) {
                    intersects.value = true;
                }
                int i = 1;
                while (i < coordinates.length) {
                    if (EnvelopeIntersector.intersectsLineSegment(coordinates[i], coordinates[i - 1], envelope)) {
                        intersects.value = true;
                    }
                    ++i;
                }
            }
        });
        return intersects.value;
    }

    private static boolean intersectsLineSegment(Coordinate a, Coordinate b, Envelope envelope) {
        return lineSegmentEnvelopeIntersector.touches(a, b, envelope);
    }

    private static class BooleanWrapper {
        public boolean value;

        public BooleanWrapper(boolean value) {
            this.value = value;
        }
    }
}

