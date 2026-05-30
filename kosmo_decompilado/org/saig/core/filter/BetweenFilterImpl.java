/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.filter.BetweenFilter;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;

public class BetweenFilterImpl
extends CompareFilterImpl
implements BetweenFilter,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger(BetweenFilterImpl.class);
    protected Expression middleValue = null;

    public BetweenFilterImpl() throws IllegalFilterException {
        super((short)19);
    }

    @Override
    public void addMiddleValue(Expression middleValue) {
        this.middleValue = middleValue;
    }

    public void setMiddleValue(Expression middleValue) {
        this.middleValue = middleValue;
    }

    @Override
    public Expression getMiddleValue() {
        return this.middleValue;
    }

    @Override
    public boolean contains(Feature feature) {
        if (this.middleValue == null) {
            return false;
        }
        Object leftObj = this.leftValue.getValue(feature);
        Object rightObj = this.rightValue.getValue(feature);
        Object middleObj = this.middleValue.getValue(feature);
        if (leftObj instanceof Number && middleObj instanceof Number && rightObj instanceof Number) {
            double left = ((Number)leftObj).doubleValue();
            double right = ((Number)rightObj).doubleValue();
            double mid = ((Number)middleObj).doubleValue();
            return left <= mid && right >= mid;
        }
        if (leftObj.getClass() == middleObj.getClass() && rightObj.getClass() == middleObj.getClass() && leftObj instanceof Comparable) {
            return ((Comparable)leftObj).compareTo(middleObj) <= 0 && ((Comparable)middleObj).compareTo(rightObj) <= 0;
        }
        String mesg = "Supplied between values are either not compatible or not supported for comparison: " + leftObj + " <= " + middleObj + " <= " + rightObj;
        throw new IllegalArgumentException(mesg);
    }

    @Override
    public String toString() {
        return "between (" + this.leftValue.toString() + "," + this.middleValue.toString() + "," + this.rightValue.toString() + ")";
    }

    @Override
    public boolean equals(Object oFilter) {
        if (oFilter == null) {
            return false;
        }
        if (!(oFilter instanceof BetweenFilterImpl)) {
            return false;
        }
        BetweenFilterImpl bFilter = (BetweenFilterImpl)oFilter;
        return bFilter.getFilterType() == this.filterType && bFilter.getLeftValue().equals(this.leftValue) && bFilter.getMiddleValue().equals(this.middleValue) && bFilter.getRightValue().equals(this.rightValue);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.leftValue == null ? 0 : this.leftValue.hashCode());
        result = 37 * result + (this.middleValue == null ? 0 : this.middleValue.hashCode());
        result = 37 * result + (this.rightValue == null ? 0 : this.rightValue.hashCode());
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Object clone() {
        BetweenFilterImpl clone = null;
        try {
            clone = new BetweenFilterImpl();
            clone.setLeftValue(this.getLeftValue());
            clone.setRightValue(this.getRightValue());
            clone.setMiddleValue(this.getMiddleValue());
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return clone;
    }
}

