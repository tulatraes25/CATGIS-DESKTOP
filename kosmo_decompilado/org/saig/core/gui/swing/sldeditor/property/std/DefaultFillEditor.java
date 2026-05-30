/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.core.styling.Gradient;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.GradientFillEditor;
import java.awt.Color;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.collections.MapUtils;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.FillEditor;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.IExpressionChangedListener;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultWKTFillPatternEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.ExternalGraphicImpl;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.jump.lang.I18N;

public class DefaultFillEditor
extends FillEditor
implements IExpressionChangedListener {
    private static final long serialVersionUID = 1L;
    private ExpressionEditor colorEditor;
    private ExpressionEditor backgroundEditor;
    private ExpressionEditor opacityEditor;
    private JLabel lblColor;
    private JLabel lblBackground;
    private JLabel lblOpacity;
    private JCheckBox chkFill;
    private JRadioButton simpleFillRadioButton;
    private JRadioButton graphicFillRadioButton;
    private JRadioButton wktPatternFillRadioButton;
    private JRadioButton gradientFillRadioButton;
    private GraphicEditor graphicFillEditor;
    private GraphicEditor wktPatternEditor;
    private GradientFillEditor gradientFillEditor;
    private Fill fill = null;
    private boolean isFillOptional;
    private FeatureSchema featureType;

    public DefaultFillEditor(FeatureSchema featureType) {
        this(featureType, styleBuilder.createFill(), true);
    }

    public DefaultFillEditor(FeatureSchema featureType, Fill fill) {
        this(featureType, fill, true);
    }

    public DefaultFillEditor(FeatureSchema featureType, Fill fill, boolean isFillOptional) {
        this.featureType = featureType;
        this.initComponents(false);
        this.isFillOptional = isFillOptional;
        this.chkFill.setVisible(isFillOptional);
        this.setFill(fill);
    }

    private void initComponents(boolean showBackground) {
        this.setLayout(new GridBagLayout());
        this.chkFill = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.use-fill"));
        this.lblColor = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.color"));
        this.colorEditor = propertyEditorFactory.createColorEditor(this.featureType);
        this.colorEditor.addExpressionChangedListener(this);
        this.lblBackground = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.background"));
        this.backgroundEditor = propertyEditorFactory.createColorEditor(this.featureType);
        this.lblOpacity = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.opacity"));
        this.opacityEditor = propertyEditorFactory.createOpacityEditor(this.featureType);
        this.opacityEditor.addExpressionChangedListener(this);
        this.simpleFillRadioButton = new JRadioButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.Basic"));
        this.simpleFillRadioButton.setBorder(BorderFactory.createEmptyBorder());
        this.graphicFillRadioButton = new JRadioButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.graphic-fill"));
        this.graphicFillRadioButton.setBorder(BorderFactory.createEmptyBorder());
        this.graphicFillRadioButton.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                DefaultFillEditor.this.graphicFillEditor.setEnabled(DefaultFillEditor.this.chkFill.isSelected() && DefaultFillEditor.this.graphicFillRadioButton.isSelected());
            }
        });
        this.graphicFillEditor = propertyEditorFactory.createGraphicFillEditor(this.featureType);
        this.wktPatternFillRadioButton = new JRadioButton(I18N.getString("workbench.ui.style.BasicStylePanel.fill-pattern"));
        this.wktPatternFillRadioButton.setBorder(BorderFactory.createEmptyBorder());
        this.wktPatternFillRadioButton.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                DefaultFillEditor.this.wktPatternEditor.setEnabled(DefaultFillEditor.this.chkFill.isSelected() && DefaultFillEditor.this.wktPatternFillRadioButton.isSelected());
            }
        });
        this.wktPatternEditor = propertyEditorFactory.createGraphicWKTFillPatternEditor(this.featureType);
        this.gradientFillRadioButton = new JRadioButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFillEditor.Gradient"));
        this.gradientFillRadioButton.setBorder(BorderFactory.createEmptyBorder());
        this.gradientFillRadioButton.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                DefaultFillEditor.this.gradientFillEditor.setEnabled(DefaultFillEditor.this.chkFill.isSelected() && DefaultFillEditor.this.gradientFillRadioButton.isSelected());
            }
        });
        this.gradientFillEditor = propertyEditorFactory.createGradientFillEditor();
        ButtonGroup fillButtonGroup = new ButtonGroup();
        fillButtonGroup.add(this.simpleFillRadioButton);
        fillButtonGroup.add(this.graphicFillRadioButton);
        fillButtonGroup.add(this.wktPatternFillRadioButton);
        fillButtonGroup.add(this.gradientFillRadioButton);
        this.simpleFillRadioButton.setSelected(true);
        this.chkFill.setBorder(BorderFactory.createEmptyBorder());
        this.chkFill.setBorderPaintedFlat(true);
        this.chkFill.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                boolean enabled = DefaultFillEditor.this.chkFill.isSelected();
                DefaultFillEditor.this.lblColor.setEnabled(enabled);
                DefaultFillEditor.this.colorEditor.setEnabled(enabled);
                DefaultFillEditor.this.lblBackground.setEnabled(enabled);
                DefaultFillEditor.this.backgroundEditor.setEnabled(enabled);
                DefaultFillEditor.this.lblOpacity.setEnabled(enabled);
                DefaultFillEditor.this.opacityEditor.setEnabled(enabled);
                DefaultFillEditor.this.graphicFillRadioButton.setEnabled(enabled);
                DefaultFillEditor.this.graphicFillEditor.setEnabled(enabled && DefaultFillEditor.this.graphicFillRadioButton.isSelected());
                DefaultFillEditor.this.wktPatternFillRadioButton.setEnabled(enabled);
                DefaultFillEditor.this.wktPatternEditor.setEnabled(enabled && DefaultFillEditor.this.wktPatternFillRadioButton.isSelected());
                DefaultFillEditor.this.gradientFillRadioButton.setEnabled(enabled);
                DefaultFillEditor.this.gradientFillEditor.setEnabled(enabled && DefaultFillEditor.this.gradientFillRadioButton.isSelected());
            }
        });
        this.chkFill.setSelected(true);
        int row = 0;
        FormUtils.addRowInGBL(this, row++, 0, this.chkFill);
        FormUtils.addRowInGBL(this, row++, 0, new JLabel());
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.lblColor, (JComponent)this.colorEditor);
        if (showBackground) {
            FormUtils.addRowInGBL((JComponent)this, row++, 0, this.lblBackground, (JComponent)this.backgroundEditor);
        }
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.lblOpacity, (JComponent)this.opacityEditor, false);
        FormUtils.addRowInGBL(this, row++, 0, this.simpleFillRadioButton);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.graphicFillRadioButton, (JComponent)this.graphicFillEditor);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.wktPatternFillRadioButton, (JComponent)this.wktPatternEditor);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, this.gradientFillRadioButton, (JComponent)this.gradientFillEditor);
        FormUtils.addFiller(this, row++, 0);
    }

    @Override
    public void setFill(Fill fill) {
        this.fill = fill;
        this.chkFill.setSelected(fill != null || !this.isFillOptional);
        if (fill != null) {
            this.colorEditor.setExpression(fill.getColor());
            this.backgroundEditor.setExpression(fill.getBackgroundColor());
            this.opacityEditor.setExpression(fill.getOpacity());
            Color currentColor = Color.decode((String)this.colorEditor.getExpression().getValue(null));
            float opacity = ((Number)this.opacityEditor.getExpression().getValue(null)).floatValue();
            currentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), (int)(opacity * 255.0f));
            ((DefaultWKTFillPatternEditor)this.wktPatternEditor).setColor(currentColor);
            if (fill.getGraphicFill() != null) {
                ExternalGraphicImpl extGraphic;
                Graphic graphicFill = fill.getGraphicFill();
                boolean isWKT = false;
                if (graphicFill.getExternalGraphics() != null && graphicFill.getExternalGraphics().length == 1 && MapUtils.isNotEmpty((extGraphic = (ExternalGraphicImpl)graphicFill.getExternalGraphics()[0]).getCustomProperties())) {
                    this.wktPatternFillRadioButton.setSelected(true);
                    this.wktPatternEditor.setGraphic(graphicFill);
                    isWKT = true;
                }
                if (!isWKT) {
                    this.graphicFillRadioButton.setSelected(true);
                    this.graphicFillEditor.setGraphic(fill.getGraphicFill());
                }
            } else {
                this.graphicFillRadioButton.setSelected(false);
            }
            if (fill.getGradientFill() != null) {
                Gradient gradientFill = fill.getGradientFill();
                this.gradientFillEditor.setGradient(gradientFill);
                this.gradientFillRadioButton.setSelected(true);
            }
        }
    }

    @Override
    public Fill getFill() {
        if (!this.chkFill.isSelected()) {
            return null;
        }
        if (this.fill == null) {
            this.fill = styleBuilder.createFill();
        }
        this.fill.setColor(this.colorEditor.getExpression());
        this.fill.setBackgroundColor(this.backgroundEditor.getExpression());
        this.fill.setOpacity(this.opacityEditor.getExpression());
        if (this.graphicFillRadioButton.isSelected()) {
            this.fill.setGraphicFill(this.graphicFillEditor.getGraphic());
        } else if (this.wktPatternFillRadioButton.isSelected()) {
            this.fill.setGraphicFill(this.wktPatternEditor.getGraphic());
        } else {
            this.fill.setGraphicFill(null);
        }
        if (this.gradientFillRadioButton.isSelected()) {
            this.fill.setGradientFill(this.gradientFillEditor.getGradient());
        } else {
            this.fill.setGradientFill(null);
        }
        return this.fill;
    }

    @Override
    public void expressionChanged(ExpressionEditor editor) {
        if (editor.equals(this.colorEditor) || editor.equals(this.opacityEditor)) {
            Color currentColor = Color.decode((String)this.colorEditor.getExpression().getValue(null));
            float opacity = ((Number)this.opacityEditor.getExpression().getValue(null)).floatValue();
            currentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), (int)(opacity * 255.0f));
            ((DefaultWKTFillPatternEditor)this.wktPatternEditor).setColor(currentColor);
        }
    }
}

