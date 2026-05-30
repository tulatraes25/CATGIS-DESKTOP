/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserDialog;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.OracleSpatialDataSource;
import org.saig.core.dao.datasource.dbdatasource.utils.EpsgToOracleCodeConverter;
import org.saig.core.dao.datasource.dbdatasource.utils.OracleToEpsgCodeNotFoundException;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.util.MetadataManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class LoadDatasetPlugIn
extends ThreadedBasePlugIn {
    private static final Logger LOGGER = Logger.getLogger(LoadDatasetPlugIn.class);
    public static final String LAST_FORMAT_KEY = String.valueOf(LoadDatasetPlugIn.class.getName()) + " - LAST FORMAT";
    public static final String NAME = String.valueOf(I18N.getString("workbench.datasource.LoadDatasetPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("Plus.gif");
    protected DataSourceQueryChooserDialog dialog;

    @Override
    public void initialize(final PlugInContext context) throws Exception {
        context.getWorkbenchFrame().addWindowListener(new WindowAdapter(){

            @Override
            public void windowOpened(WindowEvent e) {
                String format = (String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(LAST_FORMAT_KEY);
                if (format != null && LoadDatasetPlugIn.this.dialog != null) {
                    LoadDatasetPlugIn.this.dialog.setSelectedFormat(format);
                }
            }
        });
    }

    protected DataSourceQueryChooserDialog getDialog(PlugInContext context) {
        return new DataSourceQueryChooserDialog(DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard()).getLoadDataSourceQueryChoosers(), context.getWorkbenchFrame(), this.getName(), true);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.dialog = this.getDialog(context);
        String format = (String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(LAST_FORMAT_KEY);
        if (format != null) {
            this.dialog.setSelectedFormat(format);
        }
        this.dialog.refreshPath();
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.setVisible(true);
        if (this.dialog.wasOKPressed()) {
            PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put(LAST_FORMAT_KEY, this.dialog.getSelectedFormat());
        }
        return this.dialog.wasOKPressed();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        Collection<DataSourceQuery> dataSourceQueries = this.dialog.getCurrentChooser().getDataSourceQueries();
        Assert.isTrue((!dataSourceQueries.isEmpty() ? 1 : 0) != 0);
        Iterator<DataSourceQuery> i = dataSourceQueries.iterator();
        while (i.hasNext()) {
            LoadDatasetPlugIn.loadDataSourceQueryToLayer(monitor, context, i.next(), LoadDatasetPlugIn.chooseCategory(context));
        }
    }

    public static String chooseCategory(PlugInContext context) {
        return context.getLayerNamePanel().getSelectedCategories().isEmpty() ? StandardCategoryNames.WORKING : context.getLayerNamePanel().getSelectedCategories().iterator().next().toString();
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createTaskWindowMustBeActiveCheck());
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
        return LoadDatasetPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static void loadDataSourceQueryToLayer(TaskMonitor monitor, PlugInContext context, DataSourceQuery dataSourceQuery, String category) {
        block20: {
            Assert.isTrue((boolean)dataSourceQuery.getDataSource().isReadable());
            monitor.report(String.valueOf(I18N.getMessage("workbench.datasource.LoadDatasetPlugIn.loading-layer-{0}", new Object[]{dataSourceQuery.toString()})) + "...");
            Task activeTask = context.getTask();
            Connection connection = dataSourceQuery.getDataSource().getConnection();
            try {
                try {
                    FeatureCollection[] fcs = connection.executeQuery(dataSourceQuery.getQuery(), activeTask.getProjection(), dataSourceQuery.getDataSource().getProperties());
                    if (fcs == null) break block20;
                    int j = 0;
                    while (j < fcs.length) {
                        if (fcs[j] != null) {
                            String layerName = fcs[j].getName();
                            if (layerName == null) {
                                layerName = dataSourceQuery.toString();
                            }
                            IProjection proj = null;
                            if (fcs[j] instanceof FeatureCollectionOnDemand) {
                                FeatureCollectionOnDemand fcd = (FeatureCollectionOnDemand)fcs[j];
                                if (fcd.getDataAccesor() instanceof AbstractJDBCDataSource) {
                                    int srid = ((AbstractJDBCDataSource)fcd.getDataAccesor()).getSrid();
                                    if (fcd.getDataAccesor() instanceof OracleSpatialDataSource) {
                                        try {
                                            srid = EpsgToOracleCodeConverter.getInstance().oracleCodeToEPSGCode(srid);
                                        }
                                        catch (OracleToEpsgCodeNotFoundException e) {
                                            srid = -1;
                                            DialogFactory.showWarningDialog(context.getWorkbenchFrame(), String.valueOf(e.getMessage()) + "." + I18N.getString("com.vividsolutions.jump.workbench.datasource.LoadDatasetPlugIn.The-current-view-projection-will-be-stablished"), I18N.getString("com.vividsolutions.jump.workbench.datasource.LoadDatasetPlugIn.EPSG-code-not-found"));
                                        }
                                    }
                                    if (srid != -1) {
                                        try {
                                            proj = CrsRepositoryManager.getInstance().getCRS("EPSG:" + srid);
                                            if (!activeTask.getProjection().getAbrev().equals("EPSG:" + srid)) {
                                                context.getWorkbenchFrame().warnUser(I18N.getString("com.vividsolutions.jump.workbench.datasource.LoadDatasetPlugIn.the-layer-spatial-reference-system-is-different-from-the-view-one-revise-if-it-is-necessary-to-configure-a-transformation"));
                                            }
                                        }
                                        catch (Exception e) {
                                            LOGGER.error((Object)"", (Throwable)e);
                                            LOGGER.warn((Object)I18N.getString("com.vividsolutions.jump.workbench.datasource.LoadDatasetPlugIn.assign-the-current-view-spatial-reference-system-by-default"));
                                            proj = activeTask.getProjection();
                                        }
                                    } else {
                                        proj = activeTask.getProjection();
                                    }
                                } else {
                                    proj = activeTask.getProjection();
                                }
                            } else {
                                proj = activeTask.getProjection();
                            }
                            Layer layer = context.getLayerManager().addLayer(category, layerName, fcs[j], proj);
                            layer.setDataSourceQuery(dataSourceQuery);
                            layer.setFeatureCollectionModified(false);
                            layer.setMetadata(MetadataManager.getInstance().loadMetadata(layer.getUltimateFeatureCollectionWrapper(), dataSourceQuery));
                        }
                        ++j;
                    }
                }
                catch (Exception ex) {
                    LOGGER.error((Object)"", (Throwable)ex);
                    DialogFactory.showErrorDialog(context.getWorkbenchFrame(), String.valueOf(dataSourceQuery.getQuery()) + " - " + ex.getMessage(), I18N.getString("workbench.datasource.LoadDatasetPlugIn.error-loading-data"));
                    connection.close();
                }
            }
            finally {
                connection.close();
            }
        }
    }
}

