/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.warp;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.util.Assert;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Triangle {
    private static GeometryFactory factory = new GeometryFactory();
    private static Point2D hasher = new Point2D.Double();
    private SaalfeldCoefficients sc;
    private Coordinate p1;
    private Coordinate p2;
    private Coordinate p3;
    private int hashCode;
    private Envelope envelope = null;

    public Triangle(Coordinate p1, Coordinate p2, Coordinate p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        Assert.isTrue((!p1.equals((Object)p2) ? 1 : 0) != 0, (String)("p1 = " + p1 + "; p2 = " + p2));
        Assert.isTrue((!p2.equals((Object)p3) ? 1 : 0) != 0, (String)("p1 = " + p1 + "; p2 = " + p2));
        Assert.isTrue((!p3.equals((Object)p1) ? 1 : 0) != 0, (String)("p1 = " + p1 + "; p2 = " + p2));
        this.initHashCode();
        this.sc = this.saalfeldCoefficients();
    }

    public Coordinate getP1() {
        return this.p1;
    }

    public Coordinate getP2() {
        return this.p2;
    }

    public Coordinate getP3() {
        return this.p3;
    }

    public double getMinHeight() {
        return 2.0 * this.getArea() / this.getMaxSideLength();
    }

    public double getArea() {
        return 0.5 * Math.abs((this.p2.x - this.p1.x) * (this.p3.y - this.p1.y) - (this.p2.y - this.p1.y) * (this.p3.x - this.p1.x));
    }

    public double getMaxSideLength() {
        return Math.max(Point2D.distance(this.p1.x, this.p1.y, this.p2.x, this.p2.y), Math.max(Point2D.distance(this.p2.x, this.p2.y, this.p3.x, this.p3.y), Point2D.distance(this.p3.x, this.p3.y, this.p1.x, this.p1.y)));
    }

    public LinearRing toLinearRing() {
        return factory.createLinearRing(new Coordinate[]{this.p1, this.p2, this.p3, this.p1});
    }

    public String toString() {
        return this.toLinearRing().toString();
    }

    public boolean contains(Coordinate p) {
        if (p.equals((Object)this.p1) || p.equals((Object)this.p2) || p.equals((Object)this.p3)) {
            return true;
        }
        if (CGAlgorithms.computeOrientation((Coordinate)this.p1, (Coordinate)this.p2, (Coordinate)p) == -CGAlgorithms.computeOrientation((Coordinate)this.p2, (Coordinate)this.p3, (Coordinate)p)) {
            return false;
        }
        return CGAlgorithms.computeOrientation((Coordinate)this.p1, (Coordinate)this.p2, (Coordinate)p) != -CGAlgorithms.computeOrientation((Coordinate)this.p3, (Coordinate)this.p1, (Coordinate)p);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Triangle)) {
            return false;
        }
        Triangle other = (Triangle)o;
        return other.hasVertex(this.p1) && other.hasVertex(this.p2) && other.hasVertex(this.p3);
    }

    public boolean hasVertex(Coordinate v) {
        return this.p1.equals((Object)v) || this.p2.equals((Object)v) || this.p3.equals((Object)v);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public List<Triangle> subTriangles(Coordinate newVertex) {
        ArrayList<Triangle> triangles = new ArrayList<Triangle>();
        triangles.add(new Triangle(this.p1, this.p2, newVertex));
        triangles.add(new Triangle(this.p2, this.p3, newVertex));
        triangles.add(new Triangle(this.p3, this.p1, newVertex));
        return triangles;
    }

    protected Coordinate min(Coordinate a, Coordinate b) {
        return a.compareTo((Object)b) < 0 ? a : b;
    }

    private void initHashCode() {
        Coordinate min = this.min(this.min(this.p1, this.p2), this.p3);
        hasher.setLocation(min.x, min.y);
        this.hashCode = hasher.hashCode();
    }

    private SaalfeldCoefficients saalfeldCoefficients() {
        double T = this.p1.x * this.p2.y + this.p2.x * this.p3.y + this.p3.x * this.p1.y - this.p3.x * this.p2.y - this.p2.x * this.p1.y - this.p1.x * this.p3.y;
        SaalfeldCoefficients sc = new SaalfeldCoefficients();
        sc.A1 = (this.p3.x - this.p2.x) / T;
        sc.B1 = (this.p2.y - this.p3.y) / T;
        sc.C1 = (this.p2.x * this.p3.y - this.p3.x * this.p2.y) / T;
        sc.A2 = (this.p1.x - this.p3.x) / T;
        sc.B2 = (this.p3.y - this.p1.y) / T;
        sc.C2 = (this.p3.x * this.p1.y - this.p1.x * this.p3.y) / T;
        return sc;
    }

    public Coordinate toSimplicialCoordinate(Coordinate euclideanCoordinate) {
        double s1 = this.s1(euclideanCoordinate);
        double s2 = this.s2(euclideanCoordinate);
        double s3 = 1.0 - s1 - s2;
        return new Coordinate(s1, s2, s3);
    }

    public Coordinate toEuclideanCoordinate(Coordinate simplicialCoordinate) {
        return this.toEuclideanCoordinate(simplicialCoordinate.x, simplicialCoordinate.y, simplicialCoordinate.z);
    }

    private Coordinate toEuclideanCoordinate(double s1, double s2, double s3) {
        return new Coordinate(s1 * this.p1.x + s2 * this.p2.x + s3 * this.p3.x, s1 * this.p1.y + s2 * this.p2.y + s3 * this.p3.y);
    }

    private double s1(Coordinate c) {
        return this.sc.A1 * c.y + this.sc.B1 * c.x + this.sc.C1;
    }

    private double s2(Coordinate c) {
        return this.sc.A2 * c.y + this.sc.B2 * c.x + this.sc.C2;
    }

    public Envelope getEnvelope() {
        if (this.envelope == null) {
            this.envelope = new Envelope(this.p1, this.p2);
            this.envelope.expandToInclude(this.p3);
        }
        return this.envelope;
    }

    private class SaalfeldCoefficients {
        public double A1;
        public double B1;
        public double C1;
        public double A2;
        public double B2;
        public double C2;

        private SaalfeldCoefficients() {
        }
    }
}

