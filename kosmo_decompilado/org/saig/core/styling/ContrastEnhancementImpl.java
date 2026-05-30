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
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.StyleVisitor;

public class ContrastEnhancementImpl
implements ContrastEnhancement,
Cloneable {
    private FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression gamma;
    private Expression type;

    @Override
    public Expression getGammaValue() {
        return this.gamma;
    }

    @Override
    public Expression getType() {
        return this.type;
    }

    @Override
    public void setGammaValue(Expression gamma) {
        this.gamma = gamma;
    }

    @Override
    public void setHistogram() {
        this.type = this.filterFactory.createLiteralExpression("Histogram");
    }

    @Override
    public void setNormalize() {
        this.type = this.filterFactory.createLiteralExpression("Normalize");
    }

    @Override
    public void setLogarithmic() {
        this.type = this.filterFactory.createLiteralExpression("Logarithmic");
    }

    @Override
    public void setExponential() {
        this.type = this.filterFactory.createLiteralExpression("Exponential");
    }

    @Override
    public void setType(Expression type) {
        this.type = type;
    }

    public Object clone() {
        ContrastEnhancementImpl clone = new ContrastEnhancementImpl();
        clone.setGammaValue(this.getGammaValue());
        clone.setType(this.getType());
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
        if (oth instanceof ContrastEnhancementImpl) {
            ContrastEnhancementImpl other = (ContrastEnhancementImpl)oth;
            return Utilities.equals((Object)this.gamma, (Object)other.gamma) && Utilities.equals((Object)this.type, (Object)other.type);
        }
        return false;
    }
}

