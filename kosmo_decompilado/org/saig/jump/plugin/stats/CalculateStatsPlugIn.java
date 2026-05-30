/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.stats;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfFileWriter;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.TableFactory;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSource;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.stats.StatsOperatorsFactory;
import org.saig.jump.widgets.stats.CalculateStatsDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class CalculateStatsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(CalculateStatsPlugIn.class);
    public static final int MODE_ATTRIBUTE_PANEL_POPUP_MENU = 1;
    public static final int MODE_ATTRIBUTE_PANEL_TOOL_BAR = 2;
    public static final int MODE_STANDARD_PLUGIN = 3;
    private static final int MONITOR_COUNTER_STEP = 100;
    public static final String NAME = I18N.getString(CalculateStatsPlugIn.class, "calculate-statistics");
    public static final Icon ICON = IconLoader.icon("sum.png");
    private final int mode;
    private Layer layer;
    private Map<String, Set<String>> operatorsByFieldMap;
    private List<String> groupByFields;
    private String path;
    protected boolean useOnlySelected = false;

    public CalculateStatsPlugIn() {
        this(3);
    }

    public CalculateStatsPlugIn(int modeLocation) {
        this.mode = modeLocation;
    }

    @Override
    public final String getName() {
        return NAME;
    }

    @Override
    public final Icon getIcon() {
        return ICON;
    }

    @Override
    public final EnableCheck getCheck() {
        return this.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public final EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck solucion = new MultiEnableCheck();
        if (this.mode == 1) {
            solucion.add(AttributeTab.createNotGeometryRightClickEnableCheck(workbenchContext));
            solucion.add(AttributeTab.createNotEditableRightClickEnableCheck(workbenchContext));
        } else if (this.mode == 3) {
            solucion.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createSelectedLayersMustNotBeEditableCheck());
        }
        return solucion;
    }

    @Override
    public final boolean execute(PlugInContext context) throws Exception {
        String columnName = null;
        LayerNamePanelProxy panelProxy = null;
        AttributeTab attributeTab = null;
        if (this.mode == 1 || this.mode == 2) {
            panelProxy = (LayerNamePanelProxy)((Object)context.getActiveInternalFrame());
            attributeTab = (AttributeTab)panelProxy.getLayerNamePanel();
        }
        if (this.mode == 1) {
            this.layer = attributeTab.getLastRightClickLayer();
        } else if (this.mode == 2 && attributeTab.getModel().getLayers().size() == 1) {
            this.layer = attributeTab.getModel().getLayers().get(0);
        } else if (this.mode == 3) {
            this.layer = (Layer)JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel().getSelectedLayers()[0];
        }
        if (this.layer == null) {
            return false;
        }
        if (this.mode == 1) {
            columnName = attributeTab.getLastRightClickColumnName();
        }
        if (columnName != null && columnName.equals("....")) {
            return false;
        }
        int numSelectedFeatures = context.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(this.layer);
        CalculateStatsDialog dialog = new CalculateStatsDialog(JUMPWorkbench.getFrameInstance(), true, this.layer, columnName, numSelectedFeatures);
        dialog.setVisible(true);
        if (dialog.isOkExit()) {
            this.operatorsByFieldMap = dialog.getOperatorsByFieldMap();
            this.groupByFields = dialog.getGroupByFields();
            this.path = dialog.getFilePath();
            this.useOnlySelected = dialog.useOnlySelected();
            dialog.dispose();
        }
        dialog.dispose();
        return dialog.isOkExit();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        ArrayList<CalculateStatsDialog.StatPair> resultStatPairs = new ArrayList<CalculateStatsDialog.StatPair>();
        monitor.report(I18N.getString(this.getClass(), "calculating-statistics"));
        Object[] selectedKeys = null;
        if (this.useOnlySelected) {
            Collection<Feature> selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(this.layer);
            ArrayList<Object> selectedKeysList = new ArrayList<Object>(selectedFeatures.size());
            for (Feature currentFeature : selectedFeatures) {
                selectedKeysList.add(currentFeature.getPrimaryKey());
            }
            selectedKeys = selectedKeysList.toArray();
        }
        List<Object[]> results = this.layer.getUltimateFeatureCollectionWrapper().queryStats(this.operatorsByFieldMap, this.groupByFields, selectedKeys, resultStatPairs);
        if (!monitor.isCancelRequested()) {
            if (!CollectionUtils.isEmpty(results)) {
                int option;
                block19: {
                    monitor.report(I18N.getMessage(this.getClass(), "creating-dbf-file-in-the-path-{0}", new Object[]{this.path}));
                    DbfFileWriter dbfFileWriter = null;
                    try {
                        try {
                            dbfFileWriter = new DbfFileWriter(this.path);
                            dbfFileWriter.writeHeader(StatsOperatorsFactory.getInstance().getDbfFieldsDefs(this.layer.getFeatureSchema(), resultStatPairs), results.size());
                            List<AttributeType> attributeTypeList = StatsOperatorsFactory.getInstance().getAttributeTypeFieldsList(this.layer.getFeatureSchema(), resultStatPairs);
                            int cont = 0;
                            int total = results.size();
                            Iterator<Object[]> iterator = results.iterator();
                            while (iterator.hasNext()) {
                                ++cont;
                                Object[] objects = iterator.next();
                                Vector<Object> vector = new Vector<Object>(objects.length);
                                int j = 0;
                                while (j < objects.length) {
                                    vector.add(j, FeatureUtil.getGoodAttribute(attributeTypeList.get(j), objects[j]));
                                    ++j;
                                }
                                dbfFileWriter.writeRecord(vector);
                                if (cont % 100 != 0) continue;
                                monitor.report(cont, total, I18N.getString(this.getClass(), "records-written"));
                            }
                            dbfFileWriter.writeRealHeader(results.size());
                            monitor.report(cont, total, I18N.getString(this.getClass(), "records-written"));
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                            if (dbfFileWriter != null) {
                                dbfFileWriter.close();
                            }
                            break block19;
                        }
                    }
                    catch (Throwable throwable) {
                        if (dbfFileWriter != null) {
                            dbfFileWriter.close();
                        }
                        throw throwable;
                    }
                    if (dbfFileWriter != null) {
                        dbfFileWriter.close();
                    }
                }
                if (!monitor.isCancelRequested() && (option = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage(this.getClass(), "file-created-{0}-do-you-want-to-open-it-as-a-table", new Object[]{this.path}), I18N.getString(this.getClass(), "warning"))) == 0) {
                    try {
                        DBFRecordDataSource dataSource = new DBFRecordDataSource(this.path, null);
                        String name = FileUtil.nameWithoutExtension(this.path);
                        ((TableRecordDataSource)dataSource).setName(name);
                        Table recordCollection = TableFactory.getRecordCollection(dataSource);
                        ViewTableFrame tableFrame = new ViewTableFrame(recordCollection, context);
                        JUMPWorkbench.getFrameInstance().getContext().getDataManager().addTable(tableFrame);
                    }
                    catch (Exception e1) {
                        LOGGER.error((Object)e1);
                        DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.core.model.data.widgets.LoadTableDialog.an-unexpected-error-has-been-produced-while-reading-the-file-{0}", new Object[]{this.path}), I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.error"));
                    }
                }
            } else {
                context.getWorkbenchFrame().warnUser(I18N.getString(this.getClass(), "no-results-were-obtained"));
            }
        }
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getString(this.getClass(), "operation-cancelled-by-the-user"));
        }
    }
}

