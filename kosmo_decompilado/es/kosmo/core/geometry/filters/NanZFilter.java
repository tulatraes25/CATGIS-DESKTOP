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

public class NanZFilter
implements CoordinateFilter {
    public void filter(Coordinate c) {
        c.z = Double.NaN;
    }
}

