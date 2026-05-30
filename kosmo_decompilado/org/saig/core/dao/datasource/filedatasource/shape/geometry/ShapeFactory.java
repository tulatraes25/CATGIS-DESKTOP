/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.edition.UtilFunctions
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import com.iver.cit.gvsig.fmap.edition.UtilFunctions;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeArc2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeCircle2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeEllipse2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeMultiPoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeMultipoint3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon3D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline2D;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline3D;

public class ShapeFactory {
    public static ShapeGeometry createPoint2D(double x, double y) {
        return new ShapeGeometry(new ShapePoint2D(x, y));
    }

    public static ShapeGeometry createPoint2D(ShapePoint2D p) {
        return new ShapeGeometry(p);
    }

    public static ShapeGeometry createMultipoint2D(double[] x, double[] y) {
        return new ShapeGeometry(new ShapeMultiPoint2D(x, y));
    }

    public static ShapeGeometry createPolyline2D(SAIGGeneralPath shape) {
        return new ShapeGeometry(new ShapePolyline2D(shape));
    }

    public static ShapeGeometry createPoint3D(double x, double y, double z) {
        return new ShapeGeometry3D(new ShapePoint3D(x, y, z));
    }

    public static ShapeGeometry createPolygon2D(SAIGGeneralPath shape) {
        return new ShapeGeometry(new ShapePolygon2D(shape));
    }

    public static ShapeGeometry createMultipoint3D(double[] x, double[] y, double[] z) {
        return new ShapeGeometry3D(new ShapeMultipoint3D(x, y, z));
    }

    public static ShapeGeometry createPolyline3D(SAIGGeneralPath shape, double[] pZ) {
        return new ShapeGeometry3D(new ShapePolyline3D(shape, pZ));
    }

    public static ShapeGeometry createPolygon3D(SAIGGeneralPath shape, double[] pZ) {
        return new ShapeGeometry3D(new ShapePolygon3D(shape, pZ));
    }

    public static ShapeGeometry createGeometry(IShape shp) {
        if (shp instanceof IShape3D) {
            return new ShapeGeometry3D(shp);
        }
        return new ShapeGeometry(shp);
    }

    public static ShapeGeometry createCircle(Point2D center, Point2D r) {
        double radio = center.distance(r);
        return ShapeFactory.createCircle(center, radio);
    }

    public static ShapeGeometry createCircle(Point2D center, double radio) {
        Arc2D.Double arc = new Arc2D.Double(center.getX() - radio, center.getY() - radio, 2.0 * radio, 2.0 * radio, 0.0, 360.0, 0);
        return new ShapeGeometry(new ShapeCircle2D(new SAIGGeneralPath(arc), center, radio));
    }

    public static ShapeGeometry createCircle(Point2D p1, Point2D p2, Point2D p3) {
        Point2D center = UtilFunctions.getCenter((Point2D)p1, (Point2D)p2, (Point2D)p3);
        if (center != null) {
            return ShapeFactory.createCircle(center, p1);
        }
        return null;
    }

    public static ShapeGeometry createArc(Point2D p1, Point2D p2, Point2D p3) {
        Arc2D arco = UtilFunctions.createArc((Point2D)p1, (Point2D)p2, (Point2D)p3);
        if (arco == null) {
            return null;
        }
        ShapeArc2D arc = new ShapeArc2D(new SAIGGeneralPath(arco), p1, p2, p3);
        ShapeGeometry geom = new ShapeGeometry(arc);
        return geom;
    }

    public static ShapeGeometry createEllipse(Point2D axis1Start, Point2D axis1End, double axis2Length) {
        double xAxis = axis1Start.distance(axis1End);
        Arc2D.Double arc = new Arc2D.Double(axis1Start.getX(), axis1Start.getY() - axis2Length, xAxis, 2.0 * axis2Length, 0.0, 360.0, 0);
        double angle = UtilFunctions.getAngle((Point2D)axis1Start, (Point2D)axis1End);
        AffineTransform mT = AffineTransform.getRotateInstance(angle, axis1Start.getX(), axis1Start.getY());
        SAIGGeneralPath gp = new SAIGGeneralPath(arc);
        gp.transform(mT);
        return new ShapeGeometry(new ShapeEllipse2D(new SAIGGeneralPath(gp), axis1Start, axis1End, axis2Length));
    }
}

