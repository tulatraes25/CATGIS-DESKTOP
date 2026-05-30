/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.print.elements;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class PrintOptionsDialog
extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(PrintOptionsDialog.class);
    private static String ppp = I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.dots-per-inch");
    private static String px = "px";
    private JPanel jContentPane;
    private JPanel printQualityPanel;
    private JPanel elementSizePanel;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private Hashtable wmsPrintQuality;
    private Hashtable wmsGridQuality;
    private Hashtable wmsQualityToValue;
    private Hashtable wmsGridToValue;
    ButtonGroup sizeButtonGroup = new ButtonGroup();
    JRadioButton keepProportions;
    JRadioButton keepSize;
    JSlider qualitySlider;
    JSlider wmsQualitySlider;
    JSlider wmsGridSlider;
    private int seleccion = 1;

    public PrintOptionsDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        try {
            this.initialize();
            this.pack();
        }
        catch (Exception ex) {
            LOGGER.error((Object)ex);
            Assert.shouldNeverReachHere((String)ex.getMessage());
        }
    }

    private void initialize() {
        this.setContentPane(this.getJContentPane());
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (PrintOptionsDialog.this.okCancelPanel.wasOKPressed()) {
                    if (PrintOptionsDialog.this.keepSize.isSelected()) {
                        PrintOptionsDialog.this.seleccion = 1;
                    } else {
                        PrintOptionsDialog.this.seleccion = 0;
                    }
                    PrintOptionsDialog.this.setVisible(false);
                } else {
                    PrintOptionsDialog.this.setVisible(false);
                }
            }
        });
        FormUtils.addRowInGBL(this.getJContentPane(), 1, 0, this.getElementSizePanel());
        FormUtils.addRowInGBL(this.getJContentPane(), 2, 0, this.getPrintQualityPanel());
        FormUtils.addRowInGBL(this.getJContentPane(), 3, 0, this.getWMSPrintQualityPanel());
        FormUtils.addRowInGBL(this.getJContentPane(), 4, 0, this.getWMSGridPanel());
        FormUtils.addRowInGBL(this.getJContentPane(), 5, 0, this.okCancelPanel);
        FormUtils.addFiller(this.getJContentPane(), 6, 0);
    }

    private JPanel getJContentPane() {
        if (this.jContentPane == null) {
            this.jContentPane = new JPanel();
            this.jContentPane.setLayout(new GridBagLayout());
        }
        return this.jContentPane;
    }

    private JPanel getElementSizePanel() {
        if (this.elementSizePanel == null) {
            this.elementSizePanel = new JPanel();
            this.elementSizePanel.setLayout(new GridBagLayout());
            this.elementSizePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.elements-size")));
            this.keepSize = new JRadioButton(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.keep-the-elements-size"));
            this.keepSize.setToolTipText(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.if-the-paper-is-resized-the-elements-would-keep-their-size"));
            this.keepProportions = new JRadioButton(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.keep-the-proportions-between-the-elements-and-the-paper"));
            this.keepProportions.setToolTipText(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.if-the-paper-is-resized-the-elements-size-would-be-modified-to-keep-the-proportions"));
            this.sizeButtonGroup.add(this.keepSize);
            this.sizeButtonGroup.add(this.keepProportions);
            FormUtils.addRowInGBL(this.elementSizePanel, 0, 0, this.keepSize);
            FormUtils.addRowInGBL(this.elementSizePanel, 1, 0, this.keepProportions);
        }
        return this.elementSizePanel;
    }

    private JPanel getPrintQualityPanel() {
        if (this.printQualityPanel == null) {
            this.printQualityPanel = new JPanel();
            this.printQualityPanel.setLayout(new GridBagLayout());
            this.printQualityPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.print-quality")));
            this.qualitySlider = new JSlider();
            String[] calidades = new String[]{I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.rough"), I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.regular"), I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.normal"), I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.good"), I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.very-good"), I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.excellent")};
            Hashtable<Integer, JLabel> hashTable = new Hashtable<Integer, JLabel>(6);
            int i = 0;
            while (i < 6) {
                hashTable.put(new Integer(100 * (i + 1)), new JLabel(calidades[i], 0));
                ++i;
            }
            this.qualitySlider.setLabelTable(hashTable);
            this.qualitySlider.setMinimum(100);
            this.qualitySlider.setMaximum(600);
            this.qualitySlider.setMinorTickSpacing(100);
            this.qualitySlider.setMajorTickSpacing(100);
            this.qualitySlider.setPaintTicks(true);
            this.qualitySlider.setSnapToTicks(true);
            this.qualitySlider.setPaintTrack(false);
            this.qualitySlider.setPaintLabels(true);
            Dimension dim = new Dimension();
            dim.setSize(300, 50);
            this.qualitySlider.setMinimumSize(dim);
            this.qualitySlider.setMaximumSize(dim);
            this.qualitySlider.setPreferredSize(dim);
            FormUtils.addRowInGBL(this.printQualityPanel, 1, 0, this.qualitySlider);
        }
        return this.printQualityPanel;
    }

    private JPanel getWMSPrintQualityPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.WMS-layers-print-quality")));
        this.wmsQualitySlider = new JSlider();
        Hashtable<Integer, JLabel> hashTableWMS = new Hashtable<Integer, JLabel>(6);
        hashTableWMS.put(new Integer(0), new JLabel("50 " + ppp));
        hashTableWMS.put(new Integer(1), new JLabel("75 " + ppp));
        hashTableWMS.put(new Integer(2), new JLabel("100 " + ppp));
        hashTableWMS.put(new Integer(3), new JLabel("150 " + ppp));
        hashTableWMS.put(new Integer(4), new JLabel("200 " + ppp));
        hashTableWMS.put(new Integer(5), new JLabel("300 " + ppp));
        this.wmsPrintQuality = new Hashtable();
        this.wmsPrintQuality.put(new Integer(0), new Integer(50));
        this.wmsPrintQuality.put(new Integer(1), new Integer(75));
        this.wmsPrintQuality.put(new Integer(2), new Integer(100));
        this.wmsPrintQuality.put(new Integer(3), new Integer(150));
        this.wmsPrintQuality.put(new Integer(4), new Integer(200));
        this.wmsPrintQuality.put(new Integer(5), new Integer(300));
        this.wmsQualityToValue = new Hashtable();
        this.wmsQualityToValue.put(new Integer(50), new Integer(0));
        this.wmsQualityToValue.put(new Integer(75), new Integer(1));
        this.wmsQualityToValue.put(new Integer(100), new Integer(2));
        this.wmsQualityToValue.put(new Integer(150), new Integer(3));
        this.wmsQualityToValue.put(new Integer(200), new Integer(4));
        this.wmsQualityToValue.put(new Integer(300), new Integer(5));
        this.wmsQualitySlider.setLabelTable(hashTableWMS);
        this.wmsQualitySlider.setMinimum(0);
        this.wmsQualitySlider.setMaximum(5);
        this.wmsQualitySlider.setMinorTickSpacing(1);
        this.wmsQualitySlider.setMajorTickSpacing(1);
        this.wmsQualitySlider.setPaintTicks(true);
        this.wmsQualitySlider.setSnapToTicks(true);
        this.wmsQualitySlider.setPaintTrack(false);
        this.wmsQualitySlider.setPaintLabels(true);
        Dimension dim = new Dimension();
        dim.setSize(300, 50);
        this.wmsQualitySlider.setMinimumSize(dim);
        this.wmsQualitySlider.setMaximumSize(dim);
        this.wmsQualitySlider.setPreferredSize(dim);
        FormUtils.addRowInGBL(panel, 0, 0, this.wmsQualitySlider);
        return panel;
    }

    private JPanel getWMSGridPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.PrintOptionsDialog.WMS-server-request-size")));
        this.wmsGridSlider = new JSlider();
        Hashtable<Integer, JLabel> hashTableWMS = new Hashtable<Integer, JLabel>(6);
        hashTableWMS.put(new Integer(0), new JLabel("50 " + px));
        hashTableWMS.put(new Integer(1), new JLabel("100 " + px));
        hashTableWMS.put(new Integer(2), new JLabel("200 " + px));
        hashTableWMS.put(new Integer(3), new JLabel("500 " + px));
        hashTableWMS.put(new Integer(4), new JLabel("750 " + px));
        hashTableWMS.put(new Integer(5), new JLabel("1000 " + px));
        this.wmsGridQuality = new Hashtable();
        this.wmsGridQuality.put(new Integer(0), new Integer(50));
        this.wmsGridQuality.put(new Integer(1), new Integer(700));
        this.wmsGridQuality.put(new Integer(2), new Integer(200));
        this.wmsGridQuality.put(new Integer(3), new Integer(500));
        this.wmsGridQuality.put(new Integer(4), new Integer(750));
        this.wmsGridQuality.put(new Integer(5), new Integer(1000));
        this.wmsGridToValue = new Hashtable();
        this.wmsGridToValue.put(new Integer(50), new Integer(0));
        this.wmsGridToValue.put(new Integer(100), new Integer(1));
        this.wmsGridToValue.put(new Integer(200), new Integer(2));
        this.wmsGridToValue.put(new Integer(500), new Integer(3));
        this.wmsGridToValue.put(new Integer(750), new Integer(4));
        this.wmsGridToValue.put(new Integer(1000), new Integer(5));
        this.wmsGridSlider.setLabelTable(hashTableWMS);
        this.wmsGridSlider.setMinimum(0);
        this.wmsGridSlider.setMaximum(5);
        this.wmsGridSlider.setMinorTickSpacing(1);
        this.wmsGridSlider.setMajorTickSpacing(1);
        this.wmsGridSlider.setPaintTicks(true);
        this.wmsGridSlider.setSnapToTicks(true);
        this.wmsGridSlider.setPaintTrack(false);
        this.wmsGridSlider.setPaintLabels(true);
        Dimension dim = new Dimension();
        dim.setSize(300, 50);
        this.wmsGridSlider.setMinimumSize(dim);
        this.wmsGridSlider.setMaximumSize(dim);
        this.wmsGridSlider.setPreferredSize(dim);
        FormUtils.addRowInGBL(panel, 0, 0, this.wmsGridSlider);
        return panel;
    }

    public int getSeleccion() {
        return this.seleccion;
    }

    public void setSeleccion(int seleccion) {
        this.seleccion = seleccion;
        if (seleccion == 0) {
            this.keepProportions.setSelected(true);
        } else {
            this.keepSize.setSelected(true);
        }
    }

    public int getPrintQuality() {
        return this.qualitySlider.getValue();
    }

    public int getWMSPrintQuality() {
        return (Integer)this.wmsPrintQuality.get(new Integer(this.wmsQualitySlider.getValue()));
    }

    public void setPrintQuality(int quality) {
        this.qualitySlider.setValue(quality);
    }

    public void setWmsPrintQuality(int quality) {
        this.wmsQualitySlider.setValue((Integer)this.wmsQualityToValue.get(new Integer(quality)));
    }

    public void setWmsGridQuality(int size) {
        this.wmsGridSlider.setValue((Integer)this.wmsGridToValue.get(new Integer(size)));
    }

    public int getWMSGridQuality() {
        return (Integer)this.wmsGridQuality.get(new Integer(this.wmsGridSlider.getValue()));
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }
}

