/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.filter.function;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.FunctionExpression;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class Collection_MinFunction
extends FunctionExpressionImpl
implements FunctionExpression {
    private static final Logger LOGGER = Logger.getLogger(Collection_MinFunction.class);
    FeatureCollection fc = null;
    double min = 0.0;
    Expression expr;

    @Override
    public String getName() {
        return I18N.getString("org.saig.core.filter.function.Collection_MinFunction.collection-min");
    }

    @Override
    public int getArgCount() {
        return 1;
    }

    private void calculateMin() {
        FeatureIterator it = null;
        try {
            try {
                it = this.fc.iterator();
                this.min = Double.POSITIVE_INFINITY;
                while (it.hasNext()) {
                    Feature f = it.next();
                    double value = ((Number)this.expr.getValue(f)).doubleValue();
                    if (!(value < this.min)) continue;
                    this.min = value;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it != null) {
                    it.close();
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public void setArgs(Expression[] args) {
        this.expr = args[0];
    }

    @Override
    public Object getValue(Feature feature) {
        FeatureCollection coll = feature.getParent();
        if (!coll.equals(this.fc)) {
            this.fc = coll;
            this.calculateMin();
        }
        return new Double(this.min);
    }

    public void setExpression(Expression e) {
        this.expr = e;
        if (this.fc != null) {
            this.calculateMin();
        }
    }

    @Override
    public Expression[] getArgs() {
        Expression[] ret = new Expression[]{this.expr};
        return ret;
    }

    @Override
    public String toString() {
        return String.valueOf(I18N.getString("org.saig.core.filter.function.Collection_MinFunction.collection-min")) + "( " + this.expr + ")";
    }
}

