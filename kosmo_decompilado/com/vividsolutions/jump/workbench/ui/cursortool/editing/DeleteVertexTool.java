/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.VerticesInFencePlugIn;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.Utils;

public class DeleteVertexTool
extends SpecifyFeaturesTool {
    private static final Logger LOGGER = Logger.getLogger(DeleteVertexTool.class);
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.DeleteVertexTool.name");
    public static final Icon ICON = IconLoader.icon("DeleteVertex.gif");
    public static final Cursor CURSOR = DeleteVertexTool.createCursor(IconLoader.icon("DeleteCursor.gif").getImage());
    protected EnableCheckFactory checkFactory;
    protected GeometryEditor geometryEditor = new GeometryEditor();

    public DeleteVertexTool(EnableCheckFactory checkFactory) {
        this.checkFactory = checkFactory;
        this.setViewClickBuffer(5);
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

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        if (!this.checkConditions()) {
            return;
        }
        HashSet<Coordinate> verticesDeleted = new HashSet<Coordinate>();
        ArrayList<VertexContext> vertexes = new ArrayList<VertexContext>();
        Geometry box = EnvelopeUtil.toGeometry(this.getBoxInModelCoordinates());
        boolean emptyGeometries = false;
        Iterator<Layer> iterator = this.getLayers().iterator();
        while (iterator.hasNext() && !emptyGeometries) {
            Layer currentLayer = iterator.next();
            ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
            ArrayList<Geometry> newGeometries = new ArrayList<Geometry>();
            Collection<Feature> featuresInRange = this.featuresInRange(box, currentLayer);
            for (Feature currentFeat : featuresInRange) {
                ArrayList<Geometry> selectedGeometries = new ArrayList<Geometry>();
                selectedGeometries.add(currentFeat.getGeometry());
                Collection<Coordinate> verticesInBox = VerticesInFencePlugIn.verticesInFence(selectedGeometries, box, true);
                if (this.wasClick() && !verticesInBox.isEmpty()) {
                    verticesDeleted.add(verticesInBox.iterator().next());
                } else {
                    verticesDeleted.addAll(verticesInBox);
                }
                Geometry modifiedGeometry = this.geometryEditor.deleteVertices(currentFeat.getGeometry(), verticesInBox);
                emptyGeometries = modifiedGeometry.isEmpty();
                featsToUpdate.add(currentFeat);
                newGeometries.add(modifiedGeometry);
                if (modifiedGeometry.isValid()) continue;
                if (this.isRollingBackInvalidEdits()) {
                    this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.EditTransaction.the-new-geometry-is-invalid-cancelled"));
                    return;
                }
                this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.EditTransaction.the-new-geometry-is-invalid"));
            }
            if (featsToUpdate.isEmpty()) continue;
            vertexes.add(new VertexContext(currentLayer, featsToUpdate, newGeometries));
        }
        if (emptyGeometries) {
            this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.DeleteVertexTool.cancelled-deletion-would-result-in-empty-geometry"));
            return;
        }
        if (verticesDeleted.isEmpty() || vertexes.isEmpty()) {
            this.getPanel().getContext().warnUser(I18N.getString("workbench.ui.cursortool.editing.DeleteVertexTool.no-selection-handles-here"));
            return;
        }
        this.gestureFinished(vertexes, verticesDeleted);
    }

    protected void gestureFinished(List<VertexContext> vertexes, Set<Coordinate> verticesDeleted) throws Exception {
        VertexContext vertex = vertexes.get(0);
        Layer vertexLayer = vertex.getLayer();
        SelectionManager selectionManager = this.getPanel().getSelectionManager();
        List<Feature> featsSelectedToUpdate = vertex.getFeaturesToUpdate();
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        List<Geometry> newGeometries = vertex.getNewGeometries();
        int i = 0;
        for (Feature currentFeat : featsSelectedToUpdate) {
            Feature cloneFeat = (Feature)currentFeat.clone();
            cloneFeat.setGeometry(newGeometries.get(i));
            featsToUpdate.add(cloneFeat);
            ++i;
        }
        this.executeCommand(vertexLayer, selectionManager, featsSelectedToUpdate, featsToUpdate, verticesDeleted);
    }

    protected void executeCommand(final Layer vertexLayer, final SelectionManager selectionManager, final List<Feature> featsSelectedToUpdate, final List<Feature> featsToUpdate, final Set<Coordinate> verticesDeleted) throws Exception {
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(vertexLayer, featsSelectedToUpdate);
                try {
                    if (!featsToUpdate.isEmpty()) {
                        vertexLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        vertexLayer.getLayerManager().fireGeometryModified(featsToUpdate, vertexLayer, featsSelectedToUpdate);
                        selectionManager.getFeatureSelection().selectItems(vertexLayer, featsToUpdate);
                    }
                    try {
                        Animations.drawExpandingRings(DeleteVertexTool.this.getPanel().getViewport().toViewPoints(verticesDeleted), true, Color.red, DeleteVertexTool.this.getPanel(), new float[]{15.0f, 15.0f});
                    }
                    catch (Throwable t) {
                        DeleteVertexTool.this.getPanel().getContext().warnUser(t.toString());
                    }
                }
                catch (TopologyRelationException e) {
                    selectionManager.getFeatureSelection().selectItems(vertexLayer, featsSelectedToUpdate);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(vertexLayer);
                if (!featsToUpdate.isEmpty()) {
                    vertexLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    vertexLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, vertexLayer, featsToUpdate);
                }
                selectionManager.getFeatureSelection().selectItems(vertexLayer, featsSelectedToUpdate);
            }
        });
    }

    protected EditTransaction createTransaction(Layer layer, final List<Coordinate> verticesDeleted) throws NoninvertibleTransformException {
        final Geometry box = EnvelopeUtil.toGeometry(this.getBoxInModelCoordinates());
        EditTransaction transaction = EditTransaction.createTransactionOnSelection(new EditTransaction.SelectionEditor(){

            @Override
            public Geometry edit(Geometry geometryWithSelectedItems, Collection<Geometry> selectedItems) {
                if (DeleteVertexTool.this.wasClick() && !verticesDeleted.isEmpty()) {
                    Assert.isTrue((verticesDeleted.size() == 1 ? 1 : 0) != 0);
                    return geometryWithSelectedItems;
                }
                if (!box.getEnvelopeInternal().intersects(geometryWithSelectedItems.getEnvelopeInternal())) {
                    return geometryWithSelectedItems;
                }
                Collection<Coordinate> verticesInBox = VerticesInFencePlugIn.verticesInFence(selectedItems, box, true);
                if (DeleteVertexTool.this.wasClick() && !verticesInBox.isEmpty()) {
                    verticesDeleted.add(verticesInBox.iterator().next());
                } else {
                    verticesDeleted.addAll(verticesInBox);
                }
                return DeleteVertexTool.this.geometryEditor.deleteVertices(geometryWithSelectedItems, verticesInBox);
            }
        }, this.getPanel(), this.getPanel().getContext(), this.getName(), layer, this.isRollingBackInvalidEdits(), false);
        return transaction;
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

    protected Collection<Layer> getLayers() {
        return this.getPanel().getLayerManager().getEditableLayers();
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

    protected Collection<Feature> featuresInRange(Geometry box, Layer layer) throws NoninvertibleTransformException {
        ArrayList<Feature> featuresInRange;
        block6: {
            Collection<Feature> featuresWithSelectedItems;
            block5: {
                featuresWithSelectedItems = this.getPanel().getSelectionManager().getFeaturesWithSelectedItems(layer);
                featuresInRange = new ArrayList<Feature>();
                if (this.isAdjacentEditionActivated()) break block5;
                if (featuresWithSelectedItems.isEmpty()) {
                    return new ArrayList<Feature>();
                }
                for (Feature candidate : featuresWithSelectedItems) {
                    if (!box.getEnvelopeInternal().intersects(candidate.getGeometry().getEnvelopeInternal())) continue;
                    featuresInRange.add(candidate);
                }
                break block6;
            }
            if (!this.isInDistanceSelected(box, featuresWithSelectedItems)) break block6;
            HashSet<Feature> candidateNeighbours = new HashSet<Feature>();
            try {
                candidateNeighbours.addAll(Utils.intersectingFeatures(layer, box.getEnvelopeInternal()));
                for (Feature candidate : candidateNeighbours) {
                    if (!box.getEnvelopeInternal().intersects(candidate.getGeometry().getEnvelopeInternal())) continue;
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

    private boolean isInDistanceSelected(Geometry box, Collection<Feature> featuresWithSelectedItems) {
        boolean ok = false;
        Iterator<Feature> itFeats = featuresWithSelectedItems.iterator();
        while (itFeats.hasNext() && !ok) {
            boolean bl = ok = box.distance(itFeats.next().getGeometry()) < this.modelRange();
        }
        return ok;
    }

    private double modelRange() {
        return 5.0 / this.getPanel().getViewport().getScale();
    }

    public static class VertexContext {
        private Layer layer;
        private List<Feature> featuresToUpdate;
        private List<Geometry> newGeometries;

        public VertexContext(Layer layer, List<Feature> featuresToUpdate, List<Geometry> newGeometries) {
            this.layer = layer;
            this.featuresToUpdate = featuresToUpdate;
            this.newGeometries = newGeometries;
        }

        public List<Feature> getFeaturesToUpdate() {
            return this.featuresToUpdate;
        }

        public Layer getLayer() {
            return this.layer;
        }

        public List<Geometry> getNewGeometries() {
            return this.newGeometries;
        }
    }
}

