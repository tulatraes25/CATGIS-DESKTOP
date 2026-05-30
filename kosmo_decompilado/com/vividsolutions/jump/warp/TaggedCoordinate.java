/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package com.vividsolutions.jump.warp;

import com.vividsolutions.jts.geom.Coordinate;

public class TaggedCoordinate
extends Coordinate {
    private static final long serialVersionUID = 1L;
    private Coordinate tag;

    public TaggedCoordinate(Coordinate c, Coordinate tag) {
        super(c);
        this.tag = tag;
    }

    public Coordinate getTag() {
        return this.tag;
    }
}

