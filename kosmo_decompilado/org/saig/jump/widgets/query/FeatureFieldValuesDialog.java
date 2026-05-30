/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.core.filter.Expression;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class FeatureFieldValuesDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final int MAX_VISIBLE_VALUES = 10000;
    private FeatureCollection featureCollection;
    private String fieldName;
    private Expression exprGeometry;
    private Object selectedValue;
    private JPanel valueListPanel;
    private JList valueList;
    private OKCancelPanel okCancelPanel;

    public FeatureFieldValuesDialog(Frame parentFrame, boolean modal) {
        super(parentFrame, modal);
        this.setTitle(I18N.getString("org.saig.jump.plugin.query.FeatureFieldValuesDialog.values"));
        this.initComponents();
    }

    public void setValues(FeatureCollection featureCollection, String fieldName, Expression exprGeometry) {
        this.featureCollection = featureCollection;
        this.fieldName = fieldName;
        this.exprGeometry = exprGeometry;
        this.buildList();
    }

    public void setValues(FeatureCollection featureCollection, String fieldName) {
        this.featureCollection = featureCollection;
        this.fieldName = fieldName;
        this.exprGeometry = null;
        this.buildList();
    }

    private void buildList() {
        DefaultListModel<Object> m = new DefaultListModel<Object>();
        TreeSet<Object> orderedValues = null;
        Set<Object> values = null;
        values = this.exprGeometry == null ? this.featureCollection.getDistintsValues(this.fieldName, 10000) : this.featureCollection.getDistintsValues(this.exprGeometry, 10000);
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
                FeatureFieldValuesDialog.this.closeDialog();
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
                    FeatureFieldValuesDialog.this.closeDialog();
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
                    FeatureFieldValuesDialog.this.jList1_valueChanged(e);
                }
            });
            this.valueList.setCellRenderer(new FieldValuesListCellRenderer());
            JScrollPane listScrollPane = new JScrollPane(this.valueList);
            this.valueListPanel.add(listScrollPane);
        }
        return this.valueListPanel;
    }

    private static class FieldValuesListCellRenderer
    extends JLabel
    implements ListCellRenderer {
        private static final long serialVersionUID = 1L;

        private FieldValuesListCellRenderer() {
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                if (value instanceof java.util.Date && !(value instanceof Date) && !(value instanceof Time) && !(value instanceof Timestamp)) {
                    this.setText(DateFormatManager.getDateFormat().format((java.util.Date)value));
                } else {
                    this.setText(value.toString());
                }
            }
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            this.setOpaque(true);
            return this;
        }
    }
}

