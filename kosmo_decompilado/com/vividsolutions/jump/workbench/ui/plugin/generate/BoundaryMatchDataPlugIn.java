/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package com.vividsolutions.jump.workbench.ui.plugin.generate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.plugin.generate.BoundaryMatchDataEngine;
import javax.swing.ImageIcon;
import org.saig.jump.lang.I18N;

public class BoundaryMatchDataPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.name");
    private BoundaryMatchDataEngine engine = new BoundaryMatchDataEngine();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        MultiInputDialog dialog = new MultiInputDialog(context.getWorkbenchFrame(), I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.generate-boundary-match-data"), true);
        this.setDialogValues(dialog);
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        if (!dialog.wasOKPressed()) {
            return false;
        }
        this.getDialogValues(dialog);
        this.engine.execute(context);
        return true;
    }

    private void setDialogValues(MultiInputDialog dialog) {
        dialog.setTitle(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.generate-boundary-match-data"));
        dialog.setSideBarImage(new ImageIcon(this.getClass().getResource("GenerateBdyMatchData.gif")));
        dialog.setSideBarDescription(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.generate-two-sample-datasets-cointaning-random-boundary-pertubations"));
        dialog.addPositiveIntegerField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.layer-width"), this.engine.getLayerWidthInCells(), 5);
        dialog.addPositiveIntegerField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.layer-height"), this.engine.getLayerHeightInCells(), 5);
        dialog.addPositiveDoubleField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.cell-side-length"), this.engine.getCellSideLength(), 5);
        dialog.addPositiveIntegerField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.vertices-per-cell-side"), this.engine.getVerticesPerCellSide(), 5);
        dialog.addPositiveIntegerField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.vertices-per-boundary-side"), this.engine.getVerticesPerBoundarySide(), 5);
        dialog.addNonNegativeDoubleField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.boundary-amplitude"), this.engine.getBoundaryAmplitude(), 5);
        dialog.addPositiveDoubleField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.boundary-period"), this.engine.getBoundaryPeriod(), 5);
        dialog.addNonNegativeDoubleField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.max-boundary-perturbation"), this.engine.getMaxBoundaryPerturbation(), 5);
        dialog.addNonNegativeDoubleField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.perturbation-probability"), this.engine.getPerturbationProbability(), 5);
        dialog.addDoubleField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.minx"), this.engine.getSouthwestCornerOfLeftLayer().x, 5);
        dialog.addDoubleField(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.miny"), this.engine.getSouthwestCornerOfLeftLayer().y, 5);
    }

    private void getDialogValues(MultiInputDialog dialog) {
        this.engine.setSouthwestCornerOfLeftLayer(new Coordinate(dialog.getDouble(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.minx")), dialog.getDouble(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.miny"))));
        this.engine.setLayerHeightInCells(dialog.getInteger(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.layer-height")));
        this.engine.setLayerWidthInCells(dialog.getInteger(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.layer-width")));
        this.engine.setCellSideLength(dialog.getDouble(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.cell-side-length")));
        this.engine.setVerticesPerCellSide(dialog.getInteger(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.vertices-per-cell-side")));
        this.engine.setBoundaryAmplitude(dialog.getDouble(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.boundary-amplitude")));
        this.engine.setBoundaryPeriod(dialog.getDouble(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.boundary-period")));
        this.engine.setVerticesPerBoundarySide(dialog.getInteger(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.vertices-per-boundary-side")));
        this.engine.setMaxBoundaryPerturbation(dialog.getDouble(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.max-boundary-perturbation")));
        this.engine.setPerturbationProbability(dialog.getDouble(I18N.getString("workbench.ui.plugin.generate.BoundaryMatchDataPlugIn.perturbation-probability")));
    }
}

