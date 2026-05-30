/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.StyleVisitor;

public interface ColorMapEntry {
    public String getLabel();

    public void setLabel(String var1);

    public void setColor(Expression var1);

    public Expression getColor();

    public void setOpacity(Expression var1);

    public Expression getOpacity();

    public void setQuantity(Expression var1);

    public Expression getQuantity();

    public void accept(StyleVisitor var1);
}

