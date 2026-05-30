/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import org.saig.core.expr.AbstractGeometryExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public class AttributeGeometryExpr
extends AbstractGeometryExpr {
    String attribute;

    public AttributeGeometryExpr() {
        this(null);
    }

    public AttributeGeometryExpr(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public Expression expression(FeatureSchema schema) {
        try {
            String path = this.attribute != null ? this.attribute : schema.getAttributeName(schema.getGeometryIndex());
            return this.factory.createAttributeExpression(schema, path);
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

