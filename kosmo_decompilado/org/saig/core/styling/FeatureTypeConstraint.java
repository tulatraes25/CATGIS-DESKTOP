/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.filter.Filter;
import org.saig.core.styling.Extent;
import org.saig.core.styling.StyleVisitor;

public interface FeatureTypeConstraint {
    public String getFeatureTypeName();

    public void setFeatureTypeName(String var1);

    public Filter getFilter();

    public void setFilter(Filter var1);

    public Extent[] getExtents();

    public void setExtents(Extent[] var1);

    public void accept(StyleVisitor var1);
}

