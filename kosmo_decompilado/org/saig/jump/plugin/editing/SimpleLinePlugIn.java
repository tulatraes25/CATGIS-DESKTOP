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
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.DrawSimpleLineTool;
import org.saig.jump.widgets.tools.editing.SimpleLineDialog;

public class SimpleLinePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.SimpleLinePlugIn.Draw-simple-line");
    public static final Icon ICON = IconLoader.icon("drawSimpleLine.png");
    protected PlugInContext context;
    protected SimpleLineDialog sld;

    public Layer getLayer() {
        return this.context.getLayerManager().getEditableLayers().iterator().next();
    }

    public Geometry createGeometry() {
        GeometryFactory geomFac = new GeometryFactory();
        Coordinate coordinateA = new Coordinate(this.sld.x1, this.sld.y1);
        Coordinate coordinateB = !this.sld.relativo ? new Coordinate(this.sld.x2, this.sld.y2) : new Coordinate(this.sld.x2 + coordinateA.x, this.sld.y2 + coordinateA.y);
        Coordinate[] cords = new Coordinate[]{coordinateA, coordinateB};
        Layer editableLayer = this.getLayer();
        int geometryType = editableLayer.getFeatureSchema().getGeometryType();
        LineString nuevaGeom = geomFac.createLineString(cords);
        if (geometryType == 2) {
            nuevaGeom = geomFac.createMultiLineString(new LineString[]{nuevaGeom});
        }
        return nuevaGeom;
    }

    public Feature createFeature() {
        Layer editableLayer = this.getLayer();
        return FeatureUtil.toFeature(this.createGeometry(), editableLayer.getFeatureSchema());
    }

    public void selectManualTool(int nPuntosACapturar, Coordinate c1, Coordinate c2) {
        DrawSimpleLineTool dslt = new DrawSimpleLineTool(nPuntosACapturar, this.sld, c1, c2);
        QuasimodeTool quasimodeTool = QuasimodeTool.addStandardQuasimodes(dslt);
        this.context.getLayerViewPanel().setCurrentCursorTool(quasimodeTool);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.context = context;
        Coordinate c1 = null;
        Coordinate c2 = null;
        this.sld = new SimpleLineDialog(this, context);
        this.sld.setVisible(true);
        if (!this.sld.cancelado) {
            int nPuntosACapturar = 0;
            if (this.sld.isPrimerPuntoCapturado()) {
                ++nPuntosACapturar;
            } else {
                c1 = new Coordinate(this.sld.x1, this.sld.y1);
                ++nPuntosACapturar;
            }
            if (this.sld.isSegundoPuntoCapturado()) {
                ++nPuntosACapturar;
            }
            if (this.sld.isPrimerPuntoCapturado() || this.sld.isSegundoPuntoCapturado()) {
                this.selectManualTool(nPuntosACapturar, c1, c2);
            } else {
                this.save();
            }
        }
        return true;
    }

    public void createLine(SimpleLineDialog sld) throws Exception {
        this.sld = sld;
        this.save();
    }

    public void save() throws Exception {
        this.reportNothingToUndoYet(this.context);
        final Layer editableLayer = this.getLayer();
        if (editableLayer == null) {
            return;
        }
        final SelectionManager selectionManager = this.context.getLayerViewPanel().getSelectionManager();
        final ArrayList<Feature> featsToAdd = new ArrayList<Feature>();
        featsToAdd.add(this.createFeature());
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToAdd.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsToAdd);
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToAdd.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().removeAll(featsToAdd);
                }
            }
        }, this.context);
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
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{3, 2, 15}));
        return solucion;
    }

    @Override
    public EnableCheck getCheck() {
        return SimpleLinePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

