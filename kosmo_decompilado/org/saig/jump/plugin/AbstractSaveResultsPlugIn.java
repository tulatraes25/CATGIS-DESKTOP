/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserDialog;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.MemoryDataSource;
import org.saig.jump.widgets.util.DialogFactory;

public class AbstractSaveResultsPlugIn
extends AbstractPlugIn {
    protected static final Logger LOGGER = Logger.getLogger(AbstractSaveResultsPlugIn.class);
    private static final String DEFAULT_CATEGORY = StandardCategoryNames.RESULT_SUBJECT;

    protected static DataSourceQueryChooserDialog getSaveAsDialog(String name) {
        return new DataSourceQueryChooserDialog(DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard()).getSaveDataSourceQueryChoosers(), JUMPWorkbench.getFrameInstance(), name, true);
    }

    protected DataSourceQuery getSaveAsQuery(FeatureSchema schema, Envelope envelope, int srid) throws Exception {
        String defaultDestino = I18N.getString("org.saig.jump.plugin.AbstractSaveResultsPlugIn.save-results");
        return this.getSaveAsQuery(defaultDestino, schema, envelope, srid);
    }

    protected DataSourceQuery getSaveAsQuery(String destino, FeatureSchema schema, Envelope envelope, int srid) throws Exception {
        return this.getSaveAsQuery(destino, schema, envelope, srid, this.getName());
    }

    protected DataSourceQuery getSaveAsQuery(String destino, FeatureSchema schema, Envelope envelope, int srid, String pluginName) throws Exception {
        return AbstractSaveResultsPlugIn.getSaveAsQuery(destino, schema, envelope, srid, pluginName, true);
    }

    public static DataSourceQuery getSaveAsQuery(String destino, String pluginName, boolean askSaveInMemory) throws Exception {
        return AbstractSaveResultsPlugIn.getSaveAsQuery(destino, null, null, 0, pluginName, askSaveInMemory);
    }

    public static DataSourceQuery getSaveAsQuery(String destino, FeatureSchema schema, Envelope envelope, int srid, String pluginName, boolean askSaveInMemory) throws Exception {
        DataSourceQuery dataSourceQuery = null;
        int option = 0;
        if (askSaveInMemory) {
            option = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.AbstractSaveResultsPlugIn.do-you-wish-to-save-data-permanently"), destino);
        }
        if (option == 0) {
            String format = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(SaveDatasetAsPlugIn.LAST_FORMAT_KEY);
            DataSourceQueryChooserDialog dialog = AbstractSaveResultsPlugIn.getSaveAsDialog(pluginName);
            if (format != null) {
                dialog.setSelectedFormat(format);
            }
            dialog.refreshPath();
            dialog.setTitle(destino);
            GUIUtil.centreOnWindow(dialog);
            dialog.setVisible(true);
            if (dialog.wasOKPressed()) {
                PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).put(SaveDatasetAsPlugIn.LAST_FORMAT_KEY, dialog.getSelectedFormat());
                dataSourceQuery = dialog.getCurrentChooser().getDataSourceQueries().iterator().next();
            }
        } else if (option == 1) {
            dataSourceQuery = new DataSourceQuery(new MemoryDataSource(), null, null);
        }
        return dataSourceQuery;
    }

    protected void saveResults(DataSourceQuery query, FeatureCollection fcResults, String layerName, IProjection proj, TaskMonitor monitor) throws Exception {
        AbstractSaveResultsPlugIn.saveResults(query, fcResults, layerName, null, proj, false, monitor);
    }

    public static List<Layer> saveResults(DataSourceQuery query, FeatureCollection fcResults, String layerName, String categoryName, IProjection projection, boolean loadResultsAsNewLayer, TaskMonitor monitor) throws Exception {
        return AbstractSaveResultsPlugIn.saveResults(query, fcResults, layerName, categoryName, projection, loadResultsAsNewLayer, null, monitor);
    }

    public static List<Layer> saveResults(DataSourceQuery query, FeatureCollection fcResults, String layerName, String categoryName, IProjection projection, boolean loadResultsAsNewLayer, Style style, TaskMonitor monitor) throws Exception {
        ArrayList<Layer> resultLayers = new ArrayList<Layer>();
        Connection connection = query.getDataSource().getConnection();
        monitor.report(I18N.getMessage("workbench.datasource.SaveDatasetAsPlugIn.saving-layer-{0}", new Object[]{layerName}));
        try {
            connection.executeUpdate(query.getQuery(), fcResults, false, null, projection, monitor);
        }
        catch (Exception e) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("workbench.datasource.SaveDatasetAsPlugIn.an-unexpected-error-has-been-produced-while-saving-the-layer")) + "\n" + e.getMessage(), I18N.getString("workbench.datasource.SaveDatasetAsPlugIn.error-saving-layer"));
            throw e;
        }
        if (!monitor.isCancelRequested()) {
            if (loadResultsAsNewLayer && query.getDataSource().isReadable()) {
                Task activeTask = JUMPWorkbench.getFrameInstance().getContext().getTask();
                WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
                if (categoryName == null || categoryName.equals("")) {
                    categoryName = DEFAULT_CATEGORY;
                }
                if (projection == null && activeTask != null) {
                    projection = activeTask.getProjection();
                }
                try {
                    try {
                        FeatureCollection[] fcs = connection.executeQuery(query.getQuery(), projection);
                        int j = 0;
                        while (j < fcs.length) {
                            if (fcs[j] != null) {
                                if (layerName == null) {
                                    layerName = query.toString();
                                }
                                Layer layer = context.getLayerManager().addLayer(categoryName, layerName, fcs[j]).setDataSourceQuery(query).setFeatureCollectionModified(false);
                                layer.setProjection(projection);
                                if (style != null) {
                                    layer.setModelStyle(style);
                                    layer.fireAppearanceChanged();
                                }
                                resultLayers.add(layer);
                            }
                            ++j;
                        }
                    }
                    catch (Exception ex) {
                        LOGGER.error((Object)"", (Throwable)ex);
                        DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(query.getQuery()) + " - " + ex.getMessage(), I18N.getString("workbench.datasource.LoadDatasetPlugIn.error-loading-data"));
                        connection.close();
                    }
                }
                finally {
                    connection.close();
                }
            }
        } else {
            return new ArrayList<Layer>();
        }
        return resultLayers;
    }
}

