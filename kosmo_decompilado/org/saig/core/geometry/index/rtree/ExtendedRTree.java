/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.index.Data
 *  org.geotools.index.Lock
 *  org.geotools.index.LockTimeoutException
 *  org.geotools.index.TreeException
 *  org.geotools.index.rtree.Entry
 *  org.geotools.index.rtree.Node
 *  org.geotools.index.rtree.PageStore
 */
package org.saig.core.geometry.index.rtree;

import org.geotools.index.Data;
import org.geotools.index.Lock;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.rtree.Entry;
import org.geotools.index.rtree.Node;
import org.geotools.index.rtree.PageStore;
import org.geotools.index.rtree.RTree;
import org.saig.jump.lang.I18N;

public class ExtendedRTree
extends RTree {
    public ExtendedRTree(PageStore store) throws TreeException {
        super(store);
    }

    public void delete(Object key) throws TreeException, LockTimeoutException {
        this.checkOpen();
        Lock lock = this.store.getWriteLock();
        try {
            Node node = this.findLeaf(this.store.getRoot(), key);
            if (node == null) {
                throw new TreeException(String.valueOf(I18N.getString("org.saig.core.geometry.index.rtree.ExtendedRTree.no-node-found-with-the-supplied-data-{0}")) + key);
            }
            Entry e = null;
            int i = 0;
            while (i < node.getEntriesCount()) {
                e = node.getEntry(i);
                if (((Data)e.getData()).getValue(0).equals(key)) {
                    this.doDelete(lock, node, e);
                    break;
                }
                ++i;
            }
        }
        finally {
            this.store.releaseLock(lock);
        }
    }

    private Node findLeaf(Node node, Object key) throws TreeException {
        Node ret = null;
        Entry entry = null;
        int i = 0;
        while (i < node.getEntriesCount()) {
            entry = node.getEntry(i);
            if (node.isLeaf()) {
                if (((Data)entry.getData()).getValue(0).equals(key)) {
                    ret = node;
                }
            } else {
                ret = this.findLeaf(this.store.getNode(entry, node), key);
            }
            if (ret != null && ret.isLeaf()) break;
            ++i;
        }
        return ret;
    }
}

