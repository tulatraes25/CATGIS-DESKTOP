/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.CGAlgorithms
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import java.util.ArrayList;
import java.util.List;
import org.saig.jump.lang.I18N;

public class CoordinateArrays {
    private static final Coordinate[] coordArrayType = new Coordinate[0];

    public static Coordinate[] toCoordinateArray(List<Coordinate> coordList) {
        return coordList.toArray(coordArrayType);
    }

    public static void reverse(Coordinate[] coord) {
        int last = coord.length - 1;
        int mid = last / 2;
        int i = 0;
        while (i <= mid) {
            Coordinate tmp = coord[i];
            coord[i] = coord[last - i];
            coord[last - i] = tmp;
            ++i;
        }
    }

    public static Geometry toLineOrPoint(Coordinate[] coords, GeometryFactory fact) {
        if (coords.length > 1) {
            return fact.createLineString(coords);
        }
        if (coords.length == 1) {
            return fact.createPoint(coords[0]);
        }
        return fact.createPoint(new Coordinate());
    }

    public static boolean equals(Coordinate[] coord1, Coordinate[] coord2) {
        if (coord1 == coord2) {
            return true;
        }
        if (coord1 == null || coord2 == null) {
            return false;
        }
        if (coord1.length != coord2.length) {
            return false;
        }
        int i = 0;
        while (i < coord1.length) {
            if (!coord1[i].equals((Object)coord2[i])) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public static List<Geometry> fromCoordinateArrays(List<Coordinate[]> coordArrays, GeometryFactory fact) {
        ArrayList<Geometry> geomList = new ArrayList<Geometry>();
        for (Coordinate[] coords : coordArrays) {
            Geometry geom = CoordinateArrays.toLineOrPoint(coords, fact);
            geomList.add(geom);
        }
        return geomList;
    }

    public static void addCoordinateArrays(Geometry g, boolean orientPolygons, List<Coordinate[]> coordArrayList) {
        if (g.getDimension() <= 0) {
            return;
        }
        if (g instanceof LineString) {
            LineString l = (LineString)g;
            coordArrayList.add(l.getCoordinates());
        } else if (g instanceof Polygon) {
            Polygon poly = (Polygon)g;
            Coordinate[] shell = poly.getExteriorRing().getCoordinates();
            if (orientPolygons) {
                shell = CoordinateArrays.ensureOrientation(shell, -1);
            }
            coordArrayList.add(shell);
            int i = 0;
            while (i < poly.getNumInteriorRing()) {
                Coordinate[] hole = poly.getInteriorRingN(i).getCoordinates();
                if (orientPolygons) {
                    hole = CoordinateArrays.ensureOrientation(hole, 1);
                }
                coordArrayList.add(hole);
                ++i;
            }
        } else if (g instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection)g;
            int i = 0;
            while (i < gc.getNumGeometries()) {
                CoordinateArrays.addCoordinateArrays(gc.getGeometryN(i), orientPolygons, coordArrayList);
                ++i;
            }
        } else {
            Assert.shouldNeverReachHere((String)I18N.getMessage("com.vividsolutions.jump.util.CoordinateArrays.geometry-of-type-{0}-not-handled", new Object[]{g.getClass().getName()}));
        }
    }

    public static Coordinate[] ensureOrientation(Coordinate[] coord, int desiredOrientation) {
        int orientation;
        if (coord.length == 0) {
            return coord;
        }
        int n = orientation = CGAlgorithms.isCCW((Coordinate[])coord) ? 1 : -1;
        if (orientation != desiredOrientation) {
            Coordinate[] reverse = (Coordinate[])coord.clone();
            CoordinateArrays.reverse(reverse);
            return reverse;
        }
        return coord;
    }

    public static List<Coordinate[]> toCoordinateArrays(Geometry g, boolean orientPolygons) {
        ArrayList<Coordinate[]> coordArrayList = new ArrayList<Coordinate[]>();
        CoordinateArrays.addCoordinateArrays(g, orientPolygons, coordArrayList);
        return coordArrayList;
    }
}

