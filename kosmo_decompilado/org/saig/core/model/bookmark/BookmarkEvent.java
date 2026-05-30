/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package org.saig.core.model.bookmark;

import com.vividsolutions.jts.util.Assert;
import org.saig.core.model.bookmark.BookmarkCategory;
import org.saig.core.model.bookmark.BookmarkEventType;
import org.saig.core.model.bookmark.IBookmark;

public class BookmarkEvent {
    private IBookmark bookmark;
    private BookmarkEventType type;
    private BookmarkCategory category;
    private int bookmarkIndex;

    public BookmarkEvent(IBookmark bookmark, BookmarkEventType type, BookmarkCategory category, int bookmarkIndex) {
        Assert.isTrue((category != null ? 1 : 0) != 0);
        Assert.isTrue((bookmark != null ? 1 : 0) != 0);
        Assert.isTrue((type != null ? 1 : 0) != 0);
        this.bookmark = bookmark;
        this.type = type;
        this.category = category;
        this.bookmarkIndex = bookmarkIndex;
    }

    public BookmarkEventType getType() {
        return this.type;
    }

    public IBookmark getBookmark() {
        return this.bookmark;
    }

    public BookmarkCategory getCategory() {
        return this.category;
    }

    public int getBookmarkIndex() {
        return this.bookmarkIndex;
    }
}

