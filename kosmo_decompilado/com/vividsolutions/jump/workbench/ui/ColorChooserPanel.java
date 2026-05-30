/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.saig.jump.lang.I18N;

public class ColorChooserPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    public static final Color DISABLED_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JButton changeButton = new JButton();
    JPanel outerColorPanel = new JPanel();
    ColorPanel colorPanel = new ColorPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    private Color color = Color.black;
    private int alpha;
    private List<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public ColorChooserPanel() {
        try {
            this.jbInit();
            this.colorPanel.setLineColor(null);
            this.changeButton.setToolTipText(I18N.getString("workbench.ui.ColorChooserPanel.browse"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(this.gridBagLayout1);
        this.changeButton.setMargin(new Insets(0, 0, 0, 0));
        this.changeButton.setText("   ...   ");
        this.changeButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ColorChooserPanel.this.changeButton_actionPerformed(e);
            }
        });
        this.outerColorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.outerColorPanel.setPreferredSize(new Dimension(60, 20));
        this.outerColorPanel.setBackground(Color.white);
        this.outerColorPanel.setLayout(this.gridBagLayout2);
        this.colorPanel.setMargin(0);
        this.colorPanel.setPreferredSize(new Dimension(45, 8));
        this.add((Component)this.changeButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 2, 0, 0), 0, 0));
        this.add((Component)this.outerColorPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.outerColorPanel.add((Component)this.colorPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    void changeButton_actionPerformed(ActionEvent e) {
        Color newColor = JColorChooser.showDialog(SwingUtilities.windowForComponent(this), I18N.getString("workbench.ui.ColorChooserPanel.choose-colour"), this.color);
        if (newColor == null) {
            return;
        }
        this.setColor(newColor);
        this.fireActionPerformed();
    }

    public void setColor(Color color) {
        this.color = color;
        this.updateColorPanel();
    }

    private void updateColorPanel() {
        this.colorPanel.setFillColor(GUIUtil.alphaColor(this.color, this.alpha));
        this.colorPanel.repaint();
    }

    public void addActionListener(ActionListener l) {
        this.actionListeners.add(l);
    }

    public void removeActionListener(ActionListener l) {
        this.actionListeners.remove(l);
    }

    protected void fireActionPerformed() {
        for (ActionListener l : this.actionListeners) {
            l.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    public Color getColor() {
        return this.color;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        this.updateColorPanel();
    }

    @Override
    public void setEnabled(boolean newEnabled) {
        this.changeButton.setEnabled(newEnabled);
        this.colorPanel.setEnabled(newEnabled);
    }
}

