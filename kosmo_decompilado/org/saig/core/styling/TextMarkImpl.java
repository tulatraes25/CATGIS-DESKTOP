/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.List;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Font;
import org.saig.core.styling.MarkImpl;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.TextMark;

public class TextMarkImpl
extends MarkImpl
implements TextMark,
Cloneable {
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression wellKnownName = null;
    private List<Font> fonts = new ArrayList<Font>();
    private Expression symbol;

    public TextMarkImpl() {
    }

    public TextMarkImpl(Font font, String symbol) {
        this.addFont(font);
        this.setSymbol(symbol);
        this.wellKnownName = filterFactory.createLiteralExpression("Symbol");
    }

    public TextMarkImpl(Font font, Expression symbol) {
        this.addFont(font);
        this.setSymbol(symbol);
        this.wellKnownName = filterFactory.createLiteralExpression("Symbol");
    }

    @Override
    public Expression getWellKnownName() {
        return this.wellKnownName;
    }

    @Override
    public Font[] getFonts() {
        return this.fonts.toArray(new Font[0]);
    }

    @Override
    public void addFont(Font font) {
        this.fonts.add(font);
    }

    @Override
    public Expression getSymbol() {
        return this.symbol;
    }

    @Override
    public void setSymbol(String symbol) {
        this.symbol = filterFactory.createLiteralExpression(symbol);
    }

    @Override
    public void setSymbol(Expression symbol) {
        this.symbol = symbol;
    }

    @Override
    public void setWellKnownName(Expression wellKnownName) {
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Object clone() {
        TextMarkImpl clone = new TextMarkImpl();
        if (this.fill != null) {
            clone.setFill((Fill)((Cloneable)this.fill).clone());
        }
        clone.setRotation(this.getRotation());
        clone.setSize(this.getSize());
        clone.setSymbol(this.getSymbol());
        if (this.stroke != null) {
            clone.setStroke((Stroke)this.stroke.clone());
        }
        if (this.fonts != null) {
            clone.fonts = new ArrayList<Font>();
            int i = 0;
            while (i < this.fonts.size()) {
                Font currentFont = this.fonts.get(i);
                clone.addFont((Font)((Cloneable)currentFont).clone());
                ++i;
            }
        }
        clone.setWellKnownName(this.getWellKnownName());
        return clone;
    }
}

