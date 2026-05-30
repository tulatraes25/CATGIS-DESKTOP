/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import org.opengis.util.Cloneable;
import org.saig.core.filter.AbstractFilterImpl;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.NullFilter;

public class NullFilterImpl
extends AbstractFilterImpl
implements NullFilter,
Cloneable {
    private Expression nullCheck = null;

    public NullFilterImpl() {
        this.filterType = (short)21;
    }

    @Override
    public void setNullCheckValue(Expression nullCheck) throws IllegalFilterException {
        if (!(nullCheck instanceof AttributeExpression)) {
            throw new IllegalFilterException("Attempted to add non-attribute expression to a null filter.");
        }
        this.nullCheck = nullCheck;
    }

    @Override
    public Expression getNullCheckValue() {
        return this.nullCheck;
    }

    @Override
    public boolean contains(Feature feature) {
        if (this.nullCheck == null) {
            return false;
        }
        Object value = this.nullCheck.getValue(feature);
        if (value != null && value instanceof Geometry) {
            return ((Geometry)value).isEmpty();
        }
        return value == null;
    }

    public String toString() {
        return "[ " + this.nullCheck.toString() + " is null ]";
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NullFilterImpl)) {
            return false;
        }
        NullFilterImpl nullFilter = (NullFilterImpl)obj;
        return nullFilter.getFilterType() == this.filterType && nullFilter.getNullCheckValue().equals(this.nullCheck);
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.nullCheck == null ? 0 : this.nullCheck.hashCode());
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        NullFilterImpl clone = new NullFilterImpl();
        clone.nullCheck = this.getNullCheckValue();
        return clone;
    }
}

