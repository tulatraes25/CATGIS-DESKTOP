/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class TransparencyPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JPanel opaquePanel = new JPanel();
    private JPanel transparentPanel = new JPanel();
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private JSlider transparencySlider = new JSlider();

    public TransparencyPanel() {
        this.transparencySlider.setMaximum(255);
        this.transparencySlider.setPreferredSize(new Dimension(100, 24));
        this.setLayout(this.gridBagLayout3);
        this.add((Component)this.transparencySlider, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.transparentPanel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.opaquePanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.opaquePanel.setBackground(Color.black);
        this.opaquePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.opaquePanel.setMinimumSize(new Dimension(11, 11));
        this.opaquePanel.setMaximumSize(new Dimension(11, 11));
        this.opaquePanel.setPreferredSize(new Dimension(11, 11));
        this.transparentPanel.setBackground(Color.white);
        this.transparentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.transparentPanel.setForeground(Color.white);
        this.transparentPanel.setMaximumSize(new Dimension(11, 11));
        this.transparentPanel.setMinimumSize(new Dimension(11, 11));
        this.transparentPanel.setPreferredSize(new Dimension(11, 11));
    }

    public void setColor(Color color) {
        this.opaquePanel.setBackground(color);
    }

    public JSlider getSlider() {
        return this.transparencySlider;
    }
}

