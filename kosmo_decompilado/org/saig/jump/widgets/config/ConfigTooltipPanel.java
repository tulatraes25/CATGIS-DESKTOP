/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class ConfigTooltipPanel
extends OptionsPanel {
    private Blackboard blackboard;
    private JPanel tooltipPanel;
    private JCheckBox tooltipCheck;
    public static final String NAME = I18N.getString("org.saig.jump.widgets.config.ConfigTooltipPanel.layer-info");
    public static final Icon ICON = IconLoader.icon("note.png");
    public static final String LAYER_TOOLTIPS_ON = String.valueOf(ConfigTooltipPanel.class.getName()) + " - LAYER_TOOLTIPS";

    public ConfigTooltipPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getTooltipPanel());
        FormUtils.addFiller(this, 2, 0);
    }

    private JPanel getTooltipPanel() {
        if (this.tooltipPanel == null) {
            this.tooltipPanel = new JPanel(new GridBagLayout());
            this.tooltipPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigTooltipPanel.tree-layer-info")));
            this.tooltipCheck = new JCheckBox(I18N.getString("org.saig.jump.widgets.config.ConfigTooltipPanel.show-the-layer-tooltip-in-the-layer-tree"));
            FormUtils.addRowInGBL(this.tooltipPanel, 0, 0, this.tooltipCheck);
        }
        return this.tooltipPanel;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() {
        boolean layerTooltipsOn = PersistentBlackboardPlugIn.get(this.blackboard).get(LAYER_TOOLTIPS_ON, true);
        if (layerTooltipsOn) {
            this.tooltipCheck.setSelected(true);
        }
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(LAYER_TOOLTIPS_ON, this.tooltipCheck.isSelected());
    }

    @Override
    public String validateInput() {
        return null;
    }
}

