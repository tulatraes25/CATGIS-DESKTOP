/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 *  org.geotools.index.Data
 *  org.geotools.index.DataDefinition
 *  org.geotools.index.Lock
 *  org.geotools.index.LockTimeoutException
 *  org.geotools.index.TreeException
 *  org.geotools.index.rtree.Entry
 *  org.geotools.index.rtree.Node
 *  org.geotools.index.rtree.PageStore
 */
package org.geotools.index.rtree;

import com.vividsolutions.jts.geom.Envelope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.Lock;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.rtree.Entry;
import org.geotools.index.rtree.Node;
import org.geotools.index.rtree.PageStore;
import org.saig.jump.lang.I18N;

public class RTree {
    protected Logger logger = Logger.getLogger(RTree.class);
    protected PageStore store;

    public RTree(PageStore store) throws TreeException {
        this.store = store;
    }

    public Envelope getBounds() throws TreeException {
        this.checkOpen();
        Node root = this.store.getRoot();
        return root != null ? root.getBounds() : null;
    }

    protected Envelope getBoundsInternal(Envelope query, Node node) throws TreeException {
        Envelope result = null;
        Entry entry = null;
        int i = 0;
        while (i < node.getEntriesCount()) {
            entry = node.getEntry(i);
            if (entry.getBounds().intersects(query)) {
                if (node.isLeaf()) {
                    if (result == null) {
                        result = new Envelope(entry.getBounds());
                    } else {
                        result.expandToInclude(entry.getBounds());
                    }
                } else {
                    this.getBoundsInternal(query, this.store.getNode(entry, node));
                }
            }
            ++i;
        }
        return result;
    }

    public DataDefinition getDataDefinition() {
        return this.store.getDataDefinition();
    }

    public List<Object> search(Envelope query) throws TreeException, LockTimeoutException {
        Lock lock = this.store.getReadLock();
        List<Object> ret = null;
        try {
            ret = this.search(query, lock);
        }
        finally {
            this.store.releaseLock(lock);
        }
        return ret;
    }

    private List<Object> search(Envelope query, Lock lock) throws TreeException, LockTimeoutException {
        long start = System.currentTimeMillis();
        this.checkOpen();
        ArrayList<Object> matches = new ArrayList<Object>();
        Node root = this.store.getRoot();
        this.searchNode(query, root, matches);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug((Object)(String.valueOf(matches.size()) + I18N.getString("org.geotools.index.rtree.RTree.data-objects-retrieved-in") + (System.currentTimeMillis() - start) + "ms."));
        }
        return matches;
    }

    private void searchNode(Envelope query, Node node, ArrayList<Object> matches) throws TreeException {
        Entry entry = null;
        int i = 0;
        while (i < node.getEntriesCount()) {
            entry = node.getEntry(i);
            if (entry.getBounds().intersects(query)) {
                if (node.isLeaf()) {
                    matches.add(entry.getData());
                } else {
                    this.searchNode(query, this.store.getNode(entry, node), matches);
                }
            }
            ++i;
        }
    }

    public void insert(Envelope bounds, Data data) throws TreeException, LockTimeoutException {
        if (!data.isValid()) {
            throw new TreeException(I18N.getString("org.geotools.index.rtree.RTree.invalid-data-supplied"));
        }
        Lock lock = this.store.getWriteLock();
        try {
            this.insert(lock, new Entry(bounds, (Object)data));
        }
        finally {
            this.store.releaseLock(lock);
        }
    }

    private void insert(Lock lock, Entry entry) throws TreeException {
        this.checkOpen();
        Node leaf = this.chooseLeaf(this.store.getRoot(), entry);
        leaf.addEntry(entry);
        if (leaf.getEntriesCount() <= this.store.getMaxNodeEntries()) {
            leaf.save();
            this.adjustTree(leaf, null);
        } else {
            Node[] split = this.splitNode(leaf);
            this.adjustTree(split[0], split[1]);
        }
    }

    private Node chooseLeaf(Node node, Entry newEntry) throws TreeException {
        if (node.isLeaf()) {
            return node;
        }
        Collection entries = node.getEntries();
        Entry best = null;
        Object env = null;
        double lastArea = Double.POSITIVE_INFINITY;
        double currentArea = 0.0;
        double w = 0.0;
        double h = 0.0;
        double nw = 0.0;
        double nh = 0.0;
        Entry element2 = null;
        for (Entry element2 : entries) {
            currentArea = this.getAreaIncrease(element2.getBounds(), newEntry.getBounds());
            if (currentArea < lastArea) {
                lastArea = currentArea;
                best = element2;
                continue;
            }
            if (currentArea != lastArea || !(this.getEntryArea(best) > this.getEntryArea(element2))) continue;
            best = element2;
        }
        return this.chooseLeaf(this.store.getNode(best, node), newEntry);
    }

    private Node[] splitNode(Node node) throws TreeException {
        Collection entriesTmp = node.getEntries();
        Entry[] e = entriesTmp.toArray(new Entry[entriesTmp.size()]);
        Entry[] firsts = null;
        firsts = this.store.getSplitAlgorithm() == 1 ? this.quadraticPickSeeds(e) : this.linearPickSeeds(e);
        ArrayList<Entry> entries = new ArrayList<Entry>(e.length - 2);
        int i = 0;
        while (i < e.length) {
            if (!e[i].equals((Object)firsts[0]) && !e[i].equals((Object)firsts[1])) {
                entries.add(e[i]);
            }
            ++i;
        }
        node.clear();
        Node newNode = this.store.getEmptyNode(node.isLeaf());
        Node[] ret = new Node[]{node, newNode};
        ret[0].addEntry(firsts[0]);
        ret[1].addEntry(firsts[1]);
        Entry toAssign = null;
        double d1 = 0.0;
        double d2 = 0.0;
        int pointer = -1;
        while (entries.size() != 0) {
            if (ret[0].getEntriesCount() + entries.size() <= this.store.getMinNodeEntries()) {
                int i2 = 0;
                while (i2 < entries.size()) {
                    ret[0].addEntry((Entry)entries.get(i2));
                    ++i2;
                }
                break;
            }
            if (ret[1].getEntriesCount() + entries.size() <= this.store.getMinNodeEntries()) {
                int i3 = 0;
                while (i3 < entries.size()) {
                    ret[1].addEntry(entries.get(i3));
                    ++i3;
                }
                break;
            }
            toAssign = null;
            toAssign = this.store.getSplitAlgorithm() == 1 ? this.quadraticPickNext(ret, entries) : this.linearPickNext(ret, entries);
            d1 = this.getAreaIncrease(ret[0].getBounds(), toAssign.getBounds());
            pointer = d1 < (d2 = this.getAreaIncrease(ret[1].getBounds(), toAssign.getBounds())) ? 0 : (d1 > d2 ? 1 : ((d1 = this.getEnvelopeArea(ret[0].getBounds())) < (d2 = this.getEnvelopeArea(ret[1].getBounds())) ? 0 : (d1 > d2 ? 1 : (ret[0].getEntriesCount() < ret[1].getEntriesCount() ? 0 : 1))));
            ret[pointer].addEntry(toAssign);
            entries.remove(toAssign);
        }
        ret[0].save();
        ret[1].save();
        return ret;
    }

    private Entry[] quadraticPickSeeds(Entry[] entries) {
        Entry[] ret = new Entry[2];
        Envelope env = null;
        double actualD = 0.0;
        double choosedD = Double.NEGATIVE_INFINITY;
        int i = 0;
        while (i < entries.length - 1) {
            env = new Envelope(entries[i].getBounds());
            int j = i + 1;
            while (j < entries.length) {
                env.expandToInclude(entries[j].getBounds());
                actualD = this.getAreaDifference(env, entries[i], entries[j]);
                if (actualD > choosedD) {
                    choosedD = actualD;
                    ret[0] = entries[i];
                    ret[1] = entries[j];
                }
                ++j;
            }
            ++i;
        }
        return ret;
    }

    private Entry[] linearPickSeeds(Entry[] entries) {
        return null;
    }

    private Entry quadraticPickNext(Node[] nodes, ArrayList<Entry> entries) {
        Entry ret = null;
        double[] d = new double[]{0.0, 0.0};
        double diff = 0.0;
        double maxDiff = Double.NEGATIVE_INFINITY;
        Envelope e = null;
        int i = 0;
        while (i < entries.size()) {
            e = entries.get(i).getBounds();
            d[0] = this.getAreaIncrease(nodes[0].getBounds(), e);
            d[1] = this.getAreaIncrease(nodes[1].getBounds(), e);
            diff = Math.abs(d[0] - d[1]);
            if (diff > maxDiff) {
                maxDiff = diff;
                ret = entries.get(i);
            }
            ++i;
        }
        return ret;
    }

    private Entry linearPickNext(Node[] nodes, ArrayList<Entry> entries) {
        return null;
    }

    private void adjustTree(Node node1, Node node2) throws TreeException {
        Node n = node1;
        Node nn = node2;
        Node p = null;
        Entry e = null;
        while (true) {
            if (n.equals(this.store.getRoot())) {
                if (nn != null) {
                    Node newRoot = this.store.getEmptyNode(false);
                    e = this.store.createEntryPointingNode(n);
                    newRoot.addEntry(e);
                    e = this.store.createEntryPointingNode(nn);
                    newRoot.addEntry(e);
                    newRoot.save();
                    this.store.setRoot(newRoot);
                    n.setParent(newRoot);
                    nn.setParent(newRoot);
                    n.save();
                    nn.save();
                    break;
                }
                this.store.setRoot(n);
                break;
            }
            p = n.getParent();
            e = p.getEntry(n);
            e.setBounds(new Envelope(n.getBounds()));
            if (nn != null) {
                Entry e2 = this.store.createEntryPointingNode(nn);
                p.addEntry(e2);
                if (p.getEntriesCount() > this.store.getMaxNodeEntries()) {
                    Node[] split = this.splitNode(p);
                    n = split[0];
                    nn = split[1];
                    continue;
                }
                p.save();
                nn = null;
                n = p;
                continue;
            }
            p.save();
            n = p;
        }
    }

    public void delete(Envelope env) throws TreeException, LockTimeoutException {
        this.checkOpen();
        Lock lock = this.store.getWriteLock();
        try {
            Node node = this.findLeaf(this.store.getRoot(), env);
            if (node == null) {
                throw new TreeException(String.valueOf(I18N.getString("org.geotools.index.rtree.RTree.no-node-found-with-the-supplied-envelope")) + ": " + env);
            }
            Entry e = null;
            int i = 0;
            while (i < node.getEntriesCount()) {
                e = node.getEntry(i);
                if (e.getBounds().equals((Object)env)) {
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

    protected void doDelete(Lock lock, Node node, Entry entry) throws TreeException {
        node.removeEntry(entry);
        node.save();
        Collection<Node> toRemove = this.condenseTree(node);
        Node root = this.store.getRoot();
        if (root.getEntriesCount() == 1 && !root.isLeaf()) {
            root = this.store.getNode(root.getEntry(0), root);
            this.store.setRoot(root);
        }
        ArrayList<Entry> entries = new ArrayList<Entry>();
        Iterator<Node> iter = toRemove.iterator();
        while (iter.hasNext()) {
            this.free(iter.next(), entries);
        }
        Entry e2 = null;
        for (Entry e2 : entries) {
            this.insert(lock, e2);
        }
    }

    private void free(Node node, Collection<Entry> entries) throws TreeException {
        if (node.isLeaf()) {
            entries.addAll(node.getEntries());
        } else {
            int i = 0;
            while (i < node.getEntriesCount()) {
                this.free(this.store.getNode(node.getEntry(i), node), entries);
                ++i;
            }
        }
        this.store.free(node);
    }

    public void close() throws TreeException {
        this.store.close();
        this.store = null;
    }

    protected void checkOpen() throws TreeException {
        if (this.store == null) {
            throw new TreeException(I18N.getString("org.geotools.index.rtree.RTree.the-index-is-closed"));
        }
    }

    private Node findLeaf(Node node, Envelope envelope) throws TreeException {
        Node ret = null;
        Entry entry = null;
        int i = 0;
        while (i < node.getEntriesCount()) {
            entry = node.getEntry(i);
            if (node.isLeaf()) {
                if (entry.getBounds().equals((Object)envelope)) {
                    ret = node;
                }
            } else if (entry.getBounds().contains(envelope)) {
                ret = this.findLeaf(this.store.getNode(entry, node), envelope);
            }
            if (ret != null && ret.isLeaf()) break;
            ++i;
        }
        return ret;
    }

    private Collection<Node> condenseTree(Node node) throws TreeException {
        ArrayList<Node> removed = new ArrayList<Node>();
        if (node.equals(this.store.getRoot())) {
            return removed;
        }
        Node parentNode = node.getParent();
        Entry parentEntry = parentNode.getEntry(node);
        if (node.getEntriesCount() < this.store.getMinNodeEntries()) {
            removed.add(node);
            parentNode.removeEntry(parentEntry);
        } else {
            parentEntry.setBounds(node.getBounds());
        }
        parentNode.save();
        if (this.store.getRoot().equals(parentNode)) {
            this.store.setRoot(parentNode);
        }
        removed.addAll(this.condenseTree(parentNode));
        return removed;
    }

    private double getEntryArea(Entry e) {
        return this.getEnvelopeArea(e.getBounds());
    }

    private double getEnvelopeArea(Envelope env) {
        return env.getWidth() * env.getHeight();
    }

    private double getAreaIncrease(Envelope orig, Envelope add) {
        double ret = 0.0;
        Envelope env = new Envelope(orig);
        double w = env.getWidth();
        double h = env.getHeight();
        env.expandToInclude(add);
        double nw = env.getWidth();
        double nh = env.getHeight();
        ret += (nw - w) * nh;
        return ret += (nh - h) * w;
    }

    private double getAreaDifference(Envelope env, Entry e1, Entry e2) {
        return this.getEnvelopeArea(env) - this.getEntryArea(e1) - this.getEntryArea(e2);
    }

    public String toString() {
        Node root = this.store.getRoot();
        String ret = null;
        try {
            ret = this.dump(root, 0);
        }
        catch (TreeException e) {
            e.printStackTrace();
            return "";
        }
        return ret;
    }

    private String dump(Node node, int indent) throws TreeException {
        StringBuffer spc = new StringBuffer();
        int i = 0;
        while (i < indent) {
            spc.append("  ");
            ++i;
        }
        StringBuffer ret = new StringBuffer();
        ret.append(spc);
        ret.append("Node: ").append(node.getBounds());
        ret.append(System.getProperty("line.separator"));
        spc.append("  ");
        int i2 = 0;
        while (i2 < node.getEntriesCount()) {
            ret.append(spc).append(node.getEntry(i2)).append(System.getProperty("line.separator"));
            if (!node.isLeaf()) {
                ret.append(this.dump(this.store.getNode(node.getEntry(i2), node), indent + 1));
            }
            ++i2;
        }
        return ret.toString();
    }
}

