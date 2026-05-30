/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
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
import es.kosmo.core.dao.datasource.memory.DynamicResultsFeatureCollection;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.plugin.utils.conversion.ExtractVertexLayerResultsFeatureIterator;
import org.saig.jump.widgets.utils.AbstractBasicOptionsDialog;

public class ExtractVertexLayerPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(ExtractVertexLayerPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.conversion.ExtractVertexLayerPlugIn.Extract-first-and-last-vertex-from-lines");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private String sourceLayerName;
    protected AbstractBasicOptionsDialog dialog;

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
        return ExtractVertexLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerTypeGeometryCheck(new int[]{3, 2}));
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layerable[] selectedLayers = context.getLayerNamePanel().getSelectedLayers();
        Layer sourceLayer = (Layer)selectedLayers[0];
        this.sourceLayerName = sourceLayer.getName();
        this.dialog = new AbstractBasicOptionsDialog(JUMPWorkbench.getFrameInstance(), true, this.getName(), null, null);
        GUIUtil.centre(this.dialog, JUMPWorkbench.getFrameInstance());
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.ExtractVertexLayerPlugIn.Creating-new-data-structure")) + "...");
        DataSourceQuery query = this.dialog.getResultsQuery();
        boolean inMemory = query.getDataSource() instanceof MemoryDataSource;
        Layer sourceLayer = JUMPWorkbench.getLayer(this.sourceLayerName);
        FeatureSchema schemaResults = this.buildResultsSchema(sourceLayer);
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.ExtractVertexLayerPlugIn.Getting-points-from-lines")) + "...");
        ExtractVertexLayerResultsFeatureIterator itFeatures = new ExtractVertexLayerResultsFeatureIterator(sourceLayer.getUltimateFeatureCollectionWrapper(), schemaResults);
        DynamicResultsFeatureCollection fc = new DynamicResultsFeatureCollection(schemaResults, itFeatures);
        fc.setName(String.valueOf(sourceLayer.getTitle(LocaleManager.getActiveLocale())) + " - " + I18N.getString("org.saig.jump.plugin.utils.conversion.ExtractVertexLayerPlugIn.First-last-vertex"));
        fc.set3d(sourceLayer.getFeatureCollectionWrapper().is3d());
        try {
            if (!monitor.isCancelRequested()) {
                monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.GetPointsFromLinesPlugIn.Creating-new-data-layer")) + "...");
                if (inMemory) {
                    Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fc.getName(), fc.toFeatureDataset(), sourceLayer.getProjection());
                    newLayer.setFeatureCollectionModified(false);
                } else {
                    ExtractVertexLayerPlugIn.saveResults(query, fc, fc.getName(), null, sourceLayer.getProjection(), true, monitor);
                }
                if (!monitor.isCancelRequested()) {
                    context.getWorkbenchFrame().warnUser(I18N.getString("org.saig.jump.plugin.utils.conversion.ExtractVertexLayerPlugIn.Vertices-correctly-extracted"));
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
        FeatureCollection fcOrig = sourceLayer.getUltimateFeatureCollectionWrapper();
        FeatureSchema newSchema = new FeatureSchema();
        newSchema.addAttribute("GID", AttributeType.INTEGER, true);
        FeatureSchema oldSchema = fcOrig.getFeatureSchema();
        int i = 0;
        while (i < oldSchema.getAttributeCount()) {
            if (i != oldSchema.getGeometryIndex()) {
                Attribute attr = oldSchema.getAttribute(i);
                String nombre = attr.getName();
                AttributeType type = attr.getType();
                if (!attr.isPrimaryKey()) {
                    newSchema.addAttribute(nombre, type);
                }
            }
            ++i;
        }
        newSchema.addAttribute("geometry", AttributeType.GEOMETRY);
        newSchema.setGeometryType(1);
        return newSchema;
    }
}

