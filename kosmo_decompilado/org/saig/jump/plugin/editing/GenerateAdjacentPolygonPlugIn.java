/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.core.model.relations.topology.topologyRelationsImpl.NoOverlapsOfAreasTopologyRelation;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.GenerateAdjacentPolygonTool;
import org.saig.jump.tools.editing.Utils;
import org.saig.jump.util.LayerUtil;

public class GenerateAdjacentPolygonPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(GenerateAdjacentPolygonPlugIn.class);
    private static final double MICRO_STEP = 1.0E-8;
    private static final double MINIMUM_CANDIDATE_AREA = 1.0E-6;
    protected LineString drawingLine;

    public GenerateAdjacentPolygonPlugIn(LineString line) {
        this.drawingLine = line;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer editableLayer = (Layer)this.getLayers(context).iterator().next();
        Envelope currentEnvelope = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
        monitor.report(I18N.getString(GenerateAdjacentPolygonPlugIn.class, "recovering-lines-collection-that-are-going-to-take-part"));
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        ArrayList<Geometry> polygonBorders = new ArrayList<Geometry>();
        HashMap<Integer, Feature> listPositionFeatsToUpdate = new HashMap<Integer, Feature>();
        List<Layer> layers = context.getLayerViewPanel().getLayerManager().getVisibleLayers(true);
        Iterator<Layer> itLayers = layers.iterator();
        int contLayers = 0;
        while (itLayers.hasNext()) {
            Layer candidateLayer = itLayers.next();
            monitor.report(contLayers++, layers.size(), I18N.getMessage(GenerateAdjacentPolygonPlugIn.class, "recovering-lines-of-layer-{0}-{1}-lines-at-this-moment", new Object[]{candidateLayer.getName(), polygonBorders.size()}));
            if (LayerUtil.isPointLayer(candidateLayer) || LayerUtil.isMultiPointLayer(candidateLayer)) continue;
            boolean isEditableLayer = candidateLayer.equals(editableLayer);
            FeatureIterator itCandidates = null;
            try {
                itCandidates = candidateLayer.getUltimateFeatureCollectionWrapper().queryIterator(currentEnvelope);
                while (itCandidates.hasNext()) {
                    Feature currentFeature = itCandidates.next();
                    Geometry workingGeometry = currentFeature.getGeometry();
                    polygonBorders.add((Geometry)workingGeometry.clone());
                    if (!isEditableLayer) continue;
                    listPositionFeatsToUpdate.put(polygonBorders.size() - 1, currentFeature);
                }
            }
            finally {
                if (itCandidates != null) {
                    itCandidates.close();
                }
            }
        }
        polygonBorders.add((Geometry)this.drawingLine.clone());
        monitor.report(String.valueOf(I18N.getString(GenerateAdjacentPolygonPlugIn.class, "generating-candidate-polygons")) + "...");
        Collection candidatePolygons = Utils.permanentExactNoderAndPolygonize(polygonBorders);
        this.drawingLine = (LineString)polygonBorders.get(polygonBorders.size() - 1);
        monitor.report(I18N.getMessage(GenerateAdjacentPolygonPlugIn.class, "recovering-valid-polygons-from-{0}-candidates", new Object[]{candidatePolygons.size()}));
        LOGGER.debug((Object)I18N.getMessage("org.saig.jump.tools.editing.GenerateAdjacentPolygonTool.{0}-candidate-polygons-were-found", new Object[]{Integer.toString(candidatePolygons.size())}));
        ArrayList<Polygon> finalPolygons = new ArrayList<Polygon>();
        for (Polygon currentCandidate : candidatePolygons) {
            LineString candidateBoundary;
            if (!currentCandidate.isValid() || currentCandidate.distance((Geometry)this.drawingLine) != 0.0 || (candidateBoundary = currentCandidate.getExteriorRing()).intersection((Geometry)this.drawingLine).getDimension() != 1) continue;
            finalPolygons.add(currentCandidate);
        }
        int i = 0;
        while (i < polygonBorders.size()) {
            Feature featToUpdate;
            Geometry currentGeom = (Geometry)polygonBorders.get(i);
            if (listPositionFeatsToUpdate.containsKey(i) && !(featToUpdate = (Feature)listPositionFeatsToUpdate.get(i)).getGeometry().equalsExact(currentGeom)) {
                Feature cloneFeat = featToUpdate.clone(true);
                cloneFeat.setGeometry(currentGeom);
                featsToUpdate.add(cloneFeat);
                featsSelectedToUpdate.add(featToUpdate);
            }
            ++i;
        }
        List<Feature> featsToAdd = this.obtainNonOverlappingFeatures(finalPolygons, featsToUpdate, editableLayer);
        SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        if (featsToAdd.isEmpty()) {
            context.getLayerViewPanel().getContext().warnUser(I18N.getString("org.saig.jump.tools.editing.GenerateAdjacentPolygonTool.there-are-not-any-polygon-generated-from-the-drawn-line"));
            return;
        }
        LOGGER.debug((Object)I18N.getMessage("org.saig.jump.tools.editing.GenerateAdjacentPolygonTool.{0}-definitive-polygons-were-found", new Object[]{Integer.toString(featsToAdd.size())}));
        if (!this.checkConditions()) {
            return;
        }
        monitor.report(I18N.getString(GenerateAdjacentPolygonPlugIn.class, "saving-changes"));
        this.applyChanges(selectionManager, editableLayer, featsToAdd, featsToUpdate, featsSelectedToUpdate, context);
    }

    protected void applyChanges(final SelectionManager selectionManager, final Layer affectedLayer, final Collection<Feature> featsToAdd, final List<Feature> featsToUpdate, final List<Feature> featsSelectedToUpdate, PlugInContext context) throws Exception {
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + I18N.getMessage("org.saig.jump.tools.editing.GenerateAdjacentPolygonTool.{0}-generated-polygons", new Object[]{featsToAdd.size()}) + "/" + featsToUpdate.size() + I18N.getString(GenerateAdjacentPolygonPlugIn.class, "updated-polygons") + " (<I>" + affectedLayer.getName() + "</I>)"){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(affectedLayer);
                try {
                    if (!featsToUpdate.isEmpty()) {
                        affectedLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        affectedLayer.getLayerManager().fireGeometryModified(featsToUpdate, affectedLayer, featsSelectedToUpdate);
                        selectionManager.getFeatureSelection().selectItems(affectedLayer, featsToUpdate);
                    }
                }
                catch (TopologyRelationException e) {
                    selectionManager.getFeatureSelection().selectItems(affectedLayer, featsSelectedToUpdate);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
                try {
                    affectedLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                    selectionManager.getFeatureSelection().selectItems(affectedLayer, featsToAdd);
                }
                catch (TopologyRelationException e) {
                    if (!featsToUpdate.isEmpty()) {
                        selectionManager.getFeatureSelection().selectItems(affectedLayer, featsSelectedToUpdate);
                    }
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    return;
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(affectedLayer);
                if (!featsToAdd.isEmpty()) {
                    affectedLayer.getFeatureCollectionWrapper().removeAll(featsToAdd);
                }
                if (!featsToUpdate.isEmpty()) {
                    affectedLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    affectedLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, affectedLayer, featsToUpdate);
                }
                selectionManager.getFeatureSelection().selectItems(affectedLayer, featsSelectedToUpdate);
            }
        }, context);
    }

    protected boolean checkConditions() {
        return true;
    }

    @Override
    public String getName() {
        return GenerateAdjacentPolygonTool.NAME;
    }

    protected Collection getLayers(PlugInContext context) {
        return context.getLayerViewPanel().getLayerManager().getEditableLayers();
    }

    protected List<Feature> obtainNonOverlappingFeatures(List<Polygon> finalPolygons, List<Feature> featsToUpdate, Layer editableLayer) throws Exception {
        ArrayList<Feature> featsToAdd = new ArrayList<Feature>();
        NoOverlapsOfAreasTopologyRelation noOverlapsTR = new NoOverlapsOfAreasTopologyRelation();
        noOverlapsTR.setSourceLayerName(editableLayer.getName());
        for (Polygon finalPolygon : finalPolygons) {
            Feature toAdd = FeatureUtil.toFeature((Geometry)finalPolygon, editableLayer.getFeatureSchema());
            if (!noOverlapsTR.check(toAdd, featsToUpdate) || !(toAdd.getGeometry().getArea() > 1.0E-6)) continue;
            boolean noOverlaps = true;
            Iterator<Feature> itUpdated = featsToUpdate.iterator();
            while (itUpdated.hasNext() && noOverlaps) {
                Feature currentFeat = itUpdated.next();
                Geometry intersection = currentFeat.getGeometry().intersection(toAdd.getGeometry());
                boolean bl = noOverlaps = intersection == null || intersection.getDimension() != 2;
            }
            if (!noOverlaps) continue;
            featsToAdd.add(toAdd);
        }
        return featsToAdd;
    }
}

