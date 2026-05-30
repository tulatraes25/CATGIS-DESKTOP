/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.StyleVisitor;

public interface Style {
    public String getName();

    public void setName(String var1);

    public String getTitle();

    public void setTitle(String var1);

    public String getAbstract();

    public void setAbstract(String var1);

    public boolean isDefault();

    public void setDefault(boolean var1);

    public FeatureTypeStyle[] getFeatureTypeStyles();

    public void setFeatureTypeStyles(FeatureTypeStyle[] var1);

    public void addFeatureTypeStyle(FeatureTypeStyle var1);

    public void accept(StyleVisitor var1);

    public void setSelectedFeatureTypeStyle(int var1);

    public FeatureTypeStyle getSelectedFeatureTypeStyle();

    public void setSelectedFeatureTypeStyle(String var1);

    public void setSelectedFeatureTypeStyle(FeatureTypeStyle var1);

    public FeatureTypeStyle getFeatureTypeStyle(String var1);
}

