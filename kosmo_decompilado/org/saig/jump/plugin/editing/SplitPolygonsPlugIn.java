/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.OrEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.SplitPolygonsTool;
import org.saig.jump.util.LayerUtil;
import org.saig.jump.widgets.util.DialogFactory;

public class SplitPolygonsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.tools.editing.SplitPolygonTool.name");
    public static final Icon ICON = IconLoader.icon("splitPolygon.png");
    protected QuasimodeTool quasimodeTool = QuasimodeTool.addStandardQuasimodes(new SplitPolygonsTool());
    public static final String USE_SELECTED_LINEAL_GEOMETRIES_OPTION = I18N.getString(SplitPolygonsPlugIn.class, "use-selected-lines");
    public static final String IGNORE_SELECTED_OPTION = I18N.getString(SplitPolygonsPlugIn.class, "ignore-selection");
    public static final String CANCEL_OPTION = I18N.getString(SplitPolygonsPlugIn.class, "cancel");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Collection<Layer> col = context.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems();
        boolean anyLinealWithSelectedFeatures = false;
        Iterator<Layer> iter = col.iterator();
        while (iter.hasNext() && !anyLinealWithSelectedFeatures) {
            Layer layer = iter.next();
            boolean bl = anyLinealWithSelectedFeatures = !layer.isEditable() && (LayerUtil.isLinealLayer(layer) || LayerUtil.isCADLayer(layer));
        }
        if (anyLinealWithSelectedFeatures) {
            Object[] options = new Object[]{USE_SELECTED_LINEAL_GEOMETRIES_OPTION, IGNORE_SELECTED_OPTION, CANCEL_OPTION};
            String message = I18N.getString(SplitPolygonsPlugIn.class, "there-are-lineal-elements-that-can-be-used-to-divide-the-selected-polygons-in-the-editable-layer");
            Object option = DialogFactory.showOptionDialog(context.getWorkbenchFrame(), message, I18N.getString(SplitPolygonsPlugIn.class, "tool-mode-selection"), options, options[0]);
            int selectedOption = ((Number)option).intValue();
            if (options[selectedOption].equals(CANCEL_OPTION)) {
                return false;
            }
            if (options[selectedOption].equals(IGNORE_SELECTED_OPTION)) {
                context.getWorkbenchFrame().toFront();
                context.getLayerViewPanel().setCurrentCursorTool(this.quasimodeTool);
                return false;
            }
            return true;
        }
        context.getWorkbenchFrame().toFront();
        context.getLayerViewPanel().setCurrentCursorTool(this.quasimodeTool);
        return false;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return SplitPolygonsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 15}));
        MultiEnableCheck multiCheck = new MultiEnableCheck().add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(new int[]{5, 4}, new int[]{10, 11}, 1));
        OrEnableCheck orEnableCheck = new OrEnableCheck();
        orEnableCheck.add(multiCheck);
        orEnableCheck.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(2));
        solucion.add(orEnableCheck);
        solucion.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(1));
        return solucion;
    }

    public Layer getLayer(PlugInContext context) {
        return context.getLayerManager().getEditableLayers().iterator().next();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(I18N.getString(SplitPolygonsPlugIn.class, "dividing-selected-polygons"));
        final SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        final Layer editableLayer = this.getLayer(context);
        if (editableLayer == null) {
            return;
        }
        final Collection<Feature> selectedFeatures = selectionManager.getFeaturesWithSelectedItems(editableLayer);
        if (selectedFeatures == null || selectedFeatures.isEmpty()) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.tools.editing.SplitPolygonsTool.You-have-not-selected-the-features-you-want-to-split"));
            return;
        }
        final ArrayList<Feature> featsToAdd = new ArrayList<Feature>();
        final ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        final ArrayList<Feature> featsSelectedToUpdate = new ArrayList<Feature>();
        Geometry linesForSplit = this.getLineStrings(selectionManager);
        boolean anySelectedPolygonNotSplitted = false;
        int numSelectedFeats = selectedFeatures.size();
        int numProcessedFeats = 0;
        for (Feature currentFeature : selectedFeatures) {
            monitor.report(numProcessedFeats++, numSelectedFeats, I18N.getString(SplitPolygonsPlugIn.class, "processed-elements"));
            Geometry g = currentFeature.getGeometry();
            if (!(g instanceof Polygon) && !(g instanceof MultiPolygon)) continue;
            Feature clonedFeature = currentFeature.clone(true);
            List<Geometry> div = SplitPolygonsTool.splitPoligon(linesForSplit, g);
            if (div.size() > 0) {
                Geometry a = div.get(0);
                clonedFeature.setGeometry(a);
                featsToUpdate.add(clonedFeature);
                featsSelectedToUpdate.add(currentFeature);
                if (div.size() <= 1) continue;
                int j = 1;
                while (j < div.size()) {
                    BasicFeature f = new BasicFeature(currentFeature.getSchema());
                    Geometry b = div.get(j);
                    FeatureUtil.copyAttributes(currentFeature, f);
                    f.setGeometry(b);
                    f.setAttribute(currentFeature.getSchema().getPrimaryKeyName(), null);
                    featsToAdd.add(f);
                    ++j;
                }
                continue;
            }
            anySelectedPolygonNotSplitted = true;
        }
        if (featsToAdd.isEmpty() && featsToUpdate.isEmpty()) {
            context.getWorkbenchFrame().warnUser(I18N.getString(SplitPolygonsPlugIn.class, "no-polygons-to-divide-were-found"));
            return;
        }
        if (anySelectedPolygonNotSplitted) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.tools.editing.SplitPolygonsTool.Some-polygons-could-not-be-split"));
        }
        FeatureUtil.fillZs(featsToAdd);
        FeatureUtil.fillZs(featsToUpdate);
        FeatureUtil.fillZs(featsSelectedToUpdate);
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + I18N.getMessage("org.saig.jump.tools.editing.SplitPolygonsTool.{0}-features-splitted", new Object[]{featsToUpdate.size()})){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                try {
                    if (!featsToAdd.isEmpty()) {
                        editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featsToAdd);
                    }
                }
                catch (TopologyRelationException e) {
                    if (!featsToUpdate.isEmpty()) {
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featsSelectedToUpdate);
                    }
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    return;
                }
                try {
                    if (!featsToUpdate.isEmpty()) {
                        editableLayer.getFeatureCollectionWrapper().updateAll(featsToUpdate);
                        editableLayer.getLayerManager().fireGeometryModified(featsToUpdate, editableLayer, featsSelectedToUpdate);
                        selectionManager.getFeatureSelection().selectItems(editableLayer, featsToUpdate);
                    }
                }
                catch (TopologyRelationException e) {
                    if (!featsToAdd.isEmpty()) {
                        editableLayer.getFeatureCollectionWrapper().removeAll(featsToAdd);
                    }
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsSelectedToUpdate);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
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
                selectionManager.getFeatureSelection().selectItems(editableLayer, selectedFeatures);
            }
        }, context);
    }

    protected Geometry getLineStrings(SelectionManager selectionManager) {
        ArrayList<Geometry> selectedGeoms = new ArrayList<Geometry>();
        Collection<Layer> layersWithSelectedItems = selectionManager.getLayersWithSelectedItems();
        for (Layer currentLayer : layersWithSelectedItems) {
            if (currentLayer.isEditable() || !LayerUtil.isLinealLayer(currentLayer) && !LayerUtil.isCADLayer(currentLayer)) continue;
            Collection<Feature> selectedFeats = selectionManager.getFeaturesWithSelectedItems(currentLayer);
            for (Feature currentFeat : selectedFeats) {
                Geometry currentGeom = currentFeat.getGeometry();
                if (currentGeom == null || !(currentGeom instanceof LineString) && !(currentGeom instanceof MultiLineString)) continue;
                selectedGeoms.add(currentFeat.getGeometry());
            }
        }
        return geomFac.createGeometryCollection(GeometryFactory.toGeometryArray(selectedGeoms));
    }
}

