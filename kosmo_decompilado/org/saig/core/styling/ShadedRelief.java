/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.StyleVisitor;

public interface ShadedRelief {
    public void setBrightnessOnly(boolean var1);

    public boolean isBrightnessOnly();

    public void setReliefFactor(Expression var1);

    public Expression getReliefFactor();

    public void accept(StyleVisitor var1);
}

