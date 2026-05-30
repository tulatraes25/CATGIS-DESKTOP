/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class RecordFieldValuesDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final int MAX_VISIBLE_VALUES = 10000;
    private TableRecordDataSource ds;
    private String fieldName;
    private Object selectedValue;
    private JPanel valueListPanel;
    private JList valueList;
    private OKCancelPanel okCancelPanel;

    public RecordFieldValuesDialog(Frame parentFrame, boolean modal) {
        super(parentFrame, modal);
        this.setTitle("Seleccionar valor atributo");
        this.initComponents();
    }

    public void setValues(TableRecordDataSource ds, String fieldName) {
        this.ds = ds;
        this.fieldName = fieldName;
        this.buildList();
    }

    private void buildList() {
        DefaultListModel<Object> m = new DefaultListModel<Object>();
        TreeSet<Object> orderedValues = null;
        Set<Object> values = this.ds.getDistintsValues(this.fieldName, 10000);
        orderedValues = new TreeSet<Object>(values);
        for (Object element : orderedValues) {
            m.addElement(element);
        }
        this.valueList.setModel(m);
    }

    public Object getSelection() {
        return this.selectedValue;
    }

    void jList1_valueChanged(ListSelectionEvent e) {
        int index = this.valueList.getSelectedIndex();
        this.valueList.ensureIndexIsVisible(index);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent evt) {
                RecordFieldValuesDialog.this.closeDialog();
            }
        });
        mainPanel.add((Component)this.getValueListPanel(), "Center");
        mainPanel.add((Component)this.getOkCancelPanel(), "South");
        this.setSize(300, 400);
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    RecordFieldValuesDialog.this.closeDialog();
                }
            });
        }
        return this.okCancelPanel;
    }

    protected void closeDialog() {
        if (this.okCancelPanel.wasOKPressed()) {
            if (this.isInputValid()) {
                this.selectedValue = this.valueList.getSelectedValue();
                this.setVisible(false);
            }
        } else {
            this.selectedValue = null;
            this.setVisible(false);
        }
    }

    protected boolean isInputValid() {
        if (this.valueList.getSelectedValue() == null) {
            DialogFactory.showErrorDialog(this, I18N.getString("org.saig.jump.widgets.query.FeatureFieldValuesDialog.you-must-select-at-least-one-list-value"), I18N.getString("org.saig.jump.widgets.query.FeatureFieldValuesDialog.value-no-selected"));
            return false;
        }
        return true;
    }

    public JPanel getValueListPanel() {
        if (this.valueListPanel == null) {
            this.valueListPanel = new JPanel(new BorderLayout());
            this.valueListPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.plugin.query.FeatureFieldValuesDialog.select-a-value")));
            this.valueList = new JList();
            this.valueList.setSelectionMode(0);
            this.valueList.addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    RecordFieldValuesDialog.this.jList1_valueChanged(e);
                }
            });
            JScrollPane listScrollPane = new JScrollPane(this.valueList);
            this.valueListPanel.add(listScrollPane);
        }
        return this.valueListPanel;
    }
}

