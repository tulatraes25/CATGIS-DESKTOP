/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.swing.JFontChooser
 */
package org.saig.jump.widgets.util;

import com.l2fprod.common.swing.JFontChooser;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.saig.jump.lang.I18N;

public class JFontChooserPanel
extends JPanel
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JTextField fontNameTextField;
    private JTextField fontSizeTextField;
    private JTextField fontStyleTextField;
    private JButton changeFontButton;
    private Font selectedFont;

    public JFontChooserPanel(Font startingFont) {
        this.selectedFont = startingFont;
        this.initialize();
        this.refresh();
    }

    private void initialize() {
        this.setLayout(new FlowLayout());
        JLabel fontNameLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Font")) + ":");
        this.fontNameTextField = new JTextField();
        this.fontNameTextField.setEditable(false);
        JLabel fontSizeLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Size")) + ":");
        this.fontSizeTextField = new JTextField();
        this.fontSizeTextField.setEditable(false);
        JLabel fontStyleLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Style")) + ":");
        this.fontStyleTextField = new JTextField();
        this.fontStyleTextField.setEditable(false);
        this.changeFontButton = new JButton(String.valueOf(I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Change")) + "...");
        this.changeFontButton.addActionListener(this);
        Dimension minDim = new Dimension(100, this.fontNameTextField.getPreferredSize().height);
        this.fontNameTextField.setMinimumSize(minDim);
        this.fontNameTextField.setPreferredSize(minDim);
        this.fontSizeTextField.setMinimumSize(minDim);
        this.fontSizeTextField.setPreferredSize(minDim);
        this.fontStyleTextField.setMinimumSize(minDim);
        this.fontStyleTextField.setPreferredSize(minDim);
        this.add(fontNameLabel);
        this.add(this.fontNameTextField);
        this.add(fontSizeLabel);
        this.add(this.fontSizeTextField);
        this.add(fontStyleLabel);
        this.add(this.fontStyleTextField);
        this.add(this.changeFontButton);
    }

    public void refresh() {
        this.refresh(this.selectedFont);
    }

    public void refresh(Font font) {
        this.selectedFont = font;
        if (this.selectedFont != null) {
            this.fontNameTextField.setText(this.selectedFont.getFamily(I18N.getLocale()));
            this.fontNameTextField.setCaretPosition(0);
            this.fontNameTextField.setToolTipText(this.selectedFont.getFamily(I18N.getLocale()));
            this.fontSizeTextField.setText("" + this.selectedFont.getSize());
            this.fontStyleTextField.setText(this.getFontStyleName(this.selectedFont.getStyle()));
        }
    }

    @Override
    public Font getFont() {
        return this.selectedFont;
    }

    private String getFontStyleName(int style) {
        switch (style) {
            case 0: {
                return I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Plain");
            }
            case 1: {
                return I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Bold");
            }
            case 2: {
                return I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Italic");
            }
            case 3: {
                return I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Bold-italic");
            }
        }
        return "";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Font newFont;
        if (e.getSource() == this.changeFontButton && (newFont = JFontChooser.showDialog((Component)this.getParent(), (String)I18N.getString("org.saig.jump.widgets.util.JFontChooserPanel.Select-font"), (Font)this.selectedFont)) != null) {
            this.refresh(newFont);
        }
    }
}

