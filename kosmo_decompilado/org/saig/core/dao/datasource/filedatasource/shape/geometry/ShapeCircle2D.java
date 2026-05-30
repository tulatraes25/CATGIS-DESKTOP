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

public class ShapeCircle2D
extends ShapePolygon2D {
    private static final long serialVersionUID = 1L;
    private Point2D center;
    private double radio;

    public ShapeCircle2D(SAIGGeneralPath gpx, Point2D c, double r) {
        super(gpx);
        this.center = c;
        this.radio = r;
    }

    public Point2D getCenter() {
        return this.center;
    }

    public double getRadio() {
        return this.radio;
    }

    @Override
    public IShape cloneShape() {
        return new ShapeCircle2D((SAIGGeneralPath)this.gp.clone(), this.center, this.radio);
    }

    @Override
    public int getShapeType() {
        return 64;
    }

    @Override
    public void transform(AffineTransform at) {
        Point2D pdist = UtilFunctions.getPerpendicularPoint((Point2D)new Point2D.Double(this.center.getX() + 10.0, this.center.getY()), (Point2D)new Point2D.Double(this.center.getX() - 10.0, this.center.getY()), (Point2D)this.center, (double)this.radio);
        Point2D.Double aux = new Point2D.Double();
        at.transform(this.center, aux);
        this.center = aux;
        Point2D.Double aux3 = new Point2D.Double();
        at.transform(pdist, aux3);
        this.radio = this.center.distance(aux3);
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
        handlers.add(new CenterSelHandler(0, this.center.getX(), this.center.getY()));
        handlers.add(new RadioSelHandler(1, this.center.getX() - this.radio, this.center.getY()));
        handlers.add(new RadioSelHandler(2, this.center.getX() + this.radio, this.center.getY()));
        handlers.add(new RadioSelHandler(3, this.center.getX(), this.center.getY() - this.radio));
        handlers.add(new RadioSelHandler(3, this.center.getX(), this.center.getY() + this.radio));
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
            ShapeCircle2D.this.center = new Point2D.Double(ShapeCircle2D.this.center.getX() + x, ShapeCircle2D.this.center.getY() + y);
            int i = 0;
            while (i < ShapeCircle2D.this.gp.numCoords / 2) {
                int n = i * 2;
                ShapeCircle2D.this.gp.pointCoords[n] = ShapeCircle2D.this.gp.pointCoords[n] + x;
                int n2 = i * 2 + 1;
                ShapeCircle2D.this.gp.pointCoords[n2] = ShapeCircle2D.this.gp.pointCoords[n2] + y;
                ++i;
            }
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
            while (i < ShapeCircle2D.this.gp.numCoords / 2) {
                int n = i * 2;
                ShapeCircle2D.this.gp.pointCoords[n] = ShapeCircle2D.this.gp.pointCoords[n] + x;
                int n2 = i * 2 + 1;
                ShapeCircle2D.this.gp.pointCoords[n2] = ShapeCircle2D.this.gp.pointCoords[n2] + y;
                ++i;
            }
        }

        public void set(double x, double y) {
            ShapeCircle2D.this.center = new Point2D.Double(x, y);
            Arc2D.Double arc = new Arc2D.Double(ShapeCircle2D.this.center.getX() - ShapeCircle2D.this.radio, ShapeCircle2D.this.center.getY() - ShapeCircle2D.this.radio, 2.0 * ShapeCircle2D.this.radio, 2.0 * ShapeCircle2D.this.radio, 0.0, 360.0, 0);
            ShapeCircle2D.this.gp = new SAIGGeneralPath(arc);
        }
    }

    class RadioSelHandler
    extends AbstractHandler {
        public RadioSelHandler(int i, double x, double y) {
            this.point = new Point2D.Double(x, y);
            this.index = i;
        }

        public void move(double x, double y) {
        }

        public void set(double x, double y) {
            ShapeCircle2D.this.radio = ShapeCircle2D.this.center.distance(x, y);
            Arc2D.Double arc = new Arc2D.Double(ShapeCircle2D.this.center.getX() - ShapeCircle2D.this.radio, ShapeCircle2D.this.center.getY() - ShapeCircle2D.this.radio, 2.0 * ShapeCircle2D.this.radio, 2.0 * ShapeCircle2D.this.radio, 0.0, 360.0, 0);
            ShapeCircle2D.this.gp = new SAIGGeneralPath(arc);
        }
    }
}

