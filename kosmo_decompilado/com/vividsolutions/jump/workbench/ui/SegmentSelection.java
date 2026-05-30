/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractSelection;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SegmentSelection
extends AbstractSelection {
    public SegmentSelection(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public String getRendererContentID() {
        return "SELECTED_SEGMENTS";
    }

    @Override
    public List<Geometry> items(Geometry geometry) {
        ArrayList<Geometry> itemList = new ArrayList<Geometry>();
        GeometryFactory gf = new GeometryFactory();
        List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(geometry, false);
        for (Coordinate[] coord : coordArrays) {
            int j = 0;
            while (j < coord.length - 1) {
                if (!coord[j].equals2D(coord[j + 1])) {
                    itemList.add((Geometry)gf.createLineString(new Coordinate[]{coord[j], coord[j + 1]}));
                }
                ++j;
            }
        }
        return itemList;
    }

    @Override
    protected boolean selectedInAncestors(Layer layer, Feature feature, Geometry item) {
        return false;
    }

    @Override
    protected void unselectInDescendants(Layer layer, Feature feature, Collection<Geometry> items) {
    }

    @Override
    public Collection<Integer> indices(Geometry geometry, Collection<Geometry> items) {
        List<Geometry> allItems = this.items(geometry);
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (LineString lineString : items) {
            boolean encontrado = false;
            int j = 0;
            while (j < allItems.size() && !encontrado) {
                LineString line = (LineString)allItems.get(j);
                if (line.equalsExact((Geometry)lineString)) {
                    encontrado = true;
                    indices.add(j);
                }
                ++j;
            }
        }
        return indices;
    }

    @Override
    public void unselectItems(Layer layer, Collection<Feature> features) {
        boolean originalPanelUpdatesEnabled = this.selectionManager.arePanelUpdatesEnabled();
        this.selectionManager.setPanelUpdatesEnabled(false);
        try {
            for (Feature feature : features) {
                this.unselectItems(layer, feature);
            }
            this.regenerateDataset(layer);
        }
        finally {
            this.selectionManager.setPanelUpdatesEnabled(originalPanelUpdatesEnabled);
        }
    }
}

