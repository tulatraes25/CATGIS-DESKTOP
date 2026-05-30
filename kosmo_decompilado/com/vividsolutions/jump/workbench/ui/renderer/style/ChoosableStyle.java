/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import javax.swing.Icon;

public interface ChoosableStyle
extends Style {
    @Override
    public String getName();

    @Override
    public Icon getIcon();
}

