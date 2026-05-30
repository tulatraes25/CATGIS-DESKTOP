/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.saig.core.filter.Expression;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.MathExpression;
import org.saig.core.filter.MathExpressionImpl;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.PointPlacement;
import org.saig.jump.lang.I18N;

public class DefaultPointPlacementEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    public static final String LEFT_KEY = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.Left");
    public static final String RIGHT_KEY = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.Right");
    public static final String CENTER_KEY = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.Center");
    public static final String TOP_KEY = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.Top");
    public static final String BOTTOM_KEY = I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.Bottom");
    private JLabel lblAnchorX;
    private JLabel lblAnchorY;
    private JLabel lblDispX;
    private JLabel lblDispY;
    private JLabel lblRotation;
    private JComboBox anchorXComboBox;
    private JComboBox anchorYComboBox;
    private ExpressionEditor neDispX;
    private ExpressionEditor neDispY;
    private ExpressionEditor rotationEditor;
    private JCheckBox chkRotationPositiveIsCCW;
    private PointPlacement pointPlacement;

    public DefaultPointPlacementEditor(FeatureSchema featureType) {
        this.init(featureType);
    }

    public DefaultPointPlacementEditor(FeatureSchema featureType, PointPlacement pp) {
        this.init(featureType);
        this.setPointPlacement(pp);
    }

    private void init(FeatureSchema featureType) {
        this.lblAnchorX = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.anchor-point-x"));
        this.lblAnchorY = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.anchor-point-y"));
        this.lblDispX = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.displacement-x"));
        this.lblDispY = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.displacement-y"));
        this.lblRotation = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor.rotation"));
        this.anchorXComboBox = new JComboBox<String>(new String[]{LEFT_KEY, CENTER_KEY, RIGHT_KEY});
        this.anchorYComboBox = new JComboBox<String>(new String[]{TOP_KEY, CENTER_KEY, BOTTOM_KEY});
        this.neDispX = propertyEditorFactory.createDoubleEditor(featureType);
        this.neDispY = propertyEditorFactory.createDoubleEditor(featureType);
        this.rotationEditor = propertyEditorFactory.createFixedOrByAttributeEditor(featureType, 0.0, -1000.0, 1000.0, 0.5, false);
        this.chkRotationPositiveIsCCW = new JCheckBox(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.Positive")) + " -> " + I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultGraphicEditor.Counterclockwise-direction"));
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.lblAnchorX, (JComponent)this.anchorXComboBox);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.lblAnchorY, (JComponent)this.anchorYComboBox);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, this.lblDispX, (JComponent)this.neDispX);
        FormUtils.addRowInGBL((JComponent)this, 3, 0, this.lblDispY, (JComponent)this.neDispY);
        FormUtils.addRowInGBL(this, 4, 0, this.lblRotation);
        FormUtils.addRowInGBL(this, 5, 0, this.rotationEditor);
        FormUtils.addRowInGBL(this, 6, 0, this.chkRotationPositiveIsCCW);
        FormUtils.addFiller(this, 7, 0);
    }

    public void setPointPlacement(PointPlacement pp) {
        if (pp == null) {
            pp = styleBuilder.createPointPlacement();
        }
        this.pointPlacement = pp;
        String anchorXOption = CENTER_KEY;
        String anchorYOption = CENTER_KEY;
        if (pp.getAnchorPoint() != null) {
            AnchorPoint ap = pp.getAnchorPoint();
            Expression anchorX = ap.getAnchorPointX();
            Expression anchorY = ap.getAnchorPointY();
            Number anchorXValue = (Number)anchorX.getValue(null);
            Number anchorYValue = (Number)anchorY.getValue(null);
            if (anchorXValue.doubleValue() == 0.0) {
                anchorXOption = LEFT_KEY;
            } else if (anchorXValue.doubleValue() == 1.0) {
                anchorXOption = RIGHT_KEY;
            }
            if (anchorYValue.doubleValue() == 0.0) {
                anchorYOption = BOTTOM_KEY;
            } else if (anchorYValue.doubleValue() == 1.0) {
                anchorYOption = TOP_KEY;
            }
        }
        this.anchorXComboBox.setSelectedItem(anchorXOption);
        this.anchorYComboBox.setSelectedItem(anchorYOption);
        if (pp.getDisplacement() == null) {
            this.neDispX.setExpression(styleBuilder.literalExpression(0.0));
            this.neDispY.setExpression(styleBuilder.literalExpression(0.0));
        } else {
            Displacement d = pp.getDisplacement();
            this.neDispX.setExpression(d.getDisplacementX());
            this.neDispY.setExpression(d.getDisplacementY());
        }
        Expression exp = pp.getRotation();
        if (exp != null) {
            if (exp instanceof MathExpression) {
                MathExpression mathExpr = (MathExpression)exp;
                exp = mathExpr.getLeftValue() instanceof LiteralExpression ? mathExpr.getRightValue() : mathExpr.getLeftValue();
                this.chkRotationPositiveIsCCW.setSelected(true);
            } else {
                this.chkRotationPositiveIsCCW.setSelected(false);
            }
            this.rotationEditor.setExpression(exp);
        }
    }

    public PointPlacement getPointPlacement() {
        Expression zero = styleBuilder.literalExpression(0);
        if (this.anchorXComboBox.getSelectedItem().equals(CENTER_KEY) && this.anchorYComboBox.getSelectedItem().equals(CENTER_KEY)) {
            this.pointPlacement.setAnchorPoint(null);
        } else {
            this.pointPlacement.setAnchorPoint(styleBuilder.createAnchorPoint(this.getAnchorXExpression(), this.getAnchorYExpression()));
        }
        if (this.neDispX.getExpression().equals(zero) && this.neDispY.getExpression().equals(zero)) {
            this.pointPlacement.setDisplacement(null);
        } else {
            this.pointPlacement.setDisplacement(styleBuilder.createDisplacement(this.neDispX.getExpression(), this.neDispY.getExpression()));
        }
        Expression rotationExpression = this.rotationEditor.getExpression();
        if (rotationExpression != null && this.chkRotationPositiveIsCCW.isSelected()) {
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
        this.pointPlacement.setRotation(rotationExpression);
        return this.pointPlacement;
    }

    private Expression getAnchorXExpression() {
        String selectedAnchorX = (String)this.anchorXComboBox.getSelectedItem();
        double anchorXValue = 0.5;
        if (selectedAnchorX.equals(LEFT_KEY)) {
            anchorXValue = 0.0;
        } else if (selectedAnchorX.equals(RIGHT_KEY)) {
            anchorXValue = 1.0;
        }
        return filterFactory.createLiteralExpression(anchorXValue);
    }

    private Expression getAnchorYExpression() {
        String selectedAnchorY = (String)this.anchorYComboBox.getSelectedItem();
        double anchorYValue = 0.5;
        if (selectedAnchorY.equals(TOP_KEY)) {
            anchorYValue = 1.0;
        } else if (selectedAnchorY.equals(BOTTOM_KEY)) {
            anchorYValue = 0.0;
        }
        return filterFactory.createLiteralExpression(anchorYValue);
    }
}

