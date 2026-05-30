/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.util.LinearComponentExtracter
 *  com.vividsolutions.jts.operation.distance.DistanceOp
 *  org.apache.commons.collections.CollectionUtils
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.algorithm.LengthSubstring;
import com.vividsolutions.jump.algorithm.LengthToPoint;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractClickSelectedLineStringsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.saig.jump.lang.I18N;

public class SplitLineStringTool
extends AbstractClickSelectedLineStringsTool {
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.SplitLineStringTool.Split-line");
    public static final Icon ICON = IconLoader.icon("splitLine.png");
    public static final Cursor CURSOR = SplitLineStringTool.createCursor(IconLoader.icon("splitLineCursor.png").getImage(), new java.awt.Point(0, 0));
    private SnapIndicatorTool snapIndicatorTool;

    public SplitLineStringTool() {
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

    @Override
    protected void gestureFinished(Collection<Feature> nearbyLineStringFeatures) throws Exception {
        this.reportNothingToUndoYet();
        Feature closestFeature = this.closest(nearbyLineStringFeatures, this.getModelClickPoint());
        if (closestFeature == null) {
            this.getWorkbench().getFrame().warnUser(NO_SELECTED_LINESTRINGS_HERE_MESSAGE);
            return;
        }
        Geometry closestGeometry = closestFeature.getGeometry();
        LineString closestPart = this.closestPart(closestGeometry, this.getModelClickPoint().getCoordinate());
        int i = 0;
        while (i < closestPart.getNumGeometries()) {
            LineString currentPart = (LineString)closestPart.getGeometryN(i);
            if (CollectionUtil.list(currentPart.getStartPoint().getCoordinate(), currentPart.getEndPoint().getCoordinate()).contains(DistanceOp.nearestPoints((Geometry)currentPart, (Geometry)this.getModelClickPoint())[0])) {
                this.getWorkbench().getFrame().warnUser(NO_SELECTED_LINESTRINGS_HERE_MESSAGE);
                return;
            }
            ++i;
        }
        this.split(closestFeature, this.getModelDestination(), this.layer(closestFeature, this.layerToSpecifiedFeaturesMap()));
    }

    protected void split(Feature feature, Coordinate coordinate, final Layer editableLayer) throws Exception {
        Geometry originalGeometry = feature.getGeometry();
        LineString closestPart = this.closestPart(originalGeometry, coordinate);
        Geometry otherParts = this.getOtherParts(originalGeometry, closestPart);
        LineString[] fragments = this.split(closestPart, coordinate, false);
        final SelectionManager selectionManager = this.getPanel().getSelectionManager();
        final ArrayList<Feature> featsToAdd = new ArrayList<Feature>();
        final ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        final ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        final ArrayList<Point2D> centers = new ArrayList<Point2D>();
        centers.add(this.getPanel().getViewport().toViewPoint(coordinate));
        featsSelectedToUpdate.add(feature);
        int i = 0;
        while (i < fragments.length) {
            LineString currentFragment = fragments[i];
            currentFragment.geometryChanged();
            Feature clonedFeature = null;
            if (i == 0) {
                clonedFeature = feature.clone(true);
                clonedFeature.setGeometry((Geometry)currentFragment);
                featsToUpdate.add(clonedFeature);
            } else {
                clonedFeature = feature.clone(true, true);
                if (otherParts != null) {
                    ArrayList<LineString> parts = new ArrayList<LineString>();
                    parts.add(currentFragment);
                    parts.addAll(LinearComponentExtracter.getLines((Geometry)otherParts));
                    clonedFeature.setGeometry(geomFac.buildGeometry(parts));
                } else {
                    clonedFeature.setGeometry((Geometry)currentFragment);
                }
                clonedFeature.setAttribute(clonedFeature.getSchema().getPrimaryKeyName(), null);
                clonedFeature.setID(FeatureUtil.nextID());
                featsToAdd.add(clonedFeature);
            }
            ++i;
        }
        if (!this.checkConditions()) {
            return;
        }
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + I18N.getMessage("com.vividsolutions.jump.workbench.ui.cursortool.SplitLineStringTool.{0}-fragments", new Object[]{fragments.length})){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                Animations.drawExpandingRings(centers, true, Color.BLUE, SplitLineStringTool.this.getPanel(), new float[]{5.0f, 5.0f});
                if (!featsToAdd.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsToAdd);
                }
                if (!featsToUpdate.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                    editableLayer.getLayerManager().fireGeometryModified(featsToUpdate, editableLayer, featsSelectedToUpdate);
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsToUpdate);
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToAdd.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().removeAll(featsToAdd);
                }
                if (!featsToUpdate.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    editableLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, editableLayer, featsToUpdate);
                }
                selectionManager.getFeatureSelection().selectItems(editableLayer, featsSelectedToUpdate);
            }
        });
    }

    protected Geometry getOtherParts(Geometry originalGeometry, LineString closestPart) {
        Geometry result = null;
        ArrayList<Geometry> otherParts = new ArrayList<Geometry>();
        int i = 0;
        while (i < originalGeometry.getNumGeometries()) {
            Geometry currentPart = originalGeometry.getGeometryN(i);
            if (!currentPart.equals((Geometry)closestPart)) {
                otherParts.add((Geometry)currentPart.clone());
            }
            ++i;
        }
        if (CollectionUtils.isNotEmpty(otherParts)) {
            result = geomFac.buildGeometry(otherParts);
        }
        return result;
    }

    protected LineString[] split(LineString lineString, Coordinate target, boolean moveSplitToTarget) {
        LineString[] lineStrings = new LineString[]{LengthSubstring.getSubstring(lineString, 0.0, LengthToPoint.length(lineString, target)), LengthSubstring.getSubstring(lineString, LengthToPoint.length(lineString, target), lineString.getLength())};
        if (moveSplitToTarget) {
            this.last(lineStrings[0]).setCoordinate(target);
            this.first(lineStrings[1]).setCoordinate(target);
        }
        if (Double.isNaN(this.last((LineString)lineStrings[0]).z)) {
            this.last((LineString)lineStrings[0]).z = this.interpolateZ(lineStrings);
        }
        if (Double.isNaN(this.first((LineString)lineStrings[1]).z)) {
            this.first((LineString)lineStrings[1]).z = this.interpolateZ(lineStrings);
        }
        return lineStrings;
    }

    private double interpolateZ(LineString[] lineStrings) {
        Coordinate a = this.secondToLast(lineStrings[0]);
        Coordinate b = this.last(lineStrings[0]);
        Coordinate c = this.second(lineStrings[1]);
        if (Double.isNaN(a.z)) {
            return Double.NaN;
        }
        if (Double.isNaN(c.z)) {
            return Double.NaN;
        }
        return a.z + (c.z - a.z) * a.distance(b) / (a.distance(b) + b.distance(c));
    }

    private Coordinate first(LineString lineString) {
        return lineString.getCoordinateN(0);
    }

    private Coordinate second(LineString lineString) {
        return lineString.getCoordinateN(1);
    }

    private Coordinate last(LineString lineString) {
        return lineString.getCoordinateN(lineString.getNumPoints() - 1);
    }

    private Coordinate secondToLast(LineString lineString) {
        return lineString.getCoordinateN(lineString.getNumPoints() - 2);
    }

    private Feature closest(Collection<Feature> features, Point point) {
        Feature closestFeature = null;
        double closestDistance = Double.MAX_VALUE;
        for (Feature feature : features) {
            double distance = feature.getGeometry().distance((Geometry)point);
            if (!(distance < closestDistance)) continue;
            closestFeature = feature;
            closestDistance = distance;
        }
        return closestFeature;
    }

    protected LineString closestPart(Geometry geom, Coordinate coord) {
        LineString closestPart = null;
        Point targetPoint = geomFac.createPoint(coord);
        double closestDistance = Double.MAX_VALUE;
        int i = 0;
        while (i < geom.getNumGeometries()) {
            LineString currentPart = (LineString)geom.getGeometryN(i);
            double distance = currentPart.distance((Geometry)targetPoint);
            if (distance < closestDistance) {
                closestPart = currentPart;
                closestDistance = distance;
            }
            ++i;
        }
        return closestPart;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createOnlyOneLayerMayHaveSelectedFeaturesCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 15}));
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(new int[]{3, 2}, new int[]{9}, 1));
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
}

