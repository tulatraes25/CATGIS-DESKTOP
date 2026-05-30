/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.utils.relations;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationType;
import org.saig.core.model.relations.TableRelation;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class RelationConfigDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    public static final Logger LOGGER = Logger.getLogger(RelationConfigDialog.class);
    private static final int TYPE_LAYER = 1;
    private static final int TYPE_TABLE = 2;
    private int type = 1;
    private JPanel cardPanel;
    private JComboBox fieldsSourceComboBox;
    private JComboBox fieldsSource2ComboBox;
    private JComboBox layersTargetComboBox;
    private JComboBox fieldsLayersTargetComboBox;
    private JComboBox tableComboBox;
    private JComboBox fieldsTableComboBox;
    private JList fieldsTableList;
    private JList fieldsLayerList;
    private JTextField relationNameTextField;
    private JCheckBox onDemmandCheckBox;
    private LayerManager layerManager;
    private DataManager dataManager;
    private Layer sourceLayer;
    private Table sourceTable;
    private Relation<?> selectedRelation;
    private JRadioButton layerRelationCheckBox;
    private JRadioButton tableRelationCheckBox;
    private JRadioButton joinRelationTypeRadioButton;
    private JRadioButton relateRelationTypeRadioButton;

    public RelationConfigDialog(JFrame parent, boolean modal, LayerManager layerManager, DataManager dataManager, Relation<?> relation, Layer layer) {
        super((Frame)parent, modal);
        this.layerManager = layerManager;
        this.dataManager = dataManager;
        this.selectedRelation = relation;
        this.sourceLayer = layer;
        this.initialize();
        this.refresh();
        this.pack();
    }

    public RelationConfigDialog(JFrame parent, boolean modal, LayerManager layerManager, DataManager dataManager, Relation<?> relation, Table table) {
        super((Frame)parent, modal);
        this.layerManager = layerManager;
        this.dataManager = dataManager;
        this.selectedRelation = relation;
        this.sourceTable = table;
        this.initialize();
        this.refresh();
        this.pack();
    }

    private void initialize() {
        this.setTitle(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.configure-relations"));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setMinimumSize(new Dimension(600, 500));
        mainPanel.setPreferredSize(new Dimension(600, 500));
        this.setContentPane(mainPanel);
        JPanel selectorPanel = this.createSelectorPanel();
        this.cardPanel = this.createCardLayoutPanel();
        OKCancelPanel okCancelPanel = this.createOKcancelPanel();
        FormUtils.addRowInGBL(mainPanel, 0, 0, selectorPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.cardPanel);
        FormUtils.addRowInGBL(mainPanel, 2, 0, okCancelPanel);
        if (this.selectedRelation != null && this.selectedRelation instanceof TableRelation || this.tableRelationCheckBox.isSelected()) {
            CardLayout cl = (CardLayout)this.cardPanel.getLayout();
            cl.show(this.cardPanel, "TABLE");
            this.type = 2;
        } else {
            CardLayout cl = (CardLayout)this.cardPanel.getLayout();
            cl.show(this.cardPanel, "LAYER");
            this.type = 1;
        }
    }

    private JPanel createSelectorPanel() {
        JPanel selectorPanel = new JPanel(new GridBagLayout());
        selectorPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.relations.RelationConfigDialog.Relation-options")));
        JLabel relationTypeLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-type")) + ": ");
        ButtonGroup agrupacion = new ButtonGroup();
        this.layerRelationCheckBox = new JRadioButton(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.layer-relation"));
        this.layerRelationCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CardLayout cl = (CardLayout)RelationConfigDialog.this.cardPanel.getLayout();
                cl.show(RelationConfigDialog.this.cardPanel, "LAYER");
                RelationConfigDialog.this.type = 1;
            }
        });
        this.tableRelationCheckBox = new JRadioButton(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.table-relation"));
        this.tableRelationCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CardLayout cl = (CardLayout)RelationConfigDialog.this.cardPanel.getLayout();
                cl.show(RelationConfigDialog.this.cardPanel, "TABLE");
                RelationConfigDialog.this.type = 2;
            }
        });
        int numTables = this.dataManager.getTables().size();
        int numLayers = this.layerManager.getLayers().size();
        if (this.sourceLayer != null) {
            this.layerRelationCheckBox.setEnabled(numLayers > 1);
            this.tableRelationCheckBox.setEnabled(numTables > 0);
        } else if (this.sourceTable != null) {
            this.layerRelationCheckBox.setEnabled(numLayers > 0);
            this.tableRelationCheckBox.setEnabled(numTables > 1);
        }
        agrupacion.add(this.layerRelationCheckBox);
        agrupacion.add(this.tableRelationCheckBox);
        if (this.layerRelationCheckBox.isEnabled()) {
            this.layerRelationCheckBox.setSelected(true);
        } else if (this.tableRelationCheckBox.isEnabled()) {
            this.tableRelationCheckBox.setSelected(true);
        }
        JLabel relationNameLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-name")) + ": ");
        this.relationNameTextField = new JTextField();
        if (this.selectedRelation != null) {
            this.relationNameTextField.setText(this.selectedRelation.getRelationName());
            if (this.selectedRelation instanceof LayerRelation) {
                this.layerRelationCheckBox.setSelected(true);
            } else {
                this.tableRelationCheckBox.setSelected(true);
            }
        }
        JLabel relationCardinalityLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.utils.relations.RelationConfigDialog.Cardinality")) + ": ");
        ButtonGroup relationTypeGroup = new ButtonGroup();
        this.joinRelationTypeRadioButton = new JRadioButton("Join (" + I18N.getString("org.saig.jump.widgets.utils.relations.RelationConfigDialog.one-to-one-relation") + ")");
        this.joinRelationTypeRadioButton.addActionListener(this);
        this.relateRelationTypeRadioButton = new JRadioButton("Relate (" + I18N.getString("org.saig.jump.widgets.utils.relations.RelationConfigDialog.one-to-many-relation") + ")");
        this.relateRelationTypeRadioButton.addActionListener(this);
        relationTypeGroup.add(this.joinRelationTypeRadioButton);
        relationTypeGroup.add(this.relateRelationTypeRadioButton);
        this.onDemmandCheckBox = new JCheckBox(I18N.getString("org.saig.core.model.relations.widgets.RelationConfigDialog.On-demmand-loading"));
        this.onDemmandCheckBox.setSelected(false);
        FormUtils.addRowInGBL((JComponent)selectorPanel, 0, 0, relationNameLabel, (JComponent)this.relationNameTextField, true);
        FormUtils.addRowInGBL(selectorPanel, 1, 0, relationTypeLabel);
        FormUtils.addRowInGBL(selectorPanel, 1, 30, this.layerRelationCheckBox);
        FormUtils.addFiller(selectorPanel, 2, 0);
        FormUtils.addRowInGBL(selectorPanel, 2, 30, this.tableRelationCheckBox);
        FormUtils.addRowInGBL(selectorPanel, 3, 0, relationCardinalityLabel);
        FormUtils.addRowInGBL(selectorPanel, 3, 30, this.joinRelationTypeRadioButton);
        FormUtils.addFiller(selectorPanel, 5, 0);
        FormUtils.addRowInGBL(selectorPanel, 5, 30, this.relateRelationTypeRadioButton);
        FormUtils.addRowInGBL(selectorPanel, 6, 0, this.onDemmandCheckBox);
        return selectorPanel;
    }

    private JPanel createCardLayoutPanel() {
        this.cardPanel = new JPanel();
        this.cardPanel.setLayout(new CardLayout());
        this.cardPanel.add((Component)this.createSourcePanel(), "LAYER");
        this.cardPanel.add((Component)this.createTablePanel(), "TABLE");
        return this.cardPanel;
    }

    private JPanel createSourcePanel() {
        JPanel layerPanel = new JPanel(new GridBagLayout());
        layerPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.relations.RelationConfigDialog.Relation-fields-configuration")));
        FeatureSchema schema = null;
        schema = this.sourceLayer != null ? this.sourceLayer.getFeatureSchema() : this.sourceTable.getSchema();
        Vector<Attribute> fieldsSource = new Vector<Attribute>();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute attr = schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate) || !attr.getType().equals(AttributeType.GEOMETRY)) {
                fieldsSource.add(attr);
            }
            ++i;
        }
        Collections.sort(fieldsSource);
        this.fieldsSourceComboBox = new JComboBox(fieldsSource);
        this.fieldsLayersTargetComboBox = new JComboBox();
        this.fieldsLayersTargetComboBox.addActionListener(this);
        List<Layer> layers = this.layerManager.getLayers();
        if (this.sourceLayer != null) {
            layers.remove(this.sourceLayer);
        }
        this.fieldsLayerList = new JList();
        Vector<Layer> layersTarget = new Vector<Layer>();
        Iterator<Layer> iter = layers.iterator();
        while (iter.hasNext()) {
            layersTarget.add(iter.next());
        }
        Collections.sort(layersTarget);
        this.layersTargetComboBox = new JComboBox(layersTarget);
        if (this.layersTargetComboBox.getSelectedItem() != null && !((Layer)this.layersTargetComboBox.getSelectedItem()).isDataBaseDataSource()) {
            this.onDemmandCheckBox.setEnabled(false);
            this.onDemmandCheckBox.setSelected(false);
        } else {
            this.onDemmandCheckBox.setEnabled(true);
        }
        if (layers.size() > 0) {
            FeatureSchema schemaTargetLayer = layers.get(0).getFeatureSchema();
            Vector<Attribute> fields = new Vector<Attribute>();
            int i2 = 0;
            while (i2 < schemaTargetLayer.getAttributeCount()) {
                AttributeType tipo;
                Attribute attr = schemaTargetLayer.getAttribute(i2);
                if (!(attr instanceof AttributeCalculate) && !(tipo = attr.getType()).equals(AttributeType.GEOMETRY)) {
                    fields.add(attr);
                }
                ++i2;
            }
            Collections.sort(fields);
            for (Attribute element : fields) {
                this.fieldsLayersTargetComboBox.addItem(element);
            }
            fields.remove(this.fieldsLayersTargetComboBox.getSelectedItem());
            this.fieldsLayerList.setListData(fields);
        } else {
            this.layersTargetComboBox.setEnabled(false);
            this.fieldsLayersTargetComboBox.setEnabled(false);
            this.fieldsLayerList.setEnabled(false);
        }
        this.layersTargetComboBox.addActionListener(this);
        JLabel fieldSourceLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-source-field")) + ": ");
        FormUtils.addRowInGBL((JComponent)layerPanel, 0, 0, fieldSourceLabel, (JComponent)this.fieldsSourceComboBox);
        JLabel targetLayerLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-layer")) + ": ");
        FormUtils.addRowInGBL((JComponent)layerPanel, 1, 0, targetLayerLabel, (JComponent)this.layersTargetComboBox);
        JLabel fieldTargetLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-target-field")) + ": ");
        FormUtils.addRowInGBL((JComponent)layerPanel, 2, 0, fieldTargetLabel, (JComponent)this.fieldsLayersTargetComboBox);
        JLabel fieldsLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.fields-to-include-in-the-relation")) + ": ");
        JScrollPane pane = new JScrollPane(this.fieldsLayerList);
        pane.setMinimumSize(new Dimension(300, 300));
        FormUtils.addRowInGBL((JComponent)layerPanel, 3, 0, fieldsLabel, (JComponent)pane);
        if (this.selectedRelation != null && this.selectedRelation instanceof LayerRelation) {
            this.onDemmandCheckBox.setSelected(this.selectedRelation.isOnDemmand());
            LayerRelation layerRel = (LayerRelation)this.selectedRelation;
            if (!layerRel.getTargetLayer().isDataBaseDataSource()) {
                this.onDemmandCheckBox.setEnabled(false);
            } else {
                this.onDemmandCheckBox.setEnabled(true);
            }
            Attribute selectedComponent_ = null;
            int i3 = 0;
            while (i3 < this.fieldsSourceComboBox.getItemCount() && selectedComponent_ == null) {
                Attribute attr = (Attribute)this.fieldsSourceComboBox.getItemAt(i3);
                if (attr.getPublicName().equals(layerRel.getSourceAttribute())) {
                    selectedComponent_ = attr;
                }
                ++i3;
            }
            this.fieldsSourceComboBox.setSelectedItem(selectedComponent_);
            this.layersTargetComboBox.setSelectedItem(layerRel.getTargetLayer());
            Attribute selectedComponent = null;
            int i4 = 0;
            while (i4 < this.fieldsLayersTargetComboBox.getItemCount() && selectedComponent == null) {
                Attribute attr = (Attribute)this.fieldsLayersTargetComboBox.getItemAt(i4);
                if (attr.getPublicName().equals(layerRel.getAttributeTarget())) {
                    selectedComponent = attr;
                }
                ++i4;
            }
            this.fieldsLayersTargetComboBox.setSelectedItem(selectedComponent);
            List<String> fields = layerRel.getRelationFields();
            if (fields != null && fields.size() > 0) {
                int[] indexes = new int[fields.size()];
                int i5 = 0;
                while (i5 < fields.size()) {
                    int j = 0;
                    while (j < this.fieldsLayerList.getModel().getSize()) {
                        if (((Attribute)this.fieldsLayerList.getModel().getElementAt(j)).getName().equals(fields.get(i5))) {
                            indexes[i5] = j;
                        }
                        ++j;
                    }
                    ++i5;
                }
                this.fieldsLayerList.setSelectedIndices(indexes);
            }
        }
        return layerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.utils.relations.RelationConfigDialog.Relation-fields-configuration")));
        FeatureSchema schema = null;
        schema = this.sourceLayer != null ? this.sourceLayer.getFeatureSchema() : this.sourceTable.getSchema();
        Vector<Attribute> fieldsSource = new Vector<Attribute>();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute attr = schema.getAttribute(i);
            if (!(attr instanceof AttributeCalculate) || !attr.getType().equals(AttributeType.GEOMETRY)) {
                fieldsSource.add(attr);
            }
            ++i;
        }
        Collections.sort(fieldsSource);
        this.fieldsSource2ComboBox = new JComboBox(fieldsSource);
        this.fieldsTableComboBox = new JComboBox();
        this.fieldsTableComboBox.addActionListener(this);
        this.fieldsTableList = new JList();
        this.fieldsTableList.setAutoscrolls(true);
        List<Table> tables = this.dataManager.getRealTables();
        this.tableComboBox = new JComboBox();
        Iterator<Table> iter = tables.iterator();
        while (iter.hasNext()) {
            this.tableComboBox.addItem(iter.next());
        }
        if (tables.size() > 0) {
            Table selectedTable = (Table)this.tableComboBox.getSelectedItem();
            if (selectedTable.getDataSource() instanceof TableDBRecordDataSource) {
                this.onDemmandCheckBox.setEnabled(true);
            } else {
                this.onDemmandCheckBox.setSelected(false);
                this.onDemmandCheckBox.setEnabled(false);
            }
            Vector<Attribute> fields = new Vector<Attribute>();
            FeatureSchema tableSchema = tables.get(0).getSchema();
            int i2 = 0;
            while (i2 < tableSchema.getAttributeCount()) {
                Attribute attr = tableSchema.getAttribute(i2);
                if (!(attr instanceof AttributeCalculate)) {
                    fields.add(attr);
                }
                ++i2;
            }
            Collections.sort(fields);
            Iterator iter2 = fields.iterator();
            while (iter2.hasNext()) {
                this.fieldsTableComboBox.addItem(iter2.next());
            }
            fields.remove(this.fieldsTableComboBox.getSelectedItem());
            this.fieldsTableList.setListData(fields);
        } else {
            this.tableComboBox.setEnabled(false);
            this.fieldsTableComboBox.setEnabled(false);
        }
        this.tableComboBox.addActionListener(this);
        JLabel fieldSourceLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-source-field")) + ": ");
        FormUtils.addRowInGBL((JComponent)tablePanel, 0, 0, fieldSourceLabel, (JComponent)this.fieldsSource2ComboBox);
        JLabel targetLayerLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-table")) + ": ");
        FormUtils.addRowInGBL((JComponent)tablePanel, 1, 0, targetLayerLabel, (JComponent)this.tableComboBox);
        JLabel fieldTargetLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-target-field")) + ": ");
        FormUtils.addRowInGBL((JComponent)tablePanel, 2, 0, fieldTargetLabel, (JComponent)this.fieldsTableComboBox);
        JScrollPane pane = new JScrollPane(this.fieldsTableList);
        pane.setSize(new Dimension(20, 20));
        JLabel fieldsLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.fields-to-include-in-the-relation")) + ": ");
        FormUtils.addRowInGBL((JComponent)tablePanel, 3, 0, fieldsLabel, (JComponent)pane);
        if (this.selectedRelation != null && this.selectedRelation instanceof TableRelation) {
            this.onDemmandCheckBox.setSelected(this.selectedRelation.isOnDemmand());
            TableRelation layerRel = (TableRelation)this.selectedRelation;
            if (layerRel.getTable().getDataSource() instanceof TableDBRecordDataSource) {
                this.onDemmandCheckBox.setEnabled(true);
            } else {
                this.onDemmandCheckBox.setEnabled(false);
            }
            Attribute selectedComponent_ = null;
            int i3 = 0;
            while (i3 < this.fieldsSource2ComboBox.getItemCount() && selectedComponent_ == null) {
                Attribute attr = (Attribute)this.fieldsSource2ComboBox.getItemAt(i3);
                if (attr.getPublicName().equals(layerRel.getSourceAttribute())) {
                    selectedComponent_ = attr;
                }
                ++i3;
            }
            this.fieldsSource2ComboBox.setSelectedItem(selectedComponent_);
            this.tableComboBox.setSelectedItem(layerRel.getTable());
            Attribute selectedComponent = null;
            int i4 = 0;
            while (i4 < this.fieldsTableComboBox.getItemCount() && selectedComponent == null) {
                Attribute attr = (Attribute)this.fieldsTableComboBox.getItemAt(i4);
                if (attr.getPublicName().equals(layerRel.getAttributeTarget())) {
                    selectedComponent = attr;
                }
                ++i4;
            }
            this.fieldsTableComboBox.setSelectedItem(selectedComponent);
            List<String> fields = layerRel.getRelationFields();
            if (fields != null && fields.size() > 0) {
                int[] indexes = new int[fields.size()];
                int i5 = 0;
                while (i5 < fields.size()) {
                    int j = 0;
                    while (j < this.fieldsTableList.getModel().getSize()) {
                        if (((Attribute)this.fieldsTableList.getModel().getElementAt(j)).getPublicName().equals(fields.get(i5))) {
                            indexes[i5] = j;
                        }
                        ++j;
                    }
                    ++i5;
                }
                this.fieldsTableList.setSelectedIndices(indexes);
            }
        }
        return tablePanel;
    }

    private OKCancelPanel createOKcancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (okCancelPanel.wasOKPressed()) {
                    boolean onDemmand = RelationConfigDialog.this.onDemmandCheckBox.isSelected();
                    RelationType relationType = RelationConfigDialog.this.getSelectedRelationType();
                    String relationName = RelationConfigDialog.this.relationNameTextField.getText().trim();
                    if (relationName.equals("")) {
                        DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-relation-must-have-a-name"), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.error"));
                        return;
                    }
                    if (RelationConfigDialog.this.sourceLayer != null) {
                        if (RelationConfigDialog.this.selectedRelation == null && RelationConfigDialog.this.sourceLayer.hasRelation(relationName) || RelationConfigDialog.this.selectedRelation != null && !relationName.equals(RelationConfigDialog.this.selectedRelation.getRelationName())) {
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-name-{0}-is-in-use-by-another-relation")) + " .\n" + I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.please-select-a-new-name"), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.error"));
                            return;
                        }
                    } else if (RelationConfigDialog.this.selectedRelation == null && RelationConfigDialog.this.sourceTable.hasRelation(relationName) || RelationConfigDialog.this.selectedRelation != null && !relationName.equals(RelationConfigDialog.this.selectedRelation.getRelationName()) && RelationConfigDialog.this.sourceTable.hasRelation(relationName)) {
                        DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-name-{0}-is-in-use-by-another-relation")) + " .\n" + I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.please-select-a-new-name"), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.error"));
                        return;
                    }
                    if (RelationConfigDialog.this.type == 1) {
                        Attribute fieldSource = (Attribute)RelationConfigDialog.this.fieldsSourceComboBox.getSelectedItem();
                        if (!RelationConfigDialog.this.layersTargetComboBox.isEnabled()) {
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-relation-can-not-be-created"), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.error"));
                            return;
                        }
                        Layer targetLayer = (Layer)RelationConfigDialog.this.layersTargetComboBox.getSelectedItem();
                        Attribute fieldTarget = (Attribute)RelationConfigDialog.this.fieldsLayersTargetComboBox.getSelectedItem();
                        if (!AttributeType.areCompatibleTypes(fieldSource.getType(), fieldTarget.getType())) {
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-relation-can-not-be-created")) + ". " + I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-linked-fields-have-different-types") + " (" + fieldSource.getType().getName() + " <-> " + fieldTarget.getType().getName() + ")", I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.error"));
                            return;
                        }
                        Object[] values = RelationConfigDialog.this.fieldsLayerList.getSelectedValues();
                        ArrayList<String> fields = null;
                        if (ArrayUtils.isEmpty((Object[])values) && RelationConfigDialog.this.joinRelationTypeRadioButton.isSelected()) {
                            int resultado = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.you-have-not-selected-any-fields-to-include-in-the-relation-do-you-want-to-create-the-relation-anyway"), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-configuration"));
                            if (resultado == 2 || resultado == 1) {
                                return;
                            }
                        } else if (!ArrayUtils.isEmpty((Object[])values)) {
                            fields = new ArrayList<String>();
                            int i = 0;
                            while (i < values.length) {
                                Attribute attr = (Attribute)values[i];
                                if (attr instanceof AttributeCalculate) {
                                    fields.add(((AttributeCalculate)attr).getRelationFieldName());
                                } else {
                                    fields.add(attr.getName());
                                }
                                ++i;
                            }
                        }
                        LayerRelation relation = new LayerRelation(fieldSource.getName(), fieldTarget.getName(), relationName);
                        relation.setOnDemmand(onDemmand);
                        relation.setRelationType(relationType);
                        relation.setRelationFields(fields);
                        relation.setTargetLayer(targetLayer);
                        if (RelationConfigDialog.this.selectedRelation != null) {
                            if (RelationConfigDialog.this.sourceLayer != null) {
                                RelationConfigDialog.this.sourceLayer.removeRelation(RelationConfigDialog.this.selectedRelation);
                            } else {
                                RelationConfigDialog.this.sourceTable.removeRelation(RelationConfigDialog.this.selectedRelation);
                            }
                        }
                        if (RelationConfigDialog.this.sourceLayer != null) {
                            RelationConfigDialog.this.sourceLayer.addRelation(relation);
                        } else {
                            RelationConfigDialog.this.sourceTable.addRelation(relation);
                        }
                    } else {
                        Attribute fieldSource = (Attribute)RelationConfigDialog.this.fieldsSource2ComboBox.getSelectedItem();
                        Table table = (Table)RelationConfigDialog.this.tableComboBox.getSelectedItem();
                        if (!RelationConfigDialog.this.tableComboBox.isEnabled()) {
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-relation-can-not-be-created"), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.error"));
                            return;
                        }
                        Attribute fieldTable = (Attribute)RelationConfigDialog.this.fieldsTableComboBox.getSelectedItem();
                        if (!AttributeType.areCompatibleTypes(fieldSource.getType(), fieldTable.getType())) {
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-relation-can-not-be-created")) + ". " + I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.the-linked-fields-have-different-types") + " (" + fieldSource.getType().getName() + " <-> " + fieldTable.getType().getName() + ")", I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.error"));
                            return;
                        }
                        Object[] values = RelationConfigDialog.this.fieldsTableList.getSelectedValues();
                        ArrayList<String> fields = null;
                        if (ArrayUtils.isEmpty((Object[])values) && RelationConfigDialog.this.joinRelationTypeRadioButton.isSelected()) {
                            int resultado = DialogFactory.showYesNoCancelWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.you-have-not-selected-any-fields-to-include-in-the-relation-do-you-want-to-create-the-relation-anyway"), I18N.getString("org.saig.core.model.relations.widgets.LayerRelationsConfigDialog.relation-configuration"));
                            if (resultado == 2 || resultado == 1) {
                                return;
                            }
                        } else if (!ArrayUtils.isEmpty((Object[])values)) {
                            fields = new ArrayList<String>();
                            int i = 0;
                            while (i < values.length) {
                                Attribute attr = (Attribute)values[i];
                                if (attr instanceof AttributeCalculate) {
                                    fields.add(((AttributeCalculate)attr).getRelationFieldName());
                                } else {
                                    fields.add(attr.getName());
                                }
                                ++i;
                            }
                        }
                        if (RelationConfigDialog.this.selectedRelation != null) {
                            if (RelationConfigDialog.this.sourceLayer != null) {
                                RelationConfigDialog.this.sourceLayer.removeRelation(RelationConfigDialog.this.selectedRelation);
                            } else {
                                RelationConfigDialog.this.sourceTable.removeRelation(RelationConfigDialog.this.selectedRelation);
                            }
                        }
                        TableRelation relation = null;
                        relation = new TableRelation(fieldSource.getName(), fieldTable.getName(), relationName);
                        relation.setOnDemmand(onDemmand);
                        relation.setRelationType(relationType);
                        relation.setRelationFields(fields);
                        relation.setTable(table);
                        if (RelationConfigDialog.this.sourceLayer != null) {
                            RelationConfigDialog.this.sourceLayer.addRelation(relation);
                        } else {
                            RelationConfigDialog.this.sourceTable.addRelation(relation);
                            RelationConfigDialog.this.sourceTable.fireTableChanged();
                        }
                    }
                }
                RelationConfigDialog.this.setVisible(false);
            }
        });
        return okCancelPanel;
    }

    protected RelationType getSelectedRelationType() {
        if (this.joinRelationTypeRadioButton.isSelected()) {
            return RelationType.JOIN;
        }
        return RelationType.RELATE;
    }

    private void refresh() {
        if (this.selectedRelation == null || this.selectedRelation.getRelationType().equals((Object)RelationType.JOIN)) {
            this.joinRelationTypeRadioButton.setSelected(true);
            this.fieldsLayerList.setEnabled(true);
            this.fieldsTableList.setEnabled(true);
        } else {
            this.relateRelationTypeRadioButton.setSelected(true);
            this.fieldsLayerList.setEnabled(false);
            this.fieldsTableList.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.joinRelationTypeRadioButton)) {
            this.fieldsLayerList.setEnabled(true);
            this.fieldsTableList.setEnabled(true);
        } else if (e.getSource().equals(this.relateRelationTypeRadioButton)) {
            this.fieldsLayerList.setEnabled(false);
            this.fieldsTableList.setEnabled(false);
        } else if (e.getSource().equals(this.tableComboBox)) {
            Table table = (Table)this.tableComboBox.getSelectedItem();
            if (table.getDataSource() instanceof TableDBRecordDataSource) {
                this.onDemmandCheckBox.setEnabled(true);
            } else {
                this.onDemmandCheckBox.setSelected(false);
                this.onDemmandCheckBox.setEnabled(false);
            }
            this.fieldsTableComboBox.removeAllItems();
            this.fieldsTableList.removeAll();
            Vector<Attribute> fields = new Vector<Attribute>();
            FeatureSchema tableSchema = table.getSchema();
            int i = 0;
            while (i < tableSchema.getAttributeCount()) {
                Attribute attr = tableSchema.getAttribute(i);
                if (!(attr instanceof AttributeCalculate)) {
                    fields.add(attr);
                }
                ++i;
            }
            Collections.sort(fields);
            Iterator iter = fields.iterator();
            while (iter.hasNext()) {
                this.fieldsTableComboBox.addItem(iter.next());
            }
            fields.remove(this.fieldsTableComboBox.getSelectedItem());
            this.fieldsTableList.setListData(fields);
        } else if (e.getSource().equals(this.fieldsTableComboBox)) {
            Attribute fieldTarget = (Attribute)this.fieldsTableComboBox.getSelectedItem();
            Vector<Attribute> data = new Vector<Attribute>();
            int i = 0;
            while (i < this.fieldsTableComboBox.getItemCount()) {
                data.add((Attribute)this.fieldsTableComboBox.getItemAt(i));
                ++i;
            }
            data.remove(fieldTarget);
            this.fieldsTableList.removeAll();
            Collections.sort(data);
            this.fieldsTableList.setListData(data);
        } else if (e.getSource().equals(this.layersTargetComboBox)) {
            Layer layer = (Layer)this.layersTargetComboBox.getSelectedItem();
            if (!layer.isDataBaseDataSource()) {
                this.onDemmandCheckBox.setEnabled(false);
                this.onDemmandCheckBox.setSelected(false);
            } else {
                this.onDemmandCheckBox.setEnabled(true);
            }
            this.fieldsLayersTargetComboBox.removeAllItems();
            this.fieldsLayerList.removeAll();
            FeatureSchema schemaTargetLayer = layer.getFeatureSchema();
            Vector<Attribute> fields = new Vector<Attribute>();
            int i = 0;
            while (i < schemaTargetLayer.getAttributeCount()) {
                AttributeType tipo;
                Attribute attr = schemaTargetLayer.getAttribute(i);
                if (!(attr instanceof AttributeCalculate) && !(tipo = attr.getType()).equals(AttributeType.GEOMETRY)) {
                    fields.add(attr);
                }
                ++i;
            }
            Collections.sort(fields);
            for (Attribute element : fields) {
                this.fieldsLayersTargetComboBox.addItem(element);
            }
            fields.remove(this.fieldsLayersTargetComboBox.getSelectedItem());
            this.fieldsLayerList.setListData(fields);
        } else if (e.getSource().equals(this.fieldsLayersTargetComboBox)) {
            Object fieldTarget = this.fieldsLayersTargetComboBox.getSelectedItem();
            Vector<Attribute> data = new Vector<Attribute>();
            int i = 0;
            while (i < this.fieldsLayersTargetComboBox.getItemCount()) {
                data.add((Attribute)this.fieldsLayersTargetComboBox.getItemAt(i));
                ++i;
            }
            data.remove(fieldTarget);
            this.fieldsLayerList.removeAll();
            Collections.sort(data);
            this.fieldsLayerList.setListData(data);
        }
    }
}

