/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;

public class Angle {
    public static final double PI_TIMES_2 = Math.PI * 2;
    public static final double PI_OVER_2 = 1.5707963267948966;
    public static final double PI_OVER_4 = 0.7853981633974483;
    public static final int COUNTERCLOCKWISE = 0;
    public static final int CLOCKWISE = 1;
    public static final int NONE = 2;

    public static double toDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }

    public static double angle(Coordinate p0, Coordinate p1) {
        double dx = p1.x - p0.x;
        double dy = p1.y - p0.y;
        return Math.atan2(dy, dx);
    }

    public static double toRadians(double angleDegrees) {
        return angleDegrees * Math.PI / 180.0;
    }

    public static double angleBetween(Coordinate tail, Coordinate tip1, Coordinate tip2) {
        double a1 = Angle.angle(tail, tip1);
        double a2 = Angle.angle(tail, tip2);
        return Angle.diff(a1, a2);
    }

    public static double interiorAngle(Coordinate p0, Coordinate p1, Coordinate p2) {
        double anglePrev = Angle.angle(p1, p0);
        double angleNext = Angle.angle(p1, p2);
        return Math.abs(angleNext - anglePrev);
    }

    public static int getTurn(double a1, double a2) {
        double crossproduct = Math.sin(a2 - a1);
        if (crossproduct > 0.0) {
            return 0;
        }
        if (crossproduct < 0.0) {
            return 1;
        }
        return 2;
    }

    public static double normalize(double angle) {
        while (angle > Math.PI) {
            angle -= Math.PI * 2;
        }
        while (angle < -Math.PI) {
            angle += Math.PI * 2;
        }
        return angle;
    }

    public static double diff(double a1, double a2) {
        double da = a1 < a2 ? a2 - a1 : a1 - a2;
        if (da > Math.PI) {
            da = Math.PI * 2 - da;
        }
        return da;
    }
}

