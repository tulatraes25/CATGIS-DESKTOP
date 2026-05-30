/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import java.awt.Graphics2D;
import javax.swing.Icon;

public interface Style
extends Cloneable {
    public void paint(Feature var1, Graphics2D var2, Viewport var3) throws Exception;

    public void initialize(Layer var1);

    public Object clone();

    public void setEnabled(boolean var1);

    public boolean isEnabled();

    public String getName();

    public Icon getIcon();
}

