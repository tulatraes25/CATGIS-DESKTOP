/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 */
package es.kosmo.core.geometry.filters;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

public class ZValueFilter
implements CoordinateFilter {
    public static final double DEFAULT_Z_VALUE = -9999.0;
    protected boolean overwriteAll;
    protected boolean isNaN;
    protected double zValue;

    public ZValueFilter(boolean overwriteAll) {
        this.zValue = -9999.0;
        this.isNaN = Double.isNaN(this.zValue);
        this.overwriteAll = overwriteAll;
    }

    public ZValueFilter(double value, boolean overwriteAll) {
        this.zValue = value;
        this.overwriteAll = overwriteAll;
    }

    public void filter(Coordinate coord) {
        if (!this.overwriteAll) {
            if (Double.isNaN(coord.z) && !this.isNaN || !Double.isNaN(coord.z) && this.isNaN) {
                coord.z = this.zValue;
            }
        } else {
            coord.z = this.zValue;
        }
    }
}

