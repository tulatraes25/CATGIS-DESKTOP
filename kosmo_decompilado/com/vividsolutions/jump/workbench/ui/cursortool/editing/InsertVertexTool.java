/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.Point
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.Utils;

public class InsertVertexTool
extends NClickTool {
    private static final Logger LOGGER = Logger.getLogger(InsertVertexTool.class);
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.InsertVertexTool.insert-vertex");
    public static final Icon ICON = IconLoader.icon("InsertVertex.gif");
    public static final Cursor CURSOR = InsertVertexTool.createCursor(IconLoader.icon("PlusCursor.gif").getImage());
    public static final int PIXEL_RANGE = 5;
    protected EnableCheckFactory checkFactory;

    public InsertVertexTool(EnableCheckFactory checkFactory) {
        super(1);
        this.checkFactory = checkFactory;
        this.allowSnapping();
    }

    public InsertVertexTool(EnableCheckFactory checkFactory, boolean checkEditability) {
        super(1, checkEditability);
        this.checkFactory = checkFactory;
        this.allowSnapping();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    private double modelRange() {
        return 5.0 / this.getPanel().getViewport().getScale();
    }

    protected Collection<Feature> featuresInRange(Coordinate modelClickCoordinate, Layer layer) {
        ArrayList<Feature> featuresInRange;
        block6: {
            Collection<Feature> featuresWithSelectedItems;
            Point modelClickPoint;
            block5: {
                modelClickPoint = geomFac.createPoint(modelClickCoordinate);
                featuresWithSelectedItems = this.getPanel().getSelectionManager().getFeaturesWithSelectedItems(layer);
                featuresInRange = new ArrayList<Feature>();
                if (this.isAdjacentEditionActivated()) break block5;
                if (CollectionUtils.isEmpty(featuresWithSelectedItems)) {
                    return new ArrayList<Feature>();
                }
                for (Feature candidate : featuresWithSelectedItems) {
                    if (!(modelClickPoint.distance(candidate.getGeometry()) <= this.modelRange())) continue;
                    featuresInRange.add(candidate);
                }
                break block6;
            }
            if (!this.isInDistanceSelected(modelClickPoint, featuresWithSelectedItems)) break block6;
            HashSet<Feature> candidateNeighbours = new HashSet<Feature>();
            try {
                candidateNeighbours.addAll(Utils.intersectingFeatures(layer, modelClickPoint.buffer(this.modelRange()).getEnvelopeInternal()));
                for (Feature candidate : candidateNeighbours) {
                    if (!(modelClickPoint.distance(candidate.getGeometry()) <= this.modelRange())) continue;
                    featuresInRange.add(candidate);
                }
                this.getPanel().getSelectionManager().getFeatureSelection().selectItems(layer, featuresInRange);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return featuresInRange;
    }

    private boolean isInDistanceSelected(Point modelClickPoint, Collection<Feature> featuresWithSelectedItems) {
        boolean ok = false;
        Iterator<Feature> itFeats = featuresWithSelectedItems.iterator();
        while (itFeats.hasNext() && !ok) {
            boolean bl = ok = modelClickPoint.distance(itFeats.next().getGeometry()) < this.modelRange();
        }
        return ok;
    }

    protected Coordinate modelClickCoordinate() throws NoninvertibleTransformException {
        return this.getCoordinates().get(0);
    }

    protected LineSegment segmentInRange(Geometry geometry, Coordinate target) {
        LineSegment closest = null;
        List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(geometry, false);
        for (Coordinate[] coordinates : coordArrays) {
            int j = 1;
            while (j < coordinates.length) {
                LineSegment candidate = new LineSegment(coordinates[j - 1], coordinates[j]);
                if (!(candidate.distance(target) > this.modelRange()) && (closest == null || candidate.distance(target) < closest.distance(target))) {
                    closest = candidate;
                }
                ++j;
            }
        }
        return closest;
    }

    protected Coordinate newVertex(LineSegment segment, Coordinate target) {
        Coordinate closestPoint = segment.closestPoint(target);
        closestPoint.z = target.z;
        if (!closestPoint.equals((Object)segment.p0) && !closestPoint.equals((Object)segment.p1)) {
            return closestPoint;
        }
        return null;
    }

    protected SegmentContext findSegment(Layer layer, Collection<Feature> features, Coordinate target) {
        for (Feature feature : features) {
            Geometry selectedItem = feature.getGeometry();
            LineSegment segment = this.segmentInRange(selectedItem, target);
            if (segment == null) continue;
            return new SegmentContext(layer, feature, segment);
        }
        return null;
    }

    protected SegmentContext findSegment(Map<Layer, Collection<Feature>> layerToFeaturesMap, Coordinate target) {
        for (Layer layer : layerToFeaturesMap.keySet()) {
            Collection<Feature> features;
            SegmentContext segmentContext = this.findSegment(layer, features = layerToFeaturesMap.get(layer), target);
            if (segmentContext == null) continue;
            return segmentContext;
        }
        return null;
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        if (!this.checkConditions()) {
            return;
        }
        Map<Layer, Collection<Feature>> layerToFeaturesInRangeMap = this.layerToFeaturesInRangeMap();
        if (layerToFeaturesInRangeMap.isEmpty()) {
            this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.InsertVertexTool.no-selected-editable-items-here"));
            return;
        }
        Coordinate targetCoord = this.modelClickCoordinate();
        ArrayList<SegmentContext> segments = new ArrayList<SegmentContext>();
        for (Layer layer : layerToFeaturesInRangeMap.keySet()) {
            Collection<Feature> featuresInRange = layerToFeaturesInRangeMap.get(layer);
            for (Feature currentFeat : featuresInRange) {
                ArrayList<Feature> featsToCheck = new ArrayList<Feature>();
                featsToCheck.add(currentFeat);
                SegmentContext segmentContext = this.findSegment(layer, featsToCheck, targetCoord);
                if (segmentContext == null) continue;
                segments.add(segmentContext);
            }
        }
        if (segments.isEmpty()) {
            this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.InsertVertexTool.no-selected-line-segments-here"));
            return;
        }
        ArrayList<Geometry> newGeometries = new ArrayList<Geometry>();
        Coordinate animationCoordinate = null;
        boolean anyNewVertex = false;
        for (SegmentContext currentSegment : segments) {
            Coordinate newVertex = this.newVertex(currentSegment.getSegment(), this.modelClickCoordinate());
            if (newVertex == null) continue;
            anyNewVertex = true;
            Geometry newGeometry = new GeometryEditor().insertVertex(currentSegment.getFeature().getGeometry(), currentSegment.getSegment().p0, currentSegment.getSegment().p1, newVertex);
            newGeometries.add(newGeometry);
            if (animationCoordinate != null) continue;
            animationCoordinate = newVertex;
        }
        if (!anyNewVertex) {
            this.getPanel().getContext().warnUser(I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.editing.InsertVertexTool.The-vertex-already-exists"));
            return;
        }
        this.gestureFinished(newGeometries, animationCoordinate, segments);
    }

    @Override
    public boolean checkConditions() {
        boolean ok = true;
        if (!this.check(this.checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(new int[]{5, 4, 3, 2}, new int[]{10, 11, 9}, 1))) {
            ok = false;
        } else if (!this.check(this.checkFactory.createAtLeastNLayersMustBeEditableCheck(1))) {
            ok = false;
        } else if (!this.check(this.checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
            ok = false;
        }
        return ok;
    }

    protected void gestureFinished(List<Geometry> newGeometries, Coordinate animationCoordinate, List<SegmentContext> segments) throws Exception {
        SelectionManager selectionManager = this.getPanel().getSelectionManager();
        ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        Layer segmentLayer = null;
        int i = 0;
        while (i < segments.size()) {
            SegmentContext segment = segments.get(i);
            Geometry newGeometry = newGeometries.get(i);
            segmentLayer = segment.getLayer();
            featsSelectedToUpdate.add(segment.getFeature());
            Feature cloneFeat = (Feature)segment.getFeature().clone();
            cloneFeat.setGeometry(newGeometry);
            featsToUpdate.add(cloneFeat);
            ++i;
        }
        this.executeCommand(selectionManager, featsSelectedToUpdate, featsToUpdate, segmentLayer, animationCoordinate);
    }

    protected void executeCommand(final SelectionManager selectionManager, final List<Feature> featsSelectedToUpdate, final List<Feature> featsToUpdate, final Layer segmentLayer, final Coordinate animationCoordinate) throws Exception {
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(segmentLayer, featsSelectedToUpdate);
                try {
                    if (!featsToUpdate.isEmpty()) {
                        segmentLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        segmentLayer.getLayerManager().fireGeometryModified(featsToUpdate, segmentLayer, featsSelectedToUpdate);
                        selectionManager.getFeatureSelection().selectItems(segmentLayer, featsToUpdate);
                    }
                    try {
                        Animations.drawExpandingRing(InsertVertexTool.this.getPanel().getViewport().toViewPoint(animationCoordinate), false, Color.green, InsertVertexTool.this.getPanel(), null);
                    }
                    catch (Throwable t) {
                        LOGGER.error((Object)"", t);
                    }
                }
                catch (TopologyRelationException e) {
                    selectionManager.getFeatureSelection().selectItems(segmentLayer, featsSelectedToUpdate);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(segmentLayer);
                if (!featsToUpdate.isEmpty()) {
                    segmentLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    segmentLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, segmentLayer, featsToUpdate);
                }
                selectionManager.getFeatureSelection().selectItems(segmentLayer, featsSelectedToUpdate);
            }
        });
    }

    protected Map<Layer, Collection<Feature>> layerToFeaturesInRangeMap() throws NoninvertibleTransformException {
        HashMap<Layer, Collection<Feature>> layerToFeaturesInRangeMap = new HashMap<Layer, Collection<Feature>>();
        for (Layer currentLayer : this.getLayers()) {
            Collection<Feature> featuresInRange = this.featuresInRange(this.modelClickCoordinate(), currentLayer);
            if (featuresInRange.isEmpty()) continue;
            layerToFeaturesInRangeMap.put(currentLayer, featuresInRange);
        }
        return layerToFeaturesInRangeMap;
    }

    protected Collection<Layer> getLayers() {
        return this.getPanel().getLayerManager().getEditableLayers();
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 3, 2, 15}));
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(new int[]{5, 4, 3, 2}, new int[]{10, 11, 9}, 1));
        solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        return solucion;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        super.activate(layerViewPanel);
        if (this.snapIndicatorTool == null) {
            this.snapIndicatorTool = new SnapIndicatorTool(this.getSnappingPolicies(JUMPWorkbench.getBlackboard()));
        } else {
            this.snapIndicatorTool.setNewPolicies(this.getSnappingPolicies(JUMPWorkbench.getBlackboard()));
        }
        this.snapIndicatorTool.activate(layerViewPanel);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.snapIndicatorTool.deactivate();
    }

    public static class SegmentContext {
        private LineSegment segment;
        private Feature feature;
        private Layer layer;

        public SegmentContext(Layer layer, Feature feature, LineSegment segment) {
            this.layer = layer;
            this.feature = feature;
            this.segment = segment;
        }

        public Feature getFeature() {
            return this.feature;
        }

        public Layer getLayer() {
            return this.layer;
        }

        public LineSegment getSegment() {
            return this.segment;
        }
    }
}

