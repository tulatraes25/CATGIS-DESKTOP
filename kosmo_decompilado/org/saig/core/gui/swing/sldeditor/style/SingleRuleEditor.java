/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.FilterEditor;
import org.saig.core.gui.swing.sldeditor.property.ScaleEditor;
import org.saig.core.gui.swing.sldeditor.style.StyleEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerListEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.jump.lang.I18N;

public class SingleRuleEditor
extends JPanel
implements SLDEditor,
StyleEditor {
    private static final long serialVersionUID = 1L;
    private boolean expertMode;
    Rule rule;
    Style style;
    FeatureSchema featureType;
    JTabbedPane tbpMain;
    JPanel pnlMetadata;
    JComponent titleGeneral;
    JComponent titleFilterScale;
    JComponent titleSymbolizers;
    JLabel lblName = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.name"));
    JTextField txtName = new JTextField();
    JLabel lblTitle = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.title"));
    JTextField txtTitle = new JTextField();
    JLabel lblAbstract = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.abstract"));
    JTextArea txaAbstract = new JTextArea();
    JCheckBox chkFilter = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.filter"));
    FilterEditor filterEditor;
    JCheckBox chkMinScale;
    ScaleEditor cmbMinScale;
    JCheckBox chkMaxScale;
    ScaleEditor cmbMaxScale;
    SymbolizerListEditor symbolizerListEditor;

    public SingleRuleEditor(FeatureSchema ft) {
        this(null, ft, true);
    }

    public SingleRuleEditor(FeatureSchema ft, boolean asSimpleStyleEditor) {
        this(null, ft, asSimpleStyleEditor);
    }

    public SingleRuleEditor(Rule r, FeatureSchema ft) {
        this(r, ft, true);
    }

    public SingleRuleEditor(FeatureSchema ft, Style s) {
        this(s.getFeatureTypeStyles()[0].getRules()[0], ft);
    }

    public SingleRuleEditor(Rule r, FeatureSchema ft, boolean asSimpleStyleEditor) {
        this.filterEditor = propertyEditorFactory.createFilterEditor(ft);
        this.chkMinScale = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.min-scale-denominator"));
        this.cmbMinScale = propertyEditorFactory.createScaleEditor();
        this.chkMaxScale = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.max-scale-denominator"));
        this.cmbMaxScale = propertyEditorFactory.createScaleEditor();
        this.symbolizerListEditor = new SymbolizerListEditor(ft);
        this.titleGeneral = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.metadata"));
        this.titleFilterScale = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.filter-and-scale"));
        this.titleSymbolizers = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.symbolizers"));
        this.chkFilter.setBorder(BorderFactory.createEmptyBorder());
        this.chkMinScale.setBorder(BorderFactory.createEmptyBorder());
        this.chkMaxScale.setBorder(BorderFactory.createEmptyBorder());
        this.chkFilter.setSelected(true);
        this.chkFilter.setSelected(true);
        this.chkMinScale.setSelected(true);
        this.chkMaxScale.setSelected(true);
        this.chkFilter.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                SingleRuleEditor.this.filterEditor.setEnabled(SingleRuleEditor.this.chkFilter.isSelected());
            }
        });
        this.chkMinScale.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                SingleRuleEditor.this.cmbMinScale.setEnabled(SingleRuleEditor.this.chkMinScale.isSelected());
            }
        });
        this.chkMaxScale.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                SingleRuleEditor.this.cmbMaxScale.setEnabled(SingleRuleEditor.this.chkMaxScale.isSelected());
            }
        });
        this.pnlMetadata = new JPanel();
        this.pnlMetadata.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this.pnlMetadata, 0, 0, this.titleGeneral);
        FormUtils.addRowInGBL((JComponent)this.pnlMetadata, 1, 0, this.lblName, (JComponent)this.txtName);
        FormUtils.addRowInGBL((JComponent)this.pnlMetadata, 2, 0, this.lblTitle, (JComponent)this.txtTitle);
        FormUtils.addRowInGBL((JComponent)this.pnlMetadata, 3, 0, (JComponent)this.lblAbstract, new JScrollPane(this.txaAbstract), 1.0, true);
        FormUtils.addRowInGBL(this.pnlMetadata, 4, 0, this.titleFilterScale);
        FormUtils.addRowInGBL((JComponent)this.pnlMetadata, 5, 0, this.chkFilter, (JComponent)this.filterEditor);
        FormUtils.addRowInGBL((JComponent)this.pnlMetadata, 7, 0, this.chkMinScale, (JComponent)this.cmbMinScale);
        FormUtils.addRowInGBL((JComponent)this.pnlMetadata, 8, 0, this.chkMaxScale, (JComponent)this.cmbMaxScale);
        this.tbpMain = new JTabbedPane();
        this.tbpMain.add(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.metadata"), this.pnlMetadata);
        this.tbpMain.add(I18N.getString("org.saig.core.gui.swing.sldeditor.style.SingleRuleEditor.symbolizers"), this.symbolizerListEditor);
        this.setLayout(new BorderLayout());
        this.add(this.tbpMain);
        this.featureType = ft;
        this.setRule(r);
    }

    private String toText(String s) {
        if (s != null) {
            return s;
        }
        return "";
    }

    public void setRule(Rule r) {
        double max;
        this.rule = r == null ? styleBuilder.createRule(SymbolizerUtils.getDefaultSymbolizer(this.featureType)) : r;
        this.txtName.setText(this.toText(this.rule.getName()));
        this.txtTitle.setText(this.toText(this.rule.getTitle()));
        this.txaAbstract.setText(this.toText(this.rule.getAbstract()));
        this.chkFilter.setSelected(this.rule.getFilter() != null);
        if (this.rule.getFilter() != null) {
            this.filterEditor.setFilter(this.rule.getFilter());
        }
        if ((max = this.rule.getMaxScaleDenominator()) != Double.POSITIVE_INFINITY && max != Double.MAX_VALUE && !Double.isNaN(max)) {
            this.chkMaxScale.setSelected(true);
            this.cmbMaxScale.setScaleDenominator(max);
        } else {
            this.chkMaxScale.setSelected(false);
        }
        double min = this.rule.getMinScaleDenominator();
        if (min > 0.0 && !Double.isNaN(min)) {
            this.chkMinScale.setSelected(true);
            this.cmbMinScale.setScaleDenominator(min);
        } else {
            this.chkMinScale.setSelected(false);
        }
        this.symbolizerListEditor.setSymbolizers(this.rule.getSymbolizers());
    }

    public Rule getRule() {
        this.rule.setName(this.txtName.getText());
        this.rule.setTitle(this.txtTitle.getText());
        this.rule.setAbstract(this.txaAbstract.getText());
        if (this.chkFilter.isSelected()) {
            this.rule.setFilter(this.filterEditor.getFilter());
        } else {
            this.rule.setFilter(null);
        }
        if (this.chkMinScale.isSelected()) {
            this.rule.setMinScaleDenominator(this.cmbMinScale.getScaleDenominator());
        } else {
            this.rule.setMinScaleDenominator(0.0);
        }
        if (this.chkMaxScale.isSelected()) {
            this.rule.setMaxScaleDenominator(this.cmbMaxScale.getScaleDenominator());
        } else {
            this.rule.setMaxScaleDenominator(Double.MAX_VALUE);
        }
        this.rule.setSymbolizers(this.symbolizerListEditor.getSymbolizers());
        return this.rule;
    }

    public boolean isExpertMode() {
        return this.expertMode;
    }

    public void setExpertMode(boolean expertMode) {
        this.expertMode = expertMode;
    }

    @Override
    public Style getStyle() {
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("", new Rule[]{this.getRule()});
        if (this.featureType != null) {
            fts.setFeatureTypeName("");
        }
        if (this.style == null) {
            this.style = styleBuilder.createStyle();
        }
        this.style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
        return this.style;
    }

    @Override
    public void setStyle(Style s) {
        this.style = s;
        if (s != null && s.getFeatureTypeStyles() != null && s.getFeatureTypeStyles().length > 0 && s.getFeatureTypeStyles()[0].getRules() != null && s.getFeatureTypeStyles()[0].getRules().length > 0) {
            this.setRule(s.getFeatureTypeStyles()[0].getRules()[0]);
        } else {
            this.setRule(null);
        }
    }

    @Override
    public boolean canEdit(Style s) {
        return SingleRuleEditor.canEditStyle(s);
    }

    public static boolean canEditStyle(Style s) {
        return s != null && s.getFeatureTypeStyles() != null && s.getFeatureTypeStyles().length == 1 && s.getFeatureTypeStyles()[0].getRules() != null && s.getFeatureTypeStyles()[0].getRules().length == 1;
    }
}

