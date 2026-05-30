/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.Layerable;

public class LayerEvent {
    private Layerable layerable;
    private LayerEventType type;
    private Category category;
    private int layerableIndex;

    public LayerEvent(Layerable layerable, LayerEventType type, Category category, int layerIndex) {
        Assert.isTrue((category != null ? 1 : 0) != 0);
        Assert.isTrue((layerable != null ? 1 : 0) != 0);
        Assert.isTrue((type != null ? 1 : 0) != 0);
        this.layerable = layerable;
        this.type = type;
        this.category = category;
        this.layerableIndex = layerIndex;
    }

    public LayerEventType getType() {
        return this.type;
    }

    public Layerable getLayerable() {
        return this.layerable;
    }

    public Category getCategory() {
        return this.category;
    }

    public int getLayerableIndex() {
        return this.layerableIndex;
    }
}

