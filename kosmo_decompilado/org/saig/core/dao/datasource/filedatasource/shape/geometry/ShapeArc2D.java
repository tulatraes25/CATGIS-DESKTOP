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
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapePolyline2D;

public class ShapeArc2D
extends ShapePolyline2D {
    private static final long serialVersionUID = 1L;
    private Point2D init;
    private Point2D center;
    private Point2D end;

    public ShapeArc2D(SAIGGeneralPath gpx, Point2D i, Point2D c, Point2D e) {
        super(gpx);
        this.init = i;
        this.center = c;
        this.end = e;
    }

    public Point2D getInit() {
        return this.init;
    }

    public Point2D getEnd() {
        return this.end;
    }

    public Point2D getCenter() {
        return UtilFunctions.getCenter((Point2D)this.init, (Point2D)this.center, (Point2D)this.end);
    }

    public Point2D getMid() {
        return this.center;
    }

    @Override
    public IShape cloneShape() {
        ShapeArc2D arc = new ShapeArc2D((SAIGGeneralPath)this.gp.clone(), this.init, this.center, this.end);
        return arc;
    }

    @Override
    public void transform(AffineTransform at) {
        this.gp.transform(at);
        InitHandler inithandler = (InitHandler)this.getStretchingHandlers()[0];
        EndHandler endhandler = (EndHandler)this.getStretchingHandlers()[1];
        Point2D.Double aux1 = new Point2D.Double();
        Point2D.Double aux2 = new Point2D.Double();
        Point2D.Double aux3 = new Point2D.Double();
        at.transform(inithandler.getPoint(), aux1);
        inithandler.setPoint(aux1);
        at.transform(endhandler.getPoint(), aux3);
        endhandler.setPoint(aux3);
        CenterSelHandler centerhandler = (CenterSelHandler)this.getSelectHandlers()[1];
        at.transform(centerhandler.getPoint(), aux2);
        centerhandler.setPoint(aux2);
    }

    @Override
    public int getShapeType() {
        return 128;
    }

    public Handler[] getStretchingHandlers() {
        ArrayList<AbstractHandler> handlers = new ArrayList<AbstractHandler>();
        handlers.add(new InitHandler(0, this.init.getX(), this.init.getY()));
        handlers.add(new EndHandler(1, this.end.getX(), this.end.getY()));
        return handlers.toArray(new Handler[0]);
    }

    public Handler[] getSelectHandlers() {
        ArrayList<AbstractHandler> handlers = new ArrayList<AbstractHandler>();
        handlers.add(new InitSelHandler(0, this.init.getX(), this.init.getY()));
        handlers.add(new CenterSelHandler(1, this.center.getX(), this.center.getY()));
        handlers.add(new EndSelHandler(2, this.end.getX(), this.end.getY()));
        return handlers.toArray(new Handler[0]);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.gp.intersects(r);
    }

    class CenterSelHandler
    extends AbstractHandler {
        public CenterSelHandler(int i, double x, double y) {
            ShapeArc2D.this.center = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
        }

        public void setPoint(Point2D p) {
            ShapeArc2D.this.center = p;
        }

        public Point2D getPoint() {
            return ShapeArc2D.this.center;
        }

        public void set(double x, double y) {
            ShapeArc2D.this.center = new Point2D.Double(x, y);
            Arc2D arco = UtilFunctions.createArc((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.center, (Point2D)ShapeArc2D.this.end);
            ShapeArc2D.this.gp = new SAIGGeneralPath(arco);
        }
    }

    class EndHandler
    extends AbstractHandler {
        public EndHandler(int i, double x, double y) {
            ShapeArc2D.this.end = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
            Point2D.Double mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            double dist = mediop.distance(ShapeArc2D.this.center);
            ShapeArc2D.this.end = new Point2D.Double(ShapeArc2D.this.end.getX() + x, ShapeArc2D.this.end.getY() + y);
            mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            Point2D[] perp = UtilFunctions.getPerpendicular((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.end, (Point2D)mediop);
            if (UtilFunctions.getAngle((Point2D)ShapeArc2D.this.end, (Point2D)ShapeArc2D.this.init) <= Math.PI) {
                dist = -dist;
            }
            ShapeArc2D.this.center = UtilFunctions.getPoint((Point2D)mediop, (Point2D)perp[1], (double)dist);
            Arc2D arco = UtilFunctions.createArc((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.center, (Point2D)ShapeArc2D.this.end);
            ShapeArc2D.this.gp = new SAIGGeneralPath(arco);
        }

        public void setPoint(Point2D p) {
            ShapeArc2D.this.end = p;
        }

        public Point2D getPoint() {
            return ShapeArc2D.this.end;
        }

        public void set(double x, double y) {
        }
    }

    class EndSelHandler
    extends AbstractHandler {
        public EndSelHandler(int i, double x, double y) {
            ShapeArc2D.this.end = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
            Point2D.Double mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            double dist = mediop.distance(ShapeArc2D.this.center);
            ShapeArc2D.this.end = new Point2D.Double(ShapeArc2D.this.end.getX() + x, ShapeArc2D.this.end.getY() + y);
            mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            Point2D[] perp = UtilFunctions.getPerpendicular((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.end, (Point2D)mediop);
            if (UtilFunctions.getAngle((Point2D)ShapeArc2D.this.end, (Point2D)ShapeArc2D.this.init) <= Math.PI) {
                dist = -dist;
            }
            ShapeArc2D.this.center = UtilFunctions.getPoint((Point2D)mediop, (Point2D)perp[1], (double)dist);
            Arc2D arco = UtilFunctions.createArc((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.center, (Point2D)ShapeArc2D.this.end);
            ShapeArc2D.this.gp = new SAIGGeneralPath(arco);
        }

        public void setPoint(Point2D p) {
            ShapeArc2D.this.end = p;
        }

        public Point2D getPoint() {
            return ShapeArc2D.this.end;
        }

        public void set(double x, double y) {
            Point2D.Double mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            double dist = mediop.distance(ShapeArc2D.this.center);
            ShapeArc2D.this.end = new Point2D.Double(x, y);
            mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            if (UtilFunctions.getAngle((Point2D)ShapeArc2D.this.end, (Point2D)ShapeArc2D.this.init) <= Math.PI) {
                dist = -dist;
            }
            Arc2D arco = UtilFunctions.createArc((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.center, (Point2D)ShapeArc2D.this.end);
            ShapeArc2D.this.gp = new SAIGGeneralPath(arco);
        }
    }

    class InitHandler
    extends AbstractHandler {
        public InitHandler(int i, double x, double y) {
            ShapeArc2D.this.init = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
            Point2D.Double mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            double dist = mediop.distance(ShapeArc2D.this.center);
            ShapeArc2D.this.init = new Point2D.Double(ShapeArc2D.this.init.getX() + x, ShapeArc2D.this.init.getY() + y);
            mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            Point2D[] perp = UtilFunctions.getPerpendicular((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.end, (Point2D)mediop);
            if (UtilFunctions.getAngle((Point2D)ShapeArc2D.this.end, (Point2D)ShapeArc2D.this.init) <= Math.PI) {
                dist = -dist;
            }
            ShapeArc2D.this.center = UtilFunctions.getPoint((Point2D)mediop, (Point2D)perp[1], (double)dist);
            Arc2D arco = UtilFunctions.createArc((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.center, (Point2D)ShapeArc2D.this.end);
            ShapeArc2D.this.gp = new SAIGGeneralPath(arco);
        }

        public void setPoint(Point2D p) {
            ShapeArc2D.this.init = p;
        }

        public Point2D getPoint() {
            return ShapeArc2D.this.init;
        }

        public void set(double x, double y) {
        }
    }

    class InitSelHandler
    extends AbstractHandler {
        public InitSelHandler(int i, double x, double y) {
            ShapeArc2D.this.init = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
            Point2D.Double mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            double dist = mediop.distance(ShapeArc2D.this.center);
            ShapeArc2D.this.init = new Point2D.Double(ShapeArc2D.this.init.getX() + x, ShapeArc2D.this.init.getY() + y);
            mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            Point2D[] perp = UtilFunctions.getPerpendicular((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.end, (Point2D)mediop);
            if (UtilFunctions.getAngle((Point2D)ShapeArc2D.this.end, (Point2D)ShapeArc2D.this.init) <= Math.PI) {
                dist = -dist;
            }
            ShapeArc2D.this.center = UtilFunctions.getPoint((Point2D)mediop, (Point2D)perp[1], (double)dist);
            Arc2D arco = UtilFunctions.createArc((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.center, (Point2D)ShapeArc2D.this.end);
            ShapeArc2D.this.gp = new SAIGGeneralPath(arco);
        }

        public void setPoint(Point2D p) {
            ShapeArc2D.this.init = p;
        }

        public Point2D getPoint() {
            return ShapeArc2D.this.init;
        }

        public void set(double x, double y) {
            Point2D.Double mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            double dist = mediop.distance(ShapeArc2D.this.center);
            ShapeArc2D.this.init = new Point2D.Double(x, y);
            mediop = new Point2D.Double((ShapeArc2D.this.init.getX() + ShapeArc2D.this.end.getX()) / 2.0, (ShapeArc2D.this.init.getY() + ShapeArc2D.this.end.getY()) / 2.0);
            if (UtilFunctions.getAngle((Point2D)ShapeArc2D.this.end, (Point2D)ShapeArc2D.this.init) <= Math.PI) {
                dist = -dist;
            }
            Arc2D arco = UtilFunctions.createArc((Point2D)ShapeArc2D.this.init, (Point2D)ShapeArc2D.this.center, (Point2D)ShapeArc2D.this.end);
            ShapeArc2D.this.gp = new SAIGGeneralPath(arco);
        }
    }
}

