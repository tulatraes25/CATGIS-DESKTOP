/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.util;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.Filter;
import org.saig.core.filter.GeometryFilterImpl;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.jump.tools.editing.Utils;

public abstract class AbstractHierarchyUpdateController {
    private static final Logger LOGGER = Logger.getLogger(AbstractHierarchyUpdateController.class);
    protected Map<Layer, List<Feature>> featuresToAddToLayerMap = new HashMap<Layer, List<Feature>>();
    protected Map<Layer, List<Feature>> featuresToUpdateToLayerMap = new HashMap<Layer, List<Feature>>();
    protected Map<Layer, List<Feature>> featuresSelectedToUpdateToLayerMap = new HashMap<Layer, List<Feature>>();
    protected Map<String, String> layerNameToChildAttributeLinkName = new HashMap<String, String>();
    protected Set<Layer> updatedLayers = new HashSet<Layer>();
    protected boolean saveNewFeatures;
    protected Envelope filterEnvelope;

    public void initializeController() {
    }

    public abstract Layer[] getParentLayers(Layer var1);

    public abstract Layer[] getChildLayers(Layer var1);

    public void commit() throws Exception {
        for (Layer currentLayer : this.featuresSelectedToUpdateToLayerMap.keySet()) {
            currentLayer.getFeatureCollectionWrapper().commit();
        }
    }

    public void rollback() throws Exception {
        for (Layer currentLayer : this.featuresSelectedToUpdateToLayerMap.keySet()) {
            currentLayer.getFeatureCollectionWrapper().rollBack();
        }
    }

    public boolean updateHierarchy(Layer startingLayer, List<Feature> featsToAdd, List<Feature> featsToUpdate, List<Feature> featsSelectedToUpdate, SelectionManager selectionManager, boolean saveNewFeatures, Envelope affectedAreaEnv) throws Exception {
        return this.updateHierarchy(startingLayer, featsToAdd, featsToUpdate, featsSelectedToUpdate, selectionManager, saveNewFeatures, true, affectedAreaEnv);
    }

    public boolean updateHierarchy(Layer startingLayer, List<Feature> featsToAdd, List<Feature> featsToUpdate, List<Feature> featsSelectedToUpdate, SelectionManager selectionManager, boolean saveNewFeatures, boolean useTolerance, Envelope affectedAreaEnv) throws Exception {
        block7: {
            try {
                selectionManager.unselectItems(startingLayer);
                this.featuresToAddToLayerMap.put(startingLayer, featsToAdd);
                this.featuresToUpdateToLayerMap.put(startingLayer, featsToUpdate);
                this.featuresSelectedToUpdateToLayerMap.put(startingLayer, featsSelectedToUpdate);
                this.saveNewFeatures = saveNewFeatures;
                this.filterEnvelope = affectedAreaEnv;
                Layer rootLayer = this.getHierarchyRootLayer(startingLayer);
                if (rootLayer == null) break block7;
                List<Geometry> newGeometries = this.getGeometries(featsToAdd);
                List<Geometry> startingGeometries = this.getGeometries(featsToUpdate);
                this.updateChildEntities(rootLayer, newGeometries, startingGeometries, this.getGeometries(featsSelectedToUpdate), useTolerance);
                int i = 0;
                while (i < featsToUpdate.size()) {
                    Feature currentFeat = featsToUpdate.get(i);
                    Geometry newGeom = startingGeometries.get(i);
                    if (!newGeom.equals(currentFeat.getGeometry())) {
                        LOGGER.info((Object)("Geometr\u00eda de inicio para el elemento " + i + " modificada, actualizamos"));
                        currentFeat.setGeometry(newGeom);
                    }
                    ++i;
                }
                try {
                    this.updateChanges(rootLayer);
                    this.commit();
                    selectionManager.getFeatureSelection().selectItems(startingLayer, featsToUpdate);
                }
                catch (Exception ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                    this.rollback();
                    selectionManager.getFeatureSelection().selectItems(startingLayer, featsSelectedToUpdate);
                    throw ex;
                }
            }
            finally {
                this.clear();
            }
        }
        return true;
    }

    protected void updateChildEntities(Layer layer, List<Geometry> newGeoms, List<Geometry> modifiedGeoms, List<Geometry> originalGeoms, boolean useTolerance) throws Exception {
        Object[] childLayers;
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        if (!this.hasBeenProcessed(layer)) {
            LOGGER.debug((Object)"**************************************");
            LOGGER.debug((Object)("Procesando la capa " + layer.getName()));
            HashSet<Feature> candidateFeatures = new HashSet<Feature>();
            int i = 0;
            while (i < modifiedGeoms.size()) {
                Geometry modifiedGeom = modifiedGeoms.get(i);
                Geometry originalGeom = originalGeoms.get(i);
                candidateFeatures.addAll(Utils.getColindantes(modifiedGeom, layer, AbstractHierarchyUpdateController.getNeighbourFilter(layer.getFeatureSchema().getAttributeName(layer.getFeatureSchema().getGeometryIndex()), this.filterEnvelope)));
                if (!originalGeom.equals(modifiedGeom)) {
                    candidateFeatures.addAll(Utils.getColindantes(originalGeom, layer));
                }
                ++i;
            }
            ArrayList<Feature> affectedFeatures = new ArrayList<Feature>(candidateFeatures);
            List<Geometry> neighbourGeometries = this.getGeometries(affectedFeatures);
            LOGGER.debug((Object)("Estudiando " + affectedFeatures.size() + " vecinos"));
            if (CollectionUtils.isNotEmpty(affectedFeatures)) {
                neighbourGeometries.addAll(newGeoms);
                neighbourGeometries.addAll(modifiedGeoms);
                if (useTolerance) {
                    Utils.permanentExactNoder(neighbourGeometries, null, this.getTolerance());
                } else {
                    Utils.permanentExactNoder(neighbourGeometries, null, 0.0);
                }
                int i2 = 0;
                while (i2 < affectedFeatures.size()) {
                    Feature currentNeighbour = (Feature)affectedFeatures.get(i2);
                    Geometry currentNeighbourGeom = currentNeighbour.getGeometry();
                    if (!currentNeighbourGeom.equals(neighbourGeometries.get(i2))) {
                        Feature featColClone = currentNeighbour.clone(true);
                        featColClone.setGeometry(neighbourGeometries.get(i2));
                        featsToUpdate.add(featColClone);
                        featsSelectedToUpdate.add(currentNeighbour);
                    }
                    ++i2;
                }
                i2 = 0;
                while (i2 < newGeoms.size()) {
                    newGeoms.set(i2, neighbourGeometries.get(i2 + affectedFeatures.size()));
                    ++i2;
                }
                i2 = 0;
                while (i2 < modifiedGeoms.size()) {
                    modifiedGeoms.set(i2, neighbourGeometries.get(i2 + affectedFeatures.size() + newGeoms.size()));
                    ++i2;
                }
            }
            LOGGER.debug((Object)("Se van a actualizar " + featsToUpdate.size() + "/" + affectedFeatures.size() + " elementos"));
            this.featuresToUpdateToLayerMap.put(layer, featsToUpdate);
            this.featuresSelectedToUpdateToLayerMap.put(layer, featsSelectedToUpdate);
            LOGGER.debug((Object)"**************************************");
        }
        if (!ArrayUtils.isEmpty((Object[])(childLayers = this.getChildLayers(layer)))) {
            Object[] objectArray = childLayers;
            int n = childLayers.length;
            int n2 = 0;
            while (n2 < n) {
                Object childLayer = objectArray[n2];
                this.updateChildEntities((Layer)childLayer, newGeoms, modifiedGeoms, originalGeoms, useTolerance);
                ++n2;
            }
        }
    }

    public static Filter getNeighbourFilter(String geometryAttributeName, Envelope envelope) {
        GeometryFilterImpl result = null;
        if (envelope != null && !envelope.isNull()) {
            try {
                GeometryFilterImpl filter = new GeometryFilterImpl(7);
                AttributeExpressionImpl2 attrExpr = new AttributeExpressionImpl2(geometryAttributeName);
                LiteralExpressionImpl litExpr = new LiteralExpressionImpl(EnvelopeUtil.toGeometry(envelope));
                filter.addLeftGeometry(attrExpr);
                filter.addRightGeometry(litExpr);
                result = filter;
            }
            catch (IllegalFilterException illegalFilterException) {
                // empty catch block
            }
        }
        return result;
    }

    public abstract double getTolerance();

    protected List<Geometry> getGeometries(List<Feature> affectedFeatures) {
        ArrayList<Geometry> result = new ArrayList<Geometry>(affectedFeatures.size());
        int i = 0;
        while (i < affectedFeatures.size()) {
            result.add(i, (Geometry)affectedFeatures.get(i).getGeometry().clone());
            ++i;
        }
        return result;
    }

    protected boolean hasBeenProcessed(Layer layer) {
        return this.featuresToUpdateToLayerMap.containsKey(layer) || this.featuresToAddToLayerMap.containsKey(layer);
    }

    protected Layer getHierarchyRootLayer(Layer startingLayer) {
        Layer rootLayer = null;
        Layer currentLayer = startingLayer;
        while (rootLayer == null) {
            if (this.isRootLayer(currentLayer)) {
                rootLayer = currentLayer;
                continue;
            }
            Layer[] parentLayers = this.getParentLayers(currentLayer);
            int i = 0;
            while (i < parentLayers.length && rootLayer == null) {
                rootLayer = this.getHierarchyRootLayer(parentLayers[i]);
                ++i;
            }
        }
        return rootLayer;
    }

    protected boolean isRootLayer(Layer layer) {
        return ArrayUtils.isEmpty((Object[])this.getParentLayers(layer));
    }

    protected void updateChanges(Layer currentLayer) throws Exception {
        Layer[] childLayers;
        if (!this.updatedLayers.contains(currentLayer)) {
            this.updatedLayers.add(currentLayer);
            LOGGER.debug((Object)("Actualizando cambios en la jerarqu\u00eda para la capa " + currentLayer.getName()));
            List<Feature> featsToAdd = this.featuresToAddToLayerMap.get(currentLayer);
            List<Feature> featsToUpdate = this.featuresToUpdateToLayerMap.get(currentLayer);
            List<Feature> featsSelectedToUpdate = this.featuresSelectedToUpdateToLayerMap.get(currentLayer);
            if (CollectionUtils.isNotEmpty(featsToUpdate)) {
                LOGGER.debug((Object)("Se van a actualizar " + featsToUpdate.size() + " elementos"));
                currentLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                currentLayer.getLayerManager().fireGeometryModified(featsToUpdate, currentLayer, featsSelectedToUpdate);
                currentLayer.setFeatureCollectionModified(false);
            }
            if (this.saveNewFeatures && CollectionUtils.isNotEmpty(featsToAdd)) {
                LOGGER.debug((Object)("Se van a a\u00f1adir " + featsToAdd.size() + " elementos"));
                currentLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
            }
        }
        Layer[] layerArray = childLayers = this.getChildLayers(currentLayer);
        int n = childLayers.length;
        int n2 = 0;
        while (n2 < n) {
            Layer childLayer = layerArray[n2];
            this.updateChanges(childLayer);
            ++n2;
        }
    }

    public void clear() {
        this.featuresToAddToLayerMap.clear();
        this.featuresToUpdateToLayerMap.clear();
        this.featuresSelectedToUpdateToLayerMap.clear();
        this.updatedLayers.clear();
    }

    public Map<String, List<Feature>> getParentFeatures(Layer childLayer, Feature childFeature) throws Exception {
        HashMap<String, List<Feature>> results = new HashMap<String, List<Feature>>();
        Layer[] parentLayers = this.getParentLayers(childLayer);
        Geometry childGeom = childFeature.getGeometry();
        Envelope childFeatureEnv = childGeom.getEnvelopeInternal();
        Layer[] layerArray = parentLayers;
        int n = parentLayers.length;
        int n2 = 0;
        while (n2 < n) {
            Layer parentLayer = layerArray[n2];
            ArrayList<Feature> parentFeats = new ArrayList<Feature>();
            List<Feature> candidates = parentLayer.getFeatureCollectionWrapper().query(childFeatureEnv);
            for (Feature parentFeature : candidates) {
                if (!parentFeature.getGeometry().contains(childGeom)) continue;
                parentFeats.add(parentFeature);
            }
            if (CollectionUtils.isNotEmpty(parentFeats)) {
                results.put(parentLayer.getName(), parentFeats);
            }
            ++n2;
        }
        return results;
    }

    public Map<String, List<Feature>> getParentIntersectingFeatures(Layer childLayer, Feature childFeature) throws Exception {
        HashMap<String, List<Feature>> results = new HashMap<String, List<Feature>>();
        Layer[] parentLayers = this.getParentLayers(childLayer);
        Geometry childGeom = childFeature.getGeometry();
        Envelope childFeatureEnv = childGeom.getEnvelopeInternal();
        Layer[] layerArray = parentLayers;
        int n = parentLayers.length;
        int n2 = 0;
        while (n2 < n) {
            Layer parentLayer = layerArray[n2];
            ArrayList<Feature> parentFeats = new ArrayList<Feature>();
            List<Feature> candidates = parentLayer.getFeatureCollectionWrapper().query(childFeatureEnv);
            for (Feature parentFeature : candidates) {
                if (!parentFeature.getGeometry().intersects(childGeom)) continue;
                Geometry intersection = parentFeature.getGeometry().intersection(childGeom);
                if (intersection.getDimension() >= 2) {
                    parentFeats.add(parentFeature);
                    continue;
                }
                if (intersection.getDimension() != 1 || !(childGeom instanceof LineString) && !(childGeom instanceof MultiLineString)) continue;
                parentFeats.add(parentFeature);
            }
            if (CollectionUtils.isNotEmpty(parentFeats)) {
                results.put(parentLayer.getName(), parentFeats);
            }
            ++n2;
        }
        return results;
    }

    public String getParentLinkAttribute(Layer layer) {
        return this.layerNameToChildAttributeLinkName.get(layer.getName());
    }
}

