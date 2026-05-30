/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

public class Radius {
    public double a;
    public double b;
    public double rf;
    public static final int WGS72 = 1;
    public static final int CLARKE = 2;
    public static final int GRS80 = 0;
    public static final int ED50 = 3;

    public Radius(int type) {
        switch (type) {
            case 0: {
                this.a = 6378137.0;
                this.b = -1.0;
                this.rf = 298.257222101;
                break;
            }
            case 1: {
                this.a = 6378135.0;
                this.b = 6356750.5;
                this.rf = -1.0;
                break;
            }
            case 2: {
                this.a = 6378206.4;
                this.b = 6356583.8;
                this.rf = -1.0;
            }
        }
    }
}

