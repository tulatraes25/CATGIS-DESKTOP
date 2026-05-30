/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.LineStringSelection;
import com.vividsolutions.jump.workbench.ui.PartSelection;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;

public class FeatureSelection
extends AbstractSelection {
    private static final Logger LOGGER = Logger.getLogger(FeatureSelection.class);

    public FeatureSelection(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public String getRendererContentID() {
        return "SELECTED_FEATURES";
    }

    @Override
    public List<Geometry> items(Geometry geometry) {
        ArrayList<Geometry> items = new ArrayList<Geometry>();
        if (geometry != null) {
            items.add((Geometry)geometry.clone());
        }
        return items;
    }

    @Override
    protected boolean selectedInAncestors(Layer layer, Feature feature, Geometry item) {
        Assert.isTrue((this.getParent() == null ? 1 : 0) != 0);
        return false;
    }

    @Override
    protected void unselectInDescendants(Layer layer, Feature feature, Collection<Geometry> items) {
        Assert.isTrue((boolean)(this.getChild() instanceof PartSelection));
        Assert.isTrue((boolean)(this.getChild().getChild() instanceof LineStringSelection));
        this.getChild().unselectItems(layer, feature);
        this.getChild().getChild().unselectItems(layer, feature);
    }

    @Override
    protected void regenerateDataset(Layer layer) {
        Collection<Feature> selectedFeatures = this.selectionManager.getFeatureSelection().getFeaturesWithSelectedItems(layer);
        Collection<Feature> featuresWithSelectedSegments = this.selectionManager.getSegmentSelection().getFeaturesWithSelectedItems();
        featuresWithSelectedSegments.removeAll(selectedFeatures);
        this.selectionManager.getSegmentSelection().unselectItems(layer, featuresWithSelectedSegments);
        ArrayList<Geometry> geoms = new ArrayList<Geometry>();
        for (Feature element : selectedFeatures) {
            if (element.getGeometry() == null) continue;
            geoms.add((Geometry)element.getGeometry().clone());
        }
        try {
            if (this.selectionToPaintForLayers.containsKey(layer.getName())) {
                ((FeatureDataset)this.selectionToPaintForLayers.get(layer.getName())).clear();
            }
            this.selectionToPaintForLayers.put(layer.getName(), (FeatureDataset)FeatureDatasetFactory.createFromGeometry(geoms));
            if (layer.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && ((FeatureCollectionOnDemand)layer.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof ShapeFileDataSource) {
                layer.refreshSelection(selectedFeatures);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }
}

