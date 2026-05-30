/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.project;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class TemporalLayersInProjectDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String ONLY_LAYERS_TITLE = I18N.getString("org.saig.jump.widgets.util.project.TemporalLayersInProjectDialog.Temporal-layers");
    private static final String ONLY_TABLES_TITLE = I18N.getString(TemporalLayersInProjectDialog.class, "temporal-tables");
    private static final String LAYERS_AND_TABLES_TITLE = I18N.getString(TemporalLayersInProjectDialog.class, "temporal-layers-and-tables");
    private JPanel messagePanel;
    private JPanel temporalLayerPanel;
    private JTextArea temporalLayerTextArea;
    private JPanel temporalTablePanel;
    private JTextArea temporalTableTextArea;
    private OKCancelPanel okCancelPanel;
    List<String> tempLayerNames = null;
    List<String> tempTableNames = null;

    public TemporalLayersInProjectDialog(JFrame owner, boolean modal, List<String> temporalLayerNames, List<String> temporalTableNames) {
        super((Frame)owner, modal);
        this.tempLayerNames = temporalLayerNames;
        this.tempTableNames = temporalTableNames;
        if (!temporalLayerNames.isEmpty() && !temporalTableNames.isEmpty()) {
            this.setTitle(LAYERS_AND_TABLES_TITLE);
        } else if (!temporalLayerNames.isEmpty()) {
            this.setTitle(ONLY_LAYERS_TITLE);
        } else {
            this.setTitle(ONLY_TABLES_TITLE);
        }
        this.initialize();
        if (!this.tempLayerNames.isEmpty()) {
            this.addTemporalLayerNames();
        }
        if (!this.tempTableNames.isEmpty()) {
            this.addTemporalTableNames();
        }
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

    private void addTemporalTableNames() {
        StringBuffer buffer = new StringBuffer();
        Collections.sort(this.tempTableNames);
        for (String currentName : this.tempTableNames) {
            buffer.append("* " + currentName + "\n");
        }
        this.temporalTableTextArea.setText(buffer.toString());
        this.temporalTableTextArea.setCaretPosition(0);
    }

    private void initialize() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getMessagePanel());
        if (!this.tempLayerNames.isEmpty()) {
            FormUtils.addRowInGBL(mainPanel, 1, 0, this.getTemporalLayerPanel());
        }
        if (!this.tempTableNames.isEmpty()) {
            FormUtils.addRowInGBL(mainPanel, 2, 0, this.getTemporalTablePanel());
        }
        FormUtils.addRowInGBL(mainPanel, 3, 0, this.getOkCancelPanel());
    }

    public JPanel getMessagePanel() {
        if (this.messagePanel == null) {
            this.messagePanel = new JPanel(new GridBagLayout());
            this.messagePanel.setBorder(BorderFactory.createTitledBorder(this.getTitle()));
            JLabel messageLabel = new JLabel("<HTML>" + I18N.getString("org.saig.jump.widgets.util.project.TemporalLayersInProjectDialog.The-project-that-you-want-to-save-have-some-temporal-layers-that-will-not-be-saved") + "<BR>" + I18N.getString("org.saig.jump.widgets.util.project.TemporalLayersInProjectDialog.Do-you-want-to-save-the-project-anyway") + "</HTML>");
            FormUtils.addRowInGBL(this.messagePanel, 0, 0, messageLabel);
        }
        return this.messagePanel;
    }

    public JPanel getTemporalLayerPanel() {
        if (this.temporalLayerPanel == null) {
            this.temporalLayerPanel = new JPanel(new GridBagLayout());
            this.temporalLayerPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.project.TemporalLayersInProjectDialog.Temporal-layers-found")));
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

    public JPanel getTemporalTablePanel() {
        if (this.temporalTablePanel == null) {
            this.temporalTablePanel = new JPanel(new GridBagLayout());
            this.temporalTablePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "found-temporal-tables")));
            this.temporalTableTextArea = new JTextArea();
            this.temporalTableTextArea.setFont(new JLabel().getFont());
            this.temporalTableTextArea.setEditable(false);
            this.temporalTableTextArea.setLineWrap(true);
            this.temporalTableTextArea.setWrapStyleWord(true);
            this.temporalTableTextArea.setColumns(40);
            this.temporalTableTextArea.setRows(7);
            JScrollPane pane = new JScrollPane(this.temporalTableTextArea, 20, 31);
            FormUtils.addRowInGBL(this.temporalTablePanel, 0, 0, pane);
        }
        return this.temporalTablePanel;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.util.project.TemporalLayersInProjectDialog.Save-project"));
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    TemporalLayersInProjectDialog.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }
}

