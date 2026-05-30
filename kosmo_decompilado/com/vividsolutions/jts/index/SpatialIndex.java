/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jts.index;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import java.util.List;

public interface SpatialIndex<T> {
    public void insert(Envelope var1, T var2);

    public List<T> query(Envelope var1);

    public void query(Envelope var1, ItemVisitor<T> var2);

    public boolean remove(Envelope var1, T var2);
}

