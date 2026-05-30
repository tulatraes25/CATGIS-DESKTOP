/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.expr;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.saig.core.expr.AndExpr;
import org.saig.core.expr.BetweenExpr;
import org.saig.core.expr.CompareExpr;
import org.saig.core.expr.Expr;
import org.saig.core.expr.Exprs;
import org.saig.core.expr.FidsExpr;
import org.saig.core.expr.FunctionExpr;
import org.saig.core.expr.NotExpr;
import org.saig.core.expr.NullExpr;
import org.saig.core.expr.OrExpr;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.IllegalFilterException;

abstract class AbstractExpr
implements Expr {
    protected FilterFactory factory = FilterFactory.createFilterFactory();

    AbstractExpr() {
    }

    @Override
    public Expr eval() {
        return this;
    }

    @Override
    public Expr reduce(String bind) {
        return this;
    }

    @Override
    public Expr resolve(String bind, Feature feature) {
        return this;
    }

    @Override
    public Filter filter(final FeatureSchema schema) throws IOException {
        return new Filter(){

            @Override
            public boolean contains(Feature feature) {
                Expression expression;
                try {
                    expression = AbstractExpr.this.expression(schema);
                }
                catch (IOException e) {
                    return false;
                }
                Object value = expression.getValue(feature);
                return Exprs.truth(value);
            }

            @Override
            public Filter and(Filter filter) {
                try {
                    return AbstractExpr.this.factory.createLogicFilter(this, filter, (short)2);
                }
                catch (IllegalFilterException e) {
                    return null;
                }
            }

            @Override
            public Filter or(Filter filter) {
                try {
                    return AbstractExpr.this.factory.createLogicFilter(this, filter, (short)1);
                }
                catch (IllegalFilterException e) {
                    return null;
                }
            }

            @Override
            public Filter not() {
                try {
                    return AbstractExpr.this.factory.createLogicFilter(this, (short)3);
                }
                catch (IllegalFilterException e) {
                    return null;
                }
            }

            @Override
            public short getFilterType() {
                return 0;
            }

            @Override
            public void accept(FilterVisitor visitor) {
                try {
                    AbstractExpr.this.expression(schema).accept(visitor);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        };
    }

    @Override
    public Expression expression(final FeatureSchema schema) throws IOException {
        return new Expression(){

            @Override
            public short getType() {
                return 114;
            }

            @Override
            public Object getValue(Feature feature) {
                boolean contains;
                try {
                    Filter filter = AbstractExpr.this.filter(feature.getSchema());
                    contains = filter.contains(feature);
                }
                catch (IOException ignore) {
                    contains = false;
                }
                return contains ? Boolean.TRUE : Boolean.FALSE;
            }

            @Override
            public void accept(FilterVisitor visitor) {
                try {
                    AbstractExpr.this.filter(schema).accept(visitor);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        };
    }

    @Override
    public Expr bbox(Envelope bbox) {
        return this.and(Exprs.geom().disjoint(Exprs.literal(bbox)).not());
    }

    @Override
    public Expr fid(String featureID) {
        HashSet<String> set = new HashSet<String>();
        set.add(featureID);
        return this.and(new FidsExpr(set));
    }

    @Override
    public Expr fid(Set fids) {
        return this.and(new FidsExpr(fids));
    }

    @Override
    public Expr not() {
        return new NotExpr(this);
    }

    @Override
    public Expr and(Expr expr) {
        return new AndExpr(this, expr);
    }

    @Override
    public Expr or(Expr expr) {
        return new OrExpr(this, expr);
    }

    @Override
    public Expr eq(Expr expr) {
        return new CompareExpr(this, 14, expr);
    }

    @Override
    public Expr gt(Expr expr) {
        return new CompareExpr(this, 16, expr);
    }

    @Override
    public Expr gte(Expr expr) {
        return new CompareExpr(this, 18, expr);
    }

    @Override
    public Expr lt(Expr expr) {
        return new CompareExpr(this, 15, expr);
    }

    @Override
    public Expr lte(Expr expr) {
        return new CompareExpr(this, 17, expr);
    }

    @Override
    public Expr ne(Expr expr) {
        return new CompareExpr(this, 23, expr);
    }

    @Override
    public Expr between(Expr min, Expr max) {
        return new BetweenExpr(min, this, max);
    }

    @Override
    public Expr notNull() {
        return new NullExpr(this);
    }

    @Override
    public Expr fn(String name) {
        return new FunctionExpr(name, this);
    }

    @Override
    public Expr fn(String name, Expr expr) {
        return new FunctionExpr(name, this, expr);
    }

    @Override
    public Expr fn(String name, Expr[] expr) {
        Expr[] params = new Expr[expr.length + 1];
        params[0] = this;
        int i = 0;
        while (i < expr.length) {
            params[i + 1] = expr[i];
            ++i;
        }
        return new FunctionExpr(name, params);
    }
}

