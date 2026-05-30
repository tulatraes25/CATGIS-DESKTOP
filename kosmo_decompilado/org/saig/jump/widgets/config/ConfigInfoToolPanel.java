/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.FeatureInfoTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class ConfigInfoToolPanel
extends OptionsPanel {
    private Blackboard blackboard;
    private JPanel infoLayersPanel;
    private JPanel configInfoPanel;
    ButtonGroup infoGroup = new ButtonGroup();
    JRadioButton selectedLayersOnlyRadioButton;
    JRadioButton visibleLayersRadioButton;
    ButtonGroup configGroup = new ButtonGroup();
    JRadioButton tableInfoRadioButton;
    JRadioButton formInfoRadioButton;

    public ConfigInfoToolPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getInfoLayersPanel());
        FormUtils.addRowInGBL(this, 2, 0, this.getConfigInfoPanel());
        FormUtils.addFiller(this, 3, 0);
    }

    public JPanel getInfoLayersPanel() {
        if (this.infoLayersPanel == null) {
            this.infoLayersPanel = new JPanel();
            this.infoLayersPanel.setLayout(new GridBagLayout());
            this.infoLayersPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigInfoToolPanel.layers-that-show-information")));
            this.selectedLayersOnlyRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigInfoToolPanel.selected-layers"));
            this.visibleLayersRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigInfoToolPanel.visible-layers"));
            this.infoGroup.add(this.selectedLayersOnlyRadioButton);
            this.infoGroup.add(this.visibleLayersRadioButton);
            this.selectedLayersOnlyRadioButton.setSelected(true);
            FormUtils.addRowInGBL(this.infoLayersPanel, 1, 0, this.selectedLayersOnlyRadioButton);
            FormUtils.addRowInGBL(this.infoLayersPanel, 2, 0, this.visibleLayersRadioButton);
        }
        return this.infoLayersPanel;
    }

    public JPanel getConfigInfoPanel() {
        if (this.configInfoPanel == null) {
            this.configInfoPanel = new JPanel();
            this.configInfoPanel.setLayout(new GridBagLayout());
            this.configInfoPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigInfoToolPanel.info-panel")));
            this.tableInfoRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigInfoToolPanel.info-as-a-table"));
            this.formInfoRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigInfoToolPanel.info-as-a-form"));
            this.configGroup.add(this.tableInfoRadioButton);
            this.configGroup.add(this.formInfoRadioButton);
            this.tableInfoRadioButton.setSelected(true);
            FormUtils.addRowInGBL(this.configInfoPanel, 1, 0, this.tableInfoRadioButton);
            FormUtils.addRowInGBL(this.configInfoPanel, 2, 0, this.formInfoRadioButton);
        }
        return this.configInfoPanel;
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(FeatureInfoTool.VISIBLE_LAYERS_KEY, this.visibleLayersRadioButton.isSelected());
        PersistentBlackboardPlugIn.get(this.blackboard).put(FeatureInfoTool.CONFIG_INFO_KEY, this.tableInfoRadioButton.isSelected());
    }

    @Override
    public void init() {
        boolean allVisibles = PersistentBlackboardPlugIn.get(this.blackboard).get(FeatureInfoTool.VISIBLE_LAYERS_KEY, true);
        this.visibleLayersRadioButton.setSelected(allVisibles);
        boolean asTable = PersistentBlackboardPlugIn.get(this.blackboard).get(FeatureInfoTool.CONFIG_INFO_KEY, true);
        this.tableInfoRadioButton.setSelected(asTable);
    }

    @Override
    public Icon getIcon() {
        return GUIUtil.toSmallIcon(FeatureInfoTool.ICON);
    }

    @Override
    public String getName() {
        return FeatureInfoTool.NAME;
    }
}

