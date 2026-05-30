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

public class Ellipse
extends Polygon {
    private static final long serialVersionUID = 1L;
    private Point2D startPoint;
    private Point2D endPoint;
    private double distancia;
    private double z;
    private boolean is3D;

    public Ellipse(LinearRing extRing, GeometryFactory geomFac, Point2D startPoint, Point2D endPoint, double distancia, double z) {
        super(extRing, null, geomFac);
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distancia = distancia;
        this.z = z;
        this.is3D = !Double.isNaN(z);
    }

    public double getDistancia() {
        return this.distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public Point2D getEndPoint() {
        return this.endPoint;
    }

    public void setEndPoint(Point2D endPoint) {
        this.endPoint = endPoint;
    }

    public Point2D getStartPoint() {
        return this.startPoint;
    }

    public void setStartPoint(Point2D startPoint) {
        this.startPoint = startPoint;
    }

    public boolean is3D() {
        return this.is3D;
    }

    public double getZ() {
        return this.z;
    }
}

