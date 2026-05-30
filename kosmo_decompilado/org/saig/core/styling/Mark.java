/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbol;

public interface Mark
extends Symbol {
    public Expression getWellKnownName();

    public void setWellKnownName(Expression var1);

    public Stroke getStroke();

    public void setStroke(Stroke var1);

    public Fill getFill();

    public void setFill(Fill var1);

    public Expression getSize();

    public void setSize(Expression var1);

    public Expression getRotation();

    public void setRotation(Expression var1);

    @Override
    public void accept(StyleVisitor var1);

    public boolean isEmptyMark();
}

