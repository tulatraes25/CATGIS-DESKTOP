/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
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
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.utils.AbstractBasicOptionsDialog;

public class ExplodeEntitiesPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(ExplodeEntitiesPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Explode-entities");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private String sourceLayerName;
    private DataSourceQuery query;

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
        return ExplodeEntitiesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        solucion.add(checkFactory.createSelectedLayersMustNotBeRasterCheck());
        return solucion;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layerable[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
        Layer sourceLayer = (Layer)selectedLayers[0];
        this.sourceLayerName = sourceLayer.getName();
        AbstractBasicOptionsDialog dialog = new AbstractBasicOptionsDialog(JUMPWorkbench.getFrameInstance(), true, this.getName(), null, null);
        GUIUtil.centre(dialog, JUMPWorkbench.getFrameInstance());
        dialog.setVisible(true);
        if (dialog.wasOkPressed()) {
            this.query = dialog.getResultsQuery();
        }
        return dialog.wasOkPressed();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Processing-features-from-selected-layer")) + "...");
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        FeatureSchema newSchema = this.buildNewFeatureSchema(sourceLayer, context);
        if (newSchema == null) {
            return;
        }
        FeatureDataset fcNuevo = new FeatureDataset(newSchema);
        int id = 0;
        int contador = 0;
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        int numElements = fcOrig.size();
        fcNuevo.setName(String.valueOf(this.sourceLayerName) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Simple"));
        fcNuevo.set3d(sourceLayer.getFeatureCollectionWrapper().is3d());
        String primaryKeyName = newSchema.getPrimaryKeyName();
        AttributeType primaryKeyType = newSchema.getPrimaryKey().getType();
        FeatureIterator it = null;
        try {
            it = fcOrig.iterator();
            while (!monitor.isCancelRequested() && it.hasNext()) {
                Feature featOrig = it.next();
                Geometry geom = featOrig.getGeometry();
                int i = 0;
                while (i < geom.getNumGeometries()) {
                    Feature feat = (Feature)featOrig.clone();
                    feat.setSchema(newSchema);
                    feat.setAttribute(primaryKeyName, FeatureUtil.getGoodAttribute(primaryKeyType, id++));
                    feat.setID(FeatureUtil.nextID());
                    feat.setGeometry(geomFac.createGeometry(geom.getGeometryN(i)));
                    fcNuevo.addWithNewKey(feat);
                    ++i;
                }
                if (contador++ % 100 != 0) continue;
                monitor.report(contador, numElements, I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Processed-features"));
            }
            if (monitor.isCancelRequested()) {
                this.warnOperationCancelled(context);
                return;
            }
            if (fcNuevo.size() < fcOrig.size()) {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.The-number-of-processed-features-has-been-lesser-than-the-original"), I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Error-in-the-number-of-precessed-features"));
                return;
            }
            boolean isMemory = this.query.getDataSource() instanceof MemoryDataSource;
            if (!fcNuevo.isEmpty()) {
                if (isMemory) {
                    monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Creating-layer")) + "...");
                    Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fcNuevo.getName(), fcNuevo);
                    newLayer.setProjection(sourceLayer.getProjection());
                    newLayer.setFeatureCollectionModified(true);
                } else {
                    monitor.report(I18N.getString(this.getClass(), "saving-results-layer"));
                    ExplodeEntitiesPlugIn.saveResults(this.query, fcNuevo, fcNuevo.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
            }
            context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Extraction-of-simple-objects-finished"));
            return;
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    private FeatureSchema buildNewFeatureSchema(Layer sourceLayer, PlugInContext context) {
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        FeatureSchema newSchema = (FeatureSchema)fcOrig.getFeatureSchema().clone();
        int schemaOrigType = newSchema.getGeometryType();
        switch (schemaOrigType) {
            case 8: {
                newSchema.setGeometryType(1);
                break;
            }
            case 2: {
                newSchema.setGeometryType(3);
                break;
            }
            case 4: {
                newSchema.setGeometryType(5);
                break;
            }
            case 0: {
                LOGGER.warn((Object)I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Geometry-type-unknown"));
                context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.ExplodeEntitiesPlugIn.Selected-layer-has-features-with-unknown-geometries"));
                return null;
            }
        }
        return newSchema;
    }
}

