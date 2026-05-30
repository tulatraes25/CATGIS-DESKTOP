/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Projection;

public class LatLong
extends Projection {
    @Override
    public Geographic asGeographic(Planar p, Geographic g) {
        g.lon = p.x;
        g.lat = p.y;
        return g;
    }

    @Override
    public Planar asPlanar(Geographic g, Planar p) {
        p.x = g.lon;
        p.y = g.lat;
        return p;
    }
}

