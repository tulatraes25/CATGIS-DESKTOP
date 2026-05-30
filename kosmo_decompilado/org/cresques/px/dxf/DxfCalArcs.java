/*
 * Decompiled with CFR 0.152.
 */
package org.cresques.px.dxf;

import java.awt.geom.Point2D;
import java.util.Vector;

public class DxfCalArcs {
    final boolean debug = true;
    Point2D coord1;
    Point2D coord2;
    Point2D center;
    double radio;
    double empieza;
    double acaba;
    double bulge;
    double d;
    double dd;
    double aci;
    Point2D coordAux;

    public DxfCalArcs(Point2D p1, Point2D p2, double bulge) {
        this.bulge = bulge;
        if (bulge < 0.0) {
            this.coord1 = p2;
            this.coord2 = p1;
        } else {
            this.coord1 = p1;
            this.coord2 = p2;
        }
        this.calculate();
    }

    DxfCalArcs calculate() {
        this.d = Math.sqrt((this.coord2.getX() - this.coord1.getX()) * (this.coord2.getX() - this.coord1.getX()) + (this.coord2.getY() - this.coord1.getY()) * (this.coord2.getY() - this.coord1.getY()));
        this.coordAux = new Point2D.Double((this.coord1.getX() + this.coord2.getX()) / 2.0, (this.coord1.getY() + this.coord2.getY()) / 2.0);
        double b = Math.abs(this.bulge);
        double beta = Math.atan(b);
        double alfa = beta * 4.0;
        double landa = alfa / 2.0;
        this.dd = this.d / 2.0 / Math.tan(landa);
        this.radio = this.d / 2.0 / Math.sin(landa);
        this.aci = Math.atan((this.coord2.getX() - this.coord1.getX()) / (this.coord2.getY() - this.coord1.getY()));
        double aciDegree = this.aci * 180.0 / Math.PI;
        if (this.coord2.getY() > this.coord1.getY()) {
            this.aci += Math.PI;
            aciDegree = this.aci * 180.0 / Math.PI;
        }
        this.center = new Point2D.Double(this.coordAux.getX() + this.dd * Math.sin(this.aci + 1.5707963267948966), this.coordAux.getY() + this.dd * Math.cos(this.aci + 1.5707963267948966));
        this.calculateEA(alfa);
        return this;
    }

    void calculateEA(double alfa) {
        this.empieza = Math.atan2(this.coord1.getY() - this.center.getY(), this.coord1.getX() - this.center.getX());
        this.acaba = this.empieza + alfa;
        this.empieza = this.empieza * 180.0 / Math.PI;
        this.acaba = this.acaba * 180.0 / Math.PI;
    }

    public Vector<Point2D> getPoints(double inc) {
        Vector<Point2D> arc = new Vector<Point2D>();
        int iempieza = (int)this.empieza + 1;
        int iacaba = (int)this.acaba;
        if (this.empieza <= this.acaba) {
            this.addNode(arc, this.empieza);
            double angulo = iempieza;
            while (angulo <= (double)iacaba) {
                this.addNode(arc, angulo);
                angulo += inc;
            }
            this.addNode(arc, this.acaba);
        } else {
            this.addNode(arc, this.empieza);
            double angulo = iempieza;
            while (angulo <= 360.0) {
                this.addNode(arc, angulo);
                angulo += inc;
            }
            angulo = 1.0;
            while (angulo <= (double)iacaba) {
                this.addNode(arc, angulo);
                angulo += inc;
            }
            this.addNode(arc, angulo);
        }
        Point2D aux = arc.get(arc.size() - 1);
        double aux1 = Math.abs(aux.getX() - this.coord2.getX());
        double aux2 = Math.abs(aux.getY() - this.coord2.getY());
        return arc;
    }

    public Vector<Point2D> getCentralPoint() {
        Vector<Point2D> arc = new Vector<Point2D>();
        if (this.empieza <= this.acaba) {
            this.addNode(arc, (this.empieza + this.acaba) / 2.0);
        } else {
            this.addNode(arc, this.empieza);
            double alfa = 360.0 - this.empieza;
            double beta = this.acaba;
            double an = alfa + beta;
            double mid = an / 2.0;
            if (mid <= alfa) {
                this.addNode(arc, this.empieza + mid);
            } else {
                this.addNode(arc, mid - alfa);
            }
        }
        return arc;
    }

    private void addNode(Vector<Point2D> arc, double angulo) {
        double yy = this.center.getY() + this.radio * Math.sin(angulo * Math.PI / 180.0);
        double xx = this.center.getX() + this.radio * Math.cos(angulo * Math.PI / 180.0);
        arc.add(new Point2D.Double(xx, yy));
    }
}

