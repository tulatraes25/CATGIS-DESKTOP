/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.visitor.FilterToStringTranslator;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class EditQueryExpressionDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EditQueryExpressionDialog.class);
    private Filter filter;
    private JTextArea textArea;
    private FeatureSchema schema;
    private OKCancelPanel okCancelPanel;
    private Filter operator;
    protected FilterToStringTranslator filterTranslator = new FilterToStringTranslator();

    public EditQueryExpressionDialog(JFrame parent, boolean modal, Filter filter, FeatureSchema schema) {
        super((Frame)parent, modal);
        this.filter = filter;
        this.schema = schema;
        this.setTitle(I18N.getString("org.saig.jump.widgets.query.EditQueryExpressionDialog.Edit-alphanumeric-expression"));
        this.setContentPane(this.getRootPanel());
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel getRootPanel() {
        JPanel rootPanel = new JPanel(new GridBagLayout());
        this.textArea = new JTextArea();
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setColumns(50);
        this.textArea.setRows(10);
        JLabel label = new JLabel();
        this.textArea.setFont(label.getFont());
        this.textArea.revalidate();
        if (this.filter != null) {
            this.textArea.setText(this.filterTranslator.translateFilter(this.filter));
        }
        this.textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(22, 31);
        scrollPane.setViewportView(this.textArea);
        FormUtils.addRowInGBL(rootPanel, 0, 0, scrollPane);
        FormUtils.addRowInGBL(rootPanel, 1, 0, this.createOkCancelPanel());
        FormUtils.addFiller(rootPanel, 2, 0);
        return rootPanel;
    }

    private boolean isInputValid() {
        try {
            this.operator = (Filter)ExpressionBuilder.parse(this.schema, this.textArea.getText());
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.operator = null;
            this.showExpressionErrorDialog(this.textArea.getText(), e.getMessage());
            return false;
        }
        return true;
    }

    private void showExpressionErrorDialog(String expression, String reason) {
        String message = "";
        if (expression != null && expression.length() > 125) {
            message = String.valueOf(expression.substring(0, 124)) + " ...";
        }
        DialogFactory.showErrorDialog(this, String.valueOf(I18N.getMessage("org.saig.jump.plugin.query.QueryWizardDialog.the-expression-{0}-is-not-valid", new Object[]{message})) + ": " + reason, this.getTitle());
    }

    public OKCancelPanel createOkCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (EditQueryExpressionDialog.this.okCancelPanel.wasOKPressed()) {
                    if (EditQueryExpressionDialog.this.isInputValid()) {
                        EditQueryExpressionDialog.this.setVisible(false);
                    }
                } else {
                    EditQueryExpressionDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    public Filter getFilter() {
        if (this.operator != null) {
            return this.operator;
        }
        return this.filter;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }
}

