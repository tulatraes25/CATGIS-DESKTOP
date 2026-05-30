/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.cursortool.CompositeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.awt.event.MouseEvent;
import java.util.Iterator;

public class AndCompositeTool
extends CompositeTool {
    public AndCompositeTool() {
        this(new CursorTool[0]);
    }

    public AndCompositeTool(CursorTool[] cursorTools) {
        super(cursorTools);
    }

    @Override
    public String getName() {
        return this.getName("&");
    }

    public void setCursorTool(CursorTool cursorTool) {
        this.cursorTools.clear();
        this.add(cursorTool);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for (CursorTool cursorTool : this.cursorTools) {
            cursorTool.mouseClicked(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (CursorTool cursorTool : this.cursorTools) {
            cursorTool.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (CursorTool cursorTool : this.cursorTools) {
            cursorTool.mouseReleased(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        for (CursorTool cursorTool : this.cursorTools) {
            cursorTool.mouseEntered(e);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        for (CursorTool cursorTool : this.cursorTools) {
            cursorTool.mouseExited(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        for (CursorTool cursorTool : this.cursorTools) {
            cursorTool.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for (CursorTool cursorTool : this.cursorTools) {
            cursorTool.mouseMoved(e);
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
        boolean ok = true;
        Iterator i = this.cursorTools.iterator();
        while (i.hasNext() && ok) {
            CursorTool cursorTool = (CursorTool)i.next();
            ok = cursorTool.checkConditions();
        }
        return ok;
    }
}

