/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class DummyTool
extends MouseAdapter
implements CursorTool {
    @Override
    public Cursor getCursor() {
        return Cursor.getDefaultCursor();
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public boolean isRightMouseButtonUsed() {
        return false;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public boolean isGestureInProgress() {
        return false;
    }

    @Override
    public void cancelGesture() {
    }

    @Override
    public String getName() {
        return I18N.getString("workbench.ui.cursortool.DummyTool.dummy-cursor-tool");
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void setActivate(boolean activate) {
    }

    @Override
    public boolean isActivate() {
        return false;
    }

    @Override
    public boolean checkConditions() {
        return true;
    }
}

