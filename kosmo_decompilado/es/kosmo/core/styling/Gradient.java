/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.styling;

import java.awt.Color;
import org.saig.core.styling.StyleVisitor;

public interface Gradient {
    public static final GradientCycleMethod DEFAULT_CYCLE_METHOD = GradientCycleMethod.NONE;

    public float[] getFractions();

    public void setFractions(float[] var1);

    public Color[] getColors();

    public void setColors(Color[] var1);

    public GradientType getType();

    public GradientCycleMethod getCycleMethod();

    public void setCycleMethod(GradientCycleMethod var1);

    public void accept(StyleVisitor var1);

    public static enum GradientCycleMethod {
        NONE,
        REPEAT,
        REFLECT;

    }

    public static enum GradientType {
        LINEAR,
        RADIAL;

    }
}

