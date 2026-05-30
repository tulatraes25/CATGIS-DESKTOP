/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.utils.topology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.relations.topology.ITopologyBinaryRelation;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.FilterConfigPanel;
import org.saig.jump.widgets.util.JAvailableLayersComboBox;
import org.saig.jump.widgets.utils.topology.JAvailableTopologyRulesComboBox;
import org.saig.jump.widgets.utils.topology.TopologyRulesTableModel;

public class AddNewTopologyRulePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private JAvailableLayersComboBox availableSourceLayersCombobox;
    private FilterConfigPanel entrySourceLayerFilterPanel;
    private JLabel targetLayerLabel;
    private JAvailableLayersComboBox availableTargetLayersCombobox;
    private JLabel targetFilterLabel;
    private FilterConfigPanel entryTargetLayerFilterPanel;
    private JAvailableTopologyRulesComboBox rulesCombobox;
    private FilterConfigPanel conditionFilterPanel;
    private JCheckBox enabledCheckBox;
    private boolean onlyOneLayerMode = false;

    public AddNewTopologyRulePanel(String filterLayerName) {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "add-new-topological-rule")));
        ArrayList<String> validSourceLayers = new ArrayList<String>();
        if (!StringUtils.isEmpty((String)filterLayerName)) {
            validSourceLayers.add(filterLayerName);
        }
        JLabel sourceLayerLabel = new JLabel(String.valueOf(TopologyRulesTableModel.COLUMN_NAMES[0]) + ":");
        this.availableSourceLayersCombobox = new JAvailableLayersComboBox(JUMPWorkbench.getFrameInstance().getContext().getLayerManager(), false, false, true, validSourceLayers, new ArrayList<String>());
        this.availableSourceLayersCombobox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddNewTopologyRulePanel.this.updateGUI();
            }
        });
        JLabel sourceFilterLabel = new JLabel(String.valueOf(TopologyRulesTableModel.COLUMN_NAMES[1]) + ":");
        this.entrySourceLayerFilterPanel = new FilterConfigPanel();
        JLabel ruleLabel = new JLabel(String.valueOf(TopologyRulesTableModel.COLUMN_NAMES[2]) + ":");
        this.rulesCombobox = new JAvailableTopologyRulesComboBox(this.availableSourceLayersCombobox.getItemCount() == 1);
        this.rulesCombobox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AddNewTopologyRulePanel.this.updateGUI();
            }
        });
        JLabel ruleFilterLabel = new JLabel(String.valueOf(TopologyRulesTableModel.COLUMN_NAMES[3]) + ":");
        this.conditionFilterPanel = new FilterConfigPanel();
        this.targetLayerLabel = new JLabel(String.valueOf(TopologyRulesTableModel.COLUMN_NAMES[4]) + ":");
        this.availableTargetLayersCombobox = new JAvailableLayersComboBox(JUMPWorkbench.getFrameInstance().getContext().getLayerManager(), false, false, true, null, new ArrayList<String>());
        this.availableTargetLayersCombobox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Layer targetLayer = AddNewTopologyRulePanel.this.availableTargetLayersCombobox.getSelectedLayer();
                if (targetLayer != null) {
                    String associatedTargetLayerName = AddNewTopologyRulePanel.this.entryTargetLayerFilterPanel.getAssociatedLayerName();
                    AddNewTopologyRulePanel.this.rulesCombobox.setTargetLayer(targetLayer);
                    if (!targetLayer.getName().equals(associatedTargetLayerName)) {
                        AddNewTopologyRulePanel.this.entryTargetLayerFilterPanel.setAssociatedLayerName(targetLayer.getName());
                        AddNewTopologyRulePanel.this.entryTargetLayerFilterPanel.setFilter(null);
                    }
                } else {
                    AddNewTopologyRulePanel.this.rulesCombobox.setTargetLayer(null);
                }
            }
        });
        this.targetFilterLabel = new JLabel(String.valueOf(TopologyRulesTableModel.COLUMN_NAMES[5]) + ":");
        this.entryTargetLayerFilterPanel = new FilterConfigPanel();
        JLabel enabledLabel = new JLabel(String.valueOf(TopologyRulesTableModel.COLUMN_NAMES[6]) + ":");
        this.enabledCheckBox = new JCheckBox();
        this.enabledCheckBox.setSelected(true);
        this.updateGUI();
        FormUtils.addRowInGBL((JComponent)this, 0, 0, sourceLayerLabel, (JComponent)this.availableSourceLayersCombobox, false);
        FormUtils.addRowInGBL((JComponent)this, 0, 30, sourceFilterLabel, (JComponent)this.entrySourceLayerFilterPanel);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, ruleLabel, (JComponent)this.rulesCombobox);
        FormUtils.addRowInGBL((JComponent)this, 1, 30, ruleFilterLabel, (JComponent)this.conditionFilterPanel);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, this.targetLayerLabel, (JComponent)this.availableTargetLayersCombobox, false);
        FormUtils.addRowInGBL((JComponent)this, 2, 30, this.targetFilterLabel, (JComponent)this.entryTargetLayerFilterPanel);
        FormUtils.addRowInGBL((JComponent)this, 3, 0, enabledLabel, (JComponent)this.enabledCheckBox);
    }

    protected void updateGUI() {
        ITopologyRelation relation = (ITopologyRelation)this.rulesCombobox.getSelectedItem();
        boolean isBinaryRelation = relation instanceof ITopologyBinaryRelation;
        this.targetLayerLabel.setEnabled(isBinaryRelation);
        this.availableTargetLayersCombobox.setEnabled(isBinaryRelation);
        this.targetFilterLabel.setEnabled(isBinaryRelation);
        this.entryTargetLayerFilterPanel.setEditable(isBinaryRelation);
        Layer sourceLayer = this.availableSourceLayersCombobox.getSelectedLayer();
        if (sourceLayer != null) {
            this.rulesCombobox.setSourceLayer(sourceLayer);
            this.entrySourceLayerFilterPanel.setAssociatedLayerName(sourceLayer.getName());
            this.conditionFilterPanel.setAssociatedLayerName(sourceLayer.getName());
        } else {
            this.rulesCombobox.setSourceLayer(null);
        }
        if (isBinaryRelation) {
            Layer newTargetLayer;
            Layer targetLayer = this.availableTargetLayersCombobox.getSelectedLayer();
            if (sourceLayer != null) {
                ArrayList<String> nonValidNames = new ArrayList<String>();
                nonValidNames.add(sourceLayer.getName());
                this.availableTargetLayersCombobox.setNonValidLayerNames(nonValidNames);
                this.availableTargetLayersCombobox.refresh();
                this.entrySourceLayerFilterPanel.setAssociatedLayerName(sourceLayer.getName());
            }
            this.availableTargetLayersCombobox.refresh();
            if (targetLayer != null) {
                this.availableTargetLayersCombobox.setSelectedItem(targetLayer);
                if (this.availableTargetLayersCombobox.getSelectedLayer() != targetLayer) {
                    this.entryTargetLayerFilterPanel.setFilter(null);
                }
            }
            if ((newTargetLayer = this.availableTargetLayersCombobox.getSelectedLayer()) != null) {
                this.rulesCombobox.setTargetLayer(this.availableTargetLayersCombobox.getSelectedLayer());
            } else {
                this.rulesCombobox.setTargetLayer(null);
            }
            Layer selectedTargetLayer = (Layer)this.availableTargetLayersCombobox.getSelectedItem();
            if (selectedTargetLayer != null) {
                this.entryTargetLayerFilterPanel.setAssociatedLayerName(selectedTargetLayer.getName());
            }
        } else if (!isBinaryRelation) {
            this.availableTargetLayersCombobox.removeAllItems();
            this.entryTargetLayerFilterPanel.setFilter(null);
        }
    }

    public ITopologyRelation getConfiguredTopologyRelation() {
        ITopologyRelation relation = (ITopologyRelation)this.rulesCombobox.getSelectedItem();
        if (relation != null) {
            relation.setSourceLayerName(this.availableSourceLayersCombobox.getSelectedLayer().getName());
            relation.setEntrySourceFilter(this.entrySourceLayerFilterPanel.getFilter());
            relation.setAlphanumericFilter(this.conditionFilterPanel.getFilter());
            if (relation instanceof ITopologyBinaryRelation) {
                ITopologyBinaryRelation binaryRelation = (ITopologyBinaryRelation)relation;
                binaryRelation.setTargetLayerName(this.availableTargetLayersCombobox.getSelectedLayer().getName());
                binaryRelation.setEntryTargetFilter(this.entryTargetLayerFilterPanel.getFilter());
            }
            relation.setEnabled(this.enabledCheckBox.isSelected());
        }
        return relation;
    }

    public void refresh(String selectedLayerName, LayerManager layerManager) {
        this.availableSourceLayersCombobox.setLayerManager(layerManager);
        this.availableTargetLayersCombobox.setLayerManager(layerManager);
        ArrayList<String> validLayerNames = null;
        if (!StringUtils.isEmpty((String)selectedLayerName)) {
            validLayerNames = new ArrayList<String>();
            validLayerNames.add(selectedLayerName);
        }
        this.availableSourceLayersCombobox.setValidLayerNames(validLayerNames);
        this.rulesCombobox.setRemoveBinaryRelations(this.onlyOneLayerMode);
        this.updateGUI();
    }

    public void setOnlyOneLayerMode(boolean onlyOneLayerMode) {
        this.onlyOneLayerMode = onlyOneLayerMode;
    }
}

