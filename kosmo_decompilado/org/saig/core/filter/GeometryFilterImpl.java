/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import org.opengis.util.Cloneable;
import org.saig.core.filter.AbstractFilterImpl;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;

public class GeometryFilterImpl
extends AbstractFilterImpl
implements GeometryFilter,
Cloneable {
    protected Expression leftGeometry = null;
    protected Expression rightGeometry = null;

    public GeometryFilterImpl(short filterType) throws IllegalFilterException {
        if (!GeometryFilterImpl.isGeometryFilter(filterType)) {
            throw new IllegalFilterException("Attempted to create geometry filter with non-geometry type.");
        }
        this.filterType = filterType;
    }

    @Override
    public void addLeftGeometry(Expression leftGeometry) throws IllegalFilterException {
        if (!DefaultExpression.isGeometryExpression(leftGeometry.getType()) && !this.permissiveConstruction) {
            throw new IllegalFilterException("Attempted to add (left) non-geometry expression to geometry filter.");
        }
        this.leftGeometry = leftGeometry;
    }

    @Override
    public void addRightGeometry(Expression rightGeometry) throws IllegalFilterException {
        if (!DefaultExpression.isGeometryExpression(rightGeometry.getType()) && !this.permissiveConstruction) {
            throw new IllegalFilterException("Attempted to add (right) non-geometryexpression to geometry filter.");
        }
        this.rightGeometry = rightGeometry;
    }

    @Override
    public Expression getLeftGeometry() {
        return this.leftGeometry;
    }

    @Override
    public Expression getRightGeometry() {
        return this.rightGeometry;
    }

    @Override
    public boolean contains(Feature feature) {
        Geometry right = null;
        right = this.rightGeometry != null ? (Geometry)this.rightGeometry.getValue(feature) : feature.getGeometry();
        Geometry left = null;
        if (this.leftGeometry != null) {
            Object obj = this.leftGeometry.getValue(feature);
            left = (Geometry)obj;
        } else {
            left = feature.getGeometry();
        }
        if (left == null) {
            return false;
        }
        Envelope envRight = right.getEnvelopeInternal();
        Envelope envLeft = left.getEnvelopeInternal();
        if (this.filterType == 5) {
            if (envRight.equals((Object)envLeft)) {
                return left.equals(right);
            }
            return false;
        }
        if (this.filterType == 6) {
            if (envRight.intersects(envLeft)) {
                return left.disjoint(right);
            }
            return true;
        }
        if (this.filterType == 7) {
            if (envRight.intersects(envLeft)) {
                return left.intersects(right);
            }
            return false;
        }
        if (this.filterType == 9) {
            if (envRight.intersects(envLeft)) {
                return left.crosses(right);
            }
            return false;
        }
        if (this.filterType == 10) {
            if (envRight.contains(envLeft)) {
                return left.within(right);
            }
            return false;
        }
        if (this.filterType == 11) {
            if (envLeft.contains(envRight)) {
                return left.contains(right);
            }
            return false;
        }
        if (this.filterType == 12) {
            if (envLeft.intersects(envRight)) {
                return left.overlaps(right);
            }
            return false;
        }
        if (this.filterType == 8) {
            return left.touches(right);
        }
        if (this.filterType == 4) {
            if (envRight.contains(envLeft) || envLeft.contains(envRight)) {
                return true;
            }
            if (envRight.intersects(envLeft)) {
                return left.intersects(right);
            }
            return false;
        }
        return true;
    }

    public String toString() {
        String operator = null;
        if (this.filterType == 5) {
            operator = " equals ";
        } else if (this.filterType == 6) {
            operator = " disjoint ";
        } else if (this.filterType == 7) {
            operator = " intersects ";
        } else if (this.filterType == 9) {
            operator = " crosses ";
        } else if (this.filterType == 10) {
            operator = " within ";
        } else if (this.filterType == 11) {
            operator = " contains ";
        } else if (this.filterType == 12) {
            operator = " overlaps ";
        } else if (this.filterType == 13) {
            operator = " beyond ";
        } else if (this.filterType == 4) {
            operator = " bbox ";
        }
        if (this.leftGeometry == null && this.rightGeometry == null) {
            return "[ " + operator + "(" + "null" + "," + "null" + ") ]";
        }
        if (this.leftGeometry == null) {
            return "[ " + operator + "(" + "null" + "," + this.rightGeometry.toString() + ") ]";
        }
        if (this.rightGeometry == null) {
            return "[ " + operator + "(" + this.leftGeometry.toString() + ",null) ]";
        }
        return "[ " + operator + "(" + this.leftGeometry.toString() + "," + this.rightGeometry.toString() + ") ]";
    }

    public boolean equals(Object obj) {
        if (obj instanceof GeometryFilterImpl) {
            GeometryFilterImpl geomFilter = (GeometryFilterImpl)obj;
            boolean isEqual = true;
            isEqual = geomFilter.getFilterType() == this.filterType;
            LOGGER.debug((Object)("filter type match:" + isEqual + "; in:" + geomFilter.getFilterType() + "; out:" + this.filterType));
            isEqual = geomFilter.leftGeometry != null ? isEqual && geomFilter.leftGeometry.equals(this.leftGeometry) : isEqual && this.leftGeometry == null;
            LOGGER.debug((Object)("left geom match:" + isEqual + "; in:" + geomFilter.leftGeometry + "; out:" + this.leftGeometry));
            isEqual = geomFilter.rightGeometry != null ? isEqual && geomFilter.rightGeometry.equals(this.rightGeometry) : isEqual && this.rightGeometry == null;
            LOGGER.debug((Object)("right geom match:" + isEqual + "; in:" + geomFilter.rightGeometry + "; out:" + this.rightGeometry));
            return isEqual;
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + this.filterType;
        result = 37 * result + (this.leftGeometry == null ? 0 : this.leftGeometry.hashCode());
        result = 37 * result + (this.rightGeometry == null ? 0 : this.rightGeometry.hashCode());
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        GeometryFilterImpl clone = null;
        try {
            clone = new GeometryFilterImpl(this.getFilterType());
            clone.leftGeometry = this.getLeftGeometry();
            clone.rightGeometry = this.getRightGeometry();
        }
        catch (IllegalFilterException illegalFilterException) {
            // empty catch block
        }
        return clone;
    }
}

