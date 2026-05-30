/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.expr;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.saig.core.expr.AbstractExpr;
import org.saig.core.expr.Expr;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.Filter;

public class FidsExpr
extends AbstractExpr {
    Set fids = new HashSet();

    public FidsExpr(Collection fidCollection) {
        this.fids.addAll(fidCollection);
    }

    public FidsExpr(String fid) {
        this.fids.add(fid);
    }

    @Override
    public Filter filter(FeatureSchema schema) {
        FidFilter filter = this.factory.createFidFilter();
        filter.addAllFids(this.fids);
        return filter;
    }

    @Override
    public Expr or(Expr expr) {
        if (expr instanceof FidsExpr) {
            return this.or((FidsExpr)expr);
        }
        return super.or(expr);
    }

    public Expr or(FidsExpr expr) {
        HashSet allFids = new HashSet();
        allFids.addAll(this.fids);
        allFids.addAll(expr.fids);
        return new FidsExpr(allFids);
    }
}

