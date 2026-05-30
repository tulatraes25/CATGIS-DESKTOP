/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.filter;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderAddValueToListDialog;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderComponentsFactory;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderConstants;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderDialog;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderRow;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.query.RecordFieldValuesDialog;

public class SQLFilterBuilderConditionEditionDialog
extends JDialog {
    private SQLFilterBuilderDialog parent;
    private OKCancelPanel okCancelPanel;
    private SQLFilterBuilderRow conditionOrig;
    private JComboBox cbbLogicConnector;
    private JComboBox cbbAttribute;
    private JComboBox cbbOperation;
    private JLabel labelValues;
    private JList listValues;
    private JScrollPane listValuesScrollPane;
    private JButton bAddValue;
    private JButton allRegistersButton;
    private JButton bRemoveValue;
    private boolean isFirstCondition;
    private TableRecordDataSource ds;

    public SQLFilterBuilderConditionEditionDialog(SQLFilterBuilderDialog parent, boolean modal, boolean isFirstCondition, SQLFilterBuilderRow condition, boolean initParenthesis, TableRecordDataSource ds) {
        super((Dialog)parent, modal);
        this.ds = ds;
        this.setTitle(I18N.getString(this.getClass(), "edit-condition"));
        this.setResizable(false);
        this.parent = parent;
        this.conditionOrig = condition;
        this.isFirstCondition = isFirstCondition;
        this.labelValues = new JLabel(I18N.getString(this.getClass(), "values-list"));
        JPanel mainPanel = new JPanel(new GridBagLayout());
        if (!initParenthesis && !isFirstCondition) {
            this.initCbbLogicConnector();
            FormUtils.addRowInGBL((JComponent)mainPanel, 0, 0, I18N.getString(this.getClass(), "logic-operation"), (JComponent)this.cbbLogicConnector);
        }
        this.initButtonsAddRemoveValue();
        this.initListValues();
        this.initCbbAttribute();
        this.initCbbOperation();
        this.initOkCancelPanel();
        if (this.conditionOrig != null) {
            if (this.cbbLogicConnector != null) {
                this.cbbLogicConnector.setSelectedItem(this.conditionOrig.getConnectOperation());
            }
            this.cbbAttribute.setSelectedItem(this.conditionOrig.getAttribute());
            this.cbbOperation.setSelectedItem(this.conditionOrig.getComparationOperation());
            DefaultListModel listModel = (DefaultListModel)this.listValues.getModel();
            Iterator<Object> it = this.conditionOrig.getValues().iterator();
            while (it.hasNext()) {
                listModel.addElement(it.next());
            }
        }
        this.modifyValuesListComponents();
        JPanel buttonsValuesPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(buttonsValuesPanel, 0, 0, this.bAddValue);
        FormUtils.addRowInGBL(buttonsValuesPanel, 1, 0, this.bRemoveValue);
        FormUtils.addRowInGBL(buttonsValuesPanel, 2, 0, this.allRegistersButton);
        FormUtils.addFiller(buttonsValuesPanel, 3, 0);
        JPanel valuesPanel = new JPanel();
        valuesPanel.setLayout(new BoxLayout(valuesPanel, 0));
        valuesPanel.add(this.listValuesScrollPane);
        valuesPanel.add(buttonsValuesPanel);
        FormUtils.addFiller(mainPanel, 1, 0);
        FormUtils.addRowInGBL((JComponent)mainPanel, 2, 0, I18N.getString(this.getClass(), "attribute"), (JComponent)this.cbbAttribute);
        FormUtils.addRowInGBL((JComponent)mainPanel, 3, 0, I18N.getString(this.getClass(), "operation"), (JComponent)this.cbbOperation);
        FormUtils.addRowInGBL((JComponent)mainPanel, 4, 0, (JComponent)this.labelValues, true, true, true);
        FormUtils.addRowInGBL((JComponent)mainPanel, 5, 0, (JComponent)valuesPanel, true, true, true);
        FormUtils.addRowInGBL(mainPanel, 6, 0, this.okCancelPanel);
        this.setContentPane(mainPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    private void initButtonsAddRemoveValue() {
        this.bAddValue = new JButton(I18N.getString(this.getClass(), "add"));
        this.bAddValue.setToolTipText(I18N.getString(this.getClass(), "add-value-to-list"));
        this.bRemoveValue = new JButton(I18N.getString(this.getClass(), "remove"));
        this.bRemoveValue.setToolTipText(I18N.getString(this.getClass(), "remove-selected-value-from-list"));
        this.bAddValue.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ap) {
                Object attribute = SQLFilterBuilderConditionEditionDialog.this.cbbAttribute.getSelectedItem();
                if (attribute != null && attribute instanceof Attribute) {
                    AttributeType type = ((Attribute)attribute).getType();
                    SQLFilterBuilderAddValueToListDialog dialog = new SQLFilterBuilderAddValueToListDialog(SQLFilterBuilderConditionEditionDialog.this, type);
                    dialog.setVisible(true);
                    if (dialog.wasOKPressed()) {
                        ((DefaultListModel)SQLFilterBuilderConditionEditionDialog.this.listValues.getModel()).addElement(dialog.getValue());
                    }
                    SQLFilterBuilderConditionEditionDialog.this.modifyValuesListComponents();
                } else {
                    SQLFilterBuilderConditionEditionDialog.this.bAddValue.setEnabled(false);
                }
            }
        });
        this.bRemoveValue.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ap) {
                int selIndex = SQLFilterBuilderConditionEditionDialog.this.listValues.getSelectedIndex();
                if (selIndex != -1) {
                    ((DefaultListModel)SQLFilterBuilderConditionEditionDialog.this.listValues.getModel()).remove(selIndex);
                    SQLFilterBuilderConditionEditionDialog.this.modifyValuesListComponents();
                }
            }
        });
        this.allRegistersButton = new JButton("...");
        this.allRegistersButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                RecordFieldValuesDialog fieldValuesDialog = new RecordFieldValuesDialog((Frame)JUMPWorkbench.getFrameInstance(), true);
                Object attribute = SQLFilterBuilderConditionEditionDialog.this.cbbAttribute.getSelectedItem();
                if (attribute != null && attribute instanceof Attribute) {
                    Attribute selectedAttr = (Attribute)attribute;
                    String attributeName = selectedAttr.getName();
                    fieldValuesDialog.setValues(SQLFilterBuilderConditionEditionDialog.this.ds, attributeName);
                    GUIUtil.centreOnScreen(fieldValuesDialog);
                    fieldValuesDialog.setVisible(true);
                    Object searchLiteral = fieldValuesDialog.getSelection();
                    if (searchLiteral != null) {
                        ((DefaultListModel)SQLFilterBuilderConditionEditionDialog.this.listValues.getModel()).addElement(searchLiteral);
                    }
                    SQLFilterBuilderConditionEditionDialog.this.modifyValuesListComponents();
                } else {
                    SQLFilterBuilderConditionEditionDialog.this.bAddValue.setEnabled(false);
                }
            }
        });
    }

    private void initOkCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ap) {
                SQLFilterBuilderConditionEditionDialog.this.setVisible(false);
            }
        });
    }

    private void initCbbLogicConnector() {
        this.cbbLogicConnector = SQLFilterBuilderComponentsFactory.getLogicOperatorsCombo();
        this.cbbLogicConnector.setToolTipText(I18N.getString(this.getClass(), "logic-operation-connecting-previous-condition-with-this"));
    }

    private void initCbbOperation() {
        Object attribute = this.cbbAttribute.getSelectedItem();
        this.cbbOperation = attribute == null || !(attribute instanceof Attribute) ? new JComboBox() : SQLFilterBuilderComponentsFactory.getComparationOperatorsCombo(((Attribute)attribute).getType());
        this.cbbOperation.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ((DefaultListModel)SQLFilterBuilderConditionEditionDialog.this.listValues.getModel()).clear();
                SQLFilterBuilderConditionEditionDialog.this.modifyValuesListComponents();
            }
        });
        this.cbbOperation.setToolTipText(I18N.getString(this.getClass(), "selection-of-filtering-operation"));
    }

    private void initCbbAttribute() {
        this.cbbAttribute = SQLFilterBuilderComponentsFactory.getAttributesCombo(this.parent.getSchema());
        this.cbbAttribute.setToolTipText(I18N.getString(this.getClass(), "selection-of-attribute-used-to-filter"));
        this.cbbAttribute.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Object selectedValue = SQLFilterBuilderConditionEditionDialog.this.cbbAttribute.getSelectedItem();
                if (selectedValue != null && selectedValue instanceof Attribute) {
                    ComboBoxModel model = SQLFilterBuilderComponentsFactory.getComparationOperatorsComboModel(((Attribute)selectedValue).getType());
                    SQLFilterBuilderConditionEditionDialog.this.cbbOperation.setModel(model);
                    ((DefaultListModel)SQLFilterBuilderConditionEditionDialog.this.listValues.getModel()).clear();
                    SQLFilterBuilderConditionEditionDialog.this.modifyValuesListComponents();
                }
            }
        });
    }

    private void initListValues() {
        this.listValues = new JList(new DefaultListModel());
        this.listValues.setToolTipText(I18N.getString(this.getClass(), "list-of-values-used-in-filtering-operation"));
        this.listValues.setCellRenderer(new ValuesListRenderer());
        Dimension listDim = new Dimension(140, 140);
        this.listValues.setMinimumSize(listDim);
        this.listValues.setPreferredSize(listDim);
        this.listValues.setSelectionMode(0);
        this.listValuesScrollPane = new JScrollPane(this.listValues);
        Dimension scrollDim = new Dimension(150, 150);
        this.listValuesScrollPane.setMinimumSize(scrollDim);
        this.listValuesScrollPane.setPreferredSize(scrollDim);
    }

    private void modifyValuesListComponents() {
        Object operation = this.cbbOperation.getSelectedItem();
        int numValues = this.listValues.getModel().getSize();
        if (operation == null) {
            this.labelValues.setText(I18N.getString(this.getClass(), "values-list"));
            this.bAddValue.setEnabled(false);
            this.allRegistersButton.setEnabled(false);
            this.bRemoveValue.setEnabled(false);
            this.okCancelPanel.setOKEnabled(false);
        } else if (SQLFilterBuilderConstants.operationHasNoValues((String)operation)) {
            this.labelValues.setText(I18N.getString(this.getClass(), "it-is-not-necessary-any-value"));
            this.labelValues.setToolTipText(I18N.getString(this.getClass(), "selected-operation-requires-no-more-values"));
            this.bAddValue.setEnabled(false);
            this.allRegistersButton.setEnabled(false);
            this.bRemoveValue.setEnabled(false);
            this.okCancelPanel.setOKEnabled(true);
        } else if (SQLFilterBuilderConstants.operationHasOneValue((String)operation)) {
            this.labelValues.setText(I18N.getString(this.getClass(), "specify-one-single-value"));
            this.labelValues.setToolTipText(I18N.getString(this.getClass(), "selected-operation-requires-one-single-value"));
            if (numValues == 0) {
                this.bAddValue.setEnabled(true);
                this.allRegistersButton.setEnabled(true);
                this.bRemoveValue.setEnabled(false);
                this.okCancelPanel.setOKEnabled(false);
            } else if (numValues == 1) {
                this.bAddValue.setEnabled(false);
                this.allRegistersButton.setEnabled(false);
                this.bRemoveValue.setEnabled(true);
                this.okCancelPanel.setOKEnabled(true);
            } else {
                this.bAddValue.setEnabled(false);
                this.allRegistersButton.setEnabled(false);
                this.bRemoveValue.setEnabled(true);
                this.okCancelPanel.setOKEnabled(false);
            }
        } else if (SQLFilterBuilderConstants.operationHasTwoValues((String)operation)) {
            this.labelValues.setText(I18N.getString(this.getClass(), "specify-two-values"));
            this.labelValues.setToolTipText(I18N.getString(this.getClass(), "selected-operation-requires-two-values"));
            if (numValues == 0) {
                this.bAddValue.setEnabled(true);
                this.allRegistersButton.setEnabled(true);
                this.bRemoveValue.setEnabled(false);
                this.okCancelPanel.setOKEnabled(false);
            } else if (numValues == 1) {
                this.bAddValue.setEnabled(true);
                this.allRegistersButton.setEnabled(true);
                this.bRemoveValue.setEnabled(true);
                this.okCancelPanel.setOKEnabled(false);
            } else if (numValues == 2) {
                this.bAddValue.setEnabled(false);
                this.allRegistersButton.setEnabled(false);
                this.bRemoveValue.setEnabled(true);
                this.okCancelPanel.setOKEnabled(true);
            } else {
                this.bAddValue.setEnabled(false);
                this.allRegistersButton.setEnabled(false);
                this.bRemoveValue.setEnabled(true);
                this.okCancelPanel.setOKEnabled(false);
            }
        } else if (SQLFilterBuilderConstants.operationHasManyValues((String)operation)) {
            this.labelValues.setText(I18N.getString(this.getClass(), "specify-a-list-of-values"));
            this.labelValues.setToolTipText(I18N.getString(this.getClass(), "selected-operation-requires-more-than-one-value"));
            this.bAddValue.setEnabled(true);
            this.allRegistersButton.setEnabled(true);
            if (numValues > 0) {
                this.bRemoveValue.setEnabled(true);
                this.okCancelPanel.setOKEnabled(true);
            } else {
                this.bRemoveValue.setEnabled(false);
                this.okCancelPanel.setOKEnabled(false);
            }
        }
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public SQLFilterBuilderRow getConditionRow() {
        String logicConnector = "";
        if (this.cbbLogicConnector != null) {
            logicConnector = (String)this.cbbLogicConnector.getSelectedItem();
        }
        List<Object> list = Arrays.asList(((DefaultListModel)this.listValues.getModel()).toArray());
        SQLFilterBuilderRow row = new SQLFilterBuilderRow(this.isFirstCondition, logicConnector, (String)this.cbbOperation.getSelectedItem(), (Attribute)this.cbbAttribute.getSelectedItem(), list, false, false, 1);
        return row;
    }

    private class ValuesListRenderer
    extends JLabel
    implements ListCellRenderer {
        private ValuesListRenderer() {
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            this.setText(SQLFilterBuilderConstants.valueFormatter(value));
            this.setOpaque(true);
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            return this;
        }
    }
}

