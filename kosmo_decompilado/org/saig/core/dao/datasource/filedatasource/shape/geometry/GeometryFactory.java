/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeFactory;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;

public class GeometryFactory {
    public static ShapeGeometry toGeneralPath(Geometry geom) {
        if (geom instanceof Point) {
            Point point = (Point)geom;
            return ShapeFactory.createPoint2D(point.getX(), point.getY());
        }
        if (geom instanceof LineString) {
            LineString line = (LineString)geom;
            SAIGGeneralPath shape = new SAIGGeneralPath(0, line.getNumPoints());
            Point p0 = line.getPointN(0);
            shape.moveTo(p0.getX(), p0.getY());
            int i = 1;
            while (i < line.getNumPoints()) {
                Point p = line.getPointN(i);
                shape.lineTo(p.getX(), p.getY());
                ++i;
            }
            return ShapeFactory.createPolyline2D(shape);
        }
        if (geom instanceof Polygon) {
            Polygon polygon = (Polygon)geom;
            SAIGGeneralPath shape = new SAIGGeneralPath(0, polygon.getNumPoints());
            int numParts = polygon.getNumGeometries();
            int i = 0;
            while (i < numParts) {
                Polygon geomPol = (Polygon)polygon.getGeometryN(i);
                LineString partGeom = geomPol.getExteriorRing();
                Point p0 = partGeom.getPointN(0);
                shape.moveTo(p0.getX(), p0.getY());
                int j = 1;
                while (j < partGeom.getNumPoints()) {
                    Point p = partGeom.getPointN(j);
                    shape.lineTo(p.getX(), p.getY());
                    ++j;
                }
                ++i;
            }
            return ShapeFactory.createPolygon2D(shape);
        }
        return null;
    }
}

