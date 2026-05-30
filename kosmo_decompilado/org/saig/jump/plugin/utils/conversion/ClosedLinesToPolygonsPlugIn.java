/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.LinearRing
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import javax.swing.Icon;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.widgets.utils.conversion.BasicOptionsDialog;

public class ClosedLinesToPolygonsPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Convert-closed-lines-to-polygons");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private GeometryFactory gf = new GeometryFactory();
    private FeatureSchema polygonsSchema;
    private FeatureSchema nonClosedSchema;
    private DataSourceQuery queryPolygons;
    private DataSourceQuery queryNonClosed;
    private String selectedLayerName;
    protected BasicOptionsDialog dialog;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer selectedLayer = (Layer)context.getSelectedLayers()[0];
        this.selectedLayerName = selectedLayer.getName();
        FeatureSchema selectedSchema = selectedLayer.getFeatureSchema();
        this.polygonsSchema = (FeatureSchema)selectedSchema.clone();
        this.polygonsSchema.setGeometryType(5);
        this.nonClosedSchema = (FeatureSchema)selectedSchema.clone();
        this.dialog = new BasicOptionsDialog(context.getWorkbenchFrame(), true, NAME, context.getLayerManager(), true, null, null);
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
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
        return ClosedLinesToPolygonsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        solucion.add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{3, 2}));
        return solucion;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Layer newLayer;
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Creating-polygons-from-closed-lines")) + "...");
        this.queryPolygons = this.dialog.getResultQuery();
        this.queryNonClosed = this.dialog.getErrorQuery();
        boolean isMemoryQP = this.queryPolygons.getDataSource() instanceof MemoryDataSource;
        boolean isMemoryQNC = this.queryNonClosed != null && this.queryNonClosed.getDataSource() instanceof MemoryDataSource;
        Layer selectedLayer = JUMPWorkbench.getLayer(this.selectedLayerName);
        FeatureCollection fcOrig = selectedLayer.getUltimateFeatureCollectionWrapper();
        FeatureDataset generatedPolygons = new FeatureDataset(this.polygonsSchema);
        generatedPolygons.setName(String.valueOf(this.selectedLayerName) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Closed-polygons"));
        generatedPolygons.set3d(fcOrig.is3d());
        FeatureDataset nonClosedLines = new FeatureDataset(this.nonClosedSchema);
        nonClosedLines.setName(String.valueOf(this.selectedLayerName) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.No-closed-lines"));
        nonClosedLines.set3d(fcOrig.is3d());
        int contador = 1;
        int closedFeatures = 0;
        int numFeatures = fcOrig.size();
        String polygonsPKName = this.polygonsSchema.getPrimaryKeyName();
        String nonClosedPKName = this.nonClosedSchema.getPrimaryKeyName();
        FeatureIterator itFeatures = null;
        try {
            itFeatures = fcOrig.iterator();
            while (!monitor.isCancelRequested() && itFeatures.hasNext()) {
                Feature currentFeat = itFeatures.next();
                Geometry currentGeom = currentFeat.getGeometry();
                if (currentGeom instanceof LineString) {
                    LineString line = (LineString)currentGeom;
                    if (line.isClosed() && line.getNumPoints() > 3) {
                        LinearRing lr = this.gf.createLinearRing(line.getCoordinates());
                        Polygon polygon = this.gf.createPolygon(lr, null);
                        Feature newFeat = FeatureUtil.copyFeature(this.polygonsSchema, currentFeat);
                        newFeat.setAttribute(polygonsPKName, null);
                        newFeat.setGeometry((Geometry)polygon);
                        generatedPolygons.addWithNewKey(newFeat);
                        ++closedFeatures;
                    } else {
                        Feature newFeat = FeatureUtil.copyFeature(this.nonClosedSchema, currentFeat);
                        newFeat.setAttribute(nonClosedPKName, null);
                        nonClosedLines.addWithNewKey(newFeat);
                    }
                } else if (currentGeom instanceof MultiLineString) {
                    ArrayList<LineString> nonClosedLineStrings = new ArrayList<LineString>();
                    int i = 0;
                    while (i < currentGeom.getNumGeometries()) {
                        LineString line = (LineString)currentGeom.getGeometryN(i);
                        if (line.isClosed() && line.getNumPoints() > 3) {
                            LinearRing lr = this.gf.createLinearRing(line.getCoordinates());
                            Polygon polygon = this.gf.createPolygon(lr, null);
                            Feature newFeat = FeatureUtil.copyFeature(this.polygonsSchema, currentFeat);
                            newFeat.setAttribute(polygonsPKName, null);
                            newFeat.setGeometry((Geometry)polygon);
                            generatedPolygons.add(newFeat);
                            ++closedFeatures;
                        } else {
                            nonClosedLineStrings.add(line);
                        }
                        ++i;
                    }
                    if (nonClosedLineStrings.size() > 0) {
                        Feature newFeat = FeatureUtil.copyFeature(this.nonClosedSchema, currentFeat);
                        newFeat.setAttribute(nonClosedPKName, null);
                        LineString[] lines = new LineString[nonClosedLineStrings.size()];
                        lines = nonClosedLineStrings.toArray(lines);
                        newFeat.setGeometry((Geometry)this.gf.createMultiLineString(lines));
                        nonClosedLines.addWithNewKey(newFeat);
                    }
                }
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numFeatures, I18N.getString("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Processed-features"));
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Operation-{0}-cancelled-by-user", new Object[]{this.getName()}));
            return;
        }
        if (generatedPolygons.size() > 0) {
            if (isMemoryQP) {
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Creating-polygons-layer")) + " ...");
                newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, generatedPolygons.getName(), generatedPolygons);
                newLayer.setProjection(selectedLayer.getProjection());
                newLayer.setFeatureCollectionModified(true);
            } else {
                ClosedLinesToPolygonsPlugIn.saveResults(this.queryPolygons, generatedPolygons, generatedPolygons.getName(), null, selectedLayer.getProjection(), true, monitor);
            }
        }
        if (nonClosedLines.size() > 0 && this.queryNonClosed != null) {
            if (isMemoryQNC) {
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Creating-no-closed-lines-layer")) + " ...");
                newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, nonClosedLines.getName(), nonClosedLines);
                newLayer.setProjection(selectedLayer.getProjection());
                newLayer.setFeatureCollectionModified(true);
            } else {
                ClosedLinesToPolygonsPlugIn.saveResults(this.queryNonClosed, nonClosedLines, nonClosedLines.getName(), null, selectedLayer.getProjection(), true, monitor);
            }
        }
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.conversion.ClosedLinesToPolygonsPlugIn.Conversion-finished-{0}-{1}-polygons-created-{2}-{3}-no-closed-lines-{4}-{5}-{6}-initial-features-{7}", new Object[]{": ", Integer.toString(closedFeatures), ",", Integer.toString(nonClosedLines.size()), " (", Integer.toString(numFeatures), "", ")"}));
    }
}

