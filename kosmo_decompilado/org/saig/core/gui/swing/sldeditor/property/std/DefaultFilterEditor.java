/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.gui.swing.sldeditor.property.FilterEditor;
import org.saig.core.gui.swing.sldeditor.style.StyleDialog;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.query.LayerQueryWizardDialog;

public class DefaultFilterEditor
extends FilterEditor {
    private static final long serialVersionUID = 1L;
    public static final Icon WIZARD_ICON = IconLoader.icon("wand.png");
    public static final Icon LANGUAGE_ICON = IconLoader.icon("country.gif");
    private Filter filter;
    private JTextArea txtExpression;
    private Exception lastException;
    private String lastInput;
    private JButton btnWizard;
    private LayerQueryWizardDialog dialog;

    public DefaultFilterEditor(FeatureSchema featureType) {
        this.setLayout(new BorderLayout());
        this.txtExpression = new JTextArea();
        this.txtExpression.setLineWrap(true);
        this.txtExpression.setWrapStyleWord(true);
        this.txtExpression.setColumns(60);
        this.txtExpression.setRows(3);
        JScrollPane pane = new JScrollPane(this.txtExpression, 22, 31);
        this.txtExpression.setEditable(false);
        this.btnWizard = new JButton(WIZARD_ICON);
        this.btnWizard.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor.configure-filter"));
        this.btnWizard.setPreferredSize(FormUtils.getButtonDimension());
        this.add(pane);
        this.add((Component)this.btnWizard, "East");
        this.btnWizard.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultFilterEditor.this.openDialog();
            }
        });
    }

    public DefaultFilterEditor(Filter f, FeatureSchema featureType) {
        this(featureType);
        this.setFilter(f);
    }

    public void openDialog() {
        if (this.dialog == null) {
            Window w = FormUtils.getWindowForComponent(this);
            this.dialog = new LayerQueryWizardDialog((JDialog)w, true, StyleDialog.getMapContext());
            this.dialog.setTitle(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor.filter-editor"));
        }
        this.dialog.setFilter(this.filter);
        this.dialog.setVisible(true);
        if (this.dialog.exitOk()) {
            this.filter = this.dialog.getFilter();
            this.txtExpression.setText(this.dialog.getRawText());
        }
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public String getFormattedErrorMessage() {
        if (this.lastException == null) {
            return null;
        }
        if (this.lastException instanceof ParseException) {
            return ExpressionBuilder.getFormattedErrorMessage((ParseException)this.lastException, this.lastInput);
        }
        return I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFilterEditor.current-input-is-an-expression-not-a-filter");
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
        this.txtExpression.setText(filter.toString());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.txtExpression.setEnabled(enabled);
        this.btnWizard.setEnabled(enabled);
    }
}

