/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.FileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.io.File;
import java.util.Collection;
import javax.swing.JFileChooser;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.widgets.config.ConfigPathPanel;

public class SaveFileDataSourceQueryChooser
extends FileDataSourceQueryChooser {
    public static final String FILE_CHOOSER_DIRECTORY_KEY = String.valueOf(SaveFileDataSourceQueryChooser.class.getName()) + " - FILE_CHOOSER_DIRECTORY_KEY";
    public String SAVE_FILE_CHOOSER_PANEL_KEY = String.valueOf(SaveFileDataSourceQueryChooser.class.getName()) + " - SAVE FILE CHOOSER PANEL";
    protected static String lastPath = null;
    protected WorkbenchContext context;

    public SaveFileDataSourceQueryChooser(Class<?> dataSourceClass, String description, String[] extensions, WorkbenchContext context) {
        super(dataSourceClass, description, extensions);
        this.context = context;
        if (lastPath == null) {
            lastPath = (String)PersistentBlackboardPlugIn.get(context).get(FILE_CHOOSER_DIRECTORY_KEY);
        }
    }

    private Blackboard blackboard() {
        return this.context.getBlackboard();
    }

    @Override
    protected FileDataSourceQueryChooser.FileChooserPanel getFileChooserPanel() {
        if (this.blackboard().get(this.SAVE_FILE_CHOOSER_PANEL_KEY) == null) {
            File lastPathFile;
            JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setControlButtonsAreShown(false);
            String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.DATA_SAVE_PATH_KEY);
            if (defaultPath != null && !defaultPath.equals("")) {
                File defaultPathFile = new File(defaultPath);
                if (defaultPathFile.canRead()) {
                    fileChooser.setCurrentDirectory(defaultPathFile);
                }
            } else if (lastPath != null && (lastPathFile = new File(lastPath)).exists() && lastPathFile.canRead()) {
                fileChooser.setCurrentDirectory(lastPathFile);
            }
            this.blackboard().put(this.SAVE_FILE_CHOOSER_PANEL_KEY, new FileDataSourceQueryChooser.FileChooserPanel(fileChooser, this.blackboard()));
            if (PersistentBlackboardPlugIn.get(this.context).get(FILE_CHOOSER_DIRECTORY_KEY) != null) {
                fileChooser.setCurrentDirectory(new File((String)PersistentBlackboardPlugIn.get(this.context).get(FILE_CHOOSER_DIRECTORY_KEY)));
            }
        }
        return (FileDataSourceQueryChooser.FileChooserPanel)this.blackboard().get(this.SAVE_FILE_CHOOSER_PANEL_KEY);
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        PersistentBlackboardPlugIn.get(this.context).put(FILE_CHOOSER_DIRECTORY_KEY, this.getFileChooserPanel().getChooser().getCurrentDirectory().toString());
        lastPath = this.getFileChooserPanel().getChooser().getCurrentDirectory().getAbsolutePath();
        return super.getDataSourceQueries();
    }

    @Override
    public void refreshPath() {
        File lastPathFile;
        String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.DATA_SAVE_PATH_KEY);
        if (!StringUtils.isEmpty((String)defaultPath)) {
            File defaultPathFile = new File(defaultPath);
            if (defaultPathFile.exists() && defaultPathFile.canRead()) {
                this.getFileChooserPanel().getChooser().setCurrentDirectory(defaultPathFile);
            }
        } else if (!StringUtils.isEmpty((String)lastPath) && (lastPathFile = new File(lastPath)).exists() && lastPathFile.canRead()) {
            this.getFileChooserPanel().getChooser().setCurrentDirectory(lastPathFile);
        }
    }
}

