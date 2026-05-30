/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
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
import org.apache.commons.lang.StringUtils;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.property.ExpressionEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.LinePlacement;
import org.saig.jump.lang.I18N;

public class DefaultLinePlacementEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private LinePlacement linePlacement;
    private JLabel lblOffset = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultLinePlacementEditor.perpendicular-offset"));
    private ExpressionEditor neOffset;
    private JLabel followLineLabel;
    private JCheckBox followLineCheckBox;
    private JLabel maximumDeltaAngleLabel;
    private ExpressionEditor maximumDeltaAngleEditor;
    private Map<String, String> labelPlacementOptions;

    public DefaultLinePlacementEditor(FeatureSchema featureType) {
        this(featureType, null);
    }

    public DefaultLinePlacementEditor(FeatureSchema featureType, LinePlacement linePlacement) {
        this.neOffset = propertyEditorFactory.createNumberEditor(new Double(0.0), new Double(-1.7976931348623157E308), new Double(Double.MAX_VALUE), new Double(1.0), featureType);
        this.followLineLabel = new JLabel(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultLinePlacementEditor.Follow-line"));
        this.followLineCheckBox = new JCheckBox();
        this.followLineCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = DefaultLinePlacementEditor.this.followLineCheckBox.isSelected();
                DefaultLinePlacementEditor.this.maximumDeltaAngleEditor.setEnabled(selected);
                DefaultLinePlacementEditor.this.maximumDeltaAngleLabel.setEnabled(selected);
                DefaultLinePlacementEditor.this.neOffset.setEnabled(!selected);
                DefaultLinePlacementEditor.this.lblOffset.setEnabled(!selected);
            }
        });
        this.maximumDeltaAngleLabel = new JLabel(String.valueOf(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultLinePlacementEditor.Maximum-angle")) + " (\u00ba)");
        this.maximumDeltaAngleEditor = propertyEditorFactory.createNumberEditor(new Double(22.5), new Double(0.0), new Double(360.0), new Double(0.1), featureType);
        this.maximumDeltaAngleEditor.setEnabled(false);
        this.maximumDeltaAngleLabel.setEnabled(false);
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.lblOffset, (JComponent)this.neOffset);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, this.followLineLabel, (JComponent)this.followLineCheckBox);
        FormUtils.addRowInGBL((JComponent)this, 2, 0, this.maximumDeltaAngleLabel, (JComponent)this.maximumDeltaAngleEditor);
        FormUtils.addFiller(this, 3, 0);
        this.setLinePlacement(linePlacement);
        if (this.labelPlacementOptions == null) {
            this.labelPlacementOptions = new HashMap<String, String>();
        }
    }

    public void setLinePlacement(LinePlacement linePlacement) {
        if (linePlacement == null) {
            linePlacement = styleBuilder.createLinePlacement(0.0);
        }
        this.linePlacement = linePlacement;
        this.neOffset.setExpression(linePlacement.getPerpendicularOffset());
    }

    public LinePlacement getLinePlacement() {
        this.linePlacement.setPerpendicularOffset(this.neOffset.getExpression());
        this.linePlacement.setAttributeRotation(null);
        return this.linePlacement;
    }

    private String getFollowLineOption() {
        return Boolean.valueOf(this.followLineCheckBox.isSelected()).toString();
    }

    private String getMaximumDeltaAngleOption() {
        Double maximumDeltaAngle = this.maximumDeltaAngleEditor.isEnabled() ? (Double)this.maximumDeltaAngleEditor.getExpression().getValue(null) : Double.valueOf(22.5);
        return maximumDeltaAngle.toString();
    }

    public void setLinePlacementOptions(Map<String, String> options) {
        String value;
        String value2;
        this.labelPlacementOptions = options;
        boolean followLine = false;
        if (this.labelPlacementOptions != null && this.labelPlacementOptions.containsKey("followLine") && StringUtils.isNotEmpty((String)(value2 = this.labelPlacementOptions.get("followLine")))) {
            followLine = value2.equalsIgnoreCase("yes") || value2.equalsIgnoreCase("true") || value2.equalsIgnoreCase("1");
        }
        double maximumDeltaAngle = 22.5;
        if (this.labelPlacementOptions != null && this.labelPlacementOptions.containsKey("maxAngleDelta") && StringUtils.isNotEmpty((String)(value = this.labelPlacementOptions.get("maxAngleDelta")))) {
            maximumDeltaAngle = Double.valueOf(value);
        }
        this.followLineCheckBox.setSelected(followLine);
        this.maximumDeltaAngleEditor.setEnabled(followLine);
        this.maximumDeltaAngleEditor.setExpression(new LiteralExpressionImpl(maximumDeltaAngle));
        this.maximumDeltaAngleLabel.setEnabled(followLine);
        this.neOffset.setEnabled(!followLine);
        this.lblOffset.setEnabled(!followLine);
    }

    public Map<String, String> getLinePlacementOptions() {
        this.labelPlacementOptions.put("followLine", this.getFollowLineOption());
        this.labelPlacementOptions.put("maxAngleDelta", this.getMaximumDeltaAngleOption());
        return this.labelPlacementOptions;
    }
}

