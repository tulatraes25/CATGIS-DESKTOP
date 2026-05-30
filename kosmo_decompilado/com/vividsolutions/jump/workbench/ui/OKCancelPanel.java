/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class OKCancelPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    protected final Logger LOGGER = Logger.getLogger(OKCancelPanel.class);
    protected JPanel innerButtonPanel;
    protected JButton cancelButton;
    protected JButton okButton;
    protected boolean okPressed = false;
    protected List<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public OKCancelPanel() {
        try {
            this.initialize();
        }
        catch (Exception ex) {
            this.LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    public OKCancelPanel(JButton[] buttons) {
        try {
            this.initialize();
            this.addButtons(buttons);
        }
        catch (Exception ex) {
            this.LOGGER.error((Object)"", (Throwable)ex);
        }
    }

    private void addButtons(JButton[] buttons) {
        if (buttons != null) {
            int i = 0;
            while (i < buttons.length) {
                this.innerButtonPanel.add((Component)buttons[i], null);
                ++i;
            }
        }
    }

    protected void initialize() throws Exception {
        GridLayout gridLayout1 = new GridLayout();
        this.innerButtonPanel = new JPanel();
        this.innerButtonPanel.setLayout(gridLayout1);
        gridLayout1.setVgap(5);
        gridLayout1.setHgap(5);
        this.setLayout(new FlowLayout());
        this.cancelButton = new JButton();
        this.cancelButton.setText(I18N.getString("workbench.ui.OKCancelPanel.cancel"));
        this.cancelButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                OKCancelPanel.this.cancelButton_actionPerformed(e);
            }
        });
        this.okButton = new JButton();
        this.okButton.setText(I18N.getString("workbench.ui.OKCancelPanel.ok"));
        this.okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                OKCancelPanel.this.okButton_actionPerformed(e);
            }
        });
        this.add((Component)this.innerButtonPanel, null);
        this.innerButtonPanel.add((Component)this.okButton, null);
        this.innerButtonPanel.add((Component)this.cancelButton, null);
    }

    public boolean wasOKPressed() {
        return this.okPressed;
    }

    public void setOKPressed(boolean okPressed) {
        this.okPressed = okPressed;
    }

    protected void okButton_actionPerformed(ActionEvent e) {
        this.okPressed = true;
        this.fireActionPerformed();
    }

    protected void cancelButton_actionPerformed(ActionEvent e) {
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

    public void setOKEnabled(boolean okEnabled) {
        this.okButton.setEnabled(okEnabled);
    }

    public void setCancelVisible(boolean cancelVisible) {
        if (cancelVisible && !this.innerButtonPanel.isAncestorOf(this.cancelButton)) {
            this.innerButtonPanel.add((Component)this.cancelButton, null);
        }
        if (!cancelVisible && this.innerButtonPanel.isAncestorOf(this.cancelButton)) {
            this.innerButtonPanel.remove(this.cancelButton);
        }
    }

    public void setAcceptButtonText(String acceptText) {
        this.okButton.setText(acceptText);
    }

    public void setCancelButtonText(String cancelText) {
        this.cancelButton.setText(cancelText);
    }

    public void setAcceptButtonIcon(Icon acceptIcon) {
        this.okButton.setIcon(acceptIcon);
    }

    public void setCancelButtonIcon(Icon cancelIcon) {
        this.cancelButton.setIcon(cancelIcon);
    }

    public JButton getAcceptButton() {
        return this.okButton;
    }

    public JButton getCancelButton() {
        return this.cancelButton;
    }

    @Override
    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque);
        if (this.innerButtonPanel != null) {
            this.innerButtonPanel.setOpaque(isOpaque);
        }
    }
}

