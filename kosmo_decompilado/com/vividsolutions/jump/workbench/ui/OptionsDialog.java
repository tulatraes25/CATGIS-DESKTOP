/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class OptionsDialog
extends JDialog {
    private JPanel panel1 = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private JTabbedPane tabbedPane = new JTabbedPane();

    private OptionsDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            this.jbInit();
            this.pack();
        }
        catch (Exception ex) {
            Assert.shouldNeverReachHere((String)ex.getMessage());
        }
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                OptionsDialog.this.fireInit();
            }
        });
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (OptionsDialog.this.okCancelPanel.wasOKPressed()) {
                    String errorMessage = OptionsDialog.this.validateInput();
                    if (errorMessage != null) {
                        DialogFactory.showErrorDialog(OptionsDialog.this, errorMessage, I18N.getString("com.vividsolutions.jump.workbench.ui.OptionsDialog.Parameters-error"));
                        return;
                    }
                    OptionsDialog.this.fireOKPressed();
                    OptionsDialog.this.setVisible(false);
                    return;
                }
                OptionsDialog.this.setVisible(false);
            }
        });
    }

    public static OptionsDialog instance(JUMPWorkbench workbench) {
        return OptionsDialog.instance(JUMPWorkbench.getBlackboard(), workbench.getFrame());
    }

    public static OptionsDialog instance(Blackboard blackboard, Frame frame) {
        return (OptionsDialog)blackboard.get(OptionsDialog.class + " - INSTANCE", new OptionsDialog(frame, I18N.getString("workbench.ui.OptionsDialog.options"), true));
    }

    private void fireOKPressed() {
        for (OptionsPanel panel : this.optionsPanels()) {
            panel.okPressed();
        }
    }

    private void fireInit() {
        for (OptionsPanel panel : this.optionsPanels()) {
            panel.init();
        }
    }

    private Collection optionsPanels() {
        ArrayList<Component> optionsPanels = new ArrayList<Component>();
        int i = 0;
        while (i < this.tabbedPane.getTabCount()) {
            optionsPanels.add(this.tabbedPane.getComponentAt(i));
            ++i;
        }
        return optionsPanels;
    }

    private String validateInput() {
        for (OptionsPanel panel : this.optionsPanels()) {
            String errorMessage = panel.validateInput();
            if (errorMessage == null) continue;
            return errorMessage;
        }
        return null;
    }

    public void addTab(String title, OptionsPanel panel) {
        this.addTab(title, null, panel);
    }

    public void addTab(OptionsPanel panel) {
        this.addTab(panel.getName(), panel.getIcon(), panel);
    }

    public void addTab(String title, Icon icon, OptionsPanel panel) {
        this.tabbedPane.addTab(title, icon, panel);
        this.pack();
    }

    public void removeTab(String title) {
        this.tabbedPane.removeTabAt(this.tabbedPane.indexOfTab(title));
        this.pack();
    }

    private void jbInit() throws Exception {
        this.panel1.setLayout(this.borderLayout1);
        this.setModal(true);
        this.setTitle(I18N.getString("workbench.ui.OptionsDialog.options"));
        this.getContentPane().add(this.panel1);
        this.panel1.add((Component)this.tabbedPane, "Center");
        this.getContentPane().add((Component)this.okCancelPanel, "South");
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }
}

