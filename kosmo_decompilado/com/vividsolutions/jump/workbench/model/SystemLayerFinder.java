/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import java.awt.Color;
import org.apache.log4j.Logger;

public abstract class SystemLayerFinder {
    protected static final Logger LOGGER = Logger.getLogger(SystemLayerFinder.class);
    public static final String APP_INTERNAL_SYSTEM_LAYER = "APP_INTERNAL_SYSTEM_LAYER";
    protected String layerName;
    protected LayerManagerProxy layerManagerProxy;

    public SystemLayerFinder(String layerName, LayerManagerProxy layerManagerProxy) {
        this.layerManagerProxy = layerManagerProxy;
        this.layerName = layerName;
    }

    public String getLayerName() {
        return this.layerName;
    }

    public Layer createLayer() throws Exception {
        FeatureSchema schema = this.buildFeatureSchema();
        FeatureDataset dataset = new FeatureDataset(schema);
        Layer layer = new Layer(this.layerName, Color.blue, dataset, this.layerManagerProxy.getLayerManager()){

            @Override
            public boolean isFeatureCollectionModified() {
                return false;
            }
        };
        this.layerName = layer.getName();
        layer.setProperty(APP_INTERNAL_SYSTEM_LAYER, true);
        boolean firingEvents = this.layerManagerProxy.getLayerManager().isFiringEvents();
        this.layerManagerProxy.getLayerManager().setFiringEvents(false);
        try {
            this.applyStyles(layer);
        }
        finally {
            this.layerManagerProxy.getLayerManager().setFiringEvents(firingEvents);
        }
        this.layerManagerProxy.getLayerManager().addLayer(StandardCategoryNames.SYSTEM, layer);
        layer.setProjection(null);
        layer.getFeatureCollectionWrapper().clear();
        return layer;
    }

    protected FeatureSchema buildFeatureSchema() {
        FeatureSchema schema = new FeatureSchema();
        schema.addAttribute("GID", AttributeType.INTEGER, new Boolean(true));
        schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        schema.setGeometryType(5);
        return schema;
    }

    public Layer getLayer() {
        if (this.layerManagerProxy.getLayerManager() == null) {
            return null;
        }
        Layer layer = this.layerManagerProxy.getLayerManager().getLayer(this.layerName);
        if (layer == null) {
            return null;
        }
        return layer;
    }

    protected abstract void applyStyles(Layer var1);
}

