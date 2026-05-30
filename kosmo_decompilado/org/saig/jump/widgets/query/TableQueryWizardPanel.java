/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Frame;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.query.AbstractQueryWizardPanel;
import org.saig.jump.widgets.query.TableRecordFieldValuesDialog;

public class TableQueryWizardPanel
extends AbstractQueryWizardPanel {
    private static final long serialVersionUID = 1L;
    protected TableRecordDataSource ds;

    public TableQueryWizardPanel(TableRecordDataSource table, PlugInContext context) {
        super(table.getSchema(), context);
        this.ds = table;
        this.initialize();
    }

    @Override
    protected void allRegisterAction() {
        TableRecordFieldValuesDialog fieldValuesDialog = new TableRecordFieldValuesDialog((Frame)JUMPWorkbench.getFrameInstance(), true);
        String attributePublicName = (String)this.leftAttributePublicNamesComboBox.getSelectedItem();
        String attributeName = this.featureSchema.getPublicAttribute(attributePublicName).getName();
        fieldValuesDialog.setValues(this.ds, attributeName);
        GUIUtil.centreOnScreen(fieldValuesDialog);
        fieldValuesDialog.setVisible(true);
        Object searchLiteral = fieldValuesDialog.getSelection();
        if (searchLiteral != null) {
            if (searchLiteral instanceof java.util.Date && !(searchLiteral instanceof Date) && !(searchLiteral instanceof Time) && !(searchLiteral instanceof Timestamp)) {
                this.txtValue.setText(DateFormatManager.getDateFormat().format((java.util.Date)searchLiteral));
            } else {
                this.txtValue.setText(searchLiteral.toString());
            }
            this.txtValue.setCaretPosition(0);
        }
    }

    @Override
    protected Expression buildExpressionFromSelectedAttribute(String selectedGeometryAttribute, String geometryFieldName) throws Exception {
        return null;
    }

    @Override
    protected void cleanQuery() {
        this.filter = null;
        this.updateTextArea();
    }

    @Override
    protected List<Feature> cloneResults(Collection<Feature> results, FeatureSchema schema) {
        return null;
    }

    @Override
    protected void executeButtonAction() {
    }

    @Override
    protected void executeSaveResultAction() {
    }

    @Override
    protected void fillGeometricOperations() {
    }

    @Override
    protected void okCancelPanelAction() {
        this.exitOk = this.okCancelPanel.wasOKPressed();
        this.hideDialog();
    }

    @Override
    protected void openCalculator() {
    }

    @Override
    protected void saveResults() {
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
        this.updateTextArea();
    }

    @Override
    protected void updateButtons() {
        boolean hasQuery = this.queryTextArea.getText() != null && !this.queryTextArea.getText().trim().equals("") && !this.queryTextArea.getText().trim().equals(NULL_QUERY);
        this.executeButton.setEnabled(false);
        this.cleanButton.setEnabled(hasQuery);
        this.saveFilterButton.setEnabled(this.filter != null);
        this.saveRecordsButton.setEnabled(false);
        this.viewRecordsButton.setEnabled(false);
        this.calculatorButton.setEnabled(false);
    }

    @Override
    protected void updateFieldsOfQuery() {
        this.leftAttributePublicNamesComboBox.removeAllItems();
        this.rightAttributePublicNamesComboBox.removeAllItems();
        Vector<String> publicNames = new Vector<String>();
        int numAttrs = this.featureSchema.getAttributeCount();
        int i = 0;
        while (i < numAttrs) {
            if (this.featureSchema.getAttribute(i).isVisibility()) {
                publicNames.add(this.featureSchema.getPublicName(i));
            }
            ++i;
        }
        Collections.sort(publicNames, Collator.getInstance(I18N.getLocale()));
        i = 0;
        while (i < publicNames.size()) {
            this.leftAttributePublicNamesComboBox.addItem(publicNames.get(i));
            this.rightAttributePublicNamesComboBox.addItem(publicNames.get(i));
            ++i;
        }
    }

    @Override
    protected void viewResults() {
    }
}

