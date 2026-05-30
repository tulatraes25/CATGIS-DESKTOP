/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.awt.BorderLayout;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import org.saig.jump.lang.I18N;

public class LayerViewFrame
extends JInternalFrame {
    protected BorderLayout borderLayout = new BorderLayout();
    protected JScrollPane scrollPane = new JScrollPane();
    protected JSplitPane splitPane = new JSplitPane();
    protected int cloneIndex;
    protected InfoFrame infoFrame = null;
    protected LayerViewPanel layerViewPanel;
    protected Task task;
    protected WorkbenchContext workbenchContext;
    protected LayerManager layerManager;

    public LayerViewPanel getLayerViewPanel() {
        return this.layerViewPanel;
    }

    public Task getTask() {
        return this.task;
    }

    public LayerManager getLayerManager() {
        return this.task.getLayerManager();
    }

    protected int nextCloneIndex() {
        String key = String.valueOf(this.getClass().getName()) + " - LAST_CLONE_INDEX";
        this.task.getLayerManager().getBlackboard().put(key, 1 + this.task.getLayerManager().getBlackboard().get(key, 0));
        return this.task.getLayerManager().getBlackboard().getInt(key);
    }

    public SelectionManager getSelectionManager() {
        return this.getLayerViewPanel().getSelectionManager();
    }

    protected void updateTitle() {
        String title = this.task.getName();
        if (this.cloneIndex > 0) {
            title = String.valueOf(title) + " (" + I18N.getString("workbench.ui.LayerViewFrame.view") + " " + (this.cloneIndex + 1) + ")";
        }
        this.setTitle(title);
    }

    public void taskNameChanged(String name) {
        this.updateTitle();
    }
}

