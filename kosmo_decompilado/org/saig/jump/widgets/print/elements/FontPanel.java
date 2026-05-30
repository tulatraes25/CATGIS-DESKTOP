/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.ColorButton;
import org.saig.jump.widgets.print.elements.ColorChooserDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class FontPanel
extends JPanel {
    private JPanel fontPanel = new JPanel();
    private JLabel name = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.font-name")) + ": ");
    private JLabel size = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.font-size")) + ": ");
    private JPanel color = new JPanel();
    private JLabel fontColor = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.font-color")) + ": ");
    private ColorButton fontColorButton = new ColorButton(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.color-palette"));
    private JPanel fontStyle = new JPanel();
    private JComboBox fontNameComboBox = new JComboBox();
    private JTextField fontSizeTextField = new JTextField(2);
    private JCheckBox boldCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.bold"));
    private JCheckBox underlineCheckBox = new JCheckBox("<html><u>" + I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.underline") + "</u></html>");
    private JCheckBox italicCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.italic"));
    private JLabel previewLabel;
    private JFrame owner;

    public FontPanel(JFrame owner, JLabel font) {
        this.previewLabel = font;
        this.owner = owner;
        this.initFontNameComboBox();
        this.initFontSize();
        this.initFontStyle();
        this.initFontColor();
        this.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.font-panel")));
        this.fontPanel.setLayout(new GridBagLayout());
        this.fontPanel.add((Component)this.name, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.fontPanel.add((Component)this.fontNameComboBox, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.fontPanel.add((Component)this.size, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.fontPanel.add((Component)this.fontSizeTextField, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.fontPanel.add((Component)this.fontColor, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.fontPanel.add((Component)this.color, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.fontPanel.add((Component)this.fontColorButton, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.fontPanel.add((Component)this.fontStyle, new GridBagConstraints(4, 1, 1, 3, 0.0, 0.0, 13, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.setLayout(new GridBagLayout());
        this.add((Component)this.fontPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void initFontNameComboBox() {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        int i = 0;
        while (i < fonts.length) {
            this.fontNameComboBox.addItem(fonts[i]);
            ++i;
        }
        this.fontNameComboBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                Font fuente = new Font(FontPanel.this.fontNameComboBox.getSelectedItem().toString(), FontPanel.this.previewLabel.getFont().getStyle(), FontPanel.this.previewLabel.getFont().getSize());
                FontPanel.this.previewLabel.setFont(fuente);
            }
        });
        this.fontNameComboBox.setSelectedItem(this.previewLabel.getFont().getName());
    }

    private void initFontSize() {
        this.fontSizeTextField.setText(Integer.toString(this.previewLabel.getFont().getSize()));
        this.fontSizeTextField.addFocusListener(new FontSizeFocusListener(this));
    }

    private void initFontStyle() {
        this.boldCheckBox.setFont(new Font(this.boldCheckBox.getFont().getName(), 1, this.boldCheckBox.getFont().getSize()));
        this.italicCheckBox.setFont(new Font(this.italicCheckBox.getFont().getName(), 2, this.italicCheckBox.getFont().getSize()));
        if (this.previewLabel.getFont().isBold()) {
            this.boldCheckBox.setSelected(true);
        }
        if (this.previewLabel.getFont().isItalic()) {
            this.italicCheckBox.setSelected(true);
        }
        if (this.isUnderlined(this.previewLabel.getText())) {
            this.underlineCheckBox.setSelected(true);
        }
        this.fontStyle.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.font-style")));
        this.fontStyle.setLayout(new GridLayout(3, 1));
        this.fontStyle.add(this.boldCheckBox);
        this.boldCheckBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                Font fuente = null;
                if (FontPanel.this.boldCheckBox.isSelected()) {
                    fuente = new Font(FontPanel.this.previewLabel.getFont().getName(), FontPanel.this.previewLabel.getFont().getStyle() + 1, FontPanel.this.previewLabel.getFont().getSize());
                    FontPanel.this.previewLabel.setFont(fuente);
                } else {
                    fuente = new Font(FontPanel.this.previewLabel.getFont().getName(), FontPanel.this.previewLabel.getFont().getStyle() - 1, FontPanel.this.previewLabel.getFont().getSize());
                    FontPanel.this.previewLabel.setFont(fuente);
                }
            }
        });
        this.fontStyle.add(this.underlineCheckBox);
        this.underlineCheckBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (FontPanel.this.underlineCheckBox.isSelected()) {
                    FontPanel.this.previewLabel.setText("<html><u>" + FontPanel.this.previewLabel.getText() + "</u></html>");
                } else {
                    String ch = FontPanel.this.previewLabel.getText().replaceAll("(?i)<html><u>", "");
                    FontPanel.this.previewLabel.setText(ch.replaceAll("(?i)</u></html>", ""));
                }
            }
        });
        this.fontStyle.add(this.italicCheckBox);
        this.italicCheckBox.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                Font fuente = null;
                if (FontPanel.this.italicCheckBox.isSelected()) {
                    fuente = new Font(FontPanel.this.previewLabel.getFont().getName(), FontPanel.this.previewLabel.getFont().getStyle() + 2, FontPanel.this.previewLabel.getFont().getSize());
                    FontPanel.this.previewLabel.setFont(fuente);
                } else {
                    fuente = new Font(FontPanel.this.previewLabel.getFont().getName(), FontPanel.this.previewLabel.getFont().getStyle() - 2, FontPanel.this.previewLabel.getFont().getSize());
                    FontPanel.this.previewLabel.setFont(fuente);
                }
            }
        });
    }

    private void initFontColor() {
        this.color.setBorder(BorderFactory.createEtchedBorder(0));
        this.color.setOpaque(true);
        this.color.setPreferredSize(new Dimension(160, 20));
        this.color.setBackground(this.previewLabel.getForeground());
        this.color.addPropertyChangeListener(new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                FontPanel.this.previewLabel.setForeground(FontPanel.this.color.getBackground());
            }
        });
        this.fontColorButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                new ColorChooserDialog(FontPanel.this.owner, I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.select-font-color"), FontPanel.this.color);
            }
        });
    }

    public JLabel getPreviewLabel() {
        return this.previewLabel;
    }

    public void setPreviewLabel(JLabel apercu) {
        this.previewLabel = apercu;
    }

    private boolean isUnderlined(String text) {
        return text.toUpperCase().lastIndexOf("<HTML><U>") != -1 && text.toUpperCase().lastIndexOf("</U></HTML") != -1;
    }

    private boolean isUpperCase(String text) {
        return text.equals(text.toUpperCase());
    }

    private class FontSizeFocusListener
    implements FocusListener {
        private FontPanel owner;

        public FontSizeFocusListener(FontPanel panel) {
            this.owner = panel;
        }

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent e) {
            try {
                int fontSize = Integer.valueOf(FontPanel.this.fontSizeTextField.getText());
                FontPanel.this.previewLabel.setFont(new Font(FontPanel.this.previewLabel.getFont().getName(), FontPanel.this.previewLabel.getFont().getStyle(), fontSize));
            }
            catch (NumberFormatException e1) {
                FontPanel.this.fontSizeTextField.setText(String.valueOf(FontPanel.this.previewLabel.getFont().getSize()));
                DialogFactory.showErrorDialog(this.owner, I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.font-size-number-format-is-incorrect"), I18N.getString("org.saig.jump.widgets.print.elements.FontPanel.font-size-error"));
            }
        }
    }
}

