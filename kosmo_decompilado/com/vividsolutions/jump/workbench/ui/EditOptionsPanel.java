/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.EditablePlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class EditOptionsPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    private JPanel basicOptionsPanel;
    private JPanel advancedOptionsPanel;
    private JPanel edition3DPanel;
    private JCheckBox preventEditsCheckBox;
    private JCheckBox selectAdjacentsCheckBox;
    private JCheckBox showAngleAndLenghtCheckBox;
    private JCheckBox concurrentEditionCheckBox;
    public static final String ROLLING_BACK_INVALID_EDITS_KEY = String.valueOf(EditTransaction.class.getName()) + " - ROLL_BACK_INVALID_EDITS";
    public static final String ADJACENTS_EDITION_KEY = String.valueOf(EditOptionsPanel.class.getName()) + " - ADJACENTS EDITION";
    public static final String CONCURRENT_EDITION_KEY = String.valueOf(EditOptionsPanel.class.getName()) + " - CONCURRENT EDITION";
    public static final String SHOW_ANGLE_AND_LENGHT_KEY = String.valueOf(EditOptionsPanel.class.getName()) + " - SHOW ANGLE AND LENGHT";

    public EditOptionsPanel() {
        this.initialize();
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).put(ROLLING_BACK_INVALID_EDITS_KEY, this.preventEditsCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).put(SHOW_ANGLE_AND_LENGHT_KEY, this.showAngleAndLenghtCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).put(ADJACENTS_EDITION_KEY, this.selectAdjacentsCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).put(CONCURRENT_EDITION_KEY, this.concurrentEditionCheckBox.isSelected());
    }

    @Override
    public void init() {
        this.preventEditsCheckBox.setSelected(EditOptionsPanel.isRollingBackInvalidEdits());
        this.showAngleAndLenghtCheckBox.setSelected(EditOptionsPanel.isShowAngleAndLenght());
        this.selectAdjacentsCheckBox.setSelected(EditOptionsPanel.isAdjacentEditionActivated());
        this.concurrentEditionCheckBox.setSelected(EditOptionsPanel.isConcurrentEditionActivated());
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getBasicOptionsPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getAdvancedOptionsPanel());
        FormUtils.addFiller(this, 3, 0);
    }

    @Override
    public Icon getIcon() {
        return GUIUtil.toSmallIcon(EditablePlugIn.ICON);
    }

    @Override
    public String getName() {
        return I18N.getString("workbench.ui.plugin.OptionsPlugIn.edit");
    }

    public JPanel getBasicOptionsPanel() {
        if (this.basicOptionsPanel == null) {
            this.basicOptionsPanel = new JPanel(new GridBagLayout());
            this.basicOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("com.vividsolutions.jump.workbench.ui.EditOptionsPanel.basic-edition-options")));
            this.preventEditsCheckBox = new JCheckBox();
            this.preventEditsCheckBox.setText(I18N.getString("workbench.ui.EditOptionsPanel.prevent-edits-resulting-in-invalid-geometries"));
            this.showAngleAndLenghtCheckBox = new JCheckBox();
            this.showAngleAndLenghtCheckBox.setText(I18N.getString("com.vividsolutions.jump.workbench.ui.EditOptionsPanel.Show-and-modify-the-angle-and-length-for-multi-click-draw-tools"));
            FormUtils.addRowInGBL(this.basicOptionsPanel, 0, 0, this.preventEditsCheckBox);
            FormUtils.addRowInGBL(this.basicOptionsPanel, 1, 0, this.showAngleAndLenghtCheckBox);
        }
        return this.basicOptionsPanel;
    }

    public JPanel get3DEditionPanel() {
        if (this.edition3DPanel == null) {
            this.edition3DPanel = new JPanel(new GridBagLayout());
            this.edition3DPanel.setBorder(new TitledBorder(I18N.getString("com.vividsolutions.jump.workbench.ui.EditOptionsPanel.Edition-using-Z")));
        }
        return this.edition3DPanel;
    }

    public JPanel getAdvancedOptionsPanel() {
        if (this.advancedOptionsPanel == null) {
            this.advancedOptionsPanel = new JPanel(new GridBagLayout());
            this.advancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("com.vividsolutions.jump.workbench.ui.EditOptionsPanel.advanced-edition-options")));
            this.selectAdjacentsCheckBox = new JCheckBox();
            this.selectAdjacentsCheckBox.setText(I18N.getString("workbench.ui.EditOptionsPanel.joined-edition-neighbour-border"));
            this.concurrentEditionCheckBox = new JCheckBox();
            this.concurrentEditionCheckBox.setText(I18N.getString("com.vividsolutions.jump.workbench.ui.EditOptionsPanel.database-multiuser-concurrent-edition"));
            FormUtils.addRowInGBL(this.advancedOptionsPanel, 0, 0, this.concurrentEditionCheckBox);
            FormUtils.addRowInGBL(this.advancedOptionsPanel, 1, 0, this.selectAdjacentsCheckBox);
        }
        return this.advancedOptionsPanel;
    }

    public static final boolean isAdjacentEditionActivated() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(ADJACENTS_EDITION_KEY, true);
    }

    public static final boolean isConcurrentEditionActivated() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(CONCURRENT_EDITION_KEY, false);
    }

    public static final boolean isRollingBackInvalidEdits() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(ROLLING_BACK_INVALID_EDITS_KEY, true);
    }

    public static final boolean isShowAngleAndLenght() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SHOW_ANGLE_AND_LENGHT_KEY, false);
    }
}

