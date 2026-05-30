/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.sdi;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class ShowServerCapabilitiesDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ShowServerCapabilitiesDialog.class);
    private JPanel capabilitiesPanel;
    private JTextArea capabilitiesTextArea;
    private OKCancelPanel okCancelPanel;

    public ShowServerCapabilitiesDialog(JDialog owner, boolean modal, String title) {
        super((Dialog)owner, modal);
        this.setTitle(title);
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getCapabilitiesPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getOkCancelPanel());
    }

    private JPanel getCapabilitiesPanel() {
        if (this.capabilitiesPanel == null) {
            this.capabilitiesPanel = new JPanel(new BorderLayout());
            this.capabilitiesTextArea = new JTextArea();
            this.capabilitiesTextArea.setEditable(false);
            this.capabilitiesTextArea.setFont(new JLabel().getFont());
            this.capabilitiesTextArea.setLineWrap(false);
            this.capabilitiesTextArea.setWrapStyleWord(false);
            JScrollPane pane = new JScrollPane(this.capabilitiesTextArea, 22, 30);
            pane.setMinimumSize(new Dimension(500, 350));
            pane.setPreferredSize(new Dimension(500, 350));
            this.capabilitiesPanel.add(pane);
        }
        return this.capabilitiesPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.sdi.ShowServerCapabilitiesDialog.save-capabilities"));
            this.okCancelPanel.setAcceptButtonIcon(IconLoader.icon("Save.gif"));
            this.okCancelPanel.setCancelButtonText(I18N.getString("org.saig.jump.widgets.sdi.ShowServerCapabilitiesDialog.close"));
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ShowServerCapabilitiesDialog.this.okCancelPanel.wasOKPressed()) {
                        JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
                        fileChooser.setFileFilter(GUIUtil.createFileFilter(I18N.getString("org.saig.jump.widgets.sdi.ShowServerCapabilitiesDialog.xml-files"), new String[]{"xml"}));
                        fileChooser.setDialogTitle(I18N.getString("org.saig.jump.widgets.sdi.ShowServerCapabilitiesDialog.save-capabilities"));
                        if (fileChooser.showSaveDialog(ShowServerCapabilitiesDialog.this) == 0) {
                            try {
                                File capabilitiesFile = fileChooser.getSelectedFile();
                                capabilitiesFile = FileUtil.addValidExtension(capabilitiesFile, "xml");
                                FileUtil.setContents(capabilitiesFile.getAbsolutePath(), ShowServerCapabilitiesDialog.this.capabilitiesTextArea.getText());
                            }
                            catch (IOException e1) {
                                LOGGER.error((Object)"", (Throwable)e1);
                            }
                            ShowServerCapabilitiesDialog.this.setVisible(false);
                        }
                    } else {
                        ShowServerCapabilitiesDialog.this.setVisible(false);
                    }
                }
            });
        }
        return this.okCancelPanel;
    }

    public void setCapabilities(String capabilities) {
        this.capabilitiesTextArea.setText(capabilities);
        this.capabilitiesTextArea.setCaretPosition(0);
    }
}

