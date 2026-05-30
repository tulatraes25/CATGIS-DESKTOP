/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Color;
import org.saig.core.filter.Expression;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.StyleVisitor;

public interface Stroke {
    public Expression getColor();

    public Color getColor(Feature var1);

    public void setColor(Expression var1);

    public Expression getWidth();

    public void setWidth(Expression var1);

    public Expression getOpacity();

    public void setOpacity(Expression var1);

    public Expression getLineJoin();

    public void setLineJoin(Expression var1);

    public Expression getLineCap();

    public void setLineCap(Expression var1);

    public float[] getDashArray();

    public void setDashArray(float[] var1);

    public Expression getDashOffset();

    public void setDashOffset(Expression var1);

    public Graphic getGraphicFill();

    public void setGraphicFill(Graphic var1);

    public Graphic getGraphicStroke();

    public void setGraphicStroke(Graphic var1);

    public void accept(StyleVisitor var1);

    public Object clone();
}

