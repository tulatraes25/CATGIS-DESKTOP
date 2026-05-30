/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.SelectDirectoryPanel;

public class ConfigPathPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    public static final Icon ICON = IconLoader.icon("folder.png");
    public static final String NAME = I18N.getString("org.saig.jump.widgets.config.ConfigPathPanel.default-paths");
    private Blackboard blackboard;
    private SelectDirectoryPanel projectsPathPanel;
    private SelectDirectoryPanel dataLoadPathPanel;
    private SelectDirectoryPanel dataSavePathPanel;
    private SelectDirectoryPanel tempFilesPathPanel;
    private SelectDirectoryPanel loadSimbologyPathPanel;
    private SelectDirectoryPanel saveSimbologyPathPanel;
    public static final String PROJECTS_PATH_KEY = String.valueOf(ConfigPathPanel.class.getName()) + " - PROJECTS_PATH_KEY";
    public static final String DATA_LOAD_PATH_KEY = String.valueOf(ConfigPathPanel.class.getName()) + " - DATA_LOAD_PATH_KEY";
    public static final String DATA_SAVE_PATH_KEY = String.valueOf(ConfigPathPanel.class.getName()) + " - DATA_SAVE_PATH_KEY";
    public static final String TEMP_FILES_PATH_KEY = String.valueOf(ConfigPathPanel.class.getName()) + " - TEMP_FILES_PATH_KEY";
    public static final String LOAD_SIMBOLOGY_PATH_KEY = String.valueOf(ConfigPathPanel.class.getName()) + " - LOAD_SIMBOLOGY_PATH_KEY";
    public static final String SAVE_SIMBOLOGY_PATH_KEY = String.valueOf(ConfigPathPanel.class.getName()) + " - SAVE_SIMBOLOGY_PATH_KEY";

    public ConfigPathPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getProjectsPathPanel());
        FormUtils.addRowInGBL(this, 2, 0, this.getDataLoadPathPanel());
        FormUtils.addRowInGBL(this, 3, 0, this.getDataSavePathPanel());
        FormUtils.addRowInGBL(this, 4, 0, this.getTempFilesPathPanel());
        FormUtils.addRowInGBL(this, 5, 0, this.getLoadSimbologyPathPanel());
        FormUtils.addRowInGBL(this, 6, 0, this.getSaveSimbologyPathPanel());
        FormUtils.addFiller(this, 10, 0);
    }

    public JPanel getProjectsPathPanel() {
        if (this.projectsPathPanel == null) {
            this.projectsPathPanel = new SelectDirectoryPanel();
            this.projectsPathPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigPathPanel.projects")));
        }
        return this.projectsPathPanel;
    }

    private JPanel getDataLoadPathPanel() {
        if (this.dataLoadPathPanel == null) {
            this.dataLoadPathPanel = new SelectDirectoryPanel();
            this.dataLoadPathPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigPathPanel.load-data")));
        }
        return this.dataLoadPathPanel;
    }

    private JPanel getDataSavePathPanel() {
        if (this.dataSavePathPanel == null) {
            this.dataSavePathPanel = new SelectDirectoryPanel();
            this.dataSavePathPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigPathPanel.save-data")));
        }
        return this.dataSavePathPanel;
    }

    private JPanel getTempFilesPathPanel() {
        if (this.tempFilesPathPanel == null) {
            this.tempFilesPathPanel = new SelectDirectoryPanel();
            this.tempFilesPathPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigPathPanel.temporal-files")));
        }
        return this.tempFilesPathPanel;
    }

    private JPanel getLoadSimbologyPathPanel() {
        if (this.loadSimbologyPathPanel == null) {
            this.loadSimbologyPathPanel = new SelectDirectoryPanel();
            this.loadSimbologyPathPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "load-symbology")));
        }
        return this.loadSimbologyPathPanel;
    }

    private JPanel getSaveSimbologyPathPanel() {
        if (this.saveSimbologyPathPanel == null) {
            this.saveSimbologyPathPanel = new SelectDirectoryPanel();
            this.saveSimbologyPathPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "save-symgology")));
        }
        return this.saveSimbologyPathPanel;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() {
        String relativePath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(PROJECTS_PATH_KEY);
        if (StringUtils.isNotEmpty((String)relativePath)) {
            this.projectsPathPanel.setSelectedPath(relativePath);
        } else {
            this.projectsPathPanel.setSelectedPath("");
        }
        relativePath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(DATA_LOAD_PATH_KEY);
        if (StringUtils.isNotEmpty((String)relativePath)) {
            this.dataLoadPathPanel.setSelectedPath(relativePath);
        } else {
            this.dataLoadPathPanel.setSelectedPath("");
        }
        relativePath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(DATA_SAVE_PATH_KEY);
        if (StringUtils.isNotEmpty((String)relativePath)) {
            this.dataSavePathPanel.setSelectedPath(relativePath);
        } else {
            this.dataSavePathPanel.setSelectedPath("");
        }
        relativePath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(TEMP_FILES_PATH_KEY);
        if (StringUtils.isNotEmpty((String)relativePath)) {
            this.tempFilesPathPanel.setSelectedPath(relativePath);
        } else {
            this.tempFilesPathPanel.setSelectedPath("");
        }
        relativePath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(LOAD_SIMBOLOGY_PATH_KEY);
        if (StringUtils.isNotEmpty((String)relativePath)) {
            this.loadSimbologyPathPanel.setSelectedPath(relativePath);
        } else {
            this.loadSimbologyPathPanel.setSelectedPath("");
        }
        relativePath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(SAVE_SIMBOLOGY_PATH_KEY);
        if (StringUtils.isNotEmpty((String)relativePath)) {
            this.saveSimbologyPathPanel.setSelectedPath(relativePath);
        } else {
            this.saveSimbologyPathPanel.setSelectedPath("");
        }
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(PROJECTS_PATH_KEY, this.projectsPathPanel.getSelectedPath());
        PersistentBlackboardPlugIn.get(this.blackboard).put(DATA_LOAD_PATH_KEY, this.dataLoadPathPanel.getSelectedPath());
        PersistentBlackboardPlugIn.get(this.blackboard).put(DATA_SAVE_PATH_KEY, this.dataSavePathPanel.getSelectedPath());
        PersistentBlackboardPlugIn.get(this.blackboard).put(TEMP_FILES_PATH_KEY, this.tempFilesPathPanel.getSelectedPath());
        PersistentBlackboardPlugIn.get(this.blackboard).put(LOAD_SIMBOLOGY_PATH_KEY, this.loadSimbologyPathPanel.getSelectedPath());
        PersistentBlackboardPlugIn.get(this.blackboard).put(SAVE_SIMBOLOGY_PATH_KEY, this.saveSimbologyPathPanel.getSelectedPath());
    }

    @Override
    public String validateInput() {
        File file;
        String path = this.projectsPathPanel.getSelectedPath();
        if (!StringUtils.isEmpty((String)path)) {
            file = new File(path);
            if (!file.exists()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-does-not-exist", new Object[]{path});
            }
            if (!file.canRead()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-can-not-be-read", new Object[]{path});
            }
        }
        if (!StringUtils.isEmpty((String)(path = this.dataLoadPathPanel.getSelectedPath()))) {
            file = new File(path);
            if (!file.exists()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-does-not-exist", new Object[]{path});
            }
            if (!file.canRead()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-can-not-be-read", new Object[]{path});
            }
        }
        if (!StringUtils.isEmpty((String)(path = this.dataSavePathPanel.getSelectedPath()))) {
            file = new File(path);
            if (!file.exists()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-does-not-exist", new Object[]{path});
            }
            if (!file.canRead()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-can-not-be-read", new Object[]{path});
            }
        }
        if (!StringUtils.isEmpty((String)(path = this.tempFilesPathPanel.getSelectedPath()))) {
            file = new File(path);
            if (!file.exists()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-does-not-exist", new Object[]{path});
            }
            if (!file.canRead()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-can-not-be-read", new Object[]{path});
            }
        }
        if (!StringUtils.isEmpty((String)(path = this.loadSimbologyPathPanel.getSelectedPath()))) {
            file = new File(path);
            if (!file.exists()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-does-not-exist", new Object[]{path});
            }
            if (!file.canRead()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-can-not-be-read", new Object[]{path});
            }
        }
        if (!StringUtils.isEmpty((String)(path = this.saveSimbologyPathPanel.getSelectedPath()))) {
            file = new File(path);
            if (!file.exists()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-does-not-exist", new Object[]{path});
            }
            if (!file.canRead()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-can-not-be-read", new Object[]{path});
            }
        }
        return null;
    }
}

