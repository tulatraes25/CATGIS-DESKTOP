/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DummyTool;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.Icon;

public abstract class DelegatingTool
implements CursorTool {
    private CursorTool delegate = new DummyTool();
    private boolean active = false;
    private LayerViewPanel layerViewPanel;

    public DelegatingTool(CursorTool cursorTool) {
        this.setDelegate(cursorTool);
    }

    public CursorTool getDelegate() {
        return this.delegate;
    }

    public void setDelegate(CursorTool delegate) {
        if (this.delegate == delegate) {
            return;
        }
        this.delegate = delegate;
        if (this.active) {
            this.delegate.activate(this.layerViewPanel);
        }
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public Icon getIcon() {
        return this.delegate.getIcon();
    }

    @Override
    public boolean isGestureInProgress() {
        return this.delegate.isGestureInProgress();
    }

    @Override
    public void cancelGesture() {
        this.delegate.cancelGesture();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.delegate.mousePressed(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.delegate.mouseClicked(e);
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        this.layerViewPanel = layerViewPanel;
        this.delegate.activate(layerViewPanel);
        this.active = true;
    }

    @Override
    public Cursor getCursor() {
        return this.delegate.getCursor();
    }

    @Override
    public void deactivate() {
        this.delegate.deactivate();
        this.active = false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.delegate.mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.delegate.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.delegate.mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.delegate.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.delegate.mouseMoved(e);
    }

    @Override
    public boolean isRightMouseButtonUsed() {
        return this.delegate.isRightMouseButtonUsed();
    }
}

