/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.widgets.symbology;

import es.kosmo.desktop.gui.dialogs.AbstractOptionsDialog;
import es.kosmo.desktop.plugins.symbology.SLDExportPlugIn;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.SelectFilePanel;

public class SLDExportOptionsDialog
extends AbstractOptionsDialog {
    private static final long serialVersionUID = 1L;
    protected SelectFilePanel fileSelectionPanel;
    protected JPanel exportOptionsPanel;
    protected JRadioButton sld_1_0_versionRadioButton;
    protected JRadioButton sld_1_1_versionRadioButton;
    protected JCheckBox exportUOMAttributesCheckBox;

    public SLDExportOptionsDialog(JFrame parent, boolean modal, String toolName, String toolDescription, String toolImagePath) {
        super(parent, modal, toolName, toolDescription, toolImagePath);
    }

    @Override
    protected JPanel getCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(centerPanel, 0, 0, this.getFileSelectionPanel());
        FormUtils.addRowInGBL(centerPanel, 1, 0, this.getExportOptionsPanel());
        FormUtils.addFiller(centerPanel, 2, 0);
        return centerPanel;
    }

    private SelectFilePanel getFileSelectionPanel() {
        this.fileSelectionPanel = new SelectFilePanel(SLDExportPlugIn.SLD_FILE_DESCRIPTION, new String[]{"sld"}, false);
        Dimension dim = new Dimension(500, 70);
        this.fileSelectionPanel.setMinimumSize(dim);
        this.fileSelectionPanel.setPreferredSize(dim);
        this.fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.symbology.SLDExportOptionsDialog.File")));
        return this.fileSelectionPanel;
    }

    private JPanel getExportOptionsPanel() {
        this.exportOptionsPanel = new JPanel(new GridBagLayout());
        this.exportOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.symbology.SLDExportOptionsDialog.Export-options")));
        JLabel sldVersionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.SLDExportOptionsDialog.SLD-version")) + ":");
        ButtonGroup sldVersionButtonGroup = new ButtonGroup();
        this.sld_1_0_versionRadioButton = new JRadioButton("1.0");
        this.sld_1_1_versionRadioButton = new JRadioButton("1.1");
        sldVersionButtonGroup.add(this.sld_1_0_versionRadioButton);
        sldVersionButtonGroup.add(this.sld_1_1_versionRadioButton);
        this.sld_1_0_versionRadioButton.setSelected(true);
        JLabel exportUOMAttributesLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.SLDExportOptionsDialog.Export-UOM-attributes")) + ":");
        this.exportUOMAttributesCheckBox = new JCheckBox();
        this.exportUOMAttributesCheckBox.setSelected(true);
        int row = 0;
        FormUtils.addRowInGBL((JComponent)this.exportOptionsPanel, row, 0, (JComponent)sldVersionLabel, false, false);
        FormUtils.addRowInGBL((JComponent)this.exportOptionsPanel, row, 30, (JComponent)this.sld_1_0_versionRadioButton, false, false);
        FormUtils.addRowInGBL((JComponent)this.exportOptionsPanel, row++, 60, (JComponent)this.sld_1_1_versionRadioButton, false, false);
        FormUtils.addRowInGBL((JComponent)this.exportOptionsPanel, row, 0, (JComponent)exportUOMAttributesLabel, false, false);
        FormUtils.addRowInGBL((JComponent)this.exportOptionsPanel, row++, 30, (JComponent)this.exportUOMAttributesCheckBox, false, false);
        FormUtils.addFiller(this.exportOptionsPanel, row, 0);
        return this.exportOptionsPanel;
    }

    public File getSelectedFile() {
        return new File(this.fileSelectionPanel.getSelectedPath());
    }

    @Override
    public boolean isInputValid() {
        String warningMessage = this.fileSelectionPanel.firstErrorMessage();
        if (StringUtils.isNotEmpty((String)warningMessage)) {
            DialogFactory.showWarningDialog(this, warningMessage, this.getTitle());
            return false;
        }
        return true;
    }

    public String getSelectedVersion() {
        if (this.sld_1_0_versionRadioButton.isSelected()) {
            return "1.0";
        }
        return "1.1";
    }

    public boolean exportUOMAttributes() {
        return this.exportUOMAttributesCheckBox.isSelected();
    }
}

