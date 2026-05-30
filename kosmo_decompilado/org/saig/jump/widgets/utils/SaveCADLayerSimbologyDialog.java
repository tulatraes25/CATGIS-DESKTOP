/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class SaveCADLayerSimbologyDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String TITLE = I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.CAD-layer-simbology-options");
    private JPanel optionSelectionPanel;
    private ButtonGroup optionGroup = new ButtonGroup();
    private JRadioButton colorOptionRadioButton;
    private JRadioButton layerOptionRadioButton;
    private JRadioButton layerAndColorOptionRadioButton;
    public static final String TRUE_COLOR_OPTION = I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.true-colors");
    public static final String LAYER_OPTION = I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.layers-with-default-layer-color");
    public static final String LAYER_COLOR_OPTION = I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.layers-with-true-colors");
    private static final String TRUE_COLOR_OPTION_EXPLAIN = " (" + I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.each-feature-is-drawn-with-its-true-color") + ")";
    private static final String LAYER_OPTION_EXPLAIN = " (" + I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.each-feature-is-drawn-with-the-layer-default-color") + ")";
    private static final String LAYER_COLOR_OPTION_EXPLAIN = " (" + I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.each-feature-is-drawn-with-its-true-color-and-is-classified-by-layer-and-color") + ")";
    private JPanel additionalOptionsPanel;
    private JCheckBox saveForShapeCheckbox;
    private OKCancelPanel okCancelPanel;

    public SaveCADLayerSimbologyDialog(JFrame owner, boolean modal) {
        super((Frame)owner, modal);
        this.setTitle(TITLE);
        this.initialize();
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getOptionSelectionPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getAdditionalOptionsPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.getOkCancelPanel());
        FormUtils.addFiller(mainPanel, 3, 0);
    }

    public JPanel getOptionSelectionPanel() {
        if (this.optionSelectionPanel == null) {
            this.optionSelectionPanel = new JPanel(new GridBagLayout());
            this.optionSelectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.default-simbology-style")));
            this.colorOptionRadioButton = new JRadioButton(String.valueOf(TRUE_COLOR_OPTION) + TRUE_COLOR_OPTION_EXPLAIN);
            this.layerOptionRadioButton = new JRadioButton(String.valueOf(LAYER_OPTION) + LAYER_OPTION_EXPLAIN);
            this.layerAndColorOptionRadioButton = new JRadioButton(String.valueOf(LAYER_COLOR_OPTION) + LAYER_COLOR_OPTION_EXPLAIN);
            this.optionGroup.add(this.colorOptionRadioButton);
            this.optionGroup.add(this.layerOptionRadioButton);
            this.optionGroup.add(this.layerAndColorOptionRadioButton);
            this.colorOptionRadioButton.setSelected(true);
            FormUtils.addRowInGBL(this.optionSelectionPanel, 0, 0, this.colorOptionRadioButton);
            FormUtils.addRowInGBL(this.optionSelectionPanel, 1, 0, this.layerOptionRadioButton);
            FormUtils.addRowInGBL(this.optionSelectionPanel, 2, 0, this.layerAndColorOptionRadioButton);
        }
        return this.optionSelectionPanel;
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SaveCADLayerSimbologyDialog.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public String getSelectedFeatureTypeStyleName() {
        String selectedFeatureTypeStyleName = null;
        selectedFeatureTypeStyleName = this.colorOptionRadioButton.isSelected() ? TRUE_COLOR_OPTION : (this.layerOptionRadioButton.isSelected() ? LAYER_OPTION : LAYER_COLOR_OPTION);
        return selectedFeatureTypeStyleName;
    }

    public JPanel getAdditionalOptionsPanel() {
        if (this.additionalOptionsPanel == null) {
            this.additionalOptionsPanel = new JPanel(new GridBagLayout());
            this.additionalOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.additional-options-according-to-the-simbology-destiny")));
            this.saveForShapeCheckbox = new JCheckBox(I18N.getString("org.saig.jump.widgets.utils.SaveCADLayerSimbologyDialog.adapt-the-simbology-to-shape-file-the-field-names-will-be-shorten-to-11-characters-in-the-simbology"));
            FormUtils.addRowInGBL(this.additionalOptionsPanel, 0, 0, this.saveForShapeCheckbox);
        }
        return this.additionalOptionsPanel;
    }

    public boolean isSaveForShape() {
        return this.saveForShapeCheckbox.isSelected();
    }
}

