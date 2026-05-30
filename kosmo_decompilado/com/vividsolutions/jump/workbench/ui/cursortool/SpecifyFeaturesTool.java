/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.saig.jump.tools.editing.Utils;

public abstract class SpecifyFeaturesTool
extends DragTool {
    protected Iterator<Layer> candidateLayersIterator() {
        return this.getPanel().getLayerManager().iterator();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!this.activate) {
            return;
        }
        try {
            super.mouseClicked(e);
            this.setViewSource(e.getPoint());
            this.setViewDestination(e.getPoint());
            this.fireGestureFinished();
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    protected Set<Feature> specifiedFeatures() throws Exception {
        HashSet<Feature> allFeatures = new HashSet<Feature>();
        for (Collection<Feature> features : this.layerToSpecifiedFeaturesMap().values()) {
            allFeatures.addAll(features);
        }
        return allFeatures;
    }

    protected Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap() throws Exception {
        HashMap<Layer, Collection<Feature>> layerToFeaturesMap = new HashMap<Layer, Collection<Feature>>();
        Iterator<Layer> i = this.candidateLayersIterator();
        while (i.hasNext()) {
            Set<Feature> intersectingFeatures;
            Layer layer = i.next();
            if (!layer.isVisible() || (intersectingFeatures = Utils.intersectingFeatures(layer, this.getBoxInModelCoordinates())).isEmpty()) continue;
            layerToFeaturesMap.put(layer, intersectingFeatures);
        }
        return layerToFeaturesMap;
    }

    protected Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap(Object[] layers) throws Exception {
        HashMap<Layer, Collection<Feature>> layerToFeaturesMap = new HashMap<Layer, Collection<Feature>>();
        int i = 0;
        while (i < layers.length) {
            Layer layer;
            Set<Feature> intersectingFeatures;
            if (!(layers[i] instanceof WMSLayer) && !(intersectingFeatures = Utils.intersectingFeatures(layer = (Layer)layers[i], this.getBoxInModelCoordinates())).isEmpty()) {
                layerToFeaturesMap.put(layer, intersectingFeatures);
            }
            ++i;
        }
        return layerToFeaturesMap;
    }

    protected Collection<Feature> specifiedFeatures(Collection<Layer> layers) throws Exception {
        ArrayList<Feature> specifiedFeatures = new ArrayList<Feature>();
        Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap = this.layerToSpecifiedFeaturesMap();
        for (Layer layer : layerToSpecifiedFeaturesMap.keySet()) {
            if (!layers.contains(layer)) continue;
            specifiedFeatures.addAll(layerToSpecifiedFeaturesMap.get(layer));
        }
        return specifiedFeatures;
    }

    public static Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap(Iterator<Layer> layerIterator, Envelope boxInModelCoordinates) throws Exception {
        HashMap<Layer, Collection<Feature>> layerToFeaturesMap = new HashMap<Layer, Collection<Feature>>();
        Iterator<Layer> i = layerIterator;
        while (i.hasNext()) {
            Set<Feature> intersectingFeatures;
            Layer layer = i.next();
            if (!layer.isVisible() || (intersectingFeatures = Utils.intersectingFeatures(layer, boxInModelCoordinates)).isEmpty()) continue;
            layerToFeaturesMap.put(layer, intersectingFeatures);
        }
        return layerToFeaturesMap;
    }

    public static Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap(Iterator<Layer> layerIterator, Set<String> layerNames, Envelope boxInModelCoordinates) throws Exception {
        HashMap<Layer, Collection<Feature>> layerToFeaturesMap = new HashMap<Layer, Collection<Feature>>();
        Iterator<Layer> i = layerIterator;
        while (i.hasNext()) {
            Set<Feature> intersectingFeatures;
            Layer layer = i.next();
            if (!layerNames.contains(layer.getName()) || (intersectingFeatures = Utils.intersectingFeatures(layer, boxInModelCoordinates)).isEmpty()) continue;
            layerToFeaturesMap.put(layer, intersectingFeatures);
        }
        return layerToFeaturesMap;
    }
}

