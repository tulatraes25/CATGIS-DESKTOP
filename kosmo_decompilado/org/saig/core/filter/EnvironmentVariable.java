/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.Expression;

public interface EnvironmentVariable
extends Expression {
    @Override
    public Object getValue(Feature var1);
}

