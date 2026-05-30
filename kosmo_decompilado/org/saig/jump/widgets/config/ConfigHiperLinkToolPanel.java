/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.hiperlink.HiperLinkCursorTool;
import org.saig.jump.widgets.util.SelectDirectoryPanel;

public class ConfigHiperLinkToolPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    private Blackboard blackboard;
    private JPanel infoLayersPanel;
    private ButtonGroup infoGroup = new ButtonGroup();
    private JRadioButton selectedLayersOnlyRadioButton;
    private JRadioButton visibleLayersRadioButton;
    private JPanel relativePathsOptionsPanel;
    private SelectDirectoryPanel relativePathDirectoryPanel;
    private JPanel otherOptionsPanel;
    private JCheckBox showImagesInInternalViewerCheckBox;

    public ConfigHiperLinkToolPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getInfoLayersPanel());
        FormUtils.addRowInGBL(this, 1, 0, this.getRelativePathsOptionsPanel());
        FormUtils.addRowInGBL(this, 2, 0, this.getOtherOptionsPanel());
        FormUtils.addFiller(this, 3, 0);
    }

    public JPanel getInfoLayersPanel() {
        if (this.infoLayersPanel == null) {
            this.infoLayersPanel = new JPanel();
            this.infoLayersPanel.setLayout(new GridBagLayout());
            this.infoLayersPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigHiperLinkToolPanel.layers-that-show-hiperlinks")));
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

    private JPanel getRelativePathsOptionsPanel() {
        if (this.relativePathsOptionsPanel == null) {
            this.relativePathsOptionsPanel = new JPanel();
            this.relativePathsOptionsPanel.setLayout(new GridBagLayout());
            this.relativePathsOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigHiperLinkToolPanel.Relative-paths")));
            this.relativePathDirectoryPanel = new SelectDirectoryPanel(false);
            FormUtils.addRowInGBL(this.relativePathsOptionsPanel, 0, 0, this.relativePathDirectoryPanel);
            FormUtils.addFiller(this.relativePathsOptionsPanel, 1, 0);
        }
        return this.relativePathsOptionsPanel;
    }

    public JPanel getOtherOptionsPanel() {
        if (this.otherOptionsPanel == null) {
            this.otherOptionsPanel = new JPanel(new GridBagLayout());
            this.otherOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.config.ConfigHiperLinkToolPanel.Other-options")));
            this.showImagesInInternalViewerCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.config.ConfigHiperLinkToolPanel.Show-the-images-in-the-Kosmo-internal-viewer"));
            FormUtils.addRowInGBL(this.otherOptionsPanel, 0, 0, this.showImagesInInternalViewerCheckBox);
            FormUtils.addFiller(this.otherOptionsPanel, 1, 0);
        }
        return this.otherOptionsPanel;
    }

    @Override
    public String validateInput() {
        boolean isValid = this.relativePathDirectoryPanel.isInputValid();
        if (!isValid) {
            return this.relativePathDirectoryPanel.firstErrorMessage();
        }
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(HiperLinkCursorTool.VISIBLE_LAYERS_KEY, this.visibleLayersRadioButton.isSelected());
        PersistentBlackboardPlugIn.get(this.blackboard).put(HiperLinkCursorTool.USE_INTERNAL_VIEWER_FOR_IMAGES, this.showImagesInInternalViewerCheckBox.isSelected());
        PersistentBlackboardPlugIn.get(this.blackboard).put(HiperLinkCursorTool.RELATIVE_DIRECTORY_PATH, this.relativePathDirectoryPanel.getSelectedPath());
    }

    @Override
    public void init() {
        boolean allVisibles = PersistentBlackboardPlugIn.get(this.blackboard).get(HiperLinkCursorTool.VISIBLE_LAYERS_KEY, true);
        boolean showImagesInInternalViewer = PersistentBlackboardPlugIn.get(this.blackboard).get(HiperLinkCursorTool.USE_INTERNAL_VIEWER_FOR_IMAGES, true);
        String relativeDirectoryPath = (String)PersistentBlackboardPlugIn.get(this.blackboard).get(HiperLinkCursorTool.RELATIVE_DIRECTORY_PATH, null);
        this.visibleLayersRadioButton.setSelected(allVisibles);
        this.showImagesInInternalViewerCheckBox.setSelected(showImagesInInternalViewer);
        if (relativeDirectoryPath != null) {
            this.relativePathDirectoryPanel.setSelectedPath(relativeDirectoryPath);
        }
    }

    @Override
    public Icon getIcon() {
        return GUIUtil.toSmallIcon(HiperLinkCursorTool.ICON);
    }

    @Override
    public String getName() {
        return HiperLinkCursorTool.NAME;
    }
}

