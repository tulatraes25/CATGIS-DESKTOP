/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.geom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CoordinateList<Coordinate>
extends ArrayList<Coordinate> {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean add(Coordinate coord) {
        Object last;
        if (this.size() >= 1 && (last = this.get(this.size() - 1)).equals(coord)) {
            return false;
        }
        return super.add(coord);
    }

    @Override
    public boolean addAll(Collection<? extends Coordinate> coll) {
        boolean isChanged = false;
        Iterator<Coordinate> i = coll.iterator();
        while (i.hasNext()) {
            boolean bl = isChanged = isChanged || this.add(i.next());
        }
        return isChanged;
    }

    public void closeRing() {
        if (this.size() > 0) {
            this.add((Coordinate)this.get(0));
        }
    }

    public Coordinate[] toCoordinateArray() {
        return this.toArray();
    }
}

