/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import es.kosmo.core.styling.Gradient;
import org.apache.log4j.Logger;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.StyleVisitor;

public class FillImpl
implements Fill,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling.FillImpl");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression color = null;
    private Expression backgroundColor = null;
    private Expression opacity = null;
    private Graphic graphicFill = null;
    private Gradient gradient = null;

    @Override
    public Expression getColor() {
        return this.color;
    }

    @Override
    public void setColor(Expression rgb) {
        this.color = rgb;
    }

    public void setColor(String rgb) {
        this.color = filterFactory.createLiteralExpression(rgb);
    }

    @Override
    public Expression getBackgroundColor() {
        return this.backgroundColor;
    }

    @Override
    public void setBackgroundColor(Expression rgb) {
        this.backgroundColor = rgb;
    }

    public void setBackgroundColor(String rgb) {
        LOGGER.info((Object)("setting bg color with " + rgb + " as a string"));
        this.backgroundColor = filterFactory.createLiteralExpression(rgb);
    }

    @Override
    public Expression getOpacity() {
        return this.opacity;
    }

    @Override
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    public void setOpacity(String opacity) {
        this.opacity = filterFactory.createLiteralExpression(opacity);
    }

    @Override
    public Graphic getGraphicFill() {
        return this.graphicFill;
    }

    @Override
    public void setGraphicFill(Graphic graphicFill) {
        this.graphicFill = graphicFill;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        FillImpl clone = new FillImpl();
        clone.setBackgroundColor(this.getBackgroundColor());
        clone.setColor(this.getColor());
        clone.setOpacity(this.getOpacity());
        if (this.graphicFill != null) {
            clone.graphicFill = (Graphic)((Cloneable)this.graphicFill).clone();
        }
        if (this.gradient != null) {
            clone.gradient = (Gradient)((Cloneable)this.gradient).clone();
        }
        return clone;
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.color != null) {
            result = 1000003 * result + this.color.hashCode();
        }
        if (this.backgroundColor != null) {
            result = 1000003 * result + this.backgroundColor.hashCode();
        }
        if (this.opacity != null) {
            result = 1000003 * result + this.opacity.hashCode();
        }
        if (this.graphicFill != null) {
            result = 1000003 * result + this.graphicFill.hashCode();
        }
        if (this.gradient != null) {
            result = 1000003 * result + this.gradient.hashCode();
        }
        return result;
    }

    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }
        if (oth instanceof FillImpl) {
            FillImpl other = (FillImpl)oth;
            return Utilities.equals((Object)this.color, (Object)other.color) && Utilities.equals((Object)this.backgroundColor, (Object)other.backgroundColor) && Utilities.equals((Object)this.opacity, (Object)other.opacity) && Utilities.equals((Object)this.graphicFill, (Object)other.graphicFill) && Utilities.equals((Object)this.gradient, (Object)other.gradient);
        }
        return false;
    }

    @Override
    public Gradient getGradientFill() {
        return this.gradient;
    }

    @Override
    public void setGradientFill(Gradient gradient) {
        this.gradient = gradient;
    }
}

