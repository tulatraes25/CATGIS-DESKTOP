/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.sldeditor.property.DashArrayEditor;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.FixedOrByAttributeExpressionEditor;
import org.saig.core.gui.swing.sldeditor.property.GraphicEditor;
import org.saig.core.gui.swing.sldeditor.property.StrokeEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.StringListEditor;
import org.saig.core.styling.Stroke;
import org.saig.jump.lang.I18N;

public class DefaultStrokeEditor
extends StrokeEditor {
    private static final long serialVersionUID = 1L;
    private ExpressionEditor colorEditor;
    private FixedOrByAttributeExpressionEditor widthEditor;
    private ExpressionEditor opacityEditor;
    private StringListEditor capEditor;
    private StringListEditor joinEditor;
    private GraphicEditor gfillEditor;
    private GraphicEditor gstrokeEditor;
    private JLabel lblColor;
    private JLabel lblOpacity;
    private JLabel lblWidth;
    private JLabel lblCap;
    private JLabel lblJoin;
    private JLabel lblDash;
    private JLabel lblDashOffset;
    private JLabel lblGraphicFill;
    private JLabel lblGraphicStroke;
    private JCheckBox chkStroke;
    private JCheckBox chkDash;
    private JCheckBox chkGraphicFill;
    private JCheckBox chkGraphicStroke;
    private DashArrayEditor dashEditor;
    private ExpressionEditor dashOffsetEditor;
    private Stroke stroke = null;
    private Map<String, String> right = new HashMap<String, String>();
    private Map<String, String> left = new HashMap<String, String>();

    public DefaultStrokeEditor(FeatureSchema featureType) {
        this(featureType, styleBuilder.createStroke());
    }

    public DefaultStrokeEditor(FeatureSchema featureType, Stroke stroke) {
        this.initializeRight();
        this.initializeLeft();
        this.initComponents(featureType);
        this.setStroke(stroke);
    }

    private void initializeRight() {
        this.right.put(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.butt"), "butt");
        this.right.put(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.round"), "round");
        this.right.put(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.square"), "square");
        this.right.put(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.miter"), "mitre");
        this.right.put(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.round"), "round");
        this.right.put(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.bevel"), "bevel");
    }

    private void initializeLeft() {
        this.left.put("butt", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.butt"));
        this.left.put("round", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.round"));
        this.left.put("square", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.square"));
        this.left.put("miter", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.miter"));
        this.left.put("mitre", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.miter"));
        this.left.put("round", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.round"));
        this.left.put("bevel", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.bevel"));
    }

    private void initComponents(FeatureSchema featureType) {
        this.setLayout(new GridBagLayout());
        this.chkStroke = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.use-stroke"));
        this.chkStroke.setSelected(true);
        this.chkStroke.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent event) {
                boolean enabled = DefaultStrokeEditor.this.chkStroke.isSelected();
                DefaultStrokeEditor.this.lblColor.setEnabled(enabled);
                DefaultStrokeEditor.this.colorEditor.setEnabled(enabled);
                DefaultStrokeEditor.this.lblWidth.setEnabled(enabled);
                DefaultStrokeEditor.this.widthEditor.setEnabled(enabled);
                DefaultStrokeEditor.this.lblOpacity.setEnabled(enabled);
                DefaultStrokeEditor.this.opacityEditor.setEnabled(enabled);
                DefaultStrokeEditor.this.lblCap.setEnabled(enabled);
                DefaultStrokeEditor.this.capEditor.setEnabled(enabled);
                DefaultStrokeEditor.this.lblJoin.setEnabled(enabled);
                DefaultStrokeEditor.this.joinEditor.setEnabled(enabled);
                DefaultStrokeEditor.this.chkDash.setEnabled(enabled);
                DefaultStrokeEditor.this.chkGraphicFill.setEnabled(enabled);
                DefaultStrokeEditor.this.chkGraphicStroke.setEnabled(enabled);
                DefaultStrokeEditor.this.updateDashComponentsState();
                DefaultStrokeEditor.this.updateGraphicFillState();
                DefaultStrokeEditor.this.updateGraphicStrokeState();
            }
        });
        this.lblColor = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.color"));
        this.colorEditor = propertyEditorFactory.createColorEditor(featureType);
        this.lblWidth = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.width"));
        this.widthEditor = propertyEditorFactory.createFixedOrByAttributeEditor(featureType, true);
        this.lblOpacity = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.opacity"));
        this.opacityEditor = propertyEditorFactory.createOpacityEditor(featureType);
        this.lblJoin = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.join"));
        this.joinEditor = new StringListEditor(new String[]{I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.miter"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.round"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.bevel")});
        this.lblCap = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.cap"));
        this.capEditor = new StringListEditor(new String[]{I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.butt"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.round"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.square")});
        this.lblDash = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.dash"));
        this.chkDash = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.dashed-line"));
        this.chkDash.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                DefaultStrokeEditor.this.updateDashComponentsState();
            }
        });
        this.dashEditor = propertyEditorFactory.createDashArrayEditor();
        this.lblDashOffset = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.dash-offset"));
        this.dashOffsetEditor = propertyEditorFactory.createIntSizeEditor(featureType);
        this.lblGraphicFill = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.graphic-fill"));
        this.chkGraphicFill = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.graphic-fill"));
        this.chkGraphicFill.setSelected(false);
        this.chkGraphicFill.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultStrokeEditor.this.updateGraphicFillState();
            }
        });
        this.gfillEditor = propertyEditorFactory.createGraphicFillEditor(featureType);
        this.lblGraphicStroke = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.graphic-stroke"));
        this.chkGraphicStroke = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultStrokeEditor.graphic-stroke"));
        this.chkGraphicStroke.setSelected(false);
        this.chkGraphicStroke.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultStrokeEditor.this.updateGraphicStrokeState();
            }
        });
        this.gstrokeEditor = propertyEditorFactory.createGraphicStrokeEditor(featureType);
        int row = 0;
        FormUtils.addRowInGBL(this, row++, 0, this.chkStroke);
        FormUtils.addRowInGBL((JComponent)this, row, 0, this.lblColor, (JComponent)this.colorEditor, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 30, this.lblCap, (JComponent)this.capEditor, true);
        FormUtils.addRowInGBL((JComponent)this, row, 0, this.lblOpacity, (JComponent)this.opacityEditor, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 30, this.lblJoin, (JComponent)this.joinEditor, true);
        FormUtils.addRowInGBL((JComponent)this, row, 0, this.lblWidth, (JComponent)this.widthEditor, false);
        FormUtils.addRowInGBL((JComponent)this, row++, 30, (JComponent)this.chkDash, (JComponent)this.dashEditor, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 30, this.lblDashOffset, (JComponent)this.dashOffsetEditor, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, (JComponent)this.chkGraphicFill, (JComponent)this.gfillEditor, true);
        FormUtils.addRowInGBL((JComponent)this, row++, 0, (JComponent)this.chkGraphicStroke, (JComponent)this.gstrokeEditor, true);
        FormUtils.addFiller(this, row++, 0);
        this.updateGraphicStrokeState();
        this.updateDashComponentsState();
        this.updateGraphicFillState();
    }

    protected void updateGraphicStrokeState() {
        boolean enabled = this.chkGraphicStroke.isSelected() && this.chkStroke.isSelected();
        this.lblGraphicStroke.setEnabled(enabled);
        this.gstrokeEditor.setEnabled(enabled);
    }

    protected void updateGraphicFillState() {
        boolean enabled = this.chkGraphicFill.isSelected() && this.chkStroke.isSelected();
        this.lblGraphicFill.setEnabled(enabled);
        this.gfillEditor.setEnabled(enabled);
    }

    protected void updateDashComponentsState() {
        boolean enabled = this.chkStroke.isSelected() && this.chkDash.isSelected();
        this.lblDash.setEnabled(enabled);
        this.dashEditor.setEnabled(enabled);
        this.lblDashOffset.setEnabled(enabled);
        this.dashOffsetEditor.setEnabled(enabled);
    }

    @Override
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        if (stroke == null) {
            this.chkStroke.setSelected(false);
            return;
        }
        this.chkStroke.setSelected(true);
        this.colorEditor.setExpression(stroke.getColor());
        this.widthEditor.setExpression(stroke.getWidth());
        this.opacityEditor.setExpression(stroke.getOpacity());
        String cap = this.left.get((String)stroke.getLineCap().getValue(null));
        this.capEditor.setExpression(styleBuilder.literalExpression(cap));
        String join = this.left.get((String)stroke.getLineJoin().getValue(null));
        this.joinEditor.setExpression(styleBuilder.literalExpression(join));
        float[] dashArray = stroke.getDashArray();
        if (dashArray == null || dashArray.length == 0) {
            this.chkDash.setSelected(false);
        } else {
            this.chkDash.setSelected(true);
            this.dashEditor.setDashArray(dashArray);
            this.dashOffsetEditor.setExpression(stroke.getDashOffset());
        }
        if (stroke.getGraphicFill() == null) {
            this.chkGraphicFill.setSelected(false);
            this.gfillEditor.setEnabled(false);
        } else {
            this.chkGraphicFill.setSelected(true);
            this.gfillEditor.setEnabled(true);
            this.gfillEditor.setGraphic(stroke.getGraphicFill());
        }
        if (stroke.getGraphicStroke() == null) {
            this.chkGraphicStroke.setSelected(false);
            this.gstrokeEditor.setEnabled(false);
        } else {
            this.chkGraphicStroke.setSelected(true);
            this.gstrokeEditor.setEnabled(true);
            this.gstrokeEditor.setGraphic(stroke.getGraphicStroke());
        }
        this.updateDashComponentsState();
        this.updateGraphicFillState();
        this.updateGraphicStrokeState();
    }

    @Override
    public Stroke getStroke() {
        if (!this.chkStroke.isSelected()) {
            return null;
        }
        if (this.stroke == null) {
            this.stroke = styleBuilder.createStroke();
        }
        this.stroke.setColor(this.colorEditor.getExpression());
        this.stroke.setWidth(this.widthEditor.getExpression());
        this.stroke.setOpacity(this.opacityEditor.getExpression());
        String cap = this.right.get((String)this.capEditor.getExpression().getValue(null));
        this.stroke.setLineCap(styleBuilder.literalExpression(cap));
        String join = this.right.get((String)this.joinEditor.getExpression().getValue(null));
        this.stroke.setLineJoin(styleBuilder.literalExpression(join));
        if (this.chkDash.isSelected()) {
            this.stroke.setDashArray(this.dashEditor.getDashArray());
            this.stroke.setDashOffset(this.dashOffsetEditor.getExpression());
        } else {
            this.stroke.setDashArray(null);
            this.stroke.setDashOffset(null);
        }
        if (this.chkGraphicFill.isSelected()) {
            this.stroke.setGraphicFill(this.gfillEditor.getGraphic());
        } else {
            this.stroke.setGraphicFill(null);
        }
        if (this.chkGraphicStroke.isSelected()) {
            this.stroke.setGraphicStroke(this.gstrokeEditor.getGraphic());
        } else {
            this.stroke.setGraphicStroke(null);
        }
        return this.stroke;
    }

    @Override
    public void setUnitsOfMeasurement(String unitsOfMeasurement) {
        this.widthEditor.setUnitsOfMeasurement(unitsOfMeasurement);
    }

    @Override
    public String getUnitsOfMeasurement() {
        return this.widthEditor.getUnitsOfMeasurement();
    }

    @Override
    public void allowDisable(boolean allow) {
        this.chkStroke.setVisible(allow);
    }
}

