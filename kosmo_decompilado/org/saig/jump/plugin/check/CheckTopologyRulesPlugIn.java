/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.xml.MarshalException
 *  org.exolab.castor.xml.Unmarshaller
 *  org.exolab.castor.xml.ValidationException
 */
package org.saig.jump.plugin.check;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.saig.core.check.CheckGroup;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.IndexedShapeFileDataSource;
import org.saig.jump.util.LoadXMLMappings;
import org.saig.jump.widgets.summary.SummaryDialog;
import org.saig.jump.widgets.summary.SummaryMessage;
import org.saig.jump.widgets.util.DialogFactory;

public class CheckTopologyRulesPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString(CheckTopologyRulesPlugIn.class, "check-topological-rules-from-file");
    public static final Icon ICON = IconLoader.icon("balanza.png");
    public static final Logger LOGGER = Logger.getLogger(CheckTopologyRulesPlugIn.class);
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Icon getDisabledIcon() {
        return null;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.fileChooser.setDialogTitle(NAME);
        this.fileChooser.setDialogType(0);
        this.fileChooser.setFileSelectionMode(0);
        this.fileChooser.setMultiSelectionEnabled(false);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        return this.fileChooser.showOpenDialog(context.getWorkbenchFrame()) == 0;
    }

    @Override
    public void finish(PlugInContext context) {
    }

    @Override
    public EnableCheck getCheck() {
        return CheckTopologyRulesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1));
    }

    /*
     * Loose catch block
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        File selectedFile = this.fileChooser.getSelectedFile();
        monitor.report(I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Processing-checks-for-file-{0}", new Object[]{selectedFile.getName()}));
        FileReader reader = null;
        CheckGroup restoredCheckGroup = null;
        try {
            try {
                reader = new FileReader(selectedFile);
                Mapping mapping = LoadXMLMappings.loadTopologyCheckingMappings();
                Unmarshaller unmar = new Unmarshaller(mapping);
                unmar.setWhitespacePreserve(true);
                restoredCheckGroup = (CheckGroup)unmar.unmarshal((Reader)reader);
            }
            catch (MarshalException me) {
                LOGGER.error((Object)I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Error-while-retrieving-file-{0}", new Object[]{selectedFile.getName()}), (Throwable)me);
                DialogFactory.showErrorDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.The-structure-of-checks-file-{0}-is-not-correct", new Object[]{selectedFile.getName()}), I18N.getString("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.File-read-error"));
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException ioe) {
                        LOGGER.error((Object)"", (Throwable)ioe);
                    }
                }
                return;
            }
            catch (ValidationException ve) {
                block24: {
                    LOGGER.error((Object)I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Error-while-retrieving-file-{0}", new Object[]{selectedFile.getName()}), (Throwable)ve);
                    DialogFactory.showErrorDialog(context.getWorkbenchFrame(), I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.The-structure-of-checks-file-{0}-is-not-correct", new Object[]{selectedFile.getName()}), I18N.getString("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.File-read-error"));
                    if (reader == null) break block24;
                    try {
                        reader.close();
                    }
                    catch (IOException ioe) {
                        LOGGER.error((Object)"", (Throwable)ioe);
                    }
                }
                return;
            }
            catch (Exception ex) {
                block25: {
                    LOGGER.error((Object)I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Error-while-retrieving-file-{0}", new Object[]{selectedFile.getName()}), (Throwable)ex);
                    DialogFactory.showErrorDialog(context.getWorkbenchFrame(), String.valueOf(I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.An-error-has-been-produced-while-retrieving-the-checks-file-{0}", new Object[]{selectedFile.getName()})) + ": " + ex.getMessage(), I18N.getString("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.File-read-error"));
                    if (reader == null) break block25;
                    {
                        catch (Throwable throwable) {
                            throw throwable;
                        }
                    }
                    try {
                        reader.close();
                    }
                    catch (IOException ioe) {
                        LOGGER.error((Object)"", (Throwable)ioe);
                    }
                }
                return;
            }
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ioe) {
                    LOGGER.error((Object)"", (Throwable)ioe);
                }
            }
        }
        LOGGER.info((Object)I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Retrieved-{0}-checks-from-file-{1}", new Object[]{Integer.toString(restoredCheckGroup.size()), selectedFile.getName()}));
        monitor.report(I18N.getString("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Processing-checks"));
        LinkedHashMap<String, List<SummaryMessage>> messageMap = new LinkedHashMap<String, List<SummaryMessage>>();
        Set<String> filesToLoad = restoredCheckGroup.checkAllGroup(messageMap, monitor);
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Operation-{0}-cancelled-by-user", new Object[]{this.getName()}));
            return;
        }
        if (messageMap.size() > 0) {
            this.showCheckSummary(messageMap, selectedFile.getName());
        }
        monitor.report(I18N.getString("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Loading-incidences-layers"));
        this.loadIncidentCategory(context, filesToLoad);
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Checks-for-file-{0}-finished", new Object[]{selectedFile.getName()}));
    }

    private void loadIncidentCategory(PlugInContext context, Set<String> filesToLoad) {
        if (CollectionUtils.isEmpty(filesToLoad)) {
            LOGGER.info((Object)I18N.getString("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.There-is-no-file-to-load"));
            return;
        }
        String categoryName = this.getNameForIncidentCategory();
        for (String currentFilePath : filesToLoad) {
            IndexedShapeFileDataSource dataSource = new IndexedShapeFileDataSource();
            FeatureCollection[] featureCollections = null;
            try {
                featureCollections = ((DataSource)dataSource).getConnection().executeQuery(currentFilePath);
                Layer layer = context.getLayerManager().addLayer(categoryName, featureCollections[0].getName(), featureCollections[0]);
                layer.setProjection(null);
                layer.setDataSourceQuery(new DataSourceQuery(dataSource, currentFilePath, layer.getName()));
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
    }

    private String getNameForIncidentCategory() {
        return I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Incidences-{0}", new Object[]{DateFormatManager.getDateTimeFormat().format(new Date())});
    }

    private void showCheckSummary(Map<String, List<SummaryMessage>> messageMap, String checksFile) {
        SummaryDialog dialog = new SummaryDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.jump.plugin.check.CheckTopologyRulesPlugIn.Checks-summary-for-file-{0}", new Object[]{checksFile}), messageMap);
        dialog.setVisible(true);
    }
}

