/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.factory.Factory
 */
package org.saig.core.filter;

import org.geotools.factory.Factory;
import org.saig.core.filter.Expression;

public interface FunctionExpression
extends Expression,
Factory {
    public int getArgCount();

    @Override
    public short getType();

    public Expression[] getArgs();

    public String getName();

    public void setArgs(Expression[] var1);
}

