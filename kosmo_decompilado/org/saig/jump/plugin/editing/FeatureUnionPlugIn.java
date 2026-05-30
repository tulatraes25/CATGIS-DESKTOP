/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.geom.TopologyException
 *  com.vividsolutions.jts.operation.linemerge.LineMerger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
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
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.editing.FeatureUnionDialog;

public class FeatureUnionPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.FeatureUnionPlugIn.Join-features");
    public static final Icon ICON = IconLoader.icon("featureUnion.png");
    protected Feature copyFeat = null;
    protected boolean dissolveGeometries = true;

    @Override
    public void initialize(PlugInContext context) throws Exception {
        LayerViewPanel.popupMenu().addSeparator();
        context.getLayerViewPanel();
        context.getFeatureInstaller().addPopupMenuItem(LayerViewPanel.popupMenu(), this, false);
        context.getFeatureInstaller().addMenuSeparator(MainMenuNames.EDIT);
        context.getFeatureInstaller().addMainMenuItem(this, MainMenuNames.EDIT, NAME, GUIUtil.toSmallIcon(ICON), this.getCheck());
    }

    protected Layer getLayer(PlugInContext context) {
        return context.getLayerManager().getEditableLayers().iterator().next();
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer editableLayer = this.getLayer(context);
        SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        int numSelectedFeatures = selectionManager.getNumFeaturesWithSelectedItems(editableLayer);
        if (numSelectedFeatures <= 1) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.editing.FeatureUnionPlugIn.Layer-{0}-has-not-two-or-more-selected-elements", new Object[]{editableLayer.getName()}));
            return false;
        }
        ArrayList<Feature> featuresToUnion = new ArrayList<Feature>(selectionManager.getFeaturesWithSelectedItems(editableLayer));
        FeatureUnionDialog dialog = this.getFeatureUnionDialog(featuresToUnion);
        dialog.pack();
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        if (!dialog.isCanceled()) {
            this.copyFeat = dialog.getSelectedFeature();
            this.dissolveGeometries = dialog.dissolveSelectedGeometries();
        }
        return !dialog.isCanceled();
    }

    protected FeatureUnionDialog getFeatureUnionDialog(List<Feature> featuresToUnion) {
        return new FeatureUnionDialog(featuresToUnion, true);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithAssociatedTaskFrameMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createAtLeastNFeaturesMustBeSelectedCheck(2));
        return check;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer editableLayer = this.getLayer(context);
        SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        ArrayList<Feature> featuresToUnion = new ArrayList<Feature>(selectionManager.getFeaturesWithSelectedItems(editableLayer));
        ArrayList<Feature> featsToAdd = new ArrayList<Feature>();
        monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.FeatureUnionPlugIn.Joining-{0}-features-from-layer-{1}", new Object[]{Integer.toString(featuresToUnion.size()), editableLayer.getName()}));
        Feature newFeat = this.combineFeatures(featuresToUnion, editableLayer.getFeatureSchema());
        if (newFeat == null) {
            context.getWorkbenchFrame().warnUser(I18N.getString(this.getClass(), "no-results-were-obtained-after-union-perform"));
            return;
        }
        featsToAdd.add(newFeat);
        this.applyChanges(selectionManager, editableLayer, featuresToUnion, featsToAdd, context);
    }

    protected void applyChanges(final SelectionManager selectionManager, final Layer editableLayer, final Collection<Feature> featuresToUnion, final List<Feature> featsToAdd, PlugInContext context) throws Exception {
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + featuresToUnion.size() + " " + I18N.getString(this.getClass(), "elements") + " (<I>" + editableLayer.getName() + "</I>)"){

            @Override
            public void execute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                try {
                    editableLayer.getFeatureCollectionWrapper().removeAll(featuresToUnion);
                }
                catch (TopologyException e) {
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    return;
                }
                try {
                    editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                }
                catch (TopologyException e) {
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                    editableLayer.getFeatureCollectionWrapper().addAll(featuresToUnion);
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featuresToUnion);
                    return;
                }
                selectionManager.getFeatureSelection().selectItems(editableLayer, featsToAdd);
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                if (!featsToAdd.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().removeAll(featsToAdd);
                }
                if (!featuresToUnion.isEmpty()) {
                    editableLayer.getFeatureCollectionWrapper().addAll(featuresToUnion);
                }
                selectionManager.getFeatureSelection().selectItems(editableLayer, featuresToUnion);
            }
        }, context);
    }

    protected Feature combineFeatures(List<Feature> selectedFeatures, FeatureSchema featureSchema) {
        List<Geometry> geometries = FeatureUtil.toGeometries(selectedFeatures);
        MultiPoint geom = null;
        int geometryType = featureSchema.getGeometryType();
        switch (geometryType) {
            case 1: 
            case 8: {
                ArrayList<Geometry> points = new ArrayList<Geometry>();
                for (Geometry element : geometries) {
                    if (element instanceof MultiPoint) {
                        MultiPoint mp = (MultiPoint)element;
                        int i = 0;
                        while (i < mp.getNumGeometries()) {
                            points.add(mp.getGeometryN(i));
                            ++i;
                        }
                        continue;
                    }
                    points.add(element);
                }
                geom = geomFac.createMultiPoint(points.toArray(new Point[points.size()]));
                if (geometryType != 1 || geom.getNumGeometries() != 1) break;
                geom = geom.getGeometryN(0);
                break;
            }
            case 2: 
            case 3: {
                ArrayList<Geometry> lines = new ArrayList<Geometry>();
                for (Geometry element : geometries) {
                    if (element instanceof MultiLineString) {
                        MultiLineString ml = (MultiLineString)element;
                        int i = 0;
                        while (i < ml.getNumGeometries()) {
                            lines.add(ml.getGeometryN(i));
                            ++i;
                        }
                        continue;
                    }
                    lines.add(element);
                }
                if (this.dissolveGeometries) {
                    LineMerger merger = new LineMerger();
                    merger.add(lines);
                    geom = geomFac.createMultiLineString(GeometryFactory.toLineStringArray((Collection)merger.getMergedLineStrings()));
                } else {
                    geom = geomFac.createMultiLineString(lines.toArray(new LineString[lines.size()]));
                }
                if (geometryType != 3 || geom.getNumGeometries() != 1) break;
                geom = geom.getGeometryN(0);
                break;
            }
            case 4: 
            case 5: {
                ArrayList<Geometry> polygons = new ArrayList<Geometry>();
                for (Geometry element : geometries) {
                    if (element instanceof MultiPolygon) {
                        MultiPolygon mp = (MultiPolygon)element;
                        int i = 0;
                        while (i < mp.getNumGeometries()) {
                            polygons.add(mp.getGeometryN(i));
                            ++i;
                        }
                        continue;
                    }
                    polygons.add(element);
                }
                if (this.dissolveGeometries) {
                    GeometryCollection gc = geomFac.createGeometryCollection(polygons.toArray(new Geometry[0]));
                    geom = gc.buffer(0.0);
                } else {
                    geom = geomFac.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
                }
                if (geometryType != 5 || geom.getNumGeometries() != 1) break;
                geom = geom.getGeometryN(0);
            }
        }
        if (geom != null) {
            return this.fillFeatureAttributes(selectedFeatures, (Geometry)geom, featureSchema);
        }
        return new BasicFeature(featureSchema);
    }

    protected Feature fillFeatureAttributes(List<Feature> selectedFeatures, Geometry geom, FeatureSchema schema) {
        if (this.copyFeat == null) {
            Feature feat = FeatureUtil.toFeature(geom, schema);
            return feat;
        }
        Feature feat = FeatureUtil.copyFeature(schema, this.copyFeat);
        feat.setID(FeatureUtil.nextID());
        feat.setGeometry(geom);
        return feat;
    }

    @Override
    public void finish(PlugInContext context) {
        context.getFeatureInstaller().removePopupMenuItem(LayerViewPanel.popupMenu(), this.getName());
        context.getFeatureInstaller().removeMainMenuItem(this, new String[]{MainMenuNames.EDIT}, this.getName());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return FeatureUnionPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

