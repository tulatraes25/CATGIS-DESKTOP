/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jump.feature.AttributeType;
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
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.core.dao.datasource.memory.AbstractResultsFeatureIterator;
import es.kosmo.core.dao.datasource.memory.DynamicResultsFeatureCollection;
import javax.swing.Icon;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.plugin.utils.conversion.ExtractSegmentsWithAttributesResultFeatureIterator;
import org.saig.jump.plugin.utils.conversion.ExtractSegmentsWithoutAttributesResultFeatureIterator;
import org.saig.jump.widgets.utils.conversion.ExtractSegmentsDialog;

public class ExtractSegmentsPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString(ExtractSegmentsPlugIn.class, "extract-segments");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private ExtractSegmentsDialog dialog;
    protected String idOrigAttrName;

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
        return ExtractSegmentsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustNotBeRasterCheck(1));
        solucion.add(checkFactory.createAtLeastNLayersMustBeActiveCheck(1));
        solucion.add(checkFactory.createAtLeastNLayersTypeGeometryCheck(1, new int[]{3, 2, 5, 4}));
        return solucion;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        if (this.dialog == null) {
            this.dialog = new ExtractSegmentsDialog(context.getWorkbenchFrame(), true, context.getLayerManager());
        } else {
            this.dialog.refresh();
        }
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        Layer sourceLayer = this.dialog.getSelectedLayer();
        monitor.report(I18N.getMessage(this.getClass(), "extracting-segment-of-layer-{0}", new Object[]{sourceLayer.getTitle(LocaleManager.getActiveLocale())}));
        DataSourceQuery query = this.dialog.getResultQuery();
        boolean inMemory = query.getDataSource() instanceof MemoryDataSource;
        boolean mergeResults = this.dialog.isMergingResults();
        boolean copyAttributesSelected = this.dialog.isCopyAttributesSelected();
        boolean allSegmentsSelected = this.dialog.isAllSegmentsOptionSelected();
        boolean allSegmentsOneTimeSelected = this.dialog.isAllSegmentsOneTimeOptionSelected();
        FeatureSchema schemaResults = this.buildResultsSchema(sourceLayer.getFeatureSchema(), copyAttributesSelected);
        AbstractResultsFeatureIterator itFeatures = null;
        itFeatures = copyAttributesSelected ? new ExtractSegmentsWithAttributesResultFeatureIterator(sourceLayer.getUltimateFeatureCollectionWrapper(), schemaResults, this.idOrigAttrName) : new ExtractSegmentsWithoutAttributesResultFeatureIterator(sourceLayer.getUltimateFeatureCollectionWrapper(), schemaResults, mergeResults, allSegmentsSelected, allSegmentsOneTimeSelected);
        DynamicResultsFeatureCollection fc = new DynamicResultsFeatureCollection(schemaResults, itFeatures);
        fc.setName(I18N.getMessage(this.getClass(), "{0}-segments", new Object[]{sourceLayer.getTitle(LocaleManager.getActiveLocale())}));
        fc.set3d(sourceLayer.getFeatureCollectionWrapper().is3d());
        try {
            if (!monitor.isCancelRequested()) {
                monitor.report(I18N.getString(this.getClass(), "creating-new-segments-layer"));
                if (inMemory) {
                    Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fc.getName(), fc.toFeatureDataset(), sourceLayer.getProjection());
                    newLayer.setFeatureCollectionModified(false);
                } else {
                    ExtractSegmentsPlugIn.saveResults(query, fc, fc.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
                if (!monitor.isCancelRequested()) {
                    context.getWorkbenchFrame().warnUser(I18N.getMessage(this.getClass(), "segments-layer-{0}-correctly-created", new Object[]{fc.getName()}));
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

    protected FeatureSchema buildResultsSchema(FeatureSchema sourceSchema, boolean copyAttributes) {
        FeatureSchema schemaResults = null;
        if (copyAttributes) {
            schemaResults = (FeatureSchema)sourceSchema.clone();
            int cont = 0;
            this.idOrigAttrName = "ID_ORIG";
            boolean exists = schemaResults.getAttribute(this.idOrigAttrName) != null;
            while (exists) {
                this.idOrigAttrName = "ID_ORIG_" + cont++;
                boolean bl = exists = schemaResults.getAttribute(this.idOrigAttrName) != null;
            }
            schemaResults.addAttribute(this.idOrigAttrName, schemaResults.getPrimaryKey().getType());
        } else {
            schemaResults = new FeatureSchema();
            schemaResults.addAttribute("GID", AttributeType.INTEGER, true);
            schemaResults.addAttribute("geometry", AttributeType.GEOMETRY);
        }
        schemaResults.setGeometryType(3);
        return schemaResults;
    }
}

