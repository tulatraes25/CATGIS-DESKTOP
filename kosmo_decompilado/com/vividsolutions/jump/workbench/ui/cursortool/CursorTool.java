/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import java.awt.Cursor;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;

public interface CursorTool
extends MouseListener,
MouseMotionListener,
KeyListener {
    public Cursor getCursor();

    public Icon getIcon();

    public void activate(LayerViewPanel var1);

    public void deactivate();

    public boolean isRightMouseButtonUsed();

    public boolean isGestureInProgress();

    public void cancelGesture();

    public String getName();

    public void setActivate(boolean var1);

    public boolean isActivate();

    public boolean checkConditions() throws Exception;
}

