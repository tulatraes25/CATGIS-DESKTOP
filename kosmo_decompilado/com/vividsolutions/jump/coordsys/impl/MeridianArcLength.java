/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jump.coordsys.Spheroid;

public class MeridianArcLength {
    public double s;
    public double a0;
    public double a2;
    public double a4;
    public double a6;
    public double a8;

    public void compute(Spheroid spheroid, double lat, int diff) {
        double a = spheroid.getA();
        double e = spheroid.getE();
        double e2 = e * e;
        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double e8 = e4 * e4;
        this.a0 = 1.0 - e2 / 4.0 - 3.0 * e4 / 64.0 - 5.0 * e6 / 256.0 - 175.0 * e8 / 16384.0;
        this.a2 = 0.375 * (e2 + e4 / 4.0 + 15.0 * e6 / 128.0 - 455.0 * e8 / 4096.0);
        this.a4 = 0.05859375 * (e4 + 3.0 * e6 / 4.0 - 77.0 * e8 / 128.0);
        this.a6 = 0.011393229166666666 * (e6 - 41.0 * e8 / 32.0);
        this.a8 = -315.0 * e8 / 131072.0;
        this.s = diff == 0 ? a * (this.a0 * lat - this.a2 * Math.sin(2.0 * lat) + this.a4 * Math.sin(4.0 * lat) - this.a6 * Math.sin(6.0 * lat) + this.a8 * Math.sin(8.0 * lat)) : this.a0 * lat - 2.0 * this.a2 * Math.cos(2.0 * lat) + 4.0 * this.a4 * Math.cos(4.0 * lat) - 6.0 * this.a6 * Math.cos(6.0 * lat) + 8.0 * this.a8 * Math.cos(8.0 * lat);
    }
}

