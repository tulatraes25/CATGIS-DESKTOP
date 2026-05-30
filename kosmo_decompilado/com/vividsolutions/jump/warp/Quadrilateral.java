/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.warp.TaggedCoordinate;
import com.vividsolutions.jump.warp.Triangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Quadrilateral
implements Cloneable {
    private Coordinate p1;
    private Coordinate p2;
    private Coordinate p3;
    private Coordinate p4;
    private GeometryFactory factory = new GeometryFactory();

    public Quadrilateral(Coordinate p1, Coordinate p2, Coordinate p3, Coordinate p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }

    public Envelope getEnvelope() {
        Envelope envelope = new Envelope(this.p1);
        envelope.expandToInclude(this.p2);
        envelope.expandToInclude(this.p3);
        envelope.expandToInclude(this.p4);
        return envelope;
    }

    protected Object clone() {
        return new Quadrilateral(new Coordinate(this.p1), new Coordinate(this.p2), new Coordinate(this.p3), new Coordinate(this.p4));
    }

    private boolean diagonalsIntersect() {
        return Line2D.linesIntersect(this.p1.x, this.p1.y, this.p3.x, this.p3.y, this.p2.x, this.p2.y, this.p4.x, this.p4.y);
    }

    public boolean isConvex() {
        return this.diagonalsIntersect();
    }

    public List<Triangle> triangles() {
        ArrayList<Triangle> triangles = new ArrayList<Triangle>();
        triangles.add(new Triangle(this.p1, this.p2, this.p3));
        triangles.add(new Triangle(this.p1, this.p4, this.p3));
        return triangles;
    }

    private String toString(Coordinate c) {
        return String.valueOf(c.x) + " " + c.y;
    }

    public String toString() {
        return "LINESTRING (" + this.toString(this.p1) + ", " + this.toString(this.p2) + ", " + this.toString(this.p3) + ", " + this.toString(this.p4) + ", " + this.toString(this.p1) + ")";
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

    public Coordinate getP4() {
        return this.p4;
    }

    public Polygon toPolygon() {
        return this.factory.createPolygon(this.factory.createLinearRing(new Coordinate[]{this.p1, this.p2, this.p3, this.p4, this.p1}), null);
    }

    public Collection<TaggedCoordinate> verticesOutside(Collection<TaggedCoordinate> vertices) {
        ArrayList<TaggedCoordinate> outsideVertices = new ArrayList<TaggedCoordinate>();
        Polygon quadrilateralPolygon = this.toPolygon();
        for (TaggedCoordinate vertex : vertices) {
            Point p = this.factory.createPoint((Coordinate)vertex);
            if (quadrilateralPolygon.contains((Geometry)p)) continue;
            outsideVertices.add(vertex);
        }
        return outsideVertices;
    }
}

