/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.JXComboBox
 *  org.jdesktop.swingx.JXLabel
 *  org.jdesktop.swingx.JXPanel
 *  org.jdesktop.swingx.JXTextField
 */
package es.kosmo.desktop.widgets.analysis;

import es.kosmo.desktop.gui.dialogs.AbstractControlledOptionsDialog;
import es.kosmo.desktop.plugins.analysis.AssignValueToFieldPlugIn;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class AssignValueToFieldOptionsDialog
extends AbstractControlledOptionsDialog {
    private static final long serialVersionUID = 1L;
    public static final String TITLE = I18N.getString("org.saig.jump.widgets.utils.AssignValueToFieldDialog.Select-attribute-to-the-layer");
    protected JXPanel optionsPanel;
    protected JXLabel fieldNameLabel;
    protected JXComboBox fieldNameComboBox;
    protected JXLabel fieldValueLabel;
    protected JXTextField fieldValueTextField;
    protected JXLabel applyToLabel;
    protected JRadioButton applyToLayerRadioButton;
    protected JRadioButton applyToSelectedOnlyRadioButton;

    public AssignValueToFieldOptionsDialog(JFrame parent, boolean modal) {
        super(parent, modal, AssignValueToFieldPlugIn.NAME, null, null);
    }

    @Override
    protected JPanel getCenterPanel() {
        JXPanel centerPanel = new JXPanel((LayoutManager)new GridBagLayout());
        centerPanel.setOpaque(false);
        FormUtils.addRowInGBL((JComponent)centerPanel, 0, 0, (JComponent)this.getOptionsPanel());
        FormUtils.addFiller((JComponent)centerPanel, 1, 0);
        return centerPanel;
    }

    protected JXPanel getOptionsPanel() {
        if (this.optionsPanel == null) {
            this.optionsPanel = new JXPanel((LayoutManager)new GridBagLayout());
            this.optionsPanel.setBorder((Border)BorderFactory.createTitledBorder(I18N.getString("com.vividsolutions.jump.util.commandline.CommandLine.options")));
            this.fieldNameLabel = new JXLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.AssignValueToFieldDialog.Attribute")) + ": ");
            this.fieldNameComboBox = new JXComboBox();
            this.fieldValueLabel = new JXLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.AssignValueToFieldDialog.New-value")) + ": ");
            this.fieldValueTextField = new JXTextField();
            this.applyToLabel = new JXLabel(String.valueOf(APPLY_TO_LABEL) + ":");
            this.applyToLayerRadioButton = new JRadioButton(WHOLE_LAYER_LABEL);
            this.applyToSelectedOnlyRadioButton = new JRadioButton();
            ButtonGroup applyToButtonGroup = new ButtonGroup();
            applyToButtonGroup.add(this.applyToLayerRadioButton);
            applyToButtonGroup.add(this.applyToSelectedOnlyRadioButton);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 0, 0, (JLabel)this.fieldNameLabel, (JComponent)this.fieldNameComboBox);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 1, 0, (JLabel)this.fieldValueLabel, (JComponent)this.fieldValueTextField);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 2, 0, (JLabel)this.applyToLabel, (JComponent)this.applyToLayerRadioButton);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 3, 0, new JLabel(""), (JComponent)this.applyToSelectedOnlyRadioButton);
        }
        return this.optionsPanel;
    }

    public JXComboBox getFieldNameComboBox() {
        return this.fieldNameComboBox;
    }

    public JXTextField getFieldValueTextField() {
        return this.fieldValueTextField;
    }

    public JRadioButton getApplyToSelectedOnlyRadioButton() {
        return this.applyToSelectedOnlyRadioButton;
    }

    public JRadioButton getApplyToLayerRadioButton() {
        return this.applyToLayerRadioButton;
    }
}

