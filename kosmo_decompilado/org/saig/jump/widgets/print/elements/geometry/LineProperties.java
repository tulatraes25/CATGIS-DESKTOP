/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.geometry;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.elements.geometry.LineElement;

public class LineProperties
extends JFrame {
    private LineElement geometry;
    private JButton lineColorButton;
    private JButton trameColorButton;
    private JComboBox lineWidth;
    private JCheckBox fondo;
    private JPanel linePanel;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();

    public LineProperties(LineElement gt) {
        this.geometry = gt;
        this.setName(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.geometry-properties"));
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.geometry-properties"));
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.linePanel = new JPanel();
        this.linePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.line")));
        this.linePanel.add(new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.line-width")) + " :"));
        this.lineWidth = new JComboBox();
        this.lineWidth.addItem("1");
        this.lineWidth.addItem("2");
        this.lineWidth.addItem("3");
        this.lineWidth.addItem("4");
        this.lineWidth.addItem("5");
        this.lineWidth.addItem("6");
        this.lineWidth.addItem("7");
        this.lineWidth.addItem("8");
        this.lineWidth.addItem("9");
        this.lineWidth.addItem("10");
        this.lineWidth.addItem("11");
        this.lineWidth.addItem("12");
        this.lineWidth.addItem("13");
        this.lineWidth.addItem("14");
        this.lineWidth.addItem("15");
        this.lineWidth.addItem("16");
        this.lineWidth.setSelectedIndex((int)gt.getLineWidth() - 1);
        this.linePanel.add(this.lineWidth);
        this.linePanel.add(new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.line-color")) + " :"));
        this.lineColorButton = new JButton();
        this.lineColorButton.setPreferredSize(new Dimension(30, 30));
        this.lineColorButton.addActionListener(new ColorActionListener());
        this.lineColorButton.setBackground(gt.getLineColor());
        this.linePanel.add(this.lineColorButton);
        this.okCancelPanel.setPreferredSize(new Dimension(400, 30));
        this.okCancelPanel.addActionListener(new OkCancelActionListener());
        this.linePanel.setPreferredSize(new Dimension(300, 80));
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add((Component)this.linePanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 10, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.okCancelPanel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, 10, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private void termine() {
        this.dispose();
    }

    private class CheckActionListener
    implements ActionListener {
        private CheckActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            LineProperties.this.trameColorButton.setEnabled(LineProperties.this.fondo.isSelected());
        }
    }

    private class ColorActionListener
    implements ActionListener {
        private ColorActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton boton = (JButton)e.getSource();
            Color newColor = JColorChooser.showDialog(LineProperties.this, I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.choose-background-color"), boton.getBackground());
            boton.setBackground(newColor);
        }
    }

    private class OkCancelActionListener
    implements ActionListener {
        private OkCancelActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (LineProperties.this.okCancelPanel.wasOKPressed()) {
                int width = new Integer((String)LineProperties.this.lineWidth.getSelectedItem());
                Color lineColor = LineProperties.this.lineColorButton.getBackground();
                LineProperties.this.geometry.setLineColor(lineColor);
                LineProperties.this.geometry.setLineWidth(width);
                LineProperties.this.geometry.repaint();
                LineProperties.this.geometry.adaptSize();
            }
            LineProperties.this.termine();
        }
    }
}

