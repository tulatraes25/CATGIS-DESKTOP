/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.model.bookmark;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.Timestamp;
import org.saig.core.model.bookmark.BasicBookmark;
import org.saig.core.model.bookmark.IBookmark;

public class TemporalBookmark
extends BasicBookmark {
    private Timestamp timemark;

    public TemporalBookmark() {
        this(DEFAULT_BOOKMARK_NAME, null, null, new Timestamp(System.currentTimeMillis()));
    }

    public TemporalBookmark(String name, String description, Geometry localization, Timestamp timemark) {
        super(name, description, localization);
        this.timemark = timemark;
    }

    public Timestamp getTimemark() {
        return this.timemark;
    }

    public void setTimemark(Timestamp timemark) {
        this.timemark = timemark;
    }

    @Override
    public int compareTo(IBookmark o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }
}

