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
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.StyleVisitor;

public class AnchorPointImpl
implements AnchorPoint,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression anchorPointX = null;
    private Expression anchorPointY = null;

    public AnchorPointImpl() {
        try {
            this.anchorPointX = filterFactory.createLiteralExpression(new Double(0.5));
            this.anchorPointY = filterFactory.createLiteralExpression(new Double(0.5));
        }
        catch (IllegalFilterException ife) {
            LOGGER.error((Object)("Failed to build defaultAnchorPoint: " + ife));
        }
    }

    @Override
    public Expression getAnchorPointX() {
        return this.anchorPointX;
    }

    @Override
    public void setAnchorPointX(Expression anchorPointX) {
        this.anchorPointX = anchorPointX;
    }

    @Override
    public Expression getAnchorPointY() {
        return this.anchorPointY;
    }

    @Override
    public void setAnchorPointY(Expression anchorPointY) {
        this.anchorPointY = anchorPointY;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        AnchorPointImpl clone = new AnchorPointImpl();
        clone.setAnchorPointX(this.getAnchorPointX());
        clone.setAnchorPointY(this.getAnchorPointY());
        return clone;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AnchorPointImpl) {
            AnchorPointImpl other = (AnchorPointImpl)obj;
            return Utilities.equals((Object)this.anchorPointX, (Object)other.anchorPointX) && Utilities.equals((Object)this.anchorPointY, (Object)other.anchorPointY);
        }
        return false;
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        if (this.anchorPointX != null) {
            result = result * 37 + this.anchorPointX.hashCode();
        }
        if (this.anchorPointY != null) {
            result = result * 37 + this.anchorPointY.hashCode();
        }
        return result;
    }
}

