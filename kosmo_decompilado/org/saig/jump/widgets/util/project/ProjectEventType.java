/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.project;

public class ProjectEventType {
    public static final ProjectEventType OPENED = new ProjectEventType("OPENED");
    public static final ProjectEventType SAVED = new ProjectEventType("SAVED");
    public static final ProjectEventType SAVED_AS = new ProjectEventType("SAVED_AS");
    public static final ProjectEventType CLOSED = new ProjectEventType("CLOSED");
    public static final ProjectEventType BEFORE_SAVE = new ProjectEventType("BEFORE_SAVE");
    public static final ProjectEventType CATALOG_LOADED = new ProjectEventType("CATALOG_LOADED");
    private String name;

    private ProjectEventType(String name) {
        this.setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

