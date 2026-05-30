/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LogicFilterImpl;

public abstract class AbstractFilterImpl
extends AbstractFilter {
    @Override
    public Filter or(Filter filter) {
        try {
            return new LogicFilterImpl(this, filter, 1);
        }
        catch (IllegalFilterException ife) {
            return filter;
        }
    }

    @Override
    public Filter and(Filter filter) {
        try {
            return new LogicFilterImpl(this, filter, 2);
        }
        catch (IllegalFilterException ife) {
            return filter;
        }
    }

    @Override
    public Filter not() {
        try {
            return new LogicFilterImpl(this, 3);
        }
        catch (IllegalFilterException ife) {
            return this;
        }
    }
}

