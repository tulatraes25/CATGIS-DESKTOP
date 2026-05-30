/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class SelectFilePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    protected JTextField pathTextField;
    protected JButton selecFileButton;
    protected JFileChooser fileChooser;
    protected FileFilter fileFilter;
    protected boolean open = true;
    protected List<ActionListener> actionListeners = new ArrayList<ActionListener>();
    protected EnableCheck[] openEnableChecks = new EnableCheck[]{new EnableCheck(){

        @Override
        public String check(JComponent component) {
            String selectedPath = SelectFilePanel.this.getSelectedPath().trim();
            if (StringUtils.isEmpty((String)selectedPath)) {
                return I18N.getString("org.saig.jump.widgets.util.SelectFilePanel.A-valid-path-has-not-been-selected");
            }
            return !new File(selectedPath).exists() ? I18N.getMessage("org.saig.jump.widgets.util.SelectFilePanel.the-selected-file-{0}-does-not-exist", new Object[]{selectedPath}) : null;
        }
    }, new EnableCheck(){

        @Override
        public String check(JComponent component) {
            String selectedPath = SelectFilePanel.this.getSelectedPath().trim();
            if (StringUtils.isEmpty((String)selectedPath)) {
                return I18N.getString("org.saig.jump.widgets.util.SelectFilePanel.A-valid-path-has-not-been-selected");
            }
            return !new File(selectedPath).canRead() ? I18N.getMessage("org.saig.jump.widgets.util.SelectFilePanel.the-selected-file-{0}-can-not-be-read", new Object[]{selectedPath}) : null;
        }
    }};
    protected EnableCheck[] saveEnableChecks = new EnableCheck[]{new EnableCheck(){

        @Override
        public String check(JComponent component) {
            String selectedPath = SelectFilePanel.this.getSelectedPath().trim();
            if (StringUtils.isEmpty((String)selectedPath)) {
                return I18N.getString("org.saig.jump.widgets.util.SelectFilePanel.A-valid-path-has-not-been-selected");
            }
            File file = new File(selectedPath);
            return file.exists() && !file.canWrite() ? I18N.getMessage("org.saig.jump.widgets.util.SelectFilePanel.the-selected-file-{0}-can-not-be-written", new Object[]{selectedPath}) : null;
        }
    }};
    protected boolean okPressed;
    protected EnableCheck[] enableChecks;

    public SelectFilePanel() {
        this.enableChecks = this.open ? this.openEnableChecks : this.saveEnableChecks;
        this.initialize();
    }

    public SelectFilePanel(String description, String[] extensions) {
        this(description, extensions, true);
    }

    public SelectFilePanel(String description, String[] extensions, boolean open) {
        this.open = open;
        this.enableChecks = open ? this.openEnableChecks : this.saveEnableChecks;
        this.initialize();
        if (extensions != null) {
            this.fileFilter = GUIUtil.createFileFilter(description, extensions);
            this.fileChooser.setFileFilter(this.fileFilter);
        }
    }

    public void setFileFilter(String[] extensions) {
        if (extensions != null) {
            this.fileFilter = GUIUtil.createFileFilter(I18N.getString("org.saig.jump.widgets.util.SelectFilePanel.select"), extensions);
            this.fileChooser.setFileFilter(this.fileFilter);
        } else {
            this.fileChooser.removeChoosableFileFilter(this.fileFilter);
            this.fileFilter = null;
            this.fileChooser.setFileFilter(null);
        }
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        this.fileChooser = this.open ? GUIUtil.createJFileChooserWithExistenceChecking() : GUIUtil.createJFileChooserWithOverwritePrompting();
        this.fileChooser.setFileSelectionMode(0);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, (JComponent)this.getPathTextField(), true, false, true);
        FormUtils.addRowInGBL((JComponent)this, 0, 1, (JComponent)this.getSelecFileButton(), false, false, true);
    }

    private JTextField getPathTextField() {
        if (this.pathTextField == null) {
            this.pathTextField = new JTextField();
        }
        return this.pathTextField;
    }

    public void setEditable(boolean editable) {
        this.pathTextField.setEditable(editable);
    }

    private JButton getSelecFileButton() {
        if (this.selecFileButton == null) {
            this.selecFileButton = new JButton();
            this.selecFileButton.setIcon(GUIUtil.resize(IconLoader.icon("Open.gif"), 20));
            this.selecFileButton.setMinimumSize(new Dimension(32, 32));
            this.selecFileButton.setPreferredSize(new Dimension(32, 32));
            this.selecFileButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    File candidateFile = new File(SelectFilePanel.this.pathTextField.getText().trim());
                    if (candidateFile.isDirectory()) {
                        SelectFilePanel.this.fileChooser.setCurrentDirectory(candidateFile);
                    } else {
                        SelectFilePanel.this.fileChooser.setSelectedFile(candidateFile);
                    }
                    String message = SelectFilePanel.this.open ? I18N.getString("org.saig.jump.widgets.util.SelectFilePanel.select") : I18N.getString("org.saig.jump.widgets.util.SelectFilePanel.save");
                    int returnVal = SelectFilePanel.this.fileChooser.showDialog(SelectFilePanel.this, message);
                    if (returnVal == 0) {
                        File file = SelectFilePanel.this.fileChooser.getSelectedFile();
                        SelectFilePanel.this.pathTextField.setText(file.getPath());
                        SelectFilePanel.this.okButton_actionPerformed(e);
                    } else {
                        SelectFilePanel.this.cancelButton_actionPerformed(e);
                    }
                }
            });
        }
        return this.selecFileButton;
    }

    public String getSelectedPath() {
        return this.getPathTextField().getText().trim();
    }

    public void setSelectedPath(String path) {
        this.getPathTextField().setText(path);
    }

    public EnableCheck[] getEnableChecks() {
        return this.enableChecks;
    }

    public boolean wasOKPressed() {
        return this.okPressed;
    }

    public void setOKPressed(boolean okPressed) {
        this.okPressed = okPressed;
    }

    void okButton_actionPerformed(ActionEvent e) {
        this.okPressed = true;
        this.fireActionPerformed();
    }

    void cancelButton_actionPerformed(ActionEvent e) {
        this.okPressed = false;
        this.fireActionPerformed();
    }

    public void addActionListener(ActionListener l) {
        this.actionListeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        this.actionListeners.remove(l);
    }

    private void fireActionPerformed() {
        for (ActionListener l : this.actionListeners) {
            l.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    public String firstErrorMessage() {
        String message = null;
        int i = 0;
        while (i < this.enableChecks.length) {
            message = this.enableChecks[i].check(null);
            if (message != null) {
                return message;
            }
            ++i;
        }
        return message;
    }

    public boolean isInputValid() {
        return this.firstErrorMessage() == null;
    }

    public void addChangePathListener(DocumentListener dl) {
        this.pathTextField.getDocument().addDocumentListener(dl);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.pathTextField.setEnabled(enabled);
        this.selecFileButton.setEnabled(enabled);
    }

    public void setEnableChecks(EnableCheck[] checks) {
        this.enableChecks = checks;
    }
}

