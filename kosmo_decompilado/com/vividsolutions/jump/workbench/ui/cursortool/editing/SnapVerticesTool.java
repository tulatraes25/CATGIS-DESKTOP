/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesOp;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.VerticesInFencePlugIn;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.ZManager;

public class SnapVerticesTool
extends SpecifyFeaturesTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.SnapVerticesTool.name");
    public static final Icon ICON = IconLoader.icon("QuickSnap.gif");
    public static final Cursor CURSOR = SnapVerticesTool.createCursor(IconLoader.icon("QuickSnapCursor.gif").getImage());
    private EnableCheckFactory checkFactory;

    public SnapVerticesTool(EnableCheckFactory checkFactory) {
        this.checkFactory = checkFactory;
        this.setColor(Color.green.darker());
        this.setStroke(new BasicStroke(1.0f, 0, 2, 0.0f));
        this.setViewClickBuffer(8);
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
        Feature targetFeature;
        this.reportNothingToUndoYet();
        if (!this.check(this.checkFactory.createAtLeastNLayersMustBeEditableCheck(1))) {
            return;
        }
        Coordinate suggestedTarget = EnvelopeUtil.centre(this.getBoxInModelCoordinates());
        if (ZManager.isZUseActive()) {
            suggestedTarget.z = ZManager.getActiveZ();
        }
        if (!this.check(new EnableCheck(targetFeature = this.targetFeature(suggestedTarget, this.getBoxInModelCoordinates())){
            private final /* synthetic */ Feature val$targetFeature;
            {
                this.val$targetFeature = feature;
            }

            @Override
            public String check(JComponent component) {
                return this.val$targetFeature == null ? I18N.getString("workbench.ui.cursortool.editing.SnapVerticesTool.no-vertices-or-edges-here") : null;
            }
        })) {
            return;
        }
        new SnapVerticesOp().execute(EnvelopeUtil.toGeometry(this.getBoxInModelCoordinates()), this.getPanel().getLayerManager().getEditableLayers(), this.isRollingBackInvalidEdits(), this.getPanel(), this.getTaskFrame().getTask(), suggestedTarget, targetFeature, JUMPWorkbench.getBlackboard().get(SnapVerticesOp.INSERT_VERTICES_IF_NECESSARY_KEY, true));
    }

    private Feature targetFeature(Coordinate suggestedTarget, Envelope fence) throws Exception {
        Feature targetFeature = this.targetFeature(suggestedTarget, fence, false);
        if (targetFeature == null) {
            targetFeature = this.targetFeature(suggestedTarget, fence, true);
        }
        return targetFeature;
    }

    private Feature targetFeature(Coordinate suggestedTarget, Envelope fence, boolean fromEditableLayers) throws Exception {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        Iterator<Layer> i = this.getPanel().getLayerManager().iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            if (layer.isEditable() != fromEditableLayers || layer.getName().equals(FenceLayerFinder.LAYER_NAME)) continue;
            layers.add(layer);
        }
        ArrayList<Feature> candidateFeatures = new ArrayList<Feature>();
        Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap = this.layerToSpecifiedFeaturesMap(layers.toArray());
        for (Layer layer : layers) {
            Collection<Feature> layerFeatures = layerToSpecifiedFeaturesMap.get(layer);
            if (layerFeatures == null) continue;
            candidateFeatures.addAll(layerFeatures);
        }
        Feature targetFeature = null;
        double distanceToTargetVertices = -1.0;
        for (Feature candidate : candidateFeatures) {
            double distanceToCandidateVertices = this.distanceToVertices(suggestedTarget, candidate, fence);
            if (distanceToCandidateVertices == -1.0 || targetFeature != null && !(distanceToCandidateVertices < distanceToTargetVertices)) continue;
            targetFeature = candidate;
            distanceToTargetVertices = distanceToCandidateVertices;
        }
        return targetFeature;
    }

    private double distanceToVertices(Coordinate referenceCoordinate, Feature feature, Envelope vertexFilter) {
        double distanceToVertices = -1.0;
        for (Coordinate vertex : VerticesInFencePlugIn.verticesInFence(feature.getGeometry(), EnvelopeUtil.toGeometry(vertexFilter), true).getCoordinates()) {
            double distanceToVertex = vertex.distance(referenceCoordinate);
            if (distanceToVertices != -1.0 && !(distanceToVertex < distanceToVertices)) continue;
            distanceToVertices = distanceToVertex;
        }
        return distanceToVertices;
    }

    @Override
    protected Envelope getBoxInModelCoordinates() throws NoninvertibleTransformException {
        return EnvelopeUtil.expand(new Envelope(this.getModelSource(), this.getModelDestination()), this.modelClickBuffer());
    }

    @Override
    protected Shape getShape() throws Exception {
        return this.getPanel().getViewport().toViewRectangle(this.getBoxInModelCoordinates());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 3, 2}));
        solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        return solucion;
    }
}

