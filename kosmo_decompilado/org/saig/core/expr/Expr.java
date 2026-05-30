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
import java.util.Set;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;

public interface Expr {
    public Expr eval();

    public Expr reduce(String var1);

    public Expr resolve(String var1, Feature var2);

    public Filter filter(FeatureSchema var1) throws IOException;

    public Expression expression(FeatureSchema var1) throws IOException;

    public Expr bbox(Envelope var1);

    public Expr fid(String var1);

    public Expr fid(Set var1);

    public Expr not();

    public Expr and(Expr var1);

    public Expr or(Expr var1);

    public Expr eq(Expr var1);

    public Expr gt(Expr var1);

    public Expr gte(Expr var1);

    public Expr lt(Expr var1);

    public Expr lte(Expr var1);

    public Expr ne(Expr var1);

    public Expr between(Expr var1, Expr var2);

    public Expr notNull();

    public Expr fn(String var1);

    public Expr fn(String var1, Expr var2);

    public Expr fn(String var1, Expr[] var2);
}

