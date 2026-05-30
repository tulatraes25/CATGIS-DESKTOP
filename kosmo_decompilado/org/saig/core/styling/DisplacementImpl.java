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
import org.saig.core.styling.Displacement;
import org.saig.core.styling.StyleVisitor;

public class DisplacementImpl
implements Displacement,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression displacementX = null;
    private Expression displacementY = null;

    public DisplacementImpl() {
        try {
            this.displacementX = filterFactory.createLiteralExpression(new Integer(0));
            this.displacementY = filterFactory.createLiteralExpression(new Integer(0));
        }
        catch (IllegalFilterException ife) {
            LOGGER.error((Object)("Failed to build defaultDisplacement: " + ife));
        }
    }

    @Override
    public void setDisplacementX(Expression displacementX) {
        this.displacementX = displacementX;
    }

    @Override
    public void setDisplacementY(Expression displacementY) {
        this.displacementY = displacementY;
    }

    @Override
    public Expression getDisplacementX() {
        return this.displacementX;
    }

    @Override
    public Expression getDisplacementY() {
        return this.displacementY;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        DisplacementImpl clone = new DisplacementImpl();
        clone.setDisplacementX(this.getDisplacementX());
        clone.setDisplacementY(this.getDisplacementY());
        return clone;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DisplacementImpl) {
            DisplacementImpl other = (DisplacementImpl)obj;
            return Utilities.equals((Object)this.displacementX, (Object)other.displacementX) && Utilities.equals((Object)this.displacementY, (Object)other.displacementY);
        }
        return false;
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        if (this.displacementX != null) {
            result = result * 37 + this.displacementX.hashCode();
        }
        if (this.displacementY != null) {
            result = result * 37 + this.displacementY.hashCode();
        }
        return result;
    }
}

