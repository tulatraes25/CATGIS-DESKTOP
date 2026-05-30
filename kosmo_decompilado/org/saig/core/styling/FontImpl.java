/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.styling.Font;
import org.saig.core.styling.StyleVisitor;

public class FontImpl
implements Font,
Cloneable {
    private Expression fontFamily = null;
    private Expression fontSize = null;
    private Expression fontStyle = null;
    private Expression fontWeight = null;

    @Override
    public Expression getFontFamily() {
        return this.fontFamily;
    }

    @Override
    public void setFontFamily(Expression fontFamily) {
        this.fontFamily = fontFamily;
    }

    @Override
    public Expression getFontSize() {
        return this.fontSize;
    }

    @Override
    public void setFontSize(Expression fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public Expression getFontStyle() {
        return this.fontStyle;
    }

    @Override
    public void setFontStyle(Expression fontStyle) {
        this.fontStyle = fontStyle;
    }

    @Override
    public Expression getFontWeight() {
        return this.fontWeight;
    }

    @Override
    public void setFontWeight(Expression fontWeight) {
        this.fontWeight = fontWeight;
    }

    public Object clone() {
        FontImpl clone = new FontImpl();
        clone.setFontFamily(this.getFontFamily());
        clone.setFontSize(this.getFontSize());
        clone.setFontStyle(this.getFontStyle());
        clone.setFontWeight(this.getFontWeight());
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.fontFamily != null) {
            result = 1000003 * result + this.fontFamily.hashCode();
        }
        if (this.fontSize != null) {
            result = 1000003 * result + this.fontSize.hashCode();
        }
        if (this.fontStyle != null) {
            result = 1000003 * result + this.fontStyle.hashCode();
        }
        if (this.fontWeight != null) {
            result = 1000003 * result + this.fontWeight.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth == null) {
            return false;
        }
        if (oth instanceof FontImpl) {
            FontImpl other = (FontImpl)oth;
            return Utilities.equals((Object)this.fontFamily, (Object)other.fontFamily) && Utilities.equals((Object)this.fontSize, (Object)other.fontSize) && Utilities.equals((Object)this.fontStyle, (Object)other.fontStyle) && Utilities.equals((Object)this.fontWeight, (Object)other.fontWeight);
        }
        return false;
    }

    @Override
    public boolean isEmptyFont() {
        return this.fontFamily == null && this.fontSize == null && this.fontStyle == null && this.fontWeight == null;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

