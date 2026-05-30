/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
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
import javax.swing.border.LineBorder;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.ColorButton;
import org.saig.jump.widgets.print.elements.ColorChooserDialog;

public class BorderPanel
extends JPanel {
    private JLabel previewLabel = new JLabel();
    private JPanel borderPanel = new JPanel();
    private JPanel choisePanel = new JPanel();
    private CheckboxGroup borderChoise = new CheckboxGroup();
    private Checkbox yesCheckBox = new Checkbox(I18N.getString("org.saig.jump.widgets.print.elements.BorderPanel.yes"), this.borderChoise, false);
    private Checkbox noCheckBox = new Checkbox(I18N.getString("org.saig.jump.widgets.print.elements.BorderPanel.no"), this.borderChoise, true);
    private JLabel borderColor = new JLabel(I18N.getString("org.saig.jump.widgets.print.elements.BorderPanel.border-color"));
    private JPanel color = new JPanel();
    private ColorButton borderColorButton = new ColorButton(I18N.getString("org.saig.jump.widgets.print.elements.BorderPanel.color-palette"));
    private JPanel borderThicknessPanel = new JPanel();
    private CheckboxGroup borderThickness = new CheckboxGroup();
    private Checkbox oneCheckBox = new Checkbox("1", this.borderThickness, true);
    private Checkbox twoCheckBox = new Checkbox("2", this.borderThickness, false);
    private Checkbox threeCheckBox = new Checkbox("3", this.borderThickness, false);
    private Checkbox fourCheckBox = new Checkbox("4", this.borderThickness, false);
    private Checkbox fiveCheckBox = new Checkbox("5", this.borderThickness, false);
    private Checkbox sixCheckBox = new Checkbox("6", this.borderThickness, false);
    private JFrame owner;

    public BorderPanel(final JFrame owner, final JLabel previewLabel) {
        this.owner = owner;
        this.previewLabel = previewLabel;
        this.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.BorderPanel.border")));
        this.choisePanel.setPreferredSize(new Dimension(400, 20));
        this.choisePanel.setLayout(new GridLayout(1, 2));
        this.choisePanel.add(this.yesCheckBox);
        this.yesCheckBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                BorderPanel.this.setEnabled(true);
            }
        });
        this.choisePanel.add(this.noCheckBox);
        this.noCheckBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                BorderPanel.this.setEnabled(false);
                previewLabel.setBorder(null);
            }
        });
        this.color.setOpaque(true);
        this.color.setBorder(BorderFactory.createEtchedBorder(0));
        this.color.setPreferredSize(new Dimension(160, 20));
        this.color.setBackground(Color.WHITE);
        this.color.addPropertyChangeListener(new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                previewLabel.setBorder(BorderFactory.createLineBorder(BorderPanel.this.color.getBackground(), Integer.valueOf(BorderPanel.this.borderThickness.getSelectedCheckbox().getLabel())));
            }
        });
        this.borderColorButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                new ColorChooserDialog(owner, I18N.getString("org.saig.jump.widgets.print.elements.BorderPanel.select-border-color"), BorderPanel.this.color);
            }
        });
        this.borderThicknessPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.BorderPanel.border-thickness")));
        this.borderThicknessPanel.setLayout(new GridLayout(1, 6));
        this.borderThicknessPanel.add(this.oneCheckBox);
        this.oneCheckBox.addItemListener(new ItemChange());
        this.borderThicknessPanel.add(this.twoCheckBox);
        this.twoCheckBox.addItemListener(new ItemChange());
        this.borderThicknessPanel.add(this.threeCheckBox);
        this.threeCheckBox.addItemListener(new ItemChange());
        this.borderThicknessPanel.add(this.fourCheckBox);
        this.fourCheckBox.addItemListener(new ItemChange());
        this.borderThicknessPanel.add(this.fiveCheckBox);
        this.fiveCheckBox.addItemListener(new ItemChange());
        this.borderThicknessPanel.add(this.sixCheckBox);
        this.sixCheckBox.addItemListener(new ItemChange());
        this.borderThicknessPanel.setPreferredSize(new Dimension(395, 45));
        this.borderPanel.setPreferredSize(new Dimension(400, 70));
        this.borderPanel.setLayout(new GridBagLayout());
        this.borderPanel.add((Component)this.borderColor, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.borderPanel.add((Component)this.color, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.borderPanel.add((Component)this.borderColorButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.borderPanel.add((Component)this.borderThicknessPanel, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.setLayout(new GridBagLayout());
        this.add((Component)this.choisePanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.borderPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.initBorder();
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.borderPanel.setEnabled(isEnabled);
        this.color.setEnabled(isEnabled);
        this.borderColor.setEnabled(isEnabled);
        this.borderColorButton.setEnabled(isEnabled);
        this.borderThicknessPanel.setEnabled(isEnabled);
        this.oneCheckBox.setEnabled(isEnabled);
        this.twoCheckBox.setEnabled(isEnabled);
        this.threeCheckBox.setEnabled(isEnabled);
        this.fourCheckBox.setEnabled(isEnabled);
        this.fiveCheckBox.setEnabled(isEnabled);
        this.sixCheckBox.setEnabled(isEnabled);
    }

    private void initBorder() {
        if (this.previewLabel.getBorder() instanceof LineBorder) {
            this.yesCheckBox.setState(true);
            this.setEnabled(true);
            LineBorder border = (LineBorder)this.previewLabel.getBorder();
            this.color.setBackground(border.getLineColor());
            switch (border.getThickness()) {
                case 2: {
                    this.twoCheckBox.setState(true);
                    break;
                }
                case 3: {
                    this.threeCheckBox.setState(true);
                    break;
                }
                case 4: {
                    this.fourCheckBox.setState(true);
                    break;
                }
                case 5: {
                    this.fiveCheckBox.setState(true);
                    break;
                }
                case 6: {
                    this.sixCheckBox.setState(true);
                    break;
                }
                default: {
                    this.oneCheckBox.setState(true);
                    break;
                }
            }
        } else if (this.previewLabel.getBorder() == null) {
            this.noCheckBox.setState(true);
            this.setEnabled(false);
            this.previewLabel.setBorder(null);
        }
    }

    private class ItemChange
    implements ItemListener {
        private ItemChange() {
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            BorderPanel.this.previewLabel.setBorder(BorderFactory.createLineBorder(BorderPanel.this.color.getBackground(), Integer.valueOf(BorderPanel.this.borderThickness.getSelectedCheckbox().getLabel())));
        }
    }
}

