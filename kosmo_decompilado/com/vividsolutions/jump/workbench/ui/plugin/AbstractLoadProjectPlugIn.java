/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.wms.WMService;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.ICrs;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.sdi.wfs.WFSFeatureCollection;
import org.saig.core.model.sdi.wfs.WFSLayer;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.MetadataManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.sdi.wfs.WfsLayerBuilder;
import org.saig.jump.widgets.datasource.AddJDBCSourceDialog;
import org.saig.jump.widgets.summary.SummaryMessage;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.SelectGeometryTypeDialog;

public abstract class AbstractLoadProjectPlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(AbstractLoadProjectPlugIn.class);
    protected JFileChooser fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
    protected Map<String, String> oldPathtoNewPath = new HashMap<String, String>();

    public void loadCategory(Task newTask, Category sourceLayerCategory, TaskMonitor monitor, List<SummaryMessage> messageList) throws Exception {
        this.loadCategory(newTask, sourceLayerCategory, monitor, messageList, false);
    }

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public void loadCategory(Task newTask, Category sourceLayerCategory, TaskMonitor monitor, List<SummaryMessage> messageList, boolean loadAsFirst) throws Exception {
        newLayerManager = newTask.getLayerManager();
        if (loadAsFirst) {
            newLayerManager.addCategory(sourceLayerCategory.getName(), 0, sourceLayerCategory.isCollapsed(), sourceLayerCategory.getTitleByLang(), sourceLayerCategory.getProperties());
        } else {
            newLayerManager.addCategory(sourceLayerCategory.getName(), sourceLayerCategory.isCollapsed(), sourceLayerCategory.getTitleByLang(), sourceLayerCategory.getProperties());
        }
        layerables = new ArrayList<Layerable>(sourceLayerCategory.getLayerables());
        Collections.reverse(layerables);
        queryToFc = new HashMap<String, FeatureCollection[]>();
        layerable = null;
        total = layerables.size();
        cont = 0;
        j = layerables.iterator();
        block6: while (j.hasNext()) {
            block22: {
                try {
                    layerable = (Layerable)j.next();
                    monitor.report(String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn.Loading-the-layers-associated-to-the-category-{0}", new Object[]{sourceLayerCategory.getTitle(LocaleManager.getActiveLocale())})) + "...");
                    monitor.report(cont++, total, I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.loading-layer-{0}", new Object[]{layerable.getName()}));
                    layerable.setLayerManager(newLayerManager);
                    if (layerable instanceof WFSLayer) {
                        layer /* !! */  = (WFSLayer)layerable;
                        this.loadWFSLayer(layer /* !! */ , newTask);
                    } else if (layerable instanceof Layer) {
                        layer /* !! */  = (Layer)layerable;
                        this.loadLayer(layer /* !! */ , newTask, queryToFc);
                    } else if (layerable instanceof WMSLayer) {
                        wmsError = "";
                        wmsLayer = (WMSLayer)layerable;
                        service = new WMService(wmsLayer.getServerURL());
                        if (wmsLayer.getServiceVersion() != null) {
                            service.setWmsVersion(wmsLayer.getServiceVersion());
                        }
                        try {
                            service.setBasicAuthData(wmsLayer.getBasicAuthData());
                            service.initialize();
                        }
                        catch (Exception e) {
                            wmsError = String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn.An-error-has-been-produced-while-connecting-with-the-server-{0}", new Object[]{service.getServerUrl()})) + ": " + e.getMessage();
                        }
                        if (!service.isInitialized() && wmsError.isEmpty()) {
                            wmsError = I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn.An-error-has-been-produced-while-connecting-with-the-server-{0}", new Object[]{service.getServerUrl()});
                        }
                        service.setFormat(wmsLayer.getFormat());
                        service.setTransparent(wmsLayer.isTransparent());
                        service.setInformationFormat(wmsLayer.getInformationFormat());
                        service.setInformationFeatureCount(wmsLayer.getInformationFeatureCount());
                        service.setExceptionFormat(wmsLayer.getExceptionFormat());
                        service.setUseDeclaredCapabilitiesURLs(wmsLayer.isUseDeclaredCapabilitiesURLs());
                        service.setVendorParameters(wmsLayer.getVendorParameters());
                        service.setAxisOrder(wmsLayer.getAxisOrder());
                        wmsLayer.setService(service);
                        if (!wmsError.isEmpty()) {
                            AbstractLoadProjectPlugIn.LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.exception-loading-the-layer-{0}", new Object[]{layerable.getName()})) + " - " + wmsError));
                            messageList.add(this.buildLayerErrorMessage(layerable.getName(), new Exception(wmsError)));
                        }
                    }
                    newLayerManager.addLayerable(sourceLayerCategory.getName(), layerable);
                    continue;
                }
                catch (Exception e) {
                    if (!(e instanceof FileNotFoundException)) break block22;
                    hecho = false;
                    ** while (!hecho)
                }
lbl-1000:
                // 1 sources

                {
                    try {
                        this.loadNotFoundFile(layerable, messageList, e, newLayerManager, sourceLayerCategory);
                        hecho = true;
                    }
                    catch (Exception e1) {
                        hecho = false;
                    }
                    continue;
lbl69:
                    // 1 sources

                    continue block6;
                }
            }
            if (e instanceof SQLException) {
                layer = (Layer)layerable;
                opcion = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getMessage("workbench.ui.plugin.AbstractLoadProjectPlugIn.the-table-associated-with-the-layer-{0}-have-not-been-found", new Object[]{layerable.getName()})) + ".\n" + I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.do-you-want-to-select-the-correct-table-localization"), I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.error-loading-layer"));
                if (opcion == 0) {
                    dialog = new AddJDBCSourceDialog(JUMPWorkbench.getFrameInstance(), true, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
                    if (dialog.isOk()) {
                        datasource = dialog.getDataSource();
                        fc = new FeatureCollectionOnDemand();
                        if (!datasource.isInitialized()) {
                            datasource.initialize();
                        }
                        fc.setId(1L);
                        fc.setName(datasource.getTableName());
                        fc.setDataAccesor(datasource);
                        fc.setSchema(datasource.getSchema());
                        layer.setFeatureCollection(fc);
                        layer.setFeatureCollectionModified(false);
                        newLayerManager.addLayerable(sourceLayerCategory.getName(), layerable);
                        continue;
                    }
                    AbstractLoadProjectPlugIn.LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.exception-loading-the-layer-{0}", new Object[]{layerable.getName()})) + " - " + e.getMessage()));
                    messageList.add(this.buildLayerErrorMessage(layerable.getName(), e));
                    continue;
                }
                AbstractLoadProjectPlugIn.LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.exception-loading-the-layer-{0}", new Object[]{layerable.getName()})) + " - " + e.getMessage()));
                messageList.add(this.buildLayerErrorMessage(layerable.getName(), e));
                continue;
            }
            AbstractLoadProjectPlugIn.LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.exception-loading-the-layer-{0}", new Object[]{layerable.getName()})) + " - " + e.getMessage()));
            messageList.add(this.buildLayerErrorMessage(layerable.getName(), e));
        }
        System.gc();
    }

    protected void loadWFSLayer(WFSLayer layer, Task newTask) throws Exception {
        ICrs proj = null;
        if (layer.getCrsWKT() == null) {
            if (layer.getCrsDescription() != null && !layer.getCrsDescription().equals("")) {
                proj = CrsRepositoryManager.getInstance().getCRS(layer.getCrsDescription());
            }
        } else {
            String params = layer.getCrsParams();
            proj = params == null ? CrsRepositoryManager.getInstance().getCRS(layer.getCrsCode(), layer.getCrsWKT()) : CrsRepositoryManager.getInstance().getCRS(layer.getCrsCode(), layer.getCrsWKT(), params);
            proj.setTransParam(layer.getNadGrid());
            proj.setTransInTarget(layer.isTargetNad());
        }
        layer.setProjection(proj, newTask.getProjection());
        if (layer.isEnabled()) {
            WfsLayerBuilder wfsLayerBuilder = new WfsLayerBuilder();
            List<WFSFeatureCollection> collections = wfsLayerBuilder.createCollections(layer.getVersion(), layer.getUrl(), layer.getWfsFeatureType(), layer.getSelectedAttributes(), layer.getGeomField(), layer.getFormat(), layer.getSelectedSrs(), layer.getFilter(), layer.getMaxFeatures(), layer.getBasicAuthData());
            if (collections.size() > 0) {
                int geometryType = layer.getGeometryType();
                WFSFeatureCollection enc = null;
                Iterator<WFSFeatureCollection> it = collections.iterator();
                while (it.hasNext() && enc == null) {
                    WFSFeatureCollection col = it.next();
                    if (col.getFeatureSchema().getGeometryType() != geometryType) continue;
                    enc = col;
                }
                if (enc == null) {
                    layer.setFeatureCollection(collections.get(0));
                } else {
                    layer.setFeatureCollection(enc);
                }
            }
            layer.setVista(layer.getVista());
            if (layer.getGeometryType() == 0 && !layer.isRaster()) {
                SelectGeometryTypeDialog selectDialog = new SelectGeometryTypeDialog(JUMPWorkbench.getFrameInstance(), true);
                GUIUtil.centreOnWindow(selectDialog);
                selectDialog.setVisible(true);
                layer.setGeometryType(selectDialog.getGeometryType());
            }
            layer.setGeometryType(layer.getGeometryType());
            layer.setFeatureCollectionModified(false);
            layer.setMetadata(MetadataManager.getInstance().loadMetadata(layer.getUltimateFeatureCollectionWrapper(), layer.getDataSourceQuery()));
            if (layer.isMemory()) {
                layer.setMemory(true);
            }
        }
    }

    protected void loadLayer(Layer layer, Task newTask, Map<String, FeatureCollection[]> queryToFc) throws Exception {
        ICrs proj = null;
        if (layer.getCrsWKT() == null) {
            if (layer.getCrsDescription() != null && !layer.getCrsDescription().equals("")) {
                proj = CrsRepositoryManager.getInstance().getCRS(layer.getCrsDescription());
            }
        } else {
            String params = layer.getCrsParams();
            proj = params == null ? CrsRepositoryManager.getInstance().getCRS(layer.getCrsCode(), layer.getCrsWKT()) : CrsRepositoryManager.getInstance().getCRS(layer.getCrsCode(), layer.getCrsWKT(), params);
            proj.setTransParam(layer.getNadGrid());
            proj.setTransInTarget(layer.isTargetNad());
        }
        layer.setProjection(proj, newTask.getProjection());
        if (layer.isEnabled()) {
            FeatureCollection[] fcs = null;
            if (queryToFc.containsKey(layer.getDataSourceQuery().getQuery())) {
                fcs = queryToFc.get(layer.getDataSourceQuery().getQuery());
            } else {
                fcs = this.executeQuery(layer.getDataSourceQuery().getQuery(), layer.getDataSourceQuery().getDataSource(), layer.getProjection());
                if (fcs.length > 1) {
                    queryToFc.put(layer.getDataSourceQuery().getQuery(), fcs);
                }
            }
            if (layer.getTopologyRelations() != null && !layer.getTopologyRelations().isEmpty()) {
                LOGGER.info((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn.building-topology-relations")) + " - " + layer.getName()));
            }
            if (fcs.length > 1) {
                boolean enc = false;
                int i = 0;
                while (i < fcs.length && !enc) {
                    FeatureCollection fc = fcs[i];
                    if (fc.getFeatureSchema().getGeometryType() == layer.getGeometryType()) {
                        layer.setFeatureCollection(fcs[i]);
                        enc = true;
                    }
                    ++i;
                }
            } else {
                layer.setFeatureCollection(fcs[0]);
            }
            layer.setVista(layer.getVista());
            if (layer.getGeometryType() == 0 && !layer.isRaster()) {
                SelectGeometryTypeDialog selectDialog = new SelectGeometryTypeDialog(JUMPWorkbench.getFrameInstance(), true);
                GUIUtil.centreOnWindow(selectDialog);
                selectDialog.setVisible(true);
                layer.setGeometryType(selectDialog.getGeometryType());
            }
            layer.setGeometryType(layer.getGeometryType());
            layer.setFeatureCollectionModified(false);
            layer.setMetadata(MetadataManager.getInstance().loadMetadata(layer.getUltimateFeatureCollectionWrapper(), layer.getDataSourceQuery()));
            if (layer.isMemory()) {
                layer.setMemory(true);
            }
        }
    }

    protected FeatureCollection[] executeQuery(String query, DataSource dataSource, IProjection proj) throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            FeatureCollection[] featureCollectionArray = connection.executeQuery(query, proj, dataSource.getProperties());
            return featureCollectionArray;
        }
        finally {
            connection.close();
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    private void loadNotFoundFile(Layerable layerable, List<SummaryMessage> messageList, Exception e, LayerManager newLayerManager, Category sourceLayerCategory) throws Exception {
        Layer layer = (Layer)layerable;
        DataSourceQuery dataSourceQuery = layer.getDataSourceQuery();
        String query = dataSourceQuery.getQuery();
        String oldDirectoryPath = new File(query).getParent();
        String fileName = new File(query).getName();
        String path = "";
        if (oldDirectoryPath == null) {
            path = String.valueOf(JUMPWorkbench.USER_DIR) + File.separator + query;
            dataSourceQuery.setQuery(path);
            dataSourceQuery.getDataSource().getProperties().put("File", path);
        } else if (this.oldPathtoNewPath.containsKey(oldDirectoryPath)) {
            path = String.valueOf(this.oldPathtoNewPath.get(oldDirectoryPath)) + File.separatorChar + fileName;
            dataSourceQuery.setQuery(path);
            dataSourceQuery.getDataSource().getProperties().put("File", path);
        } else {
            int opcion = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getMessage("workbench.ui.plugin.AbstractLoadProjectPlugIn.the-file-associated-with-the-layer-{0}-have-not-been-found", new Object[]{layerable.getName()})) + ".\n" + I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.do-you-want-to-select-the-correct-file-localization"), I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.error-loading-layer"));
            if (opcion != 0) {
                LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.exception-loading-the-layer-{0}", new Object[]{layerable.getName()})) + " - " + e.getMessage()));
                messageList.add(this.buildLayerErrorMessage(layerable.getName(), e));
                return;
            }
            String fileExt = FileUtil.getExtension(fileName);
            if (StringUtils.isEmpty((String)fileExt)) {
                this.fileChooser.setFileSelectionMode(1);
                this.fileChooser.setDialogTitle(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn.find-directory")) + " - " + query);
            } else {
                this.fileChooser.setFileFilter(GUIUtil.createFileFilter(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.AbstractLoadProjectPlugIn.{0}-files", new Object[]{fileExt}), new String[]{fileExt}));
                this.fileChooser.setFileSelectionMode(0);
                this.fileChooser.setDialogTitle(String.valueOf(I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.find-file")) + " - " + query);
            }
            this.fileChooser.setDialogType(0);
            this.fileChooser.setMultiSelectionEnabled(false);
            if (this.fileChooser.showOpenDialog(JUMPWorkbench.getFrameInstance()) != 0) {
                LOGGER.warn((Object)(String.valueOf(I18N.getMessage("workbench.ui.plugin.OpenProjectPlugIn.exception-loading-the-layer-{0}", new Object[]{layerable.getName()})) + " - " + e.getMessage()));
                messageList.add(this.buildLayerErrorMessage(layerable.getName(), e));
                return;
            }
            File file = this.fileChooser.getSelectedFile();
            dataSourceQuery.setQuery(file.getAbsolutePath());
            dataSourceQuery.getDataSource().getProperties().put("File", file.getAbsolutePath());
            int opcion2 = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.do-you-want-the-rest-of-the-layers-from-the-old-directory-to-change-their-directory"), I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.error-loading-layer"));
            if (opcion2 == 0) {
                String pathDirectory = file.getParent();
                this.oldPathtoNewPath.put(oldDirectoryPath, pathDirectory);
            }
        }
        layer.setFeatureCollection(this.executeQuery(layer.getDataSourceQuery().getQuery(), layer.getDataSourceQuery().getDataSource(), layer.getProjection())[0]);
        layer.setFeatureCollectionModified(false);
        newLayerManager.addLayerable(sourceLayerCategory.getName(), layerable);
    }

    protected SummaryMessage buildLayerErrorMessage(String layerName, Exception e) {
        String basicMessage = I18N.getMessage("workbench.ui.plugin.AbstractLoadProjectPlugIn.the-layer-{0}-could-not-be-loaded", new Object[]{layerName});
        String extendedMessage = String.valueOf(I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.error-produced")) + ":\n" + e.getMessage();
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 2);
        return message;
    }

    protected SummaryMessage buildTableErrorMessage(String tableName, Exception e) {
        String basicMessage = I18N.getMessage("workbench.ui.plugin.AbstractLoadProjectPlugIn.the-table-{0}-could-not-be-loaded", new Object[]{tableName});
        String extendedMessage = String.valueOf(I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.error-produced")) + ":\n" + e.getMessage();
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 2);
        return message;
    }

    protected SummaryMessage buildLayoutErrorMessage(String layoutName, Exception e) {
        String basicMessage = I18N.getMessage("workbench.ui.plugin.AbstractLoadProjectPlugIn.the-layout-{0}-could-not-be-loaded", new Object[]{layoutName});
        String extendedMessage = String.valueOf(I18N.getString("workbench.ui.plugin.AbstractLoadProjectPlugIn.error-produced")) + ":\n" + e.getMessage();
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 2);
        return message;
    }
}

