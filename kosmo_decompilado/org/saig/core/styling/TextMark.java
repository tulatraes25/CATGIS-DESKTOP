/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.Font;
import org.saig.core.styling.Mark;

public interface TextMark
extends Mark {
    public Font[] getFonts();

    public void addFont(Font var1);

    public Expression getSymbol();

    public void setSymbol(Expression var1);

    public void setSymbol(String var1);

    @Override
    public Expression getWellKnownName();

    @Override
    public void setWellKnownName(Expression var1);
}

