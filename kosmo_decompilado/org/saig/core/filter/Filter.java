/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.FilterType;
import org.saig.core.filter.FilterVisitor;

public interface Filter
extends FilterType {
    public static final Filter NONE = new Filter(){

        @Override
        public final boolean contains(Feature f) {
            return true;
        }

        @Override
        public final Filter or(Filter f) {
            return this;
        }

        @Override
        public final Filter and(Filter f) {
            return f;
        }

        @Override
        public final Filter not() {
            return ALL;
        }

        @Override
        public final short getFilterType() {
            return 12345;
        }

        @Override
        public final void accept(FilterVisitor v) {
            v.visit(this);
        }

        public final String toString() {
            return "Filter.NONE";
        }
    };
    public static final Filter ALL = new Filter(){

        @Override
        public final boolean contains(Feature f) {
            return false;
        }

        @Override
        public final Filter or(Filter f) {
            return f;
        }

        @Override
        public final Filter and(Filter f) {
            return this;
        }

        @Override
        public final Filter not() {
            return NONE;
        }

        @Override
        public final short getFilterType() {
            return -12345;
        }

        @Override
        public final void accept(FilterVisitor v) {
            v.visit(this);
        }

        public final String toString() {
            return "Filter.ALL";
        }
    };

    public boolean contains(Feature var1);

    public Filter and(Filter var1);

    public Filter or(Filter var1);

    public Filter not();

    public short getFilterType();

    public void accept(FilterVisitor var1);
}

