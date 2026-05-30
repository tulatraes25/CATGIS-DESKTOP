/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package com.vividsolutions.jump.workbench.ui.plugin.scalebar;

import java.text.DecimalFormat;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

public class RoundQuantity {
    private Unit<Length> unit;
    private Unit<Length> mapLengthUnit;
    private int mantissa;
    private int exponent;

    public RoundQuantity(int mantissa, int exponent, Unit<Length> unit, Unit<Length> lengthUnit) {
        this.mantissa = mantissa;
        this.exponent = exponent;
        this.unit = unit;
        this.mapLengthUnit = lengthUnit;
    }

    public Unit<Length> getUnit() {
        return this.unit;
    }

    public int getMantissa() {
        return this.mantissa;
    }

    public String toString() {
        return String.valueOf(this.getAmountString()) + " " + this.getUnit();
    }

    public String getAmountString() {
        if (this.getMantissa() == 0) {
            return "0";
        }
        if (this.getExponent() >= 0 && this.getExponent() <= 3) {
            return new DecimalFormat("#").format(this.getAmount());
        }
        if (-4 <= this.getExponent() && this.getExponent() < 0) {
            return new DecimalFormat("#.####").format(this.getAmount());
        }
        return String.valueOf(this.getMantissa()) + "E" + this.getExponent();
    }

    public int getExponent() {
        return this.exponent;
    }

    public double getAmount() {
        return (double)this.mantissa * Math.pow(10.0, this.exponent);
    }

    public double getModelValue() {
        double goodUnitDistance = this.unit.getConverterTo(this.mapLengthUnit).convert(1.0);
        return this.getAmount() * goodUnitDistance;
    }
}

