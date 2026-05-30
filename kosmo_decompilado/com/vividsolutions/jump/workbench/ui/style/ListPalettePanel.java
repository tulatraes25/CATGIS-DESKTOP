/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyleListCellRenderer;
import com.vividsolutions.jump.workbench.ui.style.AbstractPalettePanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListPalettePanel
extends AbstractPalettePanel {
    private JList list;
    private BasicStyleListCellRenderer basicStyleListCellRenderer = new BasicStyleListCellRenderer();
    public DefaultListCellRenderer testRenderer = new DefaultListCellRenderer(){

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent((JList<?>)list, "test", index, isSelected, cellHasFocus);
        }
    };

    public ListPalettePanel(int verticalScrollBarPolicy) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(verticalScrollBarPolicy);
        final JList list = new JList(new Vector(this.basicStyles()));
        this.setLayout(new BorderLayout());
        this.add((Component)scrollPane, "Center");
        scrollPane.getViewport().add(list);
        list.setCellRenderer(this.basicStyleListCellRenderer);
        list.setSelectionMode(0);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListPalettePanel.this.fireBasicStyleChosen((BasicStyle)list.getSelectedValue());
            }
        });
        this.list = list;
    }

    @Override
    public void setAlpha(int alpha) {
        this.basicStyleListCellRenderer.setAlpha(alpha);
        this.repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.list.setEnabled(false);
    }
}

