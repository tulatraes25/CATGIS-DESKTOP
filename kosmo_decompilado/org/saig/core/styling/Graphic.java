/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Expression;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.Mark;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbol;

public interface Graphic {
    public ExternalGraphic[] getExternalGraphics();

    public void setExternalGraphics(ExternalGraphic[] var1);

    public void addExternalGraphic(ExternalGraphic var1);

    public Mark[] getMarks();

    public void setMarks(Mark[] var1);

    public void addMark(Mark var1);

    public Symbol[] getSymbols();

    public void setSymbols(Symbol[] var1);

    public void addSymbol(Symbol var1);

    public Expression getOpacity();

    public void setOpacity(Expression var1);

    public Expression getSize();

    public void setSize(Expression var1);

    public Displacement getDisplacement();

    public void setDisplacement(Displacement var1);

    public Expression getRotation();

    public void setRotation(Expression var1);

    public String getGeometryPropertyName();

    public void setGeometryPropertyName(String var1);

    public void accept(StyleVisitor var1);

    @Deprecated
    public Expression getAttributeRotation();

    @Deprecated
    public void setAttributeRotation(Expression var1);
}

