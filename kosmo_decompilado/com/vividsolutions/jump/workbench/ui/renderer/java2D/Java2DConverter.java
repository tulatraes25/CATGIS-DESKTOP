/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package com.vividsolutions.jump.workbench.ui.renderer.java2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.GeometryCollectionShape;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.PolygonShape;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.saig.jump.lang.I18N;

public class Java2DConverter {
    private static double POINT_MARKER_SIZE = 3.0;
    private PointConverter pointConverter;

    public Java2DConverter(PointConverter pointConverter) {
        this.pointConverter = pointConverter;
    }

    private Shape toShape(Polygon p) throws NoninvertibleTransformException {
        ArrayList<Coordinate[]> holeVertexCollection = new ArrayList<Coordinate[]>();
        int j = 0;
        while (j < p.getNumInteriorRing()) {
            holeVertexCollection.add(this.toViewCoordinates(p.getInteriorRingN(j).getCoordinates()));
            ++j;
        }
        return new PolygonShape(this.toViewCoordinates(p.getExteriorRing().getCoordinates()), holeVertexCollection);
    }

    private Coordinate[] toViewCoordinates(Coordinate[] modelCoordinates) throws NoninvertibleTransformException {
        Coordinate[] viewCoordinates = new Coordinate[modelCoordinates.length];
        int i = 0;
        while (i < modelCoordinates.length) {
            Point2D point2D = this.toViewPoint(modelCoordinates[i]);
            viewCoordinates[i] = new Coordinate(point2D.getX(), point2D.getY());
            ++i;
        }
        return viewCoordinates;
    }

    private Shape toShape(GeometryCollection gc) throws NoninvertibleTransformException {
        GeometryCollectionShape shape = new GeometryCollectionShape();
        int i = 0;
        while (i < gc.getNumGeometries()) {
            Geometry g = gc.getGeometryN(i);
            shape.add(this.toShape(g));
            ++i;
        }
        return shape;
    }

    private GeneralPath toShape(MultiLineString mls) throws NoninvertibleTransformException {
        GeneralPath path = new GeneralPath();
        int i = 0;
        while (i < mls.getNumGeometries()) {
            LineString lineString = (LineString)mls.getGeometryN(i);
            path.append(this.toShape(lineString), false);
            ++i;
        }
        return path;
    }

    private GeneralPath toShape(LineString lineString) throws NoninvertibleTransformException {
        GeneralPath shape = new GeneralPath();
        Point2D viewPoint = this.toViewPoint(lineString.getCoordinateN(0));
        shape.moveTo((float)viewPoint.getX(), (float)viewPoint.getY());
        int i = 1;
        while (i < lineString.getNumPoints()) {
            viewPoint = this.toViewPoint(lineString.getCoordinateN(i));
            shape.lineTo((float)viewPoint.getX(), (float)viewPoint.getY());
            ++i;
        }
        return shape;
    }

    private Shape toShape(Point point) throws NoninvertibleTransformException {
        Rectangle2D.Double pointMarker = new Rectangle2D.Double(0.0, 0.0, POINT_MARKER_SIZE, POINT_MARKER_SIZE);
        Point2D viewPoint = this.toViewPoint(point.getCoordinate());
        pointMarker.x = viewPoint.getX() - POINT_MARKER_SIZE / 2.0;
        pointMarker.y = viewPoint.getY() - POINT_MARKER_SIZE / 2.0;
        return pointMarker;
    }

    private Point2D toViewPoint(Coordinate modelCoordinate) throws NoninvertibleTransformException {
        Point2D viewPoint = this.pointConverter.toViewPoint(modelCoordinate);
        return new Point2D.Double(Math.round(viewPoint.getX()), Math.round(viewPoint.getY()));
    }

    public Shape toShape(Geometry geometry) throws NoninvertibleTransformException {
        if (geometry.isEmpty()) {
            return new GeneralPath();
        }
        if (geometry instanceof Polygon) {
            return this.toShape((Polygon)geometry);
        }
        if (geometry instanceof MultiPolygon) {
            return this.toShape((GeometryCollection)((MultiPolygon)geometry));
        }
        if (geometry instanceof LineString) {
            return this.toShape((LineString)geometry);
        }
        if (geometry instanceof MultiLineString) {
            return this.toShape((MultiLineString)geometry);
        }
        if (geometry instanceof Point) {
            return this.toShape((Point)geometry);
        }
        if (geometry instanceof GeometryCollection) {
            return this.toShape((GeometryCollection)geometry);
        }
        throw new IllegalArgumentException(String.valueOf(I18N.getString("workbench.ui.renderer.java2D.Java2DConverter.unrecognized-geometry-class")) + geometry.getClass());
    }

    public static interface PointConverter {
        public Point2D toViewPoint(Coordinate var1) throws NoninvertibleTransformException;
    }
}

