/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style.full;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.locale.TranslatableSelectionDialog;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.FilterEditor;
import org.saig.core.gui.swing.sldeditor.property.ScaleEditor;
import org.saig.core.gui.swing.sldeditor.style.full.BasicMetadataEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.Rule;
import org.saig.jump.lang.I18N;

public class RuleMetadataEditor
extends BasicMetadataEditor
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private JCheckBox chkMinScale;
    private JCheckBox chkElseFilter;
    private FilterEditor filterEditor;
    private JCheckBox chkFilter;
    private JLabel titleFilterScale;
    private ScaleEditor maxScaleEditor;
    private JCheckBox chkMaxScale;
    private ScaleEditor minScaleEditor;
    private JCheckBox chkEnabled;
    private Rule rule;

    public RuleMetadataEditor(Rule rule, FeatureSchema ft) {
        super(true);
        this.rule = rule;
        this.metadataLabel.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.rule-metadata"));
        this.chkEnabled = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.active-rule"));
        this.chkEnabled.setSelected(rule.isEnabled());
        this.titleFilterScale = FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.filter-and-scale"));
        this.chkFilter = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.filter"));
        this.filterEditor = propertyEditorFactory.createFilterEditor(ft);
        this.chkElseFilter = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.else-rule"));
        this.chkMinScale = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.min-scale-denominator"));
        this.minScaleEditor = propertyEditorFactory.createScaleEditor();
        this.chkMaxScale = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.max-scale-denominator"));
        this.maxScaleEditor = propertyEditorFactory.createScaleEditor();
        int lastRow = this.getLastRow();
        FormUtils.addRowInGBL(this, 0, 0, this.chkEnabled);
        FormUtils.addRowInGBL(this, lastRow + 1, 0, this.titleFilterScale);
        FormUtils.addRowInGBL((JComponent)this, lastRow + 2, 0, this.chkFilter, (JComponent)this.filterEditor);
        FormUtils.addRowInGBL(this, lastRow + 3, 0, this.chkElseFilter);
        FormUtils.addRowInGBL((JComponent)this, lastRow + 4, 0, this.chkMinScale, (JComponent)this.minScaleEditor);
        FormUtils.addRowInGBL((JComponent)this, lastRow + 5, 0, this.chkMaxScale, (JComponent)this.maxScaleEditor);
        this.txtName.setText(this.toText(rule.getName()));
        this.txtTitle.setText(this.toText(rule.getTitle()));
        this.txaAbstract.setText(this.toText(rule.getAbstract()));
        this.chkFilter.setSelected(rule.getFilter() != null);
        this.chkFilter.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                RuleMetadataEditor.this.filterEditor.setEnabled(RuleMetadataEditor.this.chkFilter.isSelected());
                if (RuleMetadataEditor.this.chkFilter.isSelected()) {
                    RuleMetadataEditor.this.chkElseFilter.setSelected(false);
                }
            }
        });
        this.chkElseFilter.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                if (RuleMetadataEditor.this.chkElseFilter.isSelected()) {
                    RuleMetadataEditor.this.chkFilter.setSelected(false);
                }
            }
        });
        this.chkMinScale.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                RuleMetadataEditor.this.minScaleEditor.setEnabled(RuleMetadataEditor.this.chkMinScale.isSelected());
            }
        });
        this.chkMaxScale.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                RuleMetadataEditor.this.maxScaleEditor.setEnabled(RuleMetadataEditor.this.chkMaxScale.isSelected());
            }
        });
        this.chkMinScale.setSelected(true);
        this.chkMaxScale.setSelected(true);
        if (rule.getFilter() != null) {
            this.filterEditor.setFilter(rule.getFilter());
        }
        this.chkElseFilter.setSelected(rule.isElseFilter());
        double max = rule.getMaxScaleDenominator();
        if (max != Double.POSITIVE_INFINITY && max != Double.MAX_VALUE && !Double.isNaN(max)) {
            this.chkMaxScale.setSelected(true);
            this.maxScaleEditor.setScaleDenominator(max);
        } else {
            this.chkMaxScale.setSelected(false);
        }
        double min = rule.getMinScaleDenominator();
        if (min > 0.0 && !Double.isNaN(min)) {
            this.chkMinScale.setSelected(true);
            this.minScaleEditor.setScaleDenominator(min);
        } else {
            this.chkMinScale.setSelected(false);
        }
    }

    public void fillRule(Rule rule) {
        rule.setName(this.txtName.getText());
        rule.setTitle(this.txtTitle.getText());
        rule.setAbstract(this.txaAbstract.getText());
        rule.setEnabled(this.chkEnabled.isSelected());
        if (this.chkFilter.isSelected()) {
            rule.setFilter(this.filterEditor.getFilter());
        } else {
            rule.setFilter(null);
        }
        rule.setElseFilter(this.chkElseFilter.isSelected());
        if (this.chkMinScale.isSelected()) {
            rule.setMinScaleDenominator(this.minScaleEditor.getScaleDenominator());
        } else {
            rule.setMinScaleDenominator(Double.NaN);
        }
        if (this.chkMaxScale.isSelected()) {
            rule.setMaxScaleDenominator(this.maxScaleEditor.getScaleDenominator());
        } else {
            rule.setMaxScaleDenominator(Double.NaN);
        }
    }

    @Override
    protected void langSelecButtonActionListener() {
        TranslatableSelectionDialog localeSelDia = new TranslatableSelectionDialog(JUMPWorkbench.getFrameInstance(), true, I18N.getMessage("org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor.Translations-for-the-rule-{0}-title", new Object[]{this.rule.getName()}), this.rule);
        if (localeSelDia.isOk()) {
            this.txtTitle.setText(this.rule.getTitle());
        }
    }
}

