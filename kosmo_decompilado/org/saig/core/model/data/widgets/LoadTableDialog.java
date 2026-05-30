/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.SortedMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.TableFactory;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSource;
import org.saig.core.model.data.widgets.MDBDialog;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.datasource.JDBCPropertiesPanel;
import org.saig.jump.widgets.util.DialogFactory;

public class LoadTableDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final int TYPE_FILE = 1;
    private static final int TYPE_DATA_BASE = 2;
    public static final Logger LOGGER = Logger.getLogger(LoadTableDialog.class);
    private int type = 1;
    private JPanel cardPanel;
    private JDBCPropertiesPanel jdbcPanel;
    private JFileChooser fileChooser;
    private boolean exitOk = false;
    private JComboBox selectCharsetCombobox;
    public String lastPath;

    public LoadTableDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.load-tables"));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        JPanel selectorPanel = this.createSelectorPanel();
        this.cardPanel = this.createCardLayoutPanel();
        OKCancelPanel okCancelPanel = this.createOKcancelPanel();
        FormUtils.addRowInGBL(mainPanel, 0, 0, selectorPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.cardPanel);
        FormUtils.addRowInGBL(mainPanel, 2, 0, okCancelPanel);
        this.setContentPane(mainPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    private JPanel createSelectorPanel() {
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new GridBagLayout());
        selectorPanel.setBorder(BorderFactory.createEtchedBorder());
        String[] valores = new String[]{I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.mdb-or-dbf-files"), I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.databases")};
        final JComboBox<String> selectorComboBox = new JComboBox<String>(valores);
        selectorComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int index = selectorComboBox.getSelectedIndex();
                if (index == 0) {
                    CardLayout cl = (CardLayout)LoadTableDialog.this.cardPanel.getLayout();
                    cl.show(LoadTableDialog.this.cardPanel, "DBF_MDB");
                    LoadTableDialog.this.fileChooser.rescanCurrentDirectory();
                    LoadTableDialog.this.type = 1;
                } else {
                    CardLayout cl = (CardLayout)LoadTableDialog.this.cardPanel.getLayout();
                    cl.show(LoadTableDialog.this.cardPanel, "DATA_BASE");
                    LoadTableDialog.this.type = 2;
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)selectorPanel, 0, 0, new JLabel(String.valueOf(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.table-type")) + ":"), selectorComboBox, false);
        FormUtils.addFiller(selectorPanel, 0, 30);
        return selectorPanel;
    }

    private JPanel createCardLayoutPanel() {
        this.cardPanel = new JPanel();
        this.cardPanel.setLayout(new CardLayout());
        this.cardPanel.add(this.createFilePanel(), "DBF_MDB");
        this.cardPanel.add(this.createDataBasePanel(), "DATA_BASE");
        return this.cardPanel;
    }

    private Component createDataBasePanel() {
        this.jdbcPanel = new JDBCPropertiesPanel(false);
        return this.jdbcPanel;
    }

    private Component createFilePanel() {
        JPanel filePanel = new JPanel(new BorderLayout());
        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(0);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        FileFilter filter = GUIUtil.createFileFilter(I18N.getString("org.saig.core.model.data.widgets.DataManagerPanel.alfanumeric-files"), new String[]{"dbf", "mdb"});
        this.fileChooser.addChoosableFileFilter(filter);
        this.fileChooser.setMultiSelectionEnabled(true);
        this.fileChooser.setFileFilter(filter);
        this.fileChooser.setControlButtonsAreShown(false);
        filePanel.add(this.fileChooser);
        JPanel southComponent1 = new JPanel(new GridBagLayout());
        southComponent1.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Advanced-options")));
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        Vector charsetVector = new Vector(charsets.keySet());
        JLabel charsetSelection = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Coding")) + " (DBFs): ");
        this.selectCharsetCombobox = new JComboBox(charsetVector);
        Charset systemCharset = ShapeFileDataSource.DEFAULT_STRING_CHARSET;
        this.selectCharsetCombobox.setSelectedItem(systemCharset.name());
        FormUtils.addRowInGBL((JComponent)southComponent1, 0, 0, charsetSelection, (JComponent)this.selectCharsetCombobox);
        filePanel.add((Component)southComponent1, "South");
        return filePanel;
    }

    public boolean isOk() {
        return this.exitOk;
    }

    public void refreshPath() {
        File lastPathFile;
        String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.DATA_LOAD_PATH_KEY);
        if (defaultPath != null && !defaultPath.equals("")) {
            File defaultPathFile = new File(defaultPath);
            if (defaultPathFile.canRead()) {
                this.fileChooser.setCurrentDirectory(defaultPathFile);
            }
        } else if (this.lastPath != null && (lastPathFile = new File(this.lastPath)).exists() && lastPathFile.canRead()) {
            this.fileChooser.setCurrentDirectory(lastPathFile);
        }
        this.fileChooser.rescanCurrentDirectory();
    }

    private OKCancelPanel createOKcancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        okCancelPanel.setLayout(gbPaneOKCancel);
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (okCancelPanel.wasOKPressed()) {
                    if (LoadTableDialog.this.type == 2) {
                        if (LoadTableDialog.this.jdbcPanel.checkParameters()) {
                            try {
                                LoadTableDialog.this.jdbcPanel.initializeTable();
                            }
                            catch (Exception e) {
                                DialogFactory.showErrorDialog(LoadTableDialog.this.jdbcPanel, String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error-check-your-parameters")) + ":\n" + e.getMessage(), I18N.getString("org.saig.jump.widgets.datasource.JDBCLoadDataSourceQueryChooser.connection-error"));
                                return;
                            }
                            Dialog dialog = null;
                            try {
                                List<TableDBRecordDataSource> datasources = LoadTableDialog.this.jdbcPanel.getTableDataSources();
                                DataManager dataManager = JUMPWorkbench.getFrameInstance().getContext().getDataManager();
                                for (TableDBRecordDataSource currentDS : datasources) {
                                    dialog = new DataBaseWaitDialog(JUMPWorkbench.getFrameInstance(), true, currentDS, dataManager);
                                    dialog.setVisible(true);
                                }
                            }
                            catch (Exception e) {
                                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.an-error-has-been-produced-the-associated-message-is")) + ":" + e.getMessage(), I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.error"));
                                if (dialog != null) {
                                    dialog.setVisible(false);
                                }
                                return;
                            }
                        }
                    } else {
                        final TaskMonitorDialog progressDialog = new TaskMonitorDialog((Frame)JUMPWorkbench.getFrameInstance(), null);
                        progressDialog.setTitle(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.load-tables"));
                        progressDialog.addComponentListener(new ComponentAdapter(){

                            @Override
                            public void componentShown(ComponentEvent e) {
                                new Thread(new Runnable(){

                                    @Override
                                    public void run() {
                                        try {
                                            try {
                                                progressDialog.report(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.loading-data"));
                                                File[] files = LoadTableDialog.this.fileChooser.getSelectedFiles();
                                                Charset selectedCharset = Charset.forName((String)LoadTableDialog.this.selectCharsetCombobox.getSelectedItem());
                                                DataManager dataManager = JUMPWorkbench.getFrameInstance().getContext().getDataManager();
                                                ((this).this).LoadTableDialog.this.lastPath = LoadTableDialog.this.fileChooser.getCurrentDirectory().getAbsolutePath();
                                                int i = 0;
                                                while (i < files.length) {
                                                    block13: {
                                                        TableRecordDataSource dataSource;
                                                        block12: {
                                                            File file;
                                                            block15: {
                                                                file = files[i];
                                                                if (file.exists()) break block15;
                                                                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.file-not-found")) + ": " + file.toString(), I18N.getString("org.saig.jump.widgets.print.actions.OpenLayout.layout-file-read-error"));
                                                                break block13;
                                                            }
                                                            String ext = FileUtil.getExtension(file);
                                                            dataSource = null;
                                                            if (ext.equalsIgnoreCase("dbf")) {
                                                                try {
                                                                    dataSource = new DBFRecordDataSource(file.getAbsolutePath(), null, selectedCharset);
                                                                    String name = FileUtil.nameWithoutExtension(file.getName());
                                                                    ((TableRecordDataSource)dataSource).setName(name);
                                                                    break block12;
                                                                }
                                                                catch (Exception e1) {
                                                                    LOGGER.error((Object)e1);
                                                                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.core.model.data.widgets.LoadTableDialog.an-unexpected-error-has-been-produced-while-reading-the-file-{0}", new Object[]{file.getAbsolutePath()}), I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.error"));
                                                                    break block13;
                                                                }
                                                            }
                                                            if (!ext.equalsIgnoreCase("mdb")) break block12;
                                                            MDBDialog mdbDialog = new MDBDialog(JUMPWorkbench.getFrameInstance(), true, file.getAbsolutePath());
                                                            if (!mdbDialog.isOk()) break block13;
                                                            dataSource = mdbDialog.getOdbcDataSource();
                                                        }
                                                        Table recordCollection = TableFactory.getRecordCollection(dataSource);
                                                        ViewTableFrame frame = new ViewTableFrame(recordCollection, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
                                                        dataManager.addTable(frame);
                                                    }
                                                    ++i;
                                                }
                                            }
                                            catch (Exception e) {
                                                LOGGER.error((Object)"", (Throwable)e);
                                                progressDialog.setExceptionMessage(e.getMessage());
                                                progressDialog.setVisible(false);
                                                return;
                                            }
                                        }
                                        finally {
                                            progressDialog.setVisible(false);
                                        }
                                    }
                                }).start();
                            }
                        });
                        GUIUtil.centre(progressDialog, LoadTableDialog.this);
                        progressDialog.setVisible(true);
                        if (StringUtils.isNotEmpty((String)progressDialog.getExceptionMessage())) {
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.an-error-has-been-produced-the-associated-message-is")) + progressDialog.getExceptionMessage(), I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.error"));
                            return;
                        }
                    }
                }
                LoadTableDialog.this.setVisible(false);
                LoadTableDialog.this.exitOk = true;
            }
        });
        return okCancelPanel;
    }

    public void refresh() {
        this.refreshPath();
        this.jdbcPanel.refresh();
    }

    private class DataBaseWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        DataBaseWaitDialog(JFrame parent, boolean modal, final TableRecordDataSource dataSource, final DataManager dataManager) {
            super((Frame)parent, modal);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(I18N.getString("org.saig.core.model.data.widgets.LoadTableDialog.loading-data"));
            JLabel label = new JLabel();
            label.setIcon(IconLoader.icon("loading.gif"));
            label.setHorizontalAlignment(0);
            this.getContentPane().add((Component)label, "Center");
            this.setSize(new Dimension(200, 100));
            GUIUtil.centreOnWindow(this);
            SwingWorker worker = new SwingWorker(){

                @Override
                public Object construct() {
                    Table recordCollection = TableFactory.getRecordCollection(dataSource);
                    ViewTableFrame dataFrame = new ViewTableFrame(recordCollection, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
                    dataManager.addTable(dataFrame);
                    return dataManager;
                }

                @Override
                public void finished() {
                    DataBaseWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        void closeWindow() {
            this.dispose();
        }
    }
}

