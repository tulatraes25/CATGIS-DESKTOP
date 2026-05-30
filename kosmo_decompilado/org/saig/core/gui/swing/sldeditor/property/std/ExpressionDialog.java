/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class ExpressionDialog
extends JDialog
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private FeatureSchema schema;
    private boolean exitOk;
    private Object lastException;
    private String lastInput;
    private JPanel contentPanel;
    private JPanel editingPanel;
    private JPanel commandPanel;
    private JButton btnOk;
    private JButton btnCancel;
    private JTextArea txaExpression;
    private JLabel lblOperators;
    private JLabel lblAttributes;
    private JComboBox cmbOperators;
    private JComboBox cmbAttributes;

    public ExpressionDialog(Frame parent, boolean modal, FeatureSchema schema) {
        super(parent, modal);
        this.schema = schema;
        this.init();
        this.setLocationRelativeTo(parent);
    }

    public ExpressionDialog(Dialog parent, boolean modal, FeatureSchema schema) {
        super(parent, modal);
        this.schema = schema;
        this.init();
        this.setLocationRelativeTo(parent);
    }

    private void init() {
        String[] operators = new String[]{"+", "-", "*", "/", "<", "<=", "==", ">=", ">"};
        String[] attributes = this.getAttributesNamesFromType();
        this.cmbAttributes = new JComboBox<String>(new DefaultComboBoxModel<String>(attributes));
        this.cmbOperators = new JComboBox<String>(new DefaultComboBoxModel<String>(operators));
        this.lblAttributes = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionDialog.attributes"));
        this.lblOperators = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionDialog.operators"));
        this.txaExpression = new JTextArea();
        this.txaExpression.setRows(4);
        this.btnOk = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionDialog.ok"));
        this.btnCancel = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionDialog.cancel"));
        this.commandPanel = new JPanel();
        this.editingPanel = new JPanel();
        this.contentPanel = new JPanel();
        this.commandPanel.setLayout(new FlowLayout(2, 3, 3));
        this.commandPanel.add(this.btnOk);
        this.commandPanel.add(this.btnCancel);
        this.editingPanel.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL((JComponent)this.editingPanel, 0, 0, this.lblAttributes, (JComponent)this.cmbAttributes);
        FormUtils.addRowInGBL((JComponent)this.editingPanel, 0, 2, this.lblOperators, (JComponent)this.cmbOperators);
        FormUtils.addFiller(this.editingPanel, 1, 0, new JScrollPane(this.txaExpression));
        this.contentPanel.setLayout(new BorderLayout());
        this.contentPanel.add(this.editingPanel);
        this.contentPanel.add((Component)this.commandPanel, "South");
        this.setContentPane(this.contentPanel);
        this.setTitle(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionDialog.expression-wizard"));
        this.pack();
        this.btnOk.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ExpressionDialog.this.exitOk = true;
                ExpressionDialog.this.dispose();
            }
        });
        this.btnCancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ExpressionDialog.this.exitOk = false;
                ExpressionDialog.this.dispose();
            }
        });
        ActionListener selectionPaster = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox combo = (JComboBox)e.getSource();
                String selection = (String)combo.getSelectedItem();
                ExpressionDialog.this.txaExpression.insert(selection, ExpressionDialog.this.txaExpression.getCaretPosition());
            }
        };
        this.cmbAttributes.addActionListener(selectionPaster);
        this.cmbOperators.addActionListener(selectionPaster);
        this.pack();
    }

    public boolean exitOk() {
        return this.exitOk;
    }

    private String[] getAttributesNamesFromType() {
        ArrayList<String> attributes = new ArrayList<String>();
        if (this.schema == null) {
            return new String[0];
        }
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            attributes.add(this.schema.getAttributeName(i));
            ++i;
        }
        return attributes.toArray(new String[attributes.size()]);
    }

    public void setExpression(Expression e) {
        this.txaExpression.setText(e.toString());
    }

    public void setFilter(Filter f) {
        this.txaExpression.setText(f.toString());
    }

    public Expression getExpression() {
        Expression result = null;
        this.lastInput = this.txaExpression.getText();
        try {
            result = (Expression)ExpressionBuilder.parse(this.lastInput);
            this.lastException = null;
        }
        catch (ParseException e) {
            this.lastException = e;
            result = null;
        }
        catch (ClassCastException e) {
            this.lastException = e;
            result = null;
        }
        return result;
    }

    public Filter getFilter() {
        Filter result = null;
        this.lastInput = this.txaExpression.getText().trim();
        if (this.lastInput.equals("")) {
            return null;
        }
        try {
            result = (Filter)ExpressionBuilder.parse(this.lastInput);
            this.lastException = null;
        }
        catch (ParseException e) {
            this.lastException = e;
            result = null;
        }
        catch (ClassCastException e) {
            this.lastException = e;
            result = null;
        }
        return result;
    }

    public String getRawText() {
        return this.txaExpression.getText();
    }

    public String getFormattedErrorMessage() {
        if (this.lastException == null) {
            return null;
        }
        if (this.lastException instanceof ParseException) {
            return ExpressionBuilder.getFormattedErrorMessage((ParseException)this.lastException, this.lastInput);
        }
        return I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.ExpressionDialog.current-input-is-a-filter-not-an-expression");
    }

    public void setRawText(String string) {
        this.txaExpression.setText(string);
    }
}

