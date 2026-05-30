/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.dialog;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.gui.listeners.ImportNewCrsDialogListener;
import org.gvsig.crs.gui.panels.EPSGpanel;
import org.saig.core.util.DialogManager;

public class ImportNewCrsDialog
extends JPanel
implements IWindow {
    private static final long serialVersionUID = 1L;
    final String epsg = "EPSG";
    private EPSGpanel epsgPanel = new EPSGpanel();
    private int crsCode = -1;
    private JPanel mainPanel = null;
    private JPanel buttonsPanel;
    private JButton acceptButton;
    private JButton cancelButton;
    private String option = "";

    public ImportNewCrsDialog(String opt) {
        this.epsgPanel.connection();
        this.setOption(opt);
        this.initialize();
        this.setListeners();
    }

    public void initialize() {
        this.setLayout(new GridLayout(0, 1));
        this.add((Component)this.getJPanelMain(), "Center");
        this.add((Component)this.getJPanelButtons(), "South");
    }

    public JPanel getJPanelMain() {
        if (this.mainPanel == null) {
            this.mainPanel = new JPanel();
            this.mainPanel.setLayout(new CardLayout());
            this.mainPanel.setPreferredSize(new Dimension(525, 230));
            this.mainPanel.add("EPSG", this.epsgPanel);
        }
        return this.mainPanel;
    }

    private JPanel getJPanelButtons() {
        if (this.buttonsPanel == null) {
            this.buttonsPanel = new JPanel();
            this.buttonsPanel.setLayout(new FlowLayout(2));
            this.buttonsPanel.add((Component)this.getJButtonCancel(), null);
            this.buttonsPanel.add((Component)this.getJButtonAccept(), null);
        }
        return this.buttonsPanel;
    }

    public JButton getJButtonCancel() {
        if (this.cancelButton == null) {
            this.cancelButton = new JButton();
            this.cancelButton.setText(CRSI18NConstants.CANCEL_KEY);
            this.cancelButton.setPreferredSize(new Dimension(100, 25));
            this.cancelButton.setMnemonic('C');
            this.cancelButton.setToolTipText(CRSI18NConstants.CANCEL_KEY);
        }
        return this.cancelButton;
    }

    public void cancelButton_actionPerformed(ActionEvent e) {
        DialogManager.closeJDialog(this);
    }

    public JButton getJButtonAccept() {
        if (this.acceptButton == null) {
            this.acceptButton = new JButton();
            this.acceptButton.setText(CRSI18NConstants.ACCEPT_KEY);
            this.acceptButton.setPreferredSize(new Dimension(100, 25));
            this.acceptButton.setEnabled(false);
            this.acceptButton.setMnemonic('A');
            this.acceptButton.setToolTipText(CRSI18NConstants.ACCEPT_KEY);
        }
        return this.acceptButton;
    }

    private void setListeners() {
        ImportNewCrsDialogListener listener = new ImportNewCrsDialogListener(this);
        this.getJButtonCancel().addActionListener(listener);
        this.getJButtonAccept().addActionListener(listener);
        ListSelectionModel rowSM = this.getEpsgPanel().getJTable().getSelectionModel();
        rowSM.addListSelectionListener(listener);
        this.getEpsgPanel().getJTable().addMouseListener(listener);
    }

    public void setOption(String opt) {
        this.option = opt;
    }

    public String getOption() {
        return this.option;
    }

    public EPSGpanel getEpsgPanel() {
        return this.epsgPanel;
    }

    public void setCode(int code) {
        this.crsCode = code;
    }

    public int getCode() {
        return this.crsCode;
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(this.option);
        m_viewinfo.setWidth(525);
        m_viewinfo.setHeight(320);
        return m_viewinfo;
    }

    public Object getWindowProfile() {
        return 8;
    }
}

