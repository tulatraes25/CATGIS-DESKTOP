/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.MathUtil;
import java.awt.geom.Point2D;
import java.util.Collection;

public class CoordUtil {
    public static Coordinate average(Coordinate c1, Coordinate c2) {
        return new Coordinate(MathUtil.avg(c1.x, c2.x), MathUtil.avg(c1.y, c2.y));
    }

    public static Coordinate average(Collection<Coordinate> coordinates) {
        Assert.isTrue((!coordinates.isEmpty() ? 1 : 0) != 0);
        double xSum = 0.0;
        double ySum = 0.0;
        for (Coordinate coordinate : coordinates) {
            xSum += coordinate.x;
            ySum += coordinate.y;
        }
        return new Coordinate(xSum / (double)coordinates.size(), ySum / (double)coordinates.size());
    }

    public static Coordinate closest(Collection<Coordinate> coordinates, Coordinate p) {
        Assert.isTrue((!coordinates.isEmpty() ? 1 : 0) != 0);
        Coordinate closest = coordinates.iterator().next();
        for (Coordinate candidate : coordinates) {
            if (!(p.distance(candidate) < p.distance(closest))) continue;
            closest = candidate;
        }
        return closest;
    }

    public static Coordinate add(Coordinate c1, Coordinate c2) {
        return new Coordinate(c1.x + c2.x, c1.y + c2.y, c1.z + c2.z);
    }

    public static Coordinate subtract(Coordinate c1, Coordinate c2) {
        return new Coordinate(c1.x - c2.x, c1.y - c2.y);
    }

    public static Coordinate multiply(double d, Coordinate c) {
        return new Coordinate(d * c.x, d * c.y);
    }

    public static Coordinate divide(Coordinate c, double d) {
        return new Coordinate(c.x / d, c.y / d);
    }

    public static Coordinate toCoordinate(Point2D point) {
        return new Coordinate(point.getX(), point.getY());
    }

    public static Point2D toPoint2D(Coordinate coordinate) {
        return new Point2D.Double(coordinate.x, coordinate.y);
    }

    public static Point2D add(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
    }

    public static Point2D subtract(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
    }
}

