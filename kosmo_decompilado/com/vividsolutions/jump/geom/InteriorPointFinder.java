/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Point
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.util.MathUtil;

public class InteriorPointFinder {
    private GeometryFactory factory = new GeometryFactory();

    public Coordinate findPoint(Geometry geometry) {
        if (geometry.isEmpty()) {
            return new Coordinate(0.0, 0.0);
        }
        if (geometry.getDimension() == 0) {
            return geometry.getCoordinate();
        }
        if (geometry instanceof GeometryCollection) {
            return this.findPoint(((GeometryCollection)geometry).getGeometryN(0));
        }
        Geometry envelopeMiddle = this.envelopeMiddle(geometry);
        if (envelopeMiddle instanceof Point) {
            return envelopeMiddle.getCoordinate();
        }
        Geometry intersections = envelopeMiddle.intersection(geometry);
        Geometry widestIntersection = this.widestGeometry(intersections);
        return this.centre(widestIntersection.getEnvelopeInternal());
    }

    protected Geometry widestGeometry(Geometry geometry) {
        if (!(geometry instanceof GeometryCollection)) {
            return geometry;
        }
        return this.widestGeometry((GeometryCollection)geometry);
    }

    private Geometry widestGeometry(GeometryCollection gc) {
        if (gc.isEmpty()) {
            return gc;
        }
        Geometry widestGeometry = gc.getGeometryN(0);
        int i = 1;
        while (i < gc.getNumGeometries()) {
            if (gc.getGeometryN(i).getEnvelopeInternal().getWidth() > widestGeometry.getEnvelopeInternal().getWidth()) {
                widestGeometry = gc.getGeometryN(i);
            }
            ++i;
        }
        return widestGeometry;
    }

    protected Geometry envelopeMiddle(Geometry geometry) {
        Envelope envelope = geometry.getEnvelopeInternal();
        if (envelope.getWidth() == 0.0) {
            return this.factory.createPoint(this.centre(envelope));
        }
        return this.factory.createLineString(new Coordinate[]{new Coordinate(envelope.getMinX(), MathUtil.avg(envelope.getMinY(), envelope.getMaxY())), new Coordinate(envelope.getMaxX(), MathUtil.avg(envelope.getMinY(), envelope.getMaxY()))});
    }

    public Coordinate centre(Envelope envelope) {
        return new Coordinate(MathUtil.avg(envelope.getMinX(), envelope.getMaxX()), MathUtil.avg(envelope.getMinY(), envelope.getMaxY()));
    }
}

