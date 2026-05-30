/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.model.UndoableEditReceiver;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public abstract class AbstractPlugIn
implements PlugIn {
    public static final String GID_DEFAULT_ATT_NAME = "GID";
    public static final String GEOMETRY_DEFAULT_ATT_NAME = "geometry";
    public static final String ERROR_ATT_NAME = "ERROR";
    protected static GeometryFactory geomFac = new GeometryFactory();

    protected void execute(UndoableCommand command, PlugInContext context) throws Exception {
        AbstractPlugIn.execute(command, context.getLayerViewPanel());
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return true;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public Icon getDisabledIcon() {
        return null;
    }

    @Override
    public String getName() {
        return AbstractPlugIn.createName(this);
    }

    public static String createName(PlugIn plugIn) {
        return StringUtil.toFriendlyName(plugIn.getClass().getName(), "PlugIn");
    }

    public static ActionListener toActionListener(final PlugIn plugIn, final WorkbenchContext workbenchContext, final TaskMonitorManager taskMonitorManager) {
        return new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UndoableEditReceiver undoableEditReceiver;
                    workbenchContext.getWorkbench().getFrame().setStatusMessage("");
                    workbenchContext.getWorkbench().getFrame().log(String.valueOf(I18N.getString("workbench.plugin.AbstractPlugIn.executing")) + plugIn.getName());
                    PlugInContext plugInContext = workbenchContext.createPlugInContext();
                    UndoableEditReceiver undoableEditReceiver2 = undoableEditReceiver = workbenchContext.getLayerManager() != null ? workbenchContext.getLayerManager().getUndoableEditReceiver() : null;
                    if (undoableEditReceiver != null) {
                        undoableEditReceiver.startReceiving();
                    }
                    try {
                        boolean executeComplete = plugIn.execute(plugInContext);
                        if (plugIn instanceof ThreadedPlugIn && executeComplete && taskMonitorManager != null) {
                            taskMonitorManager.execute((ThreadedPlugIn)plugIn, plugInContext);
                        }
                    }
                    finally {
                        if (undoableEditReceiver != null) {
                            undoableEditReceiver.stopReceiving();
                        }
                    }
                }
                catch (Throwable t) {
                    workbenchContext.getErrorHandler().handleThrowable(t);
                }
            }
        };
    }

    public static void execute(UndoableCommand command, LayerManagerProxy layerManagerProxy) throws Exception {
        if (command != null) {
            command.execute();
            layerManagerProxy.getLayerManager().getUndoableEditReceiver().receive(command.toUndoableEdit());
        }
    }

    public String toString() {
        return this.getName();
    }

    protected void reportNothingToUndoYet(PlugInContext context) {
        if (context.getLayerManager() == null) {
            return;
        }
        context.getLayerManager().getUndoableEditReceiver().reportNothingToUndoYet();
    }

    @Override
    public void finish(PlugInContext context) {
    }

    @Override
    public EnableCheck getCheck() {
        return null;
    }

    protected boolean isRollingBackInvalidEdits() {
        return EditOptionsPanel.isRollingBackInvalidEdits();
    }

    protected boolean isConcurrentEditionActivated() {
        return EditOptionsPanel.isConcurrentEditionActivated();
    }

    protected void warnOperationCancelled(PlugInContext context) {
        context.getWorkbenchFrame().warnUser(I18N.getMessage("com.vividsolutions.jump.workbench.plugin.AbstractPlugIn.Operation-{0}-cancelled-by-the-user", new Object[]{this.getName()}));
    }

    protected void warnOperationSuccessful(PlugInContext context) {
        context.getWorkbenchFrame().warnUser(I18N.getMessage("com.vividsolutions.jump.workbench.plugin.AbstractPlugIn.The-operation-{0}-has-successfully-finished", new Object[]{this.getName()}));
    }
}

