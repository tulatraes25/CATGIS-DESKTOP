/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
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
import com.vividsolutions.jump.workbench.ui.GeometryEditor;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DeleteVertexTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class DeleteSelectedSegmentsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(DeleteSelectedSegmentsPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.remove");
    public static final Icon ICON = IconLoader.icon("DeleteSelectedSegments.png");
    protected static boolean shiftPressed;
    private static boolean keyRegistered;
    protected GeometryEditor geometryEditor = new GeometryEditor();
    protected LinkedList<Coordinate> consecutiveCoordinateList;
    protected Feature feature;
    protected Layer editableLayer;
    protected PlugInContext context;
    protected SelectionManager selectionManager;
    protected Collection<Geometry> selectedSegments;
    protected TaskMonitor monitor;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.selectionManager = ((SelectionManagerProxy)((Object)JUMPWorkbench.getFrameInstance().getActiveInternalFrame())).getSelectionManager();
        this.editableLayer = this.getLayer();
        Collection<Layer> layersWithSelectedItems = this.selectionManager.getSegmentSelection().getLayersWithSelectedItems();
        if (layersWithSelectedItems.size() != 1 || !layersWithSelectedItems.contains(this.editableLayer)) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.must-only-have-selected-elements-the-editable-layer"), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.warning"));
            return false;
        }
        Collection<Feature> featuresWithSelectedItems = this.selectionManager.getSegmentSelection().getFeaturesWithSelectedItems(this.editableLayer);
        if (featuresWithSelectedItems.size() != 1) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.must-only-be-only-one-element-with-selected-segments"), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.warning"));
            return false;
        }
        this.feature = featuresWithSelectedItems.iterator().next();
        this.selectedSegments = this.selectionManager.getSegmentSelection().getSelectedItems(this.editableLayer, this.feature);
        ArrayList<Geometry> tmpSelectedSegments = new ArrayList<Geometry>(this.selectedSegments);
        if (tmpSelectedSegments.size() < 2) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.at-least-two-consecutive-segments-must-be-selected"), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.warning"));
            return false;
        }
        List<Coordinate[]> coordinateArrayList = CoordinateArrays.toCoordinateArrays(this.feature.getGeometry(), false);
        Iterator selecIt = tmpSelectedSegments.iterator();
        LineString lineStringSegment = (LineString)selecIt.next();
        boolean encontrado = false;
        List<Coordinate> coordList = null;
        Iterator<Coordinate[]> coordArrayIt = coordinateArrayList.iterator();
        while (coordArrayIt.hasNext() && !encontrado) {
            Coordinate[] coordArrayTmp = coordArrayIt.next();
            int i = 0;
            while (i < coordArrayTmp.length - 1) {
                if (coordArrayTmp[i] == lineStringSegment.getStartPoint().getCoordinate() && coordArrayTmp[i + 1] == lineStringSegment.getEndPoint().getCoordinate()) {
                    encontrado = true;
                    coordList = Arrays.asList(coordArrayTmp);
                }
                ++i;
            }
        }
        if (coordList == null) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.not-localized-segment"), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.warning"));
            return false;
        }
        Coordinate coordInicial = (Coordinate)coordList.get(0);
        Coordinate coordFinal = (Coordinate)coordList.get(coordList.size() - 1);
        boolean geometryClosed = coordInicial.equals2D(coordFinal);
        this.consecutiveCoordinateList = new LinkedList();
        if (geometryClosed) {
            this.consecutiveCoordinateList.addFirst(DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lineStringSegment.getStartPoint().getCoordinate()));
            this.consecutiveCoordinateList.addLast(DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lineStringSegment.getEndPoint().getCoordinate()));
        } else {
            this.consecutiveCoordinateList.addFirst(lineStringSegment.getStartPoint().getCoordinate());
            this.consecutiveCoordinateList.addLast(lineStringSegment.getEndPoint().getCoordinate());
        }
        tmpSelectedSegments.remove(lineStringSegment);
        boolean added = true;
        while (added && !tmpSelectedSegments.isEmpty()) {
            selecIt = tmpSelectedSegments.iterator();
            added = false;
            LineString lsTmp = null;
            while (selecIt.hasNext() && !added) {
                lsTmp = (LineString)selecIt.next();
                Coordinate lsStartCoord = lsTmp.getStartPoint().getCoordinate();
                Coordinate lsEndCoord = lsTmp.getEndPoint().getCoordinate();
                if (geometryClosed) {
                    lsStartCoord = DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lsStartCoord);
                    lsEndCoord = DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lsEndCoord);
                }
                if (lsStartCoord == this.consecutiveCoordinateList.getLast()) {
                    added = true;
                    this.consecutiveCoordinateList.addLast(lsEndCoord);
                    continue;
                }
                if (lsEndCoord != this.consecutiveCoordinateList.getFirst()) continue;
                added = true;
                this.consecutiveCoordinateList.addFirst(lsStartCoord);
            }
            if (added) {
                tmpSelectedSegments.remove(lsTmp);
                continue;
            }
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.selected-segments-must-be-consecutive"), I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.warning"));
            return false;
        }
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck check = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        check.add(checkFactory.createTaskWindowMustBeActiveCheck());
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 3, 2, 15}));
        check.add(checkFactory.createAtLeastNSegmentsMustBeSelectedCheck(2));
        check.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return DeleteSelectedSegmentsPlugIn.checkConsecutiveSelectedSegments(2);
            }
        });
        return check;
    }

    public static final String checkConsecutiveSelectedSegments(int minConsecutiveSegments) {
        SelectionManager selectionManager = ((SelectionManagerProxy)((Object)JUMPWorkbench.getFrameInstance().getActiveInternalFrame())).getSelectionManager();
        Layer editableLayer = JUMPWorkbench.getFrameInstance().getContext().getLayerManager().getEditableLayers().iterator().next();
        if (editableLayer == null) {
            return I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.selected-layer-not-editable");
        }
        Collection<Layer> layersWithSelectedItems = selectionManager.getSegmentSelection().getLayersWithSelectedItems();
        if (layersWithSelectedItems.size() != 1 || !layersWithSelectedItems.contains(editableLayer)) {
            return I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.must-only-have-selected-elements-the-editable-layer");
        }
        Collection<Feature> featuresWithSelectedItems = selectionManager.getSegmentSelection().getFeaturesWithSelectedItems(editableLayer);
        if (featuresWithSelectedItems.size() != 1) {
            return I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.must-only-be-only-one-element-with-selected-segments");
        }
        Feature feat = featuresWithSelectedItems.iterator().next();
        Collection<Geometry> selectedSegments = selectionManager.getSegmentSelection().getSelectedItems(editableLayer, feat);
        if (selectedSegments.size() < minConsecutiveSegments) {
            return I18N.getMessage("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.at-least-{0}-consecutive-segments-must-be-selected", new Object[]{minConsecutiveSegments});
        }
        List<Coordinate[]> coordinateArrayList = CoordinateArrays.toCoordinateArrays(feat.getGeometry(), false);
        Iterator<Geometry> selecIt = selectedSegments.iterator();
        LineString lineStringSegment = (LineString)selecIt.next();
        boolean encontrado = false;
        List<Coordinate> coordList = null;
        Iterator<Coordinate[]> coordArrayIt = coordinateArrayList.iterator();
        while (coordArrayIt.hasNext() && !encontrado) {
            Coordinate[] coordArrayTmp = coordArrayIt.next();
            int i = 0;
            while (i < coordArrayTmp.length - 1) {
                if (coordArrayTmp[i] == lineStringSegment.getStartPoint().getCoordinate() && coordArrayTmp[i + 1] == lineStringSegment.getEndPoint().getCoordinate()) {
                    encontrado = true;
                    coordList = Arrays.asList(coordArrayTmp);
                }
                ++i;
            }
        }
        if (coordList == null) {
            return I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.not-localized-segment");
        }
        Coordinate coordInicial = (Coordinate)coordList.get(0);
        Coordinate coordFinal = (Coordinate)coordList.get(coordList.size() - 1);
        boolean cerrado = coordInicial.equals2D(coordFinal);
        LinkedList<Coordinate> consecutiveCoordsList = new LinkedList<Coordinate>();
        if (cerrado) {
            consecutiveCoordsList.addFirst(DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lineStringSegment.getStartPoint().getCoordinate()));
            consecutiveCoordsList.addLast(DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lineStringSegment.getEndPoint().getCoordinate()));
        } else {
            consecutiveCoordsList.addFirst(lineStringSegment.getStartPoint().getCoordinate());
            consecutiveCoordsList.addLast(lineStringSegment.getEndPoint().getCoordinate());
        }
        selectedSegments.remove(lineStringSegment);
        boolean added = true;
        while (added && !selectedSegments.isEmpty()) {
            selecIt = selectedSegments.iterator();
            added = false;
            LineString lsTmp = null;
            while (selecIt.hasNext() && !added) {
                lsTmp = (LineString)selecIt.next();
                Coordinate lsStartCoord = lsTmp.getStartPoint().getCoordinate();
                Coordinate lsEndCoord = lsTmp.getEndPoint().getCoordinate();
                if (cerrado) {
                    lsStartCoord = DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lsStartCoord);
                    lsEndCoord = DeleteSelectedSegmentsPlugIn.closedGeomValidCoord(coordInicial, coordFinal, lsEndCoord);
                }
                if (lsStartCoord == consecutiveCoordsList.getLast()) {
                    added = true;
                    consecutiveCoordsList.addLast(lsEndCoord);
                    continue;
                }
                if (lsEndCoord != consecutiveCoordsList.getFirst()) continue;
                added = true;
                consecutiveCoordsList.addFirst(lsStartCoord);
            }
            if (added) {
                selectedSegments.remove(lsTmp);
                continue;
            }
            return I18N.getString("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.selected-segments-must-be-consecutive");
        }
        return null;
    }

    public static Coordinate closedGeomValidCoord(Coordinate startCoord, Coordinate endCoord, Coordinate coordToValid) {
        if (coordToValid == endCoord) {
            return startCoord;
        }
        return coordToValid;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        super.initialize(context);
        this.registerDeleteKey(context.getWorkbenchContext());
    }

    private synchronized void registerDeleteKey(final WorkbenchContext context) {
        if (keyRegistered) {
            return;
        }
        final MultiEnableCheck enableCheck = DeleteSelectedSegmentsPlugIn.createEnableCheck(context);
        context.getWorkbench().getFrame().addEasyKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                block8: {
                    if (context.getWorkbench().getFrame().getActiveInternalFrame() instanceof TaskFrame && e.getKeyCode() == 127) {
                        if (enableCheck.check(null) != null) {
                            return;
                        }
                        context.getLayerManager().getUndoableEditReceiver().startReceiving();
                        try {
                            try {
                                PlugInContext pc = context.createPlugInContext();
                                boolean ok = DeleteSelectedSegmentsPlugIn.this.execute(pc);
                                if (ok) {
                                    DeleteSelectedSegmentsPlugIn.this.setShiftPressed(e.isShiftDown());
                                    new TaskMonitorManager().execute(DeleteSelectedSegmentsPlugIn.this, pc);
                                }
                            }
                            catch (Exception ex) {
                                LOGGER.error((Object)"", (Throwable)ex);
                                context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                                break block8;
                            }
                        }
                        catch (Throwable throwable) {
                            context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                            throw throwable;
                        }
                        context.getLayerManager().getUndoableEditReceiver().stopReceiving();
                    }
                }
                DeleteSelectedSegmentsPlugIn.this.setShiftPressed(e.isShiftDown());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                DeleteSelectedSegmentsPlugIn.this.setShiftPressed(e.isShiftDown());
            }
        });
        keyRegistered = true;
    }

    protected void setShiftPressed(boolean isShiftPressed) {
        shiftPressed = isShiftPressed;
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
    public EnableCheck getCheck() {
        return DeleteSelectedSegmentsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        this.monitor = monitor;
        this.context = context;
        this.consecutiveCoordinateList.removeFirst();
        this.consecutiveCoordinateList.removeLast();
        Geometry modifiedGeometry = this.geometryEditor.deleteVerticesWithClosingOption(this.feature.getGeometry(), this.consecutiveCoordinateList);
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        featsToUpdate.add(this.feature);
        ArrayList<Geometry> newGeometries = new ArrayList<Geometry>();
        newGeometries.add(modifiedGeometry);
        if (!modifiedGeometry.isValid()) {
            if (this.isRollingBackInvalidEdits()) {
                context.getLayerViewPanel().getContext().warnUser(I18N.getString("workbench.ui.EditTransaction.the-new-geometry-is-invalid-cancelled"));
                return;
            }
            context.getLayerViewPanel().getContext().warnUser(I18N.getString("workbench.ui.EditTransaction.the-new-geometry-is-invalid"));
        }
        ArrayList<DeleteVertexTool.VertexContext> vertexes = new ArrayList<DeleteVertexTool.VertexContext>();
        vertexes.add(new DeleteVertexTool.VertexContext(this.editableLayer, featsToUpdate, newGeometries));
        this.deleteVertexes(vertexes, new HashSet<Coordinate>(this.consecutiveCoordinateList));
    }

    protected void deleteVertexes(List<DeleteVertexTool.VertexContext> vertexes, Set<Coordinate> verticesDeleted) throws Exception {
        DeleteVertexTool.VertexContext vertex = vertexes.get(0);
        Layer vertexLayer = vertex.getLayer();
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
        if (shiftPressed) {
            this.monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.removing-{0}-segments-in-layer-{1}", new Object[]{this.selectedSegments.size(), this.editableLayer.getName()}));
            this.selectionManager.clearSegmentSelection();
            this.selectionManager.unselectItems(vertexLayer, featsSelectedToUpdate);
            try {
                if (!featsToUpdate.isEmpty()) {
                    vertexLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                    vertexLayer.getLayerManager().fireGeometryModified(featsToUpdate, vertexLayer, featsSelectedToUpdate);
                    this.selectionManager.getFeatureSelection().selectItems(vertexLayer, featsToUpdate);
                }
                try {
                    Animations.drawExpandingRings(this.context.getLayerViewPanel().getViewport().toViewPoints(verticesDeleted), true, Color.red, this.context.getLayerViewPanel(), new float[]{15.0f, 15.0f});
                }
                catch (Throwable t) {
                    this.context.getLayerViewPanel().getContext().warnUser(t.toString());
                }
                JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.removed-{0}-segments-in-layer-{1}", new Object[]{this.selectedSegments.size(), this.editableLayer.getName()}));
            }
            catch (TopologyRelationException e) {
                this.selectionManager.getFeatureSelection().selectItems(vertexLayer, featsSelectedToUpdate);
                JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
            }
        } else {
            this.applyChanges(vertexLayer, featsSelectedToUpdate, featsToUpdate, verticesDeleted);
        }
    }

    protected void applyChanges(final Layer vertexLayer, final List<Feature> featsSelectedToUpdate, final List<Feature> featsToUpdate, final Set<Coordinate> verticesDeleted) throws Exception {
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                DeleteSelectedSegmentsPlugIn.this.monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.removing-{0}-segments-in-layer-{1}", new Object[]{DeleteSelectedSegmentsPlugIn.this.selectedSegments.size(), DeleteSelectedSegmentsPlugIn.this.editableLayer.getName()}));
                DeleteSelectedSegmentsPlugIn.this.selectionManager.clearSegmentSelection();
                DeleteSelectedSegmentsPlugIn.this.selectionManager.unselectItems(vertexLayer, featsSelectedToUpdate);
                try {
                    if (!featsToUpdate.isEmpty()) {
                        vertexLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        vertexLayer.getLayerManager().fireGeometryModified(featsToUpdate, vertexLayer, featsSelectedToUpdate);
                        DeleteSelectedSegmentsPlugIn.this.selectionManager.getFeatureSelection().selectItems(vertexLayer, featsToUpdate);
                    }
                    try {
                        Animations.drawExpandingRings(DeleteSelectedSegmentsPlugIn.this.context.getLayerViewPanel().getViewport().toViewPoints(verticesDeleted), true, Color.red, DeleteSelectedSegmentsPlugIn.this.context.getLayerViewPanel(), new float[]{15.0f, 15.0f});
                    }
                    catch (Throwable t) {
                        DeleteSelectedSegmentsPlugIn.this.context.getLayerViewPanel().getContext().warnUser(t.toString());
                    }
                    JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.removed-{0}-segments-in-layer-{1}", new Object[]{DeleteSelectedSegmentsPlugIn.this.selectedSegments.size(), DeleteSelectedSegmentsPlugIn.this.editableLayer.getName()}));
                }
                catch (TopologyRelationException e) {
                    DeleteSelectedSegmentsPlugIn.this.selectionManager.getFeatureSelection().selectItems(vertexLayer, featsSelectedToUpdate);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
            }

            @Override
            public void unexecute() throws Exception {
                DeleteSelectedSegmentsPlugIn.this.monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.restoring-{0}-segments-in-layer-{1}", new Object[]{DeleteSelectedSegmentsPlugIn.this.selectedSegments.size(), DeleteSelectedSegmentsPlugIn.this.editableLayer.getName()}));
                DeleteSelectedSegmentsPlugIn.this.selectionManager.unselectItems(vertexLayer);
                if (!featsToUpdate.isEmpty()) {
                    vertexLayer.getFeatureCollectionWrapper().updateAll(featsSelectedToUpdate);
                    vertexLayer.getLayerManager().fireGeometryModified(featsSelectedToUpdate, vertexLayer, featsToUpdate);
                }
                DeleteSelectedSegmentsPlugIn.this.selectionManager.getFeatureSelection().selectItems(vertexLayer, featsSelectedToUpdate);
                DeleteSelectedSegmentsPlugIn.this.selectionManager.getSegmentSelection().selectItems(DeleteSelectedSegmentsPlugIn.this.editableLayer, DeleteSelectedSegmentsPlugIn.this.feature, DeleteSelectedSegmentsPlugIn.this.selectedSegments, true);
                JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn.restored-{0}-segments-in-layer-{1}", new Object[]{DeleteSelectedSegmentsPlugIn.this.selectedSegments.size(), DeleteSelectedSegmentsPlugIn.this.editableLayer.getName()}));
            }
        }, this.context);
    }

    protected Layer getLayer() {
        return JUMPWorkbench.getFrameInstance().getContext().getLayerManager().getEditableLayers().iterator().next();
    }
}

