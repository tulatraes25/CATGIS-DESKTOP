/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.IllegalFilterException;

public interface GeometryDistanceFilter
extends GeometryFilter {
    public boolean equals(Object var1);

    public void setDistance(double var1) throws IllegalFilterException;

    @Override
    public boolean contains(Feature var1);

    public double getDistance();
}

