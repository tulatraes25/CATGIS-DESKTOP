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
import org.saig.core.styling.ColorMapEntry;
import org.saig.core.styling.StyleVisitor;

public class ColorMapEntryImpl
implements ColorMapEntry,
Cloneable {
    private Expression quantity;
    private Expression opacity;
    private Expression color;
    private String label;

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setColor(Expression color) {
        this.color = color;
    }

    @Override
    public Expression getColor() {
        return this.color;
    }

    @Override
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    @Override
    public Expression getOpacity() {
        return this.opacity;
    }

    @Override
    public void setQuantity(Expression quantity) {
        this.quantity = quantity;
    }

    @Override
    public Expression getQuantity() {
        return this.quantity;
    }

    public Object clone() {
        ColorMapEntryImpl clone = new ColorMapEntryImpl();
        clone.setColor(this.getColor());
        clone.setLabel(this.getLabel());
        clone.setOpacity(this.getOpacity());
        clone.setQuantity(this.getQuantity());
        return clone;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof ColorMapEntryImpl) {
            ColorMapEntryImpl other = (ColorMapEntryImpl)oth;
            return Utilities.equals((Object)this.quantity, (Object)other.quantity) && Utilities.equals((Object)this.label, (Object)other.label) && Utilities.equals((Object)this.opacity, (Object)other.opacity) && Utilities.equals((Object)this.color, (Object)other.color);
        }
        return false;
    }
}

