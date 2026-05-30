/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;

public class DeleteSelectedItemsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(DeleteSelectedItemsPlugIn.class);
    private static boolean keyRegistered;
    public static final String NAME;
    public static final Icon ICON;
    protected static boolean shiftPressed;

    static {
        NAME = I18N.getString("workbench.ui.plugin.DeleteSelectedItemsPlugIn.name");
        ICON = IconLoader.icon("delete_small.gif");
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Collection<Layer> affectedLayers = this.getLayers(context);
        int numSelectedItems = 0;
        Iterator<Layer> iterator = affectedLayers.iterator();
        while (iterator.hasNext() && numSelectedItems == 0) {
            Layer currentLayer = iterator.next();
            numSelectedItems += ((SelectionManagerProxy)((Object)context.getActiveInternalFrame())).getSelectionManager().getNumFeaturesWithSelectedItems(currentLayer);
        }
        return numSelectedItems > 0;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithSelectionManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1)).add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        super.initialize(context);
        this.registerDeleteKey(context.getWorkbenchContext());
    }

    private synchronized void registerDeleteKey(final WorkbenchContext context) {
        if (keyRegistered) {
            return;
        }
        final MultiEnableCheck enableCheck = DeleteSelectedItemsPlugIn.createEnableCheck(context);
        context.getWorkbench().getFrame().addEasyKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                block8: {
                    if (context.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame && e.getKeyCode() == 127) {
                        if (enableCheck.check(null) != null) {
                            return;
                        }
                        context.getLayerManager().getUndoableEditReceiver().startReceiving();
                        try {
                            try {
                                PlugInContext pc = context.createPlugInContext();
                                boolean ok = DeleteSelectedItemsPlugIn.this.execute(pc);
                                if (ok) {
                                    DeleteSelectedItemsPlugIn.this.setShiftPressed(e.isShiftDown());
                                    new TaskMonitorManager().execute(DeleteSelectedItemsPlugIn.this, pc);
                                }
                            }
                            catch (Exception ex) {
                                LOGGER.error((Object)"", (Throwable)ex);
                                context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                                break block8;
                            }
                        }
                        catch (Throwable throwable) {
                            context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                            throw throwable;
                        }
                        context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                    }
                }
                DeleteSelectedItemsPlugIn.this.setShiftPressed(e.isShiftDown());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                DeleteSelectedItemsPlugIn.this.setShiftPressed(e.isShiftDown());
            }
        });
        keyRegistered = true;
    }

    protected void setShiftPressed(boolean isShiftPressed) {
        shiftPressed = isShiftPressed;
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
        return DeleteSelectedItemsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void run(final TaskMonitor monitor, PlugInContext context) throws Exception {
        Collection<Layer> affectedLayers = this.getLayers(context);
        final Layer editableLayer = affectedLayers.iterator().next();
        final SelectionManager selectionManager = ((SelectionManagerProxy)((Object)context.getActiveInternalFrame())).getSelectionManager();
        final Collection<Feature> featuresWithSelectedItems = selectionManager.getFeaturesWithSelectedItems(editableLayer);
        if (shiftPressed) {
            monitor.report(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn.deleting-{0}-elements-from-the-layer-{1}", new Object[]{featuresWithSelectedItems.size(), editableLayer.getName()}));
            editableLayer.getFeatureCollectionWrapper().removeAll(featuresWithSelectedItems);
            editableLayer.fireAppearanceChanged();
        } else {
            this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + featuresWithSelectedItems.size() + I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn.{0}-elements")){

                @Override
                public void execute() throws Exception {
                    try {
                        monitor.report(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn.deleting-{0}-elements-from-the-layer-{1}", new Object[]{featuresWithSelectedItems.size(), editableLayer.getName()}));
                        editableLayer.getFeatureCollectionWrapper().removeAll(featuresWithSelectedItems);
                        selectionManager.unselectItems(editableLayer);
                    }
                    catch (TopologyRelationException e) {
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featuresWithSelectedItems);
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    }
                }

                @Override
                public void unexecute() throws Exception {
                    try {
                        monitor.report(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn.restoring-{0}-elements-from-the-layer-{1}", new Object[]{featuresWithSelectedItems.size(), editableLayer.getName()}));
                        editableLayer.getFeatureCollectionWrapper().addAll(featuresWithSelectedItems);
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featuresWithSelectedItems);
                    }
                    catch (TopologyRelationException e) {
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featuresWithSelectedItems);
                        JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    }
                }
            }, context);
        }
    }

    protected Collection<Layer> getLayers(PlugInContext context) {
        return context.getLayerViewPanel().getLayerManager().getEditableLayers();
    }
}

