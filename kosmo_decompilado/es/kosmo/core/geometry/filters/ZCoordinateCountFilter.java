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

public class ZCoordinateCountFilter
implements CoordinateFilter {
    protected int n = 0;

    public int getCount() {
        return this.n;
    }

    public void filter(Coordinate coord) {
        if (!Double.isNaN(coord.z)) {
            ++this.n;
        }
    }
}

