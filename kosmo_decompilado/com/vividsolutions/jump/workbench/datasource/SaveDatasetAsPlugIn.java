/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserDialog;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import javax.swing.Icon;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.IndexedShapeFileDataSource;
import org.saig.jump.plugin.datasource.JumpJDBCDataSource;
import org.saig.jump.widgets.util.DialogFactory;

public class SaveDatasetAsPlugIn
extends ThreadedBasePlugIn {
    public static final String NAME = String.valueOf(I18N.getString("workbench.datasource.SaveDatasetAsPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("SaveTheme.gif");
    public static final String LAST_FORMAT_KEY = String.valueOf(SaveDatasetAsPlugIn.class.getName()) + " - LAST FORMAT";
    public static final Logger LOGGER = Logger.getLogger(SaveDatasetAsPlugIn.class);
    private boolean updateDataSourceReference = false;
    private FeatureCollectionOnDemand fcUpdate;
    private DataSourceQueryChooserDialog dialog;

    public SaveDatasetAsPlugIn() {
    }

    public SaveDatasetAsPlugIn(boolean updateReference) {
        this.updateDataSourceReference = updateReference;
    }

    @Override
    public void initialize(final PlugInContext context) throws Exception {
        context.getWorkbenchFrame().addWindowListener(new WindowAdapter(){

            @Override
            public void windowOpened(WindowEvent e) {
                String format = (String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(LAST_FORMAT_KEY);
                if (format != null && SaveDatasetAsPlugIn.this.dialog != null) {
                    SaveDatasetAsPlugIn.this.dialog.setSelectedFormat(format);
                }
            }
        });
    }

    protected DataSourceQueryChooserDialog getDialog(PlugInContext context) {
        return new DataSourceQueryChooserDialog(DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard()).getSaveDataSourceQueryChoosers(), context.getWorkbenchFrame(), this.getName(), true);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.dialog = this.getDialog(context);
        String format = (String)PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).get(LAST_FORMAT_KEY);
        if (StringUtils.isNotEmpty((String)format)) {
            this.dialog.setSelectedFormat(format);
        }
        this.dialog.refreshPath();
        GUIUtil.centreOnWindow(this.dialog);
        Layer selectedLayer = null;
        selectedLayer = this.updateDataSourceReference ? context.getLayerManager().getEditableLayers().iterator().next() : (Layer)context.getSelectedLayer(0);
        if (selectedLayer.getUltimateFeatureCollectionWrapper().size() == 0) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn.The-layer-{0}-can-not-be-saved-because-it-has-no-elements", new Object[]{selectedLayer.getName()}), I18N.getString("com.vividsolutions.jump.workbench.datasource.SaveDatasetAsPlugIn.Error-while-saving-the-layer"));
            return false;
        }
        this.dialog.setVisible(true);
        boolean ok = this.dialog.wasOKPressed();
        if (ok) {
            PersistentBlackboardPlugIn.get(context.getWorkbenchContext()).put(LAST_FORMAT_KEY, this.dialog.getSelectedFormat());
        }
        return ok;
    }

    /*
     * Exception decompiling
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [6[CATCHBLOCK]], but top level block is 5[CATCHBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private DataSourceQuery updateDataSourceQuery(DataSourceQuery dataSourceQuery, AbstractDataSource dataAccesor) {
        DataSourceQuery newQuery = new DataSourceQuery();
        newQuery.setName(dataSourceQuery.getName());
        newQuery.setLayerName(dataSourceQuery.getLayerName());
        if (dataAccesor instanceof ShapeFileDataSource) {
            ShapeFileDataSource psDataSource = (ShapeFileDataSource)dataAccesor;
            String filePath = FileUtil.convertPathToSystemIndependentPath(psDataSource.getFile().getAbsolutePath());
            newQuery.setQuery(filePath);
            IndexedShapeFileDataSource sifds = new IndexedShapeFileDataSource();
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put("File", filePath);
            newQuery.setName(FileUtil.nameWithoutExtension(psDataSource.getFile().getName()));
            sifds.setProperties(properties);
            newQuery.setDataSource(sifds);
        } else if (dataAccesor instanceof AbstractJDBCDataSource) {
            AbstractJDBCDataSource jdbcDataSource = (AbstractJDBCDataSource)dataAccesor;
            JumpJDBCDataSource jumpDataSource = new JumpJDBCDataSource(jdbcDataSource);
            newQuery.setDataSource(jumpDataSource);
            newQuery.setName(jdbcDataSource.getTableName());
        } else {
            newQuery = dataSourceQuery;
        }
        return newQuery;
    }

    public FeatureCollection getUpdateDataSourceFeatureCollection() {
        return this.fcUpdate;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck());
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
        return SaveDatasetAsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

