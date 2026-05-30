/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.MathExpressionImpl;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.FixedOrByAttributeExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.SymbolEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.ExternalGraphicImpl;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.MarkImpl;
import org.saig.core.styling.Symbol;
import org.saig.jump.lang.I18N;

public class DefaultGraphicEditor
extends GraphicEditor {
    private static final long serialVersionUID = 1L;
    private Graphic graphic;
    private JLabel lblSize;
    private JLabel lblRotation;
    private JLabel lblOpacity;
    private FixedOrByAttributeExpressionEditor sizeEditor;
    private ExpressionEditor rotationEditor;
    private JCheckBox chkRotationPositiveIsCCW;
    private ExpressionEditor opacityEditor;
    private SymbolEditor symbolEditor;

    public DefaultGraphicEditor(FeatureSchema featureType) {
        this(styleBuilder.createGraphic(), featureType);
    }

    public DefaultGraphicEditor(Graphic graphic, FeatureSchema featureType) {
        this.setLayout(new GridBagLayout());
        JPanel basicPanel = new JPanel(new GridBagLayout());
        JPanel symbolPanel = new JPanel(new GridBagLayout());
        this.lblSize = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.size"));
        this.lblRotation = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.rotation"));
        this.lblOpacity = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.opacity"));
        this.sizeEditor = propertyEditorFactory.createFixedOrByAttributeEditor(featureType, true);
        this.rotationEditor = propertyEditorFactory.createFixedOrByAttributeEditor(featureType, 0.0, -1000.0, 1000.0, 0.5, false);
        this.chkRotationPositiveIsCCW = new JCheckBox(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.Positive")) + " -> " + I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.Counterclockwise-direction"));
        this.opacityEditor = propertyEditorFactory.createOpacityEditor(featureType);
        this.symbolEditor = propertyEditorFactory.createSymbolEditor(featureType);
        FormUtils.addRowInGBL(basicPanel, 0, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.symbolizer.std.DefaultTextSymbolizerEditor.general")));
        FormUtils.addRowInGBL((JComponent)basicPanel, 1, 0, this.lblSize, (JComponent)this.sizeEditor, false);
        FormUtils.addRowInGBL((JComponent)basicPanel, 1, 30, this.lblRotation, (JComponent)this.rotationEditor, true);
        FormUtils.addRowInGBL((JComponent)basicPanel, 2, 0, this.lblOpacity, (JComponent)this.opacityEditor, false);
        FormUtils.addRowInGBL((JComponent)basicPanel, 2, 30, new JLabel(" "), (JComponent)this.chkRotationPositiveIsCCW, true);
        FormUtils.addRowInGBL(symbolPanel, 0, 0, FormUtils.getTitleLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.symbols")));
        FormUtils.addRowInGBL((JComponent)symbolPanel, 1, 0, (JComponent)this.symbolEditor, true, true);
        FormUtils.addRowInGBL(this, 0, 0, basicPanel);
        FormUtils.addRowInGBL(this, 1, 0, symbolPanel);
        FormUtils.addFiller(this, 2, 0);
        this.setGraphic(graphic);
    }

    @Override
    public void setGraphic(Graphic graphic) {
        this.sizeEditor.setExpression(graphic.getSize());
        Expression rotationExpression = graphic.getRotation();
        if (graphic.getRotation() != null && graphic.getRotation() instanceof MathExpression) {
            MathExpression mathExpr = (MathExpression)graphic.getRotation();
            rotationExpression = mathExpr.getLeftValue() instanceof LiteralExpression ? mathExpr.getRightValue() : mathExpr.getLeftValue();
            this.chkRotationPositiveIsCCW.setSelected(true);
        } else {
            this.chkRotationPositiveIsCCW.setSelected(false);
        }
        this.rotationEditor.setExpression(rotationExpression);
        this.opacityEditor.setExpression(graphic.getOpacity());
        Symbol[] symbols = graphic.getSymbols();
        if (symbols != null && symbols.length > 0) {
            this.symbolEditor.setSymbol(symbols[0]);
        }
        this.graphic = graphic;
    }

    @Override
    public Graphic getGraphic() {
        this.graphic.setMarks(new MarkImpl[0]);
        this.graphic.setExternalGraphics(new ExternalGraphicImpl[0]);
        this.graphic.setSize(this.sizeEditor.getExpression());
        Expression rotationExpression = this.rotationEditor.getExpression();
        if (this.chkRotationPositiveIsCCW.isSelected()) {
            try {
                MathExpressionImpl mathExpr = new MathExpressionImpl(107);
                mathExpr.addLeftValue(new LiteralExpressionImpl(-1));
                mathExpr.addRightValue(rotationExpression);
                rotationExpression = mathExpr;
            }
            catch (IllegalFilterException illegalFilterException) {
                // empty catch block
            }
        }
        this.graphic.setRotation(rotationExpression);
        this.graphic.setOpacity(this.opacityEditor.getExpression());
        this.graphic.setSymbols(new Symbol[]{this.symbolEditor.getSymbol()});
        return this.graphic;
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultGraphicEditor(null));
    }

    @Override
    public void setUnitsOfMeasurement(String unitsOfMeasurement) {
        this.sizeEditor.setUnitsOfMeasurement(unitsOfMeasurement);
    }

    @Override
    public String getUnitsOfMeasurement() {
        return this.sizeEditor.getUnitsOfMeasurement();
    }
}

