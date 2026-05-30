/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.dao.coverage;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.dao.coverage.GridCoverage;
import org.saig.core.dao.coverage.ImageDataAccesor;
import org.saig.core.renderer.RendererParameterWrapper;

public class GridCoverageCollection
extends GridCoverage {
    protected Envelope collectionEnvelope = new Envelope();
    protected Map<String, GridCoverage> nameToCoverageMap = new HashMap<String, GridCoverage>();
    protected SpatialIndex<GridCoverage> spatialIndex = new Quadtree<GridCoverage>();

    public GridCoverageCollection() {
    }

    public GridCoverageCollection(ImageDataAccesor ida) {
        this();
        this.dataAccesor = ida;
        this.collectionEnvelope = this.dataAccesor.getEnvelope();
    }

    public Iterator<GridCoverage> getImageIterator() {
        return this.nameToCoverageMap.values().iterator();
    }

    public void addGridCoverage(GridCoverage imagen) {
        this.nameToCoverageMap.put(imagen.getDataAccesor().getImagePath(), imagen);
        this.spatialIndex.insert(imagen.getEnvelope(), imagen);
        this.collectionEnvelope.expandToInclude(imagen.getEnvelope());
    }

    public GridCoverage getGridCoverage(String imgName) {
        return this.nameToCoverageMap.get(imgName);
    }

    @Override
    public Envelope getEnvelope() {
        return this.collectionEnvelope;
    }

    @Override
    public void getImage(Graphics2D g2d, RendererParameterWrapper renderPS) {
        Envelope envelope = renderPS.getViewEnvelope();
        List<Coverage> intersectingCoverages = this.getImageSelection(envelope);
        for (GridCoverage gridCoverage : intersectingCoverages) {
            gridCoverage.getImage(g2d, renderPS);
        }
    }

    public List<Coverage> getImageSelection(Envelope envelope) {
        ArrayList<Coverage> solucion = new ArrayList<Coverage>();
        for (GridCoverage coverage : this.spatialIndex.query(envelope)) {
            if (!envelope.intersects(coverage.getEnvelope())) continue;
            solucion.add(coverage);
        }
        return solucion;
    }

    @Override
    public void close() {
        for (Coverage coverage : this.nameToCoverageMap.values()) {
            coverage.close();
        }
        this.spatialIndex = null;
        this.nameToCoverageMap.clear();
        this.nameToCoverageMap = null;
        this.collectionEnvelope = null;
        this.dataAccesor.close();
        this.dataAccesor = null;
    }
}

