/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.MoveSelectedItemsPlugIn;

public class MoveSelectedItemsTool
extends DragTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.MoveSelectedItemsTool.name");
    public static final Icon ICON = IconLoader.icon("Move.gif");
    public static final Cursor CURSOR = Cursor.getPredefinedCursor(13);
    protected EnableCheckFactory checkFactory;
    protected Shape selectedFeaturesShape;
    protected List<Coordinate> verticesToSnap = null;
    protected SnapIndicatorTool snapIndicatorTool;

    public MoveSelectedItemsTool(EnableCheckFactory checkFactory) {
        this.checkFactory = checkFactory;
        this.setStroke(new BasicStroke(1.0f, 0, 2, 0.0f, new float[]{3.0f, 3.0f}, 0.0f));
        this.allowSnapping();
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

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustHaveSelectedItemsCheck(1)).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1)).add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        return solucion;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        MoveSelectedItemsPlugIn moveSelectedItemsPlugIn = new MoveSelectedItemsPlugIn(this.getModelDestination(), this.getModelSource());
        PlugInContext pc = JUMPWorkbench.getFrameInstance().getContext().createPlugInContext();
        new TaskMonitorManager().execute(moveSelectedItemsPlugIn, pc);
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.mousePressed(e, true);
    }

    public void mousePressed(MouseEvent e, boolean check) {
        try {
            if (check) {
                if (!this.check(this.checkFactory.createAtLeastNItemsMustBeSelectedCheck(1))) {
                    return;
                }
                if (!this.check(this.checkFactory.createSelectedItemsLayersMustBeEditableCheck())) {
                    return;
                }
            }
            this.verticesToSnap = null;
            this.selectedFeaturesShape = this.createSelectedItemsShape();
            super.mousePressed(e);
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    private Collection<Coordinate> verticesToSnap() {
        Envelope viewportEnvelope = this.getPanel().getViewport().getEnvelopeInModelCoordinates();
        if (this.verticesToSnap == null) {
            this.verticesToSnap = new ArrayList<Coordinate>();
            for (Geometry selectedItem : this.getPanel().getSelectionManager().getSelectedItems()) {
                Coordinate[] coordinates = selectedItem.getCoordinates();
                int j = 0;
                while (j < coordinates.length) {
                    if (viewportEnvelope.contains(coordinates[j])) {
                        this.verticesToSnap.add(coordinates[j]);
                    }
                    ++j;
                }
            }
            if (this.verticesToSnap.size() > 100) {
                Collections.shuffle(this.verticesToSnap);
                this.verticesToSnap = this.verticesToSnap.subList(0, 99);
            }
        }
        return this.verticesToSnap;
    }

    protected Shape createSelectedItemsShape() throws NoninvertibleTransformException {
        List<Object> itemsToRender = new ArrayList<Geometry>(this.getPanel().getSelectionManager().getSelectedItems());
        if (itemsToRender.size() > 100) {
            Collections.shuffle(itemsToRender);
            itemsToRender = itemsToRender.subList(0, 99);
        }
        GeometryCollection gc = geomFac.createGeometryCollection(itemsToRender.toArray(new Geometry[0]));
        return this.getPanel().getJava2DConverter().toShape((Geometry)gc);
    }

    @Override
    protected Shape getShape() throws Exception {
        AffineTransform transform = new AffineTransform();
        transform.translate(this.getViewDestination().getX() - this.getViewSource().getX(), this.getViewDestination().getY() - this.getViewSource().getY());
        return transform.createTransformedShape(this.selectedFeaturesShape);
    }

    @Override
    protected void setModelDestination(Coordinate modelDestination) {
        for (Coordinate vertex : this.verticesToSnap()) {
            Coordinate displacement = CoordUtil.subtract(vertex, this.getModelSource());
            Coordinate snapPoint = this.snap(CoordUtil.add(modelDestination, displacement));
            modelDestination.z = vertex.z;
            if (!this.getSnapManager().wasSnapCoordinateFound()) continue;
            this.modelDestination = CoordUtil.subtract(snapPoint, displacement);
            this.modelDestination.z = vertex.z;
            return;
        }
        this.modelDestination = modelDestination;
    }
}

