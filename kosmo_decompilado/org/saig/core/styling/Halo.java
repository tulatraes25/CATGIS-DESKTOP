/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.Fill;
import org.saig.core.styling.StyleVisitor;

public interface Halo {
    public Expression getRadius();

    public void setRadius(Expression var1);

    public Fill getFill();

    public void setFill(Fill var1);

    public void accept(StyleVisitor var1);
}

