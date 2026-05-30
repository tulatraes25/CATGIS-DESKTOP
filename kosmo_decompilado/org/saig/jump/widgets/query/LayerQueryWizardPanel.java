/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.operation.valid.IsValidOp
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.io.WKTReader;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.UndoableEditReceiver;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.EnterWKTDialog;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.desktop.plugins.analysis.CalculateAttributeByExpressionPlugIn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.dbdatasource.MySQLDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FunctionExpressionImpl;
import org.saig.core.filter.function.FilterFunction_area;
import org.saig.core.filter.function.FilterFunction_geomLength;
import org.saig.core.filter.function.FilterFunction_getX;
import org.saig.core.filter.function.FilterFunction_getY;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.util.DateFormatManager;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.query.AbstractQueryWizardPanel;
import org.saig.jump.widgets.query.FeatureFieldValuesDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class LayerQueryWizardPanel
extends AbstractQueryWizardPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LayerQueryWizardPanel.class);
    public static final String CONTAINS = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.contains");
    public static final String CROSSES_WITH = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.crosses-with");
    public static final String WITHIN = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.within");
    public static final String DISJOINT = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.disjoint");
    public static final String EQUAL_THAN = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.equal-than");
    public static final String INSIDE_THE_BBOX = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.inside-the-bbox");
    public static final String INTERSECTS_WITH = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.intersects-with");
    public static final String OVERLAPS = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.overlaps");
    private static final String AREA_ATTRIBUTE = I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.area");
    private static final String LENGTH_ATTRIBUTE = I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.length");
    private static final String PERIMETER_ATTRIBUTE = I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.perimeter");
    private static final String X_COORDINATE_ATTRIBUTE = I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.x-coordinate");
    private static final String Y_COORDINATE_ATTRIBUTE = I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.y-coordinate");
    private static final String[] POINT_GEOMETRY_ATTRIBUTES = new String[]{"-----", X_COORDINATE_ATTRIBUTE, Y_COORDINATE_ATTRIBUTE};
    private static final String[] LINE_GEOMETRY_ATTRIBUTES = new String[]{"-----", LENGTH_ATTRIBUTE};
    private static final String[] POLYGON_GEOMETRY_ATTRIBUTES = new String[]{"-----", AREA_ATTRIBUTE, PERIMETER_ATTRIBUTE};
    protected static final String[] GEOMETRIC_OPERATORS = new String[]{CONTAINS, CROSSES_WITH, WITHIN, DISJOINT, EQUAL_THAN, INSIDE_THE_BBOX, INTERSECTS_WITH, OVERLAPS, IS_NULL};
    protected static final String[] OPERATORS = new String[]{GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL_TO, LESS_OR_EQUAL_TO, DIFFERENT_TO, EQUAL_TO, LIKE, CONTAINS, CROSSES_WITH, WITHIN, DISJOINT, EQUAL_THAN, INSIDE_THE_BBOX, INTERSECTS_WITH, OVERLAPS, IS_NULL};
    private Layer layer;
    private List<Feature> results;
    private InfoFrame infoFrame;
    private JPanel pnLayersPanel;
    private JComboBox layersCombo;
    private EnterWKTDialog wktDialog;
    private boolean showLayersSelectionPanel;

    public LayerQueryWizardPanel(FeatureSchema featureSchema, Layer layer, boolean showLayersSelectionPanel, PlugInContext context) {
        super(featureSchema, context);
        this.layer = layer;
        this.showLayersSelectionPanel = showLayersSelectionPanel;
        this.results = new ArrayList<Feature>();
    }

    @Override
    public void initialize() {
        this.initializeOperators();
        this.initializeChoosers();
        this.infoFrame = new InfoFrame(this.context.getWorkbenchContext(), (LayerManagerProxy)((Object)this.context.getActiveInternalFrame()), ((TaskFrameProxy)((Object)this.context.getActiveInternalFrame())).getTaskFrame());
        this.createWKTDialog();
        if (this.showLayersSelectionPanel) {
            this.createLayersSelectionPanel();
        }
        this.createQueryFieldsPanel();
        this.createLogicalOperatorsPanel();
        this.createExecutionPanel();
        this.createOKcancelPanel();
        if (this.featureSchema == null) {
            this.layerSelected();
        }
        this.loadLastStatus();
        this.updateButtons();
    }

    @Override
    protected void initializeOperators() {
        super.initializeOperators();
        this.opgeom.put(CONTAINS, "CONTAINS");
        this.opgeom.put(CROSSES_WITH, "CROSSES");
        this.opgeom.put(WITHIN, "WITHIN");
        this.opgeom.put(DISJOINT, "DISJOINT");
        this.opgeom.put(EQUAL_THAN, "EQUALS");
        this.opgeom.put(INSIDE_THE_BBOX, "BBOX");
        this.opgeom.put(INTERSECTS_WITH, "INTERSECTS");
        this.opgeom.put(OVERLAPS, "OVERLAPS");
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
        int geometryType = this.featureSchema.getGeometryType();
        this.leftGeometryAttributesComboBox.removeAllItems();
        this.rightGeometryAttributesComboBox.removeAllItems();
        switch (geometryType) {
            case 1: {
                int i2 = 0;
                while (i2 < POINT_GEOMETRY_ATTRIBUTES.length) {
                    this.leftGeometryAttributesComboBox.addItem(POINT_GEOMETRY_ATTRIBUTES[i2]);
                    this.rightGeometryAttributesComboBox.addItem(POINT_GEOMETRY_ATTRIBUTES[i2]);
                    ++i2;
                }
                break;
            }
            case 2: 
            case 3: {
                int i3 = 0;
                while (i3 < LINE_GEOMETRY_ATTRIBUTES.length) {
                    this.leftGeometryAttributesComboBox.addItem(LINE_GEOMETRY_ATTRIBUTES[i3]);
                    this.rightGeometryAttributesComboBox.addItem(LINE_GEOMETRY_ATTRIBUTES[i3]);
                    ++i3;
                }
                break;
            }
            case 4: 
            case 5: {
                int i4 = 0;
                while (i4 < POLYGON_GEOMETRY_ATTRIBUTES.length) {
                    this.leftGeometryAttributesComboBox.addItem(POLYGON_GEOMETRY_ATTRIBUTES[i4]);
                    this.rightGeometryAttributesComboBox.addItem(POLYGON_GEOMETRY_ATTRIBUTES[i4]);
                    ++i4;
                }
                break;
            }
            default: {
                int i5 = 0;
                while (i5 < DEFAULT_GEOMETRY_ATTRIBUTES.length) {
                    this.leftGeometryAttributesComboBox.addItem(DEFAULT_GEOMETRY_ATTRIBUTES[i5]);
                    this.rightGeometryAttributesComboBox.addItem(DEFAULT_GEOMETRY_ATTRIBUTES[i5]);
                    ++i5;
                }
                break block0;
            }
        }
    }

    @Override
    protected void fillGeometricOperations() {
        this.operatorsCombo.removeAllItems();
        boolean isMysql = false;
        if (this.layer.isDataBaseDataSource()) {
            FeatureCollectionOnDemand fcd = (FeatureCollectionOnDemand)this.layer.getUltimateFeatureCollectionWrapper();
            isMysql = fcd.getDataAccesor() instanceof MySQLDataSource;
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
                ExecuteWKTWaitDialog dialog = new ExecuteWKTWaitDialog(JUMPWorkbench.getFrameInstance(), this);
                dialog.setVisible(true);
                return;
            } else {
                String expressionString = attributeName;
                try {
                    String selectedGeometryAttribute = (String)this.leftGeometryAttributesComboBox.getSelectedItem();
                    expressionString = this.buildExpressionFromSelectedAttribute(selectedGeometryAttribute, attributeName).toString();
                    Expression expr = null;
                    expr = (Expression)ExpressionBuilder.parse(this.featureSchema, expressionString);
                    fieldValuesDialog.setValues(this.layer.getUltimateFeatureCollectionWrapper(), attributeName, expr);
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
            fieldValuesDialog.setValues(this.layer.getUltimateFeatureCollectionWrapper(), attributeName);
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
    protected void insertWKTString() {
        this.wktDialog.setVisible(true);
    }

    @Override
    protected void selectedFeaturesByBuffer() {
        Collection<Layer> layers = this.context.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems();
        if (layers.size() > 0) {
            Collection<Feature> col = this.context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
            Object value = DialogFactory.showInputDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.insert-the-distance-in-meters"), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.buffer-configuration"), "0");
            if (value == null) {
                return;
            }
            try {
                Double buffer = new Double((String)value);
                GeometryFactory factory = new GeometryFactory();
                ArrayList<Geometry> geometrias = new ArrayList<Geometry>();
                for (Feature element : col) {
                    geometrias.add(element.getGeometry());
                }
                Geometry[] geoms = new Geometry[geometrias.size()];
                geometrias.toArray(geoms);
                Geometry geom = factory.createGeometryCollection(geoms).buffer(buffer.doubleValue());
                this.txtValue.setText(geom.toText());
                return;
            }
            catch (NumberFormatException e) {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.the-input-value-is-incorrect"), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.error"));
            }
        }
    }

    @Override
    protected void currentViewEnvelopeByBuffer() {
        Envelope env = this.context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
        Object value = DialogFactory.showInputDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.insert-the-distance-in-meters"), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.buffer-configuration"), "0");
        if (value == null) {
            return;
        }
        try {
            Double buffer = new Double((String)value);
            Geometry geom = EnvelopeUtil.toGeometry(env).buffer(buffer.doubleValue());
            this.txtValue.setText(geom.toText());
            return;
        }
        catch (NumberFormatException e) {
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.the-input-value-is-incorrect"), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.error"));
            return;
        }
    }

    @Override
    protected void cleanQuery() {
        this.filter = null;
        if (this.results.size() > 0) {
            if (this.layer == null) {
                this.context.getLayerViewPanel().getSelectionManager().getFeatureSelection().unselectItems((Layer)this.layersCombo.getSelectedItem(), this.results);
            } else {
                this.context.getLayerViewPanel().getSelectionManager().getFeatureSelection().unselectItems(this.layer, this.results);
            }
        }
        this.results = new ArrayList<Feature>();
        this.updateTextArea();
    }

    @Override
    protected List<Feature> cloneResults(Collection<Feature> results, FeatureSchema schema) {
        ArrayList<Feature> cloneResults = new ArrayList<Feature>();
        for (Feature currentFeature : results) {
            Feature newFeature = FeatureUtil.copyFeature(schema, currentFeature);
            cloneResults.add(newFeature);
        }
        return cloneResults;
    }

    @Override
    protected void executeButtonAction() {
        new ExecuteWaitDialog(JUMPWorkbench.getFrameInstance(), this).setVisible(true);
    }

    @Override
    protected void executeSaveResultAction() {
        new ExecuteSaveResultsWaitDialog(JUMPWorkbench.getFrameInstance(), this).setVisible(true);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    protected void openCalculator() {
        CalculateAttributeByExpressionPlugIn plugIn = new CalculateAttributeByExpressionPlugIn();
        EnableCheck enableCheck = plugIn.getCheck();
        String error = enableCheck.check(this.calculatorButton);
        this.calculatorButton.setEnabled(error == null);
        if (error == null) {
            UndoableEditReceiver undoableEditReceiver;
            UndoableEditReceiver undoableEditReceiver2 = undoableEditReceiver = this.context.getLayerManager() != null ? this.context.getLayerManager().getUndoableEditReceiver() : null;
            if (undoableEditReceiver != null) {
                undoableEditReceiver.startReceiving();
            }
            try {
                try {
                    plugIn.execute(this.context);
                    return;
                }
                catch (Exception e1) {
                    LOGGER.error((Object)"", (Throwable)e1);
                    if (undoableEditReceiver == null) return;
                    undoableEditReceiver.stopReceiving();
                }
                return;
            }
            finally {
                if (undoableEditReceiver != null) {
                    undoableEditReceiver.stopReceiving();
                }
            }
        } else {
            this.calculatorButton.setToolTipText("<HTML><B>" + this.calculatorButton.getName() + "</B><BR>" + error + "</HTML>");
        }
    }

    @Override
    protected void saveResults() {
        if (this.results != null && !this.results.isEmpty()) {
            FeatureDataset fc = null;
            IProjection proj = null;
            if (this.layer != null) {
                fc = new FeatureDataset(this.cloneResults(this.results, this.featureSchema), this.featureSchema);
                proj = this.layer.getProjection();
            } else {
                fc = new FeatureDataset(this.cloneResults(this.results, this.featureSchema), this.featureSchema);
            }
            this.context.getLayerManager().addCategory(StandardCategoryNames.WORKING);
            Layer newLayer = this.context.getLayerManager().addLayer(StandardCategoryNames.WORKING, I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.new-query-layer"), fc);
            newLayer.setProjection(proj);
            newLayer.setFeatureCollectionModified(false);
        }
    }

    @Override
    protected void updateButtons() {
        boolean hasQuery = this.queryTextArea.getText() != null && !this.queryTextArea.getText().trim().equals("") && !this.queryTextArea.getText().trim().equals(NULL_QUERY);
        this.executeButton.setEnabled(hasQuery);
        this.cleanButton.setEnabled(hasQuery);
        this.saveFilterButton.setEnabled(this.filter != null);
        this.saveRecordsButton.setEnabled(this.results != null && this.results.size() > 0);
        this.viewRecordsButton.setEnabled(this.results != null && this.results.size() > 0);
        EnableCheck enableCheck = CalculateAttributeByExpressionPlugIn.createEnableCheck(this.context.getWorkbenchContext());
        String error = enableCheck.check(this.calculatorButton);
        this.calculatorButton.setEnabled(error == null);
        if (error != null) {
            this.calculatorButton.setToolTipText("<HTML><B>" + this.calculatorButton.getName() + "</B><BR>" + error + "</HTML>");
            this.calculatorButton.setEnabled(false);
        } else {
            this.calculatorButton.setEnabled(this.results != null && this.results.size() > 0);
        }
    }

    @Override
    protected void viewResults() {
        if (this.layer == null) {
            this.context.getLayerViewPanel().getSelectionManager().getFeatureSelection().unselectItems((Layer)this.layersCombo.getSelectedItem(), this.results);
        } else {
            this.context.getLayerViewPanel().getSelectionManager().getFeatureSelection().unselectItems(this.layer, this.results);
        }
        this.infoFrame.getModel().clear();
        if (this.layer != null) {
            this.infoFrame.getModel().add(this.layer, this.results);
        } else {
            this.infoFrame.getModel().add((Layer)this.layersCombo.getSelectedItem(), this.results);
        }
        boolean isAdd = false;
        JInternalFrame[] frames = this.context.getWorkbenchFrame().getInternalFrames();
        int i = 0;
        while (i < frames.length) {
            if (frames[i].equals(this.infoFrame)) {
                isAdd = true;
                break;
            }
            ++i;
        }
        Dimension dim = ((AttributeTab)this.infoFrame.getAttributeTab()).getTableSize();
        Rectangle parentBounds = this.context.getWorkbenchContext().getWorkbench().getFrame().getDesktopPane().getBounds();
        this.infoFrame.setSize(Math.min(parentBounds.width, dim.width + 57), 266);
        this.infoFrame.setLocation(0, Math.max(0, parentBounds.height - 266));
        if (!isAdd) {
            this.context.getWorkbenchFrame().addInternalFrame(this.infoFrame);
        } else {
            this.infoFrame.setVisible(true);
        }
    }

    @Override
    protected void okCancelPanelAction() {
        if (this.okCancelPanel.wasOKPressed()) {
            Blackboard bb = this.context.getWorkbenchContext().getBlackboard();
            if (this.showLayersSelectionPanel) {
                PersistentBlackboardPlugIn.get(bb).put(LAST_LAYER_NAME_KEY, ((Layer)this.layersCombo.getSelectedItem()).getName());
            } else {
                PersistentBlackboardPlugIn.get(bb).put(LAST_LAYER_NAME_KEY, this.layer.getName());
            }
            PersistentBlackboardPlugIn.get(bb).put(LAST_ATTRIBUTE_NAME_KEY, this.leftAttributePublicNamesComboBox.getSelectedItem()).put(LAST_OPERATOR_KEY, this.operatorsCombo.getSelectedItem()).put(LAST_GEOMETRY_ATTRIBUTE_KEY, this.leftGeometryAttributesComboBox.getSelectedItem());
            PersistentBlackboardPlugIn.get(bb).put(LAST_VALUE_SELECTED_OPTION_KEY, this.literalRadioButton.isSelected());
            if (this.literalRadioButton.isSelected()) {
                PersistentBlackboardPlugIn.get(bb).put(LAST_VALUE_KEY, this.txtValue.getText());
            } else {
                PersistentBlackboardPlugIn.get(bb).put(LAST_RIGHT_ATTRIBUTE_NAME_KEY, this.rightAttributePublicNamesComboBox.getSelectedItem()).put(LAST_RIGHT_GEOMETRY_ATTRIBUTE_KEY, this.rightGeometryAttributesComboBox.getSelectedItem());
            }
            this.exitOk = true;
        } else {
            this.exitOk = false;
        }
        this.hideDialog();
    }

    protected void loadLastStatus() {
        Blackboard bb = this.context.getWorkbenchContext().getBlackboard();
        String layerName = (String)PersistentBlackboardPlugIn.get(bb).get(LAST_LAYER_NAME_KEY);
        String attributeName = (String)PersistentBlackboardPlugIn.get(bb).get(LAST_ATTRIBUTE_NAME_KEY);
        String operator = (String)PersistentBlackboardPlugIn.get(bb).get(LAST_OPERATOR_KEY);
        String value = (String)PersistentBlackboardPlugIn.get(bb).get(LAST_VALUE_KEY);
        String geometryAttributeValue = (String)PersistentBlackboardPlugIn.get(bb).get(LAST_GEOMETRY_ATTRIBUTE_KEY);
        Boolean literalValueSelected = (Boolean)PersistentBlackboardPlugIn.get(bb).get(LAST_VALUE_SELECTED_OPTION_KEY);
        String rightAttributeName = (String)PersistentBlackboardPlugIn.get(bb).get(LAST_RIGHT_ATTRIBUTE_NAME_KEY);
        String rightGeometryAttributeValue = (String)PersistentBlackboardPlugIn.get(bb).get(LAST_RIGHT_GEOMETRY_ATTRIBUTE_KEY);
        Layer lastLayer = null;
        List<Layer> layersWithoutRaster = this.context.getLayerManager().getNoRasterLayers();
        lastLayer = this.showLayersSelectionPanel ? this.context.getLayerManager().getLayer(layerName) : this.layer;
        if (lastLayer != null && layersWithoutRaster.contains(lastLayer) && (lastLayer.getAttributePublicNames().containsKey(attributeName) || lastLayer.getAttributePublicNames().isEmpty())) {
            if (this.showLayersSelectionPanel) {
                this.layersCombo.setSelectedItem(lastLayer);
                this.layerSelected();
            }
            this.leftAttributePublicNamesComboBox.setSelectedItem(attributeName);
            this.leftGeometryAttributesComboBox.setSelectedItem(geometryAttributeValue);
            this.loadOperations();
            this.operatorsCombo.setSelectedItem(operator);
            if (literalValueSelected == null || literalValueSelected.booleanValue()) {
                this.literalRadioButton.setSelected(true);
                this.updateValueOptions();
                this.txtValue.setText(value);
            } else {
                this.fieldRadioButton.setSelected(true);
                this.updateValueOptions();
                this.rightAttributePublicNamesComboBox.setSelectedItem(rightAttributeName);
                this.rightGeometryAttributesComboBox.setSelectedItem(rightGeometryAttributeValue);
            }
        }
    }

    private void createLayersSelectionPanel() {
        this.pnLayersPanel = new JPanel(new GridBagLayout());
        this.pnLayersPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.layer-to-query")));
        List<Layer> layersWithoutRaster = this.context.getLayerManager().getNoRasterLayers();
        Object[] layers = new Object[layersWithoutRaster.size()];
        layersWithoutRaster.toArray(layers);
        Arrays.sort(layers);
        this.layersCombo = new JComboBox<Object>(layers);
        this.layersCombo.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                LayerQueryWizardPanel.this.layerSelected();
            }
        });
        FormUtils.addRowInGBL((JComponent)this.pnLayersPanel, 0, 0, I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.layer"), (JComponent)this.layersCombo);
        FormUtils.addRowInGBL(this, this.rowIndex++, 0, this.pnLayersPanel);
    }

    @Override
    protected Expression buildExpressionFromSelectedAttribute(String selectedGeometryAttribute, String geometryFieldName) throws Exception {
        FunctionExpressionImpl expr = null;
        AttributeExpression geomFieldExpression = factory.createAttributeExpression(this.featureSchema, geometryFieldName);
        if (selectedGeometryAttribute.equals(AREA_ATTRIBUTE)) {
            FilterFunction_area areaFunction = new FilterFunction_area();
            areaFunction.setArgs(new Expression[]{geomFieldExpression});
            expr = areaFunction;
        } else if (selectedGeometryAttribute.equals(PERIMETER_ATTRIBUTE) || selectedGeometryAttribute.equals(LENGTH_ATTRIBUTE)) {
            FilterFunction_geomLength geomLengthFunction = new FilterFunction_geomLength();
            geomLengthFunction.setArgs(new Expression[]{geomFieldExpression});
            expr = geomLengthFunction;
        } else if (selectedGeometryAttribute.equals(X_COORDINATE_ATTRIBUTE)) {
            FilterFunction_getX getXFunction = new FilterFunction_getX();
            getXFunction.setArgs(new Expression[]{geomFieldExpression});
            expr = getXFunction;
        } else if (selectedGeometryAttribute.equals(Y_COORDINATE_ATTRIBUTE)) {
            FilterFunction_getY getYFunction = new FilterFunction_getY();
            getYFunction.setArgs(new Expression[]{geomFieldExpression});
            expr = getYFunction;
        } else {
            throw new Exception(I18N.getMessage("org.saig.jump.plugin.query.QueryWizardDialog.the-operator-{0}-can-not-be-applied-to-geometric-type-attributes", new Object[]{selectedGeometryAttribute}));
        }
        return expr;
    }

    private void createWKTDialog() {
        final EnterWKTDialog d = new EnterWKTDialog(this.context.getWorkbenchFrame(), I18N.getString("workbench.ui.plugin.WKTPlugIn.enter-well-known-text"), true);
        d.setSize(500, 400);
        d.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (d.wasOKPressed()) {
                        try {
                            LayerQueryWizardPanel.this.apply(d.getText(), LayerQueryWizardPanel.this.context);
                            LayerQueryWizardPanel.this.txtValue.setText(d.getText());
                        }
                        catch (Exception e1) {
                            LOGGER.error((Object)"", (Throwable)e1);
                            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.the-wkt-expression-is-not-valid")) + ": " + e1.getMessage(), I18N.getString("org.saig.jump.plugin.query.QueryWizardPlugIn.name"));
                        }
                    }
                    d.setVisible(false);
                }
                catch (Throwable t) {
                    LayerQueryWizardPanel.this.context.getErrorHandler().handleThrowable(t);
                }
            }
        });
        GUIUtil.centreOnWindow(d);
        this.wktDialog = d;
    }

    private void apply(String wkt, PlugInContext context) throws Exception {
        StringReader stringReader = new StringReader(wkt);
        try {
            WKTReader wktReader = new WKTReader();
            FeatureCollection c = wktReader.read(stringReader);
            this.validate(c, context);
        }
        finally {
            stringReader.close();
        }
    }

    private void validate(FeatureCollection c, PlugInContext context) throws WorkbenchException {
        FeatureIterator i = null;
        try {
            try {
                i = c.iterator();
                Feature f = i.next();
                IsValidOp op = new IsValidOp(f.getGeometry());
                if (!op.isValid()) {
                    if (EditOptionsPanel.isRollingBackInvalidEdits()) {
                        throw new WorkbenchException(op.getValidationError().getMessage());
                    }
                    context.getWorkbenchFrame().warnUser(op.getValidationError().getMessage());
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (i != null) {
                    i.close();
                }
            }
        }
        finally {
            if (i != null) {
                i.close();
            }
        }
    }

    private void layerSelected() {
        Layer layer = (Layer)this.layersCombo.getSelectedItem();
        if (layer == null || !layer.isEnabled()) {
            return;
        }
        if (layer.equals(this.layer)) {
            return;
        }
        this.layer = layer;
        this.featureSchema = layer.getFeatureSchema();
        this.updateFieldsOfQuery();
    }

    protected void executeQuery() throws Exception {
        if (this.filter == null) {
            return;
        }
        if (this.results.size() > 0) {
            if (this.layer == null) {
                this.context.getLayerViewPanel().getSelectionManager().getFeatureSelection().unselectItems((Layer)this.layersCombo.getSelectedItem(), this.results);
            } else {
                this.context.getLayerViewPanel().getSelectionManager().getFeatureSelection().unselectItems(this.layer, this.results);
            }
        }
        if (this.layer != null) {
            FeatureCollection fc = this.layer.getUltimateFeatureCollectionWrapper();
            this.results = fc.query(this.filter);
        } else {
            this.results = this.layer.getUltimateFeatureCollectionWrapper().query(this.filter);
        }
        if (this.results.size() > 0) {
            this.highLightSelection();
        } else {
            DialogFactory.showWarningDialog(this, I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.no-results-have-been-found-for-the-query"), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.there-are-no-results"));
        }
        this.updateButtons();
    }

    protected void highLightSelection() {
        if (this.results.size() > 0) {
            Layer selectedLayer = null;
            List<Feature> featsToSelect = null;
            selectedLayer = this.layer == null ? (Layer)this.layersCombo.getSelectedItem() : this.layer;
            if (selectedLayer.getCoordTrans() == null) {
                featsToSelect = this.results;
            } else {
                featsToSelect = new ArrayList<Feature>(this.results.size());
                for (Feature currentFeat : this.results) {
                    Feature cloneFeat = (Feature)currentFeat.clone();
                    IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(cloneFeat.getGeometry());
                    pathGeom.reProject(this.layer.getCoordTrans());
                    Geometry geomRepro = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                    cloneFeat.setGeometry(geomRepro);
                    featsToSelect.add(cloneFeat);
                }
            }
            this.context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(selectedLayer, featsToSelect);
        }
    }

    public void layerChangedNotification() {
        if (this.showLayersSelectionPanel) {
            List<Layer> layersWithoutRasterList = this.context.getLayerManager().getNoRasterLayers();
            Layer currentLayer = (Layer)this.layersCombo.getSelectedItem();
            String currentAttr = (String)this.leftAttributePublicNamesComboBox.getSelectedItem();
            String currentOp = (String)this.operatorsCombo.getSelectedItem();
            Object[] layers = new Object[layersWithoutRasterList.size()];
            layersWithoutRasterList.toArray(layers);
            Arrays.sort(layers);
            this.layersCombo.removeAllItems();
            int i = 0;
            while (i < layers.length) {
                this.layersCombo.addItem(layers[i]);
                ++i;
            }
            if (currentLayer != null) {
                this.layersCombo.setSelectedItem(currentLayer);
                if (this.layersCombo.getSelectedItem().equals(currentLayer)) {
                    this.leftAttributePublicNamesComboBox.setSelectedItem(currentAttr);
                    this.operatorsCombo.setSelectedItem(currentOp);
                }
            }
        }
    }

    private void calculateBuffer() {
        Collection<Layer> layers = this.context.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems();
        if (layers.size() > 0) {
            Collection<Feature> col = this.context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
            Object value = DialogFactory.showInputDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.insert-the-distance-in-meters"), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.buffer-configuration"), "0");
            if (value == null) {
                return;
            }
            try {
                Double buffer = new Double((String)value);
                GeometryFactory factory = new GeometryFactory();
                ArrayList<Geometry> geometrias = new ArrayList<Geometry>();
                for (Feature element : col) {
                    geometrias.add(element.getGeometry());
                }
                Geometry[] geoms = new Geometry[geometrias.size()];
                geometrias.toArray(geoms);
                Geometry geom = factory.createGeometryCollection(geoms).buffer(buffer.doubleValue());
                this.txtValue.setText(geom.toText());
                return;
            }
            catch (NumberFormatException e) {
                DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.the-input-value-is-incorrect"), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.error"));
            }
        } else {
            this.wktDialog.setVisible(true);
        }
    }

    public void closeInfoFrame() {
        if (this.infoFrame != null) {
            if (this.infoFrame.isClosed()) {
                this.infoFrame.removeLayerListeners();
            } else if (!this.infoFrame.isVisible()) {
                this.infoFrame.dispose();
            }
        }
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
        this.results = new ArrayList<Feature>();
        this.updateTextArea();
    }

    protected class ExecuteSaveResultsWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        public ExecuteSaveResultsWaitDialog(JFrame frame, final LayerQueryWizardPanel layerQueryWizardPanel2) {
            super((Frame)frame, true);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(String.valueOf(I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.Saving-results")) + " ...");
            JLabel label = new JLabel();
            label.setIcon(IconLoader.icon("loading.gif"));
            label.setHorizontalAlignment(0);
            this.getContentPane().add((Component)label, "Center");
            this.setSize(new Dimension(200, 100));
            GUIUtil.centreOnWindow(this);
            SwingWorker worker = new SwingWorker(){

                @Override
                public Object construct() {
                    try {
                        layerQueryWizardPanel2.saveResults();
                        Boolean bl = Boolean.TRUE;
                        return bl;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        Boolean bl = Boolean.FALSE;
                        return bl;
                    }
                    finally {
                        ExecuteSaveResultsWaitDialog.this.setVisible(false);
                    }
                }

                @Override
                public void finished() {
                    ExecuteSaveResultsWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        public void closeWindow() {
            this.dispose();
        }
    }

    protected class ExecuteWKTWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        public ExecuteWKTWaitDialog(JFrame frame, final LayerQueryWizardPanel layerQueryWizardPanel2) {
            super((Frame)frame, true);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(String.valueOf(I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.Calculating-geometry")) + " ...");
            JLabel label = new JLabel();
            label.setIcon(IconLoader.icon("loading.gif"));
            label.setHorizontalAlignment(0);
            this.getContentPane().add((Component)label, "Center");
            this.setSize(new Dimension(200, 100));
            GUIUtil.centreOnWindow(this);
            SwingWorker worker = new SwingWorker(){

                @Override
                public Object construct() {
                    try {
                        layerQueryWizardPanel2.calculateBuffer();
                        Boolean bl = Boolean.TRUE;
                        return bl;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        Boolean bl = Boolean.FALSE;
                        return bl;
                    }
                    finally {
                        ExecuteWKTWaitDialog.this.setVisible(false);
                    }
                }

                @Override
                public void finished() {
                    ExecuteWKTWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        public void closeWindow() {
            this.dispose();
        }
    }

    protected class ExecuteWaitDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;

        public ExecuteWaitDialog(JFrame frame, final LayerQueryWizardPanel layerQueryWizardPanel2) {
            super((Frame)frame, true);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle(I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.Executing-query"));
            JLabel label = new JLabel();
            label.setIcon(IconLoader.icon("loading.gif"));
            label.setHorizontalAlignment(0);
            this.getContentPane().add((Component)label, "Center");
            this.setSize(new Dimension(200, 100));
            GUIUtil.centreOnWindow(this);
            SwingWorker worker = new SwingWorker(){

                @Override
                public Object construct() {
                    try {
                        layerQueryWizardPanel2.executeQuery();
                        Boolean bl = Boolean.TRUE;
                        return bl;
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        Boolean bl = Boolean.FALSE;
                        return bl;
                    }
                    finally {
                        ExecuteWaitDialog.this.setVisible(false);
                    }
                }

                @Override
                public void finished() {
                    ExecuteWaitDialog.this.closeWindow();
                }
            };
            worker.start();
        }

        public void closeWindow() {
            this.dispose();
        }
    }
}

