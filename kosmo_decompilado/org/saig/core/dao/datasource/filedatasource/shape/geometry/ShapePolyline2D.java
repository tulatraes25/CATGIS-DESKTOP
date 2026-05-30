/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;

public class ShapePolyline2D
implements IShape {
    private static final Logger LOGGER = Logger.getLogger(ShapePolyline2D.class);
    private static final long serialVersionUID = 1L;
    protected SAIGGeneralPath gp;

    public ShapePolyline2D(SAIGGeneralPath gpx) {
        this.gp = gpx;
    }

    @Override
    public boolean contains(double x, double y) {
        return this.gp.contains(x, y);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return this.gp.contains(x, y, w, h);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return this.gp.intersects(x, y, w, h);
    }

    @Override
    public Rectangle getBounds() {
        return this.gp.getBounds();
    }

    @Override
    public boolean contains(Point2D p) {
        return this.gp.contains(p);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return this.gp.getBounds2D();
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.gp.contains(r);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        boolean bool = false;
        if (this.gp.intersects(r)) {
            double[] theData = new double[6];
            Point2D.Double p1 = new Point2D.Double(r.getMinX(), r.getMinY());
            Point2D.Double p2 = new Point2D.Double(r.getMinX(), r.getMaxY());
            Point2D.Double p3 = new Point2D.Double(r.getMaxX(), r.getMaxY());
            Point2D.Double p4 = new Point2D.Double(r.getMaxX(), r.getMinY());
            Line2D.Double l1 = new Line2D.Double(p1, p2);
            Line2D.Double l2 = new Line2D.Double(p2, p3);
            Line2D.Double l3 = new Line2D.Double(p3, p4);
            Line2D.Double l4 = new Line2D.Double(p4, p1);
            PathIterator theIterator = this.getPathIterator(null);
            ArrayList<Point2D.Double> arrayCoords = new ArrayList<Point2D.Double>();
            while (!theIterator.isDone()) {
                int theType = theIterator.currentSegment(theData);
                if (theType == 0) {
                    arrayCoords.add(new Point2D.Double(theData[0], theData[1]));
                } else if (theType == 1) {
                    arrayCoords.add(new Point2D.Double(theData[0], theData[1]));
                    Point2D pAnt = (Point2D)arrayCoords.get(arrayCoords.size() - 2);
                    Line2D.Double l = new Line2D.Double(pAnt.getX(), pAnt.getY(), theData[0], theData[1]);
                    if (l.intersectsLine(((Line2D)l1).getX1(), ((Line2D)l1).getY1(), ((Line2D)l1).getX2(), ((Line2D)l1).getY2()) || l.intersectsLine(((Line2D)l2).getX1(), ((Line2D)l2).getY1(), ((Line2D)l2).getX2(), ((Line2D)l2).getY2()) || l.intersectsLine(((Line2D)l3).getX1(), ((Line2D)l3).getY1(), ((Line2D)l3).getX2(), ((Line2D)l3).getY2()) || l.intersectsLine(((Line2D)l4).getX1(), ((Line2D)l4).getY1(), ((Line2D)l4).getX2(), ((Line2D)l4).getY2()) || r.intersectsLine(l)) {
                        bool = true;
                    }
                } else {
                    LOGGER.error((Object)"Not supported here");
                }
                theIterator.next();
            }
        }
        return bool;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return this.gp.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return this.gp.getPathIterator(at, flatness);
    }

    public void transform(AffineTransform at) {
        this.gp.transform(at);
    }

    @Override
    public int getShapeType() {
        return 2;
    }

    @Override
    public IShape cloneShape() {
        return new ShapePolyline2D((SAIGGeneralPath)this.gp.clone());
    }

    @Override
    public SAIGGeneralPath getGeneralPath() {
        return this.gp;
    }

    @Override
    public void reProject(ICoordTrans ct) {
        this.gp.reProject(ct);
    }

    @Override
    public boolean isEmpty() {
        return this.gp == null || this.gp.numCoords == 0;
    }
}

