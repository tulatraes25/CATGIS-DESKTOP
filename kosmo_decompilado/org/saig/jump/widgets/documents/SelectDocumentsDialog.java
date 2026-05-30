/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.documents;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.jump.widgets.documents.ManageDocumentsPanel;

public class SelectDocumentsDialog
extends JDialog {
    private ManageDocumentsPanel manageDocumentsPanel;
    private JPanel buttonPanel;
    private JButton exitButton;
    private JButton selectButton;

    public SelectDocumentsDialog(JFrame owner, boolean modal) {
        super((Frame)owner, modal);
        this.initGUI();
    }

    private void initGUI() {
        try {
            this.setTitle("Seleccionar documentos");
            this.manageDocumentsPanel = new ManageDocumentsPanel();
            this.getContentPane().add((Component)this.manageDocumentsPanel, "Center");
            this.buttonPanel = new JPanel();
            FlowLayout buttonPanelLayout = new FlowLayout();
            buttonPanelLayout.setHgap(15);
            this.buttonPanel.setLayout(buttonPanelLayout);
            this.getContentPane().add((Component)this.buttonPanel, "South");
            this.selectButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("accept.png")));
            this.buttonPanel.add(this.getSelectButton());
            this.selectButton.setText("Seleccionar");
            this.exitButton = new JButton();
            this.buttonPanel.add(this.getExitButton());
            this.exitButton.setText("Salir");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JButton getSelectButton() {
        return this.selectButton;
    }

    public JButton getExitButton() {
        return this.exitButton;
    }

    public ManageDocumentsPanel getManageDocumentsPanel() {
        return this.manageDocumentsPanel;
    }
}

