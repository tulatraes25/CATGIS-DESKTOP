/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.Utils;

public class MoveVertexTool
extends DragTool {
    private static final Logger LOGGER = Logger.getLogger(MoveVertexTool.class);
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.MoveVertexTool.name");
    public static final Icon ICON = IconLoader.icon("MoveVertex.gif");
    public static final Cursor CURSOR = MoveVertexTool.createCursor(IconLoader.icon("MoveVertexCursor3.gif").getImage());
    public static final int TOLERANCE = 5;
    protected EnableCheckFactory checkFactory;
    private boolean checkEditable = true;
    protected SnapIndicatorTool snapIndicatorTool;
    protected static final double POINT_MARKER_SIZE = 3.0;

    public MoveVertexTool(EnableCheckFactory checkFactory) {
        this(checkFactory, true);
    }

    public MoveVertexTool(EnableCheckFactory checkFactory, boolean setStroke) {
        this.checkFactory = checkFactory;
        if (setStroke) {
            this.setColor(new Color(194, 179, 205));
            this.setStroke(new BasicStroke(3.0f));
        }
        this.allowSnapping();
    }

    public MoveVertexTool(EnableCheckFactory checkFactory, boolean setStroke, boolean checkEditable) {
        this.checkFactory = checkFactory;
        if (setStroke) {
            this.setColor(new Color(194, 179, 205));
            this.setStroke(new BasicStroke(3.0f));
        }
        this.allowSnapping();
        this.checkEditable = checkEditable;
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
        WorkbenchContext context = this.getWorkbench().getContext();
        SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        Map<Layer, Collection<Feature>> featSelection = this.getSelectedFeaturesForAffectedLayers(selectionManager);
        if (featSelection.isEmpty()) {
            return;
        }
        Coordinate validModelDestination = this.getValidModelDestination(featSelection, this.getModelSource(), this.getModelDestination());
        if (validModelDestination == null) {
            return;
        }
        boolean selectAdjacents = this.isAdjacentEditionActivated();
        Envelope oldVertexEnvelope = this.vertexBuffer(this.getModelSource());
        if (selectAdjacents) {
            boolean recalculateSelection = false;
            for (Layer currentLayer : featSelection.keySet()) {
                Collection<Feature> selected = featSelection.get(currentLayer);
                for (Feature currentFeat : selected) {
                    List<Feature> colindantes;
                    if (!oldVertexEnvelope.intersects(currentFeat.getGeometry().getEnvelopeInternal()) || (colindantes = Utils.getColindantes(currentFeat.getGeometry(), currentLayer)).isEmpty()) continue;
                    recalculateSelection = true;
                    selectionManager.getFeatureSelection().selectItems(currentLayer, colindantes);
                }
            }
            if (recalculateSelection) {
                featSelection = this.getSelectedFeaturesForAffectedLayers(selectionManager);
                this.insertVertexesIfNecesary(featSelection);
            }
        }
        this.gestureFinished(featSelection, oldVertexEnvelope, validModelDestination);
    }

    protected void insertVertexesIfNecesary(Map<Layer, Collection<Feature>> selected) {
    }

    protected Coordinate getValidModelDestination(Map<Layer, Collection<Feature>> featSelection, Coordinate modelSource, Coordinate modelDestination) {
        return modelDestination;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mousePressed(e, this.checkEditable);
    }

    public void mousePressed(final MouseEvent e, boolean check) {
        try {
            if (check) {
                if (!this.check(this.checkFactory.createAtLeastNLayersMustBeEditableCheck(1))) {
                    return;
                }
                if (!this.check(this.checkFactory.createAtLeastNItemsMustBeSelectedCheck(1))) {
                    return;
                }
                if (!this.check(new EnableCheck(){

                    @Override
                    public String check(JComponent component) {
                        try {
                            return !MoveVertexTool.this.nearSelectionHandle(e.getPoint()) ? I18N.getString("workbench.ui.cursortool.editing.MoveVertexTool.no-editable-selection-handles-here") : null;
                        }
                        catch (Exception e2) {
                            return e2.toString();
                        }
                    }
                })) {
                    return;
                }
            }
            super.mousePressed(e);
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    protected void superMousePressed(MouseEvent e) {
        super.mousePressed(e);
    }

    public boolean nearSelectionHandle(Point2D p) throws NoninvertibleTransformException {
        final Envelope buffer = this.vertexBuffer(this.getPanel().getViewport().toModelCoordinate(p));
        final boolean[] result = new boolean[1];
        Collection<Layer> affectedLayers = this.getLayers();
        for (Layer layer : this.getPanel().getSelectionManager().getLayersWithSelectedItems()) {
            if (!affectedLayers.contains(layer)) continue;
            for (Geometry item : this.getPanel().getSelectionManager().getSelectedItems(layer)) {
                item.apply(new CoordinateFilter(){

                    public void filter(Coordinate coord) {
                        if (buffer.contains(coord)) {
                            result[0] = true;
                        }
                    }
                });
            }
        }
        return result[0];
    }

    protected Envelope vertexBuffer(Coordinate c) throws NoninvertibleTransformException {
        double tolerance = 5.0 / this.getPanel().getViewport().getScale();
        return this.vertexBuffer(c, tolerance);
    }

    public Map<Layer, Collection<Feature>> getSelectedFeaturesForAffectedLayers(SelectionManager manager) {
        HashMap<Layer, Collection<Feature>> result = new HashMap<Layer, Collection<Feature>>();
        for (Layer currentLayer : this.getLayers()) {
            Collection<Feature> selectedFeats = manager.getFeaturesWithSelectedItems(currentLayer);
            if (selectedFeats.isEmpty()) continue;
            result.put(currentLayer, selectedFeats);
        }
        return result;
    }

    protected void gestureFinished(Map<Layer, Collection<Feature>> featSelection, Envelope oldVertexBuffer, Coordinate newVertex) throws Exception {
        SelectionManager selectionManager = this.getPanel().getSelectionManager();
        ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        Layer editableLayer = featSelection.keySet().iterator().next();
        Collection<Feature> selectedFeatures = featSelection.get(editableLayer);
        boolean hasBeenWarned = false;
        for (Feature currentFeature : selectedFeatures) {
            featsSelectedToUpdate.add(currentFeature);
            Feature cloneFeature = currentFeature.clone(true);
            Geometry cloneGeom = this.applyCoordinateFilter(cloneFeature.getGeometry(), oldVertexBuffer, newVertex);
            if (!hasBeenWarned && !cloneGeom.isValid()) {
                if (this.isRollingBackInvalidEdits()) {
                    this.getPanel().getContext().warnUser(EditTransaction.INVALID_GEOMETRY_CANCELLED);
                    return;
                }
                this.getPanel().getContext().warnUser(EditTransaction.INVALID_GEOMETRY);
                hasBeenWarned = true;
            }
            cloneFeature.setGeometry(cloneGeom);
            featsToUpdate.add(cloneFeature);
        }
        this.executeCommand(selectionManager, featsSelectedToUpdate, featsToUpdate, editableLayer, newVertex);
    }

    protected void executeCommand(final SelectionManager selectionManager, final List<Feature> featsSelectedToUpdate, final List<Feature> featsToUpdate, final Layer editableLayer, final Coordinate newVertex) throws Exception {
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(editableLayer, featsSelectedToUpdate);
                try {
                    if (!featsToUpdate.isEmpty()) {
                        editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        editableLayer.getLayerManager().fireGeometryModified(featsToUpdate, editableLayer, featsSelectedToUpdate);
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featsToUpdate);
                    }
                    try {
                        Animations.drawExpandingRing(MoveVertexTool.this.getPanel().getViewport().toViewPoint(newVertex), false, Color.green, MoveVertexTool.this.getPanel(), null);
                    }
                    catch (Throwable t) {
                        LOGGER.error((Object)"", t);
                    }
                }
                catch (TopologyRelationException e) {
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsSelectedToUpdate);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToUpdate.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    editableLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, editableLayer, featsToUpdate);
                }
                selectionManager.getFeatureSelection().selectItems(editableLayer, featsSelectedToUpdate);
            }
        });
    }

    protected Geometry applyCoordinateFilter(Geometry geometry, final Envelope oldVertexBuffer, final Coordinate newVertex) {
        Geometry solution = (Geometry)geometry.clone();
        solution.apply(new CoordinateFilter(){

            public void filter(Coordinate coordinate) {
                if (oldVertexBuffer.contains(coordinate)) {
                    coordinate.x = newVertex.x;
                    coordinate.y = newVertex.y;
                }
            }
        });
        solution.geometryChanged();
        return solution;
    }

    protected EditTransaction createTransaction(Layer layer, final Envelope oldVertexBuffer, final Coordinate newVertex) {
        return EditTransaction.createTransactionOnSelection(new EditTransaction.SelectionEditor(){

            @Override
            public Geometry edit(Geometry geometryWithSelectedItems, Collection<Geometry> selectedItems) {
                for (Geometry item : selectedItems) {
                    this.edit(item);
                }
                return geometryWithSelectedItems;
            }

            private void edit(Geometry selectedItem) {
                selectedItem.apply(new CoordinateFilter(){

                    public void filter(Coordinate coordinate) {
                        if (oldVertexBuffer.contains(coordinate)) {
                            coordinate.x = newVertex.x;
                            coordinate.y = newVertex.y;
                        }
                    }
                });
            }
        }, this.getPanel(), this.getPanel().getContext(), this.getName(), layer, this.isRollingBackInvalidEdits(), false);
    }

    @Override
    protected Shape getShape(Point2D source, Point2D destination) throws Exception {
        Rectangle2D.Double pointMarker = new Rectangle2D.Double(0.0, 0.0, 3.0, 3.0);
        pointMarker.x = destination.getX() - 1.5;
        pointMarker.y = destination.getY() - 1.5;
        return pointMarker;
    }

    private Envelope vertexBuffer(Coordinate vertex, double tolerance) {
        return new Envelope(vertex.x - tolerance, vertex.x + tolerance, vertex.y - tolerance, vertex.y + tolerance);
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
}

