/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.plugin.VerticesInFencePlugIn;
import java.awt.Color;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.saig.jump.lang.I18N;

public class SnapVerticesOp {
    public static final String INSERT_VERTICES_IF_NECESSARY_KEY = String.valueOf(SnapVerticesOp.class.getName()) + " - INSERT_VERTICES_IF_NECESSARY";
    private static final String NO_TARGET_VERTICES_IN_FENCE_WARNING = I18N.getString("workbench.ui.cursortool.editing.SnapVerticesOp.fence-contains-no-vertices-of-the-selected-feature-part-or-linestring");
    protected GeometryFactory geomFac = new GeometryFactory();
    private GeometryEditor geometryEditor = new GeometryEditor();

    private Collection<Feature> featuresInFence(Layer layer, Geometry fence, LayerViewPanel panel) {
        Collection<Feature> featuresInFence = panel.visibleLayerToFeaturesInFenceMap(fence).get(layer);
        if (featuresInFence == null) {
            return new ArrayList<Feature>();
        }
        return featuresInFence;
    }

    public Coordinate pickTarget(Geometry targetGeometry, Geometry fence, Coordinate suggestedTarget) throws Exception {
        List<Coordinate> verticesInFence = VerticesInFencePlugIn.verticesInFence(targetGeometry, fence, true).getCoordinates();
        if (verticesInFence.isEmpty()) {
            return null;
        }
        Coordinate c = CoordUtil.closest(verticesInFence, suggestedTarget);
        c.z = suggestedTarget.z;
        return c;
    }

    public boolean execute(Geometry fence, Collection<Layer> editableLayers, boolean rollingBackEdits, final LayerViewPanel panel, Task task, Coordinate suggestedTarget, Feature targetFeature, boolean insertVerticesIfNecessary) throws Exception {
        Map<Layer, Collection<Feature>> editableLayerToFeaturesInFenceMap = this.editableLayerToFeaturesInFenceMap(editableLayers, fence, panel);
        Collection editableFeatures = CollectionUtil.concatenate(editableLayerToFeaturesInFenceMap.values());
        if (editableFeatures.isEmpty()) {
            panel.getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.SnapVerticesOp.fence-contains-no-features-from-editable-layers"));
            return false;
        }
        if (VerticesInFencePlugIn.verticesInFence(targetFeature.getGeometry(), fence, true).getCoordinates().isEmpty() && VerticesInFencePlugIn.verticesInFence(FeatureUtil.toGeometries(editableFeatures), fence, true).isEmpty()) {
            panel.getContext().warnUser(NO_TARGET_VERTICES_IN_FENCE_WARNING);
            return false;
        }
        Geometry targetGeometry = targetFeature.getGeometry();
        ArrayList<EditTransaction> transactions = new ArrayList<EditTransaction>();
        for (Layer editableLayer : editableLayers) {
            Collection<Feature> featuresInFence = editableLayerToFeaturesInFenceMap.get(editableLayer);
            EditTransaction transaction = new EditTransaction(featuresInFence, I18N.getString("workbench.ui.cursortool.editing.SnapVerticesOp.snap-vertices-together"), editableLayer, rollingBackEdits, false, panel);
            transactions.add(transaction);
            if (!insertVerticesIfNecessary) continue;
            this.insertVerticesIfNecessary(transaction, suggestedTarget, fence);
            if (!featuresInFence.contains(targetFeature)) continue;
            targetGeometry = transaction.getGeometry(targetFeature);
        }
        final Coordinate target = this.pickTarget(targetGeometry, fence, suggestedTarget);
        if (target == null) {
            panel.getContext().warnUser(NO_TARGET_VERTICES_IN_FENCE_WARNING);
            return false;
        }
        boolean geometryChanged = this.moveVertices(transactions, fence, target);
        if (!geometryChanged) {
            return true;
        }
        return EditTransaction.commit(transactions, new EditTransaction.SuccessAction(){

            @Override
            public void run() {
                try {
                    SnapVerticesOp.this.indicateSuccess(target, panel);
                }
                catch (Throwable t) {
                    panel.getContext().warnUser(t.toString());
                }
            }
        });
    }

    private boolean moveVertices(List<EditTransaction> transactions, Geometry fence, Coordinate target) {
        boolean geometryChanged = false;
        for (EditTransaction transaction : transactions) {
            int j = 0;
            while (j < transaction.size()) {
                Geometry proposedGeometry = transaction.getGeometry(j);
                this.move(VerticesInFencePlugIn.verticesInFence(proposedGeometry, fence, false).getCoordinates(), target);
                try {
                    proposedGeometry = this.geometryEditor.removeRepeatedPoints(proposedGeometry);
                }
                catch (IllegalArgumentException e) {
                    Assert.isTrue((e.getMessage().toLowerCase().indexOf("point") > -1 && e.getMessage().toLowerCase().indexOf(">") > -1 ? 1 : 0) != 0, (String)"I assumed that we would get here only if too few points were passed into the Geometry constructor [Jon Aquino]");
                    int srid = proposedGeometry.getSRID();
                    proposedGeometry = this.geomFac.createPoint(target);
                    proposedGeometry.setSRID(srid);
                }
                transaction.setGeometry(j, proposedGeometry);
                ++j;
            }
            boolean bl = geometryChanged = geometryChanged || !this.coordinatesEqual(transaction, fence);
        }
        return geometryChanged;
    }

    private Map<Layer, Collection<Feature>> editableLayerToFeaturesInFenceMap(Collection<Layer> editableLayers, Geometry fence, LayerViewPanel panel) {
        HashMap<Layer, Collection<Feature>> editableLayerToFeaturesInFenceMap = new HashMap<Layer, Collection<Feature>>();
        for (Layer editableLayer : editableLayers) {
            Assert.isTrue((boolean)editableLayer.isEditable());
            editableLayerToFeaturesInFenceMap.put(editableLayer, this.featuresInFence(editableLayer, fence, panel));
        }
        return editableLayerToFeaturesInFenceMap;
    }

    private boolean coordinatesEqual(EditTransaction transaction, Geometry fence) {
        int i = 0;
        while (i < transaction.size()) {
            Feature originalFeature = transaction.getFeature(i);
            Geometry newGeometry = transaction.getGeometry(i);
            if (!this.coordinatesEqual(VerticesInFencePlugIn.verticesInFence(originalFeature.getGeometry(), fence, true).getCoordinates(), VerticesInFencePlugIn.verticesInFence(newGeometry, fence, true).getCoordinates())) {
                return false;
            }
            ++i;
        }
        return true;
    }

    private boolean coordinatesEqual(List a, List b) {
        if (a.size() != b.size()) {
            return false;
        }
        TreeSet A = new TreeSet(a);
        TreeSet B = new TreeSet(b);
        if (A.size() != B.size()) {
            return false;
        }
        Iterator Ai = A.iterator();
        Iterator Bi = B.iterator();
        while (Ai.hasNext()) {
            if (Ai.next().equals(Bi.next())) continue;
            return false;
        }
        return true;
    }

    private void indicateSuccess(Coordinate target, LayerViewPanel panel) throws NoninvertibleTransformException {
        Point2D center = panel.getViewport().toViewPoint(CoordUtil.toPoint2D(target));
        Animations.drawExpandingRing(center, false, Color.green, panel, null);
    }

    private void move(Collection<Coordinate> verticesToMove, Coordinate target) {
        for (Coordinate vertexToMove : verticesToMove) {
            vertexToMove.setCoordinate(target);
        }
    }

    private int insertVerticesIfNecessary(EditTransaction transaction, final Coordinate target, final Geometry fence) throws NoninvertibleTransformException {
        final int[] verticesInserted = new int[1];
        int i = 0;
        while (i < transaction.size()) {
            transaction.setGeometry(i, this.geometryEditor.edit(transaction.getGeometry(i), new GeometryEditor.GeometryEditorOperation(){

                @Override
                public Geometry edit(Geometry geometry) {
                    if (geometry instanceof Polygon) {
                        return geometry;
                    }
                    if (geometry instanceof GeometryCollection) {
                        return geometry;
                    }
                    if (!fence.intersects(geometry)) {
                        return geometry;
                    }
                    if (!VerticesInFencePlugIn.verticesInFence(geometry, fence, true).getCoordinates().isEmpty()) {
                        return geometry;
                    }
                    Geometry newGeometry = SnapVerticesOp.this.geometryEditor.insertVertex(geometry, target, fence);
                    if (newGeometry == null) {
                        return geometry;
                    }
                    verticesInserted[0] = verticesInserted[0] + 1;
                    return newGeometry;
                }
            }));
            ++i;
        }
        return verticesInserted[0];
    }
}

