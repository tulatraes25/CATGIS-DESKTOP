/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.resources.Utilities
 */
package org.saig.core.styling;

import java.util.Arrays;
import org.geotools.resources.Utilities;
import org.saig.core.filter.Filter;
import org.saig.core.styling.Extent;
import org.saig.core.styling.FeatureTypeConstraint;
import org.saig.core.styling.StyleVisitor;

public class FeatureTypeConstraintImpl
implements FeatureTypeConstraint,
Cloneable {
    protected String featureTypeName;
    protected Filter filter;
    protected Extent[] extents;

    @Override
    public String getFeatureTypeName() {
        return this.featureTypeName;
    }

    @Override
    public void setFeatureTypeName(String name) {
        this.featureTypeName = name;
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Extent[] getExtents() {
        return this.extents;
    }

    @Override
    public void setExtents(Extent[] extents) {
        this.extents = extents;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

    public int hashCode() {
        int PRIME = 1000003;
        int result = 0;
        if (this.featureTypeName != null) {
            result = 1000003 * result + this.featureTypeName.hashCode();
        }
        if (this.filter != null) {
            result = 1000003 * result + this.filter.hashCode();
        }
        if (this.extents != null) {
            result = 1000003 * result + this.extents.hashCode();
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FeatureTypeConstraintImpl) {
            FeatureTypeConstraintImpl other = (FeatureTypeConstraintImpl)obj;
            return Utilities.equals((Object)this.featureTypeName, (Object)other.featureTypeName) && Utilities.equals((Object)this.filter, (Object)other.filter) && Arrays.equals(this.extents, other.extents);
        }
        return false;
    }
}

