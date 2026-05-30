/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.ColorButton;
import org.saig.jump.widgets.print.elements.ColorChooserDialog;

public class TramePanel
extends JPanel {
    private JPanel stylePanel = new JPanel();
    private TrameColorPanel trameColorPanel;
    private CheckboxGroup cbg = new CheckboxGroup();
    private Checkbox transparent = new Checkbox(I18N.getString("org.saig.jump.widgets.print.elements.TramePanel.transparent"), this.cbg, true);
    private Checkbox opaque = new Checkbox(I18N.getString("org.saig.jump.widgets.print.elements.TramePanel.opaque"), this.cbg, false);
    private JLabel previewLabel = new JLabel();
    private JFrame owner;

    public TramePanel(JFrame owner, final JLabel previewLabel) {
        this.owner = owner;
        this.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.TramePanel.trame")));
        this.previewLabel = previewLabel;
        this.trameColorPanel = new TrameColorPanel();
        this.stylePanel.setPreferredSize(new Dimension(400, 20));
        this.stylePanel.setLayout(new GridLayout(1, 2));
        this.stylePanel.add(this.transparent);
        this.transparent.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                previewLabel.setOpaque(false);
                TramePanel.this.trameColorPanel.setEnabled(previewLabel.isOpaque());
            }
        });
        this.stylePanel.add(this.opaque);
        this.opaque.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                previewLabel.setOpaque(true);
                TramePanel.this.trameColorPanel.setEnabled(previewLabel.isOpaque());
            }
        });
        this.setLayout(new GridBagLayout());
        this.add((Component)this.stylePanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.trameColorPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.initTrame();
    }

    private void initTrame() {
        if (this.previewLabel.isOpaque()) {
            this.opaque.setState(true);
            this.setEnabled(true);
            this.trameColorPanel.setEnabled(true);
            this.trameColorPanel.color.setBackground(this.previewLabel.getBackground());
        } else {
            this.opaque.setState(false);
            this.trameColorPanel.setEnabled(false);
            this.setEnabled(false);
        }
    }

    private class TrameColorPanel
    extends JPanel {
        private JLabel borderColor = new JLabel(I18N.getString("org.saig.jump.widgets.print.elements.TramePanel.color"));
        private JPanel color = new JPanel();
        private ColorButton trameColorButton = new ColorButton(I18N.getString("org.saig.jump.widgets.print.elements.TramePanel.color-palette"));

        public TrameColorPanel() {
            this.color.setOpaque(true);
            this.color.setBorder(BorderFactory.createEtchedBorder(0));
            this.color.setPreferredSize(new Dimension(160, 20));
            this.color.setBackground(TramePanel.this.previewLabel.getBackground());
            this.color.addPropertyChangeListener(new PropertyChangeListener(){

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    TramePanel.this.previewLabel.setBackground(TrameColorPanel.this.color.getBackground());
                }
            });
            this.trameColorButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    new ColorChooserDialog(TramePanel.this.owner, I18N.getString("org.saig.jump.widgets.print.elements.TramePanel.select-trame-color"), TrameColorPanel.this.color);
                }
            });
            this.setPreferredSize(new Dimension(400, 30));
            this.setLayout(new GridBagLayout());
            this.add((Component)this.borderColor, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 13, 0, new Insets(0, 0, 0, 5), 0, 0));
            this.add((Component)this.color, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 13, 0, new Insets(0, 0, 0, 0), 0, 0));
            this.add((Component)this.trameColorButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, 13, 0, new Insets(0, 0, 0, 0), 0, 0));
            this.setEnabled(false);
        }

        @Override
        public void setEnabled(boolean isEnabled) {
            super.setEnabled(isEnabled);
            this.borderColor.setEnabled(isEnabled);
            this.color.setEnabled(isEnabled);
            this.trameColorButton.setEnabled(isEnabled);
        }
    }
}

