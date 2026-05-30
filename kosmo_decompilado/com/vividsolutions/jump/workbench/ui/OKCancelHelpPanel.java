/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.saig.core.context.documents.DocumentManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class OKCancelHelpPanel
extends OKCancelPanel {
    protected JButton helpButton;
    protected String parentID;
    protected String helpTag;

    public OKCancelHelpPanel(String parentID) {
        this.parentID = parentID;
        this.initilizeHelpPanel();
    }

    public OKCancelHelpPanel(JButton[] botones, String parentID) {
        this.parentID = parentID;
        this.initilizeHelpPanel(botones);
    }

    private void initilizeHelpPanel() {
        this.initilizeHelpPanel(null);
    }

    private void initilizeHelpPanel(JButton[] botones) {
        if (botones != null) {
            int i = 0;
            while (i < botones.length) {
                this.innerButtonPanel.add((Component)botones[i], null);
                ++i;
            }
        }
        this.helpTag = DocumentManager.getHelpTag(this.parentID);
        if (this.helpTag != null) {
            this.helpButton = new JButton(IconLoader.icon("help.png"));
            this.helpButton.setText(I18N.getString("com.vividsolutions.jump.workbench.ui.OKCancelHelpPanel.help"));
            this.helpButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    DocumentManager docManager = DocumentManager.getInstance("_HELP_DOCUMENT_MANAGER_KEY_");
                    try {
                        docManager.openDocumentByInternalName(JUMPWorkbench.getFrameInstance().getActiveInternalFrame(), OKCancelHelpPanel.this.helpTag);
                    }
                    catch (Exception e1) {
                        OKCancelHelpPanel.this.LOGGER.error((Object)"", (Throwable)e1);
                        DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("com.vividsolutions.jump.workbench.ui.OKCancelHelpPanel.there-were-errors-while-opening-help-document-look-at-log-file"), I18N.getString("com.vividsolutions.jump.workbench.ui.OKCancelHelpPanel.error-while-opening-document"));
                    }
                }
            });
            this.innerButtonPanel.add((Component)this.helpButton, null);
        }
        this.setAcceptButtonIcon(IconLoader.icon("accept.png"));
        this.setCancelButtonIcon(IconLoader.icon("delete_small.gif"));
    }
}

