/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.analysis;

import es.kosmo.desktop.gui.dialogs.AbstractBasicQueryOptionsDialog;
import es.kosmo.desktop.plugins.analysis.ConvexHullLayerPlugIn;
import es.kosmo.desktop.widgets.analysis.AssignValueToFieldOptionsDialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class ConvexHullLayerOptionsDialog
extends AbstractBasicQueryOptionsDialog {
    private static final long serialVersionUID = 1L;
    protected JPanel selectionPanel;
    protected JRadioButton applyToLayerRadioButton;
    protected JRadioButton applyToSelectedOnlyRadioButton;

    public ConvexHullLayerOptionsDialog(JFrame parent, boolean modal) {
        super(parent, modal, ConvexHullLayerPlugIn.NAME, null, null);
    }

    @Override
    protected JPanel getCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(centerPanel, 0, 0, super.getCenterPanel());
        FormUtils.addRowInGBL(centerPanel, 1, 0, this.getSelectionPanel());
        FormUtils.addFiller(centerPanel, 2, 0);
        return centerPanel;
    }

    private JPanel getSelectionPanel() {
        this.selectionPanel = new JPanel(new GridBagLayout());
        this.selectionPanel.setBorder(BorderFactory.createTitledBorder(APPLY_TO_LABEL));
        this.applyToLayerRadioButton = new JRadioButton(WHOLE_LAYER_LABEL);
        this.applyToSelectedOnlyRadioButton = new JRadioButton();
        ButtonGroup applyToButtonGroup = new ButtonGroup();
        applyToButtonGroup.add(this.applyToLayerRadioButton);
        applyToButtonGroup.add(this.applyToSelectedOnlyRadioButton);
        this.applyToLayerRadioButton.setSelected(true);
        FormUtils.addRowInGBL((JComponent)this.selectionPanel, 0, 0, (JComponent)this.applyToLayerRadioButton, true, true);
        FormUtils.addRowInGBL((JComponent)this.selectionPanel, 1, 0, (JComponent)this.applyToSelectedOnlyRadioButton, true, true);
        return this.selectionPanel;
    }

    public void refresh(String layerName, int numFeaturesSelected) {
        this.setTitle(String.valueOf(I18N.getMessage("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.{0}-options", new Object[]{ConvexHullLayerPlugIn.NAME})) + " - " + layerName);
        this.applyToSelectedOnlyRadioButton.setText(String.valueOf(AssignValueToFieldOptionsDialog.BASE_SELECTION_LABEL) + " (" + I18N.getMessage("es.kosmo.desktop.controllers.analysis.AssignValueToFieldOptionsDialogController.{0}-selected", new Object[]{numFeaturesSelected}) + ")");
        this.applyToSelectedOnlyRadioButton.setEnabled(numFeaturesSelected > 0);
        this.pack();
        FontMetrics fm = this.getFontMetrics(this.getFont());
        int width = fm.stringWidth(this.getTitle()) + 75;
        width = Math.max(width, this.getPreferredSize().width);
        this.setSize(new Dimension(width, this.getPreferredSize().height));
    }

    public boolean useSelectedOnly() {
        return this.applyToSelectedOnlyRadioButton.isSelected();
    }
}

