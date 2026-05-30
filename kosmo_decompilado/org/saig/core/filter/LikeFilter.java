/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;

public interface LikeFilter
extends Filter {
    public void setPattern(String var1, String var2, String var3, String var4);

    public String getWildcardMulti();

    public String getEscape();

    public void setPattern(Expression var1, String var2, String var3, String var4);

    public String getPattern();

    public void setValue(Expression var1) throws IllegalFilterException;

    public Expression getValue();

    public String getWildcardSingle();

    @Override
    public boolean contains(Feature var1);
}

