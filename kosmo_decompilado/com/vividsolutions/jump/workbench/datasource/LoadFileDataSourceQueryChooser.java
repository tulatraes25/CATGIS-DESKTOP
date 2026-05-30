/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.FileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.io.File;
import java.util.Collection;
import javax.swing.JFileChooser;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.widgets.config.ConfigPathPanel;

public class LoadFileDataSourceQueryChooser
extends FileDataSourceQueryChooser {
    public static final String FILE_CHOOSER_DIRECTORY_KEY = String.valueOf(LoadFileDataSourceQueryChooser.class.getName()) + " - FILE_CHOOSER_DIRECTORY_KEY";
    private static String lastPath = null;
    public final String LOAD_FILE_CHOOSER_PANEL_KEY = String.valueOf(LoadFileDataSourceQueryChooser.class.getName()) + " - LOAD FILE CHOOSER PANEL";
    protected WorkbenchContext context;

    public LoadFileDataSourceQueryChooser(Class<?> dataSourceClass, String description, String[] extensions, WorkbenchContext context) {
        super(dataSourceClass, description, extensions);
        this.context = context;
        if (lastPath == null) {
            lastPath = (String)PersistentBlackboardPlugIn.get(context).get(FILE_CHOOSER_DIRECTORY_KEY);
        }
    }

    protected Blackboard blackboard() {
        return this.context.getBlackboard();
    }

    @Override
    protected FileDataSourceQueryChooser.FileChooserPanel getFileChooserPanel() {
        if (this.blackboard().get(this.LOAD_FILE_CHOOSER_PANEL_KEY) == null) {
            File lastPathFile;
            JFileChooser fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setControlButtonsAreShown(false);
            fileChooser.setFileSelectionMode(2);
            String defaultPath = (String)PersistentBlackboardPlugIn.get(this.blackboard()).get(ConfigPathPanel.DATA_LOAD_PATH_KEY);
            if (defaultPath != null && !defaultPath.equals("")) {
                File defaultPathFile = new File(defaultPath);
                if (defaultPathFile.canRead()) {
                    fileChooser.setCurrentDirectory(defaultPathFile);
                }
            } else if (lastPath != null && (lastPathFile = new File(lastPath)).exists() && lastPathFile.canRead()) {
                fileChooser.setCurrentDirectory(lastPathFile);
            }
            this.blackboard().put(this.LOAD_FILE_CHOOSER_PANEL_KEY, new FileDataSourceQueryChooser.FileChooserPanel(fileChooser, this.blackboard()));
            if (PersistentBlackboardPlugIn.get(this.context).get(FILE_CHOOSER_DIRECTORY_KEY) != null) {
                fileChooser.setCurrentDirectory(new File((String)PersistentBlackboardPlugIn.get(this.context).get(FILE_CHOOSER_DIRECTORY_KEY)));
            }
        }
        return (FileDataSourceQueryChooser.FileChooserPanel)this.blackboard().get(this.LOAD_FILE_CHOOSER_PANEL_KEY);
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        PersistentBlackboardPlugIn.get(this.context).put(FILE_CHOOSER_DIRECTORY_KEY, this.getFileChooserPanel().getChooser().getCurrentDirectory().toString());
        lastPath = this.getFileChooserPanel().getChooser().getCurrentDirectory().getAbsolutePath();
        return super.getDataSourceQueries();
    }

    public boolean checkSelection() {
        return super.isInputValid();
    }

    @Override
    public boolean isInputValid() {
        boolean solucion = this.checkSelection();
        FileDataSourceQueryChooser.FileChooserPanel chooserPanel = (FileDataSourceQueryChooser.FileChooserPanel)this.context.getBlackboard().get(this.LOAD_FILE_CHOOSER_PANEL_KEY);
        File[] files = chooserPanel.getChooser().getSelectedFiles();
        int i = 0;
        while (i < files.length && solucion) {
            solucion = !files[i].isDirectory();
            ++i;
        }
        return solucion;
    }

    @Override
    public void refreshPath() {
        File lastPathFile;
        String defaultPath = (String)PersistentBlackboardPlugIn.get(this.blackboard()).get(ConfigPathPanel.DATA_LOAD_PATH_KEY);
        if (StringUtils.isNotEmpty((String)defaultPath)) {
            File defaultPathFile = new File(defaultPath);
            if (defaultPathFile.exists() && defaultPathFile.canRead()) {
                this.getFileChooserPanel().getChooser().setCurrentDirectory(defaultPathFile);
            }
        } else if (StringUtils.isNotEmpty((String)lastPath) && (lastPathFile = new File(lastPath)).exists() && lastPathFile.canRead()) {
            this.getFileChooserPanel().getChooser().setCurrentDirectory(lastPathFile);
        }
        this.getFileChooserPanel().getChooser().rescanCurrentDirectory();
    }
}

