/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.styling.ContrastEnhancement;
import org.saig.core.styling.ContrastEnhancementImpl;
import org.saig.core.styling.SelectedChannelType;
import org.saig.core.styling.StyleVisitor;

public class SelectedChannelTypeImpl
implements SelectedChannelType,
Cloneable {
    private static FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private ContrastEnhancement contrastEnhancement = this.contrastEnhancement(filterFactory.createLiteralExpression(1.0));
    private String name = "channel";

    @Override
    public String getChannelName() {
        return this.name;
    }

    @Override
    public ContrastEnhancement getContrastEnhancement() {
        return this.contrastEnhancement;
    }

    @Override
    public void setChannelName(String name) {
        this.name = name;
    }

    @Override
    public void setContrastEnhancement(ContrastEnhancement enhancement) {
        this.contrastEnhancement = enhancement;
    }

    public Object clone() {
        SelectedChannelTypeImpl clone = new SelectedChannelTypeImpl();
        clone.setChannelName(this.getChannelName());
        clone.setContrastEnhancement(this.getContrastEnhancement());
        return clone;
    }

    protected ContrastEnhancement contrastEnhancement(Expression expr) {
        ContrastEnhancementImpl enhancement = new ContrastEnhancementImpl();
        enhancement.setGammaValue(filterFactory.createLiteralExpression(1.0));
        return enhancement;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

