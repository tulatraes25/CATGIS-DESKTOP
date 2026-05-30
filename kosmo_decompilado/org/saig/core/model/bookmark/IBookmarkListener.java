/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.bookmark;

import org.saig.core.model.bookmark.BookmarkCategoryEvent;
import org.saig.core.model.bookmark.BookmarkEvent;

public interface IBookmarkListener {
    public void bookmarkChanged(BookmarkEvent var1);

    public void bookmarkCategoryChanged(BookmarkCategoryEvent var1);
}

