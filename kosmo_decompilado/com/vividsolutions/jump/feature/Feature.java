/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.Map;

public interface Feature
extends Cloneable,
Comparable<Feature> {
    public void setAttributes(Map<String, Object> var1);

    public void setSchema(FeatureSchema var1);

    public int getID();

    public void setID(int var1);

    public void setAttribute(int var1, Object var2);

    public void setAttributeCorrectType(int var1, Object var2);

    public void setAttribute(String var1, Object var2);

    public void setAttributeCorrectType(String var1, Object var2);

    public void setGeometry(Geometry var1);

    public Object getAttribute(int var1);

    public Object getAttribute(String var1);

    public String getString(int var1);

    public int getInteger(int var1);

    public double getDouble(int var1);

    public String getString(String var1);

    public Geometry getGeometry();

    public FeatureSchema getSchema();

    public Object clone();

    public Feature clone(boolean var1);

    public Feature clone(boolean var1, boolean var2);

    public Map<String, Object> getAttributes();

    public void setParent(FeatureCollection var1);

    public FeatureCollection getParent();

    public boolean isUnsaved();

    public Object getPrimaryKey();

    public int getPrimaryKeyAsInt();
}

