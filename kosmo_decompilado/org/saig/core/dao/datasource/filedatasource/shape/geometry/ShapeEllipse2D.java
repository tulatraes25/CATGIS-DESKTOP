/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.core.AbstractHandler
 *  com.iver.cit.gvsig.fmap.core.Handler
 *  com.iver.cit.gvsig.fmap.edition.UtilFunctions
 */
package org.saig.core.dao.datasource.filedatasource.shape.geometry;

import com.iver.cit.gvsig.fmap.core.AbstractHandler;
import com.iver.cit.gvsig.fmap.core.Handler;
import com.iver.cit.gvsig.fmap.edition.UtilFunctions;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShape;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.SAIGGeneralPath;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolygon2D;

public class ShapeEllipse2D
extends ShapePolygon2D {
    private static final long serialVersionUID = 1L;
    private Point2D init;
    private Point2D end;
    private double ydist;

    public ShapeEllipse2D(SAIGGeneralPath gpx, Point2D i, Point2D e, double d) {
        super(gpx);
        this.init = i;
        this.end = e;
        this.ydist = d;
    }

    public Point2D getInit() {
        return this.init;
    }

    public Point2D getEnd() {
        return this.end;
    }

    public double getDist() {
        return this.ydist;
    }

    @Override
    public IShape cloneShape() {
        return new ShapeEllipse2D((SAIGGeneralPath)this.gp.clone(), this.init, this.end, this.ydist);
    }

    @Override
    public int getShapeType() {
        return 256;
    }

    @Override
    public void transform(AffineTransform at) {
        Point2D.Double center = new Point2D.Double((this.init.getX() + this.end.getX()) / 2.0, (this.init.getY() + this.end.getY()) / 2.0);
        Point2D pdist = UtilFunctions.getPerpendicularPoint((Point2D)this.init, (Point2D)this.end, (Point2D)center, (double)this.ydist);
        Point2D.Double aux1 = new Point2D.Double();
        at.transform(this.init, aux1);
        this.init = aux1;
        Point2D.Double aux2 = new Point2D.Double();
        at.transform(this.end, aux2);
        this.end = aux2;
        center = new Point2D.Double((this.init.getX() + this.end.getX()) / 2.0, (this.init.getY() + this.end.getY()) / 2.0);
        Point2D.Double aux3 = new Point2D.Double();
        at.transform(pdist, aux3);
        this.ydist = center.distance(aux3);
        this.gp.transform(at);
    }

    public Handler[] getStretchingHandlers() {
        ArrayList<CenterHandler> handlers = new ArrayList<CenterHandler>();
        Rectangle2D rect = this.getBounds2D();
        handlers.add(new CenterHandler(0, rect.getCenterX(), rect.getCenterY()));
        return handlers.toArray(new Handler[0]);
    }

    public Handler[] getSelectHandlers() {
        ArrayList<AbstractHandler> handlers = new ArrayList<AbstractHandler>();
        Rectangle2D rect = this.getBounds2D();
        handlers.add(new CenterSelHandler(0, rect.getCenterX(), rect.getCenterY()));
        handlers.add(new InitSelHandler(1, this.init.getX(), this.init.getY()));
        handlers.add(new EndSelHandler(2, this.end.getX(), this.end.getY()));
        Point2D.Double mediop = new Point2D.Double((this.end.getX() + this.init.getX()) / 2.0, (this.end.getY() + this.init.getY()) / 2.0);
        Point2D[] p = UtilFunctions.getPerpendicular((Point2D)this.init, (Point2D)this.end, (Point2D)mediop);
        Point2D u = UtilFunctions.getPoint((Point2D)mediop, (Point2D)p[1], (double)this.ydist);
        Point2D d = UtilFunctions.getPoint((Point2D)mediop, (Point2D)p[1], (double)(-this.ydist));
        handlers.add(new RadioSelYHandler(3, u.getX(), u.getY()));
        handlers.add(new RadioSelYHandler(4, d.getX(), d.getY()));
        return handlers.toArray(new Handler[0]);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.gp.intersects(r);
    }

    class CenterHandler
    extends AbstractHandler {
        public CenterHandler(int i, double x, double y) {
            this.point = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
            int i = 0;
            while (i < ShapeEllipse2D.this.gp.numCoords / 2) {
                int n = i * 2;
                ShapeEllipse2D.this.gp.pointCoords[n] = ShapeEllipse2D.this.gp.pointCoords[n] + x;
                int n2 = i * 2 + 1;
                ShapeEllipse2D.this.gp.pointCoords[n2] = ShapeEllipse2D.this.gp.pointCoords[n2] + y;
                ++i;
            }
            ShapeEllipse2D.this.init = new Point2D.Double(ShapeEllipse2D.this.init.getX() + x, ShapeEllipse2D.this.init.getY() + y);
            ShapeEllipse2D.this.end = new Point2D.Double(ShapeEllipse2D.this.end.getX() + x, ShapeEllipse2D.this.end.getY() + y);
        }

        public void set(double x, double y) {
        }
    }

    class CenterSelHandler
    extends AbstractHandler {
        public CenterSelHandler(int i, double x, double y) {
            this.point = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
            int i = 0;
            while (i < ShapeEllipse2D.this.gp.numCoords / 2) {
                int n = i * 2;
                ShapeEllipse2D.this.gp.pointCoords[n] = ShapeEllipse2D.this.gp.pointCoords[n] + x;
                int n2 = i * 2 + 1;
                ShapeEllipse2D.this.gp.pointCoords[n2] = ShapeEllipse2D.this.gp.pointCoords[n2] + y;
                ++i;
            }
        }

        public void set(double x, double y) {
            Point2D.Double center = new Point2D.Double((ShapeEllipse2D.this.init.getX() + ShapeEllipse2D.this.end.getX()) / 2.0, (ShapeEllipse2D.this.init.getY() + ShapeEllipse2D.this.end.getY()) / 2.0);
            double dx = x - ((Point2D)center).getX();
            double dy = y - ((Point2D)center).getY();
            int i = 0;
            while (i < ShapeEllipse2D.this.gp.numCoords / 2) {
                int n = i * 2;
                ShapeEllipse2D.this.gp.pointCoords[n] = ShapeEllipse2D.this.gp.pointCoords[n] + dx;
                int n2 = i * 2 + 1;
                ShapeEllipse2D.this.gp.pointCoords[n2] = ShapeEllipse2D.this.gp.pointCoords[n2] + dy;
                ++i;
            }
            ShapeEllipse2D.this.init = new Point2D.Double(ShapeEllipse2D.this.init.getX() + dx, ShapeEllipse2D.this.init.getY() + dy);
            ShapeEllipse2D.this.end = new Point2D.Double(ShapeEllipse2D.this.end.getX() + dx, ShapeEllipse2D.this.end.getY() + dy);
        }
    }

    class EndSelHandler
    extends AbstractHandler {
        public EndSelHandler(int i, double x, double y) {
            this.point = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
        }

        public void set(double x, double y) {
            Point2D.Double center = new Point2D.Double((ShapeEllipse2D.this.init.getX() + ShapeEllipse2D.this.end.getX()) / 2.0, (ShapeEllipse2D.this.init.getY() + ShapeEllipse2D.this.end.getY()) / 2.0);
            double xdist = 2.0 * center.distance(x, y);
            ShapeEllipse2D.this.end = UtilFunctions.getPoint((Point2D)center, (Point2D)ShapeEllipse2D.this.end, (double)center.distance(x, y));
            ShapeEllipse2D.this.init = UtilFunctions.getPoint((Point2D)ShapeEllipse2D.this.end, (Point2D)center, (double)xdist);
            Arc2D.Double arc = new Arc2D.Double(ShapeEllipse2D.this.init.getX(), ShapeEllipse2D.this.init.getY() - ShapeEllipse2D.this.ydist, xdist, 2.0 * ShapeEllipse2D.this.ydist, 0.0, 360.0, 0);
            double angle = UtilFunctions.getAngle((Point2D)ShapeEllipse2D.this.init, (Point2D)ShapeEllipse2D.this.end);
            AffineTransform mT = AffineTransform.getRotateInstance(angle, ShapeEllipse2D.this.init.getX(), ShapeEllipse2D.this.init.getY());
            ShapeEllipse2D.this.gp = new SAIGGeneralPath(arc);
            ShapeEllipse2D.this.gp.transform(mT);
        }
    }

    class InitSelHandler
    extends AbstractHandler {
        public InitSelHandler(int i, double x, double y) {
            this.point = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
        }

        public void set(double x, double y) {
            Point2D.Double center = new Point2D.Double((ShapeEllipse2D.this.init.getX() + ShapeEllipse2D.this.end.getX()) / 2.0, (ShapeEllipse2D.this.init.getY() + ShapeEllipse2D.this.end.getY()) / 2.0);
            double xdist = 2.0 * center.distance(x, y);
            ShapeEllipse2D.this.init = UtilFunctions.getPoint((Point2D)center, (Point2D)ShapeEllipse2D.this.init, (double)center.distance(x, y));
            ShapeEllipse2D.this.end = UtilFunctions.getPoint((Point2D)ShapeEllipse2D.this.init, (Point2D)center, (double)xdist);
            Arc2D.Double arc = new Arc2D.Double(ShapeEllipse2D.this.init.getX(), ShapeEllipse2D.this.init.getY() - ShapeEllipse2D.this.ydist, xdist, 2.0 * ShapeEllipse2D.this.ydist, 0.0, 360.0, 0);
            double angle = UtilFunctions.getAngle((Point2D)ShapeEllipse2D.this.init, (Point2D)ShapeEllipse2D.this.end);
            AffineTransform mT = AffineTransform.getRotateInstance(angle, ShapeEllipse2D.this.init.getX(), ShapeEllipse2D.this.init.getY());
            ShapeEllipse2D.this.gp = new SAIGGeneralPath(arc);
            ShapeEllipse2D.this.gp.transform(mT);
        }
    }

    class RadioSelYHandler
    extends AbstractHandler {
        public RadioSelYHandler(int i, double x, double y) {
            this.point = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
        }

        public void set(double x, double y) {
            ShapeEllipse2D.this.ydist = new Point2D.Double((ShapeEllipse2D.this.init.getX() + ShapeEllipse2D.this.end.getX()) / 2.0, (ShapeEllipse2D.this.init.getY() + ShapeEllipse2D.this.end.getY()) / 2.0).distance(x, y);
            double xdist = ShapeEllipse2D.this.init.distance(ShapeEllipse2D.this.end);
            Arc2D.Double arc = new Arc2D.Double(ShapeEllipse2D.this.init.getX(), ShapeEllipse2D.this.init.getY() - ShapeEllipse2D.this.ydist, xdist, 2.0 * ShapeEllipse2D.this.ydist, 0.0, 360.0, 0);
            double angle = UtilFunctions.getAngle((Point2D)ShapeEllipse2D.this.init, (Point2D)ShapeEllipse2D.this.end);
            AffineTransform mT = AffineTransform.getRotateInstance(angle, ShapeEllipse2D.this.init.getX(), ShapeEllipse2D.this.init.getY());
            ShapeEllipse2D.this.gp = new SAIGGeneralPath(arc);
            ShapeEllipse2D.this.gp.transform(mT);
        }
    }
}

