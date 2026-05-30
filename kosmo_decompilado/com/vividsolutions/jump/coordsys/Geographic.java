/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

public class Geographic {
    public double lat;
    public double lon;
    public double hgt;

    public Geographic() {
        this.lat = 0.0;
        this.lon = 0.0;
        this.hgt = 0.0;
    }

    public Geographic(double _lat, double _lon) {
        this.lat = _lat;
        this.lon = _lon;
        this.hgt = 0.0;
    }

    public String toString() {
        return String.valueOf(this.lat) + ", " + this.lon;
    }
}

