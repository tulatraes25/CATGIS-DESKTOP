/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.util;

public class MathUtil {
    public static double orderOfMagnitude(double x) {
        return MathUtil.base10Log(x);
    }

    public static double base10Log(double x) {
        return Math.log(x) / Math.log(10.0);
    }

    public static int mostSignificantDigit(double x) {
        return (int)(x / Math.pow(10.0, Math.floor(MathUtil.orderOfMagnitude(x))));
    }

    public static double avg(double a, double b) {
        return (a + b) / 2.0;
    }
}

