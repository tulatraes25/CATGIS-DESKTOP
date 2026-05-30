/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class BasicStyleListCellRenderer
implements ListCellRenderer {
    protected ColorPanel colorPanel = new ColorPanel();
    private JPanel panel = new JPanel();
    protected DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer(){
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("Dialog", 0, 10));
            return label;
        }
    };
    private int alpha;

    public BasicStyleListCellRenderer() {
        this.panel.setLayout(new GridBagLayout());
        this.panel.add((Component)this.colorPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.setColorPanelSize(new Dimension(45, 8));
        this.alpha = 255;
    }

    protected void setColorPanelSize(Dimension d) {
        this.colorPanel.setMinimumSize(d);
        this.colorPanel.setMaximumSize(d);
        this.colorPanel.setPreferredSize(d);
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof String) {
            return this.defaultListCellRenderer.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        }
        BasicStyle basicStyle = (BasicStyle)value;
        this.colorPanel.setLineWidth(Math.min(3, basicStyle.getLineWidth()));
        this.colorPanel.setLineColor(basicStyle.isRenderingLine() ? GUIUtil.alphaColor(basicStyle.getLineColor(), this.alpha) : (isSelected ? list.getSelectionBackground() : list.getBackground()));
        this.colorPanel.setFillColor(basicStyle.isRenderingFill() ? GUIUtil.alphaColor(basicStyle.getFillColor(), this.alpha) : (isSelected ? list.getSelectionBackground() : list.getBackground()));
        if (isSelected) {
            this.colorPanel.setForeground(list.getSelectionForeground());
            this.colorPanel.setBackground(list.getSelectionBackground());
            this.panel.setForeground(list.getSelectionForeground());
            this.panel.setBackground(list.getSelectionBackground());
        } else {
            this.colorPanel.setForeground(list.getForeground());
            this.colorPanel.setBackground(list.getBackground());
            this.panel.setForeground(list.getForeground());
            this.panel.setBackground(list.getBackground());
        }
        return this.panel;
    }
}

