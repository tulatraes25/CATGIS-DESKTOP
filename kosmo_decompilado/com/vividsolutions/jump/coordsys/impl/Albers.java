/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Projection;

public class Albers
extends Projection {
    double L0;
    double k0;
    double phi1;
    double phi2;
    double phi0;
    double X0;
    double Y0;
    double A_n;
    double A_C;
    double A_p0;
    Geographic q = new Geographic();

    public void setParameters(double centralMeridian, double firstStandardParallel, double secondStandardParallel, double latitudeOfProjection, double falseEasting, double falseNorthing) {
        this.L0 = centralMeridian * Math.PI / 180.0;
        this.phi1 = firstStandardParallel * Math.PI / 180.0;
        this.phi2 = secondStandardParallel * Math.PI / 180.0;
        this.phi0 = latitudeOfProjection * Math.PI / 180.0;
        this.X0 = falseEasting;
        this.Y0 = falseNorthing;
        double m1 = this.albersM(this.phi1);
        double m2 = this.albersM(this.phi2);
        double q1 = this.albersQ(this.phi1);
        double q2 = this.albersQ(this.phi2);
        double q0 = this.albersQ(this.phi0);
        this.A_n = (m1 * m1 - m2 * m2) / (q2 - q1);
        this.A_C = m1 * m1 + this.A_n * q1;
        double a = this.currentSpheroid.getA();
        this.A_p0 = a * Math.sqrt(this.A_C - this.A_n * q0) / this.A_n;
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
        double que = this.albersQ(q.lat);
        double theta = this.A_n * (q.lon - this.L0);
        double pee = a * Math.sqrt(this.A_C - this.A_n * que) / this.A_n;
        p.x = pee * Math.sin(theta) + this.X0;
        p.y = this.A_p0 - pee * Math.cos(theta) + this.Y0;
    }

    public void inverse(Planar p, Geographic q) {
        double a = this.currentSpheroid.getA();
        double e = this.currentSpheroid.getE();
        double es = e * e;
        double x = p.x - this.X0;
        double y = p.y - this.Y0;
        double theta = Math.atan2(x, this.A_p0 - y);
        double pee = Math.sqrt(x * x + Math.pow(this.A_p0 - y, 2.0));
        double que = (this.A_C - pee * pee * this.A_n * this.A_n / (a * a)) / this.A_n;
        q.lon = this.L0 + theta / this.A_n;
        double li = Math.asin(que / 2.0);
        double delta = 1.0E11;
        do {
            double j1 = Math.pow(1.0 - es * Math.pow(Math.sin(li), 2.0), 2.0) / (2.0 * Math.cos(li));
            double k1 = que / (1.0 - es);
            double k2 = Math.sin(li) / (1.0 - es * Math.pow(Math.sin(li), 2.0));
            double k3 = 1.0 / (2.0 * e) * Math.log((1.0 - e * Math.sin(li)) / (1.0 + e * Math.sin(li)));
            double lip1 = li + j1 * (k1 - k2 + k3);
            delta = Math.abs(lip1 - li);
            li = lip1;
        } while (delta > 1.0E-12);
        q.lat = li;
    }

    double albersQ(double lat) {
        double e = this.currentSpheroid.getE();
        double q = (1.0 - e * e) * (Math.sin(lat) / (1.0 - e * e * Math.pow(Math.sin(lat), 2.0)) - 1.0 / (2.0 * e) * Math.log((1.0 - e * Math.sin(lat)) / (1.0 + e * Math.sin(lat))));
        return q;
    }

    double albersM(double lat) {
        double e = this.currentSpheroid.getE();
        double m = Math.cos(lat) / Math.sqrt(1.0 - e * e * Math.pow(Math.sin(lat), 2.0));
        return m;
    }
}

