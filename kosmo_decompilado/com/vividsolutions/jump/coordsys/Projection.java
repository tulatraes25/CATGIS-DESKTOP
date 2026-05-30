/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.coordsys;

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Spheroid;

public abstract class Projection {
    protected Spheroid currentSpheroid;

    public void setSpheroid(Spheroid s) {
        this.currentSpheroid = s;
    }

    public abstract Planar asPlanar(Geographic var1, Planar var2);

    public abstract Geographic asGeographic(Planar var1, Geographic var2);
}

