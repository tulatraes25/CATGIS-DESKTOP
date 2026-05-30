/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.ChangeFirerPanel;

public class SelectDirectoryPanel
extends ChangeFirerPanel
implements DocumentListener {
    private static final long serialVersionUID = 1L;
    private static final Icon FOLDER_ICON = IconLoader.icon("folder_open.png");
    private JTextField pathTextField;
    private JButton selectDirectoryButton;
    private JFileChooser fileChooser;
    private EnableCheck mandatoryEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            String selectedPath = SelectDirectoryPanel.this.getSelectedPath().trim();
            if (StringUtils.isEmpty((String)selectedPath)) {
                return I18N.getString("org.saig.jump.widgets.util.SelectDirectoryPanel.A-valid-path-has-not-been-selected");
            }
            return !new File(selectedPath).exists() ? I18N.getMessage("org.saig.jump.widgets.util.SelectDirectoryPanel.the-selected-directory-{0}-does-not-exist", new Object[]{selectedPath}) : null;
        }
    };
    private EnableCheck existenceEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            String selectedPath = SelectDirectoryPanel.this.getSelectedPath().trim();
            if (StringUtils.isEmpty((String)selectedPath)) {
                return null;
            }
            return !new File(selectedPath).canRead() ? I18N.getMessage("org.saig.jump.widgets.util.SelectDirectoryPanel.the-selected-directory-{0}-can-not-be-read", new Object[]{selectedPath}) : null;
        }
    };
    private EnableCheck[] enableChecks = null;
    private boolean mandatory;

    public SelectDirectoryPanel() {
        this(true);
    }

    public SelectDirectoryPanel(boolean isObligatory) {
        this.mandatory = isObligatory;
        this.listeners = new ArrayList();
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(1);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, (JComponent)this.getPathTextField(), true, false, true);
        FormUtils.addRowInGBL((JComponent)this, 0, 1, (JComponent)this.getSelectDirectoryButton(), false, false, true);
        this.enableChecks = this.mandatory ? new EnableCheck[]{this.mandatoryEnableCheck, this.existenceEnableCheck} : new EnableCheck[]{this.existenceEnableCheck};
    }

    private JTextField getPathTextField() {
        if (this.pathTextField == null) {
            this.pathTextField = new JTextField();
            this.pathTextField.getDocument().addDocumentListener(this);
        }
        return this.pathTextField;
    }

    private JButton getSelectDirectoryButton() {
        if (this.selectDirectoryButton == null) {
            this.selectDirectoryButton = new JButton();
            this.selectDirectoryButton.setIcon(FOLDER_ICON);
            this.selectDirectoryButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SelectDirectoryPanel.this.fileChooser.setSelectedFile(new File(SelectDirectoryPanel.this.pathTextField.getText().trim()));
                    int returnVal = SelectDirectoryPanel.this.fileChooser.showDialog(SelectDirectoryPanel.this, I18N.getString("org.saig.jump.widgets.util.SelectDirectoryPanel.select"));
                    if (returnVal == 0) {
                        File file = SelectDirectoryPanel.this.fileChooser.getSelectedFile();
                        SelectDirectoryPanel.this.pathTextField.setText(file.getPath());
                    }
                }
            });
        }
        return this.selectDirectoryButton;
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

    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        this.selectDirectoryButton.setEnabled(enable);
        this.pathTextField.setEnabled(enable);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.firePanelChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.firePanelChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.firePanelChanged();
    }
}

