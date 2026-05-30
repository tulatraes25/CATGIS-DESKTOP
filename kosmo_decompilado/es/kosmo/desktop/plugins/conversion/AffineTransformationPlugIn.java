/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.util.AffineTransformation
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.conversion;

import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jump.feature.FeatureSchema;
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
import es.kosmo.core.dao.datasource.memory.DynamicResultsFeatureCollection;
import es.kosmo.desktop.plugins.conversion.AffineTransformationResultsFeatureIterator;
import es.kosmo.desktop.widgets.conversion.AffineTransformationDialog;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;

public class AffineTransformationPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(AffineTransformationPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.AffineTransformationPlugIn.new-affine-transformation");
    public static final Icon ICON = IconLoader.icon("blank.png");
    protected AffineTransformationDialog dialog;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layer sourceLayer = this.getLayer(context);
        if (sourceLayer.getUltimateFeatureCollectionWrapper().isEmpty()) {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.AffineTransformationPlugIn.selected-layer-has-no-elements"));
            return false;
        }
        this.dialog = new AffineTransformationDialog(JUMPWorkbench.getFrameInstance(), true, sourceLayer, context.getLayerViewPanel().getSelectionManager(), context.getLayerViewPanel().getLayerManager());
        this.dialog.setVisible(true);
        if (!this.dialog.isExitOk()) {
            this.warnOperationCancelled(context);
            return false;
        }
        if (!this.dialog.hasFeaturesToProcess(context)) {
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.AffineTransformationPlugIn.selected-collection-is-empty"));
            return false;
        }
        return true;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        DataSourceQuery query = this.dialog.getResultQuery();
        boolean inMemory = query.getDataSource() instanceof MemoryDataSource;
        Layer sourceLayer = this.getLayer(context);
        double originX = this.dialog.getDoubleOriginX();
        double originY = this.dialog.getDoubleOriginY();
        double transX = this.dialog.getDoubleTransX();
        double transY = this.dialog.getDoubleTransY();
        double scaleX = this.dialog.getDoubleScaleX();
        double scaleY = this.dialog.getDoubleScaleY();
        double shearX = this.dialog.getDoubleShearX();
        double shearY = this.dialog.getDoubleShearY();
        double rotationAngle = this.dialog.getDoubleRotationAngle();
        AffineTransformation trans = new AffineTransformation();
        AffineTransformation toOriginTrans = AffineTransformation.translationInstance((double)(-originX), (double)(-originY));
        trans.compose(toOriginTrans);
        if (scaleX != 1.0 || scaleY != 1.0) {
            trans.scale(scaleX, scaleY);
        }
        if (shearX != 0.0 || shearY != 0.0) {
            trans.shear(shearX, shearY);
        }
        if (rotationAngle != 0.0) {
            trans.rotate(Math.toRadians(rotationAngle));
        }
        AffineTransformation fromOriginTrans = AffineTransformation.translationInstance((double)originX, (double)originY);
        trans.compose(fromOriginTrans);
        if (transX != 0.0 || transY != 0.0) {
            AffineTransformation translateTrans = AffineTransformation.translationInstance((double)transX, (double)transY);
            trans.compose(translateTrans);
        }
        FeatureSchema schemaResults = this.buildResultsSchema(sourceLayer);
        AffineTransformationResultsFeatureIterator itFeatures = new AffineTransformationResultsFeatureIterator(this.dialog, context, schemaResults, trans);
        DynamicResultsFeatureCollection fc = new DynamicResultsFeatureCollection(schemaResults, itFeatures);
        fc.setName(String.valueOf(sourceLayer.getTitle(LocaleManager.getActiveLocale())) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.AffineTransformationPlugIn.affine"));
        fc.set3d(sourceLayer.getFeatureCollectionWrapper().is3d());
        try {
            if (!monitor.isCancelRequested()) {
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.AffineTransformationPlugIn.realizing-transformation")) + "...");
                if (inMemory) {
                    Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fc.getName(), fc.toFeatureDataset(), sourceLayer.getProjection());
                    newLayer.setFeatureCollectionModified(false);
                } else {
                    AffineTransformationPlugIn.saveResults(query, fc, fc.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
                if (!monitor.isCancelRequested()) {
                    this.warnOperationSuccessful(context);
                } else {
                    this.warnOperationCancelled(context);
                }
            } else {
                this.warnOperationCancelled(context);
            }
        }
        finally {
            if (fc != null) {
                fc.dispose();
            }
        }
    }

    protected FeatureSchema buildResultsSchema(Layer sourceLayer) {
        FeatureSchema newSchema = (FeatureSchema)sourceLayer.getFeatureSchema().clone();
        return newSchema;
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
        return AffineTransformationPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        solucion.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        return solucion;
    }

    protected Layer getLayer(PlugInContext context) {
        return (Layer)context.getLayerNamePanel().getSelectedLayers()[0];
    }
}

