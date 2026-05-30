/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.StyledLayer;

public interface StyledLayerDescriptor {
    public StyledLayer[] getStyledLayers();

    public void setStyledLayers(StyledLayer[] var1);

    public void addStyledLayer(StyledLayer var1);

    public String getName();

    public void setName(String var1);

    public String getTitle();

    public void setTitle(String var1);

    public String getAbstract();

    public void setAbstract(String var1);

    public void accept(StyleVisitor var1);
}

