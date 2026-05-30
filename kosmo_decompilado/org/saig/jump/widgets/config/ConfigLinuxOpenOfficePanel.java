/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.saig.core.model.data.dao.export.OpenOfficeLibLoader;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.IconRenderer;
import org.saig.jump.widgets.config.OOPathTableModel;
import org.saig.jump.widgets.util.SelectDirectoryPanel;

public class ConfigLinuxOpenOfficePanel
extends OptionsPanel
implements ActionListener {
    public static Logger LOGGER = Logger.getLogger(ConfigLinuxOpenOfficePanel.class);
    private static final long serialVersionUID = 1L;
    public static final Icon ICON = IconLoader.icon("folderOO.png");
    public static final String NAME = "Open Office";
    public static final String OPENOFFICE_PATH_KEY = String.valueOf(ConfigLinuxOpenOfficePanel.class.getName()) + " - OPENOFFICE_PATH_KEY";
    private Blackboard blackboard;
    private SelectDirectoryPanel openOfficePathPanel;
    private JPanel pathPanel;
    private JTable pathTable;
    private JPanel southPanel;
    private JButton addPathButton;
    private JButton deletePathButton;
    private JButton defaultValueButton;
    OOPathTableModel tableModel;

    public ConfigLinuxOpenOfficePanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new BorderLayout());
        this.southPanel = new JPanel();
        this.southPanel.setLayout(new FlowLayout(2));
        this.addPathButton = new JButton(String.valueOf(I18N.getString("org.saig.jump.widgets.config.ConfigLinuxOpenOfficePanel.Add")) + "...");
        this.addPathButton.addActionListener(this);
        this.deletePathButton = new JButton(I18N.getString("org.saig.jump.widgets.config.ConfigLinuxOpenOfficePanel.Remove"));
        this.deletePathButton.addActionListener(this);
        this.defaultValueButton = new JButton(I18N.getString("org.saig.jump.widgets.config.ConfigLinuxOpenOfficePanel.Stablish-default-values"));
        this.defaultValueButton.addActionListener(this);
        this.southPanel.add(this.defaultValueButton);
        this.southPanel.add(this.addPathButton);
        this.southPanel.add(this.deletePathButton);
        this.pathTable = new JTable();
        this.tableModel = new OOPathTableModel();
        this.pathTable.setDefaultRenderer(Icon.class, new IconRenderer());
        this.pathTable.setModel(this.tableModel);
        TableColumn tc = this.pathTable.getColumnModel().getColumn(1);
        tc.setMaxWidth(45);
        this.pathPanel = new JPanel();
        this.pathPanel.setLayout(new BorderLayout());
        this.pathPanel.add((Component)new JScrollPane(this.pathTable), "Center");
        this.pathPanel.add((Component)this.southPanel, "South");
        this.pathPanel.setBorder(new TitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigLinuxOpenOfficePanel.Path-config-for-OpenOffice")));
        this.add((Component)this.pathPanel, "Center");
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
        String path = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(OPENOFFICE_PATH_KEY);
        if (path != null && !path.equals("")) {
            this.tableModel.removeAllPath();
            List<String> paths = OpenOfficeLibLoader.extractPathsFromPathString(path);
            for (String p : paths) {
                this.tableModel.addPath(p);
            }
        } else {
            this.defaultValues();
        }
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(OPENOFFICE_PATH_KEY, OpenOfficeLibLoader.pathStringFromPaths(this.tableModel.getPaths()));
    }

    @Override
    public String validateInput() {
        boolean ok = true;
        for (String path : this.tableModel.getPaths()) {
            File f = new File(path);
            ok &= f.exists();
        }
        return ok ? null : I18N.getString("org.saig.jump.widgets.config.ConfigLinuxOpenOfficePanel.Some-OpenOffice-library-paths-are-not-valid");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.addPathButton) {
            this.addPath();
        } else if (e.getSource() == this.deletePathButton) {
            this.deletePath();
        } else if (e.getSource() == this.defaultValueButton) {
            this.defaultValues();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void defaultValues() {
        this.tableModel.removeAllPath();
        File f = new File("resources/openoffice/oodefaultpath.cfg");
        if (f.exists()) {
            BufferedReader br = null;
            try {
                try {
                    br = new BufferedReader(new FileReader(f));
                    String st = null;
                    while ((st = br.readLine()) != null) {
                        if (st.startsWith("#")) continue;
                        this.tableModel.addPath(st);
                    }
                    return;
                }
                catch (IOException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (br == null) return;
                    try {
                        br.close();
                        return;
                    }
                    catch (IOException e2) {
                        LOGGER.error((Object)"", (Throwable)e2);
                    }
                }
                return;
            }
            finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                }
            }
        }
        LOGGER.error((Object)I18N.getMessage("org.saig.jump.widgets.config.ConfigLinuxOpenOfficePanel.The-file-{0}-have-not-been-found", new Object[]{"resources/openoffice/oodefaultpath.cfg"}));
    }

    private void deletePath() {
        int i = this.pathTable.getSelectedRow();
        if (i != -1) {
            this.tableModel.removePath(i);
        }
    }

    private void addPath() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(JUMPWorkbench.getFrameInstance());
        if (returnVal == 0) {
            File f = fc.getSelectedFile();
            if (f.getName().contains("soffice")) {
                f = f.getParentFile();
            }
            this.tableModel.addPath(f.getAbsolutePath());
        }
    }
}

