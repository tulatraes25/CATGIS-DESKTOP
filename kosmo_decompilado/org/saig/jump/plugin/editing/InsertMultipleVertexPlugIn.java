/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.Point
 *  javax.vecmath.Point2d
 *  javax.vecmath.Point3d
 *  javax.vecmath.Tuple3d
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.editing.InsertMultipleVertexDialog;

public class InsertMultipleVertexPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final int MONITOR_SHOW_STEP = 10;
    private static final double ROUND_TOLERANCE = 1.0E-9;
    public static final Logger LOGGER = Logger.getLogger(InsertMultipleVertexPlugIn.class);
    public static final String NAME = I18N.getString(InsertMultipleVertexPlugIn.class, "insert-vertex");
    public static final Icon ICON = IconLoader.icon("InsertMultipleVertex.png");
    private final Point snap;
    private int selectedOption;
    private int nPuntos;
    private double distance;
    private final double modelRange;
    private Feature feat;
    protected Layer layerOrig;
    private WorkbenchContext wContext;
    protected final LayerViewPanel layerViewPanel;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return InsertMultipleVertexPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public InsertMultipleVertexPlugIn(Point snap, double modelRange, LayerViewPanel layerViewPanel) {
        this.snap = snap;
        this.modelRange = modelRange;
        this.layerViewPanel = layerViewPanel;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.wContext = context.getWorkbenchContext();
        this.layerOrig = this.getLayer();
        this.feat = ((SelectionManagerProxy)((Object)this.wContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getFeaturesWithSelectedItems(this.layerOrig).iterator().next();
        InsertMultipleVertexDialog dialog = new InsertMultipleVertexDialog(JUMPWorkbench.getFrameInstance(), true);
        dialog.setVisible(true);
        if (dialog.isExitOk()) {
            this.selectedOption = dialog.getSelectedOption();
            this.nPuntos = dialog.getNPuntos();
            this.distance = dialog.getDistancia();
            return true;
        }
        return false;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 5, 4}));
        boolean n = true;
        solucion.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (!workbenchContext.getLayerManager().getEditableLayers().isEmpty()) {
                    Layer layer = workbenchContext.getLayerManager().getEditableLayers().iterator().next();
                    boolean hasFrame = workbenchContext.getWorkbench().getFrame() != null && workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() != null && workbenchContext.getWorkbench().getFrame().getActiveInternalFrame() instanceof SelectionManagerProxy;
                    int numSelected = -1;
                    if (hasFrame) {
                        numSelected = ((SelectionManagerProxy)((Object)workbenchContext.getWorkbench().getFrame().getActiveInternalFrame())).getSelectionManager().getFeatureSelection().getNumFeaturesWithSelectedItems(layer);
                    }
                    if (numSelected == -1 || 1 != numSelected) {
                        return I18N.getString(InsertMultipleVertexPlugIn.class, "exactly-one-element-must-be-selected-in-editable-layer");
                    }
                }
                return null;
            }
        });
        return solucion;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        Geometry geomOrig = this.feat.getGeometry();
        LineSegment segmentoInicial = this.segmentInRange(geomOrig, this.snap.getCoordinate(), this.modelRange);
        if (segmentoInicial == null) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(InsertMultipleVertexPlugIn.class, "initial-segment-not-located"));
            return;
        }
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
        ArrayList<ExtendedSegmentContext> segments = new ArrayList<ExtendedSegmentContext>();
        Geometry simplyGeometryToProcess = this.findSimplyGeomToProcess(geomOrig, segmentoInicial);
        int contInsertados = 0;
        int contExistentes = 0;
        int contTotales = 0;
        if (this.selectedOption == 1) {
            this.nPuntos = 1;
            contTotales = 1;
        } else if (this.selectedOption == 3) {
            contTotales = this.estimateNumPoints(simplyGeometryToProcess, this.snap.getCoordinate());
            this.nPuntos = -1;
        } else {
            contTotales = this.nPuntos;
        }
        LineSegment segmentoActual = new LineSegment(new Coordinate(segmentoInicial.p0), new Coordinate(segmentoInicial.p1));
        Coordinate coordenadaActual = this.getInitialCoordinate(segmentoActual, this.snap.getCoordinate());
        while (this.nPuntos != 0 && segmentoActual != null && !monitor.isCancelRequested()) {
            double distanciaACubrir = this.distance;
            while (segmentoActual != null && distanciaACubrir > coordenadaActual.distance(segmentoActual.p1)) {
                distanciaACubrir -= coordenadaActual.distance(segmentoActual.p1);
                if ((segmentoActual = this.nextLineSegment(simplyGeometryToProcess, segmentoActual)) == null) continue;
                coordenadaActual = segmentoActual.p0;
            }
            if (segmentoActual == null) continue;
            if (Math.abs(distanciaACubrir - coordenadaActual.distance(segmentoActual.p1)) < 1.0E-9) {
                coordenadaActual = new Coordinate(segmentoActual.p1);
                segmentoActual = this.nextLineSegment(simplyGeometryToProcess, segmentoActual);
                ++contExistentes;
            } else {
                coordenadaActual = this.getCoordinateInLineSegment(new LineSegment(coordenadaActual, segmentoActual.p1), distanciaACubrir);
                coordinates.add(coordenadaActual);
                segments.add(new ExtendedSegmentContext(this.layerOrig, this.feat, coordenadaActual));
                simplyGeometryToProcess = new GeometryEditor().insertVertex(simplyGeometryToProcess, segmentoActual.p0, segmentoActual.p1, coordenadaActual);
                segmentoActual = new LineSegment(coordenadaActual, segmentoActual.p1);
                ++contInsertados;
            }
            if ((contInsertados + contExistentes) % 10 == 0) {
                monitor.report(I18N.getString(InsertMultipleVertexPlugIn.class, "generating-main-vertex"));
                monitor.report(contInsertados + contExistentes, contTotales, I18N.getString(InsertMultipleVertexPlugIn.class, "processed-coordinates"));
            }
            distanciaACubrir = this.distance;
            --this.nPuntos;
        }
        monitor.report(I18N.getString(InsertMultipleVertexPlugIn.class, "generating-main-vertex"));
        monitor.report(contInsertados + contExistentes, contTotales, I18N.getString(InsertMultipleVertexPlugIn.class, "processed-coordinates"));
        if (monitor.isCancelRequested()) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(InsertMultipleVertexPlugIn.class, "process-cancelled-by-the-user"));
            return;
        }
        int layerType = this.layerOrig.getFeatureCollectionWrapper().getFeatureSchema().getGeometryType();
        if (EditOptionsPanel.isAdjacentEditionActivated() && (layerType == 5 || layerType == 4)) {
            contTotales = coordinates.size();
            int coordCont = 0;
            Collection<Feature> adjacentFeatures = this.getAdjacentFeatures(simplyGeometryToProcess);
            adjacentFeatures.remove(this.feat);
            Iterator coordIt = coordinates.iterator();
            while (coordIt.hasNext() && !monitor.isCancelRequested()) {
                Coordinate coordinate = (Coordinate)coordIt.next();
                Collection<Feature> featuresInRange = this.featuresInRange(coordinate, adjacentFeatures);
                for (Feature feature : featuresInRange) {
                    Geometry selectedItem = feature.getGeometry();
                    LineSegment segment = this.segmentInRange(selectedItem, coordinate, 1.0E-9);
                    if (segment == null) continue;
                    segments.add(new ExtendedSegmentContext(this.layerOrig, feature, coordinate));
                }
                if (++coordCont % 10 != 0) continue;
                monitor.report(I18N.getString(InsertMultipleVertexPlugIn.class, "generating-vertex-of-adjacents-elements"));
                monitor.report(coordCont, contTotales, I18N.getString(InsertMultipleVertexPlugIn.class, "processed-coordinates"));
            }
            monitor.report(I18N.getString(InsertMultipleVertexPlugIn.class, "generating-vertex-of-adjacents-elements"));
            monitor.report(coordCont, contTotales, I18N.getString(InsertMultipleVertexPlugIn.class, "processed-coordinates"));
            if (monitor.isCancelRequested()) {
                JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(InsertMultipleVertexPlugIn.class, "process-cancelled-by-the-user"));
                return;
            }
        }
        HashMap<Feature, Geometry> geometries = new HashMap<Feature, Geometry>();
        contTotales = segments.size();
        int segmentsCont = 0;
        Iterator iterator = segments.iterator();
        while (iterator.hasNext() && !monitor.isCancelRequested()) {
            ExtendedSegmentContext currentSegment = (ExtendedSegmentContext)iterator.next();
            if (!geometries.containsKey(currentSegment.getFeature())) {
                geometries.put(currentSegment.getFeature(), (Geometry)currentSegment.getFeature().getGeometry().clone());
            }
            Geometry geom = (Geometry)geometries.get(currentSegment.getFeature());
            LineSegment tempLineSeg = this.segmentInRange(geom, currentSegment.getCoordinate(), this.modelRange);
            if (!currentSegment.getCoordinate().equals2D(tempLineSeg.p0) && !currentSegment.getCoordinate().equals2D(tempLineSeg.p0)) {
                geom = new GeometryEditor().insertVertex(geom, tempLineSeg.p0, tempLineSeg.p1, currentSegment.getCoordinate());
            }
            geometries.put(currentSegment.getFeature(), geom);
            if (++segmentsCont % 10 != 0) continue;
            monitor.report(I18N.getString(InsertMultipleVertexPlugIn.class, "processing-vertex"));
            monitor.report(segmentsCont, contTotales, I18N.getString(InsertMultipleVertexPlugIn.class, "processed-vertex"));
        }
        monitor.report(I18N.getString(InsertMultipleVertexPlugIn.class, "processing-vertex"));
        monitor.report(segmentsCont, contTotales, I18N.getString(InsertMultipleVertexPlugIn.class, "processed-vertex"));
        if (monitor.isCancelRequested()) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(InsertMultipleVertexPlugIn.class, "process-cancelled-by-the-user"));
            return;
        }
        SelectionManager selectionManager = this.layerViewPanel.getSelectionManager();
        ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        for (Feature feature : geometries.keySet()) {
            featsSelectedToUpdate.add(feature);
            Feature cloneFeat = (Feature)feature.clone();
            cloneFeat.setGeometry((Geometry)geometries.get(feature));
            featsToUpdate.add(cloneFeat);
        }
        this.applyChanges(selectionManager, featsSelectedToUpdate, featsToUpdate, coordinates, context);
        String warnText = I18N.getMessage(InsertMultipleVertexPlugIn.class, "{0}-inserted-vertex-{1}-existent", new Object[]{contInsertados, contExistentes});
        if (this.nPuntos > 0) {
            warnText = String.valueOf(warnText) + I18N.getMessage(InsertMultipleVertexPlugIn.class, "missing-{0}-vertex", new Object[]{this.nPuntos});
        }
        JUMPWorkbench.getFrameInstance().warnUser(warnText);
        CursorTool cursor = context.getWorkbenchContext().getWorkbench().getFrame().getToolBar().getDefaultEditingCursorTool();
        this.layerViewPanel.setCurrentCursorTool(cursor);
    }

    protected void applyChanges(final SelectionManager selectionManager, final List<Feature> featsSelectedToUpdate, final List<Feature> featsToUpdate, final List<Coordinate> coordinates, PlugInContext context) throws Exception {
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(InsertMultipleVertexPlugIn.this.layerOrig, featsSelectedToUpdate);
                try {
                    if (!featsToUpdate.isEmpty()) {
                        InsertMultipleVertexPlugIn.this.layerOrig.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        InsertMultipleVertexPlugIn.this.layerOrig.getLayerManager().fireGeometryModified(featsToUpdate, InsertMultipleVertexPlugIn.this.layerOrig, featsSelectedToUpdate);
                        selectionManager.getFeatureSelection().selectItems(InsertMultipleVertexPlugIn.this.layerOrig, featsToUpdate);
                    }
                    try {
                        if (coordinates.size() <= 10) {
                            for (Coordinate coordinate2 : coordinates) {
                                Animations.drawExpandingRing(InsertMultipleVertexPlugIn.this.layerViewPanel.getViewport().toViewPoint(coordinate2), false, Color.green, InsertMultipleVertexPlugIn.this.layerViewPanel, null);
                            }
                        } else {
                            double ratio = Double.valueOf(coordinates.size() - 1) / 9.0;
                            int i = 0;
                            while (i < 10) {
                                Coordinate coordinate2 = (Coordinate)coordinates.get(Double.valueOf(Math.floor(ratio * (double)i)).intValue());
                                Animations.drawExpandingRing(InsertMultipleVertexPlugIn.this.layerViewPanel.getViewport().toViewPoint(coordinate2), false, Color.green, InsertMultipleVertexPlugIn.this.layerViewPanel, null);
                                ++i;
                            }
                        }
                    }
                    catch (Throwable t) {
                        LOGGER.error((Object)"", t);
                    }
                }
                catch (TopologyRelationException e) {
                    selectionManager.getFeatureSelection().selectItems(InsertMultipleVertexPlugIn.this.layerOrig, featsSelectedToUpdate);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(InsertMultipleVertexPlugIn.this.layerOrig);
                if (!featsToUpdate.isEmpty()) {
                    InsertMultipleVertexPlugIn.this.layerOrig.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    InsertMultipleVertexPlugIn.this.layerOrig.getLayerManager().fireGeometryModified(featsSelectedToUpdate, InsertMultipleVertexPlugIn.this.layerOrig, featsToUpdate);
                }
                selectionManager.getFeatureSelection().selectItems(InsertMultipleVertexPlugIn.this.layerOrig, featsSelectedToUpdate);
            }
        }, context);
    }

    public Collection<Feature> getAdjacentFeatures(Geometry simplyGeometryToProcess) throws Exception {
        List<Feature> candidatos = this.layerOrig.getFeatureCollectionWrapper().query(simplyGeometryToProcess.getEnvelopeInternal());
        ArrayList<Feature> result = new ArrayList<Feature>();
        for (Feature feat : candidatos) {
            if (!simplyGeometryToProcess.intersects(feat.getGeometry())) continue;
            result.add(feat);
        }
        return result;
    }

    public int estimateNumPoints(Geometry geometry, Coordinate coordinate) {
        LineSegment tmpSegment = this.segmentInRange(geometry, coordinate, this.modelRange);
        Coordinate initialCoordinate = this.getInitialCoordinate(tmpSegment, coordinate);
        double length = initialCoordinate.distance(tmpSegment.p1);
        tmpSegment = this.nextLineSegment(geometry, tmpSegment);
        while (tmpSegment != null) {
            length += tmpSegment.getLength();
            tmpSegment = this.nextLineSegment(geometry, tmpSegment);
        }
        return (int)(length / this.distance);
    }

    protected Collection<Feature> featuresInRange(Coordinate modelClickCoordinate, Collection adjacentsFeatures) {
        Point modelClickPoint = geomFac.createPoint(modelClickCoordinate);
        if (adjacentsFeatures.isEmpty()) {
            return new ArrayList<Feature>();
        }
        ArrayList<Feature> featuresInRange = new ArrayList<Feature>();
        for (Feature candidate : adjacentsFeatures) {
            if (!(modelClickPoint.distance(candidate.getGeometry()) <= 1.0E-9)) continue;
            featuresInRange.add(candidate);
        }
        return featuresInRange;
    }

    private Geometry findSimplyGeomToProcess(Geometry geomOrig, LineSegment segmentoInicial) {
        if (geomOrig.getNumGeometries() > 1) {
            int i = 0;
            while (i < geomOrig.getNumGeometries()) {
                Geometry geom = geomOrig.getGeometryN(i);
                List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(geom, false);
                for (Coordinate[] coordinates : coordArrays) {
                    int j = 1;
                    while (j < coordinates.length) {
                        LineSegment candidate = new LineSegment(coordinates[j - 1], coordinates[j]);
                        if (candidate.equalsTopo(segmentoInicial)) {
                            return geom;
                        }
                        ++j;
                    }
                }
                ++i;
            }
        }
        return geomOrig;
    }

    protected Coordinate getInitialCoordinate(LineSegment segmentoInicial, Coordinate coordinate) {
        Point2d pOrigen = new Point2d(segmentoInicial.p0.x, segmentoInicial.p0.y);
        Point2d point = new Point2d(coordinate.x, coordinate.y);
        return this.getCoordinateInLineSegment(segmentoInicial, pOrigen.distance(point));
    }

    protected LineSegment nextLineSegment(Geometry geometry, LineSegment lineSegment) {
        LineSegment next = null;
        if (!geometry.isEmpty() && geometry.getNumGeometries() == 1) {
            List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(geometry, false);
            for (Coordinate[] coordinates : coordArrays) {
                int j = 1;
                while (j < coordinates.length) {
                    LineSegment candidate = new LineSegment(coordinates[j - 1], coordinates[j]);
                    if (candidate.equals((Object)lineSegment) && j + 1 < coordinates.length) {
                        return new LineSegment(coordinates[j], coordinates[j + 1]);
                    }
                    ++j;
                }
            }
        }
        return next;
    }

    protected LineSegment segmentInRange(Geometry geometry, Coordinate target, double tolerance) {
        LineSegment closest = null;
        List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(geometry, false);
        for (Coordinate[] coordinates : coordArrays) {
            int j = 1;
            while (j < coordinates.length) {
                LineSegment candidate = new LineSegment(coordinates[j - 1], coordinates[j]);
                if (!(candidate.distance(target) > tolerance) && (closest == null || candidate.distance(target) < closest.distance(target))) {
                    closest = candidate;
                }
                ++j;
            }
        }
        return closest;
    }

    private Coordinate getCoordinateInLineSegment(LineSegment lineSegment, double distanceFromOrigin) {
        Coordinate result = null;
        if (lineSegment.getLength() > distanceFromOrigin) {
            Point3d pFinal = new Point3d(lineSegment.p1.x, lineSegment.p1.y, lineSegment.p1.z);
            Point3d pInicial = new Point3d(lineSegment.p0.x, lineSegment.p0.y, lineSegment.p0.z);
            Point3d pInterpolado = new Point3d();
            pInterpolado.interpolate((Tuple3d)pInicial, (Tuple3d)pFinal, distanceFromOrigin / lineSegment.getLength());
            return new Coordinate(pInterpolado.x, pInterpolado.y, pInterpolado.z);
        }
        if (lineSegment.getLength() == distanceFromOrigin) {
            return new Coordinate(lineSegment.p1);
        }
        return result;
    }

    protected Layer getLayer() {
        return this.wContext.getLayerManager().getEditableLayers().iterator().next();
    }

    public static class ExtendedSegmentContext {
        private final Feature feature;
        private final Layer layer;
        private final Coordinate coordinate;

        public ExtendedSegmentContext(Layer layer, Feature feature, Coordinate coordinate) {
            this.layer = layer;
            this.feature = feature;
            this.coordinate = coordinate;
        }

        public Feature getFeature() {
            return this.feature;
        }

        public Layer getLayer() {
            return this.layer;
        }

        public Coordinate getCoordinate() {
            return this.coordinate;
        }
    }
}

