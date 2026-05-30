/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.awt.Toolkit;

public class Conversion {
    public static final double cm_In_inch = 2.54;
    public static final double inch_In_cm = 0.3937007874;
    private static final int pt_In_inch = Toolkit.getDefaultToolkit().getScreenResolution();
    private static final int pt_In_cm = Math.round((float)((double)pt_In_inch / 2.54));

    public static int inch_To_cm(double value) {
        return Math.round((float)(value * 2.54));
    }

    public static int cm_To_inch(double value) {
        return Math.round((float)(value * 0.3937007874));
    }

    public static double seventyTwoInch_To_Cm(double value) {
        return value / 72.0 * 2.54;
    }

    public static double Cm_To_seventyTwoInch(double value) {
        return value * 0.3937007874 * 72.0;
    }

    public static int seventyTwoInch_To_Inch(double value) {
        return Math.round((float)(value / 72.0));
    }

    public static int inch_To_Pixel(double value) {
        return Math.round((float)(value * (double)pt_In_inch));
    }

    public static int pixel_To_Inch(double value) {
        return Math.round((float)(value / (double)pt_In_inch));
    }

    public static int seventyTwoInch_To_Pixel(double value) {
        return Math.round((float)(value * 72.0 / (double)pt_In_inch));
    }

    public static int cm_To_Pixel(double value) {
        return Math.round((float)value * (float)pt_In_cm);
    }
}

