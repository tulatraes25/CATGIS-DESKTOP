/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package org.saig.jump.widgets.utils.bookmarks;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.SimpleTreeModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import org.saig.core.model.bookmark.BookmarkCategory;
import org.saig.core.model.bookmark.BookmarkManager;
import org.saig.core.model.bookmark.IBookmark;

public class BookmarkTreeModel
extends SimpleTreeModel {
    public BookmarkTreeModel() {
        super(new Root());
    }

    @Override
    public List<?> getChildren(Object parent) {
        if (parent == this.getRoot()) {
            return new ArrayList<BookmarkCategory>(BookmarkManager.getInstance().getCategories());
        }
        if (parent instanceof BookmarkCategory) {
            return new ArrayList<IBookmark>(((BookmarkCategory)parent).getBookmarks());
        }
        if (parent instanceof IBookmark) {
            return new ArrayList();
        }
        Assert.shouldNeverReachHere((String)parent.getClass().getName());
        return null;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        if (path.getLastPathComponent() instanceof IBookmark) {
            ((IBookmark)path.getLastPathComponent()).setName((String)newValue);
        } else if (path.getLastPathComponent() instanceof BookmarkCategory) {
            ((BookmarkCategory)path.getLastPathComponent()).setName((String)newValue);
        } else {
            Assert.shouldNeverReachHere();
        }
    }

    public static class Root {
        private Root() {
        }
    }
}

