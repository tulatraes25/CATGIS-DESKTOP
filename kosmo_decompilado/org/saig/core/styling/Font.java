/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.StyleVisitor;

public interface Font {
    public Expression getFontFamily();

    public void setFontFamily(Expression var1);

    public Expression getFontStyle();

    public void setFontStyle(Expression var1);

    public Expression getFontWeight();

    public void setFontWeight(Expression var1);

    public Expression getFontSize();

    public void setFontSize(Expression var1);

    public boolean isEmptyFont();

    public void accept(StyleVisitor var1);
}

