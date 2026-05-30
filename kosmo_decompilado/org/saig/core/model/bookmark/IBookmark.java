/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.model.bookmark;

import com.vividsolutions.jts.geom.Geometry;

public interface IBookmark {
    public String getName();

    public void setName(String var1);

    public void setName(String var1, boolean var2);

    public String getDescription();

    public void setDescription(String var1);

    public Geometry getLocalization();

    public void setLocalization(Geometry var1);
}

