/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class TemporalLayersInLayerGroupDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String TITLE = I18N.getString("org.saig.jump.widgets.utils.TemporalLayersInLayerGroupDialog.Temporal-layers");
    private JPanel messagePanel;
    private JPanel temporalLayerPanel;
    private JTextArea temporalLayerTextArea;
    private OKCancelPanel okCancelPanel;
    List<String> tempLayerNames = new ArrayList<String>();

    public TemporalLayersInLayerGroupDialog(JFrame owner, boolean modal, List<String> temporalLayerNames) {
        super(owner, TITLE, modal);
        this.tempLayerNames = temporalLayerNames;
        this.initialize();
        this.addTemporalLayerNames();
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    private void addTemporalLayerNames() {
        StringBuffer buffer = new StringBuffer();
        Collections.sort(this.tempLayerNames);
        for (String currentName : this.tempLayerNames) {
            buffer.append("* " + currentName + "\n");
        }
        this.temporalLayerTextArea.setText(buffer.toString());
        this.temporalLayerTextArea.setCaretPosition(0);
    }

    private void initialize() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getMessagePanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getTemporalLayerPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.getOkCancelPanel());
    }

    public JPanel getMessagePanel() {
        if (this.messagePanel == null) {
            this.messagePanel = new JPanel(new GridBagLayout());
            this.messagePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.TemporalLayersInLayerGroupDialog.Temporal-layers")));
            JLabel messageLabel = new JLabel("<HTML>" + I18N.getString("org.saig.jump.widgets.utils.TemporalLayersInLayerGroupDialog.The-category-that-you-want-to-save-have-some-temporal-layers-that-will-not-be-saved") + "<BR>" + I18N.getString("org.saig.jump.widgets.utils.TemporalLayersInLayerGroupDialog.Do-you-want-to-save-the-category-anyway") + "</HTML>");
            FormUtils.addRowInGBL(this.messagePanel, 0, 0, messageLabel);
        }
        return this.messagePanel;
    }

    public JPanel getTemporalLayerPanel() {
        if (this.temporalLayerPanel == null) {
            this.temporalLayerPanel = new JPanel(new GridBagLayout());
            this.temporalLayerPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.TemporalLayersInLayerGroupDialog.Temporal-layers-found")));
            this.temporalLayerTextArea = new JTextArea();
            this.temporalLayerTextArea.setFont(new JLabel().getFont());
            this.temporalLayerTextArea.setEditable(false);
            this.temporalLayerTextArea.setLineWrap(true);
            this.temporalLayerTextArea.setWrapStyleWord(true);
            this.temporalLayerTextArea.setColumns(40);
            this.temporalLayerTextArea.setRows(7);
            JScrollPane pane = new JScrollPane(this.temporalLayerTextArea, 20, 31);
            FormUtils.addRowInGBL(this.temporalLayerPanel, 0, 0, pane);
        }
        return this.temporalLayerPanel;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.utils.TemporalLayersInLayerGroupDialog.Save-category"));
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    TemporalLayersInLayerGroupDialog.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }
}

