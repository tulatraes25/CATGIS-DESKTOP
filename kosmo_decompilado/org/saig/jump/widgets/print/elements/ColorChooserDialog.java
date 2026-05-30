/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorChooserDialog
extends JDialog {
    private JColorChooser colorChooser;
    private JPanel colorChooserPanel = new JPanel();
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private Color selectedColor;

    public ColorChooserDialog(JFrame owner, String title, final Component component) {
        super((Frame)owner, true);
        this.setTitle(title);
        this.colorChooser = new JColorChooser(component.getBackground());
        this.colorChooser.getSelectionModel().addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                ColorChooserDialog.this.selectedColor = ColorChooserDialog.this.colorChooser.getColor();
            }
        });
        this.colorChooserPanel.add(this.colorChooser);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add((Component)this.colorChooserPanel, "North");
        this.getContentPane().add((Component)this.okCancelPanel, "South");
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (ColorChooserDialog.this.okCancelPanel.wasOKPressed()) {
                    component.setBackground(ColorChooserDialog.this.selectedColor);
                    ColorChooserDialog.this.termine();
                } else {
                    ColorChooserDialog.this.termine();
                }
            }
        });
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private void termine() {
        this.dispose();
    }
}

