/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.saig.jump.lang.I18N;

public class ErrorDialog
extends JOptionPane {
    private static final long serialVersionUID = 1L;
    private static final String SHOW_DETAILS = I18N.getString("workbench.ui.ErrorDialog.show-details");
    private static final String HIDE_DETAILS = I18N.getString("workbench.ui.ErrorDialog.hide-details");
    private static final int MIN_DIALOG_WIDTH = 500;
    private String details;

    private ErrorDialog() {
        super(I18N.getString("workbench.ui.ErrorDialog.Message"), 0, -1);
    }

    private void addDetailPanel(final JDialog dialog, final JButton detailButton) {
        final JPanel panel = new JPanel(new GridBagLayout());
        final JScrollPane scrollPane = new JScrollPane();
        JTextArea textArea = new JTextArea();
        textArea.setOpaque(false);
        scrollPane.setOpaque(false);
        textArea.setFont(new Font("Monospaced", 0, 12));
        textArea.setEditable(false);
        scrollPane.setHorizontalScrollBarPolicy(30);
        scrollPane.setVerticalScrollBarPolicy(20);
        scrollPane.getViewport().add(textArea);
        panel.add((Component)scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 18, 1, new Insets(0, 0, 0, 0), 0, 0));
        textArea.setText(this.details);
        detailButton.addActionListener(new ActionListener(){
            private boolean showingDetails = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (this.showingDetails) {
                    dialog.remove(panel);
                    detailButton.setText(SHOW_DETAILS);
                } else {
                    scrollPane.setPreferredSize(new Dimension(dialog.getWidth(), 200));
                    dialog.getContentPane().add((Component)panel, "South");
                    detailButton.setText(HIDE_DETAILS);
                }
                this.showingDetails = !this.showingDetails;
                dialog.pack();
            }
        });
    }

    private void setDetails(String details) {
        this.details = details;
    }

    @Override
    public JDialog createDialog(Component parentComponent, String title) {
        JButton okButton = new JButton(I18N.getString("workbench.ui.ErrorDialog.ok"));
        JButton detailButton = new JButton(SHOW_DETAILS);
        this.setOptions(new Object[]{okButton, detailButton});
        final JDialog dialog = super.createDialog(parentComponent, title);
        this.addDetailPanel(dialog, detailButton);
        okButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        JPanel horizontalStrut = new JPanel();
        horizontalStrut.setPreferredSize(new Dimension(500, 0));
        horizontalStrut.setMinimumSize(horizontalStrut.getPreferredSize());
        horizontalStrut.setMaximumSize(horizontalStrut.getPreferredSize());
        dialog.getContentPane().add((Component)horizontalStrut, "North");
        dialog.pack();
        if (parentComponent != null) {
            GUIUtil.centreOnWindow(dialog);
        }
        dialog.setResizable(false);
        return dialog;
    }

    public static void show(Component parentComponent, String title, String message, String details) {
        ErrorDialog dialog = new ErrorDialog();
        dialog.setMessage(StringUtil.split(message, 80));
        dialog.setDetails(details);
        dialog.createDialog(parentComponent, title).setVisible(true);
    }
}

