/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.core.geometry;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.geom.Point2D;

public class Circle
extends Polygon {
    private static final long serialVersionUID = 1L;
    private Point2D centro;
    private double radio;
    private double z;
    private boolean is3D = false;

    public Circle(LinearRing extRing, GeometryFactory geomFact, Point2D centro, double r, double z) {
        super(extRing, null, geomFact);
        this.centro = centro;
        this.radio = r;
        this.z = z;
        this.is3D = !Double.isNaN(z);
    }

    public Point2D getCentro() {
        return this.centro;
    }

    public void setCentro(Point2D centro) {
        this.centro = centro;
    }

    public double getRadio() {
        return this.radio;
    }

    public void setRadio(double radio) {
        this.radio = radio;
    }

    public boolean is3D() {
        return this.is3D;
    }

    public double getZ() {
        return this.z;
    }
}

