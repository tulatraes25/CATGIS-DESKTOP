/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.ShadedRelief;
import org.saig.core.styling.StyleVisitor;

public class ShadedReliefImpl
implements ShadedRelief,
Cloneable {
    private static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression reliefFactor = filterFactory.createLiteralExpression(55);
    private boolean brightness = false;

    @Override
    public Expression getReliefFactor() {
        return this.reliefFactor;
    }

    @Override
    public boolean isBrightnessOnly() {
        return this.brightness;
    }

    @Override
    public void setBrightnessOnly(boolean flag) {
        this.brightness = flag;
    }

    @Override
    public void setReliefFactor(Expression reliefFactor) {
        this.reliefFactor = reliefFactor;
    }

    public Object clone() {
        ShadedReliefImpl clone = new ShadedReliefImpl();
        clone.setBrightnessOnly(this.isBrightnessOnly());
        clone.setReliefFactor(this.getReliefFactor());
        return clone;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof ShadedReliefImpl) {
            ShadedReliefImpl other = (ShadedReliefImpl)oth;
            return this.brightness == other.brightness && Utilities.equals((Object)this.reliefFactor, (Object)other.reliefFactor);
        }
        return false;
    }
}

