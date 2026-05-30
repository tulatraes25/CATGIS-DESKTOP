/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.plugin.Configuration;
import com.vividsolutions.jump.workbench.plugin.PlugInManager;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ExtensionsAboutPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JScrollPane scrollPane = new JScrollPane();
    private JEditorPane editorPane = new JEditorPane();

    public ExtensionsAboutPanel() {
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPlugInManager(PlugInManager plugInManager) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><head></head><body>");
        for (Configuration configuration : plugInManager.getConfigurations()) {
            sb.append("<b>" + GUIUtil.escapeHTML(PlugInManager.name(configuration), false) + "</b> " + GUIUtil.escapeHTML(PlugInManager.version(configuration), false) + "<br>");
        }
        sb.append("</body></html>");
        this.editorPane.setText(sb.toString());
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.editorPane.setEditable(false);
        this.editorPane.setOpaque(false);
        this.editorPane.setText("jEditorPane1");
        this.editorPane.setContentType("text/html");
        this.add((Component)this.scrollPane, "Center");
        this.scrollPane.getViewport().add((Component)this.editorPane, null);
    }
}

