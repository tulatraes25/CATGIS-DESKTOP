/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class ColorSchemeListCellRenderer
extends JPanel
implements ListCellRenderer {
    private static final long serialVersionUID = 1L;
    private ColorPanel colorPanel1 = new ColorPanel();
    private ColorPanel colorPanel2 = new ColorPanel();
    private ColorPanel colorPanel3 = new ColorPanel();
    private ColorPanel colorPanel4 = new ColorPanel();
    private ColorPanel colorPanel5 = new ColorPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel label = new JLabel();

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String name = (String)value;
        List<Color> colors = this.colorScheme(name).getColors();
        Iterator<Color> i = CollectionUtil.stretch(colors, new ArrayList(), 5).iterator();
        this.label.setText("(" + colors.size() + ") " + name);
        this.color(this.colorPanel1, i.next());
        if (i.hasNext()) {
            this.color(this.colorPanel2, i.next());
            this.colorPanel2.setVisible(true);
        } else {
            this.colorPanel2.setVisible(false);
        }
        if (i.hasNext()) {
            this.color(this.colorPanel3, i.next());
            this.colorPanel3.setVisible(true);
        } else {
            this.colorPanel3.setVisible(false);
        }
        if (i.hasNext()) {
            this.color(this.colorPanel4, i.next());
            this.colorPanel4.setVisible(true);
        } else {
            this.colorPanel4.setVisible(false);
        }
        if (i.hasNext()) {
            this.color(this.colorPanel5, i.next());
            this.colorPanel5.setVisible(true);
        } else {
            this.colorPanel5.setVisible(false);
        }
        if (isSelected) {
            this.label.setForeground(list.getSelectionForeground());
            this.label.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
            this.setBackground(list.getSelectionBackground());
        } else {
            this.label.setForeground(list.getForeground());
            this.label.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
            this.setBackground(list.getBackground());
        }
        return this;
    }

    protected ColorScheme colorScheme(String name) {
        return ColorScheme.create(name);
    }

    private void color(ColorPanel colorPanel, Color fillColor) {
        this.color(colorPanel, fillColor, Layer.defaultLineColor(fillColor));
    }

    protected void color(ColorPanel colorPanel, Color fillColor, Color lineColor) {
        colorPanel.setFillColor(fillColor);
        colorPanel.setLineColor(lineColor);
    }

    public ColorSchemeListCellRenderer() {
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(this.gridBagLayout1);
        this.label.setText("jLabel1");
        this.add((Component)this.colorPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 2, 0, 0), 0, 0));
        this.add((Component)this.colorPanel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.colorPanel3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.colorPanel4, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.colorPanel5, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.label, new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0, 10, 2, new Insets(0, 2, 0, 0), 0, 0));
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        this.validate();
    }
}

