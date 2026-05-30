/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class AbstractPlugInOptionsDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    protected JPanel imagePanel;
    protected JPanel optionsPanel;
    protected OKCancelPanel okCancelPanel;

    public AbstractPlugInOptionsDialog(JFrame owner, boolean modal, String title) {
        super(owner, title, modal);
        this.initializeGUI();
        this.pack();
    }

    protected void initializeGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        if (this.getImageIcon() != null) {
            mainPanel.add((Component)this.getImagePanel(), "West");
        }
        mainPanel.add((Component)this.getOptionsPanel(), "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    protected JPanel getImagePanel() {
        if (this.imagePanel == null) {
            this.imagePanel = new JPanel(new BorderLayout());
            this.imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel imageLabel = new JLabel(this.getImageIcon());
            this.imagePanel.add((Component)imageLabel, "Center");
        }
        return this.imagePanel;
    }

    protected abstract Icon getImageIcon();

    protected abstract JPanel getOptionsPanel();

    protected JPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.okCancelPanel)) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.setVisible(false);
                }
            } else {
                this.setVisible(false);
            }
        }
    }

    protected abstract boolean isInputValid();

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }
}

