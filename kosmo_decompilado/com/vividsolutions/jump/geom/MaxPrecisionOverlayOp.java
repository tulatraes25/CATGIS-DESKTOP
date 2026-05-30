/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.geom.AffineTransform;

public class MaxPrecisionOverlayOp {
    public static double getMinInAbsValue(double x0, double x1) {
        double absx1;
        double absx0 = Math.abs(x0);
        if (absx0 < (absx1 = Math.abs(x1))) {
            return x0;
        }
        return x1;
    }

    private void printBits(double x) {
        System.out.println("double value = " + x);
        System.out.println(Long.toBinaryString(Double.doubleToLongBits(x)));
        System.out.println(Long.toHexString(Double.doubleToLongBits(x)));
    }

    public Geometry intersection(Geometry g0, Geometry g1) {
        Envelope env = new Envelope();
        env.expandToInclude(g0.getEnvelopeInternal());
        env.expandToInclude(g1.getEnvelopeInternal());
        this.printBits(env.getMinX());
        this.printBits(env.getMaxX());
        this.printBits(env.getMinY());
        this.printBits(env.getMaxY());
        Coordinate minPt = new Coordinate();
        minPt.x = MaxPrecisionOverlayOp.getMinInAbsValue(env.getMinX(), env.getMaxX());
        minPt.y = MaxPrecisionOverlayOp.getMinInAbsValue(env.getMinY(), env.getMaxY());
        minPt.x = 475136.0;
        minPt.y = 5366784.0;
        Coordinate negMinPt = new Coordinate(minPt);
        negMinPt.x = -negMinPt.x;
        negMinPt.y = -negMinPt.y;
        AffineTransform trans = new AffineTransform();
        trans.translate(negMinPt);
        Geometry g0Copy = (Geometry)g0.clone();
        Geometry g1Copy = (Geometry)g1.clone();
        this.printBits(g1Copy.getCoordinate().x);
        g0Copy.apply((CoordinateFilter)new CoordinatePrecisionReducer());
        g1Copy.apply((CoordinateFilter)new CoordinatePrecisionReducer());
        this.printBits(g1Copy.getCoordinate().x);
        this.printBits(2345.626654971);
        System.out.println(g0Copy);
        System.out.println(g1Copy);
        Geometry result = g0Copy.intersection(g1Copy);
        System.out.println(result.getArea() / g0Copy.getArea());
        trans.translate(minPt);
        trans.apply(result);
        return result;
    }

    public class CoordinatePrecisionReducer
    implements CoordinateFilter {
        private static final double POW10 = 1000.0;

        public void filter(Coordinate p) {
            double x = p.x * 1000.0;
            p.x = x = Math.floor(x);
            double y = p.y * 1000.0;
            p.y = y = Math.floor(y);
        }
    }
}

