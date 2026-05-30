/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import es.kosmo.core.styling.Gradient;
import org.saig.core.filter.Expression;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.StyleVisitor;

public interface Fill {
    public Expression getColor();

    public void setColor(Expression var1);

    public Expression getBackgroundColor();

    public void setBackgroundColor(Expression var1);

    public Expression getOpacity();

    public void setOpacity(Expression var1);

    public Graphic getGraphicFill();

    public void setGraphicFill(Graphic var1);

    public void accept(StyleVisitor var1);

    public Gradient getGradientFill();

    public void setGradientFill(Gradient var1);
}

