/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.resources.Utilities
 *  org.opengis.util.Cloneable
 */
package org.saig.core.styling;

import org.apache.log4j.Logger;
import org.geotools.resources.Utilities;
import org.opengis.util.Cloneable;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.styling.Fill;
import org.saig.core.styling.FillImpl;
import org.saig.core.styling.Halo;
import org.saig.core.styling.StyleVisitor;

public class HaloImpl
implements Halo,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling.HaloImpl");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Fill fill = new FillImpl();
    private Expression radius = null;

    public HaloImpl() {
        try {
            this.radius = filterFactory.createLiteralExpression(new Integer(1));
        }
        catch (IllegalFilterException ife) {
            LOGGER.error((Object)("Failed to build defaultHalo: " + ife));
        }
        this.fill.setColor(filterFactory.createLiteralExpression("#FFFFFF"));
    }

    @Override
    public Fill getFill() {
        return this.fill;
    }

    @Override
    public void setFill(Fill fill) {
        this.fill = fill;
    }

    @Override
    public Expression getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(Expression radius) {
        this.radius = radius;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        HaloImpl clone = new HaloImpl();
        if (this.fill != null) {
            clone.setFill((Fill)((Cloneable)this.fill).clone());
        } else {
            clone.setFill(null);
        }
        clone.setRadius(this.getRadius());
        return clone;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof HaloImpl) {
            HaloImpl other = (HaloImpl)obj;
            return Utilities.equals((Object)this.radius, (Object)other.radius) && Utilities.equals((Object)this.fill, (Object)other.fill);
        }
        return false;
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        if (this.radius != null) {
            result = result * 37 + this.radius.hashCode();
        }
        if (this.fill != null) {
            result = result * 37 + this.fill.hashCode();
        }
        return result;
    }
}

