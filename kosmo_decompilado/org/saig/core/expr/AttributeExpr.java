/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import org.saig.core.expr.AbstractExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;

public class AttributeExpr
extends AbstractExpr {
    String path;

    public AttributeExpr(String path) {
        this.path = path;
    }

    @Override
    public Expression expression(FeatureSchema schema) {
        try {
            return this.factory.createAttributeExpression(schema, this.path);
        }
        catch (IllegalFilterException e) {
            return null;
        }
    }
}

