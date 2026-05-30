/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.bookmark;

public class BookmarkEventType {
    public static final BookmarkEventType ADDED = new BookmarkEventType("ADDED");
    public static final BookmarkEventType REMOVED = new BookmarkEventType("REMOVED");
    public static final BookmarkEventType METADATA_CHANGED = new BookmarkEventType("METADATA_CHANGED");
    private String name;

    private BookmarkEventType(String name) {
        this.setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

