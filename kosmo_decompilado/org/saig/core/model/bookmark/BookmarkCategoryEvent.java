/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package org.saig.core.model.bookmark;

import com.vividsolutions.jts.util.Assert;
import org.saig.core.model.bookmark.BookmarkCategory;
import org.saig.core.model.bookmark.BookmarkCategoryEventType;

public class BookmarkCategoryEvent {
    private BookmarkCategoryEventType type;
    private BookmarkCategory category;
    private int bookmarkCategoryIndex;

    public BookmarkCategoryEvent(BookmarkCategory category, BookmarkCategoryEventType type, int bookmarkCategoryIndex) {
        Assert.isTrue((category != null ? 1 : 0) != 0);
        Assert.isTrue((type != null ? 1 : 0) != 0);
        this.type = type;
        this.category = category;
        this.bookmarkCategoryIndex = bookmarkCategoryIndex;
    }

    public BookmarkCategoryEventType getType() {
        return this.type;
    }

    public BookmarkCategory getCategory() {
        return this.category;
    }

    public int getBookmarkCategoryIndex() {
        return this.bookmarkCategoryIndex;
    }
}

