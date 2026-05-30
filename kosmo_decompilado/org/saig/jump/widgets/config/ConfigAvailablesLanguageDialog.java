/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigAvailablesLanguagePanel;

public class ConfigAvailablesLanguageDialog
extends JDialog {
    private boolean ok;
    private ConfigAvailablesLanguagePanel configAvailablesLanguagePanel;

    public ConfigAvailablesLanguageDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(I18N.getString("org.saig.jump.widgets.config.ConfigAvailablesLanguageDialog.Configure-available-languages"));
        this.setContentPane(this.getMainPanel());
        this.setMinimumSize(new Dimension(290, 480));
        this.setPreferredSize(new Dimension(290, 480));
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.setVisible(true);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.configAvailablesLanguagePanel = new ConfigAvailablesLanguagePanel();
        mainPanel.add((Component)this.configAvailablesLanguagePanel, "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
        return mainPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigAvailablesLanguageDialog.this.ok = okCancelPanel.wasOKPressed();
                if (ConfigAvailablesLanguageDialog.this.ok) {
                    ConfigAvailablesLanguageDialog.this.configAvailablesLanguagePanel.okPressed();
                }
                ConfigAvailablesLanguageDialog.this.dispose();
            }
        });
        return okCancelPanel;
    }

    public boolean isOk() {
        return this.ok;
    }
}

