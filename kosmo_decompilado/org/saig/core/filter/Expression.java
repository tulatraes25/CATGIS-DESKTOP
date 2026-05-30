/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.ExpressionType;
import org.saig.core.filter.FilterVisitor;

public interface Expression
extends ExpressionType {
    public short getType();

    public Object getValue(Feature var1);

    public void accept(FilterVisitor var1);
}

