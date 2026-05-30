/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.printing.scale;

import org.saig.jump.lang.I18N;

public class Scale {
    private String style;
    private String units;
    private double intLong;
    private int nInt;
    private double leftDivLon;
    public static final String TEXT = I18N.getString("org.saig.core.printing.scale.Scale.text");
    public static final String GRAPHIC_CLASSIC = I18N.getString("org.saig.core.printing.scale.Scale.graphic");
    public static String[] scaleTypes = new String[]{TEXT, GRAPHIC_CLASSIC};
    public static final String METRIC = I18N.getString("org.saig.core.printing.scale.Scale.meters");
    public static String[] unitsAvaliable = new String[]{METRIC};

    public double getIntLong() {
        return this.intLong;
    }

    public void setIntLong(double intLong) {
        this.intLong = intLong;
    }

    public double getLeftDivLon() {
        return this.leftDivLon;
    }

    public void setLeftDivLon(double leftDivLon) {
        this.leftDivLon = leftDivLon;
    }

    public int getNInt() {
        return this.nInt;
    }

    public void setNInt(int int1) {
        this.nInt = int1;
    }

    public String getStyle() {
        return this.style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUnits() {
        return this.units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}

