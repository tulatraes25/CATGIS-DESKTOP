/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import java.util.ArrayList;

public class ZoomHistory {
    private LayerViewPanel layerViewPanel;
    private ArrayList envelopes = new ArrayList();
    private int currentIndex = -1;
    private boolean adding = true;

    public ZoomHistory(LayerViewPanel layerViewPanel) {
        this.layerViewPanel = layerViewPanel;
    }

    public void setAdding(boolean adding) {
        this.adding = adding;
    }

    public void add(Envelope envelope) {
        if (!this.adding) {
            return;
        }
        this.envelopes.subList(this.currentIndex + 1, this.envelopes.size()).clear();
        this.envelopes.add(envelope);
        this.currentIndex = this.envelopes.size() - 1;
    }

    public Envelope next() {
        ++this.currentIndex;
        return this.getCurrentEnvelope();
    }

    public Envelope prev() {
        --this.currentIndex;
        return this.getCurrentEnvelope();
    }

    private Envelope getCurrentEnvelope() {
        return (Envelope)this.envelopes.get(this.currentIndex);
    }

    public boolean hasPrev() {
        return this.currentIndex > 0;
    }

    public boolean hasNext() {
        return this.currentIndex < this.envelopes.size() - 1;
    }

    public void dispose() {
        this.layerViewPanel = null;
    }
}

