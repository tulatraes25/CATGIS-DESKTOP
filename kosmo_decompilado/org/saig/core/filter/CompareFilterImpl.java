/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import java.text.ParseException;
import java.util.Date;
import org.opengis.util.Cloneable;
import org.saig.core.filter.AbstractFilterImpl;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.util.DateFormatManager;

public class CompareFilterImpl
extends AbstractFilterImpl
implements CompareFilter,
Cloneable {
    protected Expression leftValue = null;
    protected Expression rightValue = null;

    public CompareFilterImpl(short filterType) throws IllegalFilterException {
        if (!CompareFilterImpl.isCompareFilter(filterType)) {
            throw new IllegalFilterException("Attempted to create compare filter with non-compare type.");
        }
        this.filterType = filterType;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void addLeftValue(Expression leftValue) throws IllegalFilterException {
        if (CompareFilterImpl.isMathFilter(this.filterType)) {
            if (!DefaultExpression.isMathExpression(leftValue.getType()) && !this.permissiveConstruction) throw new IllegalFilterException("Attempted to add non-math expression to math filter.");
            this.leftValue = leftValue;
            return;
        } else {
            this.leftValue = leftValue;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void addRightValue(Expression rightValue) throws IllegalFilterException {
        if (CompareFilterImpl.isMathFilter(this.filterType)) {
            if (!DefaultExpression.isMathExpression(this.leftValue.getType()) && !this.permissiveConstruction) throw new IllegalFilterException("Attempted to add non-math expression to math filter.");
            this.rightValue = rightValue;
            return;
        } else {
            this.rightValue = rightValue;
        }
    }

    @Override
    public Expression getLeftValue() {
        return this.leftValue;
    }

    @Override
    public Expression getRightValue() {
        return this.rightValue;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean contains(Feature feature) {
        LOGGER.debug((Object)"CompareFilter contains");
        if (this.leftValue == null | this.rightValue == null) {
            LOGGER.debug((Object)"one value has not been set");
            return false;
        }
        try {
            Object leftObj = this.leftValue.getValue(feature);
            Object rightObj = this.rightValue.getValue(feature);
            if (!(leftObj instanceof Number) || !(rightObj instanceof Number)) {
                if (leftObj == null) return false;
                if (rightObj == null) {
                    return false;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug((Object)"is equals thingy");
                    LOGGER.debug((Object)("left value class: " + leftObj.getClass().toString()));
                    LOGGER.debug((Object)("right value class: " + rightObj.getClass().toString()));
                }
                if (leftObj.getClass() != rightObj.getClass()) {
                    if (Number.class.isAssignableFrom(leftObj.getClass()) && rightObj.getClass() == String.class) {
                        try {
                            rightObj = new Double(Double.parseDouble((String)rightObj));
                            leftObj = new Double(((Number)leftObj).doubleValue());
                        }
                        catch (Exception e) {
                            leftObj = leftObj.toString();
                            rightObj = rightObj.toString();
                        }
                    } else if (leftObj.getClass() == String.class && Number.class.isAssignableFrom(rightObj.getClass())) {
                        try {
                            leftObj = new Double(Double.parseDouble((String)leftObj));
                            rightObj = new Double(((Number)rightObj).doubleValue());
                        }
                        catch (Exception e) {
                            leftObj = leftObj.toString();
                            rightObj = rightObj.toString();
                        }
                    } else if (Date.class.isAssignableFrom(leftObj.getClass()) && Date.class.isAssignableFrom(rightObj.getClass())) {
                        try {
                            leftObj = DateFormatManager.getDateTimeFormat().parse(DateFormatManager.getDateTimeFormat().format(leftObj));
                        }
                        catch (ParseException e) {
                            leftObj = leftObj.toString();
                        }
                        try {
                            rightObj = DateFormatManager.getDateTimeFormat().parse(DateFormatManager.getDateTimeFormat().format(rightObj));
                        }
                        catch (ParseException e) {
                            rightObj = rightObj.toString();
                        }
                    } else if (Date.class.isAssignableFrom(leftObj.getClass())) {
                        try {
                            leftObj = DateFormatManager.getDateTimeFormat().parse(DateFormatManager.getDateTimeFormat().format(leftObj));
                        }
                        catch (ParseException e) {
                            leftObj = leftObj.toString();
                        }
                        try {
                            rightObj = DateFormatManager.getDateTimeFormat().parse(rightObj.toString());
                        }
                        catch (ParseException e) {
                            try {
                                rightObj = DateFormatManager.getDateFormat().parse(rightObj.toString());
                            }
                            catch (ParseException pe) {
                                rightObj = rightObj.toString();
                            }
                        }
                    } else if (Date.class.isAssignableFrom(rightObj.getClass())) {
                        try {
                            leftObj = DateFormatManager.getDateTimeFormat().parse(leftObj.toString());
                        }
                        catch (ParseException e) {
                            try {
                                leftObj = DateFormatManager.getDateFormat().parse(leftObj.toString());
                            }
                            catch (ParseException pe) {
                                leftObj = leftObj.toString();
                            }
                        }
                        try {
                            rightObj = DateFormatManager.getDateTimeFormat().parse(DateFormatManager.getDateTimeFormat().format(rightObj));
                        }
                        catch (ParseException e) {
                            rightObj = rightObj.toString();
                        }
                    } else {
                        leftObj = leftObj.toString();
                        rightObj = rightObj.toString();
                    }
                }
                if (leftObj.getClass() == String.class && rightObj.getClass() == String.class) {
                    leftObj = leftObj.toString().trim().toLowerCase();
                    rightObj = rightObj.toString().trim().toLowerCase();
                }
                if (this.filterType == 14) {
                    return leftObj.equals(rightObj);
                }
                if (this.filterType == 23) {
                    if (!leftObj.equals(rightObj)) return true;
                    return false;
                }
                if (leftObj.getClass() != rightObj.getClass()) throw new IllegalArgumentException();
                if (!(leftObj instanceof Comparable)) throw new IllegalArgumentException();
                Comparable leftComp = (Comparable)leftObj;
                Comparable rightComp = (Comparable)rightObj;
                int comparison = leftComp.compareTo(rightComp);
                if (this.filterType == 15) {
                    if (comparison >= 0) return false;
                    return true;
                }
                if (this.filterType == 16) {
                    if (comparison <= 0) return false;
                    return true;
                }
                if (this.filterType == 17) {
                    if (comparison > 0) return false;
                    return true;
                }
                if (this.filterType == 18) {
                    if (comparison < 0) return false;
                    return true;
                }
            }
            double leftResult = ((Number)leftObj).doubleValue();
            double rightResult = ((Number)rightObj).doubleValue();
            if (this.filterType == 14) {
                if (leftResult != rightResult) return false;
                return true;
            }
            if (this.filterType == 23) {
                if (leftResult == rightResult) return false;
                return true;
            }
            if (this.filterType == 15) {
                if (!(leftResult < rightResult)) return false;
                return true;
            }
            if (this.filterType == 16) {
                if (!(leftResult > rightResult)) return false;
                return true;
            }
            if (this.filterType == 17) {
                if (!(leftResult <= rightResult)) return false;
                return true;
            }
            if (this.filterType != 18) throw new IllegalArgumentException();
            if (!(leftResult >= rightResult)) return false;
            return true;
        }
        catch (IllegalArgumentException iae) {
            return false;
        }
    }

    public String toString() {
        String operator = null;
        if (this.filterType == 14) {
            operator = " = ";
        }
        if (this.filterType == 15) {
            operator = " < ";
        }
        if (this.filterType == 16) {
            operator = " > ";
        }
        if (this.filterType == 17) {
            operator = " <= ";
        }
        if (this.filterType == 18) {
            operator = " >= ";
        }
        if (this.filterType == 23) {
            operator = " != ";
        }
        return "[ " + this.leftValue + operator + this.rightValue + " ]";
    }

    public boolean equals(Object obj) {
        if (obj instanceof CompareFilter) {
            CompareFilter cFilter = (CompareFilter)obj;
            return this.filterType == cFilter.getFilterType() && (this.leftValue == cFilter.getLeftValue() || this.leftValue != null && this.leftValue.equals(cFilter.getLeftValue())) && (this.rightValue == cFilter.getRightValue() || this.rightValue != null && this.rightValue.equals(cFilter.getRightValue()));
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + this.filterType;
        result = 37 * result + (this.leftValue == null ? 0 : this.leftValue.hashCode());
        result = 37 * result + (this.rightValue == null ? 0 : this.rightValue.hashCode());
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public void setLeftValue(Expression leftValue) {
        this.leftValue = leftValue;
    }

    public void setRightValue(Expression rightValue) {
        this.rightValue = rightValue;
    }

    public Object clone() {
        CompareFilterImpl clone = null;
        try {
            clone = new CompareFilterImpl(this.getFilterType());
            clone.setLeftValue(this.getLeftValue());
            clone.setRightValue(this.getRightValue());
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return clone;
    }
}

