/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.conversion;

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
import es.kosmo.core.dao.datasource.memory.DynamicResultsFeatureCollection;
import es.kosmo.core.utils.FeatureSchemaUtils;
import es.kosmo.desktop.images.DesktopIconLoader;
import es.kosmo.desktop.plugins.conversion.TableToLayerResultsFeatureIterator;
import es.kosmo.desktop.widgets.conversion.TableToLayerDialog;
import javax.swing.Icon;
import javax.swing.JFrame;
import org.apache.log4j.Logger;
import org.saig.core.model.data.Table;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;

public class TableToLayerPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(TableToLayerPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.widgets.utils.conversion.TableToLayerDialog.Convert-table-to-layer");
    public static final Icon ICON = DesktopIconLoader.icon("tableToLayer.png");
    protected TableToLayerDialog dialog;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.dialog = new TableToLayerDialog((JFrame)JUMPWorkbench.getFrameInstance(), true, context.getWorkbenchContext().getDataManager().getRealTables());
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("es.kosmo.desktop.plugins.conversion.TableToLayerPlugIn.Transforming-table-to-layer")) + "...");
        Table table = this.dialog.getSelectedTable();
        boolean createPointLayer = this.dialog.isToPoint();
        DataSourceQuery query = this.dialog.getDataSourceQuery();
        boolean inMemory = query.getDataSource() instanceof MemoryDataSource;
        FeatureSchema schemaResults = this.buildResultFeatureSchema(table, createPointLayer);
        TableToLayerResultsFeatureIterator itFeatures = new TableToLayerResultsFeatureIterator(this.dialog, schemaResults, table);
        DynamicResultsFeatureCollection fc = new DynamicResultsFeatureCollection(schemaResults, itFeatures);
        String baseName = table.getName();
        baseName = createPointLayer ? String.valueOf(baseName) + I18N.getString("org.saig.jump.plugin.utils.conversion.TableToLayerPlugIn._POINTS") : String.valueOf(baseName) + I18N.getString("org.saig.jump.plugin.utils.conversion.TableToLayerPlugIn._RECTANGLES");
        fc.setName(baseName);
        fc.set3d(this.dialog.getSelectedPointZName() != null);
        try {
            if (!monitor.isCancelRequested()) {
                if (createPointLayer) {
                    monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.TableToLayerPlugIn.Generating-points-layer")) + "...");
                } else {
                    monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.conversion.TableToLayerPlugIn.Generating-rectangles-layer")) + "...");
                }
                if (inMemory) {
                    Layer newLayer = context.getLayerManager().addLayer(StandardCategoryNames.RESULT_SUBJECT, fc.getName(), fc.toFeatureDataset(), context.getTask().getProjection());
                    newLayer.setFeatureCollectionModified(false);
                } else {
                    TableToLayerPlugIn.saveResults(query, fc, fc.getName(), null, context.getTask().getProjection(), true, monitor);
                }
                if (!monitor.isCancelRequested()) {
                    if (itFeatures.getNumErrors() == 0) {
                        this.warnOperationSuccessful(context);
                    } else {
                        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.conversion.TableToLayerPlugIn.{0}-errors-were-found-while-generating-the-points-layer", new Object[]{itFeatures.getNumErrors()}));
                    }
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

    private FeatureSchema buildResultFeatureSchema(Table table, boolean createPointLayer) {
        FeatureSchema result = (FeatureSchema)table.getSchema().clone();
        result.addAttribute(FeatureSchemaUtils.getUniqueAttributeName(result, "geometry"), AttributeType.GEOMETRY);
        if (createPointLayer) {
            result.setGeometryType(1);
        } else {
            result.setGeometryType(5);
        }
        return result;
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
        return TableToLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck check = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        check.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNTablesMustExistCheck(1));
        return check;
    }
}

