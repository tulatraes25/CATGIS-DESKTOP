/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package es.kosmo.core.styling;

import es.kosmo.core.styling.Gradient;
import java.awt.Color;
import org.opengis.util.Cloneable;
import org.saig.core.styling.StyleVisitor;

public abstract class GradientImpl
implements Gradient,
Cloneable {
    protected float[] fractions;
    protected Color[] colors;
    protected Gradient.GradientCycleMethod cycleMethod = Gradient.GradientCycleMethod.NONE;

    @Override
    public float[] getFractions() {
        return this.fractions;
    }

    @Override
    public void setFractions(float[] fractions) {
        this.fractions = fractions;
    }

    @Override
    public Color[] getColors() {
        return this.colors;
    }

    @Override
    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    @Override
    public Gradient.GradientCycleMethod getCycleMethod() {
        return this.cycleMethod;
    }

    public abstract Object clone();

    @Override
    public void setCycleMethod(Gradient.GradientCycleMethod cycleMethod) {
        this.cycleMethod = cycleMethod;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

