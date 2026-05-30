/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateSequence
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.Point
 */
package org.saig.core.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import java.awt.geom.Point2D;
import org.saig.core.geometry.Arc;
import org.saig.core.geometry.Circle;
import org.saig.core.geometry.Ellipse;

public class ExtendedGeometryFactory
extends GeometryFactory {
    private static final long serialVersionUID = 1L;

    public Arc createArc(CoordinateSequence cs) {
        Coordinate c0 = cs.getCoordinate(0);
        boolean is3D = false;
        double[] z = new double[3];
        if (!Double.isNaN(c0.z)) {
            is3D = true;
            z[0] = c0.z;
        }
        Coordinate ci = cs.getCoordinate(cs.size() / 2);
        if (is3D) {
            z[1] = ci.z;
        }
        Coordinate c1 = cs.getCoordinate(cs.size() - 1);
        if (is3D) {
            z[2] = c1.z;
        }
        if (!is3D) {
            z = null;
        }
        return new Arc(cs, this, new Point2D.Double(c0.x, c0.y), new Point2D.Double(ci.x, ci.y), new Point2D.Double(c1.x, c1.y), z);
    }

    public Circle createCircle(LinearRing extRing) {
        double z = extRing.getCoordinateN((int)0).z;
        Point p = extRing.getCentroid();
        double r = extRing.getCoordinateN((extRing.getNumPoints() - 1) / 2).distance(p.getCoordinate());
        return new Circle(extRing, this, new Point2D.Double(p.getX(), p.getY()), r, z);
    }

    public Ellipse createEllipse(LinearRing extRing, double d, Point2D.Double pInit, Point2D.Double pEnd) {
        double z = extRing.getCoordinateN((int)0).z;
        return new Ellipse(extRing, this, pInit, pEnd, d, z);
    }
}

