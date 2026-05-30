/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;

public class JRadioButtonWithIcon
extends JPanel {
    private static final long serialVersionUID = 1L;
    protected JRadioButton radio = new JRadioButton();
    protected JLabel label = new JLabel();

    public JRadioButtonWithIcon(String text, Icon icon) {
        this.label.setText(text);
        this.label.setIcon(icon);
        this.label.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                JRadioButtonWithIcon.this.radio.doClick();
            }
        });
        this.setLayout(new FlowLayout(0));
        this.add(this.radio);
        this.add(this.label);
    }

    public void addToButtonGroup(ButtonGroup group) {
        group.add(this.radio);
    }

    public void addActionListener(ActionListener listener) {
        this.radio.addActionListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        this.radio.addChangeListener(listener);
    }

    public Icon getImage() {
        return this.label.getIcon();
    }

    public void setImage(Icon icon) {
        this.label.setIcon(icon);
    }

    public String getText() {
        return this.label.getText();
    }

    public void setText(String text) {
        this.label.setText(text);
    }

    public void setSelected(boolean selected) {
        this.radio.setSelected(selected);
    }

    public boolean isSelected() {
        return this.radio.isSelected();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.radio != null) {
            this.radio.setEnabled(enabled);
        }
        if (this.label != null) {
            this.label.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    @Override
    public void setOpaque(boolean isOpaque) {
        if (this.radio != null) {
            this.radio.setOpaque(isOpaque);
        }
        if (this.label != null) {
            this.label.setOpaque(isOpaque);
        }
        super.setOpaque(isOpaque);
    }
}

