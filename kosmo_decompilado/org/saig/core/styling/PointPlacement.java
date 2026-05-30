/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.StyleVisitor;

public interface PointPlacement
extends LabelPlacement {
    public AnchorPoint getAnchorPoint();

    public void setAnchorPoint(AnchorPoint var1);

    public Displacement getDisplacement();

    public void setDisplacement(Displacement var1);

    public Expression getRotation();

    public void setRotation(Expression var1);

    @Override
    public void accept(StyleVisitor var1);
}

