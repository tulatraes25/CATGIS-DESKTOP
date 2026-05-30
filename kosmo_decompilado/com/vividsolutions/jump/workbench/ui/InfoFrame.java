/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.saig.jump.lang.I18N;

public class InfoFrame
extends JInternalFrame
implements LayerManagerProxy,
SelectionManagerProxy,
LayerNamePanelProxy,
TaskFrameProxy,
LayerViewPanelProxy {
    private LayerManager layerManager;
    private BorderLayout borderLayout1 = new BorderLayout();
    private AttributeTab attributeTab;
    private InfoModel model = new InfoModel();
    private WorkbenchFrame workbenchFrame;

    @Override
    public LayerManager getLayerManager() {
        return this.layerManager;
    }

    @Override
    public TaskFrame getTaskFrame() {
        return this.attributeTab.getTaskFrame();
    }

    public InfoFrame(WorkbenchContext workbenchContext, LayerManagerProxy layerManagerProxy, final TaskFrame taskFrame) {
        Assert.isTrue((layerManagerProxy.getLayerManager() != null ? 1 : 0) != 0);
        this.layerManager = layerManagerProxy.getLayerManager();
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                InfoFrame.this.removeLayerListeners();
                InfoFrame.this.layerManager = new LayerManager();
            }
        });
        this.attributeTab = new AttributeTab(this.model, workbenchContext, taskFrame, this, true);
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                InfoFrame.this.attributeTab.getToolBar().updateEnabledState();
            }
        });
        this.workbenchFrame = workbenchContext.getWorkbench().getFrame();
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.updateTitle(taskFrame.getTask().getName());
        taskFrame.getTask().add(new Task.NameListener(){

            @Override
            public void taskNameChanged(String name) {
                InfoFrame.this.updateTitle(taskFrame.getTask().getName());
            }
        });
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                InfoFrame.this.model.dispose();
                if (InfoFrame.this.getTaskFrame() != null) {
                    InfoFrame.this.workbenchFrame.activateFrame(InfoFrame.this.getTaskFrame());
                }
            }
        });
    }

    public JPanel getAttributeTab() {
        return this.attributeTab;
    }

    public static String title(String taskName) {
        return String.valueOf(I18N.getString("workbench.ui.InfoFrame.feature-info")) + ": " + taskName;
    }

    private void updateTitle(String taskName) {
        this.setTitle(InfoFrame.title(taskName));
    }

    public InfoModel getModel() {
        return this.model;
    }

    private void jbInit() throws Exception {
        this.setDefaultCloseOperation(2);
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                try {
                    InfoFrame.this.workbenchFrame.removeInternalFrame(InfoFrame.this);
                }
                catch (Exception x) {
                    InfoFrame.this.workbenchFrame.handleThrowable(x);
                }
            }
        });
        this.setTitle(I18N.getString("workbench.ui.InfoFrame.feature-info"));
        this.getContentPane().setLayout(this.borderLayout1);
        this.getContentPane().add((Component)this.attributeTab, "Center");
        Rectangle parentBounds = this.workbenchFrame.getDesktopPane().getBounds();
        this.setSize(parentBounds.width, 250);
        this.setLocation(0, Math.max(0, parentBounds.height - 250));
    }

    public void surface() {
        JInternalFrame activeFrame = this.workbenchFrame.getActiveInternalFrame();
        if (!this.workbenchFrame.hasInternalFrame(this)) {
            this.workbenchFrame.addInternalFrame(this, false, true);
        }
        if (activeFrame != null) {
            this.workbenchFrame.activateFrame(activeFrame);
        }
        this.moveToFront();
    }

    @Override
    public SelectionManager getSelectionManager() {
        return this.attributeTab.getTaskFrame().getSelectionManager();
    }

    @Override
    public LayerNamePanel getLayerNamePanel() {
        return this.attributeTab;
    }

    @Override
    public LayerViewPanel getLayerViewPanel() {
        return this.getTaskFrame().getLayerViewPanel();
    }

    public void removeLayerListeners() {
        if (this.layerManager != null && this.attributeTab != null) {
            this.layerManager.removeLayerListeners(this.attributeTab.getLayerListeners());
        }
    }
}

