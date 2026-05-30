/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.wms;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.wms.BoundingBox;
import com.vividsolutions.wms.MapStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class MapLayer {
    private MapLayer parent = null;
    private String name;
    private String title;
    private List<String> srsList;
    private List<MapLayer> subLayers;
    private BoundingBox bbox;
    private List<MapStyle> styles;
    private List<BoundingBox> boundingBoxList;
    private boolean queryable;

    public MapLayer(String name, String title, Collection<String> srsList, Collection<MapLayer> subLayers, BoundingBox bbox, boolean queryable, List<MapStyle> styles) {
        this.name = name;
        this.title = title;
        this.srsList = new ArrayList<String>(srsList);
        this.subLayers = new ArrayList<MapLayer>(subLayers);
        this.queryable = queryable;
        this.setStyles(styles);
        Iterator<MapLayer> it = subLayers.iterator();
        while (it.hasNext()) {
            it.next().parent = this;
        }
        this.bbox = bbox;
    }

    public MapLayer(String name, String title, Collection<String> srsList, Collection<MapLayer> subLayers, BoundingBox bbox, List<BoundingBox> boundingBoxList, boolean queryable, List<MapStyle> styles) {
        this(name, title, srsList, subLayers, bbox, queryable, styles);
        this.boundingBoxList = boundingBoxList;
    }

    /*
     * Unable to fully structure code
     */
    public List<BoundingBox> getAllBoundingBoxList() {
        allBoundingBoxList = new ArrayList<BoundingBox>(this.getBoundingBoxList());
        mapLayer = this;
        if (allBoundingBoxList.size() <= 0) ** GOTO lbl10
        return allBoundingBoxList;
lbl-1000:
        // 1 sources

        {
            if ((mapLayer = mapLayer.getParent()) == null) {
                return allBoundingBoxList;
            }
            allBoundingBoxList = mapLayer.getBoundingBoxList();
            if (allBoundingBoxList.size() <= 0) continue;
            return allBoundingBoxList;
lbl10:
            // 2 sources

            ** while (mapLayer != null)
        }
lbl11:
        // 1 sources

        return allBoundingBoxList;
    }

    public int numSubLayers() {
        return this.subLayers.size();
    }

    public MapLayer getSubLayer(int n) {
        return this.subLayers.get(n);
    }

    public List<MapLayer> getSubLayerList() {
        return new ArrayList<MapLayer>(this.subLayers);
    }

    public List<MapLayer> getLayerList() {
        ArrayList<MapLayer> list = new ArrayList<MapLayer>();
        list.add(this);
        Iterator<MapLayer> it = this.subLayers.iterator();
        while (it.hasNext()) {
            list.addAll(it.next().getLayerList());
        }
        return list;
    }

    public MapLayer getMapLayer(String title) {
        if (this.getName() != null && this.getName().equals(title)) {
            return this;
        }
        for (MapLayer mapLayer : this.subLayers) {
            String name = mapLayer.getName();
            if (name != null && name.equals(title)) {
                return mapLayer;
            }
            MapLayer result = mapLayer.getMapLayer(title);
            if (result == null) continue;
            return result;
        }
        return null;
    }

    public String getTitle() {
        return this.title;
    }

    public String getName() {
        return this.name;
    }

    public MapLayer getParent() {
        return this.parent;
    }

    public BoundingBox getBoundingBox() {
        if (this.bbox != null) {
            return this.bbox;
        }
        if (this.parent != null) {
            return this.parent.getBoundingBox();
        }
        return null;
    }

    public BoundingBox getLatLonBoundingBox() {
        if (this.bbox != null) {
            return this.bbox;
        }
        if (this.parent != null) {
            return this.parent.getLatLonBoundingBox();
        }
        return null;
    }

    public List<BoundingBox> getBoundingBoxList() {
        return new ArrayList<BoundingBox>(this.boundingBoxList);
    }

    public List<String> getSRSList() {
        return new ArrayList<String>(this.srsList);
    }

    public Collection<String> getFullSRSList() {
        TreeSet<String> fullSRSList = new TreeSet<String>(this.getSRSList());
        if (this.parent != null) {
            fullSRSList.addAll(this.parent.getFullSRSList());
        }
        return fullSRSList;
    }

    public String toString() {
        return this.getName();
    }

    public List<MapStyle> getStyles() {
        return this.styles;
    }

    public void setStyles(List<MapStyle> newStyles) {
        this.styles = newStyles;
        for (MapStyle element : this.styles) {
            element.setLayer(this);
        }
        if (!this.styles.isEmpty()) {
            this.styles.get(0).setSelected(true, true);
        }
    }

    public boolean isQueryable() {
        return this.queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    public MapStyle getSelectedStyle() {
        for (MapStyle element : this.styles) {
            if (!element.isSelected()) continue;
            return element;
        }
        return null;
    }

    public void setSelectedStyle(MapStyle selectedStyle) {
        for (MapStyle element : this.styles) {
            element.setSelected(false, false);
        }
        selectedStyle.setSelected(true, false);
    }

    public MapStyle getStyle(String styleName) {
        for (MapStyle element : this.styles) {
            if (!element.getName().equals(styleName)) continue;
            return element;
        }
        return null;
    }

    public Envelope getEnvelope(String srs) {
        return this.getEnvelope(srs, true, true);
    }

    public Envelope getEnvelope(String srs, boolean lookForInChilds, boolean lookForInParents) {
        MapLayer parentMapLayer;
        Envelope env = null;
        if (this.bbox != null && (this.bbox.getSRS().equalsIgnoreCase(srs) || ("EPSG:" + this.bbox.getSRS()).equalsIgnoreCase(srs))) {
            env = new Envelope(this.bbox.getMinX(), this.bbox.getMaxX(), this.bbox.getMinY(), this.bbox.getMaxY());
        } else if (this.getLatLonBoundingBox() != null && (this.getLatLonBoundingBox().getSRS().equalsIgnoreCase(srs) || ("EPSG:" + this.getLatLonBoundingBox().getSRS()).equalsIgnoreCase(srs))) {
            BoundingBox latLonBB = this.getLatLonBoundingBox();
            env = new Envelope(latLonBB.getMinX(), latLonBB.getMaxX(), latLonBB.getMinY(), latLonBB.getMaxY());
        } else {
            List<BoundingBox> bbList = this.getBoundingBoxList();
            for (BoundingBox bb : bbList) {
                if (!bb.getSRS().equalsIgnoreCase(srs) && !("EPSG:" + bb.getSRS()).equalsIgnoreCase(srs)) continue;
                env = new Envelope(bb.getMinX(), bb.getMaxX(), bb.getMinY(), bb.getMaxY());
            }
        }
        if (env == null && lookForInParents && (parentMapLayer = this.getParent()) != null) {
            env = parentMapLayer.getEnvelope(srs, false, true);
        }
        if (env == null && lookForInChilds) {
            for (MapLayer currentMapLayer : this.subLayers) {
                Envelope currentMapLayerEnv = currentMapLayer.getEnvelope(srs);
                if (currentMapLayerEnv == null) continue;
                if (env == null) {
                    env = currentMapLayer.getEnvelope(srs, true, false);
                    continue;
                }
                env.expandToInclude(currentMapLayerEnv);
            }
        }
        return env;
    }
}

