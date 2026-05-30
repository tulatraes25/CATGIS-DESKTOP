/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.FeatureSelection;
import com.vividsolutions.jump.workbench.ui.PartSelection;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LineStringSelection
extends AbstractSelection {
    public LineStringSelection(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public String getRendererContentID() {
        return "SELECTED_LINESTRINGS";
    }

    @Override
    public List<Geometry> items(Geometry geometry) {
        int i;
        ArrayList<Geometry> items = new ArrayList<Geometry>();
        if (geometry instanceof LineString) {
            items.add(geometry);
        }
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon)geometry;
            items.add((Geometry)polygon.getExteriorRing());
            i = 0;
            while (i < polygon.getNumInteriorRing()) {
                items.add((Geometry)polygon.getInteriorRingN(i));
                ++i;
            }
        }
        if (geometry instanceof GeometryCollection) {
            GeometryCollection geometryCollection = (GeometryCollection)geometry;
            i = 0;
            while (i < geometryCollection.getNumGeometries()) {
                items.addAll(this.items(geometryCollection.getGeometryN(i)));
                ++i;
            }
        }
        return items;
    }

    @Override
    protected boolean selectedInAncestors(Layer layer, Feature feature, Geometry item) {
        Assert.isTrue((boolean)(this.getParent().getParent() instanceof FeatureSelection));
        Assert.isTrue((boolean)(this.getParent() instanceof PartSelection));
        if (this.getParent().getParent().getFeaturesWithSelectedItems().contains(feature)) {
            return true;
        }
        for (Geometry selectedPart : this.getParent().getSelectedItems(layer, feature)) {
            if (!this.items(selectedPart).contains(item)) continue;
            return true;
        }
        return false;
    }

    @Override
    protected void unselectInDescendants(Layer layer, Feature feature, Collection<Geometry> items) {
        Assert.isTrue((this.getChild() == null ? 1 : 0) != 0);
    }

    @Override
    protected void regenerateDataset(Layer layer) {
    }
}

