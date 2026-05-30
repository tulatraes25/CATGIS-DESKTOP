/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.widgets.datasource;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.desktop.widgets.datasource.ITableSelectionPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang.StringUtils;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigPathPanel;

public class DBFPanel
extends JPanel
implements ITableSelectionPanel {
    private static final long serialVersionUID = 1L;
    protected JFileChooser fileChooser = new JFileChooser();
    protected JComboBox selectCharsetCombobox;
    private String lastPath;

    public DBFPanel() {
        super(new BorderLayout());
        this.fileChooser.setFileSelectionMode(0);
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        FileFilter filter = GUIUtil.createFileFilter(this.getDescription(), new String[]{"dbf"});
        this.fileChooser.addChoosableFileFilter(filter);
        this.fileChooser.setMultiSelectionEnabled(true);
        this.fileChooser.setFileFilter(filter);
        this.fileChooser.setControlButtonsAreShown(false);
        JPanel southComponent1 = new JPanel(new GridBagLayout());
        southComponent1.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Advanced-options")));
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        Vector charsetVector = new Vector(charsets.keySet());
        JLabel charsetSelection = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Coding")) + ": ");
        this.selectCharsetCombobox = new JComboBox(charsetVector);
        Charset systemCharset = ShapeFileDataSource.DEFAULT_STRING_CHARSET;
        this.selectCharsetCombobox.setSelectedItem(systemCharset.name());
        FormUtils.addRowInGBL((JComponent)southComponent1, 0, 0, charsetSelection, (JComponent)this.selectCharsetCombobox, false);
        FormUtils.addFiller(southComponent1, 1, 0);
        this.add((Component)this.fileChooser, "Center");
        this.add((Component)southComponent1, "South");
    }

    @Override
    public String getID() {
        return "DBF";
    }

    @Override
    public void refresh() {
        File lastPathFile;
        String defaultPath = (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigPathPanel.DATA_LOAD_PATH_KEY);
        if (StringUtils.isNotEmpty((String)defaultPath)) {
            File defaultPathFile = new File(defaultPath);
            if (defaultPathFile.exists() && defaultPathFile.canRead()) {
                this.fileChooser.setCurrentDirectory(defaultPathFile);
            }
        } else if (StringUtils.isNotEmpty((String)this.lastPath) && (lastPathFile = new File(this.lastPath)).exists() && lastPathFile.canRead()) {
            this.fileChooser.setCurrentDirectory(lastPathFile);
        }
        this.fileChooser.rescanCurrentDirectory();
    }

    @Override
    public String getDescription() {
        return I18N.getString("es.kosmo.desktop.widgets.datasource.DBFPanel.DBF-files");
    }

    @Override
    public Component getComponent() {
        return this;
    }

    public List<TableRecordDataSource> getTableDataSources() throws Exception {
        ArrayList<TableRecordDataSource> datasources = new ArrayList<TableRecordDataSource>();
        this.lastPath = this.fileChooser.getCurrentDirectory().getAbsolutePath();
        List<File> selectedFiles = this.getSelectedFiles();
        Charset selectedCharset = Charset.forName((String)this.selectCharsetCombobox.getSelectedItem());
        int i = 0;
        while (i < selectedFiles.size()) {
            File file = selectedFiles.get(i);
            DBFRecordDataSource currentDS = new DBFRecordDataSource(file.getAbsolutePath(), null, selectedCharset);
            String name = FileUtil.nameWithoutExtension(file.getName());
            ((TableRecordDataSource)currentDS).setName(name);
            datasources.add(currentDS);
            ++i;
        }
        return datasources;
    }

    @Override
    public boolean isInputValid() {
        return this.getSelectedFiles().size() > 0;
    }

    protected List<File> getSelectedFiles() {
        ArrayList<File> selectedFiles = new ArrayList<File>();
        File[] files = this.fileChooser.getSelectedFiles();
        int i = 0;
        while (i < files.length) {
            String ext;
            File currentFile = files[i];
            if (currentFile.exists() && (ext = FileUtil.getExtension(currentFile)).equalsIgnoreCase("dbf")) {
                selectedFiles.add(currentFile);
            }
            ++i;
        }
        return selectedFiles;
    }
}

