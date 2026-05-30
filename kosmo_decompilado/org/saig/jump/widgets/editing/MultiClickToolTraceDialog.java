/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.editing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import org.saig.jump.lang.I18N;

public class MultiClickToolTraceDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private JPanel jPanelCenter;
    private JLabel jLabelAngle;
    private JButton jButtonFinishGesture;
    private JSpinner jSpinnerLenght;
    private JSpinner jSpinnerAngle;
    private JLabel jLabelDistance;
    private JLabel jLabelAcum;
    private JTextField jTextFieldAcum;

    public MultiClickToolTraceDialog(JFrame parent) {
        super(parent);
        this.initGUI();
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            this.getContentPane().setLayout(thisLayout);
            this.jPanelCenter = new JPanel();
            GridBagLayout jPanelCenterLayout = new GridBagLayout();
            this.getContentPane().add((Component)this.jPanelCenter, "Center");
            jPanelCenterLayout.rowWeights = new double[]{0.1, 0.1, 0.1};
            jPanelCenterLayout.rowHeights = new int[]{7, 7, 20};
            jPanelCenterLayout.columnWeights = new double[]{0.0, 0.1};
            jPanelCenterLayout.columnWidths = new int[]{67, 7};
            this.jPanelCenter.setLayout(jPanelCenterLayout);
            this.jPanelCenter.setBorder(BorderFactory.createTitledBorder(null, I18N.getString("org.saig.jump.widgets.editing.MultiClickToolTraceDialog.Last-line"), 4, 0));
            this.jLabelAngle = new JLabel();
            this.jPanelCenter.add((Component)this.jLabelAngle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            this.jLabelAngle.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.editing.MultiClickToolTraceDialog.Angle")) + ":");
            this.jLabelDistance = new JLabel();
            this.jPanelCenter.add((Component)this.jLabelDistance, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            this.jLabelDistance.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.editing.MultiClickToolTraceDialog.Length")) + ":");
            SpinnerListModel jSpinnerAngleModel = new SpinnerListModel(new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
            this.jSpinnerAngle = new JSpinner();
            this.jPanelCenter.add((Component)this.getJSpinnerAngle(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
            this.jSpinnerAngle.setModel(jSpinnerAngleModel);
            SpinnerListModel jSpinnerLenghtModel = new SpinnerListModel(new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
            this.jSpinnerLenght = new JSpinner();
            this.jPanelCenter.add((Component)this.getJSpinnerLenght(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
            this.jSpinnerLenght.setModel(jSpinnerLenghtModel);
            this.jButtonFinishGesture = new JButton();
            this.jPanelCenter.add((Component)this.getJButtonFinishGesture(), new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
            this.jButtonFinishGesture.setText(I18N.getString("org.saig.jump.widgets.editing.MultiClickToolTraceDialog.Finish-drawing"));
            this.jLabelAcum = new JLabel();
            this.jPanelCenter.add((Component)this.jLabelAcum, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            this.jLabelAcum.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.editing.MultiClickToolTraceDialog.Accumulated")) + ":");
            this.jTextFieldAcum = new JTextField();
            this.jPanelCenter.add((Component)this.getjTextFieldAcum(), new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
            this.getjTextFieldAcum().setEnabled(true);
            this.getjTextFieldAcum().setEditable(false);
            this.getjTextFieldAcum().setHorizontalAlignment(4);
            this.setSize(225, 157);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSpinner getJSpinnerAngle() {
        return this.jSpinnerAngle;
    }

    public JSpinner getJSpinnerLenght() {
        return this.jSpinnerLenght;
    }

    public JButton getJButtonFinishGesture() {
        return this.jButtonFinishGesture;
    }

    public JTextField getjTextFieldAcum() {
        return this.jTextFieldAcum;
    }
}

