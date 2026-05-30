/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.bookmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.saig.core.model.bookmark.IBookmark;
import org.saig.jump.lang.I18N;

public class BookmarkCategory {
    private String name;
    public static final BookmarkCategory DEFAULT_CATEGORY = new BookmarkCategory(I18N.getString("org.saig.core.model.bookmark.BookmarkCategory.no-category-assigned"));
    List<IBookmark> bookmarks;

    public BookmarkCategory(String name) {
        this.name = name;
        this.bookmarks = new ArrayList<IBookmark>();
    }

    public void addBookmark(IBookmark newBookmark) {
        this.bookmarks.add(newBookmark);
    }

    public void addBookmark(int index, IBookmark newBookmark) {
        this.bookmarks.add(index, newBookmark);
    }

    public void addBookmarks(Collection<IBookmark> newBookmarks) {
        this.bookmarks.addAll(newBookmarks);
    }

    public void clear() {
        this.bookmarks.clear();
    }

    public IBookmark getBookmark(String bookmarkName) {
        IBookmark result = null;
        Iterator<IBookmark> iterator = this.bookmarks.iterator();
        while (result == null && iterator.hasNext()) {
            IBookmark currentBookmark = iterator.next();
            if (!currentBookmark.getName().equals(bookmarkName)) continue;
            result = currentBookmark;
        }
        return result;
    }

    public Collection<IBookmark> getBookmarks() {
        return this.bookmarks;
    }

    public int size() {
        return this.bookmarks.size();
    }

    public boolean isEmpty() {
        return this.bookmarks.isEmpty();
    }

    public int indexOf(IBookmark bookmark) {
        return this.bookmarks.indexOf(bookmark);
    }

    public void removeBookmark(IBookmark bookmark) {
        this.bookmarks.remove(bookmark);
    }

    public void removeBookmarks(Collection<IBookmark> bookmarksToRemove) {
        this.bookmarks.removeAll(bookmarksToRemove);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return this.getName();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BookmarkCategory)) {
            return false;
        }
        BookmarkCategory category = (BookmarkCategory)obj;
        if (this.getName() == null && category.getName() == null) {
            return true;
        }
        if (this.getName() == null || category.getName() == null) {
            return false;
        }
        return this.getName().equals(category.getName());
    }
}

