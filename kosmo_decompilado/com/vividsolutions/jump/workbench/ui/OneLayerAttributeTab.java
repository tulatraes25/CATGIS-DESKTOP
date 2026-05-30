/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import java.util.List;

public class OneLayerAttributeTab
extends AttributeTab {
    private static final long serialVersionUID = 1L;
    private LayerListener oneLayerAttributeTabLayerListener = new LayerListener(){

        @Override
        public void featuresChanged(FeatureEvent e) {
            if (OneLayerAttributeTab.this.getLayerTableModel() == null) {
                return;
            }
            if (e.getLayer() == OneLayerAttributeTab.this.getLayerTableModel().getLayer() && e.getType() == FeatureEventType.ADDED) {
                OneLayerAttributeTab.this.getLayerTableModel().addAll(e.getFeatures());
            }
            if (e.getLayer() == OneLayerAttributeTab.this.getLayerTableModel().getLayer() && e.getType() == FeatureEventType.GEOMETRY_MODIFIED) {
                OneLayerAttributeTab.this.getLayerTableModel().updateAll(e.getFeatures(), e.getOldFeatureClones());
            }
        }

        @Override
        public void layerChanged(LayerEvent e) {
        }

        @Override
        public void categoryChanged(CategoryEvent e) {
        }
    };

    public OneLayerAttributeTab(WorkbenchContext context, TaskFrame taskFrame, LayerManagerProxy layerManagerProxy) {
        super(new InfoModel(), context, taskFrame, layerManagerProxy, true);
        context.getLayerManager().addLayerListener(this.oneLayerAttributeTabLayerListener);
    }

    public OneLayerAttributeTab setLayer(Layer layer) {
        if (!this.getModel().getLayers().isEmpty()) {
            this.getModel().remove(this.getLayer());
        }
        this.getModel().addKeys(layer, layer.getFeatureCollectionWrapper().getKeys());
        return this;
    }

    public Layer getLayer() {
        return this.getLayerTableModel() != null ? this.getLayerTableModel().getLayer() : null;
    }

    public LayerTableModel getLayerTableModel() {
        return !this.getModel().getLayerTableModels().isEmpty() ? this.getModel().getLayerTableModels().iterator().next() : null;
    }

    @Override
    public List<LayerListener> getLayerListeners() {
        List<LayerListener> listeners = super.getLayerListeners();
        listeners.add(this.oneLayerAttributeTabLayerListener);
        InfoModel model = this.getModel();
        listeners.addAll(model.getLayerListeners());
        return listeners;
    }
}

