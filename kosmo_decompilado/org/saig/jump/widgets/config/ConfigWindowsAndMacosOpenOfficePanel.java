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
import org.saig.core.model.data.dao.export.OpenOfficeLibLoader;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.SelectDirectoryPanel;

public class ConfigWindowsAndMacosOpenOfficePanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    public static final Icon ICON = IconLoader.icon("folderOO.png");
    public static final String NAME = "Open Office";
    private Blackboard blackboard;
    private SelectDirectoryPanel openOfficePathPanel;
    public static final String OPENOFFICE_PATH_KEY = String.valueOf(ConfigWindowsAndMacosOpenOfficePanel.class.getName()) + " - OPENOFFICE_PATH_KEY";

    public ConfigWindowsAndMacosOpenOfficePanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getOpenOfficePathPanel());
        FormUtils.addFiller(this, 2, 0);
    }

    public JPanel getOpenOfficePathPanel() {
        if (this.openOfficePathPanel == null) {
            this.openOfficePathPanel = new SelectDirectoryPanel();
            this.openOfficePathPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigOpenOfficePanel.openoffice-instalation-directory")));
        }
        return this.openOfficePathPanel;
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
        String relativePath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(OPENOFFICE_PATH_KEY);
        if (relativePath != null && !relativePath.equals("")) {
            this.openOfficePathPanel.setSelectedPath(relativePath);
        } else {
            this.openOfficePathPanel.setSelectedPath("");
        }
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(OPENOFFICE_PATH_KEY, this.openOfficePathPanel.getSelectedPath());
    }

    @Override
    public String validateInput() {
        String path = this.openOfficePathPanel.getSelectedPath().trim();
        if (!StringUtils.isEmpty((String)path)) {
            String[] pathlist;
            File file = new File(path);
            if (!file.exists()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-does-not-exist", new Object[]{path});
            }
            if (!file.canRead()) {
                return I18N.getMessage("org.saig.jump.widgets.config.ConfigPathPanel.the-directory-{0}-can-not-be-read", new Object[]{path});
            }
            boolean openofficeExist3 = true;
            String[] stringArray = pathlist = OpenOfficeLibLoader.WIN_PATH_3;
            int n = pathlist.length;
            int n2 = 0;
            while (n2 < n) {
                String p = stringArray[n2];
                File f = new File(String.valueOf(path) + p);
                openofficeExist3 &= f.exists();
                ++n2;
            }
            boolean openofficeExist2 = true;
            String[] stringArray2 = pathlist = OpenOfficeLibLoader.WIN_PATH_2;
            int n3 = pathlist.length;
            n = 0;
            while (n < n3) {
                String p = stringArray2[n];
                File f = new File(String.valueOf(path) + p);
                openofficeExist2 &= f.exists();
                ++n;
            }
            if (!openofficeExist3 && !openofficeExist2) {
                return I18N.getString("org.saig.jump.widgets.config.ConfigWindowsAndMacosOpenOfficePanel.The-selected-folder-does-not-correspond-to-an-OpenOffice-2-or-3-installation");
            }
        }
        return null;
    }
}

