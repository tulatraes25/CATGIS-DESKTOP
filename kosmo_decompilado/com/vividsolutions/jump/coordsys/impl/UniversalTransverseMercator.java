/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Projection;
import com.vividsolutions.jump.coordsys.Spheroid;
import com.vividsolutions.jump.coordsys.impl.TransverseMercator;

public class UniversalTransverseMercator
extends Projection {
    private static final double SCALE_FACTOR = 0.9996;
    private static final double FALSE_EASTING = 500000.0;
    private static final double FALSE_NORTHING = 0.0;
    private TransverseMercator transverseMercator = new TransverseMercator();
    private int zone = -1;

    public void setParameters(int zone) {
        this.transverseMercator.setParameters(zone * 6 - 177);
        this.zone = zone;
    }

    @Override
    public void setSpheroid(Spheroid s) {
        this.transverseMercator.setSpheroid(s);
    }

    @Override
    public Geographic asGeographic(Planar p, Geographic q) {
        Assert.isTrue((this.zone != -1 ? 1 : 0) != 0, (String)"Call #setParameters first");
        p.x = (p.x - 500000.0) / 0.9996;
        p.y = (p.y - 0.0) / 0.9996;
        this.transverseMercator.asGeographic(p, q);
        return q;
    }

    @Override
    public Planar asPlanar(Geographic q0, Planar p) {
        Assert.isTrue((this.zone != -1 ? 1 : 0) != 0, (String)"Call #setParameters first");
        this.transverseMercator.asPlanar(q0, p);
        p.x = 0.9996 * p.x + 500000.0;
        p.y = 0.9996 * p.y + 0.0;
        return p;
    }
}

