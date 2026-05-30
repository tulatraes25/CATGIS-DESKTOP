/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.StyleVisitor;

public interface Displacement {
    public Expression getDisplacementX();

    public void setDisplacementX(Expression var1);

    public Expression getDisplacementY();

    public void setDisplacementY(Expression var1);

    public void accept(StyleVisitor var1);
}

