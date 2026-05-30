/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.DefaultExpression;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;

public class AttributeExpressionImpl
extends DefaultExpression
implements AttributeExpression {
    private static final Logger LOGGER = Logger.getLogger(AttributeExpressionImpl.class);
    protected String attPath;
    protected FeatureSchema schema = null;

    public void setSchema(FeatureSchema schema) {
        this.schema = schema;
    }

    public AttributeExpressionImpl() {
        this.schema = null;
        this.expressionType = (short)113;
    }

    public AttributeExpressionImpl(FeatureSchema schema) {
        this.schema = schema;
        this.expressionType = (short)113;
    }

    public AttributeExpressionImpl(FeatureSchema schema, String attPath) throws IllegalFilterException {
        this.schema = schema;
        this.expressionType = (short)113;
        this.setAttributePath(attPath);
    }

    @Override
    public void setAttributePath(String attPath) throws IllegalFilterException {
        LOGGER.debug((Object)("ExpressionAttribute - setAttributePath" + attPath));
        LOGGER.debug((Object)("schema: " + this.schema + "\n\nattribute: " + attPath));
        this.attPath = this.schema != null ? attPath : attPath;
    }

    @Override
    public String getAttributePath() {
        return this.attPath;
    }

    @Override
    public Object getValue(Feature feature) {
        if (feature != null) {
            return feature.getAttribute(this.attPath);
        }
        return null;
    }

    public String toString() {
        return this.attPath;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AttributeExpressionImpl)) {
            return false;
        }
        AttributeExpressionImpl expAttr = (AttributeExpressionImpl)obj;
        boolean isEqual = expAttr.getType() == this.expressionType;
        LOGGER.debug((Object)("expression type match:" + isEqual + "; in:" + expAttr.getType() + "; out:" + this.expressionType));
        isEqual = expAttr.attPath != null ? isEqual && expAttr.attPath.equals(this.attPath) : isEqual && this.attPath == null;
        LOGGER.debug((Object)("attribute match:" + isEqual + "; in:" + expAttr.getAttributePath() + "; out:" + this.attPath));
        isEqual = expAttr.schema != null ? isEqual && expAttr.schema.equals(this.schema) : isEqual && this.schema == null;
        LOGGER.debug((Object)("schema match:" + isEqual + "; in:" + expAttr.schema + "; out:" + this.schema));
        return isEqual;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + (this.attPath == null ? 0 : this.attPath.hashCode());
        result = 37 * result + (this.schema == null ? 0 : this.schema.hashCode());
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }
}

