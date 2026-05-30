/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.ICoordTrans
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPathIterator;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePoint2D;

public class ShapeMultiPoint2D
implements IShape {
    private static final long serialVersionUID = 1L;
    protected double[] x = null;
    protected double[] y = null;

    public ShapeMultiPoint2D(double[] x, double[] y) {
        this.x = x;
        this.y = y;
    }

    public ShapeMultiPoint2D(ShapePoint2D[] points) {
        double[] auxX = new double[points.length];
        double[] auxY = new double[points.length];
        int i = 0;
        while (i < points.length) {
            auxX[i] = points[i].getX();
            auxY[i] = points[i].getY();
            ++i;
        }
        this.x = auxX;
        this.y = auxY;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        int i = 0;
        while (i < this.getNumPoints()) {
            if (r.contains(this.x[i], this.y[i])) {
                return true;
            }
            ++i;
        }
        return false;
    }

    @Override
    public Rectangle2D getBounds2D() {
        Rectangle2D.Double r = null;
        if (this.getNumPoints() > 0) {
            r = new Rectangle2D.Double(this.x[0], this.y[0], 0.001, 0.001);
        }
        int i = 1;
        while (i < this.getNumPoints()) {
            r.add(this.x[i], this.y[i]);
            ++i;
        }
        return r;
    }

    @Override
    public Rectangle getBounds() {
        Rectangle r = null;
        if (this.getNumPoints() > 0) {
            r = new Rectangle((int)this.x[0], (int)this.y[0], 0, 0);
        }
        int i = 1;
        while (i < this.getNumPoints()) {
            r.add(this.x[i], this.y[i]);
            ++i;
        }
        return r;
    }

    public int getNumPoints() {
        return this.x.length;
    }

    public ShapePoint2D getPoint(int i) {
        return new ShapePoint2D(this.x[i], this.y[i]);
    }

    @Override
    public int getShapeType() {
        return 32;
    }

    @Override
    public IShape cloneShape() {
        return new ShapeMultiPoint2D((double[])this.x.clone(), (double[])this.y.clone());
    }

    @Override
    public SAIGGeneralPath getGeneralPath() {
        SAIGGeneralPath gpx = new SAIGGeneralPath();
        if (this.getNumPoints() > 0) {
            gpx.moveTo(this.x[0], this.y[0]);
        }
        int i = 1;
        while (i < this.getNumPoints()) {
            gpx.moveTo(this.x[i], this.y[i]);
            ++i;
        }
        return gpx;
    }

    @Override
    public boolean contains(double x, double y) {
        int i = 0;
        while (i < this.x.length) {
            if (x == this.x[i] || y == this.y[i]) {
                return true;
            }
            ++i;
        }
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        Rectangle2D.Double rAux = new Rectangle2D.Double(x, y, w, h);
        int i = 0;
        while (i < this.x.length) {
            if (rAux.contains(this.x[i], this.y[i])) {
                return true;
            }
            ++i;
        }
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return this.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Point2D arg0) {
        return this.contains(arg0.getX(), arg0.getY());
    }

    @Override
    public boolean contains(Rectangle2D arg0) {
        return this.contains(arg0.getX(), arg0.getY(), arg0.getWidth(), arg0.getHeight());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform arg0) {
        return (SAIGGeneralPathIterator)this.getGeneralPath().getPathIterator(arg0);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform arg0, double arg1) {
        return this.getGeneralPath().getPathIterator(arg0, arg1);
    }

    @Override
    public void reProject(ICoordTrans ct) {
        int i = 0;
        while (i < this.x.length) {
            Point2D p = new Point2D.Double(this.x[i], this.y[i]);
            p = ct.convert(p, p);
            this.x[i] = p.getX();
            this.y[i] = p.getY();
            ++i;
        }
    }

    public IShape getInternalShape() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return this.x == null || this.y == null;
    }
}

