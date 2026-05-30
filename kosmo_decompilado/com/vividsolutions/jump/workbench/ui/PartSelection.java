/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.FeatureSelection;
import com.vividsolutions.jump.workbench.ui.LineStringSelection;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PartSelection
extends AbstractSelection {
    public PartSelection(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public String getRendererContentID() {
        return "SELECTED_PARTS";
    }

    @Override
    public List<Geometry> items(Geometry geometry) {
        ArrayList<Geometry> items = new ArrayList<Geometry>();
        if (geometry instanceof GeometryCollection) {
            int i = 0;
            while (i < ((GeometryCollection)geometry).getNumGeometries()) {
                items.addAll(this.items(((GeometryCollection)geometry).getGeometryN(i)));
                ++i;
            }
        } else {
            items.add(geometry);
        }
        return items;
    }

    @Override
    protected boolean selectedInAncestors(Layer layer, Feature feature, Geometry item) {
        Assert.isTrue((boolean)(this.getParent() instanceof FeatureSelection));
        return this.getParent().getFeaturesWithSelectedItems().contains(feature);
    }

    @Override
    protected void unselectInDescendants(Layer layer, Feature feature, Collection<Geometry> items) {
        Assert.isTrue((boolean)(this.getChild() instanceof LineStringSelection));
        for (Geometry part : items) {
            List<Geometry> partLineStrings = this.getChild().items(part);
            for (LineString lineString : this.getChild().getSelectedItems(layer, feature)) {
                if (!partLineStrings.contains(lineString)) continue;
                this.getChild().unselectItem(layer, feature, partLineStrings.indexOf(lineString));
            }
        }
    }

    @Override
    protected void regenerateDataset(Layer layer) {
    }
}

