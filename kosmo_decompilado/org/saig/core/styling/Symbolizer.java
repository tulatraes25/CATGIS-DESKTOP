/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.StyleVisitor;
import org.saig.jump.util.MeasureUtils;

public interface Symbolizer {
    public static final String PIXEL_UNITS = "pixel";
    public static final String[] UOM_ALLOWED = new String[]{"pixel", MeasureUtils.AceptedLengthUnits.METER.getUnit().toString(), MeasureUtils.AceptedLengthUnits.KILOMETER.getUnit().toString(), MeasureUtils.AceptedLengthUnits.MILIMETER.getUnit().toString(), MeasureUtils.AceptedLengthUnits.CENTIMETER.getUnit().toString(), MeasureUtils.AceptedLengthUnits.INCH.getUnit().toString(), MeasureUtils.AceptedLengthUnits.FOOT.getUnit().toString(), MeasureUtils.AceptedLengthUnits.MILE.getUnit().toString()};

    public void accept(StyleVisitor var1);

    public void setActive(boolean var1);

    public boolean isActive();

    public String getUnitsOfMeasurement();

    public void setUnitsOfMeasurement(String var1);
}

