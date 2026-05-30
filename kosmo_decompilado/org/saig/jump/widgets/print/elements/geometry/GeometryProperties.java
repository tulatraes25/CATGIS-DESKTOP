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
import org.saig.jump.widgets.print.elements.geometry.GeometryFrame;

public class GeometryProperties
extends JFrame {
    private GeometryFrame geometry;
    private JPanel tramePanel;
    private JButton lineColorButton;
    private JButton trameColorButton;
    private JComboBox lineWidth;
    private JCheckBox fondo;
    private JPanel linePanel;
    private JPanel typePanel;
    private JComboBox type;
    private String rectangleStr = I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.rectangle");
    private String ellipseStr = I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.ellipse");
    private String lineStr = I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.line");
    private String squareStr = I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.square");
    private String circleStr = I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.circle");
    private OKCancelPanel okCancelPanel = new OKCancelPanel();

    public GeometryProperties(GeometryFrame gt) {
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
        this.tramePanel = new JPanel();
        this.tramePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.background")));
        this.fondo = new JCheckBox(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.opaque"));
        this.fondo.addActionListener(new CheckActionListener());
        this.fondo.setSelected(gt.isOpaque());
        this.tramePanel.add(this.fondo);
        this.tramePanel.add(new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.background-color")) + " :"));
        this.trameColorButton = new JButton();
        this.trameColorButton.setPreferredSize(new Dimension(30, 30));
        this.trameColorButton.addActionListener(new ColorActionListener());
        this.trameColorButton.setBackground(gt.getBackground());
        this.tramePanel.add(this.trameColorButton);
        this.okCancelPanel.setPreferredSize(new Dimension(400, 30));
        this.okCancelPanel.addActionListener(new OkCancelActionListener());
        this.type = new JComboBox();
        this.type.addItem(this.rectangleStr);
        this.type.addItem(this.ellipseStr);
        this.type.addItem(this.lineStr);
        this.type.addItem(this.squareStr);
        this.type.addItem(this.circleStr);
        this.type.setSelectedIndex(this.geometry.getType() - 1);
        this.typePanel = new JPanel();
        this.typePanel.add(this.type);
        this.typePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.geometry-type")));
        this.typePanel.setPreferredSize(new Dimension(300, 80));
        this.tramePanel.setPreferredSize(new Dimension(300, 80));
        this.linePanel.setPreferredSize(new Dimension(300, 80));
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add((Component)this.typePanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, -1, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.linePanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 10, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.getContentPane().add((Component)this.tramePanel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, 10, 0, new Insets(2, 2, 2, 2), 0, 0));
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
            GeometryProperties.this.trameColorButton.setEnabled(GeometryProperties.this.fondo.isSelected());
        }
    }

    private class ColorActionListener
    implements ActionListener {
        private ColorActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton boton = (JButton)e.getSource();
            Color newColor = JColorChooser.showDialog(GeometryProperties.this, I18N.getString("org.saig.jump.widgets.print.elements.geometry.GeometryProperties.choose-background-color"), boton.getBackground());
            boton.setBackground(newColor);
        }
    }

    private class OkCancelActionListener
    implements ActionListener {
        private OkCancelActionListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (GeometryProperties.this.okCancelPanel.wasOKPressed()) {
                int width = new Integer((String)GeometryProperties.this.lineWidth.getSelectedItem());
                Color lineColor = GeometryProperties.this.lineColorButton.getBackground();
                Color backColor = GeometryProperties.this.trameColorButton.getBackground();
                boolean isopaque = GeometryProperties.this.fondo.isSelected();
                GeometryProperties.this.geometry.setLineColor(lineColor);
                GeometryProperties.this.geometry.setLineWidth(width);
                GeometryProperties.this.geometry.setBackground(backColor);
                GeometryProperties.this.geometry.setOpaque(isopaque);
                if (GeometryProperties.this.type.getSelectedItem().equals(GeometryProperties.this.rectangleStr)) {
                    GeometryProperties.this.geometry.setType(1);
                } else if (GeometryProperties.this.type.getSelectedItem().equals(GeometryProperties.this.ellipseStr)) {
                    GeometryProperties.this.geometry.setType(2);
                } else if (GeometryProperties.this.type.getSelectedItem().equals(GeometryProperties.this.lineStr)) {
                    GeometryProperties.this.geometry.setType(3);
                } else if (GeometryProperties.this.type.getSelectedItem().equals(GeometryProperties.this.squareStr)) {
                    GeometryProperties.this.geometry.setType(4);
                } else if (GeometryProperties.this.type.getSelectedItem().equals(GeometryProperties.this.circleStr)) {
                    GeometryProperties.this.geometry.setType(5);
                }
                GeometryProperties.this.geometry.repaint();
                GeometryProperties.this.geometry.adaptSize();
            }
            GeometryProperties.this.termine();
        }
    }
}

