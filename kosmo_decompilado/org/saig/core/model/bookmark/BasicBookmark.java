/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.model.bookmark;

import com.vividsolutions.jts.geom.Geometry;
import org.saig.core.model.bookmark.BookmarkEventType;
import org.saig.core.model.bookmark.BookmarkManager;
import org.saig.core.model.bookmark.IBookmark;
import org.saig.jump.lang.I18N;

public class BasicBookmark
implements IBookmark,
Comparable<IBookmark> {
    public static final String DEFAULT_BOOKMARK_NAME = I18N.getString("org.saig.core.model.bookmark.BasicBookmark.Untitled");
    protected String name;
    protected String description;
    protected Geometry localization;

    public BasicBookmark() {
        this(DEFAULT_BOOKMARK_NAME, null, null);
    }

    public BasicBookmark(String name, String description, Geometry localization) {
        this.name = name;
        this.description = description;
        this.localization = localization;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String desc) {
        this.description = desc;
    }

    @Override
    public Geometry getLocalization() {
        return this.localization;
    }

    @Override
    public void setLocalization(Geometry geom) {
        this.localization = geom;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.setName(name, true);
    }

    @Override
    public void setName(String name, boolean fireEvent) {
        this.name = name;
        if (fireEvent) {
            BookmarkManager.getInstance().fireBookmarkChanged(this, BookmarkEventType.METADATA_CHANGED);
        }
    }

    public String toString() {
        return this.getName();
    }

    @Override
    public int compareTo(IBookmark o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }
}

