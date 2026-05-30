/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.renderer.RenderingHintsManager;
import org.saig.jump.lang.I18N;

public class ConfigRenderOptionsPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    public static final String NAME = I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Render");
    public static final Icon ICON = IconLoader.icon("monitor.png");
    public static final String KEY_LINE_ANTIALIASING_ON = String.valueOf(ConfigRenderOptionsPanel.class.getName()) + " - LINE_ANTIALIASING";
    public static final String KEY_TEXT_ANTIALIASING_ON = String.valueOf(ConfigRenderOptionsPanel.class.getName()) + " - TEXT_ANTIALIASING";
    public static final String KEY_QUALITY = String.valueOf(ConfigRenderOptionsPanel.class.getName()) + " - QUALITY";
    private JPanel lineAntialiasingPanel;
    private ButtonGroup lineAntialiasingButtonGroup;
    private JRadioButton lineAntialiasingOnRadioButton;
    private JRadioButton lineAntialiasingOffRadioButton;
    private JPanel textAntialiasingPanel;
    private ButtonGroup textAntialiasingButtonGroup;
    private JRadioButton textAntialiasingOnRadioButton;
    private JRadioButton textAntialiasingOffRadioButton;
    private JPanel qualityOrSpeedPanel;
    private ButtonGroup qualityOrSpeedButtonGroup;
    private JRadioButton qualityRadioButton;
    private JRadioButton speedRadioButton;
    private Blackboard blackboard;

    public ConfigRenderOptionsPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 1, 0, this.getLineAntialiasingPanel());
        FormUtils.addRowInGBL(this, 2, 0, this.getTextAntialiasingPanel());
        FormUtils.addRowInGBL(this, 3, 0, this.getQualityOrSpeedPanel());
        FormUtils.addFiller(this, 4, 0);
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_LINE_ANTIALIASING_ON, this.lineAntialiasingOnRadioButton.isSelected());
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_TEXT_ANTIALIASING_ON, this.textAntialiasingOnRadioButton.isSelected());
        PersistentBlackboardPlugIn.get(this.blackboard).put(KEY_QUALITY, this.qualityRadioButton.isSelected());
        RenderingHintsManager.refreshRenderingHints();
        if (JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel() != null) {
            JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().repaint();
        }
    }

    @Override
    public void init() {
        boolean lineAntialiasingOn = PersistentBlackboardPlugIn.get(this.blackboard).get(KEY_LINE_ANTIALIASING_ON, true);
        boolean textAntialiasingOn = PersistentBlackboardPlugIn.get(this.blackboard).get(KEY_TEXT_ANTIALIASING_ON, false);
        boolean quality = PersistentBlackboardPlugIn.get(this.blackboard).get(KEY_QUALITY, false);
        if (lineAntialiasingOn) {
            this.lineAntialiasingOnRadioButton.setSelected(true);
        } else {
            this.lineAntialiasingOffRadioButton.setSelected(true);
        }
        if (textAntialiasingOn) {
            this.textAntialiasingOnRadioButton.setSelected(true);
        } else {
            this.textAntialiasingOffRadioButton.setSelected(true);
        }
        if (quality) {
            this.qualityRadioButton.setSelected(true);
        } else {
            this.speedRadioButton.setSelected(true);
        }
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public JPanel getLineAntialiasingPanel() {
        if (this.lineAntialiasingPanel == null) {
            this.lineAntialiasingPanel = new JPanel(new GridBagLayout());
            this.lineAntialiasingPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Lines-antialiasing")));
            this.lineAntialiasingOnRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Enabled"));
            this.lineAntialiasingOffRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Disabled"));
            this.lineAntialiasingButtonGroup = new ButtonGroup();
            this.lineAntialiasingButtonGroup.add(this.lineAntialiasingOnRadioButton);
            this.lineAntialiasingButtonGroup.add(this.lineAntialiasingOffRadioButton);
            FormUtils.addRowInGBL(this.lineAntialiasingPanel, 0, 0, this.lineAntialiasingOnRadioButton);
            FormUtils.addRowInGBL(this.lineAntialiasingPanel, 1, 0, this.lineAntialiasingOffRadioButton);
        }
        return this.lineAntialiasingPanel;
    }

    public JPanel getQualityOrSpeedPanel() {
        if (this.qualityOrSpeedPanel == null) {
            this.qualityOrSpeedPanel = new JPanel(new GridBagLayout());
            this.qualityOrSpeedPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Quality")));
            this.qualityRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.More-quality"));
            this.speedRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.More-speed"));
            this.qualityOrSpeedButtonGroup = new ButtonGroup();
            this.qualityOrSpeedButtonGroup.add(this.qualityRadioButton);
            this.qualityOrSpeedButtonGroup.add(this.speedRadioButton);
            FormUtils.addRowInGBL(this.qualityOrSpeedPanel, 0, 0, this.qualityRadioButton);
            FormUtils.addRowInGBL(this.qualityOrSpeedPanel, 1, 0, this.speedRadioButton);
        }
        return this.qualityOrSpeedPanel;
    }

    public JPanel getTextAntialiasingPanel() {
        if (this.textAntialiasingPanel == null) {
            this.textAntialiasingPanel = new JPanel(new GridBagLayout());
            this.textAntialiasingPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Text-antialiasing")));
            this.textAntialiasingOnRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Enabled"));
            this.textAntialiasingOffRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.config.ConfigRenderOptionsPanel.Disabled"));
            this.textAntialiasingButtonGroup = new ButtonGroup();
            this.textAntialiasingButtonGroup.add(this.textAntialiasingOnRadioButton);
            this.textAntialiasingButtonGroup.add(this.textAntialiasingOffRadioButton);
            FormUtils.addRowInGBL(this.textAntialiasingPanel, 0, 0, this.textAntialiasingOnRadioButton);
            FormUtils.addRowInGBL(this.textAntialiasingPanel, 1, 0, this.textAntialiasingOffRadioButton);
        }
        return this.textAntialiasingPanel;
    }
}

