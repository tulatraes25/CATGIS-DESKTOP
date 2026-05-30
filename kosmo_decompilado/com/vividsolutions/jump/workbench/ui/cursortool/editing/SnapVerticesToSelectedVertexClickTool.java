/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesOp;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class SnapVerticesToSelectedVertexClickTool
extends NClickTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexClickTool.snap-vertices-to-selected-vertex");
    private EnableCheckFactory checkFactory;

    public SnapVerticesToSelectedVertexClickTool(EnableCheckFactory checkFactory) {
        super(1);
        this.checkFactory = checkFactory;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        Assert.shouldNeverReachHere();
        return null;
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.reportNothingToUndoYet();
        final Coordinate clickCoordinate = this.getCoordinates().get(0);
        if (!this.check(this.checkFactory.createAtLeastNLayersMustBeEditableCheck(1))) {
            return;
        }
        if (!this.check(this.checkFactory.createFenceMustBeDrawnCheck())) {
            return;
        }
        if (!this.check(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (!SnapVerticesToSelectedVertexClickTool.this.getPanel().getFence().contains((Geometry)geomFac.createPoint(clickCoordinate))) {
                    return I18N.getString("workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexClickTool.please-click-inside-the-fence");
                }
                if (SnapVerticesToSelectedVertexClickTool.this.getPanel().getSelectionManager().getSelectedItems().isEmpty()) {
                    return I18N.getString("workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexClickTool.select-a-feature-part-or-linestring-in-the-fence-containing-the-vertex-to-snap-to");
                }
                if (SnapVerticesToSelectedVertexClickTool.this.getPanel().getSelectionManager().getSelectedItems().size() > 1) {
                    return I18N.getString("workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexClickTool.select-only-one-feature-part-or-linestring-containing-the-vertex-to-snap-to");
                }
                return null;
            }
        })) {
            return;
        }
        SnapVerticesOp snapVerticesOp = new SnapVerticesOp();
        Geometry geometry = this.getPanel().getFence();
        Collection<Layer> collection = this.getPanel().getLayerManager().getEditableLayers();
        boolean bl = this.isRollingBackInvalidEdits();
        LayerViewPanel layerViewPanel = this.getPanel();
        Task task = this.getTaskFrame().getTask();
        this.getWorkbench();
        snapVerticesOp.execute(geometry, collection, bl, layerViewPanel, task, clickCoordinate, this.getPanel().getSelectionManager().getFeaturesWithSelectedItems(this.getPanel().getSelectionManager().getLayersWithSelectedItems().iterator().next()).iterator().next(), JUMPWorkbench.getBlackboard().get(SnapVerticesOp.INSERT_VERTICES_IF_NECESSARY_KEY, true));
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        return null;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, AbstractCursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 3, 2}));
        solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        return solucion;
    }
}

