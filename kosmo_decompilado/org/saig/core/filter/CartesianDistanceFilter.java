/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.filter.FilterVisitor;
import org.saig.core.filter.GeometryDistanceFilter;
import org.saig.core.filter.GeometryFilterImpl;
import org.saig.core.filter.IllegalFilterException;

public class CartesianDistanceFilter
extends GeometryFilterImpl
implements GeometryDistanceFilter,
Cloneable {
    private static final Logger LOGGER = Logger.getLogger(CartesianDistanceFilter.class);
    private double distance;

    public CartesianDistanceFilter(short filterType) throws IllegalFilterException {
        super(filterType);
        if (!CartesianDistanceFilter.isGeometryDistanceFilter(filterType)) {
            throw new IllegalFilterException("Attempted to create distance geometry filter with nondistance geometry type.");
        }
        this.filterType = filterType;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public boolean contains(Feature feature) {
        Geometry right = null;
        right = this.rightGeometry != null ? (Geometry)this.rightGeometry.getValue(feature) : feature.getGeometry();
        Geometry left = null;
        if (this.leftGeometry != null) {
            Object obj = this.leftGeometry.getValue(feature);
            left = (Geometry)obj;
        } else {
            left = feature.getGeometry();
        }
        if (this.filterType == 13) {
            return !left.isWithinDistance(right, this.distance);
        }
        if (this.filterType == 24) {
            return left.isWithinDistance(right, this.distance);
        }
        return true;
    }

    @Override
    public String toString() {
        String operator = null;
        if (this.filterType == 13) {
            operator = " beyond ";
        } else if (this.filterType == 24) {
            operator = " dwithin ";
        }
        String distStr = ", distance: " + this.distance;
        if (this.leftGeometry == null && this.rightGeometry == null) {
            return "[ null" + operator + "null" + distStr + " ]";
        }
        if (this.leftGeometry == null) {
            return "[ null" + operator + this.rightGeometry.toString() + distStr + " ]";
        }
        if (this.rightGeometry == null) {
            return "[ " + this.leftGeometry.toString() + operator + "null" + distStr + " ]";
        }
        return "[ " + this.leftGeometry.toString() + operator + this.rightGeometry.toString() + distStr + " ]";
    }

    @Override
    public boolean equals(Object oFilter) {
        boolean equals = super.equals(oFilter);
        if (!equals) {
            return false;
        }
        if (!(oFilter instanceof CartesianDistanceFilter)) {
            return false;
        }
        boolean equalDistance = ((CartesianDistanceFilter)oFilter).getDistance() == this.distance;
        return equalDistance;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long bits = Double.doubleToLongBits(this.distance);
        result = 37 * result + (int)(bits ^ bits >>> 32);
        return result;
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Object clone() {
        CartesianDistanceFilter clone = null;
        try {
            clone = new CartesianDistanceFilter(this.getFilterType());
            clone.leftGeometry = this.getLeftGeometry();
            clone.rightGeometry = this.getRightGeometry();
            clone.setDistance(this.getDistance());
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return clone;
    }
}

