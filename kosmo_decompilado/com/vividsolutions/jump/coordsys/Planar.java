/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

public class Planar {
    public double x;
    public double y;
    public double z;

    public Planar() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public Planar(double _x, double _y) {
        this.x = _x;
        this.y = _y;
        this.z = 0.0;
    }

    public String toString() {
        return String.valueOf(this.x) + ", " + this.y;
    }
}

