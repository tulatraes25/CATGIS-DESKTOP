/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.index.quadtree.Root
 */
package com.vividsolutions.jts.index.quadtree;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ArrayListVisitor;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Root;
import java.util.ArrayList;
import java.util.List;

public class Quadtree<T>
implements SpatialIndex<T> {
    private Root root = new Root();
    private double minExtent = 1.0;

    public static Envelope ensureExtent(Envelope itemEnv, double minExtent) {
        double minx = itemEnv.getMinX();
        double maxx = itemEnv.getMaxX();
        double miny = itemEnv.getMinY();
        double maxy = itemEnv.getMaxY();
        if (minx != maxx && miny != maxy) {
            return itemEnv;
        }
        if (minx == maxx) {
            maxx = (minx -= minExtent / 2.0) + minExtent / 2.0;
        }
        if (miny == maxy) {
            maxy = (miny -= minExtent / 2.0) + minExtent / 2.0;
        }
        return new Envelope(minx, maxx, miny, maxy);
    }

    public int depth() {
        if (this.root != null) {
            return this.root.depth();
        }
        return 0;
    }

    public int size() {
        if (this.root != null) {
            return this.root.size();
        }
        return 0;
    }

    @Override
    public void insert(Envelope itemEnv, T item) {
        this.collectStats(itemEnv);
        Envelope insertEnv = Quadtree.ensureExtent(itemEnv, this.minExtent);
        this.root.insert(insertEnv, item);
    }

    @Override
    public boolean remove(Envelope itemEnv, T item) {
        Envelope posEnv = Quadtree.ensureExtent(itemEnv, this.minExtent);
        return this.root.remove(posEnv, item);
    }

    @Override
    public List<T> query(Envelope searchEnv) {
        ArrayListVisitor visitor = new ArrayListVisitor();
        this.query(searchEnv, visitor);
        return visitor.getItems();
    }

    @Override
    public void query(Envelope searchEnv, ItemVisitor<T> visitor) {
        this.root.visit(searchEnv, visitor);
    }

    public List<T> queryAll() {
        ArrayList foundItems = new ArrayList();
        this.root.addAllItems(foundItems);
        return foundItems;
    }

    private void collectStats(Envelope itemEnv) {
        double delY;
        double delX = itemEnv.getWidth();
        if (delX < this.minExtent && delX > 0.0) {
            this.minExtent = delX;
        }
        if ((delY = itemEnv.getHeight()) < this.minExtent && delY > 0.0) {
            this.minExtent = delY;
        }
    }
}

