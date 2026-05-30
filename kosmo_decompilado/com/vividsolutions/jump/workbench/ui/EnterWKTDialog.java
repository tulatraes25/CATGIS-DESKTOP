/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.plugin.WKTDisplayHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.saig.jump.lang.I18N;

public class EnterWKTDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    protected static final int THRESHOLD_WKT_LENGTH = 500;
    protected JPanel mainPanel = new JPanel();
    protected BorderLayout borderLayout1 = new BorderLayout();
    protected List<ActionListener> actionListeners = new ArrayList<ActionListener>();
    protected JLabel descriptionLabel = new JLabel();
    protected boolean syncing = false;
    private Timer annotationUpdateTimer = GUIUtil.createRestartableSingleEventTimer(500, new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent e) {
            EnterWKTDialog.this.updateAnnotations();
        }
    });
    private WKTDisplayHelper helper = new WKTDisplayHelper();
    private JSplitPane splitPane = new JSplitPane();
    private JScrollPane annotationScrollPane = new JScrollPane();
    private JEditorPane wktEditorPane = new JEditorPane(){
        private static final long serialVersionUID = 1L;

        @Override
        public void setText(String t) {
            super.setText(t);
            this.setCaretPosition(0);
        }
    };
    private JEditorPane annotationEditorPane = new JEditorPane();
    private JScrollPane mainScrollPane = new JScrollPane();
    private JPanel buttonPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JButton formatButton = new JButton();
    private OKCancelPanel okCancelPanel = new OKCancelPanel();

    public EnterWKTDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            this.jbInit();
            this.pack();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        this.wktEditorPane.getDocument().addDocumentListener(new DocumentListener(){

            @Override
            public void insertUpdate(DocumentEvent e) {
                EnterWKTDialog.this.queueAnnotationUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                EnterWKTDialog.this.queueAnnotationUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                EnterWKTDialog.this.queueAnnotationUpdate();
            }
        });
        this.mainScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                EnterWKTDialog.this.syncScrollBars();
            }
        });
        this.annotationScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                EnterWKTDialog.this.syncScrollBars();
            }
        });
    }

    public EnterWKTDialog() {
        this((Frame)null, "", true);
    }

    private void syncScrollBars() {
        if (this.syncing) {
            return;
        }
        this.syncing = true;
        try {
            this.annotationScrollPane.getVerticalScrollBar().setValue(Math.max(1, this.mainScrollPane.getVerticalScrollBar().getValue()));
        }
        finally {
            this.syncing = false;
        }
    }

    private void queueAnnotationUpdate() {
        if (this.wktEditorPane.getText().length() < 500) {
            this.updateAnnotations();
        } else {
            this.annotationUpdateTimer.restart();
        }
    }

    private void updateAnnotations() {
        this.annotationEditorPane.setText(this.helper.annotate(this.getText()));
    }

    public void setDescription(String d) {
        this.descriptionLabel.setText(d);
    }

    public void setEditable(boolean editable) {
        this.wktEditorPane.setEditable(editable);
        this.okCancelPanel.setCancelVisible(editable);
        this.wktEditorPane.setOpaque(editable);
        this.formatButton.setEnabled(editable);
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public void addActionListener(ActionListener l) {
        this.actionListeners.add(l);
    }

    void jbInit() throws Exception {
        this.formatButton.setToolTipText(I18N.getString("workbench.ui.EnterWKTDialog.beautify-the-well-known-text"));
        this.splitPane.setDividerSize(2);
        this.annotationEditorPane.setMargin(new Insets(0, 5, 0, 0));
        this.mainPanel.setLayout(this.borderLayout1);
        this.annotationScrollPane.setHorizontalScrollBarPolicy(31);
        this.annotationScrollPane.setVerticalScrollBarPolicy(21);
        this.annotationEditorPane.setBackground(Color.blue.darker());
        this.annotationEditorPane.setForeground(Color.white);
        this.annotationEditorPane.setEditable(false);
        this.annotationEditorPane.setContentType("text/html");
        this.wktEditorPane.setContentType("text/html");
        this.buttonPanel.setLayout(this.gridBagLayout1);
        this.formatButton.setText(I18N.getString("workbench.ui.EnterWKTDialog.format"));
        this.formatButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                EnterWKTDialog.this.formatButton_actionPerformed(e);
            }
        });
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                EnterWKTDialog.this.okCancelPanel_actionPerformed(e);
            }
        });
        this.getContentPane().add(this.mainPanel);
        this.mainPanel.add((Component)this.splitPane, "Center");
        this.splitPane.add((Component)this.annotationScrollPane, "left");
        this.splitPane.add((Component)this.mainScrollPane, "right");
        this.mainPanel.add((Component)this.buttonPanel, "South");
        this.buttonPanel.add((Component)this.formatButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(4, 4, 4, 4), 0, 0));
        this.mainScrollPane.getViewport().add((Component)this.wktEditorPane, null);
        this.annotationScrollPane.getViewport().add((Component)this.annotationEditorPane, null);
        this.getContentPane().add((Component)this.descriptionLabel, "North");
        this.buttonPanel.add((Component)this.okCancelPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
        this.splitPane.setDividerLocation(75);
    }

    public String getText() {
        return this.helper.getCleanWKT(this.wktEditorPane.getText());
    }

    public void setText(String text) {
        this.wktEditorPane.setText(this.helper.format(text));
    }

    void okCancelPanel_actionPerformed(ActionEvent e) {
        for (ActionListener l : this.actionListeners) {
            l.actionPerformed(e);
        }
    }

    void formatButton_actionPerformed(ActionEvent e) {
        this.wktEditorPane.setText(this.helper.format(this.getText()));
    }
}

