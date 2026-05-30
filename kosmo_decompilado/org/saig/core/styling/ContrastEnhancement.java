/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.StyleVisitor;

public interface ContrastEnhancement {
    public void setType(Expression var1);

    public Expression getType();

    public void setGammaValue(Expression var1);

    public Expression getGammaValue();

    public void setNormalize();

    public void setHistogram();

    public void setLogarithmic();

    public void setExponential();

    public void accept(StyleVisitor var1);
}

