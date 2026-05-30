/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Projection;
import com.vividsolutions.jump.coordsys.impl.MeridianArcLength;

public class TransverseMercator
extends Projection {
    double L0;
    double k0;
    private MeridianArcLength S = new MeridianArcLength();

    public void setParameters(double centralMeridian) {
        this.L0 = centralMeridian / 180.0 * Math.PI;
    }

    @Override
    public Geographic asGeographic(Planar p, Geographic q) {
        this.planarToGeographicInRadians(p, q);
        q.lat = q.lat * 180.0 / Math.PI;
        q.lon = q.lon * 180.0 / Math.PI;
        return q;
    }

    @Override
    public Planar asPlanar(Geographic q0, Planar p) {
        Geographic q = new Geographic();
        q.lat = q0.lat / 180.0 * Math.PI;
        q.lon = q0.lon / 180.0 * Math.PI;
        this.geographicInRadiansToPlanar(q, p);
        return p;
    }

    void planarToGeographicInRadians(Planar p, Geographic q) {
        double XdN1;
        double L1 = this.footPointLatitude(p.y);
        double a = this.currentSpheroid.getA();
        double b = this.currentSpheroid.getB();
        double ep2 = (a * a - b * b) / (b * b);
        double N1 = this.currentSpheroid.primeVerticalRadiusOfCurvature(L1);
        double M1 = this.currentSpheroid.meridianRadiusOfCurvature(L1);
        double n12 = ep2 * Math.pow(Math.cos(L1), 2.0);
        double n1 = Math.sqrt(n12);
        double n14 = n12 * n12;
        double n16 = n14 * n12;
        double n18 = n14 * n14;
        double t1 = Math.tan(L1);
        double t12 = t1 * t1;
        double t14 = t12 * t12;
        double t16 = t14 * t12;
        double u0 = t1 * Math.pow(p.x, 2.0) / (2.0 * M1 * N1);
        double u1 = t1 * Math.pow(p.x, 4.0) / (24.0 * M1 * Math.pow(N1, 3.0));
        double u2 = t1 * Math.pow(p.x, 6.0) / (720.0 * M1 * Math.pow(N1, 5.0));
        double u3 = t1 * Math.pow(p.x, 8.0) / (40320.0 * M1 * Math.pow(N1, 7.0));
        double v1 = 5.0 + 3.0 * t12 + n12 - 4.0 * n14 - 9.0 * n12 * t12;
        double v2 = 61.0 - 90.0 * t12 + 46.0 * n12 + 45.0 * t14 - 252.0 * t12 * n12 - 3.0 * n14 + 100.0 * n16 - 66.0 * t12 * n14 - 90.0 * t14 * n12 + 88.0 * n18 + 225.0 * t14 * n14 + 84.0 * t12 * n16 - 192.0 * t12 * n18;
        double v3 = 1385.0 + 3633.0 * t12 + 4095.0 * t14 + 1575.0 * t16;
        q.lat = L1 - u0 + u1 * v1 - u2 * v2 + u3 * v3;
        u0 = XdN1 = p.x / N1;
        u1 = Math.pow(XdN1, 3.0) / 6.0;
        u2 = Math.pow(XdN1, 5.0) / 120.0;
        u3 = Math.pow(XdN1, 7.0) / 5040.0;
        v1 = 1.0 + 2.0 * t12 + n12;
        v2 = 5.0 + 6.0 * n12 + 28.0 * t12 - 3.0 * n14 + 8.0 * t12 * n12 + 24.0 * t14 - 4.0 * n16 + 4.0 * t12 * n14 + 24.0 * t12 * n16;
        v3 = 61.0 + 662.0 * t12 + 1320.0 * t14 + 720.0 * t16;
        q.lon = 1.0 / Math.cos(L1) * (u0 - u1 * v1 + u2 * v2 - u3 * v3) + this.L0;
    }

    void geographicInRadiansToPlanar(Geographic q, Planar p) {
        double a = this.currentSpheroid.getA();
        double b = this.currentSpheroid.getB();
        double ep2 = (a * a - b * b) / (b * b);
        double N = this.currentSpheroid.primeVerticalRadiusOfCurvature(q.lat);
        double n2 = ep2 * Math.pow(Math.cos(q.lat), 2.0);
        double n = Math.sqrt(n2);
        double n4 = n2 * n2;
        double n6 = n4 * n2;
        double n8 = n4 * n4;
        double t = Math.tan(q.lat);
        double t2 = t * t;
        double t4 = t2 * t2;
        double t6 = t4 * t2;
        this.S.compute(this.currentSpheroid, q.lat, 0);
        double cosLat = Math.cos(q.lat);
        double sinLat = Math.sin(q.lat);
        double L = q.lon - this.L0;
        double L2 = L * L;
        double L3 = L2 * L;
        double L4 = L2 * L2;
        double L5 = L4 * L;
        double L6 = L4 * L2;
        double L7 = L5 * L2;
        double L8 = L4 * L4;
        double u0 = L * cosLat;
        double u1 = L3 * Math.pow(cosLat, 3.0) / 6.0;
        double u2 = L5 * Math.pow(cosLat, 5.0) / 120.0;
        double u3 = L7 * Math.pow(cosLat, 7.0) / 5040.0;
        double v1 = 1.0 - t2 + n2;
        double v2 = 5.0 - 18.0 * t2 + t4 + 14.0 * n2 - 58.0 * t2 * n2 + 13.0 * n4 + 4.0 * n6 - 64.0 * n4 * t2 - 24.0 * n6 * t2;
        double v3 = 61.0 - 479.0 * t2 + 179.0 * t4 - t6;
        p.x = u0 + u1 * v1 + u2 * v2 + u3 * v3;
        u0 = L2 / 2.0 * sinLat * cosLat;
        u1 = L4 / 24.0 * sinLat * Math.pow(cosLat, 3.0);
        u2 = L6 / 720.0 * sinLat * Math.pow(cosLat, 5.0);
        u3 = L8 / 40320.0 * sinLat * Math.pow(cosLat, 7.0);
        v1 = 5.0 - t2 + 9.0 * n2 + 4.0 * n4;
        v2 = 61.0 - 58.0 * t2 + t4 + 270.0 * n2 - 330.0 * t2 * n2 + 445.0 * n4 + 324.0 * n6 - 680.0 * n4 * t2 + 88.0 * n8 - 600.0 * n6 * t2 - 192.0 * n8 * t2;
        v3 = 1385.0 - 311.0 * t2 + 543.0 * t4 - t6;
        p.y = this.S.s / N + u0 + u1 * v1 + u2 * v2 + u3 * v3;
        p.x = N * p.x;
        p.y = N * p.y;
    }

    private double footPointLatitude(double y) {
        double dflat;
        double flat;
        double Lat1;
        double a = this.currentSpheroid.getA();
        double newlat = y / a;
        int i = 0;
        do {
            Lat1 = newlat;
            if (++i == 100) break;
            this.S.compute(this.currentSpheroid, Lat1, 0);
        } while (Math.abs((newlat = Lat1 - (flat = this.S.s - y) / (dflat = a * (this.S.a0 - 2.0 * this.S.a2 * Math.cos(2.0 * Lat1) + 4.0 * this.S.a4 * Math.cos(4.0 * Lat1) - 6.0 * this.S.a6 * Math.cos(6.0 * Lat1) + 8.0 * this.S.a8 * Math.cos(8.0 * Lat1)))) - Lat1) > 1.0E-15);
        Lat1 = newlat;
        return Lat1;
    }
}

