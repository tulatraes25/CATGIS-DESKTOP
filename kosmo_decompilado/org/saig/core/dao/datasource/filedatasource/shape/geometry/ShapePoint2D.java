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
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePointIterator;

public class ShapePoint2D
implements IShape {
    private static final long serialVersionUID = 1L;
    protected Point2D p;

    public ShapePoint2D(double x, double y) {
        this.p = new Point2D.Double(x, y);
    }

    public ShapePoint2D() {
    }

    public ShapePoint2D(Point2D p) {
        this.p = p;
    }

    public void transform(AffineTransform at) {
        at.transform(this.p, this.p);
    }

    @Override
    public boolean contains(double x, double y) {
        return x == this.p.getX() || y == this.p.getY();
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        Rectangle2D.Double rAux = new Rectangle2D.Double(x, y, w, h);
        return rAux.contains(this.p.getX(), this.p.getY());
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)this.p.getX(), (int)this.p.getY(), 0, 0);
    }

    public double getX() {
        return this.p.getX();
    }

    public double getY() {
        return this.p.getY();
    }

    @Override
    public boolean contains(Point2D p) {
        return false;
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle2D.Double(this.p.getX() - 0.01, this.p.getY() - 0.01, 0.02, 0.02);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return r.contains(this.p);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new ShapePointIterator(this.p, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new ShapePointIterator(this.p, at);
    }

    @Override
    public int getShapeType() {
        return 1;
    }

    @Override
    public IShape cloneShape() {
        return new ShapePoint2D(this.p.getX(), this.p.getY());
    }

    @Override
    public SAIGGeneralPath getGeneralPath() {
        SAIGGeneralPath gpx = new SAIGGeneralPath();
        gpx.moveTo(this.p.getX(), this.p.getY());
        return gpx;
    }

    @Override
    public void reProject(ICoordTrans ct) {
        this.p = ct.convert(this.p, this.p);
    }

    @Override
    public boolean isEmpty() {
        return this.p == null;
    }
}

