/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.filter;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelHelpPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderConditionEditionDialog;
import org.saig.core.model.data.widgets.tables.management.filter.SQLFilterBuilderRow;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class SQLFilterBuilderDialog
extends JDialog
implements ListSelectionListener,
ActionListener {
    public static final Logger LOGGER = Logger.getLogger(SQLFilterBuilderDialog.class);
    private FeatureSchema schema;
    private JButton buttonAdd;
    private JButton buttonEdit;
    private JButton buttonRemove;
    private JButton buttonAddParenthesis;
    private JButton buttonClear;
    private OKCancelHelpPanel okCancelPanel;
    private JList list;
    private int indent = 0;
    private TableRecordDataSource ds;

    public SQLFilterBuilderDialog(FeatureSchema fs, TableRecordDataSource ds) {
        super((Frame)JUMPWorkbench.getFrameInstance(), true);
        this.ds = ds;
        this.setTitle(I18N.getString(this.getClass(), "filter-constructor"));
        this.setResizable(false);
        this.schema = fs;
        Dimension dim = new Dimension(300, 300);
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.initList();
        this.initButtons();
        this.initOKCancelPanel();
        this.list.setMinimumSize(dim);
        this.list.setPreferredSize(dim);
        Dimension dim2 = new Dimension(320, 320);
        JScrollPane scrollPaneList = new JScrollPane(this.list);
        scrollPaneList.setMinimumSize(dim2);
        scrollPaneList.setPreferredSize(dim2);
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(this.buttonAdd);
        toolBar.add(this.buttonEdit);
        toolBar.add(this.buttonRemove);
        toolBar.add(this.buttonClear);
        mainPanel.add((Component)toolBar, "North");
        mainPanel.add((Component)scrollPaneList, "Center");
        mainPanel.add((Component)this.okCancelPanel, "South");
        this.setContentPane(mainPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
    }

    private void initList() {
        DefaultListModel listModel = new DefaultListModel();
        this.list = new JList(listModel);
        this.list.addListSelectionListener(this);
    }

    private void initButtons() {
        Dimension dim = new Dimension(90, 25);
        this.buttonAdd = new JButton(IconLoader.icon("document_add.png"));
        this.buttonAdd.setToolTipText(I18N.getString(this.getClass(), "add-condition-to-filter"));
        this.buttonAdd.addActionListener(this);
        this.buttonEdit = new JButton(IconLoader.icon("document_edit.png"));
        this.buttonEdit.setEnabled(false);
        this.buttonEdit.setToolTipText(I18N.getString(this.getClass(), "edit-selected-condition"));
        this.buttonEdit.addActionListener(this);
        this.buttonRemove = new JButton(IconLoader.icon("delete_small.gif"));
        this.buttonRemove.setEnabled(false);
        this.buttonRemove.setToolTipText(I18N.getString(this.getClass(), "remove-selected-conditions"));
        this.buttonRemove.addActionListener(this);
        this.buttonAddParenthesis = new JButton(I18N.getString(this.getClass(), "parenthesis"));
        this.buttonAddParenthesis.setToolTipText(I18N.getString(this.getClass(), "add-parenthesis-to-filter"));
        this.buttonAddParenthesis.setPreferredSize(dim);
        this.buttonAddParenthesis.setMinimumSize(dim);
        this.buttonAddParenthesis.setMaximumSize(dim);
        this.buttonAddParenthesis.addActionListener(this);
        this.buttonClear = new JButton(IconLoader.icon("document_delete.png"));
        this.buttonClear.setToolTipText(I18N.getString(this.getClass(), "remove-all-conditions-from-filter"));
        this.buttonClear.addActionListener(this);
    }

    private void initOKCancelPanel() {
        this.okCancelPanel = new OKCancelHelpPanel(SQLFilterBuilderDialog.class.getName());
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                SQLFilterBuilderDialog.this.setVisible(false);
            }
        });
    }

    private void editCondition() {
        Object conditionObj = this.list.getSelectedValue();
        if (conditionObj == null) {
            LOGGER.warn((Object)I18N.getString(this.getClass(), "you-tried-to-edit-a-condition-none-was-selected"));
            return;
        }
        if (!(conditionObj instanceof SQLFilterBuilderRow)) {
            LOGGER.warn((Object)I18N.getString(this.getClass(), "you-tried-to-edit-a-condition-the-condition-was-not-an-object-of-type-sqlfilterbuilderrow"));
            return;
        }
        SQLFilterBuilderRow condition = (SQLFilterBuilderRow)conditionObj;
        SQLFilterBuilderConditionEditionDialog editDialog = new SQLFilterBuilderConditionEditionDialog(this, true, condition.isFirstCondition(), condition, false, this.ds);
        editDialog.setVisible(true);
        if (editDialog.wasOkPressed()) {
            int index = this.list.getSelectedIndex();
            ((DefaultListModel)this.list.getModel()).set(index, editDialog.getConditionRow());
        }
    }

    private void removeCondition() {
        int selectedIndex = this.list.getSelectedIndex();
        if (selectedIndex == -1) {
            LOGGER.warn((Object)I18N.getString(this.getClass(), "you-tried-to-remove-a-condition-none-was-selected"));
            return;
        }
        Object conditionObj = this.list.getSelectedValue();
        SQLFilterBuilderRow condition = (SQLFilterBuilderRow)conditionObj;
        if (condition.isParenthesisOpen || condition.isParenthesisClose) {
            LOGGER.warn((Object)I18N.getString(this.getClass(), "you-tried-to-remove-a-parenthesis"));
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getString(this.getClass(), "you-tried-to-remove-a-parenthesis"));
            return;
        }
        try {
            ((DefaultListModel)this.list.getModel()).remove(selectedIndex);
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error((Object)"", (Throwable)ex);
            return;
        }
        if (selectedIndex < ((DefaultListModel)this.list.getModel()).getSize()) {
            this.list.setSelectedIndex(selectedIndex);
            Object c = ((DefaultListModel)this.list.getModel()).getElementAt(0);
            if (c == null) {
                return;
            }
            if (condition.isFirstCondition) {
                ((SQLFilterBuilderRow)c).setFirstCondition(true);
            }
        }
    }

    private void addCondition() {
        if (((DefaultListModel)this.list.getModel()).isEmpty()) {
            SQLFilterBuilderConditionEditionDialog dialog = new SQLFilterBuilderConditionEditionDialog(this, true, true, null, false, this.ds);
            dialog.setVisible(true);
            if (dialog.wasOkPressed()) {
                SQLFilterBuilderRow newCondition = dialog.getConditionRow();
                ((DefaultListModel)this.list.getModel()).addElement(newCondition);
            }
        } else {
            int selectedIndex = this.list.getSelectedIndex();
            if (selectedIndex == -1) {
                selectedIndex = this.list.getModel().getSize() - 1;
            }
            Object conditionObj = this.list.getModel().getElementAt(selectedIndex);
            SQLFilterBuilderRow condition = (SQLFilterBuilderRow)conditionObj;
            if (condition.isParenthesisOpen || condition.isParenthesisClose) {
                LOGGER.warn((Object)I18N.getString(this.getClass(), "you-tried-to-add-a-condition-selected-condition-must-not-be-a-parenthesis"));
                DialogFactory.showWarningDialog(this, I18N.getString(this.getClass(), "you-must-select-a-condition-behind-what-the-new-condition-will-be-inserted-selected-condition-can-not-be-a-parenthesis"), I18N.getString(this.getClass(), "warning"));
                return;
            }
            SQLFilterBuilderConditionEditionDialog dialog = new SQLFilterBuilderConditionEditionDialog(this, true, false, null, false, this.ds);
            dialog.setVisible(true);
            if (dialog.wasOkPressed()) {
                SQLFilterBuilderRow newCondition = dialog.getConditionRow();
                ((DefaultListModel)this.list.getModel()).add(selectedIndex + 1, newCondition);
            }
        }
    }

    public void clear() {
        ((DefaultListModel)this.list.getModel()).clear();
    }

    public String getSQLFilterText() {
        String filter = "";
        int numConditions = this.list.getModel().getSize();
        int i = 0;
        while (i < numConditions) {
            SQLFilterBuilderRow condition = (SQLFilterBuilderRow)this.list.getModel().getElementAt(i);
            filter = String.valueOf(filter) + condition.getSQLText();
            ++i;
        }
        return filter;
    }

    public Filter getFilter() {
        Filter filter = null;
        int numConditions = this.list.getModel().getSize();
        int i = 0;
        while (i < numConditions) {
            SQLFilterBuilderRow condition = (SQLFilterBuilderRow)this.list.getModel().getElementAt(i);
            Filter newFilter = condition.getFilter();
            if (filter == null) {
                filter = newFilter;
            } else {
                String logicConnector = condition.getConnectOperation();
                if (logicConnector != null) {
                    if (logicConnector.equals("AND     ")) {
                        filter = filter.and(newFilter);
                    } else if (logicConnector.equals("OR      ")) {
                        filter = filter.or(newFilter);
                    }
                }
            }
            ++i;
        }
        return filter;
    }

    public boolean wasOKPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    public FeatureSchema getSchema() {
        return this.schema;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean hasSelected = this.list.getSelectedIndices().length > 0;
        this.buttonEdit.setEnabled(hasSelected);
        this.buttonRemove.setEnabled(hasSelected);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.buttonAdd) {
            this.addCondition();
        } else if (e.getSource() == this.buttonClear) {
            this.clear();
        } else if (e.getSource() == this.buttonEdit) {
            this.editCondition();
        } else if (e.getSource() == this.buttonRemove) {
            this.removeCondition();
        }
    }
}

