/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Projection;
import com.vividsolutions.jump.coordsys.impl.MeridianArcLength;

public class Polyconic
extends Projection {
    double L0;
    double k0;
    double phi1;
    double phi2;
    double phi0;
    double X0;
    double Y0;
    int zone;
    MeridianArcLength S = new MeridianArcLength();
    Geographic q = new Geographic();

    public void setParameters(double originLatitude, double originLongitude) {
        this.L0 = originLongitude / 180.0 * Math.PI;
        this.phi0 = originLatitude / 180.0 * Math.PI;
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
        this.S.compute(this.currentSpheroid, q.lat, 0);
        double M = this.S.s;
        this.S.compute(this.currentSpheroid, this.phi0, 0);
        double M0 = this.S.s;
        double a = this.currentSpheroid.a;
        double e = this.currentSpheroid.e;
        double e2 = e * e;
        double t = Math.sin(q.lat);
        double N = a / Math.sqrt(1.0 - e2 * t * t);
        double E = (q.lon - this.L0) * Math.sin(q.lat);
        t = 1.0 / Math.tan(q.lat);
        p.x = N * t * Math.sin(E);
        p.y = M - M0 + N * t * (1.0 - Math.cos(E));
    }

    public void inverse(Planar p, Geographic q) {
        double C;
        double phiN;
        double a = this.currentSpheroid.getA();
        double e = this.currentSpheroid.getE();
        double es = e * e;
        this.S.compute(this.currentSpheroid, this.phi0, 0);
        double M0 = this.S.s;
        double A = (M0 + p.y) / a;
        double B = p.x * p.x / (a * a) + A * A;
        q.lat = A;
        int count = 0;
        do {
            phiN = q.lat;
            C = Math.sqrt(1.0 - es * Math.sin(phiN) * Math.sin(phiN)) * Math.tan(phiN);
            this.S.compute(this.currentSpheroid, phiN, 0);
            double M = this.S.s;
            double Ma = M / a;
            double Ma2 = Ma * Ma;
            this.S.compute(this.currentSpheroid, phiN, 1);
            double Mp = this.S.s;
            double s2p = Math.sin(2.0 * phiN);
            q.lat -= (A * (C * Ma + 1.0) - Ma - 0.5 * (Ma2 + B) * C) / (es * s2p * (Ma2 + B - 2.0 * A * Ma) / 4.0 * C + (A - Ma) * (C * Mp - 2.0 / s2p) - Mp);
        } while (Math.abs(q.lat - phiN) > 1.0E-6 && count++ < 100);
        q.lon = Math.asin(p.x * C / a) / Math.sin(q.lat) + this.L0;
    }
}

