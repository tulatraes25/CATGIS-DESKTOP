/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.eteks.parser.CompilationException
 *  com.eteks.parser.Interpreter
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.gui.swing.sldeditor.symbolizer.std;

import com.eteks.parser.CompilationException;
import com.eteks.parser.Interpreter;
import com.vividsolutions.jump.feature.AbstractBasicFeature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import es.kosmo.desktop.gui.components.JFontFamilyCombobox;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.GraphicResizeEditor;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.LineToLabelEndingAnchorTypeEditor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.Expression;
import org.saig.core.filter.LabelExpression;
import org.saig.core.filter.LabelExpressionUtil;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.DashArrayEditor;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.FillEditor;
import org.saig.core.gui.swing.sldeditor.property.GeometryChooser;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.LabelPlacementEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultColorEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.std.DumbInterpreter;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.core.styling.Font;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.util.ColorUtil;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class DefaultTextSymbolizerEditor
extends SymbolizerEditor
implements SLDEditor {
    private static Logger LOGGER = Logger.getLogger(DefaultTextSymbolizerEditor.class);
    private static final long serialVersionUID = 1L;
    private static String[] fontStyles = new String[]{I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.normal"), I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.bold"), I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.italic"), I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.bold-italic")};
    private TextSymbolizer symbolizer;
    private FeatureSchema ft;
    private JLabel lblGeometry;
    private GeometryChooser geomChooser;
    private JLabel lblLabel;
    private ExpressionEditor attributeChooser;
    private JTextField tfExpression;
    private JCheckBox chkUseExpression;
    private JLabel unitsLabel;
    private JComboBox units;
    private JLabel lblFont;
    private JFontFamilyCombobox fontFamilyCombobox;
    private JLabel lblFontStyle;
    private JComboBox cmbFontStyle;
    private JLabel lblFontSize;
    private ExpressionEditor neFontSize;
    private JCheckBox chkUseHalo;
    private JLabel lblHaloRadius;
    private FillEditor fbeHaloFill;
    private ExpressionEditor neHaloRadius;
    private FillEditor fillEditor;
    private LabelPlacementEditor placementEditor;
    private JCheckBox chkAttributeHeight;
    private ExpressionEditor heightAttributeChooser;
    private ExpressionEditor attributePriorityChooser;
    private JCheckBox attributePriorityCheckBox;
    private JLabel repeatTitleLabel;
    private JLabel repeatDistanceLabel;
    private ExpressionEditor repeatDistanceExpressionEditor;
    private JLabel autowrapTitleLabel;
    private JLabel autowrapWidthLabel;
    private ExpressionEditor autowrapWidthExpressionEditor;
    private JLabel spaceAroundTitleLabel;
    private JLabel spaceAroundLabel;
    private ExpressionEditor spaceAroundExpressionEditor;
    private JLabel maxDisplacementTitleLabel;
    private JLabel maxDisplacementLabel;
    private ExpressionEditor maxDisplacementExpressionEditor;
    private JLabel shieldGraphicTitleLabel;
    private JCheckBox shieldGraphicCheckBox;
    private GraphicEditor shieldGraphicEditor;
    private GraphicResizeEditor shieldGraphicResizeEditor;
    private JLabel shieldGraphicMarginsLabel;
    private ExpressionEditor shieldGraphicMarginsEditor;
    private JLabel forceLeftToRightTitleLabel;
    private JCheckBox forceLeftToRightCheckBox;
    private JLabel lineFromGeomOptionsLabel;
    private JCheckBox lineFromGeomToLabelCheckBox;
    private JCheckBox noLabelIfNoGeomCheckBox;
    private JLabel labelLineColorLabel;
    private DefaultColorEditor labelLineColorEditor;
    private JLabel widhtJLabel;
    private ExpressionEditor widthExpressionEditor;
    private JLabel labelLineDashJLabel;
    private DashArrayEditor labelLineDashEditor;
    private LineToLabelEndingAnchorTypeEditor labelLineEndingAnchorTypeEditor;
    private JCheckBox chkScale;
    private ExpressionEditor neScaleMin;
    private ExpressionEditor neScaleMax;

    public DefaultTextSymbolizerEditor(FeatureSchema ft) {
        this(null, ft);
        this.ft = ft;
    }

    public DefaultTextSymbolizerEditor(TextSymbolizer ts, FeatureSchema ft) {
        if (ts == null) {
            ts = styleBuilder.createTextSymbolizer();
        }
        this.setLayout(new GridBagLayout());
        JTabbedPane tabPane = new JTabbedPane();
        JPanel basicPanel = new JPanel(new GridBagLayout());
        JPanel advancedPanel = new JPanel(new GridBagLayout());
        JPanel basicColumnLeft = new JPanel(new GridBagLayout());
        JPanel basicColumnRight = new JPanel(new GridBagLayout());
        JPanel advancedColumnLeft = new JPanel(new GridBagLayout());
        JPanel advancedColumnRight = new JPanel(new GridBagLayout());
        this.lblGeometry = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.geometry-property"));
        this.geomChooser = propertyEditorFactory.createGeometryChooser(ft);
        this.lblLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.label"));
        this.attributeChooser = propertyEditorFactory.createFeatureAttributeChooser(ft);
        this.lblFont = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.font"));
        this.fontFamilyCombobox = new JFontFamilyCombobox(16);
        this.lblFontStyle = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.font-style"));
        this.cmbFontStyle = new JComboBox<String>(fontStyles);
        this.lblFontSize = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.font-size"));
        this.neFontSize = propertyEditorFactory.createIntSizeEditor(ft);
        this.fillEditor = propertyEditorFactory.createFillEditor(ft);
        this.lineFromGeomOptionsLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Line-from-label-to-geom"));
        this.lineFromGeomToLabelCheckBox = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Draw-line-from-the-label-to-the-geometry"));
        this.lineFromGeomToLabelCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTextSymbolizerEditor.this.refreshLineToLabelOptionsGUI(DefaultTextSymbolizerEditor.this.lineFromGeomToLabelCheckBox.isSelected());
            }
        });
        this.noLabelIfNoGeomCheckBox = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Do-not-draw-the-label-if-the-geometry-is-null"));
        this.chkUseHalo = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.use-halo"));
        this.chkUseHalo.setBorder(BorderFactory.createEmptyBorder());
        this.fbeHaloFill = propertyEditorFactory.createCompactFillEditor(ft);
        this.fbeHaloFill.setPreferredSize(FormUtils.getButtonDimension());
        this.neHaloRadius = propertyEditorFactory.createIntSizeEditor(ft);
        this.lblHaloRadius = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.halo-radious"));
        this.placementEditor = propertyEditorFactory.createLabelPlacementEditor(ft);
        this.chkUseExpression = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Use-expression"));
        this.chkUseExpression.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                DefaultTextSymbolizerEditor.this.attributeChooser.setEnabled(!DefaultTextSymbolizerEditor.this.chkUseExpression.isSelected());
                DefaultTextSymbolizerEditor.this.tfExpression.setEnabled(DefaultTextSymbolizerEditor.this.chkUseExpression.isSelected());
            }
        });
        this.tfExpression = new JTextField();
        this.chkScale = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Scale-label"));
        this.chkScale.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultTextSymbolizerEditor.this.neScaleMax.setEnabled(DefaultTextSymbolizerEditor.this.chkScale.isSelected());
                DefaultTextSymbolizerEditor.this.neScaleMin.setEnabled(DefaultTextSymbolizerEditor.this.chkScale.isSelected());
            }
        });
        this.neScaleMax = propertyEditorFactory.createIntSizeEditor(ft);
        this.neScaleMin = propertyEditorFactory.createIntSizeEditor(ft);
        this.chkAttributeHeight = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Height-by-attribute"));
        this.chkAttributeHeight.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultTextSymbolizerEditor.this.heightAttributeChooser.setEnabled(DefaultTextSymbolizerEditor.this.chkAttributeHeight.isSelected());
            }
        });
        this.heightAttributeChooser = propertyEditorFactory.createFeatureAttributeChooser(ft, new Class[]{Number.class});
        this.tfExpression.addFocusListener(new FocusListener(){

            @Override
            public void focusGained(FocusEvent arg0) {
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                try {
                    AbstractBasicFeature.getParser().computeExpression(DefaultTextSymbolizerEditor.this.tfExpression.getText(), (Interpreter)new DumbInterpreter());
                    List<String> atributos = LabelExpressionUtil.getLabelsFromExpression(DefaultTextSymbolizerEditor.this.tfExpression.getText());
                    for (String attr : atributos) {
                        Attribute o = DefaultTextSymbolizerEditor.this.ft.getAttribute(attr);
                        if (o != null) continue;
                        DialogFactory.showWarningDialog(DefaultTextSymbolizerEditor.this, I18N.getMessage("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.The-attribute-{0}-does-not-exist", new Object[]{attr}), "Atributo no existente");
                    }
                }
                catch (CompilationException e) {
                    DialogFactory.showErrorDialog(DefaultTextSymbolizerEditor.this, I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Expression-error"), "Expresi\u00f3n incorrecta");
                }
            }
        });
        this.unitsLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Units"));
        this.units = new JComboBox<String>(Symbolizer.UOM_ALLOWED);
        this.labelLineColorLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Color"));
        this.labelLineColorEditor = new DefaultColorEditor();
        this.widhtJLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Line-width"));
        this.widthExpressionEditor = propertyEditorFactory.createDoubleSizeEditor(ft);
        this.widthExpressionEditor.setExpression(new LiteralExpressionImpl(1.0));
        this.labelLineDashJLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Dotted"));
        this.labelLineDashEditor = propertyEditorFactory.createSimpleDashArrayEditor();
        this.labelLineEndingAnchorTypeEditor = propertyEditorFactory.createLineToLabelEndingAnchorTypeEditor();
        this.attributePriorityChooser = propertyEditorFactory.createFeatureAttributeChooser(ft, new Class[]{Number.class});
        this.attributePriorityCheckBox = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.priority"));
        this.attributePriorityCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (DefaultTextSymbolizerEditor.this.attributePriorityCheckBox.isSelected()) {
                    DefaultTextSymbolizerEditor.this.attributePriorityChooser.setEnabled(true);
                } else {
                    DefaultTextSymbolizerEditor.this.attributePriorityChooser.setEnabled(false);
                }
            }
        });
        this.repeatTitleLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Repeat-labels"));
        this.repeatDistanceLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Distance")) + " (px)");
        this.repeatDistanceExpressionEditor = propertyEditorFactory.createNumberEditor(new Integer(0), new Integer(0), new Integer(Integer.MAX_VALUE), new Integer(1), ft);
        this.autowrapTitleLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Autowrap"));
        this.autowrapWidthLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Maximum-width")) + " (px)");
        this.autowrapWidthExpressionEditor = propertyEditorFactory.createNumberEditor(new Integer(0), new Integer(0), new Integer(Integer.MAX_VALUE), new Integer(1), ft);
        this.spaceAroundTitleLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Minimum-distance-between-labels"));
        this.spaceAroundLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Distance")) + " (px)");
        this.spaceAroundExpressionEditor = propertyEditorFactory.createNumberEditor(new Integer(0), new Integer(0), new Integer(Integer.MAX_VALUE), new Integer(1), ft);
        this.maxDisplacementTitleLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Maximum-label-displacement"));
        this.maxDisplacementLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Distance")) + " (px)");
        this.maxDisplacementExpressionEditor = propertyEditorFactory.createNumberEditor(new Integer(0), new Integer(0), new Integer(Integer.MAX_VALUE), new Integer(1), ft);
        this.forceLeftToRightTitleLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Force-left-to-right-reading"));
        this.forceLeftToRightCheckBox = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Force-left-to-right-label-reading"));
        this.shieldGraphicTitleLabel = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Graphic-shield"));
        this.shieldGraphicCheckBox = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Use-graphic-shield"));
        this.shieldGraphicCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTextSymbolizerEditor.this.shieldGraphicEditor.setEnabled(DefaultTextSymbolizerEditor.this.shieldGraphicCheckBox.isSelected());
                DefaultTextSymbolizerEditor.this.shieldGraphicResizeEditor.setEnabled(DefaultTextSymbolizerEditor.this.shieldGraphicCheckBox.isSelected());
                DefaultTextSymbolizerEditor.this.shieldGraphicMarginsLabel.setEnabled(DefaultTextSymbolizerEditor.this.shieldGraphicCheckBox.isSelected());
                DefaultTextSymbolizerEditor.this.shieldGraphicMarginsEditor.setEnabled(DefaultTextSymbolizerEditor.this.shieldGraphicCheckBox.isSelected());
            }
        });
        this.shieldGraphicEditor = propertyEditorFactory.createGraphicFillEditor(ft);
        this.shieldGraphicEditor.setEnabled(false);
        this.shieldGraphicResizeEditor = propertyEditorFactory.createGraphicResizeEditor();
        this.shieldGraphicResizeEditor.setEnabled(false);
        this.shieldGraphicMarginsLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Margins")) + " (px)");
        this.shieldGraphicMarginsEditor = propertyEditorFactory.createIntSizeEditor(ft);
        this.shieldGraphicMarginsLabel.setEnabled(false);
        this.shieldGraphicMarginsEditor.setEnabled(false);
        FormUtils.addRowInGBL(basicColumnLeft, 0, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.general")));
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 1, 0, this.lblGeometry, (JComponent)this.geomChooser);
        FormUtils.addRowInGBL(basicColumnLeft, 2, 0, this.noLabelIfNoGeomCheckBox);
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 3, 0, this.lblLabel, (JComponent)this.attributeChooser);
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 4, 0, this.chkUseExpression, (JComponent)this.tfExpression);
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 5, 0, this.lblFont, (JComponent)this.fontFamilyCombobox);
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 6, 0, this.lblFontStyle, (JComponent)this.cmbFontStyle);
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 7, 0, this.lblFontSize, (JComponent)this.neFontSize);
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 8, 0, this.unitsLabel, (JComponent)this.units);
        FormUtils.addRowInGBL(basicColumnLeft, 9, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.halo")));
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 10, 0, this.chkUseHalo, (JComponent)this.fbeHaloFill);
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 11, 0, this.lblHaloRadius, (JComponent)this.neHaloRadius);
        FormUtils.addRowInGBL(basicColumnLeft, 12, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.fill")));
        FormUtils.addRowInGBL((JComponent)basicColumnLeft, 13, 0, (JComponent)this.fillEditor, true, false);
        FormUtils.addFiller(basicColumnLeft, 19, 0);
        FormUtils.addRowInGBL(basicColumnRight, 1, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.placement")));
        FormUtils.addRowInGBL(basicColumnRight, 2, 0, this.placementEditor);
        FormUtils.addRowInGBL(basicColumnRight, 7, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.behaviour-by-attributes")));
        FormUtils.addRowInGBL(basicColumnRight, 8, 0, this.chkAttributeHeight);
        FormUtils.addRowInGBL((JComponent)basicColumnRight, 9, 0, I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Height-attribute"), (JComponent)this.heightAttributeChooser);
        FormUtils.addRowInGBL(basicColumnRight, 12, 0, this.attributePriorityCheckBox);
        FormUtils.addRowInGBL((JComponent)basicColumnRight, 13, 0, I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.priority-attribute"), (JComponent)this.attributePriorityChooser);
        FormUtils.addFiller(basicColumnRight, 14, 0);
        int advColLeftCont = 0;
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.repeatTitleLabel);
        FormUtils.addRowInGBL((JComponent)advancedColumnLeft, advColLeftCont++, 0, this.repeatDistanceLabel, (JComponent)this.repeatDistanceExpressionEditor);
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.autowrapTitleLabel);
        FormUtils.addRowInGBL((JComponent)advancedColumnLeft, advColLeftCont++, 0, this.autowrapWidthLabel, (JComponent)this.autowrapWidthExpressionEditor);
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.spaceAroundTitleLabel);
        FormUtils.addRowInGBL((JComponent)advancedColumnLeft, advColLeftCont++, 0, this.spaceAroundLabel, (JComponent)this.spaceAroundExpressionEditor);
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.maxDisplacementTitleLabel);
        FormUtils.addRowInGBL((JComponent)advancedColumnLeft, advColLeftCont++, 0, this.maxDisplacementLabel, (JComponent)this.maxDisplacementExpressionEditor);
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.forceLeftToRightTitleLabel);
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.forceLeftToRightCheckBox);
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.shieldGraphicTitleLabel);
        FormUtils.addRowInGBL((JComponent)advancedColumnLeft, advColLeftCont++, 0, this.shieldGraphicCheckBox, (JComponent)this.shieldGraphicEditor);
        FormUtils.addRowInGBL(advancedColumnLeft, advColLeftCont++, 0, this.shieldGraphicResizeEditor);
        FormUtils.addRowInGBL((JComponent)advancedColumnLeft, advColLeftCont++, 0, this.shieldGraphicMarginsLabel, (JComponent)this.shieldGraphicMarginsEditor);
        FormUtils.addFiller(advancedColumnRight, 99, 0);
        int advColRightCont = 0;
        FormUtils.addRowInGBL(advancedColumnRight, advColRightCont++, 0, this.lineFromGeomOptionsLabel);
        FormUtils.addRowInGBL(advancedColumnRight, advColRightCont++, 0, this.lineFromGeomToLabelCheckBox);
        FormUtils.addRowInGBL((JComponent)advancedColumnRight, advColRightCont++, 0, this.labelLineColorLabel, (JComponent)this.labelLineColorEditor);
        FormUtils.addRowInGBL((JComponent)advancedColumnRight, advColRightCont++, 0, this.widhtJLabel, (JComponent)this.widthExpressionEditor);
        FormUtils.addRowInGBL((JComponent)advancedColumnRight, advColRightCont++, 0, this.labelLineDashJLabel, (JComponent)this.labelLineDashEditor);
        FormUtils.addRowInGBL((JComponent)advancedColumnRight, advColRightCont++, 0, (JComponent)this.labelLineEndingAnchorTypeEditor, true, false);
        FormUtils.addRowInGBL((JComponent)advancedColumnRight, advColRightCont++, 0, (JComponent)FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.scale")), true, true);
        FormUtils.addRowInGBL(advancedColumnRight, advColRightCont++, 0, this.chkScale);
        FormUtils.addRowInGBL((JComponent)advancedColumnRight, advColRightCont++, 0, String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Minimum-value")) + ":", (JComponent)this.neScaleMin);
        FormUtils.addRowInGBL((JComponent)advancedColumnRight, advColRightCont++, 0, String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Maximum-value")) + ":", (JComponent)this.neScaleMax);
        FormUtils.addFiller(advancedColumnRight, 99, 0);
        FormUtils.addRowInGBL((JComponent)basicPanel, 0, 0, (JComponent)basicColumnLeft, basicColumnRight, 1.0, false);
        FormUtils.addFiller(basicPanel, 0, 2);
        FormUtils.addRowInGBL((JComponent)advancedPanel, 0, 0, (JComponent)advancedColumnLeft, advancedColumnRight, 1.0, false);
        FormUtils.addFiller(advancedPanel, 0, 2);
        tabPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.basic"), basicPanel);
        tabPane.add(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultPointSymbolizerEditor.Advanced"), advancedPanel);
        FormUtils.addRowInGBL(this, 0, 0, tabPane);
        FormUtils.addFiller(this, 1, 0);
        Dimension newSize = new Dimension(200, 20);
        this.fontFamilyCombobox.setMinimumSize(newSize);
        this.fontFamilyCombobox.setPreferredSize(newSize);
        this.repeatDistanceExpressionEditor.setMinimumSize(newSize);
        this.repeatDistanceExpressionEditor.setPreferredSize(newSize);
    }

    @Override
    public Symbolizer getSymbolizer() {
        if (this.geomChooser.isVisible()) {
            this.symbolizer.setGeometryPropertyName(this.geomChooser.getSelectedName());
        }
        Expression exp = null;
        exp = !this.chkUseExpression.isSelected() ? this.attributeChooser.getExpression() : new LabelExpression(this.tfExpression.getText());
        String fontName = (String)this.fontFamilyCombobox.getSelectedItem();
        String[] fontNames = new String[]{fontName};
        if (fontNames == null || fontNames.length == 0) {
            this.symbolizer.setFonts(null);
        } else {
            int cmbIndex = this.cmbFontStyle.getSelectedIndex();
            boolean bold = cmbIndex == 1 || cmbIndex == 3;
            boolean italic = cmbIndex == 2 || cmbIndex == 3;
            Expression weight = null;
            weight = bold ? styleBuilder.literalExpression("bold") : styleBuilder.literalExpression("normal");
            Expression style = null;
            style = italic ? styleBuilder.literalExpression("italic") : styleBuilder.literalExpression("normal");
            Expression size = this.neFontSize.getExpression();
            Font[] fonts = new Font[fontNames.length];
            int i = 0;
            while (i < fonts.length) {
                fonts[i] = styleBuilder.createFont(styleBuilder.literalExpression(fontNames[i]), style, weight, size);
                ++i;
            }
            this.symbolizer.setFonts(fonts);
        }
        this.symbolizer.setFill(this.fillEditor.getFill());
        if (!this.chkUseHalo.isSelected()) {
            this.symbolizer.setHalo(null);
        } else {
            Halo halo = styleBuilder.createHalo();
            halo.setFill(this.fbeHaloFill.getFill());
            halo.setRadius(this.neHaloRadius.getExpression());
            this.symbolizer.setHalo(halo);
        }
        this.symbolizer.setLabel(exp);
        if (this.placementEditor.isSelected()) {
            this.symbolizer.setLabelPlacement(this.placementEditor.getLabelPlacement());
            Map<String, String> placementOptions = this.placementEditor.getLabelPlacementOptions();
            for (String option : placementOptions.keySet()) {
                this.symbolizer.addToOptions(option, placementOptions.get(option));
            }
            this.placementEditor.setSelected(true);
        } else {
            this.placementEditor.setSelected(false);
            this.symbolizer.setLabelPlacement(null);
        }
        if (this.chkScale.isSelected()) {
            int valueMax = 0;
            int valueMin = 0;
            boolean check = true;
            try {
                valueMax = ((Number)this.neScaleMax.getExpression().getValue(null)).intValue();
                valueMin = ((Number)this.neScaleMin.getExpression().getValue(null)).intValue();
                if (valueMin > valueMax) {
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.The-maximum-value-must-be-higher-than-the-minimum-one"), I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.Error"));
                    check = false;
                }
            }
            catch (NumberFormatException e) {
                check = false;
            }
            if (check) {
                this.symbolizer.setScale(true);
                this.symbolizer.setScaleMaxValue(((Number)this.neScaleMax.getExpression().getValue(null)).doubleValue());
                this.symbolizer.setScaleMinValue(((Number)this.neScaleMin.getExpression().getValue(null)).doubleValue());
            } else {
                this.symbolizer.setScale(false);
            }
        } else {
            this.symbolizer.setScale(false);
        }
        if (this.chkAttributeHeight.isSelected()) {
            this.symbolizer.setHeightAttribute(this.heightAttributeChooser.getExpression());
        } else {
            this.symbolizer.setHeightAttribute(null);
        }
        if (this.attributePriorityCheckBox.isSelected()) {
            this.symbolizer.setPriority(this.attributePriorityChooser.getExpression());
        } else {
            this.symbolizer.setPriority(null);
        }
        this.symbolizer.setAttributeRotation(null);
        this.symbolizer.addToOptions("lineToLabel", this.lineFromGeomToLabelCheckBox.isSelected() ? "true" : null);
        this.symbolizer.addToOptions("ignoreLabelIfGeometriesAreNull", this.noLabelIfNoGeomCheckBox.isSelected() ? "true" : null);
        if (this.lineFromGeomToLabelCheckBox.isSelected()) {
            this.symbolizer.addToOptions("lineToLabelColor", ColorUtil.toHex(this.labelLineColorEditor.getColor()));
            this.symbolizer.addToOptions("lineToLabelWidth", this.widthExpressionEditor.getExpression().toString());
            this.symbolizer.addToOptions("lineToLabelDash", StringUtil.join(this.labelLineDashEditor.getDashArray()));
            this.symbolizer.addToOptions("lineToLabelEndingAnchor", this.labelLineEndingAnchorTypeEditor.getLineToLabelEndingAnchorType().toString());
        }
        this.symbolizer.setUnitsOfMeasurement((String)this.units.getSelectedItem());
        this.symbolizer.addToOptions("repeat", Integer.toString(((Number)this.repeatDistanceExpressionEditor.getExpression().getValue(null)).intValue()));
        this.symbolizer.addToOptions("autoWrap", Integer.toString(((Number)this.autowrapWidthExpressionEditor.getExpression().getValue(null)).intValue()));
        this.symbolizer.addToOptions("spaceAround", Integer.toString(((Number)this.spaceAroundExpressionEditor.getExpression().getValue(null)).intValue()));
        this.symbolizer.addToOptions("maxDisplacement", Integer.toString(((Number)this.maxDisplacementExpressionEditor.getExpression().getValue(null)).intValue()));
        this.symbolizer.addToOptions("forceLeftToRight", Boolean.toString(this.forceLeftToRightCheckBox.isSelected()));
        if (this.shieldGraphicCheckBox.isSelected()) {
            this.symbolizer.setGraphic(this.shieldGraphicEditor.getGraphic());
            this.symbolizer.addToOptions("graphic-resize", this.shieldGraphicResizeEditor.getGraphicResize().toString());
            this.symbolizer.addToOptions("graphic-margin", Integer.toString(((Number)this.shieldGraphicMarginsEditor.getExpression().getValue(null)).intValue()));
        } else {
            this.symbolizer.setGraphic(null);
        }
        return this.symbolizer;
    }

    @Override
    public void setSymbolizer(Symbolizer s) {
        String forceLeftToRightOptionValue;
        String maxDisplacementOptionValue;
        String spaceAroundOptionValue;
        String autoWrapOptionValue;
        if (!(s instanceof TextSymbolizer)) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.cannot-set-symbolizer-other-than-a-text-symbolizer"));
        }
        this.symbolizer = (TextSymbolizer)s;
        this.geomChooser.setSelectedName(this.symbolizer.getGeometryPropertyName());
        Font[] fonts = this.symbolizer.getFonts();
        if (fonts != null) {
            String style;
            String weight;
            String[] names = new String[fonts.length];
            int i = 0;
            while (i < fonts.length) {
                names[i] = fonts[i].getFontFamily().toString();
                ++i;
            }
            this.fontFamilyCombobox.setSelectedItem(names[0]);
            int comboIndex = 0;
            if (fonts[0].getFontWeight() != null && (weight = fonts[0].getFontWeight().toString()).equalsIgnoreCase("bold")) {
                comboIndex = 1;
            }
            if (fonts[0].getFontStyle() != null && ((style = fonts[0].getFontStyle().toString()).equalsIgnoreCase("italic") || style.equalsIgnoreCase("oblique"))) {
                comboIndex += 2;
            }
            this.cmbFontStyle.setSelectedIndex(comboIndex);
            if (fonts[0].getFontSize() != null) {
                this.neFontSize.setExpression(fonts[0].getFontSize());
            } else {
                this.neFontSize.setExpression(styleBuilder.literalExpression(10));
            }
        }
        this.fillEditor.setFill(this.symbolizer.getFill());
        if (this.symbolizer.getHalo() == null) {
            this.chkUseHalo.setSelected(false);
        } else {
            this.chkUseHalo.setSelected(true);
            this.neHaloRadius.setExpression(this.symbolizer.getHalo().getRadius());
            this.fbeHaloFill.setFill(this.symbolizer.getHalo().getFill());
        }
        Expression atributeLabel = this.symbolizer.getLabel();
        if (atributeLabel instanceof LabelExpression) {
            this.tfExpression.setText(((LabelExpression)atributeLabel).getExpression());
            this.chkUseExpression.setSelected(true);
            this.attributeChooser.setEnabled(false);
        } else {
            this.attributeChooser.setExpression(this.symbolizer.getLabel());
            this.chkUseExpression.setSelected(false);
            this.tfExpression.setEnabled(false);
        }
        if (this.symbolizer.getLabelPlacement() != null) {
            this.placementEditor.setLabelPlacement(this.symbolizer.getLabelPlacement());
            this.placementEditor.setLabelPlacementOptions(this.symbolizer.getOptions());
            this.placementEditor.setSelected(true);
        } else {
            this.placementEditor.setSelected(false);
        }
        if (this.symbolizer.isScale()) {
            this.chkScale.setSelected(true);
            this.neScaleMin.setExpression(new LiteralExpressionImpl(this.symbolizer.getScaleMinValue()));
            this.neScaleMax.setExpression(new LiteralExpressionImpl(this.symbolizer.getScaleMaxValue()));
        }
        this.neScaleMax.setEnabled(this.symbolizer.isScale());
        this.neScaleMin.setEnabled(this.symbolizer.isScale());
        if (this.symbolizer.getHeightAttribute() != null) {
            this.chkAttributeHeight.setSelected(true);
            this.heightAttributeChooser.setExpression(this.symbolizer.getHeightAttribute());
            this.heightAttributeChooser.setEnabled(true);
        } else {
            this.chkAttributeHeight.setSelected(false);
            this.heightAttributeChooser.setEnabled(false);
        }
        if (this.symbolizer.getPriority() != null) {
            this.attributePriorityCheckBox.setSelected(true);
            this.attributePriorityChooser.setExpression(this.symbolizer.getPriority());
            this.attributePriorityChooser.setEnabled(true);
        } else {
            this.attributePriorityCheckBox.setSelected(false);
            this.attributePriorityChooser.setEnabled(false);
        }
        boolean lineToLabel = this.symbolizer.getOption("lineToLabel") != null;
        this.lineFromGeomToLabelCheckBox.setSelected(lineToLabel);
        this.noLabelIfNoGeomCheckBox.setSelected(this.symbolizer.getOption("ignoreLabelIfGeometriesAreNull") != null);
        if (lineToLabel) {
            String strEndAnchorType;
            String strDash;
            String strWidht;
            String hexColor = this.symbolizer.getOption("lineToLabelColor");
            if (hexColor != null && !hexColor.isEmpty()) {
                Color fromHex = ColorUtil.fromHex(hexColor);
                this.labelLineColorEditor.setColor(fromHex);
            }
            if ((strWidht = this.symbolizer.getOption("lineToLabelWidth")) != null && !strWidht.isEmpty()) {
                LiteralExpressionImpl expression = null;
                try {
                    expression = new LiteralExpressionImpl(new Double(strWidht));
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                this.widthExpressionEditor.setExpression(expression);
            }
            if ((strDash = this.symbolizer.getOption("lineToLabelDash")) != null && !strDash.isEmpty()) {
                float[] dash = StringUtil.floatArrayFromString(strDash);
                this.labelLineDashEditor.setDashArray(dash);
            }
            if (!StringUtils.isEmpty((String)(strEndAnchorType = this.symbolizer.getOption("lineToLabelEndingAnchor")))) {
                TextSymbolizer.LineToLabelEndingAnchorOptions enumValue = Enum.valueOf(TextSymbolizer.LineToLabelEndingAnchorOptions.class, strEndAnchorType.toUpperCase());
                this.labelLineEndingAnchorTypeEditor.setLineToLabelEndingAnchorType(enumValue);
            }
        }
        this.refreshLineToLabelOptionsGUI(lineToLabel);
        String unitsOfMeasurement = this.symbolizer.getUnitsOfMeasurement();
        if (unitsOfMeasurement == null || unitsOfMeasurement.isEmpty()) {
            unitsOfMeasurement = "pixel";
        }
        this.units.setSelectedItem(unitsOfMeasurement);
        String repeatOptionValue = this.symbolizer.getOption("repeat");
        if (StringUtils.isNotEmpty((String)repeatOptionValue)) {
            int repeatValue = Integer.valueOf(repeatOptionValue);
            this.repeatDistanceExpressionEditor.setExpression(new LiteralExpressionImpl(repeatValue));
        }
        if (StringUtils.isNotEmpty((String)(autoWrapOptionValue = this.symbolizer.getOption("autoWrap")))) {
            int autowrapValue = Integer.valueOf(autoWrapOptionValue);
            this.autowrapWidthExpressionEditor.setExpression(new LiteralExpressionImpl(autowrapValue));
        }
        if (StringUtils.isNotEmpty((String)(spaceAroundOptionValue = this.symbolizer.getOption("spaceAround")))) {
            int spaceAroundValue = Integer.valueOf(spaceAroundOptionValue);
            this.spaceAroundExpressionEditor.setExpression(new LiteralExpressionImpl(spaceAroundValue));
        }
        if (StringUtils.isNotEmpty((String)(maxDisplacementOptionValue = this.symbolizer.getOption("maxDisplacement")))) {
            int maxDisplacementValue = Integer.valueOf(maxDisplacementOptionValue);
            this.maxDisplacementExpressionEditor.setExpression(new LiteralExpressionImpl(maxDisplacementValue));
        }
        if (StringUtils.isNotEmpty((String)(forceLeftToRightOptionValue = this.symbolizer.getOption("forceLeftToRight")))) {
            boolean forceLeftToRightValue = Boolean.valueOf(forceLeftToRightOptionValue);
            this.forceLeftToRightCheckBox.setSelected(forceLeftToRightValue);
        } else {
            this.forceLeftToRightCheckBox.setSelected(true);
        }
        Graphic shieldGraphic = this.symbolizer.getGraphic();
        if (shieldGraphic != null) {
            this.shieldGraphicCheckBox.setSelected(true);
            this.shieldGraphicEditor.setGraphic(shieldGraphic);
            this.shieldGraphicEditor.setEnabled(true);
            String strGraphicResize = this.symbolizer.getOption("graphic-resize");
            if (!StringUtils.isEmpty((String)strGraphicResize)) {
                TextSymbolizer.GraphicResize enumValue = Enum.valueOf(TextSymbolizer.GraphicResize.class, strGraphicResize.toUpperCase());
                this.shieldGraphicResizeEditor.setGraphicResize(enumValue);
            }
            this.shieldGraphicResizeEditor.setEnabled(true);
            String strGraphicMargin = this.symbolizer.getOption("graphic-margin");
            if (!StringUtils.isEmpty((String)strGraphicMargin)) {
                int graphicMarginValue = Integer.valueOf(strGraphicMargin);
                this.shieldGraphicMarginsEditor.setExpression(new LiteralExpressionImpl(graphicMarginValue));
            }
            this.shieldGraphicMarginsLabel.setEnabled(true);
            this.shieldGraphicMarginsEditor.setEnabled(true);
        } else {
            this.shieldGraphicCheckBox.setSelected(false);
            this.shieldGraphicEditor.setEnabled(false);
            this.shieldGraphicResizeEditor.setEnabled(false);
            this.shieldGraphicMarginsLabel.setEnabled(false);
            this.shieldGraphicMarginsEditor.setEnabled(false);
        }
        if (this.geomChooser.getGeomPropertiesCount() < 2) {
            this.lblGeometry.setVisible(false);
            this.geomChooser.setVisible(false);
            this.noLabelIfNoGeomCheckBox.setVisible(false);
            this.lineFromGeomToLabelCheckBox.setVisible(false);
            this.lineFromGeomOptionsLabel.setVisible(false);
            this.labelLineColorLabel.setVisible(false);
            this.labelLineColorEditor.setVisible(false);
            this.labelLineDashJLabel.setVisible(false);
            this.labelLineDashEditor.setVisible(false);
            this.widhtJLabel.setVisible(false);
            this.widthExpressionEditor.setVisible(false);
            this.labelLineEndingAnchorTypeEditor.setVisible(false);
        }
    }

    private void refreshLineToLabelOptionsGUI(boolean lineToLabel) {
        this.labelLineColorLabel.setEnabled(lineToLabel);
        this.labelLineColorEditor.setEnabled(lineToLabel);
        this.labelLineDashJLabel.setEnabled(lineToLabel);
        this.labelLineDashEditor.setEnabled(lineToLabel);
        this.widhtJLabel.setEnabled(lineToLabel);
        this.widthExpressionEditor.setEnabled(lineToLabel);
        this.labelLineEndingAnchorTypeEditor.setEnabled(lineToLabel);
    }
}

