/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import java.util.ArrayList;
import java.util.List;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayer;
import org.saig.core.styling.StyledLayerDescriptor;

public class StyledLayerDescriptorImpl
implements StyledLayerDescriptor {
    private String name;
    private String title;
    private String abstractStr;
    private List<StyledLayer> layers = new ArrayList<StyledLayer>();

    @Override
    public StyledLayer[] getStyledLayers() {
        return this.layers.toArray(new StyledLayer[this.layers.size()]);
    }

    @Override
    public void setStyledLayers(StyledLayer[] styledLayers) {
        this.layers.clear();
        int i = 0;
        while (i < styledLayers.length) {
            this.addStyledLayer(styledLayers[i]);
            ++i;
        }
    }

    @Override
    public void addStyledLayer(StyledLayer layer) {
        this.layers.add(layer);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getAbstract() {
        return this.abstractStr;
    }

    @Override
    public void setAbstract(String abstractStr) {
        this.abstractStr = abstractStr;
    }

    @Override
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

