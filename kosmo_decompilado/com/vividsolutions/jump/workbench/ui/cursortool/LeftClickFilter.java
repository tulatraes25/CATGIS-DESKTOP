/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

public class LeftClickFilter
implements CursorTool {
    private CursorTool wrappee;

    public LeftClickFilter(CursorTool wrappee) {
        this.wrappee = wrappee;
    }

    public CursorTool getWrappee() {
        return this.wrappee;
    }

    @Override
    public Icon getIcon() {
        return this.wrappee.getIcon();
    }

    @Override
    public String getName() {
        return this.wrappee.getName();
    }

    @Override
    public Cursor getCursor() {
        return this.wrappee.getCursor();
    }

    @Override
    public void activate(LayerViewPanel panel) {
        this.wrappee.activate(panel);
    }

    @Override
    public void deactivate() {
        this.wrappee.deactivate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (this.isOnlyLeftMouseButton(e)) {
            this.wrappee.mouseClicked(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (this.isOnlyLeftMouseButton(e)) {
            this.wrappee.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.isOnlyLeftMouseButton(e)) {
            this.wrappee.mouseReleased(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.wrappee.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.wrappee.mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.isOnlyLeftMouseButton(e)) {
            this.wrappee.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.wrappee.mouseMoved(e);
    }

    @Override
    public boolean isRightMouseButtonUsed() {
        return false;
    }

    @Override
    public boolean isGestureInProgress() {
        return this.wrappee.isGestureInProgress();
    }

    @Override
    public void cancelGesture() {
        this.wrappee.cancelGesture();
    }

    private boolean isOnlyLeftMouseButton(MouseEvent e) {
        return SwingUtilities.isLeftMouseButton(e) && !SwingUtilities.isRightMouseButton(e);
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
    public boolean checkConditions() throws Exception {
        return this.wrappee.checkConditions();
    }
}

