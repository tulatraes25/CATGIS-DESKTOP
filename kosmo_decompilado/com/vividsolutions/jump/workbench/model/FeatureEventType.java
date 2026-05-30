/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

public class FeatureEventType {
    public static final FeatureEventType ADDED = new FeatureEventType("ADDED");
    public static final FeatureEventType DELETED = new FeatureEventType("DELETED");
    public static final FeatureEventType GEOMETRY_MODIFIED = new FeatureEventType("GEOMETRY MODIFIED");
    public static final FeatureEventType ATTRIBUTES_MODIFIED = new FeatureEventType("ATTRIBUTES MODIFIED");
    private String name;

    private FeatureEventType(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

