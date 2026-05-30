/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.DeleteSelectedSegmentsPlugIn;
import org.saig.jump.tools.editing.SegmentLateralDisplacementTool;
import org.saig.jump.widgets.editing.SegmentLateralDisplacementDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class SegmentLateralDisplacementPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString(SegmentLateralDisplacementPlugIn.class, "lateral-displacement");
    public static final Icon ICON = IconLoader.icon("SegmentLateralDisplacement.png");
    protected boolean withMouse;
    protected boolean perpendicular;
    protected double incrX;
    protected double incrY;
    protected LineString segmentLineString;
    protected SelectionManager selectionManager;
    protected Feature feature;
    protected Layer editableLayer;
    protected Collection<Geometry> selectedSegments;
    private GeometryFactory gf;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.gf = new GeometryFactory();
        String error = this.init();
        if (error != null) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), error, I18N.getString(SegmentLateralDisplacementPlugIn.class, "warning"));
            return false;
        }
        SegmentLateralDisplacementDialog dialog = new SegmentLateralDisplacementDialog(JUMPWorkbench.getFrameInstance(), true, this.segmentLineString.getStartPoint().getCoordinate(), this.segmentLineString.getEndPoint().getCoordinate());
        dialog.setVisible(true);
        if (!dialog.isExitOk()) {
            return false;
        }
        this.withMouse = dialog.isWithMouseSelected();
        this.perpendicular = dialog.isPerpendicularSelected();
        this.incrX = dialog.getIncrX();
        this.incrY = dialog.getIncrY();
        this.applyChanges(context);
        return true;
    }

    protected void applyChanges(PlugInContext context) throws Exception {
        SegmentLateralDisplacementTool sldTool = new SegmentLateralDisplacementTool(this.perpendicular, this.selectionManager, this.editableLayer, this.feature, this.selectedSegments, this.segmentLineString);
        if (this.withMouse) {
            context.getLayerViewPanel().setCurrentCursorTool(sldTool);
        } else {
            sldTool.activate(context.getLayerViewPanel());
            sldTool.moveSegments(this.incrX, this.incrY);
            sldTool.deactivate();
        }
    }

    @Override
    public EnableCheck getCheck() {
        return SegmentLateralDisplacementPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 3, 2, 15}));
        solucion.add(checkFactory.createAtLeastNSegmentsMustBeSelectedCheck(1));
        solucion.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return DeleteSelectedSegmentsPlugIn.checkConsecutiveSelectedSegments(1);
            }
        });
        return solucion;
    }

    private String init() {
        this.selectionManager = ((SelectionManagerProxy)((Object)JUMPWorkbench.getFrameInstance().getActiveInternalFrame())).getSelectionManager();
        this.editableLayer = this.getLayer();
        Collection<Layer> layersWithSelectedItems = this.selectionManager.getSegmentSelection().getLayersWithSelectedItems();
        if (layersWithSelectedItems.size() != 1 || !layersWithSelectedItems.contains(this.editableLayer)) {
            return I18N.getString(SegmentLateralDisplacementPlugIn.class, "only-the-editable-layer-can-have-selected-segments");
        }
        Collection<Feature> featuresWithSelectedItems = this.selectionManager.getSegmentSelection().getFeaturesWithSelectedItems(this.editableLayer);
        if (featuresWithSelectedItems.size() != 1) {
            return I18N.getString(SegmentLateralDisplacementPlugIn.class, "only-one-element-can-have-selected-segments");
        }
        this.feature = featuresWithSelectedItems.iterator().next();
        this.selectedSegments = this.selectionManager.getSegmentSelection().getSelectedItems(this.editableLayer, this.feature);
        ArrayList<Geometry> tmpSelectedSegments = new ArrayList<Geometry>(this.selectedSegments);
        if (tmpSelectedSegments.size() < 1) {
            return I18N.getString(SegmentLateralDisplacementPlugIn.class, "there-must-are-at-least-two-consecutive-segments");
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
            return I18N.getString(SegmentLateralDisplacementPlugIn.class, "not-located-segment");
        }
        Coordinate coordInicial = (Coordinate)coordList.get(0);
        Coordinate coordFinal = (Coordinate)coordList.get(coordList.size() - 1);
        boolean geometryClosed = coordInicial.equals2D(coordFinal);
        LinkedList<Coordinate> consecutiveCoordinateList = new LinkedList<Coordinate>();
        consecutiveCoordinateList.addFirst(lineStringSegment.getStartPoint().getCoordinate());
        consecutiveCoordinateList.addLast(lineStringSegment.getEndPoint().getCoordinate());
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
                if (geometryClosed && lsStartCoord == coordInicial && ((Coordinate)consecutiveCoordinateList.getLast()).equals2D(lsStartCoord)) {
                    added = true;
                    consecutiveCoordinateList.addLast(lsStartCoord);
                    consecutiveCoordinateList.addLast(lsEndCoord);
                    continue;
                }
                if (geometryClosed && lsEndCoord == coordFinal && ((Coordinate)consecutiveCoordinateList.getFirst()).equals2D(lsEndCoord)) {
                    added = true;
                    consecutiveCoordinateList.addFirst(lsEndCoord);
                    consecutiveCoordinateList.addFirst(lsStartCoord);
                    continue;
                }
                if (lsStartCoord == consecutiveCoordinateList.getLast()) {
                    added = true;
                    consecutiveCoordinateList.addLast(lsEndCoord);
                    continue;
                }
                if (lsEndCoord != consecutiveCoordinateList.getFirst()) continue;
                added = true;
                consecutiveCoordinateList.addFirst(lsStartCoord);
            }
            if (added) {
                tmpSelectedSegments.remove(lsTmp);
                continue;
            }
            return I18N.getString(SegmentLateralDisplacementPlugIn.class, "selected-segments-must-be-consecutive");
        }
        this.segmentLineString = this.gf.createLineString(consecutiveCoordinateList.toArray(new Coordinate[0]));
        return null;
    }

    protected Layer getLayer() {
        return JUMPWorkbench.getFrameInstance().getContext().getLayerManager().getEditableLayers().iterator().next();
    }
}

