/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchFileFilter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.saig.jump.lang.I18N;

public class FileNamePanel
extends JPanel {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel upperDescriptionLabel = new JLabel();
    JComboBox comboBox = new JComboBox();
    JButton browseButton = new JButton();
    private ErrorHandler errorHandler;
    private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
    private boolean fileMustExist;
    private ArrayList browseListeners = new ArrayList();
    private int MAX_CACHE_SIZE = 10;
    private JLabel leftDescriptionLabel = new JLabel("");
    private FileFilter fileFilter = null;

    public void setDescription(String description) {
        this.upperDescriptionLabel.setText(description);
        if (description.equals("FME GML")) {
            this.setFileFilter(new WorkbenchFileFilter("FME GML"));
        } else if (description.equals("GML")) {
            this.setFileFilter(new WorkbenchFileFilter("GML"));
        } else if (description.equals("JCS GML")) {
            this.setFileFilter(new WorkbenchFileFilter("JCS GML"));
        } else if (description.equals("ESRI Shapefile")) {
            this.setFileFilter(new WorkbenchFileFilter("ESRI Shapefile"));
        } else if (description.equals(GUIUtil.wktDesc)) {
            this.setFileFilter(new WorkbenchFileFilter(GUIUtil.wktDesc));
        } else if (description.equals("XML")) {
            this.setFileFilter(new WorkbenchFileFilter("XML"));
        }
    }

    public FileNamePanel(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        GUIUtil.fixEditableComboBox(this.comboBox);
    }

    public void setFileMustExist(boolean fileMustExist) {
        this.fileMustExist = fileMustExist;
    }

    void jbInit() throws Exception {
        this.upperDescriptionLabel.setText("Description Text Goes Here");
        this.setLayout(this.gridBagLayout1);
        this.browseButton.setText(String.valueOf(I18N.getString("workbench.ui.FileNamePanel.browse")) + " ...");
        this.browseButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FileNamePanel.this.browseButton_actionPerformed(e);
            }
        });
        this.comboBox.setPreferredSize(new Dimension(300, 21));
        this.comboBox.setEditable(true);
        this.comboBox.setModel(this.comboBoxModel);
        this.add((Component)this.upperDescriptionLabel, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.leftDescriptionLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 8), 0, 0));
        this.add((Component)this.comboBox, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 8), 0, 0));
        this.add((Component)this.browseButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    public File getSelectedFile() {
        Assert.isTrue((boolean)this.isInputValid(), (String)this.getValidationError());
        File file = new File(this.getComboBoxText());
        this.addToComboBox(file, this.comboBoxModel);
        return file;
    }

    public boolean isInputValid() {
        return this.getValidationError() == null;
    }

    private String getComboBoxText() {
        return (String)this.comboBox.getEditor().getItem();
    }

    public void setSelectedFile(File file) {
        if (file == null) {
            this.comboBox.getEditor().setItem("");
            return;
        }
        this.comboBox.getEditor().setItem(file.getAbsolutePath());
    }

    public String getValidationError() {
        if (this.getComboBoxText().trim().equals("")) {
            return I18N.getString("workbench.ui.FileNamePanel.no-file-was-specified");
        }
        File file = new File(this.getComboBoxText());
        if (this.fileMustExist && !file.exists()) {
            return String.valueOf(I18N.getString("workbench.ui.FileNamePanel.the-specified-file-does-not-exist")) + " " + this.getComboBoxText();
        }
        if (this.fileMustExist && file.isDirectory()) {
            return String.valueOf(I18N.getString("workbench.ui.FileNamePanel.the-specified-file-is-a-directory")) + " " + this.getComboBoxText();
        }
        if (this.fileMustExist && !file.isFile()) {
            return String.valueOf(I18N.getString("workbench.ui.FileNamePanel.the-specified-file-is-not-normal")) + " " + this.getComboBoxText();
        }
        if (!this.fileMustExist && file.getParentFile() == null) {
            return String.valueOf(I18N.getString("workbench.ui.FileNamePanel.the-specified-parent-directory-is-not-specified")) + " " + this.getComboBoxText();
        }
        if (!this.fileMustExist && !file.getParentFile().exists()) {
            return String.valueOf(I18N.getString("workbench.ui.FileNamePanel.the-specified-parent-directory-does-not-exist")) + " " + this.getComboBoxText();
        }
        if (!this.fileMustExist && !file.getParentFile().isDirectory()) {
            return String.valueOf(I18N.getString("workbench.ui.FileNamePanel.the-specified-parent-directory-is-not-a-directory")) + " " + this.getComboBoxText();
        }
        return null;
    }

    void browseButton_actionPerformed(ActionEvent e) {
        try {
            File file = this.browse();
            if (file == null) {
                return;
            }
            this.comboBox.getEditor().setItem(file.getAbsolutePath());
            this.fireBrowseEvent(e);
        }
        catch (Throwable t) {
            this.errorHandler.handleThrowable(t);
        }
    }

    private void fireBrowseEvent(ActionEvent e) {
        for (ActionListener l : this.browseListeners) {
            l.actionPerformed(e);
        }
    }

    public void addBrowseListener(ActionListener l) {
        this.browseListeners.add(l);
    }

    private File browse() {
        File initialFile;
        JFileChooser fileChooser = this.fileMustExist ? GUIUtil.createJFileChooserWithExistenceChecking() : new JFileChooser();
        fileChooser.setDialogTitle(I18N.getString("workbench.ui.FileNamePanel.browse"));
        fileChooser.setFileSelectionMode(0);
        GUIUtil.removeChoosableFileFilters(fileChooser);
        fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        if (this.fileFilter != null) {
            fileChooser.addChoosableFileFilter(this.fileFilter);
            fileChooser.setFileFilter(this.fileFilter);
        }
        if ((initialFile = this.getInitialFile()).exists() && initialFile.isFile()) {
            fileChooser.setSelectedFile(initialFile);
        } else if (initialFile.exists() && initialFile.isDirectory()) {
            fileChooser.setCurrentDirectory(initialFile);
        } else if (initialFile.getParentFile() != null && initialFile.getParentFile().exists()) {
            fileChooser.setCurrentDirectory(initialFile.getParentFile());
        }
        fileChooser.setMultiSelectionEnabled(false);
        if (fileChooser.showOpenDialog(SwingUtilities.windowForComponent(this)) != 0) {
            return null;
        }
        return fileChooser.getSelectedFile();
    }

    protected File getInitialFile() {
        return new File(this.getComboBoxText());
    }

    private void addToComboBox(File file, DefaultComboBoxModel comboBoxModel) {
        comboBoxModel.removeElement(file.getAbsolutePath());
        comboBoxModel.insertElementAt(file.getAbsolutePath(), 0);
        if (comboBoxModel.getSize() > this.MAX_CACHE_SIZE) {
            comboBoxModel.removeElementAt(comboBoxModel.getSize() - 1);
        }
        this.comboBox.setSelectedIndex(0);
    }

    public void setUpperDescription(String description) {
        this.upperDescriptionLabel.setText(description);
    }

    public void setLeftDescription(String description) {
        this.leftDescriptionLabel.setText(description);
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }
}

