/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.LeftClickFilter;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.IModifiableCoordinateTrace;

public class RemoveLastPointPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.RemoveLastPointPlugIn.name");
    public static final Icon ICON = GUIUtil.resize(IconLoader.icon("undo_trace.png"), 20);

    @Override
    public boolean execute(PlugInContext context) {
        RemoveLastPointPlugIn.getInProgressCursorTool().undoLastCoordinate();
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return RemoveLastPointPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheck check = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                boolean cond = false;
                boolean bl = cond = RemoveLastPointPlugIn.getInProgressCursorTool() != null;
                if (!cond) {
                    return "";
                }
                return null;
            }
        };
        return new MultiEnableCheck().add(check);
    }

    protected static IModifiableCoordinateTrace getInProgressCursorTool() {
        CursorTool currentCursorTool;
        IModifiableCoordinateTrace inProgressCursorTool = null;
        if (JUMPWorkbench.getFrameInstance() != null && JUMPWorkbench.getFrameInstance().getContext() != null && JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel() != null && (currentCursorTool = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getCurrentCursorTool()) != null) {
            QuasimodeTool quasimodeTool;
            if (currentCursorTool instanceof QuasimodeTool && (currentCursorTool = (quasimodeTool = (QuasimodeTool)currentCursorTool).getDelegate()) instanceof LeftClickFilter) {
                LeftClickFilter leftClickFilter = (LeftClickFilter)currentCursorTool;
                currentCursorTool = leftClickFilter.getWrappee();
            }
            if (currentCursorTool instanceof DelegatingTool) {
                DelegatingTool delegatingTool = (DelegatingTool)currentCursorTool;
                currentCursorTool = delegatingTool.getDelegate();
            }
            if (currentCursorTool.isGestureInProgress() && currentCursorTool instanceof IModifiableCoordinateTrace) {
                inProgressCursorTool = (IModifiableCoordinateTrace)((Object)currentCursorTool);
            }
        }
        return inProgressCursorTool;
    }
}

