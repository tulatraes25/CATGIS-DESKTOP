/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.saig.core.gui.swing.sldeditor.property.LabelPlacementEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultLinePlacementEditor;
import org.saig.core.gui.swing.sldeditor.property.std.DefaultPointPlacementEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LinePlacement;
import org.saig.core.styling.PointPlacement;
import org.saig.jump.lang.I18N;

public class DefaultLabelPlacementEditor
extends LabelPlacementEditor {
    private static final long serialVersionUID = 1L;
    LabelPlacement labelPlacement;
    Map<String, String> labelPlacementOptions;
    JCheckBox chkUseLabelPlacement;
    JComboBox cmbPlacementType;
    DefaultPointPlacementEditor ppEditor;
    DefaultLinePlacementEditor lpEditor;

    public DefaultLabelPlacementEditor(FeatureSchema featureType) {
        this(featureType, styleBuilder.createPointPlacement());
    }

    public DefaultLabelPlacementEditor(FeatureSchema featureType, LabelPlacement labelPlacement) {
        this.setLayout(new GridBagLayout());
        this.cmbPlacementType = new JComboBox<String>(new String[]{I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultLabelPlacementEditor.point-placement"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultLabelPlacementEditor.line-placement")});
        if (labelPlacement instanceof PointPlacement) {
            this.cmbPlacementType.setSelectedIndex(0);
        } else {
            this.cmbPlacementType.setSelectedIndex(1);
        }
        this.cmbPlacementType.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                DefaultLabelPlacementEditor.this.switchEditor();
            }
        });
        this.ppEditor = new DefaultPointPlacementEditor(featureType);
        this.lpEditor = new DefaultLinePlacementEditor(featureType);
        this.chkUseLabelPlacement = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultLabelPlacementEditor.label-placement"));
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.chkUseLabelPlacement, (JComponent)this.cmbPlacementType);
        this.setEditor(this.ppEditor);
        this.setLabelPlacement(labelPlacement);
    }

    private void switchEditor() {
        if (this.cmbPlacementType.getSelectedIndex() == 0) {
            this.setEditor(this.ppEditor);
        } else {
            this.setEditor(this.lpEditor);
        }
        Window parent = FormUtils.getWindowForComponent(this);
        parent.pack();
    }

    private void setEditor(JComponent component) {
        this.removeAll();
        FormUtils.addRowInGBL((JComponent)this, 0, 0, this.chkUseLabelPlacement, (JComponent)this.cmbPlacementType);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 0;
        gbc.gridheight = 0;
        gbc.fill = 1;
        this.add((Component)component, gbc);
    }

    @Override
    public void setLabelPlacement(LabelPlacement labelPlacement) {
        this.labelPlacement = labelPlacement;
        if (labelPlacement instanceof PointPlacement) {
            this.ppEditor.setPointPlacement((PointPlacement)labelPlacement);
            this.cmbPlacementType.setSelectedIndex(0);
        } else if (labelPlacement instanceof LinePlacement) {
            this.lpEditor.setLinePlacement((LinePlacement)labelPlacement);
            this.lpEditor.setLinePlacementOptions(this.labelPlacementOptions);
            this.cmbPlacementType.setSelectedIndex(1);
        }
    }

    @Override
    public LabelPlacement getLabelPlacement() {
        if (this.chkUseLabelPlacement.isSelected()) {
            if (this.cmbPlacementType.getSelectedIndex() == 0) {
                return this.ppEditor.getPointPlacement();
            }
            return this.lpEditor.getLinePlacement();
        }
        return null;
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultLabelPlacementEditor(null));
    }

    @Override
    public boolean isSelected() {
        return this.chkUseLabelPlacement.isSelected();
    }

    @Override
    public void setSelected(boolean selected) {
        this.chkUseLabelPlacement.setSelected(selected);
    }

    @Override
    public Map<String, String> getLabelPlacementOptions() {
        if (this.chkUseLabelPlacement.isSelected() && this.cmbPlacementType.getSelectedIndex() != 0) {
            this.labelPlacementOptions.putAll(this.lpEditor.getLinePlacementOptions());
        }
        return this.labelPlacementOptions;
    }

    @Override
    public void setLabelPlacementOptions(Map<String, String> options) {
        HashMap hashMap = this.labelPlacementOptions = options != null ? options : new HashMap();
        if (this.chkUseLabelPlacement.isSelected() && this.cmbPlacementType.getSelectedIndex() != 0) {
            this.lpEditor.setLinePlacementOptions(this.labelPlacementOptions);
        }
    }
}

