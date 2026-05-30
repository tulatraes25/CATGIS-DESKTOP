/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.cursortool.CompositeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.Iterator;

public class OrCompositeTool
extends CompositeTool {
    public OrCompositeTool() {
        this(new CursorTool[0]);
    }

    public OrCompositeTool(CursorTool[] cursorTools) {
        super(cursorTools);
    }

    private CursorTool currentTool() {
        for (CursorTool tool : this.cursorTools) {
            if (!tool.isGestureInProgress()) continue;
            return tool;
        }
        return null;
    }

    @Override
    public Cursor getCursor() {
        if (this.currentTool() == null) {
            return this.firstCursorTool().getCursor();
        }
        return super.getCursor();
    }

    private void clearOtherTools() {
        for (CursorTool tool : this.cursorTools) {
            if (tool == this.currentTool()) continue;
            tool.cancelGesture();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (this.currentTool() != null) {
            this.currentTool().mouseClicked(e);
            this.clearOtherTools();
            return;
        }
        for (CursorTool tool : this.cursorTools) {
            tool.mouseClicked(e);
            if (this.currentTool() == null) continue;
            this.clearOtherTools();
            return;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (this.currentTool() != null) {
            this.currentTool().mousePressed(e);
            this.clearOtherTools();
            return;
        }
        for (CursorTool tool : this.cursorTools) {
            tool.mousePressed(e);
            if (this.currentTool() == null) continue;
            this.clearOtherTools();
            return;
        }
    }

    @Override
    public String getName() {
        return this.getName("|");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.currentTool() != null) {
            this.currentTool().mouseReleased(e);
            this.clearOtherTools();
            return;
        }
        for (CursorTool tool : this.cursorTools) {
            tool.mouseReleased(e);
            if (this.currentTool() == null) continue;
            this.clearOtherTools();
            return;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (this.currentTool() != null) {
            this.currentTool().mouseEntered(e);
            this.clearOtherTools();
            return;
        }
        for (CursorTool tool : this.cursorTools) {
            tool.mouseEntered(e);
            if (this.currentTool() == null) continue;
            this.clearOtherTools();
            return;
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (this.currentTool() != null) {
            this.currentTool().mouseExited(e);
            this.clearOtherTools();
            return;
        }
        for (CursorTool tool : this.cursorTools) {
            tool.mouseExited(e);
            if (this.currentTool() == null) continue;
            this.clearOtherTools();
            return;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.currentTool() != null) {
            this.currentTool().mouseDragged(e);
            this.clearOtherTools();
            return;
        }
        for (CursorTool tool : this.cursorTools) {
            tool.mouseDragged(e);
            if (this.currentTool() == null) continue;
            this.clearOtherTools();
            return;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (this.currentTool() != null) {
            this.currentTool().mouseMoved(e);
            this.clearOtherTools();
            return;
        }
        for (CursorTool tool : this.cursorTools) {
            tool.mouseMoved(e);
            if (this.currentTool() == null) continue;
            this.clearOtherTools();
            return;
        }
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
        boolean ok = false;
        Iterator i = this.cursorTools.iterator();
        while (i.hasNext() && !ok) {
            CursorTool tool = (CursorTool)i.next();
            ok = tool.checkConditions();
        }
        return ok;
    }
}

