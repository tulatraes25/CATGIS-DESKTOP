/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.bookmark;

public class BookmarkCategoryEventType {
    public static final BookmarkCategoryEventType ADDED = new BookmarkCategoryEventType("ADDED");
    public static final BookmarkCategoryEventType REMOVED = new BookmarkCategoryEventType("REMOVED");
    public static final BookmarkCategoryEventType METADATA_CHANGED = new BookmarkCategoryEventType("METADATA_CHANGED");
    private String name;

    private BookmarkCategoryEventType(String name) {
        this.setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

