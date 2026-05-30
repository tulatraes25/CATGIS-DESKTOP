/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.ConvexHull
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.analysis;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
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
import es.kosmo.desktop.widgets.analysis.ConvexHullLayerOptionsDialog;
import java.util.ArrayList;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.memory.CollectionIterator;
import org.saig.core.model.feature.DummyFeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.widgets.util.DialogFactory;

public class ConvexHullLayerPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(ConvexHullLayerPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Generate-convex-hull");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private static final int PARTIAL_CONVEX = 1000;
    protected ConvexHullLayerOptionsDialog dialog;

    @Override
    public EnableCheck getCheck() {
        return ConvexHullLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        check.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        return check;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer sourceLayer = (Layer)context.getSelectedLayers()[0];
        int numFeaturesSelected = context.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(sourceLayer);
        this.dialog = new ConvexHullLayerOptionsDialog(context.getWorkbenchFrame(), true);
        this.dialog.refresh(sourceLayer.getTitle(), numFeaturesSelected);
        GUIUtil.centre(this.dialog, context.getWorkbenchFrame());
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        sourceLayer = (Layer)context.getSelectedLayers()[0];
        monitor.report(String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Checking-source-layer-{0}", new Object[]{sourceLayer.getTitle()})) + "...");
        query = this.dialog.getResultsQuery();
        useSelectedOnly = this.dialog.useSelectedOnly();
        isMemory = query.getDataSource() instanceof MemoryDataSource;
        fcToSave = null;
        selectedFeatures = null;
        if (useSelectedOnly) {
            selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(sourceLayer);
        } else {
            fcToSave = sourceLayer.getFeatureCollectionWrapper();
        }
        geom_List = new ArrayList<Geometry>();
        convex_List = null;
        itFeatures = null;
        contador = 0;
        numElements = 0;
        try {
            block30: {
                block31: {
                    if (!useSelectedOnly) {
                        itFeatures = fcToSave.iterator();
                        numElements = fcToSave.size();
                    } else {
                        itFeatures = selectedFeatures.isEmpty() != false ? new DummyFeatureIterator() : new CollectionIterator(selectedFeatures);
                        numElements = selectedFeatures.size();
                    }
                    while (!monitor.isCancelRequested() && itFeatures.hasNext()) {
                        feat_temp = itFeatures.next();
                        g_temp = feat_temp.getGeometry();
                        if (g_temp == null) continue;
                        if (!g_temp.isSimple()) {
                            i = 0;
                            while (i < g_temp.getNumGeometries()) {
                                g_temp_x = g_temp.getGeometryN(i);
                                if (g_temp_x != null && !g_temp_x.isEmpty()) {
                                    geom_List.add(g_temp_x);
                                }
                                ++i;
                            }
                        } else if (!g_temp.isEmpty()) {
                            geom_List.add(g_temp);
                        }
                        if (contador++ % 100 == 0) {
                            monitor.report(String.valueOf(I18N.getMessage("org.saig.jump.plugin.utils.CalculateAttributeByExpressionPlugIn.Checking-source-layer-{0}", new Object[]{sourceLayer.getTitle()})) + "...");
                            monitor.report(contador - 1, numElements, I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Processed-features"));
                        }
                        if (geom_List.size() < 1000) continue;
                        if (convex_List == null) {
                            convex_List = new ArrayList<Geometry>();
                        }
                        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Calculating-partial-convex-hull")) + "...");
                        monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Processed-features"));
                        gc = new GeometryCollection(geom_List.toArray(new Geometry[0]), ConvexHullLayerPlugIn.geomFac);
                        convexHull = new ConvexHull((Geometry)gc);
                        convex_List.add(convexHull.getConvexHull());
                        geom_List = new ArrayList<E>();
                    }
                    if (monitor.isCancelRequested()) {
                        this.warnOperationCancelled(context);
                        return;
                    }
                    monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Processed-features"));
                    convexGeometry = null;
                    if (convex_List != null) break block30;
                    if (geom_List.size() <= 0) break block31;
                    monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Calculating-convex-hull")) + "...");
                    monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Processed-features"));
                    gc = new GeometryCollection(geom_List.toArray(new Geometry[0]), ConvexHullLayerPlugIn.geomFac);
                    convexHull = new ConvexHull((Geometry)gc);
                    convexGeometry = convexHull.getConvexHull();
                    ** GOTO lbl87
                }
                DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.The-layer-{0}-has-no-geometries", new Object[]{sourceLayer.getName()}), ConvexHullLayerPlugIn.NAME);
                return;
            }
            try {
                if (geom_List.size() > 0) {
                    monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Calculating-partial-convex-hull")) + "...");
                    monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Processed-features"));
                    gc = new GeometryCollection(geom_List.toArray(new Geometry[0]), ConvexHullLayerPlugIn.geomFac);
                    convexHull = new ConvexHull((Geometry)gc);
                    convex_List.add(convexHull.getConvexHull());
                    geom_List.clear();
                }
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Calculating-total-convex-hull")) + "...");
                monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Processed-features"));
                gc = new GeometryCollection(convex_List.toArray(new Geometry[0]), new GeometryFactory());
                convexHull = new ConvexHull((Geometry)gc);
                convexGeometry = convexHull.getConvexHull();
lbl87:
                // 2 sources

                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Generating-new-layer")) + " ...");
                newSchema = this.buildFeatureSchema();
                if (convexGeometry.getGeometryType().equals("Polygon")) {
                    newSchema.setGeometryType(5);
                } else if (convexGeometry.getGeometryType().equals("LineString")) {
                    newSchema.setGeometryType(3);
                } else if (convexGeometry.getGeometryType().equals("Point")) {
                    newSchema.setGeometryType(1);
                }
                fd = new FeatureDataset(newSchema);
                fd.setName(String.valueOf(sourceLayer.getTitle()) + "_" + I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Convexhull"));
                feat_temp = FeatureUtil.toFeature(convexGeometry, newSchema);
                feat_temp.setAttribute("GID", (Object)new Integer(0));
                fd.addWithNewKey(feat_temp);
                if (isMemory) {
                    monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Creating-new-data-layer-in-memory")) + "...");
                    newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fd.getName(), fd);
                    newLayer.setProjection(sourceLayer.getProjection());
                    newLayer.setFeatureCollectionModified(true);
                } else {
                    ConvexHullLayerPlugIn.saveResults(query, fd, fd.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
                context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.ConvexHullLayerPlugIn.Convex-hull-correctly-generated"));
            }
            catch (Exception e) {
                ConvexHullLayerPlugIn.LOGGER.error((Object)"", (Throwable)e);
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
    }

    private FeatureSchema buildFeatureSchema() {
        FeatureSchema fs = new FeatureSchema();
        fs.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        fs.addAttribute("geometry", AttributeType.GEOMETRY);
        return fs;
    }
}

