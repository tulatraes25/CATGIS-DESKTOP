/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.plugin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;
import javax.swing.JInternalFrame;
import org.apache.log4j.Logger;

public class PlugInContext
implements LayerManagerProxy {
    private static final Logger LOGGER = Logger.getLogger(PlugInContext.class);
    private Task task;
    private LayerNamePanel layerNamePanel;
    private LayerViewPanel layerViewPanel;
    private WorkbenchContext workbenchContext;
    private EnableCheckFactory checkFactory;
    private FeatureInstaller featureInstaller;
    private LayerManagerProxy layerManagerProxy;

    public PlugInContext(WorkbenchContext workbenchContext, Task task, LayerManagerProxy layerManagerProxy, LayerNamePanel layerNamePanel, LayerViewPanel layerViewPanel) {
        this.workbenchContext = workbenchContext;
        this.task = task;
        this.layerManagerProxy = layerManagerProxy;
        this.layerNamePanel = layerNamePanel;
        this.layerViewPanel = layerViewPanel;
        this.checkFactory = new EnableCheckFactory(workbenchContext);
        this.featureInstaller = new FeatureInstaller(workbenchContext);
    }

    public DriverManager getDriverManager() {
        return this.workbenchContext.getDriverManager();
    }

    public ErrorHandler getErrorHandler() {
        return this.workbenchContext.getErrorHandler();
    }

    public WorkbenchContext getWorkbenchContext() {
        return this.workbenchContext;
    }

    public Layerable getSelectedLayer(int i) {
        Layerable[] selectedLayers = this.getSelectedLayers();
        if (selectedLayers.length > i) {
            return selectedLayers[i];
        }
        return null;
    }

    public Layer getCandidateLayer(int i) {
        Layer lyr = (Layer)this.getSelectedLayer(i);
        if (lyr != null) {
            return lyr;
        }
        return this.getLayerManager().getLayer(i);
    }

    public Layerable[] getSelectedLayers() {
        return this.getLayerNamePanel().getSelectedLayers();
    }

    public Envelope getSelectedLayerEnvelope() {
        Envelope envelope = null;
        try {
            envelope = ((Layer)this.getSelectedLayer(0)).getFeatureCollectionWrapper().getEnvelope();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            envelope = new Envelope();
        }
        return envelope;
    }

    public Task getTask() {
        return this.task;
    }

    public LayerNamePanel getLayerNamePanel() {
        return this.layerNamePanel;
    }

    @Override
    public LayerManager getLayerManager() {
        if (this.layerManagerProxy == null) {
            return null;
        }
        return this.layerManagerProxy.getLayerManager();
    }

    public LayerViewPanel getLayerViewPanel() {
        return this.layerViewPanel;
    }

    public WorkbenchFrame getWorkbenchFrame() {
        return this.workbenchContext.getWorkbench().getFrame();
    }

    public Layer addLayer(String categoryName, String layerName, FeatureCollection featureCollection) {
        return this.getLayerManager().addLayer(categoryName, layerName, featureCollection);
    }

    public HTMLFrame getOutputFrame() {
        return this.workbenchContext.getWorkbench().getFrame().getOutputFrame();
    }

    public JInternalFrame getActiveInternalFrame() {
        return this.workbenchContext.getWorkbench().getFrame().getActiveInternalFrame();
    }

    public EnableCheckFactory getCheckFactory() {
        return this.checkFactory;
    }

    public FeatureInstaller getFeatureInstaller() {
        return this.featureInstaller;
    }
}

