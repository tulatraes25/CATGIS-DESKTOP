/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.undo.UndoManager;
import org.saig.jump.lang.I18N;

public class UndoPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.UndoPlugIn.name");
    public static final Icon ICON = IconLoader.icon("Undo.gif");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        ((LayerManagerProxy)((Object)context.getWorkbenchContext().getWorkbench().getFrame().getActiveInternalFrame())).getLayerManager().getUndoableEditReceiver().getUndoManager().undo();
        this.reportNothingToUndoYet(context);
        context.getWorkbenchFrame().getToolBar().updateEnabledState();
        return true;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                UndoManager undoManager = ((LayerManagerProxy)((Object)workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getLayerManager().getUndoableEditReceiver().getUndoManager();
                component.setToolTipText("<HTML>" + undoManager.getUndoPresentationName() + "</HTML>");
                return !undoManager.canUndo() ? "X" : null;
            }
        });
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return UndoPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

