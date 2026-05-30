/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;

public class AttributeExpressionImpl2
extends DefaultExpression
implements AttributeExpression {
    protected String attPath;

    public AttributeExpressionImpl2() {
        this.attPath = null;
        this.expressionType = (short)113;
    }

    public AttributeExpressionImpl2(String attributePath) {
        this.attPath = attributePath;
        this.expressionType = (short)113;
    }

    @Override
    public void setAttributePath(String attPath) throws IllegalFilterException {
        this.attPath = attPath;
    }

    @Override
    public String getAttributePath() {
        return this.attPath;
    }

    @Override
    public Object getValue(Feature feature) {
        return feature.getAttribute(this.attPath);
    }

    public String toString() {
        return this.attPath;
    }

    public boolean equals(Object obj) {
        boolean isEqual;
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AttributeExpressionImpl2)) {
            return false;
        }
        AttributeExpressionImpl2 expAttr = (AttributeExpressionImpl2)obj;
        boolean bl = isEqual = expAttr.getType() == this.expressionType;
        isEqual = expAttr.attPath != null ? isEqual && expAttr.attPath.equals(this.attPath) : isEqual && this.attPath == null;
        return isEqual;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.attPath == null ? 0 : this.attPath.hashCode());
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}

