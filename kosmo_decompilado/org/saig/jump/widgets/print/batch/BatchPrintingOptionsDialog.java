/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.batch;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.util.JAvailableLayersComboBox;

public class BatchPrintingOptionsDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    protected PrintLayoutFrame frame;
    protected JPanel decorationPanel;
    protected JPanel centerPanel;
    protected JPanel titleOptionsPanel;
    protected JCheckBox labelNameCheckBox;
    protected JCheckBox printLabelInPageCheckBox;
    protected JComboBox attrNamesComboBox;
    protected JPanel envelopeOptionsPanel;
    protected JRadioButton rbLayer;
    protected JRadioButton rbSelection;
    protected JPanel otherOptionsPanel;
    protected JCheckBox oneDocumentCheckBox;
    protected JAvailableLayersComboBox availableLayersComboBox;
    protected OKCancelPanel okCancelPanel;

    public BatchPrintingOptionsDialog(PrintLayoutFrame owner) {
        super(owner, I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Batch-printing"), true);
        this.frame = owner;
        this.initialize();
        this.enableDisable();
        this.refreshAttributes();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        mainPanel.add((Component)this.getDecorationPanel(), "West");
        mainPanel.add((Component)this.getCenterPanel(), "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
    }

    private JPanel getCenterPanel() {
        if (this.centerPanel == null) {
            this.centerPanel = new JPanel(new GridBagLayout());
            FormUtils.addRowInGBL(this.centerPanel, 0, 0, this.getOtherOptionsPanel());
            FormUtils.addRowInGBL(this.centerPanel, 1, 0, this.getEnvelopeOptionsPanel());
            FormUtils.addRowInGBL(this.centerPanel, 2, 0, this.getTitleOptionsPanel());
            FormUtils.addFiller(this.centerPanel, 3, 0);
        }
        return this.centerPanel;
    }

    private JPanel getTitleOptionsPanel() {
        if (this.titleOptionsPanel == null) {
            this.titleOptionsPanel = new JPanel(new GridBagLayout());
            this.titleOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Title")));
            this.labelNameCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Use-field-to-name-the-document"));
            this.printLabelInPageCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Print-field-to-page"));
            JLabel attrNamesLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Field")) + ": ");
            this.attrNamesComboBox = new JComboBox();
            FormUtils.addRowInGBL((JComponent)this.titleOptionsPanel, 0, 0, (JComponent)this.labelNameCheckBox, true, false);
            FormUtils.addRowInGBL((JComponent)this.titleOptionsPanel, 1, 0, (JComponent)this.printLabelInPageCheckBox, true, false);
            FormUtils.addRowInGBL((JComponent)this.titleOptionsPanel, 2, 0, attrNamesLabel, (JComponent)this.attrNamesComboBox, true);
        }
        return this.titleOptionsPanel;
    }

    private JPanel getEnvelopeOptionsPanel() {
        if (this.envelopeOptionsPanel == null) {
            this.envelopeOptionsPanel = new JPanel(new GridBagLayout());
            this.envelopeOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Index")));
            this.rbLayer = new JRadioButton(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Full-layer"));
            this.rbLayer.addActionListener(this);
            this.rbLayer.setSelected(true);
            this.rbSelection = new JRadioButton(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Selected-features"));
            this.rbSelection.addActionListener(this);
            ButtonGroup bg = new ButtonGroup();
            bg.add(this.rbLayer);
            bg.add(this.rbSelection);
            JLabel availableLayersLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Printing-index-layer")) + ": ");
            this.availableLayersComboBox = new JAvailableLayersComboBox(this.frame.getLayerViewPanel().getLayerManager(), false, false, true);
            this.availableLayersComboBox.addActionListener(this);
            FormUtils.addRowInGBL((JComponent)this.envelopeOptionsPanel, 0, 0, (JComponent)this.rbLayer, true, false);
            FormUtils.addRowInGBL((JComponent)this.envelopeOptionsPanel, 1, 0, (JComponent)this.rbSelection, true, false);
            FormUtils.addRowInGBL((JComponent)this.envelopeOptionsPanel, 2, 0, availableLayersLabel, (JComponent)this.availableLayersComboBox, true);
        }
        return this.envelopeOptionsPanel;
    }

    private JPanel getOtherOptionsPanel() {
        if (this.otherOptionsPanel == null) {
            this.otherOptionsPanel = new JPanel(new GridBagLayout());
            this.otherOptionsPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Options")));
            this.oneDocumentCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Print-to-only-one-multipage-document"));
            this.oneDocumentCheckBox.addActionListener(this);
            FormUtils.addRowInGBL(this.otherOptionsPanel, 0, 0, this.oneDocumentCheckBox);
        }
        return this.otherOptionsPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Print"));
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    private void enableDisable() {
        boolean oneDocument = this.oneDocumentCheckBox.isSelected();
        this.labelNameCheckBox.setEnabled(!oneDocument);
    }

    private JPanel getDecorationPanel() {
        if (this.decorationPanel == null) {
            this.decorationPanel = new JPanel(new BorderLayout());
            this.decorationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel imageLabel = new JLabel(IconLoader.icon("toolImages/batch_printing.png"));
            this.decorationPanel.add((Component)imageLabel, "Center");
        }
        return this.decorationPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.okCancelPanel) {
            if (this.okCancelPanel.wasOKPressed()) {
                if (this.isInputValid()) {
                    this.setVisible(false);
                }
            } else {
                this.setVisible(false);
            }
        } else if (e.getSource() == this.oneDocumentCheckBox) {
            this.enableDisable();
        } else if (e.getSource() == this.rbLayer || e.getSource() == this.rbSelection) {
            this.enableDisable();
        } else if (e.getSource() == this.availableLayersComboBox) {
            this.refreshAttributes();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    private boolean isInputValid() {
        return true;
    }

    private void refreshAttributes() {
        Layer selectedLayer = this.availableLayersComboBox.getSelectedLayer();
        this.attrNamesComboBox.removeAllItems();
        if (selectedLayer != null) {
            FeatureSchema schema = selectedLayer.getFeatureSchema();
            ArrayList<String> attrNames = new ArrayList<String>();
            int i = 0;
            while (i < schema.getAttributeCount()) {
                Attribute currentAttr = schema.getAttribute(i);
                if (!currentAttr.getType().equals(AttributeType.GEOMETRY)) {
                    attrNames.add(currentAttr.getName());
                }
                ++i;
            }
            Iterator itAttrNames = attrNames.iterator();
            while (itAttrNames.hasNext()) {
                this.attrNamesComboBox.addItem(itAttrNames.next());
            }
        }
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public Layer getSelectedLayer() {
        return this.availableLayersComboBox.getSelectedLayer();
    }

    public String getSelectedAttrName() {
        return (String)this.attrNamesComboBox.getSelectedItem();
    }

    public boolean useDocumentName() {
        return this.printLabelInPageCheckBox.isSelected();
    }

    public boolean useLabelName() {
        return this.labelNameCheckBox.isSelected();
    }

    public boolean printAllInOneDocument() {
        return this.oneDocumentCheckBox.isSelected();
    }

    public boolean printWholeLayer() {
        return this.rbLayer.isSelected();
    }

    @Override
    public void dispose() {
        this.frame = null;
        super.dispose();
    }
}

