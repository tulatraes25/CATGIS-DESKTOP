/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.hiperlink;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.HiperLink;
import com.vividsolutions.jump.feature.HiperLinkCompound;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.Table;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;

public class HiperLinkConfigurationDialog
extends JDialog {
    private static final int HIPERLINK_SIMPLE = 0;
    private static final int HIPERLINK_COMPOUND = 1;
    private int hiperLinkType = 0;
    private WorkbenchContext context;
    private JPanel cardPanel;
    private JComboBox sourceHiperLinkFieldComboBox;
    private JComboBox sourceKeysFieldComboBox;
    private JComboBox tableComboBox;
    private JComboBox descriptionFieldTableComboBox;
    private JComboBox fieldTargetKeysComboBox;
    private JComboBox targetHiperLinkFieldComboBox;
    private JComboBox descriptionHiperLinkFieldComboBox;
    private JCheckBox simple;
    private JCheckBox compound;
    private JButton desactivarJButton = new JButton(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.Off"));
    private boolean deactivate = false;
    private Layer layer;
    private boolean exitOk = false;
    private static final String NO_DESCRIPTION_VALUE = "---------";

    public HiperLinkConfigurationDialog(JFrame parent, WorkbenchContext context, boolean modal, HiperLink hiperlink) {
        super((Frame)parent, modal);
        this.context = context;
        this.setTitle(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.hiperlink-configuration"));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        Layerable[] layers = context.getLayerNamePanel().getSelectedLayers();
        if (layers != null) {
            this.layer = (Layer)layers[0];
        }
        JPanel selectorPanel = this.createSelectorPanel(hiperlink);
        this.cardPanel = this.createCardLayoutPanel(hiperlink);
        OKCancelPanel okCancelPanel = this.createOKcancelPanel();
        okCancelPanel.add(this.desactivarJButton);
        this.desactivarJButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                HiperLinkConfigurationDialog.this.deactivate = true;
                HiperLinkConfigurationDialog.this.exitOk = true;
                HiperLinkConfigurationDialog.this.setVisible(false);
            }
        });
        FormUtils.addRowInGBL(mainPanel, 0, 0, selectorPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.cardPanel);
        FormUtils.addRowInGBL(mainPanel, 2, 0, okCancelPanel);
        this.setContentPane(mainPanel);
        this.pack();
        if (hiperlink instanceof HiperLinkCompound) {
            this.compound.setSelected(true);
            this.compound.getActionListeners()[0].actionPerformed(null);
        } else {
            this.simple.setSelected(true);
        }
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel createSelectorPanel(HiperLink hiperlink) {
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new GridBagLayout());
        selectorPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.hiperlink-type")));
        ButtonGroup agrupacion = new ButtonGroup();
        this.simple = new JCheckBox(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.basic-hiperlink"));
        this.simple.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CardLayout cl = (CardLayout)HiperLinkConfigurationDialog.this.cardPanel.getLayout();
                cl.show(HiperLinkConfigurationDialog.this.cardPanel, "SIMPLE");
                HiperLinkConfigurationDialog.this.hiperLinkType = 0;
            }
        });
        this.compound = new JCheckBox(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.compound-hiperlink"));
        this.compound.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CardLayout cl = (CardLayout)HiperLinkConfigurationDialog.this.cardPanel.getLayout();
                cl.show(HiperLinkConfigurationDialog.this.cardPanel, "COMPOUND");
                HiperLinkConfigurationDialog.this.hiperLinkType = 1;
            }
        });
        if (this.context.getDataManager().size() == 0) {
            this.compound.setEnabled(false);
        }
        agrupacion.add(this.simple);
        agrupacion.add(this.compound);
        this.simple.setSelected(true);
        FormUtils.addRowInGBL(selectorPanel, 0, 0, this.simple);
        FormUtils.addRowInGBL(selectorPanel, 1, 0, this.compound);
        return selectorPanel;
    }

    private JPanel createSimplePanel(HiperLink hiperlink) {
        JPanel simplePanel = new JPanel();
        simplePanel.setLayout(new GridBagLayout());
        FeatureSchema schema = this.layer.getFeatureSchema();
        ArrayList<String> attributeNames = new ArrayList<String>();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute element = schema.getAttribute(i);
            if (element.getType().toJavaClass().equals(String.class) && !element.isPrimaryKey()) {
                attributeNames.add(element.getName());
            }
            ++i;
        }
        Collections.sort(attributeNames);
        this.sourceHiperLinkFieldComboBox = new JComboBox<Object>(attributeNames.toArray());
        attributeNames.add(NO_DESCRIPTION_VALUE);
        Collections.sort(attributeNames);
        this.descriptionHiperLinkFieldComboBox = new JComboBox<Object>(attributeNames.toArray());
        JLabel hiperLinkLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.hiperlink-field")) + " :");
        FormUtils.addRowInGBL((JComponent)simplePanel, 0, 0, hiperLinkLabel, (JComponent)this.sourceHiperLinkFieldComboBox);
        JLabel descriptionLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.Description-field")) + " :");
        FormUtils.addRowInGBL((JComponent)simplePanel, 1, 0, descriptionLabel, (JComponent)this.descriptionHiperLinkFieldComboBox);
        FormUtils.addFiller(simplePanel, 2, 0);
        if (hiperlink != null) {
            this.sourceHiperLinkFieldComboBox.setSelectedItem(hiperlink.getFieldWithHiperLink());
            this.descriptionHiperLinkFieldComboBox.setSelectedItem(hiperlink.getFieldDescription());
        }
        return simplePanel;
    }

    private JPanel createCompoundPanel(HiperLink hiperlink) {
        JPanel compoundPanel = new JPanel();
        compoundPanel.setLayout(new GridBagLayout());
        JLabel sourceKeyLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.layer-key-field")) + " :");
        this.fieldTargetKeysComboBox = new JComboBox();
        this.targetHiperLinkFieldComboBox = new JComboBox();
        this.descriptionFieldTableComboBox = new JComboBox();
        FeatureSchema layerSchema = this.layer.getFeatureSchema();
        ArrayList<String> attributeNames = new ArrayList<String>();
        int i = 0;
        while (i < layerSchema.getAttributeCount()) {
            Attribute element = layerSchema.getAttribute(i);
            if (!element.getType().equals(AttributeType.GEOMETRY)) {
                attributeNames.add(element.getName());
            }
            ++i;
        }
        Collections.sort(attributeNames);
        this.sourceKeysFieldComboBox = new JComboBox<Object>(attributeNames.toArray());
        FormUtils.addRowInGBL((JComponent)compoundPanel, 0, 0, sourceKeyLabel, (JComponent)this.sourceKeysFieldComboBox);
        JLabel tableLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.table")) + " :");
        List<Table> tables = this.context.getDataManager().getRealTables();
        this.tableComboBox = new JComboBox<Object>(tables.toArray());
        this.tableComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Table selectedTable = (Table)HiperLinkConfigurationDialog.this.tableComboBox.getSelectedItem();
                FeatureSchema schema = selectedTable.getSchema();
                ArrayList<String> validFieldTargetKeysAttrNames = new ArrayList<String>();
                ArrayList<String> validTargetHiperLinkFieldAttrNames = new ArrayList<String>();
                int i = 0;
                while (i < schema.getAttributeCount()) {
                    Attribute element = schema.getAttribute(i);
                    if (!element.getType().equals(AttributeType.GEOMETRY)) {
                        validFieldTargetKeysAttrNames.add(element.getName());
                    }
                    if (element.getType().toJavaClass().equals(String.class)) {
                        validTargetHiperLinkFieldAttrNames.add(element.getName());
                    }
                    ++i;
                }
                Collections.sort(validFieldTargetKeysAttrNames);
                Collections.sort(validTargetHiperLinkFieldAttrNames);
                HiperLinkConfigurationDialog.this.fieldTargetKeysComboBox.removeAllItems();
                HiperLinkConfigurationDialog.this.descriptionFieldTableComboBox.removeAllItems();
                HiperLinkConfigurationDialog.this.descriptionFieldTableComboBox.addItem(HiperLinkConfigurationDialog.NO_DESCRIPTION_VALUE);
                for (String name : validFieldTargetKeysAttrNames) {
                    HiperLinkConfigurationDialog.this.fieldTargetKeysComboBox.addItem(name);
                    HiperLinkConfigurationDialog.this.descriptionFieldTableComboBox.addItem(name);
                }
                HiperLinkConfigurationDialog.this.targetHiperLinkFieldComboBox.removeAllItems();
                for (String name : validTargetHiperLinkFieldAttrNames) {
                    HiperLinkConfigurationDialog.this.targetHiperLinkFieldComboBox.addItem(name);
                }
            }
        });
        if (tables.size() > 0) {
            Table selectedTable = tables.get(0);
            FeatureSchema tableSchema = selectedTable.getSchema();
            ArrayList<String> validFieldTargetKeysAttrNames = new ArrayList<String>();
            ArrayList<String> validTargetHiperLinkFieldAttrNames = new ArrayList<String>();
            int i2 = 0;
            while (i2 < tableSchema.getAttributeCount()) {
                Attribute element = tableSchema.getAttribute(i2);
                if (!element.getType().equals(AttributeType.GEOMETRY)) {
                    validFieldTargetKeysAttrNames.add(element.getName());
                }
                if (element.getType().toJavaClass().equals(String.class)) {
                    validTargetHiperLinkFieldAttrNames.add(element.getName());
                }
                ++i2;
            }
            Collections.sort(validFieldTargetKeysAttrNames);
            Collections.sort(validTargetHiperLinkFieldAttrNames);
            this.fieldTargetKeysComboBox = new JComboBox<Object>(validFieldTargetKeysAttrNames.toArray());
            this.targetHiperLinkFieldComboBox = new JComboBox<Object>(validTargetHiperLinkFieldAttrNames.toArray());
            validTargetHiperLinkFieldAttrNames.add(NO_DESCRIPTION_VALUE);
            Collections.sort(validTargetHiperLinkFieldAttrNames);
            this.descriptionFieldTableComboBox = new JComboBox<Object>(validTargetHiperLinkFieldAttrNames.toArray());
        }
        FormUtils.addRowInGBL((JComponent)compoundPanel, 1, 0, tableLabel, (JComponent)this.tableComboBox);
        JLabel fieldTargetKeyLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.table-key-field")) + " :");
        FormUtils.addRowInGBL((JComponent)compoundPanel, 2, 0, fieldTargetKeyLabel, (JComponent)this.fieldTargetKeysComboBox);
        JLabel hiperLinkLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.hiperlink-field")) + " :");
        FormUtils.addRowInGBL((JComponent)compoundPanel, 3, 0, hiperLinkLabel, (JComponent)this.targetHiperLinkFieldComboBox);
        JLabel descrptionLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.hiperlink.HiperLinkConfigurationDialog.Description-field")) + " :");
        FormUtils.addRowInGBL((JComponent)compoundPanel, 4, 0, descrptionLabel, (JComponent)this.descriptionFieldTableComboBox);
        if (hiperlink != null && hiperlink instanceof HiperLinkCompound) {
            HiperLinkCompound hc = (HiperLinkCompound)hiperlink;
            this.fieldTargetKeysComboBox.setSelectedItem(hc.getKeyFieldTarget());
            this.targetHiperLinkFieldComboBox.setSelectedItem(hc.getFieldWithHiperLink());
            this.descriptionFieldTableComboBox.setSelectedItem(hc.getFieldDescription());
            this.sourceHiperLinkFieldComboBox.setSelectedItem(hc.getKeyFieldSource());
            this.sourceKeysFieldComboBox.setSelectedItem(hc.getKeyFieldSource());
        }
        return compoundPanel;
    }

    private JPanel createCardLayoutPanel(HiperLink hiperlink) {
        this.cardPanel = new JPanel();
        this.cardPanel.setLayout(new CardLayout());
        this.cardPanel.add((Component)this.createSimplePanel(hiperlink), "SIMPLE");
        this.cardPanel.add((Component)this.createCompoundPanel(hiperlink), "COMPOUND");
        return this.cardPanel;
    }

    private OKCancelPanel createOKcancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        okCancelPanel.setLayout(gbPaneOKCancel);
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (okCancelPanel.wasOKPressed()) {
                    HiperLinkConfigurationDialog.this.exitOk = true;
                } else {
                    HiperLinkConfigurationDialog.this.exitOk = false;
                }
                HiperLinkConfigurationDialog.this.setVisible(false);
            }
        });
        return okCancelPanel;
    }

    public boolean isOk() {
        return this.exitOk;
    }

    public HiperLink getHiperLink() {
        if (this.deactivate) {
            return null;
        }
        HiperLink hiperLink = null;
        switch (this.hiperLinkType) {
            case 0: {
                if (this.sourceHiperLinkFieldComboBox.getSelectedItem() == null || ((String)this.sourceHiperLinkFieldComboBox.getSelectedItem()).equals("")) {
                    return null;
                }
                String descriptionFieldValue = (String)this.descriptionHiperLinkFieldComboBox.getSelectedItem();
                if (descriptionFieldValue == NO_DESCRIPTION_VALUE) {
                    descriptionFieldValue = null;
                }
                hiperLink = new HiperLink((String)this.sourceHiperLinkFieldComboBox.getSelectedItem());
                hiperLink.setFieldDescription(descriptionFieldValue);
                break;
            }
            case 1: {
                if (this.targetHiperLinkFieldComboBox.getSelectedItem() == null || ((String)this.targetHiperLinkFieldComboBox.getSelectedItem()).equals("")) {
                    return null;
                }
                String descriptionTableFieldValue = (String)this.descriptionFieldTableComboBox.getSelectedItem();
                if (descriptionTableFieldValue == NO_DESCRIPTION_VALUE) {
                    descriptionTableFieldValue = null;
                }
                HiperLinkCompound compound = new HiperLinkCompound((String)this.targetHiperLinkFieldComboBox.getSelectedItem(), (String)this.sourceKeysFieldComboBox.getSelectedItem(), (String)this.fieldTargetKeysComboBox.getSelectedItem());
                compound.setFieldDescription(descriptionTableFieldValue);
                compound.setTable((Table)this.tableComboBox.getSelectedItem());
                hiperLink = compound;
                break;
            }
        }
        return hiperLink;
    }

    public Layer getLayer() {
        return this.layer;
    }
}

