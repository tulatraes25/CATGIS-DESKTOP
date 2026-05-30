/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import java.util.Collection;
import org.saig.core.styling.Style;

public class LayerSimbology {
    private Collection<com.vividsolutions.jump.workbench.ui.renderer.style.Style> jumpStyles;
    private Style modelStyle;
    private String name = "";
    private int geometryType = 0;

    public Collection<com.vividsolutions.jump.workbench.ui.renderer.style.Style> getJumpStyles() {
        return this.jumpStyles;
    }

    public void setJumpStyles(Collection<com.vividsolutions.jump.workbench.ui.renderer.style.Style> jumpStyles) {
        this.jumpStyles = jumpStyles;
    }

    public Style getModelStyle() {
        return this.modelStyle;
    }

    public void setModelStyle(Style modelStyle) {
        this.modelStyle = modelStyle;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGeometryType() {
        return this.geometryType;
    }

    public void setGeometryType(int geometryType) {
        this.geometryType = geometryType;
    }
}

