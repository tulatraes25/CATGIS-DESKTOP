/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.WKTReader
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.gui.buttons.DropDownButton;
import es.kosmo.desktop.plugins.analysis.CalculateAttributeByExpressionPlugIn;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import org.saig.core.filter.AbstractFilter;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.GeometryFilter;
import org.saig.core.filter.LikeFilter;
import org.saig.core.filter.NullFilter;
import org.saig.core.filter.visitor.FilterToStringTranslator;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.query.EditQueryExpressionDialog;
import org.saig.jump.widgets.query.actions.CurrentViewEnvelopeBufferAction;
import org.saig.jump.widgets.query.actions.GenerateAllValuesAction;
import org.saig.jump.widgets.query.actions.InsertWKTStringAction;
import org.saig.jump.widgets.query.actions.SelectedFeaturesBufferAction;
import org.saig.jump.widgets.util.DialogFactory;

public abstract class AbstractQueryWizardPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AbstractQueryWizardPanel.class);
    protected static final String LAST_LAYER_NAME_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_LAYER_NAME_KEY";
    protected static final String LAST_ATTRIBUTE_NAME_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_ATTRIBUTE_NAME_KEY";
    protected static final String LAST_OPERATOR_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_OPERATOR_KEY";
    protected static final String LAST_VALUE_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_VALUE_KEY";
    protected static final String LAST_GEOMETRY_ATTRIBUTE_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_GEOMETRY_ATTRIBUTE_KEY";
    protected static final String LAST_VALUE_SELECTED_OPTION_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_VALUE_SELECTED_OPTION_KEY";
    protected static final String LAST_RIGHT_ATTRIBUTE_NAME_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_RIGHT_ATTRIBUTE_NAME_KEY";
    protected static final String LAST_RIGHT_GEOMETRY_ATTRIBUTE_KEY = String.valueOf(AbstractQueryWizardPanel.class.getName()) + " - LAST_RIGHT_GEOMETRY_ATTRIBUTE_KEY";
    public static final String GREATER_THAN = String.valueOf(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.greater-than")) + " (>)";
    public static final String IS_NULL = I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.Is-null");
    public static final String LESS_THAN = String.valueOf(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.less-than")) + " (<)";
    public static final String GREATER_OR_EQUAL_TO = String.valueOf(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.greater-or-equal-to")) + " (>=)";
    public static final String LESS_OR_EQUAL_TO = String.valueOf(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.less-or-equal-to")) + " (<=)";
    public static final String DIFFERENT_TO = String.valueOf(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.different-to")) + " (<>)";
    public static final String EQUAL_TO = String.valueOf(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.equal-to")) + " (=)";
    public static final String LIKE = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.like");
    protected static final String NULL_QUERY = I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.query-as-sql");
    protected static final String[] OPERATORS = new String[]{GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL_TO, LESS_OR_EQUAL_TO, DIFFERENT_TO, EQUAL_TO, LIKE, IS_NULL};
    private static final String[] ALFANUMERIC_OPERATORS = new String[]{GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL_TO, LESS_OR_EQUAL_TO, DIFFERENT_TO, EQUAL_TO, LIKE, IS_NULL};
    protected Map<String, String> opgeom;
    protected FeatureSchema featureSchema;
    protected static final FilterFactory factory = FilterFactory.createFilterFactory();
    private static final FilterToStringTranslator filterTranslator = new FilterToStringTranslator();
    private static final WKTReader reader = new WKTReader();
    private static JFileChooser loadingFileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
    private static JFileChooser savingFileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();
    private static final FileFilter SAIG_FILE_FILTER = GUIUtil.createFileFilter(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.filter-files"), new String[]{"ftr"});
    protected static final String NO_ATTRIBUTE = "-----";
    protected static final String[] DEFAULT_GEOMETRY_ATTRIBUTES = new String[]{"-----"};
    protected Filter filter;
    protected JComboBox leftAttributePublicNamesComboBox;
    protected JComboBox rightAttributePublicNamesComboBox;
    protected JComboBox operatorsCombo;
    protected JComboBox leftGeometryAttributesComboBox;
    protected JComboBox rightGeometryAttributesComboBox;
    protected JRadioButton literalRadioButton;
    protected JRadioButton fieldRadioButton;
    protected JTextArea txtValue;
    protected DropDownButton allRegistersButton;
    protected JTextArea queryTextArea;
    protected JButton executeButton;
    protected JButton editButton;
    protected JButton cleanButton;
    protected JButton loadFilterButton;
    protected JButton saveFilterButton;
    protected JButton saveRecordsButton;
    protected JButton viewRecordsButton;
    protected JButton calculatorButton;
    protected OKCancelPanel okCancelPanel;
    protected boolean exitOk = false;
    protected PlugInContext context;
    protected int rowIndex = 0;

    public AbstractQueryWizardPanel(FeatureSchema featureSchema, PlugInContext context) {
        super(new GridBagLayout());
        this.context = context;
        this.featureSchema = featureSchema;
    }

    public void initialize() {
        this.initializeOperators();
        this.initializeChoosers();
        this.createQueryFieldsPanel();
        this.createLogicalOperatorsPanel();
        this.createExecutionPanel();
        this.createOKcancelPanel();
        this.updateButtons();
    }

    protected void initializeChoosers() {
        loadingFileChooser.setDialogTitle(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.filter"));
        GUIUtil.removeChoosableFileFilters(loadingFileChooser);
        loadingFileChooser.addChoosableFileFilter(SAIG_FILE_FILTER);
        loadingFileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        loadingFileChooser.setFileFilter(SAIG_FILE_FILTER);
        savingFileChooser.setDialogTitle(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.filter"));
        GUIUtil.removeChoosableFileFilters(savingFileChooser);
        savingFileChooser.addChoosableFileFilter(SAIG_FILE_FILTER);
        savingFileChooser.addChoosableFileFilter(GUIUtil.ALL_FILES_FILTER);
        savingFileChooser.setFileFilter(SAIG_FILE_FILTER);
    }

    protected void initializeOperators() {
        this.opgeom = new HashMap<String, String>();
        this.opgeom.put(GREATER_THAN, ">");
        this.opgeom.put(LESS_THAN, "<");
        this.opgeom.put(GREATER_OR_EQUAL_TO, ">=");
        this.opgeom.put(LESS_OR_EQUAL_TO, "<=");
        this.opgeom.put(DIFFERENT_TO, "!=");
        this.opgeom.put(EQUAL_TO, "=");
        this.opgeom.put(LIKE, "LIKE");
        this.opgeom.put(IS_NULL, "ISNULL");
    }

    protected void createQueryFieldsPanel() {
        JPanel conditionPanel = new JPanel(new GridBagLayout());
        conditionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.expression")));
        this.leftAttributePublicNamesComboBox = new JComboBox();
        this.leftGeometryAttributesComboBox = new JComboBox<String>(DEFAULT_GEOMETRY_ATTRIBUTES);
        Dimension dim = new Dimension(120, 20);
        this.leftGeometryAttributesComboBox.setMinimumSize(dim);
        this.leftGeometryAttributesComboBox.setPreferredSize(dim);
        this.leftGeometryAttributesComboBox.setMaximumSize(dim);
        this.rightAttributePublicNamesComboBox = new JComboBox();
        this.rightAttributePublicNamesComboBox.setEnabled(false);
        this.rightAttributePublicNamesComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractQueryWizardPanel.this.updateRightFieldValueOptions();
            }
        });
        this.rightGeometryAttributesComboBox = new JComboBox<String>(DEFAULT_GEOMETRY_ATTRIBUTES);
        this.rightGeometryAttributesComboBox.setMinimumSize(dim);
        this.rightGeometryAttributesComboBox.setPreferredSize(dim);
        this.rightGeometryAttributesComboBox.setMaximumSize(dim);
        this.rightGeometryAttributesComboBox.setEnabled(false);
        if (this.featureSchema != null) {
            this.updateFieldsOfQuery();
        }
        this.leftAttributePublicNamesComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractQueryWizardPanel.this.loadOperations();
            }
        });
        this.leftGeometryAttributesComboBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractQueryWizardPanel.this.loadOperations();
            }
        });
        this.operatorsCombo = new JComboBox<String>(OPERATORS);
        this.allRegistersButton = new DropDownButton("...");
        this.allRegistersButton.setRunFirstMenuOption(true);
        this.loadOperations();
        this.literalRadioButton = new JRadioButton();
        this.literalRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractQueryWizardPanel.this.updateValueOptions();
            }
        });
        this.fieldRadioButton = new JRadioButton();
        this.fieldRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractQueryWizardPanel.this.updateValueOptions();
            }
        });
        ButtonGroup valueButtonGroup = new ButtonGroup();
        valueButtonGroup.add(this.literalRadioButton);
        valueButtonGroup.add(this.fieldRadioButton);
        this.literalRadioButton.setSelected(true);
        this.txtValue = new JTextArea();
        this.txtValue.setFont(new JLabel().getFont());
        this.txtValue.setLineWrap(true);
        this.txtValue.setWrapStyleWord(true);
        this.txtValue.setColumns(40);
        this.txtValue.setRows(3);
        JScrollPane pane = new JScrollPane(this.txtValue, 22, 31);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 0, 0, (JComponent)new JLabel(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.field")), false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 0, 20, (JComponent)this.leftAttributePublicNamesComboBox, false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 0, 40, (JComponent)this.leftGeometryAttributesComboBox, true, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 1, 0, (JComponent)new JLabel(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.operator")), false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 1, 20, (JComponent)this.operatorsCombo, false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 2, 0, (JComponent)new JLabel(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.value")), true, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 3, 0, (JComponent)this.literalRadioButton, false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 3, 20, (JComponent)pane, false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 3, 40, (JComponent)this.allRegistersButton, true, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 4, 0, (JComponent)this.fieldRadioButton, false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 4, 20, (JComponent)this.rightAttributePublicNamesComboBox, false, true);
        FormUtils.addRowInGBL((JComponent)conditionPanel, 4, 40, (JComponent)this.rightGeometryAttributesComboBox, true, true);
        FormUtils.addRowInGBL(this, this.rowIndex++, 0, conditionPanel);
    }

    protected void createLogicalOperatorsPanel() {
        JPanel logicalOperatorPanel = new JPanel(new GridBagLayout());
        logicalOperatorPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.condition")));
        JButton addButton = new JButton(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.add"));
        addButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (AbstractQueryWizardPanel.this.filter != null) {
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.it-can-only-be-applied-logic-conditions"), I18N.getString("org.saig.jump.plugin.query.QueryWizardPlugIn.name"));
                    return;
                }
                Filter operator = null;
                try {
                    operator = AbstractQueryWizardPanel.this.buildFilter();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    AbstractQueryWizardPanel.this.showExpressionErrorDialog(e.getMessage(), "");
                    return;
                }
                if (operator != null) {
                    AbstractQueryWizardPanel.this.filter = operator;
                }
                AbstractQueryWizardPanel.this.updateTextArea();
            }
        });
        FormUtils.addRowInGBL((JComponent)logicalOperatorPanel, 0, 0, (JComponent)addButton, false, true);
        JButton andButton = new JButton(I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.AND"));
        andButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (AbstractQueryWizardPanel.this.filter == null) {
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.a-binary-operator-can-not-be-applied"), I18N.getString("org.saig.jump.plugin.query.QueryWizardPlugIn.name"));
                    return;
                }
                Filter operator = null;
                try {
                    operator = AbstractQueryWizardPanel.this.buildFilter();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    AbstractQueryWizardPanel.this.showExpressionErrorDialog(e.getMessage(), "");
                    return;
                }
                if (operator != null) {
                    AbstractQueryWizardPanel.this.filter = AbstractQueryWizardPanel.this.filter != null ? AbstractQueryWizardPanel.this.filter.and(operator) : operator;
                    AbstractQueryWizardPanel.this.updateTextArea();
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)logicalOperatorPanel, 0, 5, (JComponent)andButton, false, true);
        JButton orButton = new JButton(I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.OR"));
        orButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (AbstractQueryWizardPanel.this.filter == null) {
                    DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.a-binary-operator-can-not-be-applied"), I18N.getString("org.saig.jump.plugin.query.QueryWizardPlugIn.name"));
                    return;
                }
                Filter operator = null;
                try {
                    operator = AbstractQueryWizardPanel.this.buildFilter();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    AbstractQueryWizardPanel.this.showExpressionErrorDialog(e.getMessage(), "");
                    return;
                }
                if (operator != null) {
                    AbstractQueryWizardPanel.this.filter = AbstractQueryWizardPanel.this.filter != null ? AbstractQueryWizardPanel.this.filter.or(operator) : operator;
                    AbstractQueryWizardPanel.this.updateTextArea();
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)logicalOperatorPanel, 0, 10, (JComponent)orButton, false, true);
        JButton notButton = new JButton(I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.NOT"));
        notButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (AbstractQueryWizardPanel.this.filter == null) {
                    Filter operator = null;
                    try {
                        operator = AbstractQueryWizardPanel.this.buildFilter();
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        AbstractQueryWizardPanel.this.showExpressionErrorDialog(e.getMessage(), "");
                        return;
                    }
                    if (operator != null) {
                        AbstractQueryWizardPanel.this.filter = operator;
                    }
                }
                AbstractQueryWizardPanel.this.filter = AbstractQueryWizardPanel.this.filter.not();
                AbstractQueryWizardPanel.this.updateTextArea();
            }
        });
        FormUtils.addRowInGBL((JComponent)logicalOperatorPanel, 0, 15, (JComponent)notButton, false, true);
        FormUtils.addRowInGBL(this, this.rowIndex++, 0, logicalOperatorPanel);
    }

    private void updateRightFieldValueOptions() {
        String publicName = (String)this.rightAttributePublicNamesComboBox.getSelectedItem();
        if (publicName == null) {
            return;
        }
        Attribute attrSelected = this.featureSchema.getPublicAttribute(publicName);
        this.rightGeometryAttributesComboBox.setEnabled(attrSelected.getType() == AttributeType.GEOMETRY);
    }

    protected void loadOperations() {
        String publicName = (String)this.leftAttributePublicNamesComboBox.getSelectedItem();
        if (publicName == null) {
            return;
        }
        Attribute attrSelected = this.featureSchema.getPublicAttribute(publicName);
        if (attrSelected.getType() == AttributeType.GEOMETRY) {
            this.leftGeometryAttributesComboBox.setEnabled(true);
            if (this.leftGeometryAttributesComboBox.getSelectedItem() == null || this.leftGeometryAttributesComboBox.getSelectedItem().equals(NO_ATTRIBUTE)) {
                this.leftGeometryAttributesComboBox.setSelectedItem(NO_ATTRIBUTE);
                this.fillGeometricOperations();
                this.buildAllRegistersMenu(true);
            } else {
                this.fillAlfanumericOperations();
                this.buildAllRegistersMenu(false);
            }
        } else {
            this.fillAlfanumericOperations();
            this.leftGeometryAttributesComboBox.setEnabled(false);
            this.buildAllRegistersMenu(false);
        }
    }

    private void fillAlfanumericOperations() {
        this.operatorsCombo.removeAllItems();
        int i = 0;
        while (i < ALFANUMERIC_OPERATORS.length) {
            this.operatorsCombo.addItem(ALFANUMERIC_OPERATORS[i]);
            ++i;
        }
    }

    protected void updateValueOptions() {
        boolean literalSelected = this.literalRadioButton.isSelected();
        this.txtValue.setEnabled(literalSelected);
        this.allRegistersButton.setEnabled(literalSelected);
        this.rightAttributePublicNamesComboBox.setEnabled(!literalSelected);
    }

    private Filter buildFilter() throws Exception {
        Filter filter = null;
        String leftFieldPublicName = (String)this.leftAttributePublicNamesComboBox.getSelectedItem();
        String leftFieldName = this.featureSchema.getPublicAttribute(leftFieldPublicName).getName();
        String operador = this.opgeom.get(this.operatorsCombo.getSelectedItem());
        boolean useLiteral = this.literalRadioButton.isSelected();
        String userSearchLiteral = this.txtValue.getText();
        AttributeType tipo = this.featureSchema.getAttributeType(leftFieldName);
        Object searchLiteral = null;
        searchLiteral = operador.equalsIgnoreCase("like") ? userSearchLiteral : FeatureUtil.getGoodAttribute(tipo, userSearchLiteral);
        AttributeExpression attribute = factory.createAttributeExpression(this.featureSchema, leftFieldName);
        Expression rightExpression = null;
        if (this.isBinary(operador)) {
            String rightFieldPublicName;
            String rightFieldName;
            AttributeType rightAttrType;
            rightExpression = useLiteral ? factory.createLiteralExpression(searchLiteral) : ((rightAttrType = this.featureSchema.getAttributeType(rightFieldName = this.featureSchema.getPublicAttribute(rightFieldPublicName = (String)this.rightAttributePublicNamesComboBox.getSelectedItem()).getName())).toJavaClass().equals(Geometry.class) ? this.buildExpressionFromSelectedAttribute((String)this.rightGeometryAttributesComboBox.getSelectedItem(), rightFieldName) : factory.createAttributeExpression(this.featureSchema, rightFieldPublicName));
        }
        if (operador.equalsIgnoreCase("like")) {
            if (tipo.toJavaClass().equals(Geometry.class)) {
                throw new Exception(I18N.getMessage("org.saig.jump.plugin.query.QueryWizardDialog.the-operator-{0}-can-not-be-applied-to-geometric-type-attributes", new Object[]{operador}));
            }
            LikeFilter likeFilter = factory.createLikeFilter();
            likeFilter.setPattern(rightExpression, "*", "?", "\\");
            likeFilter.setValue(attribute);
            filter = likeFilter;
        } else if (operador.equalsIgnoreCase("isnull")) {
            NullFilter nullFilter = factory.createNullFilter();
            nullFilter.setNullCheckValue(attribute);
            filter = nullFilter;
        } else if (operador.equalsIgnoreCase("equals") || operador.equalsIgnoreCase("disjoint") || operador.equalsIgnoreCase("INTERSECTS") || operador.equalsIgnoreCase("CROSSES") || operador.equalsIgnoreCase("WITHIN") || operador.equalsIgnoreCase("CONTAINS") || operador.equalsIgnoreCase("OVERLAPS") || operador.equalsIgnoreCase("BEYOND") || operador.equalsIgnoreCase("BBOX")) {
            if (!tipo.toJavaClass().equals(Geometry.class)) {
                throw new Exception(I18N.getMessage("org.saig.jump.plugin.query.QueryWizardDialog.the-operator-{0}-can-only-be-applied-to-geometric-type-attributes", new Object[]{operador}));
            }
            short geomFilterType = this.lookupGeometryFilter(operador);
            GeometryFilter geomFilter = factory.createGeometryFilter(geomFilterType);
            if (useLiteral) {
                Geometry geom = reader.read((String)searchLiteral);
                rightExpression = factory.createLiteralExpression(geom);
            }
            geomFilter.addLeftGeometry(attribute);
            geomFilter.addRightGeometry(rightExpression);
            filter = geomFilter;
        } else if (tipo.toJavaClass().equals(Geometry.class)) {
            Expression geomFunctionExpr = this.buildExpressionFromSelectedAttribute((String)this.leftGeometryAttributesComboBox.getSelectedItem(), leftFieldName);
            short type = this.lookupCompareFilter(operador);
            CompareFilter compareFilter = factory.createCompareFilter(type);
            compareFilter.addLeftValue(geomFunctionExpr);
            compareFilter.addRightValue(rightExpression);
            filter = compareFilter;
        } else {
            short type = this.lookupCompareFilter(operador);
            CompareFilter compareFilter = factory.createCompareFilter(type);
            compareFilter.addLeftValue(attribute);
            compareFilter.addRightValue(rightExpression);
            filter = compareFilter;
        }
        return filter;
    }

    protected void updateTextArea() {
        String textFilter = null;
        if (this.filter == null) {
            textFilter = NULL_QUERY;
        } else if (this.filter != null) {
            textFilter = filterTranslator.translateFilter(this.filter);
        }
        this.queryTextArea.setText(textFilter);
        this.updateButtons();
        this.repaint();
    }

    protected void showExpressionErrorDialog(String errorMessage, Object expression) {
        String expressionString = "";
        if (expression != null) {
            expressionString = expression.toString();
        }
        if (expressionString.length() > 125) {
            expressionString = String.valueOf(expressionString.substring(0, 124)) + " ...";
        }
        String userMessage = I18N.getMessage("org.saig.jump.plugin.query.QueryWizardDialog.the-expression-{0}-is-not-valid", new Object[]{expressionString});
        if (errorMessage != null && !errorMessage.isEmpty()) {
            userMessage = String.valueOf(userMessage) + ": " + errorMessage;
        }
        DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), userMessage, I18N.getString("org.saig.jump.plugin.query.QueryWizardPlugIn.name"));
    }

    private short lookupGeometryFilter(String name) {
        Field[] f = AbstractFilter.class.getFields();
        name = name.toUpperCase();
        int i = 0;
        int ii = f.length;
        while (i < ii) {
            if (f[i].getName().endsWith(name)) {
                try {
                    return f[i].getShort(null);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            ++i;
        }
        return -1;
    }

    private short lookupCompareFilter(String operador) {
        int solucion = -1;
        if (operador.equals("<=")) {
            solucion = 17;
        } else if (operador.equals("<")) {
            solucion = 15;
        } else if (operador.equals(">=")) {
            solucion = 18;
        } else if (operador.equals(">")) {
            solucion = 16;
        } else if (operador.equals("=")) {
            solucion = 14;
        } else if (operador.equals("!=")) {
            solucion = 23;
        }
        return (short)solucion;
    }

    protected void createExecutionPanel() {
        JPanel panelsButton = new JPanel(new GridBagLayout());
        panelsButton.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.query")));
        this.queryTextArea = new JTextArea(2, 10);
        this.queryTextArea.setFont(new JLabel().getFont());
        this.queryTextArea.setEditable(false);
        this.queryTextArea.setLineWrap(true);
        this.queryTextArea.setWrapStyleWord(true);
        this.queryTextArea.setColumns(40);
        this.queryTextArea.setRows(3);
        JScrollPane pane = new JScrollPane(this.queryTextArea, 22, 31);
        FormUtils.addRowInGBL(panelsButton, 0, 0, pane);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        this.executeButton = new JButton();
        this.executeButton.setIcon(IconLoader.icon("Hammer.gif"));
        this.executeButton.setToolTipText(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.execute-query"));
        this.executeButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractQueryWizardPanel.this.executeButtonAction();
            }
        });
        buttonPanel.add(this.executeButton);
        this.editButton = new JButton();
        this.editButton.setIcon(IconLoader.icon("EditingToolbox.gif"));
        this.editButton.setToolTipText(I18N.getString("org.saig.jump.widgets.query.QueryWizardDialog.Edit-alphanumeric-expression"));
        this.editButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                EditQueryExpressionDialog editDialog = new EditQueryExpressionDialog(JUMPWorkbench.getFrameInstance(), true, AbstractQueryWizardPanel.this.getFilter(), AbstractQueryWizardPanel.this.featureSchema);
                if (editDialog.wasOkPressed()) {
                    AbstractQueryWizardPanel.this.setFilter(editDialog.getFilter());
                }
            }
        });
        buttonPanel.add(this.editButton);
        this.cleanButton = new JButton();
        this.cleanButton.setIcon(IconLoader.icon("Delete.gif"));
        this.cleanButton.setToolTipText(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.clean-query"));
        this.cleanButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractQueryWizardPanel.this.cleanQuery();
            }
        });
        buttonPanel.add(this.cleanButton);
        this.viewRecordsButton = new JButton("");
        this.viewRecordsButton.setIcon(IconLoader.icon("Column.gif"));
        this.viewRecordsButton.setToolTipText(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.view-records"));
        this.viewRecordsButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractQueryWizardPanel.this.viewResults();
            }
        });
        buttonPanel.add(this.viewRecordsButton);
        this.saveRecordsButton = new JButton();
        this.saveRecordsButton.setIcon(IconLoader.icon("SaveTheme2.gif"));
        this.saveRecordsButton.setToolTipText(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.save-results-as-new-layer"));
        this.saveRecordsButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractQueryWizardPanel.this.executeSaveResultAction();
            }
        });
        buttonPanel.add(this.saveRecordsButton);
        this.loadFilterButton = new JButton();
        this.loadFilterButton.setIcon(IconLoader.icon("Open.gif"));
        this.loadFilterButton.setToolTipText(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.load-filter"));
        this.loadFilterButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractQueryWizardPanel.this.loadFilter();
            }
        });
        buttonPanel.add(this.loadFilterButton);
        this.saveFilterButton = new JButton();
        this.saveFilterButton.setIcon(IconLoader.icon("saveFilter.png"));
        this.saveFilterButton.setToolTipText(I18N.getString("org.saig.jump.plugin.query.QueryWizardDialog.save-filter"));
        this.saveFilterButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractQueryWizardPanel.this.saveFilter();
            }
        });
        buttonPanel.add(this.saveFilterButton);
        this.calculatorButton = new JButton(GUIUtil.resize((ImageIcon)CalculateAttributeByExpressionPlugIn.ICON, 20));
        this.calculatorButton.setName(CalculateAttributeByExpressionPlugIn.NAME);
        this.calculatorButton.setToolTipText(CalculateAttributeByExpressionPlugIn.NAME);
        this.calculatorButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                AbstractQueryWizardPanel.this.openCalculator();
            }
        });
        buttonPanel.add(this.calculatorButton);
        FormUtils.addRowInGBL(panelsButton, 1, 0, buttonPanel);
        FormUtils.addRowInGBL(this, this.rowIndex++, 0, panelsButton);
    }

    protected void createOKcancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        this.okCancelPanel.setLayout(gbPaneOKCancel);
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                AbstractQueryWizardPanel.this.okCancelPanelAction();
            }
        });
        FormUtils.addRowInGBL(this, this.rowIndex++, 0, this.okCancelPanel);
    }

    private boolean isBinary(String operador) {
        return !operador.equalsIgnoreCase("isnull");
    }

    private void loadFilter() {
        if (loadingFileChooser.showOpenDialog(JUMPWorkbench.getFrameInstance()) != 0) {
            return;
        }
        InputStreamReader reader = null;
        File file = null;
        try {
            file = loadingFileChooser.getSelectedFile();
            reader = new FileReader(file);
            int c = -1;
            String expression = "";
            while ((c = reader.read()) != -1) {
                expression = String.valueOf(expression) + (char)c;
            }
            Filter operator = null;
            try {
                operator = (Filter)ExpressionBuilder.parse(this.featureSchema, expression);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.showExpressionErrorDialog(e.getMessage(), expression);
                try {
                    if (reader != null) {
                        reader.close();
                    }
                }
                catch (Exception e2) {
                    LOGGER.error((Object)"", (Throwable)e2);
                }
                file = null;
                return;
            }
            try {
                this.cleanQuery();
                if (operator != null) {
                    this.filter = operator;
                }
                this.updateTextArea();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            file = null;
        }
    }

    private void saveFilter() {
        if (savingFileChooser.showSaveDialog(JUMPWorkbench.getFrameInstance()) != 0) {
            return;
        }
        try {
            File file = savingFileChooser.getSelectedFile();
            file = FileUtil.addValidExtension(file, "ftr");
            StringWriter stringWriter = new StringWriter();
            stringWriter.write(filterTranslator.translateFilter(this.filter));
            FileUtil.setContents(file.getAbsolutePath(), stringWriter.toString());
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    protected void hideDialog() {
        JDialog dialog = (JDialog)SwingUtilities.getWindowAncestor(this);
        dialog.dispose();
    }

    public Filter getFilter() {
        return this.filter;
    }

    public boolean exitOk() {
        return this.exitOk;
    }

    public String getRawText() {
        return this.queryTextArea.getText();
    }

    public abstract void setFilter(Filter var1);

    protected abstract void allRegisterAction();

    protected void insertWKTString() {
    }

    protected void selectedFeaturesByBuffer() {
    }

    protected void currentViewEnvelopeByBuffer() {
    }

    protected abstract void saveResults();

    protected abstract List<Feature> cloneResults(Collection<Feature> var1, FeatureSchema var2);

    protected abstract void cleanQuery();

    protected abstract void viewResults();

    protected abstract void updateButtons();

    protected abstract void openCalculator();

    protected abstract void executeButtonAction();

    protected abstract void executeSaveResultAction();

    protected abstract void okCancelPanelAction();

    protected abstract void fillGeometricOperations();

    protected abstract void updateFieldsOfQuery();

    protected abstract Expression buildExpressionFromSelectedAttribute(String var1, String var2) throws Exception;

    protected void buildAllRegistersMenu(boolean isGeometricAttribute) {
        JMenu currentMenu = this.allRegistersButton.getMenu();
        currentMenu.removeAll();
        if (isGeometricAttribute) {
            this.allRegistersButton.getButton().setText(I18N.getString("org.saig.jump.widgets.query.AbstractQueryWizardPanel.WKT-string"));
            InsertWKTStringAction insertWKTStringAction = new InsertWKTStringAction(){
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractQueryWizardPanel.this.insertWKTString();
                }
            };
            currentMenu.add(insertWKTStringAction);
            SelectedFeaturesBufferAction selectedFeaturesBufferAction = new SelectedFeaturesBufferAction(){
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractQueryWizardPanel.this.selectedFeaturesByBuffer();
                }
            };
            currentMenu.add(selectedFeaturesBufferAction);
            CurrentViewEnvelopeBufferAction viewEnvelopeBufferAction = new CurrentViewEnvelopeBufferAction(){
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractQueryWizardPanel.this.currentViewEnvelopeByBuffer();
                }
            };
            currentMenu.add(viewEnvelopeBufferAction);
        } else {
            this.allRegistersButton.getButton().setText("...");
            GenerateAllValuesAction allValuesAction = new GenerateAllValuesAction(){
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractQueryWizardPanel.this.allRegisterAction();
                }
            };
            currentMenu.add(allValuesAction);
        }
    }
}

