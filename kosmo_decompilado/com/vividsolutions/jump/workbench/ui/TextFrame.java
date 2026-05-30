/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class TextFrame
extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    BorderLayout borderLayout1 = new BorderLayout();
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    protected JPanel scrollPanePanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane();
    GridBagLayout gridBagLayout = new GridBagLayout();
    private JEditorPane editorPane = new JEditorPane();
    private ErrorHandler errorHandler;

    public TextFrame(ErrorHandler errorHandler) {
        this(false, errorHandler);
    }

    public TextFrame(boolean showingButtons, ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        try {
            this.jbInit();
            this.okCancelPanel.setVisible(showingButtons);
        }
        catch (Exception e) {
            errorHandler.handleThrowable(e);
        }
    }

    public OKCancelPanel getOKCancelPanel() {
        return this.okCancelPanel;
    }

    private void jbInit() throws Exception {
        this.getContentPane().setLayout(this.borderLayout1);
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
        this.setSize(500, 300);
        this.scrollPanePanel.setLayout(this.gridBagLayout);
        this.editorPane.setBackground(UIManager.getColor("inactiveCaptionBorder"));
        this.editorPane.setText("jEditorPane1");
        this.editorPane.setContentType("text/html");
        this.getContentPane().add((Component)this.getOKCancelPanel(), "South");
        this.getContentPane().add((Component)this.scrollPanePanel, "Center");
        this.scrollPanePanel.add((Component)this.scrollPane, new GridBagConstraints(0, 0, 0, 1, 1.0, 1.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.scrollPane.getViewport().add((Component)this.editorPane, null);
    }

    public void clear() {
        this.setText("");
    }

    public void setText(String s) {
        try {
            this.editorPane.setText(s);
            this.editorPane.setCaretPosition(0);
        }
        catch (Throwable t) {
            System.out.println(s);
            this.errorHandler.handleThrowable(t);
        }
    }

    public String getText() {
        return this.editorPane.getText();
    }
}

