/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public interface StylePanel {
    public String getTitle();

    public Style updateStyles();

    public String validateInput();
}

