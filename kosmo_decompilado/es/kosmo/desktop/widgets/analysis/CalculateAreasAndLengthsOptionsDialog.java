/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.JXComboBox
 *  org.jdesktop.swingx.JXLabel
 *  org.jdesktop.swingx.JXPanel
 */
package es.kosmo.desktop.widgets.analysis;

import com.vividsolutions.jump.workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn;
import es.kosmo.desktop.gui.dialogs.AbstractControlledOptionsDialog;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class CalculateAreasAndLengthsOptionsDialog
extends AbstractControlledOptionsDialog {
    private static final long serialVersionUID = 1L;
    public static String CALCULATE_PERIMETER_CHECKBOX_TEXT = I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.calculate-length");
    public static String CALCULATE_PERIMETER_FIELD_TEXT = String.valueOf(I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.field-name-to-store-calculated-lengths")) + ": ";
    public static String CALCULATE_LENGTH_CHECKBOX_TEXT = I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.Calculate-lengths");
    public static String CALCULATE_LENGTH_FIELD_TEXT = String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.Field-name-where-the-calculated-length-will-be-saved")) + ": ";
    protected JXPanel optionsPanel;
    protected JCheckBox calculateAreaCheckBox;
    protected JXLabel areaFieldLabel;
    protected JXComboBox areaFieldComboBox;
    protected JCheckBox calculateLengthCheckBox;
    protected JXLabel lengthFieldLabel;
    protected JXComboBox lengthFieldComboBox;
    protected JXLabel applyToLabel;
    protected JRadioButton applyToLayerRadioButton;
    protected JRadioButton applyToSelectedOnlyRadioButton;

    public CalculateAreasAndLengthsOptionsDialog(JFrame parent, boolean modal) {
        super(parent, modal, CalculateAreasAndLengthsPlugIn.NAME, null, null);
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
            this.calculateAreaCheckBox = new JCheckBox(I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.calculate-area"));
            this.calculateAreaCheckBox.setSelected(true);
            this.areaFieldLabel = new JXLabel(String.valueOf(I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.field-name-to-store-calculated-areas")) + ": ");
            this.areaFieldComboBox = new JXComboBox();
            this.calculateLengthCheckBox = new JCheckBox();
            this.calculateLengthCheckBox.setSelected(true);
            this.lengthFieldLabel = new JXLabel();
            this.lengthFieldComboBox = new JXComboBox();
            this.applyToLabel = new JXLabel(String.valueOf(APPLY_TO_LABEL) + ":");
            this.applyToLayerRadioButton = new JRadioButton(WHOLE_LAYER_LABEL);
            this.applyToSelectedOnlyRadioButton = new JRadioButton();
            ButtonGroup applyToButtonGroup = new ButtonGroup();
            applyToButtonGroup.add(this.applyToLayerRadioButton);
            applyToButtonGroup.add(this.applyToSelectedOnlyRadioButton);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 0, 0, (JComponent)this.calculateAreaCheckBox, true, true);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 1, 0, (JLabel)this.areaFieldLabel, (JComponent)this.areaFieldComboBox);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 2, 0, (JComponent)this.calculateLengthCheckBox, true, true);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 3, 0, (JLabel)this.lengthFieldLabel, (JComponent)this.lengthFieldComboBox);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 4, 0, (JLabel)this.applyToLabel, (JComponent)this.applyToLayerRadioButton);
            FormUtils.addRowInGBL((JComponent)this.optionsPanel, 5, 0, new JLabel(""), (JComponent)this.applyToSelectedOnlyRadioButton);
        }
        return this.optionsPanel;
    }

    public JCheckBox getCalculateLengthCheckBox() {
        return this.calculateLengthCheckBox;
    }

    public JXLabel getLengthFieldLabel() {
        return this.lengthFieldLabel;
    }

    public JCheckBox getCalculateAreaCheckBox() {
        return this.calculateAreaCheckBox;
    }

    public JXLabel getAreaFieldLabel() {
        return this.areaFieldLabel;
    }

    public JXComboBox getAreaFieldComboBox() {
        return this.areaFieldComboBox;
    }

    public JComboBox getLengthFieldComboBox() {
        return this.lengthFieldComboBox;
    }

    public JRadioButton getApplyToSelectedOnlyRadioButton() {
        return this.applyToSelectedOnlyRadioButton;
    }

    public JRadioButton getApplyToLayerRadioButton() {
        return this.applyToLayerRadioButton;
    }
}

