/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Projection;

public class Mercator
extends Projection {
    double L0;
    double X0;
    double Y0;
    Geographic q = new Geographic();

    public void setParameters(double centralMeridian, double falseEasting, double falseNorthing) {
        this.L0 = centralMeridian / 180.0 * Math.PI;
        this.X0 = falseEasting;
        this.Y0 = falseNorthing;
    }

    @Override
    public Planar asPlanar(Geographic q0, Planar p) {
        this.q.lat = q0.lat / 180.0 * Math.PI;
        this.q.lon = q0.lon / 180.0 * Math.PI;
        this.forward(this.q, p);
        return p;
    }

    @Override
    public Geographic asGeographic(Planar p, Geographic q) {
        this.inverse(p, q);
        q.lat = q.lat * 180.0 / Math.PI;
        q.lon = q.lon * 180.0 / Math.PI;
        return q;
    }

    public void forward(Geographic q, Planar p) {
        double a = this.currentSpheroid.getA();
        double e = this.currentSpheroid.getE();
        p.x = a * (q.lon - this.L0);
        p.y = a / 2.0 * Math.log((1.0 + Math.sin(q.lat)) / (1.0 - Math.sin(q.lat)) * Math.pow((1.0 - e * Math.sin(q.lat)) / (1.0 + e * Math.sin(q.lat)), e));
    }

    public void inverse(Planar p, Geographic q) {
        double a = this.currentSpheroid.getA();
        double e = this.currentSpheroid.getE();
        double t = Math.exp(-p.y / a);
        double phi = 1.5707963267948966 - 2.0 * Math.atan(t);
        double delta = 10000.0;
        do {
            double phiI = 1.5707963267948966 - 2.0 * Math.atan(t * Math.pow((1.0 - e * Math.sin(phi)) / (1.0 + e * Math.sin(phi)), e / 2.0));
            delta = Math.abs(phiI - phi);
            phi = phiI;
        } while (delta > 1.0E-14);
        double lambda = p.x / a + this.L0;
        q.lat = phi;
        q.lon = lambda;
    }
}

