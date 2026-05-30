/*
 * Decompiled with CFR 0.152.
 */
package com.pcauto.gui.table;

import java.util.EventObject;

public class EntityListEvent
extends EventObject {
    private static final long serialVersionUID = 1L;
    public static final int INSERTED = 0;
    public static final int DELETED = 1;
    public static final int MODIFIED = 2;
    int rowIndex = -1;
    int endIndex = -1;
    int eventType = -1;

    public EntityListEvent(Object source) {
        super(source);
    }

    public EntityListEvent(Object source, int index) {
        super(source);
        this.rowIndex = index;
        this.endIndex = index;
    }

    public EntityListEvent(Object source, int index, int event) {
        super(source);
        this.rowIndex = index;
        this.endIndex = index;
        this.eventType = event;
    }

    public EntityListEvent(Object source, int startIndex, int endIndex, int event) {
        super(source);
        this.rowIndex = startIndex;
        this.endIndex = endIndex;
        this.eventType = event;
    }

    public int getFirstEntity() {
        return this.rowIndex;
    }

    public int getLastEntity() {
        return this.endIndex;
    }

    public int getType() {
        return this.eventType;
    }
}

