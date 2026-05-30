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
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.StyleVisitor;

public class LinePlacementImpl
implements LinePlacement,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger(LinePlacementImpl.class);
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression perpendicularOffset = null;
    private Expression attributeRotation;

    public LinePlacementImpl() {
        try {
            this.perpendicularOffset = filterFactory.createLiteralExpression(new Integer(0));
        }
        catch (IllegalFilterException ife) {
            LOGGER.error((Object)("Failed to build defaultLinePlacement: " + ife));
        }
    }

    @Override
    public Expression getPerpendicularOffset() {
        return this.perpendicularOffset;
    }

    @Override
    public void setPerpendicularOffset(Expression perpendicularOffset) {
        this.perpendicularOffset = perpendicularOffset;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        LinePlacementImpl clone = new LinePlacementImpl();
        clone.setAttributeRotation(this.getAttributeRotation());
        clone.setPerpendicularOffset(this.getPerpendicularOffset());
        return clone;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LinePlacementImpl) {
            LinePlacementImpl other = (LinePlacementImpl)obj;
            return Utilities.equals((Object)this.perpendicularOffset, (Object)other.perpendicularOffset);
        }
        return false;
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        if (this.perpendicularOffset != null) {
            result = result * 37 + this.perpendicularOffset.hashCode();
        }
        return result;
    }

    @Override
    public Expression getAttributeRotation() {
        return this.attributeRotation;
    }

    @Override
    public void setAttributeRotation(Expression attributeRotation) {
        this.attributeRotation = attributeRotation;
    }
}

