/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class SplashPanel
extends JPanel {
    private GridBagLayout gridBagLayout = new GridBagLayout();
    private JLabel captionLabel = new JLabel();
    private JLabel imageLabel = new JLabel();
    private Border border1;
    private Border border2;

    public SplashPanel(Icon image, String caption) {
        try {
            this.jbInit();
            this.imageLabel.setIcon(image);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.border2 = BorderFactory.createBevelBorder(0, Color.white, Color.white, new Color(103, 101, 98), new Color(148, 145, 140));
        CompoundBorder compoundBorder = new CompoundBorder(BorderFactory.createLineBorder(Color.black), this.border2);
        this.setLayout(this.gridBagLayout);
        this.captionLabel.setFont(new Font("Dialog", 1, 16));
        this.captionLabel.setForeground(Color.blue);
        this.captionLabel.setBorder(this.border1);
        this.captionLabel.setText("");
        this.captionLabel.setHorizontalAlignment(4);
        this.setBorder(compoundBorder);
        this.add((Component)this.imageLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, 18, 1, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.captionLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 13, 0, new Insets(0, 0, 0, 10), 0, 0));
    }
}

