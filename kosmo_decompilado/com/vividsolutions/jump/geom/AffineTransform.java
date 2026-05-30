/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.geom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;

public class AffineTransform
implements CoordinateFilter {
    private Coordinate transPt = null;

    public void translate(Coordinate p) {
        if (this.transPt == null) {
            this.transPt = new Coordinate(p);
        } else {
            this.transPt.x += p.x;
            this.transPt.y += p.y;
            this.transPt.z += p.z;
        }
    }

    public void apply(Geometry g) {
        g.apply((CoordinateFilter)this);
    }

    public void filter(Coordinate coord) {
        coord.x += this.transPt.x;
        coord.y += this.transPt.y;
        coord.z += this.transPt.z;
    }
}

