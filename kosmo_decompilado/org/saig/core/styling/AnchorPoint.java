/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.StyleVisitor;

public interface AnchorPoint {
    public Expression getAnchorPointX();

    public void setAnchorPointX(Expression var1);

    public Expression getAnchorPointY();

    public void setAnchorPointY(Expression var1);

    public void accept(StyleVisitor var1);
}

