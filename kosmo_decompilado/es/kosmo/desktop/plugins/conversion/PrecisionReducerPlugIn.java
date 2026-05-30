/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jcs.precision.GeometryPrecisionReducer
 *  com.vividsolutions.jcs.precision.NumberPrecisionReducer
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.conversion;

import com.vividsolutions.jcs.precision.GeometryPrecisionReducer;
import com.vividsolutions.jcs.precision.NumberPrecisionReducer;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDatasetFactory;
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
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.conversion.PrecisionReducerDialog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;

public class PrecisionReducerPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(PrecisionReducerPlugIn.class);
    private static final int FEEDBACK_COUNTER = 100;
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.precision-reducer");
    public static final Icon ICON = IconLoader.icon("blank.png");
    protected PrecisionReducerDialog dialog;

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
        return PrecisionReducerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        solucion.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        return solucion;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.reportNothingToUndoYet(context);
        Layer sourceLayer = this.getLayer(context);
        if (sourceLayer.getUltimateFeatureCollectionWrapper().isEmpty()) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.editable-layer-is-empty"));
            return false;
        }
        this.dialog = new PrecisionReducerDialog(JUMPWorkbench.getFrameInstance(), true, sourceLayer, context.getLayerViewPanel().getSelectionManager());
        this.dialog.setVisible(true);
        if (!this.dialog.isExitOk()) {
            this.warnOperationCancelled(context);
            return false;
        }
        if (!this.dialog.hasFeaturesToProcess(context)) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.selected-collection-is-empty"));
            return false;
        }
        return true;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.reducing-precision")) + "...");
        Layer sourceLayer = this.getLayer(context);
        int scaleFactor = this.dialog.getScaleFactor();
        int decimalPlaces = this.dialog.getDecimalPlaces();
        GeometryPrecisionReducer pr = new GeometryPrecisionReducer(this.createNumberPrecisionReducer(scaleFactor, decimalPlaces));
        DataSourceQuery invalidReducedQuery = this.dialog.getInvalidReducedQuery();
        DataSourceQuery invalidInputQuery = this.dialog.getInvalidInputQuery();
        FeatureCollection fc = sourceLayer.getUltimateFeatureCollectionWrapper();
        List<List<Geometry>> bad = this.reducePrecision(fc, this.dialog.getFeaturesToProcess(context), pr, monitor);
        sourceLayer.fireAppearanceChanged();
        if (monitor.isCancelRequested()) {
            this.warnOperationCancelled(context);
            return;
        }
        if (CollectionUtils.isNotEmpty((Collection)bad.get(0))) {
            boolean isMemoryInvalidReducedQuery = false;
            if (invalidReducedQuery != null) {
                isMemoryInvalidReducedQuery = invalidReducedQuery.getDataSource() instanceof MemoryDataSource;
                FeatureCollection fcError = FeatureDatasetFactory.createFromGeometry((Collection<Geometry>)bad.get(1));
                fcError.setName(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.invalid-reduced-geometries"));
                fcError.getFeatureSchema().setGeometryType(fc.getFeatureSchema().getGeometryType());
                if (isMemoryInvalidReducedQuery) {
                    monitor.report(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.creating-layer-with-invalid-reduced-geometries"));
                    Layer errorLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcError.getName(), fcError);
                    errorLayer.setProjection(sourceLayer.getProjection());
                    errorLayer.setFeatureCollectionModified(true);
                    errorLayer.fireAppearanceChanged();
                } else {
                    PrecisionReducerPlugIn.saveResults(invalidReducedQuery, fcError, fcError.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
            }
            boolean isMemoryInvalidInputQuery = false;
            if (invalidInputQuery != null) {
                isMemoryInvalidInputQuery = invalidInputQuery.getDataSource() instanceof MemoryDataSource;
                FeatureCollection fcError = FeatureDatasetFactory.createFromGeometry((Collection<Geometry>)bad.get(0));
                fcError.setName(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.invalid-input-geometries"));
                fcError.getFeatureSchema().setGeometryType(fc.getFeatureSchema().getGeometryType());
                if (isMemoryInvalidInputQuery) {
                    monitor.report(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.creating-layer-with-invalid-input-geometries"));
                    Layer errorLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcError.getName(), fcError);
                    errorLayer.setProjection(sourceLayer.getProjection());
                    errorLayer.setFeatureCollectionModified(true);
                    errorLayer.fireAppearanceChanged();
                } else {
                    PrecisionReducerPlugIn.saveResults(invalidInputQuery, fcError, fcError.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
            }
        }
        if (!monitor.isCancelRequested()) {
            this.warnOperationSuccessful(context);
        } else {
            this.warnOperationCancelled(context);
        }
    }

    private List<List<Geometry>> reducePrecision(FeatureCollection fc, FeatureIterator it, GeometryPrecisionReducer pr, TaskMonitor monitor) throws Exception {
        ArrayList<List<Geometry>> bad = new ArrayList<List<Geometry>>(2);
        bad.add(new ArrayList());
        bad.add(new ArrayList());
        int total = -1;
        int count = 0;
        ArrayList<Feature> featsToReduce = new ArrayList<Feature>();
        while (!monitor.isCancelRequested() && it.hasNext()) {
            Feature f = it.next();
            Geometry g = f.getGeometry();
            Geometry g2 = (Geometry)g.clone();
            pr.reduce(g2);
            if (g2.isValid()) {
                Feature featToReduce = (Feature)f.clone();
                featToReduce.setGeometry(g2);
                featsToReduce.add(featToReduce);
            } else {
                ((List)bad.get(0)).add((Geometry)g.clone());
                ((List)bad.get(1)).add(g2);
            }
            if (++count % 100 != 0) continue;
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.reducing-precision")) + "...");
            monitor.report(count, total, I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.processed-geometries"));
        }
        if (!monitor.isCancelRequested()) {
            monitor.report(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.reducing-precision"));
            monitor.report(count, total, I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.procesed-geometries"));
            fc.updateAll(featsToReduce);
        }
        if (!monitor.isCancelRequested()) {
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.saving-changes")) + "...");
            fc.commit();
        } else {
            monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.PrecisionReducerPlugIn.undoing-changes")) + "...");
            fc.rollBack();
        }
        return bad;
    }

    private NumberPrecisionReducer createNumberPrecisionReducer(double scaleFactor, int decimalPlaces) {
        double sf = scaleFactor;
        if (scaleFactor != NumberPrecisionReducer.scaleFactorForDecimalPlaces((int)decimalPlaces)) {
            sf = NumberPrecisionReducer.scaleFactorForDecimalPlaces((int)decimalPlaces);
        }
        return new NumberPrecisionReducer(sf);
    }

    protected Layer getLayer(PlugInContext context) {
        return context.getLayerManager().getEditableLayers().iterator().next();
    }
}

