/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.snap.algorithms;

public class AngleAlgs {
    public double normalizeAngle(double a, double center) {
        return a - Math.PI * 2 * Math.floor((a + Math.PI - center) / (Math.PI * 2));
    }

    public double clipAngle(double angle, double baseAngle, double[] angles) {
        double ang = this.normalizeAngle(angle, Math.PI);
        double distance = Double.MAX_VALUE;
        double clipped = 0.0;
        double auxDistance = 0.0;
        double[] dArray = angles;
        int n = angles.length;
        int n2 = 0;
        while (n2 < n) {
            double allowed = dArray[n2];
            auxDistance = Math.abs((allowed = this.normalizeAngle(baseAngle + allowed, Math.PI)) - ang);
            if (auxDistance < distance) {
                clipped = allowed;
                distance = auxDistance;
            }
            ++n2;
        }
        return clipped;
    }

    public double clipAngle(double angle, double[] angles) {
        double ang = this.normalizeAngle(angle, Math.PI);
        double distance = Double.MAX_VALUE;
        double clipped = 0.0;
        double auxDistance = 0.0;
        double[] dArray = angles;
        int n = angles.length;
        int n2 = 0;
        while (n2 < n) {
            double allowed = dArray[n2];
            auxDistance = Math.abs((allowed = this.normalizeAngle(allowed, Math.PI)) - ang);
            if (auxDistance < distance) {
                clipped = allowed;
                distance = auxDistance;
            }
            ++n2;
        }
        return clipped;
    }
}

