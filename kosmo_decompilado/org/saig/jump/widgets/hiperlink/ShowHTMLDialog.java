/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.hiperlink;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class ShowHTMLDialog
extends JDialog {
    public ShowHTMLDialog(JFrame parent, boolean modal, JEditorPane textArea, String title) {
        super((Frame)parent, modal);
        this.setTitle(title);
        this.getContentPane().setLayout(new BorderLayout());
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        this.getContentPane().add((Component)scroller, "Center");
        this.setSize(600, 400);
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }
}

