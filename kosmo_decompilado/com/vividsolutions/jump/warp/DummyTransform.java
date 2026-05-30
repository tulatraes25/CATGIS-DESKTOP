/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.warp.CoordinateTransform;

public class DummyTransform
extends CoordinateTransform {
    @Override
    public Coordinate transform(Coordinate c) {
        return c;
    }

    @Override
    public Geometry transform(Geometry oldGeometry) {
        return (Geometry)oldGeometry.clone();
    }
}

