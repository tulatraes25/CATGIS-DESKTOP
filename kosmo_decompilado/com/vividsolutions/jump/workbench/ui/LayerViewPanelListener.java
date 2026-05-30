/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Graphics;

public interface LayerViewPanelListener {
    public void selectionChanged();

    public void cursorPositionChanged(String var1, String var2);

    public void painted(Graphics var1);

    public void renderingFinished();

    public void renderingStarted();
}

