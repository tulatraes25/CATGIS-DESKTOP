/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.MathExpression;

public class MathExpressionImpl
extends DefaultExpression
implements MathExpression {
    private Expression leftValue = null;
    private Expression rightValue = null;

    public MathExpressionImpl() {
    }

    public MathExpressionImpl(short expType) throws IllegalFilterException {
        if (!MathExpressionImpl.isMathExpression(expType)) {
            throw new IllegalFilterException("Attempted to add non-math expression to math expression.");
        }
        this.expressionType = expType;
    }

    @Override
    public void addLeftValue(Expression leftValue) throws IllegalFilterException {
        if (MathExpressionImpl.isGeometryExpression(leftValue.getType()) || leftValue.getType() == 103) {
            throw new IllegalFilterException("Attempted to add Geometry or String expression to math expression.");
        }
        this.leftValue = leftValue;
    }

    @Override
    public void addRightValue(Expression rightValue) throws IllegalFilterException {
        if (MathExpressionImpl.isGeometryExpression(rightValue.getType()) || rightValue.getType() == 103) {
            throw new IllegalFilterException("Attempted to add Geometry or String sub expression to math expression.");
        }
        this.rightValue = rightValue;
    }

    @Override
    public Expression getLeftValue() {
        return this.leftValue;
    }

    @Override
    public Expression getRightValue() {
        return this.rightValue;
    }

    @Override
    public short getType() {
        return this.expressionType;
    }

    @Override
    public Object getValue(Feature feature) throws IllegalArgumentException {
        if (this.leftValue == null || this.rightValue == null) {
            throw new IllegalArgumentException("Attempted read math expression with missing sub expressions.");
        }
        Number leftFeatureValue = (Number)this.leftValue.getValue(feature);
        Number rightFeatureValue = (Number)this.rightValue.getValue(feature);
        if (leftFeatureValue == null || rightFeatureValue == null) {
            return null;
        }
        double leftDouble = leftFeatureValue.doubleValue();
        double rightDouble = rightFeatureValue.doubleValue();
        if (this.expressionType == 105) {
            return new Double(leftDouble + rightDouble);
        }
        if (this.expressionType == 106) {
            return new Double(leftDouble - rightDouble);
        }
        if (this.expressionType == 107) {
            return new Double(leftDouble * rightDouble);
        }
        if (this.expressionType == 108) {
            return new Double(leftDouble / rightDouble);
        }
        throw new IllegalArgumentException("Attempted read math expression with invalid type (ie. Add, Subtract, etc.).");
    }

    public String toString() {
        String operation;
        switch (this.expressionType) {
            case 105: {
                operation = " + ";
                break;
            }
            case 106: {
                operation = " - ";
                break;
            }
            case 107: {
                operation = " * ";
                break;
            }
            case 108: {
                operation = " / ";
                break;
            }
            default: {
                operation = " ? ";
            }
        }
        return "(" + this.leftValue.toString() + operation + this.rightValue.toString() + ")";
    }

    public boolean equals(Object obj) {
        if (obj instanceof MathExpressionImpl) {
            MathExpression expMath = (MathExpression)obj;
            return expMath.getType() == this.expressionType && expMath.getLeftValue().equals(this.leftValue) && expMath.getRightValue().equals(this.rightValue);
        }
        return false;
    }

    public int hashCode() {
        int result = 23;
        result = 37 * result + this.expressionType;
        result = 37 * result + this.leftValue.hashCode();
        result = 37 * result + this.rightValue.hashCode();
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setType(short type) {
        this.expressionType = type;
    }
}

