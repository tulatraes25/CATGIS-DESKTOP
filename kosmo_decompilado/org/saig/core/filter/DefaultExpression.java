/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;

public abstract class DefaultExpression
implements Expression {
    protected short expressionType;
    protected boolean permissiveConstruction;

    @Override
    public short getType() {
        return this.expressionType;
    }

    @Override
    public Object getValue(Feature feature) {
        return new Object();
    }

    protected static boolean isAttributeExpression(short expressionType) {
        return expressionType == 109 || expressionType == 110 || expressionType == 111;
    }

    protected static boolean isMathExpression(short expressionType) {
        return expressionType == 105 || expressionType == 106 || expressionType == 107 || expressionType == 108;
    }

    protected static boolean isLiteralExpression(short expressionType) {
        return expressionType == 104 || expressionType == 101 || expressionType == 102 || expressionType == 103;
    }

    protected static boolean isGeometryExpression(short expressionType) {
        return expressionType == 112 || expressionType == 104;
    }

    protected static boolean isExpression(short expressionType) {
        return DefaultExpression.isMathExpression(expressionType) || DefaultExpression.isAttributeExpression(expressionType) || DefaultExpression.isLiteralExpression(expressionType) || DefaultExpression.isFunctionExpression(expressionType);
    }

    protected static boolean isFunctionExpression(short expressionType) {
        return expressionType == 114;
    }
}

