/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

public final class LayerEventType {
    public static final LayerEventType ADDED = new LayerEventType("ADDED");
    public static final LayerEventType REMOVED = new LayerEventType("REMOVED");
    public static final LayerEventType METADATA_CHANGED = new LayerEventType("METADATA_CHANGED");
    public static final LayerEventType APPEARANCE_CHANGED = new LayerEventType("APPEARANCE_CHANGED");
    public static final LayerEventType VISIBILITY_CHANGED = new LayerEventType("VISIBILITY_CHANGED");
    public static final LayerEventType COMMITED = new LayerEventType("COMMITED");
    private String name;

    private LayerEventType(String name) {
        this.setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

