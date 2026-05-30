/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Component;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class TableFrame
extends JInternalFrame {
    private JScrollPane scrollPane = new JScrollPane();
    private DefaultTableModel model = new DefaultTableModel();
    private JTable table = new JTable(this.model);

    public TableFrame() {
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.setSize(400, 200);
    }

    public DefaultTableModel getModel() {
        return this.model;
    }

    private void jbInit() throws Exception {
        this.getContentPane().add((Component)this.scrollPane, "Center");
        this.scrollPane.getViewport().add((Component)this.table, null);
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
    }
}

