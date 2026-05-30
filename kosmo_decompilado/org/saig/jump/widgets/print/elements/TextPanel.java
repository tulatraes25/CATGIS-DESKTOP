/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.saig.jump.lang.I18N;

public class TextPanel
extends JPanel {
    private JLabel previewLabel = new JLabel();
    private JPanel textPanel = new JPanel();
    private JPanel yourText = new JPanel();
    private JTextArea yourTextArea = new JTextArea();
    private PositionPanel positionPanel = new PositionPanel();

    public TextPanel(JLabel label) {
        this.previewLabel = label;
        this.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.TextPanel.name")));
        this.init();
        this.yourTextArea.addKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                TextPanel.this.setText(TextPanel.this.previewLabel, TextPanel.this.yourTextArea.getText());
            }
        });
        this.yourText.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.TextPanel.your-text")));
        this.yourText.setPreferredSize(new Dimension(320, 70));
        this.yourText.setLayout(new BorderLayout());
        this.yourText.add((Component)this.yourTextArea, "Center");
        this.textPanel.setPreferredSize(new Dimension(400, 75));
        this.textPanel.setSize(this.textPanel.getPreferredSize());
        this.textPanel.setLayout(new GridBagLayout());
        this.textPanel.add((Component)this.yourText, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 18, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.textPanel.add((Component)this.positionPanel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.setLayout(new GridBagLayout());
        this.add((Component)this.textPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 18, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    protected void setText(JLabel label, String newText) {
        String oldText = label.getText();
        if (this.isUnderlined(oldText)) {
            newText = "<html><u>" + newText + "</u></html>";
        }
        label.setText(newText);
    }

    private boolean isUnderlined(String text) {
        return text.toUpperCase().lastIndexOf("<HTML><U>") != -1 && text.toUpperCase().lastIndexOf("</U></HTML") != -1;
    }

    private void init() {
        String ch = this.previewLabel.getText().replaceAll("(?i)<html><u>", "");
        this.yourTextArea.setText(ch.replaceAll("(?i)</u></html>", ""));
        block0 : switch (this.previewLabel.getVerticalAlignment()) {
            case 1: {
                switch (this.previewLabel.getHorizontalAlignment()) {
                    case 2: {
                        this.positionPanel.topLeft.setState(true);
                        break;
                    }
                    case 0: {
                        this.positionPanel.topCenter.setState(true);
                        break;
                    }
                    case 4: {
                        this.positionPanel.topRight.setState(true);
                    }
                }
                break;
            }
            case 0: {
                switch (this.previewLabel.getHorizontalAlignment()) {
                    case 2: {
                        this.positionPanel.centerLeft.setState(true);
                        break;
                    }
                    case 0: {
                        this.positionPanel.centerCenter.setState(true);
                        break;
                    }
                    case 4: {
                        this.positionPanel.centerRight.setState(true);
                    }
                }
                break;
            }
            case 3: {
                switch (this.previewLabel.getHorizontalAlignment()) {
                    case 2: {
                        this.positionPanel.bottomLeft.setState(true);
                        break block0;
                    }
                    case 0: {
                        this.positionPanel.bottomCenter.setState(true);
                        break block0;
                    }
                    case 4: {
                        this.positionPanel.bottomRight.setState(true);
                    }
                }
            }
        }
    }

    private class PositionPanel
    extends JPanel {
        private JPanel positionPanel = new JPanel();
        private CheckboxGroup alignment = new CheckboxGroup();
        private Checkbox topLeft = new Checkbox("", this.alignment, false);
        private Checkbox topCenter = new Checkbox("", this.alignment, false);
        private Checkbox topRight = new Checkbox("", this.alignment, false);
        private Checkbox centerLeft = new Checkbox("", this.alignment, false);
        private Checkbox centerCenter = new Checkbox("", this.alignment, true);
        private Checkbox centerRight = new Checkbox("", this.alignment, false);
        private Checkbox bottomLeft = new Checkbox("", this.alignment, false);
        private Checkbox bottomCenter = new Checkbox("", this.alignment, false);
        private Checkbox bottomRight = new Checkbox("", this.alignment, false);

        public PositionPanel() {
            this.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.TextPanel.position")));
            this.positionPanel.setPreferredSize(new Dimension(60, 45));
            this.positionPanel.setSize(this.getPreferredSize());
            this.positionPanel.setLayout(new GridLayout(3, 3));
            this.positionPanel.add(this.topLeft);
            this.topLeft.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(1);
                    TextPanel.this.previewLabel.setHorizontalAlignment(2);
                }
            });
            this.positionPanel.add(this.topCenter);
            this.topCenter.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(1);
                    TextPanel.this.previewLabel.setHorizontalAlignment(0);
                }
            });
            this.positionPanel.add(this.topRight);
            this.topRight.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(1);
                    TextPanel.this.previewLabel.setHorizontalAlignment(4);
                }
            });
            this.positionPanel.add(this.centerLeft);
            this.centerLeft.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(0);
                    TextPanel.this.previewLabel.setHorizontalAlignment(2);
                }
            });
            this.positionPanel.add(this.centerCenter);
            this.centerCenter.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(0);
                    TextPanel.this.previewLabel.setHorizontalAlignment(0);
                }
            });
            this.positionPanel.add(this.centerRight);
            this.centerRight.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(0);
                    TextPanel.this.previewLabel.setHorizontalAlignment(4);
                }
            });
            this.positionPanel.add(this.bottomLeft);
            this.bottomLeft.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(3);
                    TextPanel.this.previewLabel.setHorizontalAlignment(2);
                }
            });
            this.positionPanel.add(this.bottomCenter);
            this.bottomCenter.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(3);
                    TextPanel.this.previewLabel.setHorizontalAlignment(0);
                }
            });
            this.positionPanel.add(this.bottomRight);
            this.bottomRight.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent e) {
                    TextPanel.this.previewLabel.setVerticalAlignment(3);
                    TextPanel.this.previewLabel.setHorizontalAlignment(4);
                }
            });
            this.setLayout(new GridBagLayout());
            this.add((Component)this.positionPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 17, 0, new Insets(0, 0, 0, 0), 0, 0));
        }
    }
}

