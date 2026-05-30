/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 */
package org.saig.core.geometry;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import java.awt.geom.Point2D;

public class Arc
extends LineString {
    private static final long serialVersionUID = 1L;
    private Point2D arcStartPoint;
    private Point2D arcIntermediatePoint;
    private Point2D arcEndPoint;
    double[] z;

    public Arc(CoordinateSequence cs, GeometryFactory geomF, Point2D puntoInicio, Point2D puntoIntermedio, Point2D puntoFinal, double[] z) {
        super(cs, geomF);
        this.arcStartPoint = puntoInicio;
        this.arcIntermediatePoint = puntoIntermedio;
        this.arcEndPoint = puntoFinal;
        this.z = z;
    }

    public boolean is3D() {
        return this.z != null;
    }

    public double[] getZs() {
        return this.z;
    }

    public Point2D geArcEndPoint() {
        return this.arcEndPoint;
    }

    public void setEndPoint(Point2D endPoint) {
        this.arcEndPoint = endPoint;
    }

    public Point2D getIntermediatePoint() {
        return this.arcIntermediatePoint;
    }

    public void setIntermediatePoint(Point2D intermediatePoint) {
        this.arcIntermediatePoint = intermediatePoint;
    }

    public Point2D getArcStartPoint() {
        return this.arcStartPoint;
    }

    public void setStartPoint(Point2D startPoint) {
        this.arcStartPoint = startPoint;
    }
}

