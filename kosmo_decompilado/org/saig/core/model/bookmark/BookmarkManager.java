/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.bookmark;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.model.bookmark.BasicBookmark;
import org.saig.core.model.bookmark.BookmarkCategory;
import org.saig.core.model.bookmark.BookmarkCategoryEvent;
import org.saig.core.model.bookmark.BookmarkCategoryEventType;
import org.saig.core.model.bookmark.BookmarkEvent;
import org.saig.core.model.bookmark.BookmarkEventType;
import org.saig.core.model.bookmark.BookmarkXMLPersistence;
import org.saig.core.model.bookmark.IBookmark;
import org.saig.core.model.bookmark.IBookmarkListener;
import org.saig.core.model.bookmark.TemporalBookmark;
import org.saig.jump.lang.I18N;

public class BookmarkManager {
    private static final Logger LOGGER = Logger.getLogger(BookmarkManager.class);
    List<BookmarkCategory> categories = new ArrayList<BookmarkCategory>();
    private static BookmarkManager instance = new BookmarkManager();
    private List<IBookmarkListener> bookmarkListeners = new ArrayList<IBookmarkListener>();

    private BookmarkManager() {
    }

    public static BookmarkManager getInstance() {
        return instance;
    }

    public IBookmark addBookmark(String categoryName) {
        BasicBookmark newBookmark = null;
        BookmarkCategory category = null;
        Iterator<BookmarkCategory> iterator = this.categories.iterator();
        while (category == null && iterator.hasNext()) {
            BookmarkCategory currentCategory = iterator.next();
            if (!currentCategory.getName().equals(categoryName)) continue;
            newBookmark = new BasicBookmark(this.generateBookmarkName(BasicBookmark.DEFAULT_BOOKMARK_NAME, currentCategory), null, null);
            currentCategory.addBookmark(newBookmark);
            category = currentCategory;
        }
        if (category == null) {
            category = this.addCategory(categoryName);
            newBookmark = new BasicBookmark(this.generateBookmarkName(BasicBookmark.DEFAULT_BOOKMARK_NAME, category), null, null);
            category.addBookmark(newBookmark);
        }
        if (category != null) {
            this.fireBookmarkChanged(new BookmarkEvent(newBookmark, BookmarkEventType.ADDED, category, category.indexOf(newBookmark)));
        }
        return newBookmark;
    }

    public IBookmark addTemporalBookmark(String categoryName) {
        TemporalBookmark newBookmark = null;
        BookmarkCategory category = null;
        Iterator<BookmarkCategory> iterator = this.categories.iterator();
        while (category == null && iterator.hasNext()) {
            BookmarkCategory currentCategory = iterator.next();
            if (!currentCategory.getName().equals(categoryName)) continue;
            newBookmark = new TemporalBookmark(this.generateBookmarkName(BasicBookmark.DEFAULT_BOOKMARK_NAME, currentCategory), null, null, new Timestamp(System.currentTimeMillis()));
            currentCategory.addBookmark(newBookmark);
            category = currentCategory;
        }
        if (category == null) {
            category = this.addCategory(categoryName);
            newBookmark = new TemporalBookmark(this.generateBookmarkName(BasicBookmark.DEFAULT_BOOKMARK_NAME, category), null, null, new Timestamp(System.currentTimeMillis()));
            category.addBookmark(newBookmark);
        }
        if (category != null) {
            this.fireBookmarkChanged(new BookmarkEvent(newBookmark, BookmarkEventType.ADDED, category, category.indexOf(newBookmark)));
        }
        return newBookmark;
    }

    private String generateBookmarkName(String name, BookmarkCategory currentCategory) {
        int cont = 0;
        String result = name;
        while (currentCategory.getBookmark(result) != null) {
            result = String.valueOf(name) + " - " + ++cont;
        }
        return result;
    }

    public void addBookmark(String categoryName, IBookmark newBookmark) {
        for (BookmarkCategory currentCategory : this.categories) {
            if (!currentCategory.getName().equals(categoryName)) continue;
            currentCategory.addBookmark(newBookmark);
        }
    }

    public void addBookmark(BookmarkCategory category, IBookmark newBookmark, int index) {
        if (category != null) {
            category.addBookmark(index, newBookmark);
            this.fireBookmarkChanged(new BookmarkEvent(newBookmark, BookmarkEventType.ADDED, category, index));
        }
    }

    public void addBookmarks(String categoryName, Collection<IBookmark> newBookmarks) {
        for (BookmarkCategory currentCategory : this.categories) {
            if (!currentCategory.getName().equals(categoryName)) continue;
            currentCategory.addBookmarks(newBookmarks);
        }
    }

    public BookmarkCategory addCategory(String categoryName) {
        if (categoryName == null) {
            categoryName = BookmarkCategory.DEFAULT_CATEGORY.getName();
        }
        BookmarkCategory category = new BookmarkCategory(categoryName);
        int cont = 0;
        while (this.categories.contains(category)) {
            category.setName(String.valueOf(categoryName) + " - " + ++cont);
        }
        this.categories.add(category);
        this.fireBookmarkCategoryChanged(new BookmarkCategoryEvent(category, BookmarkCategoryEventType.ADDED, this.categories.indexOf(category)));
        return category;
    }

    public void addCategory(BookmarkCategory category, int index) {
        this.categories.add(index, category);
        this.fireBookmarkCategoryChanged(new BookmarkCategoryEvent(category, BookmarkCategoryEventType.ADDED, index));
    }

    public void clearBookmarks() {
        for (BookmarkCategory currentCategory : this.categories) {
            currentCategory.clear();
        }
    }

    public Collection<IBookmark> getBookmarks(String categoryName) {
        for (BookmarkCategory currentCategory : this.categories) {
            if (!currentCategory.getName().equals(categoryName)) continue;
            return currentCategory.getBookmarks();
        }
        return new ArrayList<IBookmark>();
    }

    public Collection<BookmarkCategory> getCategories() {
        return this.categories;
    }

    public BookmarkCategory getCategory(String categoryName) {
        for (BookmarkCategory currentCategory : this.categories) {
            if (!currentCategory.getName().equals(categoryName)) continue;
            return currentCategory;
        }
        return null;
    }

    public BookmarkCategory getCategory(IBookmark bookmark) {
        BookmarkCategory category = null;
        Iterator<BookmarkCategory> iterator = this.categories.iterator();
        while (category == null && iterator.hasNext()) {
            BookmarkCategory currentCategory = iterator.next();
            if (currentCategory.indexOf(bookmark) == -1) continue;
            category = currentCategory;
        }
        return category;
    }

    public boolean isEmpty() {
        boolean result = false;
        if (this.categories.isEmpty()) {
            result = true;
        } else {
            Iterator<BookmarkCategory> iterator = this.categories.iterator();
            while (!result && iterator.hasNext()) {
                BookmarkCategory currentCategory = iterator.next();
                result = currentCategory.isEmpty();
            }
        }
        return result;
    }

    public void removeBookmark(String categoryName, IBookmark bookmark) {
        BookmarkCategory category = null;
        Iterator<BookmarkCategory> iterator = this.categories.iterator();
        while (category == null && iterator.hasNext()) {
            BookmarkCategory currentCategory = iterator.next();
            if (!currentCategory.getName().equals(categoryName)) continue;
            category = currentCategory;
        }
        if (category != null) {
            this.fireBookmarkChanged(new BookmarkEvent(bookmark, BookmarkEventType.REMOVED, category, category.indexOf(bookmark)));
            category.removeBookmark(bookmark);
        }
    }

    public void removeBookmark(BookmarkCategory category, IBookmark bookmark) {
        if (category != null) {
            this.fireBookmarkChanged(new BookmarkEvent(bookmark, BookmarkEventType.REMOVED, category, category.indexOf(bookmark)));
            category.removeBookmark(bookmark);
        }
    }

    public void removeBookmarks(String categoryName, Collection<IBookmark> bookmarksToRemove) {
        for (BookmarkCategory currentCategory : this.categories) {
            if (!currentCategory.getName().equals(categoryName)) continue;
            currentCategory.removeBookmarks(bookmarksToRemove);
        }
    }

    public void removeCategory(BookmarkCategory category) {
        int catIndex = this.categories.indexOf(category);
        if (category != null) {
            this.fireBookmarkCategoryChanged(new BookmarkCategoryEvent(category, BookmarkCategoryEventType.REMOVED, catIndex));
            this.categories.remove(category);
        }
    }

    public void saveBookmarks() {
        if (this.categories != null) {
            BookmarkXMLPersistence persistence = new BookmarkXMLPersistence();
            persistence.setBookmarkCategories(this.categories);
            persistence.setPersistent();
        } else {
            LOGGER.warn((Object)I18N.getString("org.saig.core.model.bookmark.BookmarkManager.there-are-not-any-bookmark-to-save"));
        }
    }

    public void restoreBookmarks() {
        BookmarkXMLPersistence persistence = new BookmarkXMLPersistence();
        this.categories = persistence.getBookmarkCategories();
        if (this.categories.size() == 0) {
            this.categories.add(BookmarkCategory.DEFAULT_CATEGORY);
        }
    }

    public void fireBookmarkChanged(IBookmark bookmark, BookmarkEventType type) {
        BookmarkCategory cat = this.getCategory(bookmark);
        this.fireBookmarkChanged(new BookmarkEvent(bookmark, type, cat, cat.indexOf(bookmark)));
    }

    public void fireBookmarkChanged(final BookmarkEvent event) {
        for (final IBookmarkListener listener : this.bookmarkListeners) {
            this.fireBookmarkEvent(new Runnable(){

                @Override
                public void run() {
                    listener.bookmarkChanged(event);
                }
            });
        }
    }

    public void fireBookmarkCategoryChanged(final BookmarkCategoryEvent event) {
        for (final IBookmarkListener listener : this.bookmarkListeners) {
            this.fireBookmarkEvent(new Runnable(){

                @Override
                public void run() {
                    listener.bookmarkCategoryChanged(event);
                }
            });
        }
    }

    private void fireBookmarkEvent(Runnable eventFirer) {
        try {
            GUIUtil.invokeOnEventThread(eventFirer);
        }
        catch (InterruptedException e) {
            Assert.shouldNeverReachHere();
        }
        catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            Assert.shouldNeverReachHere();
        }
    }

    public void addBookmarkListener(IBookmarkListener listener) {
        this.bookmarkListeners.add(listener);
    }

    public void removeBookmarkListener(IBookmarkListener listener) {
        this.bookmarkListeners.remove(listener);
    }
}

