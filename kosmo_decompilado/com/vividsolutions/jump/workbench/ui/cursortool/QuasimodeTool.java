/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.cursortool.LeftClickFilter;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.zoom.PanTool;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomTool;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuasimodeTool
extends DelegatingTool {
    private boolean altKeyDown = false;
    private boolean mouseDown = false;
    private Cursor cursor;
    private Set<CursorTool> toolsToRemove = new HashSet<CursorTool>();
    private KeyEvent currentKeyEvent = null;
    private LayerViewPanel panel;
    private WorkbenchFrame frame;
    private Map<ModifierKeySpec, CursorTool> keySpecToToolMap = new HashMap<ModifierKeySpec, CursorTool>();
    private KeyListener keyListener = new KeyListener(){

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            this.keyStateChanged(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            this.keyStateChanged(e);
        }

        private void keyStateChanged(KeyEvent e) {
            QuasimodeTool.this.altKeyDown = e.isAltDown();
            QuasimodeTool.this.setTool(e);
        }
    };
    private WindowAdapter windowListener = new WindowAdapter(){

        @Override
        public void windowActivated(WindowEvent e) {
            super.windowActivated(e);
            QuasimodeTool.this.setTool(new KeyEvent(QuasimodeTool.this.panel, 401, 0L, 0, 0, '\uffff'));
        }
    };

    public QuasimodeTool(CursorTool defaultTool) {
        super(defaultTool);
        this.add(new ModifierKeySpec(false, false, false), defaultTool);
        this.cursor = defaultTool.getCursor();
    }

    public CursorTool getDefaultTool() {
        return this.keySpecToToolMap.get(new ModifierKeySpec(false, false, false));
    }

    @Override
    public Cursor getCursor() {
        return this.cursor;
    }

    private CursorTool getTool(KeyEvent e) {
        CursorTool tool = this.keySpecToToolMap.get(new ModifierKeySpec(e.isControlDown(), e.isShiftDown(), e.isAltDown() || e.isMetaDown()));
        return tool != null ? tool : this.getDefaultTool();
    }

    private void setTool(KeyEvent e) {
        if (!this.mouseDown) {
            this.cursor = this.getTool(e).getCursor();
            this.panel.setCursor(this.cursor);
            this.currentKeyEvent = e;
            this.setDelegate(this.getTool(e));
            this.toolsToRemove.add(this.getTool(e));
        }
    }

    @Override
    public void activate(LayerViewPanel panel) {
        super.activate(panel);
        this.panel = panel;
        this.frame = AbstractCursorTool.workbenchFrame(panel);
        if (this.frame != null) {
            this.frame.addEasyKeyListener(this.keyListener);
            this.frame.addWindowListener(this.windowListener);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        this.mouseDown = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        this.mouseDown = false;
    }

    @Override
    public void deactivate() {
        if (!this.altKeyDown) {
            super.deactivate();
            if (this.frame != null) {
                this.frame.removeEasyKeyListener(this.keyListener);
                this.frame.removeWindowListener(this.windowListener);
            }
        }
        for (CursorTool tool : this.toolsToRemove) {
            tool.deactivate();
        }
        this.toolsToRemove.clear();
    }

    public QuasimodeTool add(ModifierKeySpec keySpec, CursorTool tool) {
        if (this.keySpecToToolMap.containsKey(keySpec)) {
            return this;
        }
        this.keySpecToToolMap.put(keySpec, tool != null ? (tool.isRightMouseButtonUsed() ? tool : new LeftClickFilter(tool)) : null);
        return this;
    }

    public static QuasimodeTool addStandardQuasimodes(CursorTool tool) {
        QuasimodeTool quasimodeTool = tool instanceof QuasimodeTool ? (QuasimodeTool)tool : new QuasimodeTool(tool);
        quasimodeTool.add(new ModifierKeySpec(false, false, true), new ZoomTool());
        quasimodeTool.add(new ModifierKeySpec(false, true, true), new PanTool());
        SelectFeaturesTool selectFeaturesTool = new SelectFeaturesTool(){

            @Override
            protected boolean selectedLayersOnly() {
                return false;
            }
        };
        quasimodeTool.add(new ModifierKeySpec(true, false, false), selectFeaturesTool);
        quasimodeTool.add(new ModifierKeySpec(true, true, false), selectFeaturesTool);
        quasimodeTool.add(new ModifierKeySpec(true, false, true), new FeatureInfoTool());
        return quasimodeTool;
    }

    @Override
    public boolean checkConditions() {
        return false;
    }

    @Override
    public boolean isActivate() {
        return false;
    }

    @Override
    public void setActivate(boolean activate) {
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

    public static class ModifierKeySpec {
        private boolean needsShift;
        private boolean needsAltOrMeta;
        private boolean needsControl;

        public ModifierKeySpec(boolean needsControl, boolean needsShift, boolean needsAltOrMeta) {
            this.needsControl = needsControl;
            this.needsShift = needsShift;
            this.needsAltOrMeta = needsAltOrMeta;
        }

        public int hashCode() {
            return 0;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ModifierKeySpec)) {
                return false;
            }
            ModifierKeySpec other = (ModifierKeySpec)obj;
            return this.needsControl == other.needsControl && this.needsShift == other.needsShift && this.needsAltOrMeta == other.needsAltOrMeta;
        }
    }
}

