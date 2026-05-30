/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;

public abstract class CompositeTool
extends KeyAdapter
implements CursorTool {
    private LayerViewPanel panel = null;
    protected List<CursorTool> cursorTools = new ArrayList<CursorTool>();

    public CompositeTool(CursorTool[] cursorTools) {
        this.cursorTools = new ArrayList<CursorTool>(Arrays.asList(cursorTools));
    }

    public CompositeTool add(CursorTool tool) {
        this.cursorTools.add(tool);
        return this;
    }

    @Override
    public Cursor getCursor() {
        for (CursorTool cursorTool : this.cursorTools) {
            if (cursorTool.getCursor() == Cursor.getDefaultCursor()) continue;
            return cursorTool.getCursor();
        }
        return Cursor.getDefaultCursor();
    }

    @Override
    public boolean isRightMouseButtonUsed() {
        for (CursorTool cursorTool : this.cursorTools) {
            if (!cursorTool.isRightMouseButtonUsed()) continue;
            return true;
        }
        return false;
    }

    protected CursorTool firstCursorTool() {
        return this.cursorTools.get(0);
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        this.panel = layerViewPanel;
        for (CursorTool tool : this.cursorTools) {
            tool.activate(layerViewPanel);
        }
    }

    @Override
    public void deactivate() {
        for (CursorTool tool : this.cursorTools) {
            tool.deactivate();
        }
    }

    @Override
    public void cancelGesture() {
        for (CursorTool tool : this.cursorTools) {
            tool.cancelGesture();
        }
    }

    @Override
    public Icon getIcon() {
        for (CursorTool tool : this.cursorTools) {
            if (tool.getIcon() == null) continue;
            return tool.getIcon();
        }
        return null;
    }

    protected String getName(String delimiter) {
        String name = "";
        int i = 0;
        while (i < this.cursorTools.size()) {
            if (i > 0) {
                name = String.valueOf(name) + " " + delimiter + " ";
            }
            name = String.valueOf(name) + this.cursorTools.get(i).getName();
            ++i;
        }
        return name;
    }

    @Override
    public boolean isGestureInProgress() {
        for (CursorTool tool : this.cursorTools) {
            if (!tool.isGestureInProgress()) continue;
            return true;
        }
        return false;
    }

    public LayerViewPanel getPanel() {
        return this.panel;
    }
}

