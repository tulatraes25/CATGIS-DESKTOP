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
import org.saig.core.styling.AnchorPointImpl;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.DisplacementImpl;
import org.saig.core.styling.PointPlacement;
import org.saig.core.styling.StyleVisitor;

public class PointPlacementImpl
implements PointPlacement,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.styling.PointPlacementImpl");
    private static final FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private AnchorPoint anchorPoint = new AnchorPointImpl();
    private Displacement displacement = new DisplacementImpl();
    private Expression rotation = null;
    private Expression attributeRotation = null;

    public PointPlacementImpl() {
        try {
            this.rotation = filterFactory.createLiteralExpression(new Integer(0));
        }
        catch (IllegalFilterException ife) {
            LOGGER.error((Object)("Failed to build defaultPointPlacement: " + ife));
        }
    }

    @Override
    public AnchorPoint getAnchorPoint() {
        return this.anchorPoint;
    }

    @Override
    public void setAnchorPoint(AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint == null ? new AnchorPointImpl() : anchorPoint;
    }

    @Override
    public Displacement getDisplacement() {
        return this.displacement;
    }

    @Override
    public void setDisplacement(Displacement displacement) {
        this.displacement = displacement == null ? new DisplacementImpl() : displacement;
    }

    @Override
    public Expression getRotation() {
        return this.rotation;
    }

    @Override
    public void setRotation(Expression rotation) {
        this.rotation = rotation;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        PointPlacementImpl clone = new PointPlacementImpl();
        if (this.anchorPoint != null) {
            clone.setAnchorPoint((AnchorPoint)((Cloneable)this.anchorPoint).clone());
        }
        clone.setAttributeRotation(this.getAttributeRotation());
        if (this.displacement != null) {
            clone.setDisplacement((Displacement)((Cloneable)this.displacement).clone());
        }
        clone.setRotation(this.getRotation());
        return clone;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PointPlacementImpl) {
            PointPlacementImpl other = (PointPlacementImpl)obj;
            return Utilities.equals((Object)this.anchorPoint, (Object)other.anchorPoint) && Utilities.equals((Object)this.displacement, (Object)other.displacement) && Utilities.equals((Object)this.rotation, (Object)other.rotation);
        }
        return false;
    }

    public int hashCode() {
        int PRIME = 37;
        int result = 17;
        if (this.anchorPoint != null) {
            result = result * 37 + this.anchorPoint.hashCode();
        }
        if (this.displacement != null) {
            result = result * 37 + this.displacement.hashCode();
        }
        if (this.rotation != null) {
            result = result * 37 + this.rotation.hashCode();
        }
        return result;
    }

    public Expression getAttributeRotation() {
        return this.attributeRotation;
    }

    public void setAttributeRotation(Expression attributeRotation) {
        this.attributeRotation = attributeRotation;
    }
}

