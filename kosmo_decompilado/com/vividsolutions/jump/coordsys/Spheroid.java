/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Radius;

public class Spheroid {
    public double a;
    public double b;
    public double f;
    public double e;
    double es;
    double t1;
    double t2;
    double t3;
    double t4;
    double t5;
    double t6;

    public Spheroid(Radius rad) {
        this.a = rad.a;
        if (rad.b > 1.0) {
            this.b = rad.b;
            this.f = 1.0 - this.b / this.a;
        } else {
            this.f = 1.0 / rad.rf;
            this.b = this.a - this.a * this.f;
        }
        this.es = this.f + this.f - this.f * this.f;
        this.e = Math.sqrt(this.es);
        double t0 = this.a * (1.0 - this.es);
        double e4 = this.es * this.es;
        double e6 = e4 * this.es;
        double e8 = e6 * this.es;
        double e10 = e8 * this.es;
        this.t1 = t0 * (1.0 + 3.0 * this.es / 4.0 + 45.0 * e4 / 64.0 + 175.0 * e6 / 256.0 + 11025.0 * e8 / 16384.0 + 43659.0 * e10 / 65536.0);
        this.t2 = t0 * (3.0 * this.es / 4.0 + 15.0 * e4 / 16.0 + 525.0 * e6 / 512.0 + 2205.0 * e8 / 2048.0 + 72765.0 * e10 / 65536.0) / 2.0;
        this.t3 = t0 * (15.0 * e4 / 64.0 + 105.0 * e6 / 256.0 + 2205.0 * e8 / 4096.0 + 10395.0 * e10 / 16384.0) / 4.0;
        this.t4 = t0 * (35.0 * e6 / 512.0 + 315.0 * e8 / 2048.0 + 31185.0 * e10 / 131072.0) / 6.0;
        this.t5 = t0 * (315.0 * e8 / 16384.0 + 3465.0 * e10 / 65536.0) / 8.0;
        this.t6 = t0 * (693.0 * e10 / 131072.0) / 10.0;
    }

    public double getA() {
        return this.a;
    }

    public double getB() {
        return this.b;
    }

    public double getF() {
        return this.f;
    }

    public double getE() {
        return this.e;
    }

    public double distance(Geographic r, Geographic s) {
        double tsm;
        double azimuthEQ;
        double sigma;
        double cosSigma;
        double dl3;
        double dl;
        double L1 = Math.atan((1.0 - this.f) * Math.tan(r.lat));
        double L2 = Math.atan((1.0 - this.f) * Math.tan(s.lat));
        double sinU1 = Math.sin(L1);
        double sinU2 = Math.sin(L2);
        double cosU1 = Math.cos(L1);
        double cosU2 = Math.cos(L2);
        double dl1 = dl = s.lon - r.lon;
        double cosdl1 = Math.cos(dl);
        double sindl1 = Math.sin(dl);
        do {
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosdl1;
            sigma = Math.acos(cosSigma);
            azimuthEQ = Math.asin(cosU1 * cosU2 * sindl1 / Math.sin(sigma));
            tsm = Math.acos(cosSigma - 2.0 * sinU1 * sinU2 / (Math.cos(azimuthEQ) * Math.cos(azimuthEQ)));
            double dl2 = this.deltaLongitude(azimuthEQ, sigma, tsm);
            dl3 = dl1 - (dl + dl2);
            dl1 = dl + dl2;
            cosdl1 = Math.cos(dl1);
            sindl1 = Math.sin(dl1);
        } while (Math.abs(dl3) > 1.0E-32);
        double u2 = this.mu2(azimuthEQ);
        double A = this.bigA(u2);
        double B = this.bigB(u2);
        double dsigma = B * Math.sin(sigma) * (Math.cos(tsm) + B * cosSigma * (-1.0 + 2.0 * (Math.cos(tsm) * Math.cos(tsm))) / 4.0);
        return this.b * (A * (sigma - dsigma));
    }

    public double direction(Geographic r, Geographic s) {
        double tsm;
        double azimuthEQ;
        double sigma;
        double cosSigma;
        double dl3;
        double dl;
        double L1 = Math.atan((1.0 - this.f) * Math.tan(r.lat));
        double L2 = Math.atan((1.0 - this.f) * Math.tan(s.lat));
        double sinU1 = Math.sin(L1);
        double sinU2 = Math.sin(L2);
        double cosU1 = Math.cos(L1);
        double cosU2 = Math.cos(L2);
        double dl1 = dl = s.lon - r.lon;
        double cosdl1 = Math.cos(dl);
        double sindl1 = Math.sin(dl);
        do {
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosdl1;
            sigma = Math.acos(cosSigma);
            azimuthEQ = Math.asin(cosU1 * cosU2 * sindl1 / Math.sin(sigma));
            tsm = Math.acos(cosSigma - 2.0 * sinU1 * sinU2 / (Math.cos(azimuthEQ) * Math.cos(azimuthEQ)));
            double dl2 = this.deltaLongitude(azimuthEQ, sigma, tsm);
            dl3 = dl1 - (dl + dl2);
            dl1 = dl + dl2;
            cosdl1 = Math.cos(dl1);
            sindl1 = Math.sin(dl1);
        } while (Math.abs(dl3) > 1.0E-32);
        double u2 = this.mu2(azimuthEQ);
        double A = this.bigA(u2);
        double B = this.bigB(u2);
        double dsigma = B * Math.sin(sigma) * (Math.cos(tsm) + B * cosSigma * (-1.0 + 2.0 * (Math.cos(tsm) * Math.cos(tsm))) / 4.0);
        double d_tmp = this.b * (A * (sigma - dsigma));
        double azimuthFD = Math.atan2(cosU2 * sindl1, cosU1 * sinU2 - sinU1 * cosU2 * cosdl1);
        if (azimuthFD < 0.0) {
            azimuthFD += Math.PI * 2;
        }
        return azimuthFD;
    }

    public Geographic project(Geographic r, double length, double angle) {
        double tsm;
        double cis;
        double s1;
        double e2 = Math.sqrt(this.a * this.a - this.b * this.b) / this.b;
        double e2s = e2 * e2;
        double L1 = Math.atan((1.0 - this.f) * Math.tan(r.lat));
        double cosU1 = Math.cos(L1);
        double sinU1 = Math.sin(L1);
        double cosa1 = Math.cos(angle);
        double sina1 = Math.sin(angle);
        double sig1 = Math.atan(Math.tan(L1) / cosa1);
        double sinae = cosU1 * sina1;
        double azimuthEQ = Math.asin(sinae);
        double u2 = this.mu2(azimuthEQ);
        double A = this.bigA(u2);
        double B = this.bigB(u2);
        double sigma = s1 = length / (this.b * A);
        do {
            tsm = 2.0 * sig1 + sigma;
            double del = B * Math.sin(sigma) * (Math.cos(tsm) + 0.25 * B * Math.cos(sigma) * (-1.0 + 2.0 * (Math.cos(tsm) * Math.cos(tsm))));
            cis = sigma - (s1 + del);
            sigma = s1 + del;
        } while (Math.abs(cis) > 1.0E-32);
        double cossigma = Math.cos(sigma);
        double sinsigma = Math.sin(sigma);
        Geographic s = new Geographic();
        s.lat = sinU1 * cossigma + cosU1 * sinsigma * cosa1;
        double dm = Math.sqrt(sinae * sinae + (sinU1 * sinsigma - cosU1 * cossigma * cosa1) * (sinU1 * sinsigma - cosU1 * cossigma * cosa1));
        s.lat = Math.atan2(s.lat, (1.0 - this.f) * dm);
        double dl1 = Math.atan2(sinsigma * sina1, cosU1 * cossigma - sinU1 * sinsigma * cosa1);
        s.lon = r.lon + dl1 - this.deltaLongitude(azimuthEQ, sigma, tsm);
        return s;
    }

    public double meridianRadiusOfCurvature(double latitude) {
        double er = 1.0 - this.es * Math.sin(latitude) * Math.sin(latitude);
        double el = Math.pow(er, 1.5);
        double M0 = this.a * (1.0 - this.es) / el;
        return M0;
    }

    public double primeVerticalRadiusOfCurvature(double latitude) {
        double T1 = this.a * this.a;
        double T2 = T1 * Math.cos(latitude) * Math.cos(latitude);
        double T3 = this.b * this.b * Math.sin(latitude) * Math.sin(latitude);
        double N0 = T1 / Math.sqrt(T2 + T3);
        return N0;
    }

    public double deltaLongitude(double azimuth, double sigma, double tsm) {
        double das = Math.cos(azimuth) * Math.cos(azimuth);
        double C = this.f / 16.0 * das * (4.0 + this.f * (4.0 - 3.0 * das));
        double ctsm = Math.cos(tsm);
        double DL = ctsm + C * Math.cos(sigma) * (-1.0 + 2.0 * ctsm * ctsm);
        DL = sigma + C * Math.sin(sigma) * DL;
        return (1.0 - C) * this.f * Math.sin(azimuth) * DL;
    }

    public double mu2(double azimuth) {
        double e2 = Math.sqrt(this.a * this.a - this.b * this.b) / this.b;
        return Math.cos(azimuth) * Math.cos(azimuth) * e2 * e2;
    }

    public double bigA(double u2) {
        return 1.0 + u2 / 256.0 * (64.0 + u2 * (-12.0 + 5.0 * u2));
    }

    public double bigB(double u2) {
        return u2 / 512.0 * (128.0 + u2 * (-64.0 + 37.0 * u2));
    }

    public double M(double latitude) {
        return this.t1 * latitude - this.t2 * Math.sin(2.0 * latitude) + this.t3 * Math.sin(4.0 * latitude) - this.t4 * Math.sin(6.0 * latitude) + this.t5 * Math.sin(8.0 * latitude) - this.t5 * Math.sin(10.0 * latitude);
    }
}

