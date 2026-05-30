/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.fmap.spatialindex.IPersistentSpatialIndex
 *  com.iver.cit.gvsig.fmap.spatialindex.SpatialIndexException
 *  com.vividsolutions.jts.geom.Envelope
 *  org.geotools.index.TreeException
 *  org.geotools.index.quadtree.Node
 *  org.geotools.index.quadtree.QuadTree
 *  org.geotools.index.quadtree.StoreException
 *  org.geotools.index.quadtree.fs.FileSystemIndexStore
 */
package com.iver.cit.gvsig.fmap.spatialindex;

import com.iver.cit.gvsig.fmap.spatialindex.IPersistentSpatialIndex;
import com.iver.cit.gvsig.fmap.spatialindex.SpatialIndexException;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import org.geotools.index.TreeException;
import org.geotools.index.quadtree.Node;
import org.geotools.index.quadtree.QuadTree;
import org.geotools.index.quadtree.StoreException;
import org.geotools.index.quadtree.fs.FileSystemIndexStore;

public class QuadtreeGt2
implements IPersistentSpatialIndex {
    QuadTree quadtree;
    String quadtreeFile;
    final String qExt = ".qix";
    String byteOrder;
    Envelope bounds;
    int numRecs = 0;
    boolean inMemory = false;

    public QuadtreeGt2(String quadtreeFile, String byteOrder, Rectangle2D bounds, int numRecords, boolean overwrite) throws SpatialIndexException {
        this.quadtreeFile = String.valueOf(quadtreeFile) + ".qix";
        this.byteOrder = byteOrder;
        this.bounds = this.toJtsEnvelope(bounds);
        this.numRecs = numRecords;
        if (this.exists() && !overwrite) {
            this.load();
            return;
        }
        this.quadtree = new QuadTree(this.numRecs, this.bounds);
    }

    public boolean exists() {
        return new File(this.quadtreeFile).length() != 0L;
    }

    public void load() throws SpatialIndexException {
        try {
            this.openQuadTree();
        }
        catch (StoreException e) {
            throw new SpatialIndexException("", (Exception)((Object)e));
        }
    }

    public List query(Rectangle2D rect) {
        try {
            return (List)this.queryQuadTree(this.toJtsEnvelope(rect));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (TreeException e) {
            e.printStackTrace();
        }
        catch (StoreException e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public void insert(Rectangle2D rect, int index) {
        try {
            this.quadtree.insert(index, this.toJtsEnvelope(rect));
        }
        catch (StoreException e) {
            e.printStackTrace();
        }
    }

    public Envelope toJtsEnvelope(Rectangle2D rect) {
        double xmin = rect.getMinX();
        double xmax = rect.getMaxX();
        double ymin = rect.getMinY();
        double ymax = rect.getMaxY();
        return new Envelope(xmin, xmax, ymin, ymax);
    }

    public void delete(Rectangle2D rect, int index) {
        if (this.inMemory) {
            this.quadtree.delete(this.toJtsEnvelope(rect), index);
        }
    }

    void openQuadTree() throws StoreException {
        if (this.quadtree == null) {
            File file = new File(this.quadtreeFile);
            FileSystemIndexStore store = new FileSystemIndexStore(file);
            this.quadtree = store.load();
        }
    }

    void openQuadTreeInMemory() throws StoreException {
        if (this.quadtree == null) {
            File file = new File(this.quadtreeFile);
            FileSystemIndexStore store = new FileSystemIndexStore(file);
            QuadTree filequadtree = store.load();
            this.quadtree = new QuadTree(filequadtree.getNumShapes(), filequadtree.getMaxDepth(), filequadtree.getRoot().getBounds());
            Stack<Node> nodes = new Stack<Node>();
            nodes.push(filequadtree.getRoot());
            while (nodes.size() != 0) {
                Node node = (Node)nodes.pop();
                Envelope nodeEnv = node.getBounds();
                int[] shapeIds = node.getShapesId();
                int i = 0;
                while (i < shapeIds.length) {
                    this.quadtree.insert(shapeIds[i], nodeEnv);
                    ++i;
                }
                int numSubnodes = node.getNumSubNodes();
                int i2 = 0;
                while (i2 < numSubnodes) {
                    nodes.push(node.getSubNode(i2));
                    ++i2;
                }
            }
            filequadtree.close();
        }
    }

    private Collection queryQuadTree(Envelope bbox) throws IOException, TreeException, StoreException {
        List solution = null;
        solution = this.quadtree != null ? this.quadtree.query(bbox) : new ArrayList();
        return solution;
    }

    public void flush() {
        byte order = 0;
        if (this.byteOrder == null || this.byteOrder.equalsIgnoreCase("NM")) {
            order = 2;
        } else if (this.byteOrder.equalsIgnoreCase("NL")) {
            order = 1;
        }
        File file = new File(this.quadtreeFile);
        FileSystemIndexStore store = new FileSystemIndexStore(file, order);
        try {
            store.store(this.quadtree);
        }
        catch (StoreException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.quadtree.close();
        }
        catch (StoreException e) {
            e.printStackTrace();
        }
    }
}

