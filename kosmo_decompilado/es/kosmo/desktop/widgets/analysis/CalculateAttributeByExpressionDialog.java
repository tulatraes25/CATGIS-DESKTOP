/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.jeks.JeksExpressionParser
 *  com.eteks.jeks.JeksExpressionSyntax
 *  com.eteks.jeks.JeksFunctionParser
 *  com.eteks.jeks.JeksFunctionSyntax
 *  com.eteks.jeks.JeksInterpreter
 *  com.eteks.jeks.JeksParameter
 *  com.eteks.jeks.JeksTableModel
 *  com.eteks.parser.CompilationException
 *  com.eteks.parser.CompiledExpression
 *  com.eteks.parser.ExpressionParameter
 *  com.eteks.parser.Function
 *  com.eteks.parser.Interpreter
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.jdesktop.swingx.JXLabel
 *  org.jdesktop.swingx.JXList
 *  org.jdesktop.swingx.JXPanel
 *  org.jdesktop.swingx.JXTextArea
 *  org.jdesktop.swingx.JXTextField
 *  org.jdesktop.swingx.decorator.HighlighterFactory
 *  org.jdesktop.swingx.renderer.DefaultListRenderer
 *  org.jdesktop.swingx.renderer.StringValue
 *  org.jdesktop.swingx.renderer.StringValues
 */
package es.kosmo.desktop.widgets.analysis;

import com.eteks.jeks.JeksExpressionParser;
import com.eteks.jeks.JeksExpressionSyntax;
import com.eteks.jeks.JeksFunctionParser;
import com.eteks.jeks.JeksFunctionSyntax;
import com.eteks.jeks.JeksInterpreter;
import com.eteks.jeks.JeksParameter;
import com.eteks.jeks.JeksTableModel;
import com.eteks.parser.CompilationException;
import com.eteks.parser.CompiledExpression;
import com.eteks.parser.ExpressionParameter;
import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import es.kosmo.desktop.gui.dialogs.AbstractOptionsDialog;
import es.kosmo.desktop.plugins.analysis.CalculateAttributeByExpressionPlugIn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.table.TableModel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.jeks.userFunctions.ConcatFunction;
import org.saig.jump.util.jeks.userFunctions.IndexOfFunction;
import org.saig.jump.util.jeks.userFunctions.InvFunction;
import org.saig.jump.util.jeks.userFunctions.LengthFunction;
import org.saig.jump.util.jeks.userFunctions.OppFunction;
import org.saig.jump.util.jeks.userFunctions.RoundFunction;
import org.saig.jump.util.jeks.userFunctions.SqrFunction;
import org.saig.jump.util.jeks.userFunctions.SubStringFunction;
import org.saig.jump.util.jeks.userFunctions.TruncFunction;
import org.saig.jump.widgets.util.DialogFactory;

public class CalculateAttributeByExpressionDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CalculateAttributeByExpressionDialog.class);
    private JXPanel centerPanel;
    private JXPanel attributePanel;
    private JXPanel operationsPanel;
    private JXPanel expressionPanel;
    protected JXTextField attrNameTextField;
    protected JXList attrList;
    protected List<Attribute> layerAttrs;
    protected JButton bMas;
    protected JButton bMenos;
    protected JButton bPor;
    protected JButton bDiv;
    protected JButton bDot;
    protected JButton bPar;
    protected JButton b0;
    protected JButton b1;
    protected JButton b2;
    protected JButton b3;
    protected JButton b4;
    protected JButton b5;
    protected JButton b6;
    protected JButton b7;
    protected JButton b8;
    protected JButton b9;
    protected JButton bInt;
    protected JButton bAbs;
    protected JButton bInv;
    protected JButton bPow;
    protected JButton bExp;
    protected JButton bLN;
    protected JButton bLog;
    protected JButton bMod;
    protected JButton bPI;
    protected JButton bSqr;
    protected JButton bRoot;
    protected JButton bCos;
    protected JButton bSin;
    protected JButton bTan;
    protected JButton bACos;
    protected JButton bASin;
    protected JButton bATan;
    protected JButton bCosH;
    protected JButton bSinH;
    protected JButton bTanH;
    protected JButton bOpp;
    protected JButton bParIzq;
    protected JButton bParDer;
    protected JButton bCor;
    protected JButton bCorIzq;
    protected JButton bCorDer;
    protected JButton bBorrarUno;
    protected JButton bBorrarTodo;
    protected JButton bUndo;
    protected JButton bE;
    protected JButton bArea;
    protected JButton bCoordX;
    protected JButton bCoordY;
    protected JButton bPerim;
    protected JButton bFact;
    protected JButton bRand;
    protected JButton bConcat;
    protected JButton bSubStr;
    protected JButton bStrLength;
    protected JButton bStrIndexOf;
    protected JButton bTrunc;
    protected JButton bRound;
    protected JXTextArea expressionTextArea;
    protected JXPanel targetPanel;
    protected JXLabel applyToLabel;
    protected JRadioButton applyToLayerRadioButton;
    protected JRadioButton applyToSelectedOnlyRadioButton;
    protected boolean okPressed = false;
    protected boolean nuevo = false;
    protected boolean esBD;
    protected boolean isAtributeField = true;
    protected OKCancelPanel okCancelPanel;
    protected String pkNombre;
    protected String geomNombre;
    protected String lastExpression;
    protected String bordeExpr = String.valueOf(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Expression")) + ":";
    protected int geometryType;
    protected int numFeatures;
    protected int numSelected;

    public CalculateAttributeByExpressionDialog(JFrame owner, boolean modal, List<Attribute> attrs, String layerName, int size, int selected, boolean isBD, String pkName, String geomAttrName, int gType) {
        super((Frame)owner, modal);
        this.layerAttrs = attrs;
        this.numFeatures = size;
        this.numSelected = selected;
        this.esBD = isBD;
        this.geomNombre = geomAttrName;
        this.pkNombre = pkName;
        this.geometryType = gType;
        this.lastExpression = "";
        this.setTitle(String.valueOf(I18N.getMessage("org.saig.jump.widgets.utils.AbstractBasicOptionsDialog.{0}-options", new Object[]{CalculateAttributeByExpressionPlugIn.NAME})) + " - " + layerName);
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 1, 0, (JComponent)this.getCenterPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, (JComponent)this.getExpressionPanel());
        FormUtils.addRowInGBL(mainPanel, 3, 0, (JComponent)this.getTargetPanel());
        FormUtils.addRowInGBL(mainPanel, 4, 0, this.createOKCancelPanel());
        return mainPanel;
    }

    private JXPanel getTargetPanel() {
        if (this.targetPanel == null) {
            this.targetPanel = new JXPanel((LayoutManager)new GridBagLayout());
            this.targetPanel.setBorder((Border)BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.analysis.CalculateAttributeByExpressionDialog.Target")));
            JXLabel attrNameLabel = new JXLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Save-to-attribute")) + ":");
            this.attrNameTextField = new JXTextField(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-name"));
            this.attrNameTextField.setToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-name"));
            this.attrNameTextField.addKeyListener(new KeyListener(){

                @Override
                public void keyPressed(KeyEvent arg0) {
                    if (arg0.getKeyCode() == 10) {
                        CalculateAttributeByExpressionDialog.this.expressionTextArea.requestFocus();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }
            });
            this.attrNameTextField.addFocusListener(new FocusListener(){

                @Override
                public void focusLost(FocusEvent arg0) {
                    CalculateAttributeByExpressionDialog.this.updateExpressionBorder();
                }

                @Override
                public void focusGained(FocusEvent e) {
                    JTextField source = (JTextField)e.getSource();
                    CalculateAttributeByExpressionDialog.this.isAtributeField = true;
                    source.selectAll();
                }
            });
            this.applyToLabel = new JXLabel(String.valueOf(AbstractOptionsDialog.APPLY_TO_LABEL) + ":");
            this.applyToLayerRadioButton = new JRadioButton(AbstractOptionsDialog.WHOLE_LAYER_LABEL);
            this.applyToSelectedOnlyRadioButton = new JRadioButton(String.valueOf(AbstractOptionsDialog.BASE_SELECTION_LABEL) + " (" + I18N.getMessage("es.kosmo.desktop.controllers.analysis.AssignValueToFieldOptionsDialogController.{0}-selected", new Object[]{this.numSelected}) + ")");
            ButtonGroup applyToButtonGroup = new ButtonGroup();
            applyToButtonGroup.add(this.applyToLayerRadioButton);
            applyToButtonGroup.add(this.applyToSelectedOnlyRadioButton);
            this.applyToLayerRadioButton.setSelected(true);
            this.applyToSelectedOnlyRadioButton.setEnabled(this.numSelected > 0);
            FormUtils.addRowInGBL((JComponent)this.targetPanel, 0, 0, (JLabel)attrNameLabel, (JComponent)this.attrNameTextField);
            FormUtils.addRowInGBL((JComponent)this.targetPanel, 1, 0, (JLabel)this.applyToLabel, (JComponent)this.applyToLayerRadioButton);
            FormUtils.addRowInGBL((JComponent)this.targetPanel, 2, 0, new JLabel(""), (JComponent)this.applyToSelectedOnlyRadioButton);
        }
        return this.targetPanel;
    }

    protected void updateExpressionBorder() {
        this.bordeExpr = this.attrNameTextField.getText().equals("") ? I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Expression") : String.valueOf(this.attrNameTextField.getText()) + " =";
        this.expressionPanel.setBorder((Border)BorderFactory.createTitledBorder(this.bordeExpr));
    }

    private JXPanel getCenterPanel() {
        this.centerPanel = new JXPanel((LayoutManager)new BorderLayout());
        this.centerPanel.add((Component)this.getAttributesPanel(), (Object)"West");
        this.centerPanel.add((Component)this.getOperationsPanel(), (Object)"Center");
        return this.centerPanel;
    }

    private JXPanel getAttributesPanel() {
        this.attributePanel = new JXPanel((LayoutManager)new BorderLayout());
        this.attributePanel.setBorder((Border)BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attributes")));
        this.attrList = new JXList(this.layerAttrs.toArray());
        StringValue sv = new StringValue(){
            private static final long serialVersionUID = 1L;

            public String getString(Object value) {
                if (value instanceof Attribute) {
                    Attribute attr = (Attribute)value;
                    return attr.getPublicName();
                }
                return StringValues.TO_STRING.getString(value);
            }
        };
        this.attrList.setCellRenderer((ListCellRenderer)new DefaultListRenderer(sv));
        this.attrList.setSelectionMode(1);
        this.attrList.setLayoutOrientation(0);
        this.attrList.setVisibleRowCount(-1);
        this.attrList.addHighlighter(HighlighterFactory.createAlternateStriping());
        JScrollPane pane = new JScrollPane((Component)this.attrList);
        pane.setVerticalScrollBarPolicy(22);
        pane.setHorizontalScrollBarPolicy(31);
        pane.setMinimumSize(new Dimension(100, 180));
        pane.setPreferredSize(new Dimension(100, 180));
        this.attributePanel.add((Component)pane);
        this.attrList.addMouseListener((MouseListener)new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (CalculateAttributeByExpressionDialog.this.isAtributeField) {
                        CalculateAttributeByExpressionDialog.this.attrNameTextField.setText(((Attribute)CalculateAttributeByExpressionDialog.this.attrList.getSelectedValue()).getPublicName());
                        CalculateAttributeByExpressionDialog.this.updateExpressionBorder();
                    } else {
                        CalculateAttributeByExpressionDialog.this.insertInExpression("[" + ((Attribute)CalculateAttributeByExpressionDialog.this.attrList.getSelectedValue()).getPublicName() + "]");
                    }
                    CalculateAttributeByExpressionDialog.this.attrList.clearSelection();
                }
            }
        });
        return this.attributePanel;
    }

    private JComponent getOperationsPanel() {
        this.operationsPanel = new JXPanel((LayoutManager)new GridBagLayout());
        this.operationsPanel.setBorder((Border)BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Operations")));
        Insets minimumInsets = new Insets(2, 4, 2, 4);
        Insets textButtonInsets = new Insets(2, 2, 2, 2);
        this.bMas = this.createButton("+", null, "+", 0, minimumInsets);
        this.bMenos = this.createButton("-", null, "-", 0, minimumInsets);
        this.bPor = this.createButton("*", null, "*", 0, minimumInsets);
        this.bDiv = this.createButton("/", null, "/", 0, minimumInsets);
        this.b0 = this.createButton("0", null, "0", 0, minimumInsets);
        this.b1 = this.createButton("1", null, "1", 0, minimumInsets);
        this.b2 = this.createButton("2", null, "2", 0, minimumInsets);
        this.b3 = this.createButton("3", null, "3", 0, minimumInsets);
        this.b4 = this.createButton("4", null, "4", 0, minimumInsets);
        this.b5 = this.createButton("5", null, "5", 0, minimumInsets);
        this.b6 = this.createButton("6", null, "6", 0, minimumInsets);
        this.b7 = this.createButton("7", null, "7", 0, minimumInsets);
        this.b8 = this.createButton("8", null, "8", 0, minimumInsets);
        this.b9 = this.createButton("9", null, "9", 0, minimumInsets);
        this.bDot = this.createButton(".", null, ".", 0, minimumInsets);
        this.bPar = this.createButton("()", null, "()", 1, minimumInsets);
        this.bParIzq = this.createButton("(", null, "(", 0, minimumInsets);
        this.bParDer = this.createButton(")", null, ")", 0, minimumInsets);
        this.bCor = this.createButton("[]", null, "[]", 1, minimumInsets);
        this.bCorIzq = this.createButton("[", null, "[", 0, minimumInsets);
        this.bCorDer = this.createButton("]", null, "]", 0, minimumInsets);
        this.bE = this.createButton("E", null, "E", 0, minimumInsets);
        this.bOpp = this.createButton("OPP", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Opposite-function"), "OPP(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "OPP()", 1, textButtonInsets);
        this.bInt = this.createButton("INT", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Integer-part-function"), "INT(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "INT()", 1, textButtonInsets);
        this.bAbs = this.createButton("ABS", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Absolute-value-function"), "ABS(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "ABS()", 1, textButtonInsets);
        this.bPow = this.createButton("<html>X<sup>y</sup></html>", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Power"), String.valueOf(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.x-expression")) + " ^ " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.y-expression")), "^", 0, textButtonInsets);
        this.bExp = this.createButton("EXP", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Exponential-function"), "EXP(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "EXP()", 1, textButtonInsets);
        this.bLN = this.createButton("LN", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Nepierian-logarithm-function"), "LN(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "LN()", 1, textButtonInsets);
        this.bLog = this.createButton("LOG10", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Decimal-logarithm-function"), "LOG10(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "LOG10()", 1, textButtonInsets);
        this.bMod = this.createButton("MOD", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Modulo-operator"), "MOD(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.number") + ", " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.divisior") + ")"), "MOD(,)", 2, textButtonInsets);
        this.bPI = this.createButton("<html>&Pi;</html>", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.PI-constant")), "(3.141592653589793)", 0, textButtonInsets);
        this.bSqr = this.createButton("<html>X<sup>2</sup></html>", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Square-function"), "SQR(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "SQR()", 1, textButtonInsets);
        this.bRoot = this.createButton("<html>&radic;X</html>", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Square-root-function"), "ROOT(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "ROOT()", 1, textButtonInsets);
        this.bTrunc = this.createButton("TRUNC", this.createToolTipText("TRUNC", "TRUNC(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ", " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.decimals") + ")", I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.sets-allowed-maximum-decimals-for-numeric-truncating-existing-values-there-is-no-rounding")), "TRUNC(,)", 2, textButtonInsets);
        this.bRound = this.createButton("ROUND", this.createToolTipText("ROUND", "ROUND(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ", " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.decimals") + ")", I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.sets-allowed-maximum-decimals-for-numeric-truncating-existing-values")), "ROUND(,)", 2, textButtonInsets);
        this.bCos = this.createButton("COS", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Cosine-function"), "COS(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "COS()", 1, textButtonInsets);
        this.bSin = this.createButton("SIN", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Sine-function"), "SIN(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "SIN()", 1, textButtonInsets);
        this.bTan = this.createButton("TAN", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Tangent-function"), "TAN(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "TAN()", 1, textButtonInsets);
        this.bACos = this.createButton("ACOS", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Arc-cosine-function"), "ACOS(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "ACOS()", 1, textButtonInsets);
        this.bASin = this.createButton("ASIN", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Arc-sine-function"), "ASIN(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "ASIN()", 1, textButtonInsets);
        this.bATan = this.createButton("ATAN", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Arc-tangent-function"), "ATAN(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "ATAN()", 1, textButtonInsets);
        this.bCosH = this.createButton("COSH", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Hyperbolic-cosine-function"), "COSH(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "COSH()", 1, textButtonInsets);
        this.bSinH = this.createButton("SINH", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Hyperbolic-sine-function"), "SINH(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "SINH()", 1, textButtonInsets);
        this.bTanH = this.createButton("TANH", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Hyperbolic-tangent-function"), "TANH(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "TANH()", 1, textButtonInsets);
        this.bInv = this.createButton("1/X", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Inverse"), "INV(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "INV()", 1, textButtonInsets);
        this.bFact = this.createButton("X!", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.factorial"), "FACT(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "FACT()", 1, textButtonInsets);
        this.bRand = this.createButton("RAND", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.random"), "RAND()", I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.generates-a-random-floating-number-between-cero-and-one")), "RAND()", 0, textButtonInsets);
        this.bBorrarUno = new JButton("<-1");
        this.bBorrarUno.setToolTipText(this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Delete-one")));
        this.bBorrarTodo = new JButton("<-All");
        this.bBorrarTodo.setToolTipText(this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Delete-all")));
        this.bUndo = new JButton("UNDO");
        this.bUndo.setToolTipText(this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Undo-Redo")));
        this.bBorrarUno.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int pos = CalculateAttributeByExpressionDialog.this.expressionTextArea.getCaretPosition();
                CalculateAttributeByExpressionDialog.this.lastExpression = CalculateAttributeByExpressionDialog.this.expressionTextArea.getText();
                int fin = CalculateAttributeByExpressionDialog.this.lastExpression.length();
                CalculateAttributeByExpressionDialog.this.expressionTextArea.setText(String.valueOf(CalculateAttributeByExpressionDialog.this.lastExpression.substring(0, pos - 1)) + CalculateAttributeByExpressionDialog.this.lastExpression.substring(pos, fin));
                CalculateAttributeByExpressionDialog.this.expressionTextArea.setCaretPosition(pos - 1);
            }
        });
        this.bBorrarTodo.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String temp = CalculateAttributeByExpressionDialog.this.expressionTextArea.getText();
                if (temp.length() != 0) {
                    CalculateAttributeByExpressionDialog.this.lastExpression = temp;
                    CalculateAttributeByExpressionDialog.this.expressionTextArea.setText("");
                }
            }
        });
        this.bUndo.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String temp = CalculateAttributeByExpressionDialog.this.expressionTextArea.getText();
                CalculateAttributeByExpressionDialog.this.expressionTextArea.setText(CalculateAttributeByExpressionDialog.this.lastExpression);
                CalculateAttributeByExpressionDialog.this.lastExpression = temp;
            }
        });
        this.bArea = this.createButton(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.area"), this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometry-area"), "area(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometric-attribute") + ")"), "area(" + this.geomNombre + ")", 0, textButtonInsets);
        this.bPerim = this.createButton(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.perimeter"), this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometry-perimeter"), "geomLength(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometric-attribute") + ")"), "geomLength(" + this.geomNombre + ")", 0, textButtonInsets);
        switch (this.geometryType) {
            case 2: 
            case 3: {
                this.bPerim.setText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.length"));
                this.bPerim.setToolTipText(this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometry-length"), "geomLength(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometric-attribute") + ")"));
            }
        }
        this.bCoordX = this.createButton(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.x-coord"), this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometry-x-coordinate"), "coordX(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometric-attribute") + ")"), "coordX(" + this.geomNombre + ")", 0, textButtonInsets);
        this.bCoordY = this.createButton(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.y-coord"), this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometry-y-coordinate"), "coordY(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.geometric-attribute") + ")"), "coordY(" + this.geomNombre + ")", 0, textButtonInsets);
        this.bArea.setEnabled(false);
        this.bPerim.setEnabled(false);
        this.bCoordX.setEnabled(false);
        this.bCoordY.setEnabled(false);
        switch (this.geometryType) {
            case 1: 
            case 8: {
                this.bCoordX.setEnabled(true);
                this.bCoordY.setEnabled(true);
                break;
            }
            case 2: 
            case 3: {
                this.bPerim.setEnabled(true);
                break;
            }
            case 4: 
            case 5: {
                this.bArea.setEnabled(true);
                this.bPerim.setEnabled(true);
            }
        }
        this.bConcat = this.createButton("CONCAT", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.concatenate"), "CONCAT(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression1") + ", " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression2") + "<i>,...</i>)"), "CONCAT(,)", 2, textButtonInsets);
        this.bSubStr = this.createButton("SUBSTRING", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.substring"), "SUBSTRING(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ", " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.initial-position") + "<i>, " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.final-position") + "</i>)", " - " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression-source-string") + "<br>" + " - " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.initial-position-position-of-the-character-from-which-the-substring-is-to-be-created") + "<br>" + " - <i>" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.final-position") + "</i>: " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.position-of-the-character-where-the-substring-ends") + " <i>(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.optional") + ")</i>"), "SUBSTRING(,)", 2, textButtonInsets);
        this.bStrLength = this.createButton("LENGTH", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.string-length"), "LENGTH(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.expression") + ")"), "LENGTH()", 1, textButtonInsets);
        this.bStrIndexOf = this.createButton("INDEXOF", this.createToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.first-index-of-the-one-string-into-the-other"), "INDEXOF(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.string") + ", " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.literal-searched-for") + "<i>, " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.initial-position") + "</i>)", " - " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.string-the-string-to-search-in") + "<br>" + " - " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.literal-searched-for-the-string-searched") + "<br>" + " - <i>" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.initial-position") + "</i>: " + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.the-initial-position") + " <i>(" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.optional") + ")</i>"), "INDEXOF(,)", 2, textButtonInsets);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 0, (JComponent)this.b7, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 1, (JComponent)this.b8, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 2, (JComponent)this.b9, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 3, (JComponent)this.bDiv, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 4, (JComponent)this.bInt, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 5, (JComponent)this.bPow, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 6, (JComponent)this.bCos, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 7, (JComponent)this.bSin, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 8, (JComponent)this.bTan, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 1, 9, (JComponent)this.bInv, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 0, (JComponent)this.b4, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 1, (JComponent)this.b5, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 2, (JComponent)this.b6, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 3, (JComponent)this.bPor, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 4, (JComponent)this.bAbs, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 5, (JComponent)this.bLN, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 6, (JComponent)this.bACos, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 7, (JComponent)this.bASin, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 8, (JComponent)this.bATan, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 2, 9, (JComponent)this.bFact, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 0, (JComponent)this.b1, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 1, (JComponent)this.b2, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 2, (JComponent)this.b3, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 3, (JComponent)this.bMenos, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 4, (JComponent)this.bOpp, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 5, (JComponent)this.bLog, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 6, (JComponent)this.bCosH, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 7, (JComponent)this.bSinH, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 8, (JComponent)this.bTanH, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 3, 9, (JComponent)this.bRand, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 0, (JComponent)this.b0, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 1, (JComponent)this.bDot, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 2, (JComponent)this.bE, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 3, (JComponent)this.bMas, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 4, (JComponent)this.bMod, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 5, (JComponent)this.bExp, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 6, (JComponent)this.bPI, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 7, (JComponent)this.bSqr, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 8, (JComponent)this.bRoot, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 4, 9, (JComponent)this.bTrunc, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 5, 0, (JComponent)this.bPar, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 5, 1, (JComponent)this.bParIzq, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 5, 2, (JComponent)this.bParDer, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 5, 3, (JComponent)this.bCor, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 5, 4, (JComponent)this.bCorIzq, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 5, 5, (JComponent)this.bCorDer, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 5, 6, (JComponent)this.bRound, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 6, 7, (JComponent)this.bBorrarUno, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 6, 8, (JComponent)this.bBorrarTodo, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 6, 9, (JComponent)this.bUndo, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 10, 0, (JComponent)new JLabel(" "), false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 11, 6, (JComponent)this.bArea, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 11, 7, (JComponent)this.bPerim, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 11, 8, (JComponent)this.bCoordX, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 11, 9, (JComponent)this.bCoordY, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 15, 0, (JComponent)new JLabel(" "), false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 16, 6, (JComponent)this.bConcat, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 16, 7, (JComponent)this.bSubStr, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 16, 8, (JComponent)this.bStrLength, false, false);
        FormUtils.addRowInGBL((JComponent)this.operationsPanel, 16, 9, (JComponent)this.bStrIndexOf, false, false);
        FormUtils.addFiller((JComponent)this.operationsPanel, 20, 0);
        return this.operationsPanel;
    }

    private JButton createButton(String labelText, String toolTipText, final String textToInsert, final int positionsToBack, Insets inset) {
        JButton button = new JButton(labelText);
        if (toolTipText != null && !toolTipText.trim().isEmpty()) {
            button.setToolTipText(toolTipText);
        }
        button.setMargin(inset);
        button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CalculateAttributeByExpressionDialog.this.insertInExpression(textToInsert);
                CalculateAttributeByExpressionDialog.this.expressionTextArea.setCaretPosition(CalculateAttributeByExpressionDialog.this.expressionTextArea.getCaretPosition() - positionsToBack);
            }
        });
        return button;
    }

    private String createToolTipText(String titleToolTip) {
        return "<html><b>" + titleToolTip + "</b></html>";
    }

    private String createToolTipText(String titleToolTip, String syntaxToolTip) {
        return "<html><b>" + titleToolTip + "</b><br>" + syntaxToolTip + "</html>";
    }

    private String createToolTipText(String titleToolTip, String syntaxToolTip, String descriptionToolTip) {
        return "<html><b>" + titleToolTip + "</b><br>" + syntaxToolTip + "<br><br>" + descriptionToolTip + "</html>";
    }

    private void insertInExpression(String text) {
        String exp;
        this.lastExpression = exp = this.expressionTextArea.getText();
        int selStart = this.expressionTextArea.getSelectionStart();
        int selEnd = this.expressionTextArea.getSelectionEnd();
        exp = String.valueOf(StringUtils.substring((String)exp, (int)0, (int)selStart)) + text + StringUtils.substring((String)exp, (int)selEnd);
        this.expressionTextArea.setText(exp);
        this.expressionTextArea.setCaretPosition(selStart + text.length());
        this.repaint();
        this.expressionTextArea.requestFocus();
    }

    private JXPanel getExpressionPanel() {
        this.expressionPanel = new JXPanel((LayoutManager)new BorderLayout());
        this.expressionPanel.setBorder((Border)BorderFactory.createTitledBorder(this.bordeExpr));
        this.expressionTextArea = new JXTextArea(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Expression"));
        this.expressionTextArea.setEditable(true);
        this.expressionTextArea.setLineWrap(true);
        this.expressionTextArea.setWrapStyleWord(true);
        this.expressionTextArea.setColumns(50);
        this.expressionTextArea.setRows(3);
        this.expressionTextArea.setToolTipText(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attributes-must-be-between-square-brackets"));
        JScrollPane pane = new JScrollPane((Component)this.expressionTextArea, 20, 31);
        this.expressionPanel.add((Component)pane, (Object)"Center");
        this.expressionTextArea.addFocusListener(new FocusListener(){

            @Override
            public void focusGained(FocusEvent e) {
                CalculateAttributeByExpressionDialog.this.isAtributeField = false;
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        return this.expressionPanel;
    }

    private OKCancelPanel createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean error = false;
                CalculateAttributeByExpressionDialog.this.nuevo = false;
                if (CalculateAttributeByExpressionDialog.this.okCancelPanel.wasOKPressed()) {
                    if (CalculateAttributeByExpressionDialog.this.isInputValid()) {
                        CalculateAttributeByExpressionDialog.this.okPressed = true;
                        error = false;
                    } else {
                        error = true;
                    }
                } else {
                    CalculateAttributeByExpressionDialog.this.okPressed = false;
                    error = false;
                }
                if (!error) {
                    CalculateAttributeByExpressionDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    protected boolean isInputValid() {
        Attribute attr;
        int i;
        String expresion;
        boolean ok = true;
        String errorMsg = null;
        String titleMsg = CalculateAttributeByExpressionPlugIn.NAME;
        if (this.attrNameTextField.getText().isEmpty() || this.expressionTextArea.getText().isEmpty()) {
            ok = false;
            errorMsg = I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-name-or-expression-field-is-empty");
        }
        if (ok) {
            expresion = this.expressionTextArea.getText();
            i = 0;
            while (i < this.layerAttrs.size()) {
                int opcion;
                AttributeType type;
                attr = this.layerAttrs.get(i);
                Pattern patron = Pattern.compile("[" + attr.getPublicName() + "]", 16);
                Matcher encaja = patron.matcher(expresion);
                if (encaja.find() && !AttributeType.isNumeric(type = attr.getType()) && !AttributeType.isString(type) && (opcion = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getMessage("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-{0}-is-not-numeric-type-it-is-{1}-type", new Object[]{attr.getPublicName(), type})) + ".\n" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Do-you-want-to-continue-with-the-actual-expression"), I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Information"))) != 0) {
                    ok = false;
                }
                ++i;
            }
        }
        if (ok) {
            expresion = this.getExpression();
            i = 0;
            while (i < this.layerAttrs.size()) {
                attr = this.layerAttrs.get(i);
                AttributeType type = attr.getType();
                String replacement = AttributeType.isNumeric(type) ? " 1 " : " \"cadena\" ";
                expresion = StringUtils.replace((String)expresion, (String)("[" + attr.getPublicName() + "]"), (String)replacement);
                ++i;
            }
            expresion = StringUtils.replace((String)expresion, (String)("area(" + this.geomNombre + ")"), (String)" 1 ");
            expresion = StringUtils.replace((String)expresion, (String)("geomLength(" + this.geomNombre + ")"), (String)" 1 ");
            expresion = StringUtils.replace((String)expresion, (String)("coordX(" + this.geomNombre + ")"), (String)" 1 ");
            expresion = StringUtils.replace((String)expresion, (String)("coordY(" + this.geomNombre + ")"), (String)" 1 ");
            expresion = CalculateAttributeByExpressionPlugIn.specialCharsPreparse(expresion);
            try {
                JeksExpressionSyntax syntax = new JeksExpressionSyntax(Locale.ENGLISH);
                JeksInterpreter interpreter = new JeksInterpreter();
                JeksFunctionParser functionParser = new JeksFunctionParser((JeksFunctionSyntax)new JeksExpressionSyntax(Locale.ENGLISH));
                JeksParameter parameter = new JeksParameter(syntax, (Interpreter)interpreter, (TableModel)new JeksTableModel());
                JeksExpressionParser jeksExpressionParser = new JeksExpressionParser(syntax, (ExpressionParameter)parameter, (Interpreter)interpreter, functionParser, null);
                jeksExpressionParser.addUserFunction((Function)new ConcatFunction());
                jeksExpressionParser.addUserFunction((Function)new SubStringFunction());
                jeksExpressionParser.addUserFunction((Function)new LengthFunction());
                jeksExpressionParser.addUserFunction((Function)new IndexOfFunction());
                jeksExpressionParser.addUserFunction((Function)new SqrFunction());
                jeksExpressionParser.addUserFunction((Function)new OppFunction());
                jeksExpressionParser.addUserFunction((Function)new InvFunction());
                jeksExpressionParser.addUserFunction((Function)new TruncFunction());
                jeksExpressionParser.addUserFunction((Function)new RoundFunction());
                CompiledExpression compiledExpression = jeksExpressionParser.compileExpression(expresion);
                compiledExpression.computeExpression((Interpreter)interpreter);
            }
            catch (Exception e) {
                LOGGER.error((Object)e);
                ok = false;
                if (e instanceof CompilationException) {
                    int err = ((CompilationException)((Object)e)).getErrorNumber();
                    errorMsg = String.valueOf(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Expression-error-has-been-found")) + ":\n";
                    if (err == 5) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Assign-operator-expected");
                    } else if (err == 4) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Closing-bracket-expected");
                    } else if (err == 9) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Closing-bracket-without-opening-bracket");
                    } else if (err == 14) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.ELSE-operator-expected");
                    } else if (err == 16) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.ELSE-operator-without-IF-THEN-operators");
                    } else if (err == 12) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Closing-bracket-expected");
                    } else if (err == 11) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Missing-parameters-in-function-call");
                    } else if (err == 0) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Opening-bracket-expected");
                    } else if (err == 2) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Reserved-word");
                    } else if (err == 8) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Syntax-error") + " ";
                        if (((CompilationException)((Object)e)).getExtractedString() != null) {
                            errorMsg = String.valueOf(errorMsg) + ((CompilationException)((Object)e)).getExtractedString() + ". ";
                        }
                    } else if (err == 13) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.THEN-opertor-expected");
                    } else if (err == 15) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.THEN-operator-without-IF-operator");
                    } else if (err == 10) {
                        errorMsg = String.valueOf(errorMsg) + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Unknown-identifier");
                    }
                } else {
                    errorMsg = String.valueOf(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.There-is-one-or-more-errors")) + ":\n" + e.toString();
                }
                titleMsg = I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Syntax-error");
            }
        }
        if (ok) {
            String atributo = this.attrNameTextField.getText();
            if (atributo.equals(this.pkNombre)) {
                ok = false;
                errorMsg = I18N.getMessage("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-{0}-is-primary-key-and-it-can-not-be-modified-Change-attribute-name", new Object[]{atributo});
            } else if (atributo.equals(this.geomNombre)) {
                ok = false;
                errorMsg = I18N.getMessage("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-{0}-is-geometry-and-it-can-not-be-modified-Change-attribute-name", new Object[]{atributo});
            } else {
                boolean existe = false;
                int pos = -1;
                int i2 = 0;
                while (i2 < this.layerAttrs.size() & !existe) {
                    if (atributo.equals(this.layerAttrs.get(i2).getPublicName())) {
                        existe = true;
                        pos = i2;
                    }
                    ++i2;
                }
                if (existe) {
                    int opcion;
                    AttributeType tipo = this.layerAttrs.get(pos).getType();
                    if ((tipo.equals(AttributeType.BOOLEAN) || tipo.equals(AttributeType.DATE) || tipo.equals(AttributeType.GEOMETRY) || tipo.equals(AttributeType.OBJECT) || tipo.equals(AttributeType.TIME) || tipo.equals(AttributeType.TIMESTAMP)) && (opcion = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Output-attribute-must-be-numeric-or-text-type")) + ".\n" + I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Do-you-want-to-continue-with-the-actual-expression"), I18N.getString("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Information"))) != 0) {
                        ok = false;
                    }
                    if (ok) {
                        int option = DialogFactory.showYesNoWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-{0}-already-exists-Do-you-want-to-overwrite-it", new Object[]{atributo}), titleMsg);
                        if (option != 0) {
                            ok = false;
                        } else {
                            this.nuevo = false;
                        }
                    }
                } else {
                    int option = DialogFactory.showYesNoWarningDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.util.CalculateAttributeByExpressionDialog.Attribute-{0}-does-not-exist-Do-you-want-to-create-it", new Object[]{atributo}), titleMsg);
                    if (option != 0) {
                        ok = false;
                    } else {
                        this.nuevo = true;
                    }
                }
            }
        }
        if (!ok && StringUtils.isNotEmpty((String)errorMsg)) {
            DialogFactory.showWarningDialog(this, errorMsg, titleMsg);
        }
        return ok;
    }

    public boolean wasOkPressed() {
        return this.okPressed;
    }

    public boolean isNewAttribute() {
        return this.nuevo;
    }

    public String getAttributeName() {
        String attrName = this.attrNameTextField.getText().trim();
        if (!this.isNewAttribute()) {
            int i = 0;
            while (i < this.layerAttrs.size()) {
                if (attrName.equals(this.layerAttrs.get(i).getPublicName())) {
                    attrName = this.layerAttrs.get(i).getName();
                }
                ++i;
            }
        }
        return attrName;
    }

    public String getExpression() {
        String expression = this.expressionTextArea.getText();
        if (!StringUtils.startsWith((String)expression, (String)"=")) {
            return "=" + expression;
        }
        return expression;
    }

    public boolean useSelectedOnly() {
        return this.applyToSelectedOnlyRadioButton.isSelected();
    }
}

