/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Frame;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.MySQLDataSource;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.widgets.query.FeatureFieldValuesDialog;
import org.saig.jump.widgets.query.LayerQueryWizardPanel;

public class LayerQueryWizardByFCPanel
extends LayerQueryWizardPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LayerQueryWizardByFCPanel.class);
    private FeatureCollection featCol;

    public LayerQueryWizardByFCPanel(FeatureCollection fc, PlugInContext context) {
        super(fc.getFeatureSchema(), null, false, context);
        this.featCol = fc;
        this.initialize();
    }

    @Override
    public void initialize() {
        this.initializeOperators();
        this.initializeChoosers();
        this.createQueryFieldsPanel();
        this.createLogicalOperatorsPanel();
        this.createExecutionPanel();
        this.createOKcancelPanel();
        this.updateButtons();
    }

    @Override
    protected void fillGeometricOperations() {
        this.operatorsCombo.removeAllItems();
        boolean isMysql = false;
        if (this.featCol instanceof FeatureCollectionOnDemand && ((FeatureCollectionOnDemand)this.featCol).getDataAccesor() instanceof AbstractJDBCDataSource) {
            isMysql = ((FeatureCollectionOnDemand)this.featCol).getDataAccesor() instanceof MySQLDataSource;
        }
        if (!isMysql) {
            int i = 0;
            while (i < GEOMETRIC_OPERATORS.length) {
                this.operatorsCombo.addItem(GEOMETRIC_OPERATORS[i]);
                ++i;
            }
        } else {
            this.operatorsCombo.addItem(INSIDE_THE_BBOX);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    protected void allRegisterAction() {
        FeatureFieldValuesDialog fieldValuesDialog = new FeatureFieldValuesDialog((Frame)JUMPWorkbench.getFrameInstance(), true);
        String attributePublicName = (String)this.leftAttributePublicNamesComboBox.getSelectedItem();
        String attributeName = this.featureSchema.getPublicAttribute(attributePublicName).getName();
        if (this.featureSchema.getAttributeType(attributeName).toJavaClass().equals(Geometry.class)) {
            if (this.leftGeometryAttributesComboBox.getSelectedItem().equals("-----")) {
                LayerQueryWizardPanel.ExecuteWKTWaitDialog dialog = new LayerQueryWizardPanel.ExecuteWKTWaitDialog(this, JUMPWorkbench.getFrameInstance(), this);
                dialog.setVisible(true);
                return;
            } else {
                String expressionString = attributeName;
                try {
                    String selectedGeometryAttribute = (String)this.leftGeometryAttributesComboBox.getSelectedItem();
                    expressionString = this.buildExpressionFromSelectedAttribute(selectedGeometryAttribute, attributeName).toString();
                    Expression expr = null;
                    expr = (Expression)ExpressionBuilder.parse(this.featureSchema, expressionString);
                    fieldValuesDialog.setValues(this.featCol, attributeName, expr);
                    GUIUtil.centreOnScreen(fieldValuesDialog);
                    fieldValuesDialog.setVisible(true);
                    Object searchLiteral = fieldValuesDialog.getSelection();
                    if (searchLiteral == null) return;
                    this.txtValue.setText(searchLiteral.toString());
                    this.txtValue.setCaretPosition(0);
                    return;
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    this.showExpressionErrorDialog(expressionString, expressionString);
                    return;
                }
            }
        } else {
            fieldValuesDialog.setValues(this.featCol, attributeName);
            GUIUtil.centreOnScreen(fieldValuesDialog);
            fieldValuesDialog.setVisible(true);
            Object searchLiteral = fieldValuesDialog.getSelection();
            if (searchLiteral == null) return;
            if (searchLiteral instanceof java.util.Date && !(searchLiteral instanceof Date) && !(searchLiteral instanceof Time) && !(searchLiteral instanceof Timestamp)) {
                this.txtValue.setText(DateFormatManager.getDateFormat().format((java.util.Date)searchLiteral));
            } else {
                this.txtValue.setText(searchLiteral.toString());
            }
            this.txtValue.setCaretPosition(0);
        }
    }

    @Override
    protected void cleanQuery() {
        this.filter = null;
        this.updateTextArea();
    }

    @Override
    protected void executeQuery() throws Exception {
        this.updateButtons();
    }

    @Override
    protected void highLightSelection() {
    }

    @Override
    protected void loadLastStatus() {
    }

    @Override
    protected void okCancelPanelAction() {
        this.exitOk = this.okCancelPanel.wasOKPressed();
        this.hideDialog();
    }

    @Override
    protected void saveResults() {
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
    protected void viewResults() {
    }
}

