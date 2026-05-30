/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.StyleVisitor;

public interface LinePlacement
extends LabelPlacement {
    public Expression getPerpendicularOffset();

    public void setPerpendicularOffset(Expression var1);

    @Override
    public void accept(StyleVisitor var1);

    public Expression getAttributeRotation();

    public void setAttributeRotation(Expression var1);
}

