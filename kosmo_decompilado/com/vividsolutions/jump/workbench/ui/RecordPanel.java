/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.ui.RecordPanelModel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RecordPanel
extends JPanel {
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JButton startButton = new JButton();
    private JButton prevButton = new JButton();
    private JTextField textField = new JTextField(4);
    private JButton nextButton = new JButton();
    private JButton endButton = new JButton();
    private RecordPanelModel model;

    public RecordPanel(RecordPanelModel model) {
        this.model = model;
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.setIcon(this.startButton, "Start.gif");
        this.setIcon(this.prevButton, "Prev.gif");
        this.setIcon(this.nextButton, "Next.gif");
        this.setIcon(this.endButton, "End.gif");
    }

    private void setIcon(JButton button, String filename) {
        button.setIcon(IconLoader.icon(filename));
        button.setText("");
    }

    public void updateAppearance() {
        this.startButton.setEnabled(this.model.getCurrentIndex() > 0);
        this.prevButton.setEnabled(this.model.getCurrentIndex() > 0);
        this.nextButton.setEnabled(this.model.getCurrentIndex() < this.model.getRecordCount() - 1);
        this.endButton.setEnabled(this.model.getCurrentIndex() < this.model.getRecordCount() - 1);
        this.textField.setText("" + (this.model.getCurrentIndex() + 1));
    }

    private void jbInit() throws Exception {
        this.startButton.setFocusPainted(false);
        this.startButton.setMargin(new Insets(0, 0, 0, 0));
        this.prevButton.setFocusPainted(false);
        this.prevButton.setMargin(new Insets(0, 0, 0, 0));
        this.nextButton.setFocusPainted(false);
        this.nextButton.setMargin(new Insets(0, 0, 0, 0));
        this.endButton.setFocusPainted(false);
        this.endButton.setMargin(new Insets(0, 0, 0, 0));
        this.startButton.setText("|<");
        this.prevButton.setText("<");
        this.startButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                RecordPanel.this.startButton_actionPerformed(e);
            }
        });
        this.setLayout(this.gridBagLayout1);
        this.prevButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                RecordPanel.this.prevButton_actionPerformed(e);
            }
        });
        this.nextButton.setText(">");
        this.nextButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                RecordPanel.this.nextButton_actionPerformed(e);
            }
        });
        this.endButton.setText(">|");
        this.endButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                RecordPanel.this.endButton_actionPerformed(e);
            }
        });
        this.textField.setEditable(false);
        this.textField.setHorizontalAlignment(4);
        this.add((Component)this.startButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.prevButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.textField, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.add((Component)this.nextButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.endButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
    }

    void nextButton_actionPerformed(ActionEvent e) {
        this.model.setCurrentIndex(this.model.getCurrentIndex() + 1);
        this.updateAppearance();
    }

    void endButton_actionPerformed(ActionEvent e) {
        this.model.setCurrentIndex(this.model.getRecordCount() - 1);
        this.updateAppearance();
    }

    void prevButton_actionPerformed(ActionEvent e) {
        this.model.setCurrentIndex(this.model.getCurrentIndex() - 1);
        this.updateAppearance();
    }

    void startButton_actionPerformed(ActionEvent e) {
        this.model.setCurrentIndex(0);
        this.updateAppearance();
    }
}

