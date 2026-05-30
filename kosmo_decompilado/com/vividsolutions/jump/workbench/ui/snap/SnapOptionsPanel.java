/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.GridRenderer;
import com.vividsolutions.jump.workbench.ui.snap.MidPointSnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToAbsolutAnglePolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToAnglePolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToCentroidPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToCrossPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToFeaturesPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToGridPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToPerpendicularPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToStartEndPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToTangentPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToVerticesPolicy;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.JColorButton;

public class SnapOptionsPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SnapOptionsPanel.class);
    public static final String SNAP_TO_VERTICES_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO VERTICES COLOR";
    public static final String SNAP_TO_FEATURES_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO FEATURES COLOR";
    public static final String SNAP_TO_MID_POINT_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO MID POINT COLOR";
    public static final String SNAP_TO_START_END_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO START END COLOR";
    public static final String SNAP_TO_GRID_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO GRID COLOR";
    public static final String SNAP_TO_CENTROID_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO CENTROID COLOR";
    public static final String SNAP_TO_PERPENDICULAR_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO PERPENDICULAR COLOR";
    public static final String SNAP_TO_ANGLE_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO ANGLE COLOR";
    public static final String SNAP_TO_ABSOLUT_ANGLE_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO ABSOLUT ANGLE COLOR";
    public static final String SNAP_TO_TANGENT_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO TANGENT COLOR";
    public static final String SNAP_TO_CROSS_COLOR_KEY = String.valueOf(SnapOptionsPanel.class.getName()) + " - SNAP TO CROSS COLOR";
    private ButtonGroup buttonGroup = new ButtonGroup();
    private JPanel snapOptionsPanel;
    private JCheckBox snapToFeaturesCheckBox = new JCheckBox();
    private JCheckBox snapToVerticesCheckBox = new JCheckBox();
    private JCheckBox snapToMidPoint = new JCheckBox();
    private JCheckBox snapToStartEndCheckBox = new JCheckBox();
    private JCheckBox snapToGridCheckBox = new JCheckBox();
    private JCheckBox snapToCentroidCheckBox = new JCheckBox();
    private JLabel bufferSizeLabel = new JLabel();
    private JTextField bufferSizeTextField = new JTextField(7);
    private JCheckBox snapToFirstDrawnVertex = new JCheckBox();
    private JCheckBox snapToPerpendicularCheckBox = new JCheckBox();
    private JCheckBox snapToAngleCheckBox = new JCheckBox();
    private JCheckBox snapToAbsoluteAngleCheckBox = new JCheckBox();
    private JCheckBox snapToTangentCheckBox = new JCheckBox();
    private JCheckBox snapToCrossCheckBox = new JCheckBox();
    private JPanel gridOptionsPanel;
    private JLabel gridSizeLabel = new JLabel();
    private JTextField gridSizeTextField = new JTextField(7);
    private JRadioButton showGridDotsRadioButton = new JRadioButton();
    private JRadioButton showGridLinesRadioButton = new JRadioButton();
    private JCheckBox showGridCheckBox = new JCheckBox();
    private JColorButton snapToVerticesColorButton;
    private JColorButton snapToFeaturesColorButton;
    private JColorButton snapToMidPointColorButton;
    private JColorButton snapToStartEndColorButton;
    private JColorButton snapToCentroidColorButton;
    private JColorButton snapToGridColorButton;
    private JColorButton snapToPerpendicularColorButton;
    private JColorButton snapToAngleColorButton;
    private JColorButton snapToAbsoluteAngleColorButton;
    private JColorButton snapToTangentColorButton;
    private JColorButton snapToCrossColorButton;

    public SnapOptionsPanel() {
        try {
            this.initialize();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.registerPoliciesKeyListeners();
    }

    private void updateEnabled() {
        this.gridSizeTextField.setEnabled(this.showGridCheckBox.isSelected());
        this.showGridDotsRadioButton.setEnabled(this.showGridCheckBox.isSelected());
        this.showGridLinesRadioButton.setEnabled(this.showGridCheckBox.isSelected());
    }

    @Override
    public String validateInput() {
        String errorMessage = "\"" + this.gridSizeTextField.getText() + "\" " + I18N.getString("workbench.ui.snap.SnapOptionsPanel.is-not-a-valid-grid-size");
        try {
            if (Double.parseDouble(this.gridSizeTextField.getText()) <= 0.0) {
                return errorMessage;
            }
        }
        catch (NumberFormatException e) {
            return errorMessage;
        }
        String errorBufferSizeMessage = I18N.getMessage("workbench.ui.snap.SnapOptionsPanel.{0}-is-not-a-valid-buffer-size-value", new Object[]{this.bufferSizeTextField.getText()});
        try {
            if (Double.parseDouble(this.bufferSizeTextField.getText()) <= 0.0) {
                return errorBufferSizeMessage;
            }
        }
        catch (NumberFormatException e) {
            return errorBufferSizeMessage;
        }
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_VERTICES_COLOR_KEY, this.snapToVerticesColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_FEATURES_COLOR_KEY, this.snapToFeaturesColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_MID_POINT_COLOR_KEY, this.snapToMidPointColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_START_END_COLOR_KEY, this.snapToStartEndColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_GRID_COLOR_KEY, this.snapToGridColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_CENTROID_COLOR_KEY, this.snapToCentroidColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_PERPENDICULAR_COLOR_KEY, this.snapToPerpendicularColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_ANGLE_COLOR_KEY, this.snapToAngleColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_ABSOLUT_ANGLE_COLOR_KEY, this.snapToAbsoluteAngleColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_TANGENT_COLOR_KEY, this.snapToTangentColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SNAP_TO_CROSS_COLOR_KEY, this.snapToCrossColorButton.getColor());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToVerticesPolicy.ENABLED_KEY, this.snapToVerticesCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToFeaturesPolicy.ENABLED_KEY, this.snapToFeaturesCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToGridPolicy.ENABLED_KEY, this.snapToGridCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToCentroidPolicy.ENABLED_KEY, this.snapToCentroidCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(MidPointSnapPolicy.ENABLED_KEY, this.snapToMidPoint.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToStartEndPolicy.ENABLED_KEY, this.snapToStartEndCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToGridPolicy.GRID_SIZE_KEY, Double.parseDouble(this.gridSizeTextField.getText()));
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToPerpendicularPolicy.ENABLED_KEY, this.snapToPerpendicularCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToAnglePolicy.ENABLED_KEY, this.snapToAngleCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToAbsolutAnglePolicy.ENABLED_KEY, this.snapToAbsoluteAngleCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToTangentPolicy.ENABLED_KEY, this.snapToTangentCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToCrossPolicy.ENABLED_KEY, this.snapToCrossCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put("SNAP_BUFFER", Double.parseDouble(this.bufferSizeTextField.getText()));
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToVerticesPolicy.FIRST_CANDIDATE, this.snapToFirstDrawnVertex.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(GridRenderer.DOTS_ENABLED_KEY, this.showGridDotsRadioButton.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(GridRenderer.LINES_ENABLED_KEY, this.showGridLinesRadioButton.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(GridRenderer.ENABLED_KEY, this.showGridCheckBox.isSelected());
    }

    @Override
    public void init() {
        this.snapToVerticesCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToVerticesPolicy.ENABLED_KEY, false));
        this.snapToFeaturesCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToFeaturesPolicy.ENABLED_KEY, false));
        this.snapToGridCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToGridPolicy.ENABLED_KEY, false));
        this.snapToCentroidCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToCentroidPolicy.ENABLED_KEY, false));
        this.snapToMidPoint.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(MidPointSnapPolicy.ENABLED_KEY, false));
        this.snapToStartEndCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToStartEndPolicy.ENABLED_KEY, false));
        this.snapToPerpendicularCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToPerpendicularPolicy.ENABLED_KEY, false));
        this.snapToAngleCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToAnglePolicy.ENABLED_KEY, false));
        this.snapToTangentCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToTangentPolicy.ENABLED_KEY, false));
        this.snapToCrossCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToCrossPolicy.ENABLED_KEY, false));
        this.snapToAbsoluteAngleCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToAbsolutAnglePolicy.ENABLED_KEY, false));
        this.gridSizeTextField.setText("" + PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToGridPolicy.GRID_SIZE_KEY, 20.0));
        this.bufferSizeTextField.setText("" + PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get("SNAP_BUFFER", 0.3));
        this.snapToFirstDrawnVertex.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToVerticesPolicy.FIRST_CANDIDATE, false));
        this.showGridCheckBox.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(GridRenderer.ENABLED_KEY, false));
        this.showGridDotsRadioButton.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(GridRenderer.DOTS_ENABLED_KEY, false));
        this.showGridLinesRadioButton.setSelected(PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(GridRenderer.LINES_ENABLED_KEY, false));
        this.snapToVerticesColorButton.setColor(SnapOptionsPanel.getSnapToVerticesColor());
        this.snapToFeaturesColorButton.setColor(SnapOptionsPanel.getSnapToFeaturesColor());
        this.snapToMidPointColorButton.setColor(SnapOptionsPanel.getSnapToMidPointColor());
        this.snapToStartEndColorButton.setColor(SnapOptionsPanel.getSnapToStartEndColor());
        this.snapToGridColorButton.setColor(SnapOptionsPanel.getSnapToGridColor());
        this.snapToCentroidColorButton.setColor(SnapOptionsPanel.getSnapToCentroidColor());
        this.snapToPerpendicularColorButton.setColor(SnapOptionsPanel.getSnapToPerpendicularColor());
        this.snapToAngleColorButton.setColor(SnapOptionsPanel.getSnapToAngleColor());
        this.snapToAbsoluteAngleColorButton.setColor(SnapOptionsPanel.getSnapToAbsolutAngleColor());
        this.snapToTangentColorButton.setColor(SnapOptionsPanel.getSnapToTangentColor());
        this.snapToCrossColorButton.setColor(SnapOptionsPanel.getSnapToCrossColor());
        this.updateEnabled();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getSnapOptionsPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getGridOptionsPanel());
        FormUtils.addFiller(this, 2, 0);
    }

    public JPanel getSnapOptionsPanel() {
        if (this.snapOptionsPanel == null) {
            this.snapOptionsPanel = new JPanel(new GridBagLayout());
            Dimension buttonDim = new Dimension(30, 20);
            this.snapToVerticesCheckBox.setText(SnapToVerticesPolicy.NAME);
            this.snapToFeaturesCheckBox.setText(SnapToFeaturesPolicy.NAME);
            this.snapToMidPoint.setText(MidPointSnapPolicy.NAME);
            this.snapToStartEndCheckBox.setText(SnapToStartEndPolicy.NAME);
            this.snapToGridCheckBox.setText(SnapToGridPolicy.NAME);
            this.snapToCentroidCheckBox.setText(SnapToCentroidPolicy.NAME);
            this.snapToPerpendicularCheckBox.setText(SnapToPerpendicularPolicy.NAME);
            this.snapToAngleCheckBox.setText(SnapToAnglePolicy.NAME);
            this.snapToAbsoluteAngleCheckBox.setText(SnapToAbsolutAnglePolicy.NAME);
            this.snapToTangentCheckBox.setText(SnapToTangentPolicy.NAME);
            this.snapToCrossCheckBox.setText(SnapToCrossPolicy.NAME);
            JPanel bufferSizePanel = new JPanel(new FlowLayout(0));
            this.bufferSizeTextField.setHorizontalAlignment(11);
            this.bufferSizeTextField.setMinimumSize(buttonDim);
            this.bufferSizeTextField.setPreferredSize(buttonDim);
            this.bufferSizeTextField.setMaximumSize(buttonDim);
            this.bufferSizeLabel.setText(String.valueOf(I18N.getString("workbench.ui.snap.SnapOptionsPanel.snap-distance")) + " (" + I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel.view-units") + ") :");
            bufferSizePanel.add(this.bufferSizeLabel);
            bufferSizePanel.add(this.bufferSizeTextField);
            this.snapToFirstDrawnVertex.setText(I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel.Allow-that-the-first-drawn-point-will-be-used-as-candidate"));
            this.snapOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("workbench.ui.snap.SnapOptionsPanel.snap-type")));
            this.snapToFeaturesCheckBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SnapOptionsPanel.this.updateEnabled();
                }
            });
            this.snapToVerticesColorButton = new JColorButton();
            this.snapToVerticesColorButton.setMinimumSize(buttonDim);
            this.snapToVerticesColorButton.setPreferredSize(buttonDim);
            this.snapToVerticesColorButton.setMaximumSize(buttonDim);
            this.snapToFeaturesColorButton = new JColorButton();
            this.snapToFeaturesColorButton.setMinimumSize(buttonDim);
            this.snapToFeaturesColorButton.setPreferredSize(buttonDim);
            this.snapToFeaturesColorButton.setMaximumSize(buttonDim);
            this.snapToMidPointColorButton = new JColorButton();
            this.snapToMidPointColorButton.setMinimumSize(buttonDim);
            this.snapToMidPointColorButton.setPreferredSize(buttonDim);
            this.snapToMidPointColorButton.setMaximumSize(buttonDim);
            this.snapToStartEndColorButton = new JColorButton();
            this.snapToStartEndColorButton.setMinimumSize(buttonDim);
            this.snapToStartEndColorButton.setPreferredSize(buttonDim);
            this.snapToStartEndColorButton.setMaximumSize(buttonDim);
            this.snapToGridColorButton = new JColorButton();
            this.snapToGridColorButton.setMinimumSize(buttonDim);
            this.snapToGridColorButton.setPreferredSize(buttonDim);
            this.snapToGridColorButton.setMaximumSize(buttonDim);
            this.snapToCentroidColorButton = new JColorButton();
            this.snapToCentroidColorButton.setMinimumSize(buttonDim);
            this.snapToCentroidColorButton.setPreferredSize(buttonDim);
            this.snapToCentroidColorButton.setMaximumSize(buttonDim);
            this.snapToPerpendicularColorButton = new JColorButton();
            this.snapToPerpendicularColorButton.setMinimumSize(buttonDim);
            this.snapToPerpendicularColorButton.setPreferredSize(buttonDim);
            this.snapToPerpendicularColorButton.setMaximumSize(buttonDim);
            this.snapToAngleColorButton = new JColorButton();
            this.snapToAngleColorButton.setMinimumSize(buttonDim);
            this.snapToAngleColorButton.setPreferredSize(buttonDim);
            this.snapToAngleColorButton.setMaximumSize(buttonDim);
            this.snapToAbsoluteAngleColorButton = new JColorButton();
            this.snapToAbsoluteAngleColorButton.setMinimumSize(buttonDim);
            this.snapToAbsoluteAngleColorButton.setPreferredSize(buttonDim);
            this.snapToAbsoluteAngleColorButton.setMaximumSize(buttonDim);
            this.snapToTangentColorButton = new JColorButton();
            this.snapToTangentColorButton.setMinimumSize(buttonDim);
            this.snapToTangentColorButton.setPreferredSize(buttonDim);
            this.snapToTangentColorButton.setMaximumSize(buttonDim);
            this.snapToCrossColorButton = new JColorButton();
            this.snapToCrossColorButton.setMinimumSize(buttonDim);
            this.snapToCrossColorButton.setPreferredSize(buttonDim);
            this.snapToCrossColorButton.setMaximumSize(buttonDim);
            int row = 0;
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row, 0, (JComponent)this.snapToVerticesCheckBox, (JComponent)this.snapToVerticesColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 30, (JComponent)this.snapToPerpendicularCheckBox, (JComponent)this.snapToPerpendicularColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row, 0, (JComponent)this.snapToFeaturesCheckBox, (JComponent)this.snapToFeaturesColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 30, (JComponent)this.snapToAngleCheckBox, (JComponent)this.snapToAngleColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row, 0, (JComponent)this.snapToMidPoint, (JComponent)this.snapToMidPointColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 30, (JComponent)this.snapToAbsoluteAngleCheckBox, (JComponent)this.snapToAbsoluteAngleColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row, 0, (JComponent)this.snapToStartEndCheckBox, (JComponent)this.snapToStartEndColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 30, (JComponent)this.snapToTangentCheckBox, (JComponent)this.snapToTangentColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row, 0, (JComponent)this.snapToCentroidCheckBox, (JComponent)this.snapToCentroidColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 30, (JComponent)this.snapToCrossCheckBox, (JComponent)this.snapToCrossColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 0, (JComponent)this.snapToGridCheckBox, (JComponent)this.snapToGridColorButton, false);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 0, (JComponent)bufferSizePanel, true, true);
            FormUtils.addRowInGBL((JComponent)this.snapOptionsPanel, row++, 0, (JComponent)this.snapToFirstDrawnVertex, true, true);
        }
        return this.snapOptionsPanel;
    }

    public JPanel getGridOptionsPanel() {
        if (this.gridOptionsPanel == null) {
            this.gridOptionsPanel = new JPanel(new GridBagLayout());
            this.gridOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("workbench.ui.snap.SnapOptionsPanel.grid")));
            this.gridSizeLabel.setText(String.valueOf(I18N.getString("workbench.ui.snap.SnapOptionsPanel.size")) + " :");
            this.gridSizeTextField.setText("20");
            this.gridSizeTextField.setHorizontalAlignment(11);
            this.gridSizeTextField.setMinimumSize(new Dimension(30, 20));
            this.gridSizeTextField.setPreferredSize(new Dimension(30, 20));
            this.gridSizeTextField.setMaximumSize(new Dimension(30, 20));
            this.showGridDotsRadioButton.setSelected(true);
            this.showGridDotsRadioButton.setText(I18N.getString("workbench.ui.snap.SnapOptionsPanel.show-grid-as-dots"));
            this.showGridLinesRadioButton.setText(I18N.getString("workbench.ui.snap.SnapOptionsPanel.show-grid-as-lines"));
            this.showGridCheckBox.setText(I18N.getString("workbench.ui.snap.SnapOptionsPanel.show-grid"));
            this.showGridCheckBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SnapOptionsPanel.this.showGridCheckBox_actionPerformed(e);
                }
            });
            this.buttonGroup.add(this.showGridDotsRadioButton);
            this.buttonGroup.add(this.showGridLinesRadioButton);
            FormUtils.addRowInGBL((JComponent)this.gridOptionsPanel, 0, 0, (JComponent)this.showGridCheckBox, false, true);
            FormUtils.addRowInGBL((JComponent)this.gridOptionsPanel, 0, 30, this.gridSizeLabel, (JComponent)this.gridSizeTextField);
            FormUtils.addRowInGBL(this.gridOptionsPanel, 1, 0, this.showGridDotsRadioButton);
            FormUtils.addRowInGBL(this.gridOptionsPanel, 2, 0, this.showGridLinesRadioButton);
        }
        return this.gridOptionsPanel;
    }

    void showGridCheckBox_actionPerformed(ActionEvent e) {
        this.updateEnabled();
    }

    @Override
    public Icon getIcon() {
        return GUIUtil.toSmallIcon(SnapVerticesTool.ICON);
    }

    @Override
    public String getName() {
        return I18N.getString("workbench.ui.snap.InstallGridPlugIn.snap-grid");
    }

    private void registerPoliciesKeyListeners() {
        JUMPWorkbench.getFrameInstance().addEasyKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent ke) {
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                String message = "";
                boolean selected = false;
                if (ke.isControlDown() && ke.getKeyCode() == 81) {
                    selected = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToVerticesPolicy.ENABLED_KEY, false);
                    if (!selected || !PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToFeaturesPolicy.ENABLED_KEY, false)) {
                        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToVerticesPolicy.ENABLED_KEY, !selected);
                    } else {
                        selected = false;
                    }
                    message = String.valueOf(SnapToVerticesPolicy.NAME) + " - ";
                } else if (ke.isControlDown() && ke.getKeyCode() == 87) {
                    selected = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToFeaturesPolicy.ENABLED_KEY, false);
                    PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToFeaturesPolicy.ENABLED_KEY, !selected);
                    if (!selected) {
                        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToVerticesPolicy.ENABLED_KEY, true);
                    }
                    message = String.valueOf(SnapToFeaturesPolicy.NAME) + " - ";
                } else if (ke.isControlDown() && ke.getKeyCode() == 69) {
                    selected = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(MidPointSnapPolicy.ENABLED_KEY, false);
                    PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(MidPointSnapPolicy.ENABLED_KEY, !selected);
                    message = String.valueOf(MidPointSnapPolicy.NAME) + " - ";
                } else if (ke.isControlDown() && ke.getKeyCode() == 82) {
                    selected = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToStartEndPolicy.ENABLED_KEY, false);
                    PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToStartEndPolicy.ENABLED_KEY, !selected);
                    message = String.valueOf(SnapToStartEndPolicy.NAME) + " - ";
                } else if (ke.isControlDown() && ke.getKeyCode() == 65) {
                    selected = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToGridPolicy.ENABLED_KEY, false);
                    PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToGridPolicy.ENABLED_KEY, !selected);
                    message = String.valueOf(SnapToGridPolicy.NAME) + " - ";
                } else if (ke.isControlDown() && ke.getKeyCode() == 83) {
                    selected = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapToCentroidPolicy.ENABLED_KEY, false);
                    PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(SnapToCentroidPolicy.ENABLED_KEY, !selected);
                    message = String.valueOf(SnapToCentroidPolicy.NAME) + " - ";
                }
                if (!message.equals("")) {
                    if (!selected) {
                        JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel.{0}-enabled", new Object[]{message}));
                    } else {
                        JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel.{0}-disabled", new Object[]{message}));
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }
        });
    }

    public static Color getSnapToVerticesColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_VERTICES_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToFeaturesColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_FEATURES_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToMidPointColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_MID_POINT_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToStartEndColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_START_END_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToGridColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_GRID_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToCentroidColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_CENTROID_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToPerpendicularColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_PERPENDICULAR_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToAngleColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_ANGLE_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToAbsolutAngleColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_ABSOLUT_ANGLE_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToTangentColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_TANGENT_COLOR_KEY, Color.GREEN);
    }

    public static Color getSnapToCrossColor() {
        return (Color)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SNAP_TO_CROSS_COLOR_KEY, Color.GREEN);
    }
}

